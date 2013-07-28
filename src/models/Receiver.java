package models;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import models.interfaces.Listener;
import controller.Simulator;

/**
 * 
 * Implementação da classe que representa uma estação receptora.
 * Sua função é simular uma estação recptora de uma sessão TCP aberta, tendo, portanto, seu par Transmissor.
 * O receptor será responsável por simular o recebimento de pacotes e então enviar o ACK correspondente.
 *
 */
public class Receiver implements Listener {

	/**
	 * Estação transmissora ligada a esta estação receptora.
	 */
	private Server server;
	
	/**
	 * Conjunto de pacotes recebidos.
	 */
	private Set<Integer> receivedPackages = null;
	
	/**
	 * Indica o próximo ACK a ser recebido.
	 */
	private Integer nextAck;
	
	/**
	 * Constrói uma estação receptora que irá escutar os eventos do tipo <code>EventType.PACKAGE_DELIVERED</code>.
	 * Por padrão, inicializa o próximo ACK a ser recebido como o ACK 0.
	 */
	public Receiver() {
		Simulator.listeners.get(EventType.PACKAGE_DELIVERED).add(this);
		receivedPackages = new HashSet<Integer>();
		nextAck = 0;
	}

	/**
	 * Implementação do método responsável por escutar os eventos.
	 * <p>
	 * Ele irá escutar os eventos do tipo <code>EventType.PACKAGE_DELIVERED</code>, enviados pela estação transmissora que está servindo ele.
	 * <p>
	 * Caso o pacote recebido corresponda ao próximo pacote que ele estava esperando, o receptor então atualiza o próximo ACK esperado,
	 * levando em consideração os pacotes que ele já tenha recebido, enquanto esperava pelo próximo pacote esperado.
	 * <p>
	 * No caso dele receber um pacote que não seja o esperado, ele então armazena o pacote no conjunto de pacotes recebidos.
	 * <p>
	 * Após essas verificações, o SACK correspondente ao recebimento do pacote é então enviado.
	 *  
	 * @param event evento que será escutado. Se a estação transmissora não corresponder ao Server desta estação, nada será feito.
	 */
	@Override
	public void listen(Event event) {
		if( this.getServer() == (Server) event.getSender() ) {
			Integer packge = (Integer)event.getValue();
			
			if (nextAck.equals(packge)) {
				nextAck += Simulator.maximumSegmentSize;
				while(receivedPackages.contains(nextAck)) {
					receivedPackages.remove(nextAck);
					nextAck += Simulator.maximumSegmentSize;
				}
			} else if(packge > nextAck) {				
				receivedPackages.add(packge);
			}
			
			sendSack(event);
		}
	}

	/**
	 * Envia o SACK correspondente ao recebimento de um pacote.
	 * <p>
	 * Dispara um evento que simula o envio de um SACK, informando o próximo pacote esperado, 
	 * assim como os pacotes posteriores que já foram recebidos.
	 *  
	 * @param event evento do recebimento de um pacote
	 */
	private void sendSack(Event event) {
		List<Object> value = new ArrayList<Object>();
		value.add(nextAck);
		value.add(receivedPackages);
		Simulator.shotEvent(EventType.SACK, event.getTime(), this, value);
	}

	/**
	 * Retorna a estação transmissora que está servindo esta estação receptora.
	 * 
	 * @return referência para a estação transmissora.
	 */
	public Server getServer() {
		return server;
	}

	/**
	 * Subistitui a estação transmissora que está servindo esta estação receptora.
	 * 
	 * @param server referência para a nova estação transmissora.
	 */
	public void setServer(Server server) {
		this.server = server;
	}
}
