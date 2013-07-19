import java.util.HashSet;
import java.util.Set;


public class ServerGroup {

	private Set<Server> servers;
	
	private Integer broadcastDelay;

	public ServerGroup() {
	}
	
	public ServerGroup(Integer broadcasDelay) {
		this.broadcastDelay = broadcasDelay;
		servers = new HashSet<Server>();
	}

	public Set<Server> getServers() {
		return servers;
	}

	public void setServers(Set<Server> servers) {
		this.servers = servers;
	}

	public Integer getBroadcastDelay() {
		return broadcastDelay;
	}

	public void setBroadcastDelay(Integer broadcastDelay) {
		this.broadcastDelay = broadcastDelay;
	}

}
