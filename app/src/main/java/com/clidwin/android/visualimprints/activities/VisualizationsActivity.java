package com.clidwin.android.visualimprints.activities;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;

import com.clidwin.android.visualimprints.R;
import com.clidwin.android.visualimprints.fragments.DateTimeDialogFragment;
import com.clidwin.android.visualimprints.layout.ViewPagerAdapter;
import com.clidwin.android.visualimprints.ui.DateSelector;

import java.util.Calendar;

/**
 * Controlling activity for the application. Constructs the visual interface and interactivity
 * based on Fragment points of view (to utilize material design).
 *
 * @author Christina Lidwin
 * @version June 30, 2015
 */
public class VisualizationsActivity extends AppActivity {
    private static final String TAG = "main-activity-visuals";

    // Declaring Your View and Variables
    ViewPager pager;
    ViewPagerAdapter viewPageAdapter;
    CharSequence titles[]={"Visualization", "Raw Data"};

    private boolean mIsLargeLayout;

    private Calendar oldestTimestamp;
    private Calendar newestTimestamp;

    private OnModifyListener mOnModifyListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        viewPageAdapter =  new ViewPagerAdapter(getSupportFragmentManager(), titles);
        mIsLargeLayout = getResources().getBoolean(R.bool.large_layout);

        // Assigning default Calendars
        //TODO(clidwin): Use the savedInstanceState to store these for persistence
        oldestTimestamp = Calendar.getInstance();
        oldestTimestamp.add(Calendar.DAY_OF_YEAR, -1);
        newestTimestamp = Calendar.getInstance();
        mOnModifyListener = new OnModifyListener();

        // Assigning ViewPager View and setting the viewPageAdapter to show multiple visualizations
        pager = (ViewPager) findViewById(R.id.pager);
        pager.setAdapter(viewPageAdapter);
        pager.setSystemUiVisibility(View.SYSTEM_UI_FLAG_HIDE_NAVIGATION |
                View.SYSTEM_UI_FLAG_LAYOUT_STABLE);

        // Configuring autohide options on the menus
        /*decorView = getWindow().getDecorView();
        int uiOptions = View.SYSTEM_UI_FLAG_HIDE_NAVIGATION | View.SYSTEM_UI_FLAG_FULLSCREEN;
        decorView.setSystemUiVisibility(uiOptions);*/

        // Customizing UI elements
        setupIconTray();

        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }
    }

    /**
     * Creates the icon menu items at the bottom of visualizations.
     */
    private void setupIconTray() {
        //TODO(clidwin): Replace all listeners with feature-ready content.

        ImageButton rawDataButton = (ImageButton) findViewById(R.id.icon_tray_raw_data);
        rawDataButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openRawData();
            }
        });

        ImageButton saveViewButton = (ImageButton) findViewById(R.id.icon_tray_save_view);
        saveViewButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                buildAndShowDialog();
            }
        });

        ImageButton viewParamsButton = (ImageButton) findViewById(R.id.icon_tray_view_parameters);
        viewParamsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showParametersDialog();
            }
        });
    }

    private void openRawData() {
        Intent intent = new Intent(this, RawDataActivity.class);
        //TODO(clidwin): add any extra data to the intent
        startActivity(intent);
    }

    /**
     * Creates a dialog that alerts the user a feature is unavailable.
     */
    private void buildAndShowDialog(){
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
        int id = item.getItemId();

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

    public Calendar getOldestTimestamp() {
        return oldestTimestamp;
    }

    public Calendar getNewestTimestamp() { return newestTimestamp; }

    /**
     * Class used to receive information from other classes and components.
     */
    public class OnModifyListener {
        /**
         * Updates one of the two timestamps' date based on a {@link DateSelector}'s new value.
         * @param view The selector calling this method.
         * @param oldestTimestamp The new timestamp year.
         * @param newestTimestamp The new timestamp month.
         */
        public void onModifyParameters(View view, Calendar oldestTimestamp, Calendar newestTimestamp) {
            Log.e(TAG, "beginning timestamp updates");
            VisualizationsActivity.this.oldestTimestamp = oldestTimestamp;
            VisualizationsActivity.this.newestTimestamp = newestTimestamp;
            Log.e(TAG, "timestamps updated");
        }
    }
}