package de.unihalle.informatik.Alida.workflows;

/**
 * Class to index a work flow node.
 * The id is NOT intended to be a means to identify the nodes, just
 * used for, e.g., debugging output.
 * 
 * @author posch
 *
 */
public class ALDWorkflowNodeID extends ALDWorkflowID {
	
	/**
	 * variable to count IDs
	 */
	private static Integer lastID = -1;

	/** Create a new node id.
	 */
	ALDWorkflowNodeID() {
		super(++lastID);
	}
}