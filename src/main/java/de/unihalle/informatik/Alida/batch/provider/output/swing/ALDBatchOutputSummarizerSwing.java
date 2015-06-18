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
 * $Rev: 5825 $
 * $Date: 2012-07-24 09:35:34 +0200 (Di, 24 Jul 2012) $
 * $Author: moeller $
 * 
 */

package de.unihalle.informatik.Alida.batch.provider.output.swing;

import javax.swing.JComponent;

import de.unihalle.informatik.Alida.batch.ALDBatchRunResultInfo;
import de.unihalle.informatik.Alida.batch.provider.output.ALDBatchOutputSummarizer;
import de.unihalle.informatik.Alida.operator.ALDParameterDescriptor;

/**
 * Interface for Alida's automatic batch result data I/O via GUI based on Swing.
 * 
 * @author moeller	
 */
public interface ALDBatchOutputSummarizerSwing
	extends ALDBatchOutputSummarizer {

	/**
	 * Generates a graphical component to visualize set of result data.
	 * @param obj		Info object containing set of result data.
	 * @param desc	Optional descriptor for additional information on parameter.
	 * @return	Graphical component visualizing the data.
	 */
	public abstract JComponent writeData(ALDBatchRunResultInfo obj,
																					ALDParameterDescriptor desc);
}
