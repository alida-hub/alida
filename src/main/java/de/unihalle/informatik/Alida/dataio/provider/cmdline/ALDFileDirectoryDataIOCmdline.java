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
 * $Rev: 4767 $
 * $Date: 2011-12-16 17:48:54 +0100 (Fr, 16 Dez 2011) $
 * $Author: moeller $
 * 
 */

package de.unihalle.informatik.Alida.dataio.provider.cmdline;

import de.unihalle.informatik.Alida.annotations.ALDDataIOProvider;
import de.unihalle.informatik.Alida.dataio.provider.ALDDataIOCmdline;
import de.unihalle.informatik.Alida.datatypes.ALDDirectoryString;
import de.unihalle.informatik.Alida.datatypes.ALDFileString;

import java.lang.reflect.Field;
import java.util.*;

/**
 * DataIO provider for filenames and directory paths from command line.
 *
 * @author moeller
 *
 */
@ALDDataIOProvider
public class ALDFileDirectoryDataIOCmdline implements ALDDataIOCmdline {

	@Override
	public Collection<Class<?>> providedClasses() {
		LinkedList<Class<?>> classes = new LinkedList<Class<?>>();
		classes.add(ALDFileString.class);
		classes.add(ALDDirectoryString.class);
		return classes;
	}
	
	@Override
	public Object readData(Field field, Class<?> cl, String valueString) {
		if (cl.equals(ALDFileString.class))
			return new ALDFileString(valueString);
		return new ALDDirectoryString(valueString);		
	}

	@Override
	public String writeData(Object obj, String locationString) {
		if (obj instanceof ALDFileString)
			return ((ALDFileString)obj).getFileName();
		return ((ALDDirectoryString)obj).getDirectoryName();
	}
}
