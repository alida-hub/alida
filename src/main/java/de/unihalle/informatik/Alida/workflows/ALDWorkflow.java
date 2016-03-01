/*
 * This file is part of MiToBo, the Microscope Image Analysis Toolbox.
 *
 * Copyright (C) 2010 - @YEAR@
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, 
 <http://www.gnu.org/licenses/>.
 *
 * Fore more information on MiToBo, visit
 *
 *    http://www.informatik.uni-halle.de/mitobo/
 *
 */

package de.unihalle.informatik.Alida.workflows;

import java.io.File;
import java.lang.reflect.Field;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.BlockingDeque;

import javax.swing.event.EventListenerList;

import de.unihalle.informatik.Alida.annotations.Parameter.Direction;
import de.unihalle.informatik.Alida.dataconverter.ALDDataConverterManager;
import de.unihalle.informatik.Alida.dataio.ALDDataIOManagerXmlbeans;
import de.unihalle.informatik.Alida.exceptions.ALDDataConverterManagerException;
import de.unihalle.informatik.Alida.exceptions.ALDDataConverterException;
import de.unihalle.informatik.Alida.exceptions.ALDException;
import de.unihalle.informatik.Alida.exceptions.ALDOperatorException;
import de.unihalle.informatik.Alida.exceptions.ALDOperatorException.OperatorExceptionType;
import de.unihalle.informatik.Alida.exceptions.ALDProcessingDAGException;
import de.unihalle.informatik.Alida.exceptions.ALDWorkflowException;
import de.unihalle.informatik.Alida.exceptions.ALDWorkflowException.WorkflowExceptionType;
import de.unihalle.informatik.Alida.helpers.ALDFilePathManipulator;
import de.unihalle.informatik.Alida.operator.ALDOpParameterDescriptor;
import de.unihalle.informatik.Alida.operator.ALDOperator;
import de.unihalle.informatik.Alida.operator.ALDOperatorControllable;
import de.unihalle.informatik.Alida.operator.ALDOperatorLocation;
import de.unihalle.informatik.Alida.operator.events.ALDOperatorExecutionProgressEvent;
import de.unihalle.informatik.Alida.workflows.ALDWorkflowNode.ALDWorkflowNodeState;
import de.unihalle.informatik.Alida.workflows.events.ALDWorkflowClassEvent;
import de.unihalle.informatik.Alida.workflows.events.ALDWorkflowClassEvent.ALDWorkflowClassEventType;
import de.unihalle.informatik.Alida.workflows.events.ALDWorkflowClassEventListener;
import de.unihalle.informatik.Alida.workflows.events.ALDWorkflowEvent;
import de.unihalle.informatik.Alida.workflows.events.ALDWorkflowEvent.ALDWorkflowEventType;
import de.unihalle.informatik.Alida.workflows.events.ALDWorkflowEventListener;
import de.unihalle.informatik.Alida.workflows.events.ALDWorkflowEventReporter;
import de.unihalle.informatik.Alida.workflows.events.ALDWorkflowRunFailureInfo;
import de.unihalle.informatik.Alida.workflows.events.ALDWorkflowStorageInfo;

/** Class to model a workflow in Alida.
 * A workflow consists of nodes each holding one {@link ALDOperator} and
 * edges connecting output parameters of the source node with input parameters of the target node.
 * 
 * @author posch
 *
 */

public class ALDWorkflow extends ALDOperatorControllable 
implements ALDWorkflowEventReporter {

	/**
	 * workflow context types.
	 */
	public static enum ALDWorkflowContextType {
		/**
		 * workflow runs in a oprunner context.
		 * Only one node is allowed an ALDControlEventType of type STEP_EVENT 
		 * are passed to the operator in the enclosed node
		 */
		OP_RUNNER,
		
		/**
		 * workflow runs with in grappa.
		 * ALDControlEventType of type STEP_EVENT are not passed to the operators in the
		 * enclosed nodes.
		 */
		GRAPPA,
		
		/**
		 * 
		 */
		OTHER
	}


	/**
	 * Standard extension for a xml file holding the external representation
	 * of a workflow.
	 * 
	 */
	public final static String workflowXMLFileExtension = "awf";
	
	/**
	 * For debugging information
	 */
	static int debug = 0;
	
	/**
	 * Name of a workflow with out a proper name
	 */
	public final static String untitledWorkflowName = "Untitled";
	
	/**
	 * Determines the context of this workflow
	 */
	private final ALDWorkflowContextType workflowContext;
	
	/**
	 * If true, an input parameter is reset (currently set to null)
	 * if is is disconnected (by removing or redirecting an edge) from its source.
	 */
	boolean resetDisconnectedInput = true;
	
	/**
	 * All nodes contained in this workflow
	 */
	private LinkedList<ALDWorkflowNode> nodes;
	
	/**
	 * This node is used as an interior shadow (or substitute) for the
	 * node holding this workflow.
	 * This is necessary (or at least convenient) as the workflow does
	 * not know about the node it holds, and this node may not exist at all.
	 */
	private ALDWorkflowNode interiorShadowNode = null;
	
	/**
	 * All edges of this workflow.
	 * An edge may connect {@linkplain wfNode} and nodes from {@linkplain #nodes}.
	 */
	private LinkedList<ALDWorkflowEdge> edges;
	
	/**
	 * If true the user has requested an interrupt of the current execution of
	 * (part of) the workflow.
	 */
	@Deprecated
	private boolean executionInterrupted;

	/**
	 * Hash map of operator nodes of this workflow. 
	 * The id is used to identify and access a node from outside of the workflow.
	 */
	private static HashMap<ALDWorkflowNodeID,ALDWorkflowNode> nodeIdToNode = 
			new HashMap<ALDWorkflowNodeID,ALDWorkflowNode>();
	
	/**
	 * 
	 */
	private static HashMap<ALDWorkflowNode, ALDWorkflowNodeID> nodeToNodeId = 
			new HashMap<ALDWorkflowNode, ALDWorkflowNodeID>();
	
	/**
	 * Hash map of edges of this workflow. 
	 * The id is used to identify and access an edge from outside of the workflow.
	 */
	private static HashMap<ALDWorkflowEdgeID,ALDWorkflowEdge> edgeIdToEdge = 
			new HashMap<ALDWorkflowEdgeID,ALDWorkflowEdge>();
	/**
	 * 
	 */
	private static HashMap<ALDWorkflowEdge,ALDWorkflowEdgeID> edgeToEdgeId = 
			new HashMap<ALDWorkflowEdge,ALDWorkflowEdgeID>();

	/**
	 * Gives the nodeIds of last loading of this workflow from an external representation
	 * in the order as store externally.
	 * This order is the some as returned by {@link #getMappingIntegerToNodeId()}
	 */
	ALDWorkflowNodeID[] loadIndexToNodeId;
	
	/**
	 * Gives the edgeIds of last loading of this workflow from an external representation
	 * in the order as store externally.
	 * This order is the some as returned by {@link #getMappingIntegerToEdgeId()}
	 */
	ALDWorkflowEdgeID[] loadIndexToEdgeId;
	
	/**
	 * converter manager singleton instance
	 */
	private static ALDDataConverterManager converterManager = ALDDataConverterManager.getInstance();

	/**
	 * List of control event listeners attached to this workflow.
	 */
	//protected transient volatile EventListenerList workflowEventlistenerList = 
	//		new EventListenerList();

	private transient HashMap<ALDWorkflowEventListener,ALDWorkflowEventManager> workflowEventMangerList =
			new HashMap<ALDWorkflowEventListener, ALDWorkflowEventManager>();

	/*
	private transient HashMap<ALDWorkflowEventListener,Thread> workflowEventMangerThreadList =
			new HashMap<ALDWorkflowEventListener, Thread>();
	*/
	
	/**
	 * List of control event listeners attached to this class used for loading workflows.
	 */
	protected static transient volatile EventListenerList workflowLoadEventlistenerList = 
			new EventListenerList();

	
	// ==================================================
	// Constructor section
	
	/** Constructor for a workflow in a grappa context.
	 * 
	 * @param name of the workflow
	 * @throws ALDOperatorException
	 */
	public ALDWorkflow( String name) throws ALDOperatorException  {
		this( name, ALDWorkflowContextType.GRAPPA);
	}
	
	/** Constructor
	 * 
	 * @param name of the workflow
	 * @param context context this workflow is running in
	 * @throws ALDOperatorException
	 */
	public ALDWorkflow( String name, ALDWorkflowContextType context) throws ALDOperatorException  {
		super();
		setName( name);
		this.workflowContext = context;
		if ( this.workflowContext == ALDWorkflowContextType.OP_RUNNER) {
			this.notifyListenersRecursively = true;
		} else {
			this.notifyListenersRecursively = false;
		}
		
		this.nodes = new LinkedList<ALDWorkflowNode>();
		this.edges = new LinkedList<ALDWorkflowEdge>();
		this.executionInterrupted = false;
		
		this.interiorShadowNode = new ALDWorkflowNode( null, this, true);
		addNode( interiorShadowNode);
		this.notifyListenersRecursively = true;
	}
	
	/** Constructor for an untitled workflow in a grappa context.
	 * 
	 * @throws ALDOperatorException
	 */
	public ALDWorkflow() throws ALDOperatorException  {
		this( untitledWorkflowName, ALDWorkflowContextType.GRAPPA);
	}

	/** Constructor for an untitled workflow
	 * 
	 * @param name of the workflow
	 * @param context context this workflow is running in
	 * @throws ALDOperatorException
	 */
	public ALDWorkflow( ALDWorkflowContextType context) throws ALDOperatorException  {
		this( untitledWorkflowName, context);
	}
	
	// ==================================================
	// Methods

	@Override
  public boolean supportsStepWiseExecution() {
		return true;
	}
	
	@Override
	protected void operate() throws ALDOperatorException,
	ALDProcessingDAGException {
		try {
			runWorkflow( true, false);
		} catch (ALDWorkflowException e) {
			throw new ALDOperatorException(OperatorExceptionType.OPERATE_FAILED, 
					"ALDWorkflow::operator failed\n" + e.getMessage());
		}
	}
	
	@Override
	public void setName( String name) {
		super.setName( name);
		
		// fire an event
		String info = new String( name);
		fireALDWorkflowEvent(new ALDWorkflowEvent(this, ALDWorkflowEventType.RENAME, info));
	}
	
	// ===================================================================
	// Getter methods 
	// TODO some part of the getter methods may be needs to be synchronized

	/**
	 * @return the resetDisconnectedInput
	 */
	public boolean isResetDisconnectedInput() {
		return resetDisconnectedInput;
	}

	/**
	 * @param resetDisconnectedInput the resetDisconnectedInput to set
	 */
	void setResetDisconnectedInput(boolean resetDisconnectedInput) {
		this.resetDisconnectedInput = resetDisconnectedInput;
	}

	/** Get the operator associated with <code>nodeId</code>.
	 * 
	 * @param nodeId id of operator to return
	 * @return operator object or <code>null</code> if not existing
	 * @throws ALDWorkflowException  if the node with <code>nodeId</code> does not exist.
	 */
	public ALDOperator getOperator( ALDWorkflowNodeID nodeId) throws ALDWorkflowException {
		return getNode(nodeId).getOperator();
	}
	
	/** Set the operator associated with <code>nodeId</code>.
	 * The new operator instance needs to be of the same class as the current operator instance.
	 * 
	 * @param nodeId id of operator to return
	 * @throws ALDWorkflowException  if the new operator instance is of wrong type.
	 */
	public void setOperator( ALDWorkflowNodeID nodeId, ALDOperator newOp) throws ALDWorkflowException {

		ALDOperator oldOp = getNode( nodeId).getOperator();
		oldOp.removeOperatorExecutionProgressEventListener(this);
		
		getNode(nodeId).setOperator( newOp);
		newOp.addOperatorExecutionProgressEventListener(this);
		
		if ( this.workflowContext == ALDWorkflowContextType.OP_RUNNER &&
				oldOp instanceof ALDOperatorControllable) {
			ALDOperatorControllable controlableOp = (ALDOperatorControllable)oldOp;
			if ( debug >= 1 ) {
				System.out.println("ALDWorkflow::setOperator remove old operator and register new operator as listener to the workflow");
			}
			this.removeALDConfigurationEventListener( controlableOp);
			this.removeALDControlEventListener(controlableOp);
			
			controlableOp =  (ALDOperatorControllable)newOp;
			this.addALDConfigurationEventListener( controlableOp);
			this.addALDControlEventListener(controlableOp);
		}
		
		this.nodeParameterChanged(nodeId);
	}
	
	/** Get the state of node with <code>nodeId</code>.
	 * 
	 * @param nodeId node id
	 * @return state of the node
	 * @throws ALDWorkflowException  if the node with <code>nodeId</code> does not exist.
	 */
	public ALDWorkflowNodeState getState( ALDWorkflowNodeID nodeId) throws ALDWorkflowException {
		return getNode(nodeId).getState();
	}
	
	/**
	 * @return the workflowContext
	 */
	public ALDWorkflowContextType getWorkflowContext() {
		return workflowContext;
	}

	/**
	 * Returns the names of all required input parameters of the operator object associated with the node
	 * which are not linked and have a value of null.
	 * 
	 * @param nodeId
	 * @return
	 * @throws ALDWorkflowException
	 */
	public Collection<String> getMissingRequiredInputs( ALDWorkflowNodeID nodeId) throws ALDWorkflowException {
		return getNode(nodeId).getMissingRequiredInputs();
	}


	/** Get the workflow node associated with <code>nodeId</code>.
	 * 
	 * @param nodeId id of workflow node to return
	 * @return node 
	 * @throws ALDWorkflowException if the node with <code>nodeId</code> does not exist.
	 * @throws NullPointerException if <code>nodeId</code> is null.

	 */
	public ALDWorkflowNode getNode( ALDWorkflowNodeID nodeId) throws ALDWorkflowException {	
		if ( nodeId == null )
			throw( new NullPointerException( "ALDWorkflow::getNode nodeId is null"));
		
		if ( ALDWorkflow.mapNodeIdToNode(nodeId) == null) {
			throw( new ALDWorkflowException( WorkflowExceptionType.NODE_DOESNOT_EXIST, 
					"NodeId <" + nodeId.toString() + "> " +	nodeId));
		}
		return nodeIdToNode.get(nodeId);
	}
	
	/** Return a collection of all nodes of this workflow
	 * 
	 * @return all nodes of this workflow
	 */
	public Collection<ALDWorkflowNode> getNodes() {
		LinkedList<ALDWorkflowNode> nodeList = new LinkedList<ALDWorkflowNode>();
		for ( ALDWorkflowNode node : this.nodes)
			nodeList.add(node);
			
		return nodeList;
	}
	
	/** Get the workflow edge associated with <code>edgeId</code>.
	 *
	 * @param edgeId
	 * @return
	 * @throws ALDWorkflowException if the edge with <code>edgeId</code> does not exist
	 */
	public ALDWorkflowEdge getEdge( ALDWorkflowEdgeID edgeId) throws ALDWorkflowException {
		if ( edgeId == null ) 
			throw( new NullPointerException( "ALDWorkflow:getEdge edgeId is null"));
		
		ALDWorkflowEdge edge = ALDWorkflow.mapeEdgeIdToEdge(edgeId);
		if ( edge == null ) {
			throw( new ALDWorkflowException( WorkflowExceptionType.EDGE_DOESNOT_EXIST, 
					"EdgeId <" + edgeId.toString() + "> " +	edgeId));
		}
		return edge;
	}
	
	/** Get the workflow edge connecting given edges and parameters.
	 * 
	 * @param sourceNodeId
	 * @param sourceParameterName
	 * @param targetNodeId
	 * @param targetParameterName
	 * @return the edge or null if the edge does not exists
	 */
	public ALDWorkflowEdge getEdge( ALDWorkflowNode sourceNode, String sourceParameterName,
			ALDWorkflowNode targetNode, String targetParameterName) {
		for ( ALDWorkflowEdge edge : edges ) {
			if ( edge.getSourceNode() == sourceNode &&
					edge.getTargetNode() == targetNode &&
					edge.getSourceParameterName().equals( sourceParameterName) &&
					edge.getTargetParameterName().equals(targetParameterName)) {
				return edge;
			}
		}
		return null;
	}
	
	/** Return a collection of all edges of this workflow
	 * 
	 * @return all edges of this workflow
	 */
	public Collection<ALDWorkflowEdge> getEdges() {
		LinkedList<ALDWorkflowEdge> edgeList = new LinkedList<ALDWorkflowEdge>();
		for ( ALDWorkflowEdge edge : this.edges)
			edgeList.add(edge);
			
		return edgeList;
	}
	

	/**
	 * Return the NodeId of the source node of this edge
	 * 
	 * @param edgeId
	 * @return
	 * @throws ALDWorkflowException
	 */
	public ALDWorkflowNodeID getSourceNodeId( ALDWorkflowEdgeID edgeId) throws ALDWorkflowException {
		ALDWorkflowEdge edge = this.getEdge( edgeId);
		return ALDWorkflow.mapNodeToNodeId( edge.getSourceNode());
	}
	
	/**
	 * Return the source parameter name of this edge
	 * 
	 * @param edgeId
	 * @return
	 * @throws ALDWorkflowException
	 */
	public String getSourceParameterName( ALDWorkflowEdgeID edgeId) throws ALDWorkflowException {
		ALDWorkflowEdge edge = this.getEdge( edgeId);
		return edge.getSourceParameterName();
	}
	
	/**
	 * Return the NodeId of the target node of this edge
	 * 
	 * @param edgeId
	 * @return
	 * @throws ALDWorkflowException
	 */
	public ALDWorkflowNodeID getTargetNodeId( ALDWorkflowEdgeID edgeId) throws ALDWorkflowException {
		ALDWorkflowEdge edge = this.getEdge( edgeId);
		return ALDWorkflow.mapNodeToNodeId( edge.getTargetNode());
	}
	
	/**
	 * Return the target parameter name of this edge
	 * 
	 * @param edgeId
	 * @return
	 * @throws ALDWorkflowException
	 */
	public String getTargetParameterName( ALDWorkflowEdgeID edgeId) throws ALDWorkflowException {
		ALDWorkflowEdge edge = this.getEdge( edgeId);
		return edge.getTargetParameterName();
	}
	
	/**
	 * Return the current mapping of NodeIds to integer ids.
	 * Note: this mapping may change during manipulation of the workflow,
	 * i.e. the same NodeId may have different mappings if the workflow has changed
	 * is graph structure.
	 * The set of ids is a contiguous interval of non negative integers starting a zero.
	 * 
	 * @return current mapping of NodeIds to integer ids
	 */
	public HashMap<ALDWorkflowNodeID, Integer> getMappingNodeIdToInteger() {
		HashMap<ALDWorkflowNodeID, Integer> res = new 	HashMap<ALDWorkflowNodeID, Integer>( this.nodes.size());
		Integer i = 0;
		for ( ALDWorkflowNode node : this.nodes) {
			res.put( ALDWorkflow.mapNodeToNodeId( node), i);
			i++;
		}
		return res;
	} 
	
	/**
	 * Return the current mapping of integer ids to NodeIds.
	 * Note: this mapping may change during manipulation of the workflow,
	 * i.e. the same NodeId may have different mappings if the workflow has changed
	 * is graph structure.
	 * The set of ids is a contiguous interval of non negative integers starting a zero.
	 * 
	 * @return current mapping of integer ids to NodeIds
	 */
	public HashMap<Integer,ALDWorkflowNodeID> getMappingIntegerToNodeId() {
		HashMap<Integer, ALDWorkflowNodeID> res = new 	HashMap< Integer, ALDWorkflowNodeID>( this.nodes.size());
		Integer i = 0;
		for ( ALDWorkflowNode node : this.nodes) {
			res.put( i, ALDWorkflow.mapNodeToNodeId( node));
			i++;
		}
		return res;
	}
	
	/**
	 * Return the current mapping of EdgeIds to integer ids.
	 * Note: this mapping may change during manipulation of the workflow,
	 * i.e. the same EdgeId may have different mappings if the workflow has changed
	 * is graph structure.
	 * The set of ids is a contiguous interval of non negative integers starting at zero.
	 * 
	 * @return current mapping of EdgeIds to integer ids
	 */
	public HashMap<ALDWorkflowEdgeID, Integer> getMappingEdgeIdToInteger() {
		HashMap<ALDWorkflowEdgeID, Integer> res = new 	HashMap<ALDWorkflowEdgeID, Integer>( this.edges.size());
		Integer i = 0;
		for ( ALDWorkflowEdge edge : this.edges) {
			res.put( ALDWorkflow.mapEgdeToEdgeId( edge), i);
			i++;
		}
		return res;
	}

	/**
	 * Return the current mapping of integer ids to EdgeIds.
	 * Note: this mapping may change during manipulation of the workflow,
	 * i.e. the same EdgeId may have different mappings if the workflow has changed
	 * is graph structure.
	 * The set of ids is a contiguous interval of non negative integers starting a zero.
	 * 
	 * @return current mapping of integer ids to EdgeIds
	 */
	public HashMap<Integer,ALDWorkflowEdgeID> getMappingIntegerToEdgeId() {
		HashMap<Integer, ALDWorkflowEdgeID> res = new 	HashMap< Integer, ALDWorkflowEdgeID>( this.edges.size());
		Integer i = 0;
		for ( ALDWorkflowEdge edge : this.edges) {
			res.put( i, ALDWorkflow.mapEgdeToEdgeId( edge));
			i++;
		}
		return res;
	}
	
	/** Return the nodeId of the idx-th node according to the order of the nodes
	 * in the external representation resulting from (last) loading this workflow.
	 * The order is identical as return from {@link #getNodeIds()} at the time of saving
	 * the workflow represented externally.
	 * 
	 * @param idx
	 * @return
	 */
	public ALDWorkflowNodeID getNodeIdDuringLoading( Integer idx) {
		if ( loadIndexToNodeId == null || (idx < 0 || idx >= loadIndexToNodeId.length ) )
			return null;
		else
			return loadIndexToNodeId[idx];
	}

	/** Return the edgeId of the idx-th edge according to the order of the edges
	 * in the external representation resulting from (last) loading this workflow.
	 * The order is identical as return from {@link #getEdgeIds()} at the time of saving
	 * the workflow represented externally.
	 * 
	 * @param idx
	 * @return
	 */
	public ALDWorkflowEdgeID getEdgeIdDuringLoading( Integer idx) {
		if ( idx < 0 || idx >= loadIndexToEdgeId.length )
			return null;
		else
			return loadIndexToEdgeId[idx];
	}

	// ===================================================================
	// Methods which manipulate the workflow, e.g. add or remove nodes and edges, state of nodes
	
	/** Add a new operator node to this workflow.
	 * 
	 * @param opName Name of operator  to add in the new node
	 * @throws ALDWorkflowException if the operator cannot be instantiated
	 */
	public synchronized ALDWorkflowNodeID createNode( String opName) throws ALDWorkflowException {
		if ( debug >= 1 ) {
			System.out.println( "ALDWorkflow::createNode for " + opName);
		}
		
		try {
			return this.createNode((ALDOperator) (Class.forName(opName).newInstance()));
		} catch (Exception e) {
			throw( new ALDWorkflowException(WorkflowExceptionType.INSTANTIATION_ERROR, 
					"ALDWorkflow::createNode cannot instantiate <" + opName + ">"));
		}
	}

	/** Add a new operator node to this workflow.
	 * 
	 * @param opName Name of operator  to add in the new node
	 * @throws ALDWorkflowException if the operator cannot be instantiated
	 */
	public synchronized ALDWorkflowNodeID createNode( ALDOperatorLocation location) throws ALDWorkflowException {
		if ( debug >= 1 ) {
			System.out.println( "ALDWorkflow::createNode for " + location.getName());
		}
		
		try {
			return this.createNode(location.createOperator());
		} catch (Exception e) {
			throw( new ALDWorkflowException(WorkflowExceptionType.INSTANTIATION_ERROR, 
					"ALDWorkflow::createNode cannot instantiate <" + location.getName() + ">"));
		}
	}

	/** Add a new operator to this workflow.
	 * 
	 * @param op Operator object to add in the new node
	 * @throws ALDWorkflowException if on OP_RUNNER context an we get more then one node
	 */
	public synchronized ALDWorkflowNodeID createNode( ALDOperator op) throws ALDWorkflowException {
		if ( op == null ) 
			throw( new NullPointerException( "ALDWorkflow::createNode operator is null"));
			
		if ( debug >= 1 ) {
			System.out.println( "ALDWorkflow::createNode for " + op.getName() + " (" + op + ")");
		}
		
		// in the OP_RUNNER context we allow only one node
		if ( this.workflowContext == ALDWorkflowContextType.OP_RUNNER &&
				nodes.size() == 1 ) {
			throw( new ALDWorkflowException( WorkflowExceptionType.ILLEGAL_GRAPH_STRUCTURE, 
					"In OP_RUNNER context only one node is allowed"));

		}
			
		ALDWorkflowNode node = new ALDWorkflowNode( this, op);
		this.addNode( node);
		op.addOperatorExecutionProgressEventListener( this);
		
		if ( node.isConfigured()) {
			// since the new node has no edges it is runnable iff configured
			node.setState(ALDWorkflowNodeState.RUNNABLE);
		}

		if ( this.workflowContext == ALDWorkflowContextType.OP_RUNNER &&
				op instanceof ALDOperatorControllable) {
			ALDOperatorControllable controlableOp = (ALDOperatorControllable)op;
			if ( debug >= 1 ) {
				System.out.println("ALDWorkflow::createNode register operator as listener to the workflow");
			}
			this.addALDConfigurationEventListener( controlableOp);
			this.addALDControlEventListener(controlableOp);
		}

		fireALDWorkflowEvent( 
				new ALDWorkflowEvent(this, ALDWorkflowEventType.ADD_NODE,ALDWorkflow.mapNodeToNodeId(node) ));
		return ALDWorkflow.mapNodeToNodeId(node);
	}
	
	/**
	 * Copy this node.
	 * 
	 * @param nodeId
	 * @param retainInEdges copy also all incoming edges
	 * @param retainParameterValues copy the values of all input parameters
	 * @return nodeId of the new copy
	 * @throws ALDWorkflowException
	 */
	public synchronized ALDWorkflowNodeID copyNode( ALDWorkflowNodeID nodeId,
			boolean retainInEdges, boolean retainParameterValues) throws ALDWorkflowException {
		if ( debug >= 1 ) {
			System.out.println( "ALDWorkflow::copyNode copy node <" + nodeId + ">");
		}
		ALDWorkflowNode node = ALDWorkflow.mapNodeIdToNode(nodeId);
		ALDOperator oldOp = node.getOperator();
		if ( oldOp instanceof ALDWorkflow ) {
			throw( new ALDWorkflowException( WorkflowExceptionType.INSTANTIATION_ERROR, 
					"Cannot copy workflows yet"));
		}
		
		ALDOperator newOp;
		try {
			newOp = oldOp.getClass().newInstance();
		} catch (Exception e1) {
			throw( new ALDWorkflowException( WorkflowExceptionType.INSTANTIATION_ERROR, 
					"Cannot instantiate new operator in copyNode"));
		}
		
		ALDWorkflowNodeID newNodeId = this.createNode(newOp);
		
		if ( retainInEdges) {
			for ( ALDWorkflowEdge edge : node.getInEdges() ) {
				this.createEdge(ALDWorkflow.mapNodeToNodeId(edge.getSourceNode()), 
						edge.getSourceParameterName(), newNodeId, edge.getTargetParameterName());
			}
		}
		
		if ( retainParameterValues ) {
			for ( String name : newOp.getInInoutNames(null) ) {
				try {
					newOp.setParameter(name, oldOp.getParameter(name));
				} catch (ALDOperatorException e) {
					throw( new ALDWorkflowException( WorkflowExceptionType.FATAL_INTERNAL_ERROR, 
							"ALDWorkflow::copyNode cannot copy parameter values"));
				}
			}
		}
		
		fireALDWorkflowEvent( 
				new ALDWorkflowEvent(this, ALDWorkflowEventType.COPY_NODE,ALDWorkflow.mapNodeToNodeId(node) ));
		return newNodeId;	
	}

	/** Remove a node with all incoming and outgoing edges from the workflow.
	 * 
	 * @param nodeId id of the node to be removed
	 * @throws ALDWorkflowException if the node with <code>nodeId</code> does not exist
	 */
	public synchronized void removeNode( ALDWorkflowNodeID nodeId) throws ALDWorkflowException {
		if ( nodeId == null )
			throw( new NullPointerException( "ALDWorkflow::removeNode nodeId is null"));
		
		if ( debug >= 1 ) {
			System.out.println( "ALDWorkflow::removeNode " + nodeId.id);
		}
		
		ALDWorkflowNode node = this.getNode(nodeId);

		// first remember descendants
		Collection<ALDWorkflowNode> oldDescendants;
		oldDescendants = node.getDescendants();

		// remove incoming  edges
		for ( ALDWorkflowEdge edge : node.getInEdges()) {
			// note: we cannot use removeEdge
			
			// remove this edge from the outEdges of the source 
			edge.getSourceNode().getOutEdges().remove(edge);

			// remove the edge from the workflow
			ALDWorkflowEdgeID edgeId = ALDWorkflow.mapEgdeToEdgeId( edge);
			this.edges.remove( edge);
			ALDWorkflow.edgeIdToEdge.remove( edgeId);
			ALDWorkflow.edgeIdToEdge.remove( edge);
			
			// fire an event
			fireALDWorkflowEvent( new ALDWorkflowEvent( this, ALDWorkflowEventType.DELETE_EDGE, edgeId));
		}
		
		// remove outgoing  edges
		for ( ALDWorkflowEdge edge : node.getOutEdges()) {
			// note: we cannot use removeEdge;

			// remove this edge from the  inEdges of the target
			edge.getTargetNode().getInEdges().remove(edge);
			
			if ( this.resetDisconnectedInput &&
					// note: we might remove and edge because source 
					edge.getTargetNode().getOperator().hasParameter(edge.getTargetParameterName()) ) {
				edge.getTargetNode().resetParameter(edge.getTargetParameterName());
			}
						
			// remove the edge from the workflow
			ALDWorkflowEdgeID edgeId = ALDWorkflow.mapEgdeToEdgeId( edge);
			this.edges.remove( edge);
			ALDWorkflow.edgeIdToEdge.remove( edgeId);
			ALDWorkflow.edgeIdToEdge.remove( edge);
						
			// fire an event
			fireALDWorkflowEvent( new ALDWorkflowEvent( this, ALDWorkflowEventType.DELETE_EDGE, edgeId));
		}
		
		// finally remove the node itself
		this.nodes.remove( node);
		ALDWorkflow.nodeIdToNode.remove(nodeId);
		ALDWorkflow.nodeToNodeId.remove( node);
		
		fireALDWorkflowEvent( new ALDWorkflowEvent(this, ALDWorkflowEventType.DELETE_NODE, nodeId));
		
		// check states changes
		updateStates( oldDescendants, true);
	}

	/** Notify the workflow that parameters of the operator object associated with node
	 * <code>nodeId</code> have changed.
	 * 
	 * @param nodeId
	 * @throws ALDWorkflowException if the node with <code>nodeId</code> does not exist.
	 */
	public void nodeParameterChanged( ALDWorkflowNodeID nodeId) throws ALDWorkflowException {
		if ( nodeId == null )
			throw( new NullPointerException( "ALDWorkflow::nodeParameterChanged nodeId is null"));
		
		ALDWorkflowNode node = this.getNode(nodeId);
		ALDWorkflowNodeState oldState = node.getState();

		if ( debug >= 1 ) {
			System.out.println( "ALDWorkflow::nodeParameterChanged of node with id <" + nodeId.id +
					"> old state " + oldState + " configured is now " + node.isConfigured());
		}
		
        // first remove edges involving parameters which have been removed
		// in edges
        ALDOperator op = node.getOperator();
        LinkedList<ALDWorkflowEdge> edgesToRemove = new LinkedList<ALDWorkflowEdge>();
        for ( ALDWorkflowEdge edge : node.getInEdges()) {
                String pName = edge.getTargetParameterName();
                if ( ! op.hasParameter(pName) ) {
                	edgesToRemove.add( edge);
                }
        }
        
        for ( ALDWorkflowEdge edge : edgesToRemove )
        	this.removeEdge(ALDWorkflow.mapEgdeToEdgeId( edge));
        
        // ... and now the out edges      
        edgesToRemove.clear();
        for ( ALDWorkflowEdge edge : node.getOutEdges()) {
                String pName = edge.getSourceParameterName();
                if ( ! op.hasParameter(pName) ) {
                	edgesToRemove.add( edge);
                }
        }

        for ( ALDWorkflowEdge edge : edgesToRemove )
        	this.removeEdge(  ALDWorkflow.mapEgdeToEdgeId( edge));

        // next update the states of downstream nodes
        updateState( node);

        // and fire event
        LinkedList<ALDWorkflowNodeID> list = new LinkedList<ALDWorkflowNodeID>();
        list.add( nodeId);
        fireALDWorkflowEvent(new ALDWorkflowEvent(this, ALDWorkflowEventType.NODE_PARAMETER_CHANGE, list));

	}
	

	/** This method calls {@link createEdge} with the argument <code>allowDataConversion = false</code>.
	 * 
	 * 
	 * @param sourceNodeId
	 * @param sourceParameterName
	 * @param targetNodeId
	 * @param targetParameterName
	 * @throws ALDWorkflowException if edge is not allowed 
	 */

	public synchronized ALDWorkflowEdgeID createEdge( ALDWorkflowNodeID sourceNodeId, String sourceParameterName,
			ALDWorkflowNodeID targetNodeId, String targetParameterName) throws ALDWorkflowException {
		return createEdge(sourceNodeId, sourceParameterName, targetNodeId, targetParameterName, false);
	}
	
	/** Add an edge representing the data flow between the parameter
	 * <code>sourceParameterName</code> in the operator represented by the node
	 * with  <code>sourceNodeId</code>
	 * to the parameter <code>targetParameterName</code> in the operator represented by the node with 
	 * <code>targetNodeId</code>.
	 * If a nodeId is null then this nodeId refers to this workflow,
	 * i.e. connects a parameter of the workflow.
	 * Otherwise source and target node need to be a node of this workflow or represent the workflow itself.
	 * 
	 * The following restrictions apply for the edge to be created:
	 * <ol>
	 * <li>Direction of the parameters to connect
	 * <ol>
	 * <li> Source parameter
	 * <br>
	 * If the source node represents the workflow, the source parameter 
	 * needs to be of direction <code>IN</code> or
	 * <code>INOUT</code>.
	 * Otherwise
	 * it needs to be of direction <code>OUT</code> or
	 * <code>INOUT</code>.</li>
	 * <li> Target parameter
	 * <br>
	 * If the target node represents the workflow, the target parameter 
	 * needs to be of direction <code>OUT</code> or
	 * <code>INOUT</code>.
	 * Otherwise
	 * it needs to be of direction <code>IN</code> or
	 * <code>INOUT</code>.</li>
	 * </ol>
	 * </li>
	 * <li>
	 * The parameter in the target operator must not already be connected by an edge.</li>
	 * <li>
	 * The java types associated with both parameters must be compatible.
	 * I.e. the target parameter must be assignable by the source parameter.
	 * if <code>allowDataConversion</code> the parameters are also compatible if a data converter is available</li>
	 * <li>
	 * No cycles may be introduced into the workflow</li>
	 * </ol>
	 * 
	 * @param sourceNodeId
	 * @param sourceParameterName
	 * @param targetNodeId
	 * @param targetParameterName
	 * @param allowDataConversion if true the edge is allowed in case of non assignable parameters if
	 *             a data converter is available
	 * @return
	 * @throws ALDWorkflowException
	 */
	public synchronized ALDWorkflowEdgeID createEdge( ALDWorkflowNodeID sourceNodeId, String sourceParameterName,
			ALDWorkflowNodeID targetNodeId, String targetParameterName,
			Boolean allowDataConversion) throws ALDWorkflowException {
		
		if ( sourceParameterName == null ) 
			throw( new NullPointerException( "ALDWorkflow::createEdge sourceParameterName is null"));
		if ( targetParameterName == null) 
			throw( new NullPointerException( "ALDWorkflow::createEdge targetParameterName is null"));

		// get source and target node and check consistency of hierarchy
		ALDWorkflowNode sourceNode;
		if ( sourceNodeId == null) {
			sourceNodeId = ALDWorkflow.mapNodeToNodeId(this.interiorShadowNode);
		}
		sourceNode = this.getNode(sourceNodeId);
		if ( sourceNode.getOperator() == this) {
			sourceNode = this.interiorShadowNode;
		}
		if ( sourceNode != this.interiorShadowNode &&
				! this.nodes.contains(sourceNode)) {
			throw( new ALDWorkflowException( WorkflowExceptionType.EDGE_CREATE_FAILED, 
					"Source node not contained in this workflow nor the workflow itself"));
		}

		ALDWorkflowNode targetNode;
		if ( targetNodeId == null) {
			targetNodeId = ALDWorkflow.mapNodeToNodeId(this.interiorShadowNode);
		}
		targetNode= this.getNode(targetNodeId);
		if ( targetNode.getOperator() == this) {
			targetNode = this.interiorShadowNode;
		}
		if ( targetNode != this.interiorShadowNode  &&
				! this.nodes.contains(targetNode)) {
			throw( new ALDWorkflowException( WorkflowExceptionType.EDGE_CREATE_FAILED, 
					"Target node not contained in this workflow nor the workflow itself"));
		}
		
		if ( ! sourceNode.getOperator().getParameterNames().contains(sourceParameterName) ) {
			throw( new ALDWorkflowException( WorkflowExceptionType.EDGE_CREATE_FAILED, 
					"Source node does not contained a parameter <" + sourceParameterName + ">"));			
		}
		
		if ( ! targetNode.getOperator().getParameterNames().contains(targetParameterName) ) {
			throw( new ALDWorkflowException( WorkflowExceptionType.EDGE_CREATE_FAILED, 
					"Target node does not contained a parameter <" + targetParameterName + ">"));			
		}
		
		if ( debug >=1 ) {
			System.out.println( "ALDWorkflow::createEdge <" + 
					sourceNode.getId() + "> (" + sourceParameterName + ") --> <" +
					targetNode.getId() + "> (" + targetParameterName + ") ");
		}

		// now tentatively create and add the edge
		ALDWorkflowEdge edge = new ALDWorkflowEdge( sourceNode, sourceParameterName, 
				targetNode, targetParameterName);
		this.addEdge( edge);

		try { 
			//  checks restrictions 
			edgeAllowed( edge, allowDataConversion);
		} catch (ALDWorkflowException ex) {
			// not allowed
			sourceNode.outEdges.remove(edge);
			targetNode.inEdges.remove(edge);

			this.edges.remove( edge);
			ALDWorkflow.edgeIdToEdge.remove(ALDWorkflow.mapEgdeToEdgeId(edge)); 
			ALDWorkflow.edgeToEdgeId.remove(edge);

			throw ex;
		}

		fireALDWorkflowEvent( 
				new ALDWorkflowEvent( this, ALDWorkflowEventType.ADD_EDGE, ALDWorkflow.mapEgdeToEdgeId(edge)));

		// check if states have changed
		updateState( targetNode);

		if ( debug >= 3) {
			this.print();
		}

		return ALDWorkflow.mapEgdeToEdgeId(edge);
	}
	
	/** Remove an edge from the workflow.
	 * 
	 * @param edgeId id of the edge to remove
	 * @throws ALDWorkflowException if edge is not allowed or the edge with <code>edgeId</code> does not exist.
	 */
	public synchronized void removeEdge( ALDWorkflowEdgeID edgeId) throws ALDWorkflowException {
		if ( edgeId == null )
			throw( new NullPointerException( "ALDWorkflow::removeEdge edgeId is null"));
		
		if ( debug >= 1) {
			System.out.println( "ALDWorkflow::removeEdge remove " + edgeId.id);
		}
		
		ALDWorkflowEdge edge = this.getEdge( edgeId);
		ALDWorkflowNode sourceNode = edge.getSourceNode();
		ALDWorkflowNode targetNode = edge.getTargetNode();

		// remove this edge from the outEdges of the source and inEdges of the target
		sourceNode.outEdges.remove(edge);
		targetNode.inEdges.remove(edge);
		
		if ( this.resetDisconnectedInput &&
				// mind, we migth remove this edge as a parameter has been removed
				targetNode.getOperator().hasParameter(edge.getTargetParameterName())) {
			targetNode.resetParameter(edge.getTargetParameterName());
		}

		// remove the edge from the workflow
		this.edges.remove( edge);
		ALDWorkflow.edgeIdToEdge.remove(edgeId);
		ALDWorkflow.edgeToEdgeId.remove(edge);
		
		// fire an event
		fireALDWorkflowEvent( new ALDWorkflowEvent(this, ALDWorkflowEventType.DELETE_EDGE, edgeId));
		
		// check state changes
		// the target node may now be unconfigured
		if ( debug >= 2 ) {
			System.out.println( "    ALDWorkflow::removeEdge targetNode old state " +
					targetNode.getState() + ", configured is now " + targetNode.isConfigured());
		}
		
		// check if states have changed
		updateState( targetNode);
	}
	
	/** Call <code>redirectSource</code> with with the argument <code>allowDataConversion = false</code>.
	 * The same restrictions as for {@link createEdge} apply.
	 * 
	 * @param edgeId
	 * @param newSourceNodeId
	 * @param newSourceParameterName
	 * @param allowDataConversion if true the edge is allowed in case of non assignable parameters if
	 *             a data converter is available
	 * @throws ALDWorkflowException if edge is not allowed or the edge with edgeId does not exist
	 */
	public synchronized void redirectSource( ALDWorkflowEdgeID edgeId, ALDWorkflowNodeID newSourceNodeId,
			String newSourceParameterName) throws ALDWorkflowException {
		redirectSource( edgeId, newSourceNodeId, newSourceParameterName, false);

	}
	/** Redirect the source of an edge.
	 * The same restrictions as for {@link createEdge} apply.
	 * 
	 * @param edgeId
	 * @param newSourceNodeId
	 * @param newSourceParameterName
	 * @param allowDataConversion if true the edge is allowed in case of non assignable parameters if
	 *             a data converter is available
	 * @throws ALDWorkflowException if edge is not allowed or the edge with edgeId does not exist
	 */
	public synchronized void redirectSource( ALDWorkflowEdgeID edgeId, ALDWorkflowNodeID newSourceNodeId,
			String newSourceParameterName, Boolean allowDataConversion) throws ALDWorkflowException {
		if ( edgeId == null )
			throw( new NullPointerException( "ALDWorkflow::redirectSource edgeId is null"));
		if ( newSourceParameterName == null )
			throw( new NullPointerException( "ALDWorkflow::redirectSource sourceParameterName is null"));

		if ( debug >= 1) {
			System.out.println( "ALDWorkflow::redirectSource edge " + edgeId.id +
					" new source <" + newSourceNodeId.id + "> for parameter " + newSourceParameterName);
		}
		
		ALDWorkflowNode newSourceNode;
		if ( newSourceNodeId == null) {
			newSourceNodeId = ALDWorkflow.mapNodeToNodeId(this.interiorShadowNode);
		}
		newSourceNode = this.getNode(newSourceNodeId);
		if ( newSourceNode.getOperator() == this) {
			newSourceNode = this.interiorShadowNode;
		}
		if ( newSourceNode != this.interiorShadowNode &&
				! this.nodes.contains(newSourceNode)) {
			throw( new ALDWorkflowException( WorkflowExceptionType.SAVE_FAILED, 
					"Source node not contained in this workflow nor the workflow itself"));
		}

		// tentatively redirect the edge
		ALDWorkflowEdge edge = this.getEdge( edgeId);		
		ALDWorkflowNode oldSourceNode = edge.getSourceNode();
		String oldSourceParameterName = edge.getSourceParameterName();
		edge.redirectSource( newSourceNode, newSourceParameterName);
		
		try { 
			//  checks restrictions 
			edgeAllowed( edge, allowDataConversion);
		} catch (ALDWorkflowException ex) {
			// not allowed
			edge.redirectSource( oldSourceNode, oldSourceParameterName);

			throw ex;
		}

		fireALDWorkflowEvent( new ALDWorkflowEvent(this, ALDWorkflowEventType.REDIRECT_EDGE_SOURCE, edgeId));
		
		// check state changes
		updateState( edge.getTargetNode());
	}
	
	/** Calls {@link redirectTarget} with the argument <code>allowDataConversion = false</code>
	 * The same restrictions as for {@link createEdge} apply.
	 * 
	 * @param edgeId
	 * @param newTargetNodeId
	 * @param newTargetParameterName
	 * @throws ALDWorkflowException if edge is not allowed or the edge with edgeId does not exist
	 */
	public synchronized void redirectTarget( ALDWorkflowEdgeID edgeId, ALDWorkflowNodeID newTargetNodeId,
			String newTargetParameterName) throws ALDWorkflowException {
		redirectTarget(edgeId, newTargetNodeId, newTargetParameterName, false);
	}
			
	/** Redirect the target of an edge.
	 * The same restrictions as for {@link createEdge} apply.
	 * 
	 * @param edgeId
	 * @param newTargetNodeId
	 * @param newTargetParameterName
	 * @param allowDataConversion if true the edge is allowed in case of non assignable parameters if
	 *             a data converter is available
	 * @throws ALDWorkflowException if edge is not allowed or the edge with edgeId does not exist
	 */
	public synchronized void redirectTarget( ALDWorkflowEdgeID edgeId, ALDWorkflowNodeID newTargetNodeId,
			String newTargetParameterName, Boolean allowDataConversion) throws ALDWorkflowException {
		
		if ( edgeId == null )
			throw( new NullPointerException( "ALDWorkflow::redirectTarget edgeId is null"));
		if ( newTargetParameterName == null )
			throw( new NullPointerException( "ALDWorkflow::redirectTarget targetParameterName is null"));

		if ( debug >= 1) {
			System.out.println( "ALDWorkflow::redirectTarget edge <" + edgeId.id +
					"> new source >" + newTargetNodeId.id + "> for parameter " + newTargetParameterName);
		}
		
		ALDWorkflowNode newTargetNode;
		if ( newTargetNodeId == null) {
			newTargetNodeId = ALDWorkflow.mapNodeToNodeId(this.interiorShadowNode);
		}
		newTargetNode= this.getNode(newTargetNodeId);
		if ( newTargetNode.getOperator() == this) {
			newTargetNode = this.interiorShadowNode;
		}
		if ( newTargetNode != this.interiorShadowNode  &&
				! this.nodes.contains(newTargetNode)) {
			throw( new ALDWorkflowException( WorkflowExceptionType.SAVE_FAILED, 
					"Target node not contained in this workflow nor the workflow itself"));
		}
		
		// tentatively redirect the edge
		ALDWorkflowEdge edge = this.getEdge( edgeId);
		ALDWorkflowNode oldTargetNode = edge.getTargetNode();
		String oldTargetParameterName = edge.getTargetParameterName();
		edge.redirectTarget( newTargetNode, newTargetParameterName);
		
		try { 
			// checks restrictions 
			edgeAllowed( edge, allowDataConversion);
		} catch (ALDWorkflowException ex) {
			// not allowed
			edge.redirectTarget( oldTargetNode, oldTargetParameterName);

			throw ex;
		}

		fireALDWorkflowEvent( new ALDWorkflowEvent(this, ALDWorkflowEventType.REDIRECT_EDGE_TARGET, edgeId));
		
		if ( this.resetDisconnectedInput ) {
			oldTargetNode.resetParameter(oldTargetParameterName);
		}

		// check state changes
		LinkedList<ALDWorkflowNode> nodeList = new LinkedList<ALDWorkflowNode>();
		nodeList.add( oldTargetNode);
		nodeList.add( newTargetNode);
		updateStates(nodeList, true);
	}
	
	// ===========================================================================================================
	// load and save support
	
	/**
	 * Save this workflow to file.
	 * Does not fire an LOAD event.
	 * 
	 * @param filename
	 * @throws ALDWorkflowException  if file cannot be opened for writing or serialization fails
	 */
	public void save( String filename) throws ALDWorkflowException  {
		save(filename, false);
	}

	/**
	 * Save this workflow to file.
	 * 
	 * @param filename
	 * @param doFireEvent if true an appropriate event is fired
	 * @throws ALDWorkflowException  if file cannot be opened for writing or serialization fails
	 */
	public void save( String filename, boolean doFireEvent) throws ALDWorkflowException  {
		if ( debug >= 2) {
			System.out.println( "ALDWorkflow.save to file " + filename);
		}
		
		this.save( new File( filename), doFireEvent);
	}
	
	/**
	 * Save this workflow to file.
	 * Does not fire an LOAD event.
	 * 
	 * @param filename
	 * @throws ALDWorkflowException if file cannot be opened for writing or serialization fails
	 */
	public void save( File file) throws ALDWorkflowException  {
		save(file, false);
	}

	/**
	 * Save this workflow to file.
	 * @param doFireEvent if true an appropriate event is fired
	 * @param filename
	 * 
	 * @throws ALDWorkflowException if file cannot be opened for writing or serialization fails
	 */
	public void save( File file, boolean doFireEvent) throws ALDWorkflowException  {
		if ( debug >= 2) {
			System.out.println( "ALDWorkflow.save to file " + file);
		}

		
		// rename workflow
		String filename = file.getAbsolutePath();
		filename = ALDFilePathManipulator.removeExtension(filename);
		String[] newFullname = filename.split("/");
		String newName = newFullname[newFullname.length-1];

		if ( this.getName().equals(untitledWorkflowName) || ! this.getName().equals( newName)) {	
			// currently untitled
			this.setName( newName);
		} 

		try { 
				ALDDataIOManagerXmlbeans.writeXml(file.getAbsolutePath(), this);
				System.out.println("saved with xmlbeans");
		} catch (Exception e1) {
			throw( new ALDWorkflowException( WorkflowExceptionType.SAVE_FAILED, 
					"Serialization failed\n" +
							e1.getMessage()));
		}

		// fire an event
		if ( doFireEvent) {
			ALDWorkflowStorageInfo info = new ALDWorkflowStorageInfo(file.getAbsolutePath(), this);
			fireALDWorkflowEvent(new ALDWorkflowEvent(this, ALDWorkflowEventType.SAVE_WORKFLOW, info));
		}
	}
	
	/**
	 * Load a workflow from file with <code>filename</code> and create a new workflow object.
	 * Does not fire an LOAD event.
	 * 
	 * @param filename
	 * @return the workflow read from file
	 * @throws ALDWorkflowException if file cannot be opened for reading or deserialization fails
	 */
	public static ALDWorkflow load( String filename) throws ALDWorkflowException {
		return load(filename, false);
	}

	/**
	 * Load a workflow from file with <code>filename</code> and create a new workflow object.
	 * 
	 * @param filename
	 * @param doFireEvent if true an appropriate event is fired
	 * @return the workflow read from file
	 * @throws ALDWorkflowException if file cannot be opened for reading or deserialization fails
	 */
	public static ALDWorkflow load( String filename, boolean doFireEvent) throws ALDWorkflowException {
		return load( new File( filename), doFireEvent);
	}
	
	/**
	 * Load a workflow from <code>File</code> and create a new workflow object.
	 * Does not fire an LOAD event.

	 * @param file
	 * @return the workflow read from file
	 * @throws ALDWorkflowException if file cannot be opened for reading or deserialization fails
	 */
	public static ALDWorkflow load( File file) throws ALDWorkflowException {
		return load(file, false);
	}

	/**
	 * Load a workflow from <code>File</code> and create a new workflow object.
	 * @param file
	 * @param doFireEvent if true an appropriate event is fired
	 * @return 
	 * @throws ALDWorkflowException if file cannot be opened for reading or deserialization fails
	 */
	public static ALDWorkflow load( File file, boolean doFireEvent) throws ALDWorkflowException {
		ALDWorkflow	newWorkflow = null;

		try {
			newWorkflow = 
				(ALDWorkflow) ALDDataIOManagerXmlbeans.readXml(file.getAbsolutePath(), 
						ALDWorkflow.class);
//				System.out.println("loaded with xmlbeans");
//		} catch (Exception e) {
//			try {
//
//				in = new FileInputStream( file.getAbsolutePath());
//				try {
//					System.out.println("trying xstream");
//					XStream xstream = new XStream(new DomDriver());
//					newWorkflow = (ALDWorkflow)(xstream.fromXML(in));
//					System.out.println("loaded with xstreams");
//				} catch (Exception e1) {
//					throw( new ALDWorkflowException( WorkflowExceptionType.LOAD_FAILED, 
//							"Deserialization failed\n" +
//									e1.getMessage()));
//				}
//
		} catch (Exception e1) {
			//				System.out.println( "exception in load");
			throw( new ALDWorkflowException( WorkflowExceptionType.LOAD_FAILED, 
					"Can not open file <" + file.getAbsolutePath() +
					"> for reading\n" +
					e1.getMessage()));	

		}		

		//fire an event
		if ( doFireEvent) {
			ALDWorkflowStorageInfo info = new ALDWorkflowStorageInfo(file.getAbsolutePath(), newWorkflow);
			ALDWorkflow.fireALDWorkflowClassEvent(new ALDWorkflowClassEvent(newWorkflow, ALDWorkflowClassEventType.LOAD_WORKFLOW, info));

		}
		return newWorkflow;
	}
	
	/**
	 * Init function for deserialized objects.
	 * <p>
	 * This function is called on an instance of this class being deserialized
	 * from file, prior to handing the instance over to the user. It takes care
	 * of a proper initialization of transient member variables as they are not
	 * initialized to the default values during deserialization.
	 * @return
	 */
	
	@Override
  protected Object readResolve() {
		super.readResolve();

		for ( ALDWorkflowNode  node : this.nodes) {
			ALDWorkflowNodeID nodeId = null;
			nodeId = new ALDWorkflowNodeID();
			ALDWorkflow.nodeIdToNode.put( nodeId, node);
			ALDWorkflow.nodeToNodeId.put( node, nodeId);
			node.setState(ALDWorkflowNodeState.UNCONFIGURED);
		}

		ALDWorkflow.edgeIdToEdge = new HashMap<ALDWorkflowEdgeID, ALDWorkflowEdge>( this.edges.size());
		ALDWorkflow.edgeToEdgeId = new HashMap<ALDWorkflowEdge, ALDWorkflowEdgeID>( this.edges.size());
		
		for ( ALDWorkflowEdge edge : edges) {
			ALDWorkflowEdgeID edgeId = null;
			edgeId = new ALDWorkflowEdgeID();
			ALDWorkflow.edgeIdToEdge.put( edgeId, edge);
			ALDWorkflow.edgeToEdgeId.put( edge, edgeId);
			
			String sourceParameterName = edge.getSourceParameterName();
			String targetParameterName = edge.getTargetParameterName();

			ALDOpParameterDescriptor sourceDescriptor = null;
			ALDOpParameterDescriptor targetDescriptor = null;

			try {
				sourceDescriptor = edge.getSourceNode().getOperator().getParameterDescriptor(sourceParameterName);
			} catch (ALDOperatorException e) {
				System.err.println("ALDWorkflow::classesAllowed fatal error, cannot get source descriptor for <" +
						sourceParameterName + ">");
			}

			try {
				targetDescriptor = edge.getTargetNode().getOperator().getParameterDescriptor(targetParameterName);
			} catch (ALDOperatorException e) {
				System.err.println("ALDWorkflow::classesAllowed fatal error, cannot get target descriptor for <" +
						targetParameterName + ">");
			}
		
			// reload data converter if necessary
			if ( edge.isNeedConverter() ) {
				try { 
					if ( debug >= 2) {
						System.out.println("ALDWorkflow::classesAllowed trying to find a convert for <" +
								sourceDescriptor.getMyclass() + "> --> < " + targetDescriptor.getMyclass());
					}

					// if we find a converter the register it in the edge
					edge.setConverter(	converterManager.
							getProvider(sourceDescriptor.getMyclass(), sourceDescriptor.getField(),
									targetDescriptor.getMyclass(), targetDescriptor.getField()));
				} catch (Exception e) {
					// TODO: need to mark edge as non valid
					System.err.println("ALDWorkflow::readResolve fatal error, cannot get required data converter for <" +
							targetParameterName + ">");
				}
			}
		}
		
		if ( debug >=2 ) {		
			System.out.println( "ALDWorkflow::readResolve workflow = " + this);
			this.print();
		}
		
		this.workflowEventMangerList = new HashMap<ALDWorkflowEventListener, ALDWorkflowEventManager>();
		//workflowEventMangerThreadList = new HashMap<ALDWorkflowEventListener, Thread>();
		this.operatorStatus = OperatorControlStatus.OP_INIT;
		controlEventlistenerList = new EventListenerList();
		configurationEventlistenerList = new EventListenerList();

		// update states of all nodes (without firing events)
		try {
			this.updateStates(this.nodes, false);
		} catch (ALDWorkflowException e) {
			return null;	
		}
		
		return this;
	}


	// ===========================================================================================================
	// Helper methods to check if an edge is allowed
	
	/** Check if this edge is allowed. It is assumed that the edge to check was already
	 * added to the graph. An edge is not allowed, if 
	 * <ol>
	 * <li>it does not connect an OUT or INOUT parameter with an IN or INOUT parameter,</li>
	 * <li>if the target IN or INOUT parameter now has more the one incident links, </li>
	 * <li>if the classes of connected parameters are not compatible, or </li>
	 * <li>if no cycle was introduced
	 * </ol>
	 * Classes are compatible either is they are assignable or a data converter exists and data conversion is allowed
	 * 
	 * @param edge
	 * @param allowDataConversion is data conversion allowed for this edge
	 * @throws ALDWorkflowException if parameters are incompatible
	 */
	private void edgeAllowed(ALDWorkflowEdge edge, Boolean allowDataConversion) throws ALDWorkflowException {

		ALDOpParameterDescriptor  sourceDescriptor = null;
		ALDOpParameterDescriptor targetDescriptor = null;
		ALDWorkflowNode sourceNode = edge.getSourceNode();
		String sourceParameterName = edge.getSourceParameterName();
		ALDWorkflowNode targetNode = edge.getTargetNode();
		String targetParameterName = edge.getTargetParameterName();
	
		try {
			sourceDescriptor = sourceNode.getOperator().getParameterDescriptor(sourceParameterName);
		} catch (ALDOperatorException e) {
			throw( new ALDWorkflowException( WorkflowExceptionType.PARAMETER_ERROR, 
					"Source parameter with name <" + sourceParameterName + "> does not exist"));
		}
		
		try {
			targetDescriptor = targetNode.getOperator().getParameterDescriptor(targetParameterName);
		} catch (ALDOperatorException e) {
			throw( new ALDWorkflowException( WorkflowExceptionType.PARAMETER_ERROR, 
					"Target parameter with name <" + targetParameterName + "> does not exist"));
		}

		// directions
		directionAllowd( sourceNode, sourceDescriptor, targetNode, targetDescriptor);

		// now we have more then one link incident to the target, i.e. input parameter
		if ( 	targetNode.getInEdgesForParameter( targetParameterName).size() > 1) {
			throw( new ALDWorkflowException( WorkflowExceptionType.MULTIPLE_INCIDENT_LINKS, 
					"Target parameter with name <" + targetParameterName + ">"));
		}
		
		// classes
		COMPATIBILITY comp = classesAllowed(sourceDescriptor, targetDescriptor, edge);
		
		if ( comp == COMPATIBILITY.INCOMPATIBLE ) {
			throw( new ALDWorkflowException( WorkflowExceptionType.INCOMPATIBLE_TYPES, 
					"Source Parameter <" + sourceParameterName.toString() + "> (" +
							sourceDescriptor.getMyclass().getName() + ")\n" +
							"Target Parameter <" + targetParameterName.toString() + "> (" +
							targetDescriptor.getMyclass().getName() + ")\n"));
		} else if ( comp == COMPATIBILITY.CONVERTIBLE && ! allowDataConversion ) {
			throw( new ALDWorkflowException( WorkflowExceptionType.INCOMPATIBLE_TYPES_BUT_CONVERTIBLE, 
					"Source Parameter <" + sourceParameterName.toString() + "> (" +
							sourceDescriptor.getMyclass().getName() + ")\n" +
							"Target Parameter <" + targetParameterName.toString() + "> (" +
							targetDescriptor.getMyclass().getName() + ")\n"));			
		}
		
		// cycles
		topSort();
	}
	
	/** Checks if the direction are allowed
	 * <ol>
	 * <li> Source parameter
	 * <br>
	 * If the source node represents the workflow, the source parameter 
	 * needs to be of direction <code>IN</code> or
	 * <code>INOUT</code>.
	 * Otherwise
	 * it needs to be of direction <code>OUT</code> or
	 * <code>INOUT</code>.</li>
	 * <li> Target parameter
	 * <br>
	 * If the target node represents the workflow, the target parameter 
	 * needs to be of direction <code>OUT</code> or
	 * <code>INOUT</code>.
	 * Otherwise
	 * it needs to be of direction <code>IN</code> or
	 * <code>INOUT</code>.</li>
	 * </ol>
	 * @param sourceNode source node
	 * @param sourceDescriptor Descriptor of the source parameter
	 * @param targetNode target node
	 * @param targetDescriptor Descriptor of the target parameter
	 * 
	 * @return
	 * @throws ALDWorkflowException if direction is wrong
	 */
	private void directionAllowd( ALDWorkflowNode sourceNode,
			ALDOpParameterDescriptor  sourceDescriptor, ALDWorkflowNode targetNode, ALDOpParameterDescriptor targetDescriptor) throws ALDWorkflowException  {

		if ( sourceNode != this.interiorShadowNode) {
			if ( sourceDescriptor.getDirection() != Direction.INOUT && 
					sourceDescriptor.getDirection() != Direction.OUT ) {
				throw( new ALDWorkflowException( WorkflowExceptionType.WRONG_SOURCE_PARAMETER_DIRECTION, 
						"Source parameter with name <" + sourceDescriptor.getName() + ">"));
			}
		} else {
			if ( sourceDescriptor.getDirection() != Direction.INOUT && 
					sourceDescriptor.getDirection() != Direction.IN ) {
				throw( new ALDWorkflowException( WorkflowExceptionType.WRONG_SOURCE_PARAMETER_DIRECTION, 
						"Source parameter with name <" + sourceDescriptor.getName() + ">"));
			}
		}
		
		if ( targetNode != this.interiorShadowNode) {
			if ( targetDescriptor.getDirection() != Direction.INOUT && 
					targetDescriptor.getDirection() != Direction.IN ) {
				throw( new ALDWorkflowException( WorkflowExceptionType.WRONG_SOURCE_PARAMETER_DIRECTION, 
						"Target parameter with name <" + targetDescriptor.getName() + ">"));
			}
		} else {
			if ( targetDescriptor.getDirection() != Direction.INOUT && 
					targetDescriptor.getDirection() != Direction.OUT ) {
				throw( new ALDWorkflowException( WorkflowExceptionType.WRONG_SOURCE_PARAMETER_DIRECTION, 
						"Target parameter with name <" + targetDescriptor.getName() + ">"));
			}	
		}
	}

	private enum COMPATIBILITY {
		ASSIGNABLE,
		CONVERTIBLE,
		INCOMPATIBLE
	}
	

	/** Check if class of source parameter is assignable to target parameter
	 * or may be converted by a converter provider or is incompatible
	 * 
	 * @param sourceDescriptor
	 * @param targetDescriptor
	 * @param edge
	 * @return
	 */
	private COMPATIBILITY classesAllowed( ALDOpParameterDescriptor  sourceDescriptor,
			ALDOpParameterDescriptor targetDescriptor, ALDWorkflowEdge edge)  {
		if ( targetDescriptor.getMyclass().isAssignableFrom(sourceDescriptor.getMyclass()) ) {
			return COMPATIBILITY.ASSIGNABLE;
		} else {
			try { 
				if ( debug >= 2) {
					System.out.println("ALDWorkflow::classesAllowed trying to find a convert for <" +
							sourceDescriptor.getMyclass() + "> --> < " + targetDescriptor.getMyclass());
				}

				// if we find a converter then register it in the edge
				edge.setConverter(
						converterManager.getProvider(sourceDescriptor.getMyclass(), 
								sourceDescriptor.getField(), 
								targetDescriptor.getMyclass(), targetDescriptor.getField()));
			} catch (Exception e) {
				// other wise classes are not compatible
				return COMPATIBILITY.INCOMPATIBLE;
			}

			return COMPATIBILITY.CONVERTIBLE;
		}
	}
	
	// ===========================================================================================================
	// methods to run operators/nodes
	
	/**
	 * Run the complete workflow.  A node is only executed if it is not ready.
	 * Immediately returns after starting the execution.
	 * 
	 * @throws ALDWorkflowException if not all nodes are at least runnable or the execution itself fails
	 */
	public void runWorkflow() throws ALDWorkflowException {
		runWorkflow( false, false);
	}

	/**
	 * Run the complete workflow.  A node is only executed if it is not ready.
	 * 
	 * @param waitForCompletion if true wait for completion, otherwise return immediately
	 * @throws ALDWorkflowException if not all nodes are at least runnable or the execution itself fails
	 */
	public void runWorkflow( boolean waitForCompletion) throws ALDWorkflowException {
		runWorkflow(waitForCompletion, false);
	}

	/**
	 * Run the complete workflow.  A node is only executed if it is not ready.
	 * 
	 * @param waitForCompletion if true wait for completion, otherwise return immediately
	 * @param stepRecursively if true OP_STEP events are passed to the child operators
	 * @throws ALDWorkflowException if not all nodes are at least runnable or the execution itself fails
	 */
	public void runWorkflow( boolean waitForCompletion, boolean stepRecursively) throws ALDWorkflowException {
		LinkedList<ALDWorkflowNode> nodes = this.topSort();

		if ( debug >= 1) {
			System.out.println( "ALDWorkflow::runFromNode " + ALDWorkflow.nodeIdsToString(nodes));
		}

		this.executeNonReadyNodes(nodes, waitForCompletion);
	}
	
	/**
	 * Run the given node and all its ancestors. A node is only executed if it is not ready.
	 * Immediately returns after starting the execution.

	 * 
	 * @param nodeId
	 * @throws ALDWorkflowException if not all nodes are at least runnable or the execution itself fails, 
	 *                         or the node with nodeId does not exist
	 */
	public void runNode( ALDWorkflowNodeID nodeId) throws ALDWorkflowException {
		runNode( nodeId, false, false);
	}
	
	/**
	 * Run the given node and all its ancestors. A node is only executed if it is not ready.
	 * 
	 * @param nodeId
	 * @param waitForCompletion if true wait for completion, otherwise return immediately
	 * @throws ALDWorkflowException if not all nodes are at least runnable or the execution itself fails, 
	 *                         or the node with nodeId does not exist
	 */
	public void runNode( ALDWorkflowNodeID nodeId, boolean waitForCompletion) throws ALDWorkflowException {
		runNode(nodeId, waitForCompletion, false);
	}

	/**
	 * Run the given node and all its ancestors. A node is only executed if it is not ready.
	 * 
	 * @param nodeId
	 * @param waitForCompletion if true wait for completion, otherwise return immediately
	 * @param stepRecursively if true OP_STEP events are passed to the child operators
	 * @throws ALDWorkflowException if not all nodes are at least runnable or the execution itself fails, 
	 *                         or the node with nodeId does not exist
	 */
	public void runNode( ALDWorkflowNodeID nodeId, boolean waitForCompletion, boolean stepRecursively) throws ALDWorkflowException {
		if ( nodeId == null )
			throw( new NullPointerException( "ALDWorkflow::nodeParameterChanged nodeId is null"));
		
		ALDWorkflowNode node = this.getNode(nodeId);
		Set<ALDWorkflowNode> allNodesToRun = new HashSet<ALDWorkflowNode>();
		allNodesToRun.add( node);
		allNodesToRun.addAll( node.getAncestors());

		if ( debug >= 1) {
			System.out.println( "ALDWorkflow::runNode" + allNodesToRun);
		}
		
		this.executeNonReadyNodes( this.topSort(allNodesToRun), waitForCompletion);
	}
	
	/**
	 * Run the given node and all descendants of the given node.
	 * Additionally also all ancestors of these nodes are invoked.
	 * A node is only executed if it is not ready.
	 * Immediately returns after starting the execution.
	 * 
	 * @param nodeId
	 * @throws ALDWorkflowException if not all nodes are at least runnable or the execution itself fails, 
	 *                         or the node with nodeId does not exist
	 */
	public void runFromNode( ALDWorkflowNodeID nodeId) throws ALDWorkflowException {
		runFromNode( nodeId, false, false);
	}
	
	/**
	 * Run the given node and all descendants of the given node.
	 * Additionally also all ancestors of these nodes are invoked.
	 * A node is only executed if it is not ready.
	 * 
	 * @param nodeId
	 * @param waitForCompletion if true wait for completion, otherwise return immediately
	 * @throws ALDWorkflowException if not all nodes are at least runnable or the execution itself fails, 
	 *                         or the node with nodeId does not exist
	 */
	public void runFromNode( ALDWorkflowNodeID nodeId, boolean waitForCompletion) throws ALDWorkflowException {
		runFromNode(nodeId, waitForCompletion, false);
	}

	/**
	 * Run the given node and all descendants of the given node.
	 * Additionally also all ancestors of these nodes are invoked.
	 * A node is only executed if it is not ready.
	 * 
	 * @param nodeId
	 * @param waitForCompletion if true wait for completion, otherwise return immediately
	 * @param stepRecursively if true OP_STEP events are passed to the child operators
	 * @throws ALDWorkflowException if not all nodes are at least runnable or the execution itself fails, 
	 *                         or the node with nodeId does not exist
	 */
	public void runFromNode( ALDWorkflowNodeID nodeId, boolean waitForCompletion, boolean stepRecursively) throws ALDWorkflowException {
		if ( nodeId == null )
			throw( new NullPointerException( "ALDWorkflow::nodeParameterChanged nodeId is null"));
		
		ALDWorkflowNode node = this.getNode(nodeId);

		Set<ALDWorkflowNode> allNodesToRun = new HashSet<ALDWorkflowNode>();
		allNodesToRun.add( node);
			allNodesToRun.addAll(node.getDescendants());
		
		Set<ALDWorkflowNode> ancestors = new HashSet<ALDWorkflowNode>();
		
		for ( ALDWorkflowNode nextnode : allNodesToRun) {
			ancestors.addAll( nextnode.getAncestors());
		}
		allNodesToRun.addAll(ancestors);
		
		if ( debug >= 1) {
			System.out.println( "ALDWorkflow::runFromNode " +  ALDWorkflow.nodeIdsToString(allNodesToRun));
		}
		
		this.executeNonReadyNodes( this.topSort( allNodesToRun), waitForCompletion);
	}
	
	/**
	 * Ask execution of nodes to be terminated.
	 */
	@Deprecated
	public void interruptExecution (){
		executionInterrupted = true;
	}
	
	// ===========================================================================================================
	// Helper methods to run an operator
	
	/**
	 * Run all not ready nodes of the list in the given order.
	 * 
	 * @param nodeIds
	 * @param waitForCompletion if true wait for completion, otherwise return immediately
	 * @throws ALDWorkflowException if one of the nodes is not at least runnable
	 *                            or the execution itself fails
	 */
	private void executeNonReadyNodes( final List<ALDWorkflowNode> nodes, boolean waitForCompletion) 
			throws ALDWorkflowException {
		//TODO guarantee that this workflow object is executed not multiple times in parallel
		boolean noContollableOperators = true;
		
		for ( ALDWorkflowNode node : nodes ) {
			if ( ! node.stateGreaterEqual(ALDWorkflowNodeState.RUNNABLE) ) {
				throw( new ALDWorkflowException( WorkflowExceptionType.RUN_FAILED,
						"The operator <" + node.getOperator().getName() +
						"> is not at least runnable."));
			}
			if ( node.getClass().isAssignableFrom(ALDOperatorControllable.class)){
				noContollableOperators = false;
			}
		}
				
		// AWT
		for ( ALDWorkflowEventManager manager : this.workflowEventMangerList.values()) {
			Thread managerThread = new Thread(manager);	
			//this.workflowEventMangerThreadList.put( listener, managerThread);
			managerThread.start();

		}
		
		ExecuteThread executeThread = new ExecuteThread( this, nodes);

		if ( waitForCompletion && noContollableOperators) {
			if ( debug >= 2) {
				System.out.println( "ALDWorkflow::executeNonReadyNodes call executeThread.run()");
			}
			executeThread.run();
		} else {
			if ( debug >= 2) {
				System.out.println( "ALDWorkflow::executeNonReadyNodes call executeThread.start()");
			}
			executeThread.start();
		}

		/*
		ExecuteThread executeThread = new ExecuteThread( this, nodes);
		executeThread.start();
		
		if ( waitForCompletion) {
			try {
				executeThread.join();
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		*/
	}

	/**
	 * This thread executes all non-ready nodes in the given order.
	 * Execution may be asked to be terminated by calling the workflows <code>interruptExecution</code>
	 * method.
	 * Currently termination will take effect before the next node is executed.
	 * 
	 * @author posch
	 *
	 */
	class ExecuteThread extends Thread {
		/**
		 * workflow to execute nodes for.
		 */
		private final ALDWorkflow workflow;
		
		/**
		 * The nodes of the workflow to execute
		 */
		private final List<ALDWorkflowNode> nodes;

		public ExecuteThread(ALDWorkflow workflow, List<ALDWorkflowNode> nodes) {
			this.workflow = workflow;
			this.nodes = nodes;
		}

		@Override
		public void run() {
			ALDWorkflowNode runningNode = null;
			ALDWorkflowNodeState oldState = null;
			try {

				workflow.executionInterrupted = false;
				for ( ALDWorkflowNode node : nodes ) {
					
					// user request to interrupt execution?
					if ( workflow.executionInterrupted == true) {
						workflow.fireALDWorkflowEvent(
								new ALDWorkflowEvent(workflow, ALDWorkflowEventType.USER_INTERRUPT, 
										ALDWorkflow.mapNodeToNodeId( runningNode)));

						return;
					}

					// check status from controllable
					// TODO complete for other states
					switch (workflow.operatorStatus) {
					case OP_STOP:
						workflow.fireALDWorkflowEvent(
								new ALDWorkflowEvent(workflow, ALDWorkflowEventType.USER_INTERRUPT, 
										ALDWorkflow.mapNodeToNodeId( runningNode)));
						return;

					default:
						break;
					}
					
					if ( node.getState() != ALDWorkflowNodeState.READY ) {
						runningNode = node;
						// should be runnable, but to be sure
						oldState = node.getState();

						Set<ALDWorkflowNode> children = node.getChildren();
						children.retainAll(nodes);
						ALDWorkflow.executeNode( workflow, node, children.isEmpty());
					}
				}
			} catch (ALDException e) {
				ALDWorkflowRunFailureInfo info =
						new ALDWorkflowRunFailureInfo(e, 
								ALDWorkflow.mapNodeToNodeId( runningNode) );

				workflow.doStateChange( runningNode, oldState);
				
				StringBuffer msg = new StringBuffer();
				// put some general information to the header
				msg.append("\nOperator name: " + runningNode.getOperator().getName() + "\n");
				msg.append("\nEvent message: " + e.getMessage() + "\n");
				msg.append("Exception class type: \n");
				msg.append(info.getException().getClass() + "\n");
				msg.append("\nException stack trace:\n");
				StackTraceElement[] trace = info.getException().getStackTrace(); 
				for (StackTraceElement elem : trace) {
					msg.append(elem.toString());
					msg.append("\n");
				}

//				workflow.fireALDWorkflowEvent(
//						new ALDWorkflowEvent(workflow, ALDWorkflowEventType.RUN_FAILURE, 
//								"Running <" + runningNode.getOperator().getName() +
//								"> failed\n" + e.getMessage(), info));
				workflow.fireALDWorkflowEvent(
						new ALDWorkflowEvent(workflow, ALDWorkflowEventType.RUN_FAILURE, 
								new String( msg), info));
				return;
			} catch (Exception e) {
				ALDWorkflowRunFailureInfo info =
						new ALDWorkflowRunFailureInfo(e, 
								ALDWorkflow.mapNodeToNodeId( runningNode) );


				workflow.doStateChange( runningNode, oldState);

				workflow.fireALDWorkflowEvent(
						new ALDWorkflowEvent(workflow, ALDWorkflowEventType.RUN_FAILURE, 
								"Running <" + runningNode.getOperator().getName() +
								"> failed\n" + e.getMessage(), info));
				return;
			}
			
			//TODO are this all possible states we have?
			if (workflow.operatorStatus == OperatorControlStatus.OP_STOP) {
				workflow.fireALDWorkflowEvent(
						new ALDWorkflowEvent(workflow, ALDWorkflowEventType.USER_INTERRUPT, 
								ALDWorkflow.mapNodeToNodeId( runningNode)));

			} else {
				workflow.fireALDWorkflowEvent(
						new ALDWorkflowEvent(this, ALDWorkflowEventType.EXECUTION_FINISHED, 
								"execution finished"));
			}
		}
	}
	
	/**
	 * This actually runOps the operator in its own thread
	 * and takes care of firing events for state changes.
	 * The runOp method of the operator is invoked independently of the
	 * node's state. if <code>showResults</code> is true an corresponding event is fired.
	 * 
	 * @param workflow
	 * @param node
	 * @param showResults
	 * @throws ALDWorkflowException 
	 * @throws ALDProcessingDAGException 
	 * @throws ALDOperatorException 
	 * @throws ALDDataConverterManagerException 
	 * @throws ALDDataConverterManagerException 
	 */
	private static void executeNode( ALDWorkflow workflow, ALDWorkflowNode node, boolean showResults ) 
			throws ALDWorkflowException, ALDOperatorException, ALDProcessingDAGException, ALDDataConverterManagerException, ALDDataConverterManagerException   {
		if ( debug >= 2 ) {
			System.out.println("    ALDWorkflow::executeNonReadyNodes execute node <" + node.getId() + ">");
		}

		final ALDOperator op = node.getOperator();
		
		//retrieve input parameters from links
		try {
			if ( debug >= 3 ) {
				System.out.println( "   retrieve input parameters ");
			}

			for ( ALDWorkflowEdge edge : node.getInEdges()) {
				Object value = edge.getSourceNode().getOperator().getParameter(
						edge.getSourceParameterName());
				if ( edge.isNeedConverter() ) {
					// we may need to convert
					Class<?> targetClass = 
							op.getParameterDescriptor(edge.getTargetParameterName()).getMyclass();
					
					ALDOperator sourceOp = edge.getSourceNode().getOperator();
					// check if objects are assignable (as the value may extend the source parameter's class 
					if ( ! targetClass.isAssignableFrom(value.getClass())) {
						try {
							if ( debug >= 2 ) {
								System.out.println( "ALDWorkflow::executeNode try to convert");
							}
							Field sourceField = sourceOp.getParameterDescriptor(
									edge.getSourceParameterName()).getField();
							Field targetField = op.getParameterDescriptor(
									edge.getTargetParameterName()).getField();
							value = converterManager.convert(edge.getConverter(),
									value, sourceField, targetClass, targetField);
						} catch (ALDDataConverterException e) {
							throw( new ALDWorkflowException( WorkflowExceptionType.FATAL_INTERNAL_ERROR,
									"Convert failed in <" + op.getName() + "> for parameter <" +
											edge.getTargetParameterName()
							));

						}
					} else {
						if ( debug >= 2 ) {
							System.out.println( "ALDWorkflow::executeNode converter non-null but no need to convert from <" +
									value.getClass().getName() + "> to <" + 
									targetClass.getName() + ">");
						}				
					}
				} else {
					if ( debug >= 2 ) {
						System.out.println( "ALDWorkflow::executeNode converter null");
					}
									}
				
				op.setParameter(edge.getTargetParameterName(), value);
				if ( debug >= 3 ) {
					System.out.println( "     set parameter " +
							edge.getTargetParameterName() + " from node <" + edge.getSourceNode().getId() +
							"> " + edge.getSourceParameterName() + " value = " + value);
				}

			}
		} catch (ALDOperatorException e) {
			throw( new ALDWorkflowException( WorkflowExceptionType.FATAL_INTERNAL_ERROR,
					"Cannot retrieve all input parameters for <" + op.getName() + ">"));
		}

		if ( workflow.workflowContext != ALDWorkflowContextType.OP_RUNNER &&
				op instanceof ALDOperatorControllable) {
			ALDOperatorControllable controlableOp = (ALDOperatorControllable)op;
			workflow.addALDControlEventListener( controlableOp);
		}
		
		workflow.doStateChange( node, ALDWorkflowNodeState.RUNNING);
		op.runOp();	
		
		if ( op.hasInOutParameters()) {
			workflow.doStateChange( node, ALDWorkflowNodeState.RUNNABLE);
		} else {
			workflow.doStateChange( node, ALDWorkflowNodeState.READY);
		}
		
		if ( workflow.workflowContext != ALDWorkflowContextType.OP_RUNNER && 
				op instanceof ALDOperatorControllable ) {
			ALDOperatorControllable controlableOp = (ALDOperatorControllable)op;
			workflow.removeALDControlEventListener( controlableOp);
		}

		// propagate outputs to the innderNode
		if ( debug >= 3 ) {
			System.out.println( "   propagate output parameters ");
		}

		for ( ALDWorkflowEdge edge : node.getOutEdges()) {
			if ( edge.getTargetNode().isInteriorShadowNode) {
				Object value = edge.getSourceNode().getOperator().getParameter(
						edge.getSourceParameterName());
				edge.getTargetNode().getOperator().setParameter(edge.getTargetParameterName(), value);
				if ( debug >= 3 ) {
					System.out.println( "     set parameter " +
							edge.getTargetParameterName() + " from node <" + edge.getSourceNode().getId() +
							"> " + edge.getSourceParameterName() + " value = " + value);
				}

			}
		}
		
		if ( showResults) {
			workflow.fireALDWorkflowEvent(
					new ALDWorkflowEvent(workflow, ALDWorkflowEventType.SHOW_RESULTS, 
							ALDWorkflow.mapNodeToNodeId( node)));
		}
	}
	
	// ===================================================================
	// Helper methods to handle states of nodes
	
	/**
	 * Updates the states of <code>nodesToCheck</code> and their descendants.
	 * The nodes <code>nodesToCheck</code> may have changed their state according
	 * to change of the configuration of their operator or adding/removing of
	 * in coming edges.
	 * These nodes and all their descendants may have also changed their state
	 * due to the data flow dependencies.
	 * This methods changes the states of the nodes within the workflow and also
	 * fires an event notifying the listeners of the state changes.
	 * 
	 * @param nodesToCheck
	 * @param doFireEvent if true an appropriate event is fired
	 * @throws ALDWorkflowException 
	 */
	private void updateStates( Collection<ALDWorkflowNode> nodesToCheck, boolean doFireEvent) throws ALDWorkflowException {
		if ( debug >= 1) {
			System.out.println( "ALDWorkflow::updateStates for nodes " +
					ALDWorkflow.nodeIdsToString(nodesToCheck));
		}
		HashSet<ALDWorkflowNodeID> changeSet = new HashSet<ALDWorkflowNodeID>();

		// first check impact of local changes to state
		try {
			for ( ALDWorkflowNode node : this.topSort(nodesToCheck)) {
				if ( this.checkLocalStateChange(node)) {
					changeSet.add( ALDWorkflow.mapNodeToNodeId(node));
				}
			}
		} catch (ALDWorkflowException e1) {
			throw( new ALDWorkflowException( WorkflowExceptionType.FATAL_INTERNAL_ERROR,
					"Graph is cyclic in updateStates"));
		}
		

		// now collect all nodes to consider, which are nodesToCheck and their descendants
		HashSet<ALDWorkflowNode> nodesToConsider = new HashSet<ALDWorkflowNode>( nodesToCheck);
		for ( ALDWorkflowNode node : nodesToCheck) {
			nodesToConsider.addAll( node.getDescendants());
		}
	
		try {
			for ( ALDWorkflowNode node : this.topSort(nodesToConsider) ) {
				if ( this.checkDataflowStateChange( node) ) {
					changeSet.add( ALDWorkflow.mapNodeToNodeId(node));
				}
			}
		} catch (ALDWorkflowException e) {
			throw( new ALDWorkflowException( WorkflowExceptionType.FATAL_INTERNAL_ERROR,
					"Graph is cyclic in updateStates"));
		}
			
		if ( doFireEvent && ! changeSet.isEmpty() ) {
			fireALDWorkflowEvent(new ALDWorkflowEvent(this, ALDWorkflowEventType.NODE_STATE_CHANGE, changeSet));
		}
	}
	
	/**
	 * Convenience method which just invokes {@link #updateStates(Collection, boolean)} with the single
	 * <code>node</code>.
	 * 
	 * @param node
	 * @throws ALDWorkflowException 
	 */
	private void updateState(ALDWorkflowNode node) throws ALDWorkflowException {
		LinkedList <ALDWorkflowNode> nodeList = new LinkedList<ALDWorkflowNode>();
		nodeList.add( node);
		updateStates( nodeList, true);
	}

	/**
	 * Updates the state of this node according to its local configuration.
	 * Does not take state changes depending on the states of its parents into account
	 * <p>
	 * Note: this method does not fire an event to notify listeners.
	 * 
	 * @param node
	 * @return true is state of the node has been changed
	 * @throws ALDWorkflowException 
	 */
	private boolean checkLocalStateChange( ALDWorkflowNode node) throws ALDWorkflowException {
		if ( debug >= 2 ) {
			System.out.println( "    ALDWorkflow::checkLocalStateChange of node with id <" + node.getId() +
					"> old state " + node.getState() + " configured is now " + node.isConfigured());
		}
		
		boolean stateChanged= false;
		ALDWorkflowNodeState oldState = node.getState();

		switch (node.getState()) {
		case UNCONFIGURED:
			if ( node.isConfigured()) {
				node.setState(ALDWorkflowNodeState.CONFIGURED);
				stateChanged = true;
			}
			break;
			
		case CONFIGURED:
		case RUNNABLE:
			if ( ! node.isConfigured()) {
				node.setState(ALDWorkflowNodeState.UNCONFIGURED);
				stateChanged = true;
			}
			break;
			
		case READY:
			if ( node.isConfigured()) {
				node.setState(ALDWorkflowNodeState.RUNNABLE);
			} else {
				node.setState(ALDWorkflowNodeState.UNCONFIGURED);
			}
			stateChanged = true;
			break;
				
		default:
			throw( new ALDWorkflowException( WorkflowExceptionType.FATAL_INTERNAL_ERROR,
					"ALDWorkflow::checkLocalStateChange fatal error: state shoud not be running for " +
					node.getId()));

		}
		
		if ( stateChanged) {
			if ( debug >= 2) {
				System.out.println( "    ALDWorkflow::checkLocalStateChange changed state for node <" + node.getId() +
						"> from " + oldState + " to " + node.getState());
			}
		}

		return stateChanged;
	}
	
	/**
	 * Updates the state of this node according to the states of its parents.
	 * Updates considered are only due to data flow,
	 * not due to changes in operator configuration or adding/removing of in coming edges.
	 * <p>
	 * Note: this method does not fire an event to notify listeners.
	 * 
	 * @param node
	 * @return true if state was changed
	 * @throws ALDWorkflowException 
	 */
	private boolean checkDataflowStateChange(ALDWorkflowNode node) throws ALDWorkflowException {
		boolean stateChanged = false;
		ALDWorkflowNodeState oldState = node.getState();
		
		if ( debug >= 2 ) {
			System.out.println("    ALDWorkflow::checkDataflowStateChange for node with  " + node.getId() +
					" with current state " + node.getState());
		}
		
		switch ( node.getState()) {
		case UNCONFIGURED:
			// remains un-configured
			break;
		case CONFIGURED:
			if ( checkParentStates( node, ALDWorkflowNodeState.RUNNABLE) ) {
				node.setState( ALDWorkflowNodeState.RUNNABLE);
				stateChanged = true;
			}
			break;
		case RUNNABLE: 
			if ( checkParentStates( node, ALDWorkflowNodeState.RUNNABLE) ) {
				//??node.setState( ALDWorkflowNodeState.RUNNABLE);
			} else {
				node.setState( ALDWorkflowNodeState.CONFIGURED);
				stateChanged = true;
			}
			break;
		case RUNNING:
			System.err.println( "ALDWorkflow::updateState fatal error: state shoud not be running for " +
					node);
			break;
		case READY:
			if ( checkParentStates( node, ALDWorkflowNodeState.READY) ) {
				// nothing to change
			} else if ( checkParentStates( node, ALDWorkflowNodeState.RUNNABLE) ){
				node.setState( ALDWorkflowNodeState.RUNNABLE);
				stateChanged = true;
			} else {
				node.setState( ALDWorkflowNodeState.CONFIGURED);
				stateChanged = true;
			}
		}
		
		if ( stateChanged) {
			if ( debug >= 2) {
				System.out.println( "    ALDWorkflow::checkDataflowStateChange changed state for node <" + node.getId() +
						"> from " + oldState + " to " + node.getState());
			}
		}
		
		return stateChanged;
	}

	/**
	 * Check if all parents of the given node have a state at least <code>requiredState</code>.
	 * 
	 * @param node
	 * @param requiredState
	 * @return true if all parents have a state at least <code>requiredState</code>
	 * @throws ALDWorkflowException 
	 */
	private boolean checkParentStates(ALDWorkflowNode node,
			ALDWorkflowNodeState requiredState) {
		for ( ALDWorkflowNode parent : node.getParents()) {
			if (  parent.getState().compareTo( requiredState)  < 0)
				return false;
		}
		return true;
	}

	/**
	 * Set the new state in the node and fire corresponding event.
	 * 
	 * @param node
	 * @param newState
	 */
	private void doStateChange( ALDWorkflowNode node, ALDWorkflowNode.ALDWorkflowNodeState newState) {
		if ( debug >= 2) {
			System.out.println( "    ALDWorkflow::doStateChange for node <" + node.getId() +
					"> from " + node.getState() + " to " + newState);
		}

		node.setState(newState);
		
		HashSet<ALDWorkflowNodeID> changeSet = new HashSet<ALDWorkflowNodeID>();
		changeSet.add( ALDWorkflow.mapNodeToNodeId(node));	
		fireALDWorkflowEvent(new ALDWorkflowEvent(this, ALDWorkflowEventType.NODE_STATE_CHANGE, changeSet));
	}
	
	
	// ===================================================================
	// topological sorting

	/** Topologically sorts the nodes given in <code>nodes</code>.
	 * For sorting the subgraph of the complete workflow induced by <code>nodes</code> is
	 * considered.
	 * If the subgraph is cyclic an exception is raised.
	 * 
	 * @param nodes of the subgraph to be sorted
	 * @return topologically sorted list of <code>nodes</code>
	 * @throws ALDWorkflowException if the subgraph is cyclicor <code>nodes</code> is null.
	 */
	LinkedList<ALDWorkflowNode> topSort( Collection<ALDWorkflowNode> nodes) throws ALDWorkflowException {
		if ( nodes == null)
			throw( new NullPointerException( "ALDWorkflow::topSort nodes is null"));
		
		if ( debug >= 2) {
			System.out.println( "    ALDWorkflow::topSort sort the nodes: " + ALDWorkflow.nodeIdsToString(nodes));
		}

		HashMap<ALDWorkflowNode, Set<ALDWorkflowNode>> sourceNodeMap = new HashMap<ALDWorkflowNode, Set<ALDWorkflowNode>>();
		for ( ALDWorkflowNode node : nodes) {
			Set<ALDWorkflowNode> sourceNodes = node.getParents();
			sourceNodes.retainAll(nodes);
			sourceNodeMap.put( node, sourceNodes);
		}
		
		if ( debug >= 3 ) {
			for ( ALDWorkflowNode node : sourceNodeMap.keySet()) {

				System.out.println("        " + node.getId() + ": ");
				for ( ALDWorkflowNode source : sourceNodeMap.get(node) ) {
					System.out.println( "            " + source.getId());
				}
			}
		}
		
		LinkedList<ALDWorkflowNode> sortedNodes = new LinkedList<ALDWorkflowNode>();
		while ( ! sourceNodeMap.isEmpty()) {
			ALDWorkflowNode nextNode = getNodeWithoutSources(sourceNodeMap);
			if ( nextNode == null ) {
				throw( new ALDWorkflowException( WorkflowExceptionType.CYCLIC, 
						"workflow is cyclic"));
			}
			sortedNodes.add(nextNode);
			sourceNodeMap.remove( nextNode);
			for (  Set<ALDWorkflowNode> sourceNodes : sourceNodeMap.values()) {
				sourceNodes.remove(nextNode);
			}
			
			if ( debug >= 3 ) {
				for ( ALDWorkflowNode node : sourceNodeMap.keySet()) {
					System.out.println("        " + node.getId() + ": ");
					for ( ALDWorkflowNode source : sourceNodeMap.get(node) ) {
						System.out.println( "            " + source.getId());
					}
				}
			}
		}
		
		return sortedNodes;	
	}
	
	/** Topologically sorts all nodes of the workflow.
	 * 
	 * @return topologically sorted list of all nodes in the workflow
	 * @throws ALDWorkflowException the workflow is cyclic
	 */
	LinkedList<ALDWorkflowNode> topSort() throws ALDWorkflowException {
		if ( debug >= 2) {
			System.out.println( "    ALDWorkflow::topSort complete workflow ");
		}
		
		return topSort( this.nodes);
	}
	
	/**
	 * Returns one node from the <code>sourceNodeMap</code> with zero
	 * source nodes, if any. If no such node exists, return null.
	 * 
	 * @param sourceNodeMap
	 * @return
	 */
	private ALDWorkflowNode getNodeWithoutSources(
			HashMap<ALDWorkflowNode, Set<ALDWorkflowNode>> sourceNodeMap) {
		for ( ALDWorkflowNode node : sourceNodeMap.keySet()) {
			if ( sourceNodeMap.get(node).size() == 0) {
				return node;
			}
		}
		return null;
	}

	// ===================================================================
	// mapping between IDs and nodes/edges
	
	/**
	 * Map a nodeId to its node.
	 * 
	 * @param nodeId the node or null if a node with the given nodeId does not exist
	 * @return
	 */
	protected static ALDWorkflowNode mapNodeIdToNode(ALDWorkflowNodeID nodeId) {
		return ALDWorkflow.nodeIdToNode.get(nodeId);
	}

	/**
	 * Map a node to its NodeId
	 * 
	 * @param node the NodeId or null (which should not happen)
	 * @return
	 */
	protected static ALDWorkflowNodeID mapNodeToNodeId( ALDWorkflowNode node) {
		return ALDWorkflow.nodeToNodeId.get( node);
	}

	/**
	 * Add this node to the workflow, create a NodeId and update mapping.
	 * @param node
	 */
	private void addNode( ALDWorkflowNode node) {
		if ( node != this.interiorShadowNode ) {
			this.nodes.add( node);
		}
		ALDWorkflowNodeID nodeId= new ALDWorkflowNodeID();
		ALDWorkflow.nodeIdToNode.put( nodeId, node);
		ALDWorkflow.nodeToNodeId.put( node, nodeId);
	}
	
	private void addEdge( ALDWorkflowEdge edge) {
		this.edges.add(edge);
		ALDWorkflowEdgeID edgeId = new ALDWorkflowEdgeID();
		ALDWorkflow.edgeIdToEdge.put( edgeId, edge);
		ALDWorkflow.edgeToEdgeId.put( edge, edgeId);
	}

	/**
	 * Map an edgeId to its edge.
	 * 
	 * @param edgeId the edge or null if no edge with the given edgeId exists
	 * @return
	 */
	protected static ALDWorkflowEdge mapeEdgeIdToEdge( ALDWorkflowEdgeID edgeId) {
		return ALDWorkflow.edgeIdToEdge.get( edgeId);
	}
	
	protected static ALDWorkflowEdgeID mapEgdeToEdgeId( ALDWorkflowEdge edge) {
		return ALDWorkflow.edgeToEdgeId.get( edge);
	}

	public ALDWorkflowNodeID getShadowNodeId() {
		return ALDWorkflow.mapNodeToNodeId(this.interiorShadowNode);
	}
	
	// ===================================================================
	// Listener section
	@Override
	public void addALDWorkflowEventListener(ALDWorkflowEventListener listener) {
		if ( listener == null)
			throw( new NullPointerException( "ALDWorkflow::addALDWorkflowEventListener listener is null"));

		ALDWorkflowEventManager manager = new ALDWorkflowEventManager(listener);
		this.workflowEventMangerList.put( listener, manager);
		
		// do not create and start event manager thread currently, AWT problem in GUI
		/*
		Thread managerThread = new Thread(manager);	
		this.workflowEventMangerThreadList.put( listener, managerThread);
		managerThread.start();
		*/
	}

	@Override
	public void removeALDWorkflowEventListener(ALDWorkflowEventListener listener) {
		ALDWorkflowEventManager manager = this.workflowEventMangerList.get( listener);
		if ( manager != null) {
			manager.setTermiate(true);
			this.workflowEventMangerList.remove(listener);
			//this.workflowEventMangerThreadList.remove(listener);
		}
	}

	/**
	 * Returns the event queue associated with the <code>listener</code> or null
	 * it the listener is not registered.
	 * 
	 * @param listener
	 * @return
	 */
	public BlockingDeque<ALDWorkflowEvent>  getEventQueue( ALDWorkflowEventListener listener) {
		ALDWorkflowEventManager manager = this.workflowEventMangerList.get( listener);
		if ( manager != null) {
			return manager.eventQueue;
		} else {
			return null;
		} 
	}
	
	@Override
	public void fireALDWorkflowEvent(final ALDWorkflowEvent event) {
		if ( event == null)
			throw( new NullPointerException( "ALDWorkflow::fireALDWorkflowEvent event is null"));
		if ( debug >= 1) {
			System.out.println( "ALDWorkflow::fireALDWorkflowEvent fire " + event.getEventType() +
					" " + event.getEventMessage() + " info: " + event.getId());
		}
		for ( ALDWorkflowEventManager manager : this.workflowEventMangerList.values()) {
			if ( debug >= 2) {
				System.out.println( "    ALDWorkflow::fireALDWorkflowEvent fire event for manager " +
						manager);
			}
			try {
				manager.eventQueue.put(event.createCopy());
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

	/**
	 * Add a listener for events thrown by the class ALDWorkflow (not an instance).
	 * 
	 * @param listener
	 */
	public static void addALDWorkflowClassEventListener(ALDWorkflowClassEventListener listener) {
		if ( listener == null)
			throw( new NullPointerException( "ALDWorkflow::addALDWorkflowClassEventListener listener is null"));

		workflowLoadEventlistenerList.add(ALDWorkflowClassEventListener.class,listener);
	}

	/**
	 * Remove a listener for events thrown by the class ALDWorkflow (not an instance).
	 * @param listener
	 */
	public static void removeALDWorkflowClassEventListener(ALDWorkflowEventListener listener) {
		workflowLoadEventlistenerList.remove(ALDWorkflowEventListener.class, 
				listener);
	}

	/** Fire an event by the class ALDWorkflow.
	 * 
	 * @param aldWorkflowClassEvent
	 */
	public static void fireALDWorkflowClassEvent(final ALDWorkflowClassEvent aldWorkflowClassEvent) {
		if ( aldWorkflowClassEvent == null)
			throw( new NullPointerException( "ALDWorkflow::fireALDWorkflowClassEvent event is null"));

		// Guaranteed to return a non-null array
		final Object[] listeners = workflowLoadEventlistenerList.getListenerList();
		
		Thread eventThread;
		// Process the listeners last to first, notifying
		// those that are interested in this event
		for (int i = listeners.length-2; i>=0; i-=2) {
			if (listeners[i]==ALDWorkflowClassEventListener.class) {
				// Lazily create the event for each listener and invoke the listener in a new thread
				final ALDWorkflowClassEventListener listener = (ALDWorkflowClassEventListener) listeners[i+1];
				eventThread = new Thread(){
					@Override
					public void run() {
						listener.handleALDWorkflowClassEvent(
								new ALDWorkflowClassEvent (this, 
										aldWorkflowClassEvent.getEventType(), aldWorkflowClassEvent.getEventMessage(), aldWorkflowClassEvent.getId()));

					}};
					
				eventThread.start();
			}
		}
	}
	
	// ========================================================
	// hierarchical workflows
		
//	/**
//	 * Add a new parameter to this workflow.
//	 * <br>
//	 * Note: adding a new parameter will shadow an already existing parameter.
//	 * 
//	 * @param descriptor
//	 */
//	public void addParameter( ALDOpParameterDescriptor descriptor) {
//		if ( debug >= 0 ) {
//			System.out.println( "ALDWorkflow::addParameter with name <" + descriptor.getName() + ">");
//		}
//		ALDOpParameterDescriptor wfDescriptor = new ALDOpParameterDescriptor(descriptor);
//		
//		super.addParameter(wfDescriptor);
////		Field field;
////		try {
////			field = ALDOperator.class.getDeclaredField( "parameterDescriptorsAll");
////			field.setAccessible(true);
////			@SuppressWarnings("unchecked")
////			Hashtable<String,ALDOpParameterDescriptor> parameterDescriptorsAll =
////					(Hashtable<String, ALDOpParameterDescriptor>) field.get( this);
////			parameterDescriptorsAll.put( descriptor.getName(), wfDescriptor);
////		} catch (Exception e) {
////			throw( new ALDWorkflowException( WorkflowExceptionType.FATAL_INTERNAL_ERROR,
////					"ALDWorkflow::addParameter cannot access parameterDescriptorsAll"));
////		}
//	}
	
//	/**
//	 * Remove a parameter from this workflow.
//	 * 
//	 * @param parameterName
//	 * @throws ALDWorkflowException  if this parameter does not exist or in case of fatal error
//	 */
//	protected void removeParameter( String parameterName) throws ALDWorkflowException{
//		ALDOpParameterDescriptor descriptor = null;
//		try {
//			descriptor = getParameterDescriptor(parameterName);
//		} catch (ALDOperatorException e1) {
//			throw( new ALDWorkflowException( WorkflowExceptionType.PARAMETER_ERROR,
//					"PCannot remove parameter <" +
//							parameterName + ">, as it does not exist"));
//		}
//		
//		// parameter which was defined as a field (and annotated) may not be removed
//		if ( descriptor.getClass() == ALDOpParameterDescriptor.class) {
//			throw( new ALDWorkflowException( WorkflowExceptionType.PARAMETER_ERROR,
//					"ALDWorkflow::removeParameter parameter <" +
//							parameterName + "> is a member variable and cannot be removed"));
//		}
//
//		Field field;
//		try {
//			field = ALDOperator.class.getDeclaredField( "parameterDescriptorsAll");
//			field.setAccessible(true);
//			@SuppressWarnings("unchecked")
//			Hashtable<String,ALDOpParameterDescriptor> parameterDescriptorsAll =
//					(Hashtable<String, ALDOpParameterDescriptor>) field.get( this);
//			parameterDescriptorsAll.remove( parameterName);
//
//		} catch (Exception e) {
//			throw( new ALDWorkflowException( WorkflowExceptionType.FATAL_INTERNAL_ERROR,
//					"ALDWorkflow::removeParameter cannot access parameterDescriptorsAll"));
//		}
//	}
	
//	@Override
//	public Object getParameter( String name) throws ALDOperatorException {
//		if (this.getParameterDescriptor(name).getClass() == ALDOpParameterDescriptor.class) {
//			return super.getParameter(name);
//		} else {
//			ALDOpParameterDescriptor descriptor = this.getParameterDescriptor(name);
//			return descriptor.getValue();
//		}
//	}
	
//	@Override
//	public void setParameter( String name, Object value) throws ALDOperatorException {
//		if (this.getParameterDescriptor(name).getClass() == ALDOpParameterDescriptor.class) {
//			super.setParameter(name, value);
//		} else {
//			ALDOpParameterDescriptor descriptor = (ALDOpParameterDescriptor)this.getParameterDescriptor(name);
//			descriptor.setValue( value);
//		}
//	}
	
	/**
	 * Add a new workflow as a child to this workflow.
	 * @return
	 */
	protected ALDWorkflowNodeID createChildWorkflow() {
		return createChildWorkflow(untitledWorkflowName);
	}

	/**
	 * Add a new workflow as a child to this workflow.
	 * @param name
	 * @return
	 */
	protected ALDWorkflowNodeID createChildWorkflow( String name) {
		//TODO implement
		return null;		
	}

	/**
	 * Relocate the node to this workflow.
	 * 
	 * @param nodeId
	 */
	protected void relocateNode( ALDWorkflowNodeID nodeId, boolean disconnectEdges) {	
		LinkedList<ALDWorkflowNodeID> nodeIds = new LinkedList<ALDWorkflowNodeID>();
		nodeIds.add( nodeId);
		relocateNodes(nodeIds, disconnectEdges);
	}
	
	/**
	 * Relocate the nodes <code>nodeIds</code> and all edges of the sub graph induced by these
	 *  nodes to this workflow.
	 * If <code>disconnectEdges</code> is true, all edges connecting <code>nodeIds</code>
	 * to nodes outside of this sub graph are removed.
	 * Otherwise an exception is thrown if such edges exist.
	 * 
	 * 
	 * @param nodeIds
	 * @param disconnectEdges
	 */
	protected void relocateNodes( Collection<ALDWorkflowNodeID> nodeIds, boolean disconnectEdges) {
		//TODO implement
		
		// first check, if all nodes belong to the same workflow
		
		// .. and if this workflow is not identical to this workflow
		
		// check is no ode has edges incident to other nodes or 
		// collect them for removal
		
		// remove edges if applicable
		
		// move nodes form old workflow to this workflow (only move form nodes field
		
		// the same for edges
	}
	
	//=======================================
	// general helpers
	
	@Override
	public void print() {
		System.out.println(" === Print workflow ===========================================");
		super.print();
		
		System.out.println( "Graph section");
		System.out.println( "workflow <" + this.name + ">");
		
		System.out.println();
		this.interiorShadowNode.print();
		
		System.out.println();

		System.out.println("Nodes:");
		System.out.println();
		for ( ALDWorkflowNode node : this.nodes ) {
			node.print();
		}
		
		System.out.println();
		System.out.println("Edges:");
		System.out.println();

		for ( ALDWorkflowEdge edge : this.edges ) {
			edge.print();
		}
		System.out.println(" === END: Print workflow =======================================");
	}

	/**
	 * Formats the ids of the given nodes into a string for debugging purposes.
	 * 
	 * @param nodes
	 * @return
	 */
	static String nodeIdsToString(Collection<ALDWorkflowNode> nodes) {
		StringBuffer buf = new StringBuffer();
		
		buf.append('[');
		for ( ALDWorkflowNode node : nodes) {
			buf.append(node.getId() + ",");
		}
		if ( buf.length() != 1) {
			buf.setCharAt(buf.length()-1, ']');
		} else {
			buf.append(']');
		}
		return new String( buf);
	}
	
	/**
	 * Formats the ids of the given edges into a string for debugging purposes.
	 * 
	 * @param edges
	 * @return
	 */
	static String edgeIdsToString(Collection<ALDWorkflowEdge> edges) {
		StringBuffer buf = new StringBuffer();
		
		buf.append('[');
		for ( ALDWorkflowEdge edge : edges) {
			buf.append(edge.getId() + ",");
		}
		if ( buf.length() != 1) {
			buf.setCharAt(buf.length()-1, ']');
		} else {
			buf.append(']');
		}
		return new String( buf);
	}

	@Override
	public void handleOperatorExecutionProgressEvent(
			ALDOperatorExecutionProgressEvent e) throws ALDWorkflowException {

		// figure out which node is associated with the operator
		HashSet<ALDWorkflowNodeID> changeSet = new HashSet<ALDWorkflowNodeID>();
		ALDOperator op = null;
		try {
			op = e.getOriginatingOperator();
		} catch (Exception e2) {
			throw( new ALDWorkflowException( WorkflowExceptionType.FATAL_INTERNAL_ERROR, 
					"ALDWorkflow::handleOperatorExecutionProgressEvent not " +
					"an ALDOperator associated with event"));
		}

		for ( ALDWorkflowNode node : this.nodes) {
			if ( node.getOperator() == op) {
				if ( debug >= 1) {
					System.out.println( "ALDWorkflow::handleOperatorExecutionProgressEvent " +
							"Operator " + op.getName() + " changed progress to <" + 
							e.getExecutionProgressDescr() + ">");
				}
				node.setOperatorExecutionProgressDescr(e.getExecutionProgressDescr());
				changeSet.add( ALDWorkflow.mapNodeToNodeId(node));
			}
		}

		if ( ! changeSet.isEmpty()) {
			fireALDWorkflowEvent(new ALDWorkflowEvent(this, 
					ALDWorkflowEventType.NODE_EXECUTION_PROGRESS, changeSet));
		} else {
			throw( new ALDWorkflowException( WorkflowExceptionType.FATAL_INTERNAL_ERROR, 
					"ALDWorkflow::handleOperatorExecutionProgressEvent no workflow " +
					"node associated with operator of event found"));			
		}
	}
}
