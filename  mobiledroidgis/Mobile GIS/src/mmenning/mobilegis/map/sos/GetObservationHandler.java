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
