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

package de.unihalle.informatik.Alida.dataio.provider;

import java.lang.reflect.Field;

import de.unihalle.informatik.Alida.operator.ALDParameterDescriptor;

/**
 * Class providing default method for initial GUI value handling.
 * <p>
 * As most providers most likely will set the given object value as
 * initial value in the GUI, this method implements the interface
 * method mimicing exactly this behaviour.
 * <p>
 * Providers can extend this class and thereby overcoming the need
 * for implementing the method explicitly 
 * 
 * @author moeller
 */
public abstract class ALDDataIOSwingInitialGUIValueDefaultHandler
	implements ALDDataIOSwing {

	@Override
  public Object getInitialGUIValue(Field field, Class<?> cl, Object obj, 
  		ALDParameterDescriptor descr) {
		return obj;
	}
}
