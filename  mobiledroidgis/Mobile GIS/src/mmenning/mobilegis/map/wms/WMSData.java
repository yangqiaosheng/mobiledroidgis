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
package mmenning.mobilegis.map.wms;

/**
 * Object to query WMS Data from the WMSDB.
 * 
 * @author Mathias Menninghaus
 * @version 15.10.2009
 */
public class WMSData {

	private static final String DT = "WMSData";
	
	/*
	 * from xml
	 */
	public String url;
	public String version;
	public String name;
	public String title;
	public String description;
	public String getMapURL;
	public boolean supportsPNG;
	/*
	 * from database
	 */
	public int id;
	public boolean visible;
}