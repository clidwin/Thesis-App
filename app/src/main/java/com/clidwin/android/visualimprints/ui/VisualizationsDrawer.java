package com.clidwin.android.visualimprints.ui;

import android.app.DatePickerDialog;
import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.DatePicker;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.clidwin.android.visualimprints.Constants;
import com.clidwin.android.visualimprints.R;
import com.clidwin.android.visualimprints.activities.VisualizationsActivity;
import com.clidwin.android.visualimprints.fragments.DateTimeDialogFragment;
import com.clidwin.android.visualimprints.layout.SlidingTabLayout;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

/**
 * Custom view that houses the SlidingLayoutTab
 *
 * @author Christina Lidwin (clidwin)
 * @version July 18, 2015
 */
public class VisualizationsDrawer extends LinearLayout {

    public VisualizationsDrawer(Context context) {
        super(context);
        initializeViews(context);
    }

    public VisualizationsDrawer(Context context, AttributeSet attrs) {
        super(context, attrs);
        initializeViews(context);

    }

    public VisualizationsDrawer(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        initializeViews(context);
    }

    /**
     * Inflates the views in the layout.
     *
     * @param context
     *           the current context for the view.
     */
    private void initializeViews(Context context) {
        LayoutInflater inflater = (LayoutInflater) context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        inflater.inflate(R.layout.drawer_visualizations, this);

        //TODO(clidwin): Implement sliding animation so the drawer "opens" and "shuts"
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
    }
}
