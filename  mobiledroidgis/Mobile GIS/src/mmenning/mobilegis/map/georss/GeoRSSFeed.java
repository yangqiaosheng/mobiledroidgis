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