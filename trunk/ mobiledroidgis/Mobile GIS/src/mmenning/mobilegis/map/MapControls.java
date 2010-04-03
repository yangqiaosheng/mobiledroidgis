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
package mmenning.mobilegis.map;

import mmenning.mobilegis.R;
import android.app.Activity;
import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.RelativeLayout;
import android.widget.CompoundButton.OnCheckedChangeListener;

/**
 * View to display MapControls. Consists if a zoom-in, zoom-out and detail
 * button.
 * 
 * @author Mathias Menninghaus
 * @version 30.11.2009
 */
public class MapControls extends RelativeLayout {

	public MapControls(Context context, AttributeSet attrs) {
		super(context, attrs);
		// TODO Auto-generated constructor stub
	}

	private MapControlsListener listen;

	/**
	 * Set the current used MapControlsListener
	 * 
	 * @param l
	 */
	public void setMapControlsListener(MapControlsListener l) {
		this.listen = l;
	}

	@Override
	protected void onFinishInflate() {
		super.onFinishInflate();

		Activity act = (Activity) this.getContext();
		act.getLayoutInflater().inflate(R.layout.mapcontrols, this);

		Button zoomIn = (Button) this.findViewById(R.id.mapcontrols_zoomin);
		zoomIn.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {
				if (listen != null) {
					listen.OnZoomInClicked(MapControls.this);
				}

			}

		});

		CheckBox details = (CheckBox) this
				.findViewById(R.id.mapcontrols_details);
		details.setOnCheckedChangeListener(new OnCheckedChangeListener() {

			@Override
			public void onCheckedChanged(CompoundButton view, boolean checked) {
				if (listen != null) {
					listen.OnDetailsCheckedChange(MapControls.this, checked);
				}
			}

		});

		Button zoomOut = (Button) this.findViewById(R.id.mapcontrols_zoomout);
		zoomOut.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				if (listen != null) {
					listen.OnZoomOutClicked(MapControls.this);
				}
			}

		});
	}

	public interface MapControlsListener {
		/**
		 * Called when the detail Button is (un)checked
		 * 
		 * @param controls
		 * @param checked
		 *            whether the Button is checked or unchecked.
		 */
		public void OnDetailsCheckedChange(MapControls controls, boolean checked);

		/**
		 * Called when the zoom-in Button is clicked
		 * 
		 * @param controls
		 */
		public void OnZoomInClicked(MapControls controls);

		/**
		 * Called when the zoom-out Button is clicked
		 * 
		 * @param controls
		 */
		public void OnZoomOutClicked(MapControls controls);
	}
}
