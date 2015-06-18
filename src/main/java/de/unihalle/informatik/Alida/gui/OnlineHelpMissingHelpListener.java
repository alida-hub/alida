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

package de.unihalle.informatik.Alida.gui;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JFrame;
import javax.swing.JOptionPane;

/**
 * Class handling requests to the online help if help files were not found.
 * 
 * @author moeller
 */
public class OnlineHelpMissingHelpListener implements ActionListener {

	/**
	 * Top level frame within which the help is to be used.
	 */
	JFrame referenceFrame;

	/**
	 * Default constructor.
	 * @param rFrame	Top level frame of component using help.
	 */
	public OnlineHelpMissingHelpListener(JFrame rFrame) {
		this.referenceFrame = rFrame;
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		JOptionPane.showMessageDialog(this.referenceFrame, 
				"Attention, the online help system is not available!\n"
						+ "Probably you need to run a Maven build on console first,\n" 
						+ "m2e in Eclipse does not support generating online help files!",  
						"Information: Online Help", JOptionPane.INFORMATION_MESSAGE);
	}
}
