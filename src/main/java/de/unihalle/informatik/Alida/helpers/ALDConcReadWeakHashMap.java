/* IMPORTANT NOTICE!!!
 * This file in its original version was taken from 
 * 
 * http://gee.cs.oswego.edu/dl/classes/EDU/oswego/cs/dl/util/concurrent/intro.html
 * 
 * For license details, see below.
 */

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
  File: ConcurrentReaderHashMap

  Written by Doug Lea. Adapted and released, under explicit
  permission, from JDK1.2 HashMap.java and Hashtable.java which
  carries the following copyright:

     * Copyright 1997 by Sun Microsystems, Inc.,
     * 901 San Antonio Road, Palo Alto, California, 94303, U.S.A.
     * All rights reserved.
     *
     * This software is the confidential and proprietary information
     * of Sun Microsystems, Inc. ("Confidential Information").  You
     * shall not disclose such Confidential Information and shall use
     * it only in accordance with the terms of the license agreement
     * you entered into with Sun.

  History:
  Date       Who                What
  28oct1999  dl               Created
  14dec1999  dl               jmm snapshot
  19apr2000  dl               use barrierLock
  12jan2001  dl               public release
  17nov2001  dl               Minor tunings
  20may2002  dl               BarrierLock can now be serialized.
  09dec2002  dl               Fix interference checks.
*/

package de.unihalle.informatik.Alida.helpers;

import java.io.IOException;
import java.lang.ref.ReferenceQueue;


/*
 * Original file comment following now...
 * (note that the class now provides minor functionality compared to the 
 *  original one as is was adapted to the specific needs of Alida)
 */

/**
 * A version of Hashtable that supports concurrent reading/exclusive writing.
 * <p>
 * <b>Note:</b><br>
 * This file in its original version was taken from: 
 * <i>
 * http://gee.cs.oswego.edu/dl/classes/EDU/oswego/cs/dl/util/concurrent/intro.html
 * </i>
 * <p>
 * According to the webpage there are no license restrictions on using the 
 * file:
 * <p>
 * "Do I need a license to use it? Can I get one?
 *   No!"
 * <p>  
 * You can find the original header of the file, which originally was named
 * 'ConcurrentReaderHashMap.java', below. For using it with Alida the class 
 * was significantly modified and also simplified as Alida requires only all 
 * very small amount of the original functionality.  
 * <p>
 * <b>
 * Details (non-modified comment of original version):</b><br>
 * This version of Hashtable supports mostly-concurrent reading, but
 * exclusive writing.  Because reads are not limited to periods
 * without writes, a concurrent reader policy is weaker than a classic
 * reader/writer policy, but is generally faster and allows more
 * concurrency. This class is a good choice especially for tables that
 * are mainly created by one thread during the start-up phase of a
 * program, and from then on, are mainly read (with perhaps occasional
 * additions or removals) in many threads.  If you also need concurrency
 * among writes, consider instead using ConcurrentHashMap.
 * <p>
 * Successful retrievals using get(key) and containsKey(key) usually
 * run without locking. Unsuccessful ones (i.e., when the key is not
 * present) do involve brief synchronization (locking).  Also, the
 * size and isEmpty methods are always synchronized.
 * <p> 
 * Because retrieval operations can ordinarily overlap with
 * writing operations (i.e., put, remove, and their derivatives),
 * retrievals can only be guaranteed to return the results of the most
 * recently <em>completed</em> operations holding upon their
 * onset. Retrieval operations may or may not return results
 * reflecting in-progress writing operations.  However, the retrieval
 * operations do always return consistent results -- either those
 * holding before any single modification or after it, but never a
 * nonsense result.  For aggregate operations such as putAll and
 * clear, concurrent reads may reflect insertion or removal of only
 * some entries. In those rare contexts in which you use a hash table
 * to synchronize operations across threads (for example, to prevent
 * reads until after clears), you should either encase operations
 * in synchronized blocks, or instead use java.util.Hashtable.
 *
 * <p>
 *
 * This class also supports optional guaranteed exclusive reads, simply by 
 * surrounding a call within a synchronized block, as in <br> 
 * <code>ConcurrentReaderHashMap t; ... Object v; <br>
 * synchronized(t) { v = t.get(k); } </code> <br>
 *
 * But this is not usually necessary in practice. 
 * For example, it is generally inefficient to write:
 *
 * <pre>
 *   ConcurrentReaderHashMap t; ...            // Inefficient version
 *   Object key; ...
 *   Object value; ...
 *   synchronized(t) { 
 *     if (!t.containsKey(key))
 *       t.put(key, value);
 *       // other code if not previously present
 *     }
 *     else {
 *       // other code if it was previously present
 *     }
 *   }
 * </pre>
 * Instead, if the values are intended to be the same in each case, 
 * just take advantage of the fact that put returns
 * null if the key was not previously present:
 * <pre>
 *   ConcurrentReaderHashMap t; ...                // Use this instead
 *   Object key; ...
 *   Object value; ...
 *   Object oldValue = t.put(key, value);
 *   if (oldValue == null) {
 *     // other code if not previously present
 *   }
 *   else {
 *     // other code if it was previously present
 *   }
 *</pre>
 * <p>
 *
 * Iterators and Enumerations (i.e., those returned by
 * keySet().iterator(), entrySet().iterator(), values().iterator(),
 * keys(), and elements()) return elements reflecting the state of the
 * hash table at some point at or since the creation of the
 * iterator/enumeration.  They will return at most one instance of
 * each element (via next()/nextElement()), but might or might not
 * reflect puts and removes that have been processed since they were
 * created.  They do <em>not</em> throw ConcurrentModificationException.
 * However, these iterators are designed to be used by only one
 * thread at a time. Sharing an iterator across multiple threads may
 * lead to unpredictable results if the table is being concurrently
 * modified.  Again, you can ensure interference-free iteration by
 * enclosing the iteration in a synchronized block.  <p>
 *
 * This class may be used as a direct replacement for any use of
 * java.util.Hashtable that does not depend on readers being blocked
 * during updates. Like Hashtable but unlike java.util.HashMap,
 * this class does NOT allow <tt>null</tt> to be used as a key or
 * value.  This class is also typically faster than ConcurrentHashMap
 * when there is usually only one thread updating the table, but 
 * possibly many retrieving values from it.
 * <p>
 *
 * Implementation note: A slightly faster implementation of
 * this class will be possible once planned Java Memory Model
 * revisions are in place.
 *
 * <p>
 * [<a href="http://gee.cs.oswego.edu/dl/classes/EDU/oswego/cs/dl/util/concurrent/intro.html"> Introduction to this package. </a>]
 */
public class ALDConcReadWeakHashMap
{
  /*
    The basic strategy is an optimistic-style scheme based on
    the guarantee that the hash table and its lists are always
    kept in a consistent enough state to be read without locking:

    * Read operations first proceed without locking, by traversing the
       apparently correct list of the apparently correct bin. If an
       entry is found, but not invalidated (value field null), it is
       returned. If not found, operations must recheck (after a memory
       barrier) to make sure they are using both the right list and
       the right table (which can change under resizes). If
       invalidated, reads must acquire main update lock to wait out
       the update, and then re-traverse.

    * All list additions are at the front of each bin, making it easy
       to check changes, and also fast to traverse.  Entry next
       pointers are never assigned. Remove() builds new nodes when
       necessary to preserve this.

    * Remove() (also clear()) invalidates removed nodes to alert read
       operations that they must wait out the full modifications.
 
  */

  /** A Serializable class for barrier lock. **/
  @SuppressWarnings("serial")
	protected static class BarrierLock implements java.io.Serializable {}

  /**
   * Lock used only for its memory effects.
   **/
  protected final BarrierLock barrierLock = new BarrierLock();

  /**
   * field written to only to guarantee lock ordering.
   **/
  protected transient Object lastWrite;

  /**
   * Force a memory synchronization that will cause
   * all readers to see table. Call only when already
   * holding main synch lock.
   **/
  protected final void recordModification(Object x) { 
    synchronized(this.barrierLock) {
      this.lastWrite = x;
    }
  }

  /**
   * Get ref to table; the reference and the cells it
   * accesses will be at least as fresh as from last
   * use of barrierLock
   **/
  protected final ALDWeakHashMapEntry[] getTableForReading() { 
    synchronized(this.barrierLock) {
      return this.table; 
    }
  }

  /**
   * The default initial number of table slots for this table (32).
   * Used when not otherwise specified in constructor.
   **/
  public static int DEFAULT_INITIAL_CAPACITY = 32; 

  /**
   * The minimum capacity, used if a lower value is implicitly specified
   * by either of the constructors with arguments.  
   * MUST be a power of two.
   */
  private static final int MINIMUM_CAPACITY = 4;
  
  /**
   * The maximum capacity, used if a higher value is implicitly specified
   * by either of the constructors with arguments.
   * MUST be a power of two <= 1<<30.
   */
  private static final int MAXIMUM_CAPACITY = 1 << 30;
  
  /**
   * The default load factor for this table (1.0).
   * Used when not otherwise specified in constructor.
   **/
  public static final float DEFAULT_LOAD_FACTOR = 0.75f; 

  /**
   * The hash table data.
   */
  protected transient ALDWeakHashMapEntry[] table;

  /**
   * The total number of mappings in the hash table.
   */
  protected transient int count;

  /**
   * The table is rehashed when its size exceeds this threshold.  (The
   * value of this field is always (int)(capacity * loadFactor).)
   *
   * @serial
   */
  protected int threshold;

  /**
   * The load factor for the hash table.
   *
   * @serial
   */
  protected float loadFactor;
  
  /**
   * Queue for managing references to deleted objects.
   */
  protected static ReferenceQueue<Object> refQueue = 
  	new ReferenceQueue<Object>();

  /**
   * Returns appropriate capacity for argument.
   * <p>
   * The appropriate capacity is calculated as the power of two of 
   * the specified initial capacity argument.
   */
  private int p2capacity(int initialCapacity) {
    int cap = initialCapacity;
    
    // Compute the appropriate capacity
    int result;
    if (cap > MAXIMUM_CAPACITY || cap < 0) {
      result = MAXIMUM_CAPACITY;
    } else {
      result = MINIMUM_CAPACITY;
      while (result < cap)
        result <<= 1;
    }
    return result;
  }

  /**
   * Return hash code for Object x. Since we are using power-of-two
   * tables, it is worth the effort to improve hashcode via
   * the same multiplicative scheme as used in IdentityHashMap.
   */
  private static int hash(Object x) {
    int h = x.hashCode();
    // Multiply by 127 (quickly, via shifts), and mix in some high
    // bits to help guard against bunching of codes that are
    // consecutive or equally spaced.
    return ((h << 7) - h + (h >>> 9) + (h >>> 17));
  }

  /** 
   * Check for equality of non-null references x and y. 
   * <p>
   * We are going to check for object references, not equality!
   */
  protected boolean eq(Object x, Object y) {
    return x == y ; // || x.equals(y);
  }

  /**
   * Constructs a new, empty map with the specified initial 
   * capacity and the specified load factor. 
   *
   * @param initialCapacity the initial capacity
   *  The actual initial capacity is rounded to the nearest power of two.
   * @param loadFac  the load factor of the ConcurrentReaderHashMap
   * @throws IllegalArgumentException  if the initial maximum number 
   *               of elements is less
   *               than zero, or if the load factor is nonpositive.
   */
  private ALDConcReadWeakHashMap(int initialCapacity,float loadFac) {
    if (loadFac <= 0)
      throw new IllegalArgumentException("Illegal Load factor: "+
                                         loadFac);
    this.loadFactor = loadFac;

    int cap = p2capacity(initialCapacity);

    this.table = new ALDWeakHashMapEntry[cap];
    this.threshold = (int)(cap * loadFac);
  }

  /**
   * Constructs a new, empty map with the specified initial 
   * capacity and default load factor.
   *
   * @param   initialCapacity   the initial capacity of the 
   *                            ConcurrentReaderHashMap.
   * @throws    IllegalArgumentException if the initial maximum number 
   *              of elements is less
   *              than zero.
   */
  public ALDConcReadWeakHashMap(int initialCapacity) {
    this(initialCapacity, DEFAULT_LOAD_FACTOR);
  }

  /**
   * Constructs a new, empty map with a default initial capacity
   * and load factor.
   */
  public ALDConcReadWeakHashMap() {
    this(DEFAULT_INITIAL_CAPACITY, DEFAULT_LOAD_FACTOR);
  }

  /**
   * Constructs a new map with the same mappings as the given map.  The
   * map is created with a capacity of twice the number of mappings in
   * the given map or 16 (whichever is greater), and a default load factor.
   */
//  public MTBConcurrentReaderWeakHashMap(Map t) {
//        this(Math.max((int) (t.size() / DEFAULT_LOAD_FACTOR) + 1, 16),
//             DEFAULT_LOAD_FACTOR);
//    putAll(t);
//  }

  /**
   * Returns the number of key-value mappings in this map.
   */
//  @Override
  public synchronized int size() {
  	this.removeWeakKeys();
  	return this.count;
  }

  /**
   * Returns <tt>true</tt> if this map contains no key-value mappings.
   */
//  @Override
  public synchronized boolean isEmpty() {
    return this.count == 0;
  }

  /**
   * Returns the value to which the specified key is mapped in this table.
   *
   * @param   key		Key in the table.
   * @return  Value to which the key is mapped in this table;
   *          <code>null</code> if the key is not mapped to any value.
   * @exception NullPointerException if the key is <code>null</code>.
   * @see     #put(Object, Object)
   */
//  @Override
  public Object get(Object key) {

    // throw null pointer exception if key null
    int hash = hash(key);
    
    /* 
       Start off at the apparently correct bin.  If entry is found, we
       need to check after a barrier anyway.  If not found, we need a
       barrier to check if we are actually in right bin. So either
       way, we encounter only one barrier unless we need to retry.
       And we only need to fully synchronize if there have been
       concurrent modifications.
    */

    ALDWeakHashMapEntry[] tab = this.table;
    int index = hash & (tab.length - 1);
    ALDWeakHashMapEntry first = tab[index];
    ALDWeakHashMapEntry e = first;

    for (;;) {
      if (e == null) {

        // If key apparently not there, check to
        // make sure this was a valid read

      	ALDWeakHashMapEntry[] reread = getTableForReading();
        if (tab == reread && first == tab[index]) {
          return null;
        }
        else {
          // Wrong list -- must restart traversal at new first
          tab = reread;
          e = first = tab[index = hash & (tab.length-1)];
        }

      }

      else if (e.hash == hash && eq(key, e.getKey())) {
        Object value = e.value;
        if (value != null) 
          return value;

        // Entry was invalidated during deletion. But it could
        // have been re-inserted, so we must retraverse.
        // To avoid useless contention, get lock to wait out modifications
        // before retraversing.

        synchronized(this) {
          tab = this.table;
        }
        // original version: better include in synchronized block?
        e = first = tab[index = hash & (tab.length-1)];

      }
      else
        e = e.next;
    }
  }

  /**
   * Tests if the specified object is a key in this table.
   * <p>
   * the method returns  <code>true</code> if and only if the specified 
   * object is a key in this table, as determined by the <tt>equals</tt> 
   * method; <code>false</code> otherwise.
   * 
   * @param   key  	Questionable key.
   * @return  <code>true</code> if and only if object is a key.
   * @exception  NullPointerException if the key is <code>null</code>.
   */
//  @Override
  public boolean containsKey(Object key) {
    return get(key) != null;
  }

  /**
   * Maps the specified <code>key</code> to the specified 
   * <code>value</code> in this table. 
   * <p>
   * Neither the key nor the value can be <code>null</code>. <p>
   *
   * The value can be retrieved by calling the <code>get</code> method 
   * with a key that is equal to the original key. 
   *
   * @param      key     The table key.
   * @param      value   The value.
   * @return     The previous value of the specified key in this table,
   *             or <code>null</code> if it did not have one.
   * @exception  NullPointerException if the key or value is <code>null</code>.
   * @see     Object#equals(Object)
   * @see     #get(Object)
   */
//  @Override
  public Object put(Object key, Object value) {
    if (value == null) 
      throw new NullPointerException();
    
    this.removeWeakKeys();
    
    int hash = hash(key);
    ALDWeakHashMapEntry[] tab = this.table;
    int index = hash & (tab.length-1);
    ALDWeakHashMapEntry first = tab[index];

    ALDWeakHashMapEntry e = null;
    for (e = first; e != null; e = e.next)
      if (e.hash == hash && eq(key, e.getKey()))
        break;

    synchronized(this) {
      if (tab == this.table) {
        if (e == null) {
          //  make sure we are adding to correct list
          if (first == tab[index]) {
            //  Add to front of list
          	ALDWeakHashMapEntry newEntry = 
          		new ALDWeakHashMapEntry(hash, key, value, first, refQueue);
            tab[index] = newEntry;
            if (++this.count >= this.threshold) rehash();
            else recordModification(newEntry);
            return null;
          }
        }
        else {
          Object oldValue = e.value; 
          if (first == tab[index] && oldValue != null) {
            e.value = value;
            return oldValue;
          }
        }
      }
      
      // retry if wrong list or lost race against concurrent remove
      return sput(key, value, hash);
    }
  }

  /**
   * Continuation of put(), called only when synch lock is
   * held and interference has been detected.
   **/
  protected Object sput(Object key, Object value, int hash) { 

  	ALDWeakHashMapEntry[] tab = this.table;
    int index = hash & (tab.length-1);
    ALDWeakHashMapEntry first = tab[index];
    ALDWeakHashMapEntry e = first;

    for (;;) {
      if (e == null) {
      	ALDWeakHashMapEntry newEntry = 
      		new ALDWeakHashMapEntry(hash, key, value, first, refQueue);
        tab[index] = newEntry;
        if (++this.count >= this.threshold) rehash();
        else recordModification(newEntry);
        return null;
      }
      else if (e.hash == hash && eq(key, e.getKey())) {
        Object oldValue = e.value; 
        e.value = value;
        return oldValue;
      }
      else
        e = e.next;
    }
  }

  /**
   * Rehashes the contents of this map into a new table
   * with a larger capacity. This method is called automatically when the
   * number of keys in this map exceeds its capacity and load factor.
   */
  protected void rehash() {
  	// deleted deprecated keys first
  	this.removeWeakKeys();
  	
  	ALDWeakHashMapEntry[] oldTable = this.table;
    int oldCapacity = oldTable.length;
    if (oldCapacity >= MAXIMUM_CAPACITY) {
    	System.out.println("Maximum capacity reached!");
    	try {
				System.in.read();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
      this.threshold = Integer.MAX_VALUE; // avoid retriggering
      return;
    }

    int newCapacity = oldCapacity << 1;
    int mask = newCapacity - 1;
    this.threshold = (int)(newCapacity * this.loadFactor);

    ALDWeakHashMapEntry[] newTable = new ALDWeakHashMapEntry[newCapacity];
    /*
     * Reclassify nodes in each list to new Map.  Because we are
     * using power-of-two expansion, the elements from each bin
     * must either stay at same index, or move to
     * oldCapacity+index. We also eliminate unnecessary node
     * creation by catching cases where old nodes can be reused
     * because their next fields won't change. Statistically, at
     * the default threshhold, only about one-sixth of them need
     * cloning. (The nodes they replace will be garbage
     * collectable as soon as they are no longer referenced by any
     * reader thread that may be in the midst of traversing table
     * right now.)
     */
    
    for (int i = 0; i < oldCapacity ; i++) {
      // We need to guarantee that any existing reads of old Map can
      //  proceed. So we cannot yet null out each bin.  
    	ALDWeakHashMapEntry e = oldTable[i];
      
      if (e != null) {
        int idx = e.hash & mask;
        ALDWeakHashMapEntry next = e.next;
        
        //  Single node on list
        if (next == null) 
          newTable[idx] = e;
        
        else {    
          // Reuse trailing consecutive sequence of all same bit
        	ALDWeakHashMapEntry lastRun = e;
          int lastIdx = idx;
          for (ALDWeakHashMapEntry last = next; last != null; last = last.next) {
            int k = last.hash & mask;
            if (k != lastIdx) {
              lastIdx = k;
              lastRun = last;
            }
          }
          newTable[lastIdx] = lastRun;
          
          // Clone all remaining nodes
          for (ALDWeakHashMapEntry p = e; p != lastRun; p = p.next) {
            int k = p.hash & mask;
            newTable[k] = new ALDWeakHashMapEntry(p.hash, p.getKey(), 
                                    p.value, newTable[k], refQueue);
          }
        }
      }
    }

    this.table = newTable;
    recordModification(newTable);
  }

  /**
   * Removes the key (and its corresponding value) from the table.
   * <p> 
   * This method does nothing if the key is not in the table.
   *
   * @param   key   Key that needs to be removed.
   * @return  Value to which the key had been mapped in this table,
   *          or <code>null</code> if the key did not have a mapping.
   * @exception  NullPointerException  if the key is <code>null</code>.
   */
//  @Override
  public Object remove(Object key) {
    /*
      Find the entry, then 
        1. Set value field to null, to force get() to retry
        2. Rebuild the list without this entry.
           All entries following removed node can stay in list, but
           all preceeding ones need to be cloned.  Traversals rely
           on this strategy to ensure that elements will not be
          repeated during iteration.
    */
          

    int hash = hash(key);
    ALDWeakHashMapEntry[] tab = this.table;
    int index = hash & (tab.length-1);
    ALDWeakHashMapEntry first = tab[index];
    ALDWeakHashMapEntry e = first;
      
    for (e = first; e != null; e = e.next) 
      if (e.hash == hash && eq(key, e.getKey())) 
        break;

    synchronized(this) {
      if (tab == this.table) {
        if (e == null) {
          if (first == tab[index])
            return null;
        }
        else {
          Object oldValue = e.value;
          if (first == tab[index] && oldValue != null) {
            e.value = null;
            this.count--;
            
            ALDWeakHashMapEntry head = e.next;
            for (ALDWeakHashMapEntry p = first; p != e; p = p.next) 
              head= new ALDWeakHashMapEntry(p.hash,p.getKey(),p.value,head,refQueue);
            
            tab[index] = head;
            recordModification(tab);
            return oldValue;
          }
        }
      }
      // Wrong list or interference
      return sremove(key, hash);
    }
  }

  /**
   * Continuation of remove(), called only when synch lock is
   * held and interference has been detected.
   **/
  protected Object sremove(Object key, int hash) {
  	ALDWeakHashMapEntry[] tab = this.table;
    int index = hash & (tab.length-1);
    ALDWeakHashMapEntry first = tab[index];
      
    for (ALDWeakHashMapEntry e = first; e != null; e = e.next) {
      if (e.hash == hash && eq(key, e.getKey())) {
        Object oldValue = e.value;
        e.value = null;
        this.count--;
        ALDWeakHashMapEntry head = e.next;
        for (ALDWeakHashMapEntry p = first; p != e; p = p.next) 
          head= new ALDWeakHashMapEntry(p.hash, p.getKey(), p.value, head, refQueue);
        tab[index] = head;
        
        tab[index] = null;
        recordModification(tab);
        return oldValue;
      }
    }
    return null;
  }

  /**
   * Returns <tt>true</tt> if this map maps one or more keys to the
   * specified value. Note: This method requires a full internal
   * traversal of the hash table, and so is much slower than
   * method <tt>containsKey</tt>.
   *
   * @param value value whose presence in this map is to be tested.
   * @return <tt>true</tt> if this map maps one or more keys to the
   * specified value.  
   * @exception  NullPointerException  if the value is <code>null</code>.
   */
//  @Override
//  public boolean containsValue(Object value) {
//    if (value == null) throw new NullPointerException();
//
//    MTBWeakHashMapEntry tab[] = getTableForReading();
//    
//    for (int i = 0 ; i < tab.length; ++i) {
//      for (MTBWeakHashMapEntry e = tab[i] ; e != null ; e = e.next) 
//        if (value.equals(e.value))
//          return true;
//    }
//
//    return false;
//  }

  /**
   * Tests if some key maps into the specified value in this table.
   * This operation is more expensive than the <code>containsKey</code>
   * method.<p>
   *
   * Note that this method is identical in functionality to containsValue,
   * (which is part of the Map interface in the collections framework).
   * 
   * @param      value   a value to search for.
   * @return     <code>true</code> if and only if some key maps to the
   *             <code>value</code> argument in this table as 
   *             determined by the <tt>equals</tt> method;
   *             <code>false</code> otherwise.
   * @exception  NullPointerException  if the value is <code>null</code>.
   * @see        #containsKey(Object)
   * @see        #containsValue(Object)
   * @see	   Map
   */
//  public boolean contains(Object value) {
//    return containsValue(value);
//  }


  /**
   * Copies all of the mappings from the specified map to this one.
   * 
   * These mappings replace any mappings that this map had for any of the
   * keys currently in the specified Map.
   *
   * @param t Mappings to be stored in this map.
   */
//  @Override
//  public synchronized void putAll(Map t) {
//    int n = t.size();
//    if (n == 0)
//      return;
//
//    // Expand enough to hold at least n elements without resizing.
//    // We can only resize table by factor of two at a time.
//    // It is faster to rehash with fewer elements, so do it now.
//    while (n >= this.threshold)
//      rehash();
//
//    for (Iterator it = t.entrySet().iterator(); it.hasNext();) {
//      Map.Entry entry = (Map.Entry) it.next();
//      Object key = entry.getKey();
//      Object value = entry.getValue();
//      put(key, value);
//    }
//  }
  
  /**
   * Removes all mappings from the map.
   */
//  @Override
  public synchronized void clear() {
  	ALDWeakHashMapEntry tab[] = this.table;
    for (int i = 0; i < tab.length ; ++i) { 

      // must invalidate all to force concurrent get's to wait and then retry
      for (ALDWeakHashMapEntry e = tab[i]; e != null; e = e.next) 
        e.value = null; 

    	ALDWeakHashMapEntry e = tab[i];
    	if (e != null) 
    		e.value = null;
      tab[i] = null;
    }
    this.count = 0;
    recordModification(tab);
  }

  /**
   * Returns a shallow copy of this <tt>ConcurrentReaderHashMap</tt> instance.
   * <p>
   * Note that the keys and values themselves are not cloned.
   *
   * @return A shallow copy of this map.
   */
  @Override
  public synchronized Object clone() {
  	try { 
  		ALDConcReadWeakHashMap t = (ALDConcReadWeakHashMap)super.clone();

//      t.keySet = null;
//      t.entrySet = null;
//      t.values = null;

      ALDWeakHashMapEntry[] tab = this.table;
      t.table = new ALDWeakHashMapEntry[tab.length];
      ALDWeakHashMapEntry[] ttab = t.table;

      for (int i = 0; i < tab.length; ++i) {
      	ALDWeakHashMapEntry first = null;
        for (ALDWeakHashMapEntry e = tab[i]; e != null; e = e.next) 
          first = 
          	new ALDWeakHashMapEntry(e.hash, e.getKey(), e.value, 
          													first, refQueue);
        ttab[i] = first;
      }

      return t;
    } 
    catch (CloneNotSupportedException e) { 
      // this shouldn't happen, since we are Cloneable
      throw new InternalError();
    }
  }

  /**
   * Cleans-up the hashmap when called.
   * <p>
   * This function is called from time to time on the hashmap to remove
   * keys of objects that have been deleted.
   */
  private void removeWeakKeys() {
  	for (Object x; (x = refQueue.poll()) != null; ) {
  		synchronized (refQueue) {
  			
  			ALDWeakHashMapEntry deletedEntry = (ALDWeakHashMapEntry)x;
  			int index = deletedEntry.hash & (this.table.length - 1);

  			// get first entry for calculated index
  			ALDWeakHashMapEntry prev = table[index];
  			ALDWeakHashMapEntry p = prev;
  			while (p != null) {
  				ALDWeakHashMapEntry next = p.next;
  				if (p == deletedEntry) {
  					if (prev == deletedEntry)
  						table[index] = next;
  					else
  						prev.next = next;
  					deletedEntry.value = null; 
  					deletedEntry.next = null;
  					this.count--;
  					break;
  				}
  				prev = p;
  				p = next;
  			}
  		}
  	}
  }

  // Views

//  protected transient Set keySet = null;
//  protected transient Set entrySet = null;
//  protected transient Collection values = null;

  /**
   * Returns a set view of the keys contained in this map.  The set is
   * backed by the map, so changes to the map are reflected in the set, and
   * vice-versa.  The set supports element removal, which removes the
   * corresponding mapping from this map, via the <tt>Iterator.remove</tt>,
   * <tt>Set.remove</tt>, <tt>removeAll</tt>, <tt>retainAll</tt>, and
   * <tt>clear</tt> operations.  It does not support the <tt>add</tt> or
   * <tt>addAll</tt> operations.
   *
   * @return a set view of the keys contained in this map.
   */
//  @Override
//  public Set keySet() {
//    Set ks = this.keySet;
//    return (ks != null)? ks : (this.keySet = new KeySet());
//  }
//  
//  private class KeySet extends AbstractSet {
//    @Override
//    public Iterator iterator() {
//      return new KeyIterator();
//    }
//    @Override
//    public int size() {
//      return MTBConcReadWeakHashMap.this.size();
//    }
//    @Override
//    public boolean contains(Object o) {
//      return MTBConcReadWeakHashMap.this.containsKey(o);
//    }
//    @Override
//    public boolean remove(Object o) {
//      return MTBConcReadWeakHashMap.this.remove(o) != null;
//    }
//    @Override
//    public void clear() {
//      MTBConcReadWeakHashMap.this.clear();
//    }
//  }

  /**
   * Returns a collection view of the values contained in this map.  The
   * collection is backed by the map, so changes to the map are reflected in
   * the collection, and vice-versa.  The collection supports element
   * removal, which removes the corresponding mapping from this map, via the
   * <tt>Iterator.remove</tt>, <tt>Collection.remove</tt>,
   * <tt>removeAll</tt>, <tt>retainAll</tt>, and <tt>clear</tt> operations.
   * It does not support the <tt>add</tt> or <tt>addAll</tt> operations.
   *
   * @return a collection view of the values contained in this map.
   */
//  @Override
//  public Collection values() {
//    Collection vs = this.values;
//    return (vs != null)? vs : (this.values = new Values());
//  }
//  
//  private class Values extends AbstractCollection {
//    @Override
//    public Iterator iterator() {
//      return new ValueIterator();
//    }
//    @Override
//    public int size() {
//      return MTBConcReadWeakHashMap.this.size();
//    }
//    @Override
//    public boolean contains(Object o) {
//      return MTBConcReadWeakHashMap.this.containsValue(o);
//    }
//    @Override
//    public void clear() {
//      MTBConcReadWeakHashMap.this.clear();
//    }
//  }

  /**
   * Returns a collection view of the mappings contained in this map.  Each
   * element in the returned collection is a <tt>Map.Entry</tt>.  The
   * collection is backed by the map, so changes to the map are reflected in
   * the collection, and vice-versa.  The collection supports element
   * removal, which removes the corresponding mapping from the map, via the
   * <tt>Iterator.remove</tt>, <tt>Collection.remove</tt>,
   * <tt>removeAll</tt>, <tt>retainAll</tt>, and <tt>clear</tt> operations.
   * It does not support the <tt>add</tt> or <tt>addAll</tt> operations.
   *
   * @return a collection view of the mappings contained in this map.
   */
//  @Override
//  public Set entrySet() {
//    Set es = this.entrySet;
//    return (es != null) ? es : (this.entrySet = new EntrySet());
//  }

//  private class EntrySet extends AbstractSet {
//    @Override
//    public Iterator iterator() {
//      return new HashIterator();
//    }
//    @Override
//    public boolean contains(Object o) {
//      if (!(o instanceof Map.Entry))
//        return false;
//      Map.Entry entry = (Map.Entry)o;
//      Object v = MTBConcReadWeakHashMap.this.get(entry.getKey());
//      return v != null && v.equals(entry.getValue());
//    }
//    @Override
//    public boolean remove(Object o) {
//      if (!(o instanceof Map.Entry))
//        return false;
//      return MTBConcReadWeakHashMap.this.findAndRemoveEntry((Map.Entry)o);
//    }
//    @Override
//    public int size() {
//      return MTBConcReadWeakHashMap.this.size();
//    }
//    @Override
//    public void clear() {
//      MTBConcReadWeakHashMap.this.clear();
//    }
//  }

  /**
   * Helper method for entrySet.remove
   **/
//  protected synchronized boolean findAndRemoveEntry(Map.Entry entry) {
//    Object key = entry.getKey();
//    Object v = get(key);
//    if (v != null && v.equals(entry.getValue())) {
//      remove(key);
//      return true;
//    }
//    else
//      return false;
//  }

  /**
   * Returns an enumeration of the keys in this table.
   *
   * @return  an enumeration of the keys in this table.
   * @see     Enumeration
   * @see     #elements()
   * @see	#keySet()
   * @see	Map
   */
//  public Enumeration keys() {
//    return new KeyIterator();
//  }

  /**
   * Returns an enumeration of the values in this table.
   * Use the Enumeration methods on the returned object to fetch the elements
   * sequentially.
   *
   * @return  an enumeration of the values in this table.
   * @see     java.util.Enumeration
   * @see     #keys()
   * @see	#values()
   * @see	Map
   */
//  public Enumeration elements() {
//    return new ValueIterator();
//  }

  /**
   * ConcurrentReaderHashMap collision list entry.
   */
//  protected static class Entry implements Map.Entry {
//
//    /* 
//       The use of volatile for value field ensures that
//       we can detect status changes without synchronization.
//       The other fields are never changed, and are
//       marked as final. 
//    */
//
//    protected final int hash;
//    protected final Object key;
//    protected final Entry next;
//    protected volatile Object value;
//
//    Entry(int hash, Object key, Object value, Entry next) {
//      this.hash = hash;
//      this.key = key;
//      this.next = next;
//      this.value = value;
//    }
//
//    // Map.Entry Ops 
//
//    @Override
//    public Object getKey() {
//      return this.key;
//    }
//
//    /**
//     * Get the value.  Note: In an entrySet or entrySet.iterator,
//     * unless the set or iterator is used under synchronization of the
//     * table as a whole (or you can otherwise guarantee lack of
//     * concurrent modification), <tt>getValue</tt> <em>might</em>
//     * return null, reflecting the fact that the entry has been
//     * concurrently removed. However, there are no assurances that
//     * concurrent removals will be reflected using this method.
//     * 
//     * @return     the current value, or null if the entry has been 
//     * detectably removed.
//     **/
//    @Override
//    public Object getValue() {
//      return this.value; 
//    }
//
//    /**
//     * Set the value of this entry.  Note: In an entrySet or
//     * entrySet.iterator), unless the set or iterator is used under
//     * synchronization of the table as a whole (or you can otherwise
//     * guarantee lack of concurrent modification), <tt>setValue</tt>
//     * is not strictly guaranteed to actually replace the value field
//     * obtained via the <tt>get</tt> operation of the underlying hash
//     * table in multithreaded applications.  If iterator-wide
//     * synchronization is not used, and any other concurrent
//     * <tt>put</tt> or <tt>remove</tt> operations occur, sometimes
//     * even to <em>other</em> entries, then this change is not
//     * guaranteed to be reflected in the hash table. (It might, or it
//     * might not. There are no assurances either way.)
//     *
//     * @param      value   the new value.
//     * @return     the previous value, or null if entry has been detectably
//     * removed.
//     * @exception  NullPointerException  if the value is <code>null</code>.
//     * 
//     **/
//
//    @Override
//    public Object setValue(Object value) {
//      if (value == null)
//        throw new NullPointerException();
//      Object oldValue = this.value;
//      this.value = value;
//      return oldValue;
//    }
//
//    @Override
//    public boolean equals(Object o) {
//      if (!(o instanceof Map.Entry))
//        return false;
//      Map.Entry e = (Map.Entry)o;
//      return (this.key.equals(e.getKey()) && this.value.equals(e.getValue()));
//    }
//    
//    @Override
//    public int hashCode() {
//      return  this.key.hashCode() ^ this.value.hashCode();
//    }
//    
//    @Override
//    public String toString() {
//      return this.key + "=" + this.value;
//    }
//
//  }

//  protected class HashIterator implements Iterator, Enumeration {
//    protected final Entry[] tab;           // snapshot of table
//    protected int index;                   // current slot 
//    protected Entry entry = null;          // current node of slot
//    protected Object currentKey;           // key for current node
//    protected Object currentValue;         // value for current node
//    protected Entry lastReturned = null;   // last node returned by next
//
//    protected HashIterator() {
//      this.tab = MTBConcReadWeakHashMap.this.getTableForReading();
//      this.index = this.tab.length - 1;
//    }
//
//    @Override
//    public boolean hasMoreElements() { return hasNext(); }
//    @Override
//    public Object nextElement() { return next(); }
//
//
//    @Override
//    public boolean hasNext() {
//
//      /*
//        currentkey and currentValue are set here to ensure that next()
//        returns normally if hasNext() returns true. This avoids
//        surprises especially when final element is removed during
//        traversal -- instead, we just ignore the removal during
//        current traversal.  
//      */
//
//      for (;;) {
//        if (this.entry != null) {
//          Object v = this.entry.value;
//          if (v != null) {
//            this.currentKey = this.entry.key;
//            this.currentValue = v;
//            return true;
//          }
//          else
//            this.entry = this.entry.next;
//        }
//
//        while (this.entry == null && this.index >= 0)
//          this.entry = this.tab[this.index--];
//
//        if (this.entry == null) {
//          this.currentKey = this.currentValue = null;
//          return false;
//        }
//      }
//    }
//
//    protected Object returnValueOfNext() { return this.entry; }
//
//    @Override
//    public Object next() {
//      if (this.currentKey == null && !hasNext())
//        throw new NoSuchElementException();
//
//      Object result = returnValueOfNext();
//      this.lastReturned = this.entry;
//      this.currentKey = this.currentValue = null;
//      this.entry = this.entry.next;
//      return result;
//    }
//
//    @Override
//    public void remove() {
//      if (this.lastReturned == null)
//        throw new IllegalStateException();
//      MTBConcReadWeakHashMap.this.remove(this.lastReturned.key);
//      this.lastReturned = null;
//    }
//
//  }

//  protected class KeyIterator extends HashIterator {
//    @Override
//    protected Object returnValueOfNext() { return this.currentKey; }
//  }
//  
//  protected class ValueIterator extends HashIterator {
//    @Override
//    protected Object returnValueOfNext() { return this.currentValue; }
//  }
  
  /**
   * Save the state of the <tt>ConcurrentReaderHashMap</tt> 
   * instance to a stream (i.e.,
   * serialize it).
   *
   * @serialData The <i>capacity</i> of the 
   * ConcurrentReaderHashMap (the length of the
   * bucket array) is emitted (int), followed  by the
   * <i>size</i> of the ConcurrentReaderHashMap (the number of key-value
   * mappings), followed by the key (Object) and value (Object)
   * for each key-value mapping represented by the ConcurrentReaderHashMap
   * The key-value mappings are emitted in no particular order.
   */
//  private synchronized void writeObject(java.io.ObjectOutputStream s)
//    throws IOException  {
//    // Write out the threshold, loadfactor, and any hidden stuff
//    s.defaultWriteObject();
//    
//    // Write out number of buckets
//    s.writeInt(this.table.length);
//    
//    // Write out size (number of Mappings)
//    s.writeInt(this.count);
//    
//    // Write out keys and values (alternating)
//    for (int index = this.table.length-1; index >= 0; index--) {
//      Entry entry = this.table[index];
//      
//      while (entry != null) {
//        s.writeObject(entry.key);
//        s.writeObject(entry.value);
//        entry = entry.next;
//      }
//    }
//  }

  /**
   * Reconstitute the <tt>ConcurrentReaderHashMap</tt> 
   * instance from a stream (i.e.,
   * deserialize it).
   */
//  private synchronized void readObject(java.io.ObjectInputStream s)
//    throws IOException, ClassNotFoundException  {
//    // Read in the threshold, loadfactor, and any hidden stuff
//    s.defaultReadObject();
//
//    // Read in number of buckets and allocate the bucket array;
//    int numBuckets = s.readInt();
//    this.table = new Entry[numBuckets];
//    
//    // Read in size (number of Mappings)
//    int size = s.readInt();
//    
//    // Read the keys and values, and put the mappings in the table
//    for (int i=0; i<size; i++) {
//      Object key = s.readObject();
//      Object value = s.readObject();
//      put(key, value);
//    }
//  }
  
  /** 
   * Returns the number of slots in this table. 
   **/
  public synchronized int capacity() {
    return this.table.length;
  }

  /** 
   * Returns the load factor. 
   **/
  public float loadFactor() {
    return this.loadFactor;
  }
}
