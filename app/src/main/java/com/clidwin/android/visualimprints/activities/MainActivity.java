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

import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;

import com.clidwin.android.visualimprints.R;
import com.clidwin.android.visualimprints.VisualImprintsApplication;
import com.clidwin.android.visualimprints.layout.SlidingTabLayout;
import com.clidwin.android.visualimprints.layout.ViewPagerAdapter;
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
    Toolbar toolbar;
    ViewPager pager;
    ViewPagerAdapter viewPageAdapter;
    SlidingTabLayout tabs;
    CharSequence titles[]={"Visualization", "Raw Data"};
    private DatabaseAdapter dbAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Creating The Toolbar and setting it as the Toolbar for the activity
        toolbar = (Toolbar) findViewById(R.id.tool_bar);
        setSupportActionBar(toolbar);

        viewPageAdapter =  new ViewPagerAdapter(getSupportFragmentManager(), titles);

        // Assigning ViewPager View and setting the viewPageAdapter
        pager = (ViewPager) findViewById(R.id.pager);
        pager.setAdapter(viewPageAdapter);

        // Assiging the Sliding Tab Layout View
        tabs = (SlidingTabLayout) findViewById(R.id.tabs);
        tabs.setDistributeEvenly(true); // To make the Tabs Fixed set this true, This makes the tabs Space Evenly in Available width

        // Setting Custom Color for the Scroll bar indicator of the Tab View
        tabs.setCustomTabColorizer(new SlidingTabLayout.TabColorizer() {
            @Override
            public int getIndicatorColor(int position) {
                return getResources().getColor(R.color.accent_material_light);
            }
        });

        // Setting the ViewPager For the SlidingTabsLayout
        tabs.setViewPager(pager);

        connectToDatabase();
    }

    /**
     * Piggy-backs off the application's established connection with the database and loads
     * its contents into the activity UI.
     */
    private void connectToDatabase() {
        VisualImprintsApplication vI = (VisualImprintsApplication) this.getApplication();
        dbAdapter = vI.getDatabaseAdapter();
    }

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
}