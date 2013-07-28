package controller;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import models.BackgroundTraffic;
import models.BottleNeck;
import models.Event;
import models.EventType;
import models.Receiver;
import models.Router;
import models.Server;
import models.ServerGroup;
import models.interfaces.Listener;
import models.utils.ConfidenceInterval;
import view.SimulatorView;


/**
 * 
 * 
 *
 */
public class Simulator {

	public static List<ServerGroup> serverGroups;
	public static Map< EventType, Set<Listener> > listeners;
	public static List<Event> eventBuffer;
	public static BackgroundTraffic backgroundTraffic;
	public static Integer maximumSegmentSize;
	public static Router router;
	public static final String FILENAME = "simulador.txt";
	
	public static Float time = (float) 0;
	public static Integer serverId;
	
	public static HashMap<Server, HashMap<Float, Integer>> series;
	public static Integer serverBroadcast   = 0;
	public static List<Double> serverAvarages = new ArrayList<Double>();
	public static Integer routerBroadcast   = 0;
	public static List<Double> routerAvarages = new ArrayList<Double>();
	public static Integer receiverBroadcast = 0;
	public static List<Double> receiverAvarages = new ArrayList<Double>();
	
	public static void registerListener(EventType eventType, Listener listener) {
		listeners.get(eventType).add(listener);
	}
	
	public static void shotEvent(EventType eventType, Float time, Object sender, Object value) {
		Event event = new Event(eventType, time, sender, value);
		eventBuffer.add(event);
		if (time > event.getTime()) {
			throw new RuntimeException("Evento anterior adicionou um evento no passado");
		}
	}
	
	/** Cancela(exclui) evento referente a este pacote */
	public static void cancelEvent(EventType eventType, Object sender, Object value){
		Iterator<Event> it = eventBuffer.iterator();
		while( it.hasNext()){
			Event event = it.next();
			if(event.getType().equals(eventType) && (event.getValue() == null || event.getValue().equals(value)) && event.getSender().equals(sender)){
				it.remove();
				break;
			}
		}
	}

	public static void startSimulator() throws IOException, InterruptedException {
		serverId = 1;
		eventBuffer = new ArrayList<Event>();
		listeners = new HashMap<EventType, Set<Listener>>();
		series = new HashMap<Server, HashMap<Float, Integer>>();
		
		for(EventType type: EventType.values()){
			listeners.put( type, new HashSet<Listener>() );
		}
		readInputFile();		
		printInputData();

		Event event = null;
		Integer simulationTime = Integer.parseInt(SimulatorView.getInstance().getSimulationTimeTextField().getText());
		long realTime = System.currentTimeMillis();
		while(eventBuffer.size() > 0 && (event == null || event.getTime() < simulationTime)){
			event = eventBuffer.remove(0);
			time = event.getTime();
			for(Listener listener: listeners.get(event.getType())){
				listener.listen(event);
			}
			time = event.getTime();
			
			switch (event.getType()) {
			case SENDING_PACKAGE:
				serverBroadcast++;
				break;
			case DELIVER_PACKAGE:
				routerBroadcast++;
				break;
			case SACK:
				receiverBroadcast++;
				break;
			default:
				break;
			}		
			sortEventBuffer(time);
		}
		updateChart();
		updateIntervalConfidence();
		
		clearSimulator();
		System.out.println(System.currentTimeMillis() - realTime);
		realTime = System.currentTimeMillis();
	}

	private static void updateChart() {
		SimulatorView.getInstance().updateChart(series);
	}

	private static void updateIntervalConfidence() {
		serverAvarages.add(new Double(SimulatorView.getInstance().getServerBroadcast().getText()));
		receiverAvarages.add(new Double(SimulatorView.getInstance().getReceiverBroadcast().getText()));
		routerAvarages.add(new Double(SimulatorView.getInstance().getRouteBroadcast().getText()));
		
		Integer numberOsRuns = serverAvarages.size();
		
		SimulatorView.getInstance().getNumOfRuns().setText(numberOsRuns.toString());
		
		if (numberOsRuns >= 30) {
			SimulatorView.getInstance().getServerCI().setText(ConfidenceInterval.getConfidenceInterval(serverAvarages));
			SimulatorView.getInstance().getReceiverCI().setText(ConfidenceInterval.getConfidenceInterval(receiverAvarages));
			SimulatorView.getInstance().getRouterCI().setText(ConfidenceInterval.getConfidenceInterval(routerAvarages));
		}
	}

	private static void clearSimulator() {
		serverGroups = null;
		listeners = null;
		eventBuffer = null;
		backgroundTraffic = null;
		maximumSegmentSize = null;
		router = null;
		time = (float) 0;
		serverBroadcast   = 0;
		routerBroadcast   = 0;
		receiverBroadcast = 0;
	}

	private static void sortEventBuffer(Float time) {
		Collections.sort(eventBuffer);
		updatePlot(time);
		if (time > 0) {
			SimulatorView.getInstance().getServerBroadcast().setText(new Float(serverBroadcast*1000/time).toString());
			SimulatorView.getInstance().getRouteBroadcast().setText(new Float(routerBroadcast*1000/time).toString());
			SimulatorView.getInstance().getReceiverBroadcast().setText(new Float(receiverBroadcast*1000/time).toString());
		}
	}

	private static void updatePlot(Float time) {
		Integer  value = 0;
		
		for (ServerGroup serverGroup : serverGroups) {
			for (Server server : serverGroup.getServers()) {
				value += (int) Math.floor(server.getCwnd());
				value /= Simulator.maximumSegmentSize;
				if(series.get(server) == null) {
					series.put(server, new HashMap<Float, Integer>());
				}
				series.get(server).put(time, value);
				value = 0;
			}
		}			
	}

	private static void printInputData() {
		System.out.println("	================ LOG DADOS DO ARQUIVO =================");
		System.out.println("	=======================================================\n");
		System.out.println("		Tráfego de fundo: " + 8*backgroundTraffic.getRate() + " bps");
		System.out.println("		Tamanho do buffer: " + router.getBufferSize() + " pacotes");
		System.out.println("		MSS: " + (maximumSegmentSize) + " bytes");
		System.out.println("		Política de atendimento: " + router.getBottleNeckPolicy());
		System.out.println("		Taxa de transmissão do roteador: " + router.getRate()*8 + " bps");
		System.out.println("	   ---------------------------------------------");
		for(ServerGroup group : serverGroups){
			System.out.println("		Servidores do grupo " + (serverGroups.indexOf(group)+1) +  ": " );
			for(Server server: group.getServers()){
				System.out.println("		Servidor com taxa " + server.getBroadcastRate()*8 + "bps do grupo " + (serverGroups.indexOf(server.getGroup())+1)  );
			}
		}
		 		
		System.out.println("\n	=======================================================");
		System.out.println("	=======================================================\n");
		
	}

	private static void readInputFile() throws IOException {

		BufferedReader reader = null;
		try {
			reader = new BufferedReader(new FileReader(FILENAME));
		} catch (FileNotFoundException e) {
			System.out.println("=============ERRO NA LEITURA DO ARQUIVO DE ENTRADA============");
			e.printStackTrace();
			return;
		}
		
		String line = null;
		Simulator.serverGroups = new ArrayList<ServerGroup>();
		
		//Ler taxa de atendimento do roteador(bps)
		line = reader.readLine();
		router = new Router( Integer.parseInt(line)/8 );
		
		//Ler taxa de transmissão dos servidores(bps)
		line = reader.readLine();
		Integer broadcastRate = Integer.parseInt(line)/8;
		
		//Numero de grupos
		line = reader.readLine();
		Integer nGroups = Integer.parseInt(line);
		int i;
		List<Server> servers = new ArrayList<Server>();
		for(i=0;i<nGroups;i++){
			//Ler atraso de propagação do grupo i
			line = reader.readLine();
			ServerGroup group = new ServerGroup( Integer.parseInt(line) );
			serverGroups.add(group);
			//Ler número de servidores no grupo i
			line = reader.readLine();
			int nServers = Integer.parseInt(line);
			//Criar servidores (seta sua taxa de transmissao e estabelece relação com o grupo)
			int j;
			for(j=0; j<nServers; j++){
				Receiver receiver = new Receiver();
				Server server = new Server(broadcastRate, group, receiver, serverId++);
				group.getServers().add(server);
				receiver.setServer(server);
				servers.add(server);
			}
		}

		//Ler tráfego de fundo
		line = reader.readLine();
		backgroundTraffic = new BackgroundTraffic(Float.parseFloat(line)/8);
		
		//Ler tamanho do buffer
		line = reader.readLine();
		router.setBufferSize(Integer.parseInt(line));
		
		//Ler MSS
		line = reader.readLine();
		maximumSegmentSize = Integer.parseInt(line)/8;
		
		//Ler política de gargalo
		line = reader.readLine();
		for(BottleNeck policy: BottleNeck.values()){
			if(line.equals(policy.name()) ){
				router.setBottleNeckPolicy(policy);
			}
		}
		
		
		reader.close();
		router.startRouter();
		for (Server server : servers) {
			server.startServer();
		}
		backgroundTraffic.startBackgroundTraffic();
	}
}
