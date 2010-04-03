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
import java.util.LinkedList;
import java.util.List;

/**
 * Object to hold GeoRSSFeed Information parsed from a GeoRSSFeed including its Entries.
 * 
 * @author Mathias Menninghaus
 * @version 15.10.2009
 * 
 * @see {@link GeoRSSHandler}
 */
public class ParsedGeoRSSFeed {

	private static final String DT = "ParsedGeoRSSFeed";
	
	public String title;
	public String description;
	public String link;
	/**
	 * The url of the request, not the link!
	 */
	public String url;

	public List<ParsedGeoRSSEntry> entries;

	public ParsedGeoRSSFeed() {
		entries = new LinkedList<ParsedGeoRSSEntry>();
	}

	/**
	 * Object to hold Entry Information of a parsed GeoRSSFeed 
	 * 
	 * @author Mathias Menninghaus
	 * @version 15.10.2009
	 */
	public class ParsedGeoRSSEntry {
		public String title;
		public String description;
		public String link;
		/**
		 * Will be set to currentTimeMillis by default
		 */
		public Date pubDate = new Date(System.currentTimeMillis());
		public int latE6;
		public int lonE6;

		public ParsedGeoRSSEntry() {
			ParsedGeoRSSFeed.this.entries.add(this);
		}
	}

}
