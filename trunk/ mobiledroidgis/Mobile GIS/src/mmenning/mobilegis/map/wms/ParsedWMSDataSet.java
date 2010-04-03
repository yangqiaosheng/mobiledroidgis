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

import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;

/**
 * Simple data storage for from the {@link GetCapabilitiesHandler} Handler
 * Based upon OGC 01-068r3 but not yet full!
 * 
 * @author Mathias Menninghaus
 * @version 02.10.2009
 */

public class ParsedWMSDataSet {

	private static final String DT = "ParsedWMSDataSet";

	public String url;
	public String version;
	public ParsedLayer rootLayer;
	public String name;
	public String title;
	public String description;
	public String getMapURL;

	public boolean supportsPNG;

	/**
	 * Represents a WMS - Layer 
	 * 
	 * @author Mathias Menninghaus
	 *
	 */
	public class ParsedLayer {

		/**
		 * Constructs a ParsedLayer and sets it as root layer in the
		 * corresponding ParsedWMSDataSet.
		 * 
		 * @throws IllegalStateException
		 *             if the corresponding ParsedWMSDataSet already has a root
		 *             layer
		 */
		public ParsedLayer() {
			if (ParsedWMSDataSet.this.rootLayer == null) {
				ParsedWMSDataSet.this.rootLayer = this;
			} else {
				throw new IllegalStateException(
						"ParsedWMSDataSet rootlayer already defined: cannot build parsedLayer without exlpicit rootLayer.");
			}
		}

		/**
		 * This is not a copy constructor! Sets the given ParsedLayer as root
		 * layer for the new constructed and adds the new one to the given root.
		 * 
		 * @throws IllegalStateException
		 *             if the corresponding ParsedWMSDataSet has not a root
		 *             layer.
		 */
		public ParsedLayer(ParsedLayer root) {
			if (ParsedWMSDataSet.this.rootLayer == null) {
				throw new RuntimeException(
						"no root parsedLayer defined in corresponding ParsedWMSDataSet");
			}
			this.rootLayer = root;
			root.parsedLayers.add(this);
		}

		public HashSet<String> parsedSRS = new HashSet<String>();
		public List<ParsedLayer> parsedLayers = new LinkedList<ParsedLayer>();

		public float bbox_maxx;
		public float bbox_maxy;
		public float bbox_miny;
		public float bbox_minx;
		public String legend_url;
		public String attribution_logourl;
		public String attribution_url;
		public String attribution_title;
		public ParsedLayer rootLayer;
		public String description;
		public String name;
		public String title;

		/**
		 * Returns name and title of this ParsedLayer
		 */
		public String toString() {
			return name + "|" + title;
		}
	}
}
