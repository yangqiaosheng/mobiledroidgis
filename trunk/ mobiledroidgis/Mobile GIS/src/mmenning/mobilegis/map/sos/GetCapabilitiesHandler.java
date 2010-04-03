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

import mmenning.mobilegis.map.sos.ParsedSOSCapabilities.ParsedObservationOffering;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

/**
 * Simple DefaultHandler to compute GetCapabilities request from a
 * SensorObservationService. Reads only Title, Abstract, GetCapabilities Get,
 * GetObservation Post, and the List of Offerings.
 * 
 * based upon OGC 06-009r6 but not yet full!
 * 
 * @see SOSUtils for used Namespaces
 * 
 * @author Mathias Menninghaus
 * 
 * @version 10.11.2009
 * 
 */
public class GetCapabilitiesHandler extends DefaultHandler {

	private static final String DT = "GetCapabilitiesHandler";
	/*
	 * Tags
	 */
	private static final String Operation = "Operation";
	private static final String ServiceIdentification = "ServiceIdentification";
	private static final String OperationsMetadata = "OperationsMetadata";
	private static final String Title = "Title";
	private static final String Abstract = "Abstract";
	private static final String Get = "Get";
	private static final String Post = "Post";

	private static final String ObservationOffering = "ObservationOffering";
	private static final String name = "name";
	private static final String observedProperty = "observedProperty";
	private static final String featureOfInterest = "featureOfInterest";
	/*
	 * Attributes
	 */
	private static final String GetCapabilities = "GetCapabilities";
	private static final String GetObservation = "GetObservation";

	private static final String id = "id";
	/* name already exists in tags */
	private static final String href = "href";

	/*
	 * Flags
	 */
	private boolean in_ServiceIdentification;
	private boolean in_OperationsMetadata;
	private boolean in_GetCapabilities;
	private boolean in_GetObservation;
	private boolean in_ObservationOffering;
	private boolean in_Title;
	private boolean in_Abstract;
	private boolean in_name;

	private ParsedSOSCapabilities data;
	private ParsedObservationOffering actOffering;

	private StringBuffer charBuffer;
	
	public GetCapabilitiesHandler() {
		data = new ParsedSOSCapabilities();
	}

	@Override
	public void characters(char[] ch, int start, int length)
			throws SAXException {
		if (in_ServiceIdentification) {
			if (in_Title) {
				charBuffer.append(ch, start, length);
			} else if (in_Abstract) {
				charBuffer.append(ch, start, length);
			}
		} else if (in_ObservationOffering) {
			if (in_name) {
				charBuffer.append(ch, start, length);
			}
		}

		super.characters(ch, start, length);
	}

	@Override
	public void endElement(String uri, String localName, String qName)
			throws SAXException {
		if (in_ServiceIdentification) {
			if (in_Title && localName.equals(Title)) {
				data.title=charBuffer.toString();
				in_Title = false;
			} else if (in_Abstract && localName.equals(Abstract)) {
				data.description=charBuffer.toString();
				in_Abstract = false;
			} else if (localName.equals(ServiceIdentification)) {
				in_ServiceIdentification = false;
			}
		} else if (in_OperationsMetadata) {
			if ((in_GetCapabilities || in_GetObservation)
					&& localName.equals(Operation)) {
				in_GetCapabilities = false;
				in_GetObservation = false;
			} else if (localName.equals(OperationsMetadata)) {
				in_OperationsMetadata = false;
			}
		} else if (in_ObservationOffering) {
			if (in_name && localName.equals(name)) {
				actOffering.name=charBuffer.toString();
				in_name = false;
			} else if (localName.equals(ObservationOffering)) {
				in_ObservationOffering = false;
			}
		}
	}

	@Override
	public void startElement(String uri, String localName, String qName,
			Attributes attributes) throws SAXException {

		if (in_ServiceIdentification) {
			if (localName.equals(Title)) {
				charBuffer = new StringBuffer();
				in_Title = true;
			} else if (localName.equals(Abstract)) {
				charBuffer = new StringBuffer();
				in_Abstract = true;
			}
		} else if (in_OperationsMetadata) {
			if (in_GetCapabilities) {
				if (localName.equals(Get)) {
					data.getCapabilitiesGet = attributes.getValue(
							SOSUtils.xlinkNamespace, href);
				}
			} else if (in_GetObservation) {
				if (localName.equals(Post)) {
					data.getObservationPost = attributes.getValue(
							SOSUtils.xlinkNamespace, href);

				}
			} else {
				if (localName.equals(Operation)) {
					String nameAttribute = attributes.getValue(name);
					if (nameAttribute != null) {
						if (nameAttribute.equals(GetCapabilities)) {
							in_GetCapabilities = true;
						} else if (nameAttribute.equals(GetObservation)) {
							in_GetObservation = true;
						}
					}
				}
			}
		} else if (in_ObservationOffering) {

			if (uri.equals(SOSUtils.sosNamespace)) {
				if (localName.equals(observedProperty)) {
					actOffering.properties.add(attributes.getValue(
							SOSUtils.xlinkNamespace, href));
				} else if (localName.equals(featureOfInterest)) {
					actOffering.featuresOfInterest.add(attributes.getValue(
							SOSUtils.xlinkNamespace, href));
				}
			} else if (uri.equals(SOSUtils.gmlNamespace)) {
				if (localName.equals(name)) {
					charBuffer = new StringBuffer();
					in_name = true;
				}
			}

		} else {
			if (uri.equals(SOSUtils.owsNamespace)) {
				if (localName.equals(ServiceIdentification)) {
					in_ServiceIdentification = true;
				} else if (localName.equals(OperationsMetadata)) {
					in_OperationsMetadata = true;
				}
			} else if (uri.equals(SOSUtils.sosNamespace)) {
				if (localName.equals(ObservationOffering)) {
					in_ObservationOffering = true;
					actOffering = data.new ParsedObservationOffering();
					actOffering.offering = attributes.getValue(
							SOSUtils.gmlNamespace, id);
				}
			}
		}
	}

	public ParsedSOSCapabilities getParsedData() {
		return data;
	}
}
