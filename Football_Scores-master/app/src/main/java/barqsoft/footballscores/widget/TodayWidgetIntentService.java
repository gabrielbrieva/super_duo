package barqsoft.footballscores.widget;

import android.app.IntentService;
import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.widget.RemoteViews;

import java.text.SimpleDateFormat;
import java.util.Date;

import barqsoft.footballscores.DatabaseContract;
import barqsoft.footballscores.R;

public class TodayWidgetIntentService extends IntentService {

    public TodayWidgetIntentService() {
        super("TodayWidgetIntentService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {

        // Get today's matches from the ContentProvider
        Uri todayMatchesUri = DatabaseContract.scores_table.buildScoreWithDate();
        String currentDate = new SimpleDateFormat("yyyy-MM-dd").format(new Date(System.currentTimeMillis()));

        Cursor c = getContentResolver().query(todayMatchesUri, null, null,
                new String[] { currentDate }, DatabaseContract.scores_table.DATE_COL + " ASC");

        if (c == null) {
            return;
        }

        if (!c.moveToFirst()) {
            c.close();
            return;
        }

        c.close();



        AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(this);
        int[] appWidgetIds = appWidgetManager.getAppWidgetIds(new ComponentName(this, TodayWidgetProvider.class));

        for (int widgetId : appWidgetIds) {

            RemoteViews view = null;

            if (TodayWidgetProvider.FIRST_MATCH_TODAY_LAYOUT.equals(intent.getAction())) {
                view = new RemoteViews(getPackageName(), R.layout.first_today_match_widget);
            } else {
                view = new RemoteViews(getPackageName(), R.layout.today_matches_widget);
            }

            appWidgetManager.updateAppWidget(widgetId, view);
        }

    }
}
