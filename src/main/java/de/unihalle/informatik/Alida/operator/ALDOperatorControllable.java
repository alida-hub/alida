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

import javax.swing.event.EventListenerList;

import de.unihalle.informatik.Alida.exceptions.ALDOperatorException;
import de.unihalle.informatik.Alida.operator.events.*;

/**
 * Operator class with inherent event handling for execution control.
 * <p>
 * This operator acts as listener for `Alida` control events. On receiving
 * events the operator changes its control status which may be used to
 * control operator execution, and in particular to interrupt calculations
 * in a controlled fashion, i.e. to keep results already obtained.
 * 
 * @author moeller
 */
public abstract class ALDOperatorControllable extends ALDOperator
	implements ALDControlEventListener, ALDControlEventReporter,
		ALDConfigurationEventListener, ALDConfigurationEventReporter {

	/**
	 * Commands for controlling the operator.
	 * <p>
	 * The status is supposed to be switched externally and the operator
	 * is expected to react on changes as soon as possible.
	 */
	public static enum OperatorControlStatus {
		/**
		 * Default state after instantiation.
		 */
		OP_INIT,
		/**
		 * Run the operator.
		 */
		OP_RUN,
		/**
		 * Stop the operator as soon as possible, save data if possible.
		 */
		OP_STOP,
		/**
		 * Pause the operator.
		 */
		OP_PAUSE,
		/**
		 * Resume operator after pause.
		 */
		OP_RESUME,
		/**
		 * Do one step.
		 */
		OP_STEP,
		/**
		 * Stop immediately, even in case of data loss.
		 */
		OP_KILL
	}
	
	/**
	 * Actual operator status.
	 */
	public static enum OperatorExecutionStatus {
		/**
		 * Default state after instantiation.
		 */
		OP_EXEC_INIT,
		/**
		 * Operator is running.
		 */
		OP_EXEC_RUNNING,
		/**
		 * Operator terminated or was killed.
		 */
		OP_EXEC_TERMINATED,
		/**
		 * Operator is paused.
		 */
		OP_EXEC_PAUSED
	}

	/**
	 * Flag for step-wise execution.
	 */
	protected boolean stepWiseExecution = false;
	
	/**
	 * Step size in step-wise execution mode.
	 */
	protected int stepSize = 1;

	/**
	 * Flag for recursive propagation of events to nested listeners.
	 */
	protected boolean notifyListenersRecursively = false; 
	
	/**
   * List of control event listeners attached to this reporter.
   */
  protected volatile EventListenerList controlEventlistenerList = 
  																									new EventListenerList();

	/**
   * List of configuration event listeners attached to this reporter.
   */
  protected volatile EventListenerList configurationEventlistenerList = 
  																									new EventListenerList();

  /**
   * Control status of operator, used to stop/pause/resume calculations.
   */
  private volatile OperatorControlStatus operatorStatus = 
  	OperatorControlStatus.OP_INIT;
  
  /**
   * Wrapper object for operator control status.
   */
  private volatile OperatorControlStatusHandle operatorStatusHandle =
  	new OperatorControlStatusHandle();
  
  /**
   * Actual execution state of operator.
   */
  protected volatile OperatorExecutionStatus operatorExecStatus = 
  	OperatorExecutionStatus.OP_EXEC_INIT; 
  
  /**
   * Default constructor.
   * @throws ALDOperatorException
   */
  public ALDOperatorControllable() throws ALDOperatorException {
	  super();
  }

  /**
   * Specify if events are to be passed forward to nested listeners or not.
   */
  public void setNotifyRecursiveFlag(boolean flag) {
  	this.notifyListenersRecursively = flag;
  }

  /**
   * Get operator control status.
   * @return	Operator control status.
   */
  public OperatorControlStatus getControlStatus() {
  	return this.operatorStatus;
  }
  
  /**
   * Set operator control status.
   * @param s	New operator control status.
   */
  public void setControlStatus(OperatorControlStatus s) {
  	this.operatorStatus = s;
  	this.operatorStatusHandle.opStatus = s;
  }

  /**
   * Get wrapper object for control status.
   * @return	Wrapper object for operator control status.
   */
  public OperatorControlStatusHandle getControlStatusHandle() {
  	return this.operatorStatusHandle;
  }
  
  /**
   * Get the current execution status of the operator.
   * @return	Current execution status.
   */
  public OperatorExecutionStatus getExecutionStatus() {
  	return this.operatorExecStatus;
  }
  
  /**
   * Function for indicating if step-wise execution is supported.
   * @return	If true, operator may be executed step-wise.
   */
  public abstract boolean supportsStepWiseExecution();
  
	@Override
  public void handleALDControlEvent(ALDControlEvent event) {
		
		switch(event.getEventType())
		{
		case RUN_EVENT:
			this.operatorStatus = OperatorControlStatus.OP_RUN;
			this.operatorStatusHandle.opStatus = OperatorControlStatus.OP_RUN;
			break;
		case PAUSE_EVENT:
			this.operatorStatus = OperatorControlStatus.OP_PAUSE;
			this.operatorStatusHandle.opStatus = OperatorControlStatus.OP_PAUSE;
			break;
		case STOP_EVENT:
			this.operatorStatus = OperatorControlStatus.OP_STOP;
			this.operatorStatusHandle.opStatus = OperatorControlStatus.OP_STOP;
			break;
		case STEP_EVENT:
			this.operatorStatus = OperatorControlStatus.OP_STEP;
			this.operatorStatusHandle.opStatus = OperatorControlStatus.OP_STEP;
			break;
		case KILL_EVENT:
			// kills the thread immediately
			Thread.currentThread().interrupt();
			break;
		case RESUME_EVENT:
			this.operatorStatus = OperatorControlStatus.OP_RESUME;
			this.operatorStatusHandle.opStatus = OperatorControlStatus.OP_RESUME;
			break;
		}
		// send event to all sub-operators and their listeners
		if (this.notifyListenersRecursively)
			this.fireALDControlEvent(new ALDControlEvent(this,event.getEventType()));
	}

	/*
	 * Implementation of event functions below according to
	 * 
	 * {@link http://www.exampledepot.com/egs/java.util/custevent.html}
	 */
	
	@Override
  public void addALDControlEventListener(ALDControlEventListener listener) {
		this.controlEventlistenerList.add(ALDControlEventListener.class, 
																			listener);
  }

	@Override
  public void removeALDControlEventListener(ALDControlEventListener listener) {
    this.controlEventlistenerList.remove(ALDControlEventListener.class, 
    																		 listener);
  }

	@Override
  public void fireALDControlEvent(ALDControlEvent event) {
    // Guaranteed to return a non-null array
    Object[] listeners = this.controlEventlistenerList.getListenerList();
    // Process the listeners last to first, notifying
    // those that are interested in this event
    for (int i = listeners.length-2; i>=0; i-=2) {
    	if (listeners[i]==ALDControlEventListener.class) {
    		// Lazily create the event:
    		((ALDControlEventListener)listeners[i+1]).handleALDControlEvent(
    				new ALDControlEvent(this, event.getEventType()));
    	}
    }
  }

	@Override
  public void addALDConfigurationEventListener(
  		ALDConfigurationEventListener listener) {
		this.configurationEventlistenerList.add(ALDConfigurationEventListener.class, 
																						listener);
  }

	@Override
  public void removeALDConfigurationEventListener(
      ALDConfigurationEventListener listener) {
    this.configurationEventlistenerList.remove(
    									ALDConfigurationEventListener.class, listener);
  }

	@Override
  public void fireALDConfigurationEvent(ALDConfigurationEvent event) {
    // Guaranteed to return a non-null array
    Object[] listeners = this.configurationEventlistenerList.getListenerList();
    // Process the listeners last to first, notifying
    // those that are interested in this event
    for (int i = listeners.length-2; i>=0; i-=2) {
    	if (listeners[i]==ALDConfigurationEventListener.class) {
    		// Lazily create the event:
    		((ALDConfigurationEventListener)listeners[i+1]).
    			handleALDConfigurationEvent(event);
//    				new ALDConfigurationEvent(this, event.getEventMessage()));
    	}
    }
  }

	@Override
  public void handleALDConfigurationEvent(ALDConfigurationEvent event) {
		this.stepSize = event.getStepSize();
		if (event.doStepwiseExecution())
			this.stepWiseExecution = true;
		else
			this.stepWiseExecution = false;
		// send event to all sub-operators, with sending object updated
		if (this.notifyListenersRecursively)
			this.fireALDConfigurationEvent(event);
//					new ALDConfigurationEvent(this,event.getEventMessage()));
	}
	
	/**
	 * Wrapper class for control status information.
	 * <p>
	 * Objects of this class are supposed to ease handing over control status 
	 * information to sub-routines of controllable operators.
	 */
	public class OperatorControlStatusHandle {
		
		/**
		 * Status of the operator.
		 */
		private volatile OperatorControlStatus opStatus;
		
		/**
		 * Get the current status
		 * @return	Current operator status.
		 */
		public OperatorControlStatus getStatus() {
			return this.opStatus;
		}
		
		/**
		 * Set the current status.
		 * @param s	New status.
		 */
		public void setStatus(OperatorControlStatus s) {
			this.opStatus = s;
			// synchronize status with operator main status
			ALDOperatorControllable.this.setControlStatus(s);
		}
	}
}
