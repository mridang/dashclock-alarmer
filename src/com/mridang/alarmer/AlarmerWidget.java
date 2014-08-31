package com.mridang.alarmer;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Random;

import org.acra.ACRA;
import org.ocpsoft.prettytime.PrettyTime;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.pm.ResolveInfo;
import android.net.Uri;
import android.provider.Settings;
import android.text.TextUtils;
import android.util.Log;

import com.google.android.apps.dashclock.api.DashClockExtension;
import com.google.android.apps.dashclock.api.ExtensionData;

/*
 * This class is the main class that provides the widget
 */
public class AlarmerWidget extends DashClockExtension {

	/*
	 * @see
	 * com.google.android.apps.dashclock.api.DashClockExtension#onInitialize
	 * (boolean)
	 */
	@Override
	protected void onInitialize(boolean booReconnect) {

		super.onInitialize(booReconnect);
		Log.d("AlarmerWidget", "Registered the content observer");

	}

	/*
	 * @see com.google.android.apps.dashclock.api.DashClockExtension#onCreate()
	 */
	public void onCreate() {

		super.onCreate();
		Log.d("AlarmerWidget", "Created");
		ACRA.init(new AcraApplication(getApplicationContext()));

	}

	/*
	 * @see
	 * com.google.android.apps.dashclock.api.DashClockExtension#onUpdateData
	 * (int)
	 */
	@SuppressLint("SimpleDateFormat")
	@Override
	protected void onUpdateData(int intReason) {

		Log.d("AlarmerWidget", "Checking the status of the next upcoming alarm");
		ExtensionData edtInformation = new ExtensionData();
		setUpdateWhenScreenOn(true);

		try {

			String strAlarma = Settings.System.getString(getContentResolver(), Settings.System.NEXT_ALARM_FORMATTED);
			if (!TextUtils.isEmpty(strAlarma)) {

				Log.d("AlarmerWidget", "Next upcoming alarm is scheduled for " + strAlarma);
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
				edtInformation.expandedBody(new PrettyTime(getResources().getConfiguration().locale).format(calAlarm
						.getTime()));
				edtInformation.visible(true);

			} else {

				Log.d("AlarmerWidget", "No upcoming alarms found");
				edtInformation.visible(false);

			}

			if (new Random().nextInt(5) == 0 && !(0 != (getApplicationInfo().flags & ApplicationInfo.FLAG_DEBUGGABLE))) {

				PackageManager mgrPackages = getApplicationContext().getPackageManager();

				try {

					mgrPackages.getPackageInfo("com.mridang.donate", PackageManager.GET_META_DATA);

				} catch (NameNotFoundException e) {

					Integer intExtensions = 0;
					Intent ittFilter = new Intent("com.google.android.apps.dashclock.Extension");
					String strPackage;

					for (ResolveInfo info : mgrPackages.queryIntentServices(ittFilter, 0)) {

						strPackage = info.serviceInfo.applicationInfo.packageName;
						intExtensions = intExtensions + (strPackage.startsWith("com.mridang.") ? 1 : 0);

					}

					if (intExtensions > 1) {

						edtInformation.visible(true);
						edtInformation.clickIntent(new Intent(Intent.ACTION_VIEW).setData(Uri
								.parse("market://details?id=com.mridang.donate")));
						edtInformation.expandedTitle("Please consider a one time purchase to unlock.");
						edtInformation
								.expandedBody("Thank you for using "
										+ intExtensions
										+ " extensions of mine. Click this to make a one-time purchase or use just one extension to make this disappear.");
						setUpdateWhenScreenOn(true);

					}

				}

			}

		} catch (Exception e) {
			edtInformation.visible(false);
			Log.e("AlarmerWidget", "Encountered an error", e);
			ACRA.getErrorReporter().handleSilentException(e);
		}

		edtInformation.icon(R.drawable.ic_dashclock);
		publishUpdate(edtInformation);
		Log.d("AlarmerWidget", "Done");

	}

	/*
	 * @see com.google.android.apps.dashclock.api.DashClockExtension#onDestroy()
	 */
	public void onDestroy() {

		super.onDestroy();
		Log.d("AlarmerWidget", "Destroyed");

	}

}
