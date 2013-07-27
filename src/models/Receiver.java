package models;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import models.interfaces.Listener;
import controller.Simulator;

public class Receiver implements Listener {

	private Server server;
	private Set<Integer> receivedPackages = null;
	private Integer nextAck;
	
	public Receiver() {
		Simulator.listeners.get(EventType.PACKAGE_DELIVERED).add(this);
		receivedPackages = new HashSet<Integer>();
		nextAck = 0;
	}

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

	private void sendSack(Event event) {
		List<Object> value = new ArrayList<Object>();
		value.add(nextAck);
		value.add(receivedPackages);
		Simulator.shotEvent(EventType.SACK, event.getTime(), this, value);
	}

	public Server getServer() {
		return server;
	}

	public void setServer(Server server) {
		this.server = server;
	}
}
