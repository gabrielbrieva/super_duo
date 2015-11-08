package it.jaschke.alexandria;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import it.jaschke.alexandria.ui.FragmentBase;


public class AboutFragment extends FragmentBase {

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        initToolbar(R.string.action_about, true);

        View rootView = inflater.inflate(R.layout.fragment_about, container, false);
        return rootView;
    }

}
