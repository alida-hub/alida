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

package de.unihalle.informatik.Alida.dataio.provider.swing;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;

import de.unihalle.informatik.Alida.dataio.provider.helpers.ALDParametrizedClassDummy;
import de.unihalle.informatik.Alida.dataio.provider.swing.components.ALDSwingComponent;
import de.unihalle.informatik.Alida.exceptions.ALDDataIOProviderException;

/**
 * JUnit test class for {@link ALDParametrizedClassDataIOSwing}.
 * 
 * @author moeller
 */
public class TestALDParametrizedClassDataIOSwing {

	/**
	 * Object supported by provider class to be tested.
	 */
	private ALDParametrizedClassDummy dummyClassObj;
	
	/**
	 * Fixture.
	 */
	@Before
	public void initTestClass() { 
		this.dummyClassObj = new ALDParametrizedClassDummy();
	}
	
	/**
	 * Test if the class really returns the object with which it was initialized.
	 */
	@Test
	public void testInitWithDefaultObject() {

		// some local variables
		Object dataRead;
		ALDSwingComponent guiElement;
		
		// instantiate object of provider class
		ALDParametrizedClassDataIOSwing testObject = 
																				new ALDParametrizedClassDataIOSwing();
		try {
			// first test if you get null if no object was passed on init
			 guiElement =
				testObject.createGUIElement(null,this.dummyClassObj.getClass(),null,null);
			dataRead =
				testObject.readData(null, this.dummyClassObj.getClass(), guiElement);
			assertTrue("Got an object, but expected null!", dataRead == null);
			
			// now hand over an object, return should be non-null
			guiElement = testObject.createGUIElement(null,
										this.dummyClassObj.getClass(), this.dummyClassObj,null);
			// fake the selection
			dataRead = testObject.readData(null, this.dummyClassObj.getClass(), 
																						guiElement);
			assertTrue("Expected an object, but got null!", dataRead != null);
			assertTrue("Expected object handed over, got another...", 
										dataRead != null && dataRead.equals(this.dummyClassObj));
		} catch (ALDDataIOProviderException e) {
			System.out.println("[TestALDParametrizedClassDataIOSwing] caught an " 
					+ "exception upon testing for correct handling of default object!");
			e.printStackTrace();
		}
	}
	
}