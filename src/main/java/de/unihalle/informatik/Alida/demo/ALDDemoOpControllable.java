/*
 * This file is part of MiToBo, the Microscope Image Analysis Toolbox.
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
 * Fore more information on MiToBo, visit
 *
 *    http://www.informatik.uni-halle.de/mitobo/
 *
 */

package de.unihalle.informatik.Alida.demo;

import de.unihalle.informatik.Alida.exceptions.ALDOperatorException;
import de.unihalle.informatik.Alida.operator.ALDOperatorControllable;
import de.unihalle.informatik.Alida.operator.events.ALDOperatorExecutionProgressEvent;
import de.unihalle.informatik.Alida.annotations.ALDAOperator;
import de.unihalle.informatik.Alida.annotations.ALDAOperator.ExecutionMode;
import de.unihalle.informatik.Alida.annotations.ALDAOperator.Level;

/**
 * Controllable demo operator for testing interaction.
 * 
 * @author moeller
 */
@ALDAOperator(genericExecutionMode=ExecutionMode.ALL,
	level=Level.STANDARD)
public class ALDDemoOpControllable extends ALDOperatorControllable {

	/**
	 * Operator identifier.
	 */
	protected	final static String operatorID = "[ALDDemoOpControllable]";

	/**
	 * Default constructor.
	 * 
	 * @throws ALDOperatorException Thrown in case of failure.
	 */
	public ALDDemoOpControllable() throws ALDOperatorException {
		// nothing to do here
	}
	
	@Override
  public boolean supportsStepWiseExecution() {
		return true;
	}
	
  @Override
  protected final void operate() {

    // counter for steps
    int steps = 0;
    this.operatorStatus = OperatorControlStatus.OP_RUN;
    
    // main optimization loop
    int loopCounter = 0;
    while (loopCounter < 15) {

    	if (this.operatorStatus == OperatorControlStatus.OP_STOP) {
    		
    		this.fireOperatorExecutionProgressEvent(
    				new ALDOperatorExecutionProgressEvent(this, 
   						operatorID + " operator cancelled, going to stop..."));
    		
    		// simulate that operate needs some time before actually stopping
        try {
        	Thread.sleep(2000);
        } catch (InterruptedException e) {
        	// just ignore the exception
        }
    		break;
    	}
    	else if (this.operatorStatus == OperatorControlStatus.OP_PAUSE) {
    		this.fireOperatorExecutionProgressEvent(
    				new ALDOperatorExecutionProgressEvent(this, 
   						operatorID + " operator paused, sleeping now..."));
    		do {
					try {
	          Thread.sleep(500);
          } catch (InterruptedException e) {
          	// just ignore the exception
          }
    		} while (this.operatorStatus != OperatorControlStatus.OP_RESUME);
    		this.fireOperatorExecutionProgressEvent(
    				new ALDOperatorExecutionProgressEvent(this, 
   						operatorID + " waking up and running again..."));
    	}

      switch (this.operatorStatus) {
        case OP_RUN:
          if (this.stepWiseExecution) {
            if (steps == this.stepSize) {
              this.operatorStatus = OperatorControlStatus.OP_PAUSE;
              while (!(this.operatorStatus==OperatorControlStatus.OP_STEP)
              		&&  !(this.operatorStatus==OperatorControlStatus.OP_STOP)) { 
              	// just wait to continue
              }
              if (this.operatorStatus==OperatorControlStatus.OP_STOP) {
            		System.err.println("Demo operator cancelled!");
            		return;
              }
            	this.operatorStatus = OperatorControlStatus.OP_RUN;
              steps = 1;
            } else {
              steps++;
            }
          }
          break;
        case OP_PAUSE:
        	while (!(this.operatorStatus == OperatorControlStatus.OP_RESUME)) { 
        		// just wait to continue
          }
          break;
        case OP_STOP:
          return;
        case OP_INIT:
        case OP_STEP:
        case OP_RESUME:
        case OP_KILL:
        	break;
      }

      // do one step
  		this.fireOperatorExecutionProgressEvent(
  				new ALDOperatorExecutionProgressEvent(this, 
 						operatorID + " iteration = " + loopCounter));
      try {
      	Thread.sleep(500);
      } catch (InterruptedException e) {
      	// just ignore the exception
      }

      // increment loop counter
      ++loopCounter;
    }
    return;
  }

}

/*BEGIN_MITOBO_ONLINE_HELP

This operator demonstrates the capabilities of controllable operators in Alida.
<p>
Such operators allow for user interaction during execution, i.e. can be paused
and interrupted.

END_MITOBO_ONLINE_HELP*/
