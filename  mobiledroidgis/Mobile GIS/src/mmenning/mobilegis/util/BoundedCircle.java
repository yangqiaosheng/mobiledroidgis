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
package mmenning.mobilegis.util;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorFilter;
import android.graphics.Paint;
import android.graphics.drawable.Drawable;

/**
 * 
 * Creates a Drawable consisting of a inner circle of given color and a black
 * outer circle.
 * 
 * @author Mathias Menninghaus
 * @version 15.10.2009
 * 
 */
public class BoundedCircle extends Drawable {

	private static final String DT = "BoundedCircle";

	private final Paint centerCircle;
	private final Paint outerCircle;

	private static final int WIDTH = 30;
	private static final int HEIGHT = 30;

	/**
	 * Create a new OverlayCircle.
	 * 
	 * @param color
	 *            Color of the inner Circle
	 */
	public BoundedCircle(int color) {

		this.centerCircle = new Paint();
		this.centerCircle.setARGB(Color.alpha(color), Color.red(color), Color
				.green(color), Color.blue(color));
		this.centerCircle.setAntiAlias(true);
		this.outerCircle = new Paint();
		this.outerCircle.setARGB(255, 0, 0, 0);
		this.outerCircle.setAntiAlias(true);
	}

	@Override
	public void draw(Canvas canvas) {
		canvas.setViewport(WIDTH, HEIGHT);
		canvas.drawCircle(0, 0, 5, outerCircle);
		canvas.drawCircle(0, 0, 4, centerCircle);
	}

	/**
	 * not supported
	 */
	@Override
	public int getOpacity() {
		// TODO Auto-generated method stub
		return 0;
	}

	/**
	 * set alpha value of outer and inner circle
	 */
	@Override
	public void setAlpha(int alpha) {
		centerCircle.setAlpha(alpha);
		outerCircle.setAlpha(alpha);
	}

	/**
	 * return HEIGHT -> 30
	 */
	@Override
	public int getIntrinsicHeight() {
		return HEIGHT;
	}

	/**
	 * return WIDTH -> 30
	 */
	@Override
	public int getIntrinsicWidth() {
		return WIDTH;
	}

	/**
	 * not supported
	 */
	@Override
	public void setColorFilter(ColorFilter cf) {
		// TODO Auto-generated method stub

	}
}
