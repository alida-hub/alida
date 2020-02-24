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
import java.awt.event.KeyEvent;
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
import javax.swing.KeyStroke;

import de.unihalle.informatik.Alida.annotations.ALDAOperator;
import de.unihalle.informatik.Alida.annotations.ALDAOperator.Level;
import de.unihalle.informatik.Alida.gui.ALDChooseOpNameFrame;
import de.unihalle.informatik.Alida.gui.ALDOperatorChooserTree;
import de.unihalle.informatik.Alida.gui.ALDOperatorDocumentationFrame;
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
		loadItem.setAccelerator(KeyStroke.getKeyStroke(
				KeyEvent.VK_L, ActionEvent.CTRL_MASK));
		JMenuItem saveItem = new JMenuItem("Save Workflow...");
		saveItem.setActionCommand("save");
		saveItem.addActionListener(this);
		saveItem.setAccelerator(KeyStroke.getKeyStroke(
				KeyEvent.VK_S, ActionEvent.CTRL_MASK));
		JMenuItem quitItem = new JMenuItem("Quit");
		quitItem.setActionCommand("quit");
		quitItem.addActionListener(this);
		quitItem.setAccelerator(KeyStroke.getKeyStroke(
				KeyEvent.VK_Q, ActionEvent.CTRL_MASK));
		fileM.add(loadItem);
		fileM.add(saveItem);
		fileM.addSeparator();
		fileM.add(quitItem);

		// operator level menue
		JMenu opSelectM = new JMenu("Options");
		JMenu opViewM = new JMenu("Operator View");
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
		opViewM.add(operatorSet);
		opSelectM.add(opViewM);
		
		// set correct initial display mode
		if ( this.opTree.getLevel() == Level.APPLICATION)
			radioItemApplication.setSelected(true);
		else
			radioItemAll.setSelected(true);
		
		opSelectM.addSeparator();
		JMenu statusViewM = new JMenu("Status View");
		this.optionCheckboxProgressEvents = 
				new JCheckBox("Show Progress Messages");
		this.optionCheckboxProgressEvents.setSelected(true);
		this.optionCheckboxProgressEvents.setActionCommand(
				"optionShowProgress");
		this.optionCheckboxProgressEvents.addActionListener(this);
		statusViewM.add(this.optionCheckboxProgressEvents);
		opSelectM.add(statusViewM);

		// workflow menu
		JMenu actionsM = new JMenu("Workflow");
		JMenuItem newItem = new JMenuItem("New");
		newItem.setActionCommand("new");
		newItem.addActionListener(this);
		newItem.setAccelerator(KeyStroke.getKeyStroke(
				KeyEvent.VK_N, ActionEvent.CTRL_MASK));
		JMenuItem closeItem = new JMenuItem("Close");
		closeItem.setActionCommand("close");
		closeItem.addActionListener(this);
		closeItem.setAccelerator(KeyStroke.getKeyStroke(
				KeyEvent.VK_W, ActionEvent.CTRL_MASK));
		JMenuItem renameItem = new JMenuItem("Rename");
		renameItem.setActionCommand("rename");
		renameItem.addActionListener(this);
		renameItem.setAccelerator(KeyStroke.getKeyStroke(
				KeyEvent.VK_U, ActionEvent.CTRL_MASK));
		JMenuItem runItem = new JMenuItem("Run");
		runItem.setActionCommand("run");
		runItem.addActionListener(this);
		runItem.setAccelerator(KeyStroke.getKeyStroke(
				KeyEvent.VK_A, ActionEvent.CTRL_MASK));
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
		if (is != null)
			try {
				is.close();
			} catch (IOException e) {
				System.err.println(
						"ALDGrappaMenuBar - problems closing icon resource...!");
			}
	}

	/**
	 * Function to setup the help menu.
	 * @return	Help menu.
	 */
	protected JMenu generateHelpMenu() {
		JMenu helpM = new JMenu("Help");
		JMenuItem itemHelp = new JMenuItem("Grappa Documentation");
		itemHelp.setActionCommand("showDocu");
		itemHelp.addActionListener(this);
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
		else if (command.equals("showDocu")) {
			this.showDocuFrame();
		}
		else if (command.equals("showAbout")) {
			this.showAboutBox();
		}
	}

	/**
	 * Show the documentation for Grappa.
	 */
	protected void showDocuFrame() {
		String docText = 
			"<p>Grappa is a graphical programming editor for Alida. It allows " + 
			"to graphically combine operators into more complex workflows. " + 
			"To this end operators are linked to nodes of a directed, acyclic graph. " + 
			"Each node has several ports corresponding to the underlying operator's " + 
			"input and output parameters. By drawing edges between ports a flow of "+ 
			"data can be defined, which in the end results in a complete workflow "+ 
			"acting on given input data and producing different kinds of result data. </p>\r\n" + 
			"\r\n" + 
			"<p>Grappa’s main window is basically divided into two sections. " + 
			"On the left, the node selection menu is visible, while on the right " + 
			"the workbench area is located. In addition, the window features a " + 
			"menubar for configuring Grappa, loading and saving workflows, and " + 
			"accessing the online help. At the bottom of the window a panel " + 
			"displaying status and progress messages is available.</p>\r\n" + 
			"\r\n" + 
			"<p>" + 
			"<h2>Operator Node Selection Menu</h2>\r\n" + 
			"In the selection menu on the left of Grappa’s main window all Alida "+
			"operators found in the classpath upon initialization are listed as " + 
			"potential nodes for Grappa workflows.</p>" +
			"<h4>Folding and Unfolding packages</h4>\r\n" + 
			"\r\n" + 
			"The operator nodes are arranged in a hierarchical ordering according " + 
			"to their package structure. The different package subtrees can be " + 
			"folded and unfolded by double- clicking on a folder’s name in the " + 
			"selection tree, or by single-clicking on the circle displayed left " + 
			"to the folder icon. \r\n" +
			"<h4>Operator Filtering</h4>\r\n" + 
			"\r\n" + 
			"Above the tree view an operator filter is available which allows to " +
			"select operators according to their names. For filtering, enter a " + 
			"substring into the text field and press the return key.\r\n" +
			"<h4>Selecting Nodes</h4>\r\n" + 
			"\r\n" + 
			"Operator nodes can be added to a workflow by double-clicking on the " + 
			"operator name. A new operator node is then instantiated in the top " + 
			"left corner of the active workflow tab. Alternatively, an operator " + 
			"can be selected by clicking once on its name and afterwards clicking " + 
			"once on the position in the workflow tab where the new operator node " + 
			"should be positioned.\r\n" + 
			"<p>\r\n" + 
			"<h2>Workbench Area</h2>\r\n" + 
			"Workflows can be designed and executed in the workbench area on the " + 
			"right of the main window. It allows for instantiating multiple " + 
			"workflows in parallel where each workflow is linked to an individual " + 
			"tab of the workbench panel.</p>" + 
			"<h4>Context Menus of the Workbench and Nodes</h4>\r\n" + 
			"\r\n" + 
			"The context menu of the workbench is displayed upon right-click on " + 
			"an empty location of the workbench area:\r\n" + 
			"<ul><li>\r\n" + 
			"<p><i>New</i>: add a new workflow tab</p>\r\n" + 
			"</li><li>\r\n" + 
			"<p><i>Rename</i>: rename the currently active tab, default name is ’Untitled’</p>\r\n" + 
			"</li><li>\r\n" + 
			"<p><i>Close</i>: close the currently active tab, unsaved changes are lost!</p>\r\n" + 
			"</li><li>\r\n" + 
			"<p><i>Load</i>: load a workflow from disk</p>\r\n" + 
			"</li><li>\r\n" + 
			"<p><i>Save</i>: save a workflow to disk</p>\r\n" + 
			"</li><li>\r\n" + 
			"<p><i>Run</i>: execute all nodes in the current tab</p>\r\n" + 
			"</li><li>\r\n" + 
			"<p><i>Stop</i>: stop workflow execution</p>\r\n" + 
			"</li></ul>\r\n" + 
			"</p>\r\n" + 
			"\r\n" + 
			"<p>The context menu for a node can be opened by a right-click on the node:\r\n" + 
			"<ul><li>\r\n" + 
			"<p><i>Configure</i>: open the configuration window</p>\r\n" + 
			"</li><li>\r\n" + 
			"<p><i>Run...</i>: run the workflow or several nodes\r\n" + 
			"<ul><li>\r\n" + 
			"<p><i>Workflow</i>: execute the whole workflow, i.e., all nodes in the current tab</p>\r\n" + 
			"</li><li>\r\n" + 
			"<p><i>Nodes from here</i>: run only the current node and successors</p>\r\n" + 
			"</li><li>\r\n" + 
			"<p><i>Node</i>: run only the current node</p>\r\n" + 
			"</li></ul>\r\n" + 
			"</p>\r\n" + 
			"</li><li>\r\n" + 
			"<p><i>Show Results</i>: only active after node execution terminated, opens result frame</p>\r\n" + 
			"</li><li>\r\n" + 
			"<p><i>View Mode</i>: select operator parameters to display\r\n" + 
			"<ul><li>\r\n" + 
			"<p><i>Standard</i>: display only standard parameters</p>\r\n" + 
			"</li><li>\r\n" + 
			"<p><i>Advanced</i>: display also parameters for expert usage</p>\r\n" + 
			"</li></ul>\r\n" + 
			"</p>\r\n" + 
			"</li><li>\r\n" + 
			"<p><i>Remove</i>: delete the node</p>\r\n" + 
			"</li></ul>\r\n" + 
			"\r\n" + 
			"<h4>Operator Nodes</h4>\r\n" + 
			"\r\n" + 
			"For each operator selected via the operator node selection menu " + 
			"(see above) a node in terms of a rectangle is added to the currently " + 
			"active workflow. Above the rectangle the name of the operator is " + 
			"displayed, while on its left and right side the operator’s input and " + 
			"output ports are shown as circles and squares. Circles are associated " + 
			"with operator parameters of directions <i>IN</i> or <i>OUT</i>, " + 
			"while squares refer to parameters with direction <i>INOUT</i>. " + 
			"The latter ports are duplicated on both sides of the node. The colors " + 
			"of the circles indicate their type. Blue circles refer to required " + 
			"parameters, yellow circles are associated with optional parameters, " + 
			"and red circles are linked to supplemental parameters. To the left " + 
			"and right of the ports, respectively, the name of the corresponding " + 
			"parameters are written. Once operator nodes have been added to a workflow, " + 
			"they can easily be dragged and repositioned as well as resized via " + 
			"intuitive mouse actions.\r\n" + 
			"<h4>Node Configuration</h4>\r\n" + 
			"\r\n" + 
			"On selecting the item for configuration, a window is displayed which " + 
			"allows to enter parameter values. Parameters values can specified by " + 
			"either of the three ways:\r\n" + 
			"<ul><li>\r\n" + 
			"<p>directly by entering values via the configuration window</p>\r\n" + 
			"</li><li>\r\n" + 
			"<p>loading values from a proper parameter file in XML format</p>\r\n" + 
			"</li><li>\r\n" + 
			"<p>dragging edges between ports of different nodes to propagate output data from one node as input data to another</p>\r\n" + 
			"</li></ul>\r\n" + 
			"\r\n" + 
			"<h4>Dragging Edges</h4>\r\n" + 
			"\r\n" + 
			"To add an edge, move the mouse over an output port of a node until " + 
			"the port is surrounded by a green square, then press the left mouse " + 
			"button. Subsequently, while keeping the button pressed, move the " + 
			"mouse to the desired input port of another node. Once a green rectangle " + 
			"shows up around the target input port, release the button.\r\n" + 
			"\r\n" + 
			"<p>Note that on dragging edges Grappa performs type and validity checks. " + 
			"Only ports being associated with compatible parameter data types can " + 
			"be linked to each other. Two parameter data types are compatible if " + 
			"they are either equal, the target data type is a super class of the " + 
			"source data type, or if Alida has access to a converter allowing to " + 
			"transform the source data type into the target type. Also edges are " + 
			"forbidden that would induce cycles into the workflow graph.</p>\r\n" + 
			"<h4>Node States and Execution</h4>\r\n" + 
			"\r\n" + 
			"Nodes in a workflow can have different states indicated by the color " + 
			"of their border. Red framed nodes are not ready for execution, i.e., " + 
			"their configuration is not complete. If a node is readily configured " + 
			"and can directly be executed, its border has a yellow color, while " + 
			"nodes that are configured, however, require additional input data " + 
			"from preceeding operator nodes have an orange color. Prior to executing " + 
			"these orange nodes it is, thus, necessary to execute the preceeding " + 
			"nodes first. Note that Grappa takes care of such dependencies, i.e., " + 
			"automatically executes nodes first from which result data is required " + 
			"for proper workflow or node execution. Note that the execution of a " + 
			"workflow will fail if one of the nodes is still colored red, or if a " + 
			"node does not produce proper output data required by others.\r\n" + 
			"\r\n" + 
			"<p>The state of a node is updated by Grappa in real-time, i.e., each " + 
			"change in its configuration directly invokes internal checkings and " + 
			"may result in a change of the node’s color.</p>\r\n" + 
			"\r\n" + 
			"<p>After successful execution of the workflow or a subset of nodes, " + 
			"the colors of the corresponding nodes change to green indicating that " + 
			"result data are available. For all terminal nodes having no successor " + 
			"the result frames are automatically opened. For all other nodes the " + 
			"result data can graphically be examined via the nodes’ context menus " + 
			"from which the result windows can manually be opened. Once a node has " + 
			"been executed and is colored in green, it is not possible to re-execute " + 
			"the node until its configuration, or at least the configuration of one " + 
			"of its preceeding nodes, was changed.</p>\r\n" + 
			"<p>\r\n" + 
			"<h2>Menu Bar and Keyboard Shortcuts</h2>\r\n" + 
			"\r\n" + 
			"The Grappa main window features a menubar offering quick access " + 
			"to the basic functions of Grappa and some additional convenience " + 
			"functionality simplifying the work with the editor:</p>\r\n" + 
			"\r\n" + 
			"<ul><li>\r\n" + 
			"<p><i>File</i>:\r\n" + 
			"<ul><li>\r\n" + 
			"<p><i>Quit</i>: exit Grappa</p>\r\n" + 
			"</li></ul>\r\n" + 
			"</p>\r\n" + 
			"</li><li>\r\n" + 
			"<p><i>Operator Level</i>:\r\n" + 
			"<ul><li>\r\n" + 
			"<p><i>Application</i>: just show operators suitable for non-expert users</p>\r\n" + 
			"</li><li>\r\n" + 
			"<p><i>Standard</i>: show all operators available</p>\r\n" + 
			"</li></ul>\r\n" + 
			"</p>\r\n" + 
			"</li><li>\r\n" + 
			"<p><i>Workflow</i>:\r\n" + 
			"<ul><li>\r\n" + 
			"<p><i>New</i>: add a new workflow tab</p>\r\n" + 
			"</li><li>\r\n" + 
			"<p><i>Rename</i>: rename the currently active tab</p>\r\n" + 
			"</li><li>\r\n" + 
			"<p><i>Close</i>: close the currently active tab</p>\r\n" + 
			"</li><li>\r\n" + 
			"<p><i>Load</i>: load a workflow from disk</p>\r\n" + 
			"</li><li>\r\n" + 
			"<p><i>Save</i>: save a workflow to disk</p>\r\n" + 
			"</li><li>\r\n" + 
			"<p><i>Run</i>: execute all nodes in the current tab</p>\r\n" + 
			"</li><li>\r\n" + 
			"<p><i>Stop</i>: stop workflow execution</p>\r\n" + 
			"</li></ul>\r\n" + 
			"</p>\r\n" + 
			"</li><li>\r\n" + 
			"<p><i>Help</i>:\r\n" + 
			"<ul><li>\r\n" + 
			"<p><i>Online Help</i>: access Alida’s online help system</p>\r\n" + 
			"</li><li>\r\n" + 
			"<p><i>About Grappa</i>: display version information of Alida/Grappa</p>\r\n" + 
			"</li></ul>\r\n" + 
			"</p>\r\n" + 
			"</li></ul>\r\n" + 
			"<h4>Operator Level</h4>\r\n" + 
			"Alida supports two categories of operators, i.e., <i>STANDARD</i> " + 
			"operators and operators mainly dedicated to direct <i>APPLICATION</i>. " + 
			"The latter ones are well-suited to be used by non-expert users. " + 
			"Other operators often require expert knowledge.\r\n" + 
			"<h4>Saving and loading workflows</h4>\r\n" + 
			"By saving a workflow currently two files are written to disk, one " + 
			"containing the information about the nodes and their configuration, " + 
			"and one storing graphical information regarding the current workflow " + 
			"layout. Both are required to load a workflow again. The first one " + 
			"has the extension <i>.awf</i>, the latter one the extension " + 
			"<i>.awf.gui</i>.\r\n" + 
			"<h4>Keyboard Shortcuts</h4>\r\n" + 
			"The most important functions for workflow and node handling in " + 
			"Grappa are also accessible via keyboard shortcuts. Currently the " + 
			"following shortcuts are implemented:\r\n" + 
			"<ul><li>\r\n" + 
			"<p><i>Ctrl-N</i> : open a new, empty workflow in a new tab</p>\r\n" + 
			"</li><li>\r\n" + 
			"<p><i>Ctrl-U</i> : rename the active workflow</p>\r\n" + 
			"</li><li>\r\n" + 
			"<p><i>Ctrl-W</i> : close the active workflow</p>\r\n" + 
			"</li><li>\r\n" + 
			"<p><i>Ctrl-S</i> : save the active workflow to disk</p>\r\n" + 
			"</li><li>\r\n" + 
			"<p><i>Ctrl-L</i> : load a workflow from disk into a new tab</p>\r\n" + 
			"</li><li>\r\n" + 
			"<p><i>Ctrl-A</i> : run the complete workflow</p>\r\n" + 
			"</li><li>\r\n" + 
			"<p><i>Ctrl-P</i> : open the configuration frames of all selected nodes in the active workflow</p>\r\n" + 
			"</li><li>\r\n" + 
			"<p><i>Ctrl-X</i> : delete all selected nodes in the active workflow</p>\r\n" + 
			"</li></ul>\r\n";
		
		ALDOperatorDocumentationFrame doc = 
				new ALDOperatorDocumentationFrame("Grappa - the Workflow Editor",
						this.getClass().getName(), docText);
		doc.setVisible(true);
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
