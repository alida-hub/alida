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

package de.unihalle.informatik.Alida.operator.events;

import de.unihalle.informatik.Alida.operator.ALDOperator;


/**
 * Event type to reflect progress of operator execution.
 * <code>eventMessage</code> contains current execution progress.
 * 
 * @author posch
 */
public class ALDOperatorExecutionProgressEvent extends ALDEvent {
	
	ALDOperator originatingOperator;

	/**
	 * @return the originatingOperator
	 */
	public ALDOperator getOriginatingOperator() {
		return originatingOperator;
	}

	/**
	 * Constructor with message.
	 *
	 * @param s		Source object of the event.
	 * @param msg	Description of current execution progress.
	 */
	public ALDOperatorExecutionProgressEvent(Object s, String msg) {
		super(s,msg);
		try {
			this.originatingOperator = (ALDOperator) s;
		} catch (Exception e) {

		}
	}

	/**
	 * Constructor with message.
	 *
	 * @param s		Source object of the event.
	 * @param msg	Description of current execution progress.
	 */
	public ALDOperatorExecutionProgressEvent(Object s, String msg, ALDOperator originatingOperator) {
		super(s,msg);
		try {
			this.originatingOperator = originatingOperator;
		} catch (Exception e) {

		}
	}

	/**
	 * @return the description of current execution progress
	 */
	public String getExecutionProgressDescr() {
		return getEventMessage();
	}
}
