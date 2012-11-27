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

			public void onClick(View arg0) {
				if (listen != null) {
					listen.OnZoomInClicked(MapControls.this);
				}

			}

		});

		CheckBox details = (CheckBox) this
				.findViewById(R.id.mapcontrols_details);
		details.setOnCheckedChangeListener(new OnCheckedChangeListener() {

			public void onCheckedChanged(CompoundButton view, boolean checked) {
				if (listen != null) {
					listen.OnDetailsCheckedChange(MapControls.this, checked);
				}
			}

		});

		Button zoomOut = (Button) this.findViewById(R.id.mapcontrols_zoomout);
		zoomOut.setOnClickListener(new OnClickListener() {

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
