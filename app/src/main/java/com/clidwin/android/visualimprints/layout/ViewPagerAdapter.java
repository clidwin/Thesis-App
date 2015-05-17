package com.clidwin.android.visualimprints.layout;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;

import com.clidwin.android.visualimprints.fragments.MapViewFragment;
import com.clidwin.android.visualimprints.fragments.RawDataFragment;
import com.clidwin.android.visualimprints.fragments.VisualizationFragment;

/**
 * Manages the fragment views within an activity.
 *
 * Source:
 *     http://www.android4devs.com/2015/01/how-to-make-material-design-sliding-tabs.html
 *
 * @version January 21, 2015
 */
public class ViewPagerAdapter extends FragmentStatePagerAdapter {

    CharSequence titles[];

    public ViewPagerAdapter(FragmentManager fm,CharSequence mTitles[]) {
        super(fm);

        this.titles = mTitles;
    }

    //This method return the fragment for the every position in the View Pager
    @Override
    public Fragment getItem(int position) {
        if(position == 0) {
            VisualizationFragment visualizationTab = new VisualizationFragment();
            return visualizationTab;
        /*} else if (position == 1) {
            MapViewFragment mapViewTab = new MapViewFragment();
            return mapViewTab;*/
        } else{
            RawDataFragment rawDataTab = new RawDataFragment();
            return rawDataTab;
        }
    }

    @Override
    public CharSequence getPageTitle(int position) {
        return titles[position];
    }

    // This method return the Number of tabs for the tabs Strip

    @Override
    public int getCount() {
        return titles.length;
    }
}
