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

package de.unihalle.informatik.Alida.operator;

import java.io.*;
import java.lang.reflect.Field;

import de.unihalle.informatik.Alida.annotations.ALDClassParameter;
import de.unihalle.informatik.Alida.annotations.ALDParametrizedClass;
import de.unihalle.informatik.Alida.annotations.Parameter;
import de.unihalle.informatik.Alida.exceptions.ALDOperatorException;
import de.unihalle.informatik.Alida.exceptions.ALDOperatorException.OperatorExceptionType;

/**
 *  This class describes the parameters of an operator.
 *  <p>
 *  A <code>ALDOpParameterDescriptor</code> has a direction (IN, OUT, INOUT) and 
 *  a boolean indicating if this parameter is supplemental.
 *  <p>
 *  If <code>field</code> is non <code>null</code> then the value of the parameter
 *  is a member variable of <code>genuineInstance</code> of the operator.
 *  This instance has to be supplied as argument when getting/setting the value.
 *  If <code>field</code> is <code>null</code> then the value of the parameter
 *  is not a member variable of the operators but is stored in the <code>value</code> object.
 *  
 *  <p>
 *  Additionally a boolean indicating if the corresponding parameter is permanent, i.e.
 *  its descriptor may not be removed from the operator
 * 
 * @author posch
 */

@ALDParametrizedClass
public class ALDOpParameterDescriptor extends ALDParameterDescriptor
	implements Cloneable {
	
	/**
	 * Holds the value of the parameter. This object is used only in case
	 * that <code>field</code> is <code>null</code>.
	 */
	private Object value;

	/**
	 * Direction of this parameter
	 */
	@ALDClassParameter(label = "direction")
	protected final Parameter.Direction direction;

	/**
	 * Is this a supplemental parameter
	 */
	@ALDClassParameter(label = "supplemental")
	protected final Boolean supplemental;
	
	/**
	 * If a parameter is declared as permanent it (reps. its descriptor) may not be removed
	 * from the operator
	 */
	@ALDClassParameter(label = "permanent")
	protected final Boolean permanent;
	
	// default constructor for dataio provider required for dataIO provider
	protected ALDOpParameterDescriptor() {
		super();
		this.direction = null;
		this.supplemental = false;
		this.permanent = false;
	}
	
	/** Construct a descriptor 
	 *
	 * @param	_name			Name of parameter.
	 * @param	_direction		Direction of parameter.
	 * @param	_supplemental 	Supplemental parameter flag.
	 * @param	cl				Java class. 
	 * @param	_explanation 	Explanatory string.
	 * @param _label				Label for parameter.
	 * @param	_required		Is this argument required for the operator?
	 * @param	_field 			The field for this member.
	 * @param guiOrder			Position in GUI of this member.
	 * @param mode				Mode for handling the parameter.
	 * @param _permanent
	 * @param _callback
	 * @param modifiesParameterDefinitions
	 * @param _info
	 */
	public ALDOpParameterDescriptor(String _name, Parameter.Direction _direction, 
			Boolean _supplemental, Class<?> cl, String _explanation,
			String _label, boolean _required, Field _field,
			int guiOrder, Parameter.ExpertMode mode, Boolean _permanent,
			String _callback, 
			Parameter.ParameterModificationMode modifiesParameterDefinitions, 
			boolean _info) {
		super(_name, cl, _explanation, _label, 
				_required, _field, guiOrder, mode,
				_callback, modifiesParameterDefinitions, _info);
		this.direction = _direction;
		this.supplemental = _supplemental;
		this.permanent = _permanent;
	}
	
	/**
	 */
	public ALDOpParameterDescriptor copy( Field _field) {
		ALDOpParameterDescriptor obj = new ALDOpParameterDescriptor( this.name,
				this.direction, this.supplemental, this.myclass, this.explanation, 
				this.label, this.required, _field,this.dataIOOrder,
				this.handlingMode,  this.permanent, 
				this.callback, this.modifyParamMode, this.info);
		return obj;
	}



	/**
	 * Clone an instance
	 */
	@Override
	public ALDOpParameterDescriptor clone() {
		ALDOpParameterDescriptor obj = new ALDOpParameterDescriptor( this.name,
				this.direction, this.supplemental, this.myclass, this.explanation, 
				this.label, this.required, this.field,this.dataIOOrder,
				this.handlingMode, this.permanent,
				this.callback, this.modifyParamMode, this.info);
		return obj;
	}

	/** 
	 * Print this descriptor to outfile
	 */
	@Override
	public void print( PrintStream outfile) {
		print( outfile, null);
	}

	/** 
	 * Print this descriptor to outfile.
	 * <p>
	 * If <code>op</code> is non null then try to get the value of the parameter and print it also.
	 */
	public void print( PrintStream outfile, ALDOperator op) {
		Object value = null;
		if ( op != null ) {
			try {
				value = op.getParameter( this.name);
			} catch (Exception ex) {
				// nothing happens here...
			}
		}


		if ( this.info) {
			if ( value != null && String.class.isAssignableFrom(value.getClass()) ) {
				outfile.println( value); 
			}
		} else {
			outfile.println( "  Parameter <" + this.name +  "> " +
					(op != null ? " = " +  value + " ": "" ) 
					+	"(" + this.direction  
					+ (this.supplemental.booleanValue() ? " supplemental)" : ")") 
					+	(this.required ? " required" : "") +
					", type: " + this.myclass.getSimpleName() +
					", label: " + this.label +
					", (" + this.explanation + ")" +
					" permanent = " + this.permanent +
					" modifies parameter descriptors = " + this.modifyParamMode);
		}
	}

	/**
	 * Returns parameter direction.
	 */
	public Parameter.Direction getDirection() {
		return this.direction;
	}

	/**
	 * Returns if parameter is supplemental.
	 */
	public Boolean getSupplemental() {
		return this.supplemental;
	}

	/**
	 * @return the isMemberVariable
	 */
	public Boolean getPermanent() {
		return this.permanent;
	}
	
	/** Sets the value of the object represented by this descriptor.
	 * If <code>field</code> of the descriptor is <code>null</code> then
	 * the <code>value</code> variable of the descriptor is used,
	 * otherwise the member variable specified by <code>field</code> in the object <code>instance</code>.
	 * 
	 * @param _value
	 * @param instance
	 * @throws ALDOperatorException
	 */
	public void setValue( Object _value, Object instance) throws ALDOperatorException{
		if ( this.field != null) {
			this.field.setAccessible(true);
			try {
				this.field.set(instance, _value);
			} catch (Exception e) {
				throw new ALDOperatorException(OperatorExceptionType.PARAMETER_ERROR,
						"failed to set value of parameter " + this.name + "...\n " 
								+ e.getMessage());
			}

		} else {
			try {
				this.value = this.myclass.cast(_value);
			} catch (Exception e) {
				throw new ALDOperatorException(OperatorExceptionType.PARAMETER_ERROR,
						"failed to set value of parameter " + this.name);
			}

		}
	}

	/**
	 * @return the value
	 * @throws ALDOperatorException 
	 */
	public Object getValue(Object instance) throws ALDOperatorException {
		if ( this.field != null) {
			this.field.setAccessible(true);
			try {
				return this.field.get(instance);
			} catch (Exception e) {
				throw new ALDOperatorException(OperatorExceptionType.PARAMETER_ERROR,
						"failed to get value of parameter " + this.name);
			}

		}
		return this.value;
	}
}
