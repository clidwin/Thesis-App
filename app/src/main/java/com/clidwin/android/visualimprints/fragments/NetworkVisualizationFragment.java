package com.clidwin.android.visualimprints.fragments;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.clidwin.android.visualimprints.R;

/**
 * View to display a network fragment.
 *
 * @author Christina Lidwin
 * @version July 27, 2015
 */
public class NetworkVisualizationFragment extends Fragment {

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view =inflater.inflate(R.layout.fragment_visualization_network,container,false);
        return view;
    }

    public static NetworkVisualizationFragment newInstance(Context context) {
        NetworkVisualizationFragment newFragment = new NetworkVisualizationFragment();

        Bundle args = new Bundle();
        args.putString("tag", context.getString(R.string.tag_fragment_network_visualization));
        newFragment.setArguments(args);

        return newFragment;
    }

}
