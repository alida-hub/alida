/**
 * This class represents an edge in an Alida work flow.
 * An edge connects parameters of two work flow nodes, i.e. essentially operators.
 * The parameter in the target node needs to be of direction <code>OUT</code> or
 * <code>INOUT</code>, while the parameter in the target node needs to be of
 * direction <code>IN</code> or
 * <code>INOUT</code>.
 * <p>
 * Nodes are identified by their indices within the work flow, parameters by their names.
 */
package de.unihalle.informatik.Alida.workflows;

import de.unihalle.informatik.Alida.dataconverter.ALDDataConverter;



/**
 * This class represents an edge within an work flow.
 * It connects an output parameter of the source node with an input parameter of the
 * target node.
 * Parameters are referenced via names.
 * 
 * @author posch
 *
 */
public class ALDWorkflowEdge {
	
	/** 
	 * source node of this edge in the work flow
	 */
	private ALDWorkflowNode sourceNode;
	
	/** 
	 * parameter name in source node
	 */
	private String sourceParameterName;
	
	/** 
	 *  target node of this edge in the work flow
	 */
	private ALDWorkflowNode targetNode;
	
	/** 
	 * parameter name in target node
	 */
	private String targetParameterName;

	/**
	 * This field is necessary for (de)serialization as the provider
	 * cannot be (de)serialized.
	 * Should be true iff converter <> null
	 */
	private boolean needConverter;
	
	/**
	 * a data converter to convert source to target class if necessary,
	 * i.e. the class of the target parameter is not assignable from the source parameter
	 */
	private transient ALDDataConverter converter;

	/** Create an edge with the given source and target.
	 * 
	 * @param sourceNode
	 * @param sourceParameterName
	 * @param targetNode
	 * @param targetParameterName
	 */
	public ALDWorkflowEdge(	ALDWorkflowNode sourceNode, String sourceParameterName, 
			ALDWorkflowNode targetNode, String targetParameterName) {
		super();
		this.sourceNode = sourceNode;
		this.sourceParameterName = sourceParameterName;
		this.targetNode = targetNode;
		this.targetParameterName = targetParameterName;
		this.sourceNode.outEdges.add(this);
		this.targetNode.inEdges.add(this);
		
		this.setConverter(null);
	}

	/** 
	 * Returns the source node.
	 * 
	 * @return
	 */
	public ALDWorkflowNode getSourceNode() {
		return sourceNode;
	}

	/** 
	 * Returns the parameter name of the source.
	 * 
	 * @return
	 */
	public String getSourceParameterName() {
		return sourceParameterName;
	}

	/** 
	 * Returns the target node.
	 * 
	 * @return
	 */
	public ALDWorkflowNode getTargetNode() {
		return targetNode;
	}

	/** 
	 * Returns the parameter name of the target.
	 * 
	 * @return
	 */
	public String getTargetParameterName() {
		return targetParameterName;
	}

	/**
	 * Return the id associated with this edge.
	 * 
	 * @return
	 */
	public Integer getId() {
		return ALDWorkflow.mapEgdeToEdgeId(this).id;
	}
	/**
	 * Redirect the source of this edge
	 * 
	 * @param newSourceNode
	 * @param newSourceParameterName
	 */
	protected void redirectSource(ALDWorkflowNode newSourceNode,
			String newSourceParameterName) {
		this.sourceNode.outEdges.remove( this);
		this.sourceNode = newSourceNode;
		this.sourceNode.outEdges.add( this);
		this.sourceParameterName = newSourceParameterName;
	}

	/** 
	 * Redirect the target of this node.
	 * 
	 * @param newTargetNode
	 * @param newTargetParameterName
	 */
	protected void redirectTarget( ALDWorkflowNode newTargetNode, String newTargetParameterName) {
		this.targetNode.inEdges.remove( this);
		this.targetNode = newTargetNode;
		this.targetNode.inEdges.add(this);
		this.targetParameterName = newTargetParameterName;
	}

	/**
	 * @return the converter
	 */
	public ALDDataConverter getConverter() {
		return converter;
	}

	/**
	 * @param converter the converter to set
	 */
	protected void setConverter(ALDDataConverter converter) {
		if ( converter != null) 
			needConverter = true;
		else
			needConverter = false;
		
		this.converter = converter;
	}

	/**
	 * @return the needConverter
	 */
	public boolean isNeedConverter() {
		return needConverter;
	}

	/**
	 * Print information of this edge to standard out.
	 */
	public void print() {
		System.out.println( "   edge <" + ALDWorkflow.mapEgdeToEdgeId(this) + ">: " + sourceNode.getId() + " (" + sourceParameterName +
				            ") -->" + targetNode.getId() + " (" + targetParameterName + ")");
	}

}
