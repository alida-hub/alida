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

package de.unihalle.informatik.Alida.dataio.provider.swing.components;

import de.unihalle.informatik.Alida.operator.ALDOperator;
import de.unihalle.informatik.Alida.operator.ALDParameterDescriptor;
import de.unihalle.informatik.Alida.annotations.ALDClassParameter;
import de.unihalle.informatik.Alida.annotations.Parameter;
import de.unihalle.informatik.Alida.dataio.ALDDataIOManagerSwing;
import de.unihalle.informatik.Alida.dataio.provider.helpers.ALDParametrizedClassDataIOHelper;
import de.unihalle.informatik.Alida.dataio.provider.swing.events.ALDSwingValueChangeEvent;
import de.unihalle.informatik.Alida.exceptions.ALDDataIOException;
import de.unihalle.informatik.Alida.exceptions.ALDDataIOProviderException;
import de.unihalle.informatik.Alida.exceptions.ALDDataIOProviderException.*;
import de.unihalle.informatik.Alida.gui.ALDOperatorDocumentationFrame;

import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Set;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButtonMenuItem;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;

/**
 * Configuration window for parametrized class objects.
 * 
 * @author moeller
 */
public class ALDParametrizedClassConfigWindow 
	extends ALDParameterPanelParent 
		implements ActionListener {

	/**
	 * Mode for displaying parameters.
	 */
	private Parameter.ExpertMode displayMode = 
			Parameter.ExpertMode.STANDARD;

	/**
	 * Main configuration window.
	 */
	private JFrame window;

	/**
	 * Fixed width of window.
	 */
	private final int frameWidth = 500;

	/**
	 * Main panel of main frame.
	 */
	private JPanel mainPanel = null;

	/**
	 * Parameter panel.
	 */
	private ALDParameterPanel parameterPanel;

	/**
	 * Listed of associated parametrized class parameters.
	 */
	private LinkedList<ALDParameterDescriptor> paramDescrips;

	/**
	 * Parametrized class to configure.
	 */
	private Class<?> myclass;

	/**
	 * Parameter descriptor associated with parametrized class object.
	 */
	private ALDParameterDescriptor myDescriptor;
	
	/**
	 * Object to configure associated with window.
	 */
	private Object myObj = null;
	
	/**
	 * Name of parametrized class to configure.
	 */
	private String className;

	/**
	 * Flag to indicate if editing is allowed or only display is requested.
	 */
	private boolean displayOnly = false;
	
	/**
	 * Default constructor.
	 * @param c		Class associated with this window.
	 * @param d		Descriptor associated with parameter configured here.
	 */
	public ALDParametrizedClassConfigWindow(Class<?> c, 
			ALDParameterDescriptor d) {
		this.myObj = null;
		this.myclass = c;
		this.myDescriptor = d;
		this.className = this.myclass.getSimpleName();
		this.displayOnly = false;
		this.buildWindow();
	}

	/**
	 * Default constructor.
	 * @param obj		Object associated with this window.
	 * @param d		Descriptor associated with parameter configured here.
	 */
	public ALDParametrizedClassConfigWindow(Object obj, 
			ALDParameterDescriptor d) {
		this.myObj = obj;
		this.myclass = obj.getClass();
		this.myDescriptor = d;
		this.className = this.myclass.getSimpleName();
		this.buildWindow();
	}

	/**
	 * Default constructor.
	 * @param obj			Object to configure.
	 * @param d				Descriptor associated with parameter configured here.
	 * @param noEdit	If true, no parameter editing is possible.
	 */
	public ALDParametrizedClassConfigWindow(Object obj,	
			ALDParameterDescriptor d, boolean noEdit) {
		this.myObj = obj;
		this.myclass = obj.getClass();
		this.myDescriptor = d;
		this.className = this.myclass.getSimpleName();
		this.displayOnly = noEdit;
		this.buildWindow();
	}

	@Override
  public Object getParameterValue(boolean isRequired, 
  		boolean isSupplemental,	ALDParameterDescriptor descr) {
		try {
			Field field = descr.getField();
			field.setAccessible(true);
			if (!this.displayOnly) {
				return field.get(this.myclass.newInstance());
			}
			return field.get(this.myObj);
		} catch (Exception e) {
			System.err.println("ALDParametrizedClassDataIOSwing: " +
			"something went wrong reading out field... skipping!");
		}
		return null;
	}
	
	/**
	 * Disable component to prohibit value changes.
	 */
	public void disableComponent() {
		if (this.parameterPanel != null)
			this.parameterPanel.disableComponents();
	}
	
	/**
	 * Enable component to allow for value changes.
	 */
	public void enableComponent() {
		if (this.parameterPanel != null)
			this.parameterPanel.enableComponents();
	}
	
	/**
	 * Dispose component and all sub-windows.
	 */
	public void dispose() {
		if (this.parameterPanel != null)
			this.parameterPanel.dispose();
		if (this.window != null)
			this.window.dispose();
	}

	/**
	 * Show or hide the window.
	 * @param b		If true, window is displayed, otherwise hidden.
	 * @throws ALDDataIOProviderException Thrown in case of failure.
	 */
	public void setVisible(boolean b) throws ALDDataIOProviderException {
		if (this.myObj == null) {
			try {
				this.myObj = this.myclass.newInstance();
				// get initial default values for object
				this.myObj = 
					ALDDataIOManagerSwing.getInstance().getInitialGUIValue(
						null, this.myObj.getClass(), this.myObj, this.myDescriptor);
				// set configuration in GUI
				this.setValue(this.myObj);
			} catch (Exception e) {
				throw new ALDDataIOProviderException(
						ALDDataIOProviderExceptionType.OBJECT_INSTANTIATION_ERROR, 
						"ALDParametrizedClassDataIOSwing: \n" + 
						"problems instantiating desired class object...");
			}
		}
		this.window.setVisible(b);
	}

	/**
	 * Method to query if configuration window is visible.
	 * @return	True if window is visible.
	 */
	public boolean isVisible() {
		return this.window.isVisible();
	}
	
	/**
	 * Enable or display parameter editing.
	 * @param b		If true, editing is disabled, otherwise enabled.
	 */
	public void setDisplayOnly(boolean b) {
		this.displayOnly = b;
	}

	/**
	 * Disable or enable the elements in the window for editing.
	 * 
	 * @param f	If true, editing is possible, otherwise not.
	 */
	public void setEditable(boolean f) {
		this.parameterPanel.setEnabled(f);
	}
	
	/**
	 * Update parameter values in GUI.
	 * 
	 * @param	obj	New value of parameter object.
	 * @throws ALDDataIOException Thrown in case of failure.
	 */
	public void setValue(Object obj) throws ALDDataIOException { 
		if (obj == null)
			return;
		for (ALDParameterDescriptor descr: this.paramDescrips) {
			Object value = 
				ALDParametrizedClassDataIOHelper.getValue(descr.getField(),obj);
			this.parameterPanel.setParameter(descr, value);
		}
		this.myObj = obj;
	}

	/**
	 * Extracts current parametrized class configuration.
	 * 
	 * @param field		Field of parametrized class object.
	 * @param cl			Desired class.
	 * @return	Value of parametrized class object.
	 * @throws ALDDataIOException Thrown in case of failure.
	 */
	public Object readData(@SuppressWarnings("unused") Field field,
			@SuppressWarnings("unused") Class<?> cl) throws ALDDataIOException { 
		if (this.myObj == null) {
			return null;
		}
		// if object available, copy parameters
		for (ALDParameterDescriptor descr: this.paramDescrips) {
			Object value = this.parameterPanel.readParameter(descr);
			try {
				ALDParametrizedClassDataIOHelper.setValue(descr.getName(), 
						this.myObj, value);
			} catch (IllegalAccessException e) {
				throw new ALDDataIOProviderException(
						ALDDataIOProviderExceptionType.OBJECT_INSTANTIATION_ERROR, 
						"ALDParametrizedClassDataIOSwing: \n" + 
				"problems initializing desired class object...");
			}
		}
		return this.myObj;
	}

	/**
	 * Generates the window.
	 */
	private void buildWindow() {
		
		this.window = new JFrame();
		this.window.setTitle("Class \"" + this.className + "\"");
		this.window.setSize(this.frameWidth,300);

		this.mainPanel = new JPanel();
		JScrollPane scroller = new JScrollPane(this.mainPanel);  
		this.window.getContentPane().add(scroller, BorderLayout.CENTER);

		BoxLayout ylayout = new BoxLayout(this.mainPanel, BoxLayout.Y_AXIS);
		this.mainPanel.setLayout( ylayout);

		// add menubar
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

		JMenu actionsM = new JMenu("Actions");
		JMenuItem itemValidate = new JMenuItem("Validate");
		itemValidate.setActionCommand("actionsM_validate");
		itemValidate.addActionListener(this);
		actionsM.add(itemValidate);

		JMenu viewM = new JMenu("View");
		ButtonGroup operatorLevelGroup = new ButtonGroup();
		// default
		JRadioButtonMenuItem radioItemStandard = 
			new JRadioButtonMenuItem("Standard");
		if (ALDParametrizedClassConfigWindow.this.displayMode.equals(
				Parameter.ExpertMode.STANDARD))
			radioItemStandard.setSelected(true);
		radioItemStandard.setActionCommand("viewM_standard");
		radioItemStandard.addActionListener(this);
		JRadioButtonMenuItem radioItemAdvanced =
			new JRadioButtonMenuItem("Advanced");
		if (ALDParametrizedClassConfigWindow.this.displayMode.equals(
				Parameter.ExpertMode.ADVANCED))
			radioItemAdvanced.setSelected(true);
		radioItemAdvanced.setActionCommand("viewM_advanced");
		radioItemAdvanced.addActionListener(this);
		operatorLevelGroup.add(radioItemStandard);
		operatorLevelGroup.add(radioItemAdvanced);
		viewM.add(radioItemStandard);
		viewM.add(radioItemAdvanced);

		// generate help menu
		JMenu helpM = new JMenu("Help");
		// only add documentation entry if operator documentation available
//		if (    this.myObj != null
//				&&  ((ALDOperator)this.myObj).getDocumentation() != null
//				&& !((ALDOperator)this.myObj).getDocumentation().isEmpty()) {
//			JMenuItem itemHelp = new JMenuItem("Operator Documentation");
//			itemHelp.setActionCommand("helpM_docu");
//			itemHelp.addActionListener(this);
//			helpM.add(itemHelp);
//		}
		JMenuItem itemAbout = new JMenuItem("About Alida");
		itemAbout.setActionCommand("helpM_about");
		itemAbout.addActionListener(this);
		helpM.add(itemAbout);

		mainWindowMenu.add(fileM);
		// add the actions menu only in configuration context
		if (!this.displayOnly)
			mainWindowMenu.add(actionsM);
		mainWindowMenu.add(viewM);
		mainWindowMenu.add(Box.createHorizontalGlue());
		mainWindowMenu.add(helpM);
		this.window.setJMenuBar(mainWindowMenu);
		this.window.pack();

		// extract annotated parameters of parametrized class 
		// to be configured
		this.paramDescrips = new LinkedList<ALDParameterDescriptor>();
		HashMap<String,Field> params = 
			ALDParametrizedClassDataIOHelper.getAnnotatedFields(this.myclass);
		Set<String> keys = params.keySet();
		for (String key : keys) {
			Field field = params.get(key);
			int guiOrder = 0;
			String description = null;
			String label = field.getName();
			Parameter.ExpertMode mode = Parameter.ExpertMode.STANDARD;
			ALDClassParameter pAnnotation = 
				field.getAnnotation(ALDClassParameter.class);
			if (pAnnotation != null) {
				guiOrder = pAnnotation.dataIOOrder();
				mode = pAnnotation.mode();
				label = pAnnotation.label();
				description = pAnnotation.description();
			}
			this.paramDescrips.add(new ALDParameterDescriptor(field.getName(),
					field.getType(), description, label, true, 
					field, guiOrder, mode, "", 
					Parameter.ParameterModificationMode.MODIFIES_NOTHING, false));
		}
		this.parameterPanel = new ALDParameterPanel(this, this.paramDescrips, 
			"Parameters", false, this.displayOnly);
		this.parameterPanel.addValueChangeEventListener(this);
		this.mainPanel.add(this.parameterPanel.getJPanel());

		// add buttons
		GridLayout gl = new GridLayout(1,3);
		JPanel buttonPanel = new JPanel();
		buttonPanel.setLayout(gl);
		if (!this.displayOnly) {
			JButton validateButton = new JButton("Validate");
			validateButton.setActionCommand( "validate");
			validateButton.addActionListener( this);
			validateButton.setBounds(50, 60, 80, 30);
			buttonPanel.add(validateButton);
		}
		JButton closeButton = new JButton("Close");
		closeButton.setActionCommand("close");
		closeButton.addActionListener(this);
		closeButton.setBounds(50, 60, 80, 30);
		buttonPanel.add(closeButton);
		this.window.add(buttonPanel,BorderLayout.SOUTH);

		// wrap up with a scrolling panel
//		JScrollPane scrollPane = new JScrollPane(this.mainPanel);
//		this.window.add(scrollPane);

		// resize window 
		this.window.setSize(this.frameWidth, 400);
	}

	/* (non-Javadoc)
	 * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
	 */
	@Override
	public void actionPerformed(ActionEvent e) {
		String cmd = e.getActionCommand(); 
		if (cmd.equals("close") || cmd.equals("fileM_quit")) {
			this.window.setVisible(false);
		}
		else if (cmd.equals("validate")|| cmd.equals("actionsM_validate")) {
			this.parameterPanel.validateParameters();
		}
		else if (cmd.equals("fileM_save")) {
			Object[] options = { "OK" };
			JOptionPane.showOptionDialog(null, 
					"Function will be provided soon...\n", "Warning",
					JOptionPane.DEFAULT_OPTION, JOptionPane.WARNING_MESSAGE,
					null, options, options[0]);
		}
		else if (cmd.equals("fileM_load")) {
			Object[] options = { "OK" };
			JOptionPane.showOptionDialog(null, 
					"Function will be provided soon...\n", "Warning",
					JOptionPane.DEFAULT_OPTION, JOptionPane.WARNING_MESSAGE,
					null, options, options[0]);
		}
		else if (cmd.equals("viewM_standard")) {
			this.parameterPanel.changeViewMode(Parameter.ExpertMode.STANDARD);
			this.window.repaint();
		}
		else if (cmd.equals("viewM_advanced")) {
			this.parameterPanel.changeViewMode(Parameter.ExpertMode.ADVANCED);
			this.window.repaint();
		}
//		else if (cmd.equals("helpM_docu")) {
//			ALDOperatorDocumentationFrame doc = 
//				new ALDOperatorDocumentationFrame(this.className,
//					this.myclass.getName(), ((ALDOperator)myObj).getDocumentation());
//			doc.setVisible(true);
//		}
		else if (cmd.equals("helpM_about")) {
			Object[] options = { "OK" };
			JOptionPane.showOptionDialog(null, 
					"Alida / MiToBo Graphical Operator Runner - \n" +
					"Chosen Operator: " + this.className + "\n" +
					" @2012 Martin Luther University Halle-Wittenberg", 
					"About Alida / MiToBo",
					JOptionPane.DEFAULT_OPTION, JOptionPane.PLAIN_MESSAGE,
					null, options, options[0]);
		}
	}		

	/* (non-Javadoc)
	 * @see de.unihalle.informatik.Alida.dataio.provider.swing.events.ALDSwingValueChangeListener#handleValueChangeEvent(de.unihalle.informatik.Alida.dataio.provider.swing.events.ALDSwingValueChangeEvent)
	 */
	@Override
	public void handleValueChangeEvent(ALDSwingValueChangeEvent event) {
		// here we need to create a new event since the name of the 
		// parameter given in the event is not known outside of this
		// class, we need to use name of parent parameter associated with
		// the complete window
		this.fireALDSwingValueChangeEvent(
				new ALDSwingValueChangeEvent(this, this.myDescriptor));
	}
}
