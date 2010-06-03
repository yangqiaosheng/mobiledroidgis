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
package mmenning.mobilegis.surface3d;

import mmenning.mobilegis.R;
import android.content.Context;
import android.util.AttributeSet;
import android.widget.Button;

/**
 * Simple extended Button two switch between 3 possible drawables if the button
 * is clicked.
 * 
 * @author Mathias Menninghaus
 * @version 06.08.2009
 * 
 */
public class Controls3D extends Button {

	public final static int ROTATE = 0;
	public final static int MOVEXY = 1;
	public final static int MOVEZ = 2;

	private int status;

	public Controls3D(Context context, AttributeSet attrs) {
		super(context, attrs);
		setStatus(ROTATE);
	}

	public void setStatus(int status) {
		switch (status) {
		case ROTATE:
			this.setBackgroundResource(R.drawable.arrow_rotate_48);
			break;
		case MOVEXY:
			this.setBackgroundResource(R.drawable.arrow_cross_48);
			break;
		case MOVEZ:
			this.setBackgroundResource(R.drawable.arrow_up_down_48);
			break;
		}
		this.status = status;

	}

	public int getStatus() {
		return status;
	}

	@Override
	public boolean performClick() {
		setStatus((status + 1) % 3);

		return super.performClick();
	}

}
