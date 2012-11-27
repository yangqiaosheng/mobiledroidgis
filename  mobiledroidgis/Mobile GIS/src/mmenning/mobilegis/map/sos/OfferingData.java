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
