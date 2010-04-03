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

import java.text.ParseException;

import mmenning.mobilegis.map.georss.ParsedGeoRSSFeed.ParsedGeoRSSEntry;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import android.util.Log;

/**
 * Simple Default Handler to manage SAX Parsing of a GeoRSS Feed. Requires RSS
 * 2.0 Feeds with geo data. Available namespaces for geo data: <br/>
 * 
 * {@link http://www.w3.org/2003/01/geo/wgs84_pos#} <br/>
 * (latitude, longitude, lat/long | location, point and altitude not supported) <br/>
 * 
 * {@link http://www.georss.org/georss} <br/>
 * (only points in GeoRSS Simple supported)<br/>
 * 
 * georss including gml :<br/>
 * 
 * {@link http://www.opengis.net/gml}<br/>
 * (only points in itmes at this time)<br/>
 * 
 * @author Mathias Menninghaus
 * @version 10.10.2009
 */
public class GeoRSSHandler extends DefaultHandler {

	private static final String DT ="GeoRSSHandler";
	
	private static final String W3GeoNamespace = "http://www.w3.org/2003/01/geo/wgs84_pos#";
	private static final String GeoRSSNamespace = "http://www.georss.org/georss";
	private static final String GMLNamespace = "http://www.opengis.net/gml";

	/*
	 * Tags for GeoRSS Namespace
	 */
	private static final String GeoRSS_point = "point";
	private static final String GeoRSS_where = "where";

	/*
	 * Tags for W3Geo Namespace
	 */
	private static final String W3Geolat = "lat";
	private static final String W3Geolong = "long";
	private static final String W3Geolatlong = "lat_lon";

	/*
	 * Tags for gml Namespace
	 */
	private static final String GML_Point = "Point";
	private static final String GML_pos = "pos";

	/*
	 * RSS 2.0 Tags
	 */
	private static final String item = "item";
	private static final String title = "title";
	private static final String description = "description";
	private static final String link = "link";
	private static final String pubDate = "pubDate";

	/*
	 * tag - flag
	 */
	private boolean in_point;
	private boolean in_where;
	private boolean in_lat;
	private boolean in_long;
	private boolean in_lat_lon;
	private boolean in_Point;
	private boolean in_pos;

	private boolean in_title;
	private boolean in_description;
	private boolean in_link;
	private boolean in_pubDate;

	private boolean in_item;

	/*
	 * stored data to output
	 */
	private ParsedGeoRSSFeed parsedFeed;
	private ParsedGeoRSSEntry actEntry;

	private StringBuffer charBuffer;
	
	public GeoRSSHandler(){
		parsedFeed=new ParsedGeoRSSFeed();
	}
	
	public ParsedGeoRSSFeed getParsedData(){
		return parsedFeed;
	}
	
	@Override
	public void startElement(String uri, String localName, String qName,
			Attributes attributes) throws SAXException {

		if (in_item) {
			/*
			 * first check for geodata
			 */
			/*
			 * GeoRSS GML
			 */
			if (in_where) {
				if (uri.equals(GMLNamespace)) {
					if (in_Point) {
						if (localName.equals(GML_pos)) {
							charBuffer= new StringBuffer();
							in_pos = true;
						}
					} else if (localName.equals(GML_Point)) {
						charBuffer = new StringBuffer();
						in_point = true;
					}
				}
				/*
				 * W3Geo
				 */
			} else if (uri.equals(W3GeoNamespace)) {
				if (localName.equals(W3Geolat)) {
					charBuffer = new StringBuffer();
					in_lat = true;
				} else if (localName.equals(W3Geolong)) {
					charBuffer = new StringBuffer();
					in_long = true;
				} else if (localName.equals(W3Geolatlong)) {
					charBuffer = new StringBuffer();
					in_lat_lon = true;
				}
				/*
				 * GeoRSS Simple
				 */
			} else if (uri.equals(GeoRSSNamespace)) {
				if (localName.equals(GeoRSS_where)) {
					in_where = true;
				} else if (localName.equals(GeoRSS_point)) {
					in_point = true;
				}
			}
			/*
			 * now check out rss 2.0 item
			 */
			else if (localName.equals(title)) {
				charBuffer = new StringBuffer();
				in_title = true;
			} else if (localName.equals(description)) {
				charBuffer = new StringBuffer();
				in_description = true;
			} else if (localName.equals(link)) {
				charBuffer = new StringBuffer();
				in_link = true;
			} else if (localName.equals(pubDate)) {
				charBuffer = new StringBuffer();
				in_pubDate = true;
			}

		} else {
			if (localName.equals(item)) {
				in_item = true;
				actEntry = parsedFeed.new ParsedGeoRSSEntry();
			} else if (localName.equals(title)) {
				charBuffer = new StringBuffer();
				in_title = true;
			} else if (localName.equals(description)) {
				charBuffer = new StringBuffer();
				in_description = true;
			} else if (localName.equals(link)) {
				charBuffer = new StringBuffer();
				in_link = true;
			}
		}
	}

	@Override
	public void characters(char[] ch, int start, int length)
			throws SAXException {
			
		if(in_item){
			if(in_pos){
				charBuffer.append(ch, start, length);
			}else if(in_point){
				charBuffer.append(ch, start, length);
			}else if(in_lat){
				charBuffer.append(ch, start, length);
			}else if(in_long){
				charBuffer.append(ch, start, length);
			}else if(in_lat_lon){
				charBuffer.append(ch, start, length);
			}else if(in_title){
				charBuffer.append(ch, start, length);
			}else if(in_description){
				charBuffer.append(ch, start, length);
			}else if(in_link){
				charBuffer.append(ch, start, length);
			}else if(in_pubDate){
				charBuffer.append(ch, start, length);
			}
		}else{
			if(in_title){
				charBuffer.append(ch, start, length);
			}else if(in_description){
				charBuffer.append(ch, start, length);
			}else if(in_link){
				charBuffer.append(ch, start, length);
			}
		}
	}

	@Override
	public void endElement(String uri, String localName, String qName)
			throws SAXException {

		if (in_item) {
			/*
			 * first check for geodata
			 */
			/*
			 * GeoRSS GML
			 */
			if (in_where) {
				if (uri.equals(GMLNamespace)) {
					if (in_Point) {
						if (in_pos && localName.equals(GML_pos)) {
							
							String[] pos = charBuffer.toString().split(" ");
							actEntry.latE6 = GeoRSSUtils.stringToE6(pos[1]);
							actEntry.lonE6 = GeoRSSUtils.stringToE6(pos[0]);
							
							in_pos = false;
						} else if (localName.equals(GML_Point)) {
							String[] pos = charBuffer.toString().split(" ");
							actEntry.latE6 = GeoRSSUtils.stringToE6(pos[1]);
							actEntry.lonE6 = GeoRSSUtils.stringToE6(pos[0]);
							in_point = false;
						}
					}
				} else if (uri.equals(GeoRSSNamespace)
						&& localName.equals(GeoRSS_where)) {
					in_where = false;
				}
				/*
				 * W3Geo
				 */
			} else if (uri.equals(W3GeoNamespace)) {
				if (in_lat && localName.equals(W3Geolat)) {
					actEntry.latE6 = GeoRSSUtils.stringToE6(charBuffer.toString());
					in_lat = false;
				} else if (in_long && localName.equals(W3Geolong)) {
					actEntry.lonE6 = GeoRSSUtils.stringToE6(charBuffer.toString());
					in_long = false;
				} else if (in_lat_lon && localName.equals(W3Geolatlong)) {
					String[] pos = charBuffer.toString().split(",");
					actEntry.latE6 = GeoRSSUtils.stringToE6(pos[0]);
					actEntry.lonE6 = GeoRSSUtils.stringToE6(pos[1]);
					in_lat_lon = false;
				}
				/*
				 * GeoRSS Simple
				 */
			} else if (in_point) {
				if (uri.equals(GeoRSSNamespace)
						&& localName.equals(GeoRSS_point)) {
					in_point = false;
				}
			}
			/*
			 * now check out rss 2.0 item
			 */
			if (in_title && localName.equals(title)) {
				actEntry.title=charBuffer.toString();
				in_title = false;
			} else if (in_description && localName.equals(description)) {
				actEntry.description=charBuffer.toString();
				in_description = false;
			} else if (in_link && localName.equals(link)) {
				actEntry.link=charBuffer.toString();
				in_link = false;
			} else if (in_pubDate && localName.equals(pubDate)) {
				try {
					actEntry.pubDate=GeoRSSUtils.rssTimeFormat.parse(charBuffer.toString());
				} catch (ParseException e) {
					Log.w(DT, e);
				}
				in_pubDate = false;
			} else if (localName.equals(item)) {
				in_item = false;
			}
		} else {
			if (in_title && localName.equals(title)) {
				parsedFeed.title=charBuffer.toString();
				in_title = false;
			} else if (in_description && localName.equals(description)) {
				parsedFeed.description=charBuffer.toString();
				in_description = false;
			} else if (in_link && localName.equals(link)) {
				parsedFeed.link=charBuffer.toString();
				in_link = false;
			}
		}
	}
}
