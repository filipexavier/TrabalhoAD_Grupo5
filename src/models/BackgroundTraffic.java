package models;

import models.utils.ExponentialVariable;
import controller.Simulator;

public class BackgroundTraffic extends Server{
	
	private Float rate;
	private Integer nextPackage;
	private ExponentialVariable sendVariable;
	
	public BackgroundTraffic(Float rate) {
		super(rate);
		Simulator.registerListener(EventType.SEND_PACKAGE, this);
		this.rate = rate;
		nextPackage = 0;
	}	
	
	public void startBackgroundTraffic() {
		sendVariable = new ExponentialVariable(rate/(1000*Simulator.maximumSegmentSize));
		Simulator.shotEvent(EventType.SEND_PACKAGE, (float) 24, this, null);
	}

	@Override
	public void listen(Event event) {
		if (event.getSender().equals(this)) {
			for (int i = 0; i < 10; i++) {
				Simulator.shotEvent(EventType.PACKAGE_SENT, event.getTime(), this, nextPackage);
				nextPackage += Simulator.maximumSegmentSize;
			}
			Simulator.shotEvent(EventType.SEND_PACKAGE, event.getTime() + 24, this, null);
		}
	}

	public Float getRate() {
		return rate;
	}
}
