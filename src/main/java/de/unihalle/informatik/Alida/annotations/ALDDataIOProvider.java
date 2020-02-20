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

package de.unihalle.informatik.Alida.annotations;

import de.unihalle.informatik.Alida.dataio.provider.ALDDataIO;
import de.unihalle.informatik.Alida.helpers.*;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.annotation.Documented;

import net.java.sezpoz.Indexable;

/**
 * Annotation for classes acting as Alida data I/O provider and 
 * implementing the ALDDataIO interface.
 *<p>
 * A data provider class NOT annotated with <code>ALDDataIOProvider</code> will not
 * be found as a provider by Alida.
 *
 * @author Stefan Posch
 */


@Retention(RetentionPolicy.SOURCE)
@Target(ElementType.TYPE)
@Indexable(type=ALDDataIO.class)
@Documented

public @interface ALDDataIOProvider {
	/** Priority to resolve cases where more than one DataIO provider is found for one class.
	 *  The provider with largest priority is used, ties are resolved by chance.
     */
	int priority() default 1;
}
