package models;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import models.interfaces.Listener;
import controller.Simulator;

public class Router implements Listener {

	private Integer rate;
	private BottleNeck bottleNeckPolicy;
	private Integer bufferSize;

	private Boolean onService;
	private List<Event> buffer;
	private ExponentialVariable sendVariable;

	// RED values
	private Float wq = 0.002f;
	private Integer minth = 5;
	private Integer maxth = 15;
	private Float maxp = 0.02f;
	private Float avg = 0f;
	private Integer count = 0; // representa o número de pacotes não descartados desde o último descarte
	private Random rand;

	public Router(int rate) {
		buffer = new ArrayList<Event>();
		onService = false;

		Simulator.registerListener(EventType.PACKAGE_SENT, this);
		Simulator.registerListener(EventType.DELIVER_PACKAGE, this);
		Simulator.registerListener(EventType.PACKAGE_DELIVERED, this);

		this.rate = rate;
		
		rand = new Random(System.currentTimeMillis());
	}

	public void startRouter() {
		sendVariable = new ExponentialVariable(rate/(1000.0*Simulator.maximumSegmentSize));
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

//	private void listenPackgeSent(Event event) {
//		if (onService) {
//			if (buffer.size() < bufferSize) {
//				buffer.add(event);
//			}
//		}else {
//			onService = true;
//			Simulator.shotEvent(EventType.DELIVER_PACKAGE, event.getTime(), this, event);
//		}
//	}
	
	private void listenPackgeSent(Event event) {
		if (BottleNeck.FIFO.equals(bottleNeckPolicy)) {
			
			if (buffer.size() < bufferSize) {
				queuePackage(event);
			}
			
		} else if (BottleNeck.RED.equals(bottleNeckPolicy)) {
			
			if (onService)
				avg = (1 - wq)*avg + wq*buffer.size(); 
			else
				// FIXME: calcular o m, que esta sendo usado como 10
				avg = (float) (Math.pow((1 - wq), 10) * avg);
			
			if (avg < minth) {
				queuePackage(event);
			} else if (avg > maxth) {
				// pacote eh perdido
				count = 0;
			} else {
				Float pb = maxp*(avg - minth) / (maxth - minth);
				Float pa = pb / (1 - count*pb);
				
				
				if (rand.nextFloat() < pa) {
					queuePackage(event);
					count++;
				} else {
					// pacote eh perdido
					count = 0;
				}
			}
			
		} else {
			
			queuePackage(event);
			
		}
	}
	
	private void queuePackage(Event event) {
		if (onService) {
			buffer.add(event);
		}else {
			onService = true;
			Simulator.shotEvent(EventType.DELIVER_PACKAGE, event.getTime(), this, event);
		}
	}

	private void listenDeliverPackage(Event event) {
		Integer time = (int) (event.getTime() + sendVariable.getSample());
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
