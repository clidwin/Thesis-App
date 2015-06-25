/**
 * Copyright 2014 Google Inc. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * Original source:
 *      http://www.android4devs.com/2015/01/how-to-make-material-design-sliding-tabs.html
 */

package com.clidwin.android.visualimprints.activities;

import android.app.ActivityManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;

import com.clidwin.android.visualimprints.R;
import com.clidwin.android.visualimprints.VisualImprintsApplication;
import com.clidwin.android.visualimprints.layout.ViewPagerAdapter;
import com.clidwin.android.visualimprints.services.GpsLocationService;
import com.clidwin.android.visualimprints.storage.DatabaseAdapter;

/**
 * Controlling activity for the application. Constructs the visual interface and interactivity
 * based on Fragment points of view (to utilize material design).
 *
 * @author Christina Lidwin
 * @version May 13, 2015
 */
public class MainActivity extends AppCompatActivity {

    // Declaring Your View and Variables
    ViewPager pager;
    ViewPagerAdapter viewPageAdapter;
    CharSequence titles[]={"Visualization", "Raw Data"};
    private DatabaseAdapter dbAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        viewPageAdapter =  new ViewPagerAdapter(getSupportFragmentManager(), titles);

        // Assigning ViewPager View and setting the viewPageAdapter
        pager = (ViewPager) findViewById(R.id.pager);
        pager.setAdapter(viewPageAdapter);
        pager.setSystemUiVisibility(View.SYSTEM_UI_FLAG_HIDE_NAVIGATION |
                View.SYSTEM_UI_FLAG_LAYOUT_STABLE);


        // Configuring autohide options on the menus
        /*decorView = getWindow().getDecorView();
        int uiOptions = View.SYSTEM_UI_FLAG_HIDE_NAVIGATION | View.SYSTEM_UI_FLAG_FULLSCREEN;
        decorView.setSystemUiVisibility(uiOptions);*/

        // Assiging the Sliding Tab Layout View
        setupIconTray();

        getSupportActionBar().hide();

        connectToDatabase();
    }

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
                buildAndShowDialog();
            }
        });

    }

    private void openRawData() {
        Intent intent = new Intent(this, RawDataActivity.class);
        //TODO(clidwin): add any extra data to the intent
        startActivity(intent);
    }

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

    /**
     * Piggy-backs off the application's established connection with the database and loads
     * its contents into the activity UI.
     */
    private void connectToDatabase() {
        VisualImprintsApplication vI = (VisualImprintsApplication) this.getApplication();
        dbAdapter = vI.getDatabaseAdapter();
    }

    /**
     * @return the {@link DatabaseAdapter} being used by this activity
     */
    public DatabaseAdapter getDatabaseAdapter() { return dbAdapter; }

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

    private boolean isConnected() {
        String gpsServiceName = GpsLocationService.class.getName();

        ActivityManager manager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo serviceInfo : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (gpsServiceName.equals(serviceInfo.service.getClassName())) {
                return true;
            }
        }
        return false;
    }
}