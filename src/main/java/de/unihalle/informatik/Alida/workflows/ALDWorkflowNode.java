/*
 * This file is part of Alida, a Java library for 
 * Advanced Library for Integrated Development of Data Analysis Applications.
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
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 * Fore more information on Alida, visit
 *
 *    http://www.informatik.uni-halle.de/alida/
 *
 */

/* 
 * Most recent change(s):
 * 
 * $Rev$
 * $Date$
 * $Author$
 * 
 */

package de.unihalle.informatik.Alida.workflows;

import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.Vector;

import de.unihalle.informatik.Alida.exceptions.ALDOperatorException;
import de.unihalle.informatik.Alida.exceptions.ALDWorkflowException;
import de.unihalle.informatik.Alida.exceptions.ALDWorkflowException.WorkflowExceptionType;
import de.unihalle.informatik.Alida.operator.ALDOperator;

/**
 * This class represents a node in an Alida work flow.
 * Essentially it holds an  {@link de.unihalle.informatik.Alida.annotations.ALDAOperator} object 
 * and edges connecting parameters of this operator.

 * @author posch
 *
 */
public class ALDWorkflowNode {
	
	public static enum ALDWorkflowNodeState {
		UNCONFIGURED,
		CONFIGURED,
		RUNNABLE,
		RUNNING,
		READY
	}
	
	/**
	 * The operator object associated with this node.
	 */
	private ALDOperator op;

	/**
	 * Is true it his node is used as an interior shadow (or substitute) for the
	 * node holding this work flow {see {@link ALDWorkflow}.
	 */
	public final boolean isInteriorShadowNode;
	
	/**
	 * The state of the node
	 */
	private transient ALDWorkflowNodeState state;

	/**
	 * The execution progress of the operator associated with this node
	 */
	private transient String operatorExecutionProgressDescr = null;

	/**
	 * This is the enclosing work flow.
	 */
	private ALDWorkflow parentWorkflow;
	
	/**
	 *  all incoming edges.
	 */
	Vector<ALDWorkflowEdge> inEdges;
	
	/**
	 *  outgoing edges.
	 */
	Vector<ALDWorkflowEdge> outEdges;

	/**
	 * Create a node
	 * @param parentWorkflow TODO
	 * @param op
	 * @param id
	 */
	public ALDWorkflowNode(ALDWorkflow parentWorkflow, ALDOperator op) {
		this(parentWorkflow, op,false);
	}

	/**
	 * Create a node
	 * @param parentWorkflow TODO
	 * @param op
	 * @param id
	 */
	public ALDWorkflowNode(ALDWorkflow parentWorkflow, ALDOperator op, boolean isInteriorShadowNode) {
		this.setParentWorkflow(parentWorkflow);
		this.op = op;
		this.isInteriorShadowNode = isInteriorShadowNode;
	
		if ( op.getMissingRequiredInputs().size() == 0 ) {
			this.state = ALDWorkflowNodeState.CONFIGURED;
		} else {
			this.state = ALDWorkflowNodeState.UNCONFIGURED;
		}
		
		inEdges = new Vector<ALDWorkflowEdge>(); 
		outEdges = new Vector<ALDWorkflowEdge>();
	}

	/**
	 * @return the parentWorkflow
	 */
	public ALDWorkflow getParentWorkflow() {
		return parentWorkflow;
	}

	/**
	 * @param parentWorkflow the parentWorkflow to set
	 */
	protected void setParentWorkflow(ALDWorkflow parentWorkflow) {
		this.parentWorkflow = parentWorkflow;
	}

	/** Returns the operator associated with this node.
	 * 
	 * @return
	 */
	public ALDOperator getOperator() {
		return op;
	}

	/** Sets the operator associated with this node.
	 * The new operator instance needs to be of the same class as the current operator instance.
	 * 
	 * @throws ALDWorkflowException if the new operator instance is of wrong type
	 */
	protected void setOperator( ALDOperator newOp) throws ALDWorkflowException {
		if ( newOp.getClass() != this.getOperator().getClass() ) {
			throw new ALDWorkflowException(WorkflowExceptionType.INVALID_OPERATOR, 
					"ALDWorkflowNode::setOperator wrong class of operator");
		}

		this.op = newOp;
	}

	/**
	 * Returns the current state of this node
	 * @return
	 */
	public ALDWorkflowNodeState getState() {
		return state;
	}
	
	/**
	 * Sets the state of this node
	 * 
	 * @param state New state 
	 */
	protected void setState( ALDWorkflowNodeState state) {
		this.state = state;
	
	}
	
	/**
	 * Return the Id associated with this node.
	 * @return
	 */
	public Integer getId() {
		return ALDWorkflow.mapNodeToNodeId(this).id;
	}
	/**
	 * Return true if the state of this node is greater equal the
	 * <code>compareState</code>
	 * @param compareState
	 * @return
	 */
	public boolean stateGreaterEqual( ALDWorkflowNodeState compareState) {
		return this.state.compareTo(compareState) >= 0;
	}
	
	/** Returns  all incoming edges.
	 * 
	 * @return
	 */
	protected Vector<ALDWorkflowEdge> getInEdges() {
		return inEdges;
	}

	/**
	 * Returns  all outgoing edges.
	 * 
	 * @return
	 */
	protected Vector<ALDWorkflowEdge> getOutEdges() {
		return outEdges;
	}

	/**
	 * @return the operatorExecutionProgressDescr
	 */
	public String getOperatorExecutionProgressDescr() {
		return operatorExecutionProgressDescr;
	}

	/**
	 * @param operatorExecutionProgressDescr the operatorExecutionProgressDescr to set
	 */
	public void setOperatorExecutionProgressDescr(
			String operatorExecutionProgressDescr) {
		this.operatorExecutionProgressDescr = operatorExecutionProgressDescr;
	}

	/** Return all parents of this node.
	 * Parents are define to be nodes with at least one edge connecting
	 * a output parameter of this source node to the node considered.
	 * The interior shadow node is define to have no parents (inside this work flow).
	 * 
	 * @return all parents
	 */
	public Set<ALDWorkflowNode> getParents() {
		HashSet<ALDWorkflowNode> indices = new HashSet<ALDWorkflowNode>();
		if ( this.isInteriorShadowNode)
			return indices;
		
		for ( ALDWorkflowEdge edge : this.inEdges ) {
			indices.add( edge.getSourceNode());
		}
		
		return indices;
	}
	
	/** Return all children nodes of this node. 
	 * Children are define to be nodes with at least one edge connecting
	 * a input parameter of this child node to the node considered.
	 * The interior shadow considered not to be a child of any node (inside this work flow).
	 * 
	 * @return indices of all child nodes
	 */
	public Set<ALDWorkflowNode> getChildren() {
		HashSet<ALDWorkflowNode> indices = new HashSet<ALDWorkflowNode>();
		for ( ALDWorkflowEdge edge : this.outEdges ) {
			if ( ! edge.getTargetNode().isInteriorShadowNode )
				indices.add( edge.getTargetNode());
		}

		return indices;
	}

	/**
	 * Return the nodeIds of all descendants of the given node.
	 *
	 * @param nodeId
	 * @return nodeIds of all descendants
	 */
	public Set<ALDWorkflowNode> getDescendants() {
		HashSet<ALDWorkflowNode> descendants = new HashSet<ALDWorkflowNode>(this.getChildren());
		
		for ( ALDWorkflowNode descendant : this.getChildren() ) {
			descendants.addAll( descendant.getDescendants());
		}
		return descendants;
	}
	
	/**
	 * Return all ancestors of this node.
	 *
	 * @return all descendants
	 */
	public Set<ALDWorkflowNode> getAncestors()  {
		HashSet<ALDWorkflowNode> ancestors = new HashSet<ALDWorkflowNode>(this.getParents());
		
		for ( ALDWorkflowNode ancestor : this.getParents() ) {
			ancestors.addAll( ancestor.getAncestors());
		}
		return ancestors;
	}

	/** Return all incoming edges for this node which
	 * have their target at <code>parameterName</code>.
	 * 
	 * @param parameterName 
	 * @return indices of all incoming edges which have their target at <code>parameterName</code>
	 */
	public Collection<ALDWorkflowEdge> getInEdgesForParameter( String parameterName)  {
		Vector<ALDWorkflowEdge> edges = new Vector<ALDWorkflowEdge>();
		for ( ALDWorkflowEdge edge : this.getInEdges() ) {
			if ( parameterName.equals( edge.getTargetParameterName()) ) {
				edges.add( edge);
			}
		}

		if ( ALDWorkflow.debug >= 2) {
			System.out.println( "ALDWorkflowNode::getInEdgesForParameter for node <" +
					ALDWorkflow.mapNodeToNodeId(this) + "> and parameter " + parameterName + ": " +
					ALDWorkflow.edgeIdsToString(edges));
		}
		return edges;
	}
	
	/** Return outgoing edges for this node which
	 * have their source at <code>parameterName</code>.
	 * 
	 * @param parameterName 
	 * @return indices of all outgoing edges which have their source at <code>parameterName</code>
	 */
	public Collection<ALDWorkflowEdge> getOutEdgesForParameter( String parameterName)  {
		Vector<ALDWorkflowEdge> edges = new Vector<ALDWorkflowEdge>();
		for ( ALDWorkflowEdge edge : this.outEdges ) {
			if ( parameterName.equals( edge.getSourceParameterName()) ) {
				edges.add( edge);
			}
		}

		if ( ALDWorkflow.debug >= 2) {
			System.out.println( "ALDWorkflowNode::getOutEdgesForParameter for node <" +
					ALDWorkflow.mapNodeToNodeId(this) + "> and parameter " + parameterName + ": " +
					edges);
		}
		return edges;
	}
	
	/** 
	 * Checks if this node is configured.
	 * A node is configured if all required input parameters have a non-null value
	 * in the operator object associated with the node or gave an incoming
	 * edge in the work flow.
	 * 
	 * @return
	 */
	public boolean isConfigured()  {
		if ( this.getMissingRequiredInputs().size() == 0) {
			return true;
		} else {
			return false;
		}
	}
	
	/**
	 * Returns the names of all required input parameters of the operator object associated with the node
	 * which are not linked and have a value of null
	 * 
	 * @return
	 */
	public Collection<String> getMissingRequiredInputs() {
		List<String> missingInputs = this.getOperator().getMissingRequiredInputs();
		if ( ALDWorkflow.debug >= 2 ) {
			System.out.println("    ALDWorkflowNode::getMissingRequiredInputs missing in operator" + missingInputs);	
		}

		LinkedList<String> missing = new LinkedList<String>( missingInputs);
		for ( String paramName : missingInputs) {
			if ( this.getInEdgesForParameter( paramName).size() != 0 ) {
				missing.remove( paramName);
			}
		}
		
		if ( ALDWorkflow.debug >= 2  ) {
			System.out.println("    ALDWorkflowNode::getMissingRequiredInputs missing considering also edges" + missingInputs);	
		}
		return missing;
	}
	
	/**
	 * Reset the parameter <code>parameterName</code> in the operator associated to
	 * this node.
	 * Currently this sets the parameters value to null.
	 * 
	 * @param parameterName
	 * @throws ALDWorkflowException if the parameter does not exists
	 */
	 void resetParameter( String parameterName) throws ALDWorkflowException {
		try {
			this.getOperator().setParameter( parameterName, null);
		} catch (ALDOperatorException e) {
			throw( new ALDWorkflowException( WorkflowExceptionType.FATAL_INTERNAL_ERROR,
					"Cannot reset  parameter <" + parameterName +
					"> in operator <" + this.getOperator().getName() + ">"));
		}

	}

	/**
	 * Print information of this node to standard out.
	 */
	public void print() {
		System.out.println( "ALDWorkflowNode <" + ALDWorkflow.mapNodeToNodeId(this) + "> for operator " + op.getName());
		try {
			System.out.println( "    verbose = " + op.getVerbose());
		} catch (ALDOperatorException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		System.out.println( "  incoming edges");
		for ( ALDWorkflowEdge edge : inEdges) {
			edge.print();
		}
		
		System.out.println( "  out going edges");
		for ( ALDWorkflowEdge edge : outEdges) {
			edge.print();
		}
	}


}
