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

import java.text.ParseException;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import android.util.Log;

/**
 * Simple DefaultHandler to read a GetObservation response on a
 * SensorObservationService.
 * 
 * based upon OGC 06-009r6 but not yet full!
 * 
 * @see SOSUtils for used Namespaces
 * 
 * @author Mathias Menninghaus
 * @version 11.11.2009
 * 
 */
public class GetObservationHandler extends DefaultHandler {

	private static final String DT = "GetObservationHandler";

	// private static final String noData = "noData";
	/*
	 * Tags
	 */
	private static final String member = "member";
	private static final String pos = "pos";
	private static final String values = "values";
	private static final String SimpleDataRecord = "SimpleDataRecord";
	private static final String uom = "uom";
	private static final String TextBlock = "TextBlock";
	private static final String encoding = "encoding";
	private static final String featureOfInterest = "featureOfInterest";
	private static final String result = "result";

	/*
	 * Attributes
	 */
	private static final String code = "code";
	private static final String decimalSeparator = "decimalSeparator";
	private static final String tokenSeparator = "tokenSeparator";
	private static final String blockSeparator = "blockSeparator";

	/*
	 * Flags
	 */
	private boolean in_member;
	private boolean in_featureOfInterest;
	private boolean in_result;
	private boolean in_pos;

	private boolean in_SimpleDataRecord;

	private boolean in_encoding;

	private boolean in_values;

	private ParsedObservationData data;

	private String tokSep;
	private String bloSep;

	private StringBuffer charBuffer;

	public GetObservationHandler() {
		data = new ParsedObservationData();
	}

	@Override
	public void characters(char[] ch, int start, int length)
			throws SAXException {

		if (in_member) {
			if (in_featureOfInterest) {
				if (in_pos) {
					charBuffer.append(ch, start, length);
				}
			} else if (in_result) {
				if (in_values) {
					charBuffer.append(ch, start, length);
				}
			}
		}

	}

	@Override
	public void endElement(String uri, String localName, String qName)
			throws SAXException {
		if (in_member) {
			if (in_featureOfInterest) {
				if (in_pos && localName.equals(pos)) {
					in_pos = false;
					/*
					 * parse position
					 */
					// Log.d(DT, "parsing pos "+charBuffer.toString());

					String[] pos = charBuffer.toString().split(" ");
					data.LatE6 = SOSUtils.stringToE6(pos[1]);
					data.LonE6 = SOSUtils.stringToE6(pos[0]);

				} else if (localName.equals(featureOfInterest)) {
					in_featureOfInterest = false;
				}
			} else if (in_result) {
				if (in_encoding && localName.equals(encoding)) {
					in_encoding = false;
				} else if (in_SimpleDataRecord
						&& localName.equals(SimpleDataRecord)) {
					in_SimpleDataRecord = false;
				} else if (in_values && localName.equals(values)) {
					in_values = false;
					/*
					 * parse values
					 */
					String[] measurements = charBuffer.toString().split(bloSep);

					for (int i = 0; i < measurements.length; i++) {

						String[] measure = measurements[i].split(tokSep);

						if (measure.length == 3) {

							try {
								data.times.add(SOSUtils.sosDateFormat
										.parse(measure[0]));
								data.values.add(Float.valueOf(measure[2]));

							} catch (ParseException e) {
								Log.w(DT, e);
							} catch (NumberFormatException e) {
								// data.values.add(Float.NaN);
								data.times.remove();
								// Log.w(DT, measure[2]);
							}
						}
					}
				} else if (uri.equals(SOSUtils.omNamespace)) {
					if (localName.equals(result)) {
						in_result = false;
					}
				}
			} else if (localName.equals(member)) {
				in_member = false;
			}
		}
	}

	@Override
	public void startElement(String uri, String localName, String qName,
			Attributes attributes) throws SAXException {
		if (in_member) {
			if (in_featureOfInterest) {
				/*
				 * TODO differentiate between points, polygons etc.
				 */
				if (uri.equals(SOSUtils.gmlNamespace)) {
					if (localName.equals(pos)) {
						in_pos = true;
						charBuffer = new StringBuffer();
					}
				}
			} else if (in_result) {
				if (uri.equals(SOSUtils.sweNamespace)) {
					if (in_SimpleDataRecord) {
						if (localName.equals(uom)) {
							data.unit = attributes.getValue(code);
						}
					} else if (in_encoding) {
						if (localName.equals(TextBlock)) {
							tokSep = attributes.getValue(tokenSeparator);
							bloSep = attributes.getValue(blockSeparator);
						}
					} else {
						if (localName.equals(SimpleDataRecord)) {
							in_SimpleDataRecord = true;
						} else if (localName.equals(encoding)) {
							in_encoding = true;
						} else if (localName.equals(values)) {
							in_values = true;
							charBuffer = new StringBuffer();
						}
					}
				}

			} else {
				if (uri.equals(SOSUtils.omNamespace)) {
					if (localName.equals(result)) {
						in_result = true;
					} else if (localName.equals(featureOfInterest)) {
						in_featureOfInterest = true;
					}
				}
			}
		} else if (uri.equals(SOSUtils.omNamespace)) {
			if (localName.equals(member)) {
				in_member = true;
			}
		}
	}

	@Override
	public void endDocument() throws SAXException {

		super.endDocument();
	}

	public ParsedObservationData getParsedData() {
		return data;
	}
}
