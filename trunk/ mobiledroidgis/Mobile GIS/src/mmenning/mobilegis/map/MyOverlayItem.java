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
package mmenning.mobilegis.map;

import android.graphics.drawable.Drawable;

import com.google.android.maps.GeoPoint;

/**
 * Class to hold data for a Item hold in MyItemizedOverlay. The Drawable Marker
 * must support getIntrinsicWidth() and getIntrinsicHeight, because on base of
 * this the tab in MyItemizedOverlay is computed.
 * 
 * @author Mathias Menninghaus
 * @version 30.11.2009
 * 
 */
public class MyOverlayItem {

	private GeoPoint geo;

	/**
	 * Get the GeoPoint referring to this MyOverlayItem.
	 * 
	 * @return
	 */
	public GeoPoint getGeo() {
		return geo;
	}

	/**
	 * Get the Drawable for this MyOverlayItem.
	 * 
	 * @return
	 */
	public Drawable getMarker() {
		return marker;
	}

	private Drawable marker;

	/**
	 * Instantiate a new MyOverlayItem.
	 * 
	 * @param geo
	 *            The referred GeoPoint
	 * @param marker
	 *            The Drawable which will be displayed at Postion of the geo.
	 */
	public MyOverlayItem(GeoPoint geo, Drawable marker) {
		this.geo = geo;
		this.marker = marker;
	}

}
