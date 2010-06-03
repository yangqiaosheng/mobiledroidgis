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
package mmenning.mobilegis.database;

import java.io.File;

import android.content.Context;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.os.Environment;

/**
 * Manages the Connection to a SQLITE Database on the SDCard. The Database will
 * be created in a folder named by the Applications package name.
 * 
 * @author Mathias Menninghaus
 * @version 23.10.2009
 */
public abstract class SQLiteOnSDCard {

	private static final String DT = "SQLiteOnSDCard";

	private static final String SDCARD = Environment.getExternalStorageDirectory().getAbsolutePath();

	private SQLiteDatabase db;

	/**
	 * Connect to a SQLite Database with name name in the folder named by the
	 * Contexts package name. If their is no such folder it will be created. If
	 * their is no such database file a new one will be created an onCreate()
	 * called.
	 * 
	 * @param context
	 *            Context in which the SQLite database will be created.
	 * @param name
	 *            Name of the Database
	 */
	public SQLiteOnSDCard(Context context, String name, int version) {
		db = null;
		
		String fullpath = SDCARD + File.separator + context.getPackageName()
				+ File.separator + name;
		try {
			db = SQLiteDatabase.openDatabase(fullpath, null,
					SQLiteDatabase.OPEN_READWRITE);
			int oldVersion = db.getVersion();
			db.setVersion(version);
			if (oldVersion != version) {
				onUpgrade(db, oldVersion, version);
			}
		} catch (SQLException ex) {
			File path = new File(SDCARD + File.separator
					+ context.getPackageName());
			if (!path.exists())
				path.mkdir();
			db = SQLiteDatabase.openOrCreateDatabase(fullpath, null);
			db.setVersion(version);
			onCreate(db);
		} finally {
			if (db != null) {
				db.close();
			}

		}
	}

	/**
	 * Close the database connection if it is not closed yet.
	 */
	public void close() {
		if (db != null && db.isOpen())
			db.close();
	}

	/**
	 * Opens a readable database connection if it dois not exist yet.
	 * 
	 * @return readable SQLite database
	 */
	public SQLiteDatabase getReadableDatabase() {
		if (db.isReadOnly() || !db.isOpen()) {
			db = SQLiteDatabase.openDatabase(db.getPath(), null,
					SQLiteDatabase.OPEN_READONLY);
		}
		return db;
	}

	/**
	 * Opens a writable database connection if it does not exist yet.
	 * 
	 * @return writable SQLite database
	 */
	public SQLiteDatabase getWritableDatabase() {
		if (db.isReadOnly() || !db.isOpen()) {
			db = SQLiteDatabase.openDatabase(db.getPath(), null,
					SQLiteDatabase.OPEN_READWRITE);
		}
		return db;
	}

	/**
	 * Called on Version Changes
	 * @param db SQLite database to change
	 * @param oldVersion old database version
	 * @param newVersion new database version
	 */
	public abstract void onUpgrade(SQLiteDatabase db, int oldVersion,
			int newVersion);

	/**
	 * Called when the Database is Created for the first time.
	 * 
	 * @param db
	 *            the newly created Database
	 */
	public abstract void onCreate(SQLiteDatabase db);

}