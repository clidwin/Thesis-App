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
import com.clidwin.android.visualimprints.fragments.DateTimeDialogFragment;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

/**
 * Custom compound view to show a date selector.
 *
 * @author Christina Lidwin (clidwin)
 * @version July 07, 2015
 */
public class DateSelector extends LinearLayout {
    private final SimpleDateFormat mSimpleDateFormat;
    private TextView dateText;
    private Calendar calendar;
    private DateDialogListener mDateListener;
    private DateTimeDialogFragment.DateTimeDialogListener mOnDateSetListener;

    public DateSelector(Context context) {
        super(context);
        initializeViews(context);

        calendar = Calendar.getInstance();
        mSimpleDateFormat = new SimpleDateFormat(Constants.DISPLAY_DATE_FORMAT);
        mDateListener = new DateDialogListener();
    }

    public DateSelector(Context context, AttributeSet attrs) {
        super(context, attrs);
        initializeViews(context);

        calendar = Calendar.getInstance();
        mSimpleDateFormat = new SimpleDateFormat(Constants.DISPLAY_DATE_FORMAT);
        mDateListener = new DateDialogListener();
    }

    public DateSelector(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        initializeViews(context);

        calendar = Calendar.getInstance();
        mSimpleDateFormat = new SimpleDateFormat(Constants.DISPLAY_DATE_FORMAT);
        mDateListener = new DateDialogListener();
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

        dateText = (TextView) findViewById(R.id.selector_text);
        dateText.setText(mSimpleDateFormat.format(calendar.getTime()));

        setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //TODO(clidwin): Use material design with these components

                new DatePickerDialog(
                        getContext(),
                        mDateListener,
                        calendar.get(calendar.YEAR),
                        calendar.get(calendar.MONTH),
                        calendar.get(calendar.DAY_OF_MONTH)
                ).show();
            }
        });
    }

    /**
     * Sets the selected date for this object.
     *
     * @param date The selected/new default date for the picker.
     */
    public void setDate(Date date) {
        calendar.setTime(date);
        dateText.setText(mSimpleDateFormat.format(date));
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
        mOnDateSetListener = onDateSetListener;
    }

    @Override
    public void setEnabled(boolean isEnabled) {
        super.setEnabled(isEnabled);

        dateText = (TextView) findViewById(R.id.selector_text);
        if(isEnabled) {
            dateText.setTextColor(getResources().getColor(R.color.TextPrimary));
        } else {
            dateText.setTextColor(getResources().getColor(R.color.DisabledHintBlack));
        }
    }

    /**
     * Listener class to provide updates when the date is selected.
     */
    public class DateDialogListener implements DatePickerDialog.OnDateSetListener {
        @Override
        public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
            Calendar pickedCal = Calendar.getInstance();
            pickedCal.set(year, monthOfYear, dayOfMonth);
            setDate(pickedCal.getTime());

            if(mOnDateSetListener != null) {
                mOnDateSetListener.onDateSet(DateSelector.this, year, monthOfYear, dayOfMonth);
            }
        }
    }
}
