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
import de.unihalle.informatik.Alida.dataio.provider.ALDDataIOSwing;
import de.unihalle.informatik.Alida.dataio.provider.swing.components.ALDSwingComponent;
import de.unihalle.informatik.Alida.dataio.provider.swing.components.ALDSwingComponentTextField;
import de.unihalle.informatik.Alida.dataio.provider.swing.events.ALDSwingValueChangeEvent;
import de.unihalle.informatik.Alida.dataio.provider.swing.events.ALDSwingValueChangeListener;
import de.unihalle.informatik.Alida.datatypes.ALDDirectoryString;
import de.unihalle.informatik.Alida.datatypes.ALDFileString;
import de.unihalle.informatik.Alida.exceptions.ALDDataIOProviderException;
import de.unihalle.informatik.Alida.exceptions.ALDDataIOProviderException.ALDDataIOProviderExceptionType;
import de.unihalle.informatik.Alida.helpers.ALDFilePathManipulator;
import de.unihalle.informatik.Alida.operator.ALDParameterDescriptor;

import javax.swing.*;

import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.lang.reflect.Field;
import java.util.*;

/**
 * Class for loading/saving file and directory paths via GUI.
 * <p>
 * This class provides a panel for a GUI which contains a text entry 
 * field for the path, and in addition a button that opens a dialog for 
 * browsing the file system. By this it allows to select files and 
 * directories in a comfortable manner. 
 * 
 * @author moeller
 *
 */
@ALDDataIOProvider
public class ALDFileDirectoryDataIOSwing implements ALDDataIOSwing {

	/**
	 * Default directory to be used initially.
	 */
	protected static final String directoryDefault = 
		System.getProperty("user.dir");
	
	/**
	 * Default file name in current working directory.
	 */
	protected static final String fileDefault = 
			System.getProperty("user.dir") + File.separator + "datei.txt";

	/**
	 * Interface method to announce class for which IO is provided for.
	 * 
	 * @return  Collection of classes provided.
	 */
	@Override
	public Collection<Class<?>> providedClasses() {
		LinkedList<Class<?>> classes = new LinkedList<Class<?>>();
		classes.add(ALDFileString.class);
		classes.add(ALDDirectoryString.class);
		return classes;
	}
	
	@Override
  public Object getInitialGUIValue(Field field, 
			Class<?> cl, Object obj, ALDParameterDescriptor descr) { 
		if (obj != null)
			return obj;
		if (cl.equals(ALDDirectoryString.class)) 
			return new ALDDirectoryString(
					ALDFileDirectoryDataIOSwing.directoryDefault);
		return new ALDFileString(ALDFileDirectoryDataIOSwing.fileDefault);				
	}

	/* (non-Javadoc)
	 * @see de.unihalle.informatik.Alida.helpers.ALDDataIO#readData(java.lang.String)
	 */
	@Override
	public ALDSwingComponent createGUIElement(Field field, Class<?> cl, 
			Object obj, ALDParameterDescriptor descr) {
		return new FileDirectoryDataIOPanel(field, cl, obj, descr);
	}

	@Override
  public void setValue(Field field, Class<?> cl, 
  		ALDSwingComponent guiElement, Object value) 
  	throws ALDDataIOProviderException {

		// set value is only called from the inside of the Alida framework,
		// so make sure that no additional value change events are triggered
		// during setting a new value in the GUI elements
		ALDDataIOManagerSwing.getInstance().setTriggerValueChangeEvents(false);

		if (!(guiElement instanceof FileDirectoryDataIOPanel)) {
			// reactivate value change events and throw exception
			ALDDataIOManagerSwing.getInstance().setTriggerValueChangeEvents(true);
			throw new ALDDataIOProviderException(
					ALDDataIOProviderExceptionType.INVALID_GUI_ELEMENT, 
						"ALDFileDirDataIOSwing: setValue() received invalid " 
								+ "GUI element!");
		}

		Object lvalue = value;
		if (value == null) {
			if (((FileDirectoryDataIOPanel)guiElement).representsDirectory())
				lvalue = new ALDDirectoryString("");
			else
				lvalue = new ALDFileString("");
		}
		else if (   !(value instanceof ALDDirectoryString)
 			       && !(value instanceof ALDFileString)) {
			// reactivate value change events and throw exception
			ALDDataIOManagerSwing.getInstance().setTriggerValueChangeEvents(true);
			throw new ALDDataIOProviderException(
					ALDDataIOProviderExceptionType.INVALID_GUI_ELEMENT, 
						"ALDFileDirDataIOSwing: setValue() received wrong " 
								+	"object type!");
		}
		((FileDirectoryDataIOPanel)guiElement).setValue(field,cl,lvalue);
		
		// reactivate value change events
		ALDDataIOManagerSwing.getInstance().setTriggerValueChangeEvents(true);
  }

	/* (non-Javadoc)
	 * @see de.unihalle.informatik.Alida.dataio.provider.swing.ALDDataIOSwing#readData(java.lang.reflect.Field, java.lang.Class, javax.swing.JComponent)
	 */
	@Override
	public Object readData(
			Field field, Class<?> cl, ALDSwingComponent guiElement)
		throws ALDDataIOProviderException {
		if (!(guiElement instanceof FileDirectoryDataIOPanel))
			throw new ALDDataIOProviderException(
					ALDDataIOProviderExceptionType.INVALID_GUI_ELEMENT, 
					"ALDFileDirDataIOSwing: readData received invalid " 
					+ "GUI element!");
		return ((FileDirectoryDataIOPanel)guiElement).readData(field,cl);
	}

	/* (non-Javadoc)
	 * @see de.unihalle.informatik.Alida.dataio.provider.swing.ALDDataIOSwing#writeData(java.lang.Object)
	 */
	@Override
	public JComponent writeData(Object obj, ALDParameterDescriptor descr) 
			throws ALDDataIOProviderException {
		if (   !(obj instanceof ALDFileString) 
				&& !(obj instanceof ALDDirectoryString)) {
			throw new ALDDataIOProviderException(
					ALDDataIOProviderExceptionType.OBJECT_TYPE_ERROR,
					"ALDFileDirDataIOSwing: writeData received object " 
							+ "of wrong type!");
		}
		if (obj instanceof ALDFileString)
			return new JTextField(((ALDFileString)obj).getFileName());
		return new JTextField(((ALDDirectoryString)obj).getDirectoryName());
	}

	/**
	 * GUI panel for IO of file and directory names.
	 * 
	 * @author moeller
	 */
	protected class FileDirectoryDataIOPanel extends ALDSwingComponent 
		implements ActionListener, ALDSwingValueChangeListener {

		/**
		 * Swing component to be integrated in GUI.
		 */
		private JPanel ioPanel = null;
		
		/**
		 * Button to select via dialogue.
		 */
		private JButton selectFileDir = null;

		/**
		 * File/directory chooser dialogue.
		 */
		private JFileChooser getDirDialog;

		/**
		 * Last directory selected by user.
		 */
		private File lastDirectory = null;
		
		/**
		 * Last file selected by user.
		 */
		private File lastFile = null;
		
		/**
		 * Flag indicating if panel refers to a directory parameter or not.
		 */
		private boolean isDirectory = false;
		
		/**
		 * Text field in the panel.
		 */
		private ALDSwingComponentTextField textField = null;
		
		/**
		 * Descriptor of the associated (operator) parameter.
		 */
		private ALDParameterDescriptor paramDescriptor;
		
		/**
		 * Default constructor.
		 * 
		 * @param field	Field to consider.
		 * @param cl		Class to consider.
		 * @param obj		Default object.
		 * @param descr Descriptor of associated parameter.
		 */
		protected FileDirectoryDataIOPanel(
				@SuppressWarnings("unused") Field field, Class<?> cl, Object obj,
				ALDParameterDescriptor descr) {

			this.paramDescriptor = descr;
			
			// some local variables and default settings
			String defValue = null;
			if (cl.equals(ALDDirectoryString.class)) {
				this.isDirectory = true;
				defValue = ALDFileDirectoryDataIOSwing.directoryDefault;
			}
			else if (cl.equals(ALDFileString.class)) {
				this.isDirectory = false;
				defValue = ALDFileDirectoryDataIOSwing.fileDefault;				
			}
			
			// init the panel
			this.ioPanel = new JPanel();
			this.ioPanel.setLayout(new GridLayout(1,2));
			this.textField = 
					new ALDSwingComponentTextField(String.class, descr, 20);
			this.textField.getJComponent().setSize(70,5);
			this.textField.addValueChangeEventListener(this);
			if (obj != null) {
				if (obj instanceof ALDFileString) {
					defValue = ((ALDFileString)obj).getFileName();
				}
				else {
					defValue = ((ALDDirectoryString)obj).getDirectoryName();
				}
			}
			if (defValue != null) {
				this.lastDirectory = new File(defValue);
				this.textField.setText(defValue);
			}
			this.ioPanel.add(this.textField.getJComponent());
			this.selectFileDir= new JButton("Choose...");
			this.selectFileDir.setActionCommand("choose");
			this.selectFileDir.addActionListener(this);
			this.ioPanel.add(this.selectFileDir);
		}
		
		@Override
		public JPanel getJComponent() {
			return this.ioPanel;
		}
		
		/**
		 * Request type of input.
		 * @return	True, if a directory is represented.
		 */
		public boolean representsDirectory() {
			return this.isDirectory;
		}
		
		/**
		 * Returns the contents of the text field.
		 * 
		 * @param field	Field to consider.
		 * @param cl		Class to consider.
		 * @param value	Object value to be set.
		 */
		public void setValue(@SuppressWarnings("unused") Field field, 
				Class<?> cl, Object value) {
			if (cl.equals(ALDFileString.class)) {
				this.textField.setText(((ALDFileString)value).getFileName());
				this.lastFile = 
					new File(((ALDFileString)value).getFileName());
			}
			else {
				this.textField.setText(((ALDDirectoryString)value).getDirectoryName());
				this.lastDirectory = 
					new File(((ALDDirectoryString)value).getDirectoryName());
			}
		}

		/**
		 * Returns the contents of the text field.
		 * 
		 * @param field	Field to consider.
		 * @param cl		Class to consider.
		 * @return	Read object.
		 * @throws ALDDataIOProviderException Thrown in case of failure.
		 */
		public Object readData(@SuppressWarnings("unused") Field field, 
															Class<?> cl) 
			throws ALDDataIOProviderException {
			if (	 this.textField.getText() == null 
					|| this.textField.getText().isEmpty())
				return null;
			if (cl.equals(ALDFileString.class)) {
				return new ALDFileString(this.textField.getText());
			}
			if (cl.equals(ALDDirectoryString.class)) {
				return new ALDDirectoryString(this.textField.getText());
			}
			throw new ALDDataIOProviderException(
					ALDDataIOProviderExceptionType.OBJECT_TYPE_ERROR,
					"ALDFileDirectoryDataIOSwing: unknown class requested!");
		}

		@Override
	  public void actionPerformed(ActionEvent e) {
			String command = e.getActionCommand();
			if (command.equals("choose")) {
				// open file chooser
				this.getDirDialog= new JFileChooser();
				if (    this.textField.getText() != null 
						&& !this.textField.getText().isEmpty()) {
					String selection = this.textField.getText();
					String file = 
						ALDFilePathManipulator.removeLeadingDirectories(selection);
					String dir = ALDFilePathManipulator.getPath(selection);
					if (!file.isEmpty())
						this.getDirDialog.setSelectedFile(new File(file));
					if (!dir.isEmpty())
						this.getDirDialog.setCurrentDirectory(new File(dir));
					else {
						if (selection.substring(0,1).equals(
								System.getProperty("file.separator"))) {
							this.getDirDialog.setCurrentDirectory(new File(selection));
						}
					}
				}
				else {
					this.getDirDialog.setCurrentDirectory(this.lastDirectory);
					this.getDirDialog.setSelectedFile(this.lastFile);
				}
				if (this.isDirectory) {
					this.getDirDialog.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
					this.getDirDialog.setDialogTitle("Select a directory...");
				}
				else {
					this.getDirDialog.setFileSelectionMode(JFileChooser.FILES_ONLY);
					this.getDirDialog.setDialogTitle("Select a file...");
				}
				
				int returnVal = this.getDirDialog.showOpenDialog(null);
				if (returnVal == JFileChooser.APPROVE_OPTION) {
					File file = this.getDirDialog.getSelectedFile();
					this.textField.setText(file.getPath());
					this.lastDirectory = this.getDirDialog.getCurrentDirectory();
					this.lastFile = this.getDirDialog.getSelectedFile();
					this.handleValueChangeEvent(
						new ALDSwingValueChangeEvent(this, this.paramDescriptor));
				}
			}
	  }

		@Override
		public void handleValueChangeEvent(ALDSwingValueChangeEvent event) {
			this.fireALDSwingValueChangeEvent(event);
		}

		@Override
    public void disableComponent() {
			this.selectFileDir.setEnabled(false);
			this.textField.disableComponent();
    }

		@Override
    public void enableComponent() {
			this.selectFileDir.setEnabled(true);
			this.textField.enableComponent();
    }
		
		@Override
    public void dispose() {
			// nothing to do here
		}
	}
}
