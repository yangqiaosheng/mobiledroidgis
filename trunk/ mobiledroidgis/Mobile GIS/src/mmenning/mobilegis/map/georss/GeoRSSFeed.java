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

/**
 * Holds Data of a GeoRSS Feed to be stored an queried from the GeoRSSDB.
 * 
 * @author Mathias Menninghaus
 * @version 15.10.2009
 * 
 */
public class GeoRSSFeed implements Comparable<GeoRSSFeed> {

	private static final String DT = "GeoRSSFeed";
	
	/*
	 * from xml
	 */
	public String title;
	public String description;
	/**
	 * The url of the request, not the link!
	 */
	public String url;
	/**
	 * Link to the feed
	 */
	public String link;
	/*
	 * for and from database
	 */
	public boolean visible;
	public int color;
	public int id;

	/**
	 * Compared by the url
	 */
	public int compareTo(GeoRSSFeed another) {
		return url.compareTo(another.url);

	}
}