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

package de.unihalle.informatik.Alida.workflows;

import de.unihalle.informatik.Alida.dataio.ALDDataIOManagerXmlbeans;
import de.unihalle.informatik.Alida.dataio.provider.xmlbeans.ALDStandardizedDataIOXmlbeans;
import de.unihalle.informatik.Alida.exceptions.ALDDataIOManagerException;
import de.unihalle.informatik.Alida.exceptions.ALDDataIOProviderException;
import de.unihalle.informatik.Alida.exceptions.ALDDataIOProviderException.ALDDataIOProviderExceptionType;
import de.unihalle.informatik.Alida.exceptions.ALDWorkflowException;
import de.unihalle.informatik.Alida.operator.ALDOperator;
import de.unihalle.informatik.Alida.operator.ALDParametrizedClassDataIOXmlbeans;
import de.unihalle.informatik.Alida.annotations.ALDDataIOProvider;
import de.unihalle.informatik.Alida_xml.ALDXMLObjectType;
import de.unihalle.informatik.Alida_xml.ALDXMLParametrizedType;
import de.unihalle.informatik.Alida_xml.ALDXMLWorkflowEdgeType;
import de.unihalle.informatik.Alida_xml.ALDXMLWorkflowNodeType;
import de.unihalle.informatik.Alida_xml.ALDXMLWorkflowType;
import de.unihalle.informatik.Alida_xml.ALDXMLOperatorType;

import java.lang.reflect.Field;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;

/**
 * DataIO provider for ALDWorkflow from xml using xmlbeans.
 * 
 * <p>
 * Ensure higher priority then provider {@link ALDParametrizedClassDataIOXmlbeans} 
 * for ALDOperator.
 * @author posch
 */

@ALDDataIOProvider(priority=2)
public class ALDWorkflowDataIOXmlbeans extends ALDStandardizedDataIOXmlbeans {

	/**
	 * debug messages
	 */
	private boolean debug = false;

	/**
	 * Interface method to announce class for which IO is provided for
	 * 
	 * @return  Collection of classes provided
	 */
	@Override
	public Collection<Class<?>> providedClasses() {
		LinkedList<Class<?>> classes = new LinkedList<Class<?>>();

		classes.add( ALDWorkflow.class);

		return classes;
	}

	// TODO: what should we do with workflowContext
	@Override
	public Object readData(Field field, Class<?> cl, ALDXMLObjectType aldXmlObject, Object obj) 
			throws ALDDataIOProviderException, ALDDataIOManagerException {
		
		if ( debug ) {
			System.out.println( "ALDWorkflowDataIOXmlbeans::readData");
		}
		
		if ( aldXmlObject == null || aldXmlObject.isNil()) 
			return null;

		if (ALDWorkflow.class.isAssignableFrom(cl) ) {
			
			// allow additional fields when reading nested xml objects
			// TODO WARNING: this is not thread save
			boolean saveAllowAdditionalFields = ALDDataIOManagerXmlbeans.getInstance().isAllowAdditionalFields();
			ALDDataIOManagerXmlbeans.getInstance().setAllowAdditionalFields(true); 

			// (optionally) create a workflow object
			ALDWorkflow workflow = null;
			if ( obj != null ) {
				try {
					workflow = (ALDWorkflow) obj;
				} catch (Exception e) {
					throw new ALDDataIOProviderException(ALDDataIOProviderExceptionType.OBJECT_TYPE_ERROR,
							"ALDWorkflowDataIOXmlbeans::readData obj has invalid class<" +
									obj.getClass().getName() + ">");
				}
			}

			// read the workflow;
			ALDXMLWorkflowType aldXmlWorkflow = (ALDXMLWorkflowType) aldXmlObject;

			// read the ALDOperator part
			ALDParametrizedClassDataIOXmlbeans provider = new ALDParametrizedClassDataIOXmlbeans();
			workflow = (ALDWorkflow) provider.
					readData(field, cl, (ALDXMLOperatorType)aldXmlWorkflow.getOperator(), workflow);
			workflow.setName( ((ALDXMLOperatorType)aldXmlWorkflow.getOperator()).getOpName());
			workflow.setResetDisconnectedInput(aldXmlWorkflow.getResetDisconnectedInput());
			
			// read the nodes, i.e. operators
			
			// map idx == order in external xml representation to NodeIds
			workflow.loadIndexToNodeId = new ALDWorkflowNodeID[aldXmlWorkflow.getNodesArray().length];
			
			Integer i = 0;
			for ( ALDXMLWorkflowNodeType aldXmlWorkflowNode : aldXmlWorkflow.getNodesArray()) {
				workflow.loadIndexToNodeId[i] = null;
				try {
					ALDOperator operator = (ALDOperator) ALDDataIOManagerXmlbeans.getInstance().
							readData(null, ALDOperator.class, aldXmlWorkflowNode.getOperator());
					ALDWorkflowNodeID nodeId;
					nodeId = workflow.createNode(operator);
					workflow.loadIndexToNodeId[i] = nodeId;

				} catch (ALDDataIOProviderException e) {
					if ( debug )
						System.out.println("failed to create node for operator " + aldXmlWorkflowNode.getOperator().getClassName());
				} catch (ALDWorkflowException e) {
					if ( debug ) 
						System.out.println("failed to create node for operator " + aldXmlWorkflowNode.getOperator().getClassName());
				}
				
				i++;
			}
			
			// read the edges
			workflow.loadIndexToEdgeId = new ALDWorkflowEdgeID[aldXmlWorkflow.getEdgesArray().length];
			i = 0;
			for ( ALDXMLWorkflowEdgeType aldXmlWorkflowEdge : aldXmlWorkflow.getEdgesArray()) {
				workflow.loadIndexToEdgeId[i] = null;
				
				try {
					ALDWorkflowEdgeID edgeId = workflow.createEdge( 
							workflow.loadIndexToNodeId[aldXmlWorkflowEdge.getSourceNodeId()], 
							aldXmlWorkflowEdge.getSourceParameterName(), 
							workflow.loadIndexToNodeId[aldXmlWorkflowEdge.getTargetNodeId()], 
							aldXmlWorkflowEdge.getTargetParameterName(),
							true);
					workflow.loadIndexToEdgeId[i] = edgeId;
				} catch (ALDWorkflowException e) {
					if ( debug ) 
						System.out.println( "failed to create edge " + aldXmlWorkflowEdge.getSourceParameterName() +
								" --> " + aldXmlWorkflowEdge.getTargetParameterName());
				}
				
				i++;
			}
			
			// reset allowAdditionalFields in manager singleton
			ALDDataIOManagerXmlbeans.getInstance().setAllowAdditionalFields(saveAllowAdditionalFields); 

			return workflow;

		} else {
			throw new ALDDataIOProviderException(ALDDataIOProviderExceptionType.OBJECT_TYPE_ERROR, 
					"ALDNativeDataIOXmlbeans::readData cannot read object of type " +
							cl.getCanonicalName() + ">" +
							" from <" + aldXmlObject.toString() + ">\n");
		}		
	}

	@Override
	public ALDXMLObjectType writeData(Object obj) throws ALDDataIOProviderException, ALDDataIOManagerException  {
		
		Class<?> cl = obj.getClass();

		if ( obj instanceof ALDWorkflow ) {
			ALDXMLWorkflowType aldXmlWorkflow = ALDXMLWorkflowType.Factory.newInstance();
			aldXmlWorkflow.setClassName(obj.getClass().getName());
			
			ALDWorkflow workflow = (ALDWorkflow) obj;
			ALDParametrizedClassDataIOXmlbeans provider = new ALDParametrizedClassDataIOXmlbeans();
			ALDXMLParametrizedType xmlOperator = 
					(ALDXMLParametrizedType)provider.writeData(workflow);
			aldXmlWorkflow.setOperator(xmlOperator);
			aldXmlWorkflow.setResetDisconnectedInput(workflow.isResetDisconnectedInput());
			
			// write each workflow node and construct a hash node to its index in the xml node array
			// we need to write the nodes in the order they appear in the nodes list of the workflow!!
			HashMap<ALDWorkflowNode,Integer> nodeIdMap = new HashMap<ALDWorkflowNode, Integer>();
			Integer i = 0;
			for ( ALDWorkflowNode node : workflow.getNodes() ) {
				aldXmlWorkflow.insertNewNodes(i);
				aldXmlWorkflow.setNodesArray(i, createALDXMLWorkflowNodeType( node));
				nodeIdMap.put(node,i);
				i++;
			}
			
			// now write all edges
			i = 0;
			for ( ALDWorkflowEdge edge : workflow.getEdges()) {
				try {
					ALDWorkflowEdgeID edgeID = ALDWorkflow.mapEgdeToEdgeId(edge);
					aldXmlWorkflow.insertNewEdges(i);
					aldXmlWorkflow.setEdgesArray(i, 
							createALDXMLWorkflowEgdeType( workflow.getEdge(edgeID), nodeIdMap));
				} catch (ALDWorkflowException e) {
					System.err.println( "ALDWorkflowDataIOXmlbeans::writeData internal error, cannot map edgeId to egde");
				}
				
				i++;
			}
				
			return aldXmlWorkflow;
		} else {
			throw new ALDDataIOProviderException(ALDDataIOProviderExceptionType.OBJECT_TYPE_ERROR,
					"ALDWorkflowDataIOXmlbeans::writeData invalid class<" +
					cl.getName() + ">");
		}
	}
	
	/**
	 * Create a xml object for a workflow node
	 * 
	 * @param node
	 * @return
	 * @throws ALDDataIOProviderException
	 * @throws ALDDataIOManagerException
	 */
	private ALDXMLWorkflowNodeType createALDXMLWorkflowNodeType( ALDWorkflowNode node) 
			throws ALDDataIOProviderException, ALDDataIOManagerException {
		ALDXMLWorkflowNodeType aldXmlWorkflowNode = ALDXMLWorkflowNodeType.Factory.newInstance();
		aldXmlWorkflowNode.setClassName(ALDWorkflowNode.class.getName());
		aldXmlWorkflowNode.setIsInteriorShadowNode(node.isInteriorShadowNode);
		
		ALDParametrizedClassDataIOXmlbeans provider = new ALDParametrizedClassDataIOXmlbeans();
		ALDXMLParametrizedType xmlOperator = 
				(ALDXMLParametrizedType)provider.writeData(node.getOperator());
		aldXmlWorkflowNode.setOperator(xmlOperator);
		
		return aldXmlWorkflowNode;

	}
	
	/** Crreate a xml object for a workflow edge
	 * 
	 * @param edge
	 * @param nodeIdMap
	 * @return
	 */
	private ALDXMLWorkflowEdgeType createALDXMLWorkflowEgdeType( ALDWorkflowEdge edge,
			HashMap<ALDWorkflowNode,Integer> nodeIdMap) {
		ALDXMLWorkflowEdgeType aldXmlWorkflowEdge = ALDXMLWorkflowEdgeType.Factory.newInstance();
		aldXmlWorkflowEdge.setClassName(ALDWorkflowEdge.class.getName());
		aldXmlWorkflowEdge.setSourceNodeId(nodeIdMap.get(edge.getSourceNode()));
		aldXmlWorkflowEdge.setSourceParameterName(edge.getSourceParameterName());
		aldXmlWorkflowEdge.setTargetNodeId(nodeIdMap.get(edge.getTargetNode()));
		aldXmlWorkflowEdge.setTargetParameterName(edge.getTargetParameterName());
		
		return aldXmlWorkflowEdge;

		
	}
}
