package de.unihalle.informatik.Alida.operator;

import java.io.File;

import de.unihalle.informatik.Alida.exceptions.ALDOperatorException;
import de.unihalle.informatik.Alida.exceptions.ALDOperatorException.OperatorExceptionType;
import de.unihalle.informatik.Alida.exceptions.ALDWorkflowException;
import de.unihalle.informatik.Alida.workflows.ALDWorkflow;

/**
 * @author posch
 *
 */
public class ALDOperatorLocation   {
	//TODO: should we add preferred color for display
	
	public enum LocationType implements ALDOperatorLocationType {
		ALDOPERATOR_FROM_CLASS,
		ALDWORKFLOW_FROM_FILE
	}

	/**
	 * Type of this location
	 */
	protected ALDOperatorLocationType locationType;
	
	/**
	 * Default constructor, should never be called directly!
	 */
	protected ALDOperatorLocation() {
		// nothing to do here
	}
	
	/**
	 * Creates and returns a new location for an class extending <code>ALDOperator</code>.
	 * 
	 * @param className
	 * @return
	 */
	public static ALDOperatorLocation createClassLocation( String className) {
		ALDOperatorLocation location = new ALDOperatorLocation();
		location.name = className;
		location.locationType = LocationType.ALDOPERATOR_FROM_CLASS;
		return location;
	}
	
	/**
	 * Creates and returns a new location for a file holding a work flow.
	 * 
	 * @param filename
	 * @return
	 */
	public static ALDOperatorLocation createWorkflowFileLocation( String filename) {
		ALDOperatorLocation location = new ALDOperatorLocation();
		location.name = filename;
		location.locationType = LocationType.ALDWORKFLOW_FROM_FILE;
		return location;
	}
	/**
	 * The name of this location in the original form, e.g. a filename
	 * or a fully qualified class name
	 */
	protected String name;
	
	/**
	 * Return the parts of the name within a hierarchy,
	 * e.g. package structure or package structure
	 * 
	 * @return The parts of the name or null if the location type of this object is unknown
	 */
	public 
	String[] getPartsOfName(){
		if (this.locationType == LocationType.ALDOPERATOR_FROM_CLASS) {
			return name.split("\\.");
		} else if (this.locationType == LocationType.ALDWORKFLOW_FROM_FILE) {
			//TODO handle windows drives
			String stdName = name.replace( File.separator + File.separator + "*", File.separator);
			if ( stdName.charAt(0) == File.separatorChar) {
				stdName = stdName.substring(1);
			}
			//System.out.println("ALDOperatorLocatio::getPartsOfName for " + name + " stdName " + stdName);
			return stdName.split( File.separator);
		} else {
			return null;
		}
	}

	/**
	 * Returns the proper name of this location
	 * 
	 * @return
	 */
	public String getName() {
		return name;
	}
	
	/**
	 * Create an {@link ALDOperator} form this location object.
	 * 
	 * @return
	 * @throws ALDOperatorException if instantiation of class fails
	 * @throws ALDWorkflowException if file not found or deserialization of work flow fails, 
	 * @see ALDWorkflow#load(String)
	 */
	public ALDOperator createOperator() throws ALDOperatorException, ALDWorkflowException {
		if (locationType == LocationType.ALDOPERATOR_FROM_CLASS) {
			try {
				return (ALDOperator) (Class.forName(this.name)).newInstance();
			} catch (Exception e) {
				throw( new ALDOperatorException(OperatorExceptionType.INSTANTIATION_ERROR, 
						"Cannot instantiate an ALDOperator for name <" + name + ">"));
			}
		} else if (locationType == LocationType.ALDWORKFLOW_FROM_FILE) {
			return ALDWorkflow.load(name);
		}else {
			return null;
		}
	}
}
