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
package mmenning.mobilegis.map.sos;

import mmenning.mobilegis.map.MyItemizedOverlay;
import mmenning.mobilegis.map.SleepableOverlay;
import mmenning.mobilegis.util.BoundedRect;

/**
 * Overlay to display MeasurementData
 * 
 * @author Mathias Menninghaus
 * @version 30.11.2009
 */
public class SOSOverlay extends MyItemizedOverlay<SOSOverlayItem> implements
		SleepableOverlay {

	private static final String DT = "SOSOverlay";

	private SOSOverlayListener listen;

	private BoundedRect actualMarker;

	public SOSOverlay(SOSOverlayListener listen, int defaultColor) {
		super();
		this.listen = listen;
		setColorForFollowing(defaultColor);
	}

	public void setColorForFollowing(int color) {
		actualMarker = new BoundedRect(color);
	}

	public void addOverlay(MeasurementData m) {
		super.addItem(new SOSOverlayItem(m, actualMarker));
	}

	@Override
	protected boolean onTab(SOSOverlayItem item) {
		listen.onItemTabbed(item.getMeasurementData());
		return true;
	}

	public interface SOSOverlayListener {
		/**
		 * Called when an Item is tabbed
		 * 
		 * @param measurementData
		 *            Referred MeasurementData.
		 */
		public void onItemTabbed(MeasurementData measurementData);
	}

}
