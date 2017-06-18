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

import java.awt.Component;
import java.util.Vector;

import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.plaf.basic.BasicComboBoxRenderer;

import de.unihalle.informatik.Alida.dataio.ALDDataIOManagerSwing;
import de.unihalle.informatik.Alida.dataio.provider.swing.events.ALDSwingValueChangeEvent;
import de.unihalle.informatik.Alida.operator.ALDParameterDescriptor;

/**
 * Alida-specific Swing component linked to a {@link JList}.
 * <p>
 * This component wraps a {@link JList} thereby triggering value change
 * events and adding support for tooltips.
 * 
 * @author moeller
 */
public class ALDSwingComponentList extends ALDSwingComponent 
	implements ListSelectionListener {
	
	/**
	 * Descriptor of associated (operator) parameter.
	 */
	protected ALDParameterDescriptor paramDescriptor;
	
	/**
	 * Associated Swing component.
	 */
	protected JList<ALDSwingComponentItem> compList = null;

	/**
	 * Items represented in list.
	 */
	protected Vector<ALDSwingComponentItem> items;
	
	/**
	 * Flag to ensure that no events are triggered if item change is triggered
	 * from internal.
	 */
	private boolean ignoreEvents = false;
	
	/**
	 * Default constructor.
	 * @param descr	Descriptor of associated parameter.
	 * @param its	Set of items to be shown in combobox.
	 */
	public ALDSwingComponentList(ALDParameterDescriptor descr,
			Vector<ALDSwingComponentItem> its) {
		this.paramDescriptor = descr;
		this.compList = new JList<ALDSwingComponentItem>(its);
		this.compList.setLayoutOrientation(JList.VERTICAL);
		this.compList.setVisibleRowCount(-1);
		this.compList.addListSelectionListener(this);
		this.items = its;
		// add our own tooltip renderer
//		this.compList.setRenderer(new ComboBoxTooltipRenderer(its));
	}

	@Override
	public JList<ALDSwingComponentItem> getJComponent() {
		return this.compList;
	}

	@Override
	public void valueChanged(ListSelectionEvent e) {
		if (this.ignoreEvents)
			return;
		if (!e.getValueIsAdjusting()) {
			if (ALDDataIOManagerSwing.getInstance().isTriggerValueChangeEvents()) {
				this.fireALDSwingValueChangeEvent(
						new ALDSwingValueChangeEvent(this, this.paramDescriptor));
			}
		}
	}
	
	/**
	 * Selects the given items.
	 * @param list		Objects which should be selected.
	 */
	public void setSelectedItems(Vector<ALDSwingComponentItem> sItems) {
		
		// make sure that triggered events are ignored
		this.ignoreEvents = true;
		this.compList.clearSelection();
		int id = 0, entry = 0;
		int[] sIndices = new int[sItems.size()];
		for(ALDSwingComponentItem i: this.items){
			if (sItems.contains(i)) {
				sIndices[entry] = id;
				++entry;
			}
			++id;
		}
		this.compList.setSelectedIndices(sIndices);
//		this.compList.updateUI();
//			// if tooltip text is given, use this for item matching
//			if (boxItem.getTooltip() != null) {
//				if (obj.getClass().getCanonicalName().equals(boxItem.getTooltip())) {
//					this.compComboBox.setSelectedIndex(i);
//					// reset event handling to default behaviour
//					this.ignoreEvents = false;
//					return;
//				}
//			}
//			// otherwise use item text directly for matching
//			else if (obj.toString().equals(boxItem.getItemText())) {
//				this.compComboBox.setSelectedIndex(i);
//				// reset event handling to default behaviour
//				this.ignoreEvents = false;
//				return;
//			}
		// reset event handling to default behaviour
		this.ignoreEvents = false;
	}
	
	/**
	 * Get set of selected items.
	 * @return List of selected items.
	 */
	public Vector<ALDSwingComponentItem> getSelectedItems() {
		Vector<ALDSwingComponentItem> sItems = new Vector<>();
		int[] sIDs = this.compList.getSelectedIndices();
		for(int i = 0; i< sIDs.length; ++i) {
			sItems.add(this.items.elementAt(sIDs[i]));
		}
		return sItems;
	}

	/**
	 * Tooltip renderer class, adds a tooltip to each item in the combobox.
	 * <p>
	 * Source code originally taken from here:
	 * @see <a href="http://www.java2s.com/Code/Java/Swing-Components/ToolTipComboBoxExample.htm">Java Swing documentation</a>
	 * 
	 * @author moeller
	 */
	protected class ComboBoxTooltipRenderer extends BasicComboBoxRenderer {
		
		/**
		 * List of items represented by combobox.
		 */
		private Vector<ALDSwingComponentItem> items = null;
		
		/**
		 * Default constructor.
		 * @param itms	Set of combobox item objects.
		 */
		public ComboBoxTooltipRenderer(Vector<ALDSwingComponentItem> itms){
			this.items = itms;
		}
		
    @Override
    public Component getListCellRendererComponent(JList list, Object value,
        int index, boolean isSelected, boolean cellHasFocus) {
      if (isSelected) {
        setBackground(list.getSelectionBackground());
        setForeground(list.getSelectionForeground());
        if (  this.items != null 
        		&& -1 < index	&& index < this.items.size()) {
          list.setToolTipText(this.items.get(index).getTooltip());
        }
      } else {
        setBackground(list.getBackground());
        setForeground(list.getForeground());
      }
      setFont(list.getFont());
      setText((value == null) ? "" : value.toString());
      return this;
    }
  }

	@Override
  public void disableComponent() {
		this.compList.setEnabled(false);
  }

	@Override
  public void enableComponent() {
		this.compList.setEnabled(true);
  }
	
	@Override
  public void dispose() {
		// nothing to do here
  }
}
