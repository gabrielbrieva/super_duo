package barqsoft.footballscores.widget;

import android.app.IntentService;
import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.widget.RemoteViews;

import java.text.SimpleDateFormat;
import java.util.Date;

import barqsoft.footballscores.DatabaseContract;
import barqsoft.footballscores.MainActivity;
import barqsoft.footballscores.R;
import barqsoft.footballscores.Utilies;

public class TodayWidgetIntentService extends IntentService {

    private static final String[] SOCCER_COLUMNS = {
            DatabaseContract.SoccerEntry.TABLE_NAME + "." + DatabaseContract.SoccerEntry._ID,
            DatabaseContract.SoccerEntry.HOME_COL,
            DatabaseContract.SoccerEntry.AWAY_COL,
            DatabaseContract.SoccerEntry.HOME_GOALS_COL,
            DatabaseContract.SoccerEntry.AWAY_GOALS_COL,
            DatabaseContract.SoccerEntry.DATE_COL,
            DatabaseContract.SoccerEntry.TIME_COL
    };

    static final int INDEX_SOCCER_ID = 0;
    static final int INDEX_HOME = 1;
    static final int INDEX_AWAY = 2;
    static final int INDEX_HOME_GOALS = 3;
    static final int INDEX_AWAY_GOALS = 4;
    static final int INDEX_DATE = 5;
    static final int INDEX_TIME = 6;

    public static final String EXTRA_WIDGETS_ID_KEY = "EXTRA_WIDGETS_ID_KEY";

    public TodayWidgetIntentService() {
        super("TodayWidgetIntentService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {

        if (intent == null || !intent.hasExtra(EXTRA_WIDGETS_ID_KEY))
            return;

        int appWidgetId = intent.getIntExtra(EXTRA_WIDGETS_ID_KEY, -1);

        if (appWidgetId == -1)
            return;

        // Get today's matches from the ContentProvider
        Uri todayMatchesUri = DatabaseContract.SoccerEntry.buildScoreWithDate();
        String currentDate = new SimpleDateFormat(Utilies.DATE_FORMAT).format(new Date(System.currentTimeMillis()));

        Cursor c = getContentResolver().query(todayMatchesUri, SOCCER_COLUMNS, null,
                new String[]{currentDate}, DatabaseContract.SoccerEntry.TIME_COL + " ASC");

        AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(this);

        if (c == null) {
            appWidgetManager.updateAppWidget(appWidgetId, new RemoteViews(getPackageName(), R.layout.empty_widget));
            return;
        }

        if (!c.moveToFirst()) {
            c.close();
            appWidgetManager.updateAppWidget(appWidgetId, new RemoteViews(getPackageName(), R.layout.empty_widget));
            return;
        }

        RemoteViews views = new RemoteViews(getPackageName(), R.layout.first_today_match_widget);

        views.setTextViewText(R.id.home_name, c.getString(INDEX_HOME));
        views.setTextViewText(R.id.away_name, c.getString(INDEX_AWAY));
        views.setTextViewText(R.id.score_textview, Utilies.getScores(c.getInt(INDEX_HOME_GOALS), c.getInt(INDEX_AWAY_GOALS)));
        views.setTextViewText(R.id.data_textview, c.getString(INDEX_TIME));
        views.setImageViewResource(R.id.home_crest, Utilies.getTeamCrestByTeamName(c.getString(INDEX_HOME)));
        views.setImageViewResource(R.id.away_crest, Utilies.getTeamCrestByTeamName(c.getString(INDEX_AWAY)));

        Intent launchIntent = new Intent(this, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, launchIntent, 0);
        views.setOnClickPendingIntent(R.id.match_item, pendingIntent);

        appWidgetManager.updateAppWidget(appWidgetId, views);

        c.close();
    }
}
