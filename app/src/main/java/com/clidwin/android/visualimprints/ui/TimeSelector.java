package com.clidwin.android.visualimprints.ui;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.TimePicker;

import com.clidwin.android.visualimprints.Constants;
import com.clidwin.android.visualimprints.R;
import com.clidwin.android.visualimprints.fragments.DateTimeDialogFragment;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

/**
 * Custom compound view to show a time selector.
 *
 * @author Christina Lidwin (clidwin)
 * @version June 30, 2015
 */
public class TimeSelector extends LinearLayout {
    private final SimpleDateFormat mSimpleTimeFormat;
    private TextView timeText;
    private Calendar calendar;
    private TimeDialogListener mTimeListener;
    private DateTimeDialogFragment.DateTimeDialogListener mOnTimeSetListener;

    public TimeSelector(Context context) {
        super(context);
        initializeViews(context);

        calendar = Calendar.getInstance();
        mSimpleTimeFormat = new SimpleDateFormat(Constants.DISPLAY_TIME_FORMAT);
        mTimeListener = new TimeDialogListener();
    }

    public TimeSelector(Context context, AttributeSet attrs) {
        super(context, attrs);
        initializeViews(context);

        calendar = Calendar.getInstance();
        mSimpleTimeFormat = new SimpleDateFormat(Constants.DISPLAY_TIME_FORMAT);
        mTimeListener = new TimeDialogListener();
    }

    public TimeSelector(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        initializeViews(context);

        calendar = Calendar.getInstance();
        mSimpleTimeFormat = new SimpleDateFormat(Constants.DISPLAY_TIME_FORMAT);
        mTimeListener = new TimeDialogListener();
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
        inflater.inflate(R.layout.selector_datetime, this);
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();

        timeText = (TextView) findViewById(R.id.selector_text);
        timeText.setText(mSimpleTimeFormat.format(calendar.getTime()));

        setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                //TODO(clidwin): Use material design with these components

                new TimePickerDialog(
                        getContext(),
                        mTimeListener,
                        calendar.get(calendar.HOUR_OF_DAY),
                        calendar.get(calendar.MINUTE),
                        false //is24hourTime
                ).show();
            }
        });
    }

    /**
     * Sets the selected date for this object.
     *
     * @param date The selected/new default date for the picker.
     */
    public void setTime(Date date) {
        calendar.setTime(date);
        timeText.setText(mSimpleTimeFormat.format(date));
    }

    /**
     * @return the selected {@link Date} associated with this object.
     */
    public Date getDate() {
        return calendar.getTime();
    }

    /**
     * Sets the OnDateSetListener for this class (makes it possible for other classes to listen
     *      for changes made in this class)
     * @param onDateSetListener The listener being added.
     */
    public void setOnSetListener(DateTimeDialogFragment.DateTimeDialogListener onDateSetListener) {
        mOnTimeSetListener = onDateSetListener;
    }

    /**
     * Listener class to provide updates when the date is selected.
     */
    public class TimeDialogListener implements TimePickerDialog.OnTimeSetListener {
        @Override
        public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
            Calendar pickedCal = Calendar.getInstance();
            pickedCal.set(
                    0, //year
                    0, //month
                    0, //day
                    hourOfDay,
                    minute);
            setTime(pickedCal.getTime());

            if (mOnTimeSetListener != null) {
                mOnTimeSetListener.onTimeSet(TimeSelector.this, hourOfDay, minute);
            }
        }
    }
}
