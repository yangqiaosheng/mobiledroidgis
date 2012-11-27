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

import mmenning.mobilegis.map.wms.ParsedWMSDataSet.ParsedLayer;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

/**
 * Handler for parsing a XML Document returned by a WMS Server on a valid
 * GetCapabilities call. It will estimate a valid and well formed XML based on
 * the currently used OGC Specification.
 * 
 * Based upon OGC 01-068r3 but not yet full!
 * 
 * @author Mathias Menninghaus
 * @version 15.10.2009
 * 
 */
public class GetCapabilitiesHandler extends DefaultHandler {

	public static final String PNGFORMAT = "image/png";

	public static final String DT = "WMSGetCapabilitiesHandler";

	/*
	 * Tags
	 */
	private static final String WMT_MS_Capabilities = "WMT_MS_Capabilities";
	private static final String Service = "Service";
	private static final String Name = "Name";
	private static final String Title = "Title";
	private static final String Abstract = "Abstract";
	private static final String GetMap = "GetMap";
	private static final String Get = "Get";
	private static final String OnlineResource = "OnlineResource";
	private static final String Layer = "Layer";
	private static final String SRS = "SRS";
	private static final String Attribution = "Attribution";
	private static final String LogoURL = "LogoURL";
	private static final String LatLonBoundingBox = "LatLonBoundingBox";
	private static final String LegendURL = "LegendURL";
	private static final String Style = "Style";
	private static final String Format = "Format";

	private static final String Request = "Request";
	private static final String Capability = "Capability";

	/*
	 * Attributes
	 */
	private static final String version = "version";
	private static final String minx = "minx";
	private static final String miny = "miny";
	private static final String maxx = "maxx";
	private static final String maxy = "maxy";
	/*
	 * some attributes have a namespace and a name
	 */
	private static final String XLINK_NAMESPACE = "http://www.w3.org/1999/xlink";
	private static final String href = "href";

	/*
	 * in-tag Flags
	 */
	private boolean in_start;

	private boolean in_Request;
	private boolean in_Capability;

	private boolean in_Service;
	private boolean in_GetMap;
	private boolean in_GetMap_Get;

	private boolean in_Layer;
	private boolean in_Attribution;
	private boolean in_LogoURL;
	private boolean in_Style;
	private boolean in_LegendURL;

	private boolean in_Name;
	private boolean in_Title;
	private boolean in_Abstract;
	private boolean in_SRS;
	private boolean in_GetMap_Format;

	/*
	 * other
	 */
	/**
	 * The currently parsedData
	 */
	private ParsedWMSDataSet parsedData;
	/**
	 * Actual Layer in the ParsedData (shall always be definite)
	 */
	private ParsedLayer actLayer = null;

	private StringBuffer charBuffer;
	
	/**
	 * Returns the parsedData
	 * 
	 * @return the Data parsed with this Handler
	 */
	public ParsedWMSDataSet getParsedData() {
		return parsedData;
	}

	@Override
	public void characters(char[] ch, int start, int length)
			throws SAXException {
		if (in_Service) {
			if (in_Name) {
				charBuffer.append(ch, start, length);
			} else if (in_Title) {
				charBuffer.append(ch, start, length);
			} else if (in_Abstract) {
				charBuffer.append(ch, start, length);
			}
		} else if (in_Capability) {
			if (in_Layer) {
				if (in_Attribution) {
					if (in_Title) {
						charBuffer.append(ch, start, length);
					}
				} else if (in_Name) {
					charBuffer.append(ch, start, length);
				} else if (in_Title) {
					charBuffer.append(ch, start, length);
				} else if (in_Abstract) {
					charBuffer.append(ch, start, length);
				} else if (in_SRS) {
					charBuffer.append(ch, start, length);
				}
			} else if (in_Request) {
				if (in_GetMap_Format) {
					charBuffer.append(ch, start, length);
				}
			}
		}
	}

	@Override
	public void endElement(String uri, String localName, String qName)
			throws SAXException {
		/*
		 * everything in service
		 */
		if (in_Service) {
			/*
			 * everything between the service tag
			 */
			if (localName.equals(Service)) {
				in_Service = false;
			} else if (in_Name && localName.equals(Name)) {
				parsedData.name = charBuffer.toString();
				in_Name = false;
			} else if (in_Title && localName.equals(Title)) {
				parsedData.title = charBuffer.toString();
				in_Title = false;
			} else if (in_Abstract && localName.equals(Abstract)) {
				parsedData.description = charBuffer.toString();
				in_Abstract = false;
			}
		} else if (in_Capability) {

			/*
			 * either parsedLayer or request information
			 */
			if (in_Request) {
				if (in_GetMap && localName.equals(GetMap)) {
					in_GetMap = false;
				} else if (in_GetMap_Get && localName.equals(Get)) {
					in_GetMap_Get = false;
				} else if (in_GetMap_Format && localName.equals(Format)) {
					if (charBuffer.toString().equals(PNGFORMAT)) {
						parsedData.supportsPNG = true;
					}
					in_GetMap_Format = false;
				} else if (localName.equals(Request)) {
					in_Request = false;
				}
			} else if (in_Layer) {
				if (!in_Attribution && !in_Style) {
					/*
					 * everything between parsedLayer tag
					 */
					if (in_Name && localName.equals(Name)) {
						actLayer.name = charBuffer.toString();
						in_Name = false;
					} else if (in_Title && localName.equals(Title)) {
						actLayer.title = charBuffer.toString();
						in_Title = false;
					} else if (in_Abstract && localName.equals(Abstract)) {
						actLayer.description = charBuffer.toString();
						in_Abstract = false;
					} else if (in_SRS && localName.equals(SRS)) {
						String[] strings = charBuffer.toString().split(" ");
						for (String s : strings) {
							actLayer.parsedSRS.add(s.toUpperCase());
						}
						in_SRS = false;
					} else if (localName.equals(Layer)) {
						if (actLayer.rootLayer == null) {
							in_Layer = false;
						}
						actLayer = actLayer.rootLayer;
					}
				} else if (in_Attribution) {
					if (in_Title && localName.equals(Title)) {
						actLayer.attribution_title = charBuffer.toString();
						in_Title = false;
					} else if (in_LogoURL && localName.equals(LogoURL)) {

						in_LogoURL = false;
					} else if (localName.equals(Attribution)) {
						in_Attribution = false;
					}
				} else if (in_Style) {
					if (in_LegendURL && localName.equals(LegendURL)) {
						in_LegendURL = false;
					} else if (localName.equals(Style)) {
						in_Style = false;
					}
				}
			} else if (localName.equals(Capability)) {
				in_Capability = false;
			}
		}
	}

	private void startNewLayer() {
		/*
		 * create root parsedLayer or go deeper
		 */

		in_Layer = true;
		if (actLayer == null) {
			actLayer = parsedData.new ParsedLayer();
		} else {
			ParsedLayer newLayer = parsedData.new ParsedLayer(actLayer);
			actLayer = newLayer;
		}
	}

	@Override
	public void startElement(String uri, String localName, String qName,
			Attributes attributes) throws SAXException {

		/*
		 * begin to read the document
		 */
		if (!in_start) {
			if (localName.equals(WMT_MS_Capabilities)) {
				parsedData = new ParsedWMSDataSet();
				parsedData.version = attributes.getValue(version);
				in_start = true;
			}
		} else {

			/*
			 * decide between service and capability
			 */
			if (!in_Service && !in_Capability) {
				if (localName.equals(Service)) {
					in_Service = true;
				} else if (localName.equals(Capability)) {
					in_Capability = true;
				}

				/*
				 * everything in service
				 */
			} else if (in_Service) {
				/*
				 * everything between the service tag
				 */
				if (localName.equals(Name)) {
					charBuffer = new StringBuffer();
					in_Name = true;
				} else if (localName.equals(Title)) {
					charBuffer = new StringBuffer();
					in_Title = true;
				} else if (localName.equals(Abstract)) {
					charBuffer = new StringBuffer();
					in_Abstract = true;
				} else if (localName.equals(OnlineResource)) {
					parsedData.url = attributes.getValue(XLINK_NAMESPACE, href);
				}
			} else if (in_Capability) {

				/*
				 * either parsedLayer or request information
				 */
				if (!in_Request && !in_Layer) {
					if (localName.equals(Layer)) {
						startNewLayer();
					} else if (localName.equals(Request)) {
						in_Request = true;
					}
				} else if (in_Request) {
					if (localName.equals(GetMap)) {
						in_GetMap = true;
					} else if (in_GetMap && localName.equals(Get)) {
						in_GetMap_Get = true;
					} else if (in_GetMap && localName.equals(Format)) {
						charBuffer = new StringBuffer();
						in_GetMap_Format = true;
					} else if (in_GetMap_Get
							&& localName.equals(OnlineResource)) {
						parsedData.getMapURL = attributes.getValue(
								XLINK_NAMESPACE, href);
					}
				} else if (in_Layer) {
					if (!in_Attribution && !in_Style) {
						/*
						 * everything between parsedLayer tag
						 */
						if (localName.equals(Layer)) {
							startNewLayer();
						} else if (localName.equals(Name)) {
							charBuffer = new StringBuffer();
							in_Name = true;
						} else if (localName.equals(Title)) {
							charBuffer = new StringBuffer();
							in_Title = true;
						} else if (localName.equals(Abstract)) {
							charBuffer = new StringBuffer();
							in_Abstract = true;
						} else if (localName.equals(Style)) {
							charBuffer = new StringBuffer();
							in_Style = true;
						} else if (localName.equals(Attribution)) {
							charBuffer = new StringBuffer();
							in_Attribution = true;
						} else if (localName.equals(SRS)) {
							charBuffer = new StringBuffer();
							in_SRS = true;
						} else if (localName.equals(LatLonBoundingBox)) {
							actLayer.bbox_maxx = Float.valueOf(attributes
									.getValue(maxx));
							actLayer.bbox_maxy = Float.valueOf(attributes
									.getValue(maxy));
							actLayer.bbox_minx = Float.valueOf(attributes
									.getValue(minx));
							actLayer.bbox_miny = Float.valueOf(attributes
									.getValue(miny));
						}
					} else if (in_Attribution) {
						if (localName.equals(Title)) {
							charBuffer = new StringBuffer();
							in_Title = true;
						} else if (localName.equals(LogoURL)) {
							in_LogoURL = true;
						} else if (!in_LogoURL
								&& localName.endsWith(OnlineResource)) {
							actLayer.attribution_url = attributes.getValue(
									XLINK_NAMESPACE, href);
						} else if (in_LogoURL) {
							if (localName.equals(OnlineResource)) {
								actLayer.attribution_logourl = attributes
										.getValue(XLINK_NAMESPACE, href);
							}
						}
					} else if (in_Style) {
						if (localName.equals(LegendURL)) {
							in_LegendURL = true;
						} else if (in_LegendURL) {
							if (localName.equals(OnlineResource)) {
								actLayer.legend_url = attributes.getValue(
										XLINK_NAMESPACE, href);
							}
						}
					}
				}
			}
		}
	}
}
