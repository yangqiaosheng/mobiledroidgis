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
 * Model to hold values for a Measurement.
 * 
 * @author Mathias Menninghaus
 * @version 15.11.2009
 */
public class TimeValuePairs {

	/**
	 * database ID of the related Measurement
	 */
	public int measurementID;
	/**
	 * Array to hold time (y-) values. Shall have the same length as values -
	 * Array. Values are assumed to be milliseconds since 01.01.1970 00:00 GMT
	 */
	public long[] times;
	/**
	 * Array to hold (x-) values. Shall have the same length as times - Array
	 */
	public float[] values;
}
