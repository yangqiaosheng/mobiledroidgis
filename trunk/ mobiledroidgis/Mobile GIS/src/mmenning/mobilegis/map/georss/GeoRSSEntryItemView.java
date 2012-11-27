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
package mmenning.mobilegis.map.georss;

import mmenning.mobilegis.R;
import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.TextView;

/**
 * View for a GeoRSSEntry. Will only work properly after calling init
 * 
 * @author Mathias Menninghaus
 * @version 14.10.2009
 * 
 */
public class GeoRSSEntryItemView extends LinearLayout {

	private static final String DT = "GeoRSSEntryItemView";

	private CheckBox read;
	private int entryID;

	private OnEntryItemClickListener listen;

	public GeoRSSEntryItemView(Context context, AttributeSet attrs) {
		super(context, attrs);
		this.setOrientation(LinearLayout.VERTICAL);

	}

	/**
	 * 
	 * @return id of the displayed GeoRSSEntry
	 */
	public int getEntryID() {
		return entryID;
	}

	/**
	 * Init this view with an entry
	 * 
	 * @param entry
	 *            GeoRSSEntry from which the view gets the information to
	 *            diplay.
	 */
	public void init(GeoRSSEntry entry, OnEntryItemClickListener listen) {
		this.entryID = entry.id;
		this.listen = listen;
		this.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				GeoRSSEntryItemView.this.listen.onClick(GeoRSSEntryItemView.this);
			}

		});
		read = (CheckBox) this.findViewById(R.id.georssentryitem_read);
		this.markRead(entry.read);
		TextView title = (TextView) this
				.findViewById(R.id.georssentryitem_title);
		title.setText(entry.title);
		TextView time = (TextView) this.findViewById(R.id.georssentryitem_time);
		time.setText(GeoRSSUtils.displayTimeFormat.format(entry.time));
	}

	/**
	 * Set the 'read' drawable in this view
	 * 
	 * @param read
	 */
	public void markRead(boolean read) {
		this.read.setChecked(read);
	}

	/**
	 * Set this EntryItemView visible or gone
	 * 
	 * @param visible
	 */
	public void setVisibility(boolean visible) {
		if (visible) {
			this.setVisibility(View.VISIBLE);
		} else {
			this.setVisibility(View.GONE);
		}
	}

	public interface OnEntryItemClickListener {
		public void onClick(GeoRSSEntryItemView view);
	}
}
