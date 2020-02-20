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

package de.unihalle.informatik.Alida.batch.provider.input.swing;

import de.unihalle.informatik.Alida.dataio.ALDDataIOManagerSwing;
import de.unihalle.informatik.Alida.dataio.provider.swing.components.ALDSwingComponent;
import de.unihalle.informatik.Alida.exceptions.ALDBatchIOProviderException;
import de.unihalle.informatik.Alida.exceptions.ALDBatchIOProviderException.ALDBatchIOProviderExceptionType;
import de.unihalle.informatik.Alida.exceptions.ALDDataIOException;
import de.unihalle.informatik.Alida.operator.ALDParameterDescriptor;
import de.unihalle.informatik.Alida.annotations.ALDBatchInputProvider;
import de.unihalle.informatik.Alida.batch.provider.input.swing.ALDBatchInputIteratorSwing;

import java.lang.reflect.Field;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;

/**
 * Implementation of {@link ALDBatchInputIteratorSwing} for native 1D arrays.
 * 
 * @author moeller
 */
@ALDBatchInputProvider(priority=1)
public class ALDBatchInputNativeArray1DSwing
	implements ALDBatchInputIteratorSwing {
	
	/**
	 * Interface method to announce class for which IO is provided for
	 * field is ignored.
	 * 
	 * @return	Collection of classes provided
	 */
	@Override
  public Collection<Class<?>> providedClasses() {
		LinkedList<Class<?>> classes = new LinkedList<Class<?>>();
		classes.add( Boolean[].class);
		classes.add( Byte[].class);
		classes.add( Double[].class);
		classes.add( Float[].class);
		classes.add( Integer[].class);
		classes.add( Short[].class);
		classes.add( String[].class);
		classes.add( boolean[].class);
		classes.add( byte[].class);
		classes.add( double[].class);
		classes.add( float[].class);
		classes.add( int[].class);
		classes.add( short[].class);
		return classes;
	}
	
//	@Override
//  public Iterator<Object> iterator() {
//		// TODO Auto-generated method stub
//		return null;
//	}

	@Override
	public ALDSwingComponent createGUIElement(Field field, Class<?> cl, 
																		Object obj, ALDParameterDescriptor descr)
		throws ALDBatchIOProviderException {
		try {
			if (cl.equals(Boolean[].class)) {
				return ALDDataIOManagerSwing.getInstance().createGUIElement(
						field, Boolean[][].class, null, descr);
			}
			if (cl.equals(Byte[].class)) {
				return ALDDataIOManagerSwing.getInstance().createGUIElement(
						field, Byte[][].class, null, descr);
			}
			if (cl.equals(Double[].class)) {
				return ALDDataIOManagerSwing.getInstance().createGUIElement(
						field, Double[][].class, null, descr);
			}
			if (cl.equals(Float[].class)) {
				return ALDDataIOManagerSwing.getInstance().createGUIElement(
						field, Float[][].class, null, descr);
			}
			if (cl.equals(Integer[].class)) {
				return ALDDataIOManagerSwing.getInstance().createGUIElement(
						field, Integer[][].class, null, descr);
			}
			if (cl.equals(Short[].class)) {
				return ALDDataIOManagerSwing.getInstance().createGUIElement(
						field, Short[][].class, null, descr);
			}
			if (cl.equals(String[].class)) {
				return ALDDataIOManagerSwing.getInstance().createGUIElement(
						field, String[][].class, null, descr);
			}
			if (cl.equals(boolean[].class)) {
				return ALDDataIOManagerSwing.getInstance().createGUIElement(
						field, boolean[][].class, null, descr);
			}
			if (cl.equals(byte[].class)) {
				return ALDDataIOManagerSwing.getInstance().createGUIElement(
						field, byte[][].class, null, descr);
			}
			if (cl.equals(double[].class)) {
				return ALDDataIOManagerSwing.getInstance().createGUIElement(
						field, double[][].class, null, descr);
			}
			if (cl.equals(float[].class)) {
				return ALDDataIOManagerSwing.getInstance().createGUIElement(
						field, float[][].class, null, descr);
			}
			if (cl.equals(int[].class)) {
				return ALDDataIOManagerSwing.getInstance().createGUIElement(
						field, int[][].class, null, descr);
			}
			if (cl.equals(short[].class)) {
				return ALDDataIOManagerSwing.getInstance().createGUIElement(
						field, short[][].class, null, descr);
			}
			throw new ALDBatchIOProviderException(
					ALDBatchIOProviderExceptionType.UNSPECIFIED_ERROR,
						"[ALDIteratorNativeArray1D] Requested class not supported!");
		} catch (ALDDataIOException e) {
			throw new ALDBatchIOProviderException(
				ALDBatchIOProviderExceptionType.UNSPECIFIED_ERROR,e.getCommentString());
		}
	}

	@Override
	public void setValue(Field field, Class<?> cl, ALDSwingComponent guiElement,
			Object value) throws ALDBatchIOProviderException {
		// TODO Auto-generated method stub

	}
	
	@Override
	public Iterator<Object> readData(Field field, Class<?> cl, 
				ALDSwingComponent guiElement) {
		Class<?> providerClass = null;
		if (cl.equals(Boolean[].class)) {
			providerClass = Boolean[][].class;
		}
		if (cl.equals(Byte[].class)) {
			providerClass = Byte[][].class;
		}
		if (cl.equals(Double[].class)) {
			providerClass = Double[][].class;
		}
		if (cl.equals(Float[].class)) {
			providerClass = Float[][].class;
		}
		if (cl.equals(Integer[].class)) {
			providerClass = Integer[][].class;
		}
		if (cl.equals(Short[].class)) {
			providerClass = Short[][].class;
		}
		if (cl.equals(String[].class)) {
			providerClass = String[][].class;
		}
		if (cl.equals(boolean[].class)) {
			providerClass = boolean[][].class;
		}
		if (cl.equals(byte[].class)) {
			providerClass = byte[][].class;
		}
		if (cl.equals(double[].class)) {
			providerClass = double[][].class;
		}
		if (cl.equals(float[].class)) {
			providerClass = float[][].class;
		}
		if (cl.equals(int[].class)) {
			providerClass = int[][].class;
		}
		if (cl.equals(short[].class)) {
			providerClass = short[][].class;
		}
		try {
			Object array2D = 
					ALDDataIOManagerSwing.getInstance().readData(field, 
																									providerClass, guiElement);
			return new ALDBatchIteratorNativeArray1D(array2D, cl);
		} catch (ALDDataIOException ex) {
			ex.printStackTrace();
		}
		return null;
	}
	
	/**
	 * Iterates row-wise over a 2D array.
	 * 
	 * @author moeller
	 */
	class ALDBatchIteratorNativeArray1D implements Iterator<Object> {

		/**
		 * Index of iterator in set.
		 */
		private int currentIndex = -1;

		/**
		 * Class of elements in array.
		 */
		private Class<?> requestedClass;
		
		/**
		 * Data.
		 */
		private Object dataArray;
		
		/**
		 * Number of rows in array.
		 */
		private int arrayRows;
		
		/**
		 * Default constructor.
		 */
		public ALDBatchIteratorNativeArray1D(Object array2D, Class<?> cl) {
			this.requestedClass = cl;
			this.dataArray = array2D;
			if (cl.equals(Boolean[].class)) {
				this.arrayRows = ((Boolean[][])this.dataArray).length;
			}
			if (cl.equals(Byte[].class)) {
				this.arrayRows = ((Byte[][])this.dataArray).length;
			}
			if (cl.equals(Double[].class)) {
				this.arrayRows = ((Double[][])this.dataArray).length;
			}
			if (cl.equals(Float[].class)) {
				this.arrayRows = ((Float[][])this.dataArray).length;
			}
			if (cl.equals(Integer[].class)) {
				this.arrayRows = ((Integer[][])this.dataArray).length;
			}
			if (cl.equals(Short[].class)) {
				this.arrayRows = ((Short[][])this.dataArray).length;
			}
			if (cl.equals(String[].class)) {
				this.arrayRows = ((String[][])this.dataArray).length;
			}
			if (cl.equals(boolean[].class)) {
				this.arrayRows = ((boolean[][])this.dataArray).length;
			}
			if (cl.equals(byte[].class)) {
				this.arrayRows = ((byte[][])this.dataArray).length;
			}
			if (cl.equals(double[].class)) {
				this.arrayRows = ((double[][])this.dataArray).length;
			}
			if (cl.equals(float[].class)) {
				this.arrayRows = ((float[][])this.dataArray).length;
			}
			if (cl.equals(int[].class)) {
				this.arrayRows = ((int[][])this.dataArray).length;
			}
			if (cl.equals(short[].class)) {
				this.arrayRows = ((short[][])this.dataArray).length;
			}
		}

		@Override
		public boolean hasNext() {
			return ( this.currentIndex < this.arrayRows - 1);
		}

		@Override
		public Object next() {
			this.currentIndex++;
			if (this.requestedClass.equals(Boolean[].class)) {
				return ((Boolean[][])this.dataArray)[this.currentIndex];
			}
			if (this.requestedClass.equals(Byte[].class)) {
				return ((Byte[][])this.dataArray)[this.currentIndex];
			}
			if (this.requestedClass.equals(Double[].class)) {
				return ((Double[][])this.dataArray)[this.currentIndex];
			}
			if (this.requestedClass.equals(Float[].class)) {
				return ((Float[][])this.dataArray)[this.currentIndex];
			}
			if (this.requestedClass.equals(Integer[].class)) {
				return ((Integer[][])this.dataArray)[this.currentIndex];
			}
			if (this.requestedClass.equals(Short[].class)) {
				return ((Short[][])this.dataArray)[this.currentIndex];
			}
			if (this.requestedClass.equals(String[].class)) {
				return ((String[][])this.dataArray)[this.currentIndex];
			}
			if (this.requestedClass.equals(boolean[].class)) {
				return ((boolean[][])this.dataArray)[this.currentIndex];
			}
			if (this.requestedClass.equals(byte[].class)) {
				return ((byte[][])this.dataArray)[this.currentIndex];
			}
			if (this.requestedClass.equals(double[].class)) {
				return ((double[][])this.dataArray)[this.currentIndex];
			}
			if (this.requestedClass.equals(float[].class)) {
				return ((float[][])this.dataArray)[this.currentIndex];
			}
			if (this.requestedClass.equals(int[].class)) {
				return ((int[][])this.dataArray)[this.currentIndex];
			}
			if (this.requestedClass.equals(short[].class)) {
				return ((short[][])this.dataArray)[this.currentIndex];
			}
			return null;
		}

		@Override
		public void remove() {
			// not supported...
		}
	}
}
