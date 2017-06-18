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

/* 
 * Most recent change(s):
 * 
 * $Rev: 5413 $
 * $Date: 2012-04-12 11:01:03 +0200 (Do, 12 Apr 2012) $
 * $Author: moeller $
 * 
 */

package de.unihalle.informatik.Alida.dataio.provider.swing.components;

/**
 * Alida-specific combobox item linked to a {@link ALDSwingComponentComboBox}.
 * <p>
 * An instance of this class contains all information associated with an entry
 * in a {@link ALDSwingComponentComboBox}, i.e. the actual object, its textual
 * representation and an optional tooltip text. Note that object and tooltip
 * text are allowed to be null.
 * 
 * @author moeller
 */
public class ALDSwingComponentItem 
	implements Comparable<ALDSwingComponentItem> {
	
	/**
	 * Item linked to the combobox entry.
	 */
	private Object item;
	
	/**
	 * Textual representation of the object.
	 */
	private String itemText;
	
	/**
	 * Tooltip text shown upon hovering over the entry.
	 */
	private String tooltip;
	
	/**
	 * Default constructor.
	 * @param _item			Object linked to item.
	 * @param _text			Textual representation of object.
	 * @param _tooltip	Tooltip text.
	 */
	public ALDSwingComponentItem(
															Object _item, String _text, String _tooltip) {
		this.item = _item;
		this.itemText = _text;
		this.tooltip = _tooltip;
	}
	
	/**
	 * Get the object associated with this item.
	 * @return	Object linked to item.
	 */
	public Object getObject() {
		return this.item;
	}

	/**
	 * Get textual representation of object.
	 * @return	Textual representation.
	 */
	public String getItemText() {
		return this.itemText;
	}
	
	/**
	 * Get tooltip text.
	 * @return	Tooltip text.
	 */
	public String getTooltip() {
		return this.tooltip;
	}

	@Override
  public int compareTo(ALDSwingComponentItem o) {
		return this.itemText.compareTo(o.itemText);
  }

	@Override
  public String toString() {
		return this.itemText;
	}
	
}
