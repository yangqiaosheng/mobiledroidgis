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
