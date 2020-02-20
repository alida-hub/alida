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

package de.unihalle.informatik.Alida.operator.events;


/**
 * Event type to configure operator execution.
 * 
 * @author moeller
 */
public class ALDConfigurationEvent extends ALDEvent {

	/**
	 * Flag for stepwise execution.
	 */
	protected boolean stepwiseExecution = false;
	
	/**
	 * Size of one step in step-wise execution mode.
	 */
	protected int stepsize = 1;
	
	/**
	 * Constructor.
	 *
	 * @param s			Source object of the event.
	 */
	public ALDConfigurationEvent(Object s) {
	  super(s);
  }
	
	/**
	 * Constructor with message.
	 *
	 * @param s		Source object of the event.
	 * @param msg	Freely configurable message.
	 */
	public ALDConfigurationEvent(Object s, String msg) {
	  super(s,msg);
  }
	
	/**
	 * Set size of steps in step-wise operator execution.
	 * 
	 * @param s Stepsize.
	 */
	public void setStepsize(int s) {
		this.stepsize = s;
	}
	
	/**
	 * Return desired step size.
	 */
	public int getStepSize() {
		return this.stepsize;
	}

	/**
	 * Set flag for step-wise execution.
	 */
	public void enableStepwiseExecution() {
		this.stepwiseExecution = true;
	}
	
	/**
	 * Reset flag for step-wise execution.
	 */
	public void disableStepwiseExecution() {
		this.stepwiseExecution = false;
	}

	/**
	 * Return flag for step-wise execution.
	 */
	public boolean doStepwiseExecution() {
		return this.stepwiseExecution;
	}
}
