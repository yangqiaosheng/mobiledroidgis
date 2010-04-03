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
 * Model for holding Data of an ObservationOffering.
 * 
 * @author Mathias Menninghaus
 * @version 15.11.2009
 * 
 */
public class OfferingData {

	/**
	 * ID of the related ObservationOffering in the Database.
	 */
	public int id;
	/**
	 * Name
	 */
	public String name;
	/**
	 * Offering id by which the ObservationOffering is identified in the
	 * SensorObservationService.
	 */
	public String offering;
	/**
	 * database id of the current selectedProperty in the ObservationOffering
	 */
	public int selectedProperty;
	/**
	 * database id of the related SensorObservationService.
	 */
	public int sosID;
}
