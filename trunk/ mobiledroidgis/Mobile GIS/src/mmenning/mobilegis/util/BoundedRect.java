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
 * Creates a Drawable consisting of a inner rectangle of given color and a black
 * outer rectangle.
 * 
 * @author Mathias Menninghaus
 * @version 15.10.2009
 * 
 */
public class BoundedRect extends Drawable {

	private static final String DT = "BoundedCircle";

	private final Paint centerRect;
	private final Paint outerRect;

	private static final int WIDTH = 30;
	private static final int HEIGHT = 30;

	/**
	 * Create a new OverlayCircle.
	 * 
	 * @param color
	 *            Color of the inner Circle
	 */
	public BoundedRect(int color) {

		this.centerRect = new Paint();
		this.centerRect.setARGB(Color.alpha(color), Color.red(color), Color
				.green(color), Color.blue(color));
		this.centerRect.setAntiAlias(true);
		this.centerRect.setStyle(Paint.Style.FILL);

		this.outerRect = new Paint();
		this.outerRect.setARGB(255, 0, 0, 0);
		this.outerRect.setAntiAlias(true);
		this.outerRect.setStyle(Paint.Style.STROKE);

	}

	@Override
	public void draw(Canvas canvas) {
		canvas.setViewport(WIDTH, HEIGHT);
		canvas.drawRect(-5, -5, 5, 5, outerRect);
		canvas.drawRect(-4, -4, 4, 4, centerRect);
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
	 * set alpha value of outer and inner rectangle
	 */
	@Override
	public void setAlpha(int alpha) {
		centerRect.setAlpha(alpha);
		outerRect.setAlpha(alpha);

	}

	/**
	 * not supported
	 */
	@Override
	public void setColorFilter(ColorFilter cf) {
		// TODO Auto-generated method stub
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

}
