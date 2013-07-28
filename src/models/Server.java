package models;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import models.interfaces.Listener;
import models.utils.HighQualityRandom;
import controller.Simulator;

/**
 * 
 * Implementação da classe que representa uma estação TCP transmissora.
 * <p>
 * Sua função é simular uma estação transmissora de uma sessão TCP aberta, tendo, portanto, seu par receptor.
 * O transmissor irá simular o envio de pacotes, obedecendo todo o protocolo TCP.
 *
 */
public class Server implements Listener {
	/**
	 * Threshold com a qual o TCP está operando. Inicializado com o valor default 65535 bytes.
	 */
	private Boolean fastRetransmit;
	private Integer serverId;
	private Float threshold = 65535f;
	private Integer broadcastRate;
	private Integer lastAck;
	private Integer nextPackage;
	private Integer numOfPackagesToSend;

	private Float cwnd;
	private Float realReturnTime;
	
	private Double expectedReturnTime;
	private Double deviationReturnTime;
	
	private ServerGroup group;
	private Receiver receiver;
	
	private HashMap<Integer, Integer> duplicatedAcks;
		
	private List<Integer> sendedPackages;
	private Set<Integer> receivedAckPackages;
	
	public Server() {}
	
	public Server(Integer rate, ServerGroup group, Receiver receiver, Integer serverId) {
		this.serverId = serverId;
		this.broadcastRate = rate;
		this.group = group;
		this.setReceiver(receiver);
		fastRetransmit = false;
		
		duplicatedAcks = new HashMap<Integer, Integer>();
		sendedPackages = new ArrayList<Integer>();
		receivedAckPackages = new HashSet<Integer>();
		
		Simulator.registerListener(EventType.SEND_PACKAGE, this);
		Simulator.registerListener(EventType.SENDING_PACKAGE, this);
		Simulator.registerListener(EventType.TIME_OUT, this);
		Simulator.registerListener(EventType.SACK, this);
		
		lastAck = 0;
		realReturnTime = (float) 0;
		nextPackage = 0;

		numOfPackagesToSend = 1;
		
		deviationReturnTime = 0d;
		expectedReturnTime = 2d*group.getBroadcastDelay();
	}
	
	public void startServer() {
		cwnd = new Float(Simulator.maximumSegmentSize);		
		
		HighQualityRandom randomGenerator = new HighQualityRandom();
		Simulator.shotEvent(EventType.SEND_PACKAGE, (float) randomGenerator.nextFloat()*100, null, this, null);
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
			Float time = (float) (event.getTime() + (1000.0*Simulator.maximumSegmentSize)/broadcastRate);

			Simulator.cancelEvent(EventType.TIME_OUT, this, nextPackage);
			Simulator.shotEvent(EventType.SENDING_PACKAGE, time, time, this, nextPackage);
						
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

			Simulator.shotEvent(EventType.TIME_OUT, (float) timeOutTime, event.getRtt(), this, event.getValue());

			Simulator.shotEvent(EventType.PACKAGE_SENT, event.getTime() + group.getBroadcastDelay(), event.getRtt(), this, event.getValue());
			
			if(numOfPackagesToSend > 0) {
				Simulator.shotEvent(EventType.SEND_PACKAGE, event.getTime(), null, this, null);
			}	
		}
	}

	private double getTimeOutTime(Event event) {
		Double differenceBetweenRealAndExpectation = realReturnTime - expectedReturnTime;
		deviationReturnTime += 0.25*(Math.abs(differenceBetweenRealAndExpectation) - deviationReturnTime);
		expectedReturnTime += 0.125*differenceBetweenRealAndExpectation;
		double timeOutTime = expectedReturnTime + 4*deviationReturnTime;
		if (timeOutTime < group.getBroadcastDelay()) {
			throw new RuntimeException("Tempo do timeout calculado errado");
		}
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
			
			if (lastAck == ackValue) {	
				for (Integer packageSequence : packageSequences) {
					receivedAckPackages.add(packageSequence);
					Simulator.cancelEvent(EventType.TIME_OUT, this, packageSequence);
				}
				Integer acks = duplicatedAcks.get(ackValue);
				duplicatedAcks.put(ackValue, ++acks);				
				
				if(fastRetransmit) {
					this.cwnd += Simulator.maximumSegmentSize;
				}
				
				if(acks == 3) {
					duplicatedAcks.put(acks, null);
					
					threshold = (float) Math.floor(cwnd/2);
					if(threshold < Simulator.maximumSegmentSize) {
						throw new RuntimeException("threshold < Simulator.maximumSegmentSize");
					}
					
					cwnd = (float) (threshold + 3.0*Simulator.maximumSegmentSize);
					
					restartSend(acks, event.getTime());
					fastRetransmit = true;
				}
			} else {
				calcRealTime(event);
				
				if(fastRetransmit) {
					cwnd = threshold;
					fastRetransmit = false;
				}else if(cwnd < threshold) {
					this.cwnd += Simulator.maximumSegmentSize;
				} else{
					Integer numAcks = (int) Math.floor(this.cwnd/Simulator.maximumSegmentSize);
					if(numAcks == 0) {
						throw new RuntimeException("cwnd = 0");
					}
					this.cwnd += Simulator.maximumSegmentSize/numAcks;
				}
				
				duplicatedAcks.remove(lastAck);
				duplicatedAcks.put(ackValue, 0);
				Simulator.cancelEvent(EventType.TIME_OUT, this, lastAck);
				sendedPackages.remove(lastAck);

				lastAck = ackValue;				
				updateBroadcastWindow(ackValue);
				
				numOfPackagesToSend = getNumberOfPackagesToSend();
				Simulator.shotEvent(EventType.SEND_PACKAGE, event.getTime(), event.getRtt(), this, null);
			}
		}
	}

	private void updateBroadcastWindow(Integer ackValue) {
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
	}

	private void calcRealTime(Event event) {
		realReturnTime = event.getTime() - event.getRtt();
		if (realReturnTime < group.getBroadcastDelay()) {
			throw new RuntimeException("Tempo real de retorno menor que o delay");
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
		
		Simulator.shotEvent(EventType.SEND_PACKAGE, time, null, this, null);
	}

	private int getNumberOfPackagesToSend() {
		return ((int) Math.floor(cwnd/Simulator.maximumSegmentSize)) - sendedPackages.size();
	}
	
	private void listenTimeOut(Event event) {
		if (((Server)event.getSender()) == this) {
			
			threshold = (float) Math.floor(cwnd/2);
			if(threshold < Simulator.maximumSegmentSize) {
				throw new RuntimeException("threshold < Simulator.maximumSegmentSize");
			}
			
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
	@Override
	public String toString() {
		return "Servidor "+serverId;
	}
	
}
