package models.interfaces;

import models.Event;

/**
 *
 * Interface 
 *
 */
public interface Listener {

	/**
	 * Método a ser implementado que irá manipular os eventos recebidos pelo por esse <code>Listener</code>.
	 * 
	 * @param event evento a ser tratado.
	 */
	public void listen(Event event);
}
