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

package de.unihalle.informatik.Alida.batch.provider.output.swing;

import de.unihalle.informatik.Alida.annotations.ALDBatchOutputProvider;
import de.unihalle.informatik.Alida.batch.ALDBatchRunResultInfo;
import de.unihalle.informatik.Alida.dataio.ALDDataIOManagerSwing;
import de.unihalle.informatik.Alida.dataio.provider.ALDDataIOSwing;
import de.unihalle.informatik.Alida.exceptions.*;
import de.unihalle.informatik.Alida.operator.ALDParameterDescriptor;

import javax.swing.*;

import java.util.*;

/**
 * Implementation of {@link ALDBatchOutputSummarizerSwing} for native 1D arrays.
 * 
 * @author moeller
 */
@ALDBatchOutputProvider(priority=1)
public class ALDSummarizerNativeArray1DSwing 
	implements ALDBatchOutputSummarizerSwing {

	/**
	 * List of supported classes.
	 */
	private static LinkedList<Class<?>> classes = null;
	
	/**
	 * Default constructor.
	 */
	public ALDSummarizerNativeArray1DSwing() {
		if (classes == null) {
			classes = new LinkedList<Class<?>>();
			ALDSummarizerNativeArray1DSwing.classes.add( Boolean[].class);
			ALDSummarizerNativeArray1DSwing.classes.add( Byte[].class);
			ALDSummarizerNativeArray1DSwing.classes.add( Double[].class);
			ALDSummarizerNativeArray1DSwing.classes.add( Float[].class);
			ALDSummarizerNativeArray1DSwing.classes.add( Integer[].class);
			ALDSummarizerNativeArray1DSwing.classes.add( Short[].class);
			ALDSummarizerNativeArray1DSwing.classes.add( String[].class);
			ALDSummarizerNativeArray1DSwing.classes.add( boolean[].class);
			ALDSummarizerNativeArray1DSwing.classes.add( byte[].class);
			ALDSummarizerNativeArray1DSwing.classes.add( double[].class);
			ALDSummarizerNativeArray1DSwing.classes.add( float[].class);
			ALDSummarizerNativeArray1DSwing.classes.add( int[].class);
			ALDSummarizerNativeArray1DSwing.classes.add( short[].class);
		}
	}
	
	/**
	 * Interface method to announce class for which IO is provided for.
	 * 
	 * @return  Collection of classes provided.
	 */
	@SuppressWarnings("unchecked")
  @Override
	public Collection<Class<?>> providedClasses() {
		return (Collection<Class<?>>)classes.clone();
	}
	
	@Override
	public JComponent writeData(ALDBatchRunResultInfo batchInfo,
			ALDParameterDescriptor descr) {
		
		Vector<Object> resultDataCollection = batchInfo.getResultDataVec();
		if (resultDataCollection == null)
			return null;
		Object refObj = resultDataCollection.elementAt(0);
		if (refObj == null)
			return null;
		Class<?> refObjCl = refObj.getClass();
		int objNum = resultDataCollection.size();

		// setup result array in 2D, for each 1D result one column
    try {
    	if (refObjCl.equals(Boolean[].class)) {
    		int elems = ((Boolean[])refObj).length;
    		Boolean[][] resultField = new Boolean[elems][objNum];
    		int count = -1;
    		for (Object obj: resultDataCollection) {
    			++count;
    			Boolean[] rarray = (Boolean[])obj;
    			for (int i=0; i<elems; ++i) {
    				resultField[i][count] = rarray[i];
    			}
    		}
    		// return data I/O provider for 2D Boolean array
    		ALDDataIOSwing arrayProvider;
    		arrayProvider = 
    			(ALDDataIOSwing)ALDDataIOManagerSwing.getInstance().getProvider(
    				Boolean[][].class, ALDDataIOSwing.class);
    		return arrayProvider.writeData(resultField, descr);
    	}
    	if (refObjCl.equals(Byte[].class)) {
    		int elems = ((Byte[])refObj).length;
    		Byte[][] resultField = new Byte[elems][objNum];
    		int count = -1;
    		for (Object obj: resultDataCollection) {
    			++count;
    			Byte[] rarray = (Byte[])obj;
    			for (int i=0; i<elems; ++i) {
    				resultField[i][count] = rarray[i];
    			}
    		}
    		// return data I/O provider for 2D Byte array
    		ALDDataIOSwing arrayProvider;
    		arrayProvider = 
    			(ALDDataIOSwing)ALDDataIOManagerSwing.getInstance().getProvider(
    				Byte[][].class, ALDDataIOSwing.class);
    		return arrayProvider.writeData(resultField, descr);
    	}
    	if (refObjCl.equals(Double[].class)) {
    		int elems = ((Double[])refObj).length;
    		Double[][] resultField = new Double[elems][objNum];
    		int count = -1;
    		for (Object obj: resultDataCollection) {
    			++count;
    			Double[] rarray = (Double[])obj;
    			for (int i=0; i<elems; ++i) {
    				resultField[i][count] = rarray[i];
    			}
    		}
    		// return data I/O provider for 2D Double array
    		ALDDataIOSwing arrayProvider;
    		arrayProvider = 
    			(ALDDataIOSwing)ALDDataIOManagerSwing.getInstance().getProvider(
    				Double[][].class, ALDDataIOSwing.class);
    		return arrayProvider.writeData(resultField, descr);
    	}
    	if (refObjCl.equals(Float[].class)) {
    		int elems = ((Float[])refObj).length;
    		Float[][] resultField = new Float[elems][objNum];
    		int count = -1;
    		for (Object obj: resultDataCollection) {
    			++count;
    			Float[] rarray = (Float[])obj;
    			for (int i=0; i<elems; ++i) {
    				resultField[i][count] = rarray[i];
    			}
    		}
    		// return data I/O provider for 2D Float array
    		ALDDataIOSwing arrayProvider;
    		arrayProvider = 
    			(ALDDataIOSwing)ALDDataIOManagerSwing.getInstance().getProvider(
    				Float[][].class, ALDDataIOSwing.class);
    		return arrayProvider.writeData(resultField, descr);
    	}
    	if (refObjCl.equals(Integer[].class)) {
    		int elems = ((Integer[])refObj).length;
    		Integer[][] resultField = new Integer[elems][objNum];
    		int count = -1;
    		for (Object obj: resultDataCollection) {
    			++count;
    			Integer[] rarray = (Integer[])obj;
    			for (int i=0; i<elems; ++i) {
    				resultField[i][count] = rarray[i];
    			}
    		}
    		// return data I/O provider for 2D Integer array
    		ALDDataIOSwing arrayProvider;
    		arrayProvider = 
    			(ALDDataIOSwing)ALDDataIOManagerSwing.getInstance().getProvider(
    				Integer[][].class, ALDDataIOSwing.class);
    		return arrayProvider.writeData(resultField, descr);
    	}
    	if (refObjCl.equals(Short[].class)) {
    		int elems = ((Short[])refObj).length;
    		Short[][] resultField = new Short[elems][objNum];
    		int count = -1;
    		for (Object obj: resultDataCollection) {
    			++count;
    			Short[] rarray = (Short[])obj;
    			for (int i=0; i<elems; ++i) {
    				resultField[i][count] = rarray[i];
    			}
    		}
    		// return data I/O provider for 2D Short array
    		ALDDataIOSwing arrayProvider;
    		arrayProvider = 
    			(ALDDataIOSwing)ALDDataIOManagerSwing.getInstance().getProvider(
    				Short[][].class, ALDDataIOSwing.class);
    		return arrayProvider.writeData(resultField, descr);
    	}
    	if (refObjCl.equals(String[].class)) {
    		int elems = ((String[])refObj).length;
    		String[][] resultField = new String[elems][objNum];
    		int count = -1;
    		for (Object obj: resultDataCollection) {
    			++count;
    			String[] rarray = (String[])obj;
    			for (int i=0; i<elems; ++i) {
    				resultField[i][count] = rarray[i];
    			}
    		}
    		// return data I/O provider for 2D String array
    		ALDDataIOSwing arrayProvider;
    		arrayProvider = 
    			(ALDDataIOSwing)ALDDataIOManagerSwing.getInstance().getProvider(
    				String[][].class, ALDDataIOSwing.class);
    		return arrayProvider.writeData(resultField, descr);
    	}
    	if (refObjCl.equals(boolean[].class)) {
    		int elems = ((boolean[])refObj).length;
    		boolean[][] resultField = new boolean[elems][objNum];
    		int count = -1;
    		for (Object obj: resultDataCollection) {
    			++count;
    			boolean[] rarray = (boolean[])obj;
    			for (int i=0; i<elems; ++i) {
    				resultField[i][count] = rarray[i];
    			}
    		}
    		// return data I/O provider for 2D boolean array
    		ALDDataIOSwing arrayProvider;
    		arrayProvider = 
    			(ALDDataIOSwing)ALDDataIOManagerSwing.getInstance().getProvider(
    				boolean[][].class, ALDDataIOSwing.class);
    		return arrayProvider.writeData(resultField, descr);
    	}
    	if (refObjCl.equals(byte[].class)) {
    		int elems = ((byte[])refObj).length;
    		byte[][] resultField = new byte[elems][objNum];
    		int count = -1;
    		for (Object obj: resultDataCollection) {
    			++count;
    			byte[] rarray = (byte[])obj;
    			for (int i=0; i<elems; ++i) {
    				resultField[i][count] = rarray[i];
    			}
    		}
    		// return data I/O provider for 2D byte array
    		ALDDataIOSwing arrayProvider;
    		arrayProvider = 
    			(ALDDataIOSwing)ALDDataIOManagerSwing.getInstance().getProvider(
    				byte[][].class, ALDDataIOSwing.class);
    		return arrayProvider.writeData(resultField, descr);
			}
			if (refObjCl.equals(double[].class)) {
    		int elems = ((double[])refObj).length;
    		double[][] resultField = new double[elems][objNum];
    		int count = -1;
    		for (Object obj: resultDataCollection) {
    			++count;
    			double[] rarray = (double[])obj;
    			for (int i=0; i<elems; ++i) {
    				resultField[i][count] = rarray[i];
    			}
    		}
    		// return data I/O provider for 2D double array
    		ALDDataIOSwing arrayProvider;
    		arrayProvider = 
    			(ALDDataIOSwing)ALDDataIOManagerSwing.getInstance().getProvider(
    				double[][].class, ALDDataIOSwing.class);
    		return arrayProvider.writeData(resultField, descr);
			}
			if (refObjCl.equals(float[].class)) {
    		int elems = ((float[])refObj).length;
    		float[][] resultField = new float[elems][objNum];
    		int count = -1;
    		for (Object obj: resultDataCollection) {
    			++count;
    			float[] rarray = (float[])obj;
    			for (int i=0; i<elems; ++i) {
    				resultField[i][count] = rarray[i];
    			}
    		}
    		// return data I/O provider for 2D float array
    		ALDDataIOSwing arrayProvider;
    		arrayProvider = 
    			(ALDDataIOSwing)ALDDataIOManagerSwing.getInstance().getProvider(
    				float[][].class, ALDDataIOSwing.class);
    		return arrayProvider.writeData(resultField, descr);
			}
			if (refObjCl.equals(int[].class)) {
    		int elems = ((int[])refObj).length;
    		int[][] resultField = new int[elems][objNum];
    		int count = -1;
    		for (Object obj: resultDataCollection) {
    			++count;
    			int[] rarray = (int[])obj;
    			for (int i=0; i<elems; ++i) {
    				resultField[i][count] = rarray[i];
    			}
    		}
    		// return data I/O provider for 2D int array
    		ALDDataIOSwing arrayProvider;
    		arrayProvider = 
    			(ALDDataIOSwing)ALDDataIOManagerSwing.getInstance().getProvider(
    				int[][].class, ALDDataIOSwing.class);
    		return arrayProvider.writeData(resultField, descr);
			}
			if (refObjCl.equals(short[].class)) {
    		int elems = ((short[])refObj).length;
    		short[][] resultField = new short[elems][objNum];
    		int count = -1;
    		for (Object obj: resultDataCollection) {
    			++count;
    			short[] rarray = (short[])obj;
    			for (int i=0; i<elems; ++i) {
    				resultField[i][count] = rarray[i];
    			}
    		}
    		// return data I/O provider for 2D short array
    		ALDDataIOSwing arrayProvider;
    		arrayProvider = 
    			(ALDDataIOSwing)ALDDataIOManagerSwing.getInstance().getProvider(
    				short[][].class, ALDDataIOSwing.class);
    		return arrayProvider.writeData(resultField, descr);
			}
    } catch (ALDDataIOManagerException e) {
    	System.err.println("[ALDSummarizerNativeArray1D] showing data failed!");
    	e.printStackTrace();
    } catch (ALDDataIOProviderException e) {
    	System.err.println("[ALDSummarizerNativeArray1D] showing data failed!");
    	e.printStackTrace();
    }
    return null;
	}	
}