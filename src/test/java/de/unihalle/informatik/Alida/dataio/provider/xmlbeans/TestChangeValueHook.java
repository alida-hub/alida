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
import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedList;
import java.util.Random;

import org.apache.xmlbeans.XmlException;
import org.junit.Before;
import org.junit.Test;

import de.unihalle.informatik.Alida.dataio.ALDDataIOManagerCmdline;
import de.unihalle.informatik.Alida.dataio.ALDDataIOManagerXmlbeans;
import de.unihalle.informatik.Alida.demo.ALDCalcMeanArray;
import de.unihalle.informatik.Alida.demo.ExperimentalData1D;
import de.unihalle.informatik.Alida.demo.Extrema1D;
import de.unihalle.informatik.Alida.exceptions.ALDDataIOManagerException;
import de.unihalle.informatik.Alida.exceptions.ALDDataIOProviderException;
import de.unihalle.informatik.Alida.exceptions.ALDOperatorException;
import de.unihalle.informatik.Alida.workflows.ALDWorkflowDataIOXmlbeans;

/**
 * JUnit test class for {@link ALDWorkflowDataIOXmlbeans}.
 * 
 * @author posch
 */
public class TestChangeValueHook {
	
	File tmpFile;
	boolean debug = true;
	Random rndGen = new Random();
	
	/**
	 * Fixture.
	 * @throws IOException 
	 */
	@Before
	public void initTestClass() throws IOException { 
		tmpFile = File.createTempFile("tmp", "xml");

	}
	
	/**
	 * Test if we can write and read back 1D int array
	 * 
	 * @throws ALDOperatorException 
	 * @throws ALDDataIOManagerException 
	 * @throws ALDDataIOProviderException 
	 * @throws IOException 
	 * @throws XmlException 
	 */
	@Test
	public void testIntArray1D() 
			throws IOException, ALDDataIOProviderException, ALDDataIOManagerException, XmlException  {

		
		int length = rndGen.nextInt(100) + 1;
		
		Extrema1D extrema = new Extrema1D();
		for ( int i = 0 ; i < length ; i++) {
			extrema.addPoint( rndGen.nextDouble(), rndGen.nextDouble());
		}
		ALDDataIOManagerXmlbeans.writeXml( tmpFile, extrema);

		Extrema1D extremaBack = (Extrema1D) ALDDataIOManagerXmlbeans.readXml(tmpFile, extrema.getClass());
		
		if ( debug ) {
			System.out.println("extrema of lenght " + extrema.getLength() + 
					" read = " + extremaBack.getLength());
		}
		
		assertTrue("Got different int[] back", isEqual( extrema, extremaBack));
	}
	
	
	// =========================

	private boolean isEqual( Extrema1D a,Extrema1D b) {
		if ( a.getLength() != b.getLength() ) {
			System.out.println( "Extrema1D have diffent length");
			return false;
		}
		
		for ( int i = 0 ; i < a.getLength() ; i++ ) {
			if ( a.getX(i).compareTo( b.getX(i)) != 0 ) {
				System.out.println( "x coordinates of Extrema1D are different  at " + i + ": " +
						a.getX(i) + " vs " + b.getX(i));
				return false;
			}
			if ( a.getY(i).compareTo( b.getY(i)) != 0 ) {
				System.out.println( "y coordinates of Extrema1D are different  at " + i + ": " +
						a.getY(i) + " vs " + b.getY(i));
				return false;
			}

		}
		
		return true;
	}

}