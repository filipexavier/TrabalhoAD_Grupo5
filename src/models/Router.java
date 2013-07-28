package models;

import java.util.ArrayList;
import java.util.List;

import models.interfaces.Listener;
import models.utils.ExponentialVariable;
import models.utils.HighQualityRandom;
import controller.Simulator;

/**
 * 
 * Implementação da classe que irá simular o roteador.
 * <p>
 * Sua função será representar o buffer por onde passam nossas sessões TCP,
 * sendo ele o gargalo na rota IP destas sessões.
 *
 */
public class Router implements Listener {

	/**
	 * Taxa com que a fila é esvaziada.
	 */
	private Integer rate;
	
	/**
	 * Política de fila que está sendo usada.
	 */
	private BottleNeck bottleNeckPolicy;
	
	/**
	 * Capacidade máxima do buffer, incluindo os pacotes que estão na fila e o que está sendo servido.
	 */
	private Integer bufferSize;

	/**
	 * Flag para indicar se o roteador está em serviço.
	 */
	private Boolean onService;
	
	/**
	 * Lista que representa a fila de espera.
	 */
	private List<Event> buffer;
	
	/**
	 * TODO: verificar se está sendo usada
	 */
	private ExponentialVariable sendVariable;

	/**
	 * Valor de wq usado pela política RED.
	 */
	private Float wq;
	
	/**
	 * Valor de minth usado pela política RED.
	 */
	private Integer minth;
	
	/**
	 * Valor de maxth usado pela política RED.
	 */
	private Integer maxth;
	
	/**
	 * Valor de maxp usado pela política RED.
	 */
	private Float maxp;
	
	/**
	 * Ocupação média da fila, usado somente pela política RED.
	 */
	private Float avg;
	
	/**
	 * Representa o número de pacotes não descartados desde o último descarte, utilizado pela política RED.
	 */
	private Integer count; 
	
	/**
	 * Gerador de número aleatórios.
	 */
	private HighQualityRandom rand;
	
	/**
	 * Armazena o tempo em que se iniciou o último período ocioso.
	 */
	private Float lastBusyPeriodTime;

	/**
	 * Constrói um roteador com a taxa fornecida.
	 * Ele irá escutar os eventos do tipo <code>EventType.PACKAGE_SENT</code>, <code>EventType.DELIVER_PACKAGE</code> e <code>EventType.PACKAGE_DELIVERED</code>.
	 * 
	 * @param rate taxa de saída do roteador. 
	 */
	public Router(int rate) {
		buffer = new ArrayList<Event>();
		onService = false;

		Simulator.registerListener(EventType.PACKAGE_SENT, this);
		Simulator.registerListener(EventType.DELIVER_PACKAGE, this);
		Simulator.registerListener(EventType.PACKAGE_DELIVERED, this);

		this.rate = rate;
		
		rand = new HighQualityRandom();
		wq = 0.002f;
		minth = 5;
		maxth = 15;
		maxp = 0.02f;
		avg = 0f;
		count = 0; 
		lastBusyPeriodTime =  0.0f;
	}

	// TODO: verificar a necessidade desse método
	public void startRouter() {
		sendVariable = new ExponentialVariable(rate/(1000.0*Simulator.maximumSegmentSize));
	}

	/**
	 * Implementação do método responsável por escutar os eventos.
	 * <p>
	 * Ele irá identificar o tipo de evento em questão, 
	 * e então delegar para o método responsável por cada tipo de evento.
	 * 
	 *  @param evento que será escutado.
	 */
	@Override
	public void listen(Event event) {
		switch (event.getType()) {
		case PACKAGE_SENT:
			listenPackgeSent(event);
			break;
		case DELIVER_PACKAGE:
			listenDeliverPackage(event);
			break;
		case PACKAGE_DELIVERED:
			listenPackageDelivered(event);
			break;
		default:
			break;
		}
	}
	
	/**
	 * Método que irá tratar os eventos do tipo <code>EventType.PACKAGE_SENT</code>;
	 * <p>
	 * Quando o roteador receber este tipo de evento, significa que o servidor transmitiu um pacote.
	 * Dessa forma, cabe ao roteador entregar este pacote.
	 * <p>
	 * Quando um pacote chega, ele será armazenado no buffer, de acordo com a política de fila a qual este roteador está sujeita.
	 * <p>
	 * Em qualquer política, se o roteador estiver em período ocioso, o pacote será imediatamente transmitido.
	 * Porém, se ele estiver em seu período ocupado, o pacote será alocado na fila de espera.
	 * Caso a política seja <code>BottleNeck.FIFO</code> o pacote será alocado no buffer somente se o buffer ainda estiver espaço disponível,
	 * sendo o pacote perdido caso o buffer esteja cheio.
	 * Se a política for <code>BottleNeck.RED</code>, o pacote só é alocado na fila de espera segundo as restrições da política.
	 * 
	 * @param event evento do tipo <code>EventType.PACKAGE_SENT</code>.
	 */
	private void listenPackgeSent(Event event) {
		if (BottleNeck.FIFO.equals(bottleNeckPolicy)) {
			
			if (buffer.size() < bufferSize) {
				queuePackage(event);
			}
			
		} else if (BottleNeck.RED.equals(bottleNeckPolicy)) {
			
			if (onService)
				avg = (1 - wq)*avg + wq*buffer.size(); 
			else
				avg = (float) (Math.pow((1 - wq), event.getTime() - lastBusyPeriodTime) * avg);
			
			if (buffer.size() >= bufferSize || avg > maxth) {
				// pacote é perdido
				count = 0;
			}else if (avg < minth) {
				queuePackage(event);
			} else {
				Float pb = maxp*(avg - minth) / (maxth - minth);
				Float pa = pb / (1 - count*pb);
				
				
				if (rand.nextFloat() < pa) {
					queuePackage(event);
					count++;
				} else {
					// pacote é perdido
					count = 0;
				}
			}
			
		} else {
			
			throw new RuntimeException("Nenhuma política de fila foi especificada!");
			
		}
	}
	
	/**
	 * Coloca um pacote na fila de espera, caso o roteador esteja em serviço, senão serve o pacote, fazendo o roteador entrar no período ocupado.
	 * <p>
	 * Caso o pacote seja servido, um evento do tipo <code>EventType.DELIVER_PACKAGE</code> será lançado pelo roteador, representando o envio de um pacote.
	 * 
	 * @param event evento do tipo <code>EventType.PACKAGE_SENT</code>.
	 */
	private void queuePackage(Event event) {
		if (onService) {
			buffer.add(event);
		}else {
			onService = true;
			Simulator.shotEvent(EventType.DELIVER_PACKAGE, event.getTime(), event.getRtt(), this, event);
		}
	}

	/**
	 * Trata os eventos cujo tipo são <code>EventType.DELIVER_PACKAGE</code>.
	 * <p>
	 * Este tipo de evento indica que o roteador está servindo um pacote, 
	 * portanto este método irá simular o evento de que o pacote foi enviado,
	 * disparando um evento do tipo <code>EventType.PACKAGE_DELIVERED</code>.
	 * <p>
	 * O evento disparado terá como <code>Sender</code> a estação trasmissora que está enviando o pacote,
	 * para que este evento possa ser tratado pela sua estação receptora correspondente.
	 * <p>
	 * TODO: explicar o cálculo do time 
	 * 
	 * @param event evento do tipo <code>EventType.DELIVER_PACKAGE</code>.
	 */
	private void listenDeliverPackage(Event event) {
		Float time = (float) (event.getTime() + (1000.0*Simulator.maximumSegmentSize)/rate);
		Event serverEvent = (Event) event.getValue();
		Simulator.shotEvent(EventType.PACKAGE_DELIVERED, time, serverEvent.getRtt(), serverEvent.getSender(), serverEvent.getValue());
	}

	/**
	 * Trata os eventos do tipo <code>EventType.PACKAGE_DELIVERED</code>.
	 * <p>
	 * O evento representa um pacote enviado com sucesso, indicando que o serviço foi concluído.
	 * <p>
	 * Sendo assim, caso não haja mais nenhum pacote para ser servido, o roteador entra em período ocioso,
	 * salvando o instante de tempo na simulação em que ele entrou em ócio.
	 * <p>
	 * Por outro lado, caso ainda tenham pacotes para serem servidos, ele então pega o primeiro da fila e começa a enviar,
	 * disparando um evento <code>EventType.DELIVER_PACKAGE</code>, representando o envio do pacote.
	 *  
	 * @param event evento do tipo <code>EventType.PACKAGE_DELIVERED</code>.
	 */
	private void listenPackageDelivered(Event event) {
		if (buffer.size() == 0) {
			onService = false;
			lastBusyPeriodTime = event.getTime();
		} else {
			Event nextEvent = buffer.remove(0);
			Simulator.shotEvent(EventType.DELIVER_PACKAGE, event.getTime(), nextEvent.getRtt(), this, nextEvent);
		}
	}

	/**
	 * Retorna a taxa com a qual o roteador é esvaziado.
	 * @return taxa de saída do roteador.
	 */
	public Integer getRate() {
		return rate;
	}

	/**
	 * Registra a taxa com a qual o roteador é esvaziado.
	 * @param rate taxa de saída do roteador.
	 */
	public void setRate(Integer rate) {
		this.rate = rate;
	}

	/**
	 * Retorna a política de manipulação da fila com a qual o roteador está operando.
	 * 
	 * @return política da fila.
	 */
	public BottleNeck getBottleNeckPolicy() {
		return bottleNeckPolicy;
	}

	/**
	 * Registra a política de manipulação da fila.
	 * @param bottleNeckPolicy política da fila.
	 */
	public void setBottleNeckPolicy(BottleNeck bottleNeckPolicy) {
		this.bottleNeckPolicy = bottleNeckPolicy;
	}

	/**
	 * Retorna a capacidade máxima do buffer, fila + serviço.
	 * @return capacidade máxima do buffer.
	 */
	public Integer getBufferSize() {
		return bufferSize;
	}

	/**
	 * Define a capacidade máxima do buffer, fila + serviço.
	 * @param bufferSize capacidade máxima do buffer.
	 */
	public void setBufferSize(Integer bufferSize) {
		this.bufferSize = bufferSize;
	}

	public List<Event> getBuffer() {
		return buffer;
	}

	public void setBuffer(List<Event> buffer) {
		this.buffer = buffer;
	}
}
