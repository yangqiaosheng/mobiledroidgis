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
