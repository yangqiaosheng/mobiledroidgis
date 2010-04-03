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
 * Model to hold data of a SensorObservationService.
 * 
 * @author Mathias Menninghaus
 * @version 15.11.2009
 * 
 */
public class SOSData {

	/**
	 * whether the sos is visible or not
	 */
	public boolean visible;
	/**
	 * database id of the currently selected ObservationOffering
	 */
	public int selectedOffering;
	/**
	 * currently selected color in Android-Format
	 */
	public int color;
	/**
	 * Url to request GetObservation via post.
	 */
	public String getObservationPost;
	/**
	 * Url to request GetCapabilities
	 */
	public String getCapabilities;
	/**
	 * Database ID of the SensorObservationService
	 */
	public int id;
	/**
	 * Title of the SensorObservationService
	 */
	public String title;
	/**
	 * Description of the SensorObservationService
	 */
	public String description;

}
