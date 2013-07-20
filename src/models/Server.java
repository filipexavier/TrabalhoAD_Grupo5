package models;

public class Server {

	private Integer broadcastRate;
	
	private ServerGroup group;
	
	public Server() {
	}

	public Server(Integer rate) {
		this.broadcastRate = rate;
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

}
