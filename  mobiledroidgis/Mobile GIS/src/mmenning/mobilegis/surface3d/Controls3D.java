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
