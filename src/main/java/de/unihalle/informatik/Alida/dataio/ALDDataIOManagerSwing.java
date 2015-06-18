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

package de.unihalle.informatik.Alida.dataio;

import de.unihalle.informatik.Alida.dataio.provider.ALDDataIOSwing;
import de.unihalle.informatik.Alida.dataio.provider.swing.components.ALDSwingComponent;
import de.unihalle.informatik.Alida.exceptions.ALDDataIOException;
import de.unihalle.informatik.Alida.exceptions.ALDDataIOManagerException;
import de.unihalle.informatik.Alida.exceptions.ALDDataIOManagerException.ALDDataIOManagerExceptionType;
import de.unihalle.informatik.Alida.operator.ALDParameterDescriptor;

import java.lang.reflect.Field;

import javax.swing.*;

/**
 * This class implements a DataIO mangager for reading/writing from a 
 * graphical user interface written in Swing.
 * <p>
 * For I/O, it essentially looks up the correct provider for GUI-based 
 * execution using the method of its super class and invokes its method.
 * <p>
 * It does its work in collaboration with 
 * {@link de.unihalle.informatik.Alida.dataio.provider.ALDDataIOSwing}.
 * 
 * @author posch
 *
 */
public class ALDDataIOManagerSwing extends ALDDataIOManager {

	/**
	 * Levels to enable/disable interactive provider behaviour.
	 * 
	 * @author moeller
	 */
	public static enum ProviderInteractionLevel {
		/**
		 * Providers are allowed to request user interaction without 
		 * restrictions.
		 * <p>
		 * This is the default. Restrictions require to change the level.
		 */
		ALL_ALLOWED,
		/**
		 * No user interaction allowed, i.e. no pop-ups or input requests.
		 */
		ALL_FORBIDDEN,
		/**
		 * Only important warnings are allowed to be displayed to the user.
		 */
		WARNINGS_ONLY
	}
	
	/**
	 * Interaction level the providers should obey.
	 */
	private ProviderInteractionLevel provInterLevel =
			ProviderInteractionLevel.ALL_ALLOWED;

	/**
	 * Flag to indicate if change value events should be triggered or not.
	 */
	private boolean triggerValueChangeEvents = true;
	
	/** The singleton instance of this class
	 */
	static final ALDDataIOManagerSwing instance;

	static {
		instance = new ALDDataIOManagerSwing();
	}

	/** private constructor 
	 */
	private ALDDataIOManagerSwing() {
		this.mapTable = initMapTable(ALDDataIOSwing.class);
	}

	/** Return the single instance of this class
	 * @return single instance
	 */
	public static ALDDataIOManagerSwing getInstance() {
		return instance;
	}

	/**
	 * Returns the current level of provider interaction.
	 * @return Current level of provider interaction.
	 */
	public ProviderInteractionLevel getProviderInteractionLevel() {
		return this.provInterLevel;
	}

	/**
	 * Set the level of provider interaction which is allowed.
	 * @param level		New level of interaction providers have to obey.
	 */
	public void setProviderInteractionLevel(
			ProviderInteractionLevel level) {
		this.provInterLevel = level;
	}
    
	/**
	 * Returns value of event trigger flag.
	 * @return	True, if value change events are allowed.
	 */
	public boolean isTriggerValueChangeEvents() {
		return this.triggerValueChangeEvents;
	}
	
	/**
	 * Set the value change event trigger flag.
	 * @param b		New value of the event trigger flag.
	 */
	public void setTriggerValueChangeEvents(boolean b) {
		this.triggerValueChangeEvents = b;
	}
	
	/** 
	 * Method to request initial value a provider sets in GUI.
	 * <p>
	 * Providers are allowed to set specific initial values in GUI 
	 * elements, i.e. change an operators configuration during the build 
	 * process of the GUI. To nevertheless allow for proper configuration 
	 * of operator objects and simulate the initialization procedure in 
	 * advance, this methods allows to request providers for these initial 
	 * values prior to initiating the actual build process.
	 * <p>
	 * Examples for providers setting initial values are for example
	 * providers for images which automatically select available images
	 * as inputs, or providers for numerical wrappers.
	 * <p>
	 * Note that the initial value a provider sets may depend on the 
	 * characteristics of the parameter, accessible via the descriptor,
	 * as well as on the current value of the parameter in the operator 
	 * object. Note that the value object is allowed to be 
	 * <code>null</code> while the descriptor is not.   
	 * 
	 * @param field	Field of the parameter the GUI element is linked to.
	 * @param cl		Class of parameter object to handled.
	 * @param obj 	The current value of the object.
	 * @param descr Descriptor to provide additional information.
	 * @return Initial GUI parameter value.
	 * @throws ALDDataIOException Thrown if no provider could be found.
	 */
	public Object getInitialGUIValue(Field field, Class<?> cl, Object obj, 
			ALDParameterDescriptor descr) 
		throws ALDDataIOException {
		ALDDataIOSwing dataLoader = 
				(ALDDataIOSwing)getProvider( cl, ALDDataIOSwing.class);
		if ( dataLoader != null )
			return dataLoader.getInitialGUIValue(field, cl, obj, descr);
		// no provider could be found
		throw new ALDDataIOManagerException(
				ALDDataIOManagerExceptionType.NO_PROVIDER_FOUND, 
					"[ALDDataIOManagerSwing::getInitialGUIValue] found no proper "
							+ "provider for parameter <" + descr.getName() + ">... ");
	}

	/** 
	 * Create a gui element which may be used to request the user to
	 * input data of class <code>cl</code>.
	 * <p>
	 * This element may later be forwarded to the method
	 * {@link #readData(Field, Class, ALDSwingComponent)} to extract an 
	 * object representig this data.<br>
	 * If no suitable provider is found, the method throws an exception.
	 * <p>
	 * Additionally, an object may be supplied which is assumed to be of 
	 * type <code>cl</code>. If it is non-null its value may be used to 
	 * set up an inital value in the GUI element.
	 * 
	 * @param field	Field of the parameter the GUI element is linked to.
	 * @param cl		Class of data to be read.
	 * @param obj 	The value of this object to set as inital value.
	 * @param descr Optional descriptor to provide additional information.
	 * @return GUI element.
	 * @throws ALDDataIOException Thrown if no provider could be found.
	 */
	public ALDSwingComponent createGUIElement(Field field, Class<?> cl,	
			Object obj, ALDParameterDescriptor descr) 
		throws ALDDataIOException {
		ALDDataIOSwing dataLoader = 
				(ALDDataIOSwing)getProvider( cl, ALDDataIOSwing.class);
		if ( dataLoader != null )
			return dataLoader.createGUIElement(field, cl, obj, descr);
		// no provider could be found
		throw new ALDDataIOManagerException(
				ALDDataIOManagerExceptionType.NO_PROVIDER_FOUND, 
					"[ALDDataIOManagerSwing::createGUIElement] found no suitable "
							+ "provider for parameter <" + descr.getName() + ">... ");
	}
	
	/**
	 * Update the value of a parameter in the GUI component.
	 * 
	 * @param field				Related field of parameter to be updated.
	 * @param cl					Class of object to be handled.
	 * @param guiElement	Corresponding GUI element.
	 * @param value				New value to be set.
	 * @throws ALDDataIOException Thrown in case of failure.
	 */
	public void setValue(Field field, Class<?> cl, 
			ALDSwingComponent guiElement, Object value)
					throws ALDDataIOException {
		// get a suitable provider
		ALDDataIOSwing dataProvider = 
				(ALDDataIOSwing)getProvider( cl, ALDDataIOSwing.class);
		// ask the provider to update the value
		if ( dataProvider != null )
			dataProvider.setValue(field, cl, guiElement, value);
		// no provider could be found
		else
			throw new ALDDataIOManagerException(
					ALDDataIOManagerExceptionType.NO_PROVIDER_FOUND, 
					"[ALDDataIOManagerSwing::setValue] found no suitable provider " 
							+ "for given parameter... ");
	}
	
	/**
	 * Reads data of given class from a specified source.
	 * 
	 * @param field				Field linked to parameter to be read.
	 * @param cl					Class of data to be read.
	 * @param guiElement	GUI element from where to read data.
	 * @return Data object read.
	 * @throws ALDDataIOException Thrown in case of failure.
	 */
	public Object readData(Field field, Class<?> cl, 
			ALDSwingComponent guiElement)	
					throws ALDDataIOException {
		ALDDataIOSwing dataLoader = 
				(ALDDataIOSwing)getProvider( cl, ALDDataIOSwing.class);		
		if ( dataLoader != null )
			return dataLoader.readData(field, cl,guiElement);
		throw new ALDDataIOManagerException(
				ALDDataIOManagerExceptionType.NO_PROVIDER_FOUND, 
				"[ALDDataIOManagerSwing::readData] found no suitable provider " 
						+ "for given parameter... ");
	}
	
	/**
	 * Returns a GUI element visualizing the given parameter object.
	 * <p>
	 * If <code>null</code> is returned, either the given object is 
	 * <code>null</code> or the object may have been displayed otherwise, 
	 * e.g., in its own independent window.
	 * 
	 * @param obj			Object to write or display, respectively.
	 * @param descr 	Optional descriptor for additional information.
	 * @return gui 		Element created.
	 * @throws ALDDataIOException Thrown in case of write failure.
	 */
	public JComponent writeData(Object obj, ALDParameterDescriptor descr) 
			throws ALDDataIOException {
		// if object is null, there is nothing to write
		if (obj == null)
			return null;
		Class<? extends Object> cl = obj.getClass();
		ALDDataIOSwing dataWriter = 
			(ALDDataIOSwing)getProvider( cl, ALDDataIOSwing.class);
		if ( dataWriter != null ) 
			return dataWriter.writeData(obj, descr);
		throw new ALDDataIOManagerException(
				ALDDataIOManagerExceptionType.NO_PROVIDER_FOUND,
				"[ALDDataIOManagerSwing::readData] found no suitable provider " 
						+ "for given parameter <" + descr.getName() + "> of class "
						+ cl.toString() + "... ");
	}
}
