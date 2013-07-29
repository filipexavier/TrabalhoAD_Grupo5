package models;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import models.interfaces.Listener;
import models.utils.HighQualityRandom;
import controller.Simulator;

/**
 * 
 * Implementação da classe que representa uma estação TCP transmissora.
 * <p>
 * Sua função é simular uma estação transmissora de uma sessão TCP aberta, tendo, portanto, seu par receptor.
 * O transmissor irá simular o envio de pacotes, obedecendo todo o protocolo TCP.
 *
 */
public class Server implements Listener {
	/**
	 * Threshold com a qual o TCP está operando em bytes. Inicializado com o valor default 65535 bytes.
	 */
	private Float threshold = 65535f;
	
	/**
	 * Flag que indica se o servidor está operando em Fast Retransmit.
	 */
	private Boolean fastRetransmit;
	
	/**
	 * Armazena a id do server.
	 */
	private Integer serverId;
	
	/**
	 * Taxa com que um servidor envia pacotes para um roteador em bytes por segundo.
	 */
	private Integer broadcastRate;
	
	/**
	 * Armazena a informação do último ACK recebido.
	 */
	private Integer lastAck;
	
	/**
	 * Registra qual é o próximo pacote a ser enviado.
	 */
	private Integer nextPackage;
	
	/**
	 * Número total de pacotes que este servidor deve enviar.
	 */
	private Integer numOfPackagesToSend;

	/**
	 * Janela móvel pela qual a estação transmissora TCP controla quais bytes podem ser transmitidos.
	 * Esta janela inclui todos os bytes que, em um dado momento, podem estar em transmissão, pendente de recebimento de ACK.
	 * ACK é um pacote enviado pelo RxTCP que indica qual o próximo byte esperado para recebimento em ordem.
	 */
	private Float cwnd;
	
	/**
	 * Tempo real que levou para se receber o ACK do último pacote enviado.
	 */
	private Float realReturnTime;
	
	/**
	 * Tempo esperado para se receber um ACK de um pacote enviado.
	 * Recalculado a cada timeout.
	 */
	private Double expectedReturnTime;
	
	/**
	 * Desvio padrão do tempo esperado para se recebe um ACK de um pacote enviado.
	 */
	private Double deviationReturnTime;
	
	/**
	 * Grupo a qual pertence este servidor. 
	 */
	private ServerGroup group;
	
	/**
	 * Estação TCP receptora que irá receber os pacotes transmitidos por esta estação transmissora.
	 */
	private Receiver receiver;
	
	/**
	 * Mapa para armazenar os ACK's duplicado recebidos, 
	 * onde a chave do mapa é o ACK que está duplicado, e o valor indica quantos ACK's duplicados já foram recebidos.
	 */
	private HashMap<Integer, Integer> duplicatedAcks;
		
	/**
	 * Armazena os pacotes enviados que estão pendentes de recebimento de ACK.
	 */
	private List<Integer> sendedPackages;
	
	/**
	 * Conjunto dos pacotes que servidor recebeu fora de ordem.
	 */
	private Set<Integer> receivedAckPackages;
	
	/**
	 * Constrói um Server vazio.
	 */
	public Server() {}
	
	/**
	 * Constrói uma estação TCP transmissora, que irá escutar os eventos do tipo 
	 * <code>EventType.SEND_PACKAGE</code>, <code>EventType.SENDING_PACKAGE</code>,
	 * <code>EventType.TIME_OUT</code>, <code>EventType.SACK</code>.
	 * 
	 * @param rate taxa com que o servidor envia seus pacotes em bytes por segundo.
	 * @param group grupo a qual pertence o servidor.
	 * @param receiver estação transmissora correspondente.
	 * @param serverId número identificador do servidor. 
	 */
	public Server(Integer rate, ServerGroup group, Receiver receiver, Integer serverId) {
		this.serverId = serverId;
		this.broadcastRate = rate;
		this.group = group;
		this.receiver = receiver;
		fastRetransmit = false;
		
		duplicatedAcks = new HashMap<Integer, Integer>();
		sendedPackages = new ArrayList<Integer>();
		receivedAckPackages = new HashSet<Integer>();
		
		Simulator.registerListener(EventType.SEND_PACKAGE, this);
		Simulator.registerListener(EventType.SENDING_PACKAGE, this);
		Simulator.registerListener(EventType.TIME_OUT, this);
		Simulator.registerListener(EventType.SACK, this);
		
		lastAck = 0;
		realReturnTime = (float) 0;
		nextPackage = 0;

		numOfPackagesToSend = 1;
		
		deviationReturnTime = 0d;
		expectedReturnTime = 2d*group.getBroadcastDelay();
	}
	
	/**
	 * Inicializa a congestion window do servidor, seu gerador de número aleatórios,
	 * e lança um evento do tipo <code>EventType.SEND_PACKAGE</code>, o qual será responsável por iniciar
	 * o envio dos pacotes deste dervidor.
	 */
	public void startServer() {
		cwnd = new Float(Simulator.maximumSegmentSize);		
		
		HighQualityRandom randomGenerator = new HighQualityRandom();
		Simulator.shotEvent(EventType.SEND_PACKAGE, (float) randomGenerator.nextFloat()*100, null, this, null);
	}
	
	/**
	 * Implementação do método responsável por escutar os eventos.
	 * <p>
	 * Ele irá escutar os eventos do tipo  
	 * <code>EventType.SEND_PACKAGE</code>, <code>EventType.SENDING_PACKAGE</code>,
	 * <code>EventType.TIME_OUT</code> e <code>EventType.SACK</code>, 
	 * delegando cada tipo de evento a um método diferente, para ser tratado especificamente.
	 * 
	 * @param event evento que será escutado. Caso o tipo do evento não corresponda a nenhum tipo de evento tratado pelo servidor, nada será feito.
	 */
	@Override
	public void listen(Event event) {
		switch (event.getType()) {
		case SEND_PACKAGE:
			listenSendPackage(event);
			break;
		case SENDING_PACKAGE:
			listenSendingPackage(event);
			break;
		case TIME_OUT:
			listenTimeOut(event);
			break;
		case SACK:
			listenSack(event);
			break;
		default:
			break;
		}
	}
	
	/**
	 * Método que irá tratar os eventos do tipo <code>EventType.SEND_PACKAGE</code>, enviados pelo próprio servidor.
	 * <p>
	 * Quando o servidor receber esse tipo de evento, significa que é para ele enviar um pacote.
	 * O que está sendo representado neste caso, é o início de um serviço por parte do servidor, ou seja,
	 * significa o servidor está começando a servir um pacote.
	 * <p>
	 * Um cancelamento de evento do tipo <code>EventType.TIME_OUT</code> é efetuado, 
	 * caso o pacote que esteja sendo enviado, seja um reenvio de uma pacote.
	 * Um novo evento é então lançado, simulando o término do serviço. Tal evento ocorre em um determinado tempo,
	 * de acordo com a taxa de envio do servidor. Este tempo de serviço é calculado como:
	 * <p>
	 * <code>1000.0 * Simulator.maximumSegmentSize / broadcastRate</code>
	 * <p>
	 * que representa o tempo em milisegundos que o servidor leva para servir um pacote.
	 * <p>
	 * O servidor então atualiza o próximo pacote a ser enviado, 
	 * levando em consideração os candidatos a próximo pacote que ele já possa ter enviado.
	 * <p>
	 * 
	 * @param event evento do tipo <code>EventType.SEND_PACKAGE</code>, Caso o <code>Sender</code> do evento não tenha sido o próprio servidor, nada será feito.
	 */
	private void listenSendPackage(Event event) {
		if (((Server)event.getSender()).equals(this)) {			
			Float time = (float) (event.getTime() + (1000.0*Simulator.maximumSegmentSize)/broadcastRate);

			Simulator.cancelEvent(EventType.TIME_OUT, this, nextPackage);
			Simulator.shotEvent(EventType.SENDING_PACKAGE, time, time, this, nextPackage);
						
			sendedPackages.add(nextPackage);			
			nextPackage += Simulator.maximumSegmentSize;
			
			numOfPackagesToSend--;
			while(sendedPackages.contains(nextPackage)){
				nextPackage += Simulator.maximumSegmentSize;
			}
		}
	}
	
	/**
	 * Método que irá tratar os eventos do tipo <code>EventType.SENDING_PACKAGE</code>, enviados pelo próprio servidor.
	 * <p>
	 * Este tipo de evento significa o término de um serviço de um pacote pelo servidor.
	 * Ou seja, representa a saída do pacote do servidor, que irá fazer o percurso até o roteador.
	 * <p>
	 * O tempo de timeout para se obter a resposta da estação receptora é então calculada, 
	 * e um evento do tipo <code>EventType.TIME_OUT</code> é programado, 
	 * para o caso da respota não chegar dentro do tempo limite.
	 * <p>
	 * Um evento do tipo <code>EventType.PACKAGE_SENT</code> é então lançado, que será tratado pelo roteador.
	 * A chegada deste evento no roteador se dará no instante de tempo presente, somado o tempo de delay até chegar no roteador.
	 *  
	 * @param event evento do tipo <code>EventType.SENDING_PACKAGE</code>, Caso o <code>Sender</code> do evento não tenha sido o próprio servidor, nada será feito.
	 */
	private void listenSendingPackage(Event event) {
		if (((Server)event.getSender()).equals(this)) {
			
			double timeOutTime = getTimeOutTime(event);

			Simulator.shotEvent(EventType.TIME_OUT, (float) timeOutTime, event.getRtt(), this, event.getValue());

			Simulator.shotEvent(EventType.PACKAGE_SENT, event.getTime() + group.getBroadcastDelay(), event.getRtt(), this, event.getValue());
			
			if (numOfPackagesToSend > 0) {
				Simulator.shotEvent(EventType.SEND_PACKAGE, event.getTime(), null, this, null);
			}	
		}
	}

	/**
	 * Calcula o timeout para o envio de um pacote.
	 * <p>
	 * O tempo de timeout para se obter a resposta da estação receptora é dado segundo a seguinte fórmula: 
	 * <p>
	 * <code>
	 * Y = M - A
	 * D ← D + 0,25 * (|Y| - D)
	 * A ← A + 0,125 * Y
	 * RTO = A + 4D
	 * </code>
	 * <p>
	 * onde M é o tempo entre o envio do pacote pelo TxTCP e o recebimento do ACK correspondete, nosso <code>realReturnTime</code>,
	 * A é o valor estimado do RTT, nosso <code>exprectedReturnTime</code>,
	 * e D é o valor estimado do desvio médio, nosso <code>deviationReturnTime</code>.
	 * <p>
	 * este tempo é então somado ao instante de tempo em que o pacote está sendo enviando, representando o tempo em que o timeout será estourado.
	 *  
	 * @param event evento do tipo <code>EventType.SENDING_PACKAGE</code>.
	 */
	private double getTimeOutTime(Event event) {
		Double differenceBetweenRealAndExpectation = realReturnTime - expectedReturnTime;
		deviationReturnTime += 0.25*(Math.abs(differenceBetweenRealAndExpectation) - deviationReturnTime);
		expectedReturnTime += 0.125*differenceBetweenRealAndExpectation;
		double timeOutTime = expectedReturnTime + 4*deviationReturnTime;
		if (timeOutTime < group.getBroadcastDelay()) {
			throw new RuntimeException("Tempo do timeout calculado errado");
		}
		return timeOutTime + event.getTime();
	}
		
	/**
	 * Método que irá tratar os eventos do tipo <code>EventType.SACK</code>, 
	 * enviados pela estação receptora conectada a esta estação transmissora.
	 * <p>
	 * Este tipo de evento confirma o recebimento de um pacote por parte da estação receptora.
	 * <p>
	 * Implementa dois procedimentos básicos em TCP: Slow Start e Congestion Avoidance, 
	 * além disso, também usa o procedimento de Fast Retransmit, que permite disparar a retransmissão de pacotes antes da ocorrência de time-out. 
	 *  
	 * @param event evento do tipo <code>EventType.SACK</code>, Caso o <code>Sender</code> do evento não tenha sido o <code>Receptor</code> conectado a este <code>Server</code>, nada será feito.
	 */
	@SuppressWarnings("unchecked")
	private void listenSack(Event event) {
		if (((Receiver)event.getSender()) == getReceiver()) {
			
			List<Object> sack = (List<Object>) event.getValue(); // pega o sack

			Integer ackValue = (Integer) sack.get(0); // extrai o ACK
			Set<Integer> packageSequences = (Set<Integer>) sack.get(1); // extrai a sequência de pacotes recebidos corretamente
			
			if (lastAck > ackValue) {
				throw new RuntimeException("Ack diminuindo");
			}
			
			if (lastAck == ackValue) {
				/*
				 * caso seja um ACK duplicado, armazena as sequências recebidas corretamente.
				 * cancela os eventos de timeout dos pacotes recebidos corretamente, uma vez que eles foram enviados com sucesso. 
				 */
				for (Integer packageSequence : packageSequences) {
					receivedAckPackages.add(packageSequence);
					Simulator.cancelEvent(EventType.TIME_OUT, this, packageSequence);
				}
				/*
				 * atualiza o número de acks duplicados recebidos
				 */
				Integer acks = duplicatedAcks.get(ackValue);
				duplicatedAcks.put(ackValue, ++acks);				
				
				/*
				 * se estiver no procedimento Fast Retransmit
				 * Enquanto o ACK deste pacote retransmitido não chegar, cwnd é incrementada de 1 MSS a cada ACK recebido 
				 */
				if(fastRetransmit) {
					this.cwnd += Simulator.maximumSegmentSize;
				}
				
				if (acks == 3) {
					/*
					 * caso seja o terceiro ACK duplicado, entra em Fast Retransmit
					 * fazemos 
					 * <code>threshold = txwnd/2</code> 
					 * <code>cwnd=threshold + 3*MSS = txwnd/2 + 3*MSS</code> 
					 */
					duplicatedAcks.put(acks, null);
					threshold = (float) Math.floor(cwnd/2);
					cwnd = (float) (threshold + 3.0*Simulator.maximumSegmentSize);
					
					/*
					 * o pacote é então reenviado
					 */
					restartSend(acks, event.getTime());
					fastRetransmit = true;
				}
			} else {
				/*
				 * neste caso não é um ACK duplicado.
				 * então atualizamos o tempo real que o pacote levou para ser transmitido.
				 */
				calcRealTime(event);
				
				if(fastRetransmit) {
					/*
					 * se estiver em Fast Retransmit, 
					 * após a chegada do ACK do pacote retransmitido (possivelmente após RTT),
					 * fazemos <code>cwnd=threshold</code>, e entramos em congestion avoidance
					 */
					cwnd = threshold;
					fastRetransmit = false;
				}else if(cwnd < threshold) {
					/*
					 * Durante slow start, a chegada de um ACK permite que outro pacote seja transmitido, 
					 * aumentando o número de pacotes no intervalo RTT e conseqüentemente duplicando o
					 * tráfego ofertado a cada RTT
					 */
					this.cwnd += Simulator.maximumSegmentSize;
				} else{
					/*
					 * Entramos nesta fase quando cwnd=threshold. A partir deste instante, o aumento de cwnd
					 * a cada ACK recebido passa a ser de MSS/cwnd, ou seja, a janela irá aumentar de um MSS,
					 * após o recebimento de um número de ACKs igual ao valor de cwnd
					 */
					Integer numAcks = (int) Math.floor(this.cwnd/Simulator.maximumSegmentSize);
					this.cwnd += Simulator.maximumSegmentSize/numAcks;
				}
				
				duplicatedAcks.remove(lastAck); // remove o último ACK recebido da lista de duplicados, já que o pacote certo chegou
				duplicatedAcks.put(ackValue, 0); // adiciona o ACK na lista de duplicados, para manter um controle sobre os ACK's duplicados
				Simulator.cancelEvent(EventType.TIME_OUT, this, lastAck); // cancela o evento de timeout, já que o ACK chegou dentro do tempo limite
				sendedPackages.remove(lastAck); // remove o pacote da lista de enviados, já que o envio dele foi efetuado com sucesso

				lastAck = ackValue; // atualiza o último ACK recebido
				updateBroadcastWindow(ackValue); // atualiza a janela de transferência
				
				numOfPackagesToSend = getNumberOfPackagesToSend(); // atualiza o número de pacotes restantes a serem enviados
				Simulator.shotEvent(EventType.SEND_PACKAGE, event.getTime(), event.getRtt(), this, null); // lança o evento para enviar o próximo ACK da fila
			}
		}
	}

	/**
	 * Atualiza a congestion window com a qual a estação transmissora está operando.
	 * <p>
	 * Essa atualização é feita cada vez que o ACK esperado é recebido,
	 * deslocando a janela de transmissão, possibilitando o envio de novos pacotes.
	 * <p>
	 * Se durante a espera pelo próximo ACK a estação transmissora tiver recebido a confirmação
	 * de pacotes entregues corretamente, o deslocamento da janela se dará levando em consideração
	 * todos esses ACK's recebidos, e todas as variáveis que controlam essa lógica serão atualizadas,
	 * inclusive o próximo pacote a ser enviado.
	 *  
	 * @param ackValue ACK do último pacote recebido.
	 */
	private void updateBroadcastWindow(Integer ackValue) {
		nextPackage = ackValue;
		
		List<Integer> removeReceivedPackages = new ArrayList<Integer>();						
		for (Integer receivedPackage : receivedAckPackages) {
			if (receivedPackage < ackValue) {
				removeReceivedPackages.add(receivedPackage);
			}else if(receivedPackage == ackValue) {
				throw new RuntimeException("Ack já recebido");
			}
		}
		receivedAckPackages.removeAll(removeReceivedPackages);
		sendedPackages.removeAll(removeReceivedPackages);
		
		while(sendedPackages.contains(nextPackage)) {
			nextPackage += Simulator.maximumSegmentSize;
		}
	}

	/**
	 * Calcula o tempo de retorno real, no processo de envio e recebimento de confirmação de um pacote.
	 * 
	 * @param event evento do tipo <code>EventType.SACK</code>.
	 */
	private void calcRealTime(Event event) {
		realReturnTime = event.getTime() - event.getRtt();
		if (realReturnTime < group.getBroadcastDelay()) {
			throw new RuntimeException("Tempo real de retorno menor que o delay");
		}
	}

	/**
	 * Reenvia um pacote que tiver sofrido timeout.
	 * <p>
	 * Realiza-se uma verificação dos pacotes que já foram enviados e ainda não receberam
	 * a confirmação correspondente ao recebimento do pacote, para que esses também possam
	 * ser reenviados.
	 * <p>
	 * O próximo pacote a ser enviado então passa a ser o pacote que sofreu timeout,
	 * atualizando também o número de pacotes que ainda faltam ser enviados.
	 * <p>
	 * Por último, o evento correspondente ao envio desse pacote é lançado, para que se
	 * proceda com o envio do pacote.
	 * 
	 * @param nextPackage pacote a ser reenviado.
	 * @param time instante de tempo na simulação que o pacote será reenviado.
	 */
	private void restartSend(Integer nextPackage, Float time) {
		List<Integer> removedPackges = new ArrayList<Integer>();
		
		for (Integer packge : sendedPackages) {
			if (!receivedAckPackages.contains(sendedPackages)) {
				removedPackges.add(packge);

				Simulator.cancelEvent(EventType.TIME_OUT, this, packge);
				Simulator.cancelEvent(EventType.SEND_PACKAGE, this, packge);
			}
		}
		sendedPackages.removeAll(removedPackges);
		this.nextPackage = nextPackage;
		numOfPackagesToSend = getNumberOfPackagesToSend();
		
		Simulator.shotEvent(EventType.SEND_PACKAGE, time, null, this, null);
	}

	/**
	 * Retorna o número de pacotes que o servidor ainda tem que enviar.
	 * 
	 * @return número de pacotes para serem enviados.
	 */
	private int getNumberOfPackagesToSend() {
		return ((int) Math.floor(cwnd/Simulator.maximumSegmentSize)) - sendedPackages.size();
	}
	
	/**
	 * Método que irá tratar os eventos do tipo <code>EventType.TIME_OUT</code>, enviados pelo próprio servidor.
	 * <p>
	 * Este tipo de evento representa o timeout de um envio de um pacote. 
	 * Ou seja, o ACK esperado pelo envio de um pacote não chegou dentro do tempo limite, indicando um congestionamento severo.
	 * <p>
	 * Sendo assim, faz-se:
	 * <p>
	 * <code>
	 * threshold = txwnd/2 //(txwnd que valia no momento do time-out)
	 * cwnd = 1 MSS
	 * </code>
	 * <p>
	 * e entra-se em slow start.
	 * <p>
	 * O pacote então é reenviado pelo servidor.
	 * 
	 * @param event evento do tipo <code>EventType.TIME_OUT</code>, Caso o <code>Sender</code> do evento não tenha sido o próprio servidor, nada será feito.
	 */
	private void listenTimeOut(Event event) {
		if (((Server)event.getSender()) == this) {
			threshold = (float) Math.floor(cwnd/2);
			cwnd = new Float(Simulator.maximumSegmentSize);
			restartSend((Integer) event.getValue(), event.getTime());
		}
	}
		
	/**
	 * Taxa com que um servidor envia pacotes para um roteador em bytes por segundo.
	 * 
	 * @return taxa de saída dos pacotes do servidor.
	 */
	public Integer getBroadcastRate() {
		return broadcastRate;
	}

	/**
	 * Retorna o grupo no qual este servidor está inserido.
	 * 
	 * @return <code>ServerGroup</code> que caracteriza o tipo deste servidor.
	 */
	public ServerGroup getGroup() {
		return group;
	}

	/**
	 * Retorna a estação TCP transmissora ligada a este servidor.
	 * 
	 * @return <code>Receiver</code> conectado a esta estação transmissora.
	 */
	public Receiver getReceiver() {
		return receiver;
	}

	/**
	 * Retorna a janela móvel pela qual a estação transmissora TCP está controlando quais bytes estão sendo transmitidos.
	 *  
	 * @return float que representa a janela móvel de transferência.
	 */
	public Float getCwnd() {
		return cwnd;
	}

	/**
	 * Representação em <code>String</code> de um servidor. 
	 */
	@Override
	public String toString() {
		return "Servidor "+serverId;
	}
	
}
