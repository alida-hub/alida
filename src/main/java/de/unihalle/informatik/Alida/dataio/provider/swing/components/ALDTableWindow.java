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

import java.awt.*;
import java.awt.event.*;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import javax.swing.*;
import javax.swing.filechooser.FileFilter;
import javax.swing.table.DefaultTableModel;

import de.unihalle.informatik.Alida.exceptions.ALDException;
import de.unihalle.informatik.Alida.helpers.ALDFilePathManipulator;
import de.unihalle.informatik.Alida.operator.ALDOperator;

/**
 * GUI window for displaying tables in Alida.
 * <p>
 * Optionally an object, e.g. a 2D array, can be linked to the table.
 * If this is done on saving the table the history of the object is 
 * saved together with the table. However, note that if the contents 
 * of the table are modified, the object is not updated.
 * 
 * @author moeller
 */
public class ALDTableWindow extends JFrame implements ActionListener {

	/**
	 * Delimiters available when writing a model to a string or file.
	 * @author moeller
	 */
	public static enum TableModelDelimiter {
		/**
		 *	Use a tabulator as delimiter. 
		 */
		TAB,		
		/**
		 * Use a white-space as delimiter.
		 */
		SPACE
	}

	/**
	 * Data table (swing tables are nicer than imageJ tables).
	 */
	protected JTable dataTab;
	
	/**
	 * Reference object to the data.
	 */
	protected DefaultTableModel dataTabModel;
	
	/**
	 * Optional data object linked to the table model.
	 */
	protected Object linkedDataObject = null;

	/**
	 * For convenience: always open last directory for saving.
	 */
	protected File lastDir;

	/**
	 * Flag to indicate if headers should be saved to file.
	 */
	protected boolean saveHeaders = true;
	
	/**
	 * Window for configuration of table options.
	 */
	protected TableConfigWindow optionsWindow; 
	
	/**
	 * Default constructor.
	 * @param mtm	Associated table model.
	 */
	public ALDTableWindow(DefaultTableModel mtm) {
		this.dataTabModel= mtm;

		// init current directory with user directory
		this.lastDir = new File(System.getProperty("user.dir"));
		// init the options window
		this.optionsWindow = new TableConfigWindow();
		
		this.setupResultTable();
	}
	
	/**
	 * Default constructor.
	 * @param mtm		Associated table model.
	 * @param obj		Optional object underlying the table model.
	 */
	public ALDTableWindow(DefaultTableModel mtm, Object obj) {
		this.dataTabModel = mtm;
		this.linkedDataObject = obj;

		// init current directory with user directory
		this.lastDir = new File(System.getProperty("user.dir"));
		// init the options window
		this.optionsWindow = new TableConfigWindow();
		
		this.setupResultTable();
	}

	/**
	 * Shows the table window in graphical environment.
	 */
	public void openWindow() {
		this.setVisible(true);
		this.validate();
	}

	/**
	 * Closes the GUI table window.
	 * <p>
	 * Attention, if you do not store a reference to the window,
	 * it cannot be opened again!
	 */
	public void closeWindow() {
		this.setVisible(false);
	}
	
	/**
	 * Function to enable/disable saving of headers to file.
	 * By default headers are saved also.
	 * @param b	If false, headers are ignored on saving data to file.
	 */
	public void setSaveHeaders(boolean b) {
		this.saveHeaders = b;
	}
	
	/**
	 * Saves the contents of the table to a user-specified file.
	 * <p>
	 * The file format is TSV, i.e. tabulator-separated values.
	 * The default ending is '.txt'. The user can select the file
	 * name through a file open dialog which pops-up on call of the 
	 * function.
	 */
	public void saveTable() {

		File file= null;
		
		JFrame dummy= new JFrame();
		JFileChooser getSaveFileDialog= new JFileChooser(); 
		getSaveFileDialog.setFileFilter(new DataTabFileFilter());
		getSaveFileDialog.setFileSelectionMode(JFileChooser.FILES_ONLY);
		getSaveFileDialog.setCurrentDirectory(this.lastDir); 
		getSaveFileDialog.setSelectedFile(new File("ResultTab.txt"));
		getSaveFileDialog.setApproveButtonText("Save");
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
					JOptionPane.showConfirmDialog(null,"The file exists already, would "
					+ "you like to overwrite it?", "Warning",JOptionPane.YES_NO_OPTION);
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
			StringBuffer[] tab = tableToString(this.dataTabModel, 
																		this.optionsWindow.getDelimiterString());
			// note: first row contains the header which might be meaningless, 
			//       i.e. sometimes they should be ignored
			int firstIndex = (this.saveHeaders ? 0 : 1);
			for (int i=firstIndex;i<tab.length;++i)
				ow.write(tab[i].toString());
			ow.close();
		} catch (IOException e) {
			JOptionPane.showMessageDialog(null,"Error!!! " +
					"Could not open output file " + file.getPath() + "!");
		}
		// optionally save the history of the data object, if there is any
		if (this.linkedDataObject != null) {
			String historyFileName = file.getAbsolutePath();
			historyFileName = ALDFilePathManipulator.removeExtension(historyFileName);
			try {
	      ALDOperator.writeHistory(this.linkedDataObject, historyFileName);
      } catch (ALDException e) {
      	System.err.println("Warning! Could not write history, " +
      			"ignoring request...");
      	e.printStackTrace();
      }
		}
	}


	/* (non-Javadoc)
	 * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
	 */
	@Override
  public void actionPerformed(ActionEvent e) {
		
		// deletes all data from the table and sets row and column counts to zero
		if (e.getActionCommand()=="clear") {
			for (int i=0;i<this.dataTabModel.getRowCount();++i)
				for (int j=0;j<this.dataTabModel.getColumnCount();++j)
					this.dataTabModel.setValueAt(null, i, j);
		}
		// closes the GUI window
		else if (e.getActionCommand()=="close" ) {
			this.setVisible(false);
		}
		// saves the table data in TSV format
		else if (e.getActionCommand()=="save") {
			this.saveTable();
		}		
		// saves the table data in TSV format
		else if (e.getActionCommand()=="option") {
			this.optionsWindow.setVisible(true);
		}		
	}
	
	/**
	 * Initializes the data table window.
	 */
	protected void setupResultTable() {
		
		// panel for the table
		JPanel tabPanel;

		// instantiate result table and put into scroll pane
		this.dataTab= new JTable(this.dataTabModel);
		JScrollPane scrollpane= new JScrollPane(this.dataTab);
		scrollpane.setPreferredSize(new Dimension (400,300));
		this.dataTab.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);

		// add a toolbar
		JButton tabOption= new JButton("Options...");
		tabOption.setActionCommand("option");
		tabOption.addActionListener(this);
		tabOption.setSize(new Dimension(80,35));
		JButton tabSave= new JButton("Save");
		tabSave.setActionCommand("save");
		tabSave.addActionListener(this);
		tabSave.setSize(new Dimension(80,35));
		JButton tabClear= new JButton("Clear");
		tabClear.setActionCommand("clear");
		tabClear.addActionListener(this);
		tabClear.setSize(new Dimension(80,35));
		JButton tabClose= new JButton("Close");
		tabClose.setActionCommand("close");
		tabClose.addActionListener(this);
		tabClose.setSize(new Dimension(80,35));
		JPanel tabToolPanel= new JPanel();
		tabToolPanel.setLayout(new FlowLayout());
		tabToolPanel.add(tabOption);
		tabToolPanel.add(tabSave);
		tabToolPanel.add(tabClear);
		tabToolPanel.add(tabClose);
		JToolBar tabTools= new JToolBar("");
		tabTools.add(tabToolPanel);
		
		tabPanel= new JPanel();
		tabPanel.setLayout(new BorderLayout());
		tabPanel.add(scrollpane, BorderLayout.CENTER);
		tabPanel.add(tabTools, BorderLayout.SOUTH);

		this.setSize(900, 250);
		this.setTitle("");
		this.add(tabPanel);
		
		// ... then show it!
		this.setVisible(false);
	}
	
	/**
	 * Converts the contents of the table to a string array
	 * in CSV format (suitable, e.g., for import in Excel or R).
	 * 
	 * @param model 			Table model to convert.
	 * @param delimString Column delimiter, e.g., '\t' or '\n'.
	 * 
	 * @return Array with contents of table (arranged line-wise).
	 */
	public static StringBuffer [] tableToString(DefaultTableModel model,
			String delimString) {
		
		// number of lines in table (without headlines)
		int lines= model.getRowCount();
		int cols = model.getColumnCount();

		StringBuffer [] tabS= new StringBuffer[lines+1];
		StringBuffer headings= new StringBuffer();
		StringBuffer newline;
		
		// transform headlines...
		for (int j=0; j<cols-1; ++j) {
			headings.append(model.getColumnName(j) + delimString);
		}
		headings.append(model.getColumnName(cols-1) + "\n");
		tabS[0]= headings;
		
		// ... and then the rows
		String buf= null;
		for (int i= 0; i<lines; ++i) {

			newline= new StringBuffer();
			newline.append(model.getValueAt(i, 0) + delimString);
			for (int j=1; j<model.getColumnCount()-1;++j) {

				if (model.getValueAt(i,j) == null) {
					newline.append(0 + delimString);
					continue;
				}
				buf= (model.getValueAt(i,j)).toString();
				// append to line
				newline.append(buf + delimString);
			}
			if (   model.getColumnCount() > 1 
					&& model.getValueAt(i, model.getColumnCount()-1) != null) {
				
				buf= (model.getValueAt(i, model.getColumnCount()-1)).toString();

				newline.append(buf + "\n");
			}
			else {
				newline.append("\n");
			}
			tabS[i+1]= newline;
		}
		return tabS;
	}

	/**
	 * Converts the contents of the table to a string array
	 * in CSV format (suitable for import in Excel or R).
	 * <p>
	 * As column delimiter tabulators are used.
	 * 
	 * @param model 			Table model to convert.
	 * @return Array with contents of table (arranged line-wise).
	 */
	public static StringBuffer [] tableToString(DefaultTableModel model) {
		return tableToString(model, "\t");
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
			return "Alida Data Table Files (*.txt)";
		}
	}
	
	/**
	 * Window class for configuring the table appearance and export.
	 * @author moeller
	 */
	protected class TableConfigWindow extends JFrame 
																			implements ActionListener {

		/**
		 * Delimiter to be used for table export to file.
		 */
		protected TableModelDelimiter delimiter = TableModelDelimiter.TAB;
		
		/**
		 * Button for selecting tabs as delimiters.
		 */
		protected JRadioButton delimButtonTab;
		
		/**
		 * Button for selecting spaces as delimiters.
		 */
		protected JRadioButton delimButtonSpace;

		/**
		 * Default constructor.
		 */
		public TableConfigWindow() {
			super("Options");
			this.delimButtonTab = new JRadioButton("Tabulator");
			this.delimButtonTab.setActionCommand("delimButtonToggled");
			this.delimButtonTab.addActionListener(this);
			this.delimButtonSpace = new JRadioButton("Space");
			this.delimButtonSpace.setActionCommand("delimButtonToggled");
			this.delimButtonSpace.addActionListener(this);
			switch (this.delimiter)
			{
			case TAB:
				this.delimButtonTab.setSelected(true);
				this.delimButtonSpace.setSelected(false);
				break;
			case SPACE:
				this.delimButtonTab.setSelected(false);
				this.delimButtonSpace.setSelected(true);
				break;
			}
			JPanel buttonPanel = new JPanel(new GridLayout(2, 1));
			buttonPanel.add(this.delimButtonTab);
			buttonPanel.add(this.delimButtonSpace);
			ButtonGroup delimButtons = new ButtonGroup();
			delimButtons.add(this.delimButtonTab);
			delimButtons.add(this.delimButtonSpace);
			this.add(new JLabel("Column deliminiter:"), 	BorderLayout.NORTH);
			this.add(buttonPanel,BorderLayout.CENTER);
			JButton closeButton = new JButton("Close");
			closeButton.setActionCommand("close");
			closeButton.addActionListener(this);
			closeButton.setSize(new Dimension(80,35));
			JPanel closeButtonPanel = new JPanel();
			closeButtonPanel.add(closeButton);
			this.add(closeButtonPanel, BorderLayout.SOUTH);
			this.setSize(300, 200);
		}
		
		/**
		 * Returns the currently selected delimiter string.
		 * @return	Delimiter string.
		 */
		public String getDelimiterString() {
			switch(this.delimiter)
			{
			case SPACE:
				return " ";
			case TAB:
			default:
				return "\t";
			}
		}

		/**
		 * Set delimiter.
		 * @param  delim     Delimiter to use.
		 */
		public void setDelimiter(TableModelDelimiter delim) {
		    this.delimiter = delim;
		    switch(this.delimiter)
			{
			case SPACE:
			    this.delimButtonSpace.setSelected(true);
			    break;
			case TAB:
			    this.delimButtonTab.setSelected(true);
			    break;
			}
		}

		@Override
		public void actionPerformed(ActionEvent ev) {
			
			String command = ev.getActionCommand();
			
			// delimiter selection has been changed
			if (command.equals("delimButtonToggled")) {
				if (this.delimButtonTab.isSelected()) {
					this.delimiter = TableModelDelimiter.TAB;
				}
				else if (this.delimButtonSpace.isSelected()) {
					this.delimiter = TableModelDelimiter.SPACE;
				}
			}
			// close the configuration window
			else if (command.equals("close")) {
				this.setVisible(false);
			}
		}
	}
}
