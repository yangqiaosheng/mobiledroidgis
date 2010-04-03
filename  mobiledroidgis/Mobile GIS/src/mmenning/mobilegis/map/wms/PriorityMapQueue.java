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
import java.util.LinkedList;

/**
 * Caches Data by a Key and provides cleaning up if the count of cached data
 * exceeds a limit. By cleaning up, the data with the oldest contains or get
 * request will be removed.
 * 
 * @author Mathias Menninghaus
 * 
 * @param <K>
 *            Key Type
 * @param <V>
 *            Value Type
 */
public class PriorityMapQueue<K, V> {

	private HashMap<K, V> values;
	private LinkedList<K> priority;

	private static final String DT = "PriorityMapQueue";

	private int maxSize;
	private int maxToleratedSize;

	/**
	 * Instantiate a new PriorityMapQueue
	 * 
	 * @param maxSize
	 *            maximum amount of data after cleaning up
	 * @param maxToleratedSize
	 *            maximum amount of data without cleaning up, if it is reached
	 *            cleanUP will be called.
	 */
	public PriorityMapQueue(int maxSize, int maxToleratedSize) {
		this.values = new HashMap<K, V>(maxToleratedSize);
		this.priority = new LinkedList<K>();
		this.maxSize = maxSize;
		this.maxToleratedSize = maxToleratedSize;
	}

	/**
	 * CleanUP the data to maxSize
	 */
	public synchronized void cleanUP() {
		int k = 0;
		while (values.size() > this.maxSize) {
			values.remove(priority.removeLast());
			k++;
		}
	}

	/**
	 * Query whether the MapQueue contains the key or not
	 * 
	 * @param key
	 * @return true if it contains the key, else false
	 */
	public synchronized boolean containsWithUpdate(K key) {
		if (values.containsKey(key)) {
			updateKey(key);
			return true;
		} else {
			return false;
		}

	}

	/**
	 * Get the value and puts it to the head of the Queue
	 * 
	 * @param key
	 * @return the value that belongs to the key
	 */
	public synchronized V getWithUpdate(K key) {

		V value = values.get(key);
		if (value != null) {
			/*
			 * update in list
			 */
			updateKey(key);

		}
		return value;

	}

	/**
	 * Update the Key Value
	 */
	private void updateKey(K key){
		priority.remove(key);
		priority.addFirst(key);
	}
	
	/**
	 * Insert to the head of the Queue. If the Queue already contains this key
	 * nothing will happen.
	 * 
	 * @param key
	 * @param value
	 */
	public synchronized void insertWithoutUpdate(K key, V value) {
		if (!values.containsKey(key)) {
			values.put(key, value);
			priority.addFirst(key);
			if (values.size() >= this.maxToleratedSize) {
				cleanUP();
			}
		}
	}
}
