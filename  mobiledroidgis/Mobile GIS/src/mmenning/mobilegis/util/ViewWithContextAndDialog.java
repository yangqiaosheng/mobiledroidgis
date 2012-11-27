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
package mmenning.mobilegis.util;

import java.util.ArrayList;
import java.util.HashMap;

import mmenning.mobilegis.R;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.util.AttributeSet;
import android.view.View;
import android.widget.LinearLayout;

/**
 * Abstract LinearLayout. A View which supports displaying a ContextMenu
 * 'onLongClick' and Dialogs. The ContextMenu is a simple AlertDialog with
 * identifier CONTEXTMENU
 * 
 * @author Mathias Menninghaus
 * @version 23.10.2009
 * 
 */
public abstract class ViewWithContextAndDialog extends LinearLayout {

	private static final String DT = "ViewWithContextAndDialog";

	public static final int CONTEXTMENU = -1;

	private HashMap<Integer, Dialog> dialogs;

	private ContextMenu contextMenu;

	public ViewWithContextAndDialog(Context context, AttributeSet attrs) {
		super(context, attrs);
		dialogs = new HashMap<Integer, Dialog>();
		this.setClickable(true);
		this.setFocusable(true);
		this.setBackgroundResource(android.R.drawable.list_selector_background);
		this.contextMenu = new ContextMenu(this.getResources().getString(
				R.string.context_menu));
		this.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				ViewWithContextAndDialog.this.onClick();
			}
		});

		this.setOnLongClickListener(new OnLongClickListener() {

			public boolean onLongClick(View v) {
				showDialog(CONTEXTMENU);
				return true;
			}
		});
		this.onCreateContextMenu(contextMenu);

	}

	/**
	 * Called if this View is clicked.
	 */
	protected abstract void onClick();

	/**
	 * Called if the ContextMenuItem with ID id is clicked
	 * 
	 * @param id
	 *            ID of the clicked ContextMenuItem.
	 */
	protected void onContextMenuItemClicked(int id) {
	}

	/**
	 * Override this Method to create your ContextMenu
	 * 
	 * @param menu
	 *            ContextMenu of this View
	 */
	protected void onCreateContextMenu(ContextMenu menu) {
	}

	/**
	 * Is called when a Dialog with ID id should be shown. Users should call
	 * super or the ContextMenu cannot be created.
	 * 
	 * @param id
	 *            id of the dialog
	 * @return Dialog to show
	 */
	protected Dialog onCreateDialog(int id) {
		AlertDialog.Builder b;
		switch (id) {
		case CONTEXTMENU:
			b = new AlertDialog.Builder(this.getContext());
			b.setTitle(contextMenu.getTitle());
			b.setItems(contextMenu.getMenuEntries(),
					new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog, int which) {
							dialog.dismiss();
							ViewWithContextAndDialog.this
									.onContextMenuItemClicked(contextMenu
											.getID(which));
						}
					});
			return b.create();
		}
		return null;
	}

	/**
	 * Called before every showDialog().
	 * 
	 * @param dialog
	 *            the dialog which will be shown and now can be manipulated
	 * @param id
	 *            ID of this Dialog
	 */
	protected void onPrepareDialog(Dialog dialog, int id) {

	}

	/**
	 * Shows the Dialog with ID id over this View
	 * 
	 * @param id
	 *            ID of the Dialog to be shown
	 */
	protected final void showDialog(int id) {
		if (!dialogs.containsKey(id)) {
			dialogs.put(id, onCreateDialog(id));
		}
		this.onPrepareDialog(dialogs.get(id), id);
		dialogs.get(id).show();
	}

	/**
	 * Dismisses the Dialog with id if it is visible
	 * 
	 * @param id
	 *            ID of the Dialog to be dismissed
	 */
	protected final void dismissDialog(int id) {
		if (dialogs.containsKey(id)) {
			Dialog d = dialogs.get(id);
			if (d.isShowing())
				d.dismiss();
		}
	}

	/**
	 * Inner Class to manage a simple ContextMenu
	 * 
	 * @author Mathias Menninghaus
	 * 
	 */
	public class ContextMenu {
		private ArrayList<ContextMenuItem> menuItems;
		private String title;

		/**
		 * Create ContextMenu
		 * 
		 * @param title
		 */
		private ContextMenu(String title) {
			menuItems = new ArrayList<ContextMenuItem>();
			this.title = title;
		}

		/**
		 * Add a ContextMenuItem to the end of this ContextMenu
		 * 
		 * @param item
		 *            ContextMenuItem to be added
		 */
		public void addContextMenuItem(ContextMenuItem item) {
			menuItems.add(item);
		}

		/**
		 * @return Title of this ContextMenu
		 */
		public String getTitle() {
			return title;
		}

		/**
		 * Set the Title of this ContextMenu
		 * 
		 * @param title
		 */
		public void setTitle(String title) {
			this.title = title;
		}

		/**
		 * ID of a ContextMenu Item at position index
		 * 
		 * @param index
		 * @return
		 */
		private int getID(int index) {
			return menuItems.get(index).getId();
		}

		private CharSequence[] getMenuEntries() {
			CharSequence[] ret = new CharSequence[menuItems.size()];
			for (int i = 0; i < ret.length; i++) {
				ret[i] = menuItems.get(i).getName();
			}
			return ret;
		}

	}

	/**
	 * Class to manage a ContextMenuItem
	 * 
	 * @author Mathias Menninghaus
	 */
	public class ContextMenuItem {

		private String name;

		private int id;

		/**
		 * Instantiate new ContextMenuItem
		 * 
		 * @param nameResourceID
		 *            String-Resource identifier for the name
		 * @param id
		 *            ID of this ContextMenuItem
		 */
		public ContextMenuItem(int nameResourceID, int id) {
			this(ViewWithContextAndDialog.this.getResources().getString(
					nameResourceID), id);
		}

		/**
		 * Instantiate new ContextMenuItem
		 * 
		 * @param name
		 *            name for this ContexMenuItem
		 * @param id
		 *            ID of this ContextMenuItem
		 */
		public ContextMenuItem(String name, int id) {
			this.name = name;
			this.id = id;
		}

		/**
		 * @return the ID with which this ContextMenuItem was created
		 */
		public int getId() {
			return id;
		}

		/**
		 * @return name of this ContextMenuItem
		 */
		public String getName() {
			return name;
		}

		/**
		 * Set the name of this ContextMenuItem
		 * 
		 * @param name
		 *            new name
		 */
		public void setName(String name) {
			this.name = name;
		}
	}
}
