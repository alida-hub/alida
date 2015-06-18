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
 * $Rev$
 * $Date$
 * $Author$
 * 
 */

/* This file is derived from:*/

//
// Parameter.java
//

/*
ImageJ software for multidimensional image processing and analysis.

Copyright (c) 2010, ImageJDev.org.
All rights reserved.

Redistribution and use in source and binary forms, with or without
modification, are permitted provided that the following conditions are met:
    * Redistributions of source code must retain the above copyright
      notice, this list of conditions and the following disclaimer.
    * Redistributions in binary form must reproduce the above copyright
      notice, this list of conditions and the following disclaimer in the
      documentation and/or other materials provided with the distribution.
    * Neither the names of the ImageJDev.org developers nor the
      names of its contributors may be used to endorse or promote products
      derived from this software without specific prior written permission.

THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDERS OR CONTRIBUTORS BE
LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
POSSIBILITY OF SUCH DAMAGE.
*/

package de.unihalle.informatik.Alida.annotations;


import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.annotation.Documented;

/**
 * Annotation definition for operator parameters.
 * 
 * @author Johannes Schindelin
 * @author Grant Harris
 * @author Curtis Rueden
 * @author Stefan Posch
 */


@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
@Documented

public @interface Parameter {

	/** Enumerates the different directions a parameter may, e.g. into or out of an operator.
     * Used for documentation of the processing history and code genereration.
     * UNKNOWN is used for compatibility. If the direction of a Parameter is UNKOWN,
     * the old way using Type is assumed.
	 */
	enum Direction { 
		/** undefine direction, used for compatibility with oder Alida version.
	     */
	    UNKNOWN, 

		/** input parameter
	     */
	    IN, 

		/** output parameter
	     */
	    OUT, 

		/** this parameter is as well input as also output of the operator
	     */
	    INOUT };

	/** Enumerates the different types or roles a field may have. Used for documentation
	 * of the processing history and code genereration.
	 */
	@Deprecated
	enum Type { 
		/** input data
	     */
	    INPUT, 

		/** output data
	     */
	    OUTPUT, 

		/** parameter
	     */
	    PARAMETER, 

		/** supplemental
	     */
	    SUPPLEMENTAL };

	/** Enumerates the different modes which are defined for a parameter to be
	 *  shown in automatically generated user interfaces.
	 */
	enum ExpertMode { 
		/** Visible in standard mode, e.g. for life scientists
	     */
	    STANDARD, 

		/** Visible for expert users
	     */
	    ADVANCED};

	/**
	 * Defines the different modes of possible changes a parameter might do
	 * on changes of its own value.
	 */
	enum ParameterModificationMode {
		/**
		 * The parameter never causes any other parameter values to change.
		 */
		MODIFIES_NOTHING,
		/**
		 * The parameter changes other parameters values.
		 */
		MODIFIES_VALUES_ONLY,
		/**
		 * The parameter modifies the interface of the operator, i.e., adds or 
		 * removes parameter dynamically, and also changes values.
		 */
		MODIFIES_INTERFACE
	}
	    
	/** Defines if the parameter is an output. */
	@Deprecated
	boolean output() default false;

	/** Defines a label for the parameter. */
	String label() default "";

	/** Defines whether the parameter is required (i.e., no default). */
	boolean required() default false;

	/** Defines whether to remember the most recent value of the parameter. */
	@Deprecated
	boolean persist() default true;

	/** Defines a key to use for saving the value persistently. */
	@Deprecated
	String persistKey() default "";

    /** Defines the type/role of this field.
     */
    Type type() default de.unihalle.informatik.Alida.annotations.Parameter.Type.PARAMETER;

    /** Defines the direction of this parameter
     */
    Direction direction() default de.unihalle.informatik.Alida.annotations.Parameter.Direction.UNKNOWN;

	/** Define the mode of this parameter to be shown for generic execution
	 */
	ExpertMode mode() default 
		de.unihalle.informatik.Alida.annotations.Parameter.ExpertMode.STANDARD;

    /** Is this parameter supplemental, i.e. does not influence the
     * result of the operator.
     * If true, the parameter is not documented in the processing history
     */
    boolean supplemental() default false;

    /** Suggest order if displaying all parameters of a parametrized class e.g. in a GUI
     */
    int dataIOOrder() default 0;

    /** Gives a potentially verbose explanation of this field.
     */
    String description() default "";
    
    /** Name of a call back function to be invoked if the value of the parameter changes
     * 
     * @return
     */
    String callback() default "";
    
    /** If true this parameter is a pseudo parameter not used for input/output of the
     * operator but rather acts as information to be displayed in the user interface.
     * 
     * @return
     */
    boolean info() default false;
    
    /** 
     * Indicates the modification mode for the parameter. 
     * <p>
     * Depending on the value of the parameter argument, changing the value of 
     * the parameter may either result in changes of the values of other 
     * parameters or in a modification (i.e., adding, removing) of other 
     * parameters of the operator. The third option indicates that changing 
     * the value of the parameter has no effect on other parameters.
     * 
     * @return
     */
    ParameterModificationMode paramModificationMode() 
    	default ParameterModificationMode.MODIFIES_NOTHING;

	/** Defines the preferred widget style. */
	/* The fully qualified name required to workaround javac bug:
	 * http://bugs.sun.com/view_bug.do?bug_id=6512707
	 * See: http://groups.google.com/group/project-lombok/browse_thread/thread/c5568eb659cab203
	 */

	// IJ V2, currently not available WidgetStyle style() default  imagej.plugin.gui.WidgetStyle.DEFAULT;

	/** Defines the minimum allowed value (numeric parameters only). */
	@Deprecated
	String min() default "";

	/** Defines the maximum allowed value (numeric parameters only). */
	@Deprecated
	String max() default "";

	/** Defines the step size to use (numeric parameters only). */
	@Deprecated
	String stepSize() default "";

	/**
	 * Defines the width of the input field in characters
	 * (text field parameters only).
	 */
	@Deprecated
	int columns() default 6;

	/** Defines the list of possible values (multiple choice text fields only). */
	@Deprecated
	String[] choices() default {};

}
