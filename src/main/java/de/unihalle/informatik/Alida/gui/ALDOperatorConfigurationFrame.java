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

import java.awt.*;
import java.awt.event.*;
import java.io.File;
import java.util.Calendar;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Set;

import javax.swing.*;
import javax.swing.event.EventListenerList;

import de.unihalle.informatik.Alida.operator.*;
import de.unihalle.informatik.Alida.operator.events.ALDOpParameterUpdateEvent;
import de.unihalle.informatik.Alida.operator.events.ALDOpParameterUpdateEventListener;
import de.unihalle.informatik.Alida.operator.events.ALDOpParameterUpdateEventReporter;
import de.unihalle.informatik.Alida.version.ALDVersionProviderFactory;
import de.unihalle.informatik.Alida.annotations.Parameter;
import de.unihalle.informatik.Alida.dataio.ALDDataIOManagerSwing.ProviderInteractionLevel;
import de.unihalle.informatik.Alida.dataio.ALDDataIOManagerXmlbeans;
import de.unihalle.informatik.Alida.dataio.provider.swing.components.ALDOperatorParameterPanel;
import de.unihalle.informatik.Alida.dataio.provider.swing.events.*;
import de.unihalle.informatik.Alida.exceptions.ALDDataIOProviderException;
import de.unihalle.informatik.Alida.exceptions.ALDOperatorException;
import de.unihalle.informatik.Alida.exceptions.ALDOperatorException.OperatorExceptionType;
import de.unihalle.informatik.Alida.helpers.ALDIcon;

/**
 * Frame to configure an operator.
 * 
 * @author Birgit Moeller
 */
public class ALDOperatorConfigurationFrame extends JFrame 
	implements ActionListener, ALDSwingValueChangeListener,
		ALDOpParameterUpdateEventReporter {

	/**
	 * The operator associated with this frame.
	 */
	protected ALDOperator op = null;

	/**
	 * The top level panel of this frame.
	 */
	protected JPanel inputPanel;

	/**
	 * Title string of window.
	 */
	protected String titleString = null;

	/**
	 * Mode for displaying parameters.
	 * <p>
	 * In standard mode only parameters annotated as standard are shown, in
	 * advanced mode all parameters annotated are displayed.
	 */
	protected Parameter.ExpertMode displayMode = Parameter.ExpertMode.STANDARD;

	/**
	 * Last directory visited, initially it's user's home.
	 */
	protected String lastDirectory = System.getProperty("user.home");

	/**
	 * Last selected file.
	 */
	protected File lastFile = new File("operatorParams.xml");

	/**
	 * Level of interaction providers are allowed to perform.
	 */
	protected ProviderInteractionLevel pLevel =
			ProviderInteractionLevel.ALL_ALLOWED;

	/**
	 * Panel where operator parameters are embedded.
	 */
	protected ALDOperatorParameterPanel operatorParameterPanel;

	/**
	 * Label of status bar, changes dynamically.
	 */
	protected JLabel status;

	/**
	 * Ok label to be used on button of Ok message boxes.
	 */
	protected final Object[] okOption = { "OK" };

	/**
	 * Labels to be used on buttons of Yes/No message boxes.
	 */
	protected final Object[] yesnoOption = { "YES", "NO" };

	/**
	 * List of registered event listeners.
	 */
	protected volatile EventListenerList listenerList =	new EventListenerList();

//	/**
//	 * Radio button for selecting standard configuration mode.
//	 */
//	protected JRadioButtonMenuItem radioItemStandard;
//
//	/**
//	 * Radio button for selecting advanced configuration mode.
//	 */
//	protected JRadioButtonMenuItem radioItemAdvanced;
	
	/**
	 * Checkbox to enable/disable display of advanced parameters.
	 */
	protected JCheckBox showAllParameters;

	/**
	 * Tab pane for configuration pane, batch mode pane, etc.
	 */
	protected JTabbedPane tabPane;
	
	/** 
	 * Constructs a control frame for an operator object.
	 * @param _op Operator to be associated with this frame object.
	 * @throws ALDOperatorException
	 */
	public ALDOperatorConfigurationFrame(ALDOperator _op, 
		ALDOpParameterUpdateEventListener pListen) 
			throws ALDOperatorException {
		if (_op == null)
			throw new ALDOperatorException(
					OperatorExceptionType.INSTANTIATION_ERROR,
					"[ALDOperatorConfigurationFrame(op)] " 
					+ "no operator given, object null!");
		this.op = _op;
		this.titleString = 
				"ALDOperatorConfigurationFrame: " + this.op.getName();
		this.addALDOpParameterUpdateEventListener(pListen);
		init();
	}
	
	/** 
	 * Does the main work to instantiate the frame.
	 */
	protected void init() {
		if (this.op == null) {
			JOptionPane.showOptionDialog(null,
					"Problems instantiating chosen operator...\n" +
							"Please check class implementation or contact programmer.",
							"Warning", JOptionPane.DEFAULT_OPTION,
							JOptionPane.WARNING_MESSAGE, null, this.okOption,
							this.okOption[0]);
			return;
		}
		// init the window
		this.setupWindow();
	}
	
	/**
	 * Construct the frame to configure an operator.
	 */
	protected void setupWindow() {

		/* Choose initial window size according to desktop, i.e. to a maximum of
		 * 40% of the desktop width and to maximum of 90% of its height; 
		 * if width is larger than 1600 pixels, maybe multiple screens are in use,
		 * in that case the width is fixed to a maximum of 1600.                
		 */
		
		final double widthFraction = 0.4;
		final double heightFraction = 0.9;
		
		// get size of desktop
		int desktopWidth = Toolkit.getDefaultToolkit().getScreenSize().width;
		int desktopHeight = Toolkit.getDefaultToolkit().getScreenSize().height;
		
		// estimate size according to number of parameters in panel...
		int paramNum = this.op.getParameterNames().size();

		int windowWidth = 800, windowHeight = 500;
		if (desktopWidth > 1600)
			windowWidth = (int)(widthFraction * 1600);
		else
			windowWidth = (int)(widthFraction * desktopWidth);
		// choose initial height according to desktop, i.e. max. 85% of height
		windowHeight = paramNum * 50 < 700 ? 700 : paramNum * 50;
		if (windowHeight > 0.85 * desktopHeight)
			windowHeight = (int)(heightFraction * desktopHeight);

		// set up the main panel containing input panel and status bar
		JPanel mainPanel = new JPanel();
		mainPanel.setLayout(new BorderLayout());
		
		// set up the input panel
		this.inputPanel = new JPanel();
		BoxLayout ylayout = new BoxLayout(this.inputPanel, BoxLayout.Y_AXIS);
		this.inputPanel.setLayout(ylayout);
		
		// init and add tab pane
		this.tabPane = new JTabbedPane();
		this.inputPanel.add(this.tabPane);

		// setup operator parameter configuration panel
		this.operatorParameterPanel = this.setupParamConfigPanel();
		// wrap into scroll pane
		JScrollPane scrollPane = 
				new JScrollPane(this.operatorParameterPanel.getJPanel());
		// add a new tabulator to the window
		this.tabPane.add(scrollPane);
		this.tabPane.setTitleAt(0, "Operator Configuration");

		// add additional tabs
		HashMap<JComponent, String> additionalTabs = 
				this.setupAdditionalTabContents();
		if (additionalTabs != null) {
			Set<JComponent> comps = additionalTabs.keySet();
			for (JComponent jcomp : comps) {
				scrollPane = new JScrollPane(jcomp);
				this.tabPane.add(scrollPane, additionalTabs.get(jcomp));
			}
		}
		
		JPanel buttonPanel = new JPanel();
		// add button panel with additional, context-specific buttons
		buttonPanel.add(this.addContextSpecificButtonPanel());
		// add panel with close button
		buttonPanel.add(this.addCloseButtonPanel());

		// put everything into a split pane
		JSplitPane splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT,
				this.inputPanel, buttonPanel);
		splitPane.setOneTouchExpandable(true);
		splitPane.setDividerLocation((int)(windowHeight*3.0/4.0));
		mainPanel.add(splitPane, BorderLayout.CENTER);
		
		// add status bar
		this.status = new JLabel("Status: Ready");
		JPanel statusBar = new JPanel();
		statusBar.setBorder(BorderFactory.createLoweredBevelBorder());
		statusBar.add(this.status);
		mainPanel.add(statusBar, BorderLayout.SOUTH);

		// add pane to this window
		this.add(mainPanel);

		// add a nice menubar
		JMenuBar mainWindowMenu = new JMenuBar();
		JMenu fileM = new JMenu("File");
		JMenuItem itemSave = new JMenuItem("Save Settings");
		itemSave.setActionCommand("fileM_save");
		itemSave.addActionListener(this);
		JMenuItem itemLoad = new JMenuItem("Load Settings");
		itemLoad.setActionCommand("fileM_load");
		itemLoad.addActionListener(this);
		JMenuItem itemQuit = new JMenuItem("Quit");
		itemQuit.setActionCommand("fileM_quit");
		itemQuit.addActionListener(this);
		fileM.add(itemSave);
		fileM.add(itemLoad);
		fileM.add(new JSeparator());
		fileM.add(itemQuit);
		mainWindowMenu.add(fileM);

		JMenu actionM = new JMenu("Actions");
		JMenuItem resetOpItem = new JMenuItem("Reset Parameters to Default Values");
		resetOpItem.setActionCommand("actionM_reset");
		resetOpItem.addActionListener(this);
		actionM.add(resetOpItem);
		mainWindowMenu.add(actionM);

		JMenu optionsMenu = new JMenu("Options");
		this.showAllParameters = new JCheckBox("Show All Parameters", false);
		this.showAllParameters.setActionCommand("viewM_changed");
		this.showAllParameters.addActionListener(this);
		optionsMenu.add(this.showAllParameters);
		Collection<JComponent> addOptions = this.setupAdditionalMenuOptionItems();
		// add additional entries to the options menu
		if (addOptions != null && !addOptions.isEmpty()) {
			optionsMenu.addSeparator();
			for (JComponent m : addOptions)
				optionsMenu.add(m);
		}
		mainWindowMenu.add(optionsMenu);

		// add additional menu entries
		Collection<JMenu> addMenus = setupAdditionalMenus();
		for (JMenu m : addMenus)
			mainWindowMenu.add(m);
		
		// generate help menu
		JMenu helpM = this.generateHelpMenu();
		mainWindowMenu.add(Box.createHorizontalGlue());
		mainWindowMenu.add(helpM);

		// and go ..
		this.setTitle(this.titleString);
		this.setJMenuBar(mainWindowMenu);
		this.setSize(new Dimension(windowWidth, windowHeight));
	}

	/**
	 * Adds operator parameter configuration panel to input panel.
	 * <p>
	 * This function is to be overwritten by other frameworks.
	 */
	protected ALDOperatorParameterPanel setupParamConfigPanel() {
		ALDOperatorParameterPanel opPanel = new ALDOperatorParameterPanel(
				this.op, this.displayMode, true, this);
		return opPanel;
	}
	
	/**
	 * Method to add additional tabs to the pane.
	 * <p>
	 * This function is to be overwritten by subclasses and other frameworks.
	 * 
	 * @return Map containing component as key and tab title string as value.
	 */
	protected HashMap<JComponent, String> setupAdditionalTabContents() {
		return null;
	}
	
	/**
	 * Adds additional entries to the options menu.
	 * <p>
	 * This function is to be overwritten by subclasses and other frameworks.
	 *
	 * @return	List of items to be added to the options menu.
	 */
	protected Collection<JComponent> setupAdditionalMenuOptionItems() {
		// no additional menus to add
		return new LinkedList<JComponent>();
	}

	/**
	 * Adds additional menu items.
	 * <p>
	 * This function is to be overwritten by subclasses and other frameworks.
	 *
	 * @return	List of menus to be added to the menubar.
	 */
	protected Collection<JMenu> setupAdditionalMenus() {
		// no additional menus to add
		return new LinkedList<JMenu>();
	}
	
	/**
	 * Adds set of control buttons to the input panel.
	 * <p>
	 * This function is to be overwritten by subclasses and other frameworks.
	 */
	protected JPanel addContextSpecificButtonPanel() {
		return new JPanel();
	}

	/**
	 * Adds set of control buttons to the input panel.
	 */
	protected JPanel addCloseButtonPanel() {

		// init panel
		JPanel runPanel = new JPanel();
		runPanel.setLayout(new GridLayout(2, 1));

		// close button
		JButton quitButton = new JButton("Close");
		quitButton.setActionCommand("close");
		quitButton.addActionListener(this);
		quitButton.setBounds(50, 60, 80, 30);

		// now set up a panel to hold the button
		JPanel controlPanel = new JPanel();
		controlPanel
				.setLayout(new BoxLayout(controlPanel, BoxLayout.LINE_AXIS));
		controlPanel.setBorder(BorderFactory.createEmptyBorder(0, 10, 10, 10));
		controlPanel.add(Box.createRigidArea(new Dimension(10, 0)));
		controlPanel.add(Box.createHorizontalGlue());
		controlPanel.add(Box.createRigidArea(new Dimension(10, 0)));
		controlPanel.add(Box.createRigidArea(new Dimension(10, 0)));
		controlPanel.add(quitButton);
		runPanel.add(controlPanel);
		return runPanel;
	}

	/**
	 * Sets up the help menu.
	 * <p>
	 * This function is to be overwritten by subclasses and other frameworks.
	 * 
	 * @return Generated help menu.
	 */
	protected JMenu generateHelpMenu() {
		JMenu helpM = new JMenu("Help");
		JMenuItem itemHelp = new JMenuItem("Online Help");
		itemHelp.addActionListener(OnlineHelpDisplayer.getHelpActionListener(
				itemHelp, this.op.getClass().getName(), this));
		JMenuItem itemAbout = new JMenuItem("About Alida");
		itemAbout.setActionCommand("helpM_about");
		itemAbout.addActionListener(this);
		helpM.add(itemHelp);
		helpM.add(itemAbout);
		return helpM;
	}

	/**
	 * Changes the status message to the given string.
	 * @param msg	Message to be shown in statusbar.
	 */
	protected void setStatus(String msg) {
		this.status.setText("Status: " + msg);
	}

	/**
	 * Clean-up on termination.
	 * @return	True if window was closed.
	 */
	public boolean quit() {
		// dispose all resources, i.e. sub-windows
		this.operatorParameterPanel.dispose();
		this.dispose();
		return true;
	}
	
	/**
	 * Reads current parameter values from GUI and updates the operator.
	 */
	public void synchronizeOperatorWithGUI() {
		this.operatorParameterPanel.handleValueChangeEvent(
			new ALDSwingValueChangeEvent(this, null));
	}
	
	/**
	 * Get a reference to the operator object.
	 * @return	Reference to underlying operator object.
	 */
	public ALDOperator getOperator() {
		return this.op;
	}
	
	/**
	 * Specify the provider interaction level.
	 * @param pl		Level to be used in program execution.
	 */
	public void setProviderInteractionLevel(ProviderInteractionLevel pl) {
		this.pLevel = pl;
	}
	
	/**
	 * Configure parameter as linked (particularly in Grappa).
	 * @param paramName					Name of the parameter.
	 * @param sourceOp					Source operator node of the link.
	 * @param sourceParamName		Name of the parameter at the source node.
	 * @return	True, if re-configuration was successful.
	 */
	public boolean setParameterLinked(String paramName, 
			String sourceOp, String sourceParamName) {
		return this.operatorParameterPanel.setParameterLinked(paramName, 
			sourceOp, sourceParamName);
	}
	
	/**
	 * Configure parameter as not linked (particularly in Grappa).
	 * @param paramName					Name of the parameter.
	 * @return	True, if re-configuration was successful.
	 */
	public boolean setParameterNotLinked(String paramName) {
		return this.operatorParameterPanel.setParameterNotLinked(paramName);
	}

	/**
	 * Updates the window according to the status of the operator parameters.
	 * <p>
	 * In the window the parameter label colors are changed according to the 
	 * configuration status of the operator parameters.
	 * 
	 * @param badParams		List of missing required parameters.
	 */
	public void updateParamConfigurationStatus(Collection<String> badParams) {
		this.operatorParameterPanel.updateConfigurationStatus(badParams);
	}
	
	/**
	 * Updates the configuration window with settings of a new operator.
	 * <p>
	 * Note that we assume here that the class of the new operator is equal to 
	 * the class of the old operator.
	 * 
	 * @param newOp		New operator instance.
	 */
	public void updateOperator(ALDOperator newOp) {
		// update operator and the GUI with new parameter values
		this.op = newOp;
		try {
			HashMap<ALDOpParameterDescriptor, String> failedParams = 
					this.operatorParameterPanel.setNewOperator(this.op);
			if (failedParams != null) {
				StringBuffer msg = new StringBuffer();
				Set<ALDOpParameterDescriptor> keys = failedParams
						.keySet();
				for (ALDOpParameterDescriptor descr : keys) {
					msg.append("Updating parameter " + descr.getLabel()
							+ " failed!\n");
					msg.append("--> " + failedParams.get(descr) + "\n");
				}
				JOptionPane.showOptionDialog(null,
						"Problems loading file, not all parameters could be updated!\n"
								+ msg, "Warning",
								JOptionPane.DEFAULT_OPTION,
								JOptionPane.WARNING_MESSAGE, null,
								this.okOption, this.okOption[0]);
			}
		} catch (ALDDataIOProviderException ex) {
			JOptionPane.showOptionDialog(null,
					"Problems loading file, operator does not match!\n"
							+ ex.getCommentString(), "Error",
							JOptionPane.DEFAULT_OPTION,
							JOptionPane.ERROR_MESSAGE, null, this.okOption,
							this.okOption[0]);
		}
	}
	
	/* (non-Javadoc)
	 * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
	 */
	@Override
	public void actionPerformed(ActionEvent e) {

		// local variables
		String command = e.getActionCommand();

		// close the frame
		if (   command.equals("frame_close")
				|| command.equals("close") 
				|| command.equals("fileM_quit")) {
			this.quit();
		}

		// handle menu item commands

		else if (command.equals("fileM_save")) {
			// open file chooser
			JFileChooser getFileDialog = new JFileChooser();
			getFileDialog.setApproveButtonText("Save");
			getFileDialog.setCurrentDirectory(new File(this.lastDirectory));
			getFileDialog.setSelectedFile(this.lastFile);
			getFileDialog.setFileSelectionMode(JFileChooser.FILES_ONLY);
			int returnVal = getFileDialog.showOpenDialog(null);
			if (returnVal == JFileChooser.APPROVE_OPTION) {
				// check if file exists already, if so ask what to do
				File file = getFileDialog.getSelectedFile();
				if (file.exists()) {
					if ( JOptionPane.showOptionDialog(null, 
							"File " + file.getAbsolutePath() + " exists, override?",
							"file exists",
							JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE,
							null, null, null) != 0 ) {
						return;
					}
				}
//				HashMap<ALDOpParameterDescriptor, String> failedParams = null;
//				HashMap<ALDOpParameterDescriptor, String> failedParams = this.operatorParameterPanel
//						.setOperatorParameters();
//				if (failedParams != null) {
//					StringBuffer msg = new StringBuffer();
//					Set<ALDOpParameterDescriptor> keys = failedParams
//							.keySet();
//					for (ALDOpParameterDescriptor descr : keys) {
//						msg.append("Loading parameter " + descr.getLabel()
//								+ "failed!\n");
//						msg.append("--> " + failedParams.get(descr) + "\n");
//					}
//					JOptionPane.showOptionDialog(null,
//							"Loading parameters failed, not all parameters could be loaded!\n"
//									+ msg,
//							"Warning - Problems loading parameters!",
//							JOptionPane.DEFAULT_OPTION,
//							JOptionPane.WARNING_MESSAGE, null,
//							this.okOption, this.okOption[0]);
//				}
				this.lastFile = file;
				try {
					ALDDataIOManagerXmlbeans.writeXml(file.getAbsolutePath(), this.op);
				} catch (Exception e1) {
					System.out.println(e1.getMessage());
					e1.printStackTrace();
//					System.out.println("save with xstream");
//					this.op.serializeToXmlFile(file.getAbsolutePath());
				}
				this.lastDirectory = file.getAbsolutePath();
			}
		} else if (command.equals("fileM_load")) {
			// open file chooser
			JFileChooser getFileDialog = new JFileChooser();
			getFileDialog.setCurrentDirectory(new File(this.lastDirectory));
			getFileDialog.setSelectedFile(this.lastFile);
			getFileDialog.setFileSelectionMode(JFileChooser.FILES_ONLY);
			int returnVal = getFileDialog.showOpenDialog(null);
			if (returnVal == JFileChooser.APPROVE_OPTION) {
				File file = getFileDialog.getSelectedFile();
				this.lastDirectory = file.getAbsolutePath();
				this.lastFile = file;
				try {
					this.op = (ALDOperator)ALDDataIOManagerXmlbeans.readXml(
						file.getAbsolutePath(), ALDOperator.class);
					// notify proxies and other listeners of parameter update
					this.fireALDOpParameterUpdateEvent(new ALDOpParameterUpdateEvent(
						this,	ALDOpParameterUpdateEvent.EventType.LOADED));
				} catch (Exception e1) {
					System.out.println(e1.getMessage());
				}
			}
//		} else if (command.equals("viewM_standard")) {
//			this.operatorParameterPanel
//					.changeViewMode(Parameter.ExpertMode.STANDARD);
//			this.repaint();
//		} else if (command.equals("viewM_advanced")) {
//			this.operatorParameterPanel
//					.changeViewMode(Parameter.ExpertMode.ADVANCED);
//			this.repaint();
		} else if (command.equals("viewM_changed")) {
			if (this.showAllParameters.isSelected())
				this.operatorParameterPanel
						.changeViewMode(Parameter.ExpertMode.ADVANCED);
			else
				this.operatorParameterPanel
						.changeViewMode(Parameter.ExpertMode.STANDARD);
			this.repaint();
		}
		else if (command.equals("actionM_reset")) {
			try {
				this.op = this.op.getClass().newInstance();
				// notify proxies and other listeners of parameter update
				this.fireALDOpParameterUpdateEvent(new ALDOpParameterUpdateEvent(
					this,	ALDOpParameterUpdateEvent.EventType.LOADED));
      } catch (InstantiationException e1) {
      	JOptionPane.showOptionDialog(null,
      			"Reset of parameters failed, could not instantiate operator!\n" 
      					+ e1.getMessage(), "Warning - Reset of parameters failed!",
      			JOptionPane.DEFAULT_OPTION,
      			JOptionPane.WARNING_MESSAGE, null,
      			this.okOption, this.okOption[0]);
      } catch (IllegalAccessException e2) {
      	JOptionPane.showOptionDialog(null,
      			"Reset of parameters failed!\n" + e2.getMessage(),
      			"Warning - Reset of parameters failed!",
      			JOptionPane.DEFAULT_OPTION,
      			JOptionPane.WARNING_MESSAGE, null,
      			this.okOption, this.okOption[0]);
      }
		}
		else if (command.equals("helpM_about")) {
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
	}

	@Override
	public void handleValueChangeEvent(ALDSwingValueChangeEvent event) {
		this.fireALDOpParameterUpdateEvent(new ALDOpParameterUpdateEvent(this,
			ALDOpParameterUpdateEvent.EventType.CHANGED));
	}

	/* ******************************************************************
	 * Implementation of the ALDOpParameterUpdateEventReporter interface
	 * ******************************************************************/
	
	/**
	 * Adds a listener to this reporter.
	 * @param listener		Listener to be added.
	 */
	@Override
  public void addALDOpParameterUpdateEventListener(
		ALDOpParameterUpdateEventListener listener) {
		this.listenerList.add(
			ALDOpParameterUpdateEventListener.class, listener);
	}

	/**
	 * Removes a listener from this reporter.
	 * @param listener		Listener to be removed.
	 */
	@Override
  public void removeALDOpParameterUpdateEventListener(
		ALDOpParameterUpdateEventListener listener) {
		this.listenerList.remove(
			ALDOpParameterUpdateEventListener.class, listener);
	}

	/**
	 * Sends an event to all registered listeners.
	 * @param ev		Event to be send to all listeners.
	 */
	@Override
  public void fireALDOpParameterUpdateEvent(ALDOpParameterUpdateEvent ev){
		// get list of listeners 
		Object[] listeners = this.listenerList.getListenerList();
		
		/* listeners will always be non-null as getListenerList() is guaranteed 
		 * to return a non-null array... */

		// process the listeners last to first, notifying
		// those that are interested in this event
		for (int i = listeners.length-2; i>=0; i-=2) {
			if (listeners[i]==ALDOpParameterUpdateEventListener.class) {
				// lazily create the event:
				((ALDOpParameterUpdateEventListener)listeners[i+1]).
						handleALDParameterUpdateEvent(
							new ALDOpParameterUpdateEvent(this, ev.getType()));
			}
		}
	}
}
