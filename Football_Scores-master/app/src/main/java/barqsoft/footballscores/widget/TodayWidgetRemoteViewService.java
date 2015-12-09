package barqsoft.footballscores.widget;

import android.annotation.TargetApi;
import android.app.PendingIntent;
import android.app.TaskStackBuilder;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Binder;
import android.os.Build;
import android.widget.AdapterView;
import android.widget.RemoteViews;
import android.widget.RemoteViewsService;

import java.text.SimpleDateFormat;
import java.util.Date;

import barqsoft.footballscores.DatabaseContract;
import barqsoft.footballscores.MainActivity;
import barqsoft.footballscores.R;
import barqsoft.footballscores.Utilies;


@TargetApi(Build.VERSION_CODES.HONEYCOMB)
public class TodayWidgetRemoteViewService extends RemoteViewsService {

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

    @Override
    public RemoteViewsFactory onGetViewFactory(Intent intent) {
        return new RemoteViewsFactory() {

            private Cursor c = null;

            @Override
            public void onCreate() {
                // Nothing to do
            }

            @Override
            public void onDataSetChanged() {
                if (c != null) {
                    c.close();
                }

                final long identityToken = Binder.clearCallingIdentity();

                Uri todayMatchesUri = DatabaseContract.SoccerEntry.buildScoreWithDate();
                String currentDate = new SimpleDateFormat(Utilies.DATE_FORMAT).format(new Date(System.currentTimeMillis()));

                c = getContentResolver().query(todayMatchesUri,
                        SOCCER_COLUMNS,
                        null,
                        new String[] { currentDate },
                        DatabaseContract.SoccerEntry.TIME_COL + " ASC");

                Binder.restoreCallingIdentity(identityToken);
            }

            @Override
            public void onDestroy() {
                if (c != null) {
                    c.close();
                    c = null;
                }
            }

            @Override
            public int getCount() {
                return c == null ? 0 : c.getCount();
            }

            @Override
            public RemoteViews getViewAt(int position) {
                if (position == AdapterView.INVALID_POSITION || c == null || !c.moveToPosition(position)) {
                    return null;
                }

                RemoteViews views = new RemoteViews(getPackageName(), R.layout.match_item);

                views.setTextViewText(R.id.home_name, c.getString(INDEX_HOME));
                views.setTextViewText(R.id.away_name, c.getString(INDEX_AWAY));
                views.setTextViewText(R.id.score_textview, Utilies.getScores(c.getInt(INDEX_HOME_GOALS), c.getInt(INDEX_AWAY_GOALS)));
                views.setTextViewText(R.id.data_textview, c.getString(INDEX_TIME));
                views.setImageViewResource(R.id.home_crest, Utilies.getTeamCrestByTeamName(c.getString(INDEX_HOME)));
                views.setImageViewResource(R.id.away_crest, Utilies.getTeamCrestByTeamName(c.getString(INDEX_AWAY)));

                Intent fillInIntent = new Intent();
                views.setOnClickFillInIntent(R.id.match_item, fillInIntent);

                return views;
            }

            @Override
            public RemoteViews getLoadingView() {
                return new RemoteViews(getPackageName(), R.layout.loading_widget);
            }

            @Override
            public int getViewTypeCount() {
                return 1;
            }

            @Override
            public long getItemId(int position) {
                if (c.moveToPosition(position))
                    return c.getLong(INDEX_SOCCER_ID);
                return position;
            }

            @Override
            public boolean hasStableIds() {
                return true;
            }
        };
    }
}
