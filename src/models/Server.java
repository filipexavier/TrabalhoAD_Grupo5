package models;

import models.interfaces.Listener;
import controller.Simulator;

public class Server implements Listener{
	public static Integer threshold = 65535;
	private Integer broadcastRate;
	private ServerGroup group;
	private Receiver receiver;
	private Integer cwnd, duplicatedAcks, lastAck, lastPackge, startedCountReturnTime, realReturnTime, numOfPackgesToSend;
	double expectedReturnTime;
	double deviationReturnTime;
	private Boolean calcNewRTT;


	public Server(Integer rate, ServerGroup group, Receiver receiver) {
		this.broadcastRate = rate;
		this.group = group;
		this.setReceiver(receiver);
		
		Simulator.registerListener(EventType.SEND_PACKAGE, this);
		Simulator.registerListener(EventType.SENDING_PACKAGE, this);
		Simulator.registerListener(EventType.TIME_OUT, this);
		Simulator.registerListener(EventType.ACK, this);
		
		cwnd = Simulator.maximumSegmentSize;
		duplicatedAcks = 0;
		lastAck = -1;
		calcNewRTT = false;
		startedCountReturnTime = 0;
		realReturnTime = 0;
		deviationReturnTime = 0;
		expectedReturnTime = (1000/getBroadcastRate()) + group.getBroadcastDelay();
		
		numOfPackgesToSend = 1;
		lastPackge = -1;
		Simulator.shotEvent(EventType.SEND_PACKAGE, 0, this, null);
	}
	
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
		case ACK:
			listenAck(event);
			break;
		default:
			break;
		}
	}
	
	private void listenSendPackage(Event event) {
		if (((Server)event.getSender()) == this) {
			Integer time = event.getTime() + (1000/getBroadcastRate());
			
			Package package1 = new Package(lastPackge + 1, lastPackge + Simulator.maximumSegmentSize);
			lastPackge += Simulator.maximumSegmentSize;
			
			Simulator.shotEvent(EventType.SENDING_PACKAGE, time, this, package1);
		}
	}
		
	private void listenSendingPackage(Event event) {
		if (((Server)event.getSender()) == this) {
			Double differenceBetweenRealAndExpectation = realReturnTime - expectedReturnTime;
			deviationReturnTime += 0.25*(Math.abs(differenceBetweenRealAndExpectation) - deviationReturnTime);
			expectedReturnTime += 0.125*differenceBetweenRealAndExpectation;
			double timeOutTime = expectedReturnTime + 4*deviationReturnTime;
			
			if(numOfPackgesToSend == cwnd/Simulator.maximumSegmentSize) {			
				Simulator.shotEvent(EventType.TIME_OUT, (int) timeOutTime, this, null);
				startedCountReturnTime = event.getTime();
				calcNewRTT = true;
			}
			
			Simulator.shotEvent(EventType.PACKAGE_SENT, event.getTime() + group.getBroadcastDelay(), this, event.getValue());
			numOfPackgesToSend--;
			
			if(numOfPackgesToSend > 0) {
				Simulator.shotEvent(EventType.SEND_PACKAGE, event.getTime(), this, null);
			}	
		}
	}
	
	private void listenTimeOut(Event event) {
		if (((Server)event.getSender()) == this) {
			threshold = cwnd/2;
			cwnd = Simulator.maximumSegmentSize;
		}
	}
	
	private void listenAck(Event event) {
		if (((Receiver)event.getSender()) == getReceiver()) {
			Integer ackValue = (Integer) event.getValue();
			
			if (calcNewRTT) {
				realReturnTime = event.getTime() - startedCountReturnTime;
				calcNewRTT = false;
			}
			
			if (lastAck != ackValue) {
				if(cwnd < threshold){
					this.cwnd += Simulator.maximumSegmentSize;
				} else{
					this.cwnd += Simulator.maximumSegmentSize/this.cwnd;
				}
				lastAck = ackValue;
			} else {
				duplicatedAcks++;
				if(duplicatedAcks == 3) {
					threshold = cwnd/2;
					cwnd = threshold + 3*Simulator.maximumSegmentSize;
				}
			}
		}
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
