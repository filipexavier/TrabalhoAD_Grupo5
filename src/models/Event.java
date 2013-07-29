package models;

/**
 * 
 * Implementação da classe que será responsável por representar um evento dentro da simulação.
 * 
 * TODO: detalhar
 *
 */
public class Event implements Comparable<Event>{

	/**
	 * Tipo do evento que está sendo representado
	 */
	private EventType type;
	
	/**
	 * Tempo na simulação em que esse evento ocorreu
	 */
	private Long time;
	
	/**
	 * Referência para quem enviou esse evento
	 */
	private Object sender;
	
	/**
	 * Armazena informação que o evento carrega, sendo específico para cada tipo de evento.
	 */
	private Object value; // pacote (TIMEOUT, ACK)

	private Long rtt;

	/**
	 * 
	 * Construtor padrão que recebe todas os atributos necessários para representar um evento, inicializando as variáveis correspondentes.
	 * 
	 * @param eventType tipo do evento sendo representado
	 * @param time tempo em que o evento ocorreu
	 * @param sender quem enviou o evento
	 * @param value informação referente ao evento
	 */
	public Event(EventType eventType, Long time, Long rtt, Object sender, Object value) {
		this.type = eventType;
		this.time = time;
		this.sender = sender;
		this.value = value;
		this.rtt = rtt;
	}

	/**
	 * Realiza uma comparação entre este evento e um outro evento.
	 * Neste caso, a comparação está sendo feita pelo tempo em que o evento ocorreu.
	 */
	@Override
	public int compareTo(Event arg0) {
		Integer compareTime = this.time.compareTo(arg0.time);
		if (compareTime == 0) {
			if (this.getType().equals(arg0.getType())) {
				return 0;
			} else if(this.getType().equals(EventType.SACK)) {
				return -1;
			} else {
				return 0;
			}
				
		} else {
			return compareTime;
		}
	}

	/**
	 * Retorna o tipo do evento que está sendo representado.
	 * @return tipo do evento
	 */
	public EventType getType() {
		return type;
	}

	/**
	 * Registra o tipo de evento que está sendo simulado.
	 * @param type tipo do evento.
	 */
	public void setType(EventType type) {
		this.type = type;
	}
	
	/**
	 * Retorna o tempo em que este evento o ocorreu na simulação.
	 * @return tempo em que o evento ocorreu.
	 */
	public Long getTime() {
		return time;
	}
	
	/**
	 * Substitui o tempo armazenado em que o evento ocorreu pelo novo tempo especificado.
	 * @param time tempo em que o evento ocorreu.
	 */
	public void setTime(Long time) {
		this.time = time;
	}

	/**
	 * Retorna o objeto responsável que enviou o evento.
	 * @return objeto que enviou o evento.
	 */
	public Object getSender() {
		return sender;
	}

	/**
	 * Substitui o objeto responsável que enviou o evento.
	 * @param sender objeto que enviou o evento
	 */
	public void setSender(Object sender) {
		this.sender = sender;
	}

	/**
	 * Retorna o value.
	 * @return informação referente ao evento.
	 */
	public Object getValue() {
		return value;
	}

	/**
	 * Registra a informação específica do evento.
	 * @param informação referente ao evento.
	 */
	public void setValue(Object value) {
		this.value = value;
	}
	
	/**
	 * Retorna uma representação legível do evento em uma String.
	 * Nesta representação estão informados o tipo do evento, o tempo em que ele ocorreu, quem enviou o evento, e o ??.
	 */
	@Override
	public String toString() {
		return "<Tipo: "+type+" Tempo: "+time+" Sender: "+sender+" Valor: "+value+">";
	}

	public Long getRtt() {
		return rtt;
	}
}
