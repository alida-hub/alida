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
import de.unihalle.informatik.Alida.dataio.provider.swing.components.ALDSwingComponent;
import de.unihalle.informatik.Alida.dataio.provider.swing.components.ALDSwingComponentLabel;
import de.unihalle.informatik.Alida.exceptions.*;
import de.unihalle.informatik.Alida.exceptions.ALDDataIOProviderException.ALDDataIOProviderExceptionType;
import de.unihalle.informatik.Alida.operator.ALDParameterDescriptor;

import javax.swing.*;

import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.lang.reflect.Field;
import java.util.*;

/**
 * Class for generic showing / saving of hash maps to GUI in Alida.
 * <p>
 * Note that the keys are converted to data type {@link String}.
 * 
 * @author moeller
 */
@ALDDataIOProvider
public class ALDHashMapDataIOSwing 
	extends ALDDataIOSwingInitialGUIValueDefaultHandler {

	/**
	 * Interface method to announce class for which IO is provided for.
	 * 
	 * @return  Collection of classes provided.
	 */
	@Override
	public Collection<Class<?>> providedClasses() {
		LinkedList<Class<?>> classes = new LinkedList<Class<?>>();
		classes.add(HashMap.class);
		return classes;
	}
	
	/** 
	 * Generic reading of hash maps.
	 */
	/* (non-Javadoc)
	 * @see de.unihalle.informatik.Alida.helpers.ALDDataIOSwing#createGUIElement(java.lang.Class, java.lang.Object)
	 */
	@Override
	public ALDSwingComponent createGUIElement(
			Field field, Class<?> cl, Object obj, ALDParameterDescriptor d) {
		return new ALDSwingComponentLabel( 
			"Sorry, graphical I/O of hash maps not yet supported!");
	}

	@Override
  public void setValue(Field field, Class<?> cl, 
  		ALDSwingComponent guiElement,	Object value) {
//		if (!(guiElement instanceof HashmapConfigButton))
//			throw new ALDDataIOProviderException(
//					ALDDataIOProviderExceptionType.INVALID_GUI_ELEMENT, 
//					"HashmapDataIO: setValue() received invalid GUI element!");
//		((HashmapConfigButton)guiElement).setValue(field, cl, value);
		System.err.println("[ALDHashMapDataIOSwing] reading hash maps " 
				+ "not yet supported, sorry...");
  }

	@Override
	public Object readData(
			Field field, Class<?> cl, ALDSwingComponent guiElement) {
//		if (!(guiElement instanceof HashmapConfigButton))
//			throw new ALDDataIOProviderException(
//					ALDDataIOProviderExceptionType.INVALID_GUI_ELEMENT, 
//					"HashmapDataIO: readData received invalid GUI element!");
//		return ((HashmapConfigButton)guiElement).readData(field, cl);
		System.err.println("[ALDHashMapDataIOSwing] reading hash maps " 
				+ "not yet supported, sorry...");
		return null;
	}

	@Override
	public JComponent writeData(Object obj, ALDParameterDescriptor descr) 
			throws ALDDataIOProviderException {
		if (!(obj instanceof HashMap))
			throw new ALDDataIOProviderException(
				ALDDataIOProviderExceptionType.OBJECT_TYPE_ERROR, 
					"[ALDHashMapDataIOSwing] object to write has wrong type!");
		// return a button to show a window with the elements
		return new HashmapShowButton(obj, descr);
	}
	
	/**
	 * GUI element for displaying hash maps.
	 * <p>
	 * This button opens a window displaying a {@link HashMap}.
	 * 
	 * @author moeller
	 */
	private class HashmapShowButton extends JButton 
		implements ActionListener {

		/**
		 * Data, i.e. hash map, to be displayed.
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
		public HashmapShowButton(Object obj, ALDParameterDescriptor descr) {
			super("Show hash map data...");
			this.setActionCommand("showButtonPressed");
			this.addActionListener(this);
			this.data = obj;
			this.descriptor = descr;
		}
		
		@Override
    public void actionPerformed(ActionEvent e) {
			String cmd = e.getActionCommand(); 
			if (cmd.equals("showButtonPressed")) {
				HashMap<?,?> c = (HashMap<?,?>)this.data;
				JFrame win = new JFrame();
				JPanel winPanel = new JPanel();
				GridLayout grl =  new GridLayout(c.size(),2);
				winPanel.setLayout(grl);
				Set<?> keys = c.keySet();
				for (Object o: keys) {
					String key = o.toString();
					Object dataItem = c.get(o);
					try {
						// get a component for a single element of the hash map...
						JComponent comp = 
							ALDDataIOManagerSwing.getInstance().writeData(
								dataItem, null); 
						if (comp == null) {
							String type = o.getClass().getSimpleName();
	      			Object[] options = { "OK" };
	      			JOptionPane.showOptionDialog(null, 
	      				"Null component received for type " + type + "!", 
	      				"Warning", 
	      				JOptionPane.DEFAULT_OPTION, JOptionPane.WARNING_MESSAGE,
	      				null, options, options[0]);
	      			continue;
						}
						winPanel.add(new JLabel("<html>&nbsp;&nbsp;&nbsp;" + key
								+ "&nbsp;&nbsp;&nbsp;</html>"));
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
     					"Displaying hash map failed! Reason:\n" + pureMsg, 
     						"Warning", 
      					JOptionPane.DEFAULT_OPTION, JOptionPane.WARNING_MESSAGE,
      					null, options, options[0]);
          }
				}
				win.add(winPanel);
				if (this.descriptor != null)
					win.setTitle("Contents of HashMap parameter <" 
							+ this.descriptor.getLabel() + ">");
				win.setVisible(true);
				win.validate();
				win.pack();
			}
    }
	}	
}
