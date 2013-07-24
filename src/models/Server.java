package models;

import models.interfaces.Listener;
import controller.Simulator;

public class Server implements Listener{
	public static Integer threshold = 65535;
	private Integer broadcastRate;
	private ServerGroup group;
	private Receiver receiver;
	private Integer cwnd, duplicatedAcks, lastAck, lastPackage, startedCountReturnTime, realReturnTime, numOfPackagesToSend;
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
		Simulator.registerListener(EventType.SACK, this);
		
		cwnd = Simulator.maximumSegmentSize;
		duplicatedAcks = 0;
		lastAck = -1;
		calcNewRTT = false;
		startedCountReturnTime = 0;
		realReturnTime = 0;
		deviationReturnTime = 0;
		expectedReturnTime = (1000/getBroadcastRate()) + group.getBroadcastDelay(); //TODO: + tempo medio da fila + serviço medio do roteador
		
		numOfPackagesToSend = 1;
		lastPackage = -1;
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
			Integer time = event.getTime() + (1000/getBroadcastRate());//TODO: MUDAR PARA VARIAVEL ALEATORIA
			
			Package package1 = new Package(lastPackage + 1, lastPackage + Simulator.maximumSegmentSize);
			lastPackage += Simulator.maximumSegmentSize;
			
			Simulator.shotEvent(EventType.SENDING_PACKAGE, time, this, package1);
		}
	}
		
	private void listenSendingPackage(Event event) {
		if (((Server)event.getSender()) == this) {
			Double differenceBetweenRealAndExpectation = realReturnTime - expectedReturnTime;
			deviationReturnTime += 0.25*(Math.abs(differenceBetweenRealAndExpectation) - deviationReturnTime);
			expectedReturnTime += 0.125*differenceBetweenRealAndExpectation;
			double timeOutTime = expectedReturnTime + 4*deviationReturnTime;
			
			if(numOfPackagesToSend == cwnd/Simulator.maximumSegmentSize) { // TODO Arrumar outro jeito de saber se este eh o primeiro pacote	
				//Coloca o pacote (event.getValue() ) no evento do time out
				Simulator.shotEvent(EventType.TIME_OUT, (int) timeOutTime, this, event.getValue());
				startedCountReturnTime = event.getTime(); //Esta levando em consideracao somente o primeiro pacote da janela.
				calcNewRTT = true;
			}
			
			Simulator.shotEvent(EventType.PACKAGE_SENT, event.getTime() + group.getBroadcastDelay(), this, event.getValue());
			numOfPackagesToSend--;
			
			if(numOfPackagesToSend > 0) {
				Simulator.shotEvent(EventType.SEND_PACKAGE, event.getTime(), this, null);
			}	
		}
	}
	
	private void listenTimeOut(Event event) {
		if (((Server)event.getSender()) == this) {
			threshold = cwnd/2;
			cwnd = Simulator.maximumSegmentSize;
			//TODO: ENTRA EM SLOW START
		}
	}
	
	private void listenSack(Event event) {
		if (((Receiver)event.getSender()) == getReceiver()) {
			Integer ackValue = (Integer) event.getValue();
			
			if (calcNewRTT) {
				realReturnTime = event.getTime() - startedCountReturnTime;
				calcNewRTT = false;
			}
			
			if (lastAck != ackValue) {
				if(cwnd < threshold){//TODO if slow start
					this.cwnd += Simulator.maximumSegmentSize;
				} else{
					this.cwnd += Simulator.maximumSegmentSize/this.cwnd;
				}
				duplicatedAcks = 0;
				lastAck = ackValue;
			} else {
				duplicatedAcks++;
				if(duplicatedAcks == 3) {
					threshold = cwnd/2;
					cwnd = threshold + 3*Simulator.maximumSegmentSize;
					// Enquanto o ACK deste pacote retransmitido não chegar, cwnd é incrementada de 1 MSS a cada ACK recebido
					// Após a chegada do ACK do pacote retransmitido (possivelmente após RTT), fazemos cwnd=threshold, e entramos em congestion avoidance
					//DELETAR TIMEOUT - Envia pacote do ack recebido
					Simulator.cancelEvent(event.getType(), event.getValue());
				}
			}
		}
		//TODO - DESLOCAR JANELA
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
