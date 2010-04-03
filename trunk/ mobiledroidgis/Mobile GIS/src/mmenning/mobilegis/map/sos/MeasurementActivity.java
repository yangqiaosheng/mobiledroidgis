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

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import mmenning.mobilegis.Preferences;
import mmenning.mobilegis.R;
import mmenning.mobilegis.util.ChartView;
import mmenning.mobilegis.util.TimePeriodPickerDialog;
import mmenning.mobilegis.util.TimePeriodPickerDialog.OnTimePeriodChangedListener;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.preference.PreferenceManager;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;

/**
 * Activity to display all Data referring to a Measurement in a Chart.
 * 
 * @author Mathias Menninghaus
 * @version 30.11.2009
 * 
 */
public class MeasurementActivity extends Activity {

	private static final String DT = "MeasurementActivity";

	/*
	 * request
	 */
	/**
	 * Standard Request for MesurementActivity to show a chart displaying the
	 * values referred to a database measurement id.
	 */
	public static final int SHOW_Measurement_INFO = 7081;
	/*
	 * response
	 */
	/**
	 * Response from MeasurementActivity to focus the Measurement identified by
	 * the database feature and property id.
	 */
	public static final int SHOW_Measurement_INMAP = 7082;

	/*
	 * intent extra identifiers
	 */
	/**
	 * Database id of the feature referring to the measurement which should be
	 * displayed by a SHOW_Measurement_INMAP request
	 */
	public static final String FeatureID = "mmenning.mobilegis.map.sos/FeatureID";
	/**
	 * Database id of the property referring to the measurement which should be
	 * displayed by a SHOW_Measurement_INMAP request
	 */
	public static final String PropertyID = "mmenning.mobilegis.map.sos/PropertyID";

	/**
	 * ID of the Measurement to be focused by SHOW_Measurement_INMAP response
	 */
	public static final String MeasurementID = "mmenning.mobilegis.map.sos/MeasurementID";

	/**
	 * LatitudeE6 to be focused by a SHOW_Measurement_INMAP response
	 */
	public static final String LatitudeE6 = "mmenning.mobilegis.map.sos/LatitudeE6";
	/**
	 * LongitudeE6 to be focused by a SHOW_Measurement_INMAP response
	 */
	public static final String LongitudeE6 = "mmenning.mobilegis.map.sos/LongitudeE6";

	/*
	 * menu items
	 */
	private static final int SHOWINMAP = 0;
	private static final int TIME_PERIOD = 1;
	private static final int SYNC = 2;
	private static final int PREFERENCES = 3;
	/*
	 * dialog ids
	 */
	private static final int TIME_PERIOD_DIALOG = 0;
	private static final int LOADING_DIALOG = 1;
	private static final int EXCEPTION_DIALOG = 2;
	private static final int POS_WARNING_DIALOG = 3;

	/*
	 * holds the current dialog message if an exception occurs while loading new
	 * data
	 */
	private static final int NO_EXCEPTION_TO_SHOW = -1;
	private int EXCEPTION_DIALOG_MESSAGE = NO_EXCEPTION_TO_SHOW;

	private SOSDB db;

	private ChartView chart;

	private MeasurementData displayedMeasurement;

	private static final int FAILMODE = 0;
	private static final int OKMODE = 1;

	private int mode;

	private int propertyID;
	private int featureID;

	private static final DateFormat DATEFORMAT = new SimpleDateFormat(
			"dd.MM-HH");

	private Date startTime;

	private Date endTime;

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		menu.add(0, SHOWINMAP, 0, R.string.showinmap).setIcon(
				R.drawable.menu_map);
		menu.add(0, SYNC, 0, R.string.synchronize).setIcon(
				R.drawable.menu_refresh);
		menu.add(0, TIME_PERIOD, 0, R.string.period).setIcon(
				R.drawable.menu_time);
		menu.add(0, PREFERENCES, 0, R.string.preferences).setIcon(
				R.drawable.menu_preferences);

		return super.onCreateOptionsMenu(menu);
	}

	@Override
	public boolean onMenuItemSelected(int featureId, MenuItem item) {
		switch (item.getItemId()) {
		case SHOWINMAP:
			if (mode != FAILMODE) {
				if (displayedMeasurement.latE6 == 0
						&& displayedMeasurement.lonE6 == 0) {
					showDialog(POS_WARNING_DIALOG);
					return true;
				}
				/*
				 * Go Strait back to the MainMap which called this activity.
				 */
				Intent data = new Intent();
				data.putExtra(LatitudeE6, displayedMeasurement.latE6);
				data.putExtra(LongitudeE6, displayedMeasurement.lonE6);
				data.putExtra(MeasurementID, displayedMeasurement.id);
				this.setResult(SHOW_Measurement_INMAP, data);
				MeasurementActivity.this.finish();

				return true;
			}
			break;
		case SYNC:
			/*
			 * update data for the displayed measurement and redraw the chart.
			 */
			if (mode != FAILMODE) {
				final SOSManager m = new SOSManager(this, new LoadingHandler());

				final int featureID = displayedMeasurement.featureID;
				final int propertyID = displayedMeasurement.propertyID;

				final int offeringID = db.getReferredOffering(propertyID,
						featureID);
				new Thread() {
					public void run() {
						m.syncMeasurement(offeringID, featureID, propertyID,
								startTime, endTime);
					}
				}.start();

				return true;
			}
			break;
		case TIME_PERIOD:
			if (mode != FAILMODE) {
				this.showDialog(TIME_PERIOD_DIALOG);
				return true;
			}
			break;
		case PREFERENCES:
			Intent i = new Intent(this, Preferences.class);
			this.startActivity(i);
			return true;
		}

		return super.onMenuItemSelected(featureId, item);
	}

	private void setFailMode() {
		mode = FAILMODE;

		this.setContentView(R.layout.measurementactivityfail);

		Button b = (Button) this
				.findViewById(R.id.measurementactivityfail_load);
		b.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				final SOSManager m = new SOSManager(MeasurementActivity.this,
						new LoadingHandler());
				final int offeringID = db.getReferredOffering(propertyID,
						featureID);
				new Thread() {
					public void run() {
						m.updateMeasurement(offeringID, featureID, propertyID,
								startTime, endTime);
					}
				}.start();
			}
		});

	}

	private void setOkMode() {
		mode = OKMODE;

		this.setTitle(db.getProperty(displayedMeasurement.propertyID) + " - "
				+ db.getFeature(displayedMeasurement.featureID));

		this.setContentView(R.layout.measurementactivityview);

		chart = (ChartView) this.findViewById(R.id.measurement_chart);

		setView();
	}

	private void setView() {

		TimeValuePairs values = db.getMeasurementValues(
				displayedMeasurement.id, startTime, endTime);

		float max = Float.MIN_VALUE;
		for (float f : values.values) {
			max = max < f ? f : max;
		}
		int digits;
		for (digits = 0; max > 1; max = max / 10, digits++)
			;
		chart.setyLabelLength(digits < 4 ? 4 : digits);
		chart.setMode(ChartView.LINE);
		chart.setData(values.times, values.values, DATEFORMAT);
		chart.setyLabel(db.getProperty(displayedMeasurement.propertyID) + " ["
				+ displayedMeasurement.unit + "]");
		chart.invalidate();
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		db = new SOSDB(this);
		db.open();

		propertyID = -1;
		featureID = -1;

		Bundle extras = this.getIntent().getExtras();
		if (extras != null) {
			if (extras.containsKey(PropertyID)) {
				propertyID = (extras.getInt(PropertyID));
			}
			if (extras.containsKey(FeatureID)) {
				featureID = (extras.getInt(FeatureID));
			}
		}

		/*
		 * set up initial request range -
		 */
		SharedPreferences sharedPreferences = PreferenceManager
				.getDefaultSharedPreferences(this);
		final String rangeString = this.getString(R.string.requestperiod);
		long storeRange = Long.parseLong(sharedPreferences.getString(
				rangeString, "" + SOSUtils.defaultDataRange));
		endTime = new Date(System.currentTimeMillis());
		startTime = new Date(endTime.getTime() - storeRange);

		displayedMeasurement = db.getMeasurementData(propertyID, featureID);

		if (displayedMeasurement == null) {
			setFailMode();

		} else {
			setOkMode();
		}
		db.close();

		/*
		 * by default everything ok
		 */
		this.setResult(RESULT_OK);

	}

	@Override
	protected Dialog onCreateDialog(int id) {
		ProgressDialog progressDialog;
		AlertDialog.Builder builder;
		switch (id) {
		case LOADING_DIALOG:
			progressDialog = new ProgressDialog(this);
			progressDialog.setMessage(this.getString(R.string.loading__));
			progressDialog.setCancelable(false);
			return progressDialog;
		case EXCEPTION_DIALOG:
			builder = new AlertDialog.Builder(this);
			builder.setTitle(R.string.loadingfailure);
			builder.setMessage(this.EXCEPTION_DIALOG_MESSAGE).setCancelable(
					false).setPositiveButton(this.getString(R.string.confirm),
					new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog, int id) {
							MeasurementActivity.this
									.dismissDialog(EXCEPTION_DIALOG);
						}
					});

			AlertDialog exceptionDialog = builder.create();
			return exceptionDialog;
		case TIME_PERIOD_DIALOG:
			Dialog d = new TimePeriodPickerDialog(this, startTime, endTime,
					new OnTimePeriodChangedListener() {
						@Override
						public void onTimePeriodChanged(Dialog view,
								Date start, Date end) {
							startTime = start;
							endTime = end;
							setView();
						}

					});
			return d;
		case POS_WARNING_DIALOG:
			builder = new AlertDialog.Builder(this);
			builder.setTitle(R.string.warning);
			builder.setMessage(R.string.invalid_pos_warning);
			builder.setCancelable(false);
			builder.setPositiveButton(this.getString(R.string.confirm),
					new DialogInterface.OnClickListener() {

						@Override
						public void onClick(DialogInterface dialog, int which) {
							MeasurementActivity.this
									.dismissDialog(MeasurementActivity.POS_WARNING_DIALOG);

						}

					});
			return builder.create();
		}
		return super.onCreateDialog(id);
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

	private class LoadingHandler extends Handler {

		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case SOSManager.START:
				MeasurementActivity.this.showDialog(LOADING_DIALOG);
				db.close();
				return;
			case SOSManager.SUCCESS:
				MeasurementActivity.this.dismissDialog(LOADING_DIALOG);
				db.open();
				switch (MeasurementActivity.this.mode) {
				case FAILMODE:
					displayedMeasurement = db.getMeasurementData(propertyID,
							featureID);
					if (displayedMeasurement == null) {
						setFailMode();
					} else {
						setOkMode();
					}
					break;
				case OKMODE:
					displayedMeasurement = db.getMeasurementData(propertyID,
							featureID);
					MeasurementActivity.this.setView();
					break;
				default:
					throw new IllegalStateException("invalid mode");
				}
				return;
			}
			MeasurementActivity.this.dismissDialog(LOADING_DIALOG);
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
			MeasurementActivity.this.showDialog(EXCEPTION_DIALOG);
		}
	}

}
