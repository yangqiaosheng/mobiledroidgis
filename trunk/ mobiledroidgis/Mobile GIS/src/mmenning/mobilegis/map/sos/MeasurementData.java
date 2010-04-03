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
