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

import java.util.List;

import mmenning.mobilegis.R;
import mmenning.mobilegis.util.ViewWithContextAndDialog;
import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.TextView;
import android.widget.CompoundButton.OnCheckedChangeListener;

/**
 * LinearLayout to display WMS Information. Call init() to initialize and fill
 * with data. Detailed informations will be shown after the expand button is
 * clicked.
 * 
 * @author Mathias Menninghaus
 * @version 08.10.2009
 * 
 */
public class WMSView extends ViewWithContextAndDialog {

	private static final String DT = "WMSView";

	private WMSListener listen;

	private TextView warnings;
	private TextView description;

	private static final int MENU_DELETE = 2;

	private static final int MENU_UP = 0;

	private static final int MENU_DOWN = 1;

	private int index;

	private int wmsID;

	public WMSView(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	public int getIndex() {
		return index;
	}

	public int getWmsID() {
		return wmsID;
	}

	/**
	 * Fill the View with WMS Data
	 * 
	 * @param wms
	 *            WMSData to be inserted in the view
	 * @param listen
	 *            Listener which handles events on this view
	 * @param index
	 *            Index of this view in the parent viewgroup
	 * @param totalLayerCount
	 *            total Count of layers for the displayed wms
	 */
	public void init(WMSData wms, WMSListener listen, int index,
			int totalLayerCount) {

		this.wmsID = wms.id;
		this.setIndex(index);
		this.listen = listen;
		TextView text = (TextView) this.findViewById(R.id.wmsview_title);
		text.setText(wms.title);

		text = (TextView) this.findViewById(R.id.wmsview_layercount);
		text.setText("(" + totalLayerCount + " "
				+ this.getResources().getString(R.string.layers) + ")");

		warnings = (TextView) this.findViewById(R.id.wmsview_warnings);

		description = (TextView) this.findViewById(R.id.wmsview_description);
		description.setText(wms.description);

		this.setOnClickListener(new OnClickListener() {

			public void onClick(View v) {
				WMSView.this.listen.onClick(WMSView.this);
			}
		});

		this.setOnLongClickListener(new OnLongClickListener() {
			public boolean onLongClick(View v) {
				WMSView.this.showDialog(CONTEXTMENU);
				return true;
			}

		});

		CheckBox check = (CheckBox) this.findViewById(R.id.wmsview_visible);
		check.setChecked(wms.visible);
		check.setOnCheckedChangeListener(new OnCheckedChangeListener() {
			public void onCheckedChanged(CompoundButton buttonView,
					boolean isChecked) {
				WMSView.this.listen.onVisibleChanged(WMSView.this, isChecked);
			}
		});
		check = (CheckBox) this.findViewById(R.id.wmsview_expand);
		expandOrMinimize(check.isChecked());
		check.setOnCheckedChangeListener(new OnCheckedChangeListener() {
			public void onCheckedChanged(CompoundButton buttonView,
					boolean isChecked) {
				WMSView.this.expandOrMinimize(isChecked);
			}

		});
	}

	public void setIndex(int index) {
		this.index = index;
	}

	/**
	 * Set the warnings shown in this view. They will all be displayed seperated
	 * by a linebreak. Note that they are not visible if the view is not
	 * expanded.
	 * 
	 * @param warnings
	 */
	public void setWarnings(List<String> warnings) {
		StringBuffer w = new StringBuffer();
		for (String s : warnings) {
			w.append(s + "\n");
		}
		this.warnings.setText(w.toString());
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
			warnings.setVisibility(View.VISIBLE);
		} else {
			description.setMaxLines(this.getResources().getInteger(
					R.integer.maxLinesMinimized));
			warnings.setVisibility(View.GONE);
		}
	}

	@Override
	protected void onClick() {
		listen.onClick(this);

	}

	@Override
	protected void onContextMenuItemClicked(int id) {
		switch (id) {
		case MENU_UP:
			listen.up(this);
			break;
		case MENU_DOWN:
			listen.down(this);
			break;
		case MENU_DELETE:
			listen.deleteWMS(this);
			break;
		}

	}

	@Override
	protected void onCreateContextMenu(ContextMenu menu) {
		menu.addContextMenuItem(new ContextMenuItem(R.string.up, MENU_UP));

		menu.addContextMenuItem(new ContextMenuItem(R.string.down, MENU_DOWN));

		menu.addContextMenuItem(new ContextMenuItem(R.string.delete,
				MENU_DELETE));
	}

	/**
	 * Listener, to react on some View events. Expand will be handled by the
	 * view itself.
	 * 
	 * @author Mathias Menninghaus
	 * 
	 */
	public interface WMSListener {

		/**
		 * Called when the delete WMS Button is clicked
		 */
		public void deleteWMS(WMSView view);

		/**
		 * Called when the down Button is clicked
		 * 
		 * @param view
		 */
		public void down(WMSView view);

		/**
		 * Called when the whole view is clicked
		 */
		public void onClick(WMSView view);

		/**
		 * Called when the visibility of the corresponding WMS is changed (not
		 * the visibility of this view!)
		 * 
		 * @param visible
		 */
		public void onVisibleChanged(WMSView view, boolean visible);

		/**
		 * Called when the up button is clicked.
		 * 
		 * @param view
		 */
		public void up(WMSView view);
	}

}
