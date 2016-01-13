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

import de.unihalle.informatik.Alida.annotations.ALDDataIOProvider;
import de.unihalle.informatik.Alida.dataio.ALDDataIOManagerSwing;
import de.unihalle.informatik.Alida.dataio.ALDDataIOManagerSwing.*;
import de.unihalle.informatik.Alida.dataio.provider.ALDDataIOSwingInitialGUIValueDefaultHandler;
import de.unihalle.informatik.Alida.dataio.provider.swing.components.*;
import de.unihalle.informatik.Alida.dataio.provider.swing.events.*;
import de.unihalle.informatik.Alida.exceptions.*;
import de.unihalle.informatik.Alida.exceptions.ALDDataIOProviderException.*;
import de.unihalle.informatik.Alida.operator.ALDParameterDescriptor;

import javax.swing.*;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.filechooser.FileFilter;
import javax.swing.table.DefaultTableModel;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.Field;
import java.util.*;

/**
 * Class for generic loading/saving of 1D-arrays from/to GUI in Alida.
 * 
 * @author moeller
 */
@ALDDataIOProvider
public class ALDNativeArray1DDataIOSwing 
	extends ALDDataIOSwingInitialGUIValueDefaultHandler {

	/**
	 * List of supported classes.
	 */
	private static LinkedList<Class<?>> classes = null;
	
	/**
	 * Default constructor.
	 */
	public ALDNativeArray1DDataIOSwing() {
		if (classes == null) {
			classes = new LinkedList<Class<?>>();
			ALDNativeArray1DDataIOSwing.classes.add( Boolean[].class);
			ALDNativeArray1DDataIOSwing.classes.add( Byte[].class);
			ALDNativeArray1DDataIOSwing.classes.add( Double[].class);
			ALDNativeArray1DDataIOSwing.classes.add( Float[].class);
			ALDNativeArray1DDataIOSwing.classes.add( Integer[].class);
			ALDNativeArray1DDataIOSwing.classes.add( Short[].class);
			ALDNativeArray1DDataIOSwing.classes.add( String[].class);
			ALDNativeArray1DDataIOSwing.classes.add( boolean[].class);
			ALDNativeArray1DDataIOSwing.classes.add( byte[].class);
			ALDNativeArray1DDataIOSwing.classes.add( double[].class);
			ALDNativeArray1DDataIOSwing.classes.add( float[].class);
			ALDNativeArray1DDataIOSwing.classes.add( int[].class);
			ALDNativeArray1DDataIOSwing.classes.add( short[].class);
		}
	}
	
	/**
	 * Interface method to announce class for which IO is provided for.
	 * 
	 * @return  Collection of classes provided.
	 */
	@SuppressWarnings("unchecked")
  @Override
	public Collection<Class<?>> providedClasses() {
		return (Collection<Class<?>>)classes.clone();
	}
	
	/** 
	 * Generic reading of 1D arrays.
	 */
	/* (non-Javadoc)
	 * @see de.unihalle.informatik.Alida.helpers.ALDDataIOSwing#createGUIElement(java.lang.Class, java.lang.Object)
	 */
	@Override
	public ALDSwingComponent createGUIElement(Field field, Class<?> cl, 
			Object obj, ALDParameterDescriptor descr) {
		return new ArrayConfigPanel(field, cl, obj, descr);
	}

	/* (non-Javadoc)
	 * @see de.unihalle.informatik.Alida.dataio.provider.ALDDataIOSwing#setValue(java.lang.reflect.Field, java.lang.Class, de.unihalle.informatik.Alida.dataio.provider.swing.components.ALDSwingComponent, java.lang.Object)
	 */
	@Override
  public void setValue(Field field, Class<?> cl, 
  		ALDSwingComponent guiElement, Object value) 
  	throws ALDDataIOProviderException {
		if (value == null)
			return;
		if (!(guiElement instanceof ArrayConfigPanel))
			throw new ALDDataIOProviderException(
					ALDDataIOProviderExceptionType.INVALID_GUI_ELEMENT, 
					"[NativeArray1DDataIO] setValue() received invalid GUI element!");
		if (!(classes.contains(value.getClass())))
			throw new ALDDataIOProviderException(
					ALDDataIOProviderExceptionType.INVALID_GUI_ELEMENT, 
					"[NativeArray1DDataIO] setValue() received wrong object type!");
		((ArrayConfigPanel)guiElement).setValue(field, cl, value);
  }

	/* (non-Javadoc)
	 * @see de.unihalle.informatik.Alida.dataio.provider.ALDDataIOSwing#readData(java.lang.reflect.Field, java.lang.Class, de.unihalle.informatik.Alida.dataio.provider.swing.components.ALDSwingComponent)
	 */
	@Override
	public Object readData(
			Field field, Class<?> cl, ALDSwingComponent guiElement) 
		throws ALDDataIOProviderException {
		if (!(guiElement instanceof ArrayConfigPanel))
			throw new ALDDataIOProviderException(
					ALDDataIOProviderExceptionType.INVALID_GUI_ELEMENT, 
					"[NativeArray1DDataIO] readData() received invalid GUI element!");
		return ((ArrayConfigPanel)guiElement).readData(field, cl);
	}

	/* (non-Javadoc)
	 * @see de.unihalle.informatik.Alida.dataio.provider.ALDDataIOSwing#writeData(java.lang.Object)
	 */
	@Override
	public JComponent writeData(Object obj, ALDParameterDescriptor descr) 
			throws ALDDataIOProviderException {
		if (!(classes.contains(obj.getClass())))
			throw new ALDDataIOProviderException(
					ALDDataIOProviderExceptionType.OBJECT_TYPE_ERROR, 
					"[NativeArray1DDataIO] object to write has wrong type!");
		// return a button to show a window with the elements
		return new ArrayShowButton(obj, descr);
	}

	/**
	 * GUI element for configuring 1D arrays of native data types and 
	 * wrapper.
	 * <p>
	 * This button has an array configuration window attached to it 
	 * where specific data is stored and accessable.
	 * 
	 * @author moeller
	 */
	private class ArrayConfigPanel extends ALDSwingComponent
		implements ALDSwingValueChangeListener, ActionListener {

		/**
		 * GUI component associated with this Swing component.
		 */
		private JPanel configPanel;
		
		/**
		 * Button to create and configure array.
		 */
		private JButton confButton;
		
		/**
		 * Button to reset array.
		 */
		private JButton resetButton;

		/**
		 * Array configuration window.
		 */
		private ArrayConfigWindow confWin;

		/**
		 * Class of array to configure.
		 */
		private Class<?> arrayClass;
		
		/**
		 * Parameter field of array to configure.
		 */
		private Field arrayField;

		/**
		 * Descriptor of parameter linked to array. 
		 */
		private ALDParameterDescriptor arrayDescriptor;

		/**
		 * Constructor.
		 * 
		 * @param field		Field of collection.
		 * @param cl			Class of collection.
		 * @param obj			Default object.
		 * @param descr		Optional descriptor for additional information.
		 */
    public ArrayConfigPanel(Field field, Class<?> cl, Object obj,
    		ALDParameterDescriptor descr) {
    	this.arrayClass = cl;
    	this.arrayField = field;
    	this.arrayDescriptor = descr;
			this.confButton = new JButton("Configure Native Array...");
			this.confButton.setActionCommand("show");
			this.confButton.addActionListener(this);
			this.resetButton = new JButton("Reset");
			this.resetButton.setActionCommand("reset");
			this.resetButton.addActionListener(this);
			this.configPanel = new JPanel();
			this.configPanel.add(this.confButton);
			this.configPanel.add(this.resetButton);
			if (obj != null) {
				this.confWin = new ArrayConfigWindow(field, cl, obj, descr);
				this.confWin.addValueChangeEventListener(this);
			}
		}
    
    @Override
    public JComponent getJComponent() {
    	return this.configPanel;
    }
		
		/**
		 * Gets the data from the configuration window.
		 * 
		 * @param field	Field of collection.
		 * @param cl		Class of collection.
		 * @param value	Default object.
		 */
		public void setValue(Field field, Class<?> cl, Object value) {
			if (this.confWin != null)
				this.confWin.setValue(field, cl, value);
			else {
				this.confWin = 
						new ArrayConfigWindow(field, cl, value, this.arrayDescriptor);
				this.confWin.addValueChangeEventListener(this);
			}
		}

		/**
		 * Gets the data from the configuration window.
		 * 
		 * @param field	Field of collection.
		 * @param cl	Class of collection.
		 * @return Current data.
		 * @throws ALDDataIOProviderException Thrown in case of read failures.
		 */
		public Object readData(Field field, Class<?> cl) 
			throws ALDDataIOProviderException {
			if (this.confWin == null)
				return null;
			return this.confWin.readData(field, cl);
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
			if (this.confWin != null)
				this.confWin.disableComponent();
    }

		@Override
    public void enableComponent() {
			if (this.confWin != null)
				this.confWin.enableComponent();
    }

		@Override
    public void dispose() {
			if (this.confWin != null)
				this.confWin.dispose();
    }

		@Override
    public void actionPerformed(ActionEvent e) {
			String cmd = e.getActionCommand();
			if (cmd.equals("reset")) {
				if (this.confWin != null)
					this.confWin.dispose();
				this.confWin = null;
				this.handleValueChangeEvent(
						new ALDSwingValueChangeEvent(this, this.arrayDescriptor));
			}
			else if (cmd.equals("show")) {
				if (this.confWin != null)
					this.confWin.setVisible(true);
				else { 
					this.confWin = new ArrayConfigWindow(this.arrayField, 
							this.arrayClass, null, this.arrayDescriptor);
					this.confWin.addValueChangeEventListener(this);
					this.confWin.setVisible(true);
					this.handleValueChangeEvent(
							new ALDSwingValueChangeEvent(this, this.arrayDescriptor));
				}
			}
    }
	}	
	
	/**
	 * GUI element for displaying 2D arrays.
	 * 
	 * @author moeller
	 */
	private class ArrayShowButton extends JButton 
		implements ActionListener{

		/**
		 * Data to be displayed.
		 */
		private Object data;

		/**
		 * Optional descriptor for additional information on parameter.
		 */
		private ALDParameterDescriptor descriptor;
		
		/**
		 * Constructor.
		 * @param obj   Object to show in GUI.
		 * @param descr Descriptor associated with operator parameter.
		 */
		public ArrayShowButton(Object obj, ALDParameterDescriptor descr) {
			super("Show data...");
			this.setActionCommand("showButtonPressed");
			this.addActionListener(this);
			this.data = obj;
			this.descriptor = descr;
		}
		
		/* (non-Javadoc)
		 * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
		 */
		@Override
    public void actionPerformed(ActionEvent e) {
			String cmd = e.getActionCommand(); 
			if (cmd.equals("showButtonPressed")) {
				Class<?> cl = this.data.getClass();
				DefaultTableModel outTabModel = null;
				if (cl.equals(Boolean[].class)) {
					Boolean [] field = (Boolean[])this.data;
					int height = field.length;
					outTabModel = new DefaultTableModel(height, 1) {
						@Override
						public boolean isCellEditable(int row, int column) {
							return false;
						}};
						for (int h=0;h<height;++h) {
							outTabModel.setValueAt(field[h], h, 0);
						}
				}
				else if (cl.equals(Byte[].class)) {
					Byte [] field = (Byte[])this.data;
					int height = field.length;
					outTabModel = new DefaultTableModel(height, 1) {
						@Override
						public boolean isCellEditable(int row, int column) {
							return false;
						}};
					for (int h=0;h<height;++h) {
							outTabModel.setValueAt(field[h], h, 0);
					}
				}
				else if (cl.equals(Double[].class)) {
					Double [] field = (Double[])this.data;
					int height = field.length;
					outTabModel = new DefaultTableModel(height, 1) {
						@Override
						public boolean isCellEditable(int row, int column) {
							return false;
						}};
						for (int h=0;h<height;++h) {
							outTabModel.setValueAt(field[h], h, 0);
						}
				}
				else if (cl.equals(Float[].class)) {
					Float [] field = (Float[])this.data;
					int height = field.length;
					outTabModel = new DefaultTableModel(height, 1) {
						@Override
						public boolean isCellEditable(int row, int column) {
							return false;
						}};
						for (int h=0;h<height;++h) {
							outTabModel.setValueAt(field[h], h, 0);
						}
				}
				else if (cl.equals(Integer[].class)) {
					Integer [] field = (Integer[])this.data;
					int height = field.length;
					outTabModel = new DefaultTableModel(height, 1) {
						@Override
						public boolean isCellEditable(int row, int column) {
							return false;
						}};
						for (int h=0;h<height;++h) {
							outTabModel.setValueAt(field[h], h, 0);
						}
				}
				else if (cl.equals(Short[].class)) {
					Short [] field = (Short[])this.data;
					int height = field.length;
					outTabModel = new DefaultTableModel(height, 1) {
						@Override
						public boolean isCellEditable(int row, int column) {
							return false;
						}};
					for (int h=0;h<height;++h) {
						outTabModel.setValueAt(field[h], h, 0);
					}
				}
				else if (cl.equals(String[].class)) {
					String [] field = (String[])this.data;
					int height = field.length;
					outTabModel = new DefaultTableModel(height, 1) {
						@Override
						public boolean isCellEditable(int row, int column) {
							return false;
						}};
					for (int h=0;h<height;++h) {
						outTabModel.setValueAt(field[h], h, 0);
					}
				}
				else if (cl.equals(boolean[].class)) {
					boolean [] field = (boolean[])this.data;
					int height = field.length;
					outTabModel = new DefaultTableModel(height, 1) {
						@Override
						public boolean isCellEditable(int row, int column) {
							return false;
						}};
					for (int h=0;h<height;++h) {
						outTabModel.setValueAt(new Boolean(field[h]), h, 0);
					}
				}
				else if (cl.equals(byte[].class)) {
					byte [] field = (byte[])this.data;
					int height = field.length;
					outTabModel = new DefaultTableModel(height, 1) {
						@Override
						public boolean isCellEditable(int row, int column) {
							return false;
						}};
					for (int h=0;h<height;++h) {
						outTabModel.setValueAt(new Byte(field[h]), h, 0);
					}
				}
				else if (cl.equals(double[].class)) {
					double [] field = (double[])this.data;
					int height = field.length;
					outTabModel = new DefaultTableModel(height, 1) {
						@Override
						public boolean isCellEditable(int row, int column) {
							return false;
						}};
					for (int h=0;h<height;++h) {
						outTabModel.setValueAt(new Double(field[h]), h, 0);
					}
				}
				else if (cl.equals(float[].class)) {
					float [] field = (float[])this.data;
					int height = field.length;
					outTabModel = new DefaultTableModel(height, 1) {
						@Override
						public boolean isCellEditable(int row, int column) {
							return false;
						}};
					for (int h=0;h<height;++h) {
						outTabModel.setValueAt(new Float(field[h]), h, 0);
					}					
				}
				else if (cl.equals(int[].class)) {
					int [] field = (int[])this.data;
					int height = field.length;
					outTabModel = new DefaultTableModel(height, 1) {
						@Override
						public boolean isCellEditable(int row, int column) {
							return false;
						}};
					for (int h=0;h<height;++h) {
						outTabModel.setValueAt(new Integer(field[h]), h, 0);
					}
				}
				else if (cl.equals(short[].class)) {
					short [] field = (short[])this.data;
					int height = field.length;
					outTabModel = new DefaultTableModel(height, 1) {
						@Override
						public boolean isCellEditable(int row, int column) {
							return false;
						}};
					for (int h=0;h<height;++h) {
						outTabModel.setValueAt(new Short(field[h]), h, 0);
					}
				}
				ALDTableWindow resultTable = new ALDTableWindow(outTabModel,
						this.data);
				String paramName = "<unknown>";
				if (this.descriptor != null)
					paramName = "<" + this.descriptor.getLabel() + ">";
				resultTable.setTitle("Result data (" + cl.getSimpleName() 
						+ ") for parameter " + paramName);
				resultTable.openWindow();
			}
    }
	}	

	/**
	 * Graphical user interface window for displaying tables.
	 * 
	 * @author moeller
	 */
	private class ArrayConfigWindow extends ALDSwingValueChangeReporter	
		implements ActionListener, TableModelListener {

		/**
		 * Main frame.
		 */
		private JFrame window = null;
		
		/**
		 * Data table (swing tables are nicer than imageJ tables).
		 */
		protected JTable dataTab;
		
		/**
		 * Reference object to the data.
		 */
		private DefaultTableModel dataTabModel;
		
		/**
		 * Table component in window.
		 */
		private JPanel tabPanel;
		
		/**
		 * Scrollpane for table panel;
		 */
		private JScrollPane tableScroller;
		
		/**
		 * Button for adding a row.
		 */
		private JButton addRow;

		/**
		 * Button for deleting a row.
		 */
		private JButton delRow;

		/**
		 * Button to save the table.
		 */
		private JButton tabSave;

		/**
		 * Button to load a table.
		 */
		private JButton tabLoad;

		/**
		 * Button to clear the table.
		 */
		private JButton tabClear;
		
		/**
		 * Button to reset the table.
		 */
		private JButton tabReset;

		/**
		 * For convenience: always open last directory for saving.
		 */
		private File lastDir;

		/**
		 * Class that table entries should represent.
		 */
		private Class<?> elementClass;
		
		/**
		 * Optional descriptor for additional information on parameter.
		 */
		protected ALDParameterDescriptor descriptor;
		
		/**
		 * List of currently specified values (to restore entry if check 
		 * fails).
		 */
		private Object[] entryRefList;
		
		/**
		 * Flag to disable entry validation checks.
		 * <p>
		 * Note that this flag is only valid for next event appearing. While 
		 * processing the event the flag is reset to false. 
		 */
		private boolean disableValueChecks = false;
		
		/**
		 * Flag to indicate the table window has the focus.
		 * <p>
		 * By this multiple warnings on the same issue should be avoided.
		 * They might happen due to table change events and focus change 
		 * events being triggered at the same time.
		 */
		protected boolean windowHasFocus = false;
		
		/**
		 * Default constructor.
		 * @param field Field of array.
		 * @param cl		Class of array elements.
		 * @param obj		Default values.
		 * @param descr Descriptor associated with operator parameter.
		 */
		public ArrayConfigWindow(Field field, Class<?> cl, Object obj,
				ALDParameterDescriptor descr) {
			// instantiate the main window
			this.window = new JFrame();
			
			// init dummy table, needs at least one cell; overwritten if obj 
			// non-null
			this.dataTabModel = new DefaultTableModel(1, 1);
			this.dataTabModel.addTableModelListener(this);
			this.entryRefList = new Object[1]; 
			// fill with default values
			this.setTabEntryToDefault(0, cl); 
			
			// remember the class of the array
			this.elementClass = cl;
			
			// store additional descriptor
			this.descriptor = descr;
			
			// init current directory with user directory
			this.lastDir= new File(System.getProperty("user.dir"));
		
			// build table window
			this.setupTable();
			
			// if available, fill table with default values
			if (obj != null)
				this.setValue(field, cl, obj);
		}
		
		/**
		 * Show or hide the configuration window.
		 * @param b	If true, window is displayed, otherwise it is hidden.
		 */
		public void setVisible(boolean b) {
			this.window.setVisible(b);
		}
		
		/**
		 * Disable table and buttons to prohibit value changes.
		 */
		public void disableComponent() {
			if (this.dataTab != null)
				this.dataTab.setEnabled(false);
			if (this.addRow != null)
				this.addRow.setEnabled(false);
			if (this.delRow != null) 
				this.delRow.setEnabled(false);
			if (this.tabClear != null)
				this.tabClear.setEnabled(false);
			if (this.tabReset != null)
				this.tabReset.setEnabled(false);
		}
		
		/**
		 * Enable table and buttons to allow for value changes.
		 */
		public void enableComponent() {
			if (this.dataTab != null)
				this.dataTab.setEnabled(true);
			if (this.addRow != null)
				this.addRow.setEnabled(true);
			if (this.delRow != null) 
				this.delRow.setEnabled(true);
			if (this.tabClear != null)
				this.tabClear.setEnabled(true);
			if (this.tabReset != null)
				this.tabReset.setEnabled(true);
		}
		
		/**
		 * Disposes all resources of this window.
		 */
		public void dispose() {
			this.window.dispose();
		}
		
		/**
		 * Sets the specified table entry to the class default.
		 * @param row		Row to set.
		 * @param cl		Class of table elements.
		 */
		protected void setTabEntryToDefault(int row, Class<?> cl) {
			if (cl.equals(boolean[].class)) {
				boolean tmpBool = false;
				this.dataTabModel.setValueAt(Boolean.toString(tmpBool), row, 0);
			}
			else if (cl.equals(byte[].class)) {
				byte tmpByte = 0;
				this.dataTabModel.setValueAt(Byte.toString(tmpByte), row, 0);	
			}
			else if (cl.equals(double[].class)) {
				double tmpDouble = 0;
				this.dataTabModel.setValueAt(Double.toString(tmpDouble), row, 0);
			}
			else if (cl.equals(float[].class)) {
				float tmpFloat = 0;
				this.dataTabModel.setValueAt(Float.toString(tmpFloat), row, 0);
			}
			else if (cl.equals(int[].class)) {
				int tmpInt = 0;
				this.dataTabModel.setValueAt(Integer.toString(tmpInt), row, 0);
			}
			else if (cl.equals(short[].class)) {
				short tmpShort = 0;
				this.dataTabModel.setValueAt(Short.toString(tmpShort), row, 0);
			}
			else if (cl.equals(Boolean[].class)) {
				Boolean tmpBool = new Boolean(false);
				this.dataTabModel.setValueAt(tmpBool.toString(), row, 0);
			}
			else if (cl.equals(Byte[].class)) {
				Byte tmpByte = new Byte((byte)0);
				this.dataTabModel.setValueAt(tmpByte.toString(), row, 0);	
			}
			else if (cl.equals(Double[].class)) {
				Double tmpDouble = new Double(0);
				this.dataTabModel.setValueAt(tmpDouble.toString(), row, 0);
			}
			else if (cl.equals(Float[].class)) {
				Float tmpFloat = new Float(0);
				this.dataTabModel.setValueAt(tmpFloat.toString(), row, 0);
			}
			else if (cl.equals(Integer[].class)) {
				Integer tmpInt = new Integer(0);
				this.dataTabModel.setValueAt(tmpInt.toString(), row, 0);
			}
			else if (cl.equals(Short[].class)) {
				Short tmpShort = new Short((short)0);
				this.dataTabModel.setValueAt(tmpShort.toString(), row, 0);
			}
			else if (cl.equals(String[].class)) {
				this.dataTabModel.setValueAt(new String(), row, 0);
			}
			// store the value for later value checking
			this.entryRefList[row] = this.dataTabModel.getValueAt(row,0);
		}
		
    /**
     * Fills the table with specified values.
     * @param field		Field specifying type of data.
     * @param cl			Class of array entries.
     * @param value		Value to set.
     */
    public void setValue(@SuppressWarnings("unused") Field field, 
    		Class<?> cl, Object value) {
    	// there is nothing to do if value is null
    	if (value == null)
    		return;
			if (   cl.equals(Boolean[].class)
					|| cl.equals(Byte[].class)
					|| cl.equals(Double[].class)
					|| cl.equals(Float[].class)
					|| cl.equals(Integer[].class)
					|| cl.equals(Short[].class)
					|| cl.equals(String[].class)) {
				Object [] array  = (Object[])value;
				int rows = array.length;
				this.dataTabModel = new DefaultTableModel(rows,1);
				this.entryRefList = new Object[rows];
				for (int r=0;r<rows;++r) {
					Object val = array[r];
					String entry = new String();
					if (val != null)
						entry = val.toString();
					this.dataTabModel.setValueAt(entry, r, 0);
					this.entryRefList[r] = this.dataTabModel.getValueAt(r,0);
				}
			}
			else if (cl.equals(boolean[].class)) {
				boolean [] array = (boolean[])value;
				int rows = array.length;
				this.dataTabModel = new DefaultTableModel(rows,1);
				this.entryRefList = new Object[rows];
				for (int r=0;r<rows;++r) {
					this.dataTabModel.setValueAt(Boolean.toString(array[r]), r, 0);
					this.entryRefList[r] = this.dataTabModel.getValueAt(r,0);
				}
			}
			else if (cl.equals(byte[].class)) {
				byte [] array = (byte[])value;
				int rows = array.length;
				this.dataTabModel = new DefaultTableModel(rows,1);
				this.entryRefList = new Object[rows];
				for (int r=0;r<rows;++r) {
					this.dataTabModel.setValueAt(Byte.toString(array[r]), r, 0);	
					this.entryRefList[r] = this.dataTabModel.getValueAt(r,0);
				}
			}
			else if (cl.equals(double[].class)) {
				double [] array = (double[])value;
				int rows = array.length;
				this.dataTabModel = new DefaultTableModel(rows,1);
				this.entryRefList = new Object[rows];
				for (int r=0;r<rows;++r) {
					this.dataTabModel.setValueAt(Double.toString(array[r]), r, 0);
					this.entryRefList[r] = this.dataTabModel.getValueAt(r,0);
				}
			}
			else if (cl.equals(float[].class)) {
				float [] array = (float[])value;
				int rows = array.length;
				this.dataTabModel = new DefaultTableModel(rows,1);
				this.entryRefList = new Object[rows];
				for (int r=0;r<rows;++r) {
					this.dataTabModel.setValueAt(Float.toString(array[r]), r, 0);
					this.entryRefList[r] = this.dataTabModel.getValueAt(r,0);
				}
			}
			else if (cl.equals(int[].class)) {
				int [] array = (int[])value;
				int rows = array.length;
				this.dataTabModel = new DefaultTableModel(rows,1);
				this.entryRefList = new Object[rows];
				for (int r=0;r<rows;++r) {
					this.dataTabModel.setValueAt(Integer.toString(array[r]), r, 0);
					this.entryRefList[r] = this.dataTabModel.getValueAt(r,0);
				}
			}
			else if (cl.equals(short[].class)) {
				short [] array = (short[])value;
				int rows = array.length;
				this.dataTabModel = new DefaultTableModel(rows,1);
				this.entryRefList = new Object[rows];
				for (int r=0;r<rows;++r) {
					this.dataTabModel.setValueAt(Short.toString(array[r]), r, 0);
					this.entryRefList[r] = this.dataTabModel.getValueAt(r,0);
				}
			}
			// update the view
			this.dataTabModel.addTableModelListener(this);
			this.dataTab.setModel(this.dataTabModel);
			this.tabPanel.updateUI();
    }

    /**
     * Read data from graphical component.
     * @param field	Field of underlying parameter.
     * @param cl		Class of underlying parameter.
     * @return	Value of the parameter in GUI.
     * @throws ALDDataIOProviderException Thrown on read failures.
     */
    @SuppressWarnings({ "unchecked" })
    public Object readData(@SuppressWarnings("unused") Field field, 
    																												Class<?> cl) 
    		throws ALDDataIOProviderException {
    	
    	// check if there is already data to check
    	if (this.dataTabModel == null) {
    		System.out.println("Data tab model is null!");
    		return null;
    	}

    	// list for collecting errors
    	LinkedList<Integer> errors = new LinkedList<Integer>();

    	// get data, each vector entry is again of type Vector<Object>
    	Vector<Object> data = this.dataTabModel.getDataVector();

    	// check if table contains at least one non-null element
    	int lastNonNullIndex = data.size()-1;
    	if (!cl.equals(String[].class)) {
      	lastNonNullIndex = data.size();
    		for (int i= data.size()-1; i>= 0; --i) {
    			if (     data.elementAt(i) == null 
    					|| ((Vector<Object>)data.elementAt(i)).isEmpty()) {
    				continue;
    			}
    			if (((Vector<Object>)data.elementAt(i)).elementAt(0) != null) {
    				lastNonNullIndex = i;
    				break;
    			}
    		}
      	// ... if not, return null
      	if (lastNonNullIndex == data.size()) {
      		return null;
      	}
    	}

    	int rows = lastNonNullIndex+1;
    	Object entry;
    	if (cl.equals(Boolean[].class)) {
    		Boolean [] tmpfield  = new Boolean[rows];
    		for (int r=0;r<rows;++r) {
    			entry = ((Vector<Object>)data.elementAt(r)).elementAt(0);
    			if (entry == null) {
    				errors.add(new Integer(r));
    			}
    			else {
    				try {
    					tmpfield[r] = Boolean.valueOf((String)(entry));
    				} catch (NumberFormatException ne) {
    					errors.add(new Integer(r));
    				}
    			}
    		}
    		if (errors.size() > 0)
    			this.errorListToMessage(errors);
    		return tmpfield;
    	}
    	if (cl.equals(Byte[].class)) {
    		Byte [] tmpfield  = new Byte[rows];
    		for (int r=0;r<rows;++r) {
    			entry = ((Vector<Object>)data.elementAt(r)).elementAt(0);
    			if (entry == null) {
    				errors.add(new Integer(r));
    				continue;
    			}
    			try {
    				tmpfield[r] = Byte.valueOf((String)(entry));
    			} catch (NumberFormatException ne) {
    				errors.add(new Integer(r));
    			}
    		}
    		if (errors.size() > 0)
    			this.errorListToMessage(errors);
    		return tmpfield;
    	}
    	if (cl.equals(Double[].class)) {
    		Double [] tmpfield  = new Double[rows];
    		for (int r=0;r<rows;++r) {
    			entry = ((Vector<Object>)data.elementAt(r)).elementAt(0);
    			if (entry == null) {
    				errors.add(new Integer(r));
    				continue;
    			}
    			try {
    				tmpfield[r] = Double.valueOf((String)(entry));
    			} catch (NumberFormatException ne) {
    				errors.add(new Integer(r));
    			}
    		}
    		if (errors.size() > 0)
    			this.errorListToMessage(errors);
    		return tmpfield;
    	}
    	if (cl.equals(Float[].class)) {
    		Float [] tmpfield  = new Float[rows];
    		for (int r=0;r<rows;++r) {
    			entry = ((Vector<Object>)data.elementAt(r)).elementAt(0);
    			if (entry == null) {
    				errors.add(new Integer(r));
    				continue;
    			}
    			try {
    				tmpfield[r] = Float.valueOf((String)(entry));
    			} catch (NumberFormatException ne) {
    				errors.add(new Integer(r));
    			}
    		}
    		if (errors.size() > 0)
    			this.errorListToMessage(errors);
    		return tmpfield;		
    	}
    	if (cl.equals(Integer[].class)) {
    		Integer [] tmpfield  = new Integer[rows];
    		for (int r=0;r<rows;++r) {
    			entry = ((Vector<Object>)data.elementAt(r)).elementAt(0);
    			if (entry == null) {
    				errors.add(new Integer(r));
    				continue;
    			}
    			try {
    				tmpfield[r] = Integer.valueOf((String)(entry));
    			} catch (NumberFormatException ne) {
    				errors.add(new Integer(r));
    			}
    		}
    		if (errors.size() > 0)
    			this.errorListToMessage(errors);
    		return tmpfield;
    	}
    	if (cl.equals(Short[].class)) {
    		Short [] tmpfield  = new Short[rows];
    		for (int r=0;r<rows;++r) {
    			entry = ((Vector<Object>)data.elementAt(r)).elementAt(0);
    			if (entry == null) {
    				errors.add(new Integer(r));
    				continue;
    			}
    			try {
    				tmpfield[r] = Short.valueOf((String)(entry));
    			} catch (NumberFormatException ne) {
    				errors.add(new Integer(r));
    			}
    		}
    		if (errors.size() > 0)
    			this.errorListToMessage(errors);
    		return tmpfield;				
    	}
    	if (cl.equals(String[].class)) {
    		String [] tmpfield  = new String[rows];
    		for (int r=0;r<rows;++r) {
    			entry = ((Vector<Object>)data.elementAt(r)).elementAt(0);
    			if (entry == null) {
    				tmpfield[r] = new String();
//    				tmpfield[r] = null;
    			}
    			else {
    				tmpfield[r] = (String)entry;
    			}
    		}
//    		if (errors.size() > 0)
//    			this.errorListToMessage(errors);
    		return tmpfield;
    	}
    	if (cl.equals(boolean[].class)) {
    		boolean [] tmpfield  = new boolean[rows];
    		for (int r=0;r<rows;++r) {
    			entry = ((Vector<Object>)data.elementAt(r)).elementAt(0);
    			if (entry == null) {
    				errors.add(new Integer(r));
    				continue;
    			}
    			try {
    				tmpfield[r] = Boolean.valueOf((String)(entry)).booleanValue();
    			} catch (NumberFormatException ne) {
    				errors.add(new Integer(r));
    			}
    		}
    		if (errors.size() > 0)
    			this.errorListToMessage(errors);
    		return tmpfield;
    	}
    	if (cl.equals(byte[].class)) {
    		byte [] tmpfield  = new byte[rows];
    		for (int r=0;r<rows;++r) {
    			entry = ((Vector<Object>)data.elementAt(r)).elementAt(0);
    			if (entry == null) {
    				errors.add(new Integer(r));
    				continue;
    			}
    			try {
    				tmpfield[r] = Byte.valueOf((String)(entry)).byteValue();
    			} catch (NumberFormatException ne) {
    				errors.add(new Integer(r));
    			}
    		}
    		if (errors.size() > 0)
    			this.errorListToMessage(errors);
    		return tmpfield;
    	}
    	if (cl.equals(double[].class)) {
    		double [] tmpfield  = new double[rows];
    		for (int r=0;r<rows;++r) {
    			entry = ((Vector<Object>)data.elementAt(r)).elementAt(0);
    			if (entry == null) {
    				errors.add(new Integer(r));
    				continue;
    			}
    			try {
    				tmpfield[r] = Double.valueOf((String)(entry)).doubleValue();
    			} catch (NumberFormatException ne) {
    				errors.add(new Integer(r));
    			}
    		}
    		if (errors.size() > 0)
    			this.errorListToMessage(errors);
    		return tmpfield;
    	}
    	if (cl.equals(float[].class)) {
    		float [] tmpfield  = new float[rows];
    		for (int r=0;r<rows;++r) {
    			entry = ((Vector<Object>)data.elementAt(r)).elementAt(0);
    			if (entry == null) {
    				errors.add(new Integer(r));
    				continue;
    			}
    			try {
    				tmpfield[r] = Float.valueOf((String)(entry)).floatValue();
    			} catch (NumberFormatException ne) {
    				errors.add(new Integer(r));
    			}
    		}
    		if (errors.size() > 0)
    			this.errorListToMessage(errors);
    		return tmpfield;
    	}
    	if (cl.equals(int[].class)) {
    		int [] tmpfield  = new int[rows];
    		for (int r=0;r<rows;++r) {
    			entry = ((Vector<Object>)data.elementAt(r)).elementAt(0);
    			if (entry == null) {
    				errors.add(new Integer(r));
    				continue;
    			}
    			try {
    				tmpfield[r] = Integer.valueOf((String)(entry)).intValue();
    			} catch (NumberFormatException ne) {
    				errors.add(new Integer(r));
    			}
    		}
    		if (errors.size() > 0)
    			this.errorListToMessage(errors);
    		return tmpfield;
    	}
    	if (cl.equals(short[].class)) {
    		short [] tmpfield  = new short[rows];
    		for (int r=0;r<rows;++r) {
    			entry = ((Vector<Object>)data.elementAt(r)).elementAt(0);
    			if (entry == null) {
    				errors.add(new Integer(r));
    				continue;
    			}
    			try {
    				tmpfield[r] = Short.valueOf((String)(entry)).shortValue();
    			} catch (NumberFormatException ne) {
    				errors.add(new Integer(r));
    			}
    		}
    		if (errors.size() > 0)
    			this.errorListToMessage(errors);
    		return tmpfield;
    	}
    	return null; 
    }
		
    /**
     * Method to resize the reference entry list.
     * <p>
     * Existing entries are preserved as far as possible.
     * @param newSize		New size for the table.
     */
    protected void resizeTableEntries(int newSize) {
    	Object[] newEntries = new Object[newSize];
    	int commonSize = (newSize > this.entryRefList.length ?
    			this.entryRefList.length : newSize); 
    	for (int i=0;i<commonSize;++i) {
    		newEntries[i] = this.entryRefList[i];
    	}
    	this.entryRefList = newEntries;
    }
    
		/**
		 * Checks if a string can be cast to the desired class.
		 * @param cl			Target class.
		 * @param entry		String under consideration.
		 * @return	True if cast is possible.
		 */
		@SuppressWarnings({ "unused" })
    protected boolean validateEntry(Class<?> cl, String entry) {
			if (entry == null)
				return false;
			// a string is allowed to be empty
			if (entry.isEmpty() && !cl.equals(String[].class))
				return false;
			if (cl.equals(Boolean[].class)) {
				try {
					Boolean t = Boolean.valueOf((entry));
				} catch (NumberFormatException ne) {
					return false;
				}
			}
			else if (cl.equals(Byte[].class)) {
				try {
					Byte t = Byte.valueOf((entry));
				} catch (NumberFormatException ne) {
					return false;
				}
			}
			else if (cl.equals(Double[].class)) {
				try {
					Double t = Double.valueOf((entry));
				} catch (NumberFormatException ne) {
					return false;
				}
			}
			else if (cl.equals(Float[].class)) {
				try {
					Float t = Float.valueOf((entry));
				} catch (NumberFormatException ne) {
					return false;
				}
			}
			else if (cl.equals(Integer[].class)) {
				try {
					Integer t = Integer.valueOf((entry));
				} catch (NumberFormatException ne) {
					return false;
				}
			}
			else if (cl.equals(Short[].class)) {
				try {
					Short t = Short.valueOf((entry));
				} catch (NumberFormatException ne) {
					return false;
				}
			}
			else if (cl.equals(String[].class)) {
				return true;
			}
			else if (cl.equals(boolean[].class)) {
				try {
					boolean t = Boolean.valueOf((entry)).booleanValue();
				} catch (NumberFormatException ne) {
					return false;
				}	
			}
			else if (cl.equals(byte[].class)) {
				try {
					byte t = Byte.valueOf((entry)).byteValue();
				} catch (NumberFormatException ne) {
					return false;
				}
			}
			else if (cl.equals(double[].class)) {
				try {
					double t = Double.valueOf((entry)).doubleValue();
				} catch (NumberFormatException ne) {
					return false;
				}
			}
			else if (cl.equals(float[].class)) {
				try {
					float t = Float.valueOf((entry)).floatValue();
				} catch (NumberFormatException ne) {
					return false;
				}
			}
			else if (cl.equals(int[].class)) {
				try {
					int t = Integer.valueOf((entry)).intValue();
				} catch (NumberFormatException ne) {
					return false;
				}
			}
			else if (cl.equals(short[].class)) {
				try {
					short t = Short.valueOf((entry)).shortValue();
				} catch (NumberFormatException ne) {
					return false;
				}
			}
			return true;
		}

		/**
		 * Saves the contents of the table to a user-specified file.
		 * <p>
		 * The file format is TSV, i.e. tabulator-separated values.
		 * The default ending is '.txt'. The user can select the file
		 * name through a file open dialog which pops-up on call of the 
		 * function.
		 */
		protected void saveTable() {

			File file= null;
			
			JFrame dummy= new JFrame();
			JFileChooser getSaveFileDialog= new JFileChooser(); 
			getSaveFileDialog.setFileFilter(new DataTabFileFilter());
			getSaveFileDialog.setFileSelectionMode(JFileChooser.FILES_ONLY);
			getSaveFileDialog.setCurrentDirectory(this.lastDir); 
			getSaveFileDialog.setSelectedFile(new File("ResultTab.txt"));
			getSaveFileDialog.setApproveButtonText("Speichern");
			getSaveFileDialog.setDialogTitle("Select file name");
			int returnVal = getSaveFileDialog.showOpenDialog(dummy);
	        if (returnVal == JFileChooser.APPROVE_OPTION) {
	            file = getSaveFileDialog.getSelectedFile();
	            this.lastDir= getSaveFileDialog.getCurrentDirectory();
	        }
	        
	        // obviously we did not get any name, can't do anything...
			if (file== null)
				return;
			
			// ... otherwise write table to selected file
			try{
				if (file.exists()) {
					int ret= 
						JOptionPane.showConfirmDialog(null,
								"The file exists already, would "
								+ "you like to overwrite it?", "Warning", 
								JOptionPane.YES_NO_OPTION);
					switch(ret)
					{
					case JOptionPane.NO_OPTION:
					case JOptionPane.CANCEL_OPTION:
						return;
					case JOptionPane.YES_OPTION:
						// nothing special to do, just proceed
					}
				}
				FileWriter ow= new FileWriter(file.getPath());
				StringBuffer [] tab= ALDTableWindow.tableToString(
						this.dataTabModel);
				// note: first row contains the header which is meaningless in 
				//       this context, therefore it is ignored
				for (int i=1;i<tab.length;++i)
					ow.write(tab[i].toString());
				ow.close();
			} catch (IOException e) {
				JOptionPane.showMessageDialog(null,"Error!!! " +
						"Could not open output file " + file.getPath() + "!");
			}
		}

		/**
		 * Loads contents of the table from a user-specified file.
		 * <p>
		 * The file format is TSV, i.e. tabulator-separated values.
		 * The default ending is '.txt'. The user can select the file
		 * name through a file open dialog which pops-up on call of the 
		 * function.
		 */
		protected void loadTable() {

			// request the file from the user
			File file= null;

			JFrame dummy= new JFrame();
			JFileChooser getSaveFileDialog= new JFileChooser(); 
			getSaveFileDialog.setFileFilter(new DataTabFileFilter());
			getSaveFileDialog.setFileSelectionMode(JFileChooser.FILES_ONLY);
			getSaveFileDialog.setCurrentDirectory(this.lastDir); 
			getSaveFileDialog.setSelectedFile(new File("ResultTab.txt"));
			getSaveFileDialog.setApproveButtonText("Open");
			getSaveFileDialog.setDialogTitle("Select file name");
			int returnVal = getSaveFileDialog.showOpenDialog(dummy);
			if (returnVal == JFileChooser.APPROVE_OPTION) {
				file = getSaveFileDialog.getSelectedFile();
				this.lastDir= getSaveFileDialog.getCurrentDirectory();
			}
	        
			// obviously we did not get any name, can't do anything...
			if (file== null)
				return;

			// ... otherwise load table from selected file
			try{
				if (!file.exists()) {
					JOptionPane.showMessageDialog(null, "The file does not exist!", 
						"Error reading file!", JOptionPane.ERROR_MESSAGE);
					return;
				}
				FileInputStream fis = new FileInputStream(file.getPath());
				BufferedReader br = 
						new BufferedReader(new InputStreamReader(fis));
				Vector<String> entries = new Vector<String>();
				String line = br.readLine();
				while( line != null ) {
					entries.add(line.split("\t")[0]);
					line = br.readLine();
				}
				Object[] tableData = new Object[entries.size()];
				for (int i=0;i<entries.size(); ++i) {
					tableData[i] = entries.elementAt(i);
				}
				br.close();
				this.setValue(null, this.elementClass, tableData);
			} catch (IOException e) {
				JOptionPane.showMessageDialog(null,"Error!!! " +
						"Could not open input file " + file.getPath() + "!");
				e.printStackTrace();
			}
		}

		/**
		 * Initializes the data table window.
		 */
		private void setupTable() {
			
			// instantiate result table and put into scroll pane
			this.dataTab= new JTable(this.dataTabModel);
			this.dataTab.addFocusListener(new TableFocusListener());
			this.tableScroller= new JScrollPane(this.dataTab);
			this.tableScroller.setPreferredSize(new Dimension (400,400));
			this.dataTab.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);

			// add a toolbar
			this.addRow= new JButton("Add Row");
			this.addRow.setActionCommand("addRow");
			this.addRow.addActionListener(this);
			this.addRow.setSize(new Dimension(80,35));
			this.addRow.setToolTipText("Appends a row to the table.");
			this.delRow= new JButton("Delete Row");
			this.delRow.setActionCommand("delRow");
			this.delRow.addActionListener(this);
			this.delRow.setSize(new Dimension(80,35));
			this.delRow.setToolTipText("Deletes selected row(s) or last one.");

			this.tabSave= new JButton("Save");
			this.tabSave.setActionCommand("save");
			this.tabSave.addActionListener(this);
			this.tabSave.setSize(new Dimension(80,35));
			this.tabSave.setToolTipText("Saves current table to file.");
			this.tabLoad= new JButton("Load");
			this.tabLoad.setActionCommand("load");
			this.tabLoad.addActionListener(this);
			this.tabLoad.setSize(new Dimension(80,35));
			this.tabLoad.setToolTipText("Loads table from file.");
			this.tabClear= new JButton("Clear");
			this.tabClear.setActionCommand("clear");
			this.tabClear.addActionListener(this);
			this.tabClear.setSize(new Dimension(80,35));
			this.tabReset= new JButton("Reset");
			this.tabReset.setActionCommand("reset");
			this.tabReset.addActionListener(this);
			this.tabReset.setSize(new Dimension(80,35));
			JButton tabClose= new JButton("Close");
			tabClose.setActionCommand("close");
			tabClose.addActionListener(this);
			tabClose.setSize(new Dimension(80,35));
			JPanel tabToolPanel= new JPanel();
			tabToolPanel.setLayout(new FlowLayout());
			tabToolPanel.add(this.addRow);
			tabToolPanel.add(this.delRow);
			tabToolPanel.add(this.tabSave);
			tabToolPanel.add(this.tabLoad);
			tabToolPanel.add(this.tabClear);
			tabToolPanel.add(this.tabReset);
			tabToolPanel.add(tabClose);
			JToolBar tabTools= new JToolBar("");
			tabTools.add(tabToolPanel);
			
			this.tabPanel= new JPanel();
			this.tabPanel.setLayout(new BorderLayout());
			this.tabPanel.add(this.tableScroller, BorderLayout.CENTER);
			this.tabPanel.add(tabTools, BorderLayout.SOUTH);

			this.window.setSize(700, 250);
			String paramName = "<unknown>";
			if (this.descriptor != null)
				paramName = "<" + this.descriptor.getLabel() + ">"; 
			this.window.setTitle("Array data (" 
					+ this.elementClass.getSimpleName() 
					+ ") for parameter " + paramName);
			this.window.add(this.tabPanel);
			
			// ... then show it!
			this.window.setVisible(false);
		}
		
		/**
		 * Error function displaying an error message box.
		 * @param errorList List of errors to format into message.
		 * @throws ALDDataIOProviderException Thrown on processing failures.
		 */
		private void errorListToMessage(LinkedList<Integer> errorList) 
			throws ALDDataIOProviderException { 
			StringBuffer errMsg = new StringBuffer();
			errMsg.append(new String("Errors in table (indices = row):\n"));
			for (Integer i: errorList) {
				errMsg.append("Invalid entry at " + i + ".\n");
			}
			throw new ALDDataIOProviderException(
					ALDDataIOProviderExceptionType.SYNTAX_ERROR, errMsg.toString());
		}

		/* (non-Javadoc)
		 * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
		 */
    @Override
	  public void actionPerformed(ActionEvent e) {
			
			String command = e.getActionCommand();
			
			// deletes all data from the table and sets row and column counts to zero
			if (command.equals("clear")) {
				for (int i=0;i<this.dataTabModel.getRowCount();++i) {
					this.setTabEntryToDefault(i, this.elementClass);
					this.entryRefList[i] = this.dataTabModel.getValueAt(i, 0);
				}
			}
			// resets the table
			else if (e.getActionCommand().equals("reset")) {
				this.dataTabModel.setColumnCount(1);
				this.dataTabModel.setRowCount(1);
				this.entryRefList = new Object[1];
				this.disableValueChecks = true;
				this.setTabEntryToDefault(0, this.elementClass);
				this.entryRefList[0] = this.dataTabModel.getValueAt(0, 0);
				this.fireALDSwingValueChangeEvent(
						new ALDSwingValueChangeEvent(this, this.descriptor));
			}
			// closes the GUI window
			else if (e.getActionCommand().equals("close")) {
				this.window.setVisible(false);
			}
			// saves the table data in TSV format
			else if (e.getActionCommand().equals("save")) {
				this.saveTable();
			}		
			// load table data from file in TSV format
			else if (e.getActionCommand().equals("load")) {
				this.loadTable();
				this.fireALDSwingValueChangeEvent(
						new ALDSwingValueChangeEvent(this, this.descriptor));
			}		
			// add a row
			else if (command.equals("addRow")) {
				this.dataTabModel.addRow(new Vector<Object>());
				this.resizeTableEntries(this.dataTabModel.getRowCount());
				this.setTabEntryToDefault(this.dataTabModel.getRowCount()-1, 
																		this.elementClass);
				this.fireALDSwingValueChangeEvent(
					new ALDSwingValueChangeEvent(this, this.descriptor));
			}
			// delete row
			else if (command.equals("delRow")) {
				// one row should always remain...
				if (this.dataTabModel.getRowCount() == 1)
					return;
				// get and remove all selected rows
				int [] selectedRows = this.dataTab.getSelectedRows();
				// if none is selected, deleted last row
				if (selectedRows.length == 0) {
					this.dataTabModel.removeRow(this.dataTabModel.getRowCount()-1);
					this.resizeTableEntries(this.dataTabModel.getRowCount());
					this.fireALDSwingValueChangeEvent(
							new ALDSwingValueChangeEvent(this, this.descriptor));
					return;
				}
				for (int c = 0; c < selectedRows.length; ++c)
					this.dataTabModel.removeRow(this.dataTabModel.getRowCount()-1);
				// update the reference entry table and reset correct values in 
				// table
				int newSize = this.dataTabModel.getRowCount();
				Object[] newEntries = new Object[newSize];
				int newIndex = 0;
				for (int i=0;i<this.entryRefList.length;++i) {
					// check if row was deleted
					int r = 0;
					for (r = 0; r<selectedRows.length; r++) {
						if (i == selectedRows[r]) {
							break;
						}
					}
					if (r == selectedRows.length) {
						this.dataTabModel.setValueAt(this.entryRefList[i],newIndex,0);
						newEntries[newIndex] = this.entryRefList[i];
						++newIndex;
					}
				}
				this.entryRefList = newEntries;
				this.dataTab.clearSelection();
				this.fireALDSwingValueChangeEvent(
					new ALDSwingValueChangeEvent(this, this.descriptor));
			}
		}
		
		@Override
		public void tableChanged(TableModelEvent e) {
			if (!this.windowHasFocus)
				return;
			// check validity of entries
			if (this.elementClass != null) {
				int r = e.getFirstRow();
				int c = e.getColumn();
				if (c == -1)
					return;
				@SuppressWarnings("unchecked")
				Vector<Object> data = this.dataTabModel.getDataVector();
				int rows = data.size();
	      if (r >= rows)
	      	return;
				@SuppressWarnings("unchecked")
				String entry = 
					(String)((Vector<Object>)data.elementAt(r)).elementAt(0);
				if (   !this.disableValueChecks 
						&& !this.validateEntry(this.elementClass, entry)) {
					ProviderInteractionLevel plevel = 
						ALDDataIOManagerSwing.getInstance().
						getProviderInteractionLevel();
					if (   plevel.equals(ProviderInteractionLevel.ALL_ALLOWED)
							|| plevel.equals(ProviderInteractionLevel.WARNINGS_ONLY)) {
						Object[] options = { "OK" };
						JOptionPane.showOptionDialog(null, 
								"Attention! You just entered an invalid value that \n" +
										"cannot be cast to " 
										+ this.elementClass.getComponentType()
										+ " , please check your input!",
										"Warning", JOptionPane.DEFAULT_OPTION,
										JOptionPane.WARNING_MESSAGE,
										null, options, options[0]);
					}
					// reset table to old value
					this.dataTabModel.setValueAt(this.entryRefList[r], r, c);
				}
				this.disableValueChecks = false;
				this.entryRefList[r] = this.dataTabModel.getValueAt(r, 0);
				this.fireALDSwingValueChangeEvent(
					new ALDSwingValueChangeEvent(this, this.descriptor));
			}
		}

		/**
		 * Internal class that implements a {@link FileFilter} 
		 * for text files where MiToBo table data is stored.
		 *	
		 * @author moeller
		 */
		protected class DataTabFileFilter extends FileFilter {
			
			/* (non-Javadoc)
			 * @see javax.swing.filechooser.FileFilter#accept(java.io.File)
			 */
			@Override
			public boolean accept(File f) {
				return (f.getName().endsWith(".txt") || f.isDirectory());
			}
			
			/* (non-Javadoc)
			 * @see javax.swing.filechooser.FileFilter#getDescription()
			 */
			@Override
			public String getDescription() {
				return "Alida Data Table Files (*.txt)";
			}
		}
		
		/**
		 * Focus listener to commit edits if table looses the focus.
		 * @author moeller
		 */
		protected class TableFocusListener implements FocusListener {

			@Override
      public void focusGained(FocusEvent e) {
				ArrayConfigWindow.this.windowHasFocus = true;
				// nothing to do here, just ignored
      }

			@Override
      public void focusLost(FocusEvent e) {
				ArrayConfigWindow.this.windowHasFocus = false;
				if (   ArrayConfigWindow.this.dataTab == null
						|| ArrayConfigWindow.this.dataTab.getCellEditor() == null)
					return;
				ArrayConfigWindow.this.dataTab.getCellEditor().stopCellEditing();
				ArrayConfigWindow.this.fireALDSwingValueChangeEvent(
					new ALDSwingValueChangeEvent(this,
						ArrayConfigWindow.this.descriptor));
      }
		
		}
	}
}
