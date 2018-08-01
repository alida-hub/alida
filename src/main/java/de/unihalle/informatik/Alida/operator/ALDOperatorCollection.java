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

import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Set;
import java.util.Vector;
import java.util.concurrent.BlockingDeque;

import javax.swing.JOptionPane;

import de.unihalle.informatik.Alida.exceptions.ALDOperatorException;
import de.unihalle.informatik.Alida.exceptions.ALDWorkflowException;
import de.unihalle.informatik.Alida.gui.ALDOperatorConfigurationFrame;
import de.unihalle.informatik.Alida.helpers.ALDClassInfo;
import de.unihalle.informatik.Alida.operator.events.*;
import de.unihalle.informatik.Alida.operator.events.ALDControlEvent.ALDControlEventType;
import de.unihalle.informatik.Alida.operator.events.ALDOperatorCollectionEvent.ALDOperatorCollectionEventType;
import de.unihalle.informatik.Alida.workflows.ALDWorkflow;
import de.unihalle.informatik.Alida.workflows.ALDWorkflowNodeID;
import de.unihalle.informatik.Alida.workflows.ALDWorkflow.ALDWorkflowContextType;
import de.unihalle.informatik.Alida.workflows.events.ALDWorkflowEvent;
import de.unihalle.informatik.Alida.workflows.events.ALDWorkflowEvent.ALDWorkflowEventType;
import de.unihalle.informatik.Alida.workflows.events.ALDWorkflowEventListener;

/**
 * Class to manage a set of operators.
 * <p>
 * Each managed operator can be graphically configured and programmatically
 * be executed. Communication with this class works via method calls and 
 * events. 
 * 
 * @author moeller
 */
public class ALDOperatorCollection<T extends ALDOperatorCollectionElement> {
	
	/**
	 * Class object of the generics type.
	 */
	private Class<T> elementType;
	
	private Set<Class> availableClasses;
	 
	private HashMap<String, T> classNameMapping;
	
	/**
	 * Mapping of short names to detector IDs.
	 */
	private HashMap<String, String> shortNamesToIDs = null;

	/**
	 * Mapping of IDs to short names.
	 */
	private HashMap<String, String> idsToShortNames = null;

	private HashMap<String, ALDOperatorConfigurationFrame> configFrames;

	/**
	 * Proxy object to run particle detector in thread mode.
	 */
	private OperatorExecutionProxy opProxy;
	
	public ALDOperatorCollection(Class<T> type) throws InstantiationException {
		this.elementType = type;
		this.classNameMapping = new HashMap<>();
		this.configFrames = new HashMap<>();
		this.idsToShortNames = new HashMap<String, String>();
		this.opProxy = new OperatorExecutionProxy();
		this.shortNamesToIDs = new HashMap<String, String>();

		this.availableClasses = ALDClassInfo.lookupExtendingClasses(
				this.elementType);

		for (Class c: this.availableClasses) {
			ALDOperatorCollectionElement dOp;
			try {
				dOp = (T)c.newInstance();
				String classID = dOp.getUniqueClassID();
				this.classNameMapping.put(classID, (T)dOp);
				this.shortNamesToIDs.put(classID, dOp.getUniqueClassID());
				this.idsToShortNames.put(dOp.getUniqueClassID(), classID);
//				detectorList.add(cname);
			} catch (IllegalAccessException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} 
		}
	}
	
	public void addOperatorCollectionEventListener(
			ALDOperatorCollectionEventListener acl) {
		this.opProxy.listenerList.add(acl);
	}
	
	public Set<Class> getAvailableClasses() {
		return this.availableClasses;
	}
	
	public Collection<String> getShortClassNames() {
		LinkedList<String> coll = new LinkedList<>();
		Set<String> shortNames = this.shortNamesToIDs.keySet();
		for (String s: shortNames)
			coll.add(s);
		return coll;
	}
	
	public Collection<String> getUniqueClassIDs() {
		LinkedList<String> coll = new LinkedList<>();
		Set<String> shortNames = this.shortNamesToIDs.keySet();
		for (String s: shortNames)
			coll.add(this.shortNamesToIDs.get(s));
		return coll;
	}

	public String getShortClassName(String id) {
		return this.idsToShortNames.get(id);
	}
	
	public String getUniqueID(String shortClassName) {
		return this.shortNamesToIDs.get(shortClassName);
	}
	
	public T getOperator(String classID) {
		return this.classNameMapping.get(classID);
	}
	
	public ALDOperatorCollectionElement getOperator(ALDWorkflowNodeID nid) {
		return this.opProxy.opNodeIDs.get(nid);
	}

	public void configureOperator(String classID) throws ALDOperatorException {
		T op = this.classNameMapping.get(classID);
		ALDOperatorConfigurationFrame confWin;
		if (this.configFrames.get(classID) == null) {
			// TODO: event listener == null!
			confWin =	new ALDOperatorConfigurationFrame(op, null);
			this.configFrames.put(classID, confWin);
		}
		else {
			confWin = this.configFrames.get(classID);
		}
		confWin.setVisible(true);
	}

	public void operatorConfigurationChanged() {
		this.opProxy.nodeParameterChanged();
	}
	
	public void runOperators(Collection<String> ops) {
		this.opProxy.runWorkflow(ops);
	}
	
	/**
	 * Manager class for (threaded) execution of operators of a collection.
	 */
	protected class OperatorExecutionProxy implements ALDWorkflowEventListener {
		
		/**
		 * Reference to the underlying Alida workflow object.
		 */
		protected ALDWorkflow alidaWorkflow;

		public LinkedList<ALDOperatorCollectionEventListener> listenerList;
		
		/**
		 * Listener object attached to the operator configuration object.
		 */
//		protected ValueChangeListener valueChangeListener;

		/**
		 * Reference IDs of the operator nodes in the Alida workflow;
		 */
		public HashMap<ALDWorkflowNodeID, ALDOperatorCollectionElement> opNodeIDs;
		
		/**
		 * Default constructor.
		 * @param op	Operator object to be executed.
		 */
		public OperatorExecutionProxy() {
			try {
				this.alidaWorkflow = 
						new ALDWorkflow(" ",ALDWorkflowContextType.OTHER);
				this.alidaWorkflow.addALDWorkflowEventListener(this);
			} catch (ALDOperatorException e) {
//				IJ.error("Workflow initialization failed! Exiting!");
				System.exit(-1);
			}
			// some initializations
			this.listenerList = new LinkedList<>();
			// init the operator and its workflow, i.e. add all operator nodes
			this.opNodeIDs = new HashMap<>();

			// process workflow events
			this.processWorkflowEventQueue();
			
		}
		
		/**
		 * Notify workflow that operator object parameters changed.
		 */
		public void nodeParameterChanged() {
			try {
				for (ALDWorkflowNodeID nid : this.opNodeIDs.keySet())
					this.alidaWorkflow.nodeParameterChanged(nid);
      } catch (ALDWorkflowException e) {
//      	IJ.error("Workflow interaction failed!");
      }
		}
		
		/**
		 * Request the state of the operator workflow node.
		 * @return	State of the node.
		 */
//		public ALDWorkflowNodeState getOpState() {
//      try {
//      	return this.alidaWorkflow.getState(this.opNodeID);
//      } catch (ALDWorkflowException e) {
//      	IJ.error("Workflow interaction failed!");
//	      return null;
//      }
//		}
		
		/**
		 * Executes the workflow.
		 */
		protected void runWorkflow(Collection<String> opIDs) {

			try {
				for (ALDWorkflowNodeID nid: this.opNodeIDs.keySet())
					this.alidaWorkflow.removeNode(nid);
				this.opNodeIDs.clear();
				for (String uid: opIDs) {
					ALDOperatorCollectionElement op = 
							ALDOperatorCollection.this.classNameMapping.get(uid);
					try {
						ALDWorkflowNodeID nid = this.alidaWorkflow.createNode(op); 
						this.opNodeIDs.put(nid, op);
//						this.valueChangeListener = new ValueChangeListener(nid);
//						op.addValueChangeEventListener(this.valueChangeListener);
					} catch (ALDWorkflowException ex) {
						JOptionPane.showMessageDialog(null, "Instantiation of operator \""
								+ op.getName() + "\" failed!\n", 
								"Error", JOptionPane.ERROR_MESSAGE);
					}
				}
				// execute the node/workflow
				this.alidaWorkflow.handleALDControlEvent(
						new ALDControlEvent(this, ALDControlEventType.RUN_EVENT));
				this.alidaWorkflow.runWorkflow();
			} catch (ALDWorkflowException e) {
				e.printStackTrace();
				JOptionPane.showMessageDialog(null, "Executing operator failed!\n",
						"Error", JOptionPane.ERROR_MESSAGE);
			} 
			// post-process workflow events
			this.processWorkflowEventQueue();
		}
		
		/**
		 * Processes all events that were recently added to the queue.
		 * <p>
		 * Note that this function needs to be called after all actions on the 
		 * Alida workflow except calls to 'run' methods.
		 */
		protected synchronized void processWorkflowEventQueue() {
			BlockingDeque<ALDWorkflowEvent> queue = 
					this.alidaWorkflow.getEventQueue(this);
			ALDWorkflowEvent event = null; 	
			while (!queue.isEmpty()) {
				event = queue.pop();
				this.handleALDWorkflowEvent(event);
			}
		}
		
		/* (non-Javadoc)
		 * @see de.unihalle.informatik.Alida.workflows.events.ALDWorkflowEventListener#handleALDWorkflowEvent(de.unihalle.informatik.Alida.workflows.events.ALDWorkflowEvent)
		 */
		@Override
		public synchronized void handleALDWorkflowEvent(ALDWorkflowEvent event) {
			
			// extract event data
			ALDWorkflowEventType type = event.getEventType();
			
			// handle the event
			ALDOperatorCollectionEvent opce = null; 
			switch(type) 
			{
			case RUN_FAILURE:
				opce = new ALDOperatorCollectionEvent(this,
					ALDOperatorCollectionEventType.RUN_FAILURE,
						"Operator execution failed!", event.getInfo());
				break;
			case SHOW_RESULTS:
				opce = new ALDOperatorCollectionEvent(this,
					ALDOperatorCollectionEventType.RESULTS_AVAILABLE,
						"Operator finished, results available!", event.getInfo());
				break;
			default:
				break;
			}						
			// just propagate relevant workflow events to registered listeners
			for (ALDOperatorCollectionEventListener opcl : this.listenerList) {
				opcl.handleALDOperatorCollectionEvent(opce);
			}
		}

//		/**
//		 * Listener class to react on parameter value changes in operator.
//		 * @author moeller
//		 */
//		class ValueChangeListener implements ALDSwingValueChangeListener {
//
//			/**
//			 * Corresponding node ID.
//			 */
//			private ALDWorkflowNodeID id;
//
//			/**
//			 * Default constructor.
//			 * @param nodeID	Alida workflow node ID of associated operator node.
//			 */
//			public ValueChangeListener(ALDWorkflowNodeID nodeID) {
//				this.id = nodeID;
//			}
//
//			/**
//			 * Updates the ID of the workflow node associated with this listener.
//			 * @param nodeID	New node ID.
//			 */
//			public void updateNodeID(ALDWorkflowNodeID nodeID) {
//				this.id = nodeID;
//			}
//
//			@Override
//      public void handleValueChangeEvent(ALDSwingValueChangeEvent event) {
//				try {
//					// notify workflow of change in node parameters
//					OperatorExecutionProxy.this.alidaWorkflow.nodeParameterChanged(
//							this.id);
//					// process event queue
//					OperatorExecutionProxy.this.processWorkflowEventQueue();
//				} catch (ALDWorkflowException ex) {
//					System.err.println("[ValueChangeListener] Warning! " 
//							+ "could not propagate parameter change event, node not found!");
//				}	  
//      }
//		}
	}
}
