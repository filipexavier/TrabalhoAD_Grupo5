package models;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;

import models.interfaces.Listener;
import controller.Simulator;

public class Server implements Listener{
	public static Integer threshold = 65535;
	private Integer broadcastRate;
	private ExponentialVariable rate;
	private ServerGroup group;
	private Receiver receiver;
	private Integer cwnd, duplicatedAcks, nextAck, nextPackage, realReturnTime, numOfPackagesToSend; 
	double expectedReturnTime;
	double deviationReturnTime;
	private HashMap<Integer, Integer> rttPerPackage;
	
	List<Integer> sendedPackages;
	Set<Integer> receivedAckPackages;


	public Server(Integer rate, ServerGroup group, Receiver receiver) {
		this.broadcastRate = rate;
		this.group = group;
		this.setReceiver(receiver);
		sendedPackages = new ArrayList<Integer>();
		receivedAckPackages = new HashSet<Integer>();
		rttPerPackage = new HashMap<Integer, Integer>();
		
		Simulator.registerListener(EventType.SEND_PACKAGE, this);
		Simulator.registerListener(EventType.SENDING_PACKAGE, this);
		Simulator.registerListener(EventType.TIME_OUT, this);
		Simulator.registerListener(EventType.SACK, this);
		
		duplicatedAcks = 0;
		nextAck = 0;
		deviationReturnTime = 0;
		expectedReturnTime = 2*group.getBroadcastDelay();
		realReturnTime = 0;
		numOfPackagesToSend = 1;
		nextPackage = 0;
	}
	
	public void startServer() {
		cwnd = Simulator.maximumSegmentSize;
		this.rate = new ExponentialVariable(broadcastRate/(1000.0*Simulator.maximumSegmentSize));
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
			Random rand = new Random(event.getTime());
			Integer time = (int) (event.getTime() + rate.generateSample(rand.nextInt()));
			
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

			rttPerPackage.put((Integer) event.getValue(), event.getTime());
			
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
		
	private void listenSack(Event event) {
		if (((Receiver)event.getSender()) == getReceiver()) {
			List<Object> sack = (List<Object>) event.getValue();
			
			Integer ackValue = (Integer) sack.get(0);
			Set<Integer> packageSequences = (Set<Integer>) sack.get(1);
			
			
			if (nextAck != ackValue) {	
				if (rttPerPackage.get(nextAck) != null) {
					realReturnTime = event.getTime() - rttPerPackage.get(nextAck);
					rttPerPackage.remove(nextAck);
				}
				
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
					duplicatedAcks = 0;
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
