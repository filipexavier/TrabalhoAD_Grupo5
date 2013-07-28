package models;
import java.util.LinkedList;
import java.util.List;

/**
 * 
 * Implementação de um grupo de servidores de um mesmo tipo.
 * <p>
 * Esta classe irá representar um tipo de grupo dos servidores sendo estudados.
 * Seu objetivo é agrupar servidores de um mesmo tipo, armazenando para este tipo a
 * informação acerca do atraso o qual um pacote sofre, quando é enviado por um servidor deste tipo.
 *
 */
public class ServerGroup {
	
	/**
	 * Lista dos servidores do tipo em questão.
	 */
	private List<Server> servers;
	
	/**
	 * Tempo que um pacote demora desde o momento em que ele sai do servidor, até chegar ao roteador.
	 */
	private Long broadcastDelay;

	/**
	 * Inicializa um grupo de servidores que compartilham o mesmo atraso informado. 
	 * 
	 * @param broadcastDelay atraso que um pacote sofre para ir do servidor até o roteador.
	 */
	public ServerGroup(Long broadcastDelay) {
		this.broadcastDelay = broadcastDelay;
		servers = new LinkedList<Server>();
	}

	/**
	 * Retorna a lista contendo todos os servidores deste grupo.
	 * @return lista de servidores.
	 */
	public List<Server> getServers() {
		return servers;
	}

	/**
	 * Retorna o atraso que um pacote sofre para ir do servidor até o roteador, quando ele for enviado por um servidor deste grupo.
	 * 
	 * @return tempo que um pacote demora desde o momento em que ele sai do servidor, até chegar ao roteador.
	 */
	public Long getBroadcastDelay() {
		return broadcastDelay;
	}
}
