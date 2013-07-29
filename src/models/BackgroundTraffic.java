package models;

import models.utils.ExponentialVariable;
import models.utils.GeometricVariable;
import controller.Simulator;

/**
 * 
 * Implementação da classe responsável por simular o tráfego de fundo.
 * <p>
 * Para minimizar o esforço de programação, além de evitar duplicação de código, 
 * esta classe extende a classe <code>Server</code>, pelo fato dela ser uma espécie de estação transmissora,
 * que só não estará recebendo nada. Só irá servir para congestionar o tráfego no <code>Router</code>.
 * 
 * @see Server
 *
 */
public class BackgroundTraffic extends Server{
	
	/**
	 * Armazena o próximo pacote a ser enviado.
	 */
	private Integer nextPackage;
	
	private ExponentialVariable sendVariable;
	private GeometricVariable gustLengthVariable;
	private Long avgGustLength;
	private Double avgGustInterval;
	
	/**
	 * Constrói um tráfego de fundo com a taxa fornecida. 
	 * <p>
	 * Este tráfego irá escutar os eventos do tipo <code>EventType.SEND_PACKAGE</code>.
	 * 
	 * @param rate taxa em que ocorre o tráfego de fundo.
	 */
	public BackgroundTraffic(Long avgGustLength, Double avgGustInterval) {
		Simulator.registerListener(EventType.SEND_PACKAGE, this);
		nextPackage = 0;
		this.avgGustLength = avgGustLength;
		this.avgGustInterval = avgGustInterval;
	}	
	
	/**
	 * Inicializa o tráfego de fundo, disparando um evento do tipo <code>EventType.SEND_PACKAGE</code>.
	 * <p>
	 * Este evento será tratado pelo próprio tráfego, que irá então enviar os seus pacotes.
	 */
	public void startBackgroundTraffic() {
		sendVariable = new ExponentialVariable(getAvgGustInterval()/1000000l);
		gustLengthVariable = new GeometricVariable(1d*getAvgGustLength());
		
		Long sampleValue =  (long) Math.ceil(sendVariable.getSample());
		Simulator.shotEvent(EventType.SEND_PACKAGE, sampleValue, sampleValue,this, null);
	}

	/**
	 * Escuta os eventos enviados pelo tráfego de fundo, cujo tipo corresponde a <code>EventType.SEND_PACKAGE</code>.
	 * <p>
	 * O evento simula o envio de vários pacotes pelo tráfego de fundo, que ocorrem a uma determinada taxa.
	 * Os pacotes são então enviados e um novo evento do tipo <code>EventType.SEND_PACKAGE</code> é lançando,
	 * representando o próximo envio de pacotes.
	 * 
	 * @param event evento do tipo <code>EventType.SEND_PACKAGE</code>. Caso o evento não tenha sido disparado pelo tráfego de fundo, nada será feito.
	 */
	@Override
	public void listen(Event event) {
		if (event.getSender().equals(this)) {
			Integer gustLength = (int) Math.ceil(gustLengthVariable.getSample());
			for (int i = 0; i < gustLength; i++) {
				Simulator.shotEvent(EventType.PACKAGE_SENT, event.getTime(), event.getRtt(), this, nextPackage);
				nextPackage += Simulator.maximumSegmentSize;
			}
			Long sampleValue = (long) Math.ceil(sendVariable.getSample());
			Simulator.shotEvent(EventType.SEND_PACKAGE, event.getTime() + sampleValue, null, this, null);
		}
	}

	public Long getAvgGustLength() {
		return avgGustLength;
	}

	public Double getAvgGustInterval() {
		return avgGustInterval;
	}
}
