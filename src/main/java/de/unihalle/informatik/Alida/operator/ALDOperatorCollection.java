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

import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Set;

import de.unihalle.informatik.Alida.exceptions.ALDOperatorException;
import de.unihalle.informatik.Alida.gui.ALDOperatorConfigurationFrame;
import de.unihalle.informatik.Alida.helpers.ALDClassInfo;
import de.unihalle.informatik.Alida.operator.events.*;

/**
 * 
 * @author moeller
 */
public class ALDOperatorCollection<T extends ALDOperatorCollectionElement> 
		implements ALDControlEventListener, ALDControlEventReporter {
//		ALDConfigurationEventListener, ALDConfigurationEventReporter {
	
	private Class<T> elementType;
	
	private Set<Class> availableClasses;
	 
	private HashMap<String, T> classNameMapping;
	
	/**
	 * Mapping of short names to detector IDs.
	 */
	private HashMap<String, String> shortNamesToIDs = null;

	private HashMap<String, ALDOperatorConfigurationFrame> configFrames;

	public ALDOperatorCollection() throws InstantiationException {
		this.availableClasses = ALDClassInfo.lookupExtendingClasses(
			this.elementType.getClass());
		this.classNameMapping = new HashMap<>();
		this.shortNamesToIDs = new HashMap<String, String>();
		for (Class c: this.availableClasses) {
			ALDOperatorCollectionElement dOp;
			try {
				dOp = (T)c.newInstance();
				String cname = dOp.getUniqueClassID();
				this.classNameMapping.put(cname, (T)dOp);
				this.shortNamesToIDs.put(cname, dOp.getUniqueClassID());
//				detectorList.add(cname);
			} catch (IllegalAccessException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} 
		}
	}
	
	public Set<Class> getAvailableClasses() {
		return this.availableClasses;
	}
	
	public Collection<String> getShortClassNames() {
		LinkedList<String> coll = new LinkedList<>();
		Set<String> shortNames = this.shortNamesToIDs.keySet();
		for (String s: shortNames)
			coll.add(s);
		return coll;
	}
	
	public Collection<String> getUniqueClassIDs() {
		LinkedList<String> coll = new LinkedList<>();
		Set<String> shortNames = this.shortNamesToIDs.keySet();
		for (String s: shortNames)
			coll.add(this.shortNamesToIDs.get(s));
		return coll;
	}

	public HashMap<String, T> getClassNameMapping() {
		return this.classNameMapping;
	}
	
	public void configureOperator(String classID) throws ALDOperatorException {
		T op = this.classNameMapping.get(classID);
		ALDOperatorConfigurationFrame confWin;
		if (this.configFrames.get(classID) == null) {
			confWin =	new ALDOperatorConfigurationFrame(op, null);
			this.configFrames.put(classID, confWin);
		}
		else {
			confWin = this.configFrames.get(classID);
		}
		confWin.setVisible(true);
	}

	public void runSelectedOperator() {
		
	}
	
	public void getResultData() {
		
	}

	@Override
	public void addALDControlEventListener(ALDControlEventListener listener) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void removeALDControlEventListener(ALDControlEventListener listener) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void fireALDControlEvent(ALDControlEvent event) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void handleALDControlEvent(ALDControlEvent event) {
		// TODO Auto-generated method stub
		
	}
	
}
