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

import de.unihalle.informatik.Alida.dataio.ALDDataIOManagerXmlbeans;
import de.unihalle.informatik.Alida.demo.ALDCalcMeanArray;
import de.unihalle.informatik.Alida.demo.ExperimentalData;
import de.unihalle.informatik.Alida.exceptions.ALDDataIOManagerException;
import de.unihalle.informatik.Alida.exceptions.ALDDataIOProviderException;
import de.unihalle.informatik.Alida.exceptions.ALDOperatorException;
import de.unihalle.informatik.Alida.workflows.ALDWorkflowDataIOXmlbeans;

/**
 * JUnit test class for {@link ALDWorkflowDataIOXmlbeans}.
 * 
 * @author posch
 */
public class TestALDBasicXmlbeans {
	
	File tmpFile;
	boolean debug = false;
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
		int[] intArray = new int[length];
		for ( int i = 0 ; i < length ; i++)
			intArray[i] = rndGen.nextInt();
		ALDDataIOManagerXmlbeans.writeXml( tmpFile, intArray);

		int[] intArrayIn = (int[]) ALDDataIOManagerXmlbeans.readXml(tmpFile, intArray.getClass());
		
		if ( debug ) {
			System.out.println("int array of lenght " + intArrayIn.length + 
					" read = " + Arrays.toString(intArrayIn));
		}
		
		assertTrue("Got different int[] back", Arrays.equals(intArray, intArrayIn));
	}
	
	/**
	 * Test if we can write and read back 1D Double array
	 * 
	 * @throws ALDOperatorException 
	 * @throws ALDDataIOManagerException 
	 * @throws ALDDataIOProviderException 
	 * @throws IOException 
	 * @throws XmlException 
	 */
	@Test
	public void testDoubleArray1D() 
			throws IOException, ALDDataIOProviderException, ALDDataIOManagerException, XmlException  {

		
		Double[] doubleArray = createDouble1D();
		ALDDataIOManagerXmlbeans.writeXml( tmpFile, doubleArray);

		Double[] doubleArrayIn = (Double[]) ALDDataIOManagerXmlbeans.readXml(tmpFile, doubleArray.getClass());
		
		if ( debug ) {
			System.out.println("int array of lenght " + doubleArrayIn.length + 
					" read = " + Arrays.toString(doubleArrayIn));
		}
		
		assertTrue("Got different int[] back", Arrays.equals(doubleArray, doubleArrayIn));
	}
	
	/**
	 * Test if we can write and read back 2D Double array
	 * 
	 * @throws ALDOperatorException 
	 * @throws ALDDataIOManagerException 
	 * @throws ALDDataIOProviderException 
	 * @throws IOException 
	 * @throws XmlException 
	 */
	@Test
	public void testDoubleArray2D() 
			throws IOException, ALDDataIOProviderException, ALDDataIOManagerException, XmlException  {

		Double[][] doubleArray = createDouble2D();		
		ALDDataIOManagerXmlbeans.writeXml( tmpFile, doubleArray);
		Double[][] doubleArrayIn = (Double[][]) ALDDataIOManagerXmlbeans.readXml(tmpFile, doubleArray.getClass());
		
		if ( debug ) {
			System.out.println("int array of lenght " + doubleArrayIn.length + 
					" read = " + Arrays.toString(doubleArrayIn));
		}
		
		assertTrue("Got different Double[][] back", isEqual(doubleArray, doubleArrayIn));
		
	}
	
	public static enum MyEnum {
		foo,
		hello,
		world
	};


	/**
	 * Test if we can write and read back an enum
	 *  
	 * @throws ALDOperatorException 
	 * @throws ALDDataIOManagerException 
	 * @throws ALDDataIOProviderException 
	 * @throws IOException 
	 * @throws XmlException 
	 */
	@Test
	public void testEnum() 
			throws IOException, ALDDataIOProviderException, ALDDataIOManagerException, XmlException  {

		
		MyEnum myEnum = MyEnum.values()[rndGen.nextInt(MyEnum.values().length)];
		ALDDataIOManagerXmlbeans.writeXml( tmpFile, myEnum);
		MyEnum myEnumIn = (MyEnum) ALDDataIOManagerXmlbeans.readXml(tmpFile, myEnum.getClass());
		
		if( debug )
			System.out.println("enum read = " + myEnumIn);
		
		assertTrue("Got different enum back", myEnum == myEnumIn);
	}
	
	/**
	 * Test if we can write and read back a string
	 *  
	 * @throws ALDOperatorException 
	 * @throws ALDDataIOManagerException 
	 * @throws ALDDataIOProviderException 
	 * @throws IOException 
	 * @throws XmlException 
	 */
	@Test
	public void testString() 
			throws IOException, ALDDataIOProviderException, ALDDataIOManagerException, XmlException  {

		String str = createString();
		ALDDataIOManagerXmlbeans.writeXml( tmpFile, str);
		String strIn = (String) ALDDataIOManagerXmlbeans.readXml(tmpFile, String.class);
		
		if( debug )
			System.out.println("string read = " + strIn);
		
		assertTrue("Got different string back", str.equals(strIn));
	}
	
	/**
	 * Test if we can write and read back a parametrized class
	 *  
	 * @throws ALDOperatorException 
	 * @throws ALDDataIOManagerException 
	 * @throws ALDDataIOProviderException 
	 * @throws IOException 
	 * @throws XmlException 
	 */
	@Test
	public void testParametrizedClass() 
			throws IOException, ALDDataIOProviderException, ALDDataIOManagerException, XmlException  {

		Double[][] doubleArray = createDouble2D();

		ExperimentalData parClassObj = new ExperimentalData("test experiment", doubleArray, false);	
		ALDDataIOManagerXmlbeans.writeXml( tmpFile, parClassObj);
		ExperimentalData parClassObjIn = (ExperimentalData) ALDDataIOManagerXmlbeans.readXml(tmpFile, parClassObj.getClass());
		
		if( debug ) {
			System.out.println("experimental data read = ");
			parClassObjIn.print();
		}
		
		assertTrue("Got different experimental data back", 
					parClassObj.getDescription().equals(parClassObjIn.getDescription()) &&
					isEqual(parClassObj.getData(), parClassObjIn.getData()) &&
					parClassObj.isNormalized() == parClassObjIn.isNormalized());
	}
	
	/**
	 * Test if we can write and read back a LinkedList
	 *  
	 * @throws ALDOperatorException 
	 * @throws ALDDataIOManagerException 
	 * @throws ALDDataIOProviderException 
	 * @throws IOException 
	 * @throws XmlException 
	 */
	@Test
	public void testLinkedList() 
			throws IOException, ALDDataIOProviderException, ALDDataIOManagerException, XmlException  {

		Collection<String> llStrings = new LinkedList<String>();
		for ( int i = 0 ; i < rndGen.nextInt(20)+1 ; i++) {
			llStrings.add( createString());
		}
		
		ALDDataIOManagerXmlbeans.writeXml( tmpFile, llStrings);
		@SuppressWarnings("unchecked")
		Collection<String> llStringsIn = (Collection<String>)ALDDataIOManagerXmlbeans.readXml(tmpFile, llStrings.getClass());

		if ( debug ) {
			System.out.println("llStrings read = ");

			for (String s : llStringsIn) 
				System.out.println("  " + s);
		}

		
		assertTrue("Got different linked lists back", 
					llStrings.equals(llStringsIn));
	}
	
	/**
	 * Test if we can write and read back an operator of type ALDCalcMeanArray
	 *  
	 * @throws ALDOperatorException 
	 * @throws ALDDataIOManagerException 
	 * @throws ALDDataIOProviderException 
	 * @throws IOException 
	 * @throws XmlException 
	 */
	@Test
	public void testALDCalcMeanArray() 
			throws IOException, ALDDataIOProviderException, ALDDataIOManagerException, XmlException, ALDOperatorException  {

		ALDCalcMeanArray meanOp = new ALDCalcMeanArray( createDouble1D());
		meanOp.setVerbose(true);
		meanOp.setParameter("doMeanFree", true);
		
		ALDDataIOManagerXmlbeans.writeXml( tmpFile, meanOp);
		ALDCalcMeanArray meanOpIn = (ALDCalcMeanArray) ALDDataIOManagerXmlbeans.readXml(tmpFile, meanOp.getClass());

		if ( debug ) {
			meanOpIn.print();
		}

		assertTrue("Got different operators back", 
					meanOp.getVerbose().equals(meanOpIn.getVerbose()) &&
					Arrays.equals( ((Double[])meanOp.getParameter("data")), ((Double[])meanOpIn.getParameter("data"))) &&
					((Boolean)meanOp.getParameter("doMeanFree")).equals((Boolean)meanOpIn.getParameter("doMeanFree")));
	}
	
	// =========================

	private boolean isEqual( Double[][] a, Double[][] b) {
		if ( a.length != b.length ) {
			System.out.println( "Doubele[][] have diffent length");
			return false;
		}
		
		for ( int i = 0; i < a.length ; i++) {
			if ( ! Arrays.equals(a[i], b[i]) ) {
				System.out.println( "Double[][] element wit index " + i + " is different");
				return false;
			}
		}
		
		return true;
	}

	private String createString() {
		StringBuffer buf = new StringBuffer();
		
		char c;
		for ( int i = 0 ; i < rndGen.nextInt(100) ; i++) {
			c = (char) (0x40 + rndGen.nextInt(0x40));
			buf.append( c);
		}
		
		return new String( buf);
	}
	
	private Double[] createDouble1D() {
		int length = rndGen.nextInt(100) + 1;
		Double[] doubleArray = new Double[length];
		for ( int i = 0 ; i < length ; i++)
			doubleArray[i] = rndGen.nextDouble();

		return doubleArray;
	}
	
	private Double[][] createDouble2D() {
		int length = rndGen.nextInt(30)+1;
		int length2 = rndGen.nextInt(40)+1;

		Double[][] doubleArray = new Double[length][length2];
		for ( int i = 0 ; i < length ; i++)
			for ( int j = 0 ; j < length2 ; j++)
				doubleArray[i][j] = rndGen.nextDouble();

		return doubleArray;
	}
}