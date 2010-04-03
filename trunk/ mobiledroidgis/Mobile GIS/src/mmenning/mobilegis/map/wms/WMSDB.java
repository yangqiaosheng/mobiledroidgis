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

import java.util.HashSet;

import mmenning.mobilegis.database.NetImageStorage;
import mmenning.mobilegis.database.SQLiteOnSDCard;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;

/**
 * Manage a SQLite database to hold WMSData and LayerData. As in the
 * {@link ParsedWMSDataSet} only the whole data corresponding to a Web Map
 * Service can be stored and deleted. Manipulating one date may cause changes on
 * the whole dataset for a WebMap Service . See the methods for details. The
 * primary keys are always ids (int). But it is not possible to insert the same
 * WMS manifold, because by adding a new ParsedDataSet it is checked whether
 * there is already one with the same url.
 * 
 * @author Mathias Menninghaus
 * @version 23.10.2009
 * 
 */
public class WMSDB {

	public static final int TRUE = 1;
	public static final int FALSE = 0;
	public static final int NOSRS = -1;
	public static final int ROOTLAYER = -1;

	/**
	 * Tag for Android-Logfile
	 */
	private static final String DT = "WMSData";

	/**
	 * Name of the Database to which this model is connected
	 */
	private static final String DATABASE_NAME = "WMSData";

	private static final int DATABASE_VERSION = 1;
	/*
	 * Constants to generate and administrate the tables
	 */
	/*
	 * Tables
	 */
	private static final String WMS_TABLE = "WMS";
	private static final String SRS_TABLE = "SRS";
	private static final String LAYER_TABLE = "Layer";
	/*
	 * overall
	 */
	private static final String ID = "_id";
	/*
	 * Fields
	 */
	public static final String WMS_url = "url";
	public static final String WMS_version = "version";
	public static final String WMS_description = "description";
	public static final String WMS_getMapURL = "getMapURL";
	public static final String WMS_name = "name";
	public static final String WMS_rootLayer = "rootLayer";
	public static final String WMS_title = "title";
	public static final String WMS_visible = "visible";
	public static final String WMS_supportsPNG = "supportsPNG";
	public static final String WMS_priority = "priority";

	public static final String SRS_layer = "Layer";
	public static final String SRS_srs = "SRS";

	public static final String LAYER_selectedSRS = "selectedSRS";
	public static final String LAYER_bbox_maxy = "bbox_maxy";
	public static final String LAYER_bbox_miny = "bbox_miny";
	public static final String LAYER_bbox_maxx = "bbox_maxx";
	public static final String LAYER_bbox_minx = "bbox_minx";
	public static final String LAYER_legend_url = "legend_url";
	public static final String LAYER_attribution_logourl = "attribution_logourl";
	public static final String LAYER_attribution_url = "attribution_url";
	public static final String LAYER_attribution_title = "attribution_title";
	public static final String LAYER_rootLayer = "rootLayer";
	public static final String LAYER_description = "description";
	public static final String LAYER_name = "name";
	public static final String LAYER_title = "title";
	public static final String LAYER_visible = "visible";
	public static final String LAYER_wms = "wms";

	/*
	 * create table statements
	 */
	private static final String CREATE_SRS = "CREATE TABLE " + SRS_TABLE + " "
			+ "(" + ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " + "" + SRS_srs
			+ " TEXT, " + SRS_layer + " INTEGER)";
	private static final String CREATE_WMS = "CREATE TABLE " + WMS_TABLE + " ("
			+ WMS_visible + " TEXT, " + WMS_url + " TEXT, " + WMS_version
			+ " TEXT, " + ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
			+ WMS_description + " TEXT, " + WMS_getMapURL + " TEXT, "
			+ WMS_name + " TEXT, " + WMS_rootLayer + " INTEGER, " + WMS_title
			+ " TEXT, " + WMS_supportsPNG + " INTEGER, " + WMS_priority
			+ " INTEGER)";
	private static final String CREATE_LAYER = "CREATE TABLE " + LAYER_TABLE
			+ " " + "(" + LAYER_selectedSRS + " INTEGER, " + LAYER_bbox_maxy
			+ " REAL, " + "" + LAYER_bbox_miny + " REAL, " + ""
			+ LAYER_bbox_maxx + " REAL, " + "" + LAYER_bbox_minx + " REAL, "
			+ "" + LAYER_legend_url + " TEXT, " + ""
			+ LAYER_attribution_logourl + " TEXT, " + ""
			+ LAYER_attribution_url + " TEXT, " + "" + LAYER_attribution_title
			+ " TEXT, " + "" + LAYER_rootLayer + " INTEGER, " + "" + ID
			+ " INTEGER PRIMARY KEY, " + "" + LAYER_description + " TEXT, "
			+ "" + LAYER_name + " TEXT, " + "" + LAYER_title + " TEXT, " + ""
			+ LAYER_visible + " INTEGER, " + LAYER_wms + " INTEGER)";

	/**
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

	/**
	 * Sets context of this class (e.g. Android-Activity)
	 * 
	 * @param ctx
	 *            Context on which the database works
	 */
	public WMSDB(Context ctx) {
		this.context = ctx;
		DBHelper = new DatabaseHelper(context);
	}

	/**
	 * Insert WMSData from a ParsedWMSDataSet. It is not inserted when the
	 * database already contains a wms with the same url.Also all containing
	 * layers and there srs will be inserted. It is assumed that layers support
	 * every SRS which there Root supports.
	 * 
	 * @param in
	 *            data set to be inserted
	 * @return id of the currently or already available WMSData
	 */
	public int addParsedData(ParsedWMSDataSet in) {

		Cursor c = db.query(WMS_TABLE, new String[] { ID }, WMS_url + "='"
				+ in.url + "'", null, null, null, null);
		int wmsID;
		if (!c.moveToFirst()) {

			ContentValues values = new ContentValues();
			values.put(WMS_name, in.name);
			values.put(WMS_description, in.description);
			values.put(WMS_title, in.title);
			values.put(WMS_url, in.url);
			values.put(WMS_version, in.version);
			values.put(WMS_getMapURL, in.getMapURL);
			values.put(WMS_supportsPNG, in.supportsPNG ? TRUE : FALSE);
			values.put(WMS_visible, FALSE);
			final int rootLayerID = getNextlayerID();
			values.put(WMS_rootLayer, rootLayerID);

			db.insert(WMS_TABLE, null, values);
			c.requery();
			c.moveToFirst();
			wmsID = c.getInt(0);

			insertLayer(in.rootLayer, ROOTLAYER, wmsID);

		} else {
			c.moveToFirst();
			wmsID = c.getInt(0);
		}

		c.close();

		return wmsID;
	}

	/**
	 * Close Database
	 */
	public void close() {
		db.close();
		DBHelper.close();
	}

	/**
	 * Requests whether the given layer contains other layers or 'is a leaf'
	 * 
	 * @param layerID
	 *            id of the root Layer
	 * @return true if it contains other Layers, else false
	 */
	public boolean containsLayers(int layerID) {
		boolean ret = false;

		Cursor c = db.query(LAYER_TABLE, new String[] { ID }, LAYER_rootLayer
				+ "=" + layerID, null, null, null, null);
		ret = c.moveToFirst();
		c.close();
		return ret;
	}

	/**
	 * Delete a WMS from this database. Also all corresponding layers and theirs
	 * srs will be deleted.
	 * 
	 * @param wmsID
	 *            id of the WMS to be deleted.
	 */
	public void deleteWMS(int wmsID) {
		this
				.deleteLayer(Integer.valueOf(this.getWMSData(wmsID,
						WMS_rootLayer)));

		db.delete(WMS_TABLE, ID + "=" + wmsID, null);

	}

	/**
	 * Get all WMSDate in this database
	 * 
	 * @return A List of WMSData in this database, maybe empty, sorted by
	 *         priority descending
	 */
	public WMSData[] getAllWMS() {

		Cursor c = db.query(WMS_TABLE, new String[] { ID }, null, null, null,
				null, WMS_priority + " DESC");

		WMSData[] ret = new WMSData[c.getCount()];

		if (c.moveToFirst()) {
			for (int i = 0; i < ret.length; i++) {
				ret[i] = getWMS(c.getInt(0));
				c.moveToNext();
			}
		}

		c.close();

		return ret;
	}

	/**
	 * Get all and only Containing Layers in a specific layer. This method is
	 * not recursive and the given root will not be returned. To get a specific
	 * layer use {@link WMSDB.getLayer}
	 * 
	 * @param layerID
	 *            id of the root, it will not be returned as layer
	 * @return Array of LayerData of the containing layers, maybe empty
	 */
	public LayerData[] getContainingLayers(int layerID) {

		Cursor c = db.query(LAYER_TABLE, new String[] { ID }, LAYER_rootLayer
				+ "=" + layerID, null, null, null, null);

		LayerData[] ret = new LayerData[c.getCount()];

		if (c.moveToFirst()) {
			for (int i = 0; i < ret.length; i++) {
				ret[i] = this.getLayer(c.getInt(0));
				c.moveToNext();
			}
		}

		c.close();

		return ret;
	}

	/**
	 * Get a specific Layer.
	 * 
	 * @param layerID
	 *            the id corresponding to the requested layer
	 * @return LayerData of the requested Layer, if there is one, else null
	 */
	public LayerData getLayer(int layerID) {
		LayerData ret = null;

		Cursor c = db.query(LAYER_TABLE, new String[] {
				LAYER_attribution_logourl, LAYER_attribution_title,
				LAYER_attribution_url, LAYER_bbox_maxx, LAYER_bbox_maxy,
				LAYER_bbox_minx, LAYER_bbox_miny, LAYER_description,
				LAYER_legend_url, LAYER_name, ID, LAYER_title, LAYER_visible },
				ID + "=" + layerID, null, null, null, null);

		if (c.moveToFirst()) {
			ret = new LayerData();
			ret.attribution_logourl = c.getString(0);
			ret.attribution_title = c.getString(1);
			ret.attribution_url = c.getString(2);
			ret.bbox_maxx = c.getFloat(3);
			ret.bbox_maxy = c.getFloat(4);
			ret.bbox_minx = c.getFloat(5);
			ret.bbox_miny = c.getFloat(6);
			ret.description = c.getString(7);
			ret.legend_url = c.getString(8);
			ret.name = c.getString(9);
			ret.id = c.getInt(10);
			ret.title = c.getString(11);
			ret.visible = (c.getInt(12) == TRUE);
			ret.selectedSRS = this.getSelectedSRS(layerID);
		}

		c.close();

		return ret;
	}

	/**
	 * Get Data of a Layer as String. This method starts a simple query for the
	 * requested attribute.
	 * 
	 * @param layerID
	 *            id of the requested layer
	 * @param column
	 *            Constant String for the Column which is requested always
	 *            starting with 'LAYER_'
	 * @return result of the query if there is one, else null
	 */
	public String getLayerData(int layerID, String column) {
		Cursor c = db.query(LAYER_TABLE, new String[] { column }, ID + "="
				+ layerID, null, null, null, null);
		String ret = null;

		if (c.moveToFirst()) {
			ret = c.getString(0);
		}
		c.close();
		return ret;
	}

	/**
	 * Get all available SRS for this Layer.
	 * 
	 * @param layerID
	 *            id of the requested Layer
	 * @return an String Array of supported SRS, maybe empty, sorted
	 */
	public String[] getSRS(int layerID) {

		Cursor c = db.query(SRS_TABLE, new String[] { SRS_srs }, SRS_layer
				+ "=" + layerID, null, null, null, SRS_srs + " DESC");

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
	 * Query all srs for the visible layers of a wms. contains to ? which should
	 * be filled with the wmsID
	 */
	private static final String selectSRSforVisibleLayers = "SELECT DISTINCT "
			+ SRS_TABLE + "." + SRS_srs + " FROM " + LAYER_TABLE + ", "
			+ SRS_TABLE + " WHERE " + LAYER_TABLE + "." + LAYER_wms + "= ?"
			+ " AND " + LAYER_TABLE + "." + LAYER_selectedSRS + "=" + SRS_TABLE
			+ "." + ID + " AND " + LAYER_TABLE + "." + LAYER_visible + "="
			+ TRUE + " AND " + LAYER_TABLE + "." + LAYER_rootLayer + " NOT IN "
			+ "(SELECT " + LAYER_TABLE + "." + ID + " FROM " + LAYER_TABLE
			+ " WHERE " + LAYER_TABLE + "." + LAYER_visible + "=" + TRUE
			+ " AND " + LAYER_wms + "=?" + ")";

	/**
	 * Get all SRS for the visible layers. If their is there is more or less
	 * than exact one srs for the visible layers in this WMS it will return an
	 * empty String.
	 * 
	 * @param wmsID
	 *            id of the wms
	 * @return a srs as string or an empty string
	 * @see {@link WMSDB.getVisibleLayerIDs}
	 */
	public String getSRSforVisibleLayers(int wmsID) {

		String ret = null;

		Cursor c = db.rawQuery(selectSRSforVisibleLayers, new String[] {
				"" + wmsID, "" + wmsID });

		if (c.moveToFirst()) {
			ret = c.getString(0);
			if (c.moveToNext()) {
				ret = "";
			}
		} else {
			ret = "";
		}
		c.close();
		return ret;
	}

	/**
	 * query all visible layers of a wms. contains to ? which should be filled
	 * with the wmsID
	 */
	private static final String selectVisibleLayerNames = "SELECT "
			+ LAYER_name + " FROM " + LAYER_TABLE + " WHERE " + LAYER_visible
			+ "=" + TRUE + " AND " + LAYER_wms + "=?" + " AND "
			+ LAYER_rootLayer + " NOT IN " + "(SELECT " + ID + " FROM "
			+ LAYER_TABLE + " WHERE " + LAYER_visible + "=" + TRUE + " AND "
			+ LAYER_wms + "=?)";

	/**
	 * Get the names of all visible layers. If the actual layer is not visible
	 * all containing layers are checked. If it is visible, they are not and its
	 * added to the list.
	 * 
	 * @param wmsID
	 *            id of the wms
	 * @return a List of layer names (Strings), maybe empty
	 */
	public String[] getVisibleLayerNames(int wmsID) {

		Cursor c = db.rawQuery(selectVisibleLayerNames, new String[] {
				"" + wmsID, "" + wmsID });

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
	 * Get a list of IDs for all visible wms.
	 * 
	 * @return a List of Integer of all visible wms, maybe empty, sorted by
	 *         priority descending
	 */
	public int[] getVisibleWMS() {

		Cursor c = db.query(WMS_TABLE, new String[] { ID }, WMS_visible + "="
				+ TRUE, null, null, null, WMS_priority + " DESC");
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
	 * Get WMSData of a single WMS
	 * 
	 * @param wmsID
	 *            id of the requested wms
	 * @return WMSData corresponding to the id, or null if it does not exist
	 */
	public WMSData getWMS(int wmsID) {

		WMSData ret = null;

		Cursor c = db.query(WMS_TABLE, new String[] { WMS_description,
				WMS_getMapURL, WMS_name, ID, WMS_supportsPNG, WMS_title,
				WMS_url, WMS_version, WMS_visible }, ID + "=" + wmsID, null,
				null, null, null);

		if (c.moveToFirst()) {
			ret = new WMSData();
			ret.description = c.getString(0);
			ret.getMapURL = c.getString(1);
			ret.name = c.getString(2);
			ret.id = c.getInt(3);
			ret.supportsPNG = (c.getInt(4) == TRUE);
			ret.title = c.getString(5);
			ret.url = c.getString(6);
			ret.version = c.getString(7);
			ret.visible = (c.getInt(8) == TRUE);
		}
		c.close();

		return ret;
	}

	/**
	 * Get Data of a WMS as String. This method starts a simple query for the
	 * requested attribute.
	 * 
	 * @param wmsID
	 *            id of the requested wms
	 * @param column
	 *            Constant String for the Column which is requested always
	 *            starting with 'WMS_'
	 * @return result of the query if there is one, else null
	 */
	public String getWMSData(int wmsID, String column) {
		Cursor c = db.query(WMS_TABLE, new String[] { column }, ID + "="
				+ wmsID, null, null, null, null);
		String ret = null;

		if (c.moveToFirst()) {
			ret = c.getString(0);
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
	public WMSDB open() throws SQLException {
		db = DBHelper.getWritableDatabase();
		return this;
	}

	/**
	 * Connects to database in Read-Only. One should only access query methods!
	 * 
	 * @return
	 * @throws SQLExcpetion
	 */
	public WMSDB openReadOnly() throws SQLException {
		db = DBHelper.getReadableDatabase();
		return this;
	}

	/**
	 * Set the visibility of a specific layer.
	 * 
	 * @param layerID
	 *            id of the layer
	 * @param visible
	 *            its new visibility
	 */
	public void setLayerVisibility(int layerID, boolean visible) {

		ContentValues values = new ContentValues();
		values.put(LAYER_visible, visible ? TRUE : FALSE);
		db.update(LAYER_TABLE, values, ID + "=" + layerID, null);
	}

	/**
	 * Sets the priority of this wms.
	 * 
	 * @param wmsID
	 *            Id of the updated WMS
	 * @param priority
	 *            new Priority to insert
	 */
	public void setPriority(int wmsID, int priority) {
		ContentValues values = new ContentValues();
		values.put(WMS_priority, priority);
		db.update(WMS_TABLE, values, ID + "=" + wmsID, null);

	}

	/**
	 * Set the selected srs of a specific layer. The layer must support this
	 * srs.
	 * 
	 * @param layerID
	 *            id of the layer
	 * @param srs
	 *            String for the srs.
	 * @return true if the layer supports this srs, else false
	 */
	public boolean setSRS(int layerID, String srs) {

		Cursor c = db.query(SRS_TABLE, new String[] { ID }, SRS_srs + "='"
				+ srs + "' AND " + SRS_layer + "=" + layerID, null, null, null,
				null);

		if (c.moveToFirst()) {
			ContentValues values = new ContentValues();
			values.put(LAYER_selectedSRS, c.getInt(0));
			db.update(LAYER_TABLE, values, ID + "=" + layerID, null);
			c.close();
			return true;

		} else {
			c.close();
			return false;
		}

	}

	/**
	 * Set the visibility of a specific wms
	 * 
	 * @param wmsID
	 *            id of the wms
	 * @param visible
	 *            new Visibility of the wms
	 */
	public void setWMSVisibility(int wmsID, boolean visible) {

		ContentValues values = new ContentValues();
		values.put(WMS_visible, visible ? TRUE : FALSE);
		db.update(WMS_TABLE, values, ID + "=" + wmsID, null);
	}

	/**
	 * Get the total count of Layers in a WMS. Containing the RootLayer and all
	 * its Children.
	 * 
	 * @param wmsID
	 *            id of wms to query
	 * @return total Count of layers in the wms
	 */
	public int getLayerCount(int wmsID) {
		int rootID = Integer.valueOf(this.getWMSData(wmsID, WMS_rootLayer));
		return getContainingLayerCount(rootID) + 1;
	}

	/**
	 * Get the count of layers in a root layer. The root layer will not be
	 * counted. A Layer without containing Layers will return 0, a Layer with
	 * containing Layers will return the number of all containing Layers
	 * including all Layer - notes.
	 * 
	 * @param layerID
	 *            id of the root layer
	 * @return number of containing layers in the root layer
	 */
	public int getContainingLayerCount(int layerID) {
		Cursor c = db.query(LAYER_TABLE, new String[] { ID }, LAYER_rootLayer
				+ "=" + layerID, null, null, null, null);
		int ret = 0;
		if (c.moveToFirst()) {
			do {
				ret += getContainingLayerCount(c.getInt(0)) + 1;
			} while (c.moveToNext());
		}
		c.close();
		return ret;
	}

	private void deleteLayer(int layerID) {

		Cursor c = db.query(LAYER_TABLE, new String[] { ID }, LAYER_rootLayer
				+ "=" + layerID, null, null, null, null);

		if (c.moveToFirst()) {
			do {
				deleteLayer(c.getInt(0));
			} while (c.moveToNext());
		}

		c.close();

		NetImageStorage nis = new NetImageStorage(this.context);
		nis.delete(this.getLayerData(layerID, LAYER_attribution_logourl));
		nis.delete(this.getLayerData(layerID, LAYER_legend_url));

		db.delete(SRS_TABLE, SRS_layer + "=" + layerID, null);

		db.delete(LAYER_TABLE, ID + "=" + layerID, null);

	}

	private static final String selectMaxLayerID = "SELECT MAX(" + ID
			+ ") FROM " + LAYER_TABLE;

	private int getNextlayerID() {

		Cursor c = db.rawQuery(selectMaxLayerID, null);
		int id;

		if (c.moveToFirst()) {
			id = c.getInt(0) + 1;
		} else {
			id = 0;
		}
		c.close();
		return id;
	}

	private String getSelectedSRS(int layerID) {

		String ret = null;
		String query = "SELECT " + SRS_srs + " FROM " + SRS_TABLE + ","
				+ LAYER_TABLE + " WHERE " + SRS_TABLE + "." + ID + "="
				+ LAYER_TABLE + "." + LAYER_selectedSRS + " AND " + LAYER_TABLE
				+ "." + ID + "=" + layerID;

		Cursor c = db.rawQuery(query, null);

		if (c.moveToFirst()) {
			ret = c.getString(0);
		}
		c.close();

		return ret;
	}

	private void insertLayer(ParsedWMSDataSet.ParsedLayer parsedLayer,
			int rootlayerID, int wmsID) {

		ContentValues values = getLayerContentValues(parsedLayer, wmsID);

		final int rootID = getNextlayerID();

		values.put(LAYER_rootLayer, rootlayerID);
		values.put(ID, rootID);
		values.put(LAYER_selectedSRS, NOSRS);

		db.insert(LAYER_TABLE, null, values);

		NetImageStorage nis = new NetImageStorage(this.context);
		nis.store(parsedLayer.attribution_logourl);
		nis.store(parsedLayer.legend_url);

		insertSRS(parsedLayer.parsedSRS, rootID);

		for (ParsedWMSDataSet.ParsedLayer l : parsedLayer.parsedLayers) {
			/*
			 * containing layer will support all srs of the rootLayer
			 */
			l.parsedSRS.addAll(parsedLayer.parsedSRS);
			insertLayer(l, rootID, wmsID);
		}
	}

	private void insertSRS(HashSet<String> parsedSRS, int layerID) {
		if (parsedSRS != null) {
			for (String s : parsedSRS) {
				ContentValues values = new ContentValues();
				values.put(SRS_srs, s);
				values.put(SRS_layer, layerID);
				db.insert(SRS_TABLE, null, values);
			}
		}
	}

	private static ContentValues getLayerContentValues(
			ParsedWMSDataSet.ParsedLayer parsedLayer, int wmsID) {

		ContentValues values = new ContentValues();
		values.put(LAYER_name, parsedLayer.name);
		values.put(LAYER_title, parsedLayer.title);
		values.put(LAYER_description, parsedLayer.description);

		values.put(LAYER_legend_url, parsedLayer.legend_url);

		values.put(LAYER_attribution_logourl, parsedLayer.attribution_logourl);
		values.put(LAYER_attribution_title, parsedLayer.attribution_title);
		values.put(LAYER_attribution_url, parsedLayer.attribution_url);

		values.put(LAYER_visible, FALSE);

		values.put(LAYER_bbox_maxx, parsedLayer.bbox_maxx);
		values.put(LAYER_bbox_maxy, parsedLayer.bbox_maxy);
		values.put(LAYER_bbox_minx, parsedLayer.bbox_minx);
		values.put(LAYER_bbox_miny, parsedLayer.bbox_miny);

		values.put(LAYER_wms, wmsID);

		return values;

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

			sqldb.execSQL(CREATE_SRS);
			sqldb.execSQL(CREATE_WMS);
			sqldb.execSQL(CREATE_LAYER);
		}

		@Override
		public void onUpgrade(SQLiteDatabase sqldb, int oldVersion,
				int newVersion) {

			sqldb.execSQL("DROP TABLE IF EXISTS " + SRS_TABLE);
			sqldb.execSQL("DROP TABLE IF EXISTS " + WMS_TABLE);
			sqldb.execSQL("DROP TABLE IF EXISTS " + LAYER_TABLE);

			onCreate(sqldb);

		}
	}
}
