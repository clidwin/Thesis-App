package com.clidwin.android.visualimprints.fragments;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.clidwin.android.visualimprints.R;

/**
 * View to display a test fragment.
 *
 * @author Christina Lidwin
 * @version July 15, 2015
 */
public class BarChartVisualizationFragment extends VisualizationFragment {

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view =inflater.inflate(R.layout.fragment_visualization_bar,container,false);
        return view;
    }

    public static BarChartVisualizationFragment newInstance(Context context) {
        VisualizationFragment newFragment = new BarChartVisualizationFragment();

        Bundle args = new Bundle();
        args.putString("tag", context.getString(R.string.tag_fragment_tile_visualization));
        newFragment.setArguments(args);

        fragment = newFragment;

        return (BarChartVisualizationFragment)newFragment;
    }

}
