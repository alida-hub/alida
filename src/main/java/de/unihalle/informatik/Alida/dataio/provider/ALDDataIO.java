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

import java.util.Collection;

import de.unihalle.informatik.Alida.dataio.ALDDataIOManager;

/**
 * This is the interface for Alida's dataIO provider.
 * <p>
 * The idea behind is to enable generic reading and writing of parameters
 * of Alida operators according to their type. Automatic reading and 
 * writing requires a well-defined interface of readers and writers, actually
 * defined by interfaces extending this interface. These interfaces define the
 * signatures of methods to be used.
 * Every DataIO provider to be used 
 * with Alida in an 
 * automatic fashion has to implement this interface. 
 * <p>
 * Typically these read and write methods are not invoked directly, rather using
 * the ALDDataIOManager corresponding to the concrete interface.
 * <p>
 * 
 * @author moeller
 * @see ALDDataIOManager
 *
 */
public interface ALDDataIO {

    /**
     * Interface method to announce all classes handled by this provider.
     * 
     * @return  Collection of classes provided
     */
    public Collection<Class<?>> providedClasses();
	
}
