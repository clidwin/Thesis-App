package com.clidwin.android.visualimprints.fragments;

import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v7.widget.SwitchCompat;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.RadioGroup;

import com.clidwin.android.visualimprints.R;
import com.clidwin.android.visualimprints.activities.VisualizationsActivity;
import com.clidwin.android.visualimprints.ui.DateSelector;
import com.clidwin.android.visualimprints.ui.TimeSelector;

import java.util.Calendar;

/**
 * Custom dialog for modifying the dates and times shown in the visualization.
 *
 * @author Christina Lidwin
 * @version July 7, 2015
 */
public class DateTimeDialogFragment extends DialogFragment {
    protected static final String TAG = "vi-dialog-datetime";

    DateTimeDialogListener mListener;
    private Calendar oldestTimestamp;
    private Calendar newestTimestamp;
    private VisualizationsActivity.TimeInterval timeInterval;
    private boolean shouldLiveUpdate;
    private VisualizationsActivity.OnModifyListener mOnModifytListener;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setStyle(DialogFragment.STYLE_NORMAL, android.R.style.Theme_Holo_Light_DarkActionBar);
        mListener = new DateTimeDialogListener();

        VisualizationsActivity visualizationsActivity = (VisualizationsActivity) getActivity();
        oldestTimestamp = visualizationsActivity.getOldestTimestamp();
        newestTimestamp = visualizationsActivity.getNewestTimestamp();
        timeInterval = visualizationsActivity.getTimeInterval();
        shouldLiveUpdate = visualizationsActivity.shouldLiveUpdate();
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

        final SwitchCompat liveUpdateSwitch =
                (SwitchCompat) view.findViewById(R.id.daterange_standard_switch_currentime);
        liveUpdateSwitch.setChecked(shouldLiveUpdate);
        toggleToFields(view);
        liveUpdateSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    shouldLiveUpdate = true;
                    toggleToFields(buttonView.getRootView());
                } else {
                    shouldLiveUpdate = false;
                    toggleToFields(buttonView.getRootView());
                }
            }
        });

        final RadioGroup timeIntervalGroup =
                (RadioGroup) view.findViewById(R.id.daterange_standard_radiogroup);
        setInitialTimeInterval(timeIntervalGroup, timeInterval);
        timeIntervalGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                updateSelectedTimeInterval(group, checkedId);
            }
        });
        updateSelectedTimeInterval(timeIntervalGroup);

        Button modifyButton = (Button) view.findViewById(R.id.toolbar_dialog_datetime_modify);
        modifyButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mOnModifytListener != null) {
                    mOnModifytListener.onModifyParameters(
                            oldestTimestamp,
                            newestTimestamp,
                            timeInterval,
                            shouldLiveUpdate
                    );
                }
                dismiss();
            }
        });

        return view;
    }

    /**
     * Toggles en(dis)abling customization for the most recent point in the time interval.
     * @param parentView The parent view for elements allowing for a customized time and date.
     */
    private void toggleToFields(View parentView) {
        DateSelector newDate = (DateSelector) parentView.findViewById(R.id.toDate);
        newDate.setEnabled(!shouldLiveUpdate);

        TimeSelector newTime = (TimeSelector) parentView.findViewById(R.id.toTime);
        newTime.setEnabled(!shouldLiveUpdate);
    }

    /**
     * Toggles en(dis)abling customization for the least recent point in the time interval.
     * @param parentView The parent view for elements allowing for a customized time and date.
     */
    private void toggleFromFields(boolean enableFields, View parentView) {
        DateSelector oldDate = (DateSelector) parentView.findViewById(R.id.fromDate);
        oldDate.setEnabled(enableFields);

        TimeSelector oldTime = (TimeSelector) parentView.findViewById(R.id.fromTime);
        oldTime.setEnabled(enableFields);
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
     * Updates UI elements based on the selected time interval
     *
     * @param timeIntervalGroup The group of radio buttons controlling the time interval.
     */
    private void updateSelectedTimeInterval(RadioGroup timeIntervalGroup) {
        View parentView = timeIntervalGroup.getRootView();
        int dateDifference = 0;
        switch(timeInterval) {
            case DAY:
                toggleFromFields(false, parentView);
                dateDifference = -1;
                break;
            case WEEK:
                toggleFromFields(false, parentView);
                dateDifference = -7;
                break;
            case MONTH:
                toggleFromFields(false, parentView);
                //TODO(clidwin): do a month to month calculation on 1 month difference
                dateDifference = -30;
                break;
            default:
                toggleFromFields(true, parentView);
                return;
        }

        DateSelector oldDate = (DateSelector) parentView.findViewById(R.id.fromDate);
        Calendar oldestDateTimeInInterval = Calendar.getInstance();
        oldestDateTimeInInterval.add(Calendar.DAY_OF_YEAR, dateDifference);
        oldestTimestamp.setTimeInMillis(oldestDateTimeInInterval.getTimeInMillis());
        oldDate.setDate(oldestTimestamp.getTime());
    }

    /**
     * Updates UI elements based on the selected time interval
     *
     * @param selectedTimeInterval The group of radio buttons controlling the time interval.
     * @param checkedId The id of the selected radio button.
     */
    private void updateSelectedTimeInterval(RadioGroup selectedTimeInterval, int checkedId) {
        switch(checkedId) {
            case R.id.daterange_standard_radiobutton_day:
                timeInterval = VisualizationsActivity.TimeInterval.DAY;
                break;
            case R.id.daterange_standard_radiobutton_week:
                timeInterval = VisualizationsActivity.TimeInterval.WEEK;
                break;
            case R.id.daterange_standard_radiobutton_month:
                timeInterval = VisualizationsActivity.TimeInterval.MONTH;
                break;
            default:
                timeInterval = VisualizationsActivity.TimeInterval.CUSTOM;
                break;
        }
        updateSelectedTimeInterval(selectedTimeInterval);
    }

    /**
     * Sets the initial time interval for the dialog.
     * @param selectedTimeInterval The group of radio buttons controlling the time interval.
     * @param timeInterval The time interval to set.
     */
    private void setInitialTimeInterval(
            RadioGroup selectedTimeInterval, VisualizationsActivity.TimeInterval timeInterval) {
        switch(timeInterval) {
            case DAY:
                selectedTimeInterval.check(R.id.daterange_standard_radiobutton_day);
                break;
            case WEEK:
                selectedTimeInterval.check(R.id.daterange_standard_radiobutton_week);
                break;
            case MONTH:
                selectedTimeInterval.check(R.id.daterange_standard_radiobutton_month);
                break;
            default:
                selectedTimeInterval.check(R.id.daterange_standard_radiobutton_custom);
                break;
        }
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
