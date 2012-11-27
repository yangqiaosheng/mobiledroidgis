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

import java.io.IOException;
import java.net.URL;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import mmenning.mobilegis.R;
import mmenning.mobilegis.map.georss.ParsedGeoRSSFeed.ParsedGeoRSSEntry;

import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

/**
 * Manages the Synchronization of all GeoRSSFeeds in the GeoRSSDB.
 * 
 * @author Mathias Menninghaus
 * @version 15.10.2009
 */
/*
 * TODO use this class to implement an GeoRSS Synchronization Service
 */
public class GeoRSSManager {

	private static final String DT = "GeoRSSManager";

	private Context context;
	private GeoRSSDB db;

	public GeoRSSManager(Context ctx) {
		this.context = ctx;
		this.db = new GeoRSSDB(this.context);
	}

	/**
	 * Add a new GeoRSSFeed and its entries to the database. While this
	 * operation works, nobody should use a GeoRSSDB.
	 * 
	 * @param url
	 *            requested url
	 * @throws ParserConfigurationException
	 * @throws SAXException
	 */
	public void addGeoRSS(String url) throws IOException,
			ParserConfigurationException, SAXException {

		try {
			db.open();

			syncWithSingle(url);

			SharedPreferences prefs = PreferenceManager
					.getDefaultSharedPreferences(context);

			db.deleteOldest(Integer.valueOf(prefs.getString(context
					.getString(R.string.maxentries), "1000")));

			db.close();
		} catch (IOException e) {
			db.close();
			throw e;
		} catch (ParserConfigurationException e) {
			db.close();
			throw e;
		} catch (SAXException e) {
			db.close();
			throw e;
		}
	}

	/**
	 * Synchronize all feeds stored in the database and write again to the
	 * database. So while this works, nobody should use a GeoRSSDB. It assumes
	 * that every feed in the GeoRSSDB is a valid source.
	 * 
	 * @throws IOException
	 * @throws ParserConfigurationException
	 * @throws SAXException
	 */
	public void sync() throws IOException, ParserConfigurationException,
			SAXException {

		try {
			db.open();
			String[] urls = db.getAllGeoRSSurls();
			for (String url : urls) {
				syncWithSingle(url);
			}

			SharedPreferences prefs = PreferenceManager
					.getDefaultSharedPreferences(context);

			db.deleteOldest(Integer.valueOf(prefs.getString(context
					.getString(R.string.maxentries), "1000")));

			db.close();
		} catch (IOException e) {
			db.close();
			throw e;
		} catch (ParserConfigurationException e) {
			db.close();
			throw e;
		} catch (SAXException e) {
			db.close();
			throw e;
		}

	}

	private void syncWithSingle(String feedUrl) throws IOException,
			ParserConfigurationException, SAXException {

		URL url = new URL(feedUrl);

		/* Get a SAXParser from the SAXPArserFactory. */
		SAXParserFactory spf = SAXParserFactory.newInstance();
		SAXParser sp = spf.newSAXParser();

		/* Get the XMLReader of the SAXParser we created. *///
		XMLReader xr = sp.getXMLReader();
		/* Create a new ContentHandler and apply it to the XML-Reader */
		GeoRSSHandler handler = new GeoRSSHandler();
		xr.setContentHandler(handler);

		/* Parse the xml-data from our URL. */
		InputSource in = new InputSource(url.openStream());
		in.setEncoding(GeoRSSUtils.ENCODING);

		xr.parse(in);

		ParsedGeoRSSFeed parsedFeed = handler.getParsedData();

		GeoRSSFeed feed = new GeoRSSFeed();
		feed.description = parsedFeed.description;
		feed.link = parsedFeed.link;
		feed.url = feedUrl;
		feed.title = parsedFeed.title;
		feed.color = GeoRSSUtils.defaultColor;
		int feedId = db.addGeoRSS(feed);
		GeoRSSEntry entry;
		for (ParsedGeoRSSEntry parsedEntry : parsedFeed.entries) {
			entry = new GeoRSSEntry();
			entry.description = parsedEntry.description;
			entry.title = parsedEntry.title;
			entry.link = parsedEntry.link;
			entry.latE6 = parsedEntry.latE6;
			entry.lonE6 = parsedEntry.lonE6;
			entry.time = parsedEntry.pubDate;
			entry.geoRSSID = feedId;
			db.addEntry(entry);
		}

	}
}
