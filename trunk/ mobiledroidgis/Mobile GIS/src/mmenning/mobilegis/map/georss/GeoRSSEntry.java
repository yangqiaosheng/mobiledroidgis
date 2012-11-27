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