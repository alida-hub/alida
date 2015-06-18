/**
 * 
 */
package de.unihalle.informatik.Alida.workflows;

import java.io.File;
import java.util.Collection;
import java.util.LinkedList;

import de.unihalle.informatik.Alida.helpers.ALDEnvironmentConfig;
import de.unihalle.informatik.Alida.helpers.ALDFilePathManipulator;
import de.unihalle.informatik.Alida.operator.ALDOperatorLocation;

/**
 * @author posch
 *
 */
public class ALDWorkflowHelper {
	private static boolean debug = false;

	/**
	 * Collects work flows from files and return location objects.
	 * TODO: which directories to consider and requirements on work flow file.
	 * 
	 * @return	List of work flow locations  
	 */
	public static Collection<ALDOperatorLocation> lookupWorkflows() {
		
		String workflowPath = ALDEnvironmentConfig.getConfigValue(
			"OPRUNNER", "WORKFLOWPATH");
		
		if ( workflowPath == null) {
			workflowPath = System.getProperty("user.home") + "/.alida/workflows";
		}

		return lookupWorkflows( workflowPath);
	}
	
	/**
	 * 	Collects work flows from files and return location objects.
	 * <code>pathnames</code> is a colon separated list of directories
	 * to search for work flow files.
	 * 
	 * TODO: which directories to consider and requirements on work flow file.

	 * @param pathnames
	 * @return
	 */
	public static Collection<ALDOperatorLocation> lookupWorkflows( String pathnames) {
		// TODO: should we search recursively?
		// TODO: should extension be required, should we look into the file to
		//       check if really a work flow or should we load the work flow to check??
		
		LinkedList<ALDOperatorLocation> locations = new LinkedList<ALDOperatorLocation>();

		if ( debug  ) 
			System.out.println("ALDWorkflowHelper::lookupWorkflows");
				
		for ( String pathname : pathnames.split(File.pathSeparator )) {
			if ( debug ) {
				System.out.println( "ALDWorkflowHelper::lookupWorkflows for pathname " + pathname);
			}
			File dir = new File(pathname);

			if ( dir != null && dir.isDirectory() ) {
				for ( String filename : dir.list() ) {
					if ( ALDFilePathManipulator.getExtension(filename).equals( 
							ALDWorkflow.workflowXMLFileExtension) ) {
						String fullFilename = dir+File.separator+filename;
						if ( new File( fullFilename).isFile() ) {
							locations.add( ALDOperatorLocation.createWorkflowFileLocation(
									fullFilename));
						}
					}
				}
			}
		}
		
		return locations;
	}
}
