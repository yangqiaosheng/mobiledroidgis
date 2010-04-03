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

import android.app.Activity;
import android.view.Window;

/**
 * Class to control the ProgressBar in a Activity Title.
 * 
 * @author Mathias Menninghaus
 * @version 23.10.2009
 * 
 */
public class ProgressAnimationManager {

	private static final String DT = "ProgressAnimationManager";

	private Activity activityContext;
	private int loadingCnt;
	private CharSequence originalTitle;

	/**
	 * @param context
	 *            Activity Context. The Activity must provide the indeterminate
	 *            ProgressBar {@link Window.FEATURE_INDETERMINATE_PROGRESS}
	 */
	public ProgressAnimationManager(Activity context) {
		this.activityContext = context;
		this.loadingCnt = 0;
		this.originalTitle = this.activityContext.getTitle();
		this.activityContext.setProgressBarIndeterminate(true);
	}

	/**
	 * Start Progress Animation, for every start() somewhen a stop() must be
	 * called.
	 */
	public synchronized void start() {
		this.loadingCnt++;
		if (loadingCnt == 1) {
			this.activityContext.setProgressBarIndeterminateVisibility(true);
		}
	}

	/**
	 * Negates one call of start(). If every start() is negated, the
	 * ProgressAnimation will stop.
	 */
	public synchronized void stop() {
		loadingCnt--;
		if (loadingCnt == 0) {
			this.activityContext.setProgressBarIndeterminateVisibility(false);
		} else if (loadingCnt < 0) {
			loadingCnt = 0;
		}
	}

	/**
	 * Negate all calls of start() and stop the ProgressAnimation.
	 */
	public synchronized void stopImediate() {
		this.loadingCnt = 0;
		this.activityContext.setProgressBarIndeterminateVisibility(false);
		this.activityContext.setTitle(originalTitle);
	}
}
