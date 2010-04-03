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

import mmenning.mobilegis.R;
import android.content.Context;
import android.graphics.Bitmap;
import android.util.AttributeSet;
import android.view.View;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.CompoundButton.OnCheckedChangeListener;

/**
 * Linear Layout to display informations about a WMSLayer. Call init to
 * initialize the shown informations. Detailed information will be shown after
 * the expand button is clicked.
 * 
 * @author Mathias Menninghaus
 * @version 08.10.2009
 */
public class WMSLayerView extends LinearLayout {

	private static final String DT = "LayerView";

	private LayerListener listen;

	private LinearLayout attribution;
	private ImageView legend;
	private TextView bbox;
	private TextView srs;
	private TextView description;

	private boolean attributionFlag;
	private boolean legendFlag;

	public WMSLayerView(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	/**
	 * Fill this View with data.
	 * 
	 * @param layer
	 *            data with which the view should be filled
	 * @param listen
	 *            Listener to react on some events in this view
	 * @param containingLayersCount
	 *            does the layer which information is shown her, contain more
	 *            layers? sets the visibility of the containg layers button.
	 */
	public void init(LayerData layer, LayerListener listen,
			int containingLayersCount, Bitmap attributionLogo,
			Bitmap legendImage) {
		this.listen = listen;

		this.layerID = layer.id;

		if (containingLayersCount != 0) {
			this.setOnClickListener(new OnClickListener() {

				public void onClick(View v) {
					WMSLayerView.this.listen.onClick(WMSLayerView.this);
				}

			});
		} else {
			this.setClickable(false);
		}

		srs = (TextView) this.findViewById(R.id.layerview_srs);
		if (layer.selectedSRS != null) {
			srs.setText(layer.selectedSRS);
		}
		srs.setOnClickListener(new OnClickListener() {
			public void onClick(View view) {
				WMSLayerView.this.listen.onSRSClicked(WMSLayerView.this,
						WMSLayerView.this.srs);
			}
		});
		CheckBox visible = (CheckBox) this.findViewById(R.id.layerview_visible);
		visible.setChecked(layer.visible);
		visible.setOnCheckedChangeListener(new OnCheckedChangeListener() {
			public void onCheckedChanged(CompoundButton buttonView,
					boolean isChecked) {
				WMSLayerView.this.listen.onVisibleChanged(WMSLayerView.this,
						isChecked);
			}
		});

		TextView text = (TextView) this.findViewById(R.id.layerview_title);
		text.setText(layer.title);
		text = (TextView) this.findViewById(R.id.layerview_layercount);
		text.setText("(" + containingLayersCount + " "
				+ this.getResources().getString(R.string.layers) + ")");

		description = (TextView) this.findViewById(R.id.layerview_description);
		description.setText(layer.description);

		attribution = (LinearLayout) this
				.findViewById(R.id.layerview_attribution);
		attributionFlag = false;
		if (layer.attribution_title != null
				&& !layer.attribution_title.equals("")) {
			text = (TextView) this
					.findViewById(R.id.layerview_attribution_title);
			text.setText(layer.attribution_title);
			attributionFlag = true;
		}
		if (layer.attribution_url != null
				&& !layer.attribution_title.equals("")) {
			text = (TextView) this.findViewById(R.id.layerview_attribution_url);
			text.setText(layer.attribution_url);
			attributionFlag = true;
		}

		if (attributionLogo != null) {
			ImageView image = (ImageView) this
					.findViewById(R.id.layerview_attribution_logo);
			image.setImageBitmap(attributionLogo);
			attributionFlag = true;
		}
		if (!attributionFlag) {
			attribution.setVisibility(View.GONE);
		}
		legend = (ImageView) this.findViewById(R.id.layerview_legend);
		if (legendImage != null) {
			legendFlag = true;
			legend.setImageBitmap(legendImage);
		} else {
			legend.setVisibility(View.GONE);
		}

		bbox = (TextView) this.findViewById(R.id.layerview_bbox);

		bbox.setText(layer.bbox_minx + "," + layer.bbox_miny + "|"
				+ layer.bbox_maxx + "," + layer.bbox_maxy);

		CheckBox expand = (CheckBox) this.findViewById(R.id.layerview_expand);
		expandOrMinimize(expand.isChecked());
		expand.setOnCheckedChangeListener(new OnCheckedChangeListener() {
			public void onCheckedChanged(CompoundButton buttonView,
					boolean isChecked) {
				expandOrMinimize(isChecked);
			}
		});

	}

	/**
	 * Make settings for expanding or minimizing this view
	 * 
	 * @param expand
	 *            shows whether the view should be expanded or not
	 */
	private void expandOrMinimize(boolean expand) {
		if (expand) {
			description.setMaxLines(this.getResources().getInteger(
					R.integer.maxLinesExpand));
			if (attributionFlag) {
				this.attribution.setVisibility(View.VISIBLE);
			}
			if (legendFlag) {
				this.legend.setVisibility(View.VISIBLE);
			}
			bbox.setVisibility(View.VISIBLE);
			srs.setVisibility(View.VISIBLE);
		} else {
			description.setMaxLines(this.getResources().getInteger(
					R.integer.maxLinesMinimized));
			if (attributionFlag) {
				this.attribution.setVisibility(View.GONE);
			}
			if (legendFlag) {
				this.legend.setVisibility(View.GONE);
			}
			bbox.setVisibility(View.GONE);
			srs.setVisibility(View.GONE);
		}
	}

	private int layerID;

	public int getLayerID() {
		return layerID;
	}

	/**
	 * Listener to handle some events on this view.
	 * 
	 * @author Mathias Menninghaus
	 */

	public interface LayerListener {

		/**
		 * Called when the containing Layers Button is clicked
		 * 
		 * @param view
		 */
		public void onClick(WMSLayerView view);

		/**
		 * Called when the displayed, current selected SRS is clicked.
		 * Implementation should fill the given TextView with the selected SRS.
		 * 
		 * @param srs
		 *            TextView where the current selected SRS of this Layer is
		 *            displayed.
		 */
		public void onSRSClicked(WMSLayerView view, TextView srs);

		/**
		 * Called when the visibility of the corresponding Layer is changed (not
		 * the visibility of this view!)
		 * 
		 * @param visible
		 */
		public void onVisibleChanged(WMSLayerView view, boolean visible);
	}
}
