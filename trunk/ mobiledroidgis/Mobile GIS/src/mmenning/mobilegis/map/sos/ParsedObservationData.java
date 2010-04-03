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
