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
