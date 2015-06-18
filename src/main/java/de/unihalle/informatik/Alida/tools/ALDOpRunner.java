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

package de.unihalle.informatik.Alida.tools;

import de.unihalle.informatik.Alida.annotations.ALDAOperator;
import de.unihalle.informatik.Alida.annotations.Parameter;
import de.unihalle.informatik.Alida.annotations.indexing.SezPozAdapter;
import de.unihalle.informatik.Alida.dataio.ALDDataIOManagerCmdline;
import de.unihalle.informatik.Alida.dataio.provider.ALDDataIOCmdline;
import de.unihalle.informatik.Alida.dataio.provider.cmdline.ALDParametrizedClassDataIOCmdline;
import de.unihalle.informatik.Alida.exceptions.ALDDataIOManagerException;
import de.unihalle.informatik.Alida.exceptions.ALDDataIOProviderException;
import de.unihalle.informatik.Alida.exceptions.ALDException;
import de.unihalle.informatik.Alida.exceptions.ALDOperatorException;
import de.unihalle.informatik.Alida.helpers.ALDParser;
import de.unihalle.informatik.Alida.operator.ALDOpParameterDescriptor;
import de.unihalle.informatik.Alida.operator.ALDOperator;
import de.unihalle.informatik.Alida.operator.events.ALDOperatorExecutionProgressEvent;
import de.unihalle.informatik.Alida.operator.events.ALDOperatorExecutionProgressEventListener;
import net.java.sezpoz.Index;
import net.java.sezpoz.IndexItem;

import java.util.*;
import java.util.regex.*;

/**
 * Generic commandline interface to run an Alida operator.
 * <p>
 * An operator needs to be annotated to allow execution mode CMDLINE.
 * <p>
 * Reading of IN and INOUT parameters and writing of OUT and INOUT parameters
 * is accomplished using the interface 
 * {@link de.unihalle.informatik.Alida.dataio.provider.ALDDataIOCmdline} 
 * in conjunction with {@link ALDDataIOManagerCmdline}.
 * For each parameter which should be read or written according to its
 * annotated <code>direction</code>, a name=value pair has to be given as 
 * argument.
 * <p>
 * If the flag <code>--donotrun</code> or <code>-n</code> is given, only the 
 * parameters of the operator and there details will be printed but the 
 * operator is not invoked.
 * <p>
 * The flag <code>noDefaultHistory</code> specifies that not for each input 
 * parameter a history file should be read and associated with the parameter 
 * for all data types (irrespective whether the corresponding provider does so).
 * Likewise this flags specifies if a history is to be written for all output 
 * parameters.
 * <p>
 * If the flag <code>--verbose</code> or <code>-v</code> the interface of the
 * operator will be printed to stdout (and potentially further information).
 * In addition the flag <code>--showProgress</code> or <code>-s</code> is 
 * available to switch on display of progress messages.
 *
 * @author Stefan Posch
 *
 */


public class ALDOpRunner implements ALDOperatorExecutionProgressEventListener {

	/** If true, the operator will not be invoked but only its 
	 * interface printed to stdout.
	 */
	private boolean donotrun;

	/**
	 * If true, history files are not read and written by default.
	 */
	private boolean noDefaultHistory = false;

	/** If true, matching of operator name from commandline to the
	 * annoteated ALDOperators uses regular expression matching,
	 * otherwise exact substring matching
	 */
	protected boolean useRegEx;

	/** verbose flag to outout additional information to stdout.
	 */
	private boolean verbose;

	/** 
	 * Flag to enable/disable display of progress events.
	 * <p>
	 * The flag is set to false by default and can be set to true using the 
	 * command line parameters <code>--showProgress</code> or <code>-s</code>.
	 */
	private boolean showProgressEvents;

	/** debug flag for additional debuging information.
     */
    private boolean debug;

	/** command line arguments.
     */
	private String [] args;

	/** The operator to be invoked.
     */
    private ALDOperator op;
    
    /** List of fully qualified class names of operators available
     */
    protected LinkedList<String> matchingClassNames= null;
    

	/** Hashmap to collect parameter name as given on command line and 
	 * correspnding value string
     */
	private HashMap<String,String> nameValueMap;

	/** Hashmap to collect parameter name as given on command line and matching
	 * parameter name.
     */
	private HashMap<String,String> nameParameterMap;

	/** Print short usage to stderr.
	 */
	private static void printUsage() {
		System.err.println( "Usage: ALDOpRunner " +
				"[-u|--usage] " +
				"[-v|--verbose] [-s|--showProgress] [-d|--debug]\n" + 
				"\t[-n|--donotrun] " +
				"\t[-m|--modifyParameters] " +
				"\t[--noDefaultHistory" +
				"\t[-r|--useRegEx]\n" + 
				"\tAlidaOperatorClassname {parametername=valuestring}*"
				);
	}

	/** Print verbose usage to stderr.
     */
	private void printUsageVerbose() {
		printUsage();
		System.err.print( "\n" + 
				 "-u or --usage     prints this explanation\n" +
				 "-d or --debug     debug information of internal processing is printed to stdout\n" +
				 "\n" +
				 "Generic commandline tools to execute an Alida operator.\n" +
				 "An Alida operator can only be executed if it supplies a public standard constructor.\n" +
				 "\n" +
				 "The operator is looked up with the following procedure:\n" +
				 "First annotated operators matching <AlidaOperatorClassname> are searched.\n" +
				 "If the option -r or --useRegEx is given <AlidaOperatorClassname> is interpreted as a Java regular expression.\n" +
				 "Otherwise an annotated operators with <AlidaOperatorClassname> as an exact exact is looked for.\n" +
				 "If this does not exist all annotated operators with <AlidaOperatorClassname> as an exact substring are looked up.\n" +
				 "If the latter results in more than one matching operator, operators with <AlidaOperatorClassname>\n" +
				 "as a suffix are found. If not exactly one match is found substring matches are retained.\n" +
				 "\n" +
				 "If the above procedure returns not exactly one matching annotated operator, a class\n" +
				 "with name <AlidaOperatorClassname> is instantiated as the\n" +
				 "operator of interest may not have been annotated\n" +
				 "or is still ambigous.\n" +
				 "If this fails the previous (maybe empty list) of operators is retained.\n" +
				 "\n" +
				 "If this procedure returns not exactly one operator, the (maybe empty list)\n" +
				 "is printed to stdout and ALDOpRunner stops execution\n" +
				 "\n" +
				 "Otherwise the operator is intantiated.\n +"
				 + "If -n or --donotrun is given the interface is printed to stdout " +
				 "and the oprunner exits\n" +
				 "\n" +
				 "Subsequently the parameters given on the command line are validated agains the operators interface.\n" +
 				 "For each parameter which should be read or written accoring to its\n" +
 				 "annotated direction, a argument as name=value pair has to be specified.\n" +
				 "The parametername specified on the command line is interpreted as a prefix of\n" +
				 "a parameter of the interface of the operator.\n" +
				 "\n" +
				 "Subsequently the operator is execute unless the option -n and --donotrun are given.\n" +
				 "\n" +
				 "Examples\n" +
				 "\tPrint all annotated Alida operators:\n" +
				 "\t\tjava de/unihalle/informatik/Alida/tools/ALDOpRunner -r \'.*\'\n" +
				 "\tPrint all annotated Alida operators with suffix PDE:\n" +
				 "\t\tjava de/unihalle/informatik/Alida/tools/ALDOpRunner -r -n '.*PDE$'\n" +
				 "\tPrint interface of an operator:\n" +
				 "\t\tjava de/unihalle/informatik/Alida/tools/ALDOpRunner -v -n Dilate\n" +
				 "\tExecute ImgDilate:\n" +
				 "\t\tjava de/unihalle/informatik/Alida/tools/ALDOpRunner Dilate in=in.tif res=out.tif mask=3\n" +

				""
		);
	}

	/** Construct a <code>ALDOpRunner</code> using <code>args</code>
     *
     * @param arguments	command line arguments
	 */
	public ALDOpRunner( String [] arguments) {
		this.verbose = false;
		this.showProgressEvents = false;
		this.debug = false;
		this.donotrun = false;
		this.noDefaultHistory = false;
		this.useRegEx = false;
		this.args = arguments;
	}

	/** Main routine of <code>ALDOpRunner</code> , see usage.
	 *
	 * @param args	command line arguments
	 */
	public static void main(String [] args) {
		// init the SezPoz adapter properly
		SezPozAdapter.initAdapter();
		// start the application
		ALDOpRunner runner = new ALDOpRunner( args);
		runner.runIt( );
	}

	/** This method does the complete work to scan arguments, read and write parameters
     * and <code>runOp</code> the operator.
     */
	public void runIt() {
        // retrieve command line options
		int i = 0;
		while ( i < this.args.length && this.args[i].charAt(0) == '-') {
			if ( this.args[i].equals( "-u") || this.args[i].equals( "--usage") ||
			     this.args[i].equals( "-h") || this.args[i].equals( "--help") ) {
				printUsageVerbose();
				System.exit(0);
			} else if ( this.args[i].equals( "-v") || this.args[i].equals( "--verbose") ) {
				this.verbose = true;
			} else if (   this.args[i].equals( "-s") 
								 || this.args[i].equals( "--showProgress") ) {
				this.showProgressEvents = true;
			} else if ( this.args[i].equals( "-d") || this.args[i].equals( "--debug") ) {
				this.debug = true;
			} else if ( this.args[i].equals( "-n") || this.args[i].equals( "--donotrun") ) {
				this.donotrun = true;
			} else if ( this.args[i].equals( "--noDefaultHistory") ) {
				this.noDefaultHistory = true;
			} else if ( this.args[i].equals( "-r") || this.args[i].equals( "--useRegEx") ) {
				this.useRegEx = true;
			} else {
				System.err.println( "ERROR: unknown option " + this.args[i]);
				System.err.println( "");
				printUsage();
				System.exit(2);
			}
			i++;
		}

        // at least one remaining arguments?
        if ( i >= this.args.length ) {
            printUsage();
            System.exit(2);
        }

		// find the operator
        findOperators( this.args[i]);
	

		if ( this.matchingClassNames.size() == 0 ) {
			System.err.println( "found no matching ALDOperator for <" + this.args[i] + ">"  +
                " using " + (this.useRegEx ? "" : "NOT") + " regular expressions");
			System.exit(2);
		} else if ( this.matchingClassNames.size() > 1 ) {
			System.out.println( "found more then one matching ALDOperator for <" + this.args[i] + ">" +
                " using " + (this.useRegEx ? "" : "NOT") + " regular expressions");
			for ( String opName : this.matchingClassNames ) 
				System.out.println( "    " + opName);
			System.exit(2);
		}

		// instantiate the operator
        this.op = null;
		String opName = this.matchingClassNames.peek();
		if ( this.verbose )
			System.out.println( "About to instantiate the ALDOperator <" + opName + ">");

		this.op = getOperator( opName); 

		if ( this.verbose ) {
			this.op.print();
		}

		// do the work
		if ( ! this.donotrun ) {
			int firstIndex = i+1;
			
			// validate and get parameters which may modify parameter descriptors
			// and set their values from the value string
			validateParameternames( firstIndex, true);
			readParameterValues( firstIndex, true);
			
			if ( verbose )
				this.op.printInterface();

			boolean oldHistoryState = ALDDataIOManagerCmdline.getInstance().isDoHistory();
			if ( ! this.noDefaultHistory ) {
				ALDDataIOManagerCmdline.getInstance().setDoHistory(true);
			}
			
			// validate and get parameters all
			// and set their values from the value string if not already done
			// (in case the do modify parameter descriptors)
			validateParameternames( firstIndex, false);
			readParameterValues( firstIndex, false);
			
			try {
				if (this.showProgressEvents)
					this.op.addOperatorExecutionProgressEventListener(this);
				this.op.runOp();
				if (this.showProgressEvents)
					this.op.removeOperatorExecutionProgressEventListener(this);
			} catch (Exception e) {
				System.err.println( "ALDOpRunner failed to runOp the operator <" + this.op.getName() +
						"> with exception " + e);
				
				e.printStackTrace();

				System.exit(2);
			}
			writeParameterValues( firstIndex);
			
			ALDDataIOManagerCmdline.getInstance().setDoHistory(oldHistoryState);
    	} else {
			this.op.printInterface();
    	}
    }

	/** Validate the <code>parametername=valuestring</code> pairs with respect to syntax
     *  and optionally validate the existence of the parameter in the operator interface.
     *  <p>
     *  If <code>modifyingParameters</code> is true, only parameters which indicate
     *  they might modifyParameterDescriptors are considered and not existing
     *  parameters are ignored.
     *  <p>
     *  The parameter names found and the corresponding value strings are collected
     *  in the hash maps <code>nameParameterMap</code> and <code>nameValueMap</code> respectively
     *  as a side effect.
     * 
     * @param firstIndex	index of first parametername in args
     * @param modifyingParameters	if true only parameters which indicate
     *  they might modifyParameterDescriptors are considered

     */

	private void validateParameternames( int firstIndex, boolean modifyingParameters) {
		this.nameParameterMap = new HashMap<String,String>();
		this.nameValueMap = new HashMap<String,String>();
		
		// first scan: validate parameter names
		for ( int i = firstIndex ; i < this.args.length ; i++ ) {
			ArrayList<String> parts = new ArrayList<String>(ALDParser.split( this.args[i].trim(), '='));
			if ( parts.size() != 2 ) {
                	System.err.println("ERROR: found = sign " + (parts.size() -1)
                        + " times, instead of once");
				System.err.println( "");
				printUsage();
				System.exit(2);
            }

			String name = parts.get(0);
			LinkedList<String> pNames = ALDParametrizedClassDataIOCmdline.lookupParameternames( this.op, name);

			if ( pNames.size() > 1 ) {
				System.err.print( "ERROR:found more than one matching parameter names for " + name + ": ");
				for ( String pName : pNames ) {
					System.err.print( pName + "  ");
				}
				System.err.println();
				this.op.printInterface();
				System.exit(2);
			} else if ( pNames.size() == 0 && ! modifyingParameters)  {
				System.err.println( "ERROR:found no matching parameter names for <" + name + ">\n");
				this.op.printInterface() ;
				System.exit(2);
			}
	
			if ( this.debug ) {
				System.out.print( "Matching parameter names for " + name + ": ");
				for ( String pName : pNames ) {
					System.out.print( pName + "  ");
				}
				System.out.println();
			}
			
			ALDOpParameterDescriptor descr = null;
			if ( modifyingParameters) {
				try {
					descr = this.op.getParameterDescriptor(name);
				} catch (ALDOperatorException e) {
                     // this parameter might be created setting a modifying parameter
				}
			}
			
			if ( ! modifyingParameters || 
					(  descr != null 
					&&    descr.parameterModificationMode() 
					   != Parameter.ParameterModificationMode.MODIFIES_NOTHING) )
				this.nameParameterMap.put( parts.get(0), pNames.peek());
			this.nameValueMap.put( parts.get(0), parts.get(1));
		}
	}

	/** Read all IN and INOUT parameters specified on the command line.
     *
     * @param firstIndex	index of first parametername in args
     */

	private void readParameterValues( int firstIndex, boolean modifyingParameters) {
		StringBuffer buf = new StringBuffer("{   ");
		for ( int i = firstIndex ; i < this.args.length ; i++) {
			LinkedList<String> parts = ALDParser.split( this.args[i], '=');
			if ( parts.peek() != null ) {
				try {
					String expandedParameterName  = this.nameParameterMap.get( parts.peek());
					if ( expandedParameterName == null) {
						continue;
					}
					
					ALDOpParameterDescriptor descr = this.op.getParameterDescriptor( expandedParameterName);

					// modifying parameters have already been set thus skip them
					if ( ! modifyingParameters &&
							   descr.parameterModificationMode() 
							!= Parameter.ParameterModificationMode.MODIFIES_NOTHING ) {
						continue;
					}
					
					if ( descr.getDirection() == Parameter.Direction.IN ||
							descr.getDirection() == Parameter.Direction.INOUT ) {
						buf.append( this.args[i] + " , ");
					}
				} catch ( ALDOperatorException e) {
					printException(e);
					System.exit(1);
				}
			}
		}
		buf.delete( buf.length()-3,  buf.length()).append("}");

		try {
			ALDParametrizedClassDataIOCmdline provider = (ALDParametrizedClassDataIOCmdline)ALDDataIOManagerCmdline.getInstance().getProvider(ALDOperator.class, ALDDataIOCmdline.class);
			this.op = (ALDOperator) provider.parse(null, this.op.getClass(), new String( buf), this.op);
		} catch (ALDDataIOManagerException e) {
			printException(e);
			System.exit(2);
		} catch (ALDDataIOProviderException e) {
			printException(e);
			System.exit(2);
		}
		
		if ( this.op == null ) {
			System.err.println( "ERROR: cannot configure operator, i.e. read parameters failed");
			System.exit(2);
		}
	}

	/** Write all OUT and INOUT parameters specified on the command line.
     *
     * @param firstIndex	index of first parametername in args
     */

	private void writeParameterValues( int firstIndex) {
		for ( String pName : this.nameParameterMap.keySet() ) {
			ALDOpParameterDescriptor descr = null;
			Object value = null;
			try {
				descr = this.op.getParameterDescriptor( this.nameParameterMap.get( pName));

				// this should never occur as we checked before
			} catch (ALDOperatorException e) {
				e.printStackTrace();
				System.err.println( "ERROR: parametername <" + pName + "> not known by operator");
				System.err.println( "");
				printUsage();
				System.exit(2);
			}

			String mappedPname = this.nameParameterMap.get( pName);
			if ( mappedPname == null ) {
				// this should never occur 
				System.err.println( "ERROR: parametername <" + pName + "> cannot be mapped");
				System.exit(2);
			}
			try {
				if ( descr.getDirection() == Parameter.Direction.OUT ||
						descr.getDirection() == Parameter.Direction.INOUT ) {
					value = this.op.getParameter( mappedPname);
					String str = null;
					
					if ( this.debug ) {
						System.out.println( "Write parameter " + mappedPname + ", value = " + value);
					}

					if ( value != null ) {
						try {
							str = ALDDataIOManagerCmdline.getInstance().writeData( value, this.nameValueMap.get( pName));
							if ( str != null ) {
								System.out.println( this.nameParameterMap.get( pName) + " = " + str);
							} else {
								if ( verbose)
									System.out.println( this.nameParameterMap.get( pName) + " written using " + this.nameValueMap.get( pName));
							}
						} catch (ALDDataIOManagerException e) {
							printException(e);
						} catch (ALDDataIOProviderException e) {
							printException(e);
						}
					}
				}
			} catch (ALDOperatorException e) {
				System.err.println( "ERROR: cannot get value for parametername <" + pName + "> mapped to <" + mappedPname + ">");
				System.err.println( "");
				printUsage();
				System.exit(2);
			}
		}
	}

	/** This method is called to instantiate the ALDOperator.
	 * May be overrridden may extending classes.
	 * 
	 *
	 */
	protected ALDOperator getOperator( String opName) {
		try {
			return (ALDOperator)(Class.forName( opName).newInstance());
		} catch (Exception e) {
			System.err.println( "ALDOpRunner failed to instantiate the operator <" + opName + ">");
			System.exit(0);
		}
		return null;
		
		
	}
	/** This method is call once to populate the member <code>matchingClassNames</code>.
	 * May be overrridden may extending classes.
	 *  
	 *  @param opNamePattern string with pattern for operator name
	 */
	protected void findOperators(String opNamePattern) {
		this.matchingClassNames = findALDOperators( opNamePattern);
	}
	
	/** Find operator with given pattern among all annotated ALDOperators
	 *  and return the full qualified names as a list.
	 *  Operators found depend on member variable useRegEx
	 *  return only ALDOperators annotated with appropriate execution mode.
	 *  
	 *  @param opNamePattern string with pattern for operator name
	 */
	public LinkedList<String> findALDOperators(String opNamePattern) {
		LinkedList<String> classNames = new LinkedList<String> ();

			if ( this.debug ) 
				System.out.println( "Looking up all annotated @Operators for <" + opNamePattern + ">");
			Index<ALDAOperator, ALDOperator> indexItems = 
					SezPozAdapter.load(ALDAOperator.class, ALDOperator.class);
			for ( final IndexItem<ALDAOperator,ALDOperator> item : indexItems ) {

				if ( item.annotation().genericExecutionMode() == ALDAOperator.ExecutionMode.ALL ||
					 item.annotation().genericExecutionMode() == ALDAOperator.ExecutionMode.CMDLINE ) {
					String className = item.className();
					if ( !this.useRegEx && className.equals( opNamePattern) ) {
						// found an exact match, keep it and we are done
						classNames.clear();
						classNames.add( className);
						if ( this.debug ) 
							System.out.println( "exact match:  " + className);
	
						break;
					} else if ( ( this.useRegEx && Pattern.matches( opNamePattern, className) ) ||
					 	        (!this.useRegEx && isExactSubstring( opNamePattern, className) ) ) {
						classNames.add( className);
						if ( this.debug ) 
							System.out.println( "match:  " + className);
					} else {
						if ( this.debug ) 
							System.out.println( "no match:  " + className);
					}
				}
			}

		if ( this.debug ) {
			System.out.println( "findOperators: annotated operators found");
			for ( String className : classNames ) {
				System.out.println( "\t" + className);
			}
		}

		// if we do not use regex and have more than one match, try to disambiguate: require the
    	// given name to match a suffix of an annotated operator
		if ( (! this.useRegEx ) && (classNames.size() > 1) ) {
			LinkedList<String> classNamesSuffix = new LinkedList<String> ();
			for ( String className : classNames ) {
				if ( className.endsWith( opNamePattern) )
					classNamesSuffix.add( className);
			}

			if ( classNamesSuffix.size() == 1 )
				classNames = classNamesSuffix;

			if ( this.debug ) {
				System.out.println( "findOperators: suffix matches found");
				for ( String className : classNames ) {
					System.out.println( "\t" + className);
				}
			}
		}

		if ( this.debug ) {
			System.out.println( "findOperators: returning");
			for ( String className : classNames ) {
				System.out.println( "\t" + className);
			}
		}

		return classNames;
	}

	/** return true, if <code>substr</code> is a exact substring of <code>str</code>	 
      */
	protected boolean isExactSubstring( String substr, String str) {
		for ( int i=0 ; i < str.length() - substr.length() + 1 ; i++ ) {
			if ( str.substring(i).startsWith( substr)) {
				return true;
			}
		}
		return false;
	}

	private void printException( ALDException e) {
		System.err.println( "Exception of type " + e.getIdentString());
		System.err.println( e.getCommentString());
	}

	/* (non-Javadoc)
	 * @see de.unihalle.informatik.Alida.operator.events.ALDOperatorExecutionProgressEventListener#handleOperatorExecutionProgressEvent(de.unihalle.informatik.Alida.operator.events.ALDOperatorExecutionProgressEvent)
	 */
	@Override
  public void handleOperatorExecutionProgressEvent(
      ALDOperatorExecutionProgressEvent e) {
		// simply print the progress status to standard out
		System.out.println(e.getExecutionProgressDescr());
  }
}
