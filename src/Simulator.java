
import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;



public abstract class Simulator {

	
	
	public static void main(String[] args) throws IOException {
		
		List<ServerGroup> serverGroups;
		
		BufferedReader reader = null;
		try {
			reader = new BufferedReader(new FileReader("file.txt"));
			//Scanner reader = new Scanner(new FileReader("file.txt"));
		} catch (FileNotFoundException e) {
			System.out.println("=============ERRO NA LEITURA DO ARQUIVO DE ENTRADA============");
			e.printStackTrace();
			return;
		}
		
		String line = null;
		serverGroups = new ArrayList<ServerGroup>();
		
		//Ler taxa de atendimento do roteador(bps)
		line = reader.readLine();
		Router router = new Router( Integer.parseInt(line) );
		
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
		//Ler nœmero de servidores no grupo 2
		line = reader.readLine();
		
		//Ler tr‡fego de fundo
		line = reader.readLine();
		
		//Ler tamanho do buffer
		line = reader.readLine();
		
		//Ler pol’tica de gargalo
		line = reader.readLine();
		
		
		reader.close();
		
	}

}
