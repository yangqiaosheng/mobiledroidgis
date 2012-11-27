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

import mmenning.mobilegis.map.MyOverlayItem;
import android.graphics.drawable.Drawable;

import com.google.android.maps.GeoPoint;

/**
 * Item to display MeasurementData as Point on the MapView.
 * 
 * @author Mathias Menninghaus
 * @version 30.11.2009
 */
public class SOSOverlayItem extends MyOverlayItem {

	private MeasurementData measurementData;

	public SOSOverlayItem(MeasurementData measurement, Drawable marker) {
		super(new GeoPoint(measurement.latE6, measurement.lonE6), marker);
		this.measurementData = measurement;
	}

	/**
	 * Get the referred MeasurementData to this Item
	 * @return
	 */
	public MeasurementData getMeasurementData() {
		return measurementData;
	}
}
