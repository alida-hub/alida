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

import java.lang.reflect.Field;
import java.io.PrintStream;

import de.unihalle.informatik.Alida.annotations.ALDClassParameter;
import de.unihalle.informatik.Alida.annotations.Parameter;
import de.unihalle.informatik.Alida.annotations.ALDParametrizedClass;

/**
 * This class describes generic parameters in Alida used, e.g., in parameterized
 * classes and operators.
 * <p>
 * The descriptions contains a name, the java class (<code>mylass</code>, and an
 * explanatory string. The <code>label</code> may be used, e.g. in a GUI, to
 * identify the parameter instead of using it <code>name</code>. The
 * <code>required</code> flag may not be interpreted for all parameters.
 * <p>
 * The field <code>guiOrder</code> may be use to order of appearance of
 * parameters in user interfaces, where smaller order indicates early
 * appearance. Likewise <code>handlingMode</code> may be used to influence GUIs.
 * <p>
 * Note: default values are valid only for the (very old) implementation without
 * annotations an will disappear in the near future.
 * <p>
 * The field is used for internal means.
 * <p>
 * Implementational note: a reference to the Parameter-annotation is NOT
 * included to allow for wrapping of non-Alida operators as Alida operators.
 * 
 * 
 * @author moeller
 * @see ALDOperator
 */

@ALDParametrizedClass
public class ALDParameterDescriptor implements Cloneable {

	/**
	 * Name of the parameter, i.e. its variable.
	 */
	@ALDClassParameter(label = "name")
	protected final String name;

	/**
	 * Class of the parameter.
	 */
	@ALDClassParameter(label = "myclass")
	protected final Class<?> myclass;

	/**
	 * Explanatory string.
	 */
	@ALDClassParameter(label = "explanation")
	protected final String explanation;

	/**
	 * Label of the parameter.
	 */
	@ALDClassParameter(label = "label")
	protected final String label;

	/**
	 * Field corresponding to parameter.
	 */
	protected final Field field;

	/**
	 * Flag to indicate if parameter is required.
	 */
	@ALDClassParameter(label = "required")
	protected final boolean required;

	/**
	 * Data I/O order in GUI.
	 */
	@ALDClassParameter(label = "dataIOOrder")
	protected final int dataIOOrder;

	/**
	 * Parameter mode, i.e. if to be relevant for all users or experts only.
	 */
	@ALDClassParameter(label = "handlingMode")
	protected final Parameter.ExpertMode handlingMode;
	
	/**
	 * Associated callback method.
	 */
	@ALDClassParameter(label = "callback")
	protected final String callback;
	
	/**
	 * Operator interface modification mode.
	 */
	@ALDClassParameter(label = "modifyParamMode")
	protected final Parameter.ParameterModificationMode modifyParamMode;
	
	/**
	 * Flag to indicate if it is just an info parameter.
	 */
	@ALDClassParameter(label = "info")
	protected final boolean info;

	// default constructor required for dataIO provider
	protected ALDParameterDescriptor() {
		this.name = null;
		this.myclass = null;
		this.explanation = null;
		this.label = null;
		this.required = false;
		this.field = null;
		this.dataIOOrder = 0;
		this.handlingMode = null;
		this.callback = null;
		this.modifyParamMode = null;
		this.info = false;

	}

	/**
	 * Constructor.
	 * 
	 * @param _name
	 *            Name of parameter.
	 * @param cl
	 *            Java class.
	 * @param _explanation
	 *            Explanatory string.
	 * @param _label
	 *            Label for parameter.
	 * @param _required
	 *            Is this argument required for the operator?
	 * @param _field
	 *            The field for this member.
	 * @param guiOrder
	 * @param mode
	 * @param _callback
	 * @param modifiesParameterDefinitions
	 * @param _info
	 */
	public ALDParameterDescriptor(String _name, Class<?> cl, String _explanation,
			String _label, boolean _required, Field _field,
			int guiOrder, Parameter.ExpertMode mode,
			String _callback, Parameter.ParameterModificationMode modifyMode, 
			boolean _info ) {
		this.name = _name;
		this.myclass = cl;
		this.explanation = _explanation;
		this.label = _label;
		this.required = _required;
		this.field = _field;
		this.dataIOOrder = guiOrder;
		this.handlingMode = mode;
		this.callback = _callback;
		this.modifyParamMode = modifyMode;
		this.info = _info;
	}

	/**
	 * Returns name of parameter.
	 */
	public String getName() {
		return this.name;
	}

	/**
	 * Returns class of parameter.
	 */
	public Class<?> getMyclass() {
		return this.myclass;
	}

	/**
	 * Returns explanation for parameter.
	 */
	public String getExplanation() {
		return this.explanation;
	}

	/**
	 * Returns label for parameter.
	 */
	public String getLabel() {
		return this.label;
	}

	/**
	 * Returns data I/O order of parameter.
	 */
	public int getDataIOOrder() {
		return this.dataIOOrder;
	}

	/**
	 * Returns mode of parameter handling.
	 */
	public Parameter.ExpertMode getHandlingMode() {
		return this.handlingMode;
	}

	/**
	 * Returns is parameter is required.
	 */
	public boolean isRequired() {
		return this.required;
	}

	/**
	 * Returns a reference to the field of the parameter.
	 */
	public Field getField() {
		return this.field;
	}


	/**
	 * @return the callback
	 */
	public String getCallback() {
		return this.callback;
	}

	/**
	 * @return the modifiesParameterDefinitions
	 */
	public Parameter.ParameterModificationMode parameterModificationMode() {
		return this.modifyParamMode;
	}

	/**
	 * @return the info
	 */
	public boolean isInfo() {
		return this.info;
	}

	/**
	 * Clones an instance.
	 */
	@Override
	public ALDParameterDescriptor clone() {
		ALDParameterDescriptor obj = new ALDParameterDescriptor(this.name,
				this.myclass, this.explanation, this.label, this.required,
				this.field, this.dataIOOrder,
				this.handlingMode, this.callback, this.modifyParamMode, this.info);
		return obj;
	}

	/**
	 * Print this descriptor to standard out.
	 */
	public void print() {
		print(System.out);
	}

	/**
	 * Print this descriptor to outfile.
	 */
	public void print(PrintStream outfile) {
		outfile.println("  Parameter <" + this.name + "> " + "(" + ", type: "
				+ this.myclass.getSimpleName() + ", label: " + this.label
				+ ", (" + this.explanation + ")");
	}
}
