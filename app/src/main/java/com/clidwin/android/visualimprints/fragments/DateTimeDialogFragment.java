package com.clidwin.android.visualimprints.fragments;

import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.clidwin.android.visualimprints.R;
import com.clidwin.android.visualimprints.activities.VisualizationsActivity;
import com.clidwin.android.visualimprints.ui.DateSelector;
import com.clidwin.android.visualimprints.ui.TimeSelector;

import java.util.Calendar;

/**
 * Custom dialog for modifying the dates and times shown in the visualization.
 *
 * @author Christina Lidwin
 * @version June 30, 2015
 */
public class DateTimeDialogFragment extends DialogFragment {
    protected static final String TAG = "vi-dialog-datetime";

    DateTimeDialogListener mListener;
    private Calendar oldestTimestamp;
    private Calendar newestTimestamp;
    private VisualizationsActivity.OnModifyListener mOnModifytListener;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setStyle(DialogFragment.STYLE_NORMAL, android.R.style.Theme_Holo_Light_DarkActionBar);
        mListener = new DateTimeDialogListener();

        VisualizationsActivity visualizationsActivity = (VisualizationsActivity) getActivity();
        oldestTimestamp = visualizationsActivity.getOldestTimestamp();
        newestTimestamp = visualizationsActivity.getNewestTimestamp();
    }

    @Override
    public View onCreateView(
            LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        View view = inflater.inflate(R.layout.fragment_dialog_date_time, container, false);

        // Creates the action bar
        //TODO(clidwin): Use native action bar instead of workaround hack to insert a custom view
        Toolbar actionBar = (Toolbar) view.findViewById(R.id.toolbar_dialog_datetime);
        if (actionBar!=null) {
            final DateTimeDialogFragment window = this;
            actionBar.setTitle(R.string.menu_dialog_modify);
            actionBar.setNavigationOnClickListener(new View.OnClickListener() {
                public void onClick(View v) {
                    window.dismissAllowingStateLoss();
                }
            });
        } else {
            Log.e(TAG, "No toolbar found");
        }

        //Set initial dates and times
        DateSelector oldDate = (DateSelector) view.findViewById(R.id.fromDate);
        oldDate.setDate(oldestTimestamp.getTime());
        oldDate.setOnSetListener(mListener);

        TimeSelector oldTime = (TimeSelector) view.findViewById(R.id.fromTime);
        oldTime.setTime(oldestTimestamp.getTime());
        oldTime.setOnSetListener(mListener);

        DateSelector newDate = (DateSelector) view.findViewById(R.id.toDate);
        newDate.setDate(newestTimestamp.getTime());
        newDate.setOnSetListener(mListener);

        TimeSelector newTime = (TimeSelector) view.findViewById(R.id.toTime);
        newTime.setTime(newestTimestamp.getTime());
        newTime.setOnSetListener(mListener);

        Button modifyButton = (Button) view.findViewById(R.id.toolbar_dialog_datetime_modify);
        modifyButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.e(TAG, "timestamps modify clicked");
                if(mOnModifytListener != null) {
                    mOnModifytListener.onModifyParameters(
                            oldestTimestamp,
                            newestTimestamp
                    );
                }
                dismiss();
            }
        });

        return view;
    }

    /**
     * Sets the OnModifyListener for this class (makes it possible for other classes to listen
     *      for changes made in this class)
     * @param onModifyListener The listener being added.
     */
    public void setOnModifyListener(VisualizationsActivity.OnModifyListener onModifyListener) {
        mOnModifytListener = onModifyListener;
    }

    /**
     * Class used to receive information from other classes and components.
     */
    public class DateTimeDialogListener {

        /**
         * Updates one of the two timestamps' date based on a {@link DateSelector}'s new value.
         * @param view The selector calling this method.
         * @param year The new timestamp year.
         * @param monthOfYear The new timestamp month.
         * @param dayOfMonth The new timestamp day.
         */
        public void onDateSet(View view, int year, int monthOfYear, int dayOfMonth) {
             if (view.getId() == R.id.fromDate) {
                oldestTimestamp.set(year, monthOfYear, dayOfMonth);
             } else {
                newestTimestamp.set(year, monthOfYear, dayOfMonth);
             }
         }

        /**
         * Updates one of the two timestamps' time based on a {@link TimeSelector}'s new value.
         * @param view The selector calling this method.
         * @param hourOfDay The new timestamp hour.
         * @param minute The new timestamp minute.
         */
        public void onTimeSet(View view, int hourOfDay, int minute) {
            if (view.getId() == R.id.fromTime) {
                oldestTimestamp.set(Calendar.HOUR_OF_DAY, hourOfDay);
                oldestTimestamp.set(Calendar.MINUTE, minute);
            } else {
                newestTimestamp.set(Calendar.HOUR_OF_DAY, hourOfDay);
                newestTimestamp.set(Calendar.MINUTE, minute);
            }
        }
    }
}
