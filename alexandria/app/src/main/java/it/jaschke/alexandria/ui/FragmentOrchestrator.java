package it.jaschke.alexandria.ui;

import android.os.Bundle;

public interface FragmentOrchestrator {

    void loadFragment(FragmentKeys fragmentKey);
    void loadFragment(FragmentKeys fragmentKey, Bundle args);
    void loadFragment(FragmentKeys fragmentKey, Bundle args, FragmentKeys backKey);

}
