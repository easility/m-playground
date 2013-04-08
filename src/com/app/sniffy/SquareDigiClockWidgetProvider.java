package com.app.sniffy;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.widget.RemoteViews;

public class SquareDigiClockWidgetProvider extends AppWidgetProvider{
	
	private static final String LOG_TAG = "ExampleWidget";

	private static final DateFormat df = new SimpleDateFormat("hh:mm,a,E,MMMMM dd");

	/**
	 * Custom Intent name that is used by the AlarmManager to tell us to update the clock once per second.
	 */
	public static String CLOCK_WIDGET_UPDATE = "com.app.sniffy.CLOCK_WIDGET_UPDATE";

	@Override
	public void onReceive(Context context, Intent intent) {
		super.onReceive(context, intent);

		if (CLOCK_WIDGET_UPDATE.equals(intent.getAction())) {
			Log.d(LOG_TAG, "Clock update");
			// Get the widget manager and ids for this widget provider, then call the shared
			// clock update method.
			ComponentName thisAppWidget = new ComponentName(context.getPackageName(), getClass().getName());
		    AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);
		    int ids[] = appWidgetManager.getAppWidgetIds(thisAppWidget);
		    for (int appWidgetID: ids) {
				updateAppWidget(context, appWidgetManager, appWidgetID);

		    }
		}
	}

	private PendingIntent createClockTickIntent(Context context) {
        Intent intent = new Intent(CLOCK_WIDGET_UPDATE);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);

        return pendingIntent;
	}

	@Override
	public void onDisabled(Context context) {
		super.onDisabled(context);
		Log.d(LOG_TAG, "Widget Provider disabled. Turning off timer");
    	AlarmManager alarmManager = (AlarmManager)context.getSystemService(Context.ALARM_SERVICE);
        alarmManager.cancel(createClockTickIntent(context));	
	}

	@Override 
	public void onEnabled(Context context) {
		super.onEnabled(context);
		Log.d(LOG_TAG, "Widget Provider enabled.  Starting timer to update widget every second");
        AlarmManager alarmManager = (AlarmManager)context.getSystemService(Context.ALARM_SERVICE);
    	
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(System.currentTimeMillis());
        calendar.add(Calendar.SECOND, 1);
        alarmManager.setRepeating(AlarmManager.RTC, calendar.getTimeInMillis(), 60000, createClockTickIntent(context));
	}


	@Override
	public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
		final int N = appWidgetIds.length;

		Log.d(LOG_TAG, "Updating Example Widgets.");

		// Perform this loop procedure for each App Widget that belongs to this
		// provider
		for (int i = 0; i < N; i++) {
			int appWidgetId = appWidgetIds[i];

			// Get the layout for the App Widget and attach an on-click listener
			// to the button
			RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.squaredigiclock);
			// Tell the AppWidgetManager to perform an update on the current app
			// widget
			appWidgetManager.updateAppWidget(appWidgetId, views);


			// Update The clock label using a shared method
			updateAppWidget(context, appWidgetManager, appWidgetId);
		}
	}

	public static void updateAppWidget(Context context,	AppWidgetManager appWidgetManager, int appWidgetId) {
		String currentTime =  df.format(new Date());
		
		String[] time = currentTime.split(",");
		
		RemoteViews updateViews = new RemoteViews(context.getPackageName(),	R.layout.squaredigiclock);
		updateViews.setTextViewText(R.id.digi_time, time[0]+" "+time[1]);

		updateViews.setTextViewText(R.id.digi_time_date, time[2]+", "+time[3]);
		appWidgetManager.updateAppWidget(appWidgetId, updateViews);
	}


}
