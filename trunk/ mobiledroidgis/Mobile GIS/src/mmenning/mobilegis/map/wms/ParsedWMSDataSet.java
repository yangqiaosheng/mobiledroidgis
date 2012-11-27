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
