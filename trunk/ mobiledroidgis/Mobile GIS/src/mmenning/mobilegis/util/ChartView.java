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

import java.text.DateFormat;
import java.util.Date;

import mmenning.mobilegis.R;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;

/**
 * View to display data in a Chart as Lines or Points. At last able to display
 * TimeStamp-float and float-float value pairs.
 * 
 * You can use xml-attributes or setters and getters to define the look and feel
 * but you have to add data via the setData(...) method.
 * 
 * </br> XML Attributes </br> xLines Number of lines in x-axis direction </br>
 * yLines Number of lines in y-axis direction </br> axisLabelSize Dot-Size of
 * the axis labels </br> labelSize Dot Size of the labels </br> xLabelLength
 * Maxiumum length (number of characters) of labels at the x axis </br>
 * yLabelLength Maximum length (number of characters) of label at the y axis
 * </br> textDistance pixel distance from labels to the axis and other labels.
 * also pixel distance from the chart to the view bounds.
 * </br> backgroundColor the background color of this chart.
 * </br> coordinateColor color of the coordinate system, the x and y lines and their labels.
 * </br> graphColor color of the displayed graph
 * </br> xLabel label at the x axis
 * </br> yLabel label at the y axis
 * </br> mode choose POINT (0) or LINE (1) mode. Other modes will cause an exception. If there is less than two data points the mode will be set to POINT automatically.
 * </br> pointsize pixel size for one point if mode POINT is selected.
 * 
 * @author Mathias Menninghaus
 * @version 20.11.2009
 * 
 * 
 */
public class ChartView extends View {

	private static final String DT = "ChartView";

	private static final int NRLINES = 5;
	private static final String XLABEL = "X - Axis";
	private static final String YLABEL = "Y - Axis";
	private static final int AXISLABELSIZE = 15;
	private static final int LABELSIZE = 10;
	private static final int TEXTDISTANCE = 5;
	private static final int LABELLENGTH = 4;

	private static final int POINTSIZE = 2;

	private static final int BACKGROUNDCOLOR = Color.BLACK;
	private static final int COORDINATECOLOR = Color.GRAY;
	private static final int GRAPHCOLOR = Color.GREEN;

	private int xLines;
	private int yLines;
	private int axisLabelSize;
	private int labelSize;
	private int xLabelLength;
	private int yLabelLength;

	private int textDistance;

	private int graphColor;

	private int backroundColor;

	private int coordinateColor;

	private String xLabel;
	private String yLabel;
	/*
	 * data storage
	 */
	private float[] xPixels;

	private float[] yPixels;
	private float[] yData;

	private long[] xTimeData;
	private float[] xData;

	private float maxX;
	private float maxY;

	private float minX;

	private float minY;
	private long maxTime;

	private long minTime;
	private static final int FLOATMODE = 1;

	private static final int TIMESTAMPMODE = 2;
	private int datamode;

	private DateFormat dateformat;

	public final static int POINT = 0;
	public final static int LINE = 1;

	private int mode;

	private int pointsize;

	public ChartView(Context context) {
		super(context);

		datamode = FLOATMODE;
		xData = new float[0];
		yData = new float[0];
		xPixels = new float[0];
		yPixels = new float[0];

		setxLines(NRLINES);
		setyLines(NRLINES);
		setAxisLabelSize(AXISLABELSIZE);
		setLabelSize(LABELSIZE);
		setxLabelLength(LABELLENGTH);
		setyLabelLength(LABELLENGTH);
		setTextDistance(TEXTDISTANCE);
		setBackgroundColor(BACKGROUNDCOLOR);
		setCoordinateColor(COORDINATECOLOR);
		setGraphColor(GRAPHCOLOR);
		setxLabel(XLABEL);
		setyLabel(YLABEL);
		setMode(LINE);
		setPointsize(POINTSIZE);
	}

	public ChartView(Context context, AttributeSet attrs) {
		super(context, attrs);

		datamode = FLOATMODE;
		xData = new float[0];
		yData = new float[0];
		xPixels = new float[0];
		yPixels = new float[0];

		TypedArray a = getContext().obtainStyledAttributes(attrs,
				R.styleable.ChartView);

		xLines = a.getInt(R.styleable.ChartView_xLines, NRLINES);
		yLines = a.getInt(R.styleable.ChartView_yLines, NRLINES);
		axisLabelSize = a.getInt(R.styleable.ChartView_axisLabelSize,
				AXISLABELSIZE);
		labelSize = a.getInt(R.styleable.ChartView_labelSize, LABELSIZE);
		xLabelLength = a
				.getInt(R.styleable.ChartView_xLabelLength, LABELLENGTH);
		yLabelLength = a
				.getInt(R.styleable.ChartView_yLabelLength, LABELLENGTH);
		textDistance = a.getInt(R.styleable.ChartView_textDistance,
				TEXTDISTANCE);

		backroundColor = a.getColor(R.styleable.ChartView_backgroundColor,
				BACKGROUNDCOLOR);
		coordinateColor = a.getColor(R.styleable.ChartView_coordinateColor,
				COORDINATECOLOR);
		graphColor = a.getColor(R.styleable.ChartView_graphColor, GRAPHCOLOR);
		mode = a.getInt(R.styleable.ChartView_mode, LINE);
		pointsize = a.getInt(R.styleable.ChartView_pointsize, POINTSIZE);

		xLabel = a.getString(R.styleable.ChartView_xLabel);
		if (xLabel == null)
			xLabel = XLABEL;
		yLabel = a.getString(R.styleable.ChartView_yLabel);
		if (yLabel == null)
			yLabel = YLABEL;
		a.recycle();
	}

	public int getAxisLabelSize() {
		return axisLabelSize;
	}

	public int getBackgroundColor() {
		return backroundColor;
	}

	public int getCoordinateColor() {
		return coordinateColor;
	}

	public int getGraphColor() {
		return graphColor;
	}

	public int getLabelSize() {
		return labelSize;
	}

	public int getMode() {
		return mode;
	}

	public int getPointsize() {
		return pointsize;
	}

	public int getTextDistance() {
		return textDistance;
	}

	public String getxLabel() {
		return xLabel;
	}

	public int getxLabelLength() {
		return xLabelLength;
	}

	public int getxLines() {
		return xLines;
	}

	public String getyLabel() {
		return yLabel;
	}

	public int getyLabelLength() {
		return yLabelLength;
	}

	public int getyLines() {
		return yLines;
	}

	public void setAxisLabelSize(int axisLabelSize) {
		this.axisLabelSize = axisLabelSize;
	}

	public void setBackgroundColor(int backroundColor) {
		this.backroundColor = backroundColor;
	}

	public void setCoordinateColor(int coordinateColor) {
		this.coordinateColor = coordinateColor;
	}

	/**
	 * Sets the Data to be displayed. the arrays will be copied.
	 * 
	 * @param xData
	 *            x coordinates
	 * @param yData
	 *            y coordinates
	 * @throws IllegalArgumentException
	 *             if the arrays do not have the same length
	 */
	public void setData(float[] xData, float[] yData) {
		if (xData.length != yData.length) {
			throw new IllegalArgumentException("xData length!=yData length");
		}
		datamode = FLOATMODE;

		maxX = Float.MIN_VALUE;
		minX = Float.MAX_VALUE;
		for (float f : xData) {
			maxX = maxX < f ? f : maxX;
			minX = minX > f ? f : minX;
		}

		this.xData = new float[xData.length];
		System.arraycopy(xData, 0, this.xData, 0, xData.length);

		maxY = Float.MIN_VALUE;
		minY = Float.MAX_VALUE;
		for (float f : yData) {
			maxY = maxY < f ? f : maxY;
			minY = minY > f ? f : minY;
		}

		this.yData = new float[yData.length];
		System.arraycopy(yData, 0, this.yData, 0, yData.length);

		xPixels = new float[xData.length];
		yPixels = new float[yData.length];

	}

	/**
	 * Sets the Data to be displayed. the arrays will be copied.
	 * 
	 * @param xData
	 *            x coordinates. assumed to be TimeStamps as long
	 * @param yData
	 *            y coordinates
	 * @param dateformat
	 *            Date Format in which style the x-axis labels should be
	 *            displayed. Users should mention the labelLength
	 * @throws IllegalArgumentException
	 *             if the arrays do not have the same length
	 */
	public void setData(long[] xData, float[] yData, DateFormat dateformat) {
		if (xData.length != yData.length) {
			throw new IllegalArgumentException("xData length!=yData length");
		}
		this.dateformat = dateformat;
		datamode = TIMESTAMPMODE;

		maxTime = Long.MIN_VALUE;
		minTime = Long.MAX_VALUE;
		for (long l : xData) {
			maxTime = maxTime < l ? l : maxTime;
			minTime = minTime > l ? l : minTime;
		}

		this.xTimeData = new long[xData.length];
		System.arraycopy(xData, 0, this.xTimeData, 0, xData.length);

		maxY = Float.MIN_VALUE;
		minY = Float.MAX_VALUE;
		for (float f : yData) {
			maxY = maxY < f ? f : maxY;
			minY = minY > f ? f : minY;
		}

		this.yData = new float[yData.length];
		System.arraycopy(yData, 0, this.yData, 0, yData.length);

		xPixels = new float[xData.length];
		yPixels = new float[yData.length];
	}

	public void setGraphColor(int graphColor) {
		this.graphColor = graphColor;
	}

	public void setLabelSize(int labelSize) {
		this.labelSize = labelSize;
	}

	public void setMode(int mode) {
		if (mode != POINT && mode != LINE) {
			throw new IllegalArgumentException("no valid mode");
		}
		this.mode = mode;
	}

	public void setPointsize(int pointsize) {
		this.pointsize = pointsize;
	}

	public void setTextDistance(int textDistance) {
		this.textDistance = textDistance;
	}

	public void setxLabel(String xLabel) {
		this.xLabel = xLabel;
	}

	public void setxLabelLength(int labelLength) {
		this.xLabelLength = labelLength;
	}

	public void setxLines(int xLines) {
		this.xLines = xLines;
	}

	public void setyLabel(String yLabel) {
		this.yLabel = yLabel;
	}

	public void setyLabelLength(int yLabelLength) {
		this.yLabelLength = yLabelLength;
	}

	public void setyLines(int yLines) {
		this.yLines = yLines;
	}

	private void convertDataToPixel(Border borders) {

		Log.d(DT, "bottom in convert: " + borders.bottom);
		/*
		 * translate into the origin and normalize
		 * 
		 * multiply with Pixel Range
		 * 
		 * and add origin Pixel
		 * 
		 * important! note that the Pixel origin is at top left of the screen
		 */

		if (minY == maxY) {
			float defaultY = (borders.bottom + borders.top) / 2;
			for (int i = 0; i < yPixels.length; i++) {
				yPixels[i] = defaultY;
			}
		} else {
			float yPixelRange = borders.bottom - borders.top;
			for (int i = 0; i < yData.length; i++) {

				yPixels[i] = (yData[i] - minY) / Math.abs(maxY - minY);
				yPixels[i] *= yPixelRange;
				yPixels[i] = borders.bottom - yPixels[i];
			}
		}

		float xPixelRange = borders.right - borders.left;

		switch (datamode) {
		case FLOATMODE:
			if (minX == maxX) {
				float defaultX = (borders.right + borders.left) / 2;
				for (int i = 0; i < yPixels.length; i++) {
					xPixels[i] = defaultX;
				}
			} else {
				for (int i = 0; i < xData.length; i++) {
					xPixels[i] = (xData[i] - minX) / Math.abs(maxX - minX);
					xPixels[i] *= xPixelRange;
					xPixels[i] += borders.left;
				}
			}
			break;
		case TIMESTAMPMODE:
			if (maxTime == minTime) {
				float defaultX = (borders.right + borders.left) / 2;
				for (int i = 0; i < yPixels.length; i++) {
					xPixels[i] = defaultX;
				}
			} else {
				for (int i = 0; i < xTimeData.length; i++) {
					xPixels[i] = ((float) (xTimeData[i] - minTime))
							/ Math.abs(((float) (maxTime - minTime)));
					xPixels[i] *= xPixelRange;
					xPixels[i] += borders.left;
				}
			}
			break;
		default:
			throw new IllegalStateException("no valid datamode");
		}
	}

	private Border drawAxisLabels(Canvas canvas, Border borders) {
		Paint paint = new Paint();

		/*
		 * draw axis labels
		 */
		paint.setTextSize(axisLabelSize);
		paint.setColor(coordinateColor);
		paint.setStyle(Paint.Style.FILL);
		paint.setAntiAlias(true);
		paint.setTextAlign(Paint.Align.CENTER);
		Rect labelRect = new Rect();

		// Paint centerCircle = new Paint();
		//		
		// centerCircle.setAntiAlias(true);
		// centerCircle.setARGB(255,255,0,0);
		// canvas.drawCircle(borders.left, borders.bottom, 10, centerCircle);
		// centerCircle.setARGB(255,0,255,0);
		// canvas.drawCircle(borders.left, borders.top, 10, centerCircle);
		// centerCircle.setARGB(255,0,0,255);
		// canvas.drawCircle(borders.right, borders.bottom,10, centerCircle);
		// centerCircle.setARGB(255,255,255,255);
		// canvas.drawCircle(borders.right, borders.top,10, centerCircle);

		/*
		 * draw y label
		 */
		paint.getTextBounds(yLabel, 0, yLabel.length(), labelRect);
		/*
		 * on the left middle
		 */
		float textx = borders.left + Math.abs(labelRect.height());
		float texty = (borders.bottom - borders.top) / 2;

		borders.left += Math.abs(labelRect.height()) + textDistance;
		canvas.save();
		/*
		 * rotated
		 */
		canvas.rotate(-90, textx, texty);
		canvas.drawText(yLabel, textx, texty, paint);
		canvas.restore();
		/*
		 * draw x label
		 */
		paint.getTextBounds(xLabel, 0, xLabel.length(), labelRect);
		textx = (borders.right - borders.left) / 2;
		texty = borders.bottom;

		borders.bottom = borders.bottom - Math.abs(labelRect.height())
				- textDistance;
		canvas.drawText(xLabel, textx, texty, paint);

		return borders;

	}

	private void drawData(Canvas canvas) {
		Paint paint = new Paint();
		paint.setStyle(Paint.Style.STROKE);
		paint.setAntiAlias(true);
		paint.setColor(graphColor);
		paint.setDither(true);

		if (xPixels.length == 0) {

			paint.setTextAlign(Paint.Align.CENTER);
			paint.setTextSize(axisLabelSize);
			paint.setColor(graphColor);

			canvas.drawText(this.getContext().getString(R.string.no_data),
					(getRight() + getLeft()) / 2, (getBottom() + getTop()) / 2,
					paint);

		} else if (xPixels.length == 1) {
			mode = POINT;
		}

		switch (mode) {
		case LINE:
			/*
			 * draw line
			 */
			Path path = new Path();

			if (xPixels.length > 0) {
				path.moveTo(xPixels[0], yPixels[0]);
			}
			for (int i = 1; i < xPixels.length; i++) {
				path.lineTo(xPixels[i], yPixels[i]);
			}

			canvas.drawPath(path, paint);
			break;
		case POINT:
			paint.setStrokeWidth(pointsize);

			for (int i = 0; i < xPixels.length; i++) {
				canvas.drawPoint(xPixels[i], yPixels[i], paint);
			}
			break;
		default:
			throw new IllegalStateException("invalid mode");
		}
	}

	private Border drawLinesAndLabels(Canvas canvas, Border borders) {
		Paint paint = new Paint();
		/*
		 * draw labels; labels should be smaller than axis labels
		 */
		paint.setTextSize(labelSize);
		paint.setColor(coordinateColor);
		paint.setStyle(Paint.Style.FILL);
		paint.setAntiAlias(true);
		paint.setTextAlign(Paint.Align.LEFT);
		/*
		 * differentiate between long and float data for the x labels
		 */
		float datadx = Math.abs(maxX - minX) / (float) xLines;
		long timedatadx = Math.abs(maxTime - minTime) / xLines;

		float datady = Math.abs(maxY - minY) / (float) yLines;

		float dx = (borders.right - borders.left) / xLines;
		float dy = (borders.bottom - borders.top) / yLines;

		String actLabel;

		/*
		 * draw lines and labels
		 */
		/*
		 * therefore first create a rectangle to determine what the max text
		 * size in px is. do this for x and y Labels do determine the left and
		 * bottom border
		 */

		Rect ylabelRect = new Rect();
		String filler = "";
		for (int i = 0; i < yLabelLength; i++) {
			filler += "0";
		}
		paint.getTextBounds(filler, 0, filler.length(), ylabelRect);

		Rect xlabelRect = new Rect();
		filler = "";
		for (int i = 0; i < xLabelLength; i++) {
			filler += datamode == FLOATMODE ? "0" : "X";
		}
		paint.getTextBounds(filler, 0, filler.length(), xlabelRect);

		/*
		 * draw y lines and labels
		 */
		float textx = borders.left;
		float texty = borders.bottom - Math.abs(xlabelRect.height())
				- textDistance;

		for (int i = 0; i < yLines; i++, texty -= dy) {

			actLabel = String.valueOf(minY + ((float) (i)) * datady);
			actLabel = actLabel.substring(0,
					actLabel.length() < yLabelLength ? actLabel.length()
							: yLabelLength);
			canvas.drawText(actLabel, textx, texty, paint);
			canvas.drawLine(textx + Math.abs(ylabelRect.width()), texty
					- Math.abs(ylabelRect.exactCenterY()), borders.right, texty
					- Math.abs(ylabelRect.exactCenterY()), paint);
		}

		/*
		 * draw x lines and labels.
		 */
		textx = borders.left + Math.abs(ylabelRect.width())
				- Math.abs(xlabelRect.exactCenterX());
		texty = borders.bottom;

		for (int i = 0; i < xLines; i++, textx += dx) {
			/*
			 * differentiate between long and float data
			 */
			switch (datamode) {
			case FLOATMODE:
				actLabel = String.valueOf(minX + ((float) (i)) * datadx);
				break;
			case TIMESTAMPMODE:
				actLabel = dateformat
						.format(new Date(timedatadx * i + minTime));
				break;

			default:
				throw new IllegalStateException("no valid datamode");
			}
			actLabel = actLabel.substring(0,
					actLabel.length() < xLabelLength ? actLabel.length()
							: xLabelLength);
			canvas.drawText(actLabel, textx, texty, paint);
			canvas.drawLine(textx + Math.abs(xlabelRect.exactCenterX()), (texty
					- Math.abs(xlabelRect.height()) - textDistance - Math
					.abs(ylabelRect.exactCenterY())), textx
					+ Math.abs(xlabelRect.exactCenterX()), borders.top, paint);
		}

		/*
		 * set up new borders
		 */
		borders.bottom = (borders.bottom - Math.abs(xlabelRect.height())
				- Math.abs(ylabelRect.exactCenterY()) - textDistance);
		borders.left += Math.abs(ylabelRect.width());

		return borders;
	}

	protected void onDraw(Canvas canvas) {
		super.onDraw(canvas);

		canvas.setViewport(this.getWidth(), this.getHeight());

		Paint paint = new Paint();
		paint.setStyle(Paint.Style.FILL);
		paint.setColor(backroundColor);
		canvas.drawPaint(paint);

		Border borders = new Border();

		borders.left = this.getLeft() + textDistance;
		borders.right = this.getRight() - textDistance;
		borders.bottom = this.getBottom() - textDistance;
		borders.top = this.getTop() + textDistance;

		borders = drawAxisLabels(canvas, borders);
		borders = drawLinesAndLabels(canvas, borders);
		convertDataToPixel(borders);
		drawData(canvas);
	}

	/**
	 * Inner class to represent the borders of the view in a canvas. 
	 * 
	 * @author Mathias Menninghaus
	 *
	 */
	private class Border {
		protected float left;
		protected float bottom;
		protected float right;
		protected float top;
	}
}