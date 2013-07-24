package models;

public enum EventType {
	SEND_PACKAGE, SENDING_PACKAGE, PACKAGE_SENT, DELIVER_PACKAGE, PACKAGE_DELIVERED, TIME_OUT, SACK
	//TODO: QUERO CRIAR CLASSE SACK, pois ela precisa guardar uma lista de sequencias de acks.
}
