package models;

import java.util.ArrayList;
import java.util.List;

import models.interfaces.Listener;
import controller.Simulator;

public class Receiver implements Listener {

	private Server server;
	
	//TODO: LISTA DE PACOTES RECEBIDOS
	private List<Integer> receivedPackages = null;
	
	public Receiver() {
		Simulator.listeners.get(EventType.PACKAGE_SENT).add(this);
		receivedPackages = new ArrayList<Integer>();
	}

	@Override
	public void listen(Event event) {
		if( this.getServer() == (Server) event.getSender() ) {
			System.out.println("Recebeu pacote tempo " + event.getTime());
			receivedPackages.add((Integer)event.getValue());
			//ACK enviado contem o pacote recebido
			Simulator.shotEvent(EventType.SACK, event.getTime(), this, event.getValue());
		}
	}

	public Server getServer() {
		return server;
	}

	public void setServer(Server server) {
		this.server = server;
	}

	public List<Integer> getReceivedPackages() {
		return receivedPackages;
	}

	public void setReceivedPackages(List<Integer> receivedPackages) {
		this.receivedPackages = receivedPackages;
	}

}
