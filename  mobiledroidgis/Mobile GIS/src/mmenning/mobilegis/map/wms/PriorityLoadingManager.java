/*
 * Copyright (C) 2010 by Mathias Menninghaus (mmenning (at) uos (dot) de)
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
 */
package mmenning.mobilegis.map.wms;

import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;

/**
 * Provides data storage of Values in two Queues. One for Data Storage that
 * should be requested by Threads to do something with it. The other one to
 * estimate whether a Thread already does something with data.
 * 
 * @author Mathias Menninghaus
 * 
 * @param <K>
 *            Key Type
 * @param <V>
 *            Value Type
 */
public class PriorityLoadingManager<K, V> {

	private HashMap<K, V> parts;
	private LinkedList<K> partPriority;
	private HashSet<K> currentlyLoading;

	private static final String DT = "PriorityLoadingManager";

	/**
	 * Instantiate a PriorityLoadingManager
	 */
	public PriorityLoadingManager() {
		this.parts = new HashMap<K, V>();
		this.partPriority = new LinkedList<K>();
		this.currentlyLoading = new HashSet<K>();
	}

	/**
	 * Wipes out all data
	 */
	public synchronized void clearLoadingQueue() {
		parts.clear();
		partPriority.clear();
	}

	/**
	 * Removes from the data queue and adds to the Thread-Queue
	 * 
	 * @return The moved Entry<K,V>
	 */
	public synchronized Entry<K, V> removeFirstAndStartLoading() {
		if (!isEmpty()) {
			K key = partPriority.getFirst();
			Entry<K, V> ret = new Entry<K, V>(key, parts.get(key));
			this.startLoading(key);
			parts.remove(key);
			partPriority.remove(key);
			return ret;
		}
		return null;
	}

	/**
	 * Inserts Key-Value Pair to the head of the data Queue. If the Queue
	 * already contains this data it will be moved to the head of the Queue.
	 * 
	 * @param key
	 * @param value
	 */
	public synchronized void insertIntoLoadingQueue(K key, V value) {
		if (!parts.containsKey(key)) {
			parts.put(key, value);
			partPriority.addFirst(key);
		} else {
			partPriority.remove(key);
			partPriority.addFirst(key);
		}
	}

	/**
	 * Estimate whether the key is in the Thread or the Data Queue
	 * @param key
	 * @return true if it does, else false
	 */
	public synchronized boolean threadRunsOrIsInQueue(K key) {
		if (parts.containsKey(key)) {
			partPriority.remove(key);
			partPriority.addFirst(key);
			return true;
		}
		return currentlyLoading.contains(key);
	}

	/**
	 * Estimate whether the Data Queue is empty or not.
	 * @return
	 */
	public synchronized boolean isEmpty() {
		return parts.isEmpty();
	}

	/**
	 * Estimate whether the Thread Queue is empty or not
	 * @param key
	 * @return
	 */
	public synchronized boolean threadRuns(K key) {
		return currentlyLoading.contains(key);
	}

	/**
	 * Remove a Key from the Thread Queue
	 * @param key
	 */
	public synchronized void completeLoading(K key) {
		currentlyLoading.remove(key);
	}

	/**
	 * Add Thread to the Thread Queue
	 * @param key
	 */
	private void startLoading(K key) {
		currentlyLoading.add(key);
	}

	/**
	 * Inner Class for Entry Output
	 * @author Mathias Menninghaus
	 *
	 * @param <K> Key Type
	 * @param <V> Value Type
	 */
	public class Entry<K, V> {

		public K key;
		public V value;

		private Entry(K key, V value) {
			this.key = key;
			this.value = value;
		}
	}
}
