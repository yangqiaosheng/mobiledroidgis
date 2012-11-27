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
package mmenning.mobilegis.map.wms;

import android.graphics.Point;

import com.google.android.maps.GeoPoint;
import com.google.android.maps.Projection;

/**
 * Util Class to supply often used methods and constants.
 * 
 * @author Mathias Menninghaus
 * @version 23.10.2009
 * 
 */
public class WMSUtils {

	private static final String DT = "WMSUtils";

	/**
	 * Ideal EPSG Code to match the maps Projection
	 */
	public static final String idealSRS = "EPSG:900913";

	/**
	 * Recommended EPSG Code to match the maps Projection
	 */
	public static final String recommendedSRS = "EPSG:4326";

	/**
	 * width of the WMS Parts
	 */
	public static final int WIDTH = 160;
	/**
	 * height of the WMS Parts
	 */
	public static final int HEIGHT = 160;

	public static final int HALFWIDTH = WIDTH / 2;
	public static final int HALFHEIGHT = HEIGHT / 2;

	public static final int LOWERLEFT = 0;
	public static final int UPPERRIGHT = 1;

	public static final int X = 0;
	public static final int Y = 1;

	/**
	 * Required WMS Version
	 */
	public static final String VERSION = "1.1.1";

	/**
	 * Encoding for XML
	 */
	public static final String ENCODING = "ISO-8859-1";

	/**
	 * Maximum of used Threads in a WMSLoader
	 */
	public static final int MAXTHREADSPerLoader = 2;

	/**
	 * Maximum cached Bitmaps in a WMSLoader
	 */
	public static final int MAXStoredBitmapsPerLoader = 60;

	/**
	 * Goal for cache size after cleaning up the Cache in a WMSLoader
	 */
	public static final int AVERAGEStoredBitmapsPerLoader = 40;

	private static String setLastSignMark(String s) {
		if (s == null)
			return "";
		if (s.endsWith("?"))
			return s;
		if (s.endsWith("&"))
			return s;
		return s.concat("?");
	}

	/**
	 * Append the getCapabilities request to an URL.
	 * 
	 * @param baseURL
	 *            URL without a getCapabilities Request. If the getCapabilities
	 *            request should be embedded in a already existing request the
	 *            URL should end with &
	 * @return baseURL + getCapabilities
	 */
	public static String generateGetCapabilitiesURL(String baseURL) {
		StringBuffer buf = new StringBuffer();
		buf.append(setLastSignMark(baseURL));
		buf.append("VERSION=" + VERSION);
		buf.append("&REQUEST=GetCapabilities");
		buf.append("&SERVICE=WMS");
		return buf.toString();
	}

	/**
	 * Append the basic getMap request (without BoundingBox) to a URL. The
	 * parameters will not be controlled if they support the OGC specifications
	 * 
	 * @param baseURL
	 *            URL to be appended
	 * @param layers
	 *            String Array where all Layers to be displayed are listed. May
	 *            cause invalid request if not the right layers are given (see
	 *            the OGC specification)
	 * @param srs
	 *            the SRS (EPSG-Code) for the getMap request, must be correct
	 *            for the layers and Web Map Service.
	 * @return getMap URL without BoundingBox request.
	 */
	public static String generateGetMapBaseURL(String baseURL, String[] layers,
			String srs) {

		StringBuffer buf = new StringBuffer();
		buf.append(setLastSignMark(baseURL));
		buf.append("TRANSPARENT=true");
		buf.append("&FORMAT=image/png");
		buf.append("&SERVICE=WMS");
		buf.append("&REQUEST=GetMap");
		buf.append("&STYLES=");
		buf.append("&VERSION=" + VERSION);
		buf.append("&EXCEPTIONS=application/vnd.ogc.se_inimage");
		buf.append("&SRS=" + srs);
		buf.append("&WIDTH=" + WIDTH);
		buf.append("&HEIGHT=" + HEIGHT);
		buf.append("&LAYERS=");
		for (String s : layers) {
			buf.append(s + ",");
		}
		return buf.substring(0, buf.length() - 1);

	}

	/**
	 * Generate an URL for a GetMap request.
	 * 
	 * @param getMapBaseURL
	 *            base GetMap request without Bounding Box
	 * @param ll
	 *            LowerLeft Corner
	 * @param ur
	 *            UpperRight Corner
	 * @return the complete GetMapURL to start a GetMAp request.
	 */
	public static String generateGetMapURL(String getMapBaseURL, GeoPoint ll,
			GeoPoint ur) {
		return getMapBaseURL + "&BBOX=" + longitude(ll) + "," + latitude(ll)
				+ "," + longitude(ur) + "," + latitude(ur);
	}

	/**
	 * Calculates the BoundingBox for the given top-left screen coordinate and
	 * the given Projection. The BoundingBox will have WIDTH and HEIGHT in
	 * screen pixels.
	 * 
	 * @param left
	 *            left border in screen pixels
	 * @param top
	 *            top border in screen pixels
	 * @param p
	 *            Projection to Project from ScreenPixels in GeoPoints
	 * @return a GeoPoint[] with entries LOWERLEFT and UPPERRIGHT
	 */
	public static GeoPoint[] corners(int left, int top, Projection p) {
		GeoPoint[] ret = new GeoPoint[2];
		ret[LOWERLEFT] = p.fromPixels(left, top + HEIGHT);
		ret[UPPERRIGHT] = p.fromPixels(left + WIDTH, top);
		return ret;
	}

	/**
	 * Returns the longitude of the given GeoPoint as floating String.
	 * 
	 * @param p
	 *            GeoPoint to extract the longitude from
	 * @return longitude as floating string
	 */
	public static String longitude(GeoPoint p) {
		return Float.toString((float) p.getLongitudeE6() / (float) 1E6);
	}

	/**
	 * Returns the latitude of the given GeoPoint as floating String.
	 * 
	 * @param p
	 *            GeoPoint to extract the latitude from
	 * @return latitude as floating string
	 */
	public static String latitude(GeoPoint p) {
		return Float.toString((float) p.getLatitudeE6() / (float) 1E6);
	}

	/**
	 * Calculate the Pixel Distance of to GeoPoints using the given Projection.
	 * (a-b)
	 * 
	 * @param a
	 *            first GeoPoint
	 * @param b
	 *            second GeoPoint
	 * @param p
	 *            Projection as basis for the calculation
	 * @return pixel Distance in X(a.x-b.x) and Y(a.y-b.y) Direction.
	 */
	public static int[] pixelDistance(GeoPoint a, GeoPoint b, Projection p) {
		int[] ret = new int[2];
		Point apx = new Point();
		Point bpx = new Point();
		p.toPixels(a, apx);
		p.toPixels(b, bpx);
		ret[X] = apx.x - bpx.x;
		ret[Y] = apx.y - bpx.y;
		return ret;
	}
}
