package it.jaschke.alexandria;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.widget.Toast;

import java.util.ArrayList;

import it.jaschke.alexandria.api.Callback;
import it.jaschke.alexandria.ui.FragmentKeys;
import it.jaschke.alexandria.ui.FragmentOrchestrator;


public class MainActivity extends AppCompatActivity implements Callback, FragmentOrchestrator, FragmentManager.OnBackStackChangedListener {

    private BroadcastReceiver messageReciever;
    private Toolbar mToolbar;

    // custom backstack
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

        // setting main layout
        setContentView(R.layout.activity_main);

        // setting toolbar from support library to activity
        mToolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(mToolbar);

        messageReciever = new MessageReciever();
        IntentFilter filter = new IntentFilter(MESSAGE_EVENT);
        LocalBroadcastManager.getInstance(this).registerReceiver(messageReciever, filter);

        if (savedInstanceState == null) {
            // load book list if is the first time
            loadFragment(FragmentKeys.BOOKS);
        } else {
            // restore custom backstack from savedinstances
            mBackStack = savedInstanceState.getStringArrayList(BACKSTACK_KEY);
        }

        // subscribe BackStackChangedListeners
        getSupportFragmentManager().addOnBackStackChangedListener(this);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        // save current custom backstack
        outState.putStringArrayList(BACKSTACK_KEY, mBackStack);

        super.onSaveInstanceState(outState);
    }

    @Override
    public void loadFragment(FragmentKeys fragmentKey) {
        loadFragment(fragmentKey, null);
    }

    @Override
    public void loadFragment(FragmentKeys fragmentKey, Bundle args) {
        loadFragment(fragmentKey, args, null);
    }

    /**
     * Create or re-use an existent fragment using FragmentManager, transaction and backstack
     * @param fragmentKey is a unique fragment identifier.
     * @param args arguments to use inside of fragment logic.
     * @param backKey which will be the back or parent Fragment Key.
     */
    @Override
    public void loadFragment(FragmentKeys fragmentKey, Bundle args, FragmentKeys backKey) {

        if (fragmentKey == null)
            return;

        String tag = fragmentKey.name();
        // we try to reuse fragment by tag
        Fragment f = getSupportFragmentManager().findFragmentByTag(tag);

        if (backKey != null) {
            // we add back fragment tag to custom back stack
            mBackStack.add(backKey.name());
        }

        if (f == null) {
            // if fragment is not reused we create a new one by fragmentKey
            switch (fragmentKey) {
                case BOOKS:
                    f = new ListOfBooksFragment();
                    break;
                case ADD:
                    f = new AddBookFragment();
                    break;
                case BARCODE_SCANNER:
                    f = new BarcodeScannerFragment();
                    break;
                case ABOUT:
                    f = new AboutFragment();
                    break;
                case BOOK_DETAIL:
                    f = new BookDetailFragment();
                    break;
            }
        }

        if (f != null) {

            if (args != null && f.getArguments() != null) {
                // we pass arguments to fragment
                f.getArguments().putAll(args);
            }

            // we start the fragment transaction
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
            // start setting activity using intent
            startActivity(new Intent(this, SettingsActivity.class));
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onDestroy() {
        // unregister event handlers
        LocalBroadcastManager.getInstance(this).unregisterReceiver(messageReciever);
        getSupportFragmentManager().removeOnBackStackChangedListener(this);
        super.onDestroy();
    }

    /**
     * Book item selected handler
     * @param ean
     */
    @Override
    public void onItemSelected(String ean) {
        // create Bundle to pass parameters to BookDetailFragment Fragment
        Bundle args = new Bundle();
        args.putString(BookDetailFragment.EAN_KEY, ean);

        // load BookDetailFragment using args and add ListOfBooksFragment to custom BackStack
        loadFragment(FragmentKeys.BOOK_DETAIL, args, FragmentKeys.BOOKS);
    }

    @Override
    public void onBackStackChanged() {
        // debug only
        Log.d("MAIN", "BackStack count: " + getSupportFragmentManager().getBackStackEntryCount());
        Log.d("MAIN", "Fragments count: " + getSupportFragmentManager().getFragments().size());
    }

    private class MessageReciever extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getStringExtra(MESSAGE_KEY) != null) {
                Toast.makeText(MainActivity.this, intent.getStringExtra(MESSAGE_KEY), Toast.LENGTH_LONG).show();
            }
        }
    }

    @Override
    public void onBackPressed() {
        if(getSupportFragmentManager().getBackStackEntryCount() < 2) {
            // application exit
            finish();
        } else if (mBackStack != null && !mBackStack.isEmpty()) {
            // pop from custom backstack and start popBackStack
            getSupportFragmentManager().popBackStackImmediate(mBackStack.remove(mBackStack.size() -1 ), 0);
        } else {
            // default action
            super.onBackPressed();
        }
    }


}