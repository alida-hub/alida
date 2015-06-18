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

import de.unihalle.informatik.Alida.exceptions.*;
import de.unihalle.informatik.Alida.helpers.ALDFilePathManipulator;

import java.io.*;

import org.graphdrawing.graphml.xmlns.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** 
 * Interface to the port hash map of Alida.
 * <p>
 * This class implements the interface to access the port hash.
 * It yields the only possibility to access the hash, direct access
 * is blocked to guarantee consistency.
 * 
 * @author moeller
 * @author posch
 */
public class ALDPortHashAccess {

	/** 
	 * File extension of Alida processing history files.
	 */
	protected static final String ALD_EXTENSION = new String( "ald");
	
	/**
	 * If true, verbose outputs are written to standard output on calling methods.
	 */
	private boolean verbose = false;
	
	/**
	 * Init a logger object.
	 */
	private static Logger logger = 
		LoggerFactory.getLogger(ALDPortHashAccess.class);
	
	/** 
	 * Constructor without function.
	 * <p>
	 * Note that there will only be only one processing history per session
	 * and not many different objects of this type.
	 */
	protected ALDPortHashAccess() {
		// nothing to be done here
	}

	/**
	 * Returns the number of objects currently stored in the history database.
	 * <p>
	 * Note that the number can be larger than the number of objects actually
	 * referenced from the Java process due to the management of weak references
	 * of the Java Garbage Collector.
	 */
	public int getNumEntries() {
		return ALDPortHash.getEntryNum();
	}
	
	/** 
	 * Read and set the processing history from file.
	 * <p>
	 * If no graphML processing history could be find, the method checks if an 
	 * arbitrary XML tree is available. If this fails, too, the data object is 
	 * left without any processing history. As a result of a call of this method 
	 * either a graphmlHistory or an xmlHistory object (if available) is added 
	 * to the port of the object.
	 * <p> 
	 * If <code>filename</code> ends with <code>.ald</code> it is used as is
	 * otherwise the extension <code>ald</code> is appended.
	 * <p>
	 * If the object is of type {@link ALDData} the location property is set 
	 * from filename and lateron documented in the processing history.
	 *
	 * @param	filename	Filename of processing history (XML file).
	 */
	protected void readHistory(Object obj, String filename) {
		GraphmlDocument graphmlDoc = null;

		// init graphml history and port for the object
		GraphmlType objGraphmlHistory = null;
		if (!isRegistered(obj)) {
			register(obj);
		}
		
		// set location in the dataport registered in the port hash
		if ( ALDPortHash.getHistoryLink(obj) instanceof ALDDataPort ) {
			ALDDataPort dataport = (ALDDataPort) ALDPortHash.getHistoryLink(obj);
			dataport.setProperty("location", filename);
		}
		
		if (obj instanceof ALDData) {
			((ALDData)obj).setLocation( filename);
		}
		
		File file = null;
		if ( ! ALDFilePathManipulator.getExtension( filename).equals( ALD_EXTENSION)) {
			filename = new String( filename + "." + ALD_EXTENSION);
		}

		try {
			file = new File( filename);
			graphmlDoc = GraphmlDocument.Factory.parse(file);
		} catch ( Exception e ){
			if (this.verbose)
				System.out.println("ALDPortHashAccess:readHistory - " +
						"   no history file found for " + filename + " !");
		}

		try {		
			objGraphmlHistory = graphmlDoc.getGraphml();
			((ALDDataPort)getHistoryLink(obj)).setGraphmlHistory(objGraphmlHistory);

			if ( this.verbose ) {
				System.out.println("readHistory::filename " + file.getCanonicalPath());
				GraphmlHelper.printGraphml( objGraphmlHistory);
				System.out.println("readHistory::filename DONE");
			}
		} catch ( Exception e ){
			if (this.verbose)
				System.out.println("ALDPortHashAccess:readHistory - " +
						" could not read given file, no history available!");
		}
	}

	/** 
	 * Write the processing history (if any) to a graphml file.
	 * <p>
	 * Equivalent to 
	 * <code>writeHistory(obj, filename, 
	 * 	 									ALDProcessingDAG.HistoryType.COMPLETE, false)</code>.
	 * 
	 * @deprecated
	 */
	@Deprecated
	protected void writeHistory(Object obj, String filename) 
		throws ALDProcessingDAGException,ALDOperatorException {
	 	writeHistory(obj, filename, ALDProcessingDAG.HistoryType.COMPLETE, false);
	}

	/** 
	 * Write the processing history (if any) to a graphml file.
	 * <p>
	 * Equivalent to 
	 * <code>writeHistory(obj, filename, historyType, false)</code>.
	 * 
	 * @deprecated
	 */
	@Deprecated
	protected void writeHistory(Object obj, String filename, 
															ALDProcessingDAG.HistoryType historyType) 
		throws ALDProcessingDAGException,ALDOperatorException {
	 	writeHistory(obj, filename, historyType, false);
	}

	/** 
	 * Write the processing history (if available) to a graphml file.
	 * <p> 
	 * The processing history is created using the opNodes and their ports
	 * starting with this data object.
	 * If no history is available (e.g. if this object is created internally and 
	 * not output of an operator) then no history file is written.
	 * If <code>filename</code> ends with <code>.ald</code> it is used as is
	 * otherwise the extension <code>ald</code> is appended.
	 *
	 * @param	obj	Object for which the history to write for.
	 * @param	filename	Filename to write the processing history into
	 * 								in graphml/XML format. For the extension see above.
	 * @param	historyType	Type/mode of the history.
	 * @param	ignoreHiding	If true, hiding of opNodes is ignored.
	 */
	protected void writeHistory(Object obj, String filename, 
			ALDProcessingDAG.HistoryType historyType,	boolean ignoreHiding) 
		throws ALDProcessingDAGException,ALDOperatorException {

		if (!this.isRegistered(obj)) {
		    if(logger.isDebugEnabled()){
			logger.warn("ALDHistoryDB - Note, you tried to write the " +
				"history of an object that was never manipulated by any operator!");
		    }
		    return;
		}
		
		if ( ! ALDFilePathManipulator.getExtension( filename).equals( ALD_EXTENSION)) {
			filename = new String( filename + "." + ALD_EXTENSION);
		}

		ALDProcessingDAG pDAG = new ALDProcessingDAG();
		GraphmlDocument graphmlDoc = 
			pDAG.createGraphmlDocument(obj, historyType, ignoreHiding);

		// any history to write?
		if ( graphmlDoc == null ) return;

		try {
			BufferedWriter xmlFile =
				new BufferedWriter(new FileWriter( filename));
			xmlFile.write( graphmlDoc.toString());
			xmlFile.close();
		} catch ( Exception e ){
			System.err.println("ALDHistoryManager:writeHistory - Exception:" + e );
			e.printStackTrace();
		}
	}

	/**
	 * Check if an object is registered in the hash.
	 * 
	 * @param obj	Object to check.
	 * @return	True, if object is known to the hash.
	 */
	protected boolean isRegistered(Object obj) {
		return ALDPortHash.isRegistered(obj);
	}
	
	/**
	 * Register the object in the hash.
	 * 
	 * @param obj	Object to register.
	 */
	protected void register(Object obj) {
		ALDPortHash.register(obj);
	}

	/** 
	 * Get the port to which the object is currently linked in history.
	 *
	 * @return Current port to which data is linked.
	 */
	protected ALDPort getHistoryLink(Object obj) { 
		return ALDPortHash.getHistoryLink(obj);
	}

	/** 
	 * Set the port to which the object is currently linked in history.
	 *
	 * @param port New port data is currently linked to.
	 */
	protected void setHistoryLink(Object obj, ALDPort port) { 
		if (port != null)
			ALDPortHash.setHistoryLink(obj, port);
	}
}
