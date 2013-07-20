package models;

public class Event implements Comparable<Event>{

	private EventType type;
	private Integer time;
	
	@Override
	public int compareTo(Event arg0) {
		return this.time.compareTo(arg0.time);
	}

	public EventType getType() {
		return type;
	}

	public void setType(EventType type) {
		this.type = type;
	}
	

	
	
}
