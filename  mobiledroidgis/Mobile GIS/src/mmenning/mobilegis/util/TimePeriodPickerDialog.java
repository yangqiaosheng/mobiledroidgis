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

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

import mmenning.mobilegis.R;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.TimePicker;
import android.widget.DatePicker.OnDateChangedListener;
import android.widget.TimePicker.OnTimeChangedListener;

public class TimePeriodPickerDialog extends AlertDialog {

	private Calendar start;
	private Calendar end;
	private OnTimePeriodChangedListener listen;

	public TimePeriodPickerDialog(Context context, Date start, Date end,
			OnTimePeriodChangedListener l) {
		super(context);

		this.start = new GregorianCalendar();
		this.start.setTime(start);
		this.end = new GregorianCalendar();
		this.end.setTime(end);
		this.listen = l;
	}

	private DatePicker startdatepicker;
	private TimePicker starttimepicker;
	private Button startdate;
	private Button starttime;

	private DatePicker enddatepicker;
	private TimePicker endtimepicker;
	private Button enddate;
	private Button endtime;

	private Button confirm;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		this.setContentView(R.layout.timeperiodpicker);

		confirm = (Button) this.findViewById(R.id.timeperiod_confirm);
		confirm.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				listen.onTimePeriodChanged(TimePeriodPickerDialog.this, start
						.getTime(), end.getTime());
				TimePeriodPickerDialog.this.dismiss();
			}
		});

		startdatepicker = (DatePicker) this
				.findViewById(R.id.timeperiod_startdatepicker);
		startdatepicker.init(start.get(Calendar.YEAR), start
				.get(Calendar.MONTH), start.get(Calendar.DAY_OF_MONTH),
				new OnDateChangedListener() {
					@Override
					public void onDateChanged(DatePicker view, int year,
							int monthOfYear, int dayOfMonth) {
						start.set(year, monthOfYear, dayOfMonth);
						startdate.setText(formatDate(start));
					}
				});

		starttimepicker = (TimePicker) this
				.findViewById(R.id.timeperiod_starttimepicker);
		starttimepicker.setIs24HourView(true);
		starttimepicker.setCurrentHour(start.get(Calendar.HOUR_OF_DAY));
		starttimepicker.setCurrentMinute(start.get(Calendar.MINUTE));
		starttimepicker.setOnTimeChangedListener(new OnTimeChangedListener() {

			@Override
			public void onTimeChanged(TimePicker view, int hourOfDay, int minute) {
				start.set(Calendar.HOUR_OF_DAY, hourOfDay);
				start.set(Calendar.MINUTE, minute);
				starttime.setText(formatTime(start));
			}
		});
		startdatepicker.setVisibility(View.VISIBLE);
		starttimepicker.setVisibility(View.GONE);

		startdate = (Button) this.findViewById(R.id.timeperiod_startdate);
		startdate.setText(formatDate(start));
		startdate.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				startdatepicker.setVisibility(View.VISIBLE);
				starttimepicker.setVisibility(View.GONE);
			}
		});
		starttime = (Button) this.findViewById(R.id.timeperiod_starttime);
		starttime.setText(formatTime(start));
		starttime.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				startdatepicker.setVisibility(View.GONE);
				starttimepicker.setVisibility(View.VISIBLE);
			}
		});

		enddatepicker = (DatePicker) this
				.findViewById(R.id.timeperiod_enddatepicker);
		enddatepicker.init(end.get(Calendar.YEAR), end.get(Calendar.MONTH), end
				.get(Calendar.DAY_OF_MONTH), new OnDateChangedListener() {
			@Override
			public void onDateChanged(DatePicker view, int year,
					int monthOfYear, int dayOfMonth) {
				end.set(year, monthOfYear, dayOfMonth);
				enddate.setText(formatDate(end));
			}
		});

		endtimepicker = (TimePicker) this
				.findViewById(R.id.timeperiod_endtimepicker);
		endtimepicker.setIs24HourView(true);
		endtimepicker.setCurrentHour(end.get(Calendar.HOUR_OF_DAY));
		endtimepicker.setCurrentMinute(end.get(Calendar.MINUTE));
		endtimepicker.setOnTimeChangedListener(new OnTimeChangedListener() {
			@Override
			public void onTimeChanged(TimePicker view, int hourOfDay, int minute) {
				end.set(Calendar.HOUR_OF_DAY, hourOfDay);
				end.set(Calendar.MINUTE, minute);
				endtime.setText(formatTime(end));
			}
		});
		enddatepicker.setVisibility(View.VISIBLE);
		endtimepicker.setVisibility(View.GONE);

		enddate = (Button) this.findViewById(R.id.timeperiod_enddate);
		enddate.setText(formatDate(end));
		enddate.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				enddatepicker.setVisibility(View.VISIBLE);
				endtimepicker.setVisibility(View.GONE);
			}
		});

		endtime = (Button) this.findViewById(R.id.timeperiod_endtime);
		endtime.setText(formatTime(end));
		endtime.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				enddatepicker.setVisibility(View.GONE);
				endtimepicker.setVisibility(View.VISIBLE);
			}
		});
	}

	private static String formatDate(Calendar c) {
		StringBuffer b = new StringBuffer();
		int d = c.get(Calendar.DAY_OF_MONTH);
		if (d < 10)
			b.append('0');
		b.append(d);
		b.append('.');
		int m = c.get(Calendar.MONTH) + 1;
		if (m < 10)
			b.append('0');
		b.append(m);
		b.append('.');
		b.append(c.get(Calendar.YEAR));
		return b.toString();
	}

	private static String formatTime(Calendar c) {
		StringBuffer b = new StringBuffer();
		int h = c.get(Calendar.HOUR_OF_DAY);
		if (h < 10)
			b.append('0');
		b.append(h);
		b.append(':');
		int m = c.get(Calendar.MINUTE);
		if (m < 10)
			b.append('0');
		b.append(m);
		return b.toString();
	}

	public interface OnTimePeriodChangedListener {

		public void onTimePeriodChanged(Dialog view, Date start, Date end);
	}

}
