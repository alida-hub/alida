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

package de.unihalle.informatik.Alida.dataconverter;

import static org.junit.Assert.*;

import java.util.Collection;
import java.util.LinkedList;
import java.util.Vector;

import org.junit.Before;
import org.junit.Test;

import de.unihalle.informatik.Alida.exceptions.ALDDataConverterException;
import de.unihalle.informatik.Alida.exceptions.ALDOperatorException;

/**
 * JUnit test class for {@link ALDParametrizedClassDataIOSwing}.
 * 
 * @author moeller
 */
public class TestALDVectorNativeArrayConverter {

	/**
	 * Object instance to test.
	 */
	private ALDVectorNativeArrayConverter converterObj;
	
	/**
	 * Set of expected source classes.
	 */
	private Collection<Class<?>> expectedSourceClasses;

	/**
	 * Set of expected target classes.
	 */
	private Collection<Class<?>> expectedTargetClasses;
	
	/**
	 * Fixture.
	 */
	@Before
	public void initTestClass() { 
		try {
	    this.converterObj = new ALDVectorNativeArrayConverter();
    } catch (ALDOperatorException e) {
    	System.err.println("[TestALDNativeArrayVectorConverter] " +
    			"Call to converter constructor failed!");
	    e.printStackTrace();
    }
		this.expectedSourceClasses = new LinkedList<Class<?>>();
		this.expectedSourceClasses.add( Vector.class);
		this.expectedTargetClasses = new LinkedList<Class<?>>();
		this.expectedTargetClasses.add( Boolean[].class);
		this.expectedTargetClasses.add( Byte[].class);
		this.expectedTargetClasses.add( Double[].class);
		this.expectedTargetClasses.add( Float[].class);
		this.expectedTargetClasses.add( Integer[].class);
		this.expectedTargetClasses.add( Short[].class);
		this.expectedTargetClasses.add( String[].class);
		this.expectedTargetClasses.add( boolean[].class);
		this.expectedTargetClasses.add( byte[].class);
		this.expectedTargetClasses.add( double[].class);
		this.expectedTargetClasses.add( float[].class);
		this.expectedTargetClasses.add( int[].class);
		this.expectedTargetClasses.add( short[].class);
	}
	
	/**
	 * Test if all expected source classes are really supported.
	 */
/*	@Test
	public void testSupportedSourceClasses() {

		Collection<Class<?>> sourceClasses = this.converterObj.sourceClasses();
		for (Class<?> c: this.expectedSourceClasses) {
			assertTrue("Expected source class " + c + " not found!",
					sourceClasses.contains(c));
		}
	}
*/
	/**
	 * Test if all expected target classes are really supported.
	 */
/*	@Test
	public void testSupportedTargetClasses() {

		Collection<Class<?>> targetClasses = this.converterObj.targetClasses();
		for (Class<?> c: this.expectedTargetClasses) {
			assertTrue("Expected target class " + c + " not found!",
					targetClasses.contains(c));
		}
	}
*/
	/**
	 * Test conversion functionality.
	 */
//	@SuppressWarnings("null")
//  @Test
//	public void testArrayConversion() {
//		Vector<Boolean> testVecBoolean = new Vector<Boolean>();
//		testVecBoolean.add(new Boolean(true));
//		testVecBoolean.add(new Boolean(false));
//		testVecBoolean.add(new Boolean(true));
//		// Boolean[]
//		try {
//	    Object resObj= this.converterObj.convert(testVecBoolean,Boolean[].class);
//	    assertTrue("Result object is null!", resObj != null);
//	    assertTrue("Result object has wrong type!", 
//	    		resObj.getClass().equals(Boolean[].class));
//	    Boolean[] resArray = (Boolean[])resObj;
//	    assertTrue("Result array has wrong size!", resArray.length == 3);
//	    for (int i=0; i<testVecBoolean.size(); ++i) {
//	    	assertTrue("Mismatch at position " + i + ", expected " + 
//	    			testVecBoolean.elementAt(i) + " - got " + resArray[i] + "...",
//	    			testVecBoolean.elementAt(i).equals(resArray[i]));
//	    }
//    } catch (ALDDataConverterException e) {
//    	fail("Did not expect an exception to be thrown on conversion...");
//	    e.printStackTrace();
//    }
//		// boolean[]
//		try {
//	    Object resObj= this.converterObj.convert(testVecBoolean,boolean[].class);
//	    assertTrue("Result object is null!", resObj != null);
//	    assertTrue("Result object has wrong type!", 
//	    		resObj.getClass().equals(boolean[].class));
//	    boolean[] resArray = (boolean[])resObj;
//	    assertTrue("Result array has wrong size!", resArray.length == 3);
//	    for (int i=0; i<testVecBoolean.size(); ++i) {
//	    	assertTrue("Mismatch at position " + i + ", expected " + 
//	    			testVecBoolean.elementAt(i) + " - got " + resArray[i] + "...",
//	    			testVecBoolean.elementAt(i).equals(new Boolean(resArray[i])));
//	    }
//    } catch (ALDDataConverterException e) {
//    	fail("Did not expect an exception to be thrown on conversion...");
//	    e.printStackTrace();
//    }
//
//		Vector<Byte> testVecByte = new Vector<Byte>();
//		testVecByte.add(new Byte((byte) 1));
//		testVecByte.add(new Byte((byte) 2));
//		testVecByte.add(new Byte((byte) 3));
//		// Byte[]
//		try {
//	    Object resObj= this.converterObj.convert(testVecByte, Byte[].class);
//	    assertTrue("Result object is null!", resObj != null);
//	    assertTrue("Result object has wrong type!", 
//	    		resObj.getClass().equals(Byte[].class));
//	    Byte[] resArray = (Byte[])resObj;
//	    assertTrue("Result array has wrong size!", resArray.length == 3);
//	    for (int i=0; i<testVecByte.size(); ++i) {
//	    	assertTrue("Mismatch at position " + i + ", expected " + 
//	    			testVecByte.elementAt(i) + " - got " + resArray[i] + "...",
//	    			testVecByte.elementAt(i).equals(resArray[i]));
//	    }
//    } catch (ALDDataConverterException e) {
//    	fail("Did not expect an exception to be thrown on conversion...");
//	    e.printStackTrace();
//    }
//		// byte[]
//		try {
//	    Object resObj= this.converterObj.convert(testVecByte, byte[].class);
//	    assertTrue("Result object is null!", resObj != null);
//	    assertTrue("Result object has wrong type!", 
//	    		resObj.getClass().equals(byte[].class));
//	    byte[] resArray = (byte[])resObj;
//	    assertTrue("Result array has wrong size!", resArray.length == 3);
//	    for (int i=0; i<testVecByte.size(); ++i) {
//	    	assertTrue("Mismatch at position " + i + ", expected " + 
//	    			testVecByte.elementAt(i) + " - got " + resArray[i] + "...",
//	    			testVecByte.elementAt(i).equals(new Byte(resArray[i])));
//	    }
//    } catch (ALDDataConverterException e) {
//    	fail("Did not expect an exception to be thrown on conversion...");
//	    e.printStackTrace();
//    }
//		
//		Vector<Double> testVecDouble = new Vector<Double>();
//		testVecDouble.add(new Double(1.0));
//		testVecDouble.add(new Double(2.0));
//		testVecDouble.add(new Double(3.0));
//		// Double[]
//		try {
//	    Object resObj= this.converterObj.convert(testVecDouble, Double[].class);
//	    assertTrue("Result object is null!", resObj != null);
//	    assertTrue("Result object has wrong type!", 
//	    		resObj.getClass().equals(Double[].class));
//	    Double[] resArray = (Double[])resObj;
//	    assertTrue("Result array has wrong size!", resArray.length == 3);
//	    for (int i=0; i<testVecDouble.size(); ++i) {
//	    	assertTrue("Mismatch at position " + i + ", expected " + 
//	    			testVecDouble.elementAt(i) + " - got " + resArray[i] + "...",
//	    			testVecDouble.elementAt(i).equals(resArray[i]));
//	    }
//    } catch (ALDDataConverterException e) {
//    	fail("Did not expect an exception to be thrown on conversion...");
//	    e.printStackTrace();
//    }
//		// double[]
//		try {
//	    Object resObj= this.converterObj.convert(testVecDouble, double[].class);
//	    assertTrue("Result object is null!", resObj != null);
//	    assertTrue("Result object has wrong type!", 
//	    		resObj.getClass().equals(double[].class));
//	    double[] resArray = (double[])resObj;
//	    assertTrue("Result array has wrong size!", resArray.length == 3);
//	    for (int i=0; i<testVecDouble.size(); ++i) {
//	    	assertTrue("Mismatch at position " + i + ", expected " + 
//	    			testVecDouble.elementAt(i) + " - got " + resArray[i] + "...",
//	    			testVecDouble.elementAt(i).equals(new Double(resArray[i])));
//	    }
//    } catch (ALDDataConverterException e) {
//    	fail("Did not expect an exception to be thrown on conversion...");
//	    e.printStackTrace();
//    }
//
//		Vector<Float> testVecFloat = new Vector<Float>();
//		testVecFloat.add(new Float(1.0));
//		testVecFloat.add(new Float(2.0));
//		testVecFloat.add(new Float(3.0));
//		// Float[]
//		try {
//	    Object resObj= this.converterObj.convert(testVecFloat, Float[].class);
//	    assertTrue("Result object is null!", resObj != null);
//	    assertTrue("Result object has wrong type!", 
//	    		resObj.getClass().equals(Float[].class));
//	    Float[] resArray = (Float[])resObj;
//	    assertTrue("Result array has wrong size!", resArray.length == 3);
//	    for (int i=0; i<testVecFloat.size(); ++i) {
//	    	assertTrue("Mismatch at position " + i + ", expected " + 
//	    			testVecFloat.elementAt(i) + " - got " + resArray[i] + "...",
//	    			testVecFloat.elementAt(i).equals(resArray[i]));
//	    }
//    } catch (ALDDataConverterException e) {
//    	fail("Did not expect an exception to be thrown on conversion...");
//	    e.printStackTrace();
//    }
//		// float[]
//		try {
//	    Object resObj= this.converterObj.convert(testVecFloat, float[].class);
//	    assertTrue("Result object is null!", resObj != null);
//	    assertTrue("Result object has wrong type!", 
//	    		resObj.getClass().equals(float[].class));
//	    float[] resArray = (float[])resObj;
//	    assertTrue("Result array has wrong size!", resArray.length == 3);
//	    for (int i=0; i<testVecFloat.size(); ++i) {
//	    	assertTrue("Mismatch at position " + i + ", expected " + 
//	    			testVecFloat.elementAt(i) + " - got " + resArray[i] + "...",
//	    			testVecFloat.elementAt(i).equals(new Float(resArray[i])));
//	    }
//    } catch (ALDDataConverterException e) {
//    	fail("Did not expect an exception to be thrown on conversion...");
//	    e.printStackTrace();
//    }
//
//		Vector<Integer> testVecInteger = new Vector<Integer>();
//		testVecInteger.add(new Integer(1));
//		testVecInteger.add(new Integer(2));
//		testVecInteger.add(new Integer(3));
//		// Integer[]
//		try {
//	    Object resObj= this.converterObj.convert(testVecInteger, Integer[].class);
//	    assertTrue("Result object is null!", resObj != null);
//	    assertTrue("Result object has wrong type!", 
//	    		resObj.getClass().equals(Integer[].class));
//	    Integer[] resArray = (Integer[])resObj;
//	    assertTrue("Result array has wrong size!", resArray.length == 3);
//	    for (int i=0; i<testVecInteger.size(); ++i) {
//	    	assertTrue("Mismatch at position " + i + ", expected " + 
//	    			testVecInteger.elementAt(i) + " - got " + resArray[i] + "...",
//	    			testVecInteger.elementAt(i).equals(resArray[i]));
//	    }
//    } catch (ALDDataConverterException e) {
//    	fail("Did not expect an exception to be thrown on conversion...");
//	    e.printStackTrace();
//    }
//		// int[]
//		try {
//	    Object resObj= this.converterObj.convert(testVecInteger, int[].class);
//	    assertTrue("Result object is null!", resObj != null);
//	    assertTrue("Result object has wrong type!", 
//	    		resObj.getClass().equals(int[].class));
//	    int[] resArray = (int[])resObj;
//	    assertTrue("Result array has wrong size!", resArray.length == 3);
//	    for (int i=0; i<testVecInteger.size(); ++i) {
//	    	assertTrue("Mismatch at position " + i + ", expected " + 
//	    			testVecInteger.elementAt(i) + " - got " + resArray[i] + "...",
//	    			testVecInteger.elementAt(i).equals(new Integer(resArray[i])));
//	    }
//    } catch (ALDDataConverterException e) {
//    	fail("Did not expect an exception to be thrown on conversion...");
//	    e.printStackTrace();
//    }
//
//		Vector<Short> testVecShort = new Vector<Short>();
//		testVecShort.add(new Short((short) 1));
//		testVecShort.add(new Short((short) 2));
//		testVecShort.add(new Short((short) 3));
//		// Short[]
//		try {
//	    Object resObj = this.converterObj.convert(testVecShort, Short[].class);
//	    assertTrue("Result object is null!", resObj != null);
//	    assertTrue("Result object has wrong type!", 
//	    		resObj.getClass().equals(Short[].class));
//	    Short[] resArray = (Short[])resObj;
//	    assertTrue("Result array has wrong size!", resArray.length == 3);
//	    for (int i=0; i<testVecShort.size(); ++i) {
//	    	assertTrue("Mismatch at position " + i + ", expected " + 
//	    			testVecShort.elementAt(i) + " - got " + resArray[i] + "...",
//	    			testVecShort.elementAt(i).equals(resArray[i]));
//	    }
//    } catch (ALDDataConverterException e) {
//    	fail("Did not expect an exception to be thrown on conversion...");
//	    e.printStackTrace();
//    }
//		// short[]
//		try {
//	    Object resObj = this.converterObj.convert(testVecShort, short[].class);
//	    assertTrue("Result object is null!", resObj != null);
//	    assertTrue("Result object has wrong type!", 
//	    		resObj.getClass().equals(short[].class));
//	    short[] resArray = (short[])resObj;
//	    assertTrue("Result array has wrong size!", resArray.length == 3);
//	    for (int i=0; i<testVecShort.size(); ++i) {
//	    	assertTrue("Mismatch at position " + i + ", expected " + 
//	    			testVecShort.elementAt(i) + " - got " + resArray[i] + "...",
//	    			testVecShort.elementAt(i).equals(new Short(resArray[i])));
//	    }
//    } catch (ALDDataConverterException e) {
//    	fail("Did not expect an exception to be thrown on conversion...");
//	    e.printStackTrace();
//    }
//
//		// String[]
//		Vector<String> testVecString = new Vector<String>();
//		testVecString.add(new String("abc"));
//		testVecString.add(new String("def"));
//		testVecString.add(new String("ghi"));
//		try {
//	    Object resObj= this.converterObj.convert(testVecString, String[].class);
//	    assertTrue("Result object is null!", resObj != null);
//	    assertTrue("Result object has wrong type!", 
//	    		resObj.getClass().equals(String[].class));
//	    String[] resArray = (String[])resObj;
//	    assertTrue("Result array has wrong size!", resArray.length == 3);
//	    for (int i=0; i<testVecString.size(); ++i) {
//	    	assertTrue("Mismatch at position " + i + ", expected " + 
//	    			testVecString.elementAt(i) + " - got " + resArray[i] + "...",
//	    			testVecString.elementAt(i).equals(resArray[i]));
//	    }
//    } catch (ALDDataConverterException e) {
//    	fail("Did not expect an exception to be thrown on conversion...");
//	    e.printStackTrace();
//    }
//	}
}