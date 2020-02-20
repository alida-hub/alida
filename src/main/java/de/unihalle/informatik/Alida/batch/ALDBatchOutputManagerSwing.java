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

package de.unihalle.informatik.Alida.batch;

import de.unihalle.informatik.Alida.batch.provider.output.swing.ALDBatchOutputSummarizerSwing;
import de.unihalle.informatik.Alida.exceptions.ALDBatchIOException;
import de.unihalle.informatik.Alida.exceptions.ALDBatchIOManagerException;
import de.unihalle.informatik.Alida.exceptions.ALDBatchIOManagerException.ALDBatchIOManagerExceptionType;
import de.unihalle.informatik.Alida.operator.ALDParameterDescriptor;

import java.util.Set;

import javax.swing.*;

/**
 * This class implements a DataIO manager for summarizing batch results 
 * in the context of graphical user interfaces.
 * <p>
 * For data summary, it essentially looks up the correct provider for GUI-based 
 * summaries using the method of its super class and invokes its method.
 * <p>
 * It does its work in collaboration with 
 * {@link de.unihalle.informatik.Alida.batch.provider.output.ALDBatchOutputSummarizer}.
 * 
 * @author moeller
 *
 */
public class ALDBatchOutputManagerSwing extends ALDBatchOutputManager {

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
	 * The singleton instance of this class
	 */
	static final ALDBatchOutputManagerSwing instance;

	static {
		instance = new ALDBatchOutputManagerSwing();
	}

	/** 
	 * Private constructor. 
	 */
	private ALDBatchOutputManagerSwing() {
		this.mapTable = initMapTable(ALDBatchOutputSummarizerSwing.class);
//		Set<Class<?>> keys = this.mapTable.keySet();
//		for (Class<?> c : keys) {
//			System.out.println("Found batch output provider for " + c.toString());
//		}
	}

	/** 
	 * Return the single instance of this class
	 * @return Single instance.
	 */
	public static ALDBatchOutputManagerSwing getInstance() {
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
	 * Interface method for displaying a batch summary in the GUI.
	 * 
	 * @param obj 	Object to be displayed or saved.
	 * @throws ALDDataIOManagerException 
	 */
	public JComponent writeData(ALDBatchRunResultInfo obj, 
																	ALDParameterDescriptor descr) 
			throws ALDBatchIOException {
		// if object is null, there is nothing to write
		if (obj == null)
			throw new ALDBatchIOManagerException(
				ALDBatchIOManagerExceptionType.UNSPECIFIED_ERROR,
				"ALDBatchOutputManagerSwing: batch result info object is null!");
		Class<? extends Object> cl = obj.getClass();
		ALDBatchOutputSummarizerSwing dataWriter;
		dataWriter = (ALDBatchOutputSummarizerSwing)getProvider( cl, 
				ALDBatchOutputSummarizerSwing.class);
		if ( dataWriter != null ) 
			return dataWriter.writeData(obj, descr);
		throw new ALDBatchIOManagerException(
				ALDBatchIOManagerExceptionType.NO_PROVIDER_FOUND,
				"ALDBatchOutputManagerSwing: Provider for " + 
																									cl.toString() + "not found");
	}
}
