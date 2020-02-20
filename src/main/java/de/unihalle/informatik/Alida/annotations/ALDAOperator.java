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

import de.unihalle.informatik.Alida.operator.*;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.annotation.Documented;

import net.java.sezpoz.Indexable;

/**
 *   ALDOperators may be annotated with this annotation in order to enable
 *   generic execution or documentation facilities for the operator.
 *   This annotation is used by indexing processes during compile time to
 *   facilitate fast lookup of all classes which extend the abstract class ALDOperators.
 *	 During runtime the annotation may serve to modify behaviour of e.g. generic execution.
 *
 * @author Stefan Posch
 */


@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
@Indexable(type=ALDOperator.class)
@Documented

public @interface ALDAOperator {
	/** Enumerates the different levels of this operator.
      */
	enum Level { 
		/** Used  for operators implementing algorithms likely to be invoked via 
		 * a user interface by life scientists
		 */
		APPLICATION, 

		/** Used  for operators like filters, essentially everything else than <code>APPLICATION</code> 
		 * for the time being.
	     */STANDARD};

	/** Enumerates the different types of user interfaces to
	  * be automatically generated for this operator. 
      */
	enum ExecutionMode { 
		/** no user interface
		 */
	    NONE, 

		/** only GUI
		 */
	    SWING, 

		/** only command line
		 */
	    CMDLINE, 

		/** all available interfaces
		 */
	    ALL };

	/** Defines the types of user interfaces to be automatically generated for this operator.
      * be automatically generated.

	 */
	ExecutionMode genericExecutionMode() default de.unihalle.informatik.Alida.annotations.ALDAOperator.ExecutionMode.NONE;

	/** Defines the level of this operator.
	 */
	Level level() default de.unihalle.informatik.Alida.annotations.ALDAOperator.Level.STANDARD;
	
	/**
	 * If true this operator may me invoked in batch mode within the gui oprunner
	 */
	boolean allowBatchMode() default true;

	/**
	 * A short string to describe the functionality of the operator.
	 * @return	Short decription string.
	 */
	String shortDescription() default "";
}
