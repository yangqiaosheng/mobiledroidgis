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

import java.text.SimpleDateFormat;
import java.util.Locale;

import android.util.Log;

/**
 * Util class for GeoRSS feeds.
 * 
 * @author Mathias Menninghaus
 * @version 15.10.2009
 * 
 */
public class GeoRSSUtils {

	private static final String DT = "GeoRSSUtils";
	/**
	 * DateFormat to format Strings from a GeoRSSFeed
	 */
	public static final SimpleDateFormat rssTimeFormat = new SimpleDateFormat(
			"EEE, dd MMM yyyy HH:mm:ss Z", Locale.US);
	/**
	 * DateFormat to display GeoRSSEntry.time
	 */
	public static final SimpleDateFormat displayTimeFormat = new SimpleDateFormat(
			"EE dd MMM yyy HH:mm:ss Z");

	/**
	 * Default Color for GeoRSSFedd
	 */
	public final static int defaultColor = (0xFFFFFFFF);

	/**
	 * Encoding for a RSS xml
	 */
	public final static String ENCODING = "ISO-8859-1";

	/**
	 * Formats a String which contains the latitude or longitude as Float to a
	 * int by multiyplying it with 1E6.
	 * 
	 * @param s
	 *            String to be formatted
	 * @return formatted String.
	 */
	public static int stringToE6(String s) {

		int ret = 0;
		try {
			ret = (int) (Float.valueOf(s) * 1E6f);
		} catch (NumberFormatException e) {
			Log.w(DT, e);
		}

		return ret;

	}
}
