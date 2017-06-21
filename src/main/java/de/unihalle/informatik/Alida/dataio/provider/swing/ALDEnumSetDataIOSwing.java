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
import de.unihalle.informatik.Alida.dataio.provider.ALDDataIOSwingInitialGUIValueDefaultHandler;
import de.unihalle.informatik.Alida.dataio.provider.helpers.ALDCollectionDataIOHelper;
import de.unihalle.informatik.Alida.dataio.provider.swing.components.ALDSwingComponent;
import de.unihalle.informatik.Alida.dataio.provider.swing.components.ALDSwingComponentItem;
import de.unihalle.informatik.Alida.dataio.provider.swing.components.ALDSwingComponentList;
import de.unihalle.informatik.Alida.dataio.provider.swing.events.ALDSwingValueChangeEvent;
import de.unihalle.informatik.Alida.dataio.provider.swing.events.ALDSwingValueChangeListener;
import de.unihalle.informatik.Alida.dataio.provider.swing.events.ALDSwingValueChangeReporter;
import de.unihalle.informatik.Alida.exceptions.*;
import de.unihalle.informatik.Alida.exceptions.ALDDataIOProviderException.ALDDataIOProviderExceptionType;
import de.unihalle.informatik.Alida.operator.ALDParameterDescriptor;

import javax.swing.*;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.lang.reflect.Field;
import java.lang.reflect.Type;
import java.util.*;

/**
 * Class for generic loading/saving of enum selections via GUI in Alida.
 * <p>
 * Note that this provider is only implicitly called from 
 * {@link ALDCollectionDataIOSwing}. Hence, it is not annotated.
 * 
 * @author moeller
 */
@ALDDataIOProvider
public class ALDEnumSetDataIOSwing 
	extends ALDDataIOSwingInitialGUIValueDefaultHandler {

	/* (non-Javadoc)
	 * @see de.unihalle.informatik.Alida.dataio.provider.ALDDataIO#providedClasses()
	 */
	@Override
	public Collection<Class<?>> providedClasses() {
		LinkedList<Class<?>> classes = new LinkedList<Class<?>>();
		classes.add(EnumSet.class);
		return classes;
	}
	
	/* (non-Javadoc)
	 * @see de.unihalle.informatik.Alida.helpers.ALDDataIOSwing#createGUIElement(java.lang.Class, java.lang.Object)
	 */
	@Override
	public ALDSwingComponent createGUIElement(
			Field field, Class<?> cl, Object obj, ALDParameterDescriptor descr) {
		return new EnumSelectionConfigButton(field, cl, obj, descr);
	}

	/* (non-Javadoc)
	 * @see de.unihalle.informatik.Alida.dataio.provider.ALDDataIOSwing#setValue(java.lang.reflect.Field, java.lang.Class, de.unihalle.informatik.Alida.dataio.provider.swing.components.ALDSwingComponent, java.lang.Object)
	 */
	@Override
  public void setValue(
  		Field field, Class<?> cl, ALDSwingComponent guiElement,	Object value) 
  	throws ALDDataIOProviderException {
		if (!(guiElement instanceof EnumSelectionConfigButton))
			throw new ALDDataIOProviderException(
					ALDDataIOProviderExceptionType.INVALID_GUI_ELEMENT, 
					"EnumSelectionDataIO: setValue() received invalid GUI element!");
		((EnumSelectionConfigButton)guiElement).setValue(field, cl, value);
  }

	/* (non-Javadoc)
	 * @see de.unihalle.informatik.Alida.dataio.provider.ALDDataIOSwing#readData(java.lang.reflect.Field, java.lang.Class, de.unihalle.informatik.Alida.dataio.provider.swing.components.ALDSwingComponent)
	 */
	@Override
	public Object readData(
			Field field, Class<?> cl, ALDSwingComponent guiElement) 
		throws ALDDataIOProviderException {
		if (!(guiElement instanceof EnumSelectionConfigButton))
			throw new ALDDataIOProviderException(
					ALDDataIOProviderExceptionType.INVALID_GUI_ELEMENT, 
					"EnumSelectionDataIO: readData received invalid GUI element!");
		return ((EnumSelectionConfigButton)guiElement).readData(field, cl);
	}

	@Override
	public JComponent writeData(Object obj, ALDParameterDescriptor descr) 
			throws ALDDataIOProviderException {
		if (!(obj instanceof EnumSet))
			throw new ALDDataIOProviderException(
					ALDDataIOProviderExceptionType.OBJECT_TYPE_ERROR, 
					"EnumSelectionDataIO: object to write has wrong type!");
		// return a button to show a window with the elements
		return new EnumSelectionShowButton(obj, descr);
	}

	/**
	 * GUI element for configuring enum selections.
	 * <p>
	 * This button has a configuration window attached to it 
	 * where the selection of enum values is stored.
	 * 
	 * @author moeller
	 */
	private class EnumSelectionConfigButton	extends ALDSwingComponent	
		implements ALDSwingValueChangeListener {

		/**
		 * Button to open configuration window.
		 */
		private JButton confButton;
		
		/**
		 * Collection configuration window.
		 */
		private EnumSelectionConfigWindow confWin;

		/**
		 * Constructor.
		 * 
		 * @param field		Field of collection.
		 * @param cl			Class of collection.
		 * @param obj			Default object.
		 * @param descr		Optional descriptor with additional information.
		 */
		public EnumSelectionConfigButton(Field field, Class<?> cl, Object obj,
																		ALDParameterDescriptor descr) {
			this.confWin = new EnumSelectionConfigWindow(field, cl, obj, descr);
			this.confWin.addValueChangeEventListener(this);
			this.confButton = new JButton("Select values...");
			this.confButton.setActionCommand("configButtonPressed");
			this.confButton.addActionListener(this.confWin);
		}
		
		/* (non-Javadoc)
		 * @see de.unihalle.informatik.Alida.dataio.provider.swing.components.ALDSwingComponent#getJComponent()
		 */
		@Override
		public JButton getJComponent() {
			return this.confButton;
		}
		
		/**
		 * Gets the data from the configuration window.
		 * 
		 * @param field	Field of collection.
		 * @param cl	Class of collection.
		 * @return Current data.
		 */
		public Object readData(Field field, Class<?> cl) {
			return this.confWin.readData(field, cl);
		}

		/**
		 * Sets new values in configuration window.
		 * 
		 * @param field	Field of collection.
		 * @param cl		Class of collection.
		 * @param value	New value.
		 */
		public void setValue(@SuppressWarnings("unused") Field field, 
										@SuppressWarnings("unused") Class<?> cl, Object value) {
			this.confWin.setValue(value);
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
			this.confWin.disableComponent();
    }

		/* (non-Javadoc)
		 * @see de.unihalle.informatik.Alida.dataio.provider.swing.components.ALDSwingComponent#enableComponent()
		 */
		@Override
    public void enableComponent() {
			this.confWin.enableComponent();
			this.confButton.setEnabled(true);
    }
		
		/* (non-Javadoc)
		 * @see de.unihalle.informatik.Alida.dataio.provider.swing.components.ALDSwingComponent#dispose()
		 */
		@Override
	  public void dispose() {
			// close the associated configuration window
			this.confWin.dispose();
	  }
	}	
	
	/**
	 * GUI element for displaying enum selections.
	 * <p>
	 * This button opens a window displaying a selection of enum values.
	 * 
	 * @author moeller
	 */
	private class EnumSelectionShowButton
		extends JButton implements ActionListener {

		/**
		 * Selection to display.
		 */
		private EnumSet<?> selection;
		
		/**
		 * Parameter descriptor.
		 */
		private ALDParameterDescriptor pDesc;
		
		/**
		 * Main window.
		 */
		private JFrame window;
		
		/**
		 * Constructor.
		 * @param 	obj		Object to be visualized on button press.
		 * @param		descr	Optional descriptor with additional information.
		 */
		public EnumSelectionShowButton(Object obj, ALDParameterDescriptor descr) {
			super("Show enum selection...");
			this.setActionCommand("showButtonPressed");
			this.addActionListener(this);
			this.selection = (EnumSet<?>)obj;
			this.pDesc = descr;
		}
		
		/* (non-Javadoc)
		 * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
		 */
		@Override
    public void actionPerformed(ActionEvent e) {
			String cmd = e.getActionCommand(); 
			if (cmd.equals("showButtonPressed")) {
				if (this.window == null) {
					this.window = new JFrame();
					JPanel mainPanel = new JPanel();
					BorderLayout bl = new BorderLayout();
					mainPanel.setLayout(bl);

					Vector<ALDSwingComponentItem>	enumListItems = new Vector<>();
					for (Object o: this.selection) {
						ALDSwingComponentItem item = 
								new ALDSwingComponentItem(o, o.toString(), o.toString());	
						enumListItems.add(item);
					}
					ALDSwingComponentList enumList = 
							new ALDSwingComponentList(this.pDesc,	enumListItems);
					JScrollPane scroller = new JScrollPane(enumList.getJComponent());  
					mainPanel.add(scroller, BorderLayout.CENTER);

					// add buttons
					JPanel tmpPanel = new JPanel();
					JButton closeButton = new JButton("Close");
					closeButton.setActionCommand("close");
					closeButton.addActionListener(this);
					tmpPanel.add(closeButton);
					mainPanel.add(tmpPanel,BorderLayout.SOUTH);
					this.window.add(mainPanel);
				}
				// scale window to a proper size
				int pWidth = 
						this.window.getWidth() < 200 ? 200 : this.window.getWidth(); 
				int pHeight =
						this.window.getHeight() < 200 ? 200 : this.window.getHeight(); 
				this.window.setPreferredSize(new Dimension(pWidth, pHeight));
				this.window.pack();
				this.window.repaint();
				this.window.validate();
				this.window.setVisible(true);
			}
			else if (cmd.equals("close")) {
				this.window.setVisible(false);
			}
    }
	}	

	/**
	 * Collection configuration window.
	 * 
	 * @author moeller
	 */
	private class EnumSelectionConfigWindow 
		extends ALDSwingValueChangeReporter
			implements ActionListener, ALDSwingValueChangeListener {

		/**
		 * Fixed minimal width of window.
		 */
		private static final int frameWidthMin = 200;
		
		/**
		 * Fixed minimal height of the configuration window.
		 */
		private static final int frameHeightMin = 200;
		
		/**
		 * Main frame.
		 */
		private JFrame window;

		/**
		 * Main panel of main frame.
		 */
		private JPanel mainPanel = null;
		
		/**
		 * List component displaying elements.
		 */
		private ALDSwingComponentList enumList;
		
		/**
		 * Items in list component.
		 */
		private Vector<ALDSwingComponentItem> enumListItems;
		
		/**
		 * Scroller element for collection elements.
		 */
		private JScrollPane scroller = null;
		
		/**
		 * Button to close the configuration window.
		 */
		private JButton closeButton;
		
		/**
		 * EnumSet element type.
		 */
		private Type elemType;

		/**
		 * Parameter descriptor of associated parameter.
		 */
		private ALDParameterDescriptor paramDescriptor;
		
		/**
		 * Default constructor.
		 * @param field		Field to specify input data objects.
		 * @param cl			Class of collection elements.
		 * @param obj			Initial value of collection.
		 * @param descr		Optional descriptor for additional information.
		 */
		public EnumSelectionConfigWindow(Field field, 
				@SuppressWarnings("unused") Class<?> cl, Object obj, 
				ALDParameterDescriptor descr) {

			// figure out type of enumeration
			this.elemType = ALDCollectionDataIOHelper.lookupType(field);
			this.paramDescriptor = descr;

			// initialize the window
			this.window = new JFrame();
			this.window.setResizable(true);
			String title = "unknown";
			if (descr != null) title = descr.getLabel();
			this.window.setTitle("Enum selection <" +title+ ">, type <" 
					+ this.elemType +">");
			this.window.setSize(frameWidthMin, frameHeightMin);
			this.window.setPreferredSize(
					new Dimension(frameWidthMin, frameHeightMin));
			this.setupWindow((EnumSet<?>)obj);
			this.window.pack();
		}
		
		/**
		 * Extracts current collection data.
		 * 
		 * @param field	Field of collection elements.
		 * @param cl	Class of collection elements.
		 * @return	Current collection.
		 */
		@SuppressWarnings("unchecked")
    public EnumSet<?> readData(@SuppressWarnings("unused") Field field, 
    		@SuppressWarnings("unused") Class<?> cl) {
			// this is a bit ugly: we first create a set with all enums, then 
			// remove selected ones and finally return the complement of the
			// set with non-selected elements; reason is that it is not possible
			// to add an object without known type, but one can remove it...
			EnumSet<?> eSet = EnumSet.allOf((Class<Enum>)this.elemType);
			Vector<ALDSwingComponentItem> sItems = this.enumList.getSelectedItems(); 
			for (ALDSwingComponentItem i: sItems) {
				eSet.remove(i.getObject());
			}
			return EnumSet.complementOf(eSet);
		}
		
		/**
		 * Updates current enum selection.
		 * 
		 * @param value	New selection.
		 */
		public void setValue(Object value) {
			
			EnumSet<?> selection = (EnumSet<?>)value;

			// if value is null, just ignore the call to this function
			if (selection == null) {
				return;
			}
			
			Vector<ALDSwingComponentItem> selectedItems = new Vector<>();
			for (ALDSwingComponentItem o: this.enumListItems) {
				if (selection.contains(o.getObject())) {
					selectedItems.add(o);
				}
			}
			// set currently selected items
			this.enumList.setSelectedItems(selectedItems);
		}

		/**
		 * Deactivates the configuration window to prohibit value changes.
		 */
		public void disableComponent() {
			if (this.enumList != null)
				this.enumList.disableComponent();
		}
		
		/**
		 * Reactivates the configuration window to allow for value changes.
		 */
		public void enableComponent() {
			if (this.enumList != null)
				this.enumList.enableComponent();
		}
		
		/**
		 * Disposes this window and all sub-components.
		 */
		public void dispose() {
			this.window.dispose();
		}
		
		/**
		 * Displays the selection in the window.
		 * @param selection Set of currently selected items.
		 */
		private void setupWindow(EnumSet<?> selection) {
			
			int wWidth = this.window.getWidth() < frameWidthMin ? 
					frameWidthMin : this.window.getWidth();
			int wHeight = this.window.getHeight() < frameHeightMin ?
					frameHeightMin : this.window.getHeight();

			// on the first call, init the main panel
			if (this.mainPanel == null) {				
				this.mainPanel = new JPanel();
				BorderLayout bl = new BorderLayout();
				this.mainPanel.setLayout(bl);

				this.enumListItems = new Vector<>();
				EnumSet<?> s = EnumSet.allOf((Class<Enum>)this.elemType);
				for (Object o: s) {
					ALDSwingComponentItem item = 
							new ALDSwingComponentItem(o, o.toString(), o.toString());
					this.enumListItems.add(item);
				}
				this.enumList = new ALDSwingComponentList(this.paramDescriptor, 
						this.enumListItems);
				this.enumList.addValueChangeEventListener(this);
				this.scroller = new JScrollPane(this.enumList.getJComponent());  
				this.mainPanel.add(this.scroller, BorderLayout.CENTER);

				// add buttons
				JPanel tmpPanel = new JPanel();
				this.closeButton = new JButton("Close");
				this.closeButton.setActionCommand("close");
				this.closeButton.addActionListener(this);
				tmpPanel.add(this.closeButton);
				this.mainPanel.add(tmpPanel,BorderLayout.SOUTH);
				
				this.window.add(this.mainPanel);
			}
			
			// set currently selected items
			Vector<ALDSwingComponentItem> selectedItems = new Vector<>();
			for (ALDSwingComponentItem o: this.enumListItems) {
				if (selection != null && selection.contains(o.getObject()))
					selectedItems.add(o);
			}
			this.enumList.setSelectedItems(selectedItems);
			
			// scale window to a proper size
			this.window.validate();
			int pWidth = 
					this.window.getWidth() < wWidth ? wWidth : this.window.getWidth(); 
			int pHeight =
					this.window.getHeight() < wHeight ? wHeight : this.window.getHeight(); 
			this.window.setPreferredSize(new Dimension(pWidth, pHeight));
			this.window.pack();
			this.window.repaint();
			this.window.validate();
		}
		
		/* (non-Javadoc)
		 * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
		 */
		@Override
    public void actionPerformed(ActionEvent e) {
			String cmd = e.getActionCommand(); 
			if (cmd.equals("configButtonPressed")) {
				this.window.setVisible(true);
			}
			else if (cmd.equals("close")) {
				this.window.setVisible(false);
			}
	  }

		/* (non-Javadoc)
		 * @see de.unihalle.informatik.Alida.dataio.provider.swing.events.ALDSwingValueChangeListener#handleValueChangeEvent(de.unihalle.informatik.Alida.dataio.provider.swing.events.ALDSwingValueChangeEvent)
		 */
		@Override
		public void handleValueChangeEvent(ALDSwingValueChangeEvent event) {
			this.fireALDSwingValueChangeEvent(event);
		}		
	}
}
