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
 * $Rev$
 * $Date$
 * $Author$
 * 
 */

package de.unihalle.informatik.Alida.helpers;

import java.lang.ref.ReferenceQueue;
import java.lang.ref.WeakReference;


/**
 * Weak reference hashmap entry.
 * 
 * @author moeller
 */
@SuppressWarnings("rawtypes")
class ALDWeakHashMapEntry extends WeakReference {
	
	/**
	 * Hash value of referenced object.
	 */
	protected final int hash;

	/**
	 * Referenced object itself.
	 */
	protected volatile Object value;
	
	/**
	 * Pointer to the next reference in hashmap list.
	 */
	protected volatile ALDWeakHashMapEntry next;

	/**
	 * Constructor.
	 * 
	 * @param _hash		Hash value.
	 * @param _key		Key object.
	 * @param _value	Hashmap value of the object.
	 * @param _next		Reference to subsequent object.
	 * @param queue		Queue for managing references of deleted objects. 
	 */
	@SuppressWarnings("unchecked")
  protected ALDWeakHashMapEntry(int _hash, Object _key, Object _value, 
  	ALDWeakHashMapEntry _next, ReferenceQueue<Object> queue) {
		super(_key, queue);
		this.hash = _hash;
		this.value = _value;
		this.next = _next;
	}

  /**
   * Returns the key of this hashmap element.
   */
  public Object getKey() {
		return this.get();
  }

  /**
   * Returns the value of this hashmap element.
   */
  public Object getValue() {
	  return this.value;
  }

  /**
   * Returns reference to subsequent hash element.
   */
  public ALDWeakHashMapEntry getNext() {
  	return this.next;
  }
  
  /**
   * Sets the value of the hashmap entry.
   * 
   * @param _value	New value of this object reference.
   * @return	Reference to the new value.
   */
  public Object setValue(Object _value) {
		this.value = _value;
		return this.value;
  }
}
