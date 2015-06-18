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

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;

import de.unihalle.informatik.Alida.operator.events.ALDEvent;

/**
 * Class for testing Alida events data structure.
 * 
 * @author moeller
 */
public class TestALDEvent {

	/**
	 * Event object to be tested.
	 */
	private ALDEvent dummyEvent;
	
	/**
	 * Fixture.
	 */
	@Before
	public void initTestClass() {
		// nothing to do here for the moment
	}
	
	/**
	 * Test if event messages are properly formatted.
	 */
	@Test
	public void testEventMessagesWrappingToMaxLineLength() {
		// space positions:      0    5     11    17    23    29    35    41
		String msg = new String("xxxxx xxxxx xxxxx xxxxx xxxxx xxxxx xxxxx xxxxx");
		this.dummyEvent = new ALDEvent(this,msg);
		
		String formattedText;
		String targetFormat;
		
		// line width = 9
		formattedText = this.dummyEvent.getEventMessage(9);
		assertTrue("Message is longer than 9 chars, should contain a newline!",
			formattedText.contains("\n"));
		targetFormat = 
			new String("xxxxx\nxxxxx\nxxxxx\nxxxxx\nxxxxx\nxxxxx\nxxxxx\nxxxxx\n");
		assertTrue("Message is not properly formatted: \n" + 
			"Got:\n" + formattedText + "\nExpected: \n" + targetFormat,
			formattedText.equals(targetFormat));

		// line width = 12
		formattedText = this.dummyEvent.getEventMessage(12);
		targetFormat = 
			new String("xxxxx xxxxx\nxxxxx xxxxx\nxxxxx xxxxx\nxxxxx xxxxx\n");
		assertTrue("Message is not properly formatted: \n" + 
			"Got:\n" + formattedText + "\nExpected: \n" + targetFormat,
			formattedText.equals(targetFormat));

		// line width = 17
		formattedText = this.dummyEvent.getEventMessage(18);
		targetFormat = 
			new String("xxxxx xxxxx xxxxx\nxxxxx xxxxx xxxxx\nxxxxx xxxxx\n");
		assertTrue("Message is not properly formatted: \n" + 
			"Got:\n" + formattedText + "\nExpected: \n" + targetFormat,
			formattedText.equals(targetFormat));
		
		// line that cannot be wrapped, length 40;
		// result should be equal to input
		msg = new String("xxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx");
		this.dummyEvent = new ALDEvent(this,msg);
		formattedText = this.dummyEvent.getEventMessage(20);
		targetFormat = new String("xxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx");
		assertTrue("Message is not properly formatted: \n" + 
			"Got:\n" + formattedText + "\nExpected: \n" + targetFormat,
			formattedText.equals(targetFormat));
		
		// line that cannot be wrapped since space is not among first 20 characters;
		// result should be that first line is overlong, 
		// but string is wrapped at space at position 37
		msg = new String("xxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx xxxx");
		this.dummyEvent = new ALDEvent(this,msg);
		formattedText = this.dummyEvent.getEventMessage(20);
		targetFormat = new String("xxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx\nxxxx\n");
		assertTrue("Message is not properly formatted: \n" + 
			"Got:\n" + formattedText + "\nExpected: \n" + targetFormat,
			formattedText.equals(targetFormat));
	}
}