package models;

import controller.Simulator;
import models.interfaces.Listener;

public class Receiver implements Listener {

	private Server server;
	
	public Receiver() {
		Simulator.listeners.get(EventType.PACKAGE_SENT).add(this);
	}

	@Override
	public void listen(Event event) {
		if( this.getServer() == (Server) event.getSender() ) {
			System.out.println("Recebeu pacote tempo " + event.getTime());
			
			Simulator.shotEvent(EventType.ACK, event.getTime(), this, null);
		}
	}

	public Server getServer() {
		return server;
	}

	public void setServer(Server server) {
		this.server = server;
	}

}
