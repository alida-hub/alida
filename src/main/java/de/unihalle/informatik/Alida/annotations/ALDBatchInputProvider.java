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
 * $Rev: 5207 $
 * $Date: 2012-03-14 15:12:09 +0100 (Mi, 14 Mrz 2012) $
 * $Author: moeller $
 * 
 */

package de.unihalle.informatik.Alida.annotations;

import de.unihalle.informatik.Alida.batch.provider.input.ALDBatchInputIterator;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.annotation.Documented;

import net.java.sezpoz.Indexable;

/**
 * Annotation for classes acting as Alida batch input providers and 
 * implementing the {@link ALDBatchInputIterator} interface.
 *
 * @author Birgit Moeller
 */
@Retention(RetentionPolicy.SOURCE)
@Target(ElementType.TYPE)
@Indexable(type=ALDBatchInputIterator.class)
@Documented
public @interface ALDBatchInputProvider {
	/** 
	 * Priority resolves cases where more than one provider is found for a class.
	 * <p>
	 * The provider with largest priority is used, ties are resolved by chance.
	 */
	int priority() default 1;
}
