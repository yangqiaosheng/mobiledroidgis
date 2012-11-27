/*
 * Copyright 2012 Mathias Menninghaus (mathias.menninghaus (at) googlemail (dot) com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package mmenning.mobilegis.map.georss;

import mmenning.mobilegis.map.MyOverlayItem;
import android.graphics.drawable.Drawable;

import com.google.android.maps.GeoPoint;

/**
 * Extended OverlayItem to display GeoRSSEntry in a GeoRSSOverlay
 * 
 * @author Mathias Menninghaus
 * @version 15.10.2009
 */
public class GeoRSSOverlayItem extends MyOverlayItem {

	private static final String DT = "GeoRSSOverlayItem";
	
	private int entryID;

	/**
	 * Create new GeoRSSOverlay item. Gets the GeoPoint and ID from entry
	 * 
	 * @param entry
	 *            GeoRSSEntry with relevant information to display in
	 *            GeoRSSOverlay
	 */
	public GeoRSSOverlayItem(GeoRSSEntry entry, Drawable marker) {
		super(new GeoPoint(entry.latE6, entry.lonE6), marker);
		this.entryID = entry.id;
	}

	/**
	 * Get the id of the GeoRSSEntry which is displayed with this
	 * GeoRSSOverlayItem
	 * 
	 * @return
	 */
	public int getID() {
		return entryID;
	}

}
