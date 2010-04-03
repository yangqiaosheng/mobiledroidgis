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
