package com.mridang.alarmer;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;

import org.acra.ACRA;
import org.ocpsoft.prettytime.PrettyTime;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.provider.Settings;
import android.text.TextUtils;
import android.util.Log;

import com.google.android.apps.dashclock.api.ExtensionData;

/*
 * This class is the main class that provides the widget
 */
public class AlarmerWidget extends ImprovedExtension {

	/*
	 * (non-Javadoc)
	 * @see com.mridang.alarmer.ImprovedExtension#getIntents()
	 */
	@Override
	protected IntentFilter getIntents() {
		return null;
	}

	/*
	 * (non-Javadoc)
	 * @see com.mridang.alarmer.ImprovedExtension#getTag()
	 */
	@Override
	protected String getTag() {
		return getClass().getSimpleName();
	}
	
	/*
	 * (non-Javadoc)
	 * @see com.mridang.alarmer.ImprovedExtension#getUris()
	 */
	@Override
	protected String[] getUris() {
		return new String[] {Settings.System.getUriFor(Settings.System.NEXT_ALARM_FORMATTED).toString()};
	}

	/*
	 * @see
	 * com.google.android.apps.dashclock.api.DashClockExtension#onUpdateData
	 * (int)
	 */
	@SuppressLint("SimpleDateFormat")
	@Override
	protected void onUpdateData(int intReason) {

		Log.d(getTag(), "Checking the status of the next upcoming alarm");
		ExtensionData edtInformation = new ExtensionData();
		setUpdateWhenScreenOn(true);

		try {

			String strAlarma = Settings.System.getString(getContentResolver(), Settings.System.NEXT_ALARM_FORMATTED);
			if (!TextUtils.isEmpty(strAlarma)) {

				Log.d(getTag(), "Next upcoming alarm is scheduled for " + strAlarma);
				Calendar calAlarm = Calendar.getInstance();
				Calendar calParsed = Calendar.getInstance();
				if (android.text.format.DateFormat.is24HourFormat(getApplicationContext())) {

					calParsed.setTime(new SimpleDateFormat("E kk:mm").parse(strAlarma));
					calAlarm.set(Calendar.HOUR_OF_DAY, calParsed.get(Calendar.HOUR_OF_DAY));

				} else {

					calParsed.setTime(new SimpleDateFormat("E h:mm aa").parse(strAlarma));
					calAlarm.set(Calendar.HOUR, calParsed.get(Calendar.HOUR));
					calAlarm.set(Calendar.AM_PM, calParsed.get(Calendar.AM_PM));

				}

				Integer intDiff = calParsed.get(Calendar.DAY_OF_WEEK) - calAlarm.get(Calendar.DAY_OF_WEEK);
				calAlarm.add(Calendar.DAY_OF_MONTH, !(intDiff >= 0) ? intDiff + 7 : intDiff);
				calAlarm.set(Calendar.MINUTE, calParsed.get(Calendar.MINUTE));
				calAlarm.set(Calendar.SECOND, 0);
				calAlarm.set(Calendar.MILLISECOND, 0);

				String strAlarm = DateFormat.getTimeInstance(DateFormat.SHORT).format(calAlarm.getTime());
				Calendar calCalendar = Calendar.getInstance();
				calCalendar.set(Calendar.HOUR_OF_DAY, 0);
				calCalendar.set(Calendar.MINUTE, 0);
				calCalendar.set(Calendar.SECOND, 0);
				calCalendar.set(Calendar.MILLISECOND, 0);

				if (calAlarm.after(calCalendar)) {

					edtInformation.expandedTitle(getString(R.string.today, strAlarm));
					calCalendar.add(Calendar.DATE, 1);
					if (calAlarm.after(calCalendar)) {

						edtInformation.expandedTitle(getString(R.string.tomorrow, strAlarm));
						calCalendar.add(Calendar.DATE, 1);
						if (calAlarm.after(calCalendar)) {

							String strDay = new SimpleDateFormat("EEEE").format(calAlarm.getTime());
							edtInformation.expandedTitle(getString(R.string.later, strDay, strAlarm));

						}

					}

				}

				edtInformation.status(strAlarm);
				edtInformation.expandedBody(new PrettyTime(getResources().getConfiguration().locale).format(calAlarm.getTime()));
				edtInformation.visible(true);

			} else {

				Log.d(getTag(), "No upcoming alarms found");
				edtInformation.visible(false);

			}

		} catch (Exception e) {
			edtInformation.visible(false);
			Log.e(getTag(), "Encountered an error", e);
			ACRA.getErrorReporter().handleSilentException(e);
		}

		edtInformation.icon(R.drawable.ic_dashclock);
		doUpdate(edtInformation);

	}

	/*
	 * (non-Javadoc)
	 * @see com.mridang.alarmer.ImprovedExtension#onReceiveIntent(android.content.Context, android.content.Intent)
	 */
	@Override
	protected void onReceiveIntent(Context ctxContext, Intent ittIntent) {
        onUpdateData(UPDATE_REASON_MANUAL);
	}

}
