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
import java.net.ConnectException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import mmenning.mobilegis.R;
import mmenning.mobilegis.map.wms.WMSView.WMSListener;

import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;

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
 * Activity to display WMS stored in the database.
 * 
 * @author Mathias Menninghaus
 * @version 02.10.2009
 * 
 * @see {@link WMSLayerActivity}
 * @see {@link WMSView}
 */
public class WMSActivity extends Activity {

	private static final String DT = "WMSActivity";

	/*
	 * request
	 */
	/**
	 * Standard Request for WMSActivity to show all WMS informations
	 */
	public static final int SHOW_WMS_INFO = 8084;
	
	/*
	 * Constants to manage menu
	 */
	private static final int ADD = 4;

	/*
	 * Constants to manage dialogs
	 */
	private static final int LOADING_DIALOG = 0;
	private static final int EXCEPTION_DIALOG = 2;
	private static final int LOADING_PROGRESS_DIALOG = 1;
	private static final int GOBACKFAST = 3;

	/*
	 * holds the current dialog message if an exception occurs while loading new
	 * wms data
	 */
	private static final int NO_EXCEPTION_TO_SHOW = -1;
	private int EXCEPTION_DIALOG_MESSAGE = NO_EXCEPTION_TO_SHOW;

	/**
	 * The database to read from
	 */
	private WMSDB db;

	/**
	 * holds the views to manipulate them on resume from and preference change
	 */
	private HashMap<Integer, WMSView> views;

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		menu.add(0, ADD, 0, R.string.addwms).setIcon(R.drawable.menu_add);
		menu.add(0, GOBACKFAST, 0, R.string.gobackfast).setIcon(
				R.drawable.menu_map);
		return true;
	}

	@Override
	public boolean onMenuItemSelected(int featureId, MenuItem item) {
		switch (item.getItemId()) {

		case ADD:
			WMSActivity.this.showDialog(LOADING_DIALOG);
			return true;
		case GOBACKFAST:
			this.setResult(WMSLayerActivity.GOBACK_FAST);
			this.finish();
			return true;
		}

		return super.onMenuItemSelected(featureId, item);
	}

	private LinearLayout root;

	private void generateView() {

		/*
		 * get the content view and the view container where the wmsViews should
		 * be stored
		 */
		this.setContentView(R.layout.wmsactivityview);
		root = (LinearLayout) this.findViewById(R.id.wmsactivityview_root);

		views.clear();

		WMSData[] wms = db.getAllWMS();
		for (WMSData w : wms) {
			addWMSView(w);
		}

	}

	private void addWMSView(WMSData wms) {
		WMSView add = (WMSView) View.inflate(this, R.layout.wmsview, null);
		root.addView(add);
		views.put(wms.id, add);
		add.init(wms, new MyWMSListener(), root.getChildCount() - 1, db.getLayerCount(wms.id));
		setWarnings(wms.id);
	}

	/**
	 * Sets the Warnings displayed for a specific wmsID 
	 * 
	 * @param wmsID
	 */
	private void setWarnings(int wmsID) {
		WMSView view = views.get(wmsID);
		List<String> warnings = new LinkedList<String>();
		String srs = db.getSRSforVisibleLayers(wmsID);

		if (srs == null) {
			warnings.add(this.getString(R.string.no_srs));
		} else {
			if (srs.equals("")) {
				warnings.add(this.getString(R.string.srs_not_definite));
			}
			if (!srs.equals(WMSUtils.idealSRS)) {
				warnings.add(this.getString(R.string.srs_not_ideal)
						+ (" (" + WMSUtils.idealSRS + ")"));
			}
			if (!srs.equals(WMSUtils.recommendedSRS)) {
				warnings.add(this.getString(R.string.srs_not_recommended)
						+ (" (" + WMSUtils.recommendedSRS + ")"));
			}
		}
		String version = db.getWMSData(wmsID, WMSDB.WMS_version);
		if (version == null || !version.equals(WMSUtils.VERSION)) {
			warnings.add(this.getString(R.string.wrongversion));
		}

		boolean supportsPNG = Integer.valueOf(db.getWMSData(wmsID,
				WMSDB.WMS_supportsPNG)) == WMSDB.TRUE;
		if (!supportsPNG) {
			warnings.add(this.getString(R.string.no_png));
		}
		view.setWarnings(warnings);

	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {

		db.open();

		super.onActivityResult(requestCode, resultCode, data);
		if (requestCode == WMSLayerActivity.SHOW_WMS_LAYERINFO) {
			switch (resultCode) {
			case WMSLayerActivity.RESULT_OK:
				final int rootID = data.getExtras().getInt(WMSLayerActivity.ID);
				setWarnings(rootID);
				break;
			case WMSLayerActivity.GOBACK_FAST:
				/*
				 * result always the same, set in onCreate
				 */
				finish();
				break;
			}
		}
		db.close();
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		db = new WMSDB(this);
		db.open();

		views = new HashMap<Integer, WMSView>();
		generateView();

		db.close();

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
			b.setView(et);
			et.setText("http://www.pegelonline.wsv.de/webservices/gis/wms");
			b.setTitle(R.string.load_wms);
			b.setPositiveButton(R.string.confirm,
					new AlertDialog.OnClickListener() {

						public void onClick(DialogInterface dialog, int which) {
							WMSActivity.this.dismissDialog(LOADING_DIALOG);
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
			builder.setTitle(WMSActivity.this
					.getString(R.string.loadingfailure));
			builder.setMessage(this.getString(this.EXCEPTION_DIALOG_MESSAGE))
					.setCancelable(false).setPositiveButton(
							this.getString(R.string.confirm),
							new DialogInterface.OnClickListener() {
								public void onClick(DialogInterface dialog,
										int id) {
									WMSActivity.this
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
				WMSActivity.this.showDialog(LOADING_PROGRESS_DIALOG);
				return;
			case LoadingThread.SUCCESS:
				WMSActivity.this.dismissDialog(LOADING_PROGRESS_DIALOG);
				/*
				 * 
				 */
				WMSActivity.this.generateView();
				return;
			}
			WMSActivity.this.dismissDialog(LOADING_PROGRESS_DIALOG);

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
			WMSActivity.this.showDialog(EXCEPTION_DIALOG);
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
				this.handler.sendEmptyMessage(START);

				URL url = new URL(WMSUtils.generateGetCapabilitiesURL(this.url));

				/* Get a SAXParser from the SAXPArserFactory. */
				SAXParserFactory spf = SAXParserFactory.newInstance();
				SAXParser sp = spf.newSAXParser();

				/* Get the XMLReader of the SAXParser we created. *///
				XMLReader xr = sp.getXMLReader();
				/* Create a new ContentHandler and apply it to the XML-Reader */
				GetCapabilitiesHandler handler = new GetCapabilitiesHandler();
				xr.setContentHandler(handler);

				/* Parse the xml-data from our URL. */
				InputSource in = new InputSource(url.openStream());
				in.setEncoding(WMSUtils.ENCODING);

				xr.parse(in);

				ParsedWMSDataSet data = handler.getParsedData();

				db.addParsedData(data);

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

	private class MyWMSListener implements WMSListener {

		public void onClick(WMSView view) {
			Intent i = new Intent(WMSActivity.this, WMSLayerActivity.class);
			i.putExtra(WMSLayerActivity.WMSID, view.getWmsID());
			WMSActivity.this.startActivityForResult(i,
					WMSLayerActivity.SHOW_WMS_LAYERINFO);
		}

		public void deleteWMS(WMSView view) {
			db.deleteWMS(view.getWmsID());
			root.removeView(view);
			views.remove(view.getWmsID());
			updateIndizesAndPriority();
		}

		public void onVisibleChanged(WMSView view, boolean visible) {
			db.setWMSVisibility(view.getWmsID(), visible);
		}

		public void down(WMSView view) {
			int index = view.getIndex();
			if (index < root.getChildCount() - 1) {
				root.removeViewAt(index);
				index++;
				root.addView(view, index);
				updateIndizesAndPriority();
			}
		}

		public void up(WMSView view) {
			int index = view.getIndex();
			if (index > 0) {
				root.removeViewAt(index);
				index--;
				root.addView(view, index);
				updateIndizesAndPriority();
			}
		}
	}

	private void updateIndizesAndPriority() {
		for (int i = 0; i < root.getChildCount(); i++) {
			WMSView v = (WMSView) root.getChildAt(i);
			v.setIndex(i);
			db.setPriority(v.getWmsID(), root.getChildCount()-i);
		}
	}
}
