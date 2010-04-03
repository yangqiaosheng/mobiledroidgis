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
package mmenning.mobilegis.map.georss;

import java.util.Date;

import mmenning.mobilegis.database.SQLiteOnSDCard;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;

/**
 * Class to manage a SQLite Database in which GeoRSSFeed and GeoRSSEntry are
 * stored. The primary key is always _id and it is definite over the whole
 * database for layers and wms. Nevertheless by inserting a GeoRSSFeed it is
 * checked whether there is a feed with the same request url in the database and
 * for GeoRSS Entry it is checked whether there is a GeoRSSEntry with the same
 * description and/or title and geoRSSid in the database. So it shall not be
 * possible to insert redundant data.
 * 
 * @author Mathias Menninghaus
 * @version 09.10.2009
 * 
 * @see {@link GeoRSSFeed}
 * @see {@link GeoRSSEntry}
 * 
 */
public class GeoRSSDB {

	private static final String DT = "GeoRSSData";

	public static final int TRUE = 1;
	public static final int FALSE = 0;

	private static final String DATABASE_NAME = "GeoRSS";

	private static final int DATABASE_VERSION = 1;

	private static final String ID = "_id";
	private static final String ENTRIES_TABLE = "entries";
	private static final String ENTRIES_TITLE = "title";
	private static final String ENTRIES_DESCRIPTION = "description";
	private static final String ENTRIES_LAT = "latitude";
	private static final String ENTRIES_LON = "longitude";
	private static final String ENTRIES_TIME = "time";
	private static final String ENTRIES_GEORSS = "georss";
	private static final String ENTRIES_LINK = "link";
	private static final String ENTRIES_READ = "read";
	private static final String GEORSS_TABLE = "georss";
	private static final String GEORSS_URL = "url";
	private static final String GEORSS_TITLE = "title";
	private static final String GEORSS_DESCRIPTION = "description";
	private static final String GEORSS_VISIBLE = "visible";
	private static final String GEORSS_COLOR = "color";
	private static final String GEORSS_LINK = "link";

	private static final String CREATE_GEORSS = "CREATE TABLE " + GEORSS_TABLE
			+ "(" + ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " + GEORSS_TITLE
			+ " TEXT, " + GEORSS_URL + " TEXT, " + GEORSS_DESCRIPTION
			+ " TEXT, " + GEORSS_VISIBLE + " INTEGER, " + GEORSS_COLOR
			+ " INTEGER, " + GEORSS_LINK + " TEXT)";
	private static final String CREATE_ENTRIES = "CREATE TABLE "
			+ ENTRIES_TABLE + "(" + ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
			+ ENTRIES_TITLE + " TEXT, " + ENTRIES_DESCRIPTION + " TEXT, "
			+ ENTRIES_LAT + " INTEGER, " + ENTRIES_LON + " INTEGER, "
			+ ENTRIES_TIME + " INTEGER, " + ENTRIES_GEORSS + " INTEGER, "
			+ ENTRIES_LINK + " TEXT, " + ENTRIES_READ + " INTEGER)";

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
	public GeoRSSDB(Context ctx) {
		this.context = ctx;
		DBHelper = new DatabaseHelper(context);
	}

	/**
	 * Adds a new GeoRSSEntry to the database. If this is already available, it
	 * will not. It is strongly recommended, that the entry knows the id of its
	 * feed!
	 * 
	 * @param entry
	 *            GeoRSSEntry to be inserted containing id of its feed
	 * @return id of the inserted or already available Entry.
	 */
	public int addEntry(GeoRSSEntry entry) {
		int ret = -1;

		Cursor c = db.query(ENTRIES_TABLE, new String[] { ID }, ENTRIES_GEORSS
				+ "=" + entry.geoRSSID + " AND " + ENTRIES_TITLE + "='"
				+ entry.title + "' AND " + ENTRIES_DESCRIPTION + "='"
				+ entry.description + "'", null, null, null, null);

		if (c.moveToFirst()) {
			ret = c.getInt(0);
		} else {
			db.insert(ENTRIES_TABLE, null, getContentValues(entry));
			c.requery();
			c.moveToFirst();
			ret = c.getInt(0);
		}
		c.close();
		return ret;
	}

	/**
	 * Adds a new GeoRSSFeed to the database. If this one already is available,
	 * nothing will be done.
	 * 
	 * @param feed
	 *            GeoRSSFeed to insert.
	 * @return the id of the the inserted or already available GeoRSSFeed
	 */
	public int addGeoRSS(GeoRSSFeed feed) {
		int ret = -1;
		Cursor c = db.query(GEORSS_TABLE, new String[] { ID }, GEORSS_URL
				+ "='" + feed.url + "'", null, null, null, null);

		if (c.moveToFirst()) {
			ret = c.getInt(0);
		} else {
			db.insert(GEORSS_TABLE, null, getContentValues(feed));
			c.requery();
			c.moveToFirst();
			ret = c.getInt(0);
		}
		c.close();
		return ret;
	}

	/**
	 * Close Database
	 */
	public void close() {
		db.close();
		DBHelper.close();
	}

	/**
	 * Delete a GeoRSSFeed
	 * 
	 * @param geoRSSID
	 *            id of the feed to be deleted {@link GeoRSSFeed.id}
	 */
	public void deleteGEORSS(int geoRSSID) {
		db.delete(ENTRIES_TABLE, ENTRIES_GEORSS + "=" + geoRSSID, null);
		db.delete(GEORSS_TABLE, ID + "=" + geoRSSID, null);
	}

	/**
	 * Delete the oldest GeoRSSEntries to match target size.
	 * 
	 * @param targetSize
	 *            maximum number of GeoRSSEntries in this database
	 */
	public void deleteOldest(int targetSize) {
		Cursor c = db.query(ENTRIES_TABLE, new String[] { ID }, null, null,
				null, null, ENTRIES_TIME + " ASC");
		if (c.moveToFirst()) {
			int cnt = c.getCount();
			if (cnt > targetSize) {
				do {
					db.delete(ENTRIES_TABLE, ID + "=" + c.getInt(0), null);
					cnt--;
				} while (c.moveToNext() && (cnt > targetSize));
			}
		}
		c.close();
	}

	/**
	 * Get all GeoRSSFeeds in this database
	 * 
	 * @return an array of GeoRSSFeed, maybe empty, unsorted
	 */
	public GeoRSSFeed[] getAllGeoRSS() {

		Cursor c = db.query(GEORSS_TABLE, new String[] { ID }, null, null,
				null, null, null);

		GeoRSSFeed[] ret = new GeoRSSFeed[c.getCount()];

		if (c.moveToFirst()) {
			for (int i = 0; i < ret.length; i++) {
				ret[i] = getGeoRSS(c.getInt(0));
				c.moveToNext();
			}
		}
		c.close();

		return ret;
	}

	/**
	 * Get the request urls (not links) of all GeoRSSFeed in this database
	 * 
	 * @return an array of GeoRSSFeed urls, maybe empty
	 */
	public String[] getAllGeoRSSurls() {

		Cursor c = db.query(GEORSS_TABLE, new String[] { GEORSS_URL }, null,
				null, null, null, null);

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
	 * Get all visible GeoRSSFeeds in this database
	 * 
	 * @return an array of GeoRSSFeed, maybe empty
	 */
	public GeoRSSFeed[] getAllVisibleGeoRSS() {

		Cursor c = db.query(GEORSS_TABLE, new String[] { ID }, GEORSS_VISIBLE
				+ "=" + TRUE, null, null, null, null);

		GeoRSSFeed[] ret = new GeoRSSFeed[c.getCount()];

		if (c.moveToFirst()) {
			for (int i = 0; i < ret.length; i++) {
				ret[i] = getGeoRSS(c.getInt(0));
				c.moveToNext();
			}
		}
		c.close();

		return ret;
	}

	/**
	 * Returns the Title of a GeoRSSFeed stored in this database
	 * 
	 * @param geoRSSID
	 *            id of the queried Feed
	 * @return the title, or null if the Feed does not exist
	 */
	public String getGeoRSSTitle(int geoRSSID) {
		String ret = null;
		Cursor c = db.query(GEORSS_TABLE, new String[] { GEORSS_TITLE }, null,
				null, null, null, null);

		if (c.moveToFirst()) {
			ret = c.getString(0);
		}
		c.close();
		return ret;
	}

	/**
	 * Get a single GeoRSSEntry corresponding to this id.
	 * 
	 * @param entryID
	 *            id of the requested GeoRSSEntry
	 * @return the requested GeoRSSEntry, maybe null
	 */
	public GeoRSSEntry getEntry(int entryID) {
		GeoRSSEntry ret = null;

		Cursor c = db.query(ENTRIES_TABLE, new String[] { ID, ENTRIES_TITLE,
				ENTRIES_DESCRIPTION, ENTRIES_LINK, ENTRIES_LAT, ENTRIES_LON,
				ENTRIES_TIME, ENTRIES_GEORSS, ENTRIES_READ }, ID + "="
				+ entryID, null, null, null, null);

		if (c.moveToFirst()) {
			ret = new GeoRSSEntry();
			ret.id = c.getInt(0);
			ret.title = c.getString(1);
			ret.description = c.getString(2);
			ret.link = c.getString(3);
			ret.latE6 = c.getInt(4);
			ret.lonE6 = c.getInt(5);
			ret.time = new Date(c.getLong(6));
			ret.geoRSSID = c.getInt(7);
			ret.read = (c.getInt(8) == TRUE ? true : false);
		}
		c.close();

		return ret;
	}

	/**
	 * Get all GeoRSSEntry corresponding to a GeoRSSFeed
	 * 
	 * @param geoRSSID
	 *            Id of the corresponding GeoRSSFeed
	 * @return an of GeoRSSEntry, maybe empty, sorted by time descending
	 */
	public GeoRSSEntry[] getEntrys(int geoRSSID) {

		Cursor c = db.query(ENTRIES_TABLE, new String[] { ID }, ENTRIES_GEORSS
				+ "=" + geoRSSID, null, null, null, ENTRIES_TIME + " DESC");

		GeoRSSEntry[] ret = new GeoRSSEntry[c.getCount()];

		if (c.moveToFirst()) {
			for (int i = 0; i < ret.length; i++) {
				ret[i] = getEntry(c.getInt(0));
				c.moveToNext();
			}
		}
		c.close();

		return ret;

	}

	/**
	 * Get a single GeoRSSFeed by its ID.
	 * 
	 * @param geoRSSID
	 *            id of the requested feed
	 * @return GeoRSSFeed corresponding to this id, maybe null
	 */
	public GeoRSSFeed getGeoRSS(int geoRSSID) {
		GeoRSSFeed ret = null;

		Cursor c = db.query(GEORSS_TABLE, new String[] { ID, GEORSS_TITLE,
				GEORSS_DESCRIPTION, GEORSS_URL, GEORSS_VISIBLE, GEORSS_COLOR,
				GEORSS_LINK

		}, ID + "=" + geoRSSID, null, null, null, null);

		if (c.moveToFirst()) {
			ret = new GeoRSSFeed();
			ret.id = c.getInt(0);
			ret.title = c.getString(1);
			ret.description = c.getString(2);
			ret.url = c.getString(3);
			ret.visible = (c.getInt(4) == TRUE ? true : false);
			ret.color = c.getInt(5);
			ret.link = c.getString(6);
		}
		c.close();

		return ret;

	}

	/**
	 * Is a specific entry marked as read?
	 * 
	 * @param entryID
	 *            id of the requested entry
	 * @return true, if it is read, else false also when there is not such an
	 *         entry
	 */
	public boolean isGeoRSSEntryRead(int entryID) {
		boolean ret = false;
		Cursor c = db.query(ENTRIES_TABLE, new String[] { ENTRIES_READ }, ID
				+ "=" + entryID, null, null, null, null);
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
	public GeoRSSDB open() throws SQLException {
		db = DBHelper.getWritableDatabase();
		return this;
	}

	/**
	 * Connects to databas in Read-Only. One should only access query methods!
	 * 
	 * @return
	 * @throws SQLExcpetion
	 */
	public GeoRSSDB openReadOnly() throws SQLException {
		db = DBHelper.getReadableDatabase();
		return this;
	}

	/**
	 * Mark all GeoRSS Entry corresponding to a feed as read/unread.
	 * 
	 * @param geoRSSID
	 *            id of the feed {@link GeoRSSFeed.id}
	 * @param read
	 *            flag to set them all read or unread.
	 */
	public void setAllRead(int geoRSSID, boolean read) {
		ContentValues values = new ContentValues();
		values.put(ENTRIES_READ, (read ? TRUE : FALSE));
		db.update(ENTRIES_TABLE, values, ENTRIES_GEORSS + "=" + geoRSSID, null);
	}

	/**
	 * Get the Count of all unread entries in a specific GeoRSSFeed
	 * 
	 * @param geoRSSID
	 *            id of the feed
	 * @return number of unread entries in this feed
	 */
	public int getUnreadEntryCount(int geoRSSID) {
		Cursor c = db.rawQuery("SELECT COUNT(" + ID + ") FROM " + ENTRIES_TABLE
				+ " WHERE " + ENTRIES_READ + "=" + FALSE + " AND "
				+ ENTRIES_GEORSS + "=" + geoRSSID, null);
		c.moveToFirst();
		int ret = c.getInt(0);
		c.close();
		return ret;
	}

	/**
	 * Get the Count of all entries in a specific GeoRSSFeed
	 * 
	 * @param geoRSSID
	 *            id of the feed
	 * @return number of entries in this feed
	 */
	public int getTotalEntryCount(int geoRSSID) {
		Cursor c = db.rawQuery("SELECT COUNT(" + ID + ") FROM " + ENTRIES_TABLE
				+ " WHERE " + ENTRIES_GEORSS + "=" + geoRSSID, null);
		c.moveToFirst();
		int ret = c.getInt(0);
		c.close();
		return ret;
	}

	/**
	 * Mark a single GeoRSSEntry as visible
	 * 
	 * @param entryID
	 *            id of the GeoRSSEntry
	 * @param read
	 *            flag to which the visibility is set.
	 */
	public void setGeoRSSEntryRead(int entryID, boolean read) {

		ContentValues values = new ContentValues();
		values.put(ENTRIES_READ, (read ? TRUE : FALSE));
		db.update(ENTRIES_TABLE, values, ID + "=" + entryID, null);
	}

	/**
	 * Set the RGB color of a GeoRSSFeed in this database
	 * 
	 * @param geoRSSID
	 *            id of the GeoRSSFeed
	 * @param color
	 *            new RGB color for the feed
	 */
	public void setGeoRSSFeedColor(int geoRSSID, int color) {
		ContentValues values = new ContentValues();
		values.put(GEORSS_COLOR, color);
		db.update(GEORSS_TABLE, values, ID + "=" + geoRSSID, null);
	}

	/**
	 * Set a GeoRSSFeedVisible
	 * 
	 * @param geoRSSID
	 *            id of the GeoRSSFeed
	 * @param visible
	 *            the new visibility
	 */
	public void setGeoRSSFeedVisible(int geoRSSID, boolean visible) {
		ContentValues values = new ContentValues();
		values.put(GEORSS_VISIBLE, (visible ? TRUE : FALSE));
		db.update(GEORSS_TABLE, values, ID + "=" + geoRSSID, null);
	}

	/**
	 * Fill content values with information of a GeoRSSEntry
	 * 
	 * @param entry
	 * @return
	 */
	private static ContentValues getContentValues(GeoRSSEntry entry) {
		ContentValues values = new ContentValues();
		values.put(ENTRIES_TITLE, entry.title);
		values.put(ENTRIES_DESCRIPTION, entry.description);
		values.put(ENTRIES_LINK, entry.link);
		values.put(ENTRIES_LAT, entry.latE6);
		values.put(ENTRIES_LON, entry.lonE6);
		values.put(ENTRIES_TIME, entry.time.getTime());
		values.put(ENTRIES_GEORSS, entry.geoRSSID);
		values.put(ENTRIES_READ, entry.read ? TRUE : FALSE);
		return values;

	}

	/**
	 * Fill content values with information of a GeoRSSFeed
	 * 
	 * @param feed
	 * @return
	 */
	private static ContentValues getContentValues(GeoRSSFeed feed) {
		ContentValues values = new ContentValues();

		values.put(GEORSS_URL, feed.url);
		values.put(GEORSS_TITLE, feed.title);
		values.put(GEORSS_DESCRIPTION, feed.description);
		values.put(GEORSS_LINK, feed.link);
		values.put(GEORSS_COLOR, feed.color);
		values.put(GEORSS_VISIBLE, feed.visible ? TRUE : FALSE);

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

			sqldb.execSQL(CREATE_ENTRIES);
			sqldb.execSQL(CREATE_GEORSS);
		}

		@Override
		public void onUpgrade(SQLiteDatabase sqldb, int oldVersion,
				int newVersion) {
			sqldb.execSQL("DROP TABLE IF EXISTS " + ENTRIES_TABLE);
			sqldb.execSQL("DROP TABLE IF EXISTS " + GEORSS_TABLE);

			onCreate(sqldb);

		}
	}

}
