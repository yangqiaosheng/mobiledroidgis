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
package mmenning.mobilegis.map.wms;

import java.io.IOException;
import java.net.URL;

import mmenning.mobilegis.map.wms.PriorityLoadingManager.Entry;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Handler;
import android.util.Log;

import com.google.android.maps.GeoPoint;
import com.google.android.maps.Projection;

/**
 * Manages Storage and Loading of WMS Images. The Parts must be identified
 * definite by a Key K. Supports WMS Specification: </br> WMS 1.1.1 </br> OGC
 * 01-068r3 </br>
 * 
 * The local Cache will only hold a specific amount of parts and delete the
 * oldest if it exceeds the limit.
 * 
 * @author Mathias Menninghaus
 * @version 23.10.2009
 * 
 * @see {@link WMSUtils}
 * 
 * @param <K> key Type
 */
public class WMSLoader<K> {

	private static final String DT = "WMSLoader";

	/**
	 * If loading of an image succedes. Loading Threads will notify their
	 * termination with STOP.
	 */
	public static final int LOADSUCCESS = 0;
	/**
	 * If loading of an image fails. Loading Threads will notify their
	 * termination with STOP.
	 */
	public static final int LOADFAIL = 1;
	/**
	 * If a Thread starts
	 */
	public static final int START = 2;
	/**
	 * If a Thread ends.
	 */
	public static final int STOP = 3;

	private String getMapBaseURL;

	private PriorityMapQueue<K, Bitmap> wmsParts;

	private PriorityLoadingManager<K, String> partsToLoad;

	private Thread[] loadingThreads;

	private Handler handler;

	/**
	 * Instantiate a new WMSLoader.
	 * 
	 * @param getMapBaseURL
	 *            BaseURL for WMS requests. {@link WMSUtils}
	 * @param handler
	 *            Handler to handle LoadingThread events.
	 */
	public WMSLoader(String getMapBaseURL, Handler handler) {
		this.wmsParts = new PriorityMapQueue<K, Bitmap>(
				WMSUtils.AVERAGEStoredBitmapsPerLoader,
				WMSUtils.MAXStoredBitmapsPerLoader);
		this.partsToLoad = new PriorityLoadingManager<K, String>();
		this.getMapBaseURL = getMapBaseURL;
		loadingThreads = new Thread[WMSUtils.MAXTHREADSPerLoader];
		this.handler = handler;
	}

	/**
	 * Get a WMSPart specified with a key. If no such one Part will be found in
	 * the Cache it will be load asynchronus and null will be returned. The
	 * LoadingThread will notify the WMSLoaders Handler with LOADSUCCESS if the
	 * part is completly loaded and cached.
	 * 
	 * @param key
	 *            definite identifier for the part to be loaded
	 * @param left
	 *            left coordinate for the part in screenpixels
	 * @param top
	 *            top coordinate for the part in screenpixels
	 * @param p
	 *            projection with which the corners of the part can be
	 *            calculated
	 * @return Bitmap or null if it is not yet cached.
	 */
	public Bitmap loadMap(K key, int left, int top, Projection p) {

		Bitmap ret = wmsParts.getWithUpdate(key);

		/*
		 * DEBUG
		 */
		/*
		 * END DEBUG
		 */

		if (ret == null) {
			/*
			 * if this part is neither currently loaded by a thread nor in the
			 * loading Queue
			 */
			if (!partsToLoad.threadRunsOrIsInQueue(key)) {
				/*
				 * build the url for getMap request
				 */
				GeoPoint[] corners;
				corners = WMSUtils.corners(left, top, p);
				String getMapURL = WMSUtils.generateGetMapURL(getMapBaseURL,
						corners[WMSUtils.LOWERLEFT],
						corners[WMSUtils.UPPERRIGHT]);

				/*
				 * insert to the head of the loading Queue
				 */
				partsToLoad.insertIntoLoadingQueue(key, getMapURL);

				/*
				 * try to start a thread for this if place is available
				 */
				for (int i = 0; i < loadingThreads.length; i++) {
					if (loadingThreads[i] == null
							|| !loadingThreads[i].isAlive()) {
						loadingThreads[i] = new Thread(
								new LoaderThread(handler));
						loadingThreads[i].start();
						i += loadingThreads.length;
					}
				}
			}

		}
		return ret;

	}

	/**
	 * Stop Loading of all Parts. The Threads will finish their current task and
	 * not being interrupted.
	 */
	public void stopLoading() {
		partsToLoad.clearLoadingQueue();
	}

	@Override
	protected void finalize() throws Throwable {
		this.stopLoading();
		super.finalize();
	}

	/**
	 * Inner Class to manage Loading from the Loading Queue.
	 * 
	 * @author Mathias Menninghaus
	 * @version 23.10.2009
	 * 
	 */
	private class LoaderThread implements Runnable {

		private static final String DT = "WMSLoader.LoaderThread";

		private Handler handler;

		public LoaderThread(Handler handler) {
			this.handler = handler;
		}

		public void run() {
			handler.sendEmptyMessage(WMSLoader.START);
			/*
			 * while their are already parts in the loading queue
			 */
			while (!WMSLoader.this.partsToLoad.isEmpty()) {
				URL url = null;
				Bitmap image = null;

				/*
				 * get a task an load the bitmap from this url
				 */
				Entry toLoad = WMSLoader.this.partsToLoad
						.removeFirstAndStartLoading();

				if (toLoad != null) {

					try {
						url = new URL((String) toLoad.value);

						image = BitmapFactory.decodeStream(url.openStream());

						if (image == null) {
							throw new NullPointerException("Image " + url);
						}

						WMSLoader.this.wmsParts.insertWithoutUpdate(
								(K) toLoad.key, image);
						handler.sendEmptyMessage(WMSLoader.LOADSUCCESS);

					} catch (IOException e) {
						Log.w(DT, "IO Exception while loading: " + url);
						Log.w(DT, "IOException: " + e.getClass().getName());

						handler.sendEmptyMessage(WMSLoader.LOADFAIL);

					} catch (NullPointerException ex) {
						Log.w(DT, ex);
						handler.sendEmptyMessage(WMSLoader.LOADFAIL);
					} finally {
						WMSLoader.this.partsToLoad
								.completeLoading((K) toLoad.key);
					}
				}

			}
			handler.sendEmptyMessage(WMSLoader.STOP);
		}

	}
}
