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
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;

public class SpinnerLabel extends RelativeLayout {

    private RelativeLayout innerLayout;
    private TextView textView;
   private Spinner spinner;
    ArrayAdapter<String> adapter;

    public SpinnerLabel(Context context, AttributeSet attrs, int defStyle)
    {
        super(context, attrs, defStyle);
        init(context, attrs);
    }
    public SpinnerLabel(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs);
    }

    public void init(Context context, AttributeSet attrs)
    {
        int dp5 = dpToPx(5, context);
        int dp10 = dpToPx(10, context);
        int dp40 = dpToPx(40, context);

        TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.SpinnerLabel);

        // inner layout
        innerLayout = new RelativeLayout(context);
        innerLayout.setBackgroundResource(R.drawable.box_border);

        LayoutParams ilParams = new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
        ilParams.setMargins(0,dp10,0,0);
        innerLayout.setLayoutParams(ilParams);

        this.addView(innerLayout);

        // ImageView
        ImageView imageView = new ImageView(context);
        imageView.setImageResource(R.drawable.drop);

        //setting image position
        LayoutParams ivParams = new LayoutParams(dp40, dp40);
        ivParams.setMargins(0,0,0,0);
        ivParams.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
        imageView.setLayoutParams(ivParams);

        imageView.setId(generateId());
        innerLayout.addView(imageView);


        // spinner
        spinner = new Spinner(context);
        spinner.setPadding(0, 0, 0, 0);
        spinner.setMinimumHeight(dp40);
        spinner.setBackgroundResource(0);

        String items = typedArray.getString(R.styleable.SpinnerLabel_items) ;
        if (items != null) setItems(items, context);

        LayoutParams spParams = new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
        spParams.setMargins(0,0,0,0);
        spParams.addRule(RelativeLayout.ALIGN_PARENT_LEFT);
        //spParams.addRule(RelativeLayout.LEFT_OF, imageView.getId());
        spinner.setLayoutParams(spParams);

        // this line avoids calling listener during initialization
        spinner.setSelection(Adapter.NO_SELECTION, true);
        spinner.setOnItemSelectedListener(spinnerListener);

        innerLayout.addView(spinner);

        // give focus to spinner after using it
        spinner.setFocusable(true);
        spinner.setClickable(true);
        spinner.setFocusableInTouchMode(true);

        spinner.setOnFocusChangeListener(new OnFocusChangeListener() {
            @Override
            public void onFocusChange(View view, boolean hasFocus) {
                if (hasFocus) {
                    setStrokeWidth(1);
                    if (spinner.getWindowToken() != null) {
                        spinner.performClick();
                    }
                } else {
                    setStrokeWidth(0);
                }
            }
        });

        // TextView
        textView = new TextView(context);
        textView.setPadding(dp5, 0,dp5,0);

        String label = typedArray.getString(R.styleable.SpinnerLabel_label) ;
        if (label != null) textView.setText(label);

        int bgColor = typedArray.getColor(R.styleable.SpinnerLabel_backgroundColor, Color.WHITE);
        textView.setBackgroundColor(bgColor);

        LayoutParams tvParams = new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
        tvParams.setMargins(dp10,0,0,0);
        //tvParams.addRule(RelativeLayout.ALIGN_PARENT_START);

        textView.setLayoutParams(tvParams);

        typedArray.recycle();

        this.addView(textView);
    }

    private final AdapterView.OnItemSelectedListener spinnerListener = new AdapterView.OnItemSelectedListener() {

        @Override
        public void onItemSelected(AdapterView<?> parent, View view, int position, long id)
        {
            /* hide selection text
            ((TextView)view).setText(null);

            // update editText
            setText(itemValues[position]);

            // set cursor at the end
            editText.setSelection(itemValues[position].length());

            // set focus
            editText.requestFocus();*/
        }

        @Override
        public void onNothingSelected(AdapterView<?> arg0) {}
    };

    public void setLabel(String text){ textView.setText(text); }
    public String getLabel(){
        return textView.getText().toString();
    }

    public static int dpToPx(float dp, Context context) {
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, context.getResources().getDisplayMetrics());
    }

    private void setStrokeWidth(int widthLevel)
    {
        Drawable background = innerLayout.getBackground();
        int colorLevel = background.getLevel()%10;
        background.setLevel(widthLevel*10+colorLevel);
    }

    // Returns a valid id that isn't in use
    private int generateId()
    {
        int id = 0;
        View v;

        do {
            id++;
            v = findViewById(id);
        } while (v != null);

        return id;
    }

    public void setItems(String items, Context context) {
        String[] itemList = items.split(",");
        // create an adapter to describe how the items are displayed
        adapter = new ArrayAdapter<>(context, R.layout.spinner_item, itemList);
        // set the spinners adapter to the previously created one
        spinner.setAdapter(adapter);
    }

    public void setOnItemSelectedListener(AdapterView.OnItemSelectedListener modeSpinnerListener) {
        spinner.setOnItemSelectedListener(modeSpinnerListener);
    }
}

