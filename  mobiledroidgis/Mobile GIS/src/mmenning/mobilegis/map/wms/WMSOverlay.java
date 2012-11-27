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
package mmenning.mobilegis.map.wms;

import java.util.ArrayList;

import mmenning.mobilegis.map.SleepableOverlay;
import mmenning.mobilegis.util.ProgressAnimationManager;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.os.Handler;
import android.os.Message;

import com.google.android.maps.GeoPoint;
import com.google.android.maps.MapView;
import com.google.android.maps.Overlay;
import com.google.android.maps.Projection;

/**
 * Overlay to display and manage WMS Overlays.
 * 
 * @author Mathias Menninghaus
 * @version 23.10.2009
 * 
 */
public class WMSOverlay extends Overlay implements SleepableOverlay {

	private static final String DT = "WMSOverlay";

	private Paint semitransparent;

	private ArrayList<WMSLoader<String>> loader;

	private static final GeoPoint ORIGIN = new GeoPoint(0, 0);

	private ProgressAnimationManager loadManager;

	private MapView map;

	private int previousZoomLevel = -1;

	private InvalidationHandler invalidationHandler;

	/**
	 * Instanciate a new WMSOverlay.
	 * 
	 * @param loadManager
	 *            to display if the overalay loads some parts
	 * @param map
	 *            father of this overlay
	 */
	public WMSOverlay(ProgressAnimationManager loadManager, MapView map) {
		this.loader = new ArrayList<WMSLoader<String>>();
		this.loadManager = loadManager;
		this.map = map;
		this.semitransparent = new Paint();
		this.invalidationHandler = new InvalidationHandler();
	}

	/**
	 * Add a new baseURL to load parts from. The first added WMS fill be
	 * displayed on the bottom.
	 * 
	 * @param getMapBaseURL
	 *            {@link WMSUtils.getMapBaseURL}
	 */
	public void addLoader(String getMapBaseURL) {
		this.loader.add(new WMSLoader<String>(getMapBaseURL,
				invalidationHandler));
	}

	/**
	 * Remove all previously added baseURLS
	 */
	public void clear() {
		for (WMSLoader<String> l : loader) {
			l.stopLoading();
		}
		loader.clear();
	}

	/**
	 * Draw Parts using the currently displayed Position and all getMapBaseUrls.
	 */
	@Override
	public void draw(Canvas canvas, MapView mapView, boolean shadow) {

		if (sleeps)
			return;

		if (this.previousZoomLevel != mapView.getZoomLevel()) {
			this.stopLoading();
			this.previousZoomLevel = mapView.getZoomLevel();
		}

		final Projection p = mapView.getProjection();

		/*
		 * calculate distance to last center
		 */
		final int[] dist = WMSUtils.pixelDistance(ORIGIN, p.fromPixels(mapView
				.getLeft(), mapView.getTop()), p);

		/*
		 * so much parts we will need to load
		 */
		final int partsX = (mapView.getWidth() / WMSUtils.WIDTH) + 1;
		final int partsY = (mapView.getHeight() / WMSUtils.HEIGHT) + 1;

		/*
		 * coordinates of the first part in ScreenPixels
		 */
		final int startX = (Math.abs(dist[WMSUtils.X]) % WMSUtils.WIDTH)
				* (dist[WMSUtils.X] < 0 ? (-1) : 1) - WMSUtils.WIDTH;
		final int startY = (Math.abs(dist[WMSUtils.Y]) % WMSUtils.HEIGHT)
				* (dist[WMSUtils.Y] < 0 ? (-1) : 1) - WMSUtils.HEIGHT;

		/*
		 * coordinates of the last part in ScreenPixels
		 */
		final int endX = startX + partsX * WMSUtils.WIDTH;
		final int endY = startY + partsY * WMSUtils.HEIGHT;

		/*
		 * identifier for the first part
		 */
		final int startIdentX = (dist[WMSUtils.X] * -1) / WMSUtils.WIDTH - 1;
		final int startIdentY = dist[WMSUtils.Y] / WMSUtils.HEIGHT + 1;

		Bitmap map;

		String key;

//		 int k = 0;

		/*
		 * Load parts for every seen part from top-left to bottom-right.
		 */
		for (int y = startY, identY = startIdentY; y <= endY; y += WMSUtils.HEIGHT, identY--) {

			for (int x = startX, identX = startIdentX; x <= endX; x += WMSUtils.WIDTH, identX++) {

				/*
				 * DEBUG rectangles
				 */
//				 k++;
				//												
//				String ident = "(" + mapView.getZoomLevel() + ") | " + identX
//						+ " | " + identY;
//
//				Paint rectangle = new Paint();
//				
//				if((identX+identY)%2==0)rectangle.setColor(Color.LTGRAY);
//				else rectangle.setColor(Color.GRAY);
//				rectangle.setAlpha(100);
//				rectangle.setStyle(Paint.Style.FILL);
//				rectangle.setStrokeWidth(3);
//				Rect r = new Rect(x, y, x + WMSUtils.WIDTH, y
//						+ WMSUtils.HEIGHT);
//				canvas.drawRect(r, rectangle);
				//END DEBUG
				
				/*
				 * key to identify the part for a wmsLoader definite
				 */
				key = mapView.getZoomLevel() + "," + identX + "," + identY;

				/*
				 * Load part from every WMSLoader.
				 */

				for (WMSLoader<String> l : loader) {
					map = l.loadMap(key, x, y, p);
					if (map != null) {
						canvas.drawBitmap(map, x, y, semitransparent);
					}
				}

				
				//DEBUG KEY	
//				rectangle.setStrokeWidth(1);
//				rectangle.setColor(Color.BLACK);
//				rectangle.setAlpha(255);
//				rectangle.setStyle(Paint.Style.FILL);
//				rectangle.setTextSize(15);
//				canvas.drawText(ident, x + 40, y + WMSUtils.HALFHEIGHT,
//						rectangle);

				/*
				 * END DEBUG
				 */
				
				
			

			}
		}
		/*
		 * DEBUG information
		 */
//		 Log.d(DT, "displayed rectangles: "+k);
		// Log.d(DT, "parts: "+partsX +" "+partsY);
		// Log.d(DT, "startPx: "+startX +" "+startY);
		// Log.d(DT, "startId: "+startIdentX+" "+startIdentY);
		/*
		 * END DEBUG
		 */
	}

	/**
	 * Should be called if the Overlay is no longer visible.
	 */
	public void onPause() {
		stopLoading();
	}

	/**
	 * Set the transparency of the WMSOverlay
	 * 
	 * @param transparency
	 *            alpha value [0..255]
	 */
	public void setTransparency(int transparency) {
		semitransparent.setAlpha(transparency);
	}

	private void stopLoading() {
		for (WMSLoader<String> l : loader) {
			l.stopLoading();
		}
	}

	/**
	 * Handler to handle Threads of the WMSLoader
	 * 
	 * @author Mathias Menninghaus
	 * 
	 */
	private class InvalidationHandler extends Handler {

		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case WMSLoader.START:
				loadManager.start();
				break;
			case WMSLoader.LOADSUCCESS:
				// loadManager.stop();
				WMSOverlay.this.map.invalidate();
				break;
			case WMSLoader.LOADFAIL:
				// loadManager.stop();
				break;
			case WMSLoader.STOP:
				loadManager.stop();

			}
			super.handleMessage(msg);
		}
	}

	private boolean sleeps;

	
	public void makeSleeping() {
		sleeps = true;

	}

	
	public void makeAwake() {
		sleeps = false;
	}

}
