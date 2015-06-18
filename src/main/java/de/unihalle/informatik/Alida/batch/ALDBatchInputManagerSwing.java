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

/* 
 * Most recent change(s):
 * 
 * $Rev: 6309 $
 * $Date: 2012-11-23 17:19:53 +0100 (Fr, 23 Nov 2012) $
 * $Author: moeller $
 * 
 */

package de.unihalle.informatik.Alida.batch;

import de.unihalle.informatik.Alida.dataio.provider.swing.components.ALDSwingComponent;
import de.unihalle.informatik.Alida.exceptions.ALDBatchIOException;
import de.unihalle.informatik.Alida.exceptions.ALDBatchIOManagerException;
import de.unihalle.informatik.Alida.exceptions.ALDBatchIOManagerException.ALDBatchIOManagerExceptionType;
import de.unihalle.informatik.Alida.operator.ALDParameterDescriptor;
import de.unihalle.informatik.Alida.batch.provider.input.swing.ALDBatchInputIteratorSwing;

import java.lang.reflect.Field;
import java.util.Iterator;

/**
 * This class implements a batch provider mangager for batch processing
 * in the context of graphical user interfaces.
 * <p>
 * For data input, it essentially looks up the correct provider for GUI-based 
 * execution using the method of its super class and invokes its method.
 * <p>
 * It does its work in collaboration with 
 * {@link de.unihalle.informatik.Alida.batch.provider.input.ALDBatchInputIterator}.
 * 
 * @author moeller
 *
 */
public class ALDBatchInputManagerSwing extends ALDBatchInputManager {

	/**
	 * Levels to enable/disable interactive provider behaviour.
	 * 
	 * @author moeller
	 */
	public static enum ProviderInteractionLevel {
		/**
		 * Providers are allowed to request user interaction without restrictions.
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
	
	/** 
	 * The singleton instance of this class.
	 */
	static final ALDBatchInputManagerSwing instance;

	static {
		instance = new ALDBatchInputManagerSwing();
	}

	/** 
	 * Private constructor which inits the provider map.
	 */
	private ALDBatchInputManagerSwing() {
		this.mapTable = initMapTable(ALDBatchInputIteratorSwing.class);
	}

	/** 
	 * Return the single instance of this class
	 * @return Single instance.
	 */
	public static ALDBatchInputManagerSwing getInstance() {
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
	public void setProviderInteractionLevel(ProviderInteractionLevel level) {
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
	 * Create a gui element which may be used to request the user to input data.
	 * <p>
	 * This element is later forwarded to <code>readData</code> to
	 * extract the data.
	 * 
	 * @param cl		Class of data to be read.
	 * @param obj 	The value of this object to set as inital value.
	 * @return	Graphical element.
	 */
	public ALDSwingComponent createGUIElement(
			Field field, Class<?> cl,	Object obj, ALDParameterDescriptor descr) 
		throws ALDBatchIOException {
		ALDBatchInputIteratorSwing dataLoader = 
				(ALDBatchInputIteratorSwing)getProvider( cl, 
															ALDBatchInputIteratorSwing.class);
		if ( dataLoader != null )
			return dataLoader.createGUIElement(field, cl, obj, descr);
		throw new ALDBatchIOManagerException(
				ALDBatchIOManagerExceptionType.PROVIDER_INSTANTIATION_ERROR, 
				"ALDBatchInputManagerSwing: provider instantiation failed");
	}
	
	/**
	 * Update the value of a parameter in the GUI component.
	 * 
	 * @param field		Related field of parameter to be updated.
	 * @param cl			Class of object to be handled.
	 * @param guiElement	Corresponding GUI element.
	 * @param value		New value to be set.
	 * @throws ALDDataIOException
	 * 
	 * TODO BM: Rückgabe-Wert der Provider sollte bei Fehler nicht Null sein!
	 */
//	public void setValue(
//			Field field, Class<?> cl,	ALDSwingComponent guiElement, Object value) 
//		throws ALDDataIOException {
//		// get a suitable provider
//		ALDDataIOSwing dataProvider = 
//				(ALDDataIOSwing)getProvider( cl, ALDDataIOSwing.class);
//		// ask the provider to update the value
//		if ( dataProvider != null )
//			dataProvider.setValue(field, cl, guiElement, value);
//	}
	
	/**
	 * Reads data of given class from a specified source.
	 * 
	 * @param cl	Class of data to be read.
	 * @param guiElement	gui element from where to read data.
	 * @return	data object read or null if no provider was found
	 * 
	 * TODO BM: Rückgabe-Wert der Provider sollte bei Fehler nicht Null sein!
	 */
	public Iterator<Object> readData(
			Field field, Class<?> cl,	ALDSwingComponent guiElement)	
		throws ALDBatchIOException {
		ALDBatchInputIteratorSwing dataLoader;
		dataLoader = (ALDBatchInputIteratorSwing)getProvider( cl, ALDBatchInputIteratorSwing.class);
		if ( dataLoader != null )
			return dataLoader.readData(field, cl,guiElement);
		throw new ALDBatchIOManagerException(
				ALDBatchIOManagerExceptionType.PROVIDER_INSTANTIATION_ERROR, 
				"ALDBatchInputManagerSwing: provider instantiation failed");
	}
}
