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
package mmenning.mobilegis.map.sos;

import java.util.Date;
import java.util.Iterator;
import java.util.LinkedList;

import mmenning.mobilegis.database.SQLiteOnSDCard;
import mmenning.mobilegis.map.sos.ParsedSOSCapabilities.ParsedObservationOffering;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

/**
 * Database Connector to manage and manipulate Storage of Sensor Observation
 * Service Data. </br> By adding new values: </br> - SensorObservationServices
 * are identified by their GetCapabilitiesURL </br> - ObservationOfferings are
 * identified by their offering id (string in the xmls) and their sos</br> -
 * Properties are identified by their value and the offering</br> - Features are
 * identified by their value and their offering </br> Measurements are the
 * connection of one Feature and one Property in a Offering. So the
 * GetObservationData should only refer to one Feature and one Property! </br>
 * Mesurements consist of Measurement (Meta-) Data and the values stored as
 * TimeValuePairs. A Measurement represents the result of a single
 * GetObservation Request. </br> To manage quick and cheap access all this Data
 * is identified by over the whole database definite ids.
 * 
 * @author Mathias Menninghaus
 * @version 11.11.2009
 * 
 */
public class SOSDB {

	private static final String DT = "SOSDB";

	private static final String DATABASE_NAME = "SOSData";
	private static final int DATABASE_VERSION = 1;

	private static final int TRUE = 1;
	private static final int FALSE = 0;

	private static final String NaN = "2143289344";

	/*
	 * Tables
	 */
	private static final String SOS_TABLE = "SOS";
	private static final String Offering_TABLE = "Offering";
	private static final String Property_TABLE = "Property";
	private static final String Feature_TABLE = "Feature";
	private static final String Measures_TABLE = "Measures";
	private static final String Measure_TABLE = "Measure";

	/*
	 * Fields
	 */
	private static final String ID = "_id";

	private static final String SOS_GetObservation = "GetObservation";
	private static final String SOS_GetCapabilities = "GetCapabilities";
	private static final String SOS_color = "color";
	private static final String SOS_visible = "visible";
	private static final String SOS_selectedOffering = "selectedOffering";
	private static final String SOS_title = "title";
	private static final String SOS_description = "description";

	private static final String Offering_name = "name";
	private static final String Offering_sos = "sos";
	private static final String Offering_offering = "offering";
	private static final String Offering_selectedProperty = "selectedProperty";

	private static final String Property_property = "property";
	private static final String Property_offering = "offering";

	private static final String Feature_feature = "feature";
	private static final String Feature_offering = "offering";

	private static final String Measures_property = "property";
	private static final String Measures_feature = "feature";
	private static final String Measures_latE6 = "latE6";
	private static final String Measures_lonE6 = "lonE6";
	private static final String Measures_unit = "unit";

	private static final String Measure_measurement = "measurement";
	private static final String Measure_time = "time";
	private static final String Measure_value = "value";

	/*
	 * Create Table Statements
	 */
	private static final String CREATE_SOS = "CREATE TABLE " + SOS_TABLE + " ("
			+ ID + " INTEGER PRIMARY KEY, " + SOS_GetObservation + " TEXT, "
			+ SOS_GetCapabilities + " TEXT, " + SOS_color + " INTEGER, "
			+ SOS_visible + " INTEGER, " + SOS_selectedOffering + " INTEGER, "
			+ SOS_title + " TEXT, " + SOS_description + " TEXT)";

	private static final String CREATE_Offering = "CREATE TABLE "
			+ Offering_TABLE + " (" + ID + " INTEGER PRIMARY KEY, "
			+ Offering_name + " TEXT, " + Offering_sos + " INTEGER, "
			+ Offering_offering + " TEXT, " + Offering_selectedProperty
			+ " TEXT" + ")";

	private static final String CREATE_Property = "CREATE TABLE "
			+ Property_TABLE + " (" + ID + " INTEGER PRIMARY KEY, "
			+ Property_property + " TEXT, " + Property_offering + " INTEGER"
			+ ")";

	private static final String CREATE_Feature = "CREATE TABLE "
			+ Feature_TABLE + " (" + ID + " INTEGER PRIMARY KEY, "
			+ Feature_feature + " TEXT, " + Feature_offering + " INTEGER" + ")";

	private static final String CREATE_Measures = "CREATE TABLE "
			+ Measures_TABLE + " (" + ID + " INTEGER PRIMARY KEY, "
			+ Measures_property + " INTEGER, " + Measures_feature
			+ " INTEGER, " + Measures_latE6 + " INTEGER, " + Measures_lonE6
			+ " INTEGER, " + Measures_unit + " TEXT)";

	private static final String CREATE_Measure = "CREATE TABLE "
			+ Measure_TABLE + " (" + Measure_measurement + " INTEGER, "
			+ Measure_time + " INTEGER, " + Measure_value
			+ " REAL, PRIMARY KEY (" + Measure_measurement + ", "
			+ Measure_time + ")) ";

	/**
	 * 
	 * Helper Class to manage connection and first-time generation of the
	 * database
	 */
	private DatabaseHelper DBHelper;

	/**
	 * the SQLite Database
	 */
	private SQLiteDatabase db;

	/**
	 * Context (e.g. Android-Activity) to which this model-instance is related
	 */
	private final Context context;

	private static final String Feature_Order = Feature_feature + " ASC";

	private static final String Offering_Order = Offering_offering + " ASC";

	private static final String Property_Order = Property_property + " ASC";

	private static final String Measure_Order = Measure_time + " DESC";

	private static final String SOS_Order = SOS_title + " ASC";

	private static final String insertOrIgnoreMeasureBase = "INSERT OR IGNORE INTO "
			+ Measure_TABLE
			+ "("
			+ Measure_time
			+ ", "
			+ Measure_value
			+ ", "
			+ Measure_measurement + ") VALUES";

	private static final String countAllQuery = "SELECT count(" + ID
			+ ") FROM " + Measures_TABLE + " WHERE " + Measures_property + "=?";

	private static final String countAvailableQuery = "SELECT count(" + ID
			+ ") FROM " + Measures_TABLE + " WHERE " + Measures_property
			+ "=? AND " + Measures_latE6 + "<>0 AND " + Measures_lonE6 + "<>0";

	private static final String queryYoungestMeasurement = "SELECT max("
			+ Measure_TABLE + "." + Measure_time + ") FROM " + Measure_TABLE
			+ ", " + Measures_TABLE + " WHERE " + Measure_TABLE + "."
			+ Measure_measurement + "=" + Measures_TABLE + "." + ID + " AND "
			+ Measures_TABLE + "." + Measures_property + "=? AND "
			+ Measures_TABLE + "." + Measures_feature + "=?";

	/**
	 * Sets context of this class (e.g. Android-Activity)
	 * 
	 * @param ctx
	 *            Context on which the database works
	 */
	public SOSDB(Context ctx) {
		this.context = ctx;
		DBHelper = new DatabaseHelper(context);
	}

	/**
	 * Add ParsedObservationData to the Database. Already contained measurement
	 * Data will only be updated, if there is an empty entry.
	 */
	public void addMeasurementData(ParsedObservationData data, int featureID,
			int propertyID) {

		ContentValues values = new ContentValues();
		values.put(Measures_latE6, data.LatE6);
		values.put(Measures_lonE6, data.LonE6);
		values.put(Measures_feature, featureID);
		values.put(Measures_property, propertyID);
		values.put(Measures_unit, data.unit);

		int measurementID = this.getMeasurementID(propertyID, featureID);
		if (measurementID == -1) {
			measurementID = getNextID(Measures_TABLE);
			db.insert(Measures_TABLE, null, values);
		} else {
			db.update(Measures_TABLE, values, ID + "=" + measurementID, null);
		}
		insertOrIgnoreMeasure(data.times, data.values, measurementID);
	}

	/**
	 * 
	 * Add a ParsedSOSCapabilities to the Database. If the database already
	 * contains the given DataSet it will be updated. All containing Offerings
	 * will be inserted or updated.
	 * 
	 * @param data
	 *            ParsedSOSCapabilities to add to the database
	 */
	public void addSOS(ParsedSOSCapabilities data) {

		ContentValues values = getContentValues(data);

		Cursor c = db.query(SOS_TABLE, new String[] { ID }, SOS_GetCapabilities
				+ "='" + data.getCapabilitiesGet + "'", null, null, null, null);

		int sosID;

		boolean update;
		if (c.moveToFirst()) {
			update = true;
			sosID = c.getInt(0);
		} else {
			update = false;
			sosID = getNextID(SOS_TABLE);
		}

		values.put(ID, sosID);
		int select = -1;
		for (ParsedObservationOffering o : data.offerings) {
			select = addOffering(o, sosID);
		}

		values.put(SOS_selectedOffering, select);

		c.close();
		if (update) {
			db.update(SOS_TABLE, values, ID + "=" + sosID, null);
		} else {
			db.insert(SOS_TABLE, null, values);
		}
	}

	/**
	 * Deletes all Measurements older than startTime and younger than endTime
	 * for all Measurements in the database.
	 * 
	 * @param startTime
	 * @param endTime
	 */
	public void clipAllMeasurementValues(Date startTime, Date endTime) {
		db.delete(Measure_TABLE, Measure_time + ">" + endTime.getTime()
				+ " OR " + Measure_time + "<" + startTime.getTime(), null);
	}

	/**
	 * Deletes all Measurements older than startTime and younger than endTime
	 * for a Measurement
	 * 
	 * @param startTime
	 * @param endTime
	 * @param measurementID
	 *            database id of the clipped Measurement
	 */
	public void clipMeasurementValues(Date startTime, Date endTime,
			int measurementID) {
		db.delete(Measure_TABLE, ID + "=" + measurementID + " AND ("
				+ Measure_time + ">" + endTime.getTime() + " OR "
				+ Measure_time + "<" + startTime.getTime() + ")", null);
	}

	/**
	 * Close Database
	 */
	public void close() {
		db.close();
		DBHelper.close();
	}

	/**
	 * Delete a SOS from the database
	 * 
	 * @param sosID
	 *            database id of the sos
	 */
	public void deleteSOS(int sosID) {
		Cursor c = db.query(Offering_TABLE, new String[] { ID }, Offering_sos
				+ "=" + sosID, null, null, null, null);
		if (c.moveToFirst()) {
			do {
				deleteOffering(c.getInt(0));
			} while (c.moveToNext());
		}
		c.close();

		db.delete(SOS_TABLE, ID + "=" + sosID, null);
	}

	/**
	 * Get the amount of all Measurements for a property
	 * 
	 * @param propertyID
	 *            database id for the property
	 * @return the amount, or -1
	 */
	public int getAllMeasurementCount(int propertyID) {
		Cursor c = db.rawQuery(countAllQuery, new String[] { "" + propertyID });
		int ret = -1;
		if (c.moveToFirst()) {
			ret = c.getInt(0);
		}
		c.close();
		return ret;
	}

	/**
	 * Get all SensorObservationService Datasets stored in this database.
	 * 
	 * @return SOSData of all SOS stored in the database, sorted ascending by
	 *         the title, maybe empty.
	 */
	public SOSData[] getAllSOS() {
		Cursor c = db.query(SOS_TABLE, new String[] { ID, SOS_GetObservation,
				SOS_GetCapabilities, SOS_color, SOS_visible,
				SOS_selectedOffering, SOS_title, SOS_description }, null, null,
				null, null, SOS_Order);

		SOSData[] ret = new SOSData[c.getCount()];
		if (c.moveToFirst()) {
			for (int i = 0; i < ret.length; i++) {
				ret[i] = new SOSData();
				ret[i].id = c.getInt(0);
				ret[i].getObservationPost = c.getString(1);
				ret[i].getCapabilities = c.getString(2);
				ret[i].color = c.getInt(3);
				ret[i].visible = c.getInt(4) == TRUE;
				ret[i].selectedOffering = c.getInt(5);
				ret[i].title = c.getString(6);
				ret[i].description = c.getString(7);
				c.moveToNext();
			}
		}
		c.close();
		return ret;

	}

	/**
	 * Get TimeValue - Pairs in a specific time range
	 * 
	 * @param startTime
	 *            oldest measurement time value
	 * @param endTime
	 *            youngest measurement time value
	 * @param measurementID
	 *            database id of the queried measurement
	 * @return TimeValuePairs int the range from startTime to endTime, sorted by
	 *         the time value descending, maybe empty.
	 */
	public TimeValuePairs getClippedMeasurementValues(Date startTime,
			Date endTime, int measurementID) {

		TimeValuePairs ret = new TimeValuePairs();

		Cursor c = db.query(Measure_TABLE, new String[] { Measure_time,
				Measure_value }, ID + "=" + measurementID + " AND "
				+ Measure_time + "<" + endTime.getTime() + " AND "
				+ Measure_time + ">" + startTime.getTime(), null, null, null,
				Measure_Order);

		ret.times = new long[c.getCount()];
		ret.values = new float[c.getCount()];
		if (c.moveToFirst()) {
			for (int i = 0; i < ret.times.length; i++) {
				ret.times[i] = c.getLong(0);
				ret.values[i] = c.getFloat(1);
				c.moveToNext();
			}
		}
		c.close();

		return ret;
	}

	/**
	 * Get the color of a SensorObservationService.
	 * 
	 * @param sosID
	 *            database id of the sos
	 * @return the color value, or -1.
	 */
	public int getColor(int sosID) {
		Cursor c = db.query(SOS_TABLE, new String[] { SOS_color }, ID + "="
				+ sosID, null, null, null, null);
		int ret = -1;
		if (c.moveToFirst()) {
			ret = c.getInt(0);
		}
		c.close();
		return ret;

	}

	/**
	 * Get a Feature
	 * 
	 * @param featureID
	 *            database id of the feature
	 * @return the string for this feature or null
	 */
	public String getFeature(int featureID) {
		Cursor c = db.query(Feature_TABLE, new String[] { Feature_feature }, ID
				+ "=" + featureID, null, null, null, null);

		String ret = null;
		if (c.moveToFirst()) {
			ret = c.getString(0);
		}
		c.close();
		return ret;
	}

	/**
	 * Get all Feature database ids of an Offering
	 * 
	 * @param offeringID
	 *            database id for the offering
	 * @return an int[] of all features related to the offering, sorted
	 *         ascending by the feature identifier. maybe empty
	 */
	public int[] getFeatureIDs(int offeringID) {
		Cursor c = db.query(Feature_TABLE, new String[] { ID },
				Feature_offering + "=" + offeringID, null, null, null,
				Feature_Order);

		int[] ret = new int[c.getCount()];
		if (c.moveToFirst()) {
			for (int i = 0; i < ret.length; i++) {
				ret[i] = c.getInt(0);
				c.moveToNext();
			}
		}
		c.close();
		return ret;
	}

	/**
	 * Get all Feature of an Offering
	 * 
	 * @param offeringID
	 *            database id for the offering
	 * @return a String[] of all features related to the offering, sorted
	 *         ascending by the feature identifier. maybe empty
	 */
	public String[] getFeatures(int offeringID) {
		Cursor c = db.query(Feature_TABLE, new String[] { Feature_feature },
				Feature_offering + "=" + offeringID, null, null, null,
				Feature_Order);

		String[] ret = new String[c.getCount()];
		if (c.moveToFirst()) {
			for (int i = 0; i < ret.length; i++) {
				ret[i] = c.getString(0);
				c.moveToNext();
			}
		}
		c.close();
		return ret;
	}

	/**
	 * Get a specific MeasurementData
	 * 
	 * @param measurementID
	 *            database id for the measurement
	 * @return MeasurementData or null if there is no such Measurement
	 */
	public MeasurementData getMeasurementData(int measurementID) {
		Cursor c = db.query(Measures_TABLE, new String[] { ID, Measures_latE6,
				Measures_lonE6, Measures_unit, Measures_property,
				Measures_feature }, ID + "=" + measurementID, null, null, null,
				null);
		MeasurementData ret = null;
		if (c.moveToFirst()) {
			ret = new MeasurementData();
			ret.id = c.getInt(0);
			ret.latE6 = c.getInt(1);
			ret.lonE6 = c.getInt(2);
			ret.unit = c.getString(3);
			ret.propertyID = c.getInt(4);
			ret.featureID = c.getInt(5);
			c.moveToNext();
		}
		c.close();
		return ret;
	}

	/**
	 * Get a specific MeasurementData
	 * 
	 * @param propertyID
	 *            databaseID of the related Property
	 * @param featureID
	 *            databaseID of the related Feature
	 * @return MeasurementData or null if there is no such Measurement
	 */
	public MeasurementData getMeasurementData(int propertyID, int featureID) {
		Cursor c = db.query(Measures_TABLE, new String[] { ID, Measures_latE6,
				Measures_lonE6, Measures_unit, Measures_property,
				Measures_feature }, Measures_property + "=" + propertyID
				+ " AND " + Measures_feature + "=" + featureID, null, null,
				null, null);
		MeasurementData ret = null;
		if (c.moveToFirst()) {
			ret = new MeasurementData();
			ret.id = c.getInt(0);
			ret.latE6 = c.getInt(1);
			ret.lonE6 = c.getInt(2);
			ret.unit = c.getString(3);
			ret.propertyID = c.getInt(4);
			ret.featureID = c.getInt(5);
			c.moveToNext();
		}
		c.close();
		return ret;
	}

	/**
	 * Get all MeasurementData for a property
	 * 
	 * @param propertyID
	 *            database ID of the selected Property
	 * @return all MeasuerementData in an Array, maybe empty
	 */
	public MeasurementData[] getMeasurementDataArray(int propertyID) {
		Cursor c = db.query(Measures_TABLE, new String[] { ID, Measures_latE6,
				Measures_lonE6, Measures_unit, Measures_property,
				Measures_feature }, Measures_property + "=" + propertyID, null,
				null, null, null);
		MeasurementData[] ret = new MeasurementData[c.getCount()];
		if (c.moveToFirst()) {
			for (int i = 0; i < ret.length; i++) {
				ret[i] = new MeasurementData();
				ret[i].id = c.getInt(0);
				ret[i].latE6 = c.getInt(1);
				ret[i].lonE6 = c.getInt(2);
				ret[i].unit = c.getString(3);
				ret[i].propertyID = c.getInt(4);
				ret[i].featureID = c.getInt(5);
				c.moveToNext();
			}
		}
		c.close();
		return ret;
	}

	/**
	 * Get all TimeValue - Pairs for a Measurement
	 * 
	 * @param measurementID
	 *            database id of the queried measurement
	 * @return TimeValuePairs sorted by the time value descending, maybe empty.
	 */
	public TimeValuePairs getMeasurementValues(int measurementID) {

		TimeValuePairs ret = new TimeValuePairs();

		Cursor c = db.query(Measure_TABLE, new String[] { Measure_time,
				Measure_value }, Measure_measurement + "=" + measurementID,
				null, null, null, Measure_Order);

		ret.times = new long[c.getCount()];
		ret.values = new float[c.getCount()];
		if (c.moveToFirst()) {
			for (int i = 0; i < ret.times.length; i++) {
				ret.times[i] = c.getLong(0);
				ret.values[i] = c.getFloat(1);
				c.moveToNext();
			}
		}
		c.close();

		return ret;
	}

	/**
	 * Get all Time-Value-Pairs in a specific range.
	 * 
	 * @param measurementID
	 *            database id for the requested measurement
	 * @param startTime
	 *            all entries will be younger than this
	 * @param endTime
	 *            all entries will be older than this
	 * @return TimeValuePairs in the requested range for the measurement, maybe
	 *         empty
	 */
	public TimeValuePairs getMeasurementValues(int measurementID,
			Date startTime, Date endTime) {

		TimeValuePairs ret = new TimeValuePairs();

		Cursor c = db.query(Measure_TABLE, new String[] { Measure_time,
				Measure_value }, Measure_measurement + "=" + measurementID
				+ " AND " + Measure_time + ">" + startTime.getTime() + " AND "
				+ Measure_time + "<" + endTime.getTime(), null, null, null,
				Measure_Order);

		ret.times = new long[c.getCount()];
		ret.values = new float[c.getCount()];
		if (c.moveToFirst()) {
			for (int i = 0; i < ret.times.length; i++) {
				ret.times[i] = c.getLong(0);
				ret.values[i] = c.getFloat(1);
				c.moveToNext();
			}
		}
		c.close();

		return ret;
	}

	/**
	 * Get the amount of all Measurements for a property which latitude and
	 * longitude are not 0.
	 * 
	 * @param propertyID
	 *            database id for the property
	 * @return the amount, or -1
	 */
	public int getNotEmptyMeasurementCount(int propertyID) {
		Cursor c = db.rawQuery(countAvailableQuery, new String[] { ""
				+ propertyID });
		int ret = -1;
		if (c.moveToFirst()) {
			ret = c.getInt(0);
		}
		c.close();
		return ret;
	}

	/**
	 * Get all MeasurementData for a property that lat and lon are not 0.
	 * 
	 * @param propertyID
	 *            database ID of the selected Property
	 * @return all MeasuerementData in an Array, maybe empty
	 */
	public MeasurementData[] getNotEmptyMeasurementDataArray(int propertyID) {
		Cursor c = db.query(Measures_TABLE, new String[] { ID, Measures_latE6,
				Measures_lonE6, Measures_unit, Measures_property,
				Measures_feature }, Measures_property + "=" + propertyID
				+ " AND " + Measures_lonE6 + "<>0 AND " + Measures_latE6 + "<>"
				+ 0, null, null, null, null);
		MeasurementData[] ret = new MeasurementData[c.getCount()];
		if (c.moveToFirst()) {
			for (int i = 0; i < ret.length; i++) {
				ret[i] = new MeasurementData();
				ret[i].id = c.getInt(0);
				ret[i].latE6 = c.getInt(1);
				ret[i].lonE6 = c.getInt(2);
				ret[i].unit = c.getString(3);
				ret[i].propertyID = c.getInt(4);
				ret[i].featureID = c.getInt(5);
				c.moveToNext();
			}
		}
		c.close();
		return ret;
	}

	/**
	 * Get a ObservationOffering dataset
	 * 
	 * @param offeringID
	 *            database id of the queried dataset
	 * @return the data set of the Offering or null
	 */
	public OfferingData getOffering(int offeringID) {
		Cursor c = db.query(Offering_TABLE, new String[] { ID, Offering_name,
				Offering_offering, Offering_selectedProperty, Offering_sos },
				ID + "=" + offeringID, null, null, null, null);
		OfferingData ret = null;
		if (c.moveToFirst()) {
			ret = new OfferingData();
			ret.id = c.getInt(0);
			ret.name = c.getString(1);
			ret.offering = c.getString(2);
			ret.selectedProperty = c.getInt(3);
			ret.sosID = c.getInt(4);
		}
		c.close();
		return ret;
	}

	/**
	 * Get all Offering identifiers for a SensorObservationService, by which
	 * they are identified in the xml requests and responses.
	 * 
	 * @param sosID
	 *            database id of the queried sos
	 * @return String[] of all ObservationOffering identifiers, sorted ascending
	 *         by the offering identifiers. maybe empty
	 */
	public String[] getOfferingIdentifiers(int sosID) {
		Cursor c = db.query(Offering_TABLE, new String[] { Offering_offering },
				Offering_sos + "=" + sosID, null, null, null, Offering_Order);

		String[] ret = new String[c.getCount()];

		if (c.moveToFirst()) {
			for (int i = 0; i < ret.length; i++) {
				ret[i] = c.getString(0);
				c.moveToNext();
			}
		}
		c.close();
		return ret;
	}

	/**
	 * Get all Offering id for a SensorObservationService, by which they are
	 * identified in the database
	 * 
	 * @param sosID
	 *            database id of the queried sos
	 * @return int[] of all ObservationOffering database ids, sorted ascending
	 *         by the offering identifiers. maybe empty
	 */
	public int[] getOfferingIDs(int sosID) {
		Cursor c = db.query(Offering_TABLE, new String[] { ID }, Offering_sos
				+ "=" + sosID, null, null, null, Offering_Order);

		int[] ret = new int[c.getCount()];

		if (c.moveToFirst()) {
			for (int i = 0; i < ret.length; i++) {
				ret[i] = c.getInt(0);
				c.moveToNext();
			}
		}
		c.close();
		return ret;
	}

	/**
	 * Get the Name of an Offering
	 * 
	 * @param offeringID
	 *            database id of the requested offering
	 * @return name of the requested offering
	 */
	public String getOfferingName(int offeringID) {
		Cursor c = db.query(Offering_TABLE, new String[] { Offering_name }, ID
				+ "=" + offeringID, null, null, null, null);

		String ret = null;

		if (c.moveToFirst()) {
			ret = c.getString(0);
		}
		c.close();
		return ret;
	}

	/**
	 * Get all Offering names for a SensorObservationService.
	 * 
	 * @param sosID
	 *            database id of the queried sos
	 * @return String[] of all ObservationOffering names, sorted ascending by
	 *         the offering identifiers. maybe empty
	 */
	public String[] getOfferingNames(int sosID) {
		Cursor c = db.query(Offering_TABLE, new String[] { Offering_name },
				Offering_sos + "=" + sosID, null, null, null, Offering_Order);

		String[] ret = new String[c.getCount()];

		if (c.moveToFirst()) {
			for (int i = 0; i < ret.length; i++) {
				ret[i] = c.getString(0);
				c.moveToNext();
			}
		}
		c.close();
		return ret;
	}

	/**
	 * Get all ObservationOffering datasets for a SensorObservationService
	 * 
	 * @param sosID
	 *            database id of the SensorObservationService
	 * @return OfferingData[], sorted ascending by the offering identifier,
	 *         maybe empty.
	 */
	public OfferingData[] getOfferings(int sosID) {
		Cursor c = db.query(Offering_TABLE, new String[] { ID, Offering_name,
				Offering_offering, Offering_selectedProperty, Offering_sos },
				Offering_sos + "=" + sosID, null, null, null, Offering_Order);
		OfferingData[] ret = new OfferingData[c.getCount()];
		if (c.moveToFirst()) {
			for (int i = 0; i < ret.length; i++) {
				ret[i] = new OfferingData();
				ret[i].id = c.getInt(0);
				ret[i].name = c.getString(1);
				ret[i].offering = c.getString(2);
				ret[i].selectedProperty = c.getInt(3);
				ret[i].sosID = c.getInt(4);
				c.moveToNext();
			}
		}
		c.close();
		return ret;
	}

	/**
	 * Get Properties related to a ObservationOffering
	 * 
	 * @param offeringID
	 *            database id of the queried offering
	 * @return String[] of properties, sorted ascending by property, maybe empty
	 */
	public String[] getProperties(int offeringID) {

		Cursor c = db.query(Property_TABLE, new String[] { Property_property },
				Property_offering + "=" + offeringID, null, null, null,
				Property_Order);

		String[] ret = new String[c.getCount()];

		if (c.moveToFirst()) {
			for (int i = 0; i < ret.length; i++) {
				ret[i] = c.getString(0);
				c.moveToNext();
			}
		}
		c.close();
		return ret;
	}

	/**
	 * Get the Property for a database id of the property.
	 * 
	 * @param propertyID
	 *            database id of the queried property
	 * @return the property or null.
	 */
	public String getProperty(int propertyID) {
		Cursor c = db.query(Property_TABLE, new String[] { Property_property },
				ID + "=" + propertyID, null, null, null, null);
		String ret = null;
		if (c.moveToFirst()) {
			ret = c.getString(0);
		}
		c.close();
		return ret;
	}

	/**
	 * Get all Property database ids related to a ObservationOffering
	 * 
	 * @param offeringID
	 *            database id of the queried offering
	 * @return int[] of database ids, sorted ascending by property, maybe empty
	 */
	public int[] getPropertyIDs(int offeringID) {

		Cursor c = db.query(Property_TABLE, new String[] { ID },
				Property_offering + "=" + offeringID, null, null, null,
				Property_Order);

		int[] ret = new int[c.getCount()];

		if (c.moveToFirst()) {
			for (int i = 0; i < ret.length; i++) {
				ret[i] = c.getInt(0);
				c.moveToNext();
			}
		}
		c.close();
		return ret;
	}

	/**
	 * Get the database id of the offering referring to a property or feature.
	 * 
	 * @param propertyID
	 *            database id of the property or -1 if you want to select by
	 *            feature
	 * @param featureID
	 *            database id of the feature or -1 if you want to select by
	 *            property
	 * @return database id of the offering or -1
	 */
	public int getReferredOffering(int propertyID, int featureID) {

		int ret = -1;
		Cursor c;
		if (propertyID != -1) {
			c = db.query(Property_TABLE, new String[] { Property_offering }, ID
					+ "=" + propertyID, null, null, null, null);
		} else {
			c = db.query(Feature_TABLE, new String[] { Feature_feature }, ID
					+ "=" + featureID, null, null, null, null);
		}
		if (c.moveToFirst()) {
			ret = c.getInt(0);
		}
		c.close();
		return ret;
	}

	/**
	 * Get the database id of the selected offering in a sos
	 * 
	 * @param sosID
	 *            database id of the sos
	 * @return database id of the selected offering or -1.
	 */
	public int getSelectedOffering(int sosID) {
		Cursor c = db.query(SOS_TABLE, new String[] { SOS_selectedOffering },
				ID + "=" + sosID, null, null, null, null);
		int ret = -1;
		if (c.moveToFirst()) {
			ret = c.getInt(0);
		}
		c.close();
		return ret;
	}

	/**
	 * Get the database id of the selected Property of an Offering.
	 * 
	 * @param offeringID
	 *            database id of the queried Offering
	 * @return the selected propertyID or -1
	 */
	public int getSelectedProperty(int offeringID) {
		Cursor c = db.query(Offering_TABLE,
				new String[] { Offering_selectedProperty }, ID + "="
						+ offeringID, null, null, null, null);
		int ret = -1;
		if (c.moveToFirst()) {
			ret = c.getInt(0);
		}
		c.close();
		return ret;

	}

	/**
	 * Get a SensorObservationService Dataset.
	 * 
	 * @param sosID
	 *            databaseID of the requested SensorObservationService
	 * @return SOSData for the given sosID or null.
	 */
	public SOSData getSOS(int sosID) {
		Cursor c = db.query(SOS_TABLE, new String[] { ID, SOS_GetObservation,
				SOS_GetCapabilities, SOS_color, SOS_visible,
				SOS_selectedOffering, SOS_title, SOS_description }, null, null,
				null, null, SOS_Order);

		SOSData ret = null;
		if (c.moveToFirst()) {

			ret = new SOSData();
			ret.id = c.getInt(0);
			ret.getObservationPost = c.getString(1);
			ret.getCapabilities = c.getString(2);
			ret.color = c.getInt(3);
			ret.visible = c.getInt(4) == TRUE;
			ret.selectedOffering = c.getInt(5);
			ret.title = c.getString(6);
			ret.description = c.getString(7);

		}
		c.close();
		return ret;

	}

	/**
	 * Get all visible sos database ids.
	 * 
	 * @return database ids if all visible sos. unsorted, maybe empty.
	 */
	public SOSData[] getVisibleSOS() {
		Cursor c = db.query(SOS_TABLE, new String[] { ID, SOS_GetObservation,
				SOS_GetCapabilities, SOS_color, SOS_visible,
				SOS_selectedOffering, SOS_title, SOS_description }, SOS_visible
				+ "=" + TRUE, null, null, null, SOS_Order);

		SOSData[] ret = new SOSData[c.getCount()];
		if (c.moveToFirst()) {
			for (int i = 0; i < ret.length; i++) {
				ret[i] = new SOSData();
				ret[i].id = c.getInt(0);
				ret[i].getObservationPost = c.getString(1);
				ret[i].getCapabilities = c.getString(2);
				ret[i].color = c.getInt(3);
				ret[i].visible = c.getInt(4) == TRUE;
				ret[i].selectedOffering = c.getInt(5);
				ret[i].title = c.getString(6);
				ret[i].description = c.getString(7);
				c.moveToNext();
			}
		}
		c.close();
		return ret;

	}

	/**
	 * Get youngest (greatest) time value of a Measurement
	 * 
	 * @param measurementID
	 *            database id of the queried Measurement
	 * @return the youngest (greatest) time value of a Measurement or null
	 */
	public Date getYoungestMeasurementTime(int measurementID) {
		Date ret = null;
		Cursor c = db.rawQuery("SELECT max(" + Measure_time + ") FROM "
				+ Measure_TABLE + " WHERE " + Measure_measurement + "="
				+ measurementID, null);
		if (c.moveToFirst()) {
			ret = new Date(c.getLong(0));
		}
		c.close();
		return ret;
	}

	/**
	 * Get the youngest (greatest time value) entry for a measurement.
	 * 
	 * @param propertyID
	 *            property database id for the measurement
	 * @param featureID
	 *            feature database id for the measurement
	 * @return the youngest entry as date, or null.
	 */
	public Date getYoungestMeasurementTime(int propertyID, int featureID) {
		Cursor c = db.rawQuery(queryYoungestMeasurement, new String[] {
				"" + propertyID, "" + featureID });
		Date ret = null;
		if (c.moveToFirst()) {
			ret = new Date(c.getLong(0));
		}
		c.close();
		return ret;
	}

	/**
	 * Get the youngest value of a Measurement
	 * 
	 * @param measurementID
	 *            database id of the queriedMeasurement
	 * @return the youngest (latest) value or NaN
	 */
	public float getYoungestMeasurementValue(int measurementID) {
		float ret = Float.NaN;
		Cursor c = db.rawQuery(
				"SELECT " + Measure_value + " FROM " + Measure_TABLE
						+ " WHERE " + Measure_measurement + "=" + measurementID
						+ " ORDER BY " + Measure_time + " DESC LIMIT 1", null);
		if (c.moveToFirst()) {
			ret = c.getFloat(0);
		}
		c.close();
		return ret;
	}

	/**
	 * Get the visibility of a sos
	 * 
	 * @param sosID
	 *            database id of the sos.
	 * @return the visibility. false also if the id does not exist.
	 */
	public boolean isVisible(int sosID) {
		Cursor c = db.query(SOS_TABLE, new String[] { SOS_visible }, ID + "="
				+ sosID, null, null, null, null);
		boolean ret = false;
		if (c.moveToFirst()) {
			ret = c.getInt(0) == TRUE;
		}
		c.close();
		return ret;
	}

	/**
	 * Connects to the database and returns this-Object If there already is an
	 * connection it will be closed an then a new one will be opened.
	 * 
	 * @return Connection to the database.
	 * @throws SQLException
	 *             if something goes wrong
	 */
	public SOSDB open() throws SQLException {
		db = DBHelper.getWritableDatabase();
		return this;
	}

	/**
	 * Connects to database in Read-Only. One should only access query methods!
	 * 
	 * @return
	 * @throws SQLExcpetion
	 */
	public SOSDB openReadOnly() throws SQLException {
		db = DBHelper.getReadableDatabase();
		return this;
	}

	/**
	 * Set the Color of a SensorObservationService
	 * 
	 * @param color
	 *            new color as int. NOTE: it will not be checked whether it is
	 *            valid or not
	 * @param sosID
	 *            database id of the soservice. NOTE: it is not checked whether
	 *            the id is valid!
	 */
	public void setColor(int color, int sosID) {
		ContentValues values = new ContentValues();
		values.put(SOS_color, color);
		db.update(SOS_TABLE, values, ID + "=" + sosID, null);
	}

	/**
	 * Set the selectedOffering for a SOS
	 * 
	 * @param offeringID
	 *            new selectedOffering. NOTE: it will not be checked whether
	 *            this id is valid
	 * @param sosID
	 *            databse id for the sos. NOTE: it is not checked whether the id
	 *            is valid!
	 */
	public void setSelectedOffering(int offeringID, int sosID) {
		ContentValues values = new ContentValues();
		values.put(SOS_selectedOffering, offeringID);
		db.update(SOS_TABLE, values, ID + "=" + sosID, null);
	}

	/**
	 * Set the selectedProperty for an Offering.
	 * 
	 * @param propertyID
	 *            new selectedProperty. NOTE: it will not be checked whether
	 *            this id is valid!
	 * @param offeringID
	 *            database id for the Offering. NOTE: it is not checked whether
	 *            the id is valid!
	 */
	public void setSelectedProperty(int propertyID, int offeringID) {
		ContentValues values = new ContentValues();
		values.put(Offering_selectedProperty, propertyID);
		db.update(Offering_TABLE, values, ID + "=" + offeringID, null);
	}

	/**
	 * Set the Visibility of a SensorObservationService
	 * 
	 * @param visible
	 *            new value
	 * @param sosID
	 *            database id of the soservice. NOTE: it is not checked whether
	 *            the id is valid!
	 */
	public void setVisibility(boolean visible, int sosID) {
		ContentValues values = new ContentValues();
		values.put(SOS_visible, visible ? TRUE : FALSE);
		db.update(SOS_TABLE, values, ID + "=" + sosID, null);
	}

	private int addOffering(ParsedObservationOffering data, int sosID) {

		Cursor c = db.query(Offering_TABLE, new String[] { ID }, Offering_sos
				+ "=" + sosID + " AND " + Offering_offering + "='"
				+ data.offering + "'", null, null, null, null);
		ContentValues values = getContentValues(data, sosID);
		int offeringID;

		boolean update;
		if (c.moveToFirst()) {
			update = true;
			offeringID = c.getInt(0);

		} else {
			update = false;
			offeringID = getNextID(Offering_TABLE);
		}

		c.close();

		values.put(ID, offeringID);

		/*
		 * insert properties and features
		 */
		int[] properties = insertOrIgnoreProperties(data.properties, offeringID);
		insertOrIgnoreFeatures(data.featuresOfInterest, offeringID);

		if (properties.length != 0) {
			values.put(Offering_selectedProperty, properties[0]);
		}
		/*
		 * insert the offering
		 */
		if (update) {
			db.update(Offering_TABLE, values, ID + "=" + offeringID, null);
		} else {
			db.insert(Offering_TABLE, null, values);
		}
		return offeringID;

	}

	private void deleteMeasurement(int propertyID) {
		Cursor c = db.query(Measures_TABLE, new String[] { ID },
				Measures_property + "=" + propertyID, null, null, null, null);
		if (c.moveToFirst()) {
			do {
				db.delete(Measure_TABLE, Measure_measurement + "="
						+ c.getInt(0), null);
			} while (c.moveToNext());
		}
		c.close();
		db.delete(Measures_TABLE, Measures_property + "=" + propertyID, null);
	}

	private void deleteOffering(int offeringID) {
		Cursor c = db.query(Property_TABLE, new String[] { ID },
				Property_offering + "=" + offeringID, null, null, null, null);
		if (c.moveToFirst()) {
			do {
				deleteMeasurement(c.getInt(0));
			} while (c.moveToNext());
		}
		c.close();
		db.delete(Property_TABLE, Property_offering + "=" + offeringID, null);
		db.delete(Feature_TABLE, Feature_offering + "=" + offeringID, null);
		db.delete(Offering_TABLE, ID + "=" + offeringID, null);
	}

	/**
	 * Returns the database ID of a Measurement.
	 * 
	 * @param propertyID
	 *            database id of the related Property
	 * @param featureID
	 *            database id of the related Feature
	 * @return the id or -1
	 */
	private int getMeasurementID(int propertyID, int featureID) {
		Cursor c = db.query(Measures_TABLE, new String[] { ID },
				Measures_property + "=" + propertyID + " AND "
						+ Measures_feature + "=" + featureID, null, null, null,
				null);
		int ret = -1;
		if (c.moveToFirst()) {
			ret = c.getInt(0);
		}
		c.close();
		return ret;
	}

	private int getNextID(String table) {
		Cursor c = db.rawQuery("SELECT max(" + ID + ") FROM " + table, null);
		int ret = 1;
		if (c.moveToFirst()) {
			ret = c.getInt(0) + 1;
		}
		c.close();
		return ret;
	}

	private int[] insertOrIgnoreFeatures(LinkedList<String> features,
			int offeringID) {
		int[] ids = new int[features.size()];
		Iterator<String> iter = features.iterator();
		ContentValues values;

		for (int i = 0; i < ids.length; i++) {
			String feature = iter.next();
			Cursor c = db.query(Feature_TABLE, new String[] { ID },
					Feature_feature + "='" + feature + "' AND "
							+ Feature_offering + "=" + offeringID, null, null,
					null, null);

			/*
			 * does feature already exist?
			 */
			if (c.moveToFirst()) {
				ids[i] = c.getInt(0);
			} else {
				values = new ContentValues();
				values.put(Feature_feature, feature);
				values.put(Feature_offering, offeringID);
				ids[i] = getNextID(Feature_TABLE);
				values.put(ID, ids[i]);
				db.insert(Feature_TABLE, null, values);
			}
			c.close();
		}

		return ids;
	}

	/**
	 * Inserts or replaces values of a measurement
	 * 
	 * @param times
	 *            time (y-) values should have the same length as values
	 * @param values
	 *            values (x) should have the same length as times
	 * @param measurementID
	 *            database id where the values should be inserted
	 * @throws IllegalArgumentExcpetion
	 *             if the lists dont have the same length
	 */
	private void insertOrIgnoreMeasure(LinkedList<Date> times,
			LinkedList<Float> values, int measurementID) {

		if (times.size() != values.size()) {
			Log.w(DT, "times size(" + times.size() + ") != values size ("
					+ values.size() + ")");
		} else {

			if (!(times.isEmpty())) {

				Iterator<Date> time = times.iterator();
				Iterator<Float> value = values.iterator();

				while (time.hasNext()) {
					float val = value.next().floatValue();

					db.execSQL(insertOrIgnoreMeasureBase + "("
							+ time.next().getTime() + ", "
							+ (Float.isNaN(val) ? NaN : val) + ", "
							+ measurementID + ")");
				}

			} else {
				// Log.w(DT, "no data to insert");
			}
		}

	}

	private int[] insertOrIgnoreProperties(LinkedList<String> properties,
			int offeringID) {
		int[] ids = new int[properties.size()];
		Iterator<String> iter = properties.iterator();
		ContentValues values;

		for (int i = 0; i < ids.length; i++) {
			String property = iter.next();
			Cursor c = db.query(Property_TABLE, new String[] { ID },
					Property_property + "='" + property + "' AND "
							+ Property_offering + "=" + offeringID, null, null,
					null, null);

			/*
			 * does feature already exist?
			 */
			if (c.moveToFirst()) {
				ids[i] = c.getInt(0);
			} else {
				values = new ContentValues();
				values.put(Property_property, property);
				values.put(Property_offering, offeringID);
				ids[i] = getNextID(Property_TABLE);
				values.put(ID, ids[i]);
				db.insert(Property_TABLE, null, values);
			}
			c.close();
		}

		return ids;
	}

	private static ContentValues getContentValues(
			ParsedObservationOffering data, int sosID) {
		ContentValues ret = new ContentValues();
		ret.put(Offering_name, data.name);
		ret.put(Offering_sos, sosID);
		ret.put(Offering_offering, data.offering);
		return ret;
	}

	private static ContentValues getContentValues(ParsedSOSCapabilities data) {
		ContentValues ret = new ContentValues();
		ret.put(SOS_GetObservation, data.getObservationPost);
		ret.put(SOS_GetCapabilities, data.getCapabilitiesGet);
		ret.put(SOS_color, 0);
		ret.put(SOS_visible, FALSE);
		ret.put(SOS_title, data.title);
		ret.put(SOS_description, data.description);
		return ret;
	}

	/**
	 * Inner Class for managing the connection and first-time initialization of
	 * the database
	 * 
	 * @author Mathias Menninghaus (mmening@uos.de)
	 * @version 22.06.2009
	 */
	private static class DatabaseHelper extends SQLiteOnSDCard {

		/**
		 * Create an new DatabaseHelper / SQLiteOpenHelper Class
		 * 
		 * @param context
		 */
		DatabaseHelper(Context context) {
			super(context, DATABASE_NAME, DATABASE_VERSION);
		}

		/**
		 * Only called when the database is created for the first time
		 */
		public void onCreate(SQLiteDatabase sqldb) {

			sqldb.execSQL(CREATE_SOS);
			sqldb.execSQL(CREATE_Offering);
			sqldb.execSQL(CREATE_Property);
			sqldb.execSQL(CREATE_Feature);
			sqldb.execSQL(CREATE_Measures);
			sqldb.execSQL(CREATE_Measure);
		}

		@Override
		public void onUpgrade(SQLiteDatabase sqldb, int oldVersion,
				int newVersion) {

			sqldb.execSQL("DROP TABLE IF EXISTS " + SOS_TABLE);
			sqldb.execSQL("DROP TABLE IF EXISTS " + Offering_TABLE);
			sqldb.execSQL("DROP TABLE IF EXISTS " + Property_TABLE);
			sqldb.execSQL("DROP TABLE IF EXISTS " + Feature_TABLE);
			sqldb.execSQL("DROP TABLE IF EXISTS " + Measures_TABLE);
			sqldb.execSQL("DROP TABLE IF EXISTS " + Measure_TABLE);

			onCreate(sqldb);
		}
	}
}
