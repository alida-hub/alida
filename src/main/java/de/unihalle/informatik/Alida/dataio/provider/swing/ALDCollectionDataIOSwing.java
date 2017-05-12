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
import de.unihalle.informatik.Alida.dataio.provider.ALDDataIOSwingInitialGUIValueDefaultHandler;
import de.unihalle.informatik.Alida.dataio.provider.helpers.ALDCollectionDataIOHelper;
import de.unihalle.informatik.Alida.dataio.provider.swing.components.ALDSwingComponent;
import de.unihalle.informatik.Alida.dataio.provider.swing.events.ALDSwingValueChangeEvent;
import de.unihalle.informatik.Alida.dataio.provider.swing.events.ALDSwingValueChangeListener;
import de.unihalle.informatik.Alida.dataio.provider.swing.events.ALDSwingValueChangeReporter;
import de.unihalle.informatik.Alida.exceptions.*;
import de.unihalle.informatik.Alida.exceptions.ALDDataIOProviderException.ALDDataIOProviderExceptionType;
import de.unihalle.informatik.Alida.operator.ALDParameterDescriptor;

import javax.swing.*;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.lang.reflect.Field;
import java.lang.reflect.Type;
import java.util.*;

/**
 * Class for generic loading/saving of collections from/to GUI in Alida.
 * 
 * @author moeller
 */
@ALDDataIOProvider
public class ALDCollectionDataIOSwing 
	extends ALDDataIOSwingInitialGUIValueDefaultHandler {

	/**
	 * Interface method to announce class for which IO is provided for.
	 * 
	 * @return  Collection of classes provided.
	 */
	@Override
	public Collection<Class<?>> providedClasses() {
		LinkedList<Class<?>> classes = new LinkedList<Class<?>>();
		classes.add(Collection.class);
		return classes;
	}
	
	/** 
	 * Generic reading of collections.
	 */
	/* (non-Javadoc)
	 * @see de.unihalle.informatik.Alida.helpers.ALDDataIOSwing#createGUIElement(java.lang.Class, java.lang.Object)
	 */
	@Override
	public ALDSwingComponent createGUIElement(
			Field field, Class<?> cl, Object obj, ALDParameterDescriptor descr) {
		return new CollectionConfigButton(field, cl, obj, descr);
	}

	@Override
  public void setValue(
  		Field field, Class<?> cl, ALDSwingComponent guiElement,	Object value) 
  	throws ALDDataIOProviderException {
		if (!(guiElement instanceof CollectionConfigButton))
			throw new ALDDataIOProviderException(
					ALDDataIOProviderExceptionType.INVALID_GUI_ELEMENT, 
					"CollectionDataIO: setValue() received invalid GUI element!");
		((CollectionConfigButton)guiElement).setValue(field, cl, value);
  }

	@Override
	public Object readData(
			Field field, Class<?> cl, ALDSwingComponent guiElement) 
		throws ALDDataIOProviderException {
		if (!(guiElement instanceof CollectionConfigButton))
			throw new ALDDataIOProviderException(
					ALDDataIOProviderExceptionType.INVALID_GUI_ELEMENT, 
					"CollectionDataIO: readData received invalid GUI element!");
		return ((CollectionConfigButton)guiElement).readData(field, cl);
	}

	@Override
	public JComponent writeData(Object obj, ALDParameterDescriptor descr) 
			throws ALDDataIOProviderException {
		if (!(obj instanceof Collection))
			throw new ALDDataIOProviderException(
					ALDDataIOProviderExceptionType.OBJECT_TYPE_ERROR, 
					"CollectionDataIO: object to write has wrong type!");
		// return a button to show a window with the elements
		return new CollectionShowButton(obj, descr);
	}

	/**
	 * GUI element for configuring collections.
	 * <p>
	 * This button has a collection configuration window attached to it 
	 * where specific data is stored and accessable.
	 * 
	 * @author moeller
	 */
	private class CollectionConfigButton extends ALDSwingComponent
		implements ALDSwingValueChangeListener {

		/**
		 * Button to open configuration window.
		 */
		private JButton confButton;
		
		/**
		 * Collection configuration window.
		 */
		private CollectionConfigWindow confWin;

		/**
		 * Constructor.
		 * 
		 * @param field		Field of collection.
		 * @param cl			Class of collection.
		 * @param obj			Default object.
		 * @param descr		Optional descriptor with additional information.
		 */
		public CollectionConfigButton(Field field, Class<?> cl, Object obj,
																		ALDParameterDescriptor descr) {
			this.confWin = new CollectionConfigWindow(field, cl, obj, descr);
			this.confWin.addValueChangeEventListener(this);
			this.confButton = new JButton("Configure Collection...");
			this.confButton.setActionCommand("configButtonPressed");
			this.confButton.addActionListener(this.confWin);
		}
		
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

		@Override
		public void handleValueChangeEvent(ALDSwingValueChangeEvent event) {
			this.fireALDSwingValueChangeEvent(event);
		}

		@Override
    public void disableComponent() {
			this.confWin.disableComponent();
    }

		@Override
    public void enableComponent() {
			this.confWin.enableComponent();
			this.confButton.setEnabled(true);
    }
		
		@Override
	  public void dispose() {
			// close the associated configuration window
			this.confWin.dispose();
	  }
	}	
	
	/**
	 * GUI element for displaying collections.
	 * <p>
	 * This button opens a window displaying a collection.
	 * 
	 * @author moeller
	 */
	private class CollectionShowButton extends JButton 
		implements ActionListener {

		/**
		 * Data to be displayed.
		 */
		private Object data;

		/**
		 * Descriptor associated with parameter object belongs to.
		 */
		private ALDParameterDescriptor descriptor;
		
		/**
		 * Constructor.
		 * @param 	obj		Object to be visualized on button press.
		 * @param		descr	Optional descriptor with additional information.
		 */
		public CollectionShowButton(Object obj, ALDParameterDescriptor descr) {
			super("Show collection data...");
			this.setActionCommand("showButtonPressed");
			this.addActionListener(this);
			this.data = obj;
			this.descriptor = descr;
		}
		
		@Override
    public void actionPerformed(ActionEvent e) {
			String cmd = e.getActionCommand(); 
			if (cmd.equals("showButtonPressed")) {
				Collection<?> c = (Collection<?>)this.data;
				JFrame win = new JFrame();
//				win.setSize(375, 25 * c.size());
				JPanel winPanel = new JPanel();
				GridLayout grl =  new GridLayout(c.size(),1);
				winPanel.setLayout(grl);
				int objectID = -1;
				for (Object o: c) {
					
					// objects being null are skipped
					if (o == null)
						continue;
					
					// update object ID counter
					++objectID;
					
					try {
						// get a component for a single element of the collection...
						ALDParameterDescriptor p = new ALDParameterDescriptor(
							this.descriptor.getName(), this.descriptor.getClass(), 
							this.descriptor.getExplanation(), 
							"Show entry " + objectID + "...",
							this.descriptor.isRequired(), this.descriptor.getField(),
							this.descriptor.getDataIOOrder(), 
							this.descriptor.getHandlingMode(),
							this.descriptor.getCallback(), 
							this.descriptor.parameterModificationMode(),
							this.descriptor.isInfo());
						JComponent comp = 
								ALDDataIOManagerSwing.getInstance().writeData(o, p); 
						if (comp == null) {
							String type = o.getClass().getSimpleName();
	      			Object[] options = { "OK" };
	      			JOptionPane.showOptionDialog(null, 
	      					"Null component received for type " + type + "!", "Warning", 
	      					JOptionPane.DEFAULT_OPTION, JOptionPane.WARNING_MESSAGE,
	      					null, options, options[0]);
	      			continue;
						}
	          winPanel.add(comp);
          } catch (ALDDataIOException e1) {
          	String pureMsg = "Unknown error!";
          	if (e1 instanceof ALDDataIOProviderException) {
          		switch (((ALDDataIOProviderException) e1).getType())
          		{
          		case SYNTAX_ERROR:
          		case OBJECT_TYPE_ERROR:
          		case OBJECT_INSTANTIATION_ERROR:
          		case FILE_IO_ERROR:
          		case INVALID_GUI_ELEMENT:
          		case SET_VALUE_FAILED:
          		case UNSPECIFIED_ERROR:
          			pureMsg = e1.getCommentString();
          			break;
          		}
          	}
          	else if (e1 instanceof ALDDataIOManagerException) {
          		switch (((ALDDataIOManagerException) e1).getType())
          		{
          		case NO_PROVIDER_FOUND:
          		case UNSPECIFIED_ERROR:
          			pureMsg = e1.getCommentString();
          			break;
          		}
          	}
      			Object[] options = { "OK" };
      			JOptionPane.showOptionDialog(null, 
      					"Displaying collection failed! Reason:\n" + pureMsg, "Warning", 
      					JOptionPane.DEFAULT_OPTION, JOptionPane.WARNING_MESSAGE,
      					null, options, options[0]);
          }
				}
				win.add(winPanel);
				if (this.descriptor != null)
					win.setTitle("Contents of collection <" 
							+ this.descriptor.getLabel() + ">");
				win.setVisible(true);
				win.validate();
				win.pack();
			}
    }
	}	

	/**
	 * Collection configuration window.
	 * 
	 * @author moeller
	 */
	private class CollectionConfigWindow extends ALDSwingValueChangeReporter
		implements ActionListener, ALDSwingValueChangeListener {

		/**
		 * Fixed minimal width of window.
		 */
		private static final int frameWidthMin = 500;
		
		/**
		 * Fixed minimal height of the configuration window.
		 */
		private static final int frameHeightMin = 300;
		
		/**
		 * Main frame.
		 */
		private JFrame window;

		/**
		 * Main panel of main frame.
		 */
		private JPanel mainPanel = null;
		
		/**
		 * Scroller element for collection elements.
		 */
		private JScrollPane scroller = null;
		
		/**
		 * Button to add an element.
		 */
		private JButton addButton;

		/**
		 * Button to delete an element;
		 */
		private JButton delButton;

		/**
		 * Button to move an element upwards.
		 */
		private JButton upButton;
		
		/**
		 * Button to move an element downwards.
		 */
		private JButton downButton;

		/**
		 * Button to close the configuration window.
		 */
		private JButton closeButton;
		
		/**
		 * Flag to remember if window is enabled or disabled.
		 */
		private boolean isEnabled;

		/**
		 * Stores the index of last element added (may vary due to element swaps).
		 */
		private int lastAdded = 0;
		
		/**
		 * Collection element field.
		 */
		private Field elemField;
		
		/**
		 * Collection element type.
		 */
		private Type elemFieldType;

		/**
		 * Collection element class.
		 */
		private Class<?> elemClass;

		/**
		 * Default collection object.
		 */
		private Object defObject;
		
		/**
		 * Parameter descriptor of associated parameter.
		 */
		private ALDParameterDescriptor paramDescriptor;
		
		/**
		 * List of current GUI components in window.
		 */
		private LinkedList<ALDSwingComponent> elemComps = 
																				new LinkedList<ALDSwingComponent>();

		/**
		 * Number of elements.
		 */
		private int elemCounter = 0;

		/**
		 * Default constructor.
		 * @param field		Field to specify input data objects.
		 * @param cl			Class of collection elements.
		 * @param obj			Initial value of collection.
		 * @param descr		Optional descriptor for additional information.
		 */
		public CollectionConfigWindow(Field field, 
				@SuppressWarnings("unused") Class<?> cl, Object obj, 
				ALDParameterDescriptor descr) {
			
			this.defObject= obj;
			this.elemField = field;
			this.elemFieldType = ALDCollectionDataIOHelper.lookupType(field);
			this.elemClass = (Class<?>)this.elemFieldType;
			this.paramDescriptor = descr;

			// initialize the window
			this.window = new JFrame();
			this.window.setResizable(true);
			String title = "unknown";
			if (descr != null) title = descr.getLabel();
			String type = this.elemClass.getSimpleName();
			this.window.setTitle("Collection <" +title+ ">, "
					+ "element type: <" + type + ">");
			this.window.setSize(frameWidthMin, frameHeightMin);
			this.window.setPreferredSize(
					new Dimension(frameWidthMin, frameHeightMin));
			this.window.pack();
			
			this.elemComps.clear();
			this.elemCounter = 0;
			try {
				if (this.defObject instanceof Collection<?>) {
					for (Object c: (Collection<?>)this.defObject) {
						ALDSwingComponent elemComp = 
								ALDDataIOManagerSwing.getInstance().createGUIElement(
//										this.elemField,this.elemClass, c, null);
										this.elemField, c.getClass(), c, null);
						// register window for value changes
						elemComp.addValueChangeEventListener(this);
						this.elemComps.add(elemComp);
						this.elemCounter++;
					}
				}
			} catch (ALDDataIOException e) {
				Object[] options = { "OK" };
				JOptionPane.showOptionDialog(null, 
					"Initializing collection view failed! Element invalid because...\n"+
							e.getCommentString(), 
							"Warning",JOptionPane.DEFAULT_OPTION,JOptionPane.WARNING_MESSAGE,
							null, options, options[0]);
			}
			this.isEnabled = true;
		}
		
		/**
		 * Extracts current collection data.
		 * 
		 * @param field	Field of collection elements.
		 * @param cl	Class of collection elements.
		 * @return	Current collection.
		 */
		@SuppressWarnings("unchecked")
    public Collection<?> readData(Field field, Class<?> cl) {
			if (this.elemComps.size() == 0)
				return null;
			Collection<Object> res = null;
			try {
				res = (Collection<Object>)cl.newInstance();
				// save current values
				for (ALDSwingComponent c: this.elemComps) {
					res.add(ALDDataIOManagerSwing.getInstance().readData(
																									field, this.elemClass, c));
				}
			} catch (Exception e) {
				System.err.println("ALDCollectionDataIOSwing::readData - error!!!");
				return null;
			}
			return res;
		}
		
		/**
		 * Updates current collection data.
		 * 
		 * @param value	New value.
		 */
		@SuppressWarnings("unchecked")
    public void setValue(Object value) {
			Collection<Object> coll = (Collection<Object>)value;

			// if value is null, just ignore the call to this function
			if (coll == null) {
				return;
			}
			
			// add new values
			try {

				// adjust size of list with components
				int elements = this.elemCounter;
				if (this.elemComps.size() < coll.size()) {
					for (int i=0; i<coll.size() - elements; ++i) {
						ALDSwingComponent comp =
								ALDDataIOManagerSwing.getInstance().createGUIElement(
										this.elemField, this.elemClass, null, null);
						comp.addValueChangeEventListener(this);
						this.elemComps.add(comp);
						this.elemCounter++;
						this.lastAdded= this.elemCounter-1;
					}
				}
				else if (this.elemComps.size() > coll.size()) {
					for (int i=0; i< elements - coll.size(); ++i) {
						// remove last element, dispose all its resources
						ALDSwingComponent obsoleteComp = this.elemComps.removeLast();
						obsoleteComp.dispose();
						this.elemCounter--;
					}
				}
			
				// update component values
				int i=0;
				for (Object obj: coll) {
					// skip objects in collection which are null
					if (obj == null) {
						continue;
					}
					if (   obj.getClass().equals(this.elemClass)
							|| this.elemClass.isAssignableFrom(obj.getClass()))
						ALDDataIOManagerSwing.getInstance().setValue(
								this.elemField, this.elemClass, this.elemComps.get(i), obj);
					else {
						ALDSwingComponent obsoleteComp = this.elemComps.get(i);
						obsoleteComp.dispose();
						ALDSwingComponent comp =
							ALDDataIOManagerSwing.getInstance().createGUIElement(
								this.elemField, this.elemClass, null, null);
						comp.addValueChangeEventListener(this);
						this.elemComps.set(i, comp);
						ALDDataIOManagerSwing.getInstance().setValue(
								this.elemField, obj.getClass(), this.elemComps.get(i), obj);
					}
					++i;
				}
				this.updateWindow();
			} catch (Exception e) {
				System.err.println("ALDCollectionDataIOSwing::setValue() - error!!!");
				e.printStackTrace();
			}
		}

		/**
		 * Deactivates the configuration window to prohibit value changes.
		 */
		public void disableComponent() {
			this.isEnabled = false;
			for (ALDSwingComponent comp: this.elemComps)
				comp.disableComponent();
			if (this.downButton != null)
				this.downButton.setEnabled(false);
			if (this.addButton != null)
				this.addButton.setEnabled(false);
			if (this.upButton != null)	
				this.upButton.setEnabled(false);
			if (this.delButton != null)
				this.delButton.setEnabled(false);
		}
		
		/**
		 * Reactivates the configuration window to allow for value changes.
		 */
		public void enableComponent() {
			this.isEnabled = true;
			for (ALDSwingComponent comp: this.elemComps)
				comp.enableComponent();
			if (this.downButton != null)
				this.downButton.setEnabled(true);
			if (this.addButton != null)
				this.addButton.setEnabled(true);
			if (this.upButton != null)
				this.upButton.setEnabled(true);
			if (this.delButton != null)
				this.delButton.setEnabled(true);
		}
		
		/**
		 * Disposes this window and all sub-components.
		 */
		public void dispose() {
			for (ALDSwingComponent comp : this.elemComps)
				comp.dispose();
			this.window.dispose();
		}
		
		/**
		 * Updates the collection in the window.
		 * <p>
		 * This function is called each time an element is added or removed or
		 * if two elements have been swapped.
		 */
		private void updateWindow() {
			
			int wWidth = this.window.getWidth() < frameWidthMin ? 
					frameWidthMin : this.window.getWidth();
			int wHeight = this.window.getHeight() < frameHeightMin ?
					frameHeightMin : this.window.getHeight();

			// on the first call, init the main panel
			if (this.mainPanel == null) {
				this.mainPanel = new JPanel();
				BorderLayout bl = new BorderLayout();
				this.mainPanel.setLayout(bl);

				// add buttons
				GridLayout gl = new GridLayout(1,5);
				JPanel tmpPanel = new JPanel();
				tmpPanel.setLayout(gl);
				this.addButton = new JButton(" + ");
				this.addButton.setActionCommand("addElement");
				this.addButton.addActionListener(this);
				this.delButton = new JButton(" - ");
				this.delButton.setActionCommand("delElement");
				this.delButton.addActionListener(this);
				this.upButton = new JButton("Up");
				this.upButton.setActionCommand("upElement");
				this.upButton.addActionListener(this);
				this.downButton = new JButton("Down");
				this.downButton.setActionCommand("downElement");
				this.downButton.addActionListener(this);
				this.closeButton = new JButton("Close");
				this.closeButton.setActionCommand("close");
				this.closeButton.addActionListener(this);
				tmpPanel.add(this.addButton);
				tmpPanel.add(this.delButton);
				tmpPanel.add(this.upButton);
				tmpPanel.add(this.downButton);
				tmpPanel.add(this.closeButton);
				this.mainPanel.add(tmpPanel,BorderLayout.NORTH);
				this.window.add(this.mainPanel);
			}
			else {
				this.mainPanel.remove(this.scroller);
			}
			
			// editable list of elements
			JPanel elementPanel = new JPanel(); 
			GridLayout glep = new GridLayout(this.elemCounter, 1, 5, 1);
			elementPanel.setLayout(glep);
			for (ALDSwingComponent c: this.elemComps) {
				elementPanel.add(c.getJComponent());
			}
			this.scroller = new JScrollPane(elementPanel);  
			this.mainPanel.add(this.scroller, BorderLayout.CENTER);
			
			// scale window to a proper size
			this.window.validate();
			int pWidth = 
					this.window.getWidth() < wWidth ? wWidth : this.window.getWidth(); 
			int pHeight =
					this.window.getHeight() < wHeight ? wHeight : this.window.getHeight(); 
			this.window.setPreferredSize(new Dimension(pWidth, pHeight));
			this.window.pack();
			this.window.repaint();
		}
		
		@Override
    public void actionPerformed(ActionEvent e) {
			String cmd = e.getActionCommand(); 
			if (cmd.equals("configButtonPressed")) {
				this.updateWindow();
				this.window.setVisible(true);
			}
			else if (cmd.equals("addElement")) {
				// if window is disabled, ignore clicks
				if (!this.isEnabled)
					return;
				try {
					ALDSwingComponent comp = 
							ALDDataIOManagerSwing.getInstance().createGUIElement(
									this.elemField, this.elemClass, null, null);
					comp.addValueChangeEventListener(this);
					this.elemComps.add(comp);
					this.elemCounter++;
					this.lastAdded= this.elemCounter-1;
	        this.updateWindow();
        } catch (ALDDataIOException e1) {
    			Object[] options = { "OK" };
    			JOptionPane.showOptionDialog(null, 
    					"Adding element failed! Current collection invalid!\n" + 
    					"Reason:\n" + e1.getCommentString(), 
    					"Warning",JOptionPane.DEFAULT_OPTION,JOptionPane.WARNING_MESSAGE,
    					null, options, options[0]);
        }
			}
			else if (cmd.equals("delElement")) {
				// if window is disabled, ignore clicks
				if (!this.isEnabled)
					return;
				if (!(this.elemCounter==0)) {
					// remove last element, dispose all its resources
					ALDSwingComponent obsoleteComp = this.elemComps.removeLast();
					obsoleteComp.dispose();
					--this.elemCounter;
				}
				this.updateWindow();
			}
			// move the recently added element one position up
			else if (cmd.equals("downElement")) {
				// if window is disabled, ignore clicks
				if (!this.isEnabled)
					return;
				// check if element has meanwhile been deleted,
				// if so, just ignore the request
				if (this.lastAdded >= this.elemComps.size() - 1)
					return;
				ALDSwingComponent ccomp = this.elemComps.get(this.lastAdded);
				ALDSwingComponent cnext = this.elemComps.get(this.lastAdded + 1);
				this.elemComps.set(this.lastAdded, cnext);
				this.elemComps.set(this.lastAdded+1, ccomp);
				++this.lastAdded;
				this.updateWindow();
			}
			else if (cmd.equals("upElement")) {
				// if window is disabled, ignore clicks
				if (!this.isEnabled)
					return;
				if (this.lastAdded == 0)
					return;
				// check if element has meanwhile been deleted,
				// if so, just ignore the request
				if (this.lastAdded >= this.elemComps.size())
					return;
				ALDSwingComponent ccomp = this.elemComps.get(this.lastAdded);
				ALDSwingComponent cnext = this.elemComps.get(this.lastAdded - 1);
				this.elemComps.set(this.lastAdded, cnext);
				this.elemComps.set(this.lastAdded-1, ccomp);
				--this.lastAdded;
				this.updateWindow();
			}
			else if (cmd.equals("close")) {
				this.window.setVisible(false);
			}
	  }

		@Override
		public void handleValueChangeEvent(ALDSwingValueChangeEvent event) {
			this.fireALDSwingValueChangeEvent(event);
		}		
	}
}
