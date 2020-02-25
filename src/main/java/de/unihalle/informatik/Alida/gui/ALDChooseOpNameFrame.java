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

import javax.swing.*;
import javax.swing.event.*;

import java.awt.*;
import java.awt.event.*;
import java.util.*;
import java.lang.ref.WeakReference;

import de.unihalle.informatik.Alida.annotations.*;
import de.unihalle.informatik.Alida.annotations.ALDAOperator.Level;
import de.unihalle.informatik.Alida.gui.ALDOperatorChooserTreeNode;
import de.unihalle.informatik.Alida.helpers.ALDClassInfo;
import de.unihalle.informatik.Alida.helpers.ALDIcon;
import de.unihalle.informatik.Alida.operator.ALDOperatorLocation;
import de.unihalle.informatik.Alida.version.ALDVersionProviderFactory;
import de.unihalle.informatik.Alida.workflows.ALDWorkflowHelper;

/**
 * Main window for selecting Alida annotated operators for running.
 * For a selected operator a configuration window may be created by double clicking or
 * hitting the configure button.
 * <p>
 * This class holds two different trees of operators, one for the standard level, one for
 * the application level.
 * These trees contain all <code>ALDAOperator</code> annotated operators allowed to be executed in a GUI,
 * and annotated as <code>Level.STANDARD</code> and <code>Level.APPLICATION</code>, respectively.
 * Deriving classes may override the method <code>additionalOperators</code> to extend the trees.
 * <p>
 * In deriving classes the method <code>getOpControlFrame</code> may be overridden to define a different
 * frame to handle the configuration of the selected operator. The frame created in by 
 * <code>ALDChooseOpNameFrame</code> is
 * {@link ALDOperatorControlFrame}.
 *   
 * @author Stefan Posch
 * @author Birgit Moeller
 */
public class ALDChooseOpNameFrame extends JFrame 
	implements  ActionListener, TreeSelectionListener {

	/**
	 * Debug flag (not accessible from outside).
	 */
	private boolean debug = false;
	
	/**
	 * Location of recently selected operator.
	 */
	protected ALDOperatorLocation opLocation = null;

	/**
	 * JComponent containing both trees of available operators.
	 */
	protected ALDOperatorChooserTree opTree;

	/**
	 * Main panel of chooser window.
	 */
	protected JRootPane mainPanel;
	
	/**
	 * Label to display selected operator.
	 */
	protected JLabel opNameJText;
	
	/**
	 * Text field to enter filter to select operators
	 */
	protected JTextField filterField;
	
	/**
	 * Scroll pane displaying the operator tree.
	 */
	protected JScrollPane opTreePane;

	/**
	 * Title of frame.
	 */
	protected String titleString = 
		"Alida - OpRunner: simply choose an operator...";

	/**
	 * List of active GUI managers.
	 */
	protected LinkedList<WeakReference<ALDOperatorGUIExecutionProxy>> guiProxys;
	
	/**
	 * Constructor.
	 */
	public ALDChooseOpNameFrame() {
		if ( this.debug )
			System.out.println( " ALDChooseOpNameFrame instantiate");
		
		// instantiate operator trees
		Collection<ALDOperatorLocation> standardOps = 
				ALDClassInfo.lookupOperators(
						ALDAOperator.Level.STANDARD, ALDAOperator.ExecutionMode.SWING);
		standardOps.addAll( ALDWorkflowHelper.lookupWorkflows());
		standardOps.addAll( additionalOperators( ALDAOperator.Level.STANDARD));
		
		Collection<ALDOperatorLocation> allicationOps = 
				ALDClassInfo.lookupOperators(
						ALDAOperator.Level.APPLICATION, ALDAOperator.ExecutionMode.SWING);		
		allicationOps.addAll( ALDWorkflowHelper.lookupWorkflows());
		allicationOps.addAll( additionalOperators( ALDAOperator.Level.APPLICATION));

		// init the operator chooser tree
		this.opTree = new ALDOperatorChooserTree( standardOps, allicationOps);
		MouseListener ml = new MyMouseAdapter();
		this.opTree.addMouseListener(ml);
		this.opTree.addTreeSelectionListener(this);
        
		// init list of active GUI managers
    this.guiProxys= 
    		new LinkedList<WeakReference<ALDOperatorGUIExecutionProxy>>();
        
		// global language settings
		Locale.setDefault(Locale.ENGLISH);
		JComponent.setDefaultLocale(Locale.ENGLISH);

		// set up main window
		this.mainPanel = new JRootPane();
		BoxLayout ylayout = new BoxLayout( this.mainPanel, BoxLayout.Y_AXIS);
		this.mainPanel.setLayout( ylayout);

		this.opTreePane = new JScrollPane( this.opTree);
		this.mainPanel.add(this.opTreePane,0);
		
		// do some global settings
		this.setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);

		// TODO Does this anything? -> if not, remove...
		this.mainPanel.setBorder(BorderFactory.createEmptyBorder(10,10,10,10));

		// text field for operator filter
		JButton buttonApplyFilter =  new JButton("Apply Filter");
		buttonApplyFilter.setActionCommand( "filter");
		buttonApplyFilter.addActionListener(this);

		// add panel for filtering operators
		JPanel filterPanel = new JPanel();
		filterPanel.add(new JLabel("Operator Filter:"));
		this.filterField = new JTextField(20);
		this.filterField.setActionCommand( "filter");
		this.filterField.addActionListener(this);
		filterPanel.add(this.filterField);
		filterPanel.add(buttonApplyFilter);
		filterPanel.add(Box.createRigidArea(new Dimension(10, 0)));
		this.mainPanel.add(filterPanel);

		// add panel for displaying selected operator
		JLabel lab =  new JLabel("Selected Operator: ");
		this.opNameJText = new JLabel( "- none -");
		JPanel selelectePanel = new JPanel(); 
		selelectePanel.add( lab);
		selelectePanel.add(Box.createRigidArea(new Dimension(10, 0)));
		selelectePanel.add( this.opNameJText);
		this.mainPanel.add( selelectePanel);

		// add buttons
		JButton configButton = new JButton("Configure Operator...");
		configButton.setActionCommand( "execute");
		configButton.addActionListener(this);
		configButton.setBounds(50, 60, 80, 30);
		configButton.setAlignmentX(Component.LEFT_ALIGNMENT);

		JButton quitButton = new JButton("Quit");
		quitButton.setActionCommand( "quit");
		quitButton.addActionListener(this);
		quitButton.setBounds(50, 60, 80, 30);
		quitButton.setAlignmentX(Component.LEFT_ALIGNMENT);

		JPanel buttonPanel = new JPanel();
		buttonPanel.setLayout(new BoxLayout(buttonPanel, BoxLayout.LINE_AXIS));
		buttonPanel.setBorder(BorderFactory.createEmptyBorder(0, 10, 10, 10));
		buttonPanel.add(Box.createHorizontalGlue());
		buttonPanel.add(configButton);
		buttonPanel.add(Box.createRigidArea(new Dimension(10, 0)));
		buttonPanel.add(quitButton);

		this.mainPanel.add(buttonPanel);

		// add a nice menu bar, file menu first
		JMenuBar mainWindowMenu = new JMenuBar();
		JMenu fileM = new JMenu("File");
		JMenuItem quitItem = new JMenuItem("Quit");
		quitItem.setActionCommand("quit");
		quitItem.addActionListener(this);
		fileM.add(quitItem);
		
		// options menu
		JMenu opSelectM = new JMenu("Options");
//		ButtonGroup operatorLevelGroup = new ButtonGroup();
//		// default
//		JRadioButtonMenuItem radioItemApplication =
//			new JRadioButtonMenuItem("Application");
//		radioItemApplication.setActionCommand("viewApps");
//		radioItemApplication.addActionListener(this);
//		JRadioButtonMenuItem radioItemStandard = 
//			new JRadioButtonMenuItem("Standard");
//		radioItemStandard.setActionCommand("viewStd");
//		radioItemStandard.addActionListener(this);
//		operatorLevelGroup.add(radioItemApplication);
//		operatorLevelGroup.add(radioItemStandard);
//		opSelectM.add(radioItemApplication);
//		opSelectM.add(radioItemStandard);
//
//		if ( this.opTree.getLevel() == Level.APPLICATION)
//			radioItemApplication.setSelected(true);
//		else
//			radioItemStandard.setSelected(true);
		JMenu operatorSet = new JMenu("Operators to Show");
		ButtonGroup operatorSetGroup = new ButtonGroup();
		JRadioButtonMenuItem radioItemApplication =
			new JRadioButtonMenuItem("Default");
		radioItemApplication.setToolTipText("<html>Shows operators to be used "
			+ "out-of-the-box<br> and well-suited also for non-expert users.</html>");
		radioItemApplication.setActionCommand("viewApps");
		radioItemApplication.addActionListener(this);
		JRadioButtonMenuItem radioItemAll = 
			new JRadioButtonMenuItem("All");
		radioItemAll.setToolTipText("<html>Shows all operators including very" 
			+ " specialized<br> ones requiring advanced expert knowledge.</html>");
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

		JMenu helpM = this.generateHelpMenu();
		mainWindowMenu.add(fileM);
		mainWindowMenu.add(opSelectM);
		mainWindowMenu.add(Box.createHorizontalGlue());
		mainWindowMenu.add(helpM);

		// ... and go setup the frame
		this.setTitle(this.titleString);
		this.getContentPane().setPreferredSize(new Dimension(500, 700));
		this.pack();
		this.setJMenuBar(mainWindowMenu);
		this.add(this.mainPanel);
		this.setDefaultCloseOperation( DISPOSE_ON_CLOSE);
		this.setVisible(true);
	}
	
	/** This method may be overridden in extending classes to add operators to the
	 * list of available operators.
	 * 
	 * @param level Level to which operators are to be added
	 */
	protected Collection<ALDOperatorLocation> additionalOperators( 
			@SuppressWarnings("unused") ALDAOperator.Level level) {
		return new LinkedList<ALDOperatorLocation>();
	}
		
	/**
	 * Sets up the help menu.
	 * 
	 * @return Generated help menu.
	 */
	protected JMenu generateHelpMenu() {
		JMenu helpM = new JMenu("Help");
		JMenuItem itemHelp = new JMenuItem("Documentation");
		itemHelp.setActionCommand("showDocu");
		itemHelp.addActionListener(this);
//		itemHelp.addActionListener(
//			OnlineHelpDisplayer.getHelpActionListener(itemHelp,
//				"alida.guiOpRunner"	,this));
		JMenuItem itemAbout = new JMenuItem("About Alida");
		itemAbout.setActionCommand("showAbout");
		itemAbout.addActionListener(this);
		helpM.add(itemHelp);
		helpM.add(itemAbout);
		return helpM;
	}	
	
	/**
	 * Show the documentation for the chooser.
	 */
	protected void showDocuFrame() {
		String docText = "The acronyme Alida refers to our <b>Advanced Library " + 
				"for Integrated Development of Data Analysis Applications</b>. " + 
				"Alida defines data analysis procedures in terms of <i>operators</i> " + 
				"which subsequently or in parallel are applied to given data.\n" + 
				"\n" + 
				"<p>All Alida operators implement a common interface which defines " + 
				"operator input and output parameters as well as a unified invocation " + 
				"procedure. Thus, all operators developed based on the Alida library " + 
				"can be configured and invoked in a standardized manner. This lays " + 
				"the fundament for integrating several very nice features in Alida " + 
				"which significantly increase the usability of Alida-based libraries, " + 
				"both for programmers as well as for users. In particular, the unified " + 
				"operator interface, e.g., supports</p>\n" + 
				"\n" + 
				"<ul><li>\n" + 
				"<p>automatic generation of graphical and commandline user interfaces</p>\n" + 
				"</li><li>\n" + 
				"<p>automatic process documentation by operator call graphs</p>\n" + 
				"</li></ul>\n" + 
				"\n" + 
				"<p>The graphical operator runner that you have started already " + 
				"presents you all properly annotated operators based on Alida that " + 
				"are found in the classpath. Given the tree view of these operators " + 
				"you can select the operator of your choice, configure it, execute it, " + 
				"and finally inspect its results (for more details see below).</p>\n" + 
				"\n" + 
				"<p>Initially Alida ships with some demo operators that demonstrate " + 
				"the broad range of features Alida incorporates, e.g.</p>\n" + 
				"\n" + 
				"<ul><li>\n" + 
				"<p>inherent data I/O for the most common Java data types, like\n" + 
				"<ul><li>\n" + 
				"<p>native types (int, boolean, Double, String, etc.)</p>\n" + 
				"</li><li>\n" + 
				"<p>1D and 2D arrays</p>\n" + 
				"</li><li>\n" + 
				"<p>enumerations</p>\n" + 
				"</li><li>\n" + 
				"<p>collections</p>\n" + 
				"</li></ul>\n" + 
				"</p>\n" + 
				"</li><li>\n" + 
				"<p>support for operators as parameters</p>\n" + 
				"</li><li>\n" + 
				"<p>support for parametrized classes as parameters</p>\n" + 
				"</li><li>\n" + 
				"<p>ranking of operators, i.e. as standard or application</p>\n" + 
				"</li><li>\n" + 
				"<p>grading of parameters as standard or advanced</p>\n" + 
				"</li><li>\n" + 
				"<p>saving and loading of parameters</p>\n" + 
				"</li></ul>\n" + 
				"\n" + 
				"<p>In parametrized classes members can be annotated as parameters. " + 
				"Given that appropriate I/O providers are available, these classes " + 
				"can then directly be used as operator parameters without additional " + 
				"effort.</p>\n" + 
				"<p>" +
				"<h1>Graphical Operator Runner</h1>\n" +
				"The graphical operator runner of Alida welcomes you with a window " + 
				"to select the operator that you wish to execute. </p>\n" + 
				"<h2>The window</h2>\n" + 
				"\n" + 
				"The main component of the window is the tree view of all available " + 
				"operators. It is organized according to the package structure of " + 
				"the operators. On top of the window there is a menubar located " + 
				"with different menus:\n" + 
				"<ul><li>\n" + 
				"<p>\"File\", which allows to quit the application</p>\n" + 
				"</li><li>\n" + 
				"<p>\"Operator Level\", which allows you to select the level of the " + 
				"operators you wish to have displayed in the tree; " + 
				"there are two levels available:\n" + 
				"<ul><li>\n" + 
				"<p>\"Application\" for operators suitable for non-expert use</p>\n" + 
				"</li><li>\n" + 
				"<p>\"Standard\" for all available operators</p>\n" + 
				"</li></ul>\n" + 
				"</p>\n" + 
				"</li><li>\n" + 
				"<p>\"Help\", where you have already found this online help and " + 
				"some additional meta information</p>\n" + 
				"</li></ul>\n" + 
				"</p>\n" +
				"<h2>Selecting an operator</h2>\n" + 
				"\n" + 
				"To select an operator for execution you can...\n" + 
				"<ul><li>\n" + 
				"<p>double-click the corresponding leave in the tree</p>\n" + 
				"</li><li>\n" + 
				"<p>select the leave with the mouse and then click on the \"Configure Operator...\" button</p>\n" + 
				"</li></ul>\n" + 
				"\n" + 
				"<p>Note that the name of the currently selected operator is shown below the tree panel.</p>\n" + 
				"\n" + 
				"<p>After selecting an operator a control window will pop-up that " + 
				"allows you to configure the parameters of your operator and let it run (see below). " + 
				"On termination another frame will be shown with the results of " + 
				"your operator run.</p>\n" + 
				"<h2>Terminating your session</h2>\n" + 
				"\n" + 
				"To exit the current session, just click the \"Quit\" button or " + 
				"choose the corresponding menu entry from the menubar.\n" + 
				"<p>" +
				"<h1 class=\"title\">Operator control window</h1>\n" + 
				"\n" + 
				"The operator control window that pops-up after selecting an operator " + 
				"contains GUI elements for configuring the parameters of your operator. " + 
				"In addition, it provides you with buttons to run the operator.\n" + 
				"\n" + 
				"<p>The menubar of the control window has the following items:</p>\n" + 
				"\n" + 
				"<ul><li>\n" + 
				"<p><b>\"File\"</b>, where you can save and load the current parameter settings, and where you can close the window</p>\n" + 
				"</li><li>\n" + 
				"<p><b>\"Actions\"</b>, where you can run the operator and do a validation of the parameters, i.e. check if all parameters are properly configured and the operator is ready to run</p>\n" + 
				"</li><li>\n" + 
				"<p><b>\"View\"</b>,  where you can switch the view between displaying all parameters (\"advanced\") or just the basic parameters (\"standard\") of your operator</p>\n" + 
				"</li><li>\n" + 
				"<p><b>\"Help\"</b>, where you find operator-specific documentation</p>\n" + 
				"</li></ul>\n" + 
				"\n" + 
				"<p>At the bottom of the window a statusbar is located that displays " + 
				"to you the current status of the control window, e.g. if the " + 
				"operator is running.</p>"; 
		
		ALDOperatorDocumentationFrame doc = 
				new ALDOperatorDocumentationFrame("Alida - Welcome!",
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
		String year = Integer.toString(Calendar.getInstance().get(Calendar.YEAR));
		String rev = ALDVersionProviderFactory.getProviderInstance().getVersion();
		if (rev.contains("=")) {
			int equalSign = rev.indexOf("=");
			int closingBracket = rev.lastIndexOf("]");
			rev = rev.substring(0, equalSign + 9) + rev.substring(closingBracket);
		}
		String msg = "<html>Alida - Advanced Library for Integrated Development<p>" 
			+ "\t of Data Analysis Applications<p><p>"
	    + "Release " + rev + "<p>" + "\u00a9 2010 - " + year + "   "
	    + "Martin Luther University Halle-Wittenberg<p>"
	    + "Institute of Computer Science, Faculty of Natural Sciences III<p><p>"
	    + "Email: mitobo@informatik.uni-halle.de<p>"
	    + "Internet: <i>www.informatik.uni-halle.de/alida</i><p>"
	    + "License: GPL 3.0, <i>http://www.gnu.org/licenses/gpl.html</i></html>";

		JOptionPane.showOptionDialog(null, new JLabel(msg),
		  "Information about Alida", JOptionPane.DEFAULT_OPTION,
		    JOptionPane.INFORMATION_MESSAGE, ALDIcon.getInstance().getIcon(), 
		    	options, options[0]);
	}
	
	/**
	 * Executes the chosen operator, i.e. opens the config/control window.
	 * @param opLoc		Location from where to instantiate the operator object.
	 */
	protected void executeOperator(ALDOperatorLocation opLoc) {
		// do we have an operator name?
		if (opLoc != null ) {
			ALDOperatorGUIExecutionProxy execManager = 
					new ALDOperatorGUIExecutionProxy(opLoc);
			this.guiProxys.add(
					new WeakReference<ALDOperatorGUIExecutionProxy>(execManager));
			execManager.showGUI();
		}
	}
	
	/**
	 * Cleans-up on termination, e.g. closes all windows.
	 */
	protected void quit() {
		// terminate all running GUI managers
		boolean quitsOk = true;
		for (WeakReference<ALDOperatorGUIExecutionProxy> guimRef: this.guiProxys) {
			ALDOperatorGUIExecutionProxy guim = guimRef.get();
			if (guim != null) {
				quitsOk = quitsOk && guim.quit();
			}
		}
		// close online help window
//		OnlineHelpDisplayer.closeWindow();
		
		// close the chooser window itself
		if (quitsOk)
			this.dispose();
	}
	
	/* (non-Javadoc)
	 * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
	 */
	@Override
	public final void actionPerformed(ActionEvent e) {

		// local variables
		String command = e.getActionCommand();

		// configure button has been pressed
		if ( command.equals("execute") ) {
			this.executeOperator(this.opLocation);
		}

		// quit button has been pressed
		else if ( command.equals("quit") ) {
			this.quit();
		}

		/* handle tree view options */
		else if (command.equals("viewApps")) {
			this.opTree.setLevel( ALDAOperator.Level.APPLICATION);
		}
		else if (command.equals("viewStd")) {
			this.opTree.setLevel( ALDAOperator.Level.STANDARD);
		}

		/* display about box */
		else if (command.equals("showDocu")) {
			showDocuFrame();
		}

		/* display about box */
		else if (command.equals("showAbout")) {
			showAboutBox();
		}
		
		/* operator name filter */
		else if (command.equals("filter")) {
			this.opTree.setOpNameFilter( this.filterField.getText());
		}
	}

	@Override
	public void valueChanged(TreeSelectionEvent event) {
		ALDOperatorChooserTreeNode node = 
				this.opTree.getLastSelectedPathComponent();
		// if sub-tree has been folded in, last selected node is null -			
		// do nothing in that case
		if (node == null)
			return;
		if (node.isOperator()) {
			this.opLocation = node.getLocation(); //new String(node.getFullName());
//			this.opName = this.opTree.getLastSelectedPathComponent().toString();
			this.opNameJText.setText(
					this.opTree.getLastSelectedPathComponent().toString());
			this.opNameJText.setToolTipText(node.getLocation().getName());
		} else {
			this.opLocation = null;
//			this.opName = null;
		}
	}

	/**
	 * Local {@link MouseAdapter} class.
	 * <p>
	 * Handles double click on operator name to execute it.
	 * 
	 * @author posch
	 */
	protected class MyMouseAdapter extends MouseAdapter {

		/**
		 * Constructor.
		 */
		public MyMouseAdapter() {
			// nothing to do here
		}

		@Override
		public void mousePressed(MouseEvent e) {
			int selRow =
				ALDChooseOpNameFrame.this.opTree.getRowForLocation(e.getX(), e.getY());
			if(selRow != -1) {
				if(e.getClickCount() == 2) {
					ALDOperatorChooserTreeNode node = 
							ALDChooseOpNameFrame.this.opTree.getLastSelectedPathComponent();
					if ( node.isOperator() ) {
						// do some changes in chooser frame
						ALDChooseOpNameFrame.this.opLocation = node.getLocation();
						ALDChooseOpNameFrame.this.opNameJText.setText(node.toString());
						ALDChooseOpNameFrame.this.opNameJText.setToolTipText(
																					node.getLocation().getName());
						ALDChooseOpNameFrame.this.executeOperator(node.getLocation());
					}
				}
			}
		}
	}
}
