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

import java.awt.Component;
import java.awt.event.ActionListener;
import java.net.URL;

import javax.help.BadIDException;
import javax.help.CSH;
import javax.help.HelpBroker;
import javax.help.HelpSet;
import javax.help.HelpSetException;
import javax.swing.JFrame;

/**
 * Convenience class for context-sensitive online help. 
 * 
 * @author moeller
 */
public class OnlineHelpDisplayer {

	/**
	 * Name of helpset to be used.
	 */
	private static String helpHS = "alida.hs";

	/**
	 * Name of the default target page within the helpset.
	 */
	private static String defaultTarget = "alida_welcome";
	
	/**
	 * Local help broker.
	 */
	static HelpBroker hb = null;

	// static initialization routine to load helpset only once
	static {
    try {
    	ClassLoader cl = OnlineHelpDisplayer.class.getClassLoader();
  		URL hsURL = HelpSet.findHelpSet(cl, helpHS);
  		HelpSet hs = new HelpSet(null, hsURL);
			hb = hs.createHelpBroker();
    } catch (HelpSetException e) {
    	System.err.println("HelpDisplay: static init failed!!!\n" 
    		+ " Could not find helpset files, switching to message frame...");
    }
	}			

	/**
	 * Explicitly init the helpset, i.e. set helpset prefix.
	 * <p>
	 * By default the prefix is set to 'alida', but to access MiToBo's 
	 * helpset pages, it needs to be set to 'mitobo' prior to the first call
	 * to any helpset page. This routine reinitializes the helpset.
	 * 
	 * @param prefix	Prefix of the target helpset, i.e. 'alida' or 'mitobo'.
	 */
	public static void initHelpset(String prefix) {
		helpHS = prefix + ".hs";
//		defaultTarget = prefix + "_welcome";
		defaultTarget = "welcome";
    try {
    	ClassLoader cl = OnlineHelpDisplayer.class.getClassLoader();
  		URL hsURL = HelpSet.findHelpSet(cl, helpHS);
  		HelpSet hs = new HelpSet(null, hsURL);
			hb = hs.createHelpBroker();
    } catch (HelpSetException e) {
    	System.err.println("HelpDisplay: static init failed!!!\n" 
    		+ " Could not find helpset files, switching to message frame...");
    }
	}			

	/**
	 * Closes the online help window if it is currently displayed.
	 */
	public static void closeWindow() {
		if (hb != null && hb.isDisplayed())
			OnlineHelpDisplayer.hb.setDisplayed(false);
	}
	
	/**
	 * Get an ActionListener linked to the Alida online help system.
	 * 
	 * @param c		Component to which to attach the listener (buttons, menu items).
	 * @param l		Label of the page in the helpset to be displayed.
	 * @param topFrame	Top-level frame using online help.
	 * @return	Listener for the component.
	 */
	public static ActionListener getHelpActionListener(Component c,String l,
			JFrame topFrame) {
		// make sure that the page can be displayed
		String s = l;
		try {
			if (OnlineHelpDisplayer.hb == null) {
				// if the help broker is null, help files could not be found,
				// switch to error message window instead of online help
				return new OnlineHelpMissingHelpListener(topFrame);
			}
			OnlineHelpDisplayer.hb.setCurrentID(s);
		} catch(BadIDException ex) {
//			System.err.println("Help index " + s + " does not exist!!");
//			System.err.println("-> switching back to main page...");
			// switch back to welcome page if ID not found
			s = defaultTarget;
		}
		CSH.setHelpIDString(c, s);
		// generate the ActionListener
    javax.help.CSH.DisplayHelpFromFocus helpdisp = 
			new CSH.DisplayHelpFromFocus(hb);
    return helpdisp;
  }
}
