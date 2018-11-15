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
import java.util.concurrent.BlockingDeque;

import javax.swing.event.EventListenerList;

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
import de.unihalle.informatik.Alida.workflows.ALDWorkflowNode.ALDWorkflowNodeState;
import de.unihalle.informatik.Alida.workflows.events.ALDWorkflowEvent;
import de.unihalle.informatik.Alida.workflows.events.ALDWorkflowEvent.ALDWorkflowEventType;
import de.unihalle.informatik.Alida.workflows.events.ALDWorkflowEventListener;

/**
 * Class to manage a set of operators.
 * <p>
 * Each managed operator can be graphically configured and be executed. 
 * Communication with this class works via method calls. Operators are run
 * in an {@link ALDWorkflow} environment, messages are handed over from the
 * operator collection to interactors outside via events. 
 * 
 * @param <T> Class parameter, indicating which type of operators is managed.
 * @author moeller
 */
public class ALDOperatorCollection<T extends ALDOperatorCollectionElement> {
	
	/**
	 * Class object of the generics type.
	 */
	protected Class<T> elementType;
	
	/**
	 * Set of available operator classes.
	 */
	@SuppressWarnings("rawtypes")
	protected Set<Class> availableClasses = null;
	 
	/**
	 * Mapping of unique operator class IDs to operator instances.
	 */
	protected HashMap<String, T> idsToOperatorObjects = null;
	
	/**
	 * Configuration map storing if operators are allowed to re-run with 
	 * identical parameter settings.
	 */
	protected HashMap<String, Boolean> idsToRerunFlags = null;

	/**
	 * Collection of configuration frames for the available operators.
	 */
	protected HashMap<String, ALDOperatorConfigurationFrame> configFrames = null;

	/**
	 * Collection of parameter change listeners.
	 */
	protected HashMap<String, ParameterUpdateListener> paramListener = null;

	/**
	 * Proxy object to run operators in threaded mode.
	 * <p>
	 * For execution of operators {@ALDWorkflow} environments are used.
	 */
	protected OperatorExecutionProxy opProxy;
	
	/**
	 * Default constructor.
	 * 
	 * @param type	Class of generics type, simplifies internal type handling.
	 * @throws InstantiationException	Thrown in case of error or failure.
	 * @throws ALDOperatorException		Thrown in case of error or failure. 
	 */
	@SuppressWarnings("unchecked")
	public ALDOperatorCollection(Class<T> type) 
			throws InstantiationException, ALDOperatorException {
		
		this.elementType = type;
		
		// instantiate arrays
		this.configFrames = new HashMap<>();
		this.idsToOperatorObjects = new HashMap<>();
		this.idsToRerunFlags = new HashMap<>();
		this.paramListener = new HashMap<>();

		// search for available classes of requested type
		this.availableClasses = 
			ALDClassInfo.lookupExtendingClasses(this.elementType);

		// fill the internal data structures,
		// configuration frames are only initialized on first call
		String classUID;
		ALDOperatorCollectionElement op;
		for (Class<?> c: this.availableClasses) {
			try {
				op = (T)c.newInstance();
				classUID = op.getUniqueClassIdentifier();
				this.idsToOperatorObjects.put(classUID, (T)op);
				this.idsToRerunFlags.put(classUID, new Boolean(false));
			} catch (IllegalAccessException e) {
				e.printStackTrace();
			} 
		}
		// needs to be run after arrays have been filled!
		this.opProxy = new OperatorExecutionProxy();
	}
	
	/**
	 * Add an event listener for getting events when running operators.
	 * 
	 * @param evl	Event listener to register.	
	 */
	public void addALDOperatorCollectionEventListener(
			ALDOperatorCollectionEventListener evl) {
		this.opProxy.listenerList.add(
				ALDOperatorCollectionEventListener.class, evl);
	}
	
	/**
	 * Deregister an event listener.
	 * 
	 * @param evl	Event listener to remove.	
	 */
  public void removeALDOperatorCollectionEventListener(
  		ALDOperatorCollectionEventListener evl) {
    this.opProxy.listenerList.remove(
    		ALDOperatorCollectionEventListener.class, evl);
  }

	/**
	 * Request set of available classes.
	 * @return	Set of derived classes.
	 */
	@SuppressWarnings("rawtypes")
	public Set<Class> getAvailableClasses() {
		return this.availableClasses;
	}
	
	/**
	 * Request collection of unique class IDs for available classes.
	 * <p>
	 * Note that the collection has no specific ordering.
	 * 
	 * @return	Collection of unique identifier strings.
	 */
	public Collection<String> getUniqueClassIDs() {
		LinkedList<String> coll = new LinkedList<>();
		Set<String> ids = this.idsToOperatorObjects.keySet();
		for (String id: ids)
			coll.add(id);
		return coll;
	}

	/**
	 * Returns the operator object for given unique class identifier.
	 * @param classUID	Unique class identifier.
	 * @return	Operator object, null if UID is unknown.
	 */
	public T getOperator(String classUID) {
		return this.idsToOperatorObjects.get(classUID);
	}
	
	/**
	 * Get operator for given workflow node ID.
	 * @param nid	Workflow node ID. 
	 * @return	Operator object.
	 */
	public T getOperator(ALDWorkflowNodeID nid) {
		return this.opProxy.opNodeIDs.get(nid);
	}

	/**
	 * Method to configure an operator, i.e., open its configuration window.
	 * @param classUID	Unique class identifier of operator.
	 */
	public void openOperatorConfigWindow(String classUID) {
		this.configFrames.get(classUID).setVisible(true);
	}

	/**
	 * Method to notify the collection that configurations of operators 
	 * might have changed.
	 * @throws ALDWorkflowException Thrown in case of failure.
	 */
	public void operatorConfigurationChanged() throws ALDWorkflowException {
		this.opProxy.nodeParameterChanged();
	}
	
	/**
	 * Sets re-run flags of all operators to given boolean value. 
	 * @param flag	If true, all operators are allowed to run again.
	 */
	public void setRerunFlags(boolean flag) {
		for (String k : this.idsToRerunFlags.keySet()) {
			this.idsToRerunFlags.put(k, new Boolean(flag));
		}
	}
	
	/**
	 * Sets re-run flags of all operators in the list to given boolean value. 
	 * @param opIDs	Re-run flags of all operators in the list are set.
	 * @param flag	If true, all operators in the list are allowed to rerun.
	 */
	public void setRerunFlags(Collection<String> opIDs, boolean flag) {
		for (String k : opIDs) {
			if (!this.idsToRerunFlags.containsKey(k))
				continue;
			this.idsToRerunFlags.put(k, new Boolean(flag));
		}
	}

	/**
	 * Run a collection of operators.
	 * 
	 * @param ops	List of unique class identifiers for operators to run. 
	 */
	public void runOperators(Collection<String> ops) {
		this.opProxy.runWorkflow(ops);
	}
	
	/**
	 * Close all configuration windows.
	 */
	public void closeConfigWindows() {
		Set<String> uids = this.configFrames.keySet();
		for (String uid : uids) {
			this.configFrames.get(uid).setVisible(false);
		}
	}
	
	/**
	 * Generate configuration window for given operator.
	 * <p>
	 * To be overwritten by sub-classes using specialized operators.
	 * 
	 * @param op				Operator object.
	 * @param pListen		Parameter update listener.
	 * @return	New configuration frame.
	 * @throws ALDOperatorException Thrown in case of failure.
	 */
	protected ALDOperatorConfigurationFrame getConfigWin(ALDOperator op, 
		ALDOpParameterUpdateEventListener pListen) throws ALDOperatorException {
		 return new ALDOperatorConfigurationFrame(op, pListen);
	}
	
	/**
	 * Manager class for (threaded) execution of operators of a collection.
	 */
	protected class OperatorExecutionProxy implements ALDWorkflowEventListener {
		
		/**
		 * Reference to the underlying Alida workflow object.
		 */
		protected ALDWorkflow alidaWorkflow;

		/**
		 * Collection of registered event listeners.
		 */
		protected volatile EventListenerList listenerList;
		
		/**
		 * Mapping of workflow node IDs of active operators to operator objects.
		 */
		protected HashMap<ALDWorkflowNodeID, T> opNodeIDs;
		
		/**
		 * Mapping of operators UIDs to workflow node IDs.
		 */
		protected HashMap<String, ALDWorkflowNodeID> workflowNodeIDs;

		/**
		 * Default constructor.
		 * @throws ALDOperatorException Thrown in case of initialization failure.
		 */
		public OperatorExecutionProxy() throws ALDOperatorException {
			
			this.alidaWorkflow = new ALDWorkflow(" ",ALDWorkflowContextType.OTHER);
			
			// register ourselves to the workflow events
			this.alidaWorkflow.addALDWorkflowEventListener(this);
			
			// some initializations
			this.listenerList = new EventListenerList();
			
			// init the operator and its workflow, i.e. add all operator nodes
			this.opNodeIDs = new HashMap<>();
			this.workflowNodeIDs = new HashMap<>();
			
			// collect available operators
			Collection<String> opUIDs = 
				ALDOperatorCollection.this.getUniqueClassIDs();

			// create new operator nodes in workflow
			ALDWorkflowNodeID nid;
			ALDOperatorConfigurationFrame confWin;
			for (String uid: opUIDs) {
				T op = ALDOperatorCollection.this.idsToOperatorObjects.get(uid);
				try {
					nid = this.alidaWorkflow.createNode(op); 
					// register nodes
					this.opNodeIDs.put(nid, op);
//					this.valueChangeListener = new ValueChangeListener(nid);
//					op.addValueChangeEventListener(this.valueChangeListener);
					// init parameter listener
					ParameterUpdateListener pListen = new ParameterUpdateListener(nid);
					confWin =	ALDOperatorCollection.this.getConfigWin(op, pListen);
					confWin.updateParamConfigurationStatus(
							this.alidaWorkflow.getMissingRequiredInputs(nid));
					ALDOperatorCollection.this.configFrames.put(uid, confWin);
					ALDOperatorCollection.this.paramListener.put(uid, pListen);
					this.workflowNodeIDs.put(uid, nid);
				} catch (ALDWorkflowException ex) {
					ALDOperatorCollectionEvent opce = 
						new ALDOperatorCollectionEvent(this,
							ALDOperatorCollectionEventType.INIT_FAILURE,
								"Instantiation of operator \"" + uid + "\" failed!");
					this.fireALDOperatorCollectionEvent(opce);
				}
			}

			// process workflow events
			this.processWorkflowEventQueue();
		}
		
		/**
		 * Notify workflow that operator object parameters changed.
		 * @throws ALDWorkflowException Thrown in case of update failure.
		 */
		public void nodeParameterChanged() throws ALDWorkflowException {
			for (ALDWorkflowNodeID nid : this.opNodeIDs.keySet())
				this.alidaWorkflow.nodeParameterChanged(nid);
		}
		
		/**
		 * Request the state of the operator workflow node.
		 * @param nid	Workflow node ID of operator. 
		 * @return	State of the node.
		 * @throws ALDWorkflowException	Thrown in case of problems/failures. 
		 */
		protected ALDWorkflowNodeState getOpState(ALDWorkflowNodeID nid) 
				throws ALDWorkflowException {
			return this.alidaWorkflow.getState(nid);
		}
		
		/**
		 * Executes the workflow, i.e., the set of selected operators.
		 * <p>
		 * There is no specific order in which the operators are run.
		 * 
		 * @param opUIDs 	List of unique class identifiers for operators to run.
		 */
		protected void runWorkflow(Collection<String> opUIDs) {
			
			ALDOperatorCollectionElement op;
			ALDWorkflowNodeID nid;
			for (String uid: opUIDs) {
				op = ALDOperatorCollection.this.idsToOperatorObjects.get(uid);

				// check if operator can be executed
				if (!op.isConfigured()) {
					ALDOperatorCollectionEvent opce = 
							new ALDOperatorCollectionEvent(this,
									ALDOperatorCollectionEventType.OP_NOT_CONFIGURED,
									"Operator \"" + uid + "\" is not ready to run!", uid);
					this.fireALDOperatorCollectionEvent(opce);
				}
				nid = this.workflowNodeIDs.get(uid);
				try {
					// execute the node/workflow
					this.alidaWorkflow.handleALDControlEvent(
							new ALDControlEvent(this, ALDControlEventType.RUN_EVENT));
					// make sure that if an operator is allowed to run multiple times
					// the workflow really executes it (... it's a hack, but currently
					// the only way to achieve the desired behaviour...)
					if (ALDOperatorCollection.this.idsToRerunFlags.get(uid)
								.booleanValue()) {
						this.alidaWorkflow.nodeParameterChanged(nid);
						this.alidaWorkflow.runNode(nid);
						// post-process workflow events that might have occured so far
						this.processWorkflowEventQueue();
					}
					else {
						// operator is not allowed to run again with same parameters
						if (   this.alidaWorkflow.getState(nid) 
								== ALDWorkflowNodeState.READY) {
							ALDOperatorCollectionEvent opce = 
									new ALDOperatorCollectionEvent(this,
											ALDOperatorCollectionEventType.RUN_FAILURE,
											"Operator \"" + uid + "\" has already been executed " 
												+ "with this configuration!", uid);
							this.fireALDOperatorCollectionEvent(opce);
						}
						else {
							// operator parameters changed, everything is fine
							this.alidaWorkflow.nodeParameterChanged(nid);
							this.alidaWorkflow.runNode(nid);
							// post-process workflow events that might have occured so far
							this.processWorkflowEventQueue();
						}
					}
				} catch (ALDWorkflowException e) {
					ALDOperatorCollectionEvent opce = new ALDOperatorCollectionEvent(
						this, ALDOperatorCollectionEventType.RUN_FAILURE,
							"Operator execution failed! OpID = " + uid, e.getCommentString());
					this.fireALDOperatorCollectionEvent(opce);
				}
			}
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
			
			// handle the different events, events not considered here are ignored
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
				opce = new ALDOperatorCollectionEvent(this,
					ALDOperatorCollectionEventType.UNKNOWN,
						"An unknown error occured!", event.getInfo());
				break;
			}				
			this.fireALDOperatorCollectionEvent(opce);
		}

	  /**
	   * Pass events to registered listeners.
	   * @param ev	Event to propagate to listeners.
	   */
	  public void fireALDOperatorCollectionEvent(ALDOperatorCollectionEvent ev) {
	    // Guaranteed to return a non-null array
	    Object[] listeners = this.listenerList.getListenerList();
	    // process the listeners last to first, notifying
	    // those that are interested in this event
	    for (int i = listeners.length-2; i>=0; i-=2) {
	    	if (listeners[i] == ALDOperatorCollectionEventListener.class) {
	    		((ALDOperatorCollectionEventListener)listeners[i+1]).
	    			handleALDOperatorCollectionEvent(ev);
	    	}
	    }
	  }		
	}
	
	/**
	 * Listener class to react on parameter value updates in config window.
	 * @author moeller
	 */
	protected class ParameterUpdateListener 
		implements ALDOpParameterUpdateEventListener {

		/**
		 * Workflow node ID.
		 */
		private ALDWorkflowNodeID nID = null;
		
		/**
		 * Default constructor.
		 * @param nid	Workflow node ID.
		 */
		public ParameterUpdateListener(ALDWorkflowNodeID nid) {
			this.nID = nid;
		}
		
		/* (non-Javadoc)
		 * @see de.unihalle.informatik.Alida.operator.events.ALDOpParameterUpdateEventListener#handleALDParameterUpdateEvent(de.unihalle.informatik.Alida.operator.events.ALDOpParameterUpdateEvent)
		 */
		@Override
    public void handleALDParameterUpdateEvent(ALDOpParameterUpdateEvent e) {
			
			// make sure that parameter updater is properly initialized,
			// if not, just ignore the event
			if (this.nID == null)
				return;

			ALDOperatorCollectionElement op = 
					ALDOperatorCollection.this.opProxy.opNodeIDs.get(this.nID);

			try {
				switch(e.getType())
				{
				case CHANGED:
					// notify workflow of change in node parameters
		      ALDOperatorCollection.this.opProxy.nodeParameterChanged();
					ALDOperatorCollection.this.configFrames.
						get(op.getUniqueClassIdentifier()).updateOperator(op);
					ALDOperatorCollection.this.configFrames.
						get(op.getUniqueClassIdentifier()).updateParamConfigurationStatus(
								ALDOperatorCollection.this.opProxy.alidaWorkflow.
									getMissingRequiredInputs(this.nID));
					break;
				case LOADED:
					ALDOperator newOp =	ALDOperatorCollection.this.configFrames.get(
							op.getUniqueClassIdentifier()).getOperator();
					ALDOperatorCollection.this.opProxy.alidaWorkflow.setOperator(
							this.nID, newOp);
					ALDOperatorCollection.this.configFrames.
						get(op.getUniqueClassIdentifier()).updateOperator(op);
					ALDOperatorCollection.this.configFrames.
						get(op.getUniqueClassIdentifier()).updateParamConfigurationStatus(
								ALDOperatorCollection.this.opProxy.alidaWorkflow.
									getMissingRequiredInputs(this.nID));
					break;
				}
	      // process event queue
				ALDOperatorCollection.this.opProxy.processWorkflowEventQueue();
      } catch (ALDWorkflowException ex) {
      	System.err.println("[ParameterUpdateListener] Warning! " 
     			+ "could not propagate parameter update event, node not found!");
      }	  
    }
	}

}
