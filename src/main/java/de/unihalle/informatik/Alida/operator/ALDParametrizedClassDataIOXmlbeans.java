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

import de.unihalle.informatik.Alida.dataio.ALDDataIOManagerXmlbeans;
import de.unihalle.informatik.Alida.dataio.provider.xmlbeans.ALDStandardizedDataIOXmlbeans;
import de.unihalle.informatik.Alida.dataio.provider.helpers.ALDInstantiationHelper;
import de.unihalle.informatik.Alida.dataio.provider.helpers.ALDParametrizedClassDataIOHelper;
import de.unihalle.informatik.Alida.dataio.provider.helpers.ALDParametrizedClassDummy;
import de.unihalle.informatik.Alida.exceptions.ALDDataIOManagerException;
import de.unihalle.informatik.Alida.exceptions.ALDDataIOProviderException;
import de.unihalle.informatik.Alida.exceptions.ALDDataIOProviderException.ALDDataIOProviderExceptionType;
import de.unihalle.informatik.Alida.exceptions.ALDOperatorException;
import de.unihalle.informatik.Alida.exceptions.ALDWorkflowException;
import de.unihalle.informatik.Alida.annotations.ALDDataIOProvider;
import de.unihalle.informatik.Alida.annotations.Parameter.Direction;
import de.unihalle.informatik.Alida_xml.ALDXMLKeyValuePairType;
import de.unihalle.informatik.Alida_xml.ALDXMLObjectType;
import de.unihalle.informatik.Alida_xml.ALDXMLOperatorType;
import de.unihalle.informatik.Alida_xml.ALDXMLOperatorWithDescriptorType;
import de.unihalle.informatik.Alida_xml.ALDXMLParametrizedType;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.Collection;
import java.util.LinkedList;
import java.util.HashMap;

/**
 * DataIO provider for parametrized classes and operators from command line.
 * <p>
 * For parametrized classes reading is done only for parameters annotated 
 * with {@link de.unihalle.informatik.Alida.annotations.ALDClassParameter}.
 * Either all annotated parameters are written/formated or a
 * subset as specified by a format string.
 * <p>
 * For operators reading is done only for IN and INOUT parameters.
 * In addition the set of active parameters and descriptors is set as represented in the
 * xml representation.
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
     * For parametrized classes each name has to be an annotated parameter.
     * <p>
     * For operators each name has to be an IN or INOUT parameter name of the operator and receives its value from
     * the <code>valueString</code>. Additionally the set of active parameters and descriptors is set.
     * 
	 * 
	 * @param field
	 * @param cl
	 * @param obj if non null the xml representation is to be parse in this  instance. 
	 *        otherwise a new instance is created. 
	 *        
	 * @return the object
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
		
		// for an operator read and set the active parameters and all associated descriptors form the xml configuration
		if ( isOperator ) {
			if ( ALDXMLOperatorWithDescriptorType.class.isAssignableFrom( aldXmlObject.getClass())) {
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
			}
		}
		
		// convert all name value pairs in the xml object
		if ( xmlParametrized.getPairsArray().length != 0) {
			// this map contains all parameters of the operator and associated fields (maybe null)
			// or all annotated member variables for a parametrized class
			HashMap<String,Field> fieldMap = null;

			// initialize fieldmap
			if ( isOperator) {
				fieldMap = new HashMap<String, Field>();
				for ( int i = 0 ; i < xmlParametrized.getPairsArray().length ; i++ )  {
					String name = xmlParametrized.getPairsArray(i).getKey();

					ALDOpParameterDescriptor descr = null;
					String pName = null;
					try {
						LinkedList<String> pNames = lookupParameternames( op, name);
						pName = pNames.getFirst();
						if ( debug ) {
							System.out.println( "Use parameter <" + pName + ">" +
									" for <" + name + ">");
						}
						descr = op.getParameterDescriptor( pName);
						//if ( descr.getDirection() == Direction.IN ||
						//		descr.getDirection() == Direction.INOUT ) {
							fieldMap.put( name, descr.getField());
						//}
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
								ALDOpParameterDescriptor descr = op.getParameterDescriptor(name);
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
							op.setParameter( name, value);
						} catch (ALDOperatorException e) {
							throw new ALDDataIOProviderException( ALDDataIOProviderExceptionType.UNSPECIFIED_ERROR,
									"ALDParametrizedClassDataIOXmlbeans::readData internal error, cannot set value of member variable <" +
											name +">");
						}

					} else {
						try {
							ALDParametrizedClassDataIOHelper.setValue( fieldMap.get( name), obj, value);

						} catch (IllegalAccessException e) {
							throw new ALDDataIOProviderException( ALDDataIOProviderExceptionType.UNSPECIFIED_ERROR,
									"ALDParametrizedClassDataIOXmlbeans::readData internal error, cannot set value of member variable <" +
											name +">");
						}
					}

				} else { // unknown parameter name
					if ( ! ALDDataIOManagerXmlbeans.getInstance().isAllowAdditionalFields()) {
						StringBuffer msg = new StringBuffer("   existing parameters:");
						for ( String key : fieldMap.keySet() ) 
							msg.append( "         " + key);

						throw new ALDDataIOProviderException( ALDDataIOProviderExceptionType.SYNTAX_ERROR,
								"ALDParametrizedClassDataIOXmlbeans::readData " + 
										(isOperator ? op : obj).getClass().getName() +
										" does not contain a parameter " + name +
										new String( msg));
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
	 * Transient members are not written.
	 * <p>
     * For parametrized classes annotated members are written.
     * <p>
     * For operators all IN and INOUT parameters are written.
     * In addition for all parameters the names written and the corresponding
     * parameter descriptors for not annotated parameters
     * 
	 * @throws ALDDataIOProviderException 
	 * @throws ALDDataIOManagerException 
	 */
	@Override
	public ALDXMLObjectType writeData(Object obj) 
			throws ALDDataIOProviderException, ALDDataIOManagerException {
		
		ALDXMLParametrizedType xmlParametrized;

		HashMap<String,Field> fieldMap;
		if ( obj instanceof ALDOperator)  {
			ALDOperator op = ((ALDOperator)obj);

			if ( debug ) {
				System.out.println("ALDParametrizedClassDataIOXmlbeans::writeData write an ALDOperator");
			}

			ALDXMLOperatorWithDescriptorType xmlOperator = ALDXMLOperatorWithDescriptorType.Factory.newInstance();
			xmlOperator.setOpName(((ALDOperator)obj).getName());
			
			// write all (current/active) parameter names into the xml object
			// as well as the associated descriptors for non annotated parameters
			for ( String pName : op.getParameterNames() ) {
				xmlOperator.addParameterNames(pName);
				
				if ( ! op.isAnnotatedParameter( pName) ) {
					
					ALDXMLObjectType xmlValue =null;
					try {
						xmlValue = ALDDataIOManagerXmlbeans.getInstance().writeData(
								op.getParameterDescriptor(pName));
					} catch (ALDOperatorException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					ALDXMLKeyValuePairType keyValuePair = 
							xmlOperator.addNewParameterDescriptors();
					keyValuePair.setKey(pName);
					keyValuePair.setValue(xmlValue);

				}
					
			}
			
			// collect name of all parameters for which values have to be written
			xmlParametrized = xmlOperator;
			
			// all non-supplemental in and inout parameters
			Collection<String> pNames = op.getInInoutNames();
			
			// add supplemental in and inout parameters
			for ( String pName :  op.getSupplementalNames() ) {
				try {
					if ( op.getParameterDescriptor(pName).getDirection() == Direction.IN ||
							op.getParameterDescriptor(pName).getDirection() == Direction.INOUT)
						pNames.add( pName);
				} catch (ALDOperatorException e) {
					throw new ALDDataIOProviderException( ALDDataIOProviderExceptionType.UNSPECIFIED_ERROR,
							"ALDParametrizedClassDataIOXmlbeans::writeData internal error: can not get descriptor for <" + 
							pName + ">");
				}
			}
			
			fieldMap = new HashMap<String, Field>();

			for ( String pName : pNames ) {
				try {
					fieldMap.put( pName, ((ALDOperator)obj).getParameterDescriptor(pName).getField());
				} catch (ALDOperatorException e) {
					throw new ALDDataIOProviderException( ALDDataIOProviderExceptionType.UNSPECIFIED_ERROR,
							"ALDParametrizedClassDataIOXmlbeans::writeData internal error: can not get descriptor for <" + 
							pName + ">");
				} 
			}
			
		} else {
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
			if ( f == null || (f.getModifiers() & Modifier.TRANSIENT) == 0) {
				this.addParameter( name, obj, f, "-", xmlParametrized);
			}
		}

		xmlParametrized.setClassName(obj.getClass().getName());

		return xmlParametrized;
	}

	/** Format the parameter <code>name</code> of the object <code>obj</code> into the buffer <code>bufstr</code>
	 * using <code>formatString</code> to determine formating.
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
				value = ((ALDOperator)obj).getParameter(name);
			} else {
				value = ALDParametrizedClassDataIOHelper.getValue( field, obj);
			}
		} catch (Exception ex) {
			throw new ALDDataIOProviderException( ALDDataIOProviderExceptionType.UNSPECIFIED_ERROR,
					"ALDParametrizedClassDataIOXmlbeans::addParameter internal error, cannot get value of member variable <" +
							name +">");
		}

		ALDXMLObjectType xmlValue = ALDDataIOManagerXmlbeans.getInstance().writeData(value);
		ALDXMLKeyValuePairType keyValuePair = 
				xmlParametrized.addNewPairs();
		keyValuePair.setKey(name);
		keyValuePair.setValue(xmlValue);
	}
	
	/** Lookup all parameter names of the operator with prefix <code>pre</code>.
     * If one of the parameters exactly matches <code>pre</code> only this single 
     * parameter name is returned.
     * @return All parameter names with prefix <code>pre</code> or the single parameter 
     *		exactly matching <code>pre</code> 
     */
	public static LinkedList<String> lookupParameternames( ALDOperator op, String pre ) {
		LinkedList<String> names = new LinkedList<String>();

		for ( String pName : op.getParameterNames() ) {
			if ( pName.startsWith( pre) ) {
				names.add( pName);
			}
		}

		if ( names.size() > 1 ) {
		// try to find one exact match
			for ( String pName : op.getParameterNames() ) {
				if ( pName.equals( pre) ) {
					names.clear();
					names.add( pName);
					break;
				}
			}
		}

		return names;
	}

}
