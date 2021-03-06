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

import de.unihalle.informatik.Alida.operator.events.ALDOperatorExecutionProgressEvent;
import de.unihalle.informatik.Alida.operator.events.ALDOperatorExecutionProgressEventListener;
import de.unihalle.informatik.Alida.version.ALDVersionProvider;
import de.unihalle.informatik.Alida.version.ALDVersionProviderFactory;
import de.unihalle.informatik.Alida.annotations.Parameter;
import de.unihalle.informatik.Alida.annotations.Parameter.ExpertMode;
import de.unihalle.informatik.Alida.datatypes.ALDConfigurationValidator;
import de.unihalle.informatik.Alida.exceptions.*;
import de.unihalle.informatik.Alida.exceptions.ALDOperatorException.OperatorExceptionType;

import java.io.*;
import java.util.*;
import java.lang.reflect.Field;

import javax.swing.event.EventListenerList;

/**
 * This is the abstract super class for all Alida operators. Alida opertators
 * facilitate automatic logging of the processing historys and automatic
 * generation of user interfaces.
 * <p>
 * The interface consisting of all parameters needs to be define by annotating
 * the corresponding fields (i.e. member variables) using the {@link Parameter}
 * annotation. The direction of a parameter may be input (IN), output (OUT), or
 * both input and output (INOUT). A Parameter may be declared to be
 * supplemental. Supplemental parameters, e.g., control output of debugging
 * information or return intermediate results. The outputs of the operator are
 * expected to be independent of the value of these supplemental values which
 * are not stored in the processing history. A parameter with direction IN or
 * INOUT which is not supplemental may be declared to be required. For OUT
 * parameter and supplemental parameters the required field is ignored. If a
 * parameter of an operator is expected to be documented in the data flow of the
 * processing history, it may be of any Java class where the instances are by
 * references. This excludes only primitive data types, interned strings, and
 * cached numerical objects. If the parameter is not be part of the data flow
 * all classes are acceptable.
 * <p>
 * Parameter may be added or removed.
 * <p>
 * Values of parameters and inputs have to be set prior to invoking the operator
 * via <code>runOp()</code> and the resulting outputs may be retrieved from the
 * operator after return from <code>runOp()</code>.
 * <p>
 * A default constructor without arguments has to be implemented for all
 * features of code generation and generic execution to be available. For
 * generic execution the operator has to be annotated with
 * {@link de.unihalle.informatik.Alida.annotations.ALDAOperator} with
 * <code>allowGenericExecution</code> set to true.
 * <p>
 * The method <code>operate()</code> implements the processing of this operator
 * and has to be overridden when implementing non-abstract operators. All
 * information passed into and back from the operator are passed via the member
 * variables of the operator which are properly annotated. This method should by
 * no means be used to invoke processing of the operator directly, rather
 * <code>runOp()</code> is to be used.
 * <p>
 * The method <code>runOp()</code> is called by the user of an operator to
 * invoke processing. <code>runOp()</code> first checks if all required
 * parameters and inputs are set to non-null values and subsequently check the
 * validity of parameters and inputs as defined by the method
 * <code>validateCustom</code> which has to be overridden when implementing an
 * operator.
 * <p>
 * Refer to the Alida manual for more details and examples.
 * 
 * @author posch
 */

public abstract class ALDOperator 
     implements ALDConfigurationValidator, ALDOperatorExecutionProgressEventListener {
	/**
	 * Determines the visibility of an operator invocation via <code>runOp()</code> in
	 * the processing history.
	 */
	public enum HidingMode {
		/** visible in the history
		 */
		VISIBLE, 
		/** hide this invocation, i.e. the corresponding opnode, form the
		 * history as well as all children, i.e. nested calls of further operators
		 */
		HIDDEN, 
		/** the invocation is visible, but all children, i.e. nested invocations
		 * of further operators, are hidden
		 */
		HIDE_CHILDREN
	}

	// ====================================================
	// MEMBER VARIABLES

	/**
	 * Name of the operator
	 */
	public String name;
		
	/**
	 * Genuine object representing the operation. This may be used in operators,
	 * wrapping another class implementing the actual operation. Reflections
	 * need to be used to set this member in extending classes.
	 */
	Object genuineInstance;
	
	/**
	 * The <code>opNode</code> of this operator during execution of <code>runOp</code>.
	 */
	private ALDOpNode opNode = null;

	/**
	 * Verbose flag to be inherited by all operators.
	 */
	@Parameter(label = "Verbose", supplemental = true, 
		direction = Parameter.Direction.IN, description = "Verbose flag",
		mode = ExpertMode.ADVANCED)
	protected Boolean verbose = new Boolean(false);

	/**
	 * Does this operator prefer a complete DAG or a data dependency DAG. This
	 * field may be changed when implementing an operator.
	 */
	protected boolean completeDAG = true;

	/**
	 * Access to the central port hash.
	 */
	protected static ALDPortHashAccess portHashAccess = new ALDPortHashAccess();

	/**
	 * A weak hashmap which contains for each thread a stack of opNodes
	 * reflecting the current state of the method stack. Whenever
	 * <code>runOp()</code> is called, an opNode is created which is associated
	 * with this call. It is pushed onto the stack and popped at the end of
	 * <code>runOp()</code>. As the first element a opNode for the dummy
	 * operator ALDToplevelOperator is created which represents outer or top
	 * most level of operator calls.
	 */
	static WeakHashMap<Thread, Stack<ALDOpNode>> opNodeStackHash = new WeakHashMap<Thread, Stack<ALDOpNode>>();

	/**
	 * Hash contains parameter descriptors
	 * for all currently active parameters.
	 */
	private Hashtable<String, ALDOpParameterDescriptor> parameterDescriptorsActive;

	/**
	 * Hash contains parameter descriptors which has previous be active but currently inactive, i.e. removed,
	 * for potential later re-use.
	 */
	private Hashtable<String, ALDOpParameterDescriptor> parameterDescriptorsInactive;

	/**
	 * Hash contains parameter descriptors which are annotated.
	 */
	private Hashtable<String, ALDOpParameterDescriptor> parameterDescriptorsAnnotated;

	/**
	 * Instance of a class providing version information.
	 */
	protected ALDVersionProvider versionProvider = ALDVersionProviderFactory
			.getProviderInstance();

	// really only debugging
	private boolean debug = false;
	
	/**
	 * List of control event listeners attached to this class listening to 
	 * {@link ALDOperatorExecutionProgressEvent}.
	 */
	
	protected transient volatile EventListenerList operatorExecutionEventlistenerList = 
			new EventListenerList();

	// ----------------------------------------------------
	// This is currently experimental for debugging purposes
	
	/**   
	 * Mode of implicit construction of the processing graph. Still experimental
	 * at this point!! 
     */
	public enum HistoryConstructionMode {
		/** obey hiding mode, i.e. construct no history for hidden nodes
		 */
		CONSIDER_HIDINGMODE,

		/** prevent implicit construction completely 
		 */
		NO_HISTORY,
		
		/** complete construction of the history, does not take hiding mode into account
		 */
		COMPLETE_HISTORY
	}

	/**
	 * Mode of implicit construction of the processing graph. 
	 * <dl>
	 * <li> 0 = complete construction of the history, does not take hiding mode into account (= COMPLETE_HISTORY) </li>
	 * <li> 1 = do not create opNodes for hidden operator invocations (= CONSIDER_HIDINGMODE) </li>
	 * <li> 2 = prevent implicit construction completely (= NO_HISTORY) </li>
	 * </dl>
	 */
	private static HistoryConstructionMode constructionMode = HistoryConstructionMode.CONSIDER_HIDINGMODE;

	// END of This is currently experimental for debugging purposes
	// ----------------------------------------------------

	
	// ====================================================
	// METHODS

	/**
	 * @param constructionMode the constructionMode to set
	 */
	public static void setConstructionMode(HistoryConstructionMode constructionMode) {
		ALDOperator.constructionMode = constructionMode;
	}
	/**
	 * @param constructionMode the constructionMode to set
	 */
	public static HistoryConstructionMode setConstructionMode() {
		return ALDOperator.constructionMode;
	}



	/**
	 * Return the current mode of implicit construction {@link #constructionMode}.
	 * 
	 * @return the constructionMode
	 * @throws ALDOperatorException 
	 */
	public static int getConstructionMode()  {
		switch ( ALDOperator.constructionMode) {
		case CONSIDER_HIDINGMODE:
			return 1;
		case NO_HISTORY:
			return 2;
		case COMPLETE_HISTORY:
			return 0;
		default:
			return 0;
//			throw new ALDOperatorException(
//					ALDOperatorException.OperatorExceptionType.UNSPECIFIED_ERROR,
//					"unhandled construction mode in getConstructionMode" );
		}
	}

	/**
	 * Set the mode of implicit construction of the processing graph. Still experimental
	 * at this point!! 
	 * <dl>
	 * <li> 0 = regular construction </li>
	 * <li> 1 = do not create opNodes for hidden operator invocations </li>
	 * <li> 2 = prevent implicit construction completely </li>
	 * </dl>
	 * 
	 * @param cMode the constructionMode to set
	 */
	public static void setConstructionMode(int cMode)  {
		switch ( cMode) {
		case 1:
			ALDOperator.constructionMode = HistoryConstructionMode.CONSIDER_HIDINGMODE;
			break;
		case 2:
			ALDOperator.constructionMode =  HistoryConstructionMode.NO_HISTORY;
			break;
		case 0:
			ALDOperator.constructionMode =  HistoryConstructionMode.COMPLETE_HISTORY;
			break;
//		default:
//			throw new ALDOperatorException(
//					ALDOperatorException.OperatorExceptionType.UNSPECIFIED_ERROR,
//					"unhandled construction mode in setConstructionMode" );
//
		}
	}

	/**
	 * This constructor initializes an operator. The name is retrieved from the
	 * classname and the parameter hash is initialized.
	 */
	public ALDOperator() throws ALDOperatorException {
		this.name = this.getClass().getSimpleName();
		this.genuineInstance = this;

		this.parameterDescriptorsActive = new Hashtable<String, ALDOpParameterDescriptor>();
		this.parameterDescriptorsInactive = new Hashtable<String, ALDOpParameterDescriptor>();
		this.parameterDescriptorsAnnotated = new Hashtable<String, ALDOpParameterDescriptor>();
		
		// loop for this class and all super classes over all declared fields to
		// find Annotations
		Class<?> myclass = this.getClass();
		do {

			for (Field field : myclass.getDeclaredFields()) {
				String name = field.getName();
				
				// if we already found a parameter with this name keep the former one
				// as it is lower in the class hierarchy
				if ( this.parameterDescriptorsActive.containsKey(name)) {
					//System.out.println("discard " + name + " in " + myclass.getName());
					break;
				}

				Parameter pAnnotation = field.getAnnotation(Parameter.class);

				// if field is annotated as parameter create and add appropriate
				// descriptor
				// and not already declared in a class downstream of the
				// inheritance hierarchy
				// (as we add the first field for each name we find when walking
				// up the inheritance hierarchy
				if (pAnnotation != null && !fieldContained(name)) {
//					Object defaultValue = null;

					String explanation;
					explanation = pAnnotation.description();

					String label = pAnnotation.label();
					
					if (pAnnotation.direction() != Parameter.Direction.UNKNOWN) {
						// this is the current version of parameter annotation
						ALDOpParameterDescriptor parameterDescriptor = new ALDOpParameterDescriptor(
								name, pAnnotation.direction(),
								pAnnotation.supplemental(), field.getType(),
								explanation, label, pAnnotation.required(),
								field, pAnnotation.dataIOOrder(),
								pAnnotation.mode(), true,
								pAnnotation.callback(),
								pAnnotation.paramModificationMode(),
								pAnnotation.info());

						if (this.debug) {
							System.out
									.println("   ALDOperator():: adding output parameter <"
											+ name + ">");
						}
						this.addParameterUnconditioned(parameterDescriptor);
						this.parameterDescriptorsAnnotated.put( name, parameterDescriptor);

					} else {
						// this is the previous version of parameter annotation
						// still
						// handled for compatibility
						Parameter.Type myType = pAnnotation.type();

						if (myType == Parameter.Type.OUTPUT) {
							// output
							ALDOpParameterDescriptor parameterDescriptor = new ALDOpParameterDescriptor(
									name, Parameter.Direction.OUT, false,
									field.getType(), explanation, label,
									pAnnotation.required(), 
									field, pAnnotation.dataIOOrder(),
									pAnnotation.mode(), true,
									pAnnotation.callback(),
									pAnnotation.paramModificationMode(),
									pAnnotation.info());
							if (!allowedClassForIO(field.getType())) {
								throw new ALDOperatorException(
										ALDOperatorException.OperatorExceptionType.INVALID_CLASS,
										"<" + field.getName() + "> of class <"
												+ field.getType().getName()
												+ ">" + " in operator "
												+ this.name);
							}
							if (this.debug) {
								System.out
										.println("   ALDOperator():: adding output parameter <"
												+ name + ">");
							}
							this.addParameterUnconditioned(parameterDescriptor);
							this.parameterDescriptorsAnnotated.put( name, parameterDescriptor);

						} else if (myType == Parameter.Type.INPUT) {
							// input
							ALDOpParameterDescriptor parameterDescriptor = new ALDOpParameterDescriptor(
									name, Parameter.Direction.IN, false,
									field.getType(), explanation, label,
									pAnnotation.required(), 
									field, pAnnotation.dataIOOrder(),
									pAnnotation.mode(), true,
									pAnnotation.callback(),
									pAnnotation.paramModificationMode(),
									pAnnotation.info());
							if (!allowedClassForIO(field.getType())) {
								throw new ALDOperatorException(
										ALDOperatorException.OperatorExceptionType.INVALID_CLASS,
										"<" + field.getName() + "> of class <"
												+ field.getType().getName()
												+ ">" + " in operator "
												+ this.name);
							}
							if (this.debug) {
								System.out
										.println("   ALDOperator():: adding input parameter <"
												+ name
												+ ">"
												+ " of type <"
												+ field.getType() + ">");
							}
							this.addParameterUnconditioned(parameterDescriptor);
							this.parameterDescriptorsAnnotated.put( name, parameterDescriptor);

						} else if (myType == Parameter.Type.PARAMETER) {
							// parameter
							ALDOpParameterDescriptor parameterDescriptor = new ALDOpParameterDescriptor(
									name, Parameter.Direction.IN, false,
									field.getType(), explanation, label,
									pAnnotation.required(), 
									field, pAnnotation.dataIOOrder(),
									pAnnotation.mode(), true,
									pAnnotation.callback(),
									pAnnotation.paramModificationMode(),
									pAnnotation.info());
							if (this.debug) {
								System.out
										.println("   ALDOperator():: adding parameter parameter <"
												+ name + ">");
							}
							this.addParameterUnconditioned(parameterDescriptor);
							this.parameterDescriptorsAnnotated.put( name, parameterDescriptor);
							
						} else if (myType == Parameter.Type.SUPPLEMENTAL) {
							// supplemental
							ALDOpParameterDescriptor parameterDescriptor = new ALDOpParameterDescriptor(
									name, Parameter.Direction.IN, true,
									field.getType(), explanation, label, false,
									field,
									pAnnotation.dataIOOrder(),
									pAnnotation.mode(), true,
									pAnnotation.callback(),
									pAnnotation.paramModificationMode(),
									pAnnotation.info());
							if (this.debug) {
								System.out
										.println("   ALDOperator():: adding supplemental parameter <"
												+ name + ">");
							}
							
							this.addParameterUnconditioned(parameterDescriptor);
							this.parameterDescriptorsAnnotated.put( name, parameterDescriptor);

						}
					}
				}
			}

			myclass = myclass.getSuperclass();
		} while (myclass != null);
	}

	/**
	 * This method does the actual work and needs to be implemented by every
	 * subclass.
	 */

	protected abstract void operate() throws ALDOperatorException,
			ALDProcessingDAGException;

	/**
	 * Get the name of this operator
	 * 
	 * @return name of the operator
	 */
	public final String getName() {
		return this.name;
	}
	
	/**
	 * Set the name of this operator
	 * 
	 * @return name of the operator
	 */
	protected void setName( String name) {
		this.name = name;
	}

	/**
	 * Full description of the operator, e.g., to be shown as documentation in GUI.
	 * @return	Full description of the operator, maybe null.
	 */
	public String getDocumentation() {
		return null;
	}

	/** Return the <code>hidingMode</code> of this operator during execution
	 * via <code>runOp</code>.
	 * 
	 * @return current hiding mode if operator is being executed
	 */
	public HidingMode getHidingMode() {
		if ( this.opNode != null ) {
			return this.opNode.getHidingMode();
		} else {
			return null;
		}
	}
	
	/**
	 * Set the hiding mode of this operator during execution via <code>runOp</code>.
	 * <p>
	 * NOTE: A subsequent call of <code>runOp</code> will reset the <code>hdingMode</code>
	 * according to the argments of this method call.
	 * 
	 * @param hidingMode
	 */
	public void setHidingMode( HidingMode hidingMode) {
		if ( this.opNode != null) {
			this.opNode.setHidden(hidingMode);
		}
	}

	/**
	 * Get the version of this operator
	 * 
	 * @return version of the operator
	 */
	public final String getVersion() {
		if (this.versionProvider != null) {
			return this.versionProvider.getVersion();
		}
		return "unknown";
	}

	/**
	 * Get a reference to the port hash access object.
	 * 
	 * @return reference to the port hash access object
	 */
	public static ALDPortHashAccess getALDPortHashAccessKey() {
		return portHashAccess;
	}

	// -------------------------------------------------------
	// functions to parameters, their descriptors, and generic getter and setter
	// methods

	/**
	 * Add a parameter with the given descriptor to the operator. 
	 * 
	 * @param descr
	 * @throws ALDOperatorException of type <code>INVALID_PARAMETERNAME</code>if parameter already 
	 * visible or is inactive but exists as an annotated parameter
	 */
	protected void addParameter( ALDOpParameterDescriptor descr) throws ALDOperatorException {
		if ( hasParameter( descr.name) ) {
			// operator has already a parameter with this name
			throw new ALDOperatorException(
					ALDOperatorException.OperatorExceptionType.INVALID_PARAMETERNAME,
					name + " already defined");
		}
		
		if ( parameterDescriptorsAnnotated.containsKey(descr.name)) {
			// operator has an annotated parameter with this name
			throw new ALDOperatorException(
					ALDOperatorException.OperatorExceptionType.INVALID_PARAMETERNAME,
					name + " is an inactive annotated parameter");
		}
		this.addParameterUnconditioned( descr);
	}
	
	/**
	 * Add a parameter with the given <code>name</code> to the operator.
	 * A parameter with this name needs to have been active previously and
	 * the descriptor of the last instance of a parameter with this name is (re)used.
	 * 
	 * @param descr
	 * @throws ALDOperatorException of type <code>INVALID_PARAMETERNAME</code>if parameter already exists
	 * or was not known/active previously.
	 */
	protected void addParameter( String name) throws ALDOperatorException {
		if ( hasParameter(name) ) {
			throw new ALDOperatorException(
					ALDOperatorException.OperatorExceptionType.INVALID_PARAMETERNAME,
					name + " already defined");
		}
		
		if ( ! parameterDescriptorsInactive.containsKey(name)) {
			throw new ALDOperatorException(
					ALDOperatorException.OperatorExceptionType.INVALID_PARAMETERNAME,
					name + " has no known descriptor");
		}
		
		this.addParameterUnconditioned( this.parameterDescriptorsInactive.get(name));
		this.parameterDescriptorsInactive.remove(name);
	}
	
	/**
	 * Add a parameter descriptor to the operator without checks or updating of
	 * parameter hashes besides active parameters.
	 * 
	 * @param descr
	 */
	protected void addParameterUnconditioned( ALDOpParameterDescriptor descr) {
		this.parameterDescriptorsActive.put(descr.getName(), descr);
	}

	
	
	/**
	 * Remove the descriptor associated with <code>name</code>.
	 * 
	 * @param name
	 * @throws ALDOperatorException
	 */
	protected void removeParameter( String name) throws ALDOperatorException {
		if ( ! hasParameter(name) ) {
			throw new ALDOperatorException(
					ALDOperatorException.OperatorExceptionType.INVALID_PARAMETERNAME,
					name + " is currently not defined");
		}
		
		ALDOpParameterDescriptor descr = this.getParameterDescriptor( name);
		parameterDescriptorsInactive.put(name, descr);
		parameterDescriptorsActive.remove(name);
	}
	
	/**
	 * Get the number of parameters
	 * 
	 * @return number of parameters
	 */
	public final int getNumParameters() {
		return this.parameterDescriptorsActive.size();
	}

	/**
	 * Get the names of in or inout parameters.
	 * 
	 * @return names of in or inout parameters
	 */
	public final Collection<String> getInInoutNames() {
		return getInInoutNames(null);
	}

	/**
	 * Get the names of in or inout parameters.
	 * 
	 * @param useRequired
	 *            If true return only required in or inout parameters, if false
	 *            return only not required in or inout parameters. If
	 *            <code>null</code> then return all in or inout parameters.
	 * @return names of in or inout parameters
	 */
	public final Collection<String> getInInoutNames(Boolean useRequired) {
		LinkedList<String> names = new LinkedList<String>();
		for (ALDOpParameterDescriptor descr : parameterDescriptorsActive.values()) {
			if ((useRequired == null || useRequired == descr.required)
					&& (!descr.supplemental)
					&& (descr.direction == Parameter.Direction.IN || descr.direction == Parameter.Direction.INOUT)) {
				names.add(descr.name);
			}
		}

		return names;
	}

	/**
	 * Get the names of out or inout parameters
	 * 
	 * @return names of out or inout parameters
	 */
	public final Collection<String> getOutInoutNames() {
		LinkedList<String> names = new LinkedList<String>();
		for (ALDOpParameterDescriptor descr : parameterDescriptorsActive.values()) {
			if ((descr.direction == Parameter.Direction.OUT || descr.direction == Parameter.Direction.INOUT)) {
				names.add(descr.name);
			}
		}

		return names;
	}

	// ................
	/**
	 * Get the names of in parameters.
	 * 
	 * @param useRequired
	 *            If true return only required in parameters, if false return
	 *            only not required in parameters. If <code>null</code> then
	 *            return all in parameters.
	 * @return names of in parameters
	 */
	public final Collection<String> getInNames(Boolean useRequired) {
		LinkedList<String> names = new LinkedList<String>();
		for (ALDOpParameterDescriptor descr : parameterDescriptorsActive.values()) {
			if ((useRequired == null || useRequired == descr.required)
					&& (descr.direction == Parameter.Direction.IN)) {
				names.add(descr.name);
			}
		}

		return names;
	}

	/**
	 * Get the names of out parameters
	 * 
	 * @return names of out parameters
	 */
	public final Collection<String> getOutNames() {
		LinkedList<String> names = new LinkedList<String>();
		for (ALDOpParameterDescriptor descr : parameterDescriptorsActive.values()) {
			if ((descr.direction == Parameter.Direction.OUT)) {
				names.add(descr.name);
			}
		}

		Collections.sort(names);
		return names;
	}

	/**
	 * Get the names of inout parameters.
	 * 
	 * @param useRequired
	 *            If true return only required inout parameters, if false return
	 *            only not required inout parameters. If <code>null</code> then
	 *            return all inout parameters.
	 * @return names of in parameters
	 */
	public final Collection<String> getInOutNames(Boolean useRequired) {
		LinkedList<String> names = new LinkedList<String>();
		for (ALDOpParameterDescriptor descr : parameterDescriptorsActive.values()) {
			if ((useRequired == null || useRequired == descr.required)
					&& (descr.direction == Parameter.Direction.INOUT)) {
				names.add(descr.name);
			}
		}

		return names;
	}
	
	/** Return true it this operator has at least one INOUT parameter
	 * 
	 * @return
	 */
	public boolean hasInOutParameters() {
		return getInOutNames(null).size() != 0;
	}

	// ................

	/**
	 * Get the names of supplemental parameters
	 * 
	 * @return names of supplemental parameters
	 */
	public final Collection<String> getSupplementalNames() {
		LinkedList<String> names = new LinkedList<String>();
		for (ALDOpParameterDescriptor descr : parameterDescriptorsActive.values()) {
			if (descr.supplemental) {
				names.add(descr.name);
			}
		}

		return names;
	}

	/**
	 * Get the names of all parameters
	 * 
	 * @return collection of all parameter names
	 */
	public final Collection<String> getParameterNames() {
		return this.parameterDescriptorsActive.keySet();
	}

	/**
	 * Get the names of all parameters
	 * 
	 * @return collection of all parameter names
	 */
	protected final Collection<String> getInactiveParameterNames() {
		return this.parameterDescriptorsInactive.keySet();
	}

	/**
	 * Returns true if the operator has a currently active parameter of the given <code>name</code>,
	 * otherwise false.
	 * 
	 * @param name
	 * @return true if parameter with name <code>name</code> exists
	 */
	public boolean hasParameter( String name) {
		return this.parameterDescriptorsActive.containsKey(name);
	}

	/**
	 * Returns true if the operator has an annotated parameter <code>name</code>
	 * irrespective whether this parameter is currently active or not
	 * 
	 * @param name
	 */
	public boolean isAnnotatedParameter( String name) {
		return this.parameterDescriptorsAnnotated.containsKey( name);
	}
	
	/**
	 * Get the parameter descriptor for given name.
	 * 
	 * @param name
	 *            Name of the parameter to get the new value for
	 * @return descriptor value
	 * @throws ALDOperatorException of type <code>INVALID_PARAMETERNAME</code>if the parameter is not active (or does not exists)
	 */
	public final ALDOpParameterDescriptor getParameterDescriptor(String name)
			throws ALDOperatorException {
		if (!this.parameterDescriptorsActive.containsKey(name))
			throw new ALDOperatorException(
					ALDOperatorException.OperatorExceptionType.INVALID_PARAMETERNAME,
					name + " is not an a an active parameter");
		return this.parameterDescriptorsActive.get(name);
	}

	/**
	 * Get the parameter descriptor for given name.
	 * 
	 * @param name
	 *            Name of the parameter to get the new value for
	 * @return descriptor value
	 * @throws ALDOperatorException of type <code>INVALID_PARAMETERNAME</code>if the parameter does not exist
	 */
	protected final ALDOpParameterDescriptor getParameterDescriptorUnconditioned(String name)
			throws ALDOperatorException {
		if (this.parameterDescriptorsActive.containsKey(name))
			return this.parameterDescriptorsActive.get(name);
		if (this.parameterDescriptorsInactive.containsKey(name))
			return this.parameterDescriptorsInactive.get(name);

		throw new ALDOperatorException(
				ALDOperatorException.OperatorExceptionType.INVALID_PARAMETERNAME,
				name + " is an unkown parameter");

	}

	/**
	 * Get the value of a parameter specified by name.
	 * 
	 * @param name
	 *            Name of the parameter to get the new value for
	 * @return value of the parameter
	 * @throws ALDOperatorException of type <code>INVALID_PARAMETERNAME</code>if the parameter is not active or does not exist
	 */
	public Object getParameter(String name) throws ALDOperatorException {
		if (!this.parameterDescriptorsActive.containsKey(name))
			throw new ALDOperatorException(
					ALDOperatorException.OperatorExceptionType.INVALID_PARAMETERNAME,
					name + " is an unkown parameter");

		return getParameterDescriptor(name).getValue( genuineInstance);
	}

	/**
	 * Get the value of a parameter specified by name.
	 * 
	 * @param name
	 *            Name of the parameter to get the new value for
	 * @return value of the parameter
	 * @throws ALDOperatorException of type <code>INVALID_PARAMETERNAME</code>if the parameter is not active or does not exist
	 */
	protected Object getParameterUnconditioned(String name) throws ALDOperatorException {
		if ( this.parameterDescriptorsActive.containsKey(name) ||
		     this.parameterDescriptorsInactive.containsKey(name) ) {
			return getParameterDescriptorUnconditioned(name).getValue( genuineInstance);

		} else {

			throw new ALDOperatorException(
					ALDOperatorException.OperatorExceptionType.INVALID_PARAMETERNAME,
					name + " is an unkown parameter");
		}

	}

	/**
	 * Set the value of a parameter specified by name.
	 * 
	 * @param name
	 *            Name of the parameter to set a new value for
	 * @param value
	 *            new value
	 * @throws ALDOperatorException of type <code><INVALID_PARAMETERNAME/code> if the
	 *          parameter does not exist and of type <code>CALLBACK_ERROR</code>
	 *          if the callback function may not be invoked or its invocations
	 *          results in an exception
	 */
	public void setParameter(String name, Object value)
			throws ALDOperatorException {
		if (!this.parameterDescriptorsActive.containsKey(name))
			throw new ALDOperatorException(
					ALDOperatorException.OperatorExceptionType.INVALID_PARAMETERNAME,
					name + " is not an active parameter");
		setParameterUnconditioned( name,  value);
	}

	/**
	 * Set the value of a parameter specified by name.
	 * 
	 * @param name
	 *            Name of the parameter to set a new value for
	 * @param value
	 *            new value
	 * @throws ALDOperatorException of type <code><INVALID_PARAMETERNAME/code> if the
	 *          parameter does not exist and of type <code>CALLBACK_ERROR</code>
	 *          if the callback function may not be invoked or its invocations
	 *          results in an exception
	 */
	protected void setParameterUnconditioned(String name, Object value)
			throws ALDOperatorException {
		if ( (!this.parameterDescriptorsActive.containsKey(name))  &&
			 (!this.parameterDescriptorsInactive.containsKey(name))     )
			throw new ALDOperatorException(
					ALDOperatorException.OperatorExceptionType.INVALID_PARAMETERNAME,
					name + " is an unkonwn parameter");

		try {
			getParameterDescriptorUnconditioned(name).setValue(value, this.genuineInstance);
			
			// invoke callback function
			String callback = getParameterDescriptorUnconditioned(name).getCallback();
			if ( callback != null && ! callback.isEmpty()) {
				java.lang.reflect.Method method = 
					this.findCallbackMethod(this.genuineInstance.getClass(), callback);
				if (method != null) {
					method.setAccessible(true);
					method.invoke(this.genuineInstance);
				}
				else
					throw new ALDOperatorException(
						ALDOperatorException.OperatorExceptionType.CALLBACK_ERROR, 
							"Callback method \"" + callback + "\" not found!");
			}
		} catch (Exception e) {
			throw new ALDOperatorException(
					ALDOperatorException.OperatorExceptionType.CALLBACK_ERROR,
					name + "\n(" + e.getMessage() + ")");
		}
	}

	/**
	 * Find callback method of parameter in class or any of its super classes.
	 * @param c					Class of parameter object in question.
	 * @param callback	Callback function to be searched for.
	 * @return	Callback method or null if not found.
	 */
	private java.lang.reflect.Method findCallbackMethod(Class<?> c, String callback) {
		Class<?> currentClass = c;
		while (true) {
			try {
				java.lang.reflect.Method method = currentClass.getDeclaredMethod(callback);
				return method;
			} catch(NoSuchMethodException e) {
				currentClass = currentClass.getSuperclass();
				if (currentClass == null)
					return null;
			}
		}
	}

	/**
	 * Init function for deserialized objects.
	 * <p>
	 * This function is called on an instance of this class being deserialized
	 * from file, prior to handing the instance over to the user. It takes care
	 * of a proper initialization of transient member variables as they are not
	 * initialized to the default values during deserialization. 
	 * @return
	 */

	protected Object readResolve() {
		this.reinitializeParameterDescriptors();
		return this;
	}
	
	/** 
	 * Reinitialize the field member of all parameter descriptors.
	 * This is required e.g. after de-serializing an operator object.
	 */
	@Deprecated
	public void reinitializeParameterDescriptors() {
		// this is a hash only find the first field with a name in the class hierarchy
		LinkedList<String> pList =new LinkedList<String>();

		Class<?> myclass = this.getClass();
		do {

			for (Field field : myclass.getDeclaredFields()) {
				String name = field.getName();
				Parameter pAnnotation = field.getAnnotation(Parameter.class);

				// if field is annotated as parameter recreate descriptor
				// and not already declared in a class downstream of the
				// inheritance hierarchy
				if (pAnnotation != null && ! pList.contains(name) ) {
					pList.add( name);
					ALDOpParameterDescriptor descr = parameterDescriptorsActive.get(name);
					parameterDescriptorsActive.put( descr.name, descr.copy( field));
				}
			}

			myclass = myclass.getSuperclass();
		} while (myclass != null);
	}
	
	// ==========================
	// runOp METHOD
	
	/**
	 * A legal method to invoke the operator and handles everything necessary to
	 * protocol the processing history. This call has the same effect as
	 * <code>runOp( HidingMode.VISIBLE)</code>, i.e. runs the operator as visible in the history.
	 * 
	 * @see ALDOperator#runOp(boolean)
	 */
	public final void runOp() throws ALDOperatorException,
			ALDProcessingDAGException {
		runOp(HidingMode.VISIBLE);
	}
	
	/**
	 * A legal method to invoke the operator and handles everything necessary to
	 * protocol the processing history. If <code>hidden</code> is true this call has the same effect as
	 * <code>runOp( HidingMode.HIDDEN)</code>, i.e. runs the operator as hidden.
	 * If <code>hidden</code> is false this call has the same effect as
	 * <code>runOp( HidingMode.VISIBLE)</code>, i.e. runs the operator as visible.

	 * 
	 * @see ALDOperator#runOp(boolean)
	 */
	public final void runOp( boolean hidden) throws ALDOperatorException,
			ALDProcessingDAGException {
		if ( hidden ) {
		    runOp(HidingMode.HIDDEN);
		} else {
			runOp(HidingMode.VISIBLE);
		}
	}

	/**
	 * A legal method to invoke the operator and handles everything necessary to
	 * protocol the processing history. Prior to calling <code>runOp</code>, all
	 * IN and INOUT parameters have to be set. When called <code>runOp</code>
	 * first validates the parameters. Validity requires for all operators, that
	 * all required IN and INOUT parameters to have non null values. In addition
	 * the implementation of an operator may impose further constrains defined
	 * by the method <code>validateCustom</code> which e.g. may restrict the
	 * interval of numerical parameters.
	 * <p>
	 * After successful validation the <code>operate</code> method is called to
	 * do the actuall processing of this operator. Upon return from
	 * <code>runOp</code>, resulting output data may be retrieved from the
	 * operator object.
	 * <p>
	 * <code>hidingMode</code> determines the visibility of this invocation in the processing history.
	 * If  <code>hidingMode</code> equals <code>null</code> this is equivalent to
	 *  <code>hidingMode</code> equal <code>VISIBLE</code> (for backward compatibility).
	 * 
	 * @param hidingMode Determines the visibility of this invocation in the processing history
	 * @return
	 */

	public final void runOp(HidingMode hidingMode) throws ALDOperatorException,
			ALDProcessingDAGException {

		if ( this.debug) {
			System.out.println("");
			System.out.println("Start of runOp for operator " + this.name);
		}

		if (this.debug) {
			this.print();
		}
		
		if ( hidingMode == null ) {
			hidingMode = HidingMode.VISIBLE;
			/*throw new ALDOperatorException(
		
				OperatorExceptionType.VALIDATION_FAILED,
				"ALDOperator.rnOp(): hidingMode  is null in operator "
						+ this.name);
			*/
		}
		
		validate();
		Stack<ALDOpNode> currentThreadsOpNodeStack = null;
		ALDOpNode parentViaThread = null;
		opNode = null;
		ALDPort origPorts[] = null;
		Collection<String> inInoutNames = null;

		try {
			// create an opNode for this operation in the DAG
			// necessary also for progress events
			opNode = new ALDOpNode(this, hidingMode);

			// find parent opNode via opNode stack of this thread
			currentThreadsOpNodeStack = findCurrentThreadsOpNodeStack();
			parentViaThread = currentThreadsOpNodeStack.peek();
			
			// handle progress events and obey hiding mode if inherited from parent
			if ( parentViaThread != null) {
				// register parent as a listener to this operators progress events
				this.addOperatorExecutionProgressEventListener( parentViaThread.op);
				
				// obey hiding mode if inherited from parent
				if (parentViaThread.getHidingMode() == HidingMode.HIDDEN ||
						parentViaThread.getHidingMode() == HidingMode.HIDE_CHILDREN)
					opNode.setHidden(HidingMode.HIDDEN);
			}

			if ( constructionMode != HistoryConstructionMode.NO_HISTORY ) {

				// documentation in the processing history
				if ( includeInHistory(  hidingMode, parentViaThread.getHidingMode()) ) {

					// obey hiding mode if inherited from parent
					if (parentViaThread.getHidingMode() == HidingMode.HIDDEN ||
							parentViaThread.getHidingMode() == HidingMode.HIDE_CHILDREN)
						opNode.setHidden(HidingMode.HIDDEN);

					// register this opNode to its parent opNode via thread parent
					parentViaThread.addDirectChild(opNode);
					opNode.setParent(parentViaThread);
					if (this.debug) {
						System.out
						.println("ALDOperator::runOp adding direct registered child "
								+ this.name 
								+ " ("
								+ opNode
								+ ")\n"
								+ "                   to opNode "
								+ parentViaThread
								+ "("
								+ parentViaThread.getName() + ")");
					}

					currentThreadsOpNodeStack.push(opNode);

					// handle inputs
					inInoutNames = getInInoutNames();
					origPorts = new ALDPort[inInoutNames.size()];

					// first we store the original dataport of inputs
					int i = -1;
					for (String inputName : inInoutNames) {
						i++;
						Object input = this.getParameter(inputName);

						if (input != null) {

							// register the input object
							if (!portHashAccess.isRegistered(input))
								portHashAccess.register(input);

							// connect each input port of this opNode to the port
							// of the input data the operator was supplied with
							opNode.setInOrigin(i, input,
									getParameterDescriptor(inputName).explanation);

							origPorts[i] = portHashAccess.getHistoryLink(input);
						} else {
							opNode.setInOrigin(i, null,
									getParameterDescriptor(inputName).explanation);
							origPorts[i] = null;
						}
					}

					// and now we can set the dataports of the data to the input ports
					// (we have to do this in a second for-loop to handle multiple
					// inputs with
					// identical objects correctly)
					i = -1;
					for (String inputName : inInoutNames) {
						i++;
						Object input = this.getParameter(inputName);

						if (input != null) {
							// set the output port of the input to the input port of
							// this opNode
							portHashAccess
							.setHistoryLink(input, opNode.getInputPort(i));
						}
					}

					if (this.debug) {
						System.out.println("");
						System.out.println("  before operate");
						for ( int i1 = 0 ; i1 < inInoutNames.size() ; i1++ ) {
							i1++;
							System.out.println("    port of " + i1 + "-th input "
									+ opNode.getInputPort(i1) + " with origin "
									+ opNode.getInputPort(i1).getOrigin());
						}
					}
				} else {
					if (this.debug) {
						System.out
						.println("ALDOperator::runOp do not add "
								+ this.name
								+ " to history");
					}
				}
			}

			if (this.debug)
				System.out.println("Running operate...");

			// do the work
			operate();

			if ( parentViaThread != null) {
				// remove parent operator from progess event listener list
				this.removeOperatorExecutionProgressEventListener( parentViaThread.op);
				// remove reference to operator from opNode to allow garbage collection
				if ( opNode != null ) {
					opNode.op = null; 
				}
			}

			
			
			if (this.debug)
				System.out.println("Running operate...done.");

			if ( constructionMode != HistoryConstructionMode.NO_HISTORY ) {
				// documentation in the processing history
				if ( includeInHistory(  hidingMode, parentViaThread.getHidingMode() )) {

					// handle output ports: connect output ports of opNode to the
					// passing output parameters
					Collection<String> outInoutNames = getOutInoutNames();
					int i = -1;
					for (String outputName : outInoutNames) {
						i++;
						Object output = this.getParameter(outputName);

						// check if datum has already a port
						if (output != null && !portHashAccess.isRegistered(output))
							portHashAccess.register(output);

						if (output != null) {

							// register the output object to the history DB
							if (!portHashAccess.isRegistered(output))
								portHashAccess.register(output);

							// connect each output port of this opNode to the output
							// port
							// its results (i.e. oututs) originate from
							opNode.setOutOrigin(i, output,
									getParameterDescriptor(outputName).explanation);

							// store properties of output at the output port
							if (output instanceof ALDData) {
								Enumeration<String> keys = ((ALDData) output)
										.getPropertyKeys();
								while (keys.hasMoreElements()) {
									String key = keys.nextElement();
									opNode.getOutputPort(i).setProperty(key,
											((ALDData) output).getProperty(key));
								}
							}
						} else {
							opNode.setOutOrigin(i, null,
									getParameterDescriptor(outputName).explanation);
						}
					}
					// restore port of inputs
					i = -1;
					for (String inputName : inInoutNames) {
						i++;
						if (origPorts[i] != null)
							portHashAccess.setHistoryLink(this.getParameter(inputName),
									origPorts[i]);
					}

					// handle outputs: connected leaving outputs parameters to output
					// ports of this opNode
					i = -1;
					for (String outputName : outInoutNames) {
						i++;
						Object output = this.getParameter(outputName);

						if (output != null) {
							// set the output port of the output (i.e. result) of the
							// operator
							// to the output port of the opNode
							portHashAccess.setHistoryLink(output,
									opNode.getOutputPort(i));
						}
					}


					if (this.debug) {
						this.print();
						for ( int i1 = 0 ; i1 < outInoutNames.size() ; i1++) {
							i1++;
							System.out.println("    port for " + i1 + "-th result "
									+ opNode.getOutputPort(i1) + " with origin "
									+ opNode.getOutputPort(i1).getOrigin());
							if (opNode.getOutputPort(i1).getOrigin() != null)
								opNode.getOutputPort(i1).getOrigin().print();
						}
					}

					currentThreadsOpNodeStack.pop();
				}
			}

			if ( this.debug) {
				System.out.println("  End of runOp");
			}
		} finally {
			// invalidate opNode
			this.opNode = null;
		}
	}

	// ===================================================================
	// Helper for runOp()
	
	/** Is this invocation to be included into the history?
	 * 
	 */
	private boolean includeInHistory( HidingMode hidingMode, HidingMode parentHidingMode) {
		return 	(constructionMode == HistoryConstructionMode.COMPLETE_HISTORY) || 
				((constructionMode == HistoryConstructionMode.CONSIDER_HIDINGMODE) && 
			     (hidingMode != HidingMode.HIDDEN) && 
			     (parentHidingMode != HidingMode.HIDDEN) &&
			     (parentHidingMode != HidingMode.HIDE_CHILDREN));
	}
	
	/**
	 * Find the opNode stack for the current thread. Create a new one, if not
	 * existing
	 * 
	 * @return opNode stack for the current thread
	 */
	private Stack<ALDOpNode> findCurrentThreadsOpNodeStack() {
		Thread currentThread = Thread.currentThread();
		Stack<ALDOpNode> currentThreadsOpNodeStack = opNodeStackHash
				.get(currentThread);
		if (currentThreadsOpNodeStack == null) {
			try {
				if (this.debug)
					System.out
							.println("   ALDOperator::runOp create opNodeStack for thread "
									+ currentThread);
				currentThreadsOpNodeStack = new Stack<ALDOpNode>();
				opNodeStackHash.put(currentThread, currentThreadsOpNodeStack);
				currentThreadsOpNodeStack.push(new ALDOpNode(new ALDToplevelOperator(), HidingMode.VISIBLE));
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

		if (this.debug)
			System.out.println("   ALDOperator::runOp opNodeStack for thread "
					+ currentThread + " found: " + currentThreadsOpNodeStack);
		return currentThreadsOpNodeStack;
	}

	// ===================================================================

	/**
	 * Validates the parameters and inputs of this operator. The generic part of
	 * this validation checks that each required parameter and input is set to a
	 * non-null value. The custom part of validation is operator depended and
	 * implemented via the method <code>validateCustom</code>. An
	 * ALDOperatorException is thrown if either validation fails.
	 */
	public final void validate() throws ALDOperatorException {
		validateGeneric();
		validateCustom();
	}

	/**
	 * Generic validation of the in and inout parameters of this operator.
	 * Validation succeeds if each required parameter and input is set to a
	 * non-null value or supplies a default value. An ALDOperatorException is
	 * thrown if validation fails.
	 */
	public final void validateGeneric() throws ALDOperatorException {

		boolean gotError = false;
		StringBuffer errString = new StringBuffer();
		Collection<String> inInoutNames = getInInoutNames();
		for (String inputName : inInoutNames) {
			if (this.parameterDescriptorsActive.get(inputName).required
					&& getParameter(inputName) == null) {
				gotError = true;
				errString.append ("\tRequired input parameter\" "
								+ inputName + "\"" + " is null\n");
			}
		}
		
		if ( gotError) {
			System.out.println("ALDOperator.validateGeneric(): failed for operator "
							+ this.name + "\n" +
							errString);
			throw new ALDOperatorException(
					OperatorExceptionType.VALIDATION_FAILED,
					"ALDOperator.validateGeneric(): failed for operator "
							+ this.name + "\n" +
							errString);
		}
	}

	/** Returns all required IN an INOUT parameters which have a null value
	 * 
	 * @return List of the parameter names
	 */
	@Deprecated
	public List<String> getMissingRequiredInputs() {
		return unconfiguredItems();
	}
	
	/** Returns all required IN an INOUT parameters which have a null value
	 * 
	 * @return List of the parameter names
	 */
	@Override
  public List<String> unconfiguredItems() {
		LinkedList<String> missingParams = new LinkedList<String>();
		try {
			for ( String param : getInInoutNames(true)) {
				if ( this.getParameter(param) == null)
					missingParams.add( param);
			} 
		}catch ( ALDOperatorException ex) {
			// this should not happen !!
		}
		
		return missingParams;
	}

	/**
	 * Operator specific validation of parameters and inputs. Should be
	 * overridden when extending an operator to impose specific constraints.
	 */
	public void validateCustom() throws ALDOperatorException {
		return;
	}
	
	@Override
	public
	boolean isConfigured() {
		return (unconfiguredItems().size() == 0);
	}

	// ===================================================================
	// GETTER/SETTER for verbose parameter
	/**
	 * Set the verbose state of this opertor.
	 * 
	 * @param verbose
	 *            New verbose state
	 */
	public void setVerbose(Boolean verbose) throws ALDOperatorException {
		this.verbose = verbose;
	}

	/**
	 * Return the verbose state of this opertor.
	 * 
	 * @return Current verbose state
	 */
	public Boolean getVerbose() throws ALDOperatorException {
		return this.verbose;
	}

	// ===================================================================
	// METHODS TO HANDLE HISTORY

	/**
	 * Reads and set the history graph of the given object from file.
	 * 
	 * @param obj
	 *            Object for which the history is to be read.
	 * @param filename
	 *            File from where the history is to be read.
	 * @see ALDPortHashAccess#readHistory(Object,String)
	 */
	public static void readHistory(Object obj, String filename) {
		portHashAccess.readHistory(obj, filename);
	}

	/**
	 * Write the processing history if any to a graphml file. Equivalent to
	 * <code>writeHistory(obj, filename, ALDProcessingDAG.HistoryType.OPNODETYPE, false)</code>
	 * 
	 * @param obj
	 *            Object for which the history to write for.
	 * @param filename
	 *            Filename to write the processing history into.
	 *            For handling of extensions see 
	 *            {@link ALDPortHashAccess#writeHistory(Object, String, de.unihalle.informatik.Alida.operator.ALDProcessingDAG.HistoryType, boolean)}
	 */
	public static void writeHistory(Object obj, String filename)
			throws ALDProcessingDAGException, ALDOperatorException {
		writeHistory(obj, filename, ALDProcessingDAG.HistoryType.OPNODETYPE,
				false);
	}

	/**
	 * Write the processing history if any to a graphml file. Equivalent to
	 * <code>writeHistory(obj, filename, historyType, false)</code>
	 * 
	 * @param obj
	 *            Object for which the history to write for.
	 * @param filename
	 *            Filename to write the processing history into.
	 *            For handling of extensions see 
	 *            {@link ALDPortHashAccess#writeHistory(Object, String, de.unihalle.informatik.Alida.operator.ALDProcessingDAG.HistoryType, boolean)}
	 * @param historyType
	 *            Type/mode of the history.
	 */
	public static void writeHistory(Object obj, String filename,
			ALDProcessingDAG.HistoryType historyType)
			throws ALDProcessingDAGException, ALDOperatorException {

		writeHistory(obj, filename, historyType, false);
	}

	/**
	 * Write the processing history if any to a graphml file.
	 * 
	 * @param obj
	 *            Object for which the history to write for.
	 * @param filename
	 *            Filename to write the processing history into ingraphml/XML
	 *            format.
	 *            For handling of extensions see 
	 *            {@link ALDPortHashAccess#writeHistory(Object, String, de.unihalle.informatik.Alida.operator.ALDProcessingDAG.HistoryType, boolean)}
	 * @param historyType
	 *            Type/mode of the history.
	 * @param ignoreHiding
	 *            If true, hiding of opNodes is ignored.
	 * 
	 * @see ALDPortHashAccess#writeHistory(Object,String,ALDProcessingDAG.HistoryType,boolean)
	 */
	public static void writeHistory(Object obj, String filename,
			ALDProcessingDAG.HistoryType historyType, boolean ignoreHiding)
			throws ALDProcessingDAGException, ALDOperatorException {
		portHashAccess.writeHistory(obj, filename, historyType, ignoreHiding);
	}

	// ===================================================================

	/**
	 * Write the parameter values to an xml file. Uses xstream for
	 * serialization.
	 * 
	 * @param filename
	 *            filename of the xml file to write to
	 */

	// ===================================================================
	// support for ALDOperators to act as event listeners

	/**
	 * Adds a listener to this reporter.
	 * @param listener		Listener to be added.
	 */
	public void addOperatorExecutionProgressEventListener(
			ALDOperatorExecutionProgressEventListener listener) {
		this.operatorExecutionEventlistenerList.add(ALDOperatorExecutionProgressEventListener.class, listener);
	}

	/**
	 * Removes a listener from this reporter.
	 * @param listener		Listener to be removed.
	 */
	public void removeOperatorExecutionProgressEventListener(
			ALDOperatorExecutionProgressEventListener listener) {
		this.operatorExecutionEventlistenerList.remove(ALDOperatorExecutionProgressEventListener.class, listener);
	}

	/**
	 * Sends an event of changed execution progress to all registered listeners.
	 * 
	 * @param ev		Event to be send to all listeners.
	 */
	protected void fireOperatorExecutionProgressEvent(ALDOperatorExecutionProgressEvent ev){
		
		// get list of listeners 
		Object[] listeners = this.operatorExecutionEventlistenerList.getListenerList();
		
		/* listeners will always be non-null as getListenerList() is guaranteed 
		 * to return a non-null array... */

		// process the listeners last to first, notifying
		// those that are interested in this event
		for (int i = listeners.length-2; i>=0; i-=2) {
			if (listeners[i]==ALDOperatorExecutionProgressEventListener.class) {
				// lazily create the event:
				try {
					((ALDOperatorExecutionProgressEventListener)listeners[i+1]).handleOperatorExecutionProgressEvent(ev);
				} catch (ALDWorkflowException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
	}

    //	===================================================================
    // printing
	/**
	 * Print some information of the current state this operator to System.out.
	 */

	public void print() {
		print(System.out);
	}

	/**
	 * Print some information the current state of this operator to
	 * <code>outfile</code>.
	 * 
	 * @param outfile
	 *            Stream to print on
	 */

	public void print(PrintStream outfile) {
		print(outfile, true);
	}

	/**
	 * Print information of the interface of this operator to System.out.
	 */

	public void printInterface() {
		print(System.out, false);
	}

	/**
	 * Print information of the interface of this operator to
	 * <code>outfile</code>.
	 * 
	 * @param outfile
	 *            Stream to print on
	 */

	public void printInterface(PrintStream outfile) {
		print(outfile, false);
	}

	/**
	 * Print information of the interface and values of this operator to
	 * <code>outfile</code>. If printValue is true, additional the current value
	 * is printed. This method note intended for public use.
	 * 
	 * @param outfile
	 *            Stream to print on
	 * @param printValue
	 */

	protected void print(PrintStream outfile, boolean printValue) {
		outfile.println("Interface of ALDOperator: " + this.name);

		// for ( String pName : getParameterNames() )
		if (getInInoutNames(true).size() > 0)
			System.out.println("Required input parameters");
		for (ALDOpParameterDescriptor descr :  sortedDescriptors( getInInoutNames(true)) )
			descr.print(outfile, printValue ? this : null);

		if (getInInoutNames(false).size() > 0)
			System.out.println("Optional input parameters");
		for (ALDOpParameterDescriptor descr : sortedDescriptors( getInInoutNames(false)) )
			descr.print(outfile, printValue ? this : null);

		if (getOutInoutNames().size() > 0)
			System.out.println("Output parameters");
		for (ALDOpParameterDescriptor descr : sortedDescriptors( getOutInoutNames()) )
			descr.print(outfile, printValue ? this : null);

		if (getSupplementalNames().size() > 0)
			System.out.println("Supplemental parameters");
		for (ALDOpParameterDescriptor descr : sortedDescriptors( getSupplementalNames()) )
			descr.print(outfile, printValue ? this : null);
	}

	/**
	 * Returns a string containing printable information about this operator including parameters.
	 */
	public String toStringVerbose() {
		StringBuffer buf = new StringBuffer();

		for (String pName : getInInoutNames(true)) {
			try {
				buf.append(pName + "=" + getParameter(pName).toString() + "\n");
			} catch (Exception e) {
			}
		}

		return new String(buf);
	}

	// ================== helper function

	/**
	 * Is this class allowed as Input and/or Output?
	 */
	boolean allowedClassForIO(Class<?> currentClass) {
		if (this.debug) {
			System.out
					.println("       ALDOperator::allowedClassForIO for class "
							+ currentClass.getName());
		}

		return !(currentClass == int.class || currentClass == byte.class
				|| currentClass == short.class || currentClass == long.class
				|| currentClass == float.class || currentClass == double.class
				|| currentClass == boolean.class || currentClass == char.class);
	}

	/**
	 * Does any hash table contain the key, i.e. this field?
	 */
	protected boolean fieldContained(String key) {
		return this.parameterDescriptorsActive.containsKey(key);
	}
	
	/** Return a collection of descriptors for the given parameter named
	 * which is sorted according to DataIoOrder.
	 * 
	 * @param parameterNames
	 * @return
	 */
	private Collection<ALDOpParameterDescriptor> sortedDescriptors( Collection<String> parameterNames) {
		LinkedList<ALDOpParameterDescriptor> descriptors = new LinkedList<ALDOpParameterDescriptor>();
		for ( String pName : parameterNames) {
			ALDOpParameterDescriptor descr = parameterDescriptorsActive.get(pName);
			if ( descr != null) {
				descriptors.add( descr);
			}
		}
		java.util.Collections.sort( descriptors, new DescriptorComparator());

		return descriptors;
	}

	//===================================================
	private class DescriptorComparator implements Comparator<ALDParameterDescriptor> {
		 
		  @Override
		  public int compare(ALDParameterDescriptor d1, ALDParameterDescriptor d2) {
		    if (d1 == null && d2 == null) {
		      return 0;
		    } else if (d1 == null) {
		      return 1;
		    } else  if (d2 == null) {
		      return -1;
		    } else if ( d1.getDataIOOrder()  == d2.getDataIOOrder() ) {
		    	return 0;
		    } else if (d1.getDataIOOrder() > d2.getDataIOOrder() ) {
			    	return 1;
		    } else {
		    	return -1;
		    }
		  }
		}
	
	//===================================================
	@Override
	public void handleOperatorExecutionProgressEvent(
			final ALDOperatorExecutionProgressEvent event) throws ALDWorkflowException {
		
		// loop over all listeners and call their handleEvent method
		
		final Object[] listeners = operatorExecutionEventlistenerList.getListenerList();

		// create a new event with this operator to allow the workflow to find the node
		final ALDOperatorExecutionProgressEvent newEvent = 
				new ALDOperatorExecutionProgressEvent( this, event.getEventMessage());
		
		Thread eventThread;
		// Process the listeners last to first, notifying
		// those that are interested in this event
		for (int i = listeners.length-2; i>=0; i-=2) {
			if (listeners[i]==ALDOperatorExecutionProgressEventListener.class) {
				// Lazily create the event for each listener and invoke the listener in a new thread
				final ALDOperatorExecutionProgressEventListener listener =
						(ALDOperatorExecutionProgressEventListener) listeners[i+1];
				eventThread = new Thread(){
					@Override
					public void run() {
						try {
							listener.handleOperatorExecutionProgressEvent( newEvent);
						} catch (ALDWorkflowException ex) {
							// TODO Auto-generated catch block
							ex.printStackTrace();
						}

					}};
					
				eventThread.start();
			}
		}
	}

}
