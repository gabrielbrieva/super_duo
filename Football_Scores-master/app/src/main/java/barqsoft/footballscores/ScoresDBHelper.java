package barqsoft.footballscores;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import barqsoft.footballscores.DatabaseContract.SoccerEntry;

/**
 * Created by yehya khaled on 2/25/2015.
 */
public class ScoresDBHelper extends SQLiteOpenHelper
{
    public static final String DATABASE_NAME = "Scores.db";
    private static final int DATABASE_VERSION = 2;
    public ScoresDBHelper(Context context)
    {
        super(context, DATABASE_NAME,null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db)
    {
        final String CreateScoresTable = "CREATE TABLE " + DatabaseContract.SoccerEntry.TABLE_NAME + " ("
                + DatabaseContract.SoccerEntry._ID + " INTEGER PRIMARY KEY,"
                + SoccerEntry.DATE_COL + " TEXT NOT NULL,"
                + DatabaseContract.SoccerEntry.TIME_COL + " INTEGER NOT NULL,"
                + DatabaseContract.SoccerEntry.HOME_COL + " TEXT NOT NULL,"
                + DatabaseContract.SoccerEntry.AWAY_COL + " TEXT NOT NULL,"
                + DatabaseContract.SoccerEntry.LEAGUE_COL + " INTEGER NOT NULL,"
                + DatabaseContract.SoccerEntry.HOME_GOALS_COL + " TEXT NOT NULL,"
                + DatabaseContract.SoccerEntry.AWAY_GOALS_COL + " TEXT NOT NULL,"
                + DatabaseContract.SoccerEntry.MATCH_ID + " INTEGER NOT NULL,"
                + SoccerEntry.MATCH_DAY + " INTEGER NOT NULL,"
                + " UNIQUE (" + DatabaseContract.SoccerEntry.MATCH_ID + ") ON CONFLICT REPLACE"
                + " );";
        db.execSQL(CreateScoresTable);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion)
    {
        //Remove old values when upgrading.
        db.execSQL("DROP TABLE IF EXISTS " + DatabaseContract.SoccerEntry.TABLE_NAME);
    }
}
