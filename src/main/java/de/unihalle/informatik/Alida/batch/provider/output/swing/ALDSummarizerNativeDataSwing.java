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
import de.unihalle.informatik.Alida.batch.provider.output.swing.ALDBatchOutputSummarizerSwing;
import de.unihalle.informatik.Alida.dataio.ALDDataIOManagerSwing;
import de.unihalle.informatik.Alida.dataio.provider.ALDDataIOSwing;
import de.unihalle.informatik.Alida.exceptions.ALDDataIOManagerException;
import de.unihalle.informatik.Alida.exceptions.ALDDataIOProviderException;
import de.unihalle.informatik.Alida.operator.ALDParameterDescriptor;

import java.util.Collection;
import java.util.LinkedList;
import java.util.Vector;

import javax.swing.JComponent;

/**
 * Implementation of {@link ALDBatchOutputSummarizerSwing} for native data.
 * 
 * @author moeller
 */
@ALDBatchOutputProvider(priority=1)
public class ALDSummarizerNativeDataSwing
	implements ALDBatchOutputSummarizerSwing {
	
	/**
	 * Interface method to announce class for which IO is provided for
	 * field is ignored.
	 * 
	 * @return	Collection of classes provided
	 */
	@Override
  public Collection<Class<?>> providedClasses() {
		LinkedList<Class<?>> classes = new LinkedList<Class<?>>();
		classes.add( String.class);
		classes.add( boolean.class);
		classes.add( byte.class);
		classes.add( double.class);
		classes.add( float.class);
		classes.add( int.class);
		classes.add( long.class);
		classes.add( short.class);
		classes.add( Boolean.class);
		classes.add( Byte.class);
		classes.add( Double.class);
		classes.add( Float.class);
		classes.add( Integer.class);
		classes.add( Long.class);
		classes.add( Short.class);
		return classes;
	}
	
	@Override
	public JComponent writeData(ALDBatchRunResultInfo batchInfo,
																ALDParameterDescriptor descr) {
		Vector<Object> resultDataCollection = batchInfo.getResultDataVec();
		Object refObj = resultDataCollection.elementAt(0);
		Class<?> refObjCl = refObj.getClass();
		int objNum = resultDataCollection.size();
		try {
			if (refObjCl.equals(String.class)) {
				String [] array = new String[objNum];
				for (int i=0;i<objNum;++i)
					array[i] = (String)resultDataCollection.elementAt(0);
				// return data I/O provider for image
				ALDDataIOSwing imageProvider =
						(ALDDataIOSwing)ALDDataIOManagerSwing.getInstance().getProvider(
								String[].class, ALDDataIOSwing.class);
				return imageProvider.writeData(array, descr);
			}
			if (refObjCl.equals(boolean.class)) {
				boolean [] array = new boolean[objNum];
				for (int i=0;i<objNum;++i)
					array[i] = Boolean.valueOf((String)resultDataCollection.elementAt(i)).
							booleanValue();
				// return data I/O provider for image
				ALDDataIOSwing imageProvider =
						(ALDDataIOSwing)ALDDataIOManagerSwing.getInstance().getProvider(
								boolean[].class, ALDDataIOSwing.class);
				return imageProvider.writeData(array, descr);
			}
			if (refObjCl.equals(byte.class)) {
				byte [] array = new byte[objNum];
				for (int i=0;i<objNum;++i)
					array[i] = Byte.valueOf((String)resultDataCollection.elementAt(i)).
							byteValue();
				// return data I/O provider for image
				ALDDataIOSwing imageProvider =
						(ALDDataIOSwing)ALDDataIOManagerSwing.getInstance().getProvider(
								byte[].class, ALDDataIOSwing.class);
				return imageProvider.writeData(array, descr);
			}
			if (refObjCl.equals(double.class)) {
				double [] array = new double[objNum];
				for (int i=0;i<objNum;++i)
					array[i] = Double.valueOf((String)resultDataCollection.elementAt(i)).
							doubleValue();
				// return data I/O provider for image
				ALDDataIOSwing imageProvider =
						(ALDDataIOSwing)ALDDataIOManagerSwing.getInstance().getProvider(
								double[].class, ALDDataIOSwing.class);
				return imageProvider.writeData(array, descr);
			}
			if (refObjCl.equals(float.class)) {
				float [] array = new float[objNum];
				for (int i=0;i<objNum;++i)
					array[i] = Float.valueOf((String)resultDataCollection.elementAt(i)).
							floatValue();
				// return data I/O provider for image
				ALDDataIOSwing imageProvider =
						(ALDDataIOSwing)ALDDataIOManagerSwing.getInstance().getProvider(
								float[].class, ALDDataIOSwing.class);
				return imageProvider.writeData(array, descr);
			}
			if (refObjCl.equals(int.class)) {
				int [] array = new int[objNum];
				for (int i=0;i<objNum;++i)
					array[i] = Integer.valueOf((String)resultDataCollection.elementAt(i)).
							intValue();
				// return data I/O provider for image
				ALDDataIOSwing imageProvider =
						(ALDDataIOSwing)ALDDataIOManagerSwing.getInstance().getProvider(
								int[].class, ALDDataIOSwing.class);
				return imageProvider.writeData(array, descr);
			}
			if (refObjCl.equals(long.class)) {
				long [] array = new long[objNum];
				for (int i=0;i<objNum;++i)
					array[i] = Long.valueOf((String)resultDataCollection.elementAt(i)).
							longValue();
				// return data I/O provider for image
				ALDDataIOSwing imageProvider =
						(ALDDataIOSwing)ALDDataIOManagerSwing.getInstance().getProvider(
								long[].class, ALDDataIOSwing.class);
				return imageProvider.writeData(array, descr);
			}
			if (refObjCl.equals(short.class)) {
				short [] array = new short[objNum];
				for (int i=0;i<objNum;++i)
					array[i] = Short.valueOf((String)resultDataCollection.elementAt(i)).
							shortValue();
				// return data I/O provider for image
				ALDDataIOSwing imageProvider =
						(ALDDataIOSwing)ALDDataIOManagerSwing.getInstance().getProvider(
								short[].class, ALDDataIOSwing.class);
				return imageProvider.writeData(array, descr);
			}
			if (refObjCl.equals(Boolean.class)) {
				Boolean [] array = new Boolean[objNum];
				for (int i=0;i<objNum;++i)
					array[i] = (Boolean)resultDataCollection.elementAt(i);
				// return data I/O provider for image
				ALDDataIOSwing imageProvider =
						(ALDDataIOSwing)ALDDataIOManagerSwing.getInstance().getProvider(
								Boolean[].class, ALDDataIOSwing.class);
				return imageProvider.writeData(array, descr);
			}
			if (refObjCl.equals(Byte.class)) {
				Byte [] array = new Byte[objNum];
				for (int i=0;i<objNum;++i)
					array[i] = (Byte)resultDataCollection.elementAt(i);
				// return data I/O provider for image
				ALDDataIOSwing imageProvider =
						(ALDDataIOSwing)ALDDataIOManagerSwing.getInstance().getProvider(
								Byte[].class, ALDDataIOSwing.class);
				return imageProvider.writeData(array, descr);
			}
			if (refObjCl.equals(Double.class)) {
				Double [] array = new Double[objNum];
				for (int i=0;i<objNum;++i)
					array[i] = (Double)resultDataCollection.elementAt(i);
				// return data I/O provider for image
				ALDDataIOSwing imageProvider =
						(ALDDataIOSwing)ALDDataIOManagerSwing.getInstance().getProvider(
								Double[].class, ALDDataIOSwing.class);
				return imageProvider.writeData(array, descr);
			}
			if (refObjCl.equals(Float.class)) {
				Float [] array = new Float[objNum];
				for (int i=0;i<objNum;++i)
					array[i] = (Float)resultDataCollection.elementAt(i);
				// return data I/O provider for image
				ALDDataIOSwing imageProvider =
						(ALDDataIOSwing)ALDDataIOManagerSwing.getInstance().getProvider(
								Float[].class, ALDDataIOSwing.class);
				return imageProvider.writeData(array, descr);
			}
			if (refObjCl.equals(Integer.class)) {
				Integer [] array = new Integer[objNum];
				for (int i=0;i<objNum;++i)
					array[i] = (Integer)resultDataCollection.elementAt(i);
				// return data I/O provider for image
				ALDDataIOSwing imageProvider =
						(ALDDataIOSwing)ALDDataIOManagerSwing.getInstance().getProvider(
								Integer[].class, ALDDataIOSwing.class);
				return imageProvider.writeData(array, descr);
			}
			if (refObjCl.equals(Long.class)) {
				Long [] array = new Long[objNum];
				for (int i=0;i<objNum;++i)
					array[i] = (Long)resultDataCollection.elementAt(i);
				// return data I/O provider for image
				ALDDataIOSwing imageProvider =
						(ALDDataIOSwing)ALDDataIOManagerSwing.getInstance().getProvider(
								Long[].class, ALDDataIOSwing.class);
				return imageProvider.writeData(array, descr);
			}
			if (refObjCl.equals(Short.class)) {
				Short [] array = new Short[objNum];
				for (int i=0;i<objNum;++i)
					array[i] = (Short)resultDataCollection.elementAt(i);
				// return data I/O provider for image
				ALDDataIOSwing imageProvider =
						(ALDDataIOSwing)ALDDataIOManagerSwing.getInstance().getProvider(
								Short[].class, ALDDataIOSwing.class);
				return imageProvider.writeData(array, descr);
			}
		} catch (ALDDataIOManagerException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ALDDataIOProviderException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}
}
