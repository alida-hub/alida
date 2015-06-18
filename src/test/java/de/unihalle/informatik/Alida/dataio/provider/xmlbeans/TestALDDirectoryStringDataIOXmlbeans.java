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

package de.unihalle.informatik.Alida.dataio.provider.xmlbeans;

import static org.junit.Assert.*;

import java.io.File;
import java.io.IOException;

import org.apache.xmlbeans.XmlException;
import org.junit.Before;
import org.junit.Test;

import de.unihalle.informatik.Alida.dataio.ALDDataIOManagerXmlbeans;
import de.unihalle.informatik.Alida.datatypes.ALDDirectoryString;
import de.unihalle.informatik.Alida.datatypes.ALDFileString;
import de.unihalle.informatik.Alida.exceptions.ALDDataIOManagerException;
import de.unihalle.informatik.Alida.exceptions.ALDDataIOProviderException;
import de.unihalle.informatik.Alida.exceptions.ALDOperatorException;
import de.unihalle.informatik.Alida.workflows.ALDWorkflowDataIOXmlbeans;

/**
 * JUnit test class for {@link ALDWorkflowDataIOXmlbeans}.
 * 
 * @author posch
 */
public class TestALDDirectoryStringDataIOXmlbeans {

	/**
	 * Fixture.
	 */
	@Before
	public void initTestClass() { 
	}
	
	/**
	 * Test if we can write and read back an directora and file string
	 * 
	 * @throws ALDOperatorException 
	 * @throws ALDDataIOManagerException 
	 * @throws ALDDataIOProviderException 
	 * @throws IOException 
	 * @throws XmlException 
	 */
	@Test
	public void testInitWithDefaultObject() 
			throws ALDOperatorException, ALDDataIOProviderException, ALDDataIOManagerException, IOException, XmlException {
		File tmpFile = File.createTempFile("tmp", "xml");

		ALDDirectoryString dir = new ALDDirectoryString("tmp");
		ALDDataIOManagerXmlbeans.writeXml(tmpFile, dir);
		ALDDirectoryString dirBack = 
				(ALDDirectoryString) ALDDataIOManagerXmlbeans.readXml(tmpFile, ALDDirectoryString.class);

		assertTrue("Expected an object, but got null!", dirBack != null);
		assertTrue("Got different string back", 
				dirBack != null && dirBack.getDirectoryName().equals(dir.getDirectoryName()));

		ALDFileString file = new ALDFileString("tmp");
		ALDDataIOManagerXmlbeans.writeXml(tmpFile, file);
		ALDFileString fileBack = 
				(ALDFileString) ALDDataIOManagerXmlbeans.readXml(tmpFile, ALDFileString.class);

		assertTrue("Expected an object, but got null!", fileBack != null);
		assertTrue("Got different string back", 
				fileBack != null && fileBack.getFileName().equals(file.getFileName()));
}
	
}