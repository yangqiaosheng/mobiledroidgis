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

import java.util.Date;

import mmenning.mobilegis.Preferences;
import mmenning.mobilegis.R;
import mmenning.mobilegis.map.MapControls.MapControlsListener;
import mmenning.mobilegis.map.georss.GeoRSSActivity;
import mmenning.mobilegis.map.georss.GeoRSSDB;
import mmenning.mobilegis.map.georss.GeoRSSEntry;
import mmenning.mobilegis.map.georss.GeoRSSEntryActivity;
import mmenning.mobilegis.map.georss.GeoRSSFeed;
import mmenning.mobilegis.map.georss.GeoRSSOverlay;
import mmenning.mobilegis.map.georss.GeoRSSUtils;
import mmenning.mobilegis.map.georss.GeoRSSOverlay.GeoRSSOverlayListener;
import mmenning.mobilegis.map.sos.MeasurementActivity;
import mmenning.mobilegis.map.sos.MeasurementData;
import mmenning.mobilegis.map.sos.SOSActivity;
import mmenning.mobilegis.map.sos.SOSDB;
import mmenning.mobilegis.map.sos.SOSData;
import mmenning.mobilegis.map.sos.SOSOverlay;
import mmenning.mobilegis.map.sos.SOSUtils;
import mmenning.mobilegis.map.sos.SOSOverlay.SOSOverlayListener;
import mmenning.mobilegis.map.wms.WMSActivity;
import mmenning.mobilegis.map.wms.WMSDB;
import mmenning.mobilegis.map.wms.WMSOverlay;
import mmenning.mobilegis.map.wms.WMSUtils;
import mmenning.mobilegis.surface3d.SurfaceVisualizer;
import mmenning.mobilegis.util.ProgressAnimationManager;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.Window;
import android.widget.Toast;

import com.google.android.maps.GeoPoint;
import com.google.android.maps.MapActivity;
import com.google.android.maps.MapController;
import com.google.android.maps.MyLocationOverlay;

/**
 * Main-Activity of this Application. A GoogleMaps view to display the users
 * actual position. From here the SurfaceVisualizer can be called. Overlayed
 * with the activated WMSLayers, GeoRSSFeeds and SOSData.
 * 
 * @author Mathias Menninghaus
 * 
 * @version 30.11.2009
 * 
 */
public class MainMap extends MapActivity {

	/**
	 * Tag for Android LogFile
	 */
	private static final String DT = "MainMap";

	/*
	 * Dialog constants
	 */
	private static final int LAYERMENU_DIALOG = 0;

	/*
	 * constants to manage calling LayerMenus
	 */
	private static final int WMS = 0;
	private static final int GEORSS = 1;
	private static final int SOS = 2;

	/**
	 * Array to manage calling LayerMenus
	 */
	private final CharSequence[] layerMenus = new CharSequence[3];

	/*
	 * MenuItem Constants
	 */
	private static final int OGLVIEW = 0;
	private static final int PREFERENCES = 1;
	private static final int MYLOCATION = 2;
	private static final int LAYERS = 3;
	/*
	 * TODO only for testing
	 */
	private static final int TEST = 666;

	/**
	 * The GoogleMaps View
	 */
	private MyMapView map;
	/**
	 * Controller for the GoogleMaps View
	 */
	private MapController mc;

	/**
	 * Overlay for displaying the users location
	 */
	private MyLocationOverlay myLocation;

	private WMSOverlay wmsOverlay;

	private GeoRSSOverlay geoRSSOverlay;

	private SOSOverlay sosOverlay;

	/**
	 * activate or deactivate the Loaction Update, so we dont need to
	 * instanciate it every time;
	 */
	private boolean myLocationUpdate = false;

	private ProgressAnimationManager progressAnim;

	private GeoRSSDB georssdb;
	private WMSDB wmsdb;
	private SOSDB sosdb;

	private boolean detailsActivated;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		this.requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
		// requestWindowFeature(Window.FEATURE_NO_TITLE);
		// getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
		// WindowManager.LayoutParams.FLAG_FULLSCREEN);
		/*
		 * link to the view
		 */
		setContentView(R.layout.mainmap);
		map = (MyMapView) findViewById(R.id.mapView_mapView);

		/*
		 * deactivate zoom controls
		 */
		map.setBuiltInZoomControls(false);

		/*
		 * and use own ones, because we dont want to have animated zoom
		 */
		MapControls controls = (MapControls) this
				.findViewById(R.id.mapView_mapControls);
		controls.setMapControlsListener(new MapControlsListener() {

			public void OnDetailsCheckedChange(MapControls controls,
					boolean checked) {
				detailsActivated = checked;
			}

			public void OnZoomInClicked(MapControls controls) {
				mc.setZoom(map.getZoomLevel() + 1);
			}

			public void OnZoomOutClicked(MapControls controls) {
				mc.setZoom(map.getZoomLevel() - 1);
			}

		});

		this.georssdb = new GeoRSSDB(this);
		this.wmsdb = new WMSDB(this);
		this.sosdb = new SOSDB(this);

		/*
		 * map controller e.g. to control loaction updates
		 */
		mc = map.getController();

		progressAnim = new ProgressAnimationManager(this);
		wmsOverlay = new WMSOverlay(progressAnim, map);

		geoRSSOverlay = new GeoRSSOverlay(new GeoRSSOverlayListener() {
			public void onItemTabbed(int entryID) {
				if (detailsActivated) {
					Intent intent = new Intent(MainMap.this,
							GeoRSSEntryActivity.class);
					intent.putExtra(GeoRSSEntryActivity.GeoRSSEntryID, entryID);
					MainMap.this.startActivityForResult(intent,
							GeoRSSEntryActivity.SHOW_GeoRSSEntry_INFO);
				} else {
					georssdb.openReadOnly();
					GeoRSSEntry entry = georssdb.getEntry(entryID);
					georssdb.close();
					Toast.makeText(MainMap.this, entry.title,
							Toast.LENGTH_SHORT).show();

				}
			}
		}, GeoRSSUtils.defaultColor);

		sosOverlay = new SOSOverlay(new SOSOverlayListener() {

			public void onItemTabbed(MeasurementData data) {

				if (detailsActivated) {
					Intent intent = new Intent(MainMap.this,
							MeasurementActivity.class);
					intent.putExtra(MeasurementActivity.FeatureID,
							data.featureID);
					intent.putExtra(MeasurementActivity.PropertyID,
							data.propertyID);

					MainMap.this.startActivityForResult(intent,
							MeasurementActivity.SHOW_Measurement_INFO);
				} else {
					sosdb.openReadOnly();
					Date d = sosdb.getYoungestMeasurementTime(data.id);
					String time = null;
					float value = 0;
					if (d != null) {
						time = SOSUtils.sosDisplayFormat.format(d);
						value = sosdb.getYoungestMeasurementValue(data.id);
					}
					sosdb.close();
					Toast
							.makeText(
									MainMap.this,
									((time == null || Float.isNaN(value)) ? MainMap.this
											.getString(R.string.no_data)
											: (time + " " + value + " " + data.unit)),
									Toast.LENGTH_SHORT).show();
				}

			}

		}, SOSUtils.defaultColor);

		layerMenus[WMS] = this.getString(R.string.wms);
		layerMenus[GEORSS] = this.getString(R.string.georss);
		layerMenus[SOS] = this.getString(R.string.sos);

		map.getOverlays().add(wmsOverlay);
		map.getOverlays().add(geoRSSOverlay);
		map.getOverlays().add(sosOverlay);

		myLocation = new MyLocationOverlay(this, map);
		rebuildGeoRSS();
		rebuildWMS();
		rebuildSOS();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		super.onCreateOptionsMenu(menu);
		menu.add(0, MYLOCATION, 0, R.string.my_location).setIcon(
				R.drawable.menu_mylocation);
		menu.add(0, OGLVIEW, 0, R.string.ogl_view).setIcon(
				R.drawable.menu_3dview);
		menu.add(0, PREFERENCES, 0, R.string.preferences).setIcon(
				R.drawable.menu_preferences);
		menu.add(0, LAYERS, 0, R.string.layers).setIcon(R.drawable.menu_layers);
		/*
		 * TODO just for testing
		 */
		// menu.add(0, TEST, 0, "test");
		return true;
	}

	@Override
	public boolean onMenuItemSelected(int featureId, MenuItem item) {
		Intent i;
		switch (item.getItemId()) {
		case OGLVIEW:
			i = new Intent(this, SurfaceVisualizer.class);
			this.startActivity(i);
			return true;
		case PREFERENCES:
			i = new Intent(this, Preferences.class);
			this.startActivity(i);
			return true;
		case MYLOCATION:
			/*
			 * once clicked, enables loaction update
			 */
			if (!myLocationUpdate) {
				map.getOverlays().add(myLocation);
				myLocationUpdate = true;
				myLocation.enableMyLocation();
			}
			/*
			 * else it will only animate to the actual users position
			 */
			GeoPoint g = myLocation.getMyLocation();
			if (g != null)
				mc.setCenter(g);
			return true;
		case LAYERS:
			this.showDialog(LAYERMENU_DIALOG);
			return true;
		case TEST:
			/*
			 * TODO just for testing
			 */
		}
		return super.onMenuItemSelected(featureId, item);
	}

	@Override
	protected boolean isRouteDisplayed() {
		/*
		 * nothing of interest
		 */
		return false;
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if (requestCode == WMSActivity.SHOW_WMS_INFO) {
			switch (resultCode) {
			case RESULT_OK:
				rebuildWMS();
				break;
			}
		} else if (requestCode == GeoRSSActivity.SHOW_GeoRSS_INFO) {

			switch (resultCode) {
			case RESULT_OK:
				rebuildGeoRSS();
				break;
			case GeoRSSEntryActivity.SHOW_GeoRSSEntry_INMAP:

				/*
				 * here we come, when we started a GeoRSSActivity to show some
				 * infos and the user selected an entry and wants to have this
				 * entry displayed
				 */
				mc.setCenter(new GeoPoint(data.getIntExtra(
						GeoRSSEntryActivity.LatitudeE6, 0), data.getIntExtra(
						GeoRSSEntryActivity.LongitudeE6, 0)));
				rebuildGeoRSS();
				break;
			}
		} else if (requestCode == GeoRSSEntryActivity.SHOW_GeoRSSEntry_INFO) {
			switch (resultCode) {
			case GeoRSSEntryActivity.SHOW_GeoRSSEntry_INMAP:

				/*
				 *
				 */
				mc.setCenter(new GeoPoint(data.getIntExtra(
						GeoRSSEntryActivity.LatitudeE6, 0), data.getIntExtra(
						GeoRSSEntryActivity.LongitudeE6, 0)));

				break;
			case RESULT_OK:

				break;
			}
		} else if (requestCode == SOSActivity.SHOW_SOS_INFO) {
			switch (resultCode) {
			case MeasurementActivity.SHOW_Measurement_INMAP:
				rebuildSOS();
				mc.setCenter(new GeoPoint(data.getIntExtra(
						MeasurementActivity.LatitudeE6, 0), data.getIntExtra(
						MeasurementActivity.LongitudeE6, 0)));

				break;
			case RESULT_OK:
				rebuildSOS();
				break;
			}

		} else if (requestCode == MeasurementActivity.SHOW_Measurement_INFO) {
			switch (resultCode) {
			case RESULT_OK:
				break;
			case MeasurementActivity.SHOW_Measurement_INMAP:
				mc.setCenter(new GeoPoint(data.getIntExtra(
						MeasurementActivity.LatitudeE6, 0), data.getIntExtra(
						MeasurementActivity.LongitudeE6, 0)));
			}
		}

	}

	@Override
	protected Dialog onCreateDialog(int id) {

		AlertDialog.Builder b;
		switch (id) {

		case LAYERMENU_DIALOG:
			b = new AlertDialog.Builder(this);
			b.setTitle(R.string.choose_layer_menu);
			b.setItems(layerMenus, new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog,
						int selectedLayerMenu) {
					dialog.dismiss();
					Intent intent;
					switch (selectedLayerMenu) {
					case WMS:
						intent = new Intent(MainMap.this, WMSActivity.class);
						MainMap.this.startActivityForResult(intent,
								WMSActivity.SHOW_WMS_INFO);
						break;
					case GEORSS:
						intent = new Intent(MainMap.this, GeoRSSActivity.class);
						MainMap.this.startActivityForResult(intent,
								GeoRSSActivity.SHOW_GeoRSS_INFO);
						break;
					case SOS:
						intent = new Intent(MainMap.this, SOSActivity.class);
						MainMap.this.startActivityForResult(intent,
								SOSActivity.SHOW_SOS_INFO);
					}
				}
			});
			return b.create();
		}
		return super.onCreateDialog(id);
	}

	private void rebuildWMS() {
		wmsOverlay.makeSleeping();

		new Thread() {
			public void run() {
				layerBuildingHandler.sendEmptyMessage(STARTANIM);
				wmsOverlay.clear();
				wmsdb.openReadOnly();
				int[] wms = wmsdb.getVisibleWMS();

				for (int i = wms.length - 1; i >= 0; i--) {
					String getMapBaseURL = WMSUtils.generateGetMapBaseURL(wmsdb
							.getWMSData(wms[i], WMSDB.WMS_getMapURL), wmsdb
							.getVisibleLayerNames(wms[i]), wmsdb
							.getSRSforVisibleLayers(wms[i]));

					wmsOverlay.addLoader(getMapBaseURL);
				}
				wmsdb.close();
				wmsOverlay.makeAwake();
				layerBuildingHandler.sendEmptyMessage(STOPANIM);
			}
		}.start();
	}

	private void rebuildSOS() {

		sosOverlay.makeSleeping();
		sosOverlay.clear();

		new Thread() {
			public void run() {
				layerBuildingHandler.sendEmptyMessage(STARTANIM);
				sosdb.openReadOnly();

				SOSData[] sosDatas = sosdb.getVisibleSOS();

				for (SOSData sos : sosDatas) {

					MeasurementData[] measurementDatas = sosdb
							.getNotEmptyMeasurementDataArray(sosdb
									.getSelectedProperty(sos.selectedOffering));

					Log.d(DT, measurementDatas.length
							+ " measuremnts to be drawn");

					sosOverlay.setColorForFollowing(sos.color);
					for (MeasurementData m : measurementDatas) {
						sosOverlay.addOverlay(m);
					}
				}
				sosOverlay.makeAwake();
				sosdb.close();
				layerBuildingHandler.sendEmptyMessage(STOPANIM);
			}
		}.start();
	}

	private void rebuildGeoRSS() {

		geoRSSOverlay.makeSleeping();
		geoRSSOverlay.clear();

		new Thread() {
			public void run() {
				layerBuildingHandler.sendEmptyMessage(STARTANIM);
				georssdb.openReadOnly();
				GeoRSSFeed[] feeds = georssdb.getAllVisibleGeoRSS();
				for (GeoRSSFeed feed : feeds) {
					GeoRSSEntry[] entries = georssdb.getEntrys(feed.id);
					geoRSSOverlay.setColorForFollowing(feed.color);
					for (GeoRSSEntry entry : entries) {
						geoRSSOverlay.addOverlay(entry);
					}
				}
				geoRSSOverlay.makeAwake();
				georssdb.close();
				layerBuildingHandler.sendEmptyMessage(STOPANIM);
			}
		}.start();

	}

	private static final int STARTANIM = 0;
	private static final int STOPANIM = 1;

	final Handler layerBuildingHandler = new Handler() {

		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case STARTANIM:
				progressAnim.start();
				break;
			case STOPANIM:
				progressAnim.stop();
				map.invalidate();
				break;
			}
			super.handleMessage(msg);
		}

	};

	@Override
	protected void onPause() {

		if (myLocationUpdate) {
			myLocation.disableMyLocation();
		}
		super.onPause();
	}

	@Override
	protected void onResume() {

		SharedPreferences prefs = PreferenceManager
				.getDefaultSharedPreferences(this);
		if (prefs.getBoolean(this.getString(R.string.enablegooglemaps), false)) {
			map.setGoogleMapsVisibility(true);
			if (prefs.getBoolean(this.getString(R.string.enablesatellite),
					false)) {
				map.setSatellite(true);
			} else {
				map.setSatellite(false);
			}
		} else {
			map.setGoogleMapsVisibility(false);

		}
		if (prefs
				.getBoolean(this.getString(R.string.enabletransparency), false)) {
			wmsOverlay.setTransparency(0x88);
		} else {
			wmsOverlay.setTransparency(0xFF);
		}

		if (myLocationUpdate) {
			myLocation.enableMyLocation();
		}

		map.invalidate();
		super.onResume();
	}
}