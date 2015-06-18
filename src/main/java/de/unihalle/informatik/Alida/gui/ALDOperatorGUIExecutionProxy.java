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

package de.unihalle.informatik.Alida.gui;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.concurrent.BlockingDeque;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

import de.unihalle.informatik.Alida.batch.ALDBatchRunResultInfo;
import de.unihalle.informatik.Alida.batch.ALDBatchOutputManagerSwing.ProviderInteractionLevel;
import de.unihalle.informatik.Alida.dataio.ALDDataIOManagerSwing;
import de.unihalle.informatik.Alida.exceptions.ALDBatchIOException;
import de.unihalle.informatik.Alida.exceptions.ALDDataIOException;
import de.unihalle.informatik.Alida.exceptions.ALDOperatorException;
import de.unihalle.informatik.Alida.exceptions.ALDWorkflowException;
import de.unihalle.informatik.Alida.gui.ALDOperatorResultFrame;
import de.unihalle.informatik.Alida.operator.ALDOperator;
import de.unihalle.informatik.Alida.operator.ALDOperatorControllable;
import de.unihalle.informatik.Alida.operator.ALDOperatorLocation;
import de.unihalle.informatik.Alida.operator.events.*;
import de.unihalle.informatik.Alida.operator.events.ALDControlEvent.ALDControlEventType;
import de.unihalle.informatik.Alida.operator.events.ALDOpParameterUpdateEvent.EventType;
import de.unihalle.informatik.Alida.workflows.ALDWorkflow;
import de.unihalle.informatik.Alida.workflows.ALDWorkflow.ALDWorkflowContextType;
import de.unihalle.informatik.Alida.workflows.ALDWorkflowNodeID;
import de.unihalle.informatik.Alida.workflows.ALDWorkflowNode.ALDWorkflowNodeState;
import de.unihalle.informatik.Alida.workflows.events.ALDWorkflowEvent;
import de.unihalle.informatik.Alida.workflows.events.ALDWorkflowRunFailureInfo;
import de.unihalle.informatik.Alida.workflows.events.ALDWorkflowEvent.*;
import de.unihalle.informatik.Alida.workflows.events.ALDWorkflowEventListener;

/**
 * Manager for executing single operator and workflow objects via GUI.
 * 
 * @author Birgit Moeller
 */
public class ALDOperatorGUIExecutionProxy 
	implements ALDWorkflowEventListener, ActionListener {
	
	/* ***********************************
	 * Some local data type declarations.
	 * ***********************************/
	
	/**
	 * Status of the associated thread to execute operator.
	 */
	protected static enum WorkflowThreadStatus {
		/**
		 * Thread is ready for execution.
		 */
		THREAD_READY,
		/**
		 * Thread is running.
		 */
		THREAD_RUNNING,
		/**
		 * Thread is paused.
		 */
		THREAD_PAUSED,
		/**
		 * Thread was interrupted.
		 */
		THREAD_INTERRUPTED,
		/**
		 * Thread is not running.
		 */
		THREAD_STOPPED
	}

	/**
	 * Labels to be used on buttons of Yes/No message boxes.
	 */
	protected final Object[] yesnoOption = { "YES", "NO" };

	/* ******************
	 * Internal members.
	 * ******************/

	/**
	 * Reference to the underlying Alida workflow object.
	 */
	protected ALDWorkflow alidaWorkflow;

	/**
	 * Current status of the operator execution thread.
	 */
	protected WorkflowThreadStatus workflowStatus = 
		WorkflowThreadStatus.THREAD_READY;

	/**
	 * Corresponding configuration and control window.
	 */
	protected ALDOperatorControlFrame controlWin;

	/**
	 * Corresponding configuration and control window.
	 */
	protected JFrame failureMessageWin;

	/**
	 * Listener object attached to the control window.
	 */
	protected ParameterUpdateListener paramUpdateListener;
	
	/**
	 * Reference ID of the operator node in Alida workflow;
	 */
	protected ALDWorkflowNodeID operatorNodeID;
	
	/**
	 * Frame showing operator execution results.
	 */
	protected ALDOperatorResultFrame resultFrame;
	
	/**
	 * Flag to indicate if the is an abortion currently going on.
	 */
//	protected boolean processEvents = true;

	/* ***********************
	 * Batch mode ingredients.
	 * ***********************/
	
	/**
	 * Flag to indicate if batch mode is active.
	 */
	protected boolean batchModeActive;
	
	/**
	 * Name of batch mode input parameter.
	 */
	protected String batchInputParameter;
	
	/**
	 * Iterator for batch mode input parameter.
	 */
	protected Iterator<Object> batchInputIterator;
	
	/**
	 * List of batch mode output parameters.
	 */
	protected LinkedList<String> batchOutputParameter;
	
	/**
	 * Batch mode result objects.
	 * <p>
	 * The key of the hashmap is corresponding to the names of the output 
	 * parameters, the values are the result data objects.
	 */
	protected HashMap<String, ALDBatchRunResultInfo> batchOutputResultMap;
	
	/**
	 * Default constructor.
	 * @param opLocation Info from where to instantiate the operator object.
	 */
	public ALDOperatorGUIExecutionProxy(ALDOperatorLocation opLocation) {
		// init workflow
		try {
			this.alidaWorkflow =
				new ALDWorkflow(" ", ALDWorkflowContextType.OP_RUNNER);
			this.alidaWorkflow.addALDWorkflowEventListener(this);
		} catch (ALDOperatorException e) {
			JOptionPane.showMessageDialog(null, "[ALDOperatorGUIExecutionProxy] " 
				+	"Instantiation of workflow failed!\n",
					"Error", JOptionPane.ERROR_MESSAGE);
		}
		// init the operator, i.e. add a single node to the flow
		try {
			this.operatorNodeID = this.alidaWorkflow.createNode(opLocation);
		} catch (ALDWorkflowException ex) {
			JOptionPane.showMessageDialog(null, "Instantiation of operator node \""
				+ opLocation.getName() + "\" failed!\n", 
					"Error", JOptionPane.ERROR_MESSAGE);
		}
		// initialize the parameter update listener
		this.paramUpdateListener = new ParameterUpdateListener(null);
		// process workflow events
		this.processWorkflowEventQueue();
	}

	/**
	 * Displays the configuration and control window.
	 */
	public void showGUI() {
		this.controlWin.setVisible(true);
	}
	
	public void configureWorkflow(ALDConfigurationEvent confEvent) {
		this.alidaWorkflow.handleALDConfigurationEvent(confEvent);
		// post-process workflow events
		this.processWorkflowEventQueue();
	}

	/**
	 * Returns current status of workflow thread.
	 * @return	Status of workflow thread.
	 */
	public WorkflowThreadStatus getWorkflowThreadStatus() {
		return this.workflowStatus;
	}
	
	/**
	 * Executes the workflow.
	 */
	public void runWorkflow() {
		try {
//			this.processEvents = true;
			ALDWorkflowNodeState state =
					this.alidaWorkflow.getState(this.operatorNodeID);
			if (   !this.workflowStatus.equals(WorkflowThreadStatus.THREAD_STOPPED)
					&& state.equals(ALDWorkflowNodeState.READY)) {
				if ( this.alidaWorkflow.getOperator(this.operatorNodeID) 
									instanceof	ALDOperatorControllable) {
					if (!this.workflowStatus.equals(
							WorkflowThreadStatus.THREAD_INTERRUPTED)) {
						JOptionPane.showMessageDialog(null, 
								"Operator was already executed with current configuration,\n" +
								"please change parameters before executing it again.",
								"Alida Message", JOptionPane.INFORMATION_MESSAGE);
						return;
					}
					this.resultFrame = null;
					this.alidaWorkflow.handleALDControlEvent(
							new ALDControlEvent(this, ALDControlEventType.RUN_EVENT));
					this.alidaWorkflow.nodeParameterChanged(this.operatorNodeID);
					this.workflowStatus = WorkflowThreadStatus.THREAD_RUNNING;
					this.alidaWorkflow.runWorkflow();
				}
				else {
					// non-controllables operators should not be run again with same config
					JOptionPane.showMessageDialog(null, 
							"Operator was already executed with current configuration,\n" +
							"please change parameters before executing it again.",
							"Alida Message", JOptionPane.INFORMATION_MESSAGE);
					return;
				}
			}
			else {
				// if node is runnable, run it
				if (state.equals(ALDWorkflowNodeState.RUNNABLE)) {
					this.resultFrame = null;
					this.workflowStatus = WorkflowThreadStatus.THREAD_RUNNING;
					this.alidaWorkflow.runWorkflow();
				}
				else {
					JOptionPane.showMessageDialog(null, "Executing operator failed -\n" + 
						"operator is not fully configured!\nCheck your parameter settings!", 
						"Error", JOptionPane.ERROR_MESSAGE);					
				}
			}
		} catch (ALDWorkflowException e) {
			JOptionPane.showMessageDialog(null, "Executing operator failed!\n" + 
					e.getCommentString(), "Error", JOptionPane.ERROR_MESSAGE);
		} 
		// post-process workflow events
		this.processWorkflowEventQueue();
	}
	
	public void runWorkflowInBatchMode() 
			throws ALDBatchIOException, ALDOperatorException {
		LinkedList<String> batchParams = this.controlWin.getBatchInputParameters();
		if ( batchParams.size() > 0) {
			this.batchInputParameter = batchParams.get(0);
			this.batchInputIterator = 
					this.controlWin.getInputParamIterator(this.batchInputParameter);
			this.batchOutputParameter = this.controlWin.getBatchOutputParameters();
			this.batchOutputResultMap = new HashMap<String, ALDBatchRunResultInfo>();
			for (String output : this.batchOutputParameter) {
				this.batchOutputResultMap.put(output, new ALDBatchRunResultInfo(output));
			}
			this.batchModeActive = true;
			this.workflowStatus = WorkflowThreadStatus.THREAD_RUNNING;
			this.doNextBatchModeStep();
		}
	}
	
	protected void doNextBatchModeStep() {
		if (this.batchInputIterator.hasNext()) {
			Object nextItem = this.batchInputIterator.next();
			for (String output : this.batchOutputParameter) {
				this.batchOutputResultMap.get(output).
																				getParameterValueVec().add(nextItem);
			}
			try {
				this.alidaWorkflow.getOperator(this.operatorNodeID).setParameter(
																					this.batchInputParameter, nextItem);
				this.alidaWorkflow.nodeParameterChanged(this.operatorNodeID);
				this.processWorkflowEventQueue();
//				this.alidaWorkflow.getOperator(this.operatorNodeID).print();
				this.alidaWorkflow.runWorkflow();
			} catch (ALDWorkflowException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (ALDOperatorException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		else {
			ALDBatchOperatorResultFrame batchResultFrame;
			try {
				batchResultFrame = new ALDBatchOperatorResultFrame(
						this.alidaWorkflow.getOperator(this.operatorNodeID),
						this.batchOutputResultMap, 
						ProviderInteractionLevel.WARNINGS_ONLY);
				batchResultFrame.setVisible(true);
				this.batchModeActive = false;
			} catch (ALDWorkflowException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
	
	public void pauseWorkflow() {
		this.workflowStatus = WorkflowThreadStatus.THREAD_PAUSED;
		this.alidaWorkflow.handleALDControlEvent(
				new ALDControlEvent(this, ALDControlEventType.PAUSE_EVENT));
		// post-process workflow events
		this.processWorkflowEventQueue();
	}
	
	public void resumeWorkflow() {
		this.workflowStatus = WorkflowThreadStatus.THREAD_RUNNING;
		this.alidaWorkflow.handleALDControlEvent(
				new ALDControlEvent(this, ALDControlEventType.RESUME_EVENT));
		// post-process workflow events
		this.processWorkflowEventQueue();
	}
	
	public void doNextStepInWorkflow() {
		this.workflowStatus = WorkflowThreadStatus.THREAD_RUNNING;
		this.alidaWorkflow.handleALDControlEvent(
				new ALDControlEvent(this, ALDControlEventType.STEP_EVENT));
		// post-process workflow events
		this.processWorkflowEventQueue();
	}
	
	public void stopWorkflow() {
		this.workflowStatus = WorkflowThreadStatus.THREAD_STOPPED;
		this.alidaWorkflow.handleALDControlEvent(
				new ALDControlEvent(this, ALDControlEventType.STOP_EVENT));
		// post-process workflow events
		this.processWorkflowEventQueue();
	}

	// TODO Kill event vs. interrupt Execution!
	public void killWorkflowThread() {
//		this.alidaWorkflow.handleALDControlEvent(
//				new ALDControlEvent(this, ALDControlEventType.KILL_EVENT));
//		this.processEvents = false;
//		this.interruptWin.setVisible(true);
		this.alidaWorkflow.interruptExecution();
		// post-process workflow events
		this.processWorkflowEventQueue();
	}
	
	/**
	 * Aborts running execution of the workflow.
	 */
	public void interruptExecution() {
//		this.processEvents = false;
//		this.interruptWin.setVisible(true);
		this.alidaWorkflow.interruptExecution();
		// post-process workflow events
		this.processWorkflowEventQueue();
	}
	
	/**
	 * (Re-)display the result frame again, if results available.
	 */
	public void showResultFrame() {
		if (this.resultFrame != null)
			this.resultFrame.setVisible(true);
	}
	
	/**
	 * Does clean-up on termination, i.e. closes all open windows.
	 */
	public boolean quit() {
//		this.processEvents = false;
		// check if thread terminated
		this.processWorkflowEventQueue();
		if (   this.workflowStatus == WorkflowThreadStatus.THREAD_RUNNING
				|| this.workflowStatus == WorkflowThreadStatus.THREAD_PAUSED) {
			int selection = JOptionPane
					.showOptionDialog(
							null,
							"The operator is running or paused!\n"
									+ "Do you really want to quit?",
									"Warning", JOptionPane.DEFAULT_OPTION,
									JOptionPane.WARNING_MESSAGE, null,
									this.yesnoOption, this.yesnoOption[0]);
			switch (selection) 
			{
			case JOptionPane.YES_OPTION:
//				this.execProxy.killWorkflowThread();
				this.workflowStatus = WorkflowThreadStatus.THREAD_STOPPED;
				//	((ALDOperatorControllable) this.op)
				//	.handleALDControlEvent(new ALDControlEvent(this,
				//			ALDControlEventType.STOP_EVENT));
				break;
			case JOptionPane.NO_OPTION:
			case JOptionPane.CANCEL_OPTION:
				return false;
			}
		} 
		else {
			this.workflowStatus = WorkflowThreadStatus.THREAD_STOPPED;
		}
		// try to close the configuration window
		if (this.resultFrame != null)
			this.resultFrame.dispose();
		return this.controlWin.quit();
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
	@SuppressWarnings("unchecked")
  @Override
	public synchronized void handleALDWorkflowEvent(ALDWorkflowEvent event) {

		// extract event data
		ALDWorkflowEventType type = event.getEventType();
		Object eventInfo = event.getId();

		// handle the event
		switch(type) 
		{
		case ADD_NODE:
			if (eventInfo instanceof ALDWorkflowNodeID) {
				ALDOperator op;
				try {
					op = this.alidaWorkflow.getOperator(
							(ALDWorkflowNodeID)eventInfo);
					// request initial GUI values for operator
					ALDOperator initialOp = (ALDOperator)ALDDataIOManagerSwing.
							getInstance().getInitialGUIValue(null, op.getClass(), op, 
									null);
					// copy parameter settings
					Collection<String> params = op.getParameterNames();
					// copy the collection, because iterating over a collection
					// which could be meanwhile modified is a very bad idea!
					String[] paramArray = new String[params.size()];
					int i=0;
					for (String s: params) {
						paramArray[i] = s;
						++i;
					}
					for (i=0; i<paramArray.length; ++i) {
						String pname = paramArray[i];
						// if the parameter does not exist anymore (e.g., due to 
						// callback changes), skip it
						if (   !op.hasParameter(pname)
						    || !initialOp.hasParameter(pname))
							continue;
						op.setParameter(pname, initialOp.getParameter(pname));
					}
					this.handleAddNodeEvent(op, (ALDWorkflowNodeID)eventInfo);
				} catch (ALDWorkflowException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (ALDDataIOException e) {
	        // TODO Auto-generated catch block
	        e.printStackTrace();
        } catch (ALDOperatorException e) {
	        // TODO Auto-generated catch block
	        e.printStackTrace();
        }
				this.workflowStatus = WorkflowThreadStatus.THREAD_READY;
			}
			break;
		case NODE_PARAMETER_CHANGE:
			// don't do updates in the GUI if batch mode is active
			// (in particular the image I/O provider would open images used as
			//  batch input parameters - which is of course, not intended)
			if (!this.batchModeActive)
				this.handleNodeParameterChangeEvent(
					(Collection<ALDWorkflowNodeID>)eventInfo);
			// update node states as well
			this.handleNodeStateChangeEvent(
				(Collection<ALDWorkflowNodeID>)eventInfo);
			break;
		case NODE_STATE_CHANGE:
			this.handleNodeStateChangeEvent(
																(Collection<ALDWorkflowNodeID>)eventInfo);
			break;
		case NODE_EXECUTION_PROGRESS:
			// only proceed if progress event messages are to be displayed
			if (!this.controlWin.showProgressEvents())
				break;
			// new status message about execution progress received
			try {
	      HashSet<ALDWorkflowNodeID> idHash = 
	      	(HashSet<ALDWorkflowNodeID>) eventInfo;
	      // we only consider the first node here...
	      Iterator<ALDWorkflowNodeID> nodeID = idHash.iterator();
	      String msg = this.alidaWorkflow.getNode(nodeID.next()).
	      		getOperatorExecutionProgressDescr();
	      // update status message in control window
	      this.controlWin.setStatus(msg);
      } catch (ALDWorkflowException e1) {
      	System.err.println("[ALDOperatorGUIExecutionProxy] " 
      		+ "could not handle/update operator status message... ignoring!");
      }
			break;
		case RUN_FAILURE:
			this.workflowStatus = WorkflowThreadStatus.THREAD_READY;
			this.displayFailureMessageWindow(event);
			break;
		case USER_INTERRUPT:
//			JOptionPane.showMessageDialog(null, "Execution was aborted!", 
//					"Workflow Execution Message", JOptionPane.ERROR_MESSAGE);
			this.workflowStatus = WorkflowThreadStatus.THREAD_INTERRUPTED;
			break;
		case EXECUTION_FINISHED:
			if (this.workflowStatus.equals(WorkflowThreadStatus.THREAD_STOPPED))
				this.workflowStatus = WorkflowThreadStatus.THREAD_STOPPED;
			// inform the control window of the termination of the thread
			this.controlWin.updateNodeStatus(ALDWorkflowNodeState.READY);
			break;
		case SHOW_RESULTS:
			if (!(eventInfo instanceof ALDWorkflowNodeID)) {
				JOptionPane.showMessageDialog(null, "Cannot display results!", 
						"Workflow Execution Error", JOptionPane.ERROR_MESSAGE);
				return;
			}
			if (this.batchModeActive) {
				ALDOperator op;
				try {
					op = this.alidaWorkflow.getOperator((ALDWorkflowNodeID)eventInfo);
					for (String output : this.batchOutputParameter) {
						Object resultValue = op.getParameter(output);
						this.batchOutputResultMap.get(output).
																					getResultDataVec().add(resultValue);
					}
				} catch (ALDWorkflowException e) {
					JOptionPane.showMessageDialog(null, "Cannot extract results!", 
							"Workflow Execution Error", JOptionPane.ERROR_MESSAGE);
				} catch (ALDOperatorException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				this.doNextBatchModeStep();
			}
			else {
				// there are results to display
				ALDOperator op;
				try {
					op = this.alidaWorkflow.getOperator((ALDWorkflowNodeID)eventInfo);
					this.resultFrame = new ALDOperatorResultFrame(op, 
									ALDDataIOManagerSwing.ProviderInteractionLevel.ALL_ALLOWED);
					this.resultFrame.setVisible(true);
				} catch (ALDWorkflowException e) {
					JOptionPane.showMessageDialog(null, "Cannot display results!", 
							"Workflow Execution Error", JOptionPane.ERROR_MESSAGE);
				}
				this.workflowStatus = WorkflowThreadStatus.THREAD_READY;
			}
			break;
		case RENAME:
			// event is specific for Grappa editor, ignored here
			break;
		default:
			System.out.println("Event type \'" + type + "\' not yet handled...");
		}
	}

	/* (non-Javadoc)
	 * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
	 */
	@Override
  public void actionPerformed(ActionEvent e) {
		String command = e.getActionCommand();
		if (command.equals("closeFailWin")) {
			this.failureMessageWin.setVisible(false);
		}
  }

	/**
	 * Displays window with detailed error information on execution failures.
	 * @param event		Workflow event got on failure.
	 */
	protected void displayFailureMessageWindow(ALDWorkflowEvent event) {
		// extract event data
		Object eventInfo = event.getId();

		// eventInfo object is of type ALDWorkflowRunFailureInfo...
		if (eventInfo instanceof ALDWorkflowRunFailureInfo) {
			ALDWorkflowRunFailureInfo failInfo =
					(ALDWorkflowRunFailureInfo)eventInfo;
			StringBuffer msg = new StringBuffer();
			
			// reset control window to runnable status
			this.controlWin.updateNodeStatus(ALDWorkflowNodeState.RUNNABLE);		
			
			// put some general information to the header
			msg.append("Something went wrong during operator execution!\n");
			msg.append("\nException message:\n");
			msg.append(event.getEventMessage(120) + "\n");
			msg.append("Exception class type: \n");
			msg.append(failInfo.getException().getClass() + "\n");
			msg.append("\nException stack trace:\n");
			StackTraceElement[] trace = failInfo.getException().getStackTrace(); 
			for (StackTraceElement elem : trace) {
				msg.append(elem.toString());
				msg.append("\n");
			}
			this.failureMessageWin = new JFrame("Operator Execution Failure!");
			this.failureMessageWin.setLayout(new BorderLayout());
			this.failureMessageWin.setSize(500, 300);
			JTextArea textArea = new JTextArea(msg.toString());
			textArea.setEditable(false);
			JScrollPane scrollPane = new JScrollPane(textArea);
			this.failureMessageWin.add(scrollPane,BorderLayout.CENTER);
			JPanel buttonPanel = new JPanel();
			JButton okButton = new JButton("Ok");
			okButton.setActionCommand("closeFailWin");
			okButton.addActionListener(this);
			buttonPanel.add(okButton);
			this.failureMessageWin.add(buttonPanel,BorderLayout.SOUTH);
			this.failureMessageWin.setVisible(true);
		}
	}
	
	/**
	 * Adds a node to the workflow graph.
	 * @param op	Operator associated with the node.
	 * @param id  Workflow node ID of the corresponding Alida workflow node.  
	 */
	protected synchronized void handleAddNodeEvent(ALDOperator op,
			ALDWorkflowNodeID id) {
		
		// if the window has already been initialized, ignore event...
		if (this.controlWin != null)
			return;

		// init the control frame
		ALDOperatorControlFrame controlFrame = null;
		try {
			this.paramUpdateListener.updateNodeID(id);
			controlFrame = this.getNewConfigWin(op);
		} catch (ALDOperatorException e) {
			System.err.println("[ALDOperatorGUIExecutionProxy::addNode] " 
					+ "cannot instantiate control frame for \"" +op.getName()+ "\"...");
			return;
		}

		// store the control frame 
		this.controlWin = controlFrame;
		
		// make sure that operator is in sync with window
//		this.controlWin.synchronizeOperatorWithGUI();
		
		LinkedList<ALDWorkflowNodeID> nodeList = 
				new LinkedList<ALDWorkflowNodeID>();
		nodeList.add(id);
		this.handleNodeParameterChangeEvent(nodeList);
		
		// tell the workflow that the operator configuration changed
		this.paramUpdateListener.handleALDParameterUpdateEvent(
				new ALDOpParameterUpdateEvent(this, EventType.CHANGED));
	}

	/**
	 * Update parameter labels according to configuration states.
	 * @param idList	List of nodes that are to be updated.
	 */
	protected synchronized void handleNodeParameterChangeEvent(
			Collection<ALDWorkflowNodeID> idList) {
		// in this case list contains only a single node
		for (ALDWorkflowNodeID nodeID: idList) {
			try {
				// update parameter values in control window
				this.controlWin.updateOperator(
						this.alidaWorkflow.getOperator(nodeID));
			

				this.controlWin.updateParamConfigurationStatus(
					this.alidaWorkflow.getOperator(nodeID).unconfiguredItems());
			}
			catch (ALDWorkflowException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

	/**
	 * Update control window color according to node's state.
	 * @param idList	List of nodes that are to be updated.
	 */
	protected synchronized void handleNodeStateChangeEvent(
			Collection<ALDWorkflowNodeID> idList) {
		// in this case list contains only a single node
		for (ALDWorkflowNodeID nodeID: idList) {
			try {
				ALDWorkflowNodeState nodeState = this.alidaWorkflow.getState(nodeID);
				// update the configuration window
				this.controlWin.updateNodeStatus(nodeState);
			}
			catch (ALDWorkflowException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
	
	/**
	 * Generate a new configuration window.
	 * <p>
	 * Is to be overwritten by subclasses.
	 * 
	 * @param op		Operator for which window is requested.
	 * @return	New configuration window.
	 * @throws ALDOperatorException Thrown if initialization of window fails.
	 */
	protected ALDOperatorControlFrame getNewConfigWin(ALDOperator op) 
			throws ALDOperatorException {
		return new ALDOperatorControlFrame(op, this, this.paramUpdateListener);
	}
	
	/**
	 * Listener class to react on parameter value updates in config window.
	 * @author moeller
	 */
	protected class ParameterUpdateListener 
		implements ALDOpParameterUpdateEventListener {

		/**
		 * Corresponding node ID.
		 */
		private ALDWorkflowNodeID id;
		
		/**
		 * Default constructor.
		 * @param nodeID	Alida workflow node ID of associated operator node.
		 */
		public ParameterUpdateListener(ALDWorkflowNodeID nodeID) {
			this.id = nodeID;
		}
		
		/**
		 * Updates the ID of the workflow node associated with this listener.
		 * @param nodeID	New node ID.
		 */
		public void updateNodeID(ALDWorkflowNodeID nodeID) {
			this.id = nodeID;
		}
		
		/* (non-Javadoc)
		 * @see de.unihalle.informatik.Alida.operator.events.ALDOpParameterUpdateEventListener#handleALDParameterUpdateEvent(de.unihalle.informatik.Alida.operator.events.ALDOpParameterUpdateEvent)
		 */
		@Override
    public void handleALDParameterUpdateEvent(ALDOpParameterUpdateEvent e) {
			
			// make sure that parameter updater is properly initialized,
			// if not, just ignore the event
			if (this.id == null)
				return;
			
			try {
				switch(e.getType())
				{
				case CHANGED:
					// notify workflow of change in node parameters
		      ALDOperatorGUIExecutionProxy.this.alidaWorkflow.nodeParameterChanged(
		      	this.id);
					break;
				case LOADED:
					ALDOperatorGUIExecutionProxy.this.alidaWorkflow.setOperator(this.id, 
						ALDOperatorGUIExecutionProxy.this.controlWin.getOperator());
					break;
				}
	      // process event queue
	      ALDOperatorGUIExecutionProxy.this.processWorkflowEventQueue();
      } catch (ALDWorkflowException ex) {
      	System.err.println("[ParameterUpdateListener] Warning! " 
     			+ "could not propagate parameter update event, node not found!");
      }	  
    }
	}
}
