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

import mmenning.mobilegis.map.MyItemizedOverlay;
import mmenning.mobilegis.util.BoundedCircle;

/**
 * Overlay to display GeoRSSEntries on a MapView.
 * 
 * @author Mathias Menninghaus
 * @version 15.10.2009
 */
public class GeoRSSOverlay extends MyItemizedOverlay<GeoRSSOverlayItem> {

	private static final String DT = "GeoRSSOverlay";

	private GeoRSSOverlayListener listen;

	private BoundedCircle actualMarker;

	public GeoRSSOverlay(GeoRSSOverlayListener listen, int defaultColor) {
		this.listen = listen;
		setColorForFollowing(defaultColor);
	}

	public void setColorForFollowing(int color) {
		actualMarker = new BoundedCircle(color);
	}

	public void addOverlay(GeoRSSEntry entry) {
		super.addItem(new GeoRSSOverlayItem(entry, actualMarker));
	}

	@Override
	protected boolean onTab(GeoRSSOverlayItem item) {
		listen.onItemTabbed(item.getID());
		return true;
	}

	/**
	 * Listener Interface to react on User input on this GeoRSSOverlay
	 * 
	 * @author Mathias Menninghaus (mmenning@uos.de)
	 * @version 15.10.2009
	 */
	public interface GeoRSSOverlayListener {

		/**
		 * Called when an GeoRSSOverlayItem in the referred GeoRSSOverlay has
		 * been tabbed.
		 * 
		 * @param entryID
		 *            ID of the GeoRSSEntry which belongs to the tabbed
		 *            GeoRSSOverlayItem
		 */
		public void onItemTabbed(int entryID);
	}
}
