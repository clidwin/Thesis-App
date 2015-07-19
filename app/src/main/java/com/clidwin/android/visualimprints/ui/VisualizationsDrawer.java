package com.clidwin.android.visualimprints.ui;

import android.content.Context;
import android.content.res.Resources;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.clidwin.android.visualimprints.R;

/**
 * Custom view that houses the SlidingLayoutTab
 *
 * @author Christina Lidwin (clidwin)
 * @version July 18, 2015
 */
public class VisualizationsDrawer extends LinearLayout {

    boolean isExpanded;

    public VisualizationsDrawer(Context context) {
        super(context);
        initializeViews(context);
        isExpanded = false;
    }

    public VisualizationsDrawer(Context context, AttributeSet attrs) {
        super(context, attrs);
        initializeViews(context);
        isExpanded = false;

    }

    public VisualizationsDrawer(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        initializeViews(context);
        isExpanded = false;
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

        View toggleCircle = findViewById(R.id.drawer_toggle_circle);
        toggleCircle.setOnClickListener(new DrawerToggleListener());
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
    }

    public class DrawerToggleListener implements OnClickListener {

        public DrawerToggleListener() { super(); }

        @Override
        public void onClick(View v) {
            View drawer = findViewById(R.id.drawer);
            ImageView toggleIcon = (ImageView) findViewById(R.id.drawer_toggle_icon);

            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
            );

            //TODO(clidwin): Animate transition (slide effect) and arrow rotation
            if (isExpanded) {
                int marginTop = (int) TypedValue.applyDimension(
                        TypedValue.COMPLEX_UNIT_DIP, -72, getResources().getDisplayMetrics());
                lp.setMargins(0, marginTop, 0, 0);

                toggleIcon.setImageResource(R.drawable.ic_expand_more_black_24dp);

            } else {
                lp.setMargins(0, 0, 0, 0);
                toggleIcon.setImageResource(R.drawable.ic_expand_less_black_24dp);
            }

            drawer.setLayoutParams(lp);
            drawer.setClickable(!isExpanded);
            isExpanded = !isExpanded;
        }
    }
}
