package barqsoft.footballscores;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import java.util.Calendar;

import barqsoft.footballscores.service.SoccerService;

public class MainActivity extends AppCompatActivity
{
    public static int SelectedMatchId;
    public static int CurrentFrameIndex = 2; // First time is 2 (middle of 5 pages).

    private final String LOG_TAG = MainActivity.class.getSimpleName();

    private final String CURRENT_PAGE_KEY = "CURRENT_PAGE_KEY";
    private final String CURRENT_SELECTED_KEY = "CURRENT_SELECTED_KEY";
    private final String PAGER_FRAGMENT_KEY = "PAGER_FRAGMENT_KEY";

    private PagerFragment pagerFragment;

    private Toolbar mToolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);

        mToolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(mToolbar);

        Log.d(LOG_TAG, "Reached MainActivity onCreate");

        if (savedInstanceState == null) {

            startSyncAlarm();

            pagerFragment = new PagerFragment();

            getSupportFragmentManager().beginTransaction()
                    .add(R.id.container, pagerFragment)
                    .commit();
        }
    }

    private void startSyncAlarm() {

        Intent alarmIntent = new Intent(this, SoccerService.AlarmReceiver.class);

        // we check if the alarm is running
        if (PendingIntent.getBroadcast(this, 0, alarmIntent, PendingIntent.FLAG_NO_CREATE) == null) {

            Log.d(LOG_TAG, "Starting alarm to trigger SoccerService each day at 1:00 a.m.");

            // Set the alarm to start approximately at 1:00 a.m.
            Calendar calendar = Calendar.getInstance();
            calendar.setTimeInMillis(System.currentTimeMillis());
            calendar.set(Calendar.HOUR_OF_DAY, 1);
            calendar.set(Calendar.MINUTE, 0);
            calendar.add(Calendar.DATE, 1);

            // Wrap in a pending intent which fires each day approximately at 1:00 a.m.
            PendingIntent pi = PendingIntent.getBroadcast(this, 0, alarmIntent, PendingIntent.FLAG_UPDATE_CURRENT);
            AlarmManager am = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
            am.setInexactRepeating(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), AlarmManager.INTERVAL_DAY, pi);

            // Start the service now.
            loadMatchesNow();
        }
    }

    private void loadMatchesNow() {
        Intent intent = new Intent(this, SoccerService.class);
        startService(intent);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_about)
        {
            Intent start_about = new Intent(this,AboutActivity.class);
            startActivity(start_about);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState)
    {
        Log.v(LOG_TAG,"will save");
        Log.v(LOG_TAG,"fragment: " + String.valueOf(pagerFragment.mPagerHandler.getCurrentItem()));
        Log.v(LOG_TAG,"selected id: " + SelectedMatchId);

        outState.putInt(CURRENT_PAGE_KEY, pagerFragment.mPagerHandler.getCurrentItem());
        outState.putInt(CURRENT_SELECTED_KEY, SelectedMatchId);

        getSupportFragmentManager().putFragment(outState, PAGER_FRAGMENT_KEY, pagerFragment);

        super.onSaveInstanceState(outState);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState)
    {
        Log.v(LOG_TAG,"will retrive");
        Log.v(LOG_TAG,"fragment: " + String.valueOf(savedInstanceState.getInt(CURRENT_PAGE_KEY)));
        Log.v(LOG_TAG,"selected id: " + savedInstanceState.getInt(CURRENT_SELECTED_KEY));

        CurrentFrameIndex = savedInstanceState.getInt(CURRENT_PAGE_KEY);
        SelectedMatchId = savedInstanceState.getInt(CURRENT_SELECTED_KEY);

        pagerFragment = (PagerFragment) getSupportFragmentManager().getFragment(savedInstanceState, PAGER_FRAGMENT_KEY);

        super.onRestoreInstanceState(savedInstanceState);
    }
}
