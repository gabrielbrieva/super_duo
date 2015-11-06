package it.jaschke.alexandria;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.PersistableBundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Stack;

import it.jaschke.alexandria.api.Callback;
import it.jaschke.alexandria.ui.FragmentKeys;
import it.jaschke.alexandria.ui.FragmentOrchestrator;


public class MainActivity extends AppCompatActivity implements Callback, FragmentOrchestrator, FragmentManager.OnBackStackChangedListener {

    private CharSequence title;
    private BroadcastReceiver messageReciever;
    private Toolbar mToolbar;

    private ArrayList<String> mBackStack;
    private final String BACKSTACK_KEY = "BACKSTACk_KEY";

    public static final String MESSAGE_EVENT = "MESSAGE_EVENT";
    public static final String MESSAGE_KEY = "MESSAGE_EXTRA";

    public MainActivity() {
        mBackStack = new ArrayList<>();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);

        mToolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(mToolbar);

        messageReciever = new MessageReciever();
        IntentFilter filter = new IntentFilter(MESSAGE_EVENT);
        LocalBroadcastManager.getInstance(this).registerReceiver(messageReciever, filter);

        title = getTitle();

        if (savedInstanceState == null) {
            loadFragment(FragmentKeys.BOOKS);
        } else {
            mBackStack = savedInstanceState.getStringArrayList(BACKSTACK_KEY);
        }

        getSupportFragmentManager().addOnBackStackChangedListener(this);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        outState.putStringArrayList(BACKSTACK_KEY, mBackStack);

        super.onSaveInstanceState(outState);
    }

    public void setTitle(int titleId) {
        title = getString(titleId);
    }

    @Override
    public void loadFragment(FragmentKeys fragmentKey) {
        loadFragment(fragmentKey, null);
    }

    @Override
    public void loadFragment(FragmentKeys fragmentKey, Bundle args) {
        loadFragment(fragmentKey, args, null);
    }

    @Override
    public void loadFragment(FragmentKeys fragmentKey, Bundle args, FragmentKeys backKey) {

        if (fragmentKey == null)
            return;

        String tag = fragmentKey.name();
        Fragment f = getSupportFragmentManager().findFragmentByTag(tag);

        mBackStack.add(backKey == null ? null : backKey.name());

        if (f == null) {
            switch (fragmentKey) {
                case BOOKS:
                    f = new ListOfBooks();
                    break;
                case ADD:
                    f = new AddBook();
                    break;
                case BARCODE_SCANNER:
                    f = new BarcodeScannerFragment();
                    break;
                case ABOUT:
                    f = new About();
                    break;
                case BOOK_DETAIL:
                    f = new BookDetail();
                    break;
            }
        }

        if (f != null) {

            if (args != null && f.getArguments() != null)
                f.getArguments().putAll(args);

            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.container, f, tag)
                    .addToBackStack(tag)
                    .commit();
        }

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_settings) {
            startActivity(new Intent(this, SettingsActivity.class));
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onDestroy() {
        LocalBroadcastManager.getInstance(this).unregisterReceiver(messageReciever);
        super.onDestroy();
    }

    @Override
    public void onItemSelected(String ean) {
        Bundle args = new Bundle();
        args.putString(BookDetail.EAN_KEY, ean);

        loadFragment(FragmentKeys.BOOK_DETAIL, args, FragmentKeys.BOOKS);
    }

    @Override
    public void onBackStackChanged() {
        Log.d("MAIN", "BackStack count: " + getSupportFragmentManager().getBackStackEntryCount());
        Log.d("MAIN", "Fragments count: " + getSupportFragmentManager().getFragments().size());

    }

    private class MessageReciever extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            if(intent.getStringExtra(MESSAGE_KEY)!=null){
                Toast.makeText(MainActivity.this, intent.getStringExtra(MESSAGE_KEY), Toast.LENGTH_LONG).show();
            }
        }
    }

    public void goBack(View view) {
        getSupportFragmentManager().popBackStack();
    }

    @Override
    public void onBackPressed() {
        if(getSupportFragmentManager().getBackStackEntryCount() < 2) {
            finish();
        } else if (mBackStack != null && !mBackStack.isEmpty()) {
            getSupportFragmentManager().popBackStackImmediate(mBackStack.remove(mBackStack.size() -1 ), 0);
        } else {
            super.onBackPressed();
        }
    }


}