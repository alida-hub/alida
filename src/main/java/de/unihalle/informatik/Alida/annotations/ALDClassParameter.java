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

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.annotation.Documented;

import de.unihalle.informatik.Alida.annotations.Parameter.ExpertMode;

/**
 * Member variables of classes annotated with {@link ALDParametrizedClass} may 
 * be annotated with this annotation to be automatically handled by Alida's 
 * data I/O.
 *
 * @author Stefan Posch
 */


@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
@Documented

public @interface ALDClassParameter {
	/** Suggest order if displaying all parameters of a parametrized class e.g. in a GUI.
	 * Smaller order indicates early appearance.
	 */
	int	dataIOOrder() default 0;
	
	/** Defines a label for the parameter. 
	 * May be used, e.g. in a GUI, to identify the parameter.
	 */
	String label();
	
	/**
	 * Defines a function name to be called after changing the parameter's value.
	 * <p>
	 * The function needs to be available in the parametrized class.
	 */
	String changeValueHook() default "";
	
	/** Define the mode of this parameter to be shown in autamatically generated user interfaces.
	 */
	ExpertMode mode() default 
		de.unihalle.informatik.Alida.annotations.Parameter.ExpertMode.STANDARD;
}
