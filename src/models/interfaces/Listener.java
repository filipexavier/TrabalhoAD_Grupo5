package models.interfaces;

import models.Event;

/**
 *
 * Interface 
 *
 */
public interface Listener {

	/**
	 * 
	 * @param event
	 */
	public void listen(Event event);
}
