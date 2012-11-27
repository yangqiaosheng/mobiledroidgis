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
