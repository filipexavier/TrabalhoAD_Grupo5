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

import models.BottleNeck;
import models.Event;
import models.EventType;
import models.Receiver;
import models.Router;
import models.Server;
import models.ServerGroup;
import models.interfaces.Listener;
import view.SimulatorView;



public class Simulator {

	public static List<ServerGroup> serverGroups;
	public static Map< EventType, Set<Listener> > listeners;
	public static List<Event> eventBuffer;
	public static Integer backgroudTraffic;
	public static Integer maximumSegmentSize;
	public static Router router;
	public static final String FILENAME = "simulador.txt";
	public static Integer time = 0;

	public static void registerListener(EventType eventType, Listener listener) {
		listeners.get(eventType).add(listener);
	}
	
	public static void shotEvent(EventType eventType, Integer time, Object sender, Object value) {
		Event event = new Event(eventType, time, sender, value);
		eventBuffer.add(event);
		System.out.println("O evento "+event+" foi enviado.");
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
	
	public static void main(String[] args) throws IOException, InterruptedException {
		startSimulator();
	}

	public static void startSimulator() throws IOException, InterruptedException {
		eventBuffer = new ArrayList<Event>();
		listeners = new HashMap<EventType, Set<Listener>>();
		for(EventType type: EventType.values()){
			listeners.put( type, new HashSet<Listener>() );
		}
		readInputFile();		
		printInputData();

		Event event = null;
		Integer time = 0;
		while(eventBuffer.size() > 0){
			event = eventBuffer.remove(0);
//			Thread.sleep(Math.abs(time - event.getTime()));
			time = event.getTime();
			for(Listener listener: listeners.get(event.getType())){
				listener.listen(event);
			}
			time = event.getTime();
			System.out.println(time);
			sortEventBuffer(time);
		}
	}

	private static void sortEventBuffer(Integer time) {
		Collections.sort(eventBuffer);
		Integer  value = 0;
		Integer numOfServers = 0;
		
		for (ServerGroup serverGroup : serverGroups) {
			for (Server server : serverGroup.getServers()) {
				value += server.getCwnd();
				numOfServers++;
			}
		}
		
		value /= numOfServers;
		value /= Simulator.maximumSegmentSize;
				
		SimulatorView.getInstance().updateChart(value, time);
	}

	private static void printInputData() {
		System.out.println("	================ LOG DADOS DO ARQUIVO =================");
		System.out.println("	=======================================================\n");
		System.out.println("		Tráfego de fundo: " + backgroudTraffic + " bps");
		System.out.println("		Tamanho do buffer: " + router.getBufferSize() + " pacotes");
		System.out.println("		MSS: " + (maximumSegmentSize/8) + " bytes");
		System.out.println("		Política de atendimento: " + router.getBottleNeckPolicy());
		System.out.println("		Taxa de transmissão do roteador: " + router.getRate() + " bps");
		System.out.println("	   ---------------------------------------------");
		for(ServerGroup group : serverGroups){
			System.out.println("		Servidores do grupo " + (serverGroups.indexOf(group)+1) +  ": " );
			for(Server server: group.getServers()){
				System.out.println("		Servidor com taxa " + server.getBroadcastRate() + "bps do grupo " + (serverGroups.indexOf(server.getGroup())+1)  );
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
		router = new Router( Integer.parseInt(line) );
		
		//Ler taxa de transmissão dos servidores(bps)
		line = reader.readLine();
		Integer broadcastRate = Integer.parseInt(line);
		
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
				Server server = new Server(broadcastRate, group, receiver);
				group.getServers().add(server);
				receiver.setServer(server);
				servers.add(server);
				
			}
			
		}

		//Ler tráfego de fundo
		line = reader.readLine();
		backgroudTraffic = Integer.parseInt(line);
		
		//Ler tamanho do buffer
		line = reader.readLine();
		router.setBufferSize(Integer.parseInt(line));
		
		//Ler MSS
		line = reader.readLine();
		maximumSegmentSize = Integer.parseInt(line);
		
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
	}
}
