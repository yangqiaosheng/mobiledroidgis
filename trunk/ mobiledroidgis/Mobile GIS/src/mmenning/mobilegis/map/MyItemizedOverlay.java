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

import java.util.ArrayList;

import android.graphics.Canvas;
import android.graphics.Point;

import com.google.android.maps.GeoPoint;
import com.google.android.maps.ItemizedOverlay;
import com.google.android.maps.MapView;
import com.google.android.maps.Overlay;
import com.google.android.maps.Projection;

/**
 * Specialized Overlay to display Point - Data on a MapView
 * 
 * @author Mathias Menninghaus
 * @version 30.11.2009
 * 
 * @param <Item>
 *            Type of the Stored Items. Must extend MyOverlayItem
 */
public class MyItemizedOverlay<Item extends MyOverlayItem> extends Overlay
		implements SleepableOverlay {

	private boolean sleeps;
	private ArrayList<Item> items;

	private static final String DT = "MyItemizedOverlay";

	public MyItemizedOverlay() {
		this.items = new ArrayList<Item>();
	}

	public void addItem(Item item) {
		items.add(item);
	}

	public void clear() {
		items.clear();
	}

	@Override
	public void draw(Canvas canvas, MapView mapView, boolean shadow) {
		super.draw(canvas, mapView, shadow);
		if (!sleeps) {

			final Projection p = mapView.getProjection();
			Point px = new Point();

			for (Item item : items) {

				p.toPixels(item.getGeo(), px);

				if (px.x >= mapView.getLeft() && px.x <= mapView.getRight()
						&& px.y >= mapView.getTop()
						&& px.y <= mapView.getBottom()) {

					draw(canvas, item, px);
				}
			}
		}
	}

	public void makeAwake() {
		sleeps = false;
	}

	public void makeSleeping() {
		sleeps = true;
	}

	/**
	 * Will check whether one of the stored Items is clicked or not. If there is
	 * one, onTab(Item) will be called.
	 */
	@Override
	public boolean onTap(GeoPoint geo, MapView mapView) {

		if (!sleeps) {

			Projection p = mapView.getProjection();
			Point tabPoint = p.toPixels(geo, null);

			Point curPoint = new Point();
			int halfwidth;
			int halfheight;

			for (Item item : items) {

				halfwidth = (item.getMarker().getIntrinsicWidth() + 1) / 2;
				halfheight = (item.getMarker().getIntrinsicHeight() + 1) / 2;

				p.toPixels(item.getGeo(), curPoint);

				if (tabPoint.x >= (curPoint.x - halfwidth)
						&& tabPoint.x <= (curPoint.x + halfwidth)
						&& tabPoint.y <= (curPoint.y + halfheight)
						&& tabPoint.y >= (curPoint.y - halfheight)) {

					return onTab(item);

				}

			}
		}

		return false;
	}

	private void draw(Canvas c, Item item, Point center) {
		ItemizedOverlay.drawAt(c, item.getMarker(), center.x, center.y, false);
	}

	/**
	 * Called when an Item is tabbed.
	 * 
	 * @param item
	 *            The tabbed Item
	 * @return Whether there is a reaction on the tab or not.
	 */
	protected boolean onTab(Item item) {
		return false;
	}
}
