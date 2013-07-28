package models;
import java.util.LinkedList;
import java.util.List;

/**
 * 
 * 
 *
 */
public class ServerGroup {
	
	/**
	 * 
	 */
	private List<Server> servers;
	
	/**
	 * 
	 */
	private Integer broadcastDelay;

	/**
	 * 
	 * @param broadcasDelay
	 */
	public ServerGroup(Integer broadcasDelay) {
		this.broadcastDelay = broadcasDelay;
		servers = new LinkedList<Server>();
	}

	/**
	 * 
	 * @return
	 */
	public List<Server> getServers() {
		return servers;
	}

	/**
	 * 
	 * @param servers
	 */
	public void setServers(List<Server> servers) {
		this.servers = servers;
	}

	/**
	 * 
	 * @return
	 */
	public Integer getBroadcastDelay() {
		return broadcastDelay;
	}

	/**
	 * 
	 * @param broadcastDelay
	 */
	public void setBroadcastDelay(Integer broadcastDelay) {
		this.broadcastDelay = broadcastDelay;
	}
}
