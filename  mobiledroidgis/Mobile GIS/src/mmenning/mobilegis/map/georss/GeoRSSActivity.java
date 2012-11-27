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

import java.io.IOException;
import java.net.ConnectException;
import java.net.MalformedURLException;
import java.net.UnknownHostException;
import java.util.HashMap;

import javax.xml.parsers.ParserConfigurationException;

import mmenning.mobilegis.Preferences;
import mmenning.mobilegis.R;
import mmenning.mobilegis.map.georss.GeoRSSFeedView.GeoRSSFeedListener;

import org.xml.sax.SAXException;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;

/**
 * Activity which displays all GeoRSSFeeds in the database.
 * 
 * @author Mathias Menninghaus
 * @version 15.10.2009
 * 
 */
public class GeoRSSActivity extends Activity {

	private static final String DT = "GeoRSSActivity";

	/*
	 * request
	 */
	/**
	 * Standard Request for GeoRSSActivity to show all Feed informations
	 */
	public static final int SHOW_GeoRSS_INFO = 9080;

	/*
	 * Constants to manage Menu
	 */
	private static final int SYNC = 0;
	private static final int PREFERENCES = 2;
	private static final int ADD = 3;

	/*
	 * Constants to manage dialogs
	 */
	private static final int LOADING_DIALOG = 0;
	private static final int EXCEPTION_DIALOG = 2;
	private static final int LOADING_PROGRESS_DIALOG = 1;

	/*
	 * holds the current dialog message if an exception occurs while loading new
	 * wms data
	 */
	private static final int NO_EXCEPTION_TO_SHOW = -1;
	private int EXCEPTION_DIALOG_MESSAGE = NO_EXCEPTION_TO_SHOW;

	/**
	 * Manager to sync feeds
	 */
	private GeoRSSManager manage;

	/**
	 * Database to manage GeoRSS data
	 */
	private GeoRSSDB db;

	private LinearLayout root;

	private HashMap<Integer, GeoRSSFeedView> views;

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		menu.add(0, SYNC, 0, R.string.synchronize).setIcon(
				R.drawable.menu_refresh);
		menu.add(0, PREFERENCES, 0, R.string.preferences).setIcon(
				R.drawable.menu_preferences);
		menu.add(0, ADD, 0, R.string.add_georss).setIcon(R.drawable.menu_add);
		return true;
	}

	@Override
	public boolean onMenuItemSelected(int featureId, MenuItem item) {
		switch (item.getItemId()) {
		case SYNC:
			new Thread(new LoadingThread(null, new LoadingHandler())).start();
			this.generateView();
			return true;
		case PREFERENCES:
			Intent i = new Intent(this, Preferences.class);
			this.startActivity(i);
			return true;
		case ADD:
			GeoRSSActivity.this.showDialog(LOADING_DIALOG);
			return true;
		}

		return super.onMenuItemSelected(featureId, item);
	}

	private void generateView() {

		/*
		 * get the content view and the view container where the GeoRSSFeedViews
		 * should be stored
		 */
		this.setContentView(R.layout.georssactivityview);
		root = (LinearLayout) this.findViewById(R.id.georssactivityview_root);

		views.clear();
		GeoRSSFeed[] feeds = db.getAllGeoRSS();
		for (GeoRSSFeed f : feeds) {

			GeoRSSFeedView add = (GeoRSSFeedView) View.inflate(this,
					R.layout.georssfeedview, null);

			root.addView(add);
			views.put(f.id, add);

			add.init(f, new MyGeoRSSFeedListener());
			add.setCount(db.getUnreadEntryCount(f.id), db
					.getTotalEntryCount(f.id));

		}
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {

		super.onActivityResult(requestCode, resultCode, data);
		db.open();
		if (requestCode == GeoRSSEntriesActivity.SHOW_GeoRSS_ENTRIES) {
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
				 * maybe somefeeds where read, so setup title
				 */
				int geoRSSID = data.getExtras().getInt(
						GeoRSSEntriesActivity.GeoRSSFeedID);

				views.get(geoRSSID).setCount(db.getUnreadEntryCount(geoRSSID),
						db.getTotalEntryCount(geoRSSID));

			}
		}
		db.close();

	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		views = new HashMap<Integer, GeoRSSFeedView>();
		db = new GeoRSSDB(this);
		db.open();

		generateView();

		db.close();

		manage = new GeoRSSManager(this);
		this.setResult(RESULT_OK);
	}

	@Override
	protected Dialog onCreateDialog(int id) {
		switch (id) {
		case LOADING_PROGRESS_DIALOG:
			ProgressDialog progressDialog = new ProgressDialog(this);
			progressDialog.setMessage(this.getString(R.string.loading__));
			progressDialog.setCancelable(false);
			return progressDialog;

		case LOADING_DIALOG:
			AlertDialog.Builder b = new AlertDialog.Builder(this);
			final EditText et = new EditText(this);
			et
					.setText("http://earthquake.usgs.gov/eqcenter/catalogs/eqs7day-M5.xml");
			b.setView(et);
			b.setTitle(R.string.load_georssfeed);
			b.setPositiveButton(R.string.confirm,
					new AlertDialog.OnClickListener() {
						public void onClick(DialogInterface dialog, int which) {
							dialog.dismiss();
							new Thread(new LoadingThread(et.getText()
									.toString(), new LoadingHandler())).start();

						}
					});
			b.setNegativeButton(R.string.cancel,
					new AlertDialog.OnClickListener() {

						public void onClick(DialogInterface dialog, int which) {
							dialog.cancel();
						}
					});
			return b.create();
		case EXCEPTION_DIALOG:
			AlertDialog.Builder builder = new AlertDialog.Builder(this);
			builder.setTitle(this.getString(R.string.loadingfailure));
			builder.setMessage(this.getString(this.EXCEPTION_DIALOG_MESSAGE))
					.setCancelable(false).setPositiveButton(
							this.getString(R.string.confirm),
							new DialogInterface.OnClickListener() {
								public void onClick(DialogInterface dialog,
										int id) {
									GeoRSSActivity.this
											.dismissDialog(EXCEPTION_DIALOG);
								}
							});

			AlertDialog exceptionDialog = builder.create();
			return exceptionDialog;

		}
		return super.onCreateDialog(id);
	}

	@Override
	protected void onPause() {
		super.onPause();
		db.close();
	}

	@Override
	protected void onPrepareDialog(int id, Dialog dialog) {
		switch (id) {
		case EXCEPTION_DIALOG:
			if (EXCEPTION_DIALOG_MESSAGE != NO_EXCEPTION_TO_SHOW) {
				((AlertDialog) dialog).setMessage(this
						.getString(this.EXCEPTION_DIALOG_MESSAGE));
			}
			break;
		}
		super.onPrepareDialog(id, dialog);
	}

	@Override
	protected void onResume() {
		db.open();
		super.onResume();
	}

	private class LoadingHandler extends Handler {

		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case LoadingThread.START:
				GeoRSSActivity.this.showDialog(LOADING_PROGRESS_DIALOG);
				db.close();
				return;
			case LoadingThread.SUCCESS:
				GeoRSSActivity.this.dismissDialog(LOADING_PROGRESS_DIALOG);
				db.open();
				GeoRSSActivity.this.generateView();
				return;
			}
			GeoRSSActivity.this.dismissDialog(LOADING_PROGRESS_DIALOG);
			db.open();
			switch (msg.what) {
			case LoadingThread.SAXEX:
				EXCEPTION_DIALOG_MESSAGE = R.string.saxex;
				break;
			case LoadingThread.IOEX:
				EXCEPTION_DIALOG_MESSAGE = R.string.ioexc;
				break;
			case LoadingThread.PARSCONFIGEX:
				EXCEPTION_DIALOG_MESSAGE = R.string.parsconfigex;
				break;
			case LoadingThread.MFURLEX:
				EXCEPTION_DIALOG_MESSAGE = R.string.mlfurlex;
				break;
			case LoadingThread.CONNECTEX:
				EXCEPTION_DIALOG_MESSAGE = R.string.connectex;
				break;
			case LoadingThread.UNKNOWNHEX:
				EXCEPTION_DIALOG_MESSAGE = R.string.unknownhex;
				break;
			}
			GeoRSSActivity.this.showDialog(EXCEPTION_DIALOG);
		}
	}

	private class LoadingThread implements Runnable {

		public static final int SAXEX = 0;
		public static final int IOEX = 1;
		public static final int PARSCONFIGEX = 2;
		public static final int SUCCESS = 3;
		public static final int MFURLEX = 4;
		public static final int CONNECTEX = 5;
		public static final int UNKNOWNHEX = 6;
		public static final int START = 7;

		private Handler handler;
		private String url;

		public LoadingThread(String url, Handler handler) {
			this.handler = handler;
			this.url = url;
		}

		public void run() {

			try {
				handler.sendEmptyMessage(START);

				if (url != null) {
					manage.addGeoRSS(this.url);
				} else {
					manage.sync();
				}
				this.handler.sendEmptyMessage(SUCCESS);
			} catch (ConnectException e) {
				Log.w(DT, e);
				this.handler.sendEmptyMessage(CONNECTEX);
			} catch (UnknownHostException e) {
				Log.w(DT, e);
				this.handler.sendEmptyMessage(UNKNOWNHEX);
			} catch (MalformedURLException e) {
				Log.w(DT, e);
				this.handler.sendEmptyMessage(MFURLEX);
			} catch (IOException e) {
				Log.w(DT, "IOException", e);
				this.handler.sendEmptyMessage(IOEX);
			} catch (ParserConfigurationException e) {
				Log.w(DT, "ParserConfiguration", e);
				this.handler.sendEmptyMessage(IOEX);
			} catch (SAXException e) {
				Log.w(DT, "SAXException", e);
				this.handler.sendEmptyMessage(SAXEX);
			}
		}
	}

	private class MyGeoRSSFeedListener implements GeoRSSFeedListener {

		public void onClick(GeoRSSFeedView view) {
			Intent i = new Intent(GeoRSSActivity.this,
					GeoRSSEntriesActivity.class);
			i.putExtra(GeoRSSEntriesActivity.GeoRSSFeedID, view.getGeoRSSID());
			GeoRSSActivity.this.startActivityForResult(i,
					GeoRSSEntriesActivity.SHOW_GeoRSS_ENTRIES);

		}

		public void onColorChanged(GeoRSSFeedView view, int newColor) {
			db.setGeoRSSFeedColor(view.getGeoRSSID(), newColor);
		}

		public void onDeleteClicked(GeoRSSFeedView view) {
			db.deleteGEORSS(view.getGeoRSSID());
			root.removeView(view);
		}

		public void onVisibilityChanged(GeoRSSFeedView view, boolean visible) {
			db.setGeoRSSFeedVisible(view.getGeoRSSID(), visible);

		}

		public void onMarkReadClicked(GeoRSSFeedView view) {
			db.setAllRead(view.getGeoRSSID(), true);
			view.setCount(db.getUnreadEntryCount(view.getGeoRSSID()), db
					.getTotalEntryCount(view.getGeoRSSID()));
		}
	}
}
