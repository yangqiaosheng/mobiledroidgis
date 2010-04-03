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