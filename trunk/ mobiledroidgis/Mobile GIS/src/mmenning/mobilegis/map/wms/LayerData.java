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
 * Object to query WMS Layer Data from the WMSDB.
 * 
 * @author Mathias Menninghaus
 * 
 * @version 15.10.2009
 */
public class LayerData {

	private static final String DT = "LayerData";

	/*
	 * from database
	 */
	public int id;
	public String selectedSRS;
	public boolean visible;
	/*
	 * from xml
	 */
	public float bbox_maxx;
	public float bbox_maxy;
	public float bbox_miny;
	public float bbox_minx;
	public String legend_url;
	public String attribution_logourl;
	public String attribution_url;
	public String attribution_title;
	public String description;
	public String name;
	public String title;

}