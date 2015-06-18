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

package de.unihalle.informatik.Alida.dataio.provider.swing.events;

import de.unihalle.informatik.Alida.dataio.provider.swing.components.*;
import de.unihalle.informatik.Alida.operator.ALDParameterDescriptor;
import de.unihalle.informatik.Alida.operator.events.ALDEvent;

/**
 * Event type for reporting changes of values in provider GUI elements.
 * 
 * @author moeller
 */
public class ALDSwingValueChangeEvent extends ALDEvent {

	/**
	 * Descriptor of the (operator) parameter changed, if available.
	 */
	protected ALDParameterDescriptor paramDescr;
	
	/**
	 * Default constructor.
	 *
	 * @param s		Source object of the event, i.e., {@link ALDSwingComponent}.
	 * @param d		Descriptor of associated (operator) parameter.
	 */
	public ALDSwingValueChangeEvent(Object s, ALDParameterDescriptor d) {
	  super(s);
	  this.paramDescr = d;
  }
	
	/**
	 * Default constructor with arguments.
	 *
	 * @param s		Source object of the event, i.e., {@link ALDSwingComponent}.
	 * @param d 	Descriptor of associated (operator) parameter.
	 * @param msg	Freely configurable message.
	 */
	public ALDSwingValueChangeEvent(Object s, 
			ALDParameterDescriptor d, String msg) {
	  super(s);
	  this.paramDescr = d;
	  this.eventMessage = msg;
  }

    /**
     * Get descriptor of associated (operator) parameter.
     * @return      Reference to the descriptor, null if not available.
     */
    public ALDParameterDescriptor getParamDescriptor() {
	return this.paramDescr;
    }

}
