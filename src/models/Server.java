package models;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import models.interfaces.Listener;
import controller.Simulator;

public class Server implements Listener{
	public static Integer threshold = 65535;
	private Integer broadcastRate;
	private ServerGroup group;
	private Receiver receiver;
	private Integer cwnd, duplicatedAcks, nextAck, nextPackage, startedCountReturnTime, realReturnTime, numOfPackagesToSend;
	
	double expectedReturnTime;
	double deviationReturnTime;
	private Boolean calcNewRTT;
	
	List<Integer> sendedPackages;
	Set<Integer> receivedAckPackages;

	private ExponentialVariable serverServiceTime;

	public Server(Integer rate, ServerGroup group, Receiver receiver) {
		this.broadcastRate = rate;
		this.group = group;
		this.setReceiver(receiver);
		sendedPackages = new ArrayList<Integer>();
		receivedAckPackages = new HashSet<Integer>();
		
		Simulator.registerListener(EventType.SEND_PACKAGE, this);
		Simulator.registerListener(EventType.SENDING_PACKAGE, this);
		Simulator.registerListener(EventType.TIME_OUT, this);
		Simulator.registerListener(EventType.SACK, this);
		
		duplicatedAcks = 0;
		nextAck = 0;
		calcNewRTT = false;
		startedCountReturnTime = 0;
		realReturnTime = 0;
		deviationReturnTime = 0;
		expectedReturnTime = (1000/getBroadcastRate()) + group.getBroadcastDelay(); //TODO: + tempo medio da fila + serviço medio do roteador
		
		numOfPackagesToSend = 1;
		nextPackage = 0;
		serverServiceTime = new ExponentialVariable(rate);
	}
	
	public void startServer() {
		cwnd = Simulator.maximumSegmentSize;
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
		case SACK:
			listenSack(event);
			break;
		default:
			break;
		}
	}
	
	private void listenSendPackage(Event event) {
		if (((Server)event.getSender()) == this) {
			Integer time = (int) (event.getTime() + serverServiceTime.getSample());
			
			Simulator.cancelEvent(EventType.TIME_OUT, this, nextPackage);
			Simulator.shotEvent(EventType.SENDING_PACKAGE, time, this, nextPackage);
			
			sendedPackages.add(nextPackage);			
			nextPackage += Simulator.maximumSegmentSize;
			
			numOfPackagesToSend--;
			while(sendedPackages.contains(nextPackage)){
				nextPackage += Simulator.maximumSegmentSize;
			}
		}
	}
		
	private void listenSendingPackage(Event event) {
		if (((Server)event.getSender()) == this) {
			
			double timeOutTime = getTimeOutTime(event);
			Simulator.shotEvent(EventType.TIME_OUT, (int) timeOutTime, this, event.getValue());

			if(numOfPackagesToSend == (Math.floor(cwnd/Simulator.maximumSegmentSize) - 1)) {	
				startedCountReturnTime = event.getTime();
				calcNewRTT = true;
			}
			
			Simulator.shotEvent(EventType.PACKAGE_SENT, event.getTime() + group.getBroadcastDelay(), this, event.getValue());
			
			if(numOfPackagesToSend > 0) {
				Simulator.shotEvent(EventType.SEND_PACKAGE, event.getTime(), this, null);
			}	
		}
	}

	private double getTimeOutTime(Event event) {
		Double differenceBetweenRealAndExpectation = realReturnTime - expectedReturnTime;
		deviationReturnTime += 0.25*(Math.abs(differenceBetweenRealAndExpectation) - deviationReturnTime);
		expectedReturnTime += 0.125*differenceBetweenRealAndExpectation;
		double timeOutTime = expectedReturnTime + 4*deviationReturnTime;
		return timeOutTime + event.getTime();
	}
		
	@SuppressWarnings("unchecked")
	private void listenSack(Event event) {
		if (((Receiver)event.getSender()) == getReceiver()) {
			List<Object> sack = (List<Object>) event.getValue();
			
			Integer ackValue = (Integer) sack.get(0);
			Set<Integer> packageSequences = (Set<Integer>) sack.get(1);
			
			if (calcNewRTT) {
				realReturnTime = event.getTime() - startedCountReturnTime;
				calcNewRTT = false;
			}
			
			if (nextAck != ackValue) {				
				
				if(cwnd < threshold) {
					this.cwnd += Simulator.maximumSegmentSize;
				} else{
					this.cwnd += Simulator.maximumSegmentSize/this.cwnd;
				}
				
				duplicatedAcks = 0;
				Simulator.cancelEvent(EventType.TIME_OUT, this, nextAck);
				sendedPackages.remove(nextAck);

				nextAck = ackValue;
				
				while(receivedAckPackages.contains(nextPackage)) {
					sendedPackages.remove(nextPackage);
					receivedAckPackages.remove(nextPackage);
					nextPackage += Simulator.maximumSegmentSize;
				}
				nextPackage = nextAck;
	
				numOfPackagesToSend = getNumberOfPackagesToSend();
				Simulator.shotEvent(EventType.SEND_PACKAGE, event.getTime(), this, null);
			} else {
				receivedAckPackages.addAll(packageSequences);
				
				duplicatedAcks++;
				if(duplicatedAcks == 3) {
					threshold = cwnd/2;
					cwnd = threshold + 3*Simulator.maximumSegmentSize;
					
					restartSend(nextAck, event.getTime());
				}
			}
		}
	}

	private void restartSend(Integer nextPackage, Integer time) {
		List<Integer> removedPackges = new ArrayList<Integer>();
		for (Integer packge : sendedPackages) {
			if (!receivedAckPackages.contains(sendedPackages)) {
				removedPackges.add(packge);

				Simulator.cancelEvent(EventType.TIME_OUT, this, packge);
				Simulator.cancelEvent(EventType.SEND_PACKAGE, this, packge);
			}
		}
		sendedPackages.removeAll(removedPackges);
		this.nextPackage = nextPackage;
		numOfPackagesToSend = getNumberOfPackagesToSend();
		Simulator.shotEvent(EventType.SEND_PACKAGE, time, this, null);
	}

	private int getNumberOfPackagesToSend() {
		return ((int) Math.floor(cwnd/Simulator.maximumSegmentSize)) - sendedPackages.size();
	}
	
	private void listenTimeOut(Event event) {
		if (((Server)event.getSender()) == this) {
			threshold = cwnd/2;
			cwnd = Simulator.maximumSegmentSize;
			restartSend((Integer) event.getValue(), event.getTime());
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
	
	public Integer getCwnd() {
		return cwnd;
	}

	public void setCwnd(Integer cwnd) {
		this.cwnd = cwnd;
	}


}
