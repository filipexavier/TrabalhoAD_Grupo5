
import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;



public abstract class Simulator {

	public static List<ServerGroup> serverGroups;
	public static Integer backgroudTraffic;
	public static Integer maximumSegmentSize;
	public static Router router;
	public static final String FILENAME = "simulador.txt";
	
	public static void main(String[] args) throws IOException {
		
		readInputFile();
		printInputData();
		
	}

	private static void printInputData() {
		System.out.println("	================ LOG DADOS DO ARQUIVO =================");
		System.out.println("	=======================================================\n");
		System.out.println("		Tr‡fego de fundo: " + backgroudTraffic + " bps");
		System.out.println("		Tamanho do buffer: " + router.getBufferSize() + " pacotes");
		System.out.println("		MSS: " + (maximumSegmentSize/8) + " bytes");
		System.out.println("		Pol’tica de atendimento: " + router.getBottleNeckPolicy());
		System.out.println("		Taxa de transmiss‹o do roteador: " + router.getRate() + " bps");
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
		
		//Ler taxa de transmiss‹o dos servidores(bps)
		line = reader.readLine();
		Integer broadcastRate = Integer.parseInt(line);
		
		//Numero de grupos
		line = reader.readLine();
		Integer nGroups = Integer.parseInt(line);
		int i;
		for(i=0;i<nGroups;i++){
			//Ler atraso de propaga‹o do grupo i
			line = reader.readLine();
			ServerGroup group = new ServerGroup( Integer.parseInt(line) );
			serverGroups.add(group);
			
			//Ler nœmero de servidores no grupo i
			line = reader.readLine();
			int nServers = Integer.parseInt(line);
			//Criar servidores (seta sua taxa de transmissao e estabelece rela‹o com o grupo)
			int j;
			for(j=0; j<nServers; j++){
				
				new Server(broadcastRate).setGroup(group);
			}
			
		}

		//Ler tr‡fego de fundo
		line = reader.readLine();
		backgroudTraffic = Integer.parseInt(line);
		
		//Ler tamanho do buffer
		line = reader.readLine();
		router.setBufferSize(Integer.parseInt(line));
		
		//Ler MSS
		line = reader.readLine();
		maximumSegmentSize = Integer.parseInt(line);
		
		//Ler pol’tica de gargalo
		line = reader.readLine();
		for(BottleNeck policy: BottleNeck.values()){
			if(line.equals(policy.name()) ){
				router.setBottleNeckPolicy(policy);
			}
		}
		
		
		reader.close();
	}

}
