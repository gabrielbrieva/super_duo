package barqsoft.footballscores.service;

import android.app.AlarmManager;
import android.app.IntentService;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.util.Log;
import android.widget.Toast;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;
import java.util.Vector;

import barqsoft.footballscores.DatabaseContract;
import barqsoft.footballscores.R;
import barqsoft.footballscores.Utilies;

/**
 * Created by yehya khaled on 3/2/2015.
 */
public class SoccerService extends IntentService
{
    public static final String LOG_TAG = "SoccerService";
    public static final String ACTION_DATA_UPDATED = "barqsoft.footballscores.app.ACTION_DATA_UPDATED";

    private final String SEASON_LINK = "http://api.football-data.org/alpha/soccerseasons/";
    private final String MATCH_LINK = "http://api.football-data.org/alpha/fixtures/";
    private final String FIXTURES = "fixtures";
    private final String LINKS = "_links";
    private final String SOCCER_SEASON = "soccerseason";
    private final String SELF = "self";
    private final String MATCH_DATE = "date";
    private final String HOME_TEAM = "homeTeamName";
    private final String AWAY_TEAM = "awayTeamName";
    private final String RESULT = "result";
    private final String HOME_GOALS = "goalsHomeTeam";
    private final String AWAY_GOALS = "goalsAwayTeam";
    private final String MATCH_DAY = "matchday";

    private int[] leagues;

    public SoccerService()
    {
        super("SoccerService");
        this.leagues = new int[] { Utilies.CHAMPIONS_LEAGUE, Utilies.PREMIER_LEGAUE };
    }

    @Override
    protected void onHandleIntent(Intent intent)
    {
        // checking network connection state first
        if (Utilies.isNetworkConnected(getApplicationContext())) {

            int deletedCount = getApplicationContext().getContentResolver().delete(DatabaseContract.BASE_CONTENT_URI, null, null);
            Log.d(LOG_TAG, "Matches deleted: " + deletedCount);

            for (int league : leagues) {
                getData(league, "n3"); // today and 2 next days.
                getData(league, "p2"); // yesterday and one day before yesterday.
            }

            // Setting the package ensures that only components in our app will receive the broadcast
            Intent dataUpdatedIntent = new Intent(ACTION_DATA_UPDATED).setPackage(getPackageName());
            sendBroadcast(dataUpdatedIntent);
        }

        // reschedule today sync (sync when each game ends)...
        rescheduleTodaySyncAlarm();
    }

    private void getData (int league, String timeFrame)
    {
        FootballAPIServiceFactory.FootballAPIService APIService = FootballAPIServiceFactory.createService(getString(R.string.api_key));

        JsonObject jsonResult = APIService.fixtures(league, timeFrame);
        if (jsonResult != null);
            processMatches(jsonResult.getAsJsonArray(FIXTURES));
    }
    private void processMatches (JsonArray matches)
    {
        if (matches == null)
            return;

        if (matches.size() == 0)
            return;

        try {

            //ContentValues to be inserted
            Vector<ContentValues> values = new Vector <ContentValues> (matches.size());
            for(int i = 0; i < matches.size(); i++)
            {
                ContentValues matchValues = GetMatchValues(matches.get(i).getAsJsonObject());

                if (matchValues != null)
                    values.add(matchValues);
            }

            int inserted_data = 0;

            ContentValues[] insert_data = new ContentValues[values.size()];
            values.toArray(insert_data);

            inserted_data = getApplicationContext().getContentResolver().bulkInsert(DatabaseContract.BASE_CONTENT_URI, insert_data);

            Log.v(LOG_TAG, "Succesfully Inserted : " + String.valueOf(inserted_data));
        }
        catch (Exception e)
        {
            Log.e(LOG_TAG, e.getMessage());
        }

    }

    private ContentValues GetMatchValues(JsonObject matchData) {

        //Match data
        String League = null;
        String mDate = null;
        String mTime = null;
        String Home = null;
        String Away = null;
        String Home_goals = null;
        String Away_goals = null;
        String match_id = null;
        String match_day = null;

        ContentValues matchValues = null;

        try {

            League = matchData.getAsJsonObject(LINKS).getAsJsonObject(SOCCER_SEASON).
                    get("href").getAsString().replace(SEASON_LINK, "");

            match_id = matchData.getAsJsonObject(LINKS).getAsJsonObject(SELF).
                    get("href").getAsString().replace(MATCH_LINK, "");

            mDate = matchData.get(MATCH_DATE).getAsString();
            mTime = mDate.substring(mDate.indexOf("T") + 1, mDate.indexOf("Z"));
            mDate = mDate.substring(0, mDate.indexOf("T"));

            SimpleDateFormat match_date = new SimpleDateFormat("yyyy-MM-ddHH:mm:ss");
            match_date.setTimeZone(TimeZone.getTimeZone("UTC"));

            try {
                Date parseddate = match_date.parse(mDate + mTime);
                SimpleDateFormat new_date = new SimpleDateFormat("yyyy-MM-dd:HH:mm");
                new_date.setTimeZone(TimeZone.getDefault());
                mDate = new_date.format(parseddate);
                mTime = mDate.substring(mDate.indexOf(":") + 1);
                mDate = mDate.substring(0, mDate.indexOf(":"));
            } catch (Exception e) {
                Log.e(LOG_TAG, e.getMessage());
            }

            Home = matchData.get(HOME_TEAM).getAsString();
            Away = matchData.get(AWAY_TEAM).getAsString();
            Home_goals = matchData.getAsJsonObject(RESULT).get(HOME_GOALS).getAsString();
            Away_goals = matchData.getAsJsonObject(RESULT).get(AWAY_GOALS).getAsString();
            match_day = matchData.get(MATCH_DAY).getAsString();

            matchValues = new ContentValues();

            matchValues.put(DatabaseContract.scores_table.MATCH_ID, match_id);
            matchValues.put(DatabaseContract.scores_table.DATE_COL, mDate);
            matchValues.put(DatabaseContract.scores_table.TIME_COL, mTime);
            matchValues.put(DatabaseContract.scores_table.HOME_COL, Home);
            matchValues.put(DatabaseContract.scores_table.AWAY_COL, Away);
            matchValues.put(DatabaseContract.scores_table.HOME_GOALS_COL, Home_goals);
            matchValues.put(DatabaseContract.scores_table.AWAY_GOALS_COL, Away_goals);
            matchValues.put(DatabaseContract.scores_table.LEAGUE_COL, League);
            matchValues.put(DatabaseContract.scores_table.MATCH_DAY, match_day);

            //log spam
            Log.v(LOG_TAG, match_id);
            Log.v(LOG_TAG, mDate);
            Log.v(LOG_TAG, mTime);
            Log.v(LOG_TAG, Home);
            Log.v(LOG_TAG, Away);
            Log.v(LOG_TAG, Home_goals);
            Log.v(LOG_TAG, Away_goals);

        } catch (Exception ex) {
            Log.e(LOG_TAG, ex.getMessage());
        }

        return matchValues;

    }

    private void rescheduleTodaySyncAlarm() {

        Date currentDate = new Date(System.currentTimeMillis());

        SimpleDateFormat mformat = new SimpleDateFormat("yyyy-MM-dd");
        mformat.setTimeZone(TimeZone.getTimeZone("UTC"));
        SimpleDateFormat tFormat = new SimpleDateFormat("HH:mm");
        tFormat.setTimeZone(TimeZone.getTimeZone("UTC"));

        String todayDate = mformat.format(currentDate);
        String currentTime = tFormat.format(currentDate);

        Cursor c = getApplicationContext().getContentResolver().query(DatabaseContract.scores_table.buildScoreWithDateAndTime(),
                new String[]{ DatabaseContract.scores_table.DATE_COL, DatabaseContract.scores_table.TIME_COL },
                null, new String[]{ todayDate, currentTime }, DatabaseContract.scores_table.DATE_COL + " ASC");

        if (c != null) {
            if (c.moveToNext()) {

                SimpleDateFormat match_date = new SimpleDateFormat("yyyy-MM-ddHH:mm");
                match_date.setTimeZone(TimeZone.getTimeZone("UTC"));

                String date = c.getString(0);
                String time = c.getString(1);

                Date nextSyncDate = null;

                try {
                    nextSyncDate = match_date.parse(date + time);
                } catch (Exception e) {
                    Log.e(LOG_TAG, e.getMessage());
                }

                if (nextSyncDate != null) {

                    nextSyncDate = new Date(nextSyncDate.getTime() + 110 * 60 * 1000);

                    Intent alarmIntent = new Intent(this, SoccerService.AlarmReceiver.class);

                    // Wrap in a pending intent which fires when match ends.
                    PendingIntent pi = PendingIntent.getBroadcast(this, 1, alarmIntent, PendingIntent.FLAG_UPDATE_CURRENT);
                    AlarmManager am = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
                    am.set(AlarmManager.RTC_WAKEUP, nextSyncDate.getTime(), pi);
                }

            }

            c.close();
        }
    }

    public static class AlarmReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            Intent sendIntent = new Intent(context, SoccerService.class);
            context.startService(sendIntent);
        }
    }
}

