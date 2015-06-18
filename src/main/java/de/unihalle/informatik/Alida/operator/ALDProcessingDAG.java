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

package de.unihalle.informatik.Alida.operator;

import de.unihalle.informatik.Alida.exceptions.*;
import de.unihalle.informatik.Alida.helpers.ALDFilePathManipulator;
import de.unihalle.informatik.Alida.operator.ALDOperator.HidingMode;

import java.util.*;

import org.apache.xmlbeans.*;
import org.graphdrawing.graphml.xmlns.*;

/** This class handles the construction of a processing DAG from opNode instances
    and writing this processing history as extended graphml to file.
 */

public class ALDProcessingDAG {

	/** Type of the processing history to be generated.
	 * COMPLETE creates a complete history for all opnodes, DATADEPENDENCIES
	 * history according to data dependencies for all opnodes, 
	 * OPNODETYPE respects the preference of the operators.
	 */
	public enum HistoryType { COMPLETE, DATADEPENDENCIES, OPNODETYPE};

	/** Type of the history to be generated.
	 */
	private HistoryType historyType = HistoryType.DATADEPENDENCIES;

	/** debug flag for debugging output.
	 * We make this static to be able to control from outside as instances of
	 * this class are created inside methods we use.
	 */
	private static boolean debug = false;

	/** Ignore hidden flag of opnodes when creating a processing history.
	 * i.e. all opnodes are visible.
	 */
	private boolean ignoreHiding = false;

	/** Character separating graphId prefix from rest
	 */
	public static final char idSeparator = ':';

	/** This index is the index used to build the prefix for all Ids for elements
	 *  for the next history to add to the current graphml.
	 *  The currently build graph has index 0.
	 */
	private int graphIndex; 

	// these are the items we have found durung traceback and will e part of the 
	// processing history, i.e. graphml history
	private Vector<ALDOpNode> allOpNodes; // opNodes found in this processing DAG
	private Vector<ALDEdge> allEdges; // edges in processing DAG
	private Vector<ALDDataPort> allDataports; // all data in processing DAG

	private Vector<ALDOpNode> opNodesClone; // opNodes clone to generate unique Ids 
	private Vector<ALDOpNode> opNodesInGraph; // opNodes written to the graph used for checks in addEdge
	private Vector<ALDOpNodePort> opNodePortsTraced; // all OpNode ports already traced in processing DAG
	private Vector<ALDDataPort> writtenDataports; // data already written to the DAG

	/** Set the debug state 
	 * @param   value     New debug state
	 */
	public static void setDebug( boolean value) {
		debug = value;
	}

	/** Return the debug state 
	 * @return	Current debug state
	 */
	public static boolean getDebug() {
		return debug;
	}

	/** Trace back the implicit DAG created during processing for sourceObjOfHistory.
	 * As a side effect the vector allOpNodes, allEdges, allDataports are filled
	 * containing all opnodes, dataports and edges as elements of the processing
	 * history of sourceObjOfHistory.
	 *
	 * @param	sourceObjOfHistory	Object for which the processing history is to be created
	 */
	private void tracebackDAG( Object sourceObjOfHistory) throws ALDProcessingDAGException {
		graphIndex = 1;
		allOpNodes = new Vector<ALDOpNode>();
		allEdges = new Vector<ALDEdge>();
		opNodePortsTraced = new Vector<ALDOpNodePort>();
		allDataports = new Vector<ALDDataPort>();
		writtenDataports = new Vector<ALDDataPort>();

		ALDPort port = ALDOperator.portHashAccess.getHistoryLink(sourceObjOfHistory);

		if ( debug ) {
			System.out.println( "ALDProcessingDAG::tracebackDAG");
		}

		if ( port == null ) {
			System.err.println( "ALDProcessingDAG::tracebackDAG sourceObjOfHistory not registered!");
			throw new ALDProcessingDAGException(ALDProcessingDAGException.DAGExceptionType.INTERNAL_TRACING_ERROR,null);
		}

		if (  port instanceof ALDDataPort ) {
			// we cannot find an opnode for a data port, thus the history will only be this data port
			// this will probably never happen, as we can not find a port associated to this object
			allDataports.add( (ALDDataPort)ALDOperator.portHashAccess.getHistoryLink(sourceObjOfHistory) );

		} else if ( port instanceof ALDOpNodePort ) {
			// either an output port or an input port of a complete opNode
			
			ALDOpNode opNode = ((ALDOpNodePort)port).getOpNode();

			// this does not work across threads
			/*
			if ( historyType == HistoryType.COMPLETE && ignoreHiding) {
				ALDOpNode toplevelOpNode = opNode;
	
				while ( toplevelOpNode != null && ! isToplevelOpnode( toplevelOpNode) ) {
					toplevelOpNode = toplevelOpNode.getParent();
				}
				if ( toplevelOpNode == null ) fatal("tracebackDAG can not find toplevel opnode");
	
				visitOpNodeComplete( toplevelOpNode, 0);

			} else {
			*/

			// traverse bottom up beginning at the port of sourceObjOfHistory

			// input ports of the first opnode of sourceObjOfHistory which
			// we have to trace back
			Vector<ALDInputPort> inputPorts;

			if ( ! isHiddenOpnode( opNode)) {

				if ( port instanceof ALDInputPort ) {
					if ( ! isCompleteOpnode( opNode) ) {
						// we want to traceback an input port according to data dependency

						// add the opNode and traceback this input port
						allOpNodes.add( opNode);
						inputPorts = new Vector<ALDInputPort> ();
						inputPorts.add( (ALDInputPort)port);
					} else {
						inputPorts = visitOpNodeComplete( opNode, 0);
					}
				} else {
					// visit the opNode with port as interesting output port
					inputPorts = visitOpNode( opNode, (ALDOutputPort)port, 0);
				}

				// trace back the sibblings and parent
				if (opNode != null)
					findSibblingsAndParent( opNode, inputPorts, 0);

			} else {
				// empty history as the opNode is hidden
			}
		} else {
			fatal( "tracebackDAG illegal port type of port " + port);
		}
		
		// clone of opNode vector used to generate unique Ids for opnodes
		opNodesClone = (Vector<ALDOpNode>)(allOpNodes.clone());

		if ( debug ) {
			System.out.println( ">>>>>>>>>>>>> opNodes found");
			Iterator<ALDOpNode> oItr = allOpNodes.iterator();
			while ( oItr.hasNext() ) {
				oItr.next().print();
			}

			System.out.println( ">>>>>>>>>>>>> edges found");
			Iterator<ALDEdge> eItr = allEdges.iterator();
			while ( eItr.hasNext() ) {
				eItr.next().print( "    ");
			}

			System.out.println( ">>>>>>>>>>>>> data found");
			Iterator<ALDDataPort> dItr = allDataports.iterator();
			while ( dItr.hasNext() ) {
				System.out.println( dItr.next());
			}
		}
	}

	/** Find all sibblings and the parent of opNode. If history is of type data dependency,
	 * then find everything on which the inputPorts given as arguments (which are input ports of opNode)
	 * depend on. If complete then visit all childs of the opNodes parent completey.
	 * <p>
	 * findSibblingsAndParent invokes itself recursively using
	 * the input ports of the parent on which the history of the parent depends on.
	 * These are all input ports if we visit the parent completely, otherwise
	 * the data dependencies of the inputPorts.
	 * The recursion terminates at the toplevel operator.
	 */
	private void findSibblingsAndParent( ALDOpNode opNode, Vector<ALDInputPort> inputPorts, int depth) 
		throws ALDProcessingDAGException {
	
		if ( debug ) 
			System.out.println( "findSibblingsAndParent for " + opNode.getName() +
				" and inputPorts " + inputPorts);

		// ALDToplevelOperator has no sibblings and parent
		if (  isToplevelOpnode( opNode) ) 
			return;

		ALDOpNode parentOpNode = opNode.getParent();

		//TODO potential check; are inputPorts ports of opNode?

		// the inputports of the parent, on which the history depends on
		Vector<ALDInputPort> parentsInputports = new Vector<ALDInputPort>();

		if ( isCompleteOpnode( parentOpNode) && ! isHiddenOpnode( parentOpNode) ) {
			visitOpNodeComplete( parentOpNode, depth-1);

			// and go one level up for all input ports of the parent
        	ALDInputPort[] inputPortsArray = parentOpNode.getInputPorts();
        	for ( int i=0 ; i < inputPortsArray.length ; i++ ) {
            	parentsInputports.add( inputPortsArray[i]);
        	}

			findSibblingsAndParent( parentOpNode, parentsInputports, depth-1);
		} else {
			// either the parent is hidden thus not visited or it is uncomplete
			// thus we search for data dependencies of the input ports of opNode

			// output ports of opNode or sibbligs of opNode which we still have to trace back
			LinkedList<ALDOutputPort> outputPortToTraceback = new LinkedList<ALDOutputPort>();

			// trace back each port of inputPorts
			// origin may be input port of parentNode (added to parentsInputports as a sid effect)
			// an output port of a sibbling (added to outputPortToTraceback as a sid effect)
			// or a data port (register directely)
			handleInputPorts( parentOpNode, inputPorts, parentsInputports, outputPortToTraceback);

			// now trace back all output ports of opNode and its sibblings
			// according to data dependency
			while ( outputPortToTraceback.size() > 0) {
				ALDOutputPort port = outputPortToTraceback.pop();

				handleInputPorts( parentOpNode, visitOpNode( port.getOpNode(), port, depth),
					parentsInputports, outputPortToTraceback);
			}

			if ( debug ) 
				System.out.println( "findSibblingsAndParent found parentsInputports " + parentsInputports);

			// as the parent is either hidden or uncomplete
			// we have to visit it only if it is not hidden and we found
			// at least one input port of the parent on which the given inputPorts depend on
			if ( parentsInputports.size() > 0 && ! isHiddenOpnode( parentOpNode) ) {
				// probably this should never happen
				if ( ! allOpNodes.contains( parentOpNode) ) {
					allOpNodes.add( parentOpNode);
					parentOpNode.setDepth( depth-1);
					findSibblingsAndParent( parentOpNode, parentsInputports, depth-1);
				}
			}
			
		}
	}

	/** Trace back each port of inputPorts which are input ports of a child of parentOpNode.
	 * If the origin of such an input port is of type input port, then it is a port
	 * of parentOpNode and added to parentsInputports.
	 * (As this are the input ports detected according to data dependency.)
	 * If it is of type output port, then is is a port of a sibbling and needs to
	 * be traced back if the sibbling is not hidden, thus added to outputPortToTraceback.
	 * If it is a data port is is just registered.
	 */
	private void handleInputPorts( ALDOpNode parentOpNode, Vector<ALDInputPort> inputPorts, 
					Vector<ALDInputPort> parentsInputports, LinkedList<ALDOutputPort> outputPortToTraceback) 
		throws ALDProcessingDAGException {

		if ( debug ) 
			System.out.println( "	handleInputPorts for parentOpNode " + parentOpNode.getName() +
				"and inputPorts " + inputPorts);

		Iterator<ALDInputPort> pItr = inputPorts.iterator();
		while ( pItr.hasNext() ) {
			ALDInputPort port = pItr.next();
			ALDPort origin = port.getOrigin();

			if ( origin != null ) {
				if ( origin instanceof ALDDataPort ) {
                	if ( ! allDataports.contains( port) ) {
                    	register( parentOpNode, (ALDDataPort)origin);
                    	allDataports.add( (ALDDataPort)origin);
                	}
                	allEdges.add( new ALDEdge( origin, port));
	
				} else if ( origin instanceof ALDInputPort ) {
					// we allow different parents if child of toplevel opnode to
					// find opnodes in other threads
					if ( ((ALDInputPort)origin).getOpNode() != parentOpNode && ! isToplevelOpnode( parentOpNode) ) {
						inconsistent( "handleInputPorts origin is of type ALDInputPort but its opNode != parentOpNode");
					} else {	
						if ( ! isHiddenOpnode( parentOpNode) ) {
							parentsInputports.add( (ALDInputPort)origin);
							allEdges.add( new ALDEdge( origin, port));
						}
					}
	
				} else if ( origin instanceof ALDOutputPort ) {
					// we allow different parents if child of toplevel opnode to
					// find opnodes in other threads
					if ( ((ALDOutputPort)origin).getOpNode().getParent() != parentOpNode && ! isToplevelOpnode( parentOpNode) ) {
						inconsistent( "handleInputPorts origin is of type ALDOutputPort but the parent of its opNode != parentOpNode");
					} else {
						if ( ! isHiddenOpnode( ((ALDOutputPort)origin).getOpNode()) ) {
							outputPortToTraceback.push( (ALDOutputPort)origin);
							allEdges.add( new ALDEdge( origin, port));
						}
					}
				} else {
					fatal( "handleInputPorts origin is of illegal type: " + origin);
				}
			}
		}
	}


	/** Visit the opNode either completely or according to data dependency as define by the opNode
	 * and the globale state of the processing DAG.
	 * If history is according to data dependency then trace back the history
	 * of portOfInterest which is an output port of opNode.
	 *
	 * @return Input ports of opNode on which its history depends on. These are all input ports for
	 *         a complete history, otherwise the input ports, on which portOfInterest  depends on.
	 */
	private Vector<ALDInputPort> visitOpNode( ALDOpNode opNode, ALDOutputPort portOfInterest, int depth) 
		throws ALDProcessingDAGException {
		if ( debug ) 
			System.out.println( "    ALDProcessingDAG::visitOpNode visit " + opNode.getName() + " for port " + portOfInterest);

		if ( isCompleteOpnode( opNode) )
			return visitOpNodeComplete( opNode, depth);
		else
			return visitOpNodeDatadependency( opNode, portOfInterest, depth);
	}

	/** Visit the opNode detecting all inner children and data ports on which
	 * the output port of interest portOfInterest depends on. 
	 * Add the opNode to the processing DAG as well as the detected children and
	 * dataports. Recursively visit the detected children of this opNode.
	 * The method assumes, that opNode is not hidden.
	 * 
	 * @return Input ports of this opNode on which the output port of interest portOfInterest
	 * 			depends on.
	 */
	private Vector<ALDInputPort> visitOpNodeDatadependency( ALDOpNode opNode, ALDOutputPort portOfInterest, int depth) 
		throws ALDProcessingDAGException {

		if ( debug ) 
			System.out.println( "    ALDProcessingDAG::visitOpNodeDatadependency visit " + opNode.getName() + " for port " + portOfInterest);

		if ( isHiddenOpnode( opNode) )
			fatal( "visitOpNodeDatadependency opNode " + opNode.getName() + " is hidden");

		// TODO potential check
		// ##if ( ! outputPorts.contains( portOfInterest) )
		// ##	fatal( "visitOpNodeDatadependency port of interest is no output port of " + opNode.getName());

		Vector<ALDInputPort> inputPorts = new Vector<ALDInputPort>();

		// if we have already traced back the portOfInterest just return an empty vector of input ports
		if (  opNodePortsTraced.contains( portOfInterest) ) 
			return inputPorts;

		// the opNode may have already be discovered, but for other ports of interest
		if ( ! allOpNodes.contains( opNode) ) {
			allOpNodes.add( opNode);
			opNode.setDepth( depth);
		}

		// output ports of opNodes or children of opNode on which the port of interest depends on
		LinkedList<ALDOpNodePort> portsToVisit = new LinkedList<ALDOpNodePort>(); 
		portsToVisit.add( portOfInterest);

		while( portsToVisit.size() > 0 ) {
			ALDOpNodePort port = portsToVisit.pop();

	 		// Port is either an OutputPort of opNode or an InputPort of a child of opNode.
	 		// If the origin of port is of type OutputPort, than a potentially new source child is found
	 		// and recursively visited.
	 		// If it is of type DataPort, than a dataport of opNode is found and registered.

			if ( ! opNodePortsTraced.contains( port) ) {
				opNodePortsTraced.add( portOfInterest);
        		ALDPort origin = port.getOrigin();

				if ( debug ) 
					System.out.println( "    ALDProcessingDAG::visitOpNodeDatadependency trace back port " + port +
						" with origin " + origin);

        		if ( origin != null ) {
            		if ( origin instanceof ALDInputPort ) {
						if ( ((ALDInputPort)origin).getOpNode() != opNode ) {
							inconsistent( "visitOpNodeDatadependency origin is of type ALDInputPort but has different parent then opNode: " + ((ALDInputPort)origin).getOpNode() + " vs " + opNode);
						} else {
            				allEdges.add( new ALDEdge( origin, port));
							inputPorts.add( (ALDInputPort)origin);
						}
            		} else if ( origin instanceof ALDOutputPort ) {
						ALDOpNode originatingOpNode = ((ALDOutputPort)origin).getOpNode();
						if ( originatingOpNode.getParent() != opNode ) {
							inconsistent( "visitOpNodeDatadependency origin is of type ALDOutputPort but its opNode has different parent then opNode" + originatingOpNode.getParent() + " vs " + opNode);
						} else {
							if ( ! isHiddenOpnode( originatingOpNode) ) {
            					allEdges.add( new ALDEdge( origin, port));
								portsToVisit.addAll( visitOpNode( originatingOpNode, (ALDOutputPort)origin, depth+1));
							}
						}
            		} else if ( origin instanceof ALDDataPort ) {
                		if ( ! allDataports.contains( origin) ) {
                    		register( opNode, (ALDDataPort)origin);
                    		allDataports.add( (ALDDataPort)origin);
                		}
            			allEdges.add( new ALDEdge( origin, port));
            		} else { 
						fatal( "visitOpNodeDatadependency illegal orign type: " + origin);
        			}
        		}
			}
		}

		if ( debug ) 
			System.out.println( "    ALDProcessingDAG::visitOpNodeDatadependency returning " + inputPorts);
		return inputPorts;
	}

	/** Completely visits this opNode. Add the opNode to the processing DAG as well as the directly
	 * included dataports. All edges within this opNode are also added if not connected to
	 * a hidden opnode.
	 * Recursively visit all non hidden  children of this opNode.
	 * We assume that this method is never called for a hidden opNode.
	 *
	 * @return All input ports a the history for a complete node depends on all inputs.
	 */
	private Vector<ALDInputPort> visitOpNodeComplete( ALDOpNode opNode, int depth) 
		throws ALDProcessingDAGException {
		if ( isHiddenOpnode( opNode) ) fatal( "visitOpNodeComplete opNode " + opNode.getName() + " is hidden");
		
		if ( debug ) 
			System.out.println( "    ALDProcessingDAG::visitOpNodeComplete visit " + opNode.getName());

		if ( ! allOpNodes.contains( opNode) ) {
			allOpNodes.add( opNode);
			opNode.setDepth( depth);

			// add edges connecting to the output ports of this opNode
			// and register data which are the source of an edge
			ALDOutputPort[] outputPorts = opNode.getOutputPorts();
			for ( int i=0 ; i < outputPorts.length ; i++ ) {
				handlePort( opNode, outputPorts[i]);
			}

			Iterator<ALDOpNode> oItr = opNode.getDirectlyRegisteredChildern().iterator();
			while ( oItr.hasNext() ) {
				ALDOpNode childOpNode = oItr.next();

				// visit non hidden children
				if ( ! allOpNodes.contains( childOpNode) && ! isHiddenOpnode( childOpNode)) {
					visitOpNodeComplete( childOpNode, depth+1);
				}

				// it might be that a child was visited before its parent opNode
				// and we still have connect edges and regsiter data parts
				if ( ! isHiddenOpnode( childOpNode)) {
					// add edges connecting to the input ports of this child
					// and register data which are the source of an edge
					ALDInputPort[] inputPorts = childOpNode.getInputPorts();
					for ( int i=0 ; i < inputPorts.length ; i++ )
						handlePort( opNode, inputPorts[i]);
				}
			}
		}

		// return all input ports
		Vector<ALDInputPort> inputPorts = new Vector<ALDInputPort>();
		ALDInputPort[] inputPortsArray = opNode.getInputPorts();
		for ( int i=0 ; i < inputPortsArray.length ; i++ ) {
			inputPorts.add( inputPortsArray[i]);
		}
		 
		return inputPorts;
	}

	/** Handle a port to register the incoming edge and to potentially register dataport to opNode.
	 * Port is either an OutputPort of opNode or an InputPort of a child of opNode.
	 * If it is of type DataPort, than a dataport of opNoe is found an registered.
	 * A edge to an output port of a child opnode is added only, if this child i snot hidden.
	 */
	private void handlePort( ALDOpNode opNode, ALDPort port ) 
		throws ALDProcessingDAGException {
		ALDPort origin = port.getOrigin();
		if ( origin != null ) {
			if ( origin instanceof ALDInputPort ) {
				// TODO potential check: origin.getOpNode == opNode
				if ( ! isHiddenOpnode( ((ALDInputPort)origin).getOpNode()) )
					allEdges.add( new ALDEdge( origin, port));
			} else if ( origin instanceof ALDOutputPort ) {
				ALDOpNode childOpNode = ((ALDOutputPort)origin).getOpNode();
				// TODO potential check: if childOpNode has already a depth, childOpNode.getDepth = depth+1
				if ( ! isHiddenOpnode( childOpNode) )
					allEdges.add( new ALDEdge( origin, port));
			} else if ( origin instanceof ALDDataPort ) {
				// TODO potential check: dataport should be discovered only once
				if ( ! allDataports.contains( origin) ) {
					register( opNode, (ALDDataPort)origin);
					allDataports.add( (ALDDataPort)origin);
				}
				allEdges.add( new ALDEdge( origin, port));
			} else { 
				fatal( "handlePort origin " + origin + " is of unknown type");
			}
		}
	}

	/** Is this an opNode of the toplevel operator.
	 */
	private boolean isToplevelOpnode( ALDOpNode opNode) {
		return  opNode.getName().equals( "ALDToplevelOperator");
	}

	/** Is a complete history to be created for this opNode?
	 * @return true if global history type/mode is COMPLETE, or
	 *       global history type/mode is OPNODETYPE and the opNode is complete.
	 *		Otherwise false.
	 * 	The only exceptions are opnodes of the ALDToplevelOperator as we have to allow
	 *  to find the way to operators in other threads.
	 */
	private boolean isCompleteOpnode( ALDOpNode opNode) {
		if ( isToplevelOpnode( opNode) )
			return false;
		else 
			return (this.historyType == HistoryType.COMPLETE) ||
				(this.historyType == HistoryType.OPNODETYPE && opNode.completeDAG);
	}

	/** Should we hide this opnode.
	 * @return true if opNode is hidden and global ignore of hiding is false.
	 */
	private boolean isHiddenOpnode( ALDOpNode opNode) {
		return (! ignoreHiding) && 
				( opNode.getHidingMode() == HidingMode.HIDDEN ) ;
	}

	/** Register an opNode as child to its parents if not already found directly or
	 * indirectly before.
	 * This is a child found through connections between ports and not during execution
	 * of operators.
	 */
	private void register( ALDOpNode parentOpNode, ALDOpNode opNode) {
		if ( opNode.getParent() == null ) {
			opNode.setParent( parentOpNode);
			parentOpNode.addChild( opNode);
		} else {
			if ( opNode.getParent() != parentOpNode )
				System.out.println( "ALDProcessingDAG::register register parent for " + opNode + " again with different parent");
		}
	}
	

    /** Register the dataport as directly included in opNode.
	 */
	private void register( ALDOpNode opNode, ALDDataPort dataport ) {
		if ( ! opNode.getIncludedData().contains( dataport ) )
			opNode.addData( dataport);
	}

	// ===================== convert to graphml structure

	/** Create the processing history for <code>sourceObjOfHistory</code> and convert to graphml object.
	 * The complete history according is created. This method is
	 * equivalent to <code>createGraphmlDocument( sourceObjOfHistory, HistoryType.COMPLETE, false)</code>.
	 *
	 * @param	sourceObjOfHistory	for which the processing history to be created.
	 * @return Processing history as graphml object or null if no history is available.
	 */
	public GraphmlDocument createGraphmlDocument( Object sourceObjOfHistory ) 
		throws ALDProcessingDAGException {

		return createGraphmlDocument( sourceObjOfHistory, HistoryType.COMPLETE, false);
	}

	/** Create the processing history for <code>sourceObjOfHistory</code> and convert to graphml object.
	 * This method is equivalent to <code>createGraphmlDocument( sourceObjOfHistory, historyType, false)</code>.
	 *
	 * @param	sourceObjOfHistory	for which the processing history to be written.
	 * @param	historyType	type/moude of history to be created.
	 * @return Processing history as graphml object or null if no history is available.
	 */
	public GraphmlDocument createGraphmlDocument( Object sourceObjOfHistory, HistoryType historyType ) 
		throws ALDProcessingDAGException {

		return createGraphmlDocument( sourceObjOfHistory, historyType, false);
	}

	/** Create the processing history for <code>sourceObjOfHistory</code> and convert to graphml object.
	 *
	 * @param	sourceObjOfHistory	for which the processing history to be written.
	 * @param	historyType	type/mode of history to be created.
	 * @param	ignoreHiding	if true hiding of opnodes is ignores, i.e. all opnodes
	 *					added to the history.
	 * @return Processing history as graphml object or null if no history is available.
	 */
	public GraphmlDocument createGraphmlDocument( Object sourceObjOfHistory, HistoryType historyType,
			boolean ignoreHiding) 
		throws ALDProcessingDAGException {

		if ( debug ) 
			System.out.println( "ALDProcessingDAG::createGraphmlDocument START for " + sourceObjOfHistory);

		this.ignoreHiding = ignoreHiding;
		this.historyType = historyType;
		tracebackDAG( sourceObjOfHistory);

		// this is the overall graphml document
		GraphmlDocument graphmlDoc = GraphmlDocument.Factory.newInstance();

		// setup the graphml element
		GraphmlType graphMl = graphmlDoc.addNewGraphml();
		graphMl.setDesc( "Alida Processing History");

		// and the keys
		addKey( graphMl, "aldName", KeyForType.NODE, "aldName", KeyTypeType.STRING);
		addKey( graphMl, "aldNodeType", KeyForType.NODE, "aldNodeType", KeyTypeType.STRING);
		addKey( graphMl, "aldExplanation", KeyForType.NODE, "aldExplanation", KeyTypeType.STRING);
		addKey( graphMl, "aldClassname", KeyForType.NODE, "aldClassname", KeyTypeType.STRING);
		addKey( graphMl, "aldProps", KeyForType.NODE, "aldProperties", KeyTypeType.STRING);
		addKey( graphMl, "aldParas", KeyForType.NODE, "aldParameteres", KeyTypeType.STRING);
		addKey( graphMl, "aldParasXML", KeyForType.NODE, "aldParameteresAsXml", KeyTypeType.STRING);
		addKey( graphMl, "aldOutput", KeyForType.GRAPH, "aldOutput", KeyTypeType.STRING);
		addKey( graphMl, "aldXml", KeyForType.GRAPH, "aldXmlHistory", null);
		addKey( graphMl, "aldEdgeType", KeyForType.EDGE, "aldEdgeType", KeyTypeType.STRING);

		// top level graph, this is the actual graph containing the history
		GraphType graph = graphMl.addNewGraph();
		graph.setEdgedefault( GraphEdgedefaultType.DIRECTED);

		// add result object for which this processing history was constrcted as attribute  to the
		// top level graph
		addAttrToGraph( graph, "aldResultObject", getPortId( ALDOperator.portHashAccess.getHistoryLink(sourceObjOfHistory)));

		// determine the minimal depth of all opNodes found
		int minDepth = 1;
		for ( int i=0 ; i < allOpNodes.size() ; i++ ) {
			if ( allOpNodes.elementAt(i).getDepth() < minDepth ) 
				minDepth = allOpNodes.elementAt(i).getDepth();
		}

		// add all opNodes at minDepth and recursively their children
		opNodesInGraph = new Vector<ALDOpNode>();
		while ( allOpNodes.size() > 0 ) {
			boolean found = false;
			for ( int i=0 ; i < allOpNodes.size() ; i++ ) {
				if ( allOpNodes.elementAt(i).getDepth() == minDepth ) {
					outputOpNode( allOpNodes.elementAt(i), graph);
					found = true;
					// we restart, as we have modified opNodes during outputOpNode
					break;
				}
			}

			// we did not find any node at minDepth
			if ( ! found ) break;
		}

		if ( allOpNodes.size() > 0 ) {
			System.err.println( "ALDProcessingDAG::createGraphmlDocument WARNING: leftover orphan opNodes");
			for ( int i=0 ; i < allOpNodes.size() ; i++ ) {
				allOpNodes.elementAt(i).print();
			}
		}

		// ALDdata which have not already been written
		Iterator<ALDDataPort> dItr = allDataports.iterator();
		while ( dItr.hasNext() ) {
			ALDDataPort d = dItr.next();
			if ( ! writtenDataports.contains( d) ) {
				if ( debug ) 
					System.out.println( "ALDProcessingDAG::createGraphmlDocument remaining ALDData " + d);
				addDataPortToGraphml( graph, d);
				writtenDataports.add( d);
			}
		}

		// edges
		int idx = 0;
		Iterator<ALDEdge> eItr = allEdges.iterator();
		while ( eItr.hasNext() ) {
			ALDEdge e = eItr.next();
			int n = allEdges.indexOf( e);
			
			addEdge( graph, e.getSourcePort(), e.getTargetPort(), idx);
			idx++;
		}

		return graphmlDoc;
	}

	// ===================== Helper methods for Write DAG

	/** Output <code>opNode</code> and it ports, and recursively its children to the <code>graph</code>.
	 * An opNode is represented by an node and contains a (graphml) graph as 
	 * is containts nested ports and further opNodes.
	 */
	private void outputOpNode( ALDOpNode opNode, GraphType graph) 
		throws ALDProcessingDAGException {

		if ( debug ) 
			System.out.println( "ALDProcessingDAG::outputOpNode start output for " + opNode.getName() + 
								"<" + opNode + ">");

		allOpNodes.remove( opNode);
		opNodesInGraph.add( opNode);

		// graph of this opNode which contains its ports and child opNodes
        GraphType nestedG;

		if (  isToplevelOpnode( opNode) ) {
			// toplevel opnodes are not to incuded into the preocssing history,
			// as they are dummies
			nestedG = graph;
		} else {
			NodeType node = addOpNodeToGraph( graph, opNode);
        	nestedG = node.addNewGraph();
        	nestedG.setEdgedefault( GraphEdgedefaultType.DIRECTED);
		
			// add input ports to the nested graph of opNode
			ALDInputPort[] inputPorts = opNode.getInputPorts();
			for ( int p=0 ; p < inputPorts.length ; p++ ) {
				// port as graphml nodes
				addOpNodePortToGraphml( nestedG, inputPorts[p]);
			}

			// add output ports to the nested graph of opNode
			ALDOutputPort[] outputPorts = opNode.getOutputPorts();
			for ( int p=0 ; p < outputPorts.length ; p++ ) {
				// port as graphml nodes
				addOpNodePortToGraphml( nestedG, outputPorts[p]);
			}	
		}	

		// add children found during traceback to the nested graph of opNode
		//##Vector<ALDOpNode> children = opNode.getChildren();
		Vector<ALDOpNode> children = opNode.getDirectlyRegisteredChildern();
		Iterator<ALDOpNode> oItr = children.iterator();
		while (oItr.hasNext()) {
			ALDOpNode child = oItr.next();
			if ( allOpNodes.contains( child) ) {
				allOpNodes.remove( child);
				outputOpNode( child, nestedG);
			}
		}

		// included data
		Iterator<ALDDataPort> dItr = opNode.getIncludedData().iterator();
		while ( dItr.hasNext() ) {
			ALDDataPort d = dItr.next();

			addDataPortToGraphml( nestedG, d);

			// delete from allDataports, to avoid multiple graph nodes for this ALDData
			writtenDataports.add( d);
		}
	}

	/** Add an graphml edge to <code>graph</code> with index <code>idx</code>
	 */
	private void addEdge( GraphType graph, ALDPort sourcePort, ALDPort targetPort, int idx) 
		throws ALDProcessingDAGException {

		// check if source and tragets are in the graph
		if ( sourcePort instanceof ALDDataPort && ! writtenDataports.contains( (ALDDataPort)sourcePort) ) {
			inconsistent( "addEdge sourcePort is of type ALDDataPort but not in allDataports: ");
			sourcePort.print( "    ");
			return;
		}
		if ( sourcePort instanceof ALDOpNodePort && ! opNodesInGraph.contains( ((ALDOpNodePort)sourcePort).getOpNode()) ){
			inconsistent( "addEdge sourcePort is of type ALDOpNodePort but its opNode not in opNodesInGraph");
			sourcePort.print( "    ");
			return;
		}
		if ( ! opNodesInGraph.contains( ((ALDOpNodePort)targetPort).getOpNode()) ) {
			inconsistent( "addEdge targetPort is of type ALDOpNodePort but its opNode not in opNodesInGraph for: ");
			targetPort.print( "    ");
			return;
		}

		EdgeType edge = graph.addNewEdge();
		edge.setId( getGraphName(0) + idSeparator + "Edge" + idx);

		// ports as graphml ports
		edge.setSource( getPortId( sourcePort));
		edge.setTarget( getPortId( targetPort));

		//addAttrToEdge( edge, "arrow", "Target");
		edge.setDirected( true);
	}

	// =========================== histories ==========================

	/** Add the complete history of an object represented by a data port,
	 *  which was read from a graphml file, to a graph object.
	 *  Add all nodes and edges (besides the toplevel graph) from the
	 *  history to the graph object at the toplevel of the graph and
	 *  connect the output port to the node representing the data port.
	 *  Rename IDs in the history which is connected.
	 *
	 * @param	graph Graph to add the history read from file 
	 * @param	dataport 
	 */
	private void addGraphmlHistory( GraphType graph, ALDDataPort dataport) 
		throws ALDProcessingDAGException {
		if ( debug ) 
			System.out.println( "ALDProcessingDAG::addGraphmlHistory");

		if ( dataport.getGraphmlHistory() == null ) return;
		
		GraphmlType history = dataport.getGraphmlHistory();

		GraphType hGraph = GraphmlHelper.getToplevelGraph( history);
		if ( hGraph == null ) return;

		// rename Ids in history graphml
		graphIndex += GraphmlHelper.renameGraphIds( hGraph, graphIndex+1);

		int oldNumNodes = graph.sizeOfNodeArray();
		for ( int n = 0 ; n < hGraph.sizeOfNodeArray() ; n++ ) {
			NodeType node = hGraph.getNodeArray( n);
			if ( debug ) 
				System.out.println( "ALDProcessingDAG::addGraphmlHistory add Node ");// + node);
			graph.insertNewNode( oldNumNodes+n);
			graph.setNodeArray( oldNumNodes+n, node);
		}

		int oldNumEdges = graph.sizeOfEdgeArray();
		for ( int e = 0 ; e < hGraph.sizeOfEdgeArray() ; e++ ) {
			EdgeType edge = hGraph.getEdgeArray( e);
			if ( debug ) 
				System.out.println( "ALDProcessingDAG::addGraphmlHistory add Edge ");// + edge);
			graph.insertNewEdge( oldNumEdges+e);
			graph.setEdgeArray( oldNumEdges+e, edge);
		}

		int oldNumDatas = graph.sizeOfDataArray();
		for ( int d = 0 ; d < hGraph.sizeOfDataArray() ; d++ ) {
			DataType gdata = hGraph.getDataArray( d);

			// add extra edge to connect graphs
			if ( gdata.getKey().equals("aldResultObject") ||
			     gdata.getKey().equals("mtbOutput") ) {
				EdgeType edge = graph.addNewEdge();
				edge.setSource( gdata.newCursor().getTextValue());
				edge.setTarget( getPortId( dataport));
				edge.setDirected(true);
				addAttrToEdge( edge, "aldEdgeType", "connectHistories");
				
				if ( debug ) 
					System.out.println( "ALDProcessingDAG::addGraphmlHistory add Edge for Output " + 
						gdata.newCursor().getTextValue() + " --> " + getPortId( dataport));
			}
		}
	}

	/** Add the complete history of a ALDData from a generic XML file to a graphml node.
	 *  The complete xml tree is put in place without any processing.
	 */
	private void addXmlHistory( NodeType node, XmlObject xmlHistory) {
		
		if ( debug ) 
			System.out.println( "ALDProcessingDAG::addXmlHistory");

		XmlCursor hCursor = xmlHistory.newCursor();
		hCursor.toStartDoc();
		hCursor.toNextToken();


		DataType data = node.addNewData();
        data.setKey( "aldXml");
		XmlCursor dCursor = data.addNewXmlHistory().newCursor();
		dCursor.toEndToken();
		hCursor.moveXml( dCursor);

	}

	/** Add the parameter hash to to a graphml node.
	 */
	private void addParameterHash( NodeType node, ALDOpNode opNode) {
		
		if ( debug ) 
			System.out.println( "ALDProcessingDAG::addParameterHash");

		XmlCursor hCursor = opNode.getParameterHashAsXml().newCursor();
		hCursor.toStartDoc();
		hCursor.toNextToken();


		DataType data = node.addNewData();
        data.setKey( "aldParasXML");
		XmlCursor dCursor = data.addNewParameterWrapper().newCursor();
		dCursor.toEndToken();
		hCursor.copyXml( dCursor);

	}

	//  ======================== graphml Helper ================================
	/** add a key definition to graphml
	 */
	private void addKey( GraphmlType graphml, String id, KeyForType.Enum domain, String name, KeyTypeType.Enum keyType) {
		KeyType key = graphml.addNewKey();
		key.setId( id);
		key.setFor( domain);
		key.setAttrName( name);
		key.setAttrType( keyType);
	}

	/** Add an opNode to the graph of the enclosing opNode.
	 * Add a node to the graph, set its attributes and properties.
	 * Add also also the parameters of the opNode with its values.
	 */
	private NodeType addOpNodeToGraph( GraphType graph, ALDOpNode opNode) {
		NodeType node = graph.addNewNode();
		node.setId( getOpNodeId( opNode));

		addAttrToNode( node, "aldName", opNode.getName());
		addAttrToNode( node, "aldNodeType", "OpNode");

		// parameteres
		DataType data = node.addNewData();
		data.setKey( "aldParas");
		PropertyListType props = data.addNewProperties();

		// commented for the moment, as it did crash
		//addParameterHash( node, opNode);

		Enumeration<String> keys = opNode.getParameterKeys();
		while ( keys.hasMoreElements() ) {
			String key = keys.nextElement();
			String value = opNode.getParameter( key);

			PropertyType prop = props.addNewProperty();
			prop.setKey( key);
			prop.setValue( value);
		}

		// properties
		data = node.addNewData();
		data.setKey( "aldProps");
		props = data.addNewProperties();

		PropertyType prop = props.addNewProperty();
		prop.setKey( "classname"); 
		prop.setValue( opNode.getOperatorClass().getSimpleName());

		if ( opNode.getOperatorClass().getPackage() != null ) {
			prop = props.addNewProperty();
			prop.setKey( "package"); 
			prop.setValue( opNode.getOperatorClass().getPackage().getName());
		}

		prop = props.addNewProperty();
		prop.setKey( "version"); 
		prop.setValue( opNode.getVersion());

		return node;
	}

	/** Add a data port to the graphML graph
	 */

	private NodeType addDataPortToGraphml( GraphType graph, ALDDataPort dataport) 
		throws ALDProcessingDAGException {
		NodeType node = graph.addNewNode();
		node.setId( getDataPortId( dataport));

		String loc = dataport.getLocation();
		if ( loc  != null ) {
			addAttrToNode( node, "aldName", ALDFilePathManipulator.removeLeadingDirectories( loc));
		}

		if ( dataport.getGraphmlHistory() != null ||
			 loc != null ) {
			addAttrToNode( node, "aldNodeType", "DataPortFromFile");
			addGraphmlHistory( graph, dataport);
		} else {
			addAttrToNode( node, "aldName", "");
			addAttrToNode( node, "aldNodeType", "DataPort");
		}

		DataType data = node.addNewData();
		data.setKey( "aldProps");
		PropertyListType props = data.addNewProperties();
		Enumeration<String> keys = dataport.getPropertyKeys();
		while ( keys.hasMoreElements() ) {
			String key = keys.nextElement();
			String value = dataport.getProperty( key);
	
			PropertyType prop = props.addNewProperty();
			prop.setKey( key);
			prop.setValue( value);
		}

		return node;
	}

	/** Add a opNode port object to the graphML graph
	 */

	private NodeType addOpNodePortToGraphml( GraphType graph, ALDOpNodePort port) 
		throws ALDProcessingDAGException {
		NodeType node = graph.addNewNode();

		node.setId( getPortId( port));

		ALDOpNode opNode = port.getOpNode();
		String descriptorName = port.getDescriptorName();

		try {
			if ( port instanceof ALDOutputPort ) {
				addAttrToNode( node, "aldName", descriptorName);
				addAttrToNode( node, "aldNodeType", "OutputPort");
				addAttrToNode( node, "aldExplanation", ((ALDOpNodePort)port).getExplanation());
				addAttrToNode( node, "aldClassname", ((ALDOpNodePort)port).getClassname());
	
				ALDOutputPort outputPort = (ALDOutputPort)port;
				// properties
				DataType data = node.addNewData();
				data.setKey( "aldProps");
				PropertyListType props = data.addNewProperties();
	
				Enumeration<String> keys = outputPort.getPropertyKeys();
				while ( keys.hasMoreElements() ) {
					String key = keys.nextElement();
					String value = outputPort.getProperty( key);
	
					PropertyType prop = props.addNewProperty();
					prop.setKey( key);
					prop.setValue( value);
				}
			} else { // InputPort
				addAttrToNode( node, "aldName", descriptorName);
				addAttrToNode( node, "aldNodeType", "InputPort");
				addAttrToNode( node, "aldExplanation", ((ALDOpNodePort)port).getExplanation());
				addAttrToNode( node, "aldClassname", ((ALDOpNodePort)port).getClassname());
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

		return node;
	}

	// ======= data attributes ===========================
	/** Add an attribute to a graphml element
	 */
	private void addAttrToGraphml( GraphmlType graphml, String key, String value) {
		DataType data = graphml.addNewData();

		data.setKey( key);
		data.set( XmlString.Factory.newValue( value));
	}

	/** Add an attribute to a graphml graph
	 */
	private void addAttrToGraph( GraphType graph, String key, String value) {
		DataType data = graph.addNewData();

		data.setKey( key);
		data.set( XmlString.Factory.newValue( value));
	}

	/** Add an attribute to a graphml node
	 */
	private void addAttrToNode( NodeType node, String key, String value) {
		DataType data = node.addNewData();

		data.setKey( key);
		data.set( XmlString.Factory.newValue( value));
	}

	/** Add an attribute to a graphml edge
	 */
	private void addAttrToEdge( EdgeType edge, String key, String value) {
		DataType data = edge.addNewData();

		data.setKey( key);
		data.set( XmlString.Factory.newValue( value));
	}

	/** Add an attribute to a graphml port
	 */
	private void addAttrToPort( PortType port, String key, String value) {
		DataType data = port.addNewData();

		data.setKey( key);
		data.set( XmlString.Factory.newValue( value));
	}

	//  ======================== IDs ================================
	/** Remove the graphIP including idSeparator at the beginning of an id 
	 */
	static String removeGraphId( String id) {
		return id.substring( id.indexOf(idSeparator) +1);
	}

	/** Get the graphIP at the beginning of an id up (exculing) idSeparator
	 */
	static String getGraphId( String id) {
		return id.substring( 0, id.indexOf(idSeparator));
	}

	/** Return an ID used in graphml for graph
	 */
	static String getGraphName( int idx) {
		return new String( "ALDDAG" + idx);
	}

	/** Return an ID used in graphml an opNode
	 */
	private String getOpNodeId( ALDOpNode opNode) {
		return getGraphName( 0) + idSeparator + "OpNode" + opNodesClone.indexOf( opNode);
	}

	/** Return an ID used in graphml for a data port
	 */
	private String getDataPortId( ALDDataPort dataport) {
		return getGraphName( 0) + idSeparator + "DataPort" + allDataports.indexOf( dataport);
	}

	/** Return an ID used in graphml for a port
	 */
	private String getPortId( ALDPort p) throws ALDProcessingDAGException {
		if ( p instanceof ALDInputPort ) {
			ALDOpNodePort op = (ALDOpNodePort)p;
			return getGraphName( 0) + idSeparator + "InputPort" + op.getPortIndex() + "-of-" + 
					removeGraphId( getOpNodeId( op.getOpNode()));
		} else if ( p instanceof ALDOutputPort ) {
			ALDOpNodePort op = (ALDOpNodePort)p;
			return getGraphName( 0) + idSeparator + "OutputPort" + op.getPortIndex() + "-of-" + 
					removeGraphId( getOpNodeId( op.getOpNode()));
		} else if ( p instanceof ALDDataPort ) {
			ALDDataPort dp = (ALDDataPort)p;
			return getDataPortId( dp);
		} else {
			System.err.println( "ALDProcessingDAG::getPortId PANIC unkwon Porttype");
			throw new ALDProcessingDAGException(ALDProcessingDAGException.DAGExceptionType.INTERNAL_TRACING_ERROR,null);
		}

	}

	/** Output fatal error to stderr and throw an exception
	 */
	private void fatal( String msg) throws ALDProcessingDAGException {
		System.err.println( "FATAL ERROR in ALDProcessingDAG::" + msg);
		throw new ALDProcessingDAGException(ALDProcessingDAGException.DAGExceptionType.INTERNAL_TRACING_ERROR,null);
	}

	/** Output error to stderr 
	 */
	private void inconsistent( String msg) {
		System.err.println( "ERROR in ALDProcessingDAG::" + msg);
	}
}
