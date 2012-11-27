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

/**
 * Model for a Measurement. Means a Observation identified by a Feature and a
 * ObservedProperty. An Observation contains a MeasurementData and a
 * TimeValuePairs Instance which can be requested from the Database.
 * 
 * @author Mathias Menninghaus
 * @version 15.11.2009
 * 
 */
public class MeasurementData {

	/**
	 * the id of this measurement by which it is stored into the database
	 */
	public int id;

	/**
	 * database id for the related feature
	 */
	public int featureID;
	/**
	 * database id for the related property
	 */
	public int propertyID;

	/**
	 * Latitude multiplied with E6
	 */
	public int latE6;
	/**
	 * Longitude multiplied with E6
	 */
	public int lonE6;

	/**
	 * Unit for this measturements
	 */
	public String unit;

}
