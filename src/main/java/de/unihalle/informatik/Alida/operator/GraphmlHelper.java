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

package de.unihalle.informatik.Alida.operator;
 

import java.io.File;
import java.util.*;
 
import org.graphdrawing.graphml.xmlns.*;
import org.apache.xmlbeans.XmlString;
 
/** This class supplies some static auxiliary methods used when explicitly constructing
 * a history graph.
 */
public class GraphmlHelper {
 
	/** turns debugging on
	 */
	private static boolean debug = false;

	/** Print a <code>graphml</code> element to standard out
	 *
	 * @param graphml graphml object to be print
	 */
    static public void printGraphml( GraphmlType graphml) {

		System.out.println( "desc " + graphml.getDesc());
        
		for ( int k = 0 ; k < graphml.sizeOfKeyArray() ; k++ ) {
			KeyType key = graphml.getKeyArray( k);
			System.out.println( "   " + key.getId());
		}

		GraphType topG = getToplevelGraph( graphml);
		printGraph( topG, "");
    }
    
	/** Print a <code>graph</code> to standard out using an indentation string
	 * 
	 * @param graph graph to be print
	 * @param indent indentation string
	 */
	static public void printGraph( GraphType graph , String indent) {
		if ( debug ) 
			System.out.println( indent + "printGraph " + graph.getId());

		for ( int n = 0 ; n < graph.sizeOfNodeArray() ; n++ ) {
			printNode( graph.getNodeArray( n), indent + "  ");
		}

		for ( int e = 0 ; e < graph.sizeOfEdgeArray() ; e++ ) {
			printEdge( graph.getEdgeArray( e), indent + "  ");
		}

		for ( int d = 0 ; d < graph.sizeOfDataArray() ; d++ ) {
			printData( graph.getDataArray( d), indent + "  ");
		}
	}

	/** Print a <code>Node</code> to standard out using an indentation string
	 * 
	 * @param node node to be print
	 * @param indent indentation string
	 */
	static public void printNode( NodeType node, String indent) {
		System.out.println( indent + "printNode " + node.getId());

		for ( int p = 0 ; p < node.sizeOfPortArray() ; p++ ) {
			printPort( node.getPortArray( p), indent + "  ");
		}
		
		for ( int d = 0 ; d < node.sizeOfDataArray() ; d++ ) {
			printData( node.getDataArray( d), indent + "  ");
		}
		
		GraphType graph = node.getGraph();
		if ( graph != null ) 
			printGraph( graph, indent + "  ");
	}
		
	/** Print an <code>Edge</code> to standard out using an indentation string
	 * 
	 * @param edge edge to be print
	 * @param indent indentation string
	 */
	static public void printEdge( EdgeType edge, String indent) {
		System.out.println( indent + "printEdge " + edge.getId() + ": " +
			edge.getSource() + " --> " + edge.getTarget());
	}

	/** Print a <code>Data</code> item to standard out using an indentation string
	 * 
	 * @param data data to be print
	 * @param indent indentation string
	 */
	static void printData( DataType data, String indent) {
		System.out.println( indent + "printData " + data.getId() + " key " + data.getKey());
	}

	/** Print a <code>Port</code> to standard out using an indentation string
	 * 
	 * @param port port to be print
	 * @param indent indentation string
	 */
	static void printPort( PortType port, String indent) {
		System.out.println( indent + "printPort " + port.getName());
	}

    /** Rename all GraphIds of nodes, edges, data, etc with a new graphID.
		The graphId is the part of the id up (excluding) the first ALDProcessingDAG.idSeparator.
	    The new graphIDs start with nextGraphIndex an are constructed via ALDProcessingDAG.getGraphName()
	 * 
	 * @param graph graph to rename
	 * @param nextGraphIndex index to use
     */
	static public int renameGraphIds( GraphType graph, int nextGraphIndex) {
		HashMap<String,String> translationTable = new HashMap<String,String>();
		findIds( graph, translationTable, nextGraphIndex);
		renameIds( graph, translationTable);

		return translationTable.size();
	}

	/** Traverse the graph, find all graphIDs and insert into the translationTable.
	 * 
	 * @param graph graph to traverse
	 * @param translationTable
	 * @param nextGraphIndex index to use
	 */
	static private void findIds( GraphType graph, HashMap<String,String> translationTable, int nextGraphIndex) {
		if ( graph.getId() != null ) 
			insertGraphId( graph.getId(), translationTable, nextGraphIndex);

		for ( int n = 0 ; n < graph.sizeOfNodeArray() ; n++ ) {
			findNodeIds( graph.getNodeArray( n), translationTable, nextGraphIndex);
		}

		for ( int e = 0 ; e < graph.sizeOfEdgeArray() ; e++ ) {
			findEdgeIds( graph.getEdgeArray( e), translationTable, nextGraphIndex);
		}

		for ( int d = 0 ; d < graph.sizeOfDataArray() ; d++ ) {
			findDataIds( graph.getDataArray( d), translationTable, nextGraphIndex);
		}
	}

	/** Insert nodeId and associated attributes into the translationTable.
	 */
    static public void findNodeIds( NodeType node, HashMap<String,String> translationTable, int nextGraphIndex) {
        if ( node.getId() != null ) 
			insertGraphId( node.getId(), translationTable, nextGraphIndex);

        // find data attriputes
        for ( int n = 0 ; n < node.sizeOfDataArray() ; n++ ) {
            DataType data = node.getDataArray( n);
            if ( data.getKey().equals( "aldResultObject") || 
                 data.getKey().equals( "mtbOutput") ) { //|| 
                                
                String value = data.newCursor().getTextValue();
                if ( value != null )  {
					insertGraphId( value, translationTable, nextGraphIndex);
        
                    if ( debug ) 
                        System.out.println( "GraphmlHelper::findNodeIds  " + value);
                }
            }
        }

        GraphType g = node.getGraph();
        if ( g != null ) 
            findIds( g, translationTable, nextGraphIndex);
    }

	/** Insert edgeId and associated nodes into the translationTable.
	 */
	static void findEdgeIds( EdgeType e, HashMap<String,String> translationTable, int nextGraphIndex) {
        if ( debug ) {
            System.out.println( "GraphmlHelper::findEdgeIds " + e.getId());
		}
		if ( e.getId() != null ) 
			insertGraphId( e.getId(), translationTable, nextGraphIndex);

		if ( e.getSource() != null )
			insertGraphId( e.getSource(), translationTable, nextGraphIndex);

		if ( e.getTarget() != null )
			insertGraphId( e.getTarget(), translationTable, nextGraphIndex);
	}

	static void findDataIds( DataType data, HashMap<String,String> translationTable, int nextGraphIndex) {
        if ( debug ) {
            System.out.println( "GraphmlHelper::findDataIds " + data.getId());
		}
		if ( data.getId() != null ) 
			insertGraphId( data.getId(), translationTable, nextGraphIndex);

		// mtbOutput: rename of output port of top level graph
		if ( data.getKey().equals( "aldResultObject") || 
		     data.getKey().equals( "mtbOutput") ) { 
			String value = data.newCursor().getTextValue();
			if ( value != null )  {
				insertGraphId( value, translationTable, nextGraphIndex);
			}
		}
	}

	/** Traverse the graph, find all graphIDs and do the renaming
	 */
	static private void renameIds( GraphType graph, HashMap<String,String> translationTable) {
		if ( graph.getId() != null ) 
			graph.setId( translationTable.get( graph.getId()));

		for ( int n = 0 ; n < graph.sizeOfNodeArray() ; n++ ) {
			renameNodeIds( graph.getNodeArray( n), translationTable);
		}

		for ( int e = 0 ; e < graph.sizeOfEdgeArray() ; e++ ) {
			renameEdgeIds( graph.getEdgeArray( e), translationTable);
		}

		for ( int d = 0 ; d < graph.sizeOfDataArray() ; d++ ) {
			renameDataIds( graph.getDataArray( d), translationTable);
		}
	}

	/** Insert id into if not already included.
	 */
	static private void insertGraphId( String id, HashMap<String,String> translationTable, int nextGraphIndex) {
		String graphID = ALDProcessingDAG.getGraphId( id);
		if ( ! translationTable.containsKey( graphID) ) {
			translationTable.put( graphID, ALDProcessingDAG.getGraphName( nextGraphIndex+translationTable.size()));
		}
	}

	/** Traverse the graph, find all graphIDs and actually rename.
	 */
	static public void renameNodeIds( NodeType node, HashMap<String,String> translationTable) {
        if ( debug ) {
            System.out.println( "GraphmlHelper::renameNodeIds " + node.getId());
		}

		if ( node.getId() != null ) 
			node.setId( translate( translationTable, node.getId()));

		// rename data attriputes
		for ( int n = 0 ; n < node.sizeOfDataArray() ; n++ ) {
			DataType data = node.getDataArray( n);
			if ( data.getKey().equals( "aldResultObject") || 
			     data.getKey().equals( "mtbOutput") ) { 
				
				String value = data.newCursor().getTextValue();
				if ( value != null )  {
					data.set( XmlString.Factory.newValue( 
					    translate( translationTable, value)));
	
					if ( debug ) 
						System.out.println( "GraphmlHelper::renameDataIds: " + value);
				}
			}
		}

		GraphType g = node.getGraph();
		if ( g != null ) {
			if ( debug ) 
				System.out.println( "GraphmlHelper::renameNodeIds call renameGraphIds recursively");
			renameIds( g, translationTable);
		}
	}
		
	static void renameEdgeIds( EdgeType e, HashMap<String,String> translationTable) {
        if ( debug ) {
            System.out.println( "GraphmlHelper::renameEdgeIds " + e.getId());
		}
		if ( e.getId() != null ) 
			e.setId( translate( translationTable, e.getId()));

		if ( e.getSource() != null )
			e.setSource( translate( translationTable, e.getSource()));

		if ( e.getTarget() != null )
			e.setTarget( translate( translationTable, e.getTarget()));
	}

	static void renameDataIds( DataType data, HashMap<String,String> translationTable) {
        if ( debug ) {
            System.out.println( "GraphmlHelper::renameDataIds " + data.getId());
		}
		if ( data.getId() != null ) 
			data.setId( translate( translationTable, data.getId()));

		// mtbOutput: rename of output port of top level graph
		if ( data.getKey().equals( "aldResultObject") || 
		     data.getKey().equals( "mtbOutput") ) { 
			String value = data.newCursor().getTextValue();
			if ( value != null )  {
				data.set( XmlString.Factory.newValue( 
					   translate( translationTable, value)));
			}
		}
	}

	/** Translate the id using translationTable.
	 */
	static String translate( HashMap<String,String> translationTable, String id) {
		String graphId = ALDProcessingDAG.getGraphId( id);

		return translationTable.get( graphId) + ALDProcessingDAG.idSeparator +
				ALDProcessingDAG.removeGraphId( id);
	}

	/** Return the first element of type Graph found at the top level of the
	 *  graphml object
	 *
	 * @param	graphml	complete graphml object
	 * @return	first top level graph object or null if none found
	 */
	public static GraphType getToplevelGraph( GraphmlType graphml) {
		if ( graphml.sizeOfGraphArray() > 0 )
			return graphml.getGraphArray(0);
		else
			return null;
    }

}
