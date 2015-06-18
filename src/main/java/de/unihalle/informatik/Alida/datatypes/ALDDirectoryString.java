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

package de.unihalle.informatik.Alida.datatypes;


/**
 * Class for representing a directory path.
 * <p>
 * The purpose of this class is mainly to simplify data IO for string 
 * variables linked to a directory path when running operators from GUI.
 * In contrast to java.lang.String for this class specific data IO providers
 * are provided that offer the user with directory chooser dialogs.
 * 
 * @author moeller
 */
public class ALDDirectoryString {
	
	/**
	 * Name of directory including full path.
	 */
	protected String dirPath;
	
	/**
	 * Default constructor.
	 * 
	 * @param dir	Name of directory including path.
	 */
	public ALDDirectoryString(String dir) {
		this.dirPath = dir;
	}
	
	/**
	 * Returns current name of directory.
	 */
	public String getDirectoryName() {
		return this.dirPath;
	}
	
	@Override
  public String toString() {
		return new String(this.dirPath);
	}
}
