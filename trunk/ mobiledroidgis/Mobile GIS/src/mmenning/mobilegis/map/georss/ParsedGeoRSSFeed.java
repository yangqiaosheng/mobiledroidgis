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
