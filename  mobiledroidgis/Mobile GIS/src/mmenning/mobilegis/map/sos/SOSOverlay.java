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
