package com.clidwin.android.visualimprints.fragments;

import android.support.v4.app.Fragment;

import com.clidwin.android.visualimprints.R;
import com.clidwin.android.visualimprints.visualizations.ParentVisualization;

/**
 * Parent for visualization fragments handling refresh cases.
 *
 * @author Christina Lidwin
 * @version July 15, 2015
 */
public abstract class VisualizationFragment extends Fragment {
    protected static VisualizationFragment fragment;

    public void refreshLocations() {
        ParentVisualization visualizationView =
                (ParentVisualization) fragment.getView().findViewById(R.id.fragmentVisualization);
        visualizationView.refreshLocations();
    }
}
