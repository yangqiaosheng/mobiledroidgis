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

import java.util.Date;
import java.util.LinkedList;

/**
 * Model to hold data from a GetObservation Response.
 * 
 * @author Mathias Menninghaus
 * @version 15.11.2009
 * 
 */
public class ParsedObservationData {

	/**
	 * unit of the measurement
	 */
	public String unit;

	/**
	 * Latitude multiplied by E6
	 */
	public int LatE6;
	/**
	 * Longitude multiplied by E6
	 */
	public int LonE6;

	/**
	 * LinkedList of time (y-) values. Shall have the same length as values
	 */
	public LinkedList<Date> times;
	/**
	 * LinkedList of values (x). Shall have the same length as times.
	 */
	public LinkedList<Float> values;
	
	public ParsedObservationData(){
		times = new LinkedList<Date>();
		values = new LinkedList<Float>();
	}

}
