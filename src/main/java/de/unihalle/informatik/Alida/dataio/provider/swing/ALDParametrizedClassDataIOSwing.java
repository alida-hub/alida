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

package de.unihalle.informatik.Alida.dataio.provider.swing;

import de.unihalle.informatik.Alida.helpers.ALDClassInfo;
import de.unihalle.informatik.Alida.operator.ALDParameterDescriptor;
import de.unihalle.informatik.Alida.annotations.ALDDataIOProvider;
import de.unihalle.informatik.Alida.dataio.ALDDataIOManagerSwing;
import de.unihalle.informatik.Alida.dataio.provider.ALDDataIOSwing;
import de.unihalle.informatik.Alida.dataio.provider.helpers.ALDParametrizedClassDataIOHelper;
import de.unihalle.informatik.Alida.dataio.provider.helpers.ALDParametrizedClassDummy;
import de.unihalle.informatik.Alida.dataio.provider.swing.components.ALDParametrizedClassConfigWindow;
import de.unihalle.informatik.Alida.dataio.provider.swing.components.ALDSwingComponent;
import de.unihalle.informatik.Alida.dataio.provider.swing.components.ALDSwingComponentComboBox;
import de.unihalle.informatik.Alida.dataio.provider.swing.components.ALDSwingComponentComboBoxItem;
import de.unihalle.informatik.Alida.dataio.provider.swing.events.ALDSwingValueChangeEvent;
import de.unihalle.informatik.Alida.dataio.provider.swing.events.ALDSwingValueChangeListener;
import de.unihalle.informatik.Alida.exceptions.ALDDataIOException;
import de.unihalle.informatik.Alida.exceptions.ALDDataIOProviderException;
import de.unihalle.informatik.Alida.exceptions.ALDDataIOProviderException.*;

import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Set;
import java.util.Vector;

import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JPanel;

/**
 * Class for generic handling of parameterized classes in GUI.
 * <p>
 * This class implements the <code>ALDDataIOSwing</code> interface for 
 * parametrized classes. It provides a button linked to a configuration 
 * window that subsumes all parameters of the class to configure.
 * 
 * @author moeller
 */
@ALDDataIOProvider
public class ALDParametrizedClassDataIOSwing implements ALDDataIOSwing {

	/**
	 * GUI configuration element.
	 */
	private ParametrizedClassPanel paramPanel;
	
	/**
	 * Default constructor.
	 */
	public ALDParametrizedClassDataIOSwing() {
		// nothing to do here
	}
	
	/**
	 * Interface method to announce class for which IO is provided for.
	 * 
	 * @return  Collection of classes provided.
	 */
	@Override
	public Collection<Class<?>> providedClasses() {
		LinkedList<Class<?>> classes = new LinkedList<Class<?>>();

		classes.add( ALDParametrizedClassDummy.class);

		return classes;
	}

	@Override
  public Object getInitialGUIValue(Field field, Class<?> cl, Object obj, 
  		ALDParameterDescriptor descr) throws ALDDataIOProviderException {
		if (obj == null)
			return null;
		
		// iterate over all parameters and request default values
		HashMap<String,Field> params = 
				ALDParametrizedClassDataIOHelper.getAnnotatedFields(
						obj.getClass());
		Set<String> paramNames = params.keySet();
		for (String pname : paramNames) {
			try {
				Field f = params.get(pname);
				Object currentValue = 
						ALDParametrizedClassDataIOHelper.getValue(f, obj);
				Class<?> currentValueClass = f.getType();
 				if (currentValue != null)
 					currentValueClass = currentValue.getClass();
				Object initialValue = 
						ALDDataIOManagerSwing.getInstance().getInitialGUIValue(
								f, currentValueClass, currentValue, null);
				ALDParametrizedClassDataIOHelper.setValue(f, obj, initialValue);
			} catch (Exception e) {
				e.printStackTrace();
				throw new ALDDataIOProviderException(
						ALDDataIOProviderExceptionType.UNSPECIFIED_ERROR,
						"[" + this.getClass().getName() + "]" 
								+ ": could not get initial GUI value for "
								+ "parameter <" + pname + "> of class <" + cl
								+ ">!");
			}
		}
		return obj;
	}

	/* (non-Javadoc)
	 * @see de.unihalle.informatik.Alida.dataio.provider.ALDDataIOSwing#createGUIElement(java.lang.reflect.Field, java.lang.Class, java.lang.Object)
	 */
	@Override
  public ParametrizedClassPanel createGUIElement(
  		Field field, Class<?> cl, Object obj, ALDParameterDescriptor descr) 
		throws ALDDataIOProviderException {
		try {
	    this.paramPanel = 
	    		new ParametrizedClassPanel(field, cl, obj, descr);
    } catch (ALDDataIOException e) {
    	throw new ALDDataIOProviderException(
    			ALDDataIOProviderExceptionType.UNSPECIFIED_ERROR,
    			"Provider Error: " + e.getCommentString());
    }
		return this.paramPanel;
  }

	/* (non-Javadoc)
	 * @see de.unihalle.informatik.Alida.dataio.provider.ALDDataIOSwing#setValue(java.lang.reflect.Field, java.lang.Class, de.unihalle.informatik.Alida.dataio.provider.swing.components.ALDSwingComponent, java.lang.Object)
	 */
	@Override
  public void setValue(Field field, Class<?> cl, 
  		ALDSwingComponent guiElement, Object value) 
  	throws ALDDataIOProviderException {
		if (!(guiElement instanceof ParametrizedClassPanel))
			throw new ALDDataIOProviderException(
					ALDDataIOProviderExceptionType.INVALID_GUI_ELEMENT, 
					"ParametrizedClassDataIO: setValue() received invalid GUI element!");
		ParametrizedClassPanel paramGUIPanel = 
				(ParametrizedClassPanel)guiElement;
		try {
			paramGUIPanel.setValue(field, cl, value);
		} catch(ALDDataIOException exp) {
			throw new ALDDataIOProviderException(
					ALDDataIOProviderExceptionType.UNSPECIFIED_ERROR,
					"setValue() - setting parameter in GUI failed!");
		}
  }
	
	/* (non-Javadoc)
	 * @see de.unihalle.informatik.Alida.dataio.provider.ALDDataIOSwing#readData(java.lang.reflect.Field, java.lang.Class, de.unihalle.informatik.Alida.dataio.provider.swing.components.ALDSwingComponent)
	 */
	@Override
  public Object readData(
  		Field field, Class<?> cl, ALDSwingComponent guiElement) 
		throws ALDDataIOProviderException {
		if (!(guiElement instanceof ParametrizedClassPanel))
			throw new ALDDataIOProviderException(
					ALDDataIOProviderExceptionType.INVALID_GUI_ELEMENT, 
					"ParametrizedClassDataIO: " 
							+ "readData received invalid GUI element!");
		ParametrizedClassPanel paramGUIPanel = 
				(ParametrizedClassPanel)guiElement;
		try {
	    Object obj = paramGUIPanel.readData(field, cl);
	    return obj;
    } catch (ALDDataIOException e) {
    	// if exception came from provider, just throw it to parent
    	if (e instanceof ALDDataIOProviderException)
    		throw (ALDDataIOProviderException)e;
    	// otherwise embed it into our own exception
    	// (... necessary due to method signature)
			throw new ALDDataIOProviderException(
      		ALDDataIOProviderExceptionType.UNSPECIFIED_ERROR, 
      		"Data IO Manager returned an error: \n" +
      		e.getCommentString());
    }
  }

	/* (non-Javadoc)
	 * @see de.unihalle.informatik.Alida.dataio.provider.ALDDataIOSwing#writeData(java.lang.Object)
	 */
	@Override
  public JComponent writeData(Object obj, ALDParameterDescriptor descr) {
		return new ParametrizedClassShowButton(obj, descr);
  }

	/**
	 * GUI element class for parametrized class parameters.
	 * 
	 * @author moeller
	 */
	private class ParametrizedClassPanel 
		extends ALDSwingComponent
			implements ActionListener, ItemListener, 
				ALDSwingValueChangeListener {

		/**
		 * Default item if no value is selected.
		 */
		private ALDSwingComponentComboBoxItem boxItemNone = 
				new ALDSwingComponentComboBoxItem(null, "none", "none");
		
		/**
		 * Main panel containing elements for class selection.
		 */
		private JPanel mainPanel;
		
		/**
		 * Combobox containing available classes to select.
		 */
		private ALDSwingComponentComboBox classSelection;
		
		/**
		 * List of available classes.
		 */
		@SuppressWarnings("rawtypes")
	  private Collection<Class> availableClasses;

		/**
		 * Hashmap to remember short class names.
		 */
		@SuppressWarnings("rawtypes")
	  private HashMap<String, Class> shortNames = 
	  	new HashMap<String, Class>();
		
		/**
		 * Hashmap to store configuration windows for classes.
		 */
	  @SuppressWarnings("rawtypes")
    private HashMap<Class, ALDParametrizedClassConfigWindow> configWins = 
	  	new HashMap<Class, ALDParametrizedClassConfigWindow>();

		/**
		 * Default constructor.
     * @param field		Field linked to the parametrized class parameter.
     * @param cl			Class of the parametrized class parameter.
     * @param obj			Initial object to use for initialization.
     * @param descr		Descriptor linked to the parameter.
		 * @throws ALDDataIOException Thrown in case of failure.
     */
    public ParametrizedClassPanel(
    		@SuppressWarnings("unused") Field field, Class<?> cl, 
    		Object obj, ALDParameterDescriptor descr) 
    				throws ALDDataIOException {
    	this.mainPanel = new JPanel();
    	this.mainPanel.setLayout(new FlowLayout());

			// combo box for possible classes
			this.availableClasses = ALDClassInfo.lookupExtendingClasses(cl);
			// add the class itself if it is not abstract nor an interface
			if (   !(cl.isInterface()) 
					&& !(Modifier.isAbstract(cl.getModifiers())) 
					&& !(this.availableClasses.contains(cl)) )
					this.availableClasses.add(cl);
			Vector<ALDSwingComponentComboBoxItem> comboFields =
					new Vector<ALDSwingComponentComboBoxItem>();
			for (Class<?> c : this.availableClasses) {
				String shortName = c.getSimpleName();
				comboFields.add(
						new ALDSwingComponentComboBoxItem(c, shortName, 
								c.getCanonicalName()));
				this.shortNames.put(shortName, c);
				// generate configuration window for each class
				ALDParametrizedClassConfigWindow win =
						new ALDParametrizedClassConfigWindow(c, descr);
				win.addValueChangeEventListener(this);
				this.configWins.put(c, win);
			}
			// sort list of classes lexicographically
			Collections.sort(comboFields);
			// add dummy to reset selection at beginning of list
			comboFields.add(0, this.boxItemNone);
			// init combobox
			this.classSelection = new ALDSwingComponentComboBox(descr, 
					comboFields);
			this.classSelection.addValueChangeEventListener(this);
			// set the default value...
			if ( obj != null ) {
				// check which name convention to use
				this.classSelection.setSelectedItem(obj);
				ALDParametrizedClassConfigWindow win =
						this.configWins.get(obj.getClass());
				try {
	        win.setValue(obj);
        } catch (NullPointerException e2) {
        	System.err.println("Attention! Default object has wrong type, " 
        			+ " class " + obj.getClass() + " not found!!!");
        }
			}
			else {
				this.classSelection.setSelectedItem(this.boxItemNone);
			}

			// configuration button
			JButton confButton = new JButton("Configure...");
			confButton.setActionCommand("configure");
			confButton.addActionListener(this);

			// reset button
			JButton resetButton = new JButton("Reset");
			resetButton.setActionCommand("reset");
			resetButton.addActionListener(this);

			this.mainPanel.add(this.classSelection.getJComponent());
			this.mainPanel.add(confButton);
			this.mainPanel.add(resetButton);
		}

    /* (non-Javadoc)
     * @see de.unihalle.informatik.Alida.dataio.provider.swing.components.ALDSwingComponent#getJComponent()
     */
    @Override
    public JPanel getJComponent() {
    	return this.mainPanel;
    }
    
		/**
		 * Function to update values of parametrized class object.
		 * @param field		Field linked to the parameter to update.
		 * @param cl			Class of the parameter to update.
		 * @param value		New value to set.
		 * @throws ALDDataIOException Thrown in case of update failure.
		 */
		@SuppressWarnings("unused")
    public void setValue(Field field, Class<?> cl, Object value) 
			throws ALDDataIOException {
			if (value == null)
				return;
			Class<?> objClass = value.getClass();
			this.classSelection.setSelectedItem(value);
			ALDParametrizedClassConfigWindow win = 
					this.configWins.get(objClass);
			if (win != null) { 
				win.setValue(value);
			}
			else {
				System.err.println("ALDParametrizedClassDataIOSwing: \n" +
						"Configuration window for requested class doesn't exist...");
			}
		}
	      
	  /**
	   * Function to read parameter values from GUI. 
	   * 
	   * @param field		Field of object.
	   * @param cl			Class of object.
	   * @return	Current object value.
	   * @throws ALDDataIOException Thrown in case of failure.
	   */
	  public Object readData(Field field,
	  		@SuppressWarnings("unused") Class<?> cl) 
	  	throws ALDDataIOException {
	  	// get selected item
	  	Object item = 
	  			this.classSelection.getJComponent().getSelectedItem();
//	  	Class<?> selectedClass = null;
//	  	if (item instanceof String)
//	  		selectedClass = this.shortNames.get(item);
//	  	else
//	  		selectedClass = (Class<?>)item;
			Class<?> selectedClass = 
					(Class<?>)((ALDSwingComponentComboBoxItem)item).getObject();
	  	if (this.configWins.get(selectedClass) == null) {
	  		return null;
	  	}
		  return this.configWins.get(selectedClass).readData(field, 
		  		selectedClass);
	  }

		/* (non-Javadoc)
		 * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
		 */
		@Override
	  public void actionPerformed(ActionEvent e) {
			
			String command = e.getActionCommand();
			// pop-up the parametrized class configuration window
			if (command.equals("configure")) {
				// get selected item
				Object item = 
						this.classSelection.getJComponent().getSelectedItem();
				if (item.equals(this.boxItemNone))
					return;
				Class<?> selectedClass = 
						(Class<?>)((ALDSwingComponentComboBoxItem)item).getObject();
				try {
					// trigger event
	        this.configWins.get(selectedClass).setVisible(true);
					this.handleValueChangeEvent(
							new ALDSwingValueChangeEvent(this, null));
        } catch (ALDDataIOProviderException e1) {
	        // TODO Auto-generated catch block
	        e1.printStackTrace();
        }
			}
			else if (command.equals("reset")) {
				// close all windows
				@SuppressWarnings("rawtypes")
        Set<Class> keys = this.configWins.keySet();
				for (Class<?> k: keys) {
					try {
	          this.configWins.get(k).setVisible(false);
          } catch (ALDDataIOProviderException e1) {
          	// we can savely ignore this exception...
          }
				}
				// reset selection to 'none'
				this.classSelection.getJComponent().setSelectedItem(
						this.boxItemNone);
				// trigger event
				this.handleValueChangeEvent(
						new ALDSwingValueChangeEvent(this, null));
			}
	  }
		
		/* (non-Javadoc)
		 * @see java.awt.event.ItemListener#itemStateChanged(java.awt.event.ItemEvent)
		 */
    @Override
		@SuppressWarnings("unchecked")
	  public void itemStateChanged(ItemEvent e) {
			this.classSelection.getJComponent().removeAllItems();

			// generate list of available classes for sorting 
//			Vector<String> comboFields = new Vector<String>();
//			for (Class<?> c : this.availableClasses) {
//				comboFields.add(c.getSimpleName());
//			}
			
			LinkedList<ALDSwingComponentComboBoxItem> comboFields = 
					new LinkedList<ALDSwingComponentComboBoxItem>();
			for (Class<?> c : this.availableClasses) {
				String shortName = c.getSimpleName();
				// add item object to combobox fields
				comboFields.add(new ALDSwingComponentComboBoxItem(c, shortName, 
						c.getCanonicalName()));
			}
			// sort list of classes lexicographically
			Collections.sort(comboFields);
			comboFields.add(0, this.boxItemNone);

			// check which name convention to use and fill box
//			if (this.longNameCheckBox.isSelected()) {
//				for (String s: comboFields) {
//					for (@SuppressWarnings("rawtypes") Class c: this.availableClasses) {
//						if (c.getSimpleName().equals(s)) {
//							this.classSelection.getJComponent().addItem(c);
//							break;
//						}
//					}
//				}
//			}
//			else {
//				for (String s: comboFields) {
//					this.classSelection.getJComponent().addItem(s);
//				}
//			}

//			// check which name convention to use and fill box
//			if (this.longNameCheckBox.isSelected()) {
//				for (@SuppressWarnings("rawtypes") Class c: this.availableClasses) {
//					this.classSelection.addItem(c);
//				}
//			}
//			else {
//				for (@SuppressWarnings("rawtypes") Class c: this.availableClasses) {
//					this.classSelection.addItem(c.getSimpleName());
//				}
//			}
			for (ALDSwingComponentComboBoxItem item: comboFields) {
				this.classSelection.getJComponent().addItem(item);
			}
		}

		/* (non-Javadoc)
		 * @see de.unihalle.informatik.Alida.dataio.provider.swing.events.ALDSwingValueChangeListener#handleValueChangeEvent(de.unihalle.informatik.Alida.dataio.provider.swing.events.ALDSwingValueChangeEvent)
		 */
		@Override
		public void handleValueChangeEvent(ALDSwingValueChangeEvent event) {
			this.fireALDSwingValueChangeEvent(event);
		}

		@Override
    public void disableComponent() {
			this.classSelection.disableComponent();
			@SuppressWarnings("rawtypes")
      Set<Class> keys = this.configWins.keySet();
			for (@SuppressWarnings("rawtypes") Class key: keys) {
				ALDParametrizedClassConfigWindow win = this.configWins.get(key);
				if (win != null)
					win.disableComponent();
			}
    }

		@Override
    public void enableComponent() {
			this.classSelection.enableComponent();
			@SuppressWarnings("rawtypes")
      Set<Class> keys = this.configWins.keySet();
			for (@SuppressWarnings("rawtypes") Class key: keys) {
				ALDParametrizedClassConfigWindow win = this.configWins.get(key);
				if (win != null)
					win.enableComponent();
			}
    }

		@Override
    public void dispose() {
			this.classSelection.dispose();
			@SuppressWarnings("rawtypes")
      Set<Class> keys = this.configWins.keySet();
			for (@SuppressWarnings("rawtypes") Class key: keys) {
				ALDParametrizedClassConfigWindow win = this.configWins.get(key);
				if (win != null)
					win.dispose();
			}
    }
	}
	
	/**
	 * Button to visualize parametrized class objects.
	 * 
	 * @author moeller
	 */
	private class ParametrizedClassShowButton extends JButton 
		implements ActionListener {
		
		/**
		 * Associated parameter window.
		 */
		private ALDParametrizedClassConfigWindow win;
		
		/**
		 * Default constructor.
		 * @param obj		Object to visualize.
		 * @param descr	Descriptor associated with corresponding parameter.
		 */
		public ParametrizedClassShowButton(Object obj, 
				ALDParameterDescriptor descr) {
			super("Show data...");
			this.setActionCommand("show");
			this.addActionListener(this);
			this.win = new ALDParametrizedClassConfigWindow(obj, descr, true);
		}

		@Override
    public void actionPerformed(ActionEvent e) {
			String command = e.getActionCommand();
			if (command.equals("show")) {
				try {
	        this.win.setVisible(true);
        } catch (ALDDataIOProviderException e1) {
	        // TODO Auto-generated catch block
	        e1.printStackTrace();
        }
			}
    }
	}
}
