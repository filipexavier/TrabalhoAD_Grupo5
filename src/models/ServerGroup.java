package models;
import java.util.LinkedList;
import java.util.List;


public class ServerGroup {

	private List<Server> servers;
	
	private Integer broadcastDelay;

	public ServerGroup() {
	}
	
	public ServerGroup(Integer broadcasDelay) {
		this.broadcastDelay = broadcasDelay;
		servers = new LinkedList<Server>();
	}

	public List<Server> getServers() {
		return servers;
	}

	public void setServers(List<Server> servers) {
		this.servers = servers;
	}

	public Integer getBroadcastDelay() {
		return broadcastDelay;
	}

	public void setBroadcastDelay(Integer broadcastDelay) {
		this.broadcastDelay = broadcastDelay;
	}

}
