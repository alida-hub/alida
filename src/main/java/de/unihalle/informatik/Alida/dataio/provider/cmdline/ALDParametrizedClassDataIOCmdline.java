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

package de.unihalle.informatik.Alida.dataio.provider.cmdline;

import de.unihalle.informatik.Alida.dataio.ALDDataIOManagerCmdline;
import de.unihalle.informatik.Alida.dataio.provider.ALDDataIOCmdline;
import de.unihalle.informatik.Alida.dataio.provider.helpers.ALDParametrizedClassDataIOHelper;
import de.unihalle.informatik.Alida.dataio.provider.helpers.ALDParametrizedClassDummy;
import de.unihalle.informatik.Alida.exceptions.ALDDataIOManagerException;
import de.unihalle.informatik.Alida.exceptions.ALDDataIOProviderException;
import de.unihalle.informatik.Alida.exceptions.ALDDataIOProviderException.ALDDataIOProviderExceptionType;
import de.unihalle.informatik.Alida.exceptions.ALDOperatorException;
import de.unihalle.informatik.Alida.helpers.ALDParser;
import de.unihalle.informatik.Alida.operator.ALDOpParameterDescriptor;
import de.unihalle.informatik.Alida.operator.ALDOperator;
import de.unihalle.informatik.Alida.annotations.ALDDataIOProvider;
import de.unihalle.informatik.Alida.annotations.Parameter.Direction;

import java.lang.reflect.Field;
import java.util.Collection;
import java.util.LinkedList;
import java.util.HashMap;

/**
 * DataIO provider for parametrized classes and operators from command line.
 * As this provider extends {@link ALDStandardizedDataIOCmdline} it
 * implements the Alida syntax conventions.
 * <p>
 * For parametrized classes reading is done only for parameters annotated 
 * with {@link de.unihalle.informatik.Alida.annotations.ALDClassParameter}.
 * Either all annotated parameters are written/formated or a
 * subset as specified by a format string.
 * <p>
 *  For oeprators reading is done only for IN and INOUT parameters.
 * Either all OUT and INPUT parameters are written/formated or a
 * subset as specified by a format string.

 * 
 * @author posch
 *
 */
@ALDDataIOProvider
public class ALDParametrizedClassDataIOCmdline extends ALDStandardizedDataIOCmdline {

	/**
	 * debug messages
	 */
	private boolean debug= false;

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

	/** Parser for parametrized classes and ALDOperators. 
	 * Expects a comma separated list of name=value pairs enclosed in curly brackets.
     * For the class of the object to be read see {@link ALDDataIOCmdline}.
     * <p>
     * For parametrized classes each name has to be an annotated parameter.
     * <p>
     * For operators each name has to be an IN or INOUT parameter name of the operator and receives its value from
     * the <code>valueString</code>.
     * <p>
	 * If the list of name=value pairs is empty, i.e. no parameters are to be parsed,
     * the empty string is accepted, too.
	 * @throws ALDDataIOProviderException 
	 * @throws ALDDataIOManagerException 
	 */
	@Override
  	public Object parse(Field field, Class<?> cl, String valueString) 
  			throws ALDDataIOProviderException, ALDDataIOManagerException {
		if ( debug ) {
			System.out.println( "ALDParametrizedClassDataIOCmdline::parse using " + valueString);
		}

		return parse( field, cl, valueString, null);
	}
	
	/** Generic Parser for parametrized classes and ALDOperators.
	 * Expects a comma separated list of name=value pairs enclosed in curly brackets.
     * For the class of the object to be read see {@link ALDDataIOCmdline}.
     * <p>
     * For parametrized classes each name has to be an annotated parameter.
     * <p>
     * For operators each name has to be an IN or INOUT parameter name of the operator and receives its value from
     * the <code>valueString</code>.
     * <p>
	 * If the list of name=value pairs is empty, i.e. no parameters are to be parsed,
     * the empty string is accepted, too.
     * <p>
	 * For the class of the object to be read see {@link ALDDataIOCmdline}.
	 * As a <code>valueString</code> a comma separated list of name=value pairs enclosed in curly brackets
	 * is expected. Each name has to be a member of the class or a super class which is annotated
	 * with {@link de.unihalle.informatik.Alida.annotations.ALDClassParameter}.
	 * The <code>readData</code> method of the provider for the class of the member variable
	 * is used to read the objects value from <code>value</code>.
	 * 
	 * @param field
	 * @param cl
	 * @param valueString
	 * @param op if a ALDOperator is to be parse this is an instance of this class. Ignored when parsing 
	 * a parametrized class
	 * @return
	 * @throws ALDDataIOProviderException
	 * @throws ALDDataIOManagerException
	 */
	public Object parse(Field field, Class<?> cl, String valueString, ALDOperator op) 
			throws ALDDataIOProviderException, ALDDataIOManagerException {
		
		if ( field != null )
			cl = field.getType();

		// if op != null we assume that parse was called from oprunner
		// and generate slightly different exception comments
		boolean calledFromOprunner;
		if ( op != null ) 
			calledFromOprunner = true;
		else
			calledFromOprunner = false;
		
		boolean parseOp;
		if ( ALDOperator.class.isAssignableFrom(cl) ) 
			parseOp = true;
		else
			parseOp = false;
		
		valueString = valueString.trim();
		if ( valueString.equals("") ) {
			valueString = "{}";
		}

		String pairStr;
		if ( valueString.charAt(0) != '{' ||
				(pairStr = ALDParser.parseBracket( valueString, '}')) == null ) {
			throw new ALDDataIOProviderException( ALDDataIOProviderExceptionType.SYNTAX_ERROR,
					"ALDParametrizedClassDataIOCmdline::parse cannot find matching {} in " + 
							valueString);
		}
		
		if ( debug ) {
			System.out.println("ALDParametrizedClassDataIOCmdline::parse for " +
					(parseOp ? "an opertor" : " a parametrized class" +
							" with pair string <" + pairStr + ">"));
		}
		
		// initialize object to return
		Object pClassObj = null;
		if ( parseOp ) {
			if ( op == null ) {
				try {
					op = (ALDOperator)(cl.newInstance());
				} catch (Exception e ) {
					throw new ALDDataIOProviderException( ALDDataIOProviderExceptionType.OBJECT_INSTANTIATION_ERROR,
							"ALDParametrizedClassDataIOCmdline::parse cannot instantiate class <" + 
									cl.getCanonicalName() + ">");
				} 
			}
		} else {
			try {
				pClassObj = cl.newInstance();
			} catch (Exception e ) {
				throw new ALDDataIOProviderException( ALDDataIOProviderExceptionType.OBJECT_INSTANTIATION_ERROR,
						"ALDParametrizedClassDataIOCmdline::parse cannot instantiate class <" + 
								cl.getCanonicalName() + ">");
			} 
		}

		// parse the value string
		if ( ! pairStr.trim().equals("") ) {
			HashMap<String,String> nameValuePairs = ALDParser.parseNameValuePairs( pairStr.trim());
			HashMap<String,Field> fieldMap = null;
			HashMap<String, Class<?>> classMap = new HashMap<String, Class<?>>(0);


			// initialize fieldmap
			if ( parseOp) {
				fieldMap = new HashMap<String, Field>();
				for ( String name : nameValuePairs.keySet() )  {
					String paramValueString = nameValuePairs.get( name);

					ALDOpParameterDescriptor descr = null;
					String pName = null;
					try {
						LinkedList<String> pNames = lookupParameternames( op, name);
						pName = pNames.getFirst();
						if ( debug ) {
							System.out.println( "Use parameter <" + pName + ">" +
									" for <" + name + "> using <" + paramValueString + "> as value");
						}
						descr = op.getParameterDescriptor( pName);
						if ( descr.getDirection() == Direction.IN ||
								descr.getDirection() == Direction.INOUT ) {
							fieldMap.put( name, descr.getField());
							classMap.put(pName, descr.getMyclass());
						}
					} catch (Exception e) {
						// handle this later consistently with parametrized class
					}
				}
					
			} else {
				fieldMap = ALDParametrizedClassDataIOHelper.getAnnotatedFields( cl);
			}

			// now parse the list of name-value pairs
			for ( String name : nameValuePairs.keySet() )  {
				if ( fieldMap.containsKey( name) ) {
					// field f may be null
					Field f = fieldMap.get( name);
					// parameter class may be null
					Class<?> pClass = classMap.get(name);
					Object value;
					String elementValueString = nameValuePairs.get( name);

					try {
						value = ALDDataIOManagerCmdline.getInstance().readData( f, pClass,elementValueString);
					} catch (ALDDataIOManagerException e) {
						throw new ALDDataIOManagerException( e.getType(), 
								"ALDParametrizedClassDataIOCmdline::parse cannot read element <" +
										name + "> \n    of class <" +
										(f != null ? f.getType().getCanonicalName() : pClass) + 
										">\n from <" + elementValueString + ">" + 
										(!calledFromOprunner ? 
												"\nwithin <" + valueString + ">\n" : "") +
												"\n" + e.getCommentString());
					} catch (ALDDataIOProviderException e) {
						throw new ALDDataIOProviderException( e.getType(), 
								"ALDParametrizedClassDataIOCmdline::parse cannot read element <" +
										name + ">\n     of class <" +
										(f != null ? f.getType().getCanonicalName() : pClass) + 
										">\n     from <" + elementValueString + ">" + 
										(!calledFromOprunner ? 
												"\nwithin <" + valueString + ">\n" : "") +
												"\n" + e.getCommentString());

					}

					if ( parseOp) {
						try {
							op.setParameter( name, value);
						} catch (ALDOperatorException e) {
							throw new ALDDataIOProviderException( ALDDataIOProviderExceptionType.UNSPECIFIED_ERROR,
									"ALDParametrizedClassDataIOCmdline::parse internal error, cannot set value of member variable <" +
											name +">");
						}

					} else {
						try {
							ALDParametrizedClassDataIOHelper.setValue( fieldMap.get( name), pClassObj, value);

						} catch (IllegalAccessException e) {
							throw new ALDDataIOProviderException( ALDDataIOProviderExceptionType.UNSPECIFIED_ERROR,
									"ALDParametrizedClassDataIOCmdline::parse internal error, cannot set value of member variable <" +
											name +">");
						}
					}

				} else { // unknown parameter name
					StringBuffer msg = new StringBuffer("   existing parameters:");
					for ( String key : fieldMap.keySet() ) 
						msg.append( "         " + key);

					throw new ALDDataIOProviderException( ALDDataIOProviderExceptionType.OBJECT_TYPE_ERROR,
							"ALDParametrizedClassDataIOCmdline::parse" + 
									(parseOp ? op : pClassObj).getClass().getName() +
									" does not contain a parameter " + name +
									new String( msg));
				}
			}
		}
		
		if ( parseOp)
			return op;
		else
			return pClassObj;
	}

	/**
	 * Format all parameters of this parametrized class annotated
	 * with {@link de.unihalle.informatik.Alida.annotations.ALDClassParameter}
	 * into a string.
	 * If <code>formatString</code> starts with a curly bracket it is assume
	 * to contained a comma seprated list of name=value pairs enclosed in a
	 * matching curly brackets.
	 * In this case, only the (annotated) members named in this list are formated where the <code>value</code>
	 * is passed to the <code>writeData</code> of the dataIO provider handling the parameter's type.
	 * In extension, is a name equals <code>*</code> all members non listed in the 
	 * <code>formatString</code> are formated using the <code>value</code> of this pair.
	 *<p>
	 * If <code>formatString</code> does not start with a curly bracket all annotated
	 * members are formated.
	 *
	 * @param obj	parametrized class to be formated
	 * @param formatString	
	 * @throws ALDDataIOProviderException 
	 * @throws ALDDataIOManagerException 
	 */
	public String formatAsString(Object obj, String formatString) 
			throws ALDDataIOProviderException, ALDDataIOManagerException {
		
		if ( formatString == null ) {
			return this.formatAsString( obj);
		} else {

			// if we do not find an opening bracket, format without format string
			if ( formatString.trim().charAt(0) != '{' ) {
				return formatAsString( obj);
			}

			String pairStr = null;
			// if we cannot parse  brackets, format without format string
			if ( (pairStr = ALDParser.parseBracket( formatString)) == null ) {
				return formatAsString( obj);
			}

			HashMap<String,String>  nameValuePairs = ALDParser.parseNameValuePairs( pairStr);
			HashMap<String,Field> fieldMap;
			if ( obj instanceof ALDOperator)  {
				fieldMap = new HashMap<String, Field>();
				for ( String pName : ((ALDOperator)obj).getOutInoutNames() ) {
					try {
						fieldMap.put( pName, ((ALDOperator)obj).getParameterDescriptor(pName).getField());
					} catch (ALDOperatorException e) {
						throw new ALDDataIOProviderException( ALDDataIOProviderExceptionType.UNSPECIFIED_ERROR,
								"ALDParametrizedClassDataIOCmdline::formatAsString internal error: can not get descriptor for <" + 
								pName + ">");
					} 
				}
			} else {
				fieldMap = ALDParametrizedClassDataIOHelper.getAnnotatedFields( obj.getClass());
			}
			
			StringBuffer bufstr = new StringBuffer( "{ ");
			boolean foundAsterix = false;
			String generalFormatString = null;

			for ( String name : nameValuePairs.keySet() ) {
				if ( name.equals( "*") ) {
					foundAsterix = true;
					generalFormatString = nameValuePairs.get( name);
					continue;
				}
				if ( fieldMap.containsKey( name) ) {
					this.addParameter( name, obj, fieldMap.get( name), nameValuePairs.get( name), bufstr);
				} else {
					StringBuffer msg = new StringBuffer("\n   existing members:\n");
					for ( String key : fieldMap.keySet() ) 
						msg.append( "         " + key + "\n");

					throw new ALDDataIOProviderException( ALDDataIOProviderExceptionType.OBJECT_TYPE_ERROR,
							"ALDParametrizedClassDataIOCmdline::formatAsString class <" + 
									obj.getClass().getName() +
							">\n   does not contain an annotated member <" + name + ">"+
							new String( msg));
				}
			}

			if ( foundAsterix ) {
				for ( String name : fieldMap.keySet() ) {
					if ( ! nameValuePairs.containsKey( name) ) {
						this.addParameter( name, obj, fieldMap.get( name), generalFormatString, bufstr);
					}
				}
			}
			if ( bufstr.length() > 3) 
				return new String( bufstr.delete( bufstr.length()-3,  bufstr.length()).append(" }"));
			else
				return new String( bufstr.append(" }"));
		}
	}

	/**
	 * Generic formatter to string of parametrized classes.
	 * Output all annotated members of the class annotated
	 * with {@link de.unihalle.informatik.Alida.annotations.ALDClassParameter}.
	 * @throws ALDDataIOProviderException 
	 * @throws ALDDataIOManagerException 
	 */
	@Override
	public String formatAsString(Object obj) 
			throws ALDDataIOProviderException, ALDDataIOManagerException {
		StringBuffer bufstr = new StringBuffer( "{ ");

		HashMap<String,Field> fieldMap;
		if ( obj instanceof ALDOperator)  {
			fieldMap = new HashMap<String, Field>();
			for ( String pName : ((ALDOperator)obj).getOutInoutNames() ) {
				try {
					fieldMap.put( pName, ((ALDOperator)obj).getParameterDescriptor(pName).getField());
				} catch (ALDOperatorException e) {
					throw new ALDDataIOProviderException( ALDDataIOProviderExceptionType.UNSPECIFIED_ERROR,
							"ALDParametrizedClassDataIOCmdline::formatAsString internal error: can not get descriptor for <" + 
							pName + ">");
				} 
			}
		} else {
			fieldMap = ALDParametrizedClassDataIOHelper.getAnnotatedFields( obj.getClass());
		}
		
		for ( String name : fieldMap.keySet() ) {
			this.addParameter( name, obj, fieldMap.get( name), "-", bufstr);
		}

		if ( bufstr.length() > 3) 
			return new String( bufstr.delete( bufstr.length()-3,  bufstr.length()).append(" }")); 
		else			
			return new String( bufstr.append(" }"));
	}

	/** Format the parameter <code>name</code> of the object <code>obj</code> into the buffer <code>bufstr</code>
	 * using <code>formatString</code> to determine formating.
	 *
	 * @param	name	parameter to be formated
	 * @param	obj	object for which to format parameter
	 * @param	field	field of parameter to be formated
	 * @param	bufstr	String buffer to append formated parameter
	 * @throws ALDDataIOProviderException 
	 * @throws ALDDataIOManagerException 
	 */
	private void addParameter( String name, Object obj, Field field, String formatString, StringBuffer bufstr) 
			throws ALDDataIOProviderException, ALDDataIOManagerException {
		Object value = null;
		try {
			value = ALDParametrizedClassDataIOHelper.getValue( field, obj);
		} catch (Exception ex) {
			throw new ALDDataIOProviderException( ALDDataIOProviderExceptionType.UNSPECIFIED_ERROR,
					"ALDParametrizedClassDataIOCmdline::addParameter internal error, cannot get value of member variable <" +
							name +">");
		}

		String str;
		str= ALDDataIOManagerCmdline.getInstance().writeData(value,formatString);
		if ( str != null ) {
			bufstr.append( name + "=" + str + " , ");
		} else {
			bufstr.append( name + " written using " + formatString + " , ");
		}
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
