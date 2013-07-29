package models;

/**
 * 
 * Conjunto dos tipos de eventos que serão simulados ao longo da simulação.
 * <p>
 * Segue uma explicação detalha do que representa cada evento:
 * <p>
 * Evento				Sender		Receiver			Descrição
 * <p>
 * SEND_PACKAGE			Server		Server				Evento que representa o pacote entrar em serviço
 * <p>
 * SENDING_PACKAGE		Server		Server				Evento que representa o pacote sair de serviço
 * <p>
 * TIME_OUT				Server		Server				Evento que representa o timeout
 * <p>
 * PACKAGE_SENT			Server		Router				Evento que representa a chegada no roteador
 * <p>
 * DELIVER_PACKAGE		Router		Router, Receiver	Evento que representa a entrada de serviço do roteador
 * <p>
 * PACKAGE_DELIVERED	Router		Receiver			Evento que representa a entrega do pacote ao receptor
 * <p>
 * SACK					Receiver	Server				Evento que representa um SACK
 * 
 */
public enum EventType {
	SEND_PACKAGE, SENDING_PACKAGE, PACKAGE_SENT, DELIVER_PACKAGE, PACKAGE_DELIVERED, TIME_OUT, SACK
}

