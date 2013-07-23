package models;

public class Event implements Comparable<Event>{

	private EventType type;
	private Integer time;
	private Object sender;
	private Object value;

	public Event(EventType ack, Integer time, Object sender, Object value) {
		this.type = ack;
		this.time = time;
		this.sender = sender;
		this.value = value;
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

	public Object getValue() {
		return value;
	}

	public void setValue(Object value) {
		this.value = value;
	}
	
	@Override
	public String toString() {
		return "<Tipo: "+type+" Tempo: "+time+" Sender: "+sender+" Valor: "+value+">";
	}	
}
