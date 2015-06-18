/**
 * 
 */
package de.unihalle.informatik.Alida.datatypes;

import java.util.List;


/**
 * A class implementing this interface provides methods to determine proper configuration
 * of an object.
 * 
 * @author posch
 *
 */
public interface ALDConfigurationValidator {
	/**
	 * Returns true if the object is properly configured.
	 * @return
	 */
	public boolean isConfigured();
	
	/**
	 * Return a list of not or not properly configured items of this object.
	 * Items may, e.g., be parameters of operators or member fields in general.
	 * 
	 * @return List of not or not properly configured items or null if the object is properly configured
	 */
	public List<String> unconfiguredItems();
}
