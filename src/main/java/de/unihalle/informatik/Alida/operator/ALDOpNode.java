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

import de.unihalle.informatik.Alida.dataio.ALDDataIOManagerCmdline;
import de.unihalle.informatik.Alida.dataio.provider.ALDDataIOCmdline;
import de.unihalle.informatik.Alida.operator.ALDOperator.HidingMode;

import java.util.*;
import java.io.*;
import org.apache.xmlbeans.*;

/** Each instance of this class represents an operator invocation for the
 * implicit processing graph.
 * It holds input and output ports used to link the objects according to the data flow,
 * as well as the parameter's values upon invocation in a parameter hash.
 */
public class ALDOpNode {
	// IMPORTANT NOTE: the reference to the operator object needs to be 
	// remove upon return of the operate method for the VM to be able to garbage
	// collect the operator object and its references
	ALDOperator op;

	/** the input ports of this opNode.
	 */
	private ALDInputPort[] inputPorts; 

	/** the output ports of this opNode.
	 */
	private ALDOutputPort[] outputPorts; 

	/** name of the operator for which an invocations is represented by this opNode
	 */
	final private String name;			
	
	/** version of the operator for which an invocations is represented by this opNode
	 */
	final private String version;

	/** class of the operator for which an invocations is represented by this opNode
	 */
	final private Class<? extends ALDOperator> operatorClass;

	/** Hiding mode of this opnode in the processing history.
	 */
	HidingMode hidingMode; 

	/** This opnode prefers a complete DAG to constructed for its invocation.
	 */
	final boolean completeDAG;

	/** This hash contains the values of all parameters as return by the <code>toString()</code> 
      * method of the parameters at the time of invocation of the operator using <code>runOp()</code>.
	  */
	private Hashtable<String, String> parameterHash;

	/** parameteHash as an XmlObject
	 */
	private XmlObject parameterHashAsXml; 

	/** parent of this opNode, will be set during back tracing
	 */
	private ALDOpNode parent; 

	/** children of this opNode, will be set during back tracing 
	 */
	private Vector<ALDOpNode> children; 

	/** children of this opNode, set when the <code> operate()</code>
     * represented by this <code>opNode</code> calls nested operators.
	 */
	private Vector<ALDOpNode> directlyRegisteredChildren; 

	/** depth in calling stack of Operators, set when back tracing 
	 */
	private int depth; 

	/** ALDDataPorts created within this opNode, set during back tracing 
	 */
	private Vector<ALDDataPort> includedData; 

	/** Construct an <code>ALDOpNode</code> for the operator <code>op</code> and <code>hidingMode</code> 
	 * 
	 * @param op  Operator to instatiate an opnode for
	 * @param hidingMode hiding mode within processing history
	 */
	public ALDOpNode( ALDOperator op, HidingMode hidingMode) {
		//Experimental
		if ( collectInstances ) {
        	allInstances.add( this );  
		}
		//END Experimental

		this.completeDAG = op.completeDAG;
		this.depth = Integer.MAX_VALUE;
		this.hidingMode = hidingMode;

		this.op = op;
		this.name = op.getName();
		this.operatorClass = op.getClass();
		this.version = op.getVersion();

		try { 
			Collection<String> inInoutNames = op.getInInoutNames();
			inputPorts = new ALDInputPort[ inInoutNames.size()];
			int i = -1;
			for ( String inputName : inInoutNames ) {
				i++;
				inputPorts[i] = new ALDInputPort( this, i, inputName);
			}
	
			Collection<String> outInoutNames = op.getOutInoutNames();
			outputPorts = new ALDOutputPort[ outInoutNames.size()];
			i = -1;
			for ( String outputName : outInoutNames ) {
				i++;
				outputPorts[i] = new ALDOutputPort( this, i, outputName);
			}
	
			parameterHash = new Hashtable<String, String>();
	
			children = new Vector<ALDOpNode>();
			directlyRegisteredChildren = new Vector<ALDOpNode>();
			includedData = new Vector<ALDDataPort>();

			// copy IN and INOUT parameters of the descriptor to a private hash
			// this is necessary, as the descriptor may be reused lateron with different
			// values of its parameters (or the values changes for another purpose)
			for ( String pName : op.getInInoutNames() ) {
				if ( op.getParameter( pName) != null ) {
					Object value = op.getParameter( pName);
					if ( value.getClass().isArray()) {        
						try {
							ALDDataIOCmdline provider = (ALDDataIOCmdline)ALDDataIOManagerCmdline.getInstance().getProvider(value.getClass(), ALDDataIOCmdline.class);
							String valueString = provider.writeData( value, "");

							parameterHash.put( op.getParameterDescriptor( pName).name, 
									valueString);
						} catch (Exception ex) {
							parameterHash.put( op.getParameterDescriptor( pName).name, 
									value.toString());
						}
					} else {
						parameterHash.put( op.getParameterDescriptor( pName).name, 
								value.toString());
					}
				}
			}
			//parameterHashAsXml = op.parametersToXmlObject();
		} catch (Exception e) {
			e.printStackTrace();
		} 
	}

	/** Get the name of the operator represented by this opNode.
	 */
	public String getName() {
		return name;
	}

	/** Get the software version.
	 */
	public String getVersion() {
		return version;
	}

	/** Get the class object of the operator represented by this opNode.
	 */
	public Class<? extends ALDOperator> getOperatorClass() {
		return operatorClass;
	}

	/** Set depth.
	 */
	void setDepth( int depth) {
		this.depth = depth;
	}

	/** Get depth.
	 */
	public int getDepth() {
		return depth;
	}

	/** Add a child found during back tracing.
	 * @param opNode child to add
	 */
	void addChild( ALDOpNode opNode) {
		children.add( opNode);
	}

	/** Add a directly registered child during invocation.
	 * @param opNode child to add
	 */
	void addDirectChild( ALDOpNode opNode) {
		directlyRegisteredChildren.add( opNode);
	}

	/** Add a data port found during back tracing.
	 */
	void addData( ALDDataPort data) {
		includedData.add( data);
	}

	/** Get input port with index i of this opNode.
	 */
	public ALDPort getInputPort( int i) {
		return inputPorts[i];
	}

	/** Get output port with index i of this opNode.
	 */
	public ALDOutputPort getOutputPort( int i) {
		return outputPorts[i];
	}

	/** Set the origin of the input port with index i.
      * This sets the origin of the associated input port as well
      * as its canoncial classname. The explanation is usually to
      * be copied from the argument descriptor of this input port.
      *
      * @param	i index of the input port
      * @param	obj object to assiciate with the port
      * @param	explanation explnatory string of port
     **/
	void setInOrigin( int i, Object obj, String explanation) {
		if ( obj != null ) {
			inputPorts[i].setOrigin(ALDOperator.getALDPortHashAccessKey().getHistoryLink(obj));
			if ( obj.getClass().getPackage() == null )
				inputPorts[i].setClassname( obj.getClass().getName());
			else
				inputPorts[i].setClassname( obj.getClass().getPackage()+"."+obj.getClass().getName());
		}
		inputPorts[i].setExplanation( explanation);

	}

	/** Set the origin of the output port with index i.
      * This sets the origin of the accociated output port as well
      * as its canoncial classname. The explanation is usually to
      * be copied from the argument descriptor of this output port.
      *
      * @param	i index of the input port
      * @param	obj object to assiciate with the port
      * @param	explanation explnatory string of port
     **/
	void setOutOrigin( int i, Object obj, String explanation) {
		if ( obj != null ) {
			outputPorts[i].setOrigin( ALDOperator.getALDPortHashAccessKey().getHistoryLink(obj) );
			if ( obj.getClass().getPackage() == null ) {
				outputPorts[i].setClassname( obj.getClass().getName());
			} else {
				outputPorts[i].setClassname( obj.getClass().getPackage()+"."+obj.getClass().getName());
			}
		}
		outputPorts[i].setExplanation( explanation);
	}

	/** Set the parent opNode.
	 */
	public void setParent( ALDOpNode parent) {
		this.parent = parent;
	}

	/** Get the parent opNode.
	 */
	public ALDOpNode getParent() {
		return parent;
	}

	/** Get the all opNode children found during back tracing.
	 */
	protected Vector<ALDOpNode> getChildren() {
		return children;
	}

	/** Get the all <code>opNode</code> children directly registered during invocation.
	 */
	protected Vector<ALDOpNode> getDirectlyRegisteredChildern() {
		return directlyRegisteredChildren;
	}

	/** Get all included data ports found during back tracing.
     */
	protected Vector<ALDDataPort> getIncludedData() {
		return includedData;
	}

	/** Get all input ports of this opNode.
     */
	protected ALDInputPort[] getInputPorts() {
		return inputPorts;
	}

	/** Get all output ports of this opNode.
     */
	protected ALDOutputPort[] getOutputPorts() {
		return outputPorts;
	}

	/** Get all keys of the parameter hash.
     */
	public Enumeration<String> getParameterKeys() {
		return this.parameterHash.keys();
	}

	/** Get value of parameter for given key.
     */
	public String getParameter( String key) {
		return this.parameterHash.get( key);
	}

	/** Set hidden flag of this opNode.
	 * This prevents this <code>opNode</code> to be included into a processing
	 * history as explicitly constructed.
     */
	public void setHidden( HidingMode hidingMode) {
		this.hidingMode = hidingMode;
	}

	/** Get hiding mode of this opNode.
     */
	public HidingMode getHidingMode() {
		return this.hidingMode;
	}

	/** Get the parameter hash where values of parameters.
	 * are represented as xml objects.
	 */
	public XmlObject getParameterHashAsXml() {
		return this.parameterHashAsXml;
	}

	/** Print information if this opNode to standard output.
	 */
	public void print() {
		System.out.println( "ALDOpNode ( with " + inputPorts.length +
					"/" + outputPorts.length + " ports for operator " + name +
					" at depth " + depth + ", completeDAG " + completeDAG + ")" );
		System.out.println( "    >>>>>   children: ");
		Iterator<ALDOpNode> oItr = children.iterator();
		while ( oItr.hasNext() ) {
			System.out.println( "    " + oItr.next().getName());
		}
		System.out.println( "    <<<<<   children: ");
		System.out.println( "    >>>>>   direct children: ");
		oItr = directlyRegisteredChildren.iterator();
		while ( oItr.hasNext() ) {
			System.out.println( "    " + oItr.next().getName());
		}
		System.out.println( "    <<<<<   direct children: ");
	}

	// Experimental
	/** if true all instances of ALDOpNode will be collected in a
	 * static Vector.
	 * WARNING: no instance of ALDOpNode will ever be freed currently in this case
	 */
	private static boolean collectInstances = false;

	// Maybe we should use weak references to allow instances of opNode to be freed
	private static Vector<ALDOpNode> allInstances = new Vector<ALDOpNode>();  

    static synchronized Vector<ALDOpNode> getAllInstances()  {  
         return( (Vector<ALDOpNode>)allInstances.clone() );  
    }  

	public static synchronized void printInstanceStatistics() {
		printInstanceStatistics( System.out);
	}

	public static synchronized void printInstanceStatistics( PrintStream stream) {
		stream.println( "Currently there are " + allInstances.size() +
						" instances of ALDOpNode");

		int numberParameters = 0;
		int charsInParameters = 0;
		int charsInValues = 0;

		for ( int i = 0 ; i < allInstances.size() ; i++ ) {
			Enumeration<String> keys = allInstances.elementAt( i).getParameterKeys();

			while(keys.hasMoreElements()) {
				numberParameters++;
				String key = keys.nextElement();
				charsInParameters += key.length();

				charsInValues += allInstances.elementAt( i).getParameter( key).length();
			}
		}
		stream.println( "   with a total of " + numberParameters + " parameters with " +
						+ charsInParameters + " characters in the keys and " +
						+ charsInValues + " characters in the values");
	}

	// END Experimental

}
