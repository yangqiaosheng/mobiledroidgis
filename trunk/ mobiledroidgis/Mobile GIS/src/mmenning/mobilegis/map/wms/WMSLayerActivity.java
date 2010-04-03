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

import mmenning.mobilegis.R;
import mmenning.mobilegis.database.NetImageStorage;
import mmenning.mobilegis.map.wms.WMSLayerView.LayerListener;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

/**
 * Activity to display informations about all containing WMSLayers of a WMSLayer
 * or WMS.
 * 
 * @author Mathias Menninghaus
 * @version 15.10.2009
 * 
 */
public class WMSLayerActivity extends Activity {

	private static final String DT = "WMSLayerActivity";
	/*
	 * request
	 */
	/**
	 * Standard Request for WMSLayerActivity to show all informations of
	 * containing or rootLayer identified by intent extras WMSID or WMSLayerID
	 */
	public static final int SHOW_WMS_LAYERINFO = 8085;
	/*
	 * response
	 */
	/**
	 * Finish activity as soon as possible and go back to the map
	 */
	public static final int GOBACK_FAST = 8080;

	/**
	 * ID of the WMS which rootLayer is to be displayed by the WMSLayerActivity
	 */
	public static final String WMSID = "mmenning.mobilegis.map.wms/WMSID";
	/**
	 * ID of the WMSLayer which is the rootLayer of all WMSLayers displayed in
	 * the WMSLayerActivity
	 */
	public static final String WMSLayerID = "mmenning.mobilegis.map.wms/WMSLayerID";
	/**
	 * Callback from WMSLayerActivity which WMS was manipulated by
	 * WMSLayerActivity.
	 */
	public static final String ID = "mmenning.mobilegis.map.wms/ID";

	/*
	 * Menu Entries
	 */
	private static final int GOBACKFAST = 0;

	private WMSDB db;

	private int rootID;

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		menu.add(0, GOBACKFAST, 0, R.string.gobackfast).setIcon(
				R.drawable.menu_map);
		return true;
	}

	@Override
	public boolean onMenuItemSelected(int featureId, MenuItem item) {
		switch (item.getItemId()) {
		case GOBACKFAST:
			this.setResult(GOBACK_FAST);
			this.finish();
			return true;
		}
		return super.onMenuItemSelected(featureId, item);
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if (requestCode == SHOW_WMS_LAYERINFO) {
			switch (resultCode) {
			case RESULT_OK:
				/*
				 * default result already set in onCreate()
				 */
				break;
			case GOBACK_FAST:
				this.setResult(GOBACK_FAST, data);
				db.close();
				finish();
				break;
			}
		}
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		db = new WMSDB(this);
		db.open();

		LayerData[] layers = new LayerData[0];
		Bundle extras = this.getIntent().getExtras();
		if (extras != null) {
			if (extras.containsKey(WMSLayerID)) {
				rootID = extras.getInt(WMSLayerID);
				layers = db.getContainingLayers(rootID);
				this.setTitle(db.getLayerData(rootID, WMSDB.LAYER_title));

			} else if (extras.containsKey(WMSID)) {
				rootID = extras.getInt(WMSID);
				layers = new LayerData[] { (db.getLayer(Integer.valueOf(db
						.getWMSData(rootID, WMSDB.WMS_rootLayer)))) };
				this.setTitle(db.getWMSData(rootID, WMSDB.WMS_title));
			}

		}

		 NetImageStorage netImageStorage = new NetImageStorage(this);

		this.setContentView(R.layout.layeractivityview);
		LinearLayout root = (LinearLayout) this
				.findViewById(R.id.layeractivityview_root);

		for (LayerData l : layers) {

			WMSLayerView add = (WMSLayerView) View.inflate(this,
					R.layout.layerview, null);

			root.addView(add);

			add.init(l, new MyLayerListener(l.id), db
					.getContainingLayerCount(l.id), netImageStorage
					.load(l.attribution_logourl), netImageStorage
					.load(l.legend_url));
		}

		db.close();
		/*
		 * setup default result ok and root ID (Layer or WMS)
		 */
		Intent result = new Intent();
		result.putExtra(ID, rootID);
		this.setResult(RESULT_OK, result);
	}

	@Override
	protected void onPause() {
		super.onPause();
		db.close();
	}

	@Override
	protected void onResume() {
		db.open();
		super.onResume();
	}

	public class MyLayerListener implements LayerListener {

		private CharSequence[] srsitems;
		private int currentsrs;

		public MyLayerListener(int layerID) {
			srsitems = db.getSRS(layerID);
			String actsrs = db.getLayerData(layerID, WMSDB.LAYER_selectedSRS);
			currentsrs = srsitems.length;
			while (--currentsrs >= 0 && !srsitems[currentsrs].equals(actsrs))
				;

		}

		public void onClick(WMSLayerView view) {
			Intent i = new Intent(WMSLayerActivity.this, WMSLayerActivity.class);
			i.putExtra(WMSLayerActivity.WMSLayerID, view.getLayerID());
			WMSLayerActivity.this.startActivityForResult(i,
					WMSLayerActivity.SHOW_WMS_LAYERINFO);
		}

		public void onSRSClicked(final WMSLayerView view, final TextView srs) {

			AlertDialog.Builder builder = new AlertDialog.Builder(
					WMSLayerActivity.this);
			builder
					.setTitle(WMSLayerActivity.this
							.getText(R.string.select_srs));
			builder.setSingleChoiceItems(srsitems, currentsrs,
					new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog, int item) {
							srs.setText(srsitems[item]);
							db.setSRS(view.getLayerID(), srsitems[item]
									.toString());
							currentsrs = item;
							dialog.dismiss();
						}
					});
			AlertDialog alert = builder.create();
			alert.show();

		}

		public void onVisibleChanged(WMSLayerView view, boolean visible) {
			db.setLayerVisibility(view.getLayerID(), visible);

		}
	}

}
