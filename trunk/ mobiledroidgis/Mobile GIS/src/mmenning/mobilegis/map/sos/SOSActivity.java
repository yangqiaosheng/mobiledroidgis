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
package mmenning.mobilegis.map.sos;

import java.util.HashMap;

import mmenning.mobilegis.Preferences;
import mmenning.mobilegis.R;
import mmenning.mobilegis.map.sos.SOSView.SOSViewListener;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;

/**
 * Activity to display all SOS Datasets stored in the SOSDB.
 * 
 * @author Mathias Menninghaus
 * @version 30.11.2009
 * 
 */
public class SOSActivity extends Activity {
	private static final String DT = "SOSActivity";

	/*
	 * request
	 */
	/**
	 * Standard Request for SOSActivity to show all SOS informations
	 */
	public static final int SHOW_SOS_INFO = 7080;

	/*
	 * Constants to manage Menu
	 */
	private static final int PREFERENCES = 2;
	private static final int ADD = 3;

	/*
	 * Constants to manage dialogs
	 */
	private static final int LOADING_DIALOG = 0;
	private static final int EXCEPTION_DIALOG = 1;
	private static final int LOADING_SINGLE_DIALOG = 2;
	private static final int LOADING_MULTIPLE_DIALOG = 3;
	private static final int DELETING_PROGRESS_DIALOG = 4;
	/*
	 * holds the current dialog message if an exception occurs while loading new
	 * wms data
	 */
	private static final int NO_EXCEPTION_TO_SHOW = -1;
	private int EXCEPTION_DIALOG_MESSAGE = NO_EXCEPTION_TO_SHOW;

	/**
	 * Manager to sync feeds
	 */
	private SOSManager manage;

	/**
	 * Database to manage GeoRSS data
	 */
	private SOSDB db;

	private LinearLayout root;

	private HashMap<Integer, SOSView> views;

	private LoadingHandler handler;

	private int multipleProgressCount;

	private ProgressDialog loadingMultiple;

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		menu.add(0, PREFERENCES, 0, R.string.preferences).setIcon(
				R.drawable.menu_preferences);
		menu.add(0, ADD, 0, R.string.add_sos).setIcon(R.drawable.menu_add);
		return true;
	}

	@Override
	public boolean onMenuItemSelected(int featureId, MenuItem item) {
		switch (item.getItemId()) {
		case PREFERENCES:
			Intent i = new Intent(this, Preferences.class);
			this.startActivity(i);
			return true;
		case ADD:
			SOSActivity.this.showDialog(LOADING_DIALOG);
		}

		return super.onMenuItemSelected(featureId, item);
	}

	private void generateView() {

		/*
		 * get the content view and the view container where the GeoRSSFeedViews
		 * should be stored
		 */
		this.setContentView(R.layout.sosactivityview);
		root = (LinearLayout) this.findViewById(R.id.sosactivityview_root);

		views.clear();
		SOSData[] sos = db.getAllSOS();
		for (SOSData s : sos) {

			SOSView add = (SOSView) View.inflate(this, R.layout.sosview, null);

			root.addView(add);
			views.put(s.id, add);

			add.init(s, new MySOSViewListener());

			int selectedOffering = db.getSelectedOffering(s.id);
			add.setOffering(db.getOfferingName(selectedOffering));
			int selectedProperty = db.getSelectedProperty(selectedOffering);
			add.setProperty(db.getProperty(selectedProperty));
			add.setMemberCount(db.getNotEmptyMeasurementCount(selectedProperty)
					+ "/" + db.getAllMeasurementCount(selectedProperty));

		}
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {

		super.onActivityResult(requestCode, resultCode, data);
		if (requestCode == MeasurementActivity.SHOW_Measurement_INFO) {
			switch (resultCode) {
			case MeasurementActivity.SHOW_Measurement_INMAP:
				/*
				 * just goback fast to display GeoRSSEntry in the calling
				 * MainMap
				 */
				this
						.setResult(MeasurementActivity.SHOW_Measurement_INMAP,
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

		handler = new LoadingHandler();
		views = new HashMap<Integer, SOSView>();
		manage = new SOSManager(this, handler);
		db = new SOSDB(this);
		db.open();

		generateView();

		db.close();

		this.setResult(RESULT_OK);
	}

	@Override
	protected Dialog onCreateDialog(int id) {
		AlertDialog.Builder b;
		switch (id) {
		case LOADING_SINGLE_DIALOG:

			ProgressDialog single = new ProgressDialog(this);
			single.setMessage(this.getString(R.string.loading__));
			single.setCancelable(false);
			single.setProgressStyle(ProgressDialog.STYLE_SPINNER);
			return single;

		case DELETING_PROGRESS_DIALOG:
			ProgressDialog delete = new ProgressDialog(this);
			delete.setMessage(this.getString(R.string.deleting___));
			delete.setCancelable(false);
			delete.setProgressStyle(ProgressDialog.STYLE_SPINNER);
			return delete;
		case LOADING_MULTIPLE_DIALOG:
			loadingMultiple = new ProgressDialog(this);
			loadingMultiple.setMessage(this.getString(R.string.loading__));
			loadingMultiple.setCancelable(false);
			loadingMultiple.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);

			return loadingMultiple;

		case LOADING_DIALOG:
			b = new AlertDialog.Builder(this);
			final EditText et = new EditText(this);
			et.setText("http://www.pegelonline.wsv.de/webservices/gis/sos");
			b.setView(et);
			b.setTitle(R.string.load_sos);
			b.setPositiveButton(R.string.confirm,
					new AlertDialog.OnClickListener() {
						public void onClick(DialogInterface dialog, int which) {
							dialog.dismiss();
							new Thread() {
								public void run() {
									manage.addSOS(et.getText().toString());
								}
							}.start();
							generateView();

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
			b = new AlertDialog.Builder(this);
			b.setTitle(this.getString(R.string.loadingfailure));
			b.setMessage(this.getString(this.EXCEPTION_DIALOG_MESSAGE))
					.setCancelable(false).setPositiveButton(
							this.getString(R.string.confirm),
							new DialogInterface.OnClickListener() {
								public void onClick(DialogInterface dialog,
										int id) {
									SOSActivity.this
											.dismissDialog(EXCEPTION_DIALOG);
								}
							});
			return b.create();

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
		case LOADING_MULTIPLE_DIALOG:
			loadingMultiple.setProgress(0);
			loadingMultiple.setMax(multipleProgressCount);
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

		private int counter;

		private int mode;
		private static final int SINGLE = 1;
		private static final int MULTIPLE = 2;

		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case SOSManager.START:

				mode = msg.arg1 <= 1 ? SINGLE : MULTIPLE;

				switch (mode) {
				case SINGLE:
					SOSActivity.this.showDialog(LOADING_SINGLE_DIALOG);
					break;
				case MULTIPLE:
					counter = 0;
					multipleProgressCount = msg.arg1;
					SOSActivity.this.showDialog(LOADING_MULTIPLE_DIALOG);
					break;
				}

				counter = 0;
				db.close();
				return;
			case SOSManager.NEXTELEMENT:
				counter++;
				SOSActivity.this.loadingMultiple.setProgress(counter);
				return;
			case SOSManager.SUCCESS:
				switch (mode) {
				case SINGLE:
					SOSActivity.this.dismissDialog(LOADING_SINGLE_DIALOG);
					break;
				case MULTIPLE:
					SOSActivity.this.dismissDialog(LOADING_MULTIPLE_DIALOG);
					break;
				}
				db.open();
				SOSActivity.this.generateView();
				return;
			}
			switch (mode) {
			case SINGLE:
				SOSActivity.this.dismissDialog(LOADING_SINGLE_DIALOG);
				break;
			case MULTIPLE:
				SOSActivity.this.dismissDialog(LOADING_MULTIPLE_DIALOG);
				break;
			}
			db.open();
			switch (msg.what) {
			case SOSManager.SAXEX:
				EXCEPTION_DIALOG_MESSAGE = R.string.saxex;
				break;
			case SOSManager.IOEX:
				EXCEPTION_DIALOG_MESSAGE = R.string.ioexc;
				break;
			case SOSManager.PARSCONFIGEX:
				EXCEPTION_DIALOG_MESSAGE = R.string.parsconfigex;
				break;
			case SOSManager.MFURLEX:
				EXCEPTION_DIALOG_MESSAGE = R.string.mlfurlex;
				break;
			case SOSManager.CONNECTEX:
				EXCEPTION_DIALOG_MESSAGE = R.string.connectex;
				break;
			case SOSManager.UNKNOWNHEX:
				EXCEPTION_DIALOG_MESSAGE = R.string.unknownhex;
				break;
			}
			SOSActivity.this.showDialog(EXCEPTION_DIALOG);
		}
	}

	private class MySOSViewListener implements SOSViewListener {

		@Override
		public void onColorChanged(SOSView view, int newColor) {
			db.setColor(newColor, view.getSosID());
		}

		@Override
		public void onDelete(SOSView view) {
			db.deleteSOS(view.getSosID());
			views.remove(view.getSosID());
			root.removeView(view);
		}

		@Override
		public void onFeatureClicked(SOSView view) {
			final int sosID = view.getSosID();
			final int selectedOffering = db.getSelectedOffering(sosID);
			final int[] featureIDs = db.getFeatureIDs(selectedOffering);
			final String[] features = db.getFeatures(selectedOffering);

			AlertDialog.Builder b = new AlertDialog.Builder(SOSActivity.this);
			b.setTitle(R.string.choose_feature);
			b.setItems(features, new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int feature) {

					dialog.dismiss();

					Intent intent = new Intent(SOSActivity.this,
							MeasurementActivity.class);
					intent.putExtra(MeasurementActivity.PropertyID, db
							.getSelectedProperty(selectedOffering));
					intent.putExtra(MeasurementActivity.FeatureID,
							featureIDs[feature]);
					SOSActivity.this.startActivityForResult(intent,
							MeasurementActivity.SHOW_Measurement_INFO);

				}
			});
			Dialog d = b.create();
			d.show();

		}

		@Override
		public void onOfferingClicked(SOSView view) {
			final int sosID = view.getSosID();
			final int[] offeringIDs = db.getOfferingIDs(sosID);
			final String[] offeringNames = db.getOfferingNames(sosID);
			AlertDialog.Builder b = new AlertDialog.Builder(SOSActivity.this);
			b.setTitle(R.string.choose_offering);
			b.setItems(offeringNames, new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int offering) {
					dialog.dismiss();
					db.setSelectedOffering(offeringIDs[offering], sosID);
					views.get(sosID).setOffering(offeringNames[offering]);
				}
			});
			Dialog d = b.create();
			d.show();
		}

		@Override
		public void onPropertyClicked(SOSView view) {
			final int sosID = view.getSosID();
			final int offeringID = db.getSelectedOffering(sosID);
			// Log.d(DT, sosID + "|" + offeringID);
			final int[] propertyIDs = db.getPropertyIDs(offeringID);
			final String[] properties = db.getProperties(offeringID);

			AlertDialog.Builder b = new AlertDialog.Builder(SOSActivity.this);
			b.setTitle(R.string.choose_property);
			b.setItems(properties, new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int property) {

					dialog.dismiss();
					db.setSelectedProperty(propertyIDs[property], offeringID);
					views.get(sosID).setProperty(properties[property]);
					views
							.get(sosID)
							.setMemberCount(
									db
											.getNotEmptyMeasurementCount(propertyIDs[property])
											+ "/"
											+ db
													.getAllMeasurementCount(propertyIDs[property]));
				}
			});
			Dialog d = b.create();
			d.show();
		}

		@Override
		public void onUpdateAll(final SOSView view) {

			new Thread() {
				public void run() {
					manage.updateAllCurrentMeasurements(view.getSosID());
				}
			}.start();

		}

		@Override
		public void onUpdateAvailable(final SOSView view) {
			new Thread() {
				public void run() {
					manage.updateAvailableCurrentMeasurements(view.getSosID());
				}
			}.start();

		}

		@Override
		public void onVisibleChanged(SOSView view, boolean visible) {
			db.setVisibility(visible, view.getSosID());

		}

	}
}
