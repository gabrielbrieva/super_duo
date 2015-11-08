package it.jaschke.alexandria.ui;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;

/**
 * Base class to normalize fragment toolbar behavior
 */
public class FragmentBase extends Fragment {

    /**
     * Method to enable/disable toolbar Home button as Up and toolbar title
     * @param titleFromResurce
     * @param showUp
     */
    protected void initToolbar(int titleFromResurce, boolean showUp) {
        ((AppCompatActivity)getActivity()).getSupportActionBar().setDisplayHomeAsUpEnabled(showUp);
        ((AppCompatActivity)getActivity()).getSupportActionBar().setDisplayShowHomeEnabled(showUp);
        ((AppCompatActivity)getActivity()).getSupportActionBar().setTitle(getString(titleFromResurce));
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {
            case android.R.id.home:
                getActivity().onBackPressed();
        }

        return super.onOptionsItemSelected(item);
    }
}
