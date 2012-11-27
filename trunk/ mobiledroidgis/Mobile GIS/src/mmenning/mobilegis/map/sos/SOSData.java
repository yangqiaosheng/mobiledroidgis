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
