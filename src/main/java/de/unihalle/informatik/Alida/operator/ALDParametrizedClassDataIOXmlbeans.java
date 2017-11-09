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

package de.unihalle.informatik.Alida.operator;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import de.unihalle.informatik.Alida.annotations.ALDDataIOProvider;
import de.unihalle.informatik.Alida.annotations.Parameter.Direction;
import de.unihalle.informatik.Alida.dataio.ALDDataIOManagerXmlbeans;
import de.unihalle.informatik.Alida.dataio.provider.helpers.ALDInstantiationHelper;
import de.unihalle.informatik.Alida.dataio.provider.helpers.ALDParametrizedClassDataIOHelper;
import de.unihalle.informatik.Alida.dataio.provider.helpers.ALDParametrizedClassDummy;
import de.unihalle.informatik.Alida.dataio.provider.xmlbeans.ALDStandardizedDataIOXmlbeans;
import de.unihalle.informatik.Alida.exceptions.ALDDataIOManagerException;
import de.unihalle.informatik.Alida.exceptions.ALDDataIOProviderException;
import de.unihalle.informatik.Alida.exceptions.ALDDataIOProviderException.ALDDataIOProviderExceptionType;
import de.unihalle.informatik.Alida.exceptions.ALDOperatorException;
import de.unihalle.informatik.Alida_xml.ALDXMLKeyValuePairType;
import de.unihalle.informatik.Alida_xml.ALDXMLObjectType;
import de.unihalle.informatik.Alida_xml.ALDXMLOperatorType;
import de.unihalle.informatik.Alida_xml.ALDXMLOperatorVersion3Type;
import de.unihalle.informatik.Alida_xml.ALDXMLOperatorWithDescriptorType;
import de.unihalle.informatik.Alida_xml.ALDXMLParametrizedType;

/**
 * DataIO provider for parametrized classes and operators for xml representation.
 * <p>
 * For parametrized classes reading is done only for parameters annotated 
 * with {@link de.unihalle.informatik.Alida.annotations.ALDClassParameter}.
 * Either all annotated parameters are written/formated or a
 * subset as specified by a format string.
 * <p>
 * For operators reading and writing is done only for IN and INOUT parameters.
 * In addition the set of active parameters and descriptors is set as represented in the
 * xml representation. For more details see the methods {@link #readData(Field, Class, ALDXMLObjectType, Object)}
 * and {@link #writeData(Object)
 * 
 * @author posch
 *
 */

// Implementation note: this package location is due to access rights

@ALDDataIOProvider
public class ALDParametrizedClassDataIOXmlbeans extends ALDStandardizedDataIOXmlbeans {

	/**
	 * debug messages
	 */
	private boolean debug = false;

	/**
	 * Interface method to announce class for which IO is provided for
	 * 
	 * @return  Collection of classes provided
	 */
	@Override
	public Collection<Class<?>> providedClasses() {
		LinkedList<Class<?>> classes = new LinkedList<Class<?>>();

		classes.add( ALDParametrizedClassDummy.class);
		classes.add( ALDOperator.class);


		return classes;
	}

	/** Xmlbeans provider for parametrized classes and ALDOperators.
     * <p>
     * For parametrized classes values for all parameters which exist in the class object to restore and
     * are also present in the xml object are restored to the value in the xml representation.
     * <p>
     * For operators first the state of active and inactive parameters is restored as close as possible
     * to the state in the xml representation, where changes in the class definition are taken
     * into account.
     * <br>
     * For all parameters in the xml representation the following cases are distinguished.
     * 
     * <ul>
     * <li> If the parameter has been annotated when writing to xml </li>
     * <ul>
     * <li> If the parameter is annotated in the current operator object 
     *       it is restored to its xml state  </li>
     * <li>If the parameter is not annotated or not existing in the current operator object
     * it is ignored</li>
     * </ul>
     * <li> If the parameter has not been annotated when writing to xml </li>
     * <ul>
     * <li> If the parameter is annotated in the current operator object 
     *       it is ignored </li>
     * <li>If the parameter is not annotated or not existing in the current operator object:
     * First, if it is not existing (as an non annotated parameter), its descriptores is restored
     * from xml. In all cases it is subsequently  restored to its xml state</li>
     * </ul>
     * </ul>
     * Restoring refers to restoring the active vs inactive status of a parameter stored in 
     * xml representation.
     * <p>
     * Then the values of all parameters stored in the xml representation are set to
     * parameters of the current operator object. If a parameter does not exist in
     * this object it is ignore.
     * <p> If the value in the xml representation can not be assigned to the member variable
     * this error is ignore for both parametrized classes and operators.
	 * 
	 * @param field
	 * @param cl
	 * @param obj if non null the xml representation is to be parse into this  instance,
	 *        otherwise a new instance is created. 
	 *        
	 * @return the object restore with the state in the xml representation
	 * 
	 * @throws ALDDataIOProviderException
	 * @throws ALDDataIOManagerException
	 */
	@Override
	public Object readData(Field field, Class<?> cl, ALDXMLObjectType aldXmlObject, Object obj) 
			throws ALDDataIOProviderException, ALDDataIOManagerException {
		
		// in case we have an ALDOperator
		ALDOperator op = null;

		if ( cl == null ) {
			cl = field.getType();
		}
		boolean isOperator;
		if ( ALDOperator.class.isAssignableFrom(cl) )  {
			isOperator = true;
			op = (ALDOperator)obj;
		} else {
			isOperator = false;	
		}
		
		// initialize object to return if necessary
		if ( obj == null ) {
			try {
				//obj = cl.newInstance();
				obj = ALDInstantiationHelper.newInstance( cl);
			} catch (Exception e ) {
				throw new ALDDataIOProviderException( ALDDataIOProviderExceptionType.OBJECT_INSTANTIATION_ERROR,
						"ALDParametrizedClassDataIOXmlbeans::readData cannot instantiate class <" + 
								cl.getCanonicalName() + ">");
			} 

			if ( isOperator ) {
				op = (ALDOperator)obj;
				// set the name of the operator
				op.name = ((ALDXMLOperatorType)aldXmlObject).getOpName();
			}
			
		}

		if ( debug ) {
			System.out.println("ALDParametrizedClassDataIOXmlbeans::readData for " +
					(isOperator ? "an opertor <" + op.getName() + ">" 
							: "a parametrized class: <" + obj.getClass().getName() + ">"));
		}
		

		ALDXMLParametrizedType xmlParametrized = null;
		try {
			xmlParametrized = (ALDXMLParametrizedType)aldXmlObject;
		} catch (Exception e) {
			throw new ALDDataIOProviderException( ALDDataIOProviderExceptionType.OBJECT_TYPE_ERROR,
					"ALDParametrizedClassDataIOXmlbeans::readData cannot cast xmlObject to ALDXMLParametrizedType");
		}
		
		// for an operator read and set the active parameters and all associated descriptors from the xml configuration
		// unless an (old) version 1 xml representation is to be read
		if ( isOperator ) {
			if ( ALDXMLOperatorWithDescriptorType.class.isAssignableFrom( aldXmlObject.getClass())) {
				// we have a version 2 xml representation of the operators configuration
				
				// first deactivate all parameter
				LinkedList<String> copyOfPnames = new LinkedList<String>();
				for ( String pName : op.getParameterNames()) {
					copyOfPnames.add(pName);
				}

				for ( String pName : copyOfPnames) {
					try {
						op.removeParameter( pName);
					} catch (Exception e) {
						throw new ALDDataIOProviderException( ALDDataIOProviderExceptionType.UNSPECIFIED_ERROR,
								"ALDParametrizedClassDataIOXmlbeans::readData internal error, cannot remove parameter <" +
										pName +">");
					}
				}

				// add parameters from configuration with associated descriptors
				ALDXMLOperatorWithDescriptorType xmlOperator = (ALDXMLOperatorWithDescriptorType)aldXmlObject;
				for ( int i = 0 ; i < xmlOperator.getParameterDescriptorsArray().length ; i++ ) {
					ALDXMLObjectType elementXmlObject = xmlOperator.getParameterDescriptorsArray(i).getValue();
					ALDOpParameterDescriptor descr = 
							(ALDOpParameterDescriptor) ALDDataIOManagerXmlbeans.getInstance().readData( null, ALDOpParameterDescriptor.class,elementXmlObject);
					try {
						op.addParameter(descr);
					} catch (ALDOperatorException e) {
						throw new ALDDataIOProviderException( ALDDataIOProviderExceptionType.UNSPECIFIED_ERROR,
								"ALDParametrizedClassDataIOXmlbeans::readData internal error, cannot add parameter from descriptor <" +
										descr.name +">");
					}
				}

				// add remaining parameters from configuration which need to have associated annotated descriptors
				for ( int i = 0 ; i < xmlOperator.getParameterNamesArray().length ; i++ ) {
					String pName = xmlOperator.getParameterNamesArray()[i];
					if ( ! op.getParameterNames().contains(pName) ) {
						try {
							op.addParameter(pName);
						} catch (ALDOperatorException e) {
							throw new ALDDataIOProviderException( ALDDataIOProviderExceptionType.UNSPECIFIED_ERROR,
									"ALDParametrizedClassDataIOXmlbeans::readData internal error, cannot add parameter with annotated descriptor <" +
											pName +">");
						}
					}

				}
			} else if ( ALDXMLOperatorVersion3Type.class.isAssignableFrom( aldXmlObject.getClass())) {
				// we have a version 3 xml representation of the operators configuration
				// add parameters from configuration with associated descriptors
				ALDXMLOperatorVersion3Type xmlOperator = (ALDXMLOperatorVersion3Type)aldXmlObject;
				
				List<String> allXmlParameternames = new LinkedList<String>();
				
				Set<String> activeXmlParameternames = new HashSet<String>();
				for ( int i = 0 ; i < xmlOperator.getActiveParameterNamesArray().length ; i++ ) {
					String pName = xmlOperator.getActiveParameterNamesArray()[i];
					allXmlParameternames.add(pName);
					activeXmlParameternames.add(pName);
				}
				
				Set<String> inactiveXmlParameternames = new HashSet<String>();
				for ( int i = 0 ; i < xmlOperator.getInactiveParameterNamesArray().length ; i++ ) {
					String pName = xmlOperator.getInactiveParameterNamesArray()[i];
					allXmlParameternames.add(pName);
					inactiveXmlParameternames.add(pName);
				}
				
				for ( String pName : allXmlParameternames) {
					if ( wasAnnotated( pName, xmlOperator.getParameterDescriptorsArray())) {
						if ( op.isAnnotatedParameter( pName) ) {
							restoreParameterStatus( pName, op, inactiveXmlParameternames, activeXmlParameternames);
						} else {
							// ignore a parameter which was annotated when writing the operator to xml
							// but is not existing or existing but not annotated in the operator object to restore
							if ( debug ) {
								System.out.println(" ALDParametrizedClassDataIOXmlbeans::readData ignore parameter <" +
										pName + "> which is annotated in xml but not annotated in the current operator object");
							}

						}
					} else {
						if ( op.isAnnotatedParameter( pName) ) {
							// ignore a parameter which was not annotated when writing the operator to xml
							// but is existing and annotated in the operator object to restore
							if ( debug ) {
								System.out.println(" ALDParametrizedClassDataIOXmlbeans::readData ignore parameter <" +
										pName + "> which is not  annotated in xml but annotated in the current operator object");
							}


						} else {
							if ( (! op.getParameterNames().contains(pName)) &&
							     (! op.getInactiveParameterNames().contains(pName)) ) {
								// there is no parameter description for this parameter, 
								// restore the parameter description from the xml representation
								for ( int i = 0 ; i < xmlOperator.getParameterDescriptorsArray().length ; i++ ) {
									if ( xmlOperator.getParameterDescriptorsArray(i).getKey().equals(pName) ) {
										ALDXMLObjectType elementXmlObject = xmlOperator.getParameterDescriptorsArray(i).getValue();
										ALDOpParameterDescriptor descr = 
												(ALDOpParameterDescriptor) ALDDataIOManagerXmlbeans.getInstance().readData( null, ALDOpParameterDescriptor.class,elementXmlObject);
										try {
											op.addParameter(descr);
										} catch (ALDOperatorException e) {
											throw new ALDDataIOProviderException( ALDDataIOProviderExceptionType.UNSPECIFIED_ERROR,
													"ALDParametrizedClassDataIOXmlbeans::readData internal error, cannot add parameter from descriptor <" +
															descr.name +">");
										}
									}
								}
								restoreParameterStatus(pName, op, inactiveXmlParameternames, activeXmlParameternames);
							}	
						}
					}
				}
			} else {
				// otherwise  we have a version 1 xml representation of the operators configuration  
				// nothing to do here (as the values are handled next
			}
		}
		
		// convert all name value pairs in the xml object
		if ( xmlParametrized.getPairsArray().length != 0) {
			// this map contains 
			// for an operator all parameters for which values have to be restored from xml and associated fields (maybe null)
			// all for a parametrized class all annotated member variables 
			HashMap<String,Field> fieldMap = null;

			// initialize fieldmap
			if ( isOperator) {
				fieldMap = new HashMap<String, Field>();
				for ( int i = 0 ; i < xmlParametrized.getPairsArray().length ; i++ )  {

					try {
						String name = xmlParametrized.getPairsArray(i).getKey();
						LinkedList<String> pNames = lookupParameternames( op, name);
						if ( pNames.size() > 0 ) {
							String pName = pNames.getFirst();
							if ( debug ) {
								System.out.println( "Use parameter <" + pName + ">" +
										" for <" + name + ">");
							}
							
							fieldMap.put( name, op.getParameterDescriptorUnconditioned( pName).getField());
						} else {
							// ignore parameter which do not exist in the operator instance to configure from xml
							if ( debug ) {
								System.out.println(" ALDParametrizedClassDataIOXmlbeans::readData ignore value of parameter <" +
										name + "> which does not exist in the current operator object");
							}
						}
						
					} catch (Exception e) {
						// handle this later consistently with parametrized class
					}
				}
					
			} else {
				fieldMap = ALDParametrizedClassDataIOHelper.getAnnotatedFields( obj.getClass());
			}

			// now handle the list of name-value pairs
			for ( int i = 0 ; i < xmlParametrized.getPairsArray().length ; i++ )  {
				String name = xmlParametrized.getPairsArray(i).getKey();
				if ( fieldMap.containsKey( name) ) {
					Field f = fieldMap.get( name);
					Object value = null;
					ALDXMLObjectType elementXmlObject = xmlParametrized.getPairsArray(i).getValue();
					
					if ( elementXmlObject != null) {
						try {
							if ( isOperator ) {
								ALDOpParameterDescriptor descr = op.getParameterDescriptorUnconditioned(name);
								value = ALDDataIOManagerXmlbeans.getInstance().readData( f, descr.getMyclass(), elementXmlObject);
							} else {
								value = ALDDataIOManagerXmlbeans.getInstance().readData( f, null, elementXmlObject);
							}
						} catch (ALDDataIOManagerException e) {
							throw new ALDDataIOManagerException( e.getType(), 
									"ALDParametrizedClassDataIOXmlbeans::readData cannot read element <" +
											name + "> \n    of class <" +
											f.getType().getCanonicalName() + ">\n    from <" + 
											elementXmlObject.toString() + ">" + 
											"\n" + e.getCommentString());
						} catch (ALDDataIOProviderException e) {
							throw new ALDDataIOProviderException( e.getType(), 
									"ALDParametrizedClassDataIOXmlbeans::readData cannot read element <" +
											name + ">\n     of class <" +
											f.getType().getCanonicalName() + ">\n     from <" + 
											elementXmlObject.toString() + ">" + 
											"\n" + e.getCommentString());
						} catch (ALDOperatorException e) {
							throw new ALDDataIOProviderException(ALDDataIOProviderExceptionType.SET_VALUE_FAILED, 
									"ALDParametrizedClassDataIOXmlbeans::readData cannot read descriptor for  <" +
											name + ">\n     of class <" +
											f.getType().getCanonicalName() + ">\n     from <" + 
											elementXmlObject.toString() + ">" + 
											"\n" + e.getCommentString());
						}
					}

					if ( debug ) {
						System.out.println(" ALDParametrizedClassDataIOXmlbeans::readData set <" +
								name + "> to value <" + value + ">");
					}
					
					if ( isOperator) {
						try {
							op.setParameterUnconditioned( name, value);
						} catch (ALDOperatorException e) {
							// ignore this error, maybe the type of the parameter has changed ....
							// TODO maybe we should log this error
							if ( debug ) {
								System.out.println(" ALDParametrizedClassDataIOXmlbeans::readData value for parameter <" +
										name + "> could not be set in the current operator object");
							}
						}

					} else {
						try {
							ALDParametrizedClassDataIOHelper.setValue( fieldMap.get( name), obj, value);

						} catch (IllegalAccessException e) {
							// ignore this error, maybe the type of the parameter has changed ....
							// TODO maybe we should log this error
							if ( debug ) {
								System.out.println(" ALDParametrizedClassDataIOXmlbeans::readData value for parameter <" +
										name + "> could not be set in tue current parametrized object");
							}
						}
					}

				} else { 
					
					if ( debug ) {
						System.out.println(" ALDParametrizedClassDataIOXmlbeans::readData value for parameter <" +
								name + "> exists in xml but no parameter with this name exists in the current object");
					}
				}
			}
		}
		
		if ( isOperator) {
			return op;
		}else {
			return obj;
		}
	}


	/**
	 *  Xmlbeans provider for parametrized classes and ALDOperators.
	 *  <p>
     * For parametrized classes values for all member variables are written
     * which are annotated with {@link de.unihalle.informatik.Alida.annotations.ALDClassParameter}.
     * 
     * <p>
     * For operators all IN and INOUT parameters the values are written for both active and inactive parameters
     * Names of all active and inactive parameters are written, as well as
     * parameter descriptors for not annotated parameters
	 * <p>
     * For both parametrized classes and operators transient members are not written.

	 * @throws ALDDataIOProviderException 
	 * @throws ALDDataIOManagerException 
	 */
	@Override
	public ALDXMLObjectType writeData(Object obj) 
			throws ALDDataIOProviderException, ALDDataIOManagerException {
		
		ALDXMLParametrizedType xmlParametrized = null;

		// map of parametername,field for all parameter for which the values have to be written to the xml representation
		HashMap<String,Field> fieldMap;
		if ( obj instanceof ALDOperator)  {
			ALDOperator op = ((ALDOperator)obj);

			if ( debug ) {
				System.out.println("ALDParametrizedClassDataIOXmlbeans::writeData write an ALDOperator");
			}

			ALDXMLOperatorVersion3Type xmlOperator = ALDXMLOperatorVersion3Type.Factory.newInstance();
			xmlParametrized = xmlOperator;

			xmlOperator.setOpName(((ALDOperator)obj).getName());

			// write all active parameter names into the xml object
			for ( String pName : op.getParameterNames() ) {
				xmlOperator.addActiveParameterNames(pName);
			}

			// write all active parameter names into the xml object
			for ( String pName : op.getInactiveParameterNames() ) {
				xmlOperator.addInactiveParameterNames(pName);
			}

			// as well as the associated descriptors for non annotated parameters
			List<String> allParameternames = new LinkedList<String>(op.getParameterNames());
			allParameternames.addAll(op.getInactiveParameterNames());

			for ( String pName : allParameternames) {
				if ( ! op.isAnnotatedParameter( pName) ) {

					ALDXMLObjectType xmlDescritptorValue =null;
					try {
						xmlDescritptorValue = ALDDataIOManagerXmlbeans.getInstance().writeData(
								op.getParameterDescriptorUnconditioned(pName));
					} catch (ALDOperatorException e) {
						throw new ALDDataIOProviderException( ALDDataIOProviderExceptionType.UNSPECIFIED_ERROR,
								"ALDParametrizedClassDataIOXmlbeans::writeData internal error: can not create xml parameter descriptor for <" + 
										pName + ">");
					}
					ALDXMLKeyValuePairType keyValuePair = xmlOperator.addNewParameterDescriptors();
					keyValuePair.setKey(pName);
					keyValuePair.setValue(xmlDescritptorValue);
				}
			}
			
			// collect name of all parameters for which values have to be written

			List<String> parameternamesToWrite = new LinkedList<String>();
			for ( String pName : allParameternames) {
				try {
					if ( op.getParameterDescriptorUnconditioned(pName).getDirection() != Direction.OUT ) {
						parameternamesToWrite.add( pName);
					}
				} catch (ALDOperatorException e) {
					throw new ALDDataIOProviderException( ALDDataIOProviderExceptionType.UNSPECIFIED_ERROR,
							"ALDParametrizedClassDataIOXmlbeans::writeData internal error: can not get descriptor and direction for <" + 
									pName + ">");
				}
			}

			fieldMap = new HashMap<String, Field>();
			for ( String pName : parameternamesToWrite ) {
				try {
					fieldMap.put( pName, ((ALDOperator)obj).getParameterDescriptorUnconditioned(pName).getField());
				} catch (ALDOperatorException e) {
					throw new ALDDataIOProviderException( ALDDataIOProviderExceptionType.UNSPECIFIED_ERROR,
							"ALDParametrizedClassDataIOXmlbeans::writeData internal error: can not get descriptor for <" + 
							pName + ">");
				} 
			}
			
		} else {
			// parametrized class
			if ( debug ) {
				System.out.println("ALDParametrizedClassDataIOXmlbeans::writeData write a parametrized class");
			}

			xmlParametrized = ALDXMLParametrizedType.Factory.newInstance();
			fieldMap = ALDParametrizedClassDataIOHelper.getAnnotatedFields( obj.getClass());
		}
		
		// write appropriate parameter values 
		// do not write transient parameters
		// take care for non member parameters which might occur in ALDOperators
		for ( String name : fieldMap.keySet() ) {
			if ( debug ) 
				System.out.println("ALDParametrizedClassDataIOXmlbeans::writeData field <" + name + ">");
			Field f = fieldMap.get( name);
			
			if ( f == null || ((f.getModifiers() & Modifier.TRANSIENT) == 0) ) {
				this.addParameter( name, obj, f, "-", xmlParametrized);
			}
		}

		xmlParametrized.setClassName(obj.getClass().getName());

		return xmlParametrized;
	}

	/** Format the parameter <code>name</code> of the object <code>obj</code> into the buffer <code>bufstr</code>
	 * using <code>formatString</code> to determine formating.
	 * 
	 * Version 2
	 *
	 * @param	name	parameter to be formated
	 * @param	obj	object for which to format parameter
	 * @param	field	field of parameter to be formated
	 * @param	xmlParametrized	String buffer to append formated parameter
	 * @throws ALDDataIOProviderException 
	 * @throws ALDDataIOManagerException 
	 */
	private void addParameter( String name, Object obj, Field field, String formatString, ALDXMLParametrizedType xmlParametrized) 
			throws ALDDataIOProviderException, ALDDataIOManagerException {
		
		if ( debug ) {
			System.out.println("ALDParametrizedClassDataIOXmlbeans::addParameter parameter name <" +
					name + ">");
		}
		
		Object value = null;
		try {
			if ( ALDOperator.class.isAssignableFrom( obj.getClass()) ) {
				value = ((ALDOperator)obj).getParameterUnconditioned(name);
			} else {
				value = ALDParametrizedClassDataIOHelper.getValue( field, obj);
			}
		} catch (Exception ex) {
			throw new ALDDataIOProviderException( ALDDataIOProviderExceptionType.UNSPECIFIED_ERROR,
					"ALDParametrizedClassDataIOXmlbeans::addParameter internal error, cannot get value of member variable <" +
							name +">");
		}

		ALDXMLObjectType xmlValue = ALDDataIOManagerXmlbeans.getInstance().writeData(value);
		ALDXMLKeyValuePairType keyValuePair = xmlParametrized.addNewPairs();
		keyValuePair.setKey(name);
		keyValuePair.setValue(xmlValue);
	}
	
	/**
	 * Restore the status of a parameter to the state as specified in the xml representation
	 * Prerequisite: pName is a parameter of the operator <code>op</code> and exiting in the xml representation
	 * 
	 * @param pName
	 * @param op
	 * @param inactiveParameternames
	 * @param activeParameternames
	 * @throws ALDDataIOProviderException
	 */
	private void restoreParameterStatus(String pName, ALDOperator op, Set<String> inactiveParameternames, Set<String> activeParameternames) 
			throws ALDDataIOProviderException {
		if ( op.getParameterNames().contains(pName) ) {
			if ( ! activeParameternames.contains(pName))
				try {
					op.removeParameter(pName);
				} catch (ALDOperatorException e) {
					throw new ALDDataIOProviderException( ALDDataIOProviderExceptionType.UNSPECIFIED_ERROR,
							"ALDParametrizedClassDataIOXmlbeans::restoreParameterVersion3 internal error, cannot remove parameter <" +
									pName +">");
				}
		} else if ( op.getInactiveParameterNames().contains(pName) ) {
			
			if ( ! inactiveParameternames.contains(pName)) 
				try {
					op.addParameter(pName);
				} catch (ALDOperatorException e) {
					throw new ALDDataIOProviderException( ALDDataIOProviderExceptionType.UNSPECIFIED_ERROR,
							"ALDParametrizedClassDataIOXmlbeans::restoreParameterVersion3 internal error, cannot add parameter <" +
									pName +">");
				}
		}
	}


	
	
	/** Lookup all parameter names of the operator with prefix <code>pre</code>.
     * If one of the parameters exactly matches <code>pre</code> only this single 
     * parameter name is returned.
     * @return All parameter names with prefix <code>pre</code> or the single parameter 
     *		exactly matching <code>pre</code> 
     */
	private static LinkedList<String> lookupParameternames( ALDOperator op, String pre ) {
		LinkedList<String> names = new LinkedList<String>();

		List<String> allParameters = new LinkedList<String>(op.getParameterNames());
		allParameters.addAll(op.getInactiveParameterNames());

		for ( String pName : allParameters ) {
			if ( pName.startsWith( pre) ) {
				names.add( pName);
			}
		}

		if ( names.size() > 1 ) {
		// try to find one exact match
			for ( String pName : allParameters ) {
				if ( pName.equals( pre) ) {
					names.clear();
					names.add( pName);
					break;
				}
			}
		}

		return names;
	}

	private static boolean wasAnnotated(String pName, ALDXMLKeyValuePairType[] parameterDescriptorsArray) {
		for ( int i = 0 ; i < parameterDescriptorsArray.length ; i++ ) {
			String pNameInXml = parameterDescriptorsArray[i].getKey();
			if ( pName.equals(pNameInXml) )
				return false;
		}
			
		return true;
	}

}
