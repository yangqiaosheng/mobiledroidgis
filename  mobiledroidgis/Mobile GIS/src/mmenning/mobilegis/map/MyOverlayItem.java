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
