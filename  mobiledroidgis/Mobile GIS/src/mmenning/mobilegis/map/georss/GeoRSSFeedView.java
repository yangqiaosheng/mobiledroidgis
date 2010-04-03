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
import mmenning.mobilegis.util.ColorPickerDialog;
import mmenning.mobilegis.util.ViewWithContextAndDialog;
import mmenning.mobilegis.util.ColorPickerDialog.OnColorChangedListener;
import android.app.Dialog;
import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.TextView;
import android.widget.CompoundButton.OnCheckedChangeListener;

/**
 * Simple Linear Layout to display and manipulate GeoRSSFeed information. Will
 * only work poperly after calling init.
 * 
 * @author Mathias Menninghaus
 * @version 15.10.2009
 */
public class GeoRSSFeedView extends ViewWithContextAndDialog {

	private static final String DT = "GeoRSSFeedView";

	/**
	 * Listener
	 */
	private GeoRSSFeedListener listen;

	private TextView url;
	private TextView description;

	private static final int COLORPICKERDIALOG = 0;

	private static final int MENU_COLORPICKER = 0;
	private static final int MENU_DELETE = 1;
	private static final int MENU_MARKREAD = 2;

	/**
	 * Dialog to select color for this Feed
	 */
	private ColorPickerDialog colorPickerDialog;

	/**
	 * View where the color of this feed is displayed with setBackroundColor
	 */
	private View colorLabel;

	/**
	 * ID of this view
	 */
	private int geoRSSID;

	public GeoRSSFeedView(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	/**
	 * Returns ID of this GeoRSSFeedView
	 * 
	 * @return the id of this view, means the id given with the GeoRSSFeed in
	 *         init
	 */
	public int getGeoRSSID() {
		return geoRSSID;
	}

	/**
	 * Inits this view and fills it with initial data
	 * 
	 * @param feed
	 *            the GeoRSSFeed which informations should be displayed
	 * @param entries
	 *            GeoRSSEntries which should be displayed in this feed view as a
	 *            list of entries
	 * @param listen
	 *            Listener, which reacts on user input
	 */
	public void init(GeoRSSFeed feed, GeoRSSFeedListener listen) {

		this.geoRSSID = feed.id;

		this.listen = listen;

		this.colorPickerDialog = new ColorPickerDialog(this.getContext(),
				new OnColorChangedListener() {

					public void colorChanged(int color) {
						GeoRSSFeedView.this.listen.onColorChanged(
								GeoRSSFeedView.this, color);
						GeoRSSFeedView.this.setColor(color);
					}

				}, feed.color);
		this.colorPickerDialog.setTitle(R.string.choosecolor);

		url = (TextView) this.findViewById(R.id.georssfeedview_url);
		url.setText(feed.link);

		description = (TextView) this
				.findViewById(R.id.georssfeedview_description);
		description.setText(feed.description);

		colorLabel = this.findViewById(R.id.georssfeedview_colorlabel);
		this.setColor(feed.color);

		CheckBox expand = (CheckBox) this
				.findViewById(R.id.georssfeedview_expand);
		expandOrMinimize(expand.isChecked());
		expand.setOnCheckedChangeListener(new OnCheckedChangeListener() {
			public void onCheckedChanged(CompoundButton buttonView,
					boolean isChecked) {
				GeoRSSFeedView.this.expandOrMinimize(isChecked);
			}
		});

		CheckBox visible = (CheckBox) this
				.findViewById(R.id.georssfeedview_visible);
		visible.setChecked(feed.visible);
		visible.setOnCheckedChangeListener(new OnCheckedChangeListener() {

			public void onCheckedChanged(CompoundButton buttonView,
					boolean isChecked) {
				GeoRSSFeedView.this.listen.onVisibilityChanged(
						GeoRSSFeedView.this, isChecked);

			}
		});

		TextView title = (TextView) this
				.findViewById(R.id.georssfeedview_title);
		title.setText(feed.title);

		entrycount = (TextView) this
				.findViewById(R.id.georssfeedview_entrycount);
	}

	private TextView entrycount;

	/**
	 * Sets the color of the ColorLabel in this feedView. By init() the color is
	 * set to GeoRSSFeed.color. By picking a new Color with the
	 * ColorPickerDialog also this method will be used.
	 * 
	 * @param color
	 */
	public void setColor(int color) {
		colorLabel.setBackgroundColor(color);
	}

	/**
	 * Make settings for expanding or minimizing this view
	 * 
	 * @param expand
	 *            shows whether the view should be expanded or not
	 */
	private void expandOrMinimize(boolean expand) {
		if (expand) {
			url.setVisibility(View.VISIBLE);
			description.setMaxLines(this.getResources().getInteger(
					R.integer.maxLinesExpand));
		} else {
			url.setVisibility(View.GONE);
			description.setMaxLines(this.getResources().getInteger(
					R.integer.maxLinesMinimized));
		}
	}

	/**
	 * Display Title of this feed, consisting of '('count')'+title
	 * 
	 * @param entrycount
	 *            title of the feed
	 * @param count
	 *            count to be displayed
	 */
	public void setCount(int unreadCount, int totalCount) {
		this.entrycount.setText(unreadCount + " / " + totalCount);
	}

	/**
	 * Listener, that reacts on User input to this GeoRSSFeedView.
	 * 
	 * @author Mathias Menninghaus
	 * @version 15.10.2009
	 */
	public interface GeoRSSFeedListener {

		/**
		 * If this Feed has been clicked
		 */
		public void onClick(GeoRSSFeedView view);

		/**
		 * If via the ColorPickerDialog the color is changed. Previously the
		 * color of the the color label will automatically set to newColor
		 * 
		 * @param newColor
		 */
		public void onColorChanged(GeoRSSFeedView view, int newColor);

		/**
		 * If the delete Button on GeoRSSFeedView is clicked
		 */
		public void onDeleteClicked(GeoRSSFeedView view);

		/**
		 * If the visibility check box of this GeoRSSFeedView is clicked
		 * 
		 * @param visible
		 *            true if changed to visible, false if changed to invisible
		 */
		public void onVisibilityChanged(GeoRSSFeedView view, boolean visible);

		
		/**
		 * If the markAllRead button on this view is clicked
		 */
		public void onMarkReadClicked(GeoRSSFeedView view);
	}

	@Override
	protected void onContextMenuItemClicked(int id) {
		switch (id) {
		case MENU_COLORPICKER:
			showDialog(COLORPICKERDIALOG);
			break;
		case MENU_DELETE:
			listen.onDeleteClicked(this);
			break;
		case MENU_MARKREAD:
			listen.onMarkReadClicked(this);
			break;
		}

		super.onContextMenuItemClicked(id);
	}

	@Override
	protected void onCreateContextMenu(ContextMenu menu) {
		menu.addContextMenuItem(new ContextMenuItem(R.string.choosecolor,
				MENU_COLORPICKER));
		menu.addContextMenuItem(new ContextMenuItem(R.string.delete,
				MENU_DELETE));
		menu.addContextMenuItem(new ContextMenuItem(R.string.markread,
				MENU_MARKREAD));
		super.onCreateContextMenu(menu);

	}

	@Override
	protected Dialog onCreateDialog(int id) {
		switch (id) {
		case COLORPICKERDIALOG:
			return colorPickerDialog;
		}

		return super.onCreateDialog(id);
	}

	@Override
	protected void onClick() {
		listen.onClick(this);

	}
}
