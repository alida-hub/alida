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

package de.unihalle.informatik.Alida.annotations;

import de.unihalle.informatik.Alida.operator.*;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.annotation.Documented;

import net.java.sezpoz.Indexable;

/**
 *   Each Class annotated with this annotation will enable Alida's DataIO mechanism to
 *   find the class as extending another class. 
 *   If a class is NOT annotated with <code>ALDDerivedClass</code> it will be not
 *   accepted as a value for a parameter of a super class.
 *   <p>
 *   Note: only non abstract classes supplying a public default constructor
 *   can be annotated.
 *
 * @author Stefan Posch
 */


@Retention(RetentionPolicy.SOURCE)
@Target(ElementType.TYPE)
@Indexable(type=Object.class)
@Documented

public @interface ALDDerivedClass {
}
