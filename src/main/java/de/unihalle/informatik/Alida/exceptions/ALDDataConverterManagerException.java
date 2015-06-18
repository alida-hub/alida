/**
 * 
 */
package de.unihalle.informatik.Alida.exceptions;

/**
 * @author posch
 *
 */
 public class ALDDataConverterManagerException extends ALDProviderManagerException {

	/**
	 * Identifier string.
	 */
	private static final String typeID= "ALDConverterManagerException";

	/**
	 * @param t
	 * @param c
	 */
	public ALDDataConverterManagerException(ALDProviderManagerExceptionType t,
			String c) {
		super(t, c);
	}

}
