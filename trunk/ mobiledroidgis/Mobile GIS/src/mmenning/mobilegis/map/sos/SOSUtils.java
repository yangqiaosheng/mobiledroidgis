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
package mmenning.mobilegis.map.sos;

import java.text.SimpleDateFormat;
import java.util.Date;

import android.util.Log;

/**
 * Util class to manage basic methods an constants for the whole SOS
 * Implementation.
 * 
 * @author Mathias Menninghaus
 * @version 30.11.2009
 * 
 */
public class SOSUtils {

	private static final String DT = "SOSUtils";

	/**
	 * Default color for a SOS is WHITE
	 */
	public static final int defaultColor = 0xFFFFFFFF;

	public static final String owsNamespace = "http://www.opengis.net/ows/1.1";
	public static final String gmlNamespace = "http://www.opengis.net/gml";
	public static final String ogcNamespace = "http://www.opengis.net/ogc";
	public static final String omNamespace = "http://www.opengis.net/om/1.0";
	public static final String xsiNamespace = "http://www.w3.org/2001/XMLSchema-instance";
	public static final String sosNamespace = "http://www.opengis.net/sos/1.0";

	public static final String xlinkNamespace = "http://www.w3.org/1999/xlink";
	public static final String sweNamespace = "http://www.opengis.net/swe/1.0.1";
	public static final String saNamespace = "http://www.opengis.net/sampling/1.0";

	public static final String sosSchema = "http://www.opengis.net/sos/1.0";
	public static final String GetObservationSchema = "http://schemas.opengis.net/sos/1.0.0/sosGetObservation.xsd";

	public static final SimpleDateFormat sosDisplayFormat = new SimpleDateFormat(
			"dd.MM.yyyy-HH:mm");

	/**
	 * Currently supported version is 1.0.0
	 */
	public static final String version = "1.0.0";

	/**
	 * EPSG 4326
	 */
	public static final String srsName = "EPSG:4326";

	public static final long defaultDataRange = 60000;
	/**
	 * Encoding for a xml
	 */
	public final static String ENCODING = "ISO-8859-1";

	private static final String GetCapabilitiesSuffix = "request=GetCapabilities&service=SOS&VERSION="
			+ version;

	private static final String GetObservationBase = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>"
			+ " <GetObservation"
			+ " xmlns=\"http://www.opengis.net/sos/1.0\""
			+ " xmlns:ows=" + "\""
			+ owsNamespace
			+ "\""
			+ " xmlns:gml="
			+ "\""
			+ gmlNamespace
			+ "\""
			+ " xmlns:ogc="
			+ "\""
			+ ogcNamespace
			+ "\""
			+ " xmlns:om="
			+ "\""
			+ omNamespace
			+ "\""
			+ " xmlns:xsi="
			+ "\""
			+ xsiNamespace
			+ "\""
			+ " xsi:schemaLocation=\""
			+ sosSchema
			+ " "
			+ GetObservationSchema
			+ "\""
			+ " service=\"SOS\" version=\""
			+ version
			+ "\" srsName=\"urn:ogc:def:crs:"
			+ srsName
			+ "\">"
			+ " <offering>%s</offering>"
			+ " <eventTime>"
			+ " <ogc:TM_During>"
			+ " <ogc:PropertyName>urn:ogc:data:time:iso8601</ogc:PropertyName>"
			+ " <gml:TimePeriod>"
			+ " <gml:beginPosition>%s</gml:beginPosition>"
			+ " <gml:endPosition>%s</gml:endPosition>"
			+ " </gml:TimePeriod>"
			+ " </ogc:TM_During>"
			+ " </eventTime>"
			+ " <observedProperty>%s</observedProperty>"
			+ " <featureOfInterest>"
			+ " <ObjectID>%s</ObjectID>"
			+ " </featureOfInterest>"
			+ " <responseFormat>text/xml;subtype=&quot;om/1.0.0&quot;</responseFormat>"
			+ " </GetObservation>";

	/**
	 * Date Format used in SOSs
	 */
	public static final SimpleDateFormat sosDateFormat = new SimpleDateFormat(
			"yyyy-MM-dd'T'HH:mm:ssZ");

	public static final int STARTDATE = 0;

	public static final int ENDDATE = 1;

	/**
	 * Generates start (at STARTDATE) and end (at ENDDATE) Date to clip Data in
	 * the SOSDB.
	 * 
	 * @param storingRange
	 *            range of stored Data in milliseconds
	 * @return
	 */
	public static Date[] calcClippingRange(long storingRange) {
		Date[] clip = new Date[2];
		clip[STARTDATE] = new Date(System.currentTimeMillis() - storingRange);
		clip[ENDDATE] = new Date(clip[STARTDATE].getTime() + storingRange);
		return clip;
	}

	/**
	 * Generates start (at STARTDATE) and end (at ENDDATE) Date for a
	 * GetObservation request.
	 * 
	 * @param requestRange
	 *            range of the request in milliseconds
	 * @param youngest
	 *            Time of the youngest database entry as Date
	 * @return
	 */
	public static Date[] calcRequestRange(long requestRange, Date youngest) {
		Date[] request = new Date[2];
		long start = System.currentTimeMillis() - requestRange;
		request[STARTDATE] = new Date(youngest.getTime() > start ? youngest
				.getTime() : start);
		request[ENDDATE] = new Date(start + requestRange);
		return request;
	}

	/**
	 * Generate a GetCapabilities Request URL
	 * 
	 * @param baseURL
	 *            base for the URL which will be appended with the
	 *            GetCapabilities request.
	 * @return GetCapabiliteis Request URL
	 */
	public static String generateGetCapabilitiesURL(String baseURL) {
		return setLastSignMark(baseURL) + GetCapabilitiesSuffix;
	}
	/**
	 * Generate GetObservation Request xml.
	 * 
	 * @param offering
	 *            offering Identifier requested ObservationOffering
	 * @param feature
	 *            Feature
	 * @param property
	 *            Property
	 * @param startTime
	 *            start of the request as Date, must be smaller as endTime
	 * @param endTime
	 *            end of the request as Date, must be greater as startTime
	 * @return GetObservation Request xml as String.
	 */
	public static String generateGetObservationRequest(String offering,
			String feature, String property, Date startTime, Date endTime) {

		return String.format(GetObservationBase, offering, sosDateFormat
				.format(startTime), sosDateFormat.format(endTime), property,
				feature);

	}

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

	private static String setLastSignMark(String s) {
		if (s == null)
			return "";
		if (s.endsWith("?"))
			return s;
		if (s.endsWith("&"))
			return s;
		return s.concat("?");
	}
}
