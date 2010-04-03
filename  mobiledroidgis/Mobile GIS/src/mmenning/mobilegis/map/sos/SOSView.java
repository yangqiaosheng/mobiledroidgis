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
package mmenning.mobilegis.map.sos;

import mmenning.mobilegis.R;
import mmenning.mobilegis.util.ColorPickerDialog;
import mmenning.mobilegis.util.ViewWithContextAndDialog;
import mmenning.mobilegis.util.ColorPickerDialog.OnColorChangedListener;
import android.app.Dialog;
import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.CompoundButton.OnCheckedChangeListener;

/**
 * View to display a SOS Dataset from the SOSDB. 
 * Must call init() until it works properly
 * 
 * @author Mathias Menninghaus
 * @version 30.11.2009
 *
 */
public class SOSView extends ViewWithContextAndDialog {

	private static final String DT="SOSView";
	
	private static final int CONTEXT_SETCOLOR = 0;
	private static final int CONTEXT_UPDATEALL = 1;
	private static final int CONTEXT_UPDATEAVAILABLE = 2;
	private static final int CONTEXT_DELETE = 3;

	private static final int DIALOG_SETCOLOR = 0;

	/**
	 * database id for the displayed SOS
	 */
	private int sosID;

	private Button property;

	private Button feature;
	private Button offering;
	private ColorPickerDialog colorPickerDialog;

	private LinearLayout colorLabel;
	private SOSViewListener listen;
	private TextView description;
	private TextView memberCount;
	public SOSView(Context context, AttributeSet attrs) {
		super(context, attrs);
		// TODO Auto-generated constructor stub
	}

	/**
	 * 
	 * @return the database id of the given sos dataset
	 */
	public int getSosID() {
		return sosID;
	}

	public void init(SOSData sos, SOSViewListener listen) {
		this.sosID=sos.id;
		this.listen = listen;
		this.colorLabel = (LinearLayout) this
				.findViewById(R.id.sosview_colorlabel);
		this.setColor(sos.color);
		this.colorPickerDialog = new ColorPickerDialog(this.getContext(),
				new OnColorChangedListener() {

					public void colorChanged(int color) {
						SOSView.this.listen.onColorChanged(SOSView.this, color);
						SOSView.this.setColor(color);
					}

				}, sos.color);
		this.colorPickerDialog.setTitle(R.string.choosecolor);

		property = (Button) this.findViewById(R.id.sosview_property);
		property.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				SOSView.this.listen.onPropertyClicked(SOSView.this);
			}
		});
		feature = (Button) this.findViewById(R.id.sosview_feature);
		feature.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				SOSView.this.listen.onFeatureClicked(SOSView.this);
			}
		});
		offering = (Button) this.findViewById(R.id.sosview_offering);
		offering.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				SOSView.this.listen.onOfferingClicked(SOSView.this);
			}
		});

		TextView title = (TextView) this.findViewById(R.id.sosview_title);
		title.setText(sos.title);

		description = (TextView) this.findViewById(R.id.sosview_description);
		description.setText(sos.description);
		memberCount = (TextView) this.findViewById(R.id.sosview_membercount);

		CheckBox visible = (CheckBox) this.findViewById(R.id.sosview_visible);
		visible.setChecked(sos.visible);
		visible.setOnCheckedChangeListener(new OnCheckedChangeListener() {
			@Override
			public void onCheckedChanged(CompoundButton buttonView,
					boolean isChecked) {
				SOSView.this.listen.onVisibleChanged(SOSView.this, isChecked);
			}
		});

		CheckBox expand = (CheckBox) this.findViewById(R.id.sosview_expand);
		expand.setChecked(false);
		expand.setOnCheckedChangeListener(new OnCheckedChangeListener() {
			@Override
			public void onCheckedChanged(CompoundButton buttonView,
					boolean isChecked) {
				SOSView.this.expandOrMinimize(isChecked);
			}
		});
		
		expandOrMinimize(false);
	}

	/**
	 * Set the Color of the sos
	 * @param color color value in Android Format
	 */
	public void setColor(int color) {
		colorLabel.setBackgroundColor(color);
	}

	/**
	 * Set the String displayed above the description. 
	 * @param memberCount
	 */
	public void setMemberCount(String memberCount){
		this.memberCount.setText(memberCount);
	}

	/**
	 * Set the String displayed on the Offering Button
	 * @param offering
	 */
	public void setOffering(String offering) {
		this.offering.setText(offering);
	}
	
	/**
	 * Set the String displayed on the Property Button 
	 * @param property
	 */
	public void setProperty(String property) {
		this.property.setText(property);
		
	}

	private void expandOrMinimize(boolean expand) {
		if (expand) {
			description.setMaxLines(this.getResources().getInteger(
					R.integer.maxLinesExpand));
			offering.setVisibility(View.VISIBLE);
			property.setVisibility(View.VISIBLE);
			feature.setVisibility(View.VISIBLE);

		} else {
			description.setMaxLines(this.getResources().getInteger(
					R.integer.maxLinesMinimized));
			offering.setVisibility(View.GONE);
			property.setVisibility(View.GONE);
			feature.setVisibility(View.GONE);
		}
	}

	@Override
	protected void onClick() {
		// TODO Auto-generated method stub

	}

	@Override
	protected void onContextMenuItemClicked(int id) {
		switch (id) {
		case CONTEXT_DELETE:
			this.listen.onDelete(this);
			break;
		case CONTEXT_SETCOLOR:
			showDialog(DIALOG_SETCOLOR);
			break;
		case CONTEXT_UPDATEALL:
			this.listen.onUpdateAll(this);
			break;
		case CONTEXT_UPDATEAVAILABLE:
			this.listen.onUpdateAvailable(this);
			break;
		}

		super.onContextMenuItemClicked(id);
	}

	@Override
	protected void onCreateContextMenu(ContextMenu menu) {
		menu.addContextMenuItem(new ContextMenuItem(R.string.delete,
				CONTEXT_DELETE));
		menu.addContextMenuItem(new ContextMenuItem(R.string.choosecolor,
				CONTEXT_SETCOLOR));
		menu.addContextMenuItem(new ContextMenuItem(R.string.updateall,
				CONTEXT_UPDATEALL));
		menu.addContextMenuItem(new ContextMenuItem(R.string.updateavailable,
				CONTEXT_UPDATEAVAILABLE));
		super.onCreateContextMenu(menu);
	}

	@Override
	protected Dialog onCreateDialog(int id) {

		switch (id) {
		case DIALOG_SETCOLOR:
			return this.colorPickerDialog;
		}

		return super.onCreateDialog(id);
	}

	public interface SOSViewListener {

		/**
		 * Called when the color has Changed. The color label will be set
		 * automatically to the new color.
		 * 
		 * @param view
		 * @param newColor
		 */
		public void onColorChanged(SOSView view, int newColor);

		/**
		 * Called when the ContextMenu item for deleting is clicked.
		 * 
		 * @param view
		 */
		public void onDelete(SOSView view);

		/**
		 * Called when the feature Button is clicked
		 * 
		 * @param view
		 */
		public void onFeatureClicked(SOSView view);

		/**
		 * Called when the offering Button is clicked. Offering Buttons Label
		 * can be changed by setOffering.
		 * 
		 * @param view
		 */
		public void onOfferingClicked(SOSView view);

		/**
		 * Called when the property Button is clicked. Property Buttons Label
		 * can be changed by setProperty
		 * 
		 * @param view
		 */
		public void onPropertyClicked(SOSView view);

		/**
		 * Called when the ContextMenu item for updating all measurements for
		 * the current selection is clicked.
		 * 
		 * @param view
		 */
		public void onUpdateAll(SOSView view);

		/**
		 * Called when the ContextMenu item for updating all currently available
		 * measurements for the current selection is clicked.
		 * 
		 * @param view
		 */
		public void onUpdateAvailable(SOSView view);

		/**
		 * Called when the visibility checkbox statues changes
		 * 
		 * @param view
		 * @param visible
		 */
		public void onVisibleChanged(SOSView view, boolean visible);
	}

}
