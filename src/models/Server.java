package models;

import models.interfaces.Listener;
import controller.Simulator;

public class Server implements Listener{

	private Integer broadcastRate;
	private ServerGroup group;
	private Receiver receiver;

	public Server(Integer rate, ServerGroup group, Receiver receiver) {
		this.broadcastRate = rate;
		this.group = group;
		this.setReceiver(receiver);
		
		Simulator.registerListener(EventType.SEND_PACKAGE, this);
		Simulator.registerListener(EventType.PACKAGE_SENT, this);
		Simulator.registerListener(EventType.TIME_OUT, this);
		Simulator.registerListener(EventType.ACK, this);
		
		Simulator.eventBuffer.add(new Event(EventType.PACKAGE_SENT, group.getBroadcastDelay(), this));
		System.out.println("Envia pacote em " + group.getBroadcastDelay());
		
	}
	
	@Override
	public void listen(Event event) {
		if( ( Receiver ) event.getSender() == this.receiver )
			System.out.println("Recebeu um ACK");
		
	}
		
	public Integer getBroadcastRate() {
		return broadcastRate;
	}

	public void setBroadcastRate(Integer broadcastRate) {
		this.broadcastRate = broadcastRate;
	}

	public ServerGroup getGroup() {
		return group;
	}

	public void setGroup(ServerGroup group) {
		this.group = group;
		group.getServers().add(this);
	}

	public Receiver getReceiver() {
		return receiver;
	}

	public void setReceiver(Receiver receiver) {
		this.receiver = receiver;
	}

}
