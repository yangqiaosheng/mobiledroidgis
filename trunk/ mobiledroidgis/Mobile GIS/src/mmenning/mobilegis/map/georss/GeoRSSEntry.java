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

/**
 * Object to hold Information of a GeoRSS Entry to insert and query to the
 * GeoRSSDB.
 * 
 * @author Mathias Menninghaus
 * @version 15.10.2009
 */
public class GeoRSSEntry implements Comparable<GeoRSSEntry> {

	private static final String DT = "GeoRSSEntry";
	
	/*
	 * from xml
	 */
	public String title;
	public String description;
	public String link;
	public Date time;
	/**
	 * Latitude in WGS84 multiplied with E6
	 */
	public int latE6;
	/**
	 * Longitude in WGS84 multiplied with E6
	 */
	public int lonE6;
	/*
	 * for and from database
	 */
	public int geoRSSID;
	public int id;
	public boolean read;

	/**
	 * Compares by the time and then by the link
	 */
	public int compareTo(GeoRSSEntry another) {

		int timeCompare = another.time.compareTo(this.time);
		if (timeCompare == 0) {
			return this.link.compareTo(another.link);
		} else
			return timeCompare;
	}

	@Override
	public String toString() {
		return title;
	}

}