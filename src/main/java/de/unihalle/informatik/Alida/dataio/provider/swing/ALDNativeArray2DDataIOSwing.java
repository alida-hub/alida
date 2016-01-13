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
import de.unihalle.informatik.Alida.dataio.ALDDataIOManagerSwing.ProviderInteractionLevel;
import de.unihalle.informatik.Alida.dataio.provider.ALDDataIOSwingInitialGUIValueDefaultHandler;
import de.unihalle.informatik.Alida.dataio.provider.swing.components.ALDSwingComponent;
import de.unihalle.informatik.Alida.dataio.provider.swing.components.ALDTableWindow;
import de.unihalle.informatik.Alida.dataio.provider.swing.events.ALDSwingValueChangeEvent;
import de.unihalle.informatik.Alida.dataio.provider.swing.events.ALDSwingValueChangeListener;
import de.unihalle.informatik.Alida.dataio.provider.swing.events.ALDSwingValueChangeReporter;
import de.unihalle.informatik.Alida.exceptions.*;
import de.unihalle.informatik.Alida.exceptions.ALDDataIOProviderException.ALDDataIOProviderExceptionType;
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
 * Class for generic loading/saving of 2D-arrays from/to GUI in Alida.
 * 
 * @author moeller
 */
@ALDDataIOProvider
public class ALDNativeArray2DDataIOSwing 
	extends ALDDataIOSwingInitialGUIValueDefaultHandler {

	/**
	 * List of supported classes.
	 */
	private static LinkedList<Class<?>> classes = null;
	
	/**
	 * Default constructor.
	 */
	public ALDNativeArray2DDataIOSwing() {
		if (classes == null) {
			classes = new LinkedList<Class<?>>();
			ALDNativeArray2DDataIOSwing.classes.add( Boolean[][].class);
			ALDNativeArray2DDataIOSwing.classes.add( Byte[][].class);
			ALDNativeArray2DDataIOSwing.classes.add( Double[][].class);
			ALDNativeArray2DDataIOSwing.classes.add( Float[][].class);
			ALDNativeArray2DDataIOSwing.classes.add( Integer[][].class);
			ALDNativeArray2DDataIOSwing.classes.add( Short[][].class);
			ALDNativeArray2DDataIOSwing.classes.add( String[][].class);
			ALDNativeArray2DDataIOSwing.classes.add( boolean[][].class);
			ALDNativeArray2DDataIOSwing.classes.add( byte[][].class);
			ALDNativeArray2DDataIOSwing.classes.add( double[][].class);
			ALDNativeArray2DDataIOSwing.classes.add( float[][].class);
			ALDNativeArray2DDataIOSwing.classes.add( int[][].class);
			ALDNativeArray2DDataIOSwing.classes.add( short[][].class);
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
	 * Generic reading of 2D arrays.
	 */
	/* (non-Javadoc)
	 * @see de.unihalle.informatik.Alida.helpers.ALDDataIOSwing#createGUIElement(java.lang.Class, java.lang.Object)
	 */
	@Override
	public ArrayConfigPanel createGUIElement(Field field, Class<?> cl, 
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
		if (!(guiElement instanceof ArrayConfigPanel))
			throw new ALDDataIOProviderException(
					ALDDataIOProviderExceptionType.INVALID_GUI_ELEMENT, 
					"CollectionDataIO: setValue() received invalid GUI element!");
		if (value != null && !(classes.contains(value.getClass())))
			throw new ALDDataIOProviderException(
					ALDDataIOProviderExceptionType.INVALID_GUI_ELEMENT, 
					"CollectionDataIO: setValue() received wrong object type!");
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
					"CollectionDataIO: readData received invalid GUI element!");
		return ((ArrayConfigPanel)guiElement).readData(field, cl);
	}

	/* (non-Javadoc)
	 * @see de.unihalle.informatik.Alida.dataio.provider.ALDDataIOSwing#writeData(java.lang.Object, de.unihalle.informatik.Alida.operator.ALDParameterDescriptor)
	 */
	@Override
	public JComponent writeData(Object obj, ALDParameterDescriptor descr) 
			throws ALDDataIOProviderException {
		if (!(classes.contains(obj.getClass())))
			throw new ALDDataIOProviderException(
					ALDDataIOProviderExceptionType.OBJECT_TYPE_ERROR, 
					"NativeArray2DDataIO: object to write has wrong type!");
		// return a button to show a window with the elements
		return new ArrayShowButton(obj, descr);
	}

	/**
	 * GUI element for configuring collections.
	 * <p>
	 * This button has a collection configuration window attached to it 
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
		 * Button to open configuration window.
		 */
		private JButton confButton;
		
		/**
		 * Button to reset array.
		 */
		private JButton resetButton;

		/**
		 * Collection configuration window.
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
		 * @param field				Field of collection.
		 * @param cl					Class of collection.
		 * @param obj					Default object.
		 * @param descr				Optional descriptor for additional information.
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
		
		/**
		 * Gets the data from the configuration window.
		 * 
		 * @param field	Field of collection.
		 * @param cl		Class of collection.
		 * @param value	Default object value.
		 */
		public void setValue(Field field, Class<?> cl, Object value) {
			if (this.confWin != null)
				this.confWin.setValue(field, cl, value);
		}

		/**
		 * Gets the data from the configuration window.
		 * 
		 * @param field	Field of collection.
		 * @param cl	Class of collection.
		 * @return Current data.
		 * @throws ALDDataIOProviderException Thrown on read failure.
		 */
		public Object readData(Field field, Class<?> cl) 
			throws ALDDataIOProviderException {
			if (this.confWin == null)
				return null;
			return this.confWin.readData(field, cl);
		}

		/* (non-Javadoc)
		 * @see de.unihalle.informatik.Alida.dataio.provider.swing.components.ALDSwingComponent#getJComponent()
		 */
		@Override
		public JComponent getJComponent() {
			return this.configPanel;
		}

		/* (non-Javadoc)
		 * @see de.unihalle.informatik.Alida.dataio.provider.swing.events.ALDSwingValueChangeListener#handleValueChangeEvent(de.unihalle.informatik.Alida.dataio.provider.swing.events.ALDSwingValueChangeEvent)
		 */
		@Override
		public void handleValueChangeEvent(ALDSwingValueChangeEvent event) {
			this.fireALDSwingValueChangeEvent(event);
		}

		/* (non-Javadoc)
		 * @see de.unihalle.informatik.Alida.dataio.provider.swing.components.ALDSwingComponent#disableComponent()
		 */
		@Override
    public void disableComponent() {
			if (this.confWin != null)
				this.confWin.disableComponent();
    }

		/* (non-Javadoc)
		 * @see de.unihalle.informatik.Alida.dataio.provider.swing.components.ALDSwingComponent#enableComponent()
		 */
		@Override
    public void enableComponent() {
			if (this.confWin != null)
				this.confWin.enableComponent();
    }
		
		/**
		 * Disposes all resources of this window.
		 */
		/* (non-Javadoc)
		 * @see de.unihalle.informatik.Alida.dataio.provider.swing.components.ALDSwingComponent#dispose()
		 */
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
		 * @param obj			Object associated with this button/window.
		 * @param descr		Optional descriptor for additional information.
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
				if (cl.equals(Boolean[][].class)) {
					Boolean [][] field = (Boolean[][])this.data;
					int height = field.length;
					int width = field[0].length;
					outTabModel = new DefaultTableModel(height, width) {
						@Override
						public boolean isCellEditable(int row, int column) {
							return false;
						}};
					for (int h=0;h<height;++h) {
						for (int w=0;w<width;++w) {
							outTabModel.setValueAt(field[h][w], h, w);
						}
					}
				}
				else if (cl.equals(Byte[][].class)) {
					Byte [][] field = (Byte[][])this.data;
					int height = field.length;
					int width = field[0].length;
					outTabModel = new DefaultTableModel(height, width) {
						@Override
						public boolean isCellEditable(int row, int column) {
							return false;
						}};
					for (int h=0;h<height;++h) {
						for (int w=0;w<width;++w) {
							outTabModel.setValueAt(field[h][w], h, w);
						}
					}
				}
				else if (cl.equals(Double[][].class)) {
					Double [][] field = (Double[][])this.data;
					int height = field.length;
					int width = field[0].length;
					outTabModel = new DefaultTableModel(height, width) {
						@Override
						public boolean isCellEditable(int row, int column) {
							return false;
						}};
					for (int h=0;h<height;++h) {
						for (int w=0;w<width;++w) {
							outTabModel.setValueAt(field[h][w], h, w);
						}
					}
				}
				else if (cl.equals(Float[][].class)) {
					Float [][] field = (Float[][])this.data;
					int height = field.length;
					int width = field[0].length;
					outTabModel = new DefaultTableModel(height, width) {
						@Override
						public boolean isCellEditable(int row, int column) {
							return false;
						}};
					for (int h=0;h<height;++h) {
						for (int w=0;w<width;++w) {
							outTabModel.setValueAt(field[h][w], h, w);
						}
					}
				}
				else if (cl.equals(Integer[][].class)) {
					Integer [][] field = (Integer[][])this.data;
					int height = field.length;
					int width = field[0].length;
					outTabModel = new DefaultTableModel(height, width) {
						@Override
						public boolean isCellEditable(int row, int column) {
							return false;
						}};
					for (int h=0;h<height;++h) {
						for (int w=0;w<width;++w) {
							outTabModel.setValueAt(field[h][w], h, w);
						}
					}
				}
				else if (cl.equals(Short[][].class)) {
					Short [][] field = (Short[][])this.data;
					int height = field.length;
					int width = field[0].length;
					outTabModel = new DefaultTableModel(height, width) {
						@Override
						public boolean isCellEditable(int row, int column) {
							return false;
						}};
					for (int h=0;h<height;++h) {
						for (int w=0;w<width;++w) {
							outTabModel.setValueAt(field[h][w], h, w);
						}
					}
				}
				else if (cl.equals(String[][].class)) {
					String [][] field = (String[][])this.data;
					int height = field.length;
					int width = field[0].length;
					outTabModel = new DefaultTableModel(height, width) {
						@Override
						public boolean isCellEditable(int row, int column) {
							return false;
						}};
					for (int h=0;h<height;++h) {
						for (int w=0;w<width;++w) {
							outTabModel.setValueAt(field[h][w], h, w);
						}
					}
				}
				else if (cl.equals(boolean[][].class)) {
					boolean [][] field = (boolean[][])this.data;
					int height = field.length;
					int width = field[0].length;
					outTabModel = new DefaultTableModel(height, width) {
						@Override
						public boolean isCellEditable(int row, int column) {
							return false;
						}};
					for (int h=0;h<height;++h) {
						for (int w=0;w<width;++w) {
							outTabModel.setValueAt(new Boolean(field[h][w]), h, w);
						}
					}
				}
				else if (cl.equals(byte[][].class)) {
					byte [][] field = (byte[][])this.data;
					int height = field.length;
					int width = field[0].length;
					outTabModel = new DefaultTableModel(height, width) {
						@Override
						public boolean isCellEditable(int row, int column) {
							return false;
						}};
					for (int h=0;h<height;++h) {
						for (int w=0;w<width;++w) {
							outTabModel.setValueAt(new Byte(field[h][w]), h, w);
						}
					}
				}
				else if (cl.equals(double[][].class)) {
					double [][] field = (double[][])this.data;
					int height = field.length;
					int width = field[0].length;
					outTabModel = new DefaultTableModel(height, width) {
						@Override
						public boolean isCellEditable(int row, int column) {
							return false;
						}};
					for (int h=0;h<height;++h) {
						for (int w=0;w<width;++w) {
							outTabModel.setValueAt(new Double(field[h][w]), h, w);
						}
					}
				}
				else if (cl.equals(float[][].class)) {
					float [][] field = (float[][])this.data;
					int height = field.length;
					int width = field[0].length;
					outTabModel = new DefaultTableModel(height, width) {
						@Override
						public boolean isCellEditable(int row, int column) {
							return false;
						}};
					for (int h=0;h<height;++h) {
						for (int w=0;w<width;++w) {
							outTabModel.setValueAt(new Float(field[h][w]), h, w);
						}
					}					
				}
				else if (cl.equals(int[][].class)) {
					int [][] field = (int[][])this.data;
					int height = field.length;
					int width = field[0].length;
					outTabModel = new DefaultTableModel(height, width) {
						@Override
						public boolean isCellEditable(int row, int column) {
							return false;
						}};
					for (int h=0;h<height;++h) {
						for (int w=0;w<width;++w) {
							outTabModel.setValueAt(new Integer(field[h][w]), h, w);
						}
					}
				}
				else if (cl.equals(short[][].class)) {
					short [][] field = (short[][])this.data;
					int height = field.length;
					int width = field[0].length;
					outTabModel = new DefaultTableModel(height, width) {
						@Override
						public boolean isCellEditable(int row, int column) {
							return false;
						}};
					for (int h=0;h<height;++h) {
						for (int w=0;w<width;++w) {
							outTabModel.setValueAt(new Short(field[h][w]), h, w);
						}
					}
				}
				ALDTableWindow tabWin = 
						new ALDTableWindow(outTabModel, this.data); 
				String paramName = "<unknown>";
				if (this.descriptor != null)
					paramName = "<" + this.descriptor.getLabel() + ">";
				tabWin.setTitle("Result data (" + cl.getSimpleName() 
						+ ") for parameter " + paramName);
				tabWin.setSaveHeaders(false);
				tabWin.openWindow();
			}
    }
	}	

	/**
	 * Graphical user interface window for displaying 
	 * objects of class {@link DefaultTableModel}.
	 * 
	 * @author moeller
	 */
	private class ArrayConfigWindow extends ALDSwingValueChangeReporter 
		implements ActionListener, TableModelListener {

		/**
		 * Main window.
		 */
		private JFrame window;
		
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
		 * Button for adding a column.
		 */
		private JButton addCol;

		/**
		 * Button for adding a row.
		 */
		private JButton addRow;

		/**
		 * Button for deleting a column.
		 */
		private JButton delCol;

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
		 * Scrollpane for table panel;
		 */
		private JScrollPane tableScroller;
		
		/**
		 * For convenience: always open last directory for saving.
		 */
		private File lastDir;
		
		/**
		 * Class of objects to be filled in the table.
		 */
		private Class<?> entryClass;

		/**
		 * Optional descriptor for additional information on parameter.
		 */
		protected ALDParameterDescriptor descriptor;

		/**
		 * List of currently specified values (to restore entry if check 
		 * fails).
		 */
		private Object[][] entryRefList;
		
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
 		 * @param field 	Field of array.
		 * @param cl			Class of array elements.
		 * @param obj			Default object value.
		 * @param descr 	Descriptor associated with parameter.
		 */
		public ArrayConfigWindow(Field field, Class<?> cl, Object obj,
				ALDParameterDescriptor descr) {
			// instantiate the configuration window
			this.window = new JFrame();
			
			// remember object class
			this.entryClass = cl;
			
			// init dummy table... is overwritten if obj != null
			this.dataTabModel= new DefaultTableModel(1, 1);
			this.dataTabModel.addTableModelListener(this);
			this.entryRefList = new Object[1][1]; 

			// store additional descriptor
			this.descriptor = descr;

			// init current directory with user directory
			this.lastDir= new File(System.getProperty("user.dir"));

			// build table window
			this.setupTable();
			
			// if available, fill table with default values
			if (obj != null)
				this.setValue(field, cl, obj);
			// init table with the datatypes's default values
			else {
				for (int r = 0; r < this.dataTabModel.getRowCount(); ++r)
					for (int c = 0; c < this.dataTabModel.getColumnCount(); ++c)
						this.setTabEntryToDefault(r, c, cl);
				this.dataTabModel.addTableModelListener(this);
				this.dataTab.setModel(this.dataTabModel);
				this.tabPanel.updateUI();
			}
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
			if (this.addCol != null)
				this.addCol.setEnabled(false);
			if (this.addRow != null)
				this.addRow.setEnabled(false);
			if (this.delCol != null)
				this.delCol.setEnabled(false);
			if (this.delRow != null)
				this.delRow.setEnabled(false);
			if (this.tabClear != null)
				this.tabClear.setEnabled(false);
		}
		
		/**
		 * Enable table and buttons to allow for value changes.
		 */
		public void enableComponent() {
			if (this.dataTab != null)
				this.dataTab.setEnabled(true);
			if (this.addCol != null)
				this.addCol.setEnabled(true);
			if (this.addRow != null)
				this.addRow.setEnabled(true);
			if (this.delCol != null)
				this.delCol.setEnabled(true);
			if (this.delRow != null)
				this.delRow.setEnabled(true);
			if (this.tabClear != null)
				this.tabClear.setEnabled(true);		
		}

		/**
		 * Disposes all resources of this window.
		 */
		public void dispose() {
			this.window.dispose();
		}

		/**
		 * Sets the specified table entry to the class default.
		 * @param row		Row index.
		 * @param col		Column index.
		 * @param cl		Class of table elements.
		 */
		protected void setTabEntryToDefault(int row, int col, Class<?> cl) {
			if (cl.equals(boolean[][].class)) {
				boolean tmpBool = false;
				this.dataTabModel.setValueAt(Boolean.toString(tmpBool),row,col);
			}
			else if (cl.equals(byte[][].class)) {
				byte tmpByte = 0;
				this.dataTabModel.setValueAt(Byte.toString(tmpByte), row, col);	
			}
			else if (cl.equals(double[][].class)) {
				double tmpDouble = 0;
				this.dataTabModel.setValueAt(Double.toString(tmpDouble),row,col);
			}
			else if (cl.equals(float[][].class)) {
				float tmpFloat = 0;
				this.dataTabModel.setValueAt(Float.toString(tmpFloat), row, col);
			}
			else if (cl.equals(int[][].class)) {
				int tmpInt = 0;
				this.dataTabModel.setValueAt(Integer.toString(tmpInt), row, col);
			}
			else if (cl.equals(short[][].class)) {
				short tmpShort = 0;
				this.dataTabModel.setValueAt(Short.toString(tmpShort), row, col);
			}
			else if (cl.equals(Boolean[][].class)) {
				Boolean tmpBool = new Boolean(false);
				this.dataTabModel.setValueAt(tmpBool.toString(), row, col);
			}
			else if (cl.equals(Byte[][].class)) {
				Byte tmpByte = new Byte((byte)0);
				this.dataTabModel.setValueAt(tmpByte.toString(), row, col);	
			}
			else if (cl.equals(Double[][].class)) {
				Double tmpDouble = new Double(0);
				this.dataTabModel.setValueAt(tmpDouble.toString(), row, col);
			}
			else if (cl.equals(Float[][].class)) {
				Float tmpFloat = new Float(0);
				this.dataTabModel.setValueAt(tmpFloat.toString(), row, col);
			}
			else if (cl.equals(Integer[][].class)) {
				Integer tmpInt = new Integer(0);
				this.dataTabModel.setValueAt(tmpInt.toString(), row, col);
			}
			else if (cl.equals(Short[][].class)) {
				Short tmpShort = new Short((short)0);
				this.dataTabModel.setValueAt(tmpShort.toString(), row, col);
			}
			else if (cl.equals(String[][].class)) {
				this.dataTabModel.setValueAt(new String(), row, col);
			}
			// store the value for later value checking
			this.entryRefList[row][col] = this.dataTabModel.getValueAt(row,col);
		}

    /**
     * Set value of parameter in GUI element.
     * @param field	Field of parameter.
     * @param cl		Class of parameter.
     * @param value	Value to be set in GUI.
     */
		public void setValue(@SuppressWarnings("unused") Field field, 
				Class<?> cl, Object value) {
    	// nothing to be done if value is null
    	if (value == null)
    		return;
			if (   cl.equals(Boolean[][].class)
					|| cl.equals(Byte[][].class)
					|| cl.equals(Double[][].class)
					|| cl.equals(Float[][].class)
					|| cl.equals(Integer[][].class)
					|| cl.equals(Short[][].class)
					|| cl.equals(String[][].class)) {
				Object [][] array  = (Object[][])value;
				int rows = array.length;
				int cols = array[0].length;
				this.dataTabModel = new DefaultTableModel(rows,cols);
				this.entryRefList = new Object[rows][cols];
				for (int r=0;r<rows;++r) {
					for (int c=0;c<cols;++c) {
						Object val = array[r][c];
						String entry = new String();
						if (val != null)
							entry = val.toString();
						this.dataTabModel.setValueAt(entry, r, c);
						this.entryRefList[r][c] = this.dataTabModel.getValueAt(r,c);
					}
				}
			}
			else if (cl.equals(boolean[][].class)) {
				boolean [][] array = (boolean[][])value;
				int rows = array.length;
				int cols = array[0].length;
				this.dataTabModel = new DefaultTableModel(rows,cols);
				this.entryRefList = new Object[rows][cols];
				for (int r=0;r<rows;++r) {
					for (int c=0;c<cols;++c) {
						this.dataTabModel.setValueAt(Boolean.toString(array[r][c]), 
								r, c);
						this.entryRefList[r][c] = this.dataTabModel.getValueAt(r,c);
					}
				}
			}
			else if (cl.equals(byte[][].class)) {
				byte [][] array = (byte[][])value;
				int rows = array.length;
				int cols = array[0].length;
				this.dataTabModel = new DefaultTableModel(rows,cols);
				this.entryRefList = new Object[rows][cols];
				for (int r=0;r<rows;++r) {
					for (int c=0;c<cols;++c) {
						this.dataTabModel.setValueAt(Byte.toString(array[r][c]), 
								r, c);
						this.entryRefList[r][c] = this.dataTabModel.getValueAt(r,c);
					}
				}
			}
			else if (cl.equals(double[][].class)) {
				double [][] array = (double[][])value;
				int rows = array.length;
				int cols = array[0].length;
				this.dataTabModel = new DefaultTableModel(rows,cols);
				this.entryRefList = new Object[rows][cols];
				for (int r=0;r<rows;++r) {
					for (int c=0;c<cols;++c) {
						this.dataTabModel.setValueAt(Double.toString(array[r][c]), 
								r, c);
						this.entryRefList[r][c] = this.dataTabModel.getValueAt(r,c);
					}
				}
			}
			else if (cl.equals(float[][].class)) {
				float [][] array = (float[][])value;
				int rows = array.length;
				int cols = array[0].length;
				this.dataTabModel = new DefaultTableModel(rows,cols);
				this.entryRefList = new Object[rows][cols];
				for (int r=0;r<rows;++r) {
					for (int c=0;c<cols;++c) {
						this.dataTabModel.setValueAt(Float.toString(array[r][c]), 
								r, c);
						this.entryRefList[r][c] = this.dataTabModel.getValueAt(r,c);
					}
				}
			}
			else if (cl.equals(int[][].class)) {
				int [][] array = (int[][])value;
				int rows = array.length;
				int cols = array[0].length;
				this.dataTabModel = new DefaultTableModel(rows,cols);
				this.entryRefList = new Object[rows][cols];
				for (int r=0;r<rows;++r) {
					for (int c=0;c<cols;++c) {
						this.dataTabModel.setValueAt(Integer.toString(array[r][c]), 
								r, c);
						this.entryRefList[r][c] = this.dataTabModel.getValueAt(r,c);
					}
				}
			}
			else if (cl.equals(short[][].class)) {
				short [][] array = (short[][])value;
				int rows = array.length;
				int cols = array[0].length;
				this.dataTabModel = new DefaultTableModel(rows,cols);
				this.entryRefList = new Object[rows][cols];
				for (int r=0;r<rows;++r) {
					for (int c=0;c<cols;++c) {
						this.dataTabModel.setValueAt(Short.toString(array[r][c]), 
								r, c);
						this.entryRefList[r][c] = this.dataTabModel.getValueAt(r,c);
					}
				}
			}
			// update the view
			this.dataTabModel.addTableModelListener(this);
			this.dataTab.setModel(this.dataTabModel);
			this.tabPanel.updateUI();
		}

		/**
		 * Read parameter value from graphical user interface. 
		 * @param field		Field of parameter to be read.
		 * @param cl			Class of parameter to be read.
		 * @return	Value of parameter in GUI.
		 * @throws ALDDataIOProviderException	Thrown in case of read failures.
		 */
		@SuppressWarnings({ "unchecked" })
		public Object readData(@SuppressWarnings("unused") Field field, 
				Class<?> cl) 
			throws ALDDataIOProviderException {
//			this.dataTab.editingStopped(null);
//			this.dataTabModel.fireTableDataChanged();
			
			// check if there is already data to check
			if (this.dataTabModel == null)
				return null;
			
			// list for collecting errors
			LinkedList<int[]> errors = new LinkedList<int[]>();

			// read data
			Vector<Object> data = this.dataTabModel.getDataVector();
			int rows = data.size();
      int cols = ((Vector<Object>)(data.elementAt(0))).size();
			Object entry;
			if (cl.equals(Boolean[][].class)) {
				Boolean [][] tmpfield  = new Boolean[rows][cols];
				for (int r=0;r<rows;++r) {
					for (int c=0;c<cols;++c) {
						entry = ((Vector<Object>)data.elementAt(r)).elementAt(c);
						if (entry == null || ((String)entry).isEmpty())
							errors.add(new int[]{r,c});
						try {
							tmpfield[r][c] = (entry != null) ?
									Boolean.valueOf((String)(entry)) : null;
						} catch (NumberFormatException ne) {
							errors.add(new int[]{r,c});
						}
					}
				}
				if (errors.size() > 0)
					this.errorListToMessage(errors);
				return tmpfield;
			}
			if (cl.equals(Byte[][].class)) {
				Byte [][] tmpfield  = new Byte[rows][cols];
				for (int r=0;r<rows;++r) {
					for (int c=0;c<cols;++c) {
						entry = ((Vector<Object>)data.elementAt(r)).elementAt(c);
						if (entry == null || ((String)entry).isEmpty())
							errors.add(new int[]{r,c});
						try {
							tmpfield[r][c] = (entry != null) ?
									Byte.valueOf((String)(entry)) : null;
						} catch (NumberFormatException ne) {
							errors.add(new int[]{r,c});
						}
					}
				}
				if (errors.size() > 0)
					this.errorListToMessage(errors);
				return tmpfield;
			}
			if (cl.equals(Double[][].class)) {
				Double [][] tmpfield  = new Double[rows][cols];
				for (int r=0;r<rows;++r) {
					for (int c=0;c<cols;++c) {
						entry = ((Vector<Object>)data.elementAt(r)).elementAt(c);
						if (entry == null || ((String)entry).isEmpty())
							errors.add(new int[]{r,c});
						try {
							tmpfield[r][c] = (entry != null) ?
									Double.valueOf((String)(entry)) : null;
						} catch (NumberFormatException ne) {
							errors.add(new int[]{r,c});
						}
					}
				}
				if (errors.size() > 0)
					this.errorListToMessage(errors);
				return tmpfield;
			}
			if (cl.equals(Float[][].class)) {
				Float [][] tmpfield  = new Float[rows][cols];
				for (int r=0;r<rows;++r) {
					for (int c=0;c<cols;++c) {
						entry = ((Vector<Object>)data.elementAt(r)).elementAt(c);
						if (entry == null || ((String)entry).isEmpty())
							errors.add(new int[]{r,c});
						try {
							tmpfield[r][c] = (entry != null) ?
									Float.valueOf((String)(entry)) : null;
						} catch (NumberFormatException ne) {
							errors.add(new int[]{r,c});
						}
					}
				}
				if (errors.size() > 0)
					this.errorListToMessage(errors);
				return tmpfield;		
			}
			if (cl.equals(Integer[][].class)) {
				Integer [][] tmpfield  = new Integer[rows][cols];
				for (int r=0;r<rows;++r) {
					for (int c=0;c<cols;++c) {
						entry = ((Vector<Object>)data.elementAt(r)).elementAt(c);
						if (entry == null || ((String)entry).isEmpty())
							errors.add(new int[]{r,c});
						try {
							tmpfield[r][c] = (entry != null) ?
									Integer.valueOf((String)(entry)) : null;
						} catch (NumberFormatException ne) {
							errors.add(new int[]{r,c});
						}
					}
				}
				if (errors.size() > 0)
					this.errorListToMessage(errors);
				return tmpfield;
			}
			if (cl.equals(Short[][].class)) {
				Short [][] tmpfield  = new Short[rows][cols];
				for (int r=0;r<rows;++r) {
					for (int c=0;c<cols;++c) {
						entry = ((Vector<Object>)data.elementAt(r)).elementAt(c);
						if (entry == null || ((String)entry).isEmpty())
							errors.add(new int[]{r,c});
						try {
							tmpfield[r][c] = (entry != null) ?
									Short.valueOf((String)(entry)) : null;
						} catch (NumberFormatException ne) {
							errors.add(new int[]{r,c});
						}
					}
				}
				if (errors.size() > 0)
					this.errorListToMessage(errors);
				return tmpfield;				
			}
			if (cl.equals(String[][].class)) {
				String [][] tmpfield  = new String[rows][cols];
				for (int r=0;r<rows;++r) {
					for (int c=0;c<cols;++c) {
						entry = ((Vector<Object>)data.elementAt(r)).elementAt(c);
						if (entry == null) // || ((String)entry).isEmpty())
							errors.add(new int[]{r,c});
						try {
//							tmpfield[r][c] = (entry != null) ? (String)entry : null;
							tmpfield[r][c] = (String)entry;
						} catch (NumberFormatException ne) {
							errors.add(new int[]{r,c});
						}
					}
				}
				if (errors.size() > 0)
					this.errorListToMessage(errors);
				return tmpfield;
			}
			if (cl.equals(boolean[][].class)) {
				boolean [][] tmpfield  = new boolean[rows][cols];
				for (int r=0;r<rows;++r) {
					for (int c=0;c<cols;++c) {
						entry = ((Vector<Object>)data.elementAt(r)).elementAt(c);
						if (entry == null || ((String)entry).isEmpty())
							errors.add(new int[]{r,c});
						try {
							tmpfield[r][c] = (entry != null) ?
									Boolean.valueOf((String)(entry)).booleanValue():false;
						} catch (NumberFormatException ne) {
							errors.add(new int[]{r,c});
						}
					}
				}
				if (errors.size() > 0)
					this.errorListToMessage(errors);
				return tmpfield;
			}
			if (cl.equals(byte[][].class)) {
				byte [][] tmpfield  = new byte[rows][cols];
				for (int r=0;r<rows;++r) {
					for (int c=0;c<cols;++c) {
						entry = ((Vector<Object>)data.elementAt(r)).elementAt(c);
						if (entry == null || ((String)entry).isEmpty())
							errors.add(new int[]{r,c});
						try {
							tmpfield[r][c] = (entry != null) ?
									Byte.valueOf((String)(entry)).byteValue() : 0;
						} catch (NumberFormatException ne) {
							errors.add(new int[]{r,c});
						}
					}
				}
				if (errors.size() > 0)
					this.errorListToMessage(errors);
				return tmpfield;
			}
			if (cl.equals(double[][].class)) {
				double [][] tmpfield  = new double[rows][cols];
				for (int r=0;r<rows;++r) {
					for (int c=0;c<cols;++c) {
						entry = ((Vector<Object>)data.elementAt(r)).elementAt(c);
						if (entry == null || ((String)entry).isEmpty())
							errors.add(new int[]{r,c});
						try {
							tmpfield[r][c] = (entry != null) ?
									Double.valueOf((String)(entry)).doubleValue() : 0.0;
						} catch (NumberFormatException ne) {
							errors.add(new int[]{r,c});
						}
					}
				}
				if (errors.size() > 0)
					this.errorListToMessage(errors);
				return tmpfield;
			}
			if (cl.equals(float[][].class)) {
				float [][] tmpfield  = new float[rows][cols];
				for (int r=0;r<rows;++r) {
					for (int c=0;c<cols;++c) {
						entry = ((Vector<Object>)data.elementAt(r)).elementAt(c);
						if (entry == null || ((String)entry).isEmpty())
							errors.add(new int[]{r,c});
						try {
							tmpfield[r][c] = (entry != null) ?
									Float.valueOf((String)(entry)).floatValue() : 0;
						} catch (NumberFormatException ne) {
							errors.add(new int[]{r,c});
						}
					}
				}
				if (errors.size() > 0)
					this.errorListToMessage(errors);
				return tmpfield;
			}
			if (cl.equals(int[][].class)) {
				int [][] tmpfield  = new int[rows][cols];
				for (int r=0;r<rows;++r) {
					for (int c=0;c<cols;++c) {
						entry = ((Vector<Object>)data.elementAt(r)).elementAt(c);
						if (entry == null || ((String)entry).isEmpty())
							errors.add(new int[]{r,c});
						try {
							tmpfield[r][c] = (entry != null) ?
								Integer.valueOf((String)(entry)).intValue() : 0;
						} catch (NumberFormatException ne) {
							errors.add(new int[]{r,c});
						}
					}
				}
				if (errors.size() > 0)
					this.errorListToMessage(errors);
				return tmpfield;
			}
			if (cl.equals(short[][].class)) {
				short [][] tmpfield  = new short[rows][cols];
				for (int r=0;r<rows;++r) {
					for (int c=0;c<cols;++c) {
						entry = ((Vector<Object>)data.elementAt(r)).elementAt(c);
						if (entry == null || ((String)entry).isEmpty())
							errors.add(new int[]{r,c});
						try {
							tmpfield[r][c] = (entry != null) ?
									Short.valueOf((String)(entry)).shortValue() : 0;
						} catch (NumberFormatException ne) {
							errors.add(new int[]{r,c});
						}
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
     * @param newRows		New number of rows for the table.
     * @param newCols		New number of cols for the table.
     */
    protected void resizeTableEntries(int newRows, int newCols) {
    	Object[][] newEntries = new Object[newRows][newCols];
    	int commonRows = (newRows > this.entryRefList.length ? 
    			this.entryRefList.length : newRows);
    	int commonCols;
    	for (int r=0;r<commonRows;++r) {
    		commonCols = (newCols > this.entryRefList[r].length ? 
						this.entryRefList[r].length : newCols);
    		for (int c=0;c<commonCols;++c) {
    		 		newEntries[r][c] = this.entryRefList[r][c];
    		}
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
			if (cl.equals(String[][].class)) {
				return true;
			}
			if (entry == null || entry.isEmpty())
				return false;
			if (cl.equals(Boolean[][].class)) {
				try {
					Boolean t = Boolean.valueOf((entry));
				} catch (NumberFormatException ne) {
					return false;
				}
			}
			if (cl.equals(Byte[][].class)) {
				try {
					Byte t = Byte.valueOf((entry));
				} catch (NumberFormatException ne) {
					return false;
				}
			}
			if (cl.equals(Double[][].class)) {
				try {
					Double t = Double.valueOf((entry));
				} catch (NumberFormatException ne) {
					return false;
				}
			}
			if (cl.equals(Float[][].class)) {
				try {
					Float t = Float.valueOf((entry));
				} catch (NumberFormatException ne) {
					return false;
				}
			}
			if (cl.equals(Integer[][].class)) {
				try {
					Integer t = Integer.valueOf((entry));
				} catch (NumberFormatException ne) {
					return false;
				}
			}
			if (cl.equals(Short[][].class)) {
				try {
					Short t = Short.valueOf((entry));
				} catch (NumberFormatException ne) {
					return false;
				}
			}
			if (cl.equals(boolean[][].class)) {
				try {
					boolean t = Boolean.valueOf((entry)).booleanValue();
				} catch (NumberFormatException ne) {
					return false;
				}	
			}
			if (cl.equals(byte[][].class)) {
				try {
					byte t = Byte.valueOf((entry)).byteValue();
				} catch (NumberFormatException ne) {
					return false;
				}
			}
			if (cl.equals(double[][].class)) {
				try {
					double t = Double.valueOf((entry)).doubleValue();
				} catch (NumberFormatException ne) {
					return false;
				}
			}
			if (cl.equals(float[][].class)) {
				try {
					float t = Float.valueOf((entry)).floatValue();
				} catch (NumberFormatException ne) {
					return false;
				}
			}
			if (cl.equals(int[][].class)) {
				try {
					int t = Integer.valueOf((entry)).intValue();
				} catch (NumberFormatException ne) {
					return false;
				}
			}
			if (cl.equals(short[][].class)) {
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
									"The file exists already, "
									+ "would you like to overwrite it?", "Warning",
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
				FileWriter ow = new FileWriter(file.getPath());
				StringBuffer [] tab = 
						ALDTableWindow.tableToString(this.dataTabModel);
				// note: first row contains the header which is meaningless in 
				//       this context, therefore it is ignored
				System.out.println(tab[0].toString());
				for (int i=1;i<tab.length;++i) {
					ow.write(tab[i].toString());
				}
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
				Vector<String[]> entries = new Vector<String[]>();
				String line = br.readLine();
				while( line != null ) {
					entries.add(line.split("\t"));
					line = br.readLine();
				}
				int colNum = entries.elementAt(0).length;
				Class<?> cl = this.entryClass;
				if (   cl.equals(Boolean[][].class)
						|| cl.equals(Byte[][].class)
						|| cl.equals(Double[][].class)
						|| cl.equals(Float[][].class)
						|| cl.equals(Integer[][].class)
						|| cl.equals(Short[][].class)
						|| cl.equals(String[][].class)) {
					Object[][] tableData = new Object[entries.size()][colNum];
					for (int i=0;i<entries.size(); ++i) {
						for (int j=0;j<colNum; ++j) {
							tableData[i][j] = entries.elementAt(i)[j];
						}
					}
					this.setValue(null, this.entryClass, tableData);
				}
				else if (cl.equals(boolean[][].class)) {
					boolean[][] tableData = new boolean[entries.size()][colNum];
					for (int i=0;i<entries.size(); ++i) {
						for (int j=0;j<colNum; ++j) {
							tableData[i][j] = 
								new Boolean(entries.elementAt(i)[j]).booleanValue();
						}
					}
					this.setValue(null, this.entryClass, tableData);
				}
				else if (cl.equals(byte[][].class)) {
					byte[][] tableData = new byte[entries.size()][colNum];
					for (int i=0;i<entries.size(); ++i) {
						for (int j=0;j<colNum; ++j) {
							tableData[i][j] = 
								new Byte(entries.elementAt(i)[j]).byteValue();
						}
					}
					this.setValue(null, this.entryClass, tableData);
				}
				else if (cl.equals(double[][].class)) {
					double [][] tableData = new double[entries.size()][colNum];
					for (int i=0;i<entries.size(); ++i) {
						for (int j=0;j<colNum; ++j) {
							tableData[i][j] = 
								new Double(entries.elementAt(i)[j]).doubleValue();
						}
					}
					this.setValue(null, this.entryClass, tableData);
				}
				else if (cl.equals(float[][].class)) {
					float[][] tableData = new float[entries.size()][colNum];
					for (int i=0;i<entries.size(); ++i) {
						for (int j=0;j<colNum; ++j) {
							tableData[i][j] = 
								new Float(entries.elementAt(i)[j]).floatValue();
						}
					}
					this.setValue(null, this.entryClass, tableData);
				}
				else if (cl.equals(int[][].class)) {
					int[][] tableData = new int[entries.size()][colNum];
					for (int i=0;i<entries.size(); ++i) {
						for (int j=0;j<colNum; ++j) {
							tableData[i][j] = 
								new Integer(entries.elementAt(i)[j]).intValue();
						}
					}
					this.setValue(null, this.entryClass, tableData);
				}
				else if (cl.equals(short[][].class)) {
					short[][] tableData = new short[entries.size()][colNum];
					for (int i=0;i<entries.size(); ++i) {
						for (int j=0;j<colNum; ++j) {
							tableData[i][j] = 
								new Short(entries.elementAt(i)[j]).shortValue();
						}
					}
					this.setValue(null, this.entryClass, tableData);
				}
				br.close();
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
			this.tableScroller.setPreferredSize(new Dimension (400,300));
			this.dataTab.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);

			// add a toolbar
			this.addCol= new JButton("Add Column");
			this.addCol.setActionCommand("addCol");
			this.addCol.addActionListener(this);
			this.addCol.setSize(new Dimension(80,35));
			this.addCol.setToolTipText("Appends a column to the table.");
			this.addRow= new JButton("Add Row");
			this.addRow.setActionCommand("addRow");
			this.addRow.addActionListener(this);
			this.addRow.setSize(new Dimension(80,35));
			this.addRow.setToolTipText("Appends a row to the table.");
			this.delCol= new JButton("Delete Last Column");
			this.delCol.setActionCommand("delCol");
			this.delCol.addActionListener(this);
			this.delCol.setSize(new Dimension(80,35));
			this.delCol.setToolTipText("Deletes the last column.");
			this.delRow= new JButton("Delete Row");
			this.delRow.setActionCommand("delRow");
			this.delRow.setToolTipText("Deletes selected row(s) or last " + 
					"one if nothing is selected.");
			this.delRow.addActionListener(this);
			this.delRow.setSize(new Dimension(80,35));

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
			this.tabClear.setToolTipText("Resets table to default entries.");
			JButton tabClose= new JButton("Close");
			tabClose.setActionCommand("close");
			tabClose.addActionListener(this);
			tabClose.setSize(new Dimension(80,35));
			JPanel tabToolPanel= new JPanel();
			tabToolPanel.setLayout(new FlowLayout());
			tabToolPanel.add(this.addCol);
			tabToolPanel.add(this.addRow);
			tabToolPanel.add(this.delCol);
			tabToolPanel.add(this.delRow);
			tabToolPanel.add(this.tabSave);
			tabToolPanel.add(this.tabLoad);
			tabToolPanel.add(this.tabClear);
			tabToolPanel.add(tabClose);
			JToolBar tabTools= new JToolBar("");
			tabTools.add(tabToolPanel);
			
			this.tabPanel= new JPanel();
			this.tabPanel.setLayout(new BorderLayout());
			this.tabPanel.add(this.tableScroller, BorderLayout.CENTER);
			this.tabPanel.add(tabTools, BorderLayout.SOUTH);

			this.window.setSize(900, 250);
			String paramName = "<unknown>";
			if (this.descriptor != null)
				paramName = "<" + this.descriptor.getLabel() + ">"; 
			this.window.setTitle("Array data (" 
				+ this.entryClass.getSimpleName()
				+ ") for parameter " + paramName);
			this.window.add(this.tabPanel);
			
			// ... then show it!
			this.window.setVisible(false);
		}
		
		/**
		 * Error function displaying an error message box.
		 * @param errorList List of errors.
		 * @throws ALDDataIOProviderException 
		 * 		Thrown in case of processing errors.
		 */
		private void errorListToMessage(LinkedList<int[]> errorList) 
			throws ALDDataIOProviderException { 
//			Object[] options = { "OK" };
//			JOptionPane.showOptionDialog(null, 
//					"Attention! Java Swing/Awt problem, some of your inputs \n" +
//					"may not have been saved correctly - please check again!\n"+
//					"Table entry at position ( " + r + " , " + c + " )" +
//					" is null! Returning null object...",	"Warning",
//					JOptionPane.DEFAULT_OPTION, JOptionPane.WARNING_MESSAGE,
//					null, options, options[0]);
			StringBuffer errMsg = new StringBuffer();
			errMsg.append("Errors in table (indices = row , col):\n");
			for (int[] ee: errorList) {
				errMsg.append("Invalid entry at " + ee[0]+ ", " + ee[1]+ ".\n");
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
			if (e.getActionCommand().equals("clear")) {
				for (int i=0;i<this.dataTabModel.getRowCount();++i) {
					for (int j=0;j<this.dataTabModel.getColumnCount();++j) {
						this.setTabEntryToDefault(i, j, this.entryClass);
						this.entryRefList[i][j] = this.dataTabModel.getValueAt(i, j);
					}
				}
			}
			// closes the GUI window
			if (e.getActionCommand().equals("close")) {
//				this.dataTab.editingStopped(null);
//				this.dataTabModel.fireTableDataChanged();
				this.window.setVisible(false);
			}
			// saves the table data in TSV format
			if (e.getActionCommand().equals("save")) {
				this.saveTable();
			}		
			// load table data from file in TSV format
			else if (e.getActionCommand().equals("load")) {
				this.loadTable();
				this.fireALDSwingValueChangeEvent(
						new ALDSwingValueChangeEvent(this, this.descriptor));
			}		
			// add a column
			if (command.equals("addCol")) {
				this.dataTabModel.addColumn(null);
				this.resizeTableEntries(this.dataTabModel.getRowCount(),
						this.dataTabModel.getColumnCount());
				for (int j=0;j<this.dataTabModel.getRowCount();++j)
					this.setTabEntryToDefault(j,
							this.dataTabModel.getColumnCount()-1,	this.entryClass);
				this.fireALDSwingValueChangeEvent(
						new ALDSwingValueChangeEvent(this, this.descriptor));
			}
			// add a row
			if (command.equals("addRow")) {
				this.dataTabModel.addRow(new Vector<Object>());
				this.resizeTableEntries(this.dataTabModel.getRowCount(),
						this.dataTabModel.getColumnCount());
				for (int j=0;j<this.dataTabModel.getColumnCount();++j)
					this.setTabEntryToDefault(this.dataTabModel.getRowCount()-1, 
							j, this.entryClass);
				this.fireALDSwingValueChangeEvent(
						new ALDSwingValueChangeEvent(this, this.descriptor));
			}
			// delete column
			if (command.equals("delCol")) {
				if (this.dataTabModel.getColumnCount() == 1)
					return;
				this.dataTabModel.setColumnCount(
						this.dataTabModel.getColumnCount()-1);
				this.resizeTableEntries(this.dataTabModel.getRowCount(),
						this.dataTabModel.getColumnCount());
				this.fireALDSwingValueChangeEvent(
					new ALDSwingValueChangeEvent(this, this.descriptor));
			}
			// delete row
			if (command.equals("delRow")) {
				if (this.dataTabModel.getRowCount() == 1)
					return;
				// get and remove all selected rows
				int [] selectedRows = this.dataTab.getSelectedRows();
				if (selectedRows.length == 0) {
					this.dataTabModel.removeRow(this.dataTabModel.getRowCount()-1);
					this.resizeTableEntries(this.dataTabModel.getRowCount(), 
																		this.dataTabModel.getColumnCount());
					this.fireALDSwingValueChangeEvent(
						new ALDSwingValueChangeEvent(this, this.descriptor));
					return;
				}
				for (int c = 0; c < selectedRows.length; ++c)
					this.dataTabModel.removeRow(this.dataTabModel.getRowCount()-1);
				// update the reference entry table and reset correct values 
				// in table
				int newSize = this.dataTabModel.getRowCount();
				Object[][] newEntries = 
						new Object[newSize][this.dataTabModel.getColumnCount()];
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
						for (int c = 0; c<this.dataTabModel.getColumnCount(); ++c) {
							this.dataTabModel.setValueAt(this.entryRefList[i][c],
									newIndex, c);
							newEntries[newIndex][c] = this.entryRefList[i][c];
						}
						++newIndex;
					}
				}
				this.entryRefList = newEntries;
				this.dataTab.clearSelection();
				this.fireALDSwingValueChangeEvent(
					new ALDSwingValueChangeEvent(this, this.descriptor));
			}
		}

    @SuppressWarnings("unchecked")
    @Override
		public void tableChanged(TableModelEvent e) {
			if (!this.windowHasFocus)
				return;
			int r = e.getFirstRow();
    	int c = e.getColumn();
			Vector<Object> data = this.dataTabModel.getDataVector();
			int rows = data.size();
      int cols = ((Vector<Object>)(data.elementAt(0))).size();
      if (r < 0 || c < 0 || r >= rows || c >= cols)
      	return;
			String entry = 
					(String)((Vector<Object>)data.elementAt(r)).elementAt(c);
    	if (this.validateEntry(this.entryClass, entry))
    		this.fireALDSwingValueChangeEvent(
    			new ALDSwingValueChangeEvent(this, this.descriptor));
    	else {
    		ProviderInteractionLevel plevel = 
    				ALDDataIOManagerSwing.getInstance().
    				getProviderInteractionLevel();
    		if (   plevel.equals(ProviderInteractionLevel.ALL_ALLOWED)
    				|| plevel.equals(ProviderInteractionLevel.WARNINGS_ONLY)) {
    			JOptionPane.showOptionDialog(null,
    					"Attention! You just entered an invalid value!\n" +
    					"The table entry at position ( " + r + " , " + c + " )" +
    					" cannot be cast to class " + 
								this.entryClass.getComponentType().getComponentType() 
								+ "!", "Warning - invalid value", 
    					JOptionPane.DEFAULT_OPTION, JOptionPane.WARNING_MESSAGE,
    					null, new Object[]{"OK"}, "OK");
    		}
				// reset table to old value
				this.dataTabModel.setValueAt(this.entryRefList[r][c], r, c);
    	}
			this.entryRefList[r][c] = this.dataTabModel.getValueAt(r, c);
			this.fireALDSwingValueChangeEvent(
				new ALDSwingValueChangeEvent(this, this.descriptor));
		}

    /**
		 * Internal class that realizes a FileFilter for text
		 * files where MiToBo table data is stored.
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
				return "MiToBo Data Table Files (*.txt)";
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
