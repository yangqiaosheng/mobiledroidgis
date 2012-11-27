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

import java.util.LinkedList;

/**
 * Model to hold Data from a GetCapabilities Response
 * 
 * @author Mathias Menninghaus
 * @version 15.11.2009
 */
public class ParsedSOSCapabilities {

	public String title;
	public String description;

	public String getCapabilitiesGet;
	public String getObservationPost;

	/**
	 * LinkedList of all Observation Offerings in the SensorObservationService
	 */
	public LinkedList<ParsedObservationOffering> offerings;

	public ParsedSOSCapabilities() {
		offerings = new LinkedList<ParsedObservationOffering>();
	}

	/**
	 * Model to hold Data for ObservationOffering Tag from a
	 * GetCapabilitiesResponse
	 * 
	 * @author Mathias Menninghaus (mmenning@uos.de)
	 * @version 15.11.2009
	 */
	public class ParsedObservationOffering {

		public String offering;

		public String name;
		public LinkedList<String> properties;

		public LinkedList<String> featuresOfInterest;
		/**
		 * Instantiate a ParsedObservationOffering and add to the offerings-
		 * List in the referring ParsedSOSCapabilities. So you shall not add the
		 * ParsedObservationOfferings by your own!
		 */
		public ParsedObservationOffering() {
			ParsedSOSCapabilities.this.offerings.add(this);
			properties = new LinkedList<String>();
			featuresOfInterest = new LinkedList<String>();
		}

	}
}
