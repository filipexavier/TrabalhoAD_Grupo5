package models;

public class Event implements Comparable<Event>{

	private EventType type;
	private Integer time;
	private Object sender;

	public Event(EventType ack, Integer time, Object sender) {
		this.type = ack;
		this.time = time;
		this.sender = sender;
	}

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
	
	
	public Integer getTime() {
		return time;
	}
	
	public void setTime(Integer time) {
		this.time = time;
	}

	public Object getSender() {
		return sender;
	}

	public void setSender(Object sender) {
		this.sender = sender;
	}

	
	
}
