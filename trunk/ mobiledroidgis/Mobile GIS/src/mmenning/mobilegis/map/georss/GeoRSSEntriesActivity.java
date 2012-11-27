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
package mmenning.mobilegis.map.georss;

import mmenning.mobilegis.R;
import mmenning.mobilegis.map.georss.GeoRSSEntryItemView.OnEntryItemClickListener;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;

/**
 * Activity to display all GeoRSSEntries of specific GeoRSSFeed identified by
 * Intent Extra GeoRSSFeedID.
 * 
 * @author Mathias Menninghaus
 * 
 */
public class GeoRSSEntriesActivity extends Activity {

	private static final String DT = "GeoRSSEntriesActivity";
	
	/*
	 * request
	 */
	/**
	 * Standard Request: show all Feeds containing in the GeoRSSFeed identified
	 * by GeoRSSFeedID.
	 */
	public static final int SHOW_GeoRSS_ENTRIES = 9083;
	/*
	 * intent extras
	 */
	/**
	 * ID of the GeoRSSFeed to be displayed by the GeoRSSEntriesActivity
	 */
	public static final String GeoRSSFeedID = "mmenning.mobilegis.map.georss/GeoRSSFeedID";

	private LinearLayout entryList;
	/**
	 * Database to manage GeoRSS data
	 */
	private GeoRSSDB db;

	private void onEntryClicked(GeoRSSEntryItemView view) {
		if (db.isGeoRSSEntryRead(view.getEntryID())) {

		} else {
			db.setGeoRSSEntryRead(view.getEntryID(), true);
			view.markRead(true);
		}

		/*
		 * Start EntryActivity for Result to get detailed Info
		 */

		Intent intent = new Intent(GeoRSSEntriesActivity.this,
				GeoRSSEntryActivity.class);
		intent.putExtra(GeoRSSEntryActivity.GeoRSSEntryID, view.getEntryID());
		GeoRSSEntriesActivity.this.startActivityForResult(intent,
				GeoRSSEntryActivity.SHOW_GeoRSSEntry_INFO);

	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {

		super.onActivityResult(requestCode, resultCode, data);
		if (requestCode == GeoRSSEntryActivity.SHOW_GeoRSSEntry_INFO) {
			switch (resultCode) {
			case GeoRSSEntryActivity.SHOW_GeoRSSEntry_INMAP:
				/*
				 * just goback fast to display GeoRSSEntry in the calling
				 * MainMap
				 */
				this
						.setResult(GeoRSSEntryActivity.SHOW_GeoRSSEntry_INMAP,
								data);
				this.finish();

				break;
			case RESULT_OK:
				/*
				 * nothing todo, result ok set in onCreate
				 */
			}
		}
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);
		
		int geoRSSID = -1;
		Bundle extras = this.getIntent().getExtras();
		if (extras != null) {
			if (extras.containsKey(GeoRSSFeedID)) {
				geoRSSID = extras.getInt(GeoRSSFeedID);
			}
		}

		this.setContentView(R.layout.georssentriesactivityview);
		entryList = (LinearLayout) this
				.findViewById(R.id.georssentriesactivityview_entryList);
		
		db = new GeoRSSDB(this);

		db.open();
		this.setTitle("(" + db.getTotalEntryCount(geoRSSID) + ") "
				+ db.getGeoRSSTitle(geoRSSID));
		GeoRSSEntry[] entries = db.getEntrys(geoRSSID);

		for (GeoRSSEntry entry : entries) {
			GeoRSSEntryItemView add = (GeoRSSEntryItemView) View.inflate(this,
					R.layout.georssentryitem, null);
			add.init(entry, new OnEntryItemClickListener() {
				public void onClick(GeoRSSEntryItemView view) {
					GeoRSSEntriesActivity.this.onEntryClicked(view);
				}
			});
			entryList.addView(add);
		}
		
		db.close();
		/*
		 * setup default result
		 */
		Intent result = new Intent();
		result.putExtra(GeoRSSFeedID, geoRSSID);
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

}
