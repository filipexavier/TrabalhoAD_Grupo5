package models;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import models.interfaces.Listener;
import models.utils.ExponentialVariable;
import controller.Simulator;

public class Server implements Listener{
	private Integer threshold = 65535;
	private Integer broadcastRate;
	private ExponentialVariable rate;
	private ServerGroup group;
	private Receiver receiver;
	private Float cwnd, realReturnTime;
	
	private Integer lastAck, nextPackage, numOfPackagesToSend;
	private Map<Integer, Integer> duplicatedAcks;
		
	double expectedReturnTime;
	double deviationReturnTime;
	private HashMap<Integer, Float> rttPerPackage;
	
	
	List<Integer> sendedPackages;
	Set<Integer> receivedAckPackages;

	public Server(Float rate) {
		
	}
	
	public Server(Integer rate, ServerGroup group, Receiver receiver) {
		this.broadcastRate = rate;
		this.group = group;
		this.setReceiver(receiver);
		sendedPackages = new ArrayList<Integer>();
		receivedAckPackages = new HashSet<Integer>();
		rttPerPackage = new HashMap<Integer, Float>();
		
		Simulator.registerListener(EventType.SEND_PACKAGE, this);
		Simulator.registerListener(EventType.SENDING_PACKAGE, this);
		Simulator.registerListener(EventType.TIME_OUT, this);
		Simulator.registerListener(EventType.SACK, this);
		
		duplicatedAcks = new HashMap<Integer, Integer>();
		lastAck = 0;
		deviationReturnTime = 0;
		expectedReturnTime = 2*group.getBroadcastDelay();
		realReturnTime = (float) 0;
		numOfPackagesToSend = 1;
		nextPackage = 0;
	}
	
	public void startServer() {
		cwnd = new Float(Simulator.maximumSegmentSize);
		this.rate = new ExponentialVariable(broadcastRate/(1000.0*Simulator.maximumSegmentSize));
		Simulator.shotEvent(EventType.SEND_PACKAGE, (float) 0, this, null);
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
		if (((Server)event.getSender()).equals(this)) {
			
			Float time = (float) (event.getTime() + rate.getSample());

			Simulator.cancelEvent(EventType.TIME_OUT, this, nextPackage);
			Simulator.shotEvent(EventType.SENDING_PACKAGE, time, this, nextPackage);
			
			rttPerPackage.put((Integer) nextPackage, event.getTime());
			sendedPackages.add(nextPackage);			
			nextPackage += Simulator.maximumSegmentSize;
			
			numOfPackagesToSend--;
			while(sendedPackages.contains(nextPackage)){
				nextPackage += Simulator.maximumSegmentSize;
			}
		}
	}
		
	private void listenSendingPackage(Event event) {
		if (((Server)event.getSender()).equals(this)) {
			
			double timeOutTime = getTimeOutTime(event);
			Simulator.shotEvent(EventType.TIME_OUT, (float) timeOutTime, this, event.getValue());			
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
			
			if (lastAck > ackValue) {
				throw new RuntimeException("Ack diminuindo");
			}
			
			if (lastAck != ackValue) {	
				if (rttPerPackage.get(lastAck) != null) {
					realReturnTime = event.getTime() - rttPerPackage.get(lastAck);
					rttPerPackage.remove(lastAck);
				}
				
				if(cwnd < threshold) {
					this.cwnd += Simulator.maximumSegmentSize;
				} else{
					Integer numAcks = (int) Math.floor(this.cwnd/Simulator.maximumSegmentSize);
					this.cwnd += Simulator.maximumSegmentSize/numAcks;
				}
				
				duplicatedAcks.put(lastAck, null);
				duplicatedAcks.put(ackValue, 0);
				Simulator.cancelEvent(EventType.TIME_OUT, this, lastAck);
				sendedPackages.remove(lastAck);

				lastAck = ackValue;
				nextPackage = ackValue;
				
				List<Integer> removeReceivedPackages = new ArrayList<Integer>();				
				for (Integer receivedPackage : receivedAckPackages) {
					if (receivedPackage < ackValue) {
						removeReceivedPackages.add(receivedPackage);
					}else if(receivedPackage == ackValue) {
						throw new RuntimeException("Ack já recebido");
					}
				}
				receivedAckPackages.removeAll(removeReceivedPackages);
				sendedPackages.removeAll(removeReceivedPackages);
				
				while(sendedPackages.contains(nextPackage)) {
					nextPackage += Simulator.maximumSegmentSize;
				}
				
				numOfPackagesToSend = getNumberOfPackagesToSend();
				Simulator.shotEvent(EventType.SEND_PACKAGE, event.getTime(), this, null);
			} else {
				for (Integer packageSequence : packageSequences) {
					receivedAckPackages.add(packageSequence);
					Simulator.cancelEvent(EventType.TIME_OUT, this, packageSequence);
				}
				Integer acks = duplicatedAcks.get(ackValue);
				duplicatedAcks.put(acks, ++acks);				
				
				if(acks == 3) {
					duplicatedAcks.put(acks, null);
					threshold = (int) Math.floor(cwnd/2);
					cwnd = (float) (threshold + 3.0*Simulator.maximumSegmentSize);
					
					restartSend(acks, event.getTime());
				}
			}
		}
	}

	private void restartSend(Integer nextPackage, Float time) {
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
			threshold = (int) Math.floor(cwnd/2);
			cwnd = new Float(Simulator.maximumSegmentSize);
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
	
	public Float getCwnd() {
		return cwnd;
	}

	public void setCwnd(Float cwnd) {
		this.cwnd = cwnd;
	}


}
