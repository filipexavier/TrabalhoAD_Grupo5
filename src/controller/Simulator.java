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
 * Classe responsável por controlar e inicializar toda a simulação das sessões TCP.
 *<p>
 * Realiza a leitura do arquivo de entrada, inicia os 4 modelos que interagem entre si 
 * e gerencia a comunicação entre eles.
 * <p> 
 * Os 4 modelos que iremos simular são: A estação transmissora. representada pela classe <code>Server</code>, 
 * o roteador, representado pela classe <code>Router</code>, receptor, <code>Receptor</code>, e o tráfego de fundo, <code>BackgroundTraffic</code>.
 * 
 * @see Server, Receptor, Router, BackgroundTraffic
 */
public class Simulator {

	/**
	 * Lista dos grupos de servidores que caracterizam um tipo de estação transmissora.
	 * Cada grupo de servidores define um único atraso de propagação para todos os servidores pertencentes ao mesmo.
	 */
	public static List<ServerGroup> serverGroups;
	
	/**
	 * Mapa para registrar os objetos que escutarão um determinado evento.
	 * <p>
	 * Uma chave no map é um enumerável do tipo <code>EventType</code>,
	 * cujo valor armazena um conjunto com referências para todos os objetos 
	 * que se registraram para escutar este tipo de evento.
	 */
	public static Map< EventType, Set<Listener> > listeners;
	
	/**
	 * Canal de eventos para comunicação interna entre as classes.
	 */
	public static List<Event> eventBuffer;
	
	/**
	 * Tráfego de fundo que será usado para congestionar o tráfego das sessões TCP.
	 */
	public static BackgroundTraffic backgroundTraffic;
	
	/**
	 * Tamanho máximo fixo em bytes no qual os pacotes IP são transmitidos.
	 */
	public static Integer maximumSegmentSize;
	
	/**
	 * Roteador por onde passam os pacotes das sessões TCP, que será o gargalo da nossa simulação
	 */
	public static Router router;
	
	/**
	 * Caminho do arquivo de entrada de onde serão lidos os dados de entrada. 
	 */
	public static final String FILENAME = "simulador.txt";
	
	/**
	 * Armazena o instante de tempo em nanosegundos no qual se encontra a simulação
	 */
	public static Long time = 0l;
	
	/**
	 * Variável de suporte à criação das estações TCP transmissoras.
	 */
	public static Integer serverId;
	
	/**
	 * Armazena dados estatísticos do comportamento dos servidores 
	 * ao longo da simulação, para serem plotados nos gráficos.
	 */
	public static HashMap<Server, HashMap<Long, Integer>> series;
	
	/**
	 * Armazena dados estatístios do comportamento do tamanho do buffer
	 * ao longo da simulação.
	 */
	public static HashMap<Long, Integer> bufferSize;
	
	/**
	 * Taxa de saída dos pacotes da estações TCP transmissoras ao longo da simulação.
	 */
	public static Integer serverBroadcast   = 0;
	
	/**
	 * Taxa de saída dos pacotes roteador ao longo da simulação.
	 */
	public static Integer routerBroadcast   = 0;
	
	/**
	 * Taxa de chegada dos pacotes às estações TCP receptoras ao longo da simulação.
	 */
	public static Integer receiverBroadcast = 0;

	/**
	 * Armazena as taxas de saída dos servidores ao longo da rodadas de simulação,
	 * para serem usadas na geração dos intervalos de confiança.
	 */
	public static List<Double> serverAverages = new ArrayList<Double>();
	
	/**
	 * Armazena as taxas de saída do roteador ao longo da rodadas de simulação,
	 * para serem usadas na geração dos intervalos de confiança.
	 */
	public static List<Double> routerAverages = new ArrayList<Double>();
	
	/**
	 * Armazena as taxas de chegada aos receptores ao longo da rodadas de simulação,
	 * para serem usadas na geração dos intervalos de confiança.
	 */
	public static List<Double> receiverAverages = new ArrayList<Double>();
	
	/**
	 * Regista um <code>Listener</code> para um determinado tipo de evento.
	 * <p>
	 * Este listener a partir de então será acionado toda vez que 
	 * um evento desste tipo ocorrer.
	 * 
	 * @param eventType tipo de evento a ser escutado.
	 * @param listener objeto que irá escutar os eventos.
	 */
	public static void registerListener(EventType eventType, Listener listener) {
		listeners.get(eventType).add(listener);
	}
	
	/**
	 * Dispara um evento de um determinado tipo <code>EventType</code>.
	 * <p>
	 * Haverá a criação de um <code>Event</code>, de acordo com os parâmetros passados.
	 * Tal evento será adicionado na estrutura de controle dos eventos, 
	 * para fazer os eventos acontecerem no instante de tempo especificado.
	 *  <p>
	 *  Caso o tempo especificado esteja atrasado em relação ao instante de tempo atual da simulação,
	 *  uma <code>RuntimeException</code> é lançada, parando imediatamente a simulação.
	 *  
	 * @param eventType tipo de evento disparado.
	 * @param time instante de tempo na simulação que ele ocorre, em nanosegundos.
	 * @param rtt tempo esperado para que esse evento gere uma ação. Usado somente em alguns casos, este parâmetro é opcional.
	 * @param sender objeto que está disparando o evento.
	 * @param value informações extras específicas para cada tipo de evento.
	 */
	public static void shotEvent(EventType eventType, Long time, Long rtt, Object sender, Object value) {
		Event event = new Event(eventType, time, rtt,sender, value);
		eventBuffer.add(event);
		if (time > event.getTime()) {
			throw new RuntimeException("Evento anterior adicionou um evento no passado");
		}
	}
	
	/** Cancela(exclui) evento referente a este pacote */
	/**
	 * Cancela a realização de um evento disparado.
	 * 
	 * @param eventType tipo de evento a ser cancelado.
	 * @param sender objeto que enviou o evento.
	 * @param value informações específicas do evento.
	 */
	public static void cancelEvent(EventType eventType, Object sender, Object value){
		Iterator<Event> it = eventBuffer.iterator();
		while( it.hasNext()){
			Event event = it.next();
			if(event.getType().equals(eventType) && (event.getValue().equals(value)) && event.getSender().equals(sender)){
				it.remove();
				break;
			}
		}
	}

	/**
	 * Inicia uma rodade da simulação, de acordo com os dados especificados no arquivo de entrada.
	 * 
	 * @throws IOException
	 * @throws InterruptedException
	 */
	public static void startSimulator() throws IOException, InterruptedException {
		/* inicializa as variáveis do simulador */
		serverId = 1;
		eventBuffer = new ArrayList<Event>();
		listeners = new HashMap<EventType, Set<Listener>>();
		series = new HashMap<Server, HashMap<Long, Integer>>();
		bufferSize = new HashMap<Long, Integer>();
		
		/* inicializa o mapa de eventos */
		for(EventType type: EventType.values()){
			listeners.put( type, new HashSet<Listener>() );
		}
		
		readInputFile(); // le os dados de entrada		
		printInputData(); // imprime no console os dados lidos 

		/* variável que irá manipular os eventos que irão ocorrer durante a simulação */
		Event event = null;
		/* pega o tempo de duração da simulação */
		Integer simulationTime = Integer.parseInt(SimulatorView.getInstance().getSimulationTimeTextField().getText());
		/* armazena o tempo em nanosegundos que a simulação irá começar */
		long realTime = System.nanoTime();
		while(eventBuffer.size() > 0 && (event == null || event.getTime() < simulationTime*1000000l)){
			/*
			 * a cada loop, um evento será processado, e todos os seus listeners serão acionados
			 */
			event = eventBuffer.remove(0);
			time = event.getTime();
			for(Listener listener: listeners.get(event.getType())){
				listener.listen(event);
			}			
			if (time > new Long(SimulatorView.getInstance().getTransientTime().getText())*1000000l) {
				/*
				 * coleta dados estatísticos para geração dos intervalos de confiança
				 */
				switch (event.getType()) {
				case SENDING_PACKAGE:
					serverBroadcast++;
					break;
				case DELIVER_PACKAGE:
					routerBroadcast++;
					break;
				case SACK:
					updatePlot(time);
					receiverBroadcast++;
					break;
				case TIME_OUT:
					updatePlot(time);
					break;
				}
			}		
			/* ordena a lista de eventos para que o próximo evento a ser processado corresponda ao próximo evento no tempo */
			sortEventBuffer(time);
		}
		
		/*
		 * a partir daqui, serão exibidos dados sobre a rodada de simulação
		 * além disso, o intervalo de confiança será atualizado 
		 */
		if (time > 0) {
			Long stationaryTime = time - new Long(SimulatorView.getInstance().getTransientTime().getText())*1000000l;
			
			SimulatorView.getInstance().getServerBroadcast().setText(new Long(serverBroadcast*1000*1000000l/stationaryTime).toString());
			SimulatorView.getInstance().getRouteBroadcast().setText(new Long(routerBroadcast*1000*1000000l/stationaryTime).toString());
			SimulatorView.getInstance().getReceiverBroadcast().setText(new Long(receiverBroadcast*1000*1000000l/stationaryTime).toString());
		}
		
		updateChart();
		updateIntervalConfidence();
		
		clearSimulator(); // limpa o simulador, para que uma nova simulação possa acontecer
		System.out.println(System.nanoTime() - realTime);
		realTime = System.currentTimeMillis();
	}

	/**
	 * Atualiza o gráfico na interface gráfica com os dados da simulação.
	 */
	private static void updateChart() {
		SimulatorView.getInstance().updateChart(series, bufferSize);
	}

	/**
	 * Atualiza os intervalos de confiança, levando em consideração todas as rodadas já efetuadas.
	 */
	private static void updateIntervalConfidence() {
		serverAverages.add(new Double(SimulatorView.getInstance().getServerBroadcast().getText()));
		receiverAverages.add(new Double(SimulatorView.getInstance().getReceiverBroadcast().getText()));
		routerAverages.add(new Double(SimulatorView.getInstance().getRouteBroadcast().getText()));
		
		Integer numberOsRuns = serverAverages.size();
		
		SimulatorView.getInstance().getNumOfRuns().setText(numberOsRuns.toString());
		
		if (numberOsRuns >= 2) {
			SimulatorView.getInstance().getServerCI().setText(ConfidenceInterval.getConfidenceInterval(serverAverages));
			SimulatorView.getInstance().getReceiverCI().setText(ConfidenceInterval.getConfidenceInterval(receiverAverages));
			SimulatorView.getInstance().getRouterCI().setText(ConfidenceInterval.getConfidenceInterval(routerAverages));
		}
	}

	/**
	 * Limpa as variáveis do <code>Simulator</code>, 
	 * preparando para uma nova rodada de simulação.
	 */
	private static void clearSimulator() {
		serverGroups = null;
		listeners = null;
		eventBuffer = null;
		backgroundTraffic = null;
		maximumSegmentSize = null;
		router = null;
		time = 0l;
		serverBroadcast   = 0;
		routerBroadcast   = 0;
		receiverBroadcast = 0;
	}

	/**
	 * Ordena a lista de eventos em ordem crescente
	 * do instante de tempo em que eles ocorrem na simulação.
	 * <p>
	 * Também atualiza as informações que serão exibidas na interface.
	 *
	 * @param time instante de tempo em nanosegundos.
	 */
	private static void sortEventBuffer(Long time) {
		Collections.sort(eventBuffer);
	}

	/**
	 * Atualiza as informações da simulação que serão exibidas na interface.
	 * <p>
	 * @param time instante de tempo em nanosegundos
	 */
	private static void updatePlot(Long time) {
		Integer  value = 0;
		
		for (ServerGroup serverGroup : serverGroups) {
			for (Server server : serverGroup.getServers()) {
				value += (int) Math.floor(server.getCwnd());
				value /= Simulator.maximumSegmentSize;
				if(series.get(server) == null) {
					series.put(server, new HashMap<Long, Integer>());
				}
				if (series.get(server).get(time) == null) {
					series.get(server).put(time, value);
				}
				// armazena o valor de txwnd/MSS para aparecer no gráfico
				value = 0;
			}
		}			
		//Adiciona o tamanho do buffer no tempo atual para aparecer no gráfico
		bufferSize.put(time, router.getBuffer().size());
	}

	/**
	 * Imprime no console os valores lidos do arquivo de entrada.
	 */
	private static void printInputData() {
		System.out.println("	================ LOG DADOS DO ARQUIVO =================");
		System.out.println("	=======================================================\n");
		System.out.println("		Tamanho médio das rajadas do tráfego de fundo: " + backgroundTraffic.getAvgGustLength());
		System.out.println("		Intervalo médio entre rajadas do tráfego de fundo: " + backgroundTraffic.getAvgGustInterval() + " ms");
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

	/**
	 * Realiza a leitura do arquivo de dados das informações necessárias para a simulação.
	 * 
	 * @throws IOException
	 */
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
			ServerGroup group = new ServerGroup( Long.parseLong(line)*1000000l );
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
		Long avgGustLength = Long.parseLong(line);
		line = reader.readLine();
		Long avgGustInterval = Long.parseLong(line);
		backgroundTraffic = new BackgroundTraffic(avgGustLength, 1d/avgGustInterval);
		
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
		for (Server server : servers) {
			server.startServer();
		}
		backgroundTraffic.startBackgroundTraffic();
	}
}
