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
