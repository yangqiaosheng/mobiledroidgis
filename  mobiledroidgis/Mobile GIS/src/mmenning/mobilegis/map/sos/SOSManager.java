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
package mmenning.mobilegis.map.sos;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.net.ConnectException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.net.UnknownHostException;
import java.util.Date;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import mmenning.mobilegis.R;

import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.os.Handler;
import android.os.Message;
import android.preference.PreferenceManager;
import android.util.Log;

/**
 * Class to manage parsing and storing SensorObservationService Capabilities and
 * Observations. In this context, "update" means only requesting data from the
 * last known stored point or startTime to the endTime and storing it to the
 * database. "sync" means requesting all data between startTime and endTime and
 * storing it to the database. After every call of update and sync, the stored data over
 * capacity will be deleted.
 * 
 * @author Mathias Menninghaus
 * @version 15.11.2009
 * 
 */
public class SOSManager {

	private static final String DT = "SOSManager";

	private SOSDB db;
	private Handler handler;
	private Context context;

	private long requestRange;

	private long storeRange;
	public static final int START = 10;

	/*
	 * To manage the handler.
	 */
	public static final int SUCCESS = 11;
	public static final int NEXTELEMENT = 12;
	public static final int SAXEX = 0;
	public static final int IOEX = 1;
	public static final int PARSCONFIGEX = 2;
	public static final int MFURLEX = 4;
	public static final int CONNECTEX = 5;
	public static final int UNKNOWNHEX = 6;

	/**
	 * Instantiate a new SOSManager.
	 * 
	 * @param ctx
	 *            Context(Activity)
	 * @param handler
	 *            Handler to manage update and sync methods which may run in
	 *            seperate Threads.
	 */
	public SOSManager(Context ctx, Handler handler) {

		this.handler = handler;
		this.context = ctx;
		db = new SOSDB(context);
		SharedPreferences sharedPreferences = PreferenceManager
				.getDefaultSharedPreferences(context);

		final String requestRangeString = context
				.getString(R.string.requestperiod);
		final String storeRangeString = context.getString(R.string.storeperiod);

		requestRange = Long.parseLong(sharedPreferences.getString(
				requestRangeString, "" + SOSUtils.defaultDataRange));

		storeRange = Long.parseLong(sharedPreferences.getString(
				storeRangeString, "" + SOSUtils.defaultDataRange));
		sharedPreferences
				.registerOnSharedPreferenceChangeListener(new OnSharedPreferenceChangeListener() {

					
					public void onSharedPreferenceChanged(
							SharedPreferences sharedPreferences, String key) {
						if (key.equals(requestRangeString)) {
							requestRange = Long.parseLong(sharedPreferences
									.getString(requestRangeString, ""
											+ SOSUtils.defaultDataRange));
						} else if (key.equals(storeRangeString)) {
							storeRange = Long.parseLong(sharedPreferences
									.getString(storeRangeString, ""
											+ SOSUtils.defaultDataRange));
						}

					}

				});
	}

	/**
	 * Make a getCapabilities request for the given sosURL and store the
	 * response to the database.
	 * 
	 * @param sosUrl
	 *            url of the requested SensoeObservationService
	 */
	public void addSOS(String sosUrl) {
		try {

			Message msg = new Message();
			msg.what = START;
			msg.arg1 = 1;
			handler.sendMessage(msg);

			URL url = new URL(SOSUtils.generateGetCapabilitiesURL(sosUrl));

			/* Get a SAXParser from the SAXPArserFactory. */
			SAXParserFactory spf = SAXParserFactory.newInstance();
			SAXParser sp = spf.newSAXParser();

			/* Get the XMLReader of the SAXParser we created. *///
			XMLReader xr = sp.getXMLReader();
			/* Create a new ContentHandler and apply it to the XML-Reader */
			GetCapabilitiesHandler xmlhandler = new GetCapabilitiesHandler();
			xr.setContentHandler(xmlhandler);

			/* Parse the xml-data from our URL. */
			InputSource in = new InputSource(url.openStream());
			in.setEncoding(SOSUtils.ENCODING);

			xr.parse(in);

			ParsedSOSCapabilities parsedCaps = xmlhandler.getParsedData();

			db.open();
			db.addSOS(parsedCaps);
			db.close();

			handler.sendEmptyMessage(SUCCESS);
		} catch (ConnectException e) {
			db.close();
			Log.w(DT, e);
			this.handler.sendEmptyMessage(CONNECTEX);
		} catch (UnknownHostException e) {
			db.close();
			Log.w(DT, e);
			this.handler.sendEmptyMessage(UNKNOWNHEX);
		} catch (MalformedURLException e) {
			db.close();
			Log.w(DT, e);
			this.handler.sendEmptyMessage(MFURLEX);
		} catch (IOException e) {
			db.close();
			Log.w(DT, "IOException", e);
			this.handler.sendEmptyMessage(IOEX);
		} catch (ParserConfigurationException e) {
			db.close();
			Log.w(DT, "ParserConfiguration", e);
			this.handler.sendEmptyMessage(IOEX);
		} catch (SAXException e) {
			db.close();
			Log.w(DT, "SAXException", e);
			this.handler.sendEmptyMessage(SAXEX);
		}
	}

	/**
	 * Make a GetObservation Request for one Measurement in the SOSDB and store
	 * it. No other connection to the SOSDB should be open while this method
	 * works!
	 * 
	 * @param offeringID
	 *            database id of the Offering
	 * @param featureID
	 *            database id of the requested Feature
	 * @param propertyID
	 *            database id ot the requested Property
	 * @param startTime
	 *            first Date of the request
	 * @param endTime
	 *            last Date of the request
	 * @throws IOException
	 * @throws ParserConfigurationException
	 * @throws SAXException
	 */
	public void syncMeasurement(int offeringID, int featureID, int propertyID,
			Date startTime, Date endTime) {
		try {
			Message msg = new Message();
			msg.what = START;
			msg.arg1 = 1;
			handler.sendMessage(msg);

			db.open();

			OfferingData offeringData = db.getOffering(offeringID);
			String feature = db.getFeature(featureID);
			String property = db.getProperty(propertyID);

			String getObservationRequest = SOSUtils
					.generateGetObservationRequest(offeringData.offering,
							feature, property, startTime, endTime);

			ParsedObservationData parsedObs = this.makeGetObservationRequest(db
					.getSOS(offeringData.sosID).getObservationPost,
					getObservationRequest);

			clip();
			db.addMeasurementData(parsedObs, featureID, propertyID);

			db.close();
			handler.sendEmptyMessage(SUCCESS);
		} catch (ConnectException e) {
			Log.w(DT, e);
			this.handler.sendEmptyMessage(CONNECTEX);
		} catch (UnknownHostException e) {
			Log.w(DT, e);
			this.handler.sendEmptyMessage(UNKNOWNHEX);
		} catch (MalformedURLException e) {
			Log.w(DT, e);
			this.handler.sendEmptyMessage(MFURLEX);
		} catch (IOException e) {
			Log.w(DT, "IOException", e);
			this.handler.sendEmptyMessage(IOEX);
		} catch (ParserConfigurationException e) {
			Log.w(DT, "ParserConfiguration", e);
			this.handler.sendEmptyMessage(IOEX);
		} catch (SAXException e) {
			Log.w(DT, "SAXException", e);
			this.handler.sendEmptyMessage(SAXEX);
		}
	}

	/**
	 * Make a GetObservation request for all Features and the selected Property
	 * of the selected Offering in a SensorObservationService and store the
	 * Response to the SOSDB. No other connection to the SOSDB should be open
	 * while this method works!
	 * 
	 * @param sosID
	 *            databsase id of the sos
	 * @throws IOException
	 * @throws ParserConfigurationException
	 * @throws SAXException
	 */
	public void updateAllCurrentMeasurements(int sosID) {

		try {

			db.open();

			SOSData sosData = db.getSOS(sosID);
			String getObservationPost = sosData.getObservationPost;
			OfferingData offeringData = db
					.getOffering(sosData.selectedOffering);
			String offering = offeringData.offering;
			String[] features = db.getFeatures(offeringData.id);
			int[] featureIDs = db.getFeatureIDs(offeringData.id);
			int propertyID = offeringData.selectedProperty;
			String property = db.getProperty(propertyID);

			Message msg = new Message();
			msg.what = START;
			msg.arg1 = features.length;
			handler.sendMessage(msg);

			for (int i = 0; i < features.length; i++) {

				this.update(offering, property, features[i], propertyID,
						featureIDs[i], getObservationPost);

				handler.sendEmptyMessage(NEXTELEMENT);
			}
			clip();
			db.close();

			handler.sendEmptyMessage(SUCCESS);

		} catch (ConnectException e) {
			db.close();
			Log.w(DT, e);
			this.handler.sendEmptyMessage(CONNECTEX);
		} catch (UnknownHostException e) {
			db.close();
			Log.w(DT, e);
			this.handler.sendEmptyMessage(UNKNOWNHEX);
		} catch (MalformedURLException e) {
			db.close();
			Log.w(DT, e);
			this.handler.sendEmptyMessage(MFURLEX);
		} catch (IOException e) {
			db.close();
			Log.w(DT, "IOException", e);
			this.handler.sendEmptyMessage(IOEX);
		} catch (ParserConfigurationException e) {
			db.close();
			Log.w(DT, "ParserConfiguration", e);
			this.handler.sendEmptyMessage(IOEX);
		} catch (SAXException e) {
			db.close();
			Log.w(DT, "SAXException", e);
			this.handler.sendEmptyMessage(SAXEX);
		}
	}

	/**
	 * Make a GetObservation request for all Features and the selected Property
	 * of the selected Offering in a SensorObservationService which Measurements
	 * are notEmpty and store the Response to the SOSDB. No other connection to
	 * the SOSDB should be open while this method works!
	 * 
	 * @param sosID
	 *            databsase id of the sos
	 * @throws IOException
	 * @throws ParserConfigurationException
	 * @throws SAXException
	 */
	public void updateAvailableCurrentMeasurements(int sosID) {

		try {
			db.open();
			SOSData sosData = db.getSOS(sosID);
			int propertyID = db.getSelectedProperty(sosData.selectedOffering);
			String property = db.getProperty(propertyID);
			MeasurementData[] measurementData = db
					.getNotEmptyMeasurementDataArray(propertyID);
			String offering = db.getOffering(sosData.selectedOffering).offering;

			Message msg = new Message();
			msg.what = START;
			msg.arg1 = measurementData.length;
			handler.sendMessage(msg);

			for (int i = 0; i < measurementData.length; i++) {

				this.update(offering, property, db
						.getFeature(measurementData[i].featureID), propertyID,
						measurementData[i].featureID,
						sosData.getObservationPost);

				handler.sendEmptyMessage(NEXTELEMENT);
			}
			clip();
			db.close();
			handler.sendEmptyMessage(SUCCESS);

		} catch (ConnectException e) {
			db.close();
			Log.w(DT, e);
			this.handler.sendEmptyMessage(CONNECTEX);
		} catch (UnknownHostException e) {
			db.close();
			Log.w(DT, e);
			this.handler.sendEmptyMessage(UNKNOWNHEX);
		} catch (MalformedURLException e) {
			db.close();
			Log.w(DT, e);
			this.handler.sendEmptyMessage(MFURLEX);
		} catch (IOException e) {
			db.close();
			Log.w(DT, "IOException", e);
			this.handler.sendEmptyMessage(IOEX);
		} catch (ParserConfigurationException e) {
			db.close();
			Log.w(DT, "ParserConfiguration", e);
			this.handler.sendEmptyMessage(IOEX);
		} catch (SAXException e) {
			db.close();
			Log.w(DT, "SAXException", e);
			this.handler.sendEmptyMessage(SAXEX);
		}

	}

	/**
	 * Make a GetObservation Request for one Measurement in the SOSDB and store
	 * it. No other connection to the SOSDB should be open while this method
	 * works!
	 * 
	 * @param offeringID
	 *            database id of the Offering
	 * @param featureID
	 *            database id of the requested Feature
	 * @param propertyID
	 *            database id ot the requested Property
	 * @param startTime
	 *            first Date of the request
	 * @param endTime
	 *            last Date of the request
	 * @throws IOException
	 * @throws ParserConfigurationException
	 * @throws SAXException
	 */
	public void updateMeasurement(int offeringID, int featureID,
			int propertyID, Date startTime, Date endTime) {
		try {
			Message msg = new Message();
			msg.what = START;
			msg.arg1 = 1;
			handler.sendMessage(msg);

			db.open();

			OfferingData offeringData = db.getOffering(offeringID);
			String feature = db.getFeature(featureID);
			String property = db.getProperty(propertyID);

			this
					.update(offeringData.offering, property, feature,
							propertyID, featureID,
							db.getSOS(offeringData.sosID).getObservationPost);

			db.close();
			handler.sendEmptyMessage(SUCCESS);
		} catch (ConnectException e) {
			db.close();
			Log.w(DT, e);
			this.handler.sendEmptyMessage(CONNECTEX);
		} catch (UnknownHostException e) {
			db.close();
			Log.w(DT, e);
			this.handler.sendEmptyMessage(UNKNOWNHEX);
		} catch (MalformedURLException e) {
			db.close();
			Log.w(DT, e);
			this.handler.sendEmptyMessage(MFURLEX);
		} catch (IOException e) {
			db.close();
			Log.w(DT, "IOException", e);
			this.handler.sendEmptyMessage(IOEX);
		} catch (ParserConfigurationException e) {
			db.close();
			Log.w(DT, "ParserConfiguration", e);
			this.handler.sendEmptyMessage(IOEX);
		} catch (SAXException e) {
			db.close();
			Log.w(DT, "SAXException", e);
			this.handler.sendEmptyMessage(SAXEX);
		}
	}

	private void clip() {
		Date[] clip = SOSUtils.calcClippingRange(storeRange);
		db.clipAllMeasurementValues(clip[SOSUtils.STARTDATE],
				clip[SOSUtils.ENDDATE]);
	}

	private ParsedObservationData makeGetObservationRequest(
			String getObservationPost, String getObservationRequest)
			throws IOException, ParserConfigurationException, SAXException {

		// Log.d(DT, getObservationRequest);

		URL url = new URL(getObservationPost);
		URLConnection conn = url.openConnection();
		conn.setDoOutput(true);
		conn.setDoInput(true);

		OutputStreamWriter out = new OutputStreamWriter(conn.getOutputStream());

		out.write(getObservationRequest);
		out.flush();
		out.close();
		/* Get a SAXParser from the SAXPArserFactory. */
		SAXParserFactory spf = SAXParserFactory.newInstance();
		SAXParser sp = spf.newSAXParser();

		/* Get the XMLReader of the SAXParser we created. *///
		XMLReader xr = sp.getXMLReader();
		/* Create a new ContentHandler and apply it to the XML-Reader */
		GetObservationHandler xmlhandler = new GetObservationHandler();
		xr.setContentHandler(xmlhandler);

		/* Parse the xml-data from our URL. */
		InputStream input = conn.getInputStream();
		InputSource in = new InputSource(input);
		in.setEncoding(SOSUtils.ENCODING);

		xr.parse(in);

		input.close();

		return xmlhandler.getParsedData();
	}

	private Date[] requestUpdateRange(int propertyID, int featureID) {

		Date youngest = db.getYoungestMeasurementTime(propertyID, featureID);
		return SOSUtils.calcRequestRange(requestRange, youngest);

	}

	private void update(String offering, String property, String feature,
			int propertyID, int featureID, String getObservationPost)
			throws IOException, ParserConfigurationException, SAXException {

		Date[] request = requestUpdateRange(propertyID, featureID);
		String getObservationRequest = SOSUtils.generateGetObservationRequest(
				offering, feature, property, request[SOSUtils.STARTDATE],
				request[SOSUtils.ENDDATE]);

		ParsedObservationData parsedObs = this.makeGetObservationRequest(
				getObservationPost, getObservationRequest);
		clip();
		db.addMeasurementData(parsedObs, featureID, propertyID);

	}
}
