package it.jaschke.alexandria.ui;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;

public class FragmentBase extends Fragment {

    protected void initToolbar(String title, boolean showUp) {
        ((AppCompatActivity)getActivity()).getSupportActionBar().setDisplayHomeAsUpEnabled(showUp);
        ((AppCompatActivity)getActivity()).getSupportActionBar().setDisplayShowHomeEnabled(showUp);
        ((AppCompatActivity)getActivity()).getSupportActionBar().setTitle(title);
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
