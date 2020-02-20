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

package de.unihalle.informatik.Alida.annotations.indexing;


import java.lang.annotation.Annotation;

import net.java.sezpoz.Index;

/**
 * 
 * @author moeller
 */
public abstract class SezPozAdapter {

	/** 
	 * For internal debugging purposes. 
    */
	protected static boolean debug = false;

	/**
	 * Class loader to be used for searching SezPoz annotation indices.
	 */
	private static ClassLoader classloader = null;
	
	/**
	 * Variable to check if init routine was called.
	 */
	protected static boolean initDone = false;
	
	/** 
	 * Default constructor, should not be called from outside. 
	 */
	private SezPozAdapter() {
		 //nothing to do here to be overridden in extending classes
	}
	
	/**
	 * Initialization routine for this class.
	 * <p>
	 * This routine is to be called prior to accessing any of the methods
	 * of this class as it takes care of proper initialization of the class.
	 * If this does not happen, a warning is shown. Do not expect the class to
	 * work properly in that case.
	 * <p>
	 * Note that the method can be called multiple times during a session.
	 * However, changing initialization may change class behaviour. 
	 */
	public static void initAdapter() {
		classloader = null;
		initDone = true;
	}

	/**
	 * Initialization routine for this class.
	 * <p>
	 * This routine is to be called prior to accessing any of the methods
	 * of this class as it takes care of proper initialization of the class.
	 * If this does not happen, a warning is shown. Do not expect the class to
	 * work properly in that case.
	 * <p>
	 * Note that the method can be called multiple times during a session.
	 * However, changing initialization may change class behaviour. 
	 * 
	 * @param cl	Class loader to be used in accessing annotation indices.
	 */
	public static void initAdapter(ClassLoader cl) {
		classloader = cl;
		initDone = true;
	}

	public static <A extends Annotation,I> Index<A,I>
		load(Class<A> annotation, Class<I> instanceType) {
		Index<A,I> indexItems = null;
		if (classloader == null)
			indexItems = Index.load(annotation, instanceType);
		else
			indexItems = Index.load(annotation, instanceType, classloader);
		return indexItems;
	}
}
