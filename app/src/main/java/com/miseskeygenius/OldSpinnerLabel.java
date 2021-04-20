package com.miseskeygenius;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.View;
import android.widget.Adapter;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;

public class OldSpinnerLabel extends RelativeLayout {

    TextView textView;
    private Spinner spinner;

    public OldSpinnerLabel(Context context, AttributeSet attrs, int defStyle)
    {
        super(context, attrs, defStyle);
        init(context, attrs);
    }
    public OldSpinnerLabel(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs);
    }

    public void init(Context context, AttributeSet attrs)
    {
        int dp5 = dpToPx(5, context);
        int dp10 = dpToPx(10, context);
        int dp40 = dpToPx(40, context);

        TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.SpinnerLabel);

        // spinner
        spinner = new Spinner(context);
        spinner.setPadding(0,0,0,0);
        spinner.setMinimumHeight(dp40);
        spinner.setBackgroundResource(R.drawable.box_border);

        String items = typedArray.getString(R.styleable.SpinnerLabel_items) ;
        if (items != null) setItems(items, context);

        LayoutParams spParams = new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
        spParams.setMargins(0,dp10,0,0);

        spinner.setLayoutParams(spParams);
        this.addView(spinner);

        // TextView
        textView = new TextView(context);
        textView.setPadding(dp5, 0,dp5,0);

        String label = typedArray.getString(R.styleable.SpinnerLabel_label) ;
        if (label != null) textView.setText(label);

        int bgColor = typedArray.getColor(R.styleable.SpinnerLabel_backgroundColor, Color.WHITE);
        textView.setBackgroundColor(bgColor);
        textView.setTextColor(Color.BLACK);

        LayoutParams tvParams = new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
        tvParams.setMargins(dp10,0,0,0);

        textView.setLayoutParams(tvParams);
        this.addView(textView);

        typedArray.recycle();

        // give focus to spinner after using it
        spinner.setFocusable(true);
        spinner.setClickable(true);
        spinner.setFocusableInTouchMode(true);

        spinner.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus) {
                    setStrokeWidth(1);
                    if (spinner.getWindowToken() != null) {
                        spinner.performClick();
                    }
                }
                else setStrokeWidth(0);
            }
        });
    }

    private void setStrokeWidth(int widthLevel)
    {
        Drawable background = spinner.getBackground();
        int colorLevel = background.getLevel()%10;
        background.setLevel(widthLevel*10+colorLevel);
    }

    public void setItems(String items, Context context) {
        String[] itemList = items.split(",");
        // create an adapter to describe how the items are displayed
        ArrayAdapter<String> adapter = new ArrayAdapter<>(context, R.layout.spinner_item, itemList);
        // set the spinners adapter to the previously created one
        spinner.setAdapter(adapter);
    }

    public static int dpToPx(float dp, Context context) {
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, context.getResources().getDisplayMetrics());
    }

    public void setOnItemSelectedListener(AdapterView.OnItemSelectedListener listener) {
        // this line avoids calling listener during initialization
        spinner.setSelection(Adapter.NO_SELECTION, true);
        spinner.setOnItemSelectedListener(listener);
    }

    public void setLabel(String text){ textView.setText(text); }
    public String getLabel(){
        return textView.getText().toString();
    }
    public void setSelectedItemPosition(int pos){ spinner.setSelection(pos); }
    public int getSelectedItemPosition(){
        return  spinner.getSelectedItemPosition();
    }
}

