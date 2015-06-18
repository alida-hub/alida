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

package de.unihalle.informatik.Alida.dataio.provider.helpers;

import de.unihalle.informatik.Alida.annotations.*;

/**
 * This class only serves as a proxy for Alida's dataIO system to look up DataIO providers
 * for parametrized classes.
 * As Alida's parametrized classes enjoy no common super class (except Object) but
 * are simply annotated using {@link de.unihalle.informatik.Alida.annotations.ALDParametrizedClass},
 *  Alida uses this dummy class for dataIO providers to
 * register as providing parametrized classes.
 * 
 * @author posch
 * @see de.unihalle.informatik.Alida.dataio.ALDDataIOManager
 *
 */
@ALDParametrizedClass
public class ALDParametrizedClassDummy {
}
