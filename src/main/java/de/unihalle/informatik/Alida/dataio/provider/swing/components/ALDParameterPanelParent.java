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

package de.unihalle.informatik.Alida.dataio.provider.swing.components;

import de.unihalle.informatik.Alida.operator.*;
import de.unihalle.informatik.Alida.dataio.provider.swing.events.ALDSwingValueChangeListener;
import de.unihalle.informatik.Alida.dataio.provider.swing.events.ALDSwingValueChangeReporter;

/**
 * Parent class for all GUI components making use of {@link ALDParameterPanel}.
 * <p>
 * This class basically enforces the parents to provide a method for accessing
 * operator or object values, respectively, without requiring direct access to
 * the operator or object.
 * 
 * @author moeller
 */
public abstract class ALDParameterPanelParent 
	extends ALDSwingValueChangeReporter implements ALDSwingValueChangeListener {

	/**
	 * Method to allow {@link ALDParameterPanel} to request parameter values.
	 * @param isRequired			Should be true, if parameter is required.
	 * @param isSupplemental	Should be true, if parameter is supplemental.
	 * @param descr						Descriptor linked to requested parameter.
	 * @return	Value of the parameter.
	 */
	protected abstract Object getParameterValue(
		boolean isRequired, boolean isSupplemental,	ALDParameterDescriptor descr);
}
