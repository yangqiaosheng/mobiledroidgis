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

import java.util.List;

import mmenning.mobilegis.R;

import android.content.Context;
import android.graphics.Canvas;
import android.util.AttributeSet;

import com.google.android.maps.MapView;
import com.google.android.maps.Overlay;

/**
 * Extended GoogleMaps MapView to enable and disable the GoogleMap Layer but to
 * keep the Projection, zoom- and walk controls. If the GoogleMap Layer is
 * disabled it will display a one-colored background.
 * 
 * @author Mathias Mennighaus
 * @version 23.10.2009
 * 
 */
public class MyMapView extends MapView {

	public MyMapView(Context context, AttributeSet attrs) {
		super(context, attrs);
		mapBackground = this.getResources().getColor(R.color.map_background);
	}

	private int mapBackground;

	@Override
	public void draw(Canvas canvas) {

		if (this.googleMapsVisible) {
			super.draw(canvas);
		} else {
			canvas.drawColor(mapBackground);

			List<Overlay> overlays = this.getOverlays();

			for (Overlay o : overlays) {
				o.draw(canvas, this, false, System.currentTimeMillis());
			}
		}
	}

	private boolean googleMapsVisible;

	/**
	 * Enable or Disable the GoogleMaps Layer.
	 * 
	 * @param visible
	 *            whether GooglMaps should be displayed or not.
	 */
	public void setGoogleMapsVisibility(boolean visible) {
		this.googleMapsVisible = visible;
	}

}
