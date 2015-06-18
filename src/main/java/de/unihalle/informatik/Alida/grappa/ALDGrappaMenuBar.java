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

package de.unihalle.informatik.Alida.grappa;

import java.awt.Graphics;
import java.awt.Image;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Calendar;

import javax.imageio.ImageIO;
import javax.swing.Box;
import javax.swing.ButtonGroup;
import javax.swing.ImageIcon;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JRadioButtonMenuItem;

import de.unihalle.informatik.Alida.annotations.ALDAOperator;
import de.unihalle.informatik.Alida.annotations.ALDAOperator.Level;
import de.unihalle.informatik.Alida.gui.ALDChooseOpNameFrame;
import de.unihalle.informatik.Alida.gui.ALDOperatorChooserTree;
import de.unihalle.informatik.Alida.gui.OnlineHelpDisplayer;
import de.unihalle.informatik.Alida.version.ALDVersionProviderFactory;

/**
 * Menu bar for Grappa window.
 * 
 * @author Birgit Moeller
 */
public class ALDGrappaMenuBar extends JMenuBar 
	implements ActionListener {
	
	/**
	 * Reference to main window of grappa.
	 */
	protected ALDGrappaFrame grappaMainWin;
	
	/**
	 * Reference to the operator chooser tree of window this menubar 
	 * belongs to.
	 */
	protected ALDOperatorChooserTree opTree;
	
	/**
	 * Reference to the workbench of window this menubar belongs to.
	 */
	protected ALDGrappaWorkbench workBench;
	
	/**
	 * Icon to be shown in about box.
	 */
	protected ImageIcon aboutIcon;

	/**
	 * Checkbox in options menu to turn on/off display of progress events.
	 */
	protected JCheckBox optionCheckboxProgressEvents;
	
	/**
	 * Default constructor
	 * @param _grappaMainWin	Main frame to which the menubar belongs.
	 * @param _opTree 				Operator tree of the main frame.
	 * @param _workBench			Workbench area of the main frame.
	 */
	public ALDGrappaMenuBar(ALDGrappaFrame _grappaMainWin, 
			ALDOperatorChooserTree _opTree,	ALDGrappaWorkbench _workBench) {
		super();
		this.grappaMainWin = _grappaMainWin;
		this.opTree = _opTree;
		this.workBench = _workBench;
		// init the icon for the about box
		this.setupAboutIcon();
		// init the menu
		this.setupMenu();
	}
	
	/**
	 * Generates the menubar.
	 */
	protected void setupMenu() {
		// file menue
		JMenu fileM = new JMenu("File");
		JMenuItem loadItem = new JMenuItem("Load Workflow...");
		loadItem.setActionCommand("load");
		loadItem.addActionListener(this);
//		loadItem.setAccelerator(KeyStroke.getKeyStroke(
//				KeyEvent.VK_L, ActionEvent.CTRL_MASK));
		JMenuItem saveItem = new JMenuItem("Save Workflow...");
		saveItem.setActionCommand("save");
		saveItem.addActionListener(this);
//		saveItem.setAccelerator(KeyStroke.getKeyStroke(
//				KeyEvent.VK_S, ActionEvent.CTRL_MASK));
		JMenuItem quitItem = new JMenuItem("Quit");
		quitItem.setActionCommand("quit");
		quitItem.addActionListener(this);
		fileM.add(loadItem);
		fileM.add(saveItem);
		fileM.addSeparator();
		fileM.add(quitItem);

		// operator level menue
		JMenu opSelectM = new JMenu("Options");
		opSelectM.add(new JLabel("<html><u>Operator View</u></html>"));
		JMenu operatorSet = new JMenu("Operators to Show");
		ButtonGroup operatorSetGroup = new ButtonGroup();
		JRadioButtonMenuItem radioItemApplication =
			new JRadioButtonMenuItem("Default");
		radioItemApplication.setToolTipText("<html>Shows operators to be "
			+ "used out-of-the-box<br> and well-suited also for non-expert "
			+ "users.</html>");
		radioItemApplication.setActionCommand("viewApps");
		radioItemApplication.addActionListener(this);
		JRadioButtonMenuItem radioItemAll = 
			new JRadioButtonMenuItem("All");
		radioItemAll.setToolTipText("<html>Shows all operators including " 
			+ " very specialized<br> ones requiring advanced expert "
			+ "knowledge.</html>");
		radioItemAll.setActionCommand("viewStd");
		radioItemAll.addActionListener(this);
		operatorSetGroup.add(radioItemApplication);
		operatorSetGroup.add(radioItemAll);
		operatorSet.add(radioItemApplication);
		operatorSet.add(radioItemAll);
		opSelectM.add(operatorSet);
		// set correct initial display mode
		if ( this.opTree.getLevel() == Level.APPLICATION)
			radioItemApplication.setSelected(true);
		else
			radioItemAll.setSelected(true);
		
		opSelectM.addSeparator();
		opSelectM.add(new JLabel("<html><u>Status View</u></html>"));
		this.optionCheckboxProgressEvents = 
				new JCheckBox("Show Progress Messages");
		this.optionCheckboxProgressEvents.setSelected(true);
		this.optionCheckboxProgressEvents.setActionCommand("optionShowProgress");
		this.optionCheckboxProgressEvents.addActionListener(this);
		opSelectM.add(this.optionCheckboxProgressEvents);

		// workflow menu
		JMenu actionsM = new JMenu("Workflow");
		JMenuItem newItem = new JMenuItem("New");
		newItem.setActionCommand("new");
		newItem.addActionListener(this);
//		newItem.setAccelerator(KeyStroke.getKeyStroke(
//				KeyEvent.VK_N, ActionEvent.CTRL_MASK));
		JMenuItem closeItem = new JMenuItem("Close");
		closeItem.setActionCommand("close");
		closeItem.addActionListener(this);
//		closeItem.setAccelerator(KeyStroke.getKeyStroke(
//				KeyEvent.VK_W, ActionEvent.CTRL_MASK));
		JMenuItem renameItem = new JMenuItem("Rename");
		renameItem.setActionCommand("rename");
		renameItem.addActionListener(this);
//		renameItem.setAccelerator(KeyStroke.getKeyStroke(
//				KeyEvent.VK_U, ActionEvent.CTRL_MASK));
		JMenuItem runItem = new JMenuItem("Run");
		runItem.setActionCommand("run");
		runItem.addActionListener(this);
//		runItem.setAccelerator(KeyStroke.getKeyStroke(
//				KeyEvent.VK_A, ActionEvent.CTRL_MASK));
		JMenuItem stopItem = new JMenuItem("Stop");
		stopItem.setActionCommand("stop");
		stopItem.addActionListener(this);
//		newItem.setAccelerator(KeyStroke.getKeyStroke(
//				KeyEvent.VK_N, ActionEvent.CTRL_MASK));
		actionsM.add(newItem);
		actionsM.add(renameItem);
		actionsM.add(closeItem);
		actionsM.addSeparator();
//		actionsM.add(loadItem);
//		actionsM.add(saveItem);
//		actionsM.addSeparator();
		actionsM.add(runItem);
		actionsM.add(stopItem);

		// help menu
		JMenu helpM = this.generateHelpMenu();
		
		// add to menu bar and finally to the main window
		this.add(fileM);
		this.add(actionsM);
		this.add(opSelectM);
		this.add(Box.createHorizontalGlue());
		this.add(helpM);
	}

	/**
	 * Method to initialize the Grappa icon in the about box.
	 */
	protected void setupAboutIcon() {
		String iconDataName = "/share/logo/Grappa_logo.png";
		Image img = null;
		BufferedImage bi = null;
		Graphics g = null;
		InputStream is = null;
		try {
			ImageIcon icon;
			File iconDataFile = new File("./" + iconDataName);
			if(iconDataFile.exists()) {
				icon = new ImageIcon("./" + iconDataName);
				img = icon.getImage();
			}
			// try to find it inside a jar archive....
			else {
				is = 
					ALDChooseOpNameFrame.class.getResourceAsStream(iconDataName);
				if (is == null) {
					System.err.println("Warning - cannot find icons...");
					img = new BufferedImage(20,20,BufferedImage.TYPE_INT_ARGB);
				}
				else {
					img = ImageIO.read(is);
				}
				bi= new BufferedImage(20,20,BufferedImage.TYPE_INT_ARGB);
				g = bi.createGraphics();
				g.drawImage(img, 0, 0, 20, 20, null);
			}
		} catch (IOException ex) {
			System.err.println(
					"ALDGrappaMenuBar - problems loading icons...!");
			img = new BufferedImage(20,20,BufferedImage.TYPE_INT_ARGB);
			bi= new BufferedImage(20,20,BufferedImage.TYPE_INT_ARGB);
			g = bi.createGraphics();
			g.drawImage(img, 0, 0, 20, 20, null);
		}
		this.aboutIcon = new ImageIcon(img);
	}

	/**
	 * Function to setup the help menu.
	 * @return	Help menu.
	 */
	protected JMenu generateHelpMenu() {
		JMenu helpM = new JMenu("Help");
		JMenuItem itemHelp = new JMenuItem("Online Help");
		itemHelp.addActionListener(
			OnlineHelpDisplayer.getHelpActionListener(itemHelp, 
					"de.unihalle.informatik.Alida.tools.ALDGrappaRunner", 
					this.grappaMainWin));
		JMenuItem itemAbout = new JMenuItem("About Grappa");
		itemAbout.setActionCommand("showAbout");
		itemAbout.addActionListener(this);
		helpM.add(itemHelp);
		helpM.add(itemAbout);
		return helpM;
	}	

	@Override
	public void actionPerformed(ActionEvent ev) {
		// extract command
		String command = ev.getActionCommand();

		// handle tree view options
		if (command.equals("viewApps")) {
			this.opTree.setLevel(ALDAOperator.Level.APPLICATION);
		} 
		else if (command.equals("viewStd")) {
			this.opTree.setLevel(ALDAOperator.Level.STANDARD);
		}
		else if (command.equals("optionShowProgress")) {
			if (this.optionCheckboxProgressEvents.isSelected())
				this.workBench.setShowProgressEvents(true);
			else
				this.workBench.setShowProgressEvents(false);
		}
		else if (command.equals("new")) {
			this.workBench.addNewWorkflow();
		}
		else if (command.equals("close")) {
			this.workBench.removeWorkflow();
		}
		else if (command.equals("save")) {
			this.workBench.saveWorkflow();
		}
		else if (command.equals("load")) {
			this.workBench.loadWorkflow();
		}
		else if (command.equals("run")) {
			this.workBench.runWorkflow();
		}
		else if (command.equals("run")) {
			this.workBench.interruptWorkflowExecution();
		}
		else if (command.equals("quit")) {
			this.grappaMainWin.quit();
		}
		else if (command.equals("rename")) {
			this.workBench.renameWorkflow();
		}
		else if (command.equals("showAbout")) {
			this.showAboutBox();
		}
	}

	/**
	 * Show an about box window.
	 * <p>
	 * Method is supposed to be overwritten by subclasses.
	 */
	protected void showAboutBox() {
		Object[] options = { "OK" };
		String year = 
				Integer.toString(Calendar.getInstance().get(Calendar.YEAR));
		String rev = 
				ALDVersionProviderFactory.getProviderInstance().getVersion();
		if (rev.contains("=")) {
			int equalSign = rev.indexOf("=");
			int closingBracket = rev.lastIndexOf("]");
			rev = 
				rev.substring(0, equalSign + 9) + rev.substring(closingBracket);
		}
		String msg = "<html>Grappa - The Graphical Program Editor for Alida<p><p>"
	    + "Release " + rev + "<p>" + "\u00a9 2010 - " + year + "   "
	    + "Martin Luther University Halle-Wittenberg<p>"
	    + "Institute of Computer Science, Faculty of Natural Sciences III<p><p>"
	    + "Email: alida@informatik.uni-halle.de<p>"
	    + "Internet: <i>www.informatik.uni-halle.de/alida</i><p>"
	    + "License: GPL 3.0, <i>http://www.gnu.org/licenses/gpl.html</i></html>";

		JOptionPane.showOptionDialog(null, new JLabel(msg),
			"Information about Alida", JOptionPane.DEFAULT_OPTION,
		  	JOptionPane.INFORMATION_MESSAGE, this.aboutIcon, options, 
		  	options[0]);
	}
}
