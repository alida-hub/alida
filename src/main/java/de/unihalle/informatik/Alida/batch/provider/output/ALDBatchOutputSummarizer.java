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
 * $Rev: 4370 $
 * $Date: 2011/11/18 14:02:18 $
 * $Author: posch $
 * 
 */

package de.unihalle.informatik.Alida.batch.provider.output;

import java.util.Collection;


/**
 * Super class for all output summarizers in Alida batch mode.
 * 
 * @author moeller
 *
 */
public interface ALDBatchOutputSummarizer {

	/**
	 * Interface method to announce all classes handled by this provider.
	 * 
	 * @return  Collection of classes provided
	 */
	public Collection<Class<?>> providedClasses();
}
