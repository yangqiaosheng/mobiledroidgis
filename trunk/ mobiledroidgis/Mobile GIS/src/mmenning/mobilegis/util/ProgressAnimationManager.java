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
