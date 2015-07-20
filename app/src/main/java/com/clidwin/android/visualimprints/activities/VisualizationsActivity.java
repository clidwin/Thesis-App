package com.clidwin.android.visualimprints.activities;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.ColorStateList;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;

import com.clidwin.android.visualimprints.R;
import com.clidwin.android.visualimprints.fragments.DateTimeDialogFragment;
import com.clidwin.android.visualimprints.layout.SlidingTabLayout;
import com.clidwin.android.visualimprints.layout.ViewPagerAdapter;
import com.clidwin.android.visualimprints.ui.DateSelector;
import com.clidwin.android.visualimprints.visualizations.ParentVisualization;

import java.util.Calendar;

/**
 * Controlling activity for the application. Constructs the visual interface and interactivity
 * based on Fragment points of view (to utilize material design).
 *
 * @author Christina Lidwin
 * @version July 13, 2015
 */
public class VisualizationsActivity extends AppActivity {
    private static final String TAG = "main-activity-visuals";
    private static final String SAVED_STATE_OLDEST_TIMESTAMP = "oldestTimestamp";
    private static final String SAVED_STATE_NEWEST_TIMESTAMP = "newestTimestamp";
    private static final String SAVED_STATE_TIME_RANGE = "timeRange";
    private static final String SAVED_STATE_LIVE_UPDATE = "liveUpdate";

    // Declaring Your View and Variables
    ViewPager pager;
    ViewPagerAdapter viewPageAdapter;

    //TODO(clidwin): Make this dynamically find visualizations
    CharSequence titles[]={"Tile", "Bar", "Map"};

    private boolean mIsLargeLayout;

    private Calendar oldestTimestamp;
    private Calendar newestTimestamp;
    private TimeInterval mTimeInterval;
    private boolean mShouldLiveUpdate;

    private OnModifyListener mOnModifyListener;
    private SlidingTabLayout tabs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_visualizations);

        viewPageAdapter =  new ViewPagerAdapter(this, getSupportFragmentManager(), titles);
        mIsLargeLayout = getResources().getBoolean(R.bool.large_layout);

        mOnModifyListener = new OnModifyListener();

        // Assigning ViewPager View and setting the viewPageAdapter to show multiple visualizations
        pager = (ViewPager) findViewById(R.id.pager);
        pager.setAdapter(viewPageAdapter);
        pager.setSystemUiVisibility(View.SYSTEM_UI_FLAG_HIDE_NAVIGATION |
                View.SYSTEM_UI_FLAG_LAYOUT_STABLE);

        // Assiging the Sliding Tab Layout View
        tabs = (SlidingTabLayout) findViewById(R.id.visualization_tabs);
        tabs.setDistributeEvenly(true);
        tabs.setCustomTabView(R.layout.tab_image_and_text, R.id.tab_text);
        tabs.setViewPager(pager);



        // Configuring autohide options on the menus
        /*decorView = getWindow().getDecorView();
        int uiOptions = View.SYSTEM_UI_FLAG_HIDE_NAVIGATION | View.SYSTEM_UI_FLAG_FULLSCREEN;
        decorView.setSystemUiVisibility(uiOptions);*/

        // Customizing UI elements
        setupIconTray();
        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }

        // Set initial timestamps for visualizations.
        oldestTimestamp = Calendar.getInstance();
        oldestTimestamp.add(Calendar.DAY_OF_YEAR, -1);
        newestTimestamp = Calendar.getInstance();
        mTimeInterval = TimeInterval.DAY;
        mShouldLiveUpdate = true;

        loadSharedPreferences();
    }

    private void loadSharedPreferences() {
        SharedPreferences preferences = getPreferences(MODE_PRIVATE);

        int timeRange = preferences.getInt(SAVED_STATE_TIME_RANGE, -1);
        if(timeRange != -1) {
            mTimeInterval = TimeInterval.getValue(timeRange);
        }

        long oldestTime = preferences.getLong(SAVED_STATE_OLDEST_TIMESTAMP, -1);
        if (oldestTime != -1) {
            oldestTimestamp.setTimeInMillis(oldestTime);
        }

        long newestTime = preferences.getLong(SAVED_STATE_NEWEST_TIMESTAMP, -1);
        if (newestTime != -1) {
            newestTimestamp.setTimeInMillis(newestTime);
        }

        if (mShouldLiveUpdate) {
            updateCalendarsLive();
        }
    }

    public ViewPager getPager() { return pager; }

    /**
     * Update the Calendar objects for the oldest and newest timestamp based on a pre-determined
     * time interval and the current system time.
     */
    private void updateCalendarsLive() {
        Calendar tempDateTime = Calendar.getInstance();
        int timeDiff = (int)(newestTimestamp.getTimeInMillis() - oldestTimestamp.getTimeInMillis());

        newestTimestamp.setTimeInMillis(tempDateTime.getTimeInMillis());

        tempDateTime = calculateOldestTime(mTimeInterval, tempDateTime);
        if (tempDateTime == null) {
            tempDateTime = newestTimestamp;
            tempDateTime.add(Calendar.MILLISECOND, 0 - timeDiff);
        }

        oldestTimestamp = tempDateTime;
    }

    @Override
    public void onPause() {
        super.onPause();

        SharedPreferences.Editor editor = getPreferences(MODE_PRIVATE).edit();
        editor.putLong(SAVED_STATE_OLDEST_TIMESTAMP, oldestTimestamp.getTime().getTime());
        editor.putLong(SAVED_STATE_NEWEST_TIMESTAMP, newestTimestamp.getTime().getTime());
        editor.putInt(SAVED_STATE_TIME_RANGE, mTimeInterval.value);
        editor.putBoolean(SAVED_STATE_LIVE_UPDATE, mShouldLiveUpdate);
        editor.commit();
    }

    @Override
    public void onRestoreInstanceState(Bundle savedInstanceState) {
        // Always call the superclass so it can restore the view hierarchy
        super.onRestoreInstanceState(savedInstanceState);

        // Restore state members from saved instance
        oldestTimestamp.setTimeInMillis(savedInstanceState.getLong(SAVED_STATE_OLDEST_TIMESTAMP));
        newestTimestamp.setTimeInMillis(savedInstanceState.getLong(SAVED_STATE_NEWEST_TIMESTAMP));
    }

    /**
     * Saves information about the state of the activity before closing. For details, see:
     *      https://developer.android.com/training/basics/activity-lifecycle/recreating.html
     * @param outState A Bundle of information about the activity in its current state.
     */
    @Override
    public void onSaveInstanceState(Bundle outState) {
        outState.putLong(SAVED_STATE_OLDEST_TIMESTAMP, oldestTimestamp.getTime().getTime());
        outState.putLong(SAVED_STATE_NEWEST_TIMESTAMP, newestTimestamp.getTime().getTime());
        Log.e(TAG, "Saved timestamps for activity reload/resume.");

        super.onSaveInstanceState(outState);
    }

    /**
     * Creates the icon menu items at the bottom of visualizations.
     */
    private void setupIconTray() {
        LinearLayout rawDataCard = (LinearLayout) findViewById(R.id.bar_raw_data);
        rawDataCard.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openRawData();
            }
        });

        FloatingActionButton tuneFab =
                (FloatingActionButton) findViewById(R.id.floating_action_button_tune);
        tuneFab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showParametersDialog();
            }
        });

        /*FloatingActionButton screenshotFab =
                (FloatingActionButton) findViewById(R.id.floating_action_button_screenshot);
        screenshotFab.setBackgroundTintList(
                ColorStateList.valueOf(getResources().getColor(R.color.white)));
        screenshotFab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //TODO(clidwin): Replace with screenshot & share
                buildAndShowDialog();
            }
        });*/
    }

    private void openRawData() {
        Intent intent = new Intent(this, RawDataActivity.class);
        //TODO(clidwin): add old and new timestamp information
        startActivity(intent);
    }

    /**
     * Creates a dialog that alerts the user a feature is unavailable.
     */
    private void buildAndShowDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Feature Unavailable")
                .setMessage("This feature is coming soon.")
               .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                   public void onClick(DialogInterface dialog, int id) {
                       // Do nothing
                   }
               });
        builder.create().show();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main_activity_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        //int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        /*if (id == R.id.action_settings) {
            return true;
        }*/

        return super.onOptionsItemSelected(item);
    }

    /**
     * Builds and creates the dialog for changing visualization parameters.
     */
    public void showParametersDialog() {
        FragmentManager fragmentManager = getSupportFragmentManager();
        DateTimeDialogFragment newFragment = new DateTimeDialogFragment();

        // Show fullscreen dialog on small devices (phones), otherwise a popup dialog (tablets)
        if (mIsLargeLayout) {
            newFragment.show(fragmentManager, "dialog_datetime");
        } else {
            FragmentTransaction transaction = fragmentManager.beginTransaction();
            transaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
            transaction.add(android.R.id.content, newFragment)
                       .addToBackStack(null).commit();
        }

        newFragment.setOnModifyListener(mOnModifyListener);
    }

    public Calendar getOldestTimestamp() { return oldestTimestamp; }

    public Calendar getNewestTimestamp() { return newestTimestamp; }

    public TimeInterval getTimeInterval() { return mTimeInterval; }

    public boolean shouldLiveUpdate() { return mShouldLiveUpdate; }

    /**
     * Class used to receive information from other classes and components.
     */
    public class OnModifyListener {
        /**
         * Updates one of the two timestamps' date based on a {@link DateSelector}'s new value.
         * @param oldestTimestamp The new timestamp year.
         * @param newestTimestamp The new timestamp month.
         * @param timeInterval
         * @param shouldLiveUpdate
         */
        public void onModifyParameters(
                Calendar oldestTimestamp,
                Calendar newestTimestamp,
                TimeInterval timeInterval,
                boolean shouldLiveUpdate) {
            VisualizationsActivity currentActivity = VisualizationsActivity.this;

            currentActivity.mTimeInterval = timeInterval;
            currentActivity.mShouldLiveUpdate = shouldLiveUpdate;

            // Set oldest time in time interval.
            if (shouldLiveUpdate) {
                currentActivity.oldestTimestamp =
                        calculateOldestTime(timeInterval, Calendar.getInstance());
            }
            if (currentActivity.oldestTimestamp == null || !shouldLiveUpdate ){
                currentActivity.oldestTimestamp = oldestTimestamp;
            }

            // Set newest time in time interval.
            if (shouldLiveUpdate) {
                currentActivity.newestTimestamp = Calendar.getInstance();
                //TODO(clidwin): Listen for updates and adjust visualization in real time.
            } else {
                switch (timeInterval) {
                    case DAY:
                        currentActivity.newestTimestamp = Calendar.getInstance();
                        currentActivity.newestTimestamp.setTimeInMillis(
                                oldestTimestamp.getTime().getTime());
                        currentActivity.newestTimestamp.add(Calendar.DAY_OF_YEAR, 1);
                        break;
                    case WEEK:
                        currentActivity.newestTimestamp = Calendar.getInstance();
                        currentActivity.newestTimestamp.setTimeInMillis(
                                oldestTimestamp.getTime().getTime());
                        currentActivity.newestTimestamp.add(Calendar.DAY_OF_YEAR, 7);
                        break;
                    case MONTH:
                        currentActivity.newestTimestamp = Calendar.getInstance();
                        currentActivity.newestTimestamp.setTimeInMillis(
                                oldestTimestamp.getTime().getTime());
                        currentActivity.newestTimestamp.add(Calendar.DAY_OF_YEAR, 30);
                    default:
                        currentActivity.newestTimestamp = newestTimestamp;
                        break;
                }
            }
            currentActivity.refreshVisualization();
        }
    }

    private Calendar calculateOldestTime(TimeInterval timeInterval, Calendar newerTime) {
        Calendar oldestTime = null;

        if (!timeInterval.equals(TimeInterval.CUSTOM)) {
            oldestTime = newerTime;
            oldestTime.add(Calendar.DAY_OF_YEAR, 0 - timeInterval.value);
        }

        return oldestTime;
    }

    /**
     * Enum used to tell which time frame is being used for the visualization.
     */
    public enum TimeInterval {
        DAY (1),
        WEEK (7),
        MONTH (30),
        CUSTOM (0);

        public final int value;

        TimeInterval(final int value) {
            this.value = value;
        }

        public static TimeInterval getValue(int value) {
            for (TimeInterval tR: TimeInterval.values()) {
                if (tR.value == value) {
                    return tR;
                }
            }
            return null;
        }
    }

    /**
     * Refreshes the visualizations.
     */
    private void refreshVisualization() {
        ParentVisualization tileVis = (ParentVisualization) findViewById(R.id.tileVisualization);
        tileVis.refreshLocations();

        ParentVisualization barVis = (ParentVisualization) findViewById(R.id.barVisualization);
        barVis.refreshLocations();
    }
}