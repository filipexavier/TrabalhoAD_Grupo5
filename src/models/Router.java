package models;

import java.util.ArrayList;
import java.util.List;

import controller.Simulator;

import models.interfaces.Listener;

public class Router implements Listener {

	private Integer rate;
	private BottleNeck bottleNeckPolicy;
	private Integer bufferSize;
	
	private Boolean onService;
	private List<Event> buffer;


	public Router(int rate) {
		buffer = new ArrayList<Event>();
		onService = false;
		
		Simulator.registerListener(EventType.PACKAGE_SENT, this);
		Simulator.registerListener(EventType.DELIVER_PACKAGE, this);
		Simulator.registerListener(EventType.PACKAGE_DELIVERED, this);
		
		this.rate = rate;
	}
	
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

	private void listenPackgeSent(Event event) {
		if (onService) {
			if (buffer.size() < bufferSize) {
				buffer.add(event);
			}
		}else {
			onService = true;
			Simulator.shotEvent(EventType.DELIVER_PACKAGE, event.getTime(), this, event);
		}
	}
	
	private void listenDeliverPackage(Event event) {
		Integer time = event.getTime() + (1000/getRate());
		Event serverEvent = (Event) event.getValue();
		Simulator.shotEvent(EventType.PACKAGE_DELIVERED, time, serverEvent.getSender(), serverEvent.getValue());
	}
	
	private void listenPackageDelivered(Event event) {
		if (buffer.size() == 0) {
			onService = false;
		} else {
			Simulator.shotEvent(EventType.DELIVER_PACKAGE, event.getTime(), this, buffer.remove(0));
		}
	}

	public Integer getRate() {
		return rate;
	}
	
	public void setRate(Integer rate) {
		this.rate = rate;
	}

	public BottleNeck getBottleNeckPolicy() {
		return bottleNeckPolicy;
	}

	public void setBottleNeckPolicy(BottleNeck bottleNeckPolicy) {
		this.bottleNeckPolicy = bottleNeckPolicy;
	}

	public Integer getBufferSize() {
		return bufferSize;
	}

	public void setBufferSize(Integer bufferSize) {
		this.bufferSize = bufferSize;
	}
}
