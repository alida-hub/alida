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

import java.util.EventObject;
import java.util.Vector;

/**
 * Super class for all events used in `Alida`.
 * 
 * @author moeller
 */
public class ALDEvent extends EventObject {

	/**
	 * Message string, freely configurable.
	 */
	protected String eventMessage;
	
	/**
	 * Default constructor for events.
	 *
	 * @param s	Source object of the event.
	 */
	public ALDEvent(Object s) {
	  super(s);
  }

	/**
	 * Default constructor for events with messages.
	 *
	 * @param s		Source object of the event.
	 * @param msg Event message.
	 */
	public ALDEvent(Object s, String msg) {
	  super(s);
	  this.eventMessage = msg;
  }
	
	/**
	 * Returns individual message string.
	 */
	public String getEventMessage() {
		return this.eventMessage;
	}

	/**
	 * Returns individual message string formatted to a maximal line length.
	 * <p>
	 * Note that the length of a line might be longer than the given length if it
	 * contains no spaces, i.e. cannot be wrapped at an earlier position.
	 * 
	 * @param maxLength		Maximal length of a line.
	 * @return Formatted message string.
	 */
	public String getEventMessage(int maxLength) {
		StringBuffer formattedMsg = new StringBuffer();
		// find all spaces
		int startIndex = 0;
		int nextPos;
		Vector<Integer> spaceIndices = new Vector<Integer>();
		while ((nextPos=this.eventMessage.indexOf(" ", startIndex)) != -1) {
			spaceIndices.add(new Integer(nextPos));
			startIndex = nextPos+1;
		}
		
		// check if spaces were found
		if (spaceIndices.isEmpty())
			return this.eventMessage;
		
		// reformat the text
		startIndex = 0;
		int arrayIndex = 0;
		do {
			nextPos = spaceIndices.elementAt(arrayIndex).intValue();
			if (nextPos-startIndex < maxLength) {
				++arrayIndex;
			}
			else {
				// append current substring 
				// (but be careful if nothing has been added before!)
				if (arrayIndex > 0)	
					--arrayIndex;
				nextPos = spaceIndices.elementAt(arrayIndex).intValue();
				formattedMsg.append(this.eventMessage.substring(startIndex,nextPos));
				formattedMsg.append("\n");
				startIndex = nextPos+1;
				++arrayIndex;
			}
		} while(arrayIndex < spaceIndices.size());
		// append string until space
		if (this.eventMessage.length()-1-startIndex >= maxLength) {
			formattedMsg.append(this.eventMessage.substring(startIndex,nextPos));
			formattedMsg.append("\n");
			startIndex = nextPos+1;
		}
		formattedMsg.append(this.eventMessage.substring(startIndex));
		formattedMsg.append("\n");
		return formattedMsg.toString();
	}
}
