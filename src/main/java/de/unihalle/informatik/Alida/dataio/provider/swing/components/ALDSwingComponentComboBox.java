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
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.Vector;

import javax.swing.*;
import javax.swing.plaf.basic.BasicComboBoxRenderer;

import de.unihalle.informatik.Alida.dataio.ALDDataIOManagerSwing;
import de.unihalle.informatik.Alida.dataio.provider.swing.events.*;
import de.unihalle.informatik.Alida.operator.ALDParameterDescriptor;

/**
 * Alida-specific Swing component linked to a {@link JComboBox}.
 * <p>
 * This component wraps a {@link JComboBox} thereby triggering value change
 * events and adding support for tooltips.
 * 
 * @author moeller
 */
public class ALDSwingComponentComboBox extends ALDSwingComponent 
	implements ItemListener {
	
	/**
	 * Descriptor of associated (operator) parameter.
	 */
	protected ALDParameterDescriptor paramDescriptor;
	
	/**
	 * Associated Swing component.
	 */
	protected JComboBox<ALDSwingComponentItem> compComboBox = null;

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
	public ALDSwingComponentComboBox(ALDParameterDescriptor descr,
			Vector<ALDSwingComponentItem> its){
		this.paramDescriptor = descr;
		this.compComboBox = new JComboBox<ALDSwingComponentItem>(its);
		this.compComboBox.addItemListener(this);
		// add our own tooltip renderer
		this.compComboBox.setRenderer(new ComboBoxTooltipRenderer(its));
	}

	@Override
	public JComboBox getJComponent() {
		return this.compComboBox;
	}

	@Override
	public void itemStateChanged(ItemEvent e) {
		if (this.ignoreEvents)
			return;
		if (   e.getStateChange() == ItemEvent.SELECTED 
				&& ALDDataIOManagerSwing.getInstance().isTriggerValueChangeEvents()) {
			this.fireALDSwingValueChangeEvent(
				new ALDSwingValueChangeEvent(this, this.paramDescriptor));
		}
	}
	
	/**
	 * Updates the set of items managed by the combo box.
	 * @param its	List of new items.
	 */
	public void updateItems(Vector<ALDSwingComponentItem> its) {
		this.compComboBox.removeAllItems();
		for (ALDSwingComponentItem si: its)
			this.compComboBox.addItem(si);
		this.compComboBox.setRenderer(new ComboBoxTooltipRenderer(its));
	}

	/**
	 * Clears the combo box, i.e. removes the current set of items.
	 */
	public void clearItems() {
		this.compComboBox.removeAllItems();
	}

	/**
	 * Selects the item associated with the given object.
	 * @param obj		Object which should be selected.
	 */
	public void setSelectedItem(Object obj) {
		// make sure that triggered events are ignored
		this.ignoreEvents = true;
		int itemNum = this.compComboBox.getItemCount();
		for (int i=0;i<itemNum;++i) {
			Object item = this.compComboBox.getItemAt(i);
			ALDSwingComponentItem boxItem =
					(ALDSwingComponentItem)item;
			// if value is null, set selection to first entry 
			if (obj == null) {
				this.compComboBox.setSelectedIndex(0);
				// reset event handling to default behaviour
				this.ignoreEvents = false;
				return;
			}
			// if tooltip text is given, use this for item matching
			if (boxItem.getTooltip() != null) {
				if (obj.getClass().getCanonicalName().equals(boxItem.getTooltip())) {
					this.compComboBox.setSelectedIndex(i);
					// reset event handling to default behaviour
					this.ignoreEvents = false;
					return;
				}
			}
			// otherwise use item text directly for matching
			else if (obj.toString().equals(boxItem.getItemText())) {
				this.compComboBox.setSelectedIndex(i);
				// reset event handling to default behaviour
				this.ignoreEvents = false;
				return;
			}
		}
		// reset event handling to default behaviour
		this.ignoreEvents = false;
	}
	
	/**
	 * Tooltip renderer class, adds a tooltip to each item in the combobox.
	 * <p>
	 * Source code originally taken from here:
	 * @see <a href="http://www.java2s.com/Code/Java/Swing-Components/ToolTipComboBoxExample.htm">Java Swing documentation</a>
	 * 
	 * @author moeller
	 */
	protected class ComboBoxTooltipRenderer	extends BasicComboBoxRenderer {
		
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
		this.compComboBox.setEnabled(false);
  }

	@Override
  public void enableComponent() {
		this.compComboBox.setEnabled(true);
  }
	
	@Override
  public void dispose() {
		// nothing to do here
  }
}
