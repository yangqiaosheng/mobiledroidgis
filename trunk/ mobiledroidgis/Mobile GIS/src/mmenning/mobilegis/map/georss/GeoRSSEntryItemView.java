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
