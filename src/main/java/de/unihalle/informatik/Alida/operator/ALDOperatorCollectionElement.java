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

import de.unihalle.informatik.Alida.exceptions.ALDOperatorException;

/**
 * Base class for operator classes managed by {@link ALDOperatorCollection}.
 * 
 * @author moeller
 */
public abstract class ALDOperatorCollectionElement extends ALDOperator {

	/**
	 * Default constructor, should never be called.
	 * @throws ALDOperatorException	Thrown in case of failure.
	 */
	private ALDOperatorCollectionElement() throws ALDOperatorException {
		super();
	}
	
	/**
	 * Request a unique identifier string for this operator class.
	 * <p>
	 * This string is used by {@link ALDOperatorCollection} to manage available
	 * operators and link them to additional information. If the string is not
	 * unique for your set of operators {@link ALDOperatorCollection} might not 
	 * work properly.
	 *
	 * @return	Unique identifier string.
	 */
	public abstract String getUniqueClassIdentifier();
}
