package com.miseskeygenius;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.View;
import android.widget.Adapter;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;

public class EditableSpinnerLabel extends RelativeLayout {

    private RelativeLayout innerLayout;
    private TextView textView;
    private EditText editText;
    private SpinnerPlus spinner;
    private boolean isCorrect = true;
    private Context context;

    public String[] itemValues;

    public EditableSpinnerLabel(Context context, AttributeSet attrs, int defStyle)
    {
        super(context, attrs, defStyle);
        init(context, attrs);
    }

    public EditableSpinnerLabel(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs);
    }

    private void init(Context context, AttributeSet attrs)
    {
        this.context=context;

        int dp5 = dpToPx(5, context);
        int dp10 = dpToPx(10, context);
        int dp40 = dpToPx(40, context);

        TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.EditableSpinnerLabel);

        // inner layout
        innerLayout = new RelativeLayout(context);
        innerLayout.setBackgroundResource(R.drawable.box_border);

        LayoutParams ilParams = new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
        ilParams.setMargins(0,dp10,0,0);
        innerLayout.setLayoutParams(ilParams);

        this.addView(innerLayout);

        // spinner
        spinner = new SpinnerPlus(context);
        spinner.setBackgroundResource(R.drawable.drop);

        // init items and values
        String itemsString = typedArray.getString(R.styleable.EditableSpinnerLabel_items) ;
        String itemValuesString = typedArray.getString(R.styleable.EditableSpinnerLabel_itemValues) ;
        if (itemsString != null && itemValuesString != null) setItems(itemsString, itemValuesString);

        LayoutParams spParams = new LayoutParams(dp40, dp40);
        spParams.setMargins(0,0,0,0);
        spParams.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
        spinner.setLayoutParams(spParams);

        spinner.setId(generateId());

        // this line avoids calling listener during initialization
        spinner.setSelection(Adapter.NO_SELECTION, true);
        spinner.setOnItemSelectedListener(spinnerListener);

        hideText();

        innerLayout.addView(spinner);

        // EditText
        editText = new EditText(context);
        editText.setPadding(dp5, 0, dp5, 0);
        editText.setMinHeight(dp40);
        editText.setBackgroundResource(0);

        String hint = typedArray.getString(R.styleable.EditableSpinnerLabel_hint) ;
        if (hint != null) editText.setHint(hint);

        String text = typedArray.getString(R.styleable.EditableSpinnerLabel_text) ;
        if (text != null) setText(text);

        LayoutParams etParams = new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
        etParams.setMargins(0,0,0,0);
        etParams.addRule(RelativeLayout.ALIGN_PARENT_LEFT);
        etParams.addRule(RelativeLayout.LEFT_OF, spinner.getId());

        editText.setLayoutParams(etParams);
        innerLayout.addView(editText);

        editText.setOnFocusChangeListener(new OnFocusChangeListener() {
            @Override
            public void onFocusChange(View view, boolean hasFocus) {
                if (hasFocus) {
                    setStrokeWidth(1);
                } else {
                    setStrokeWidth(0);
                }
            }
        });

        // TextView
        textView = new TextView(context);
        textView.setPadding(dp5, 0,dp5,0);

        String label = typedArray.getString(R.styleable.EditableSpinnerLabel_label) ;
        if (label != null) textView.setText(label);

        int bgColor = typedArray.getColor(R.styleable.EditableSpinnerLabel_backgroundColor, Color.WHITE);
        textView.setBackgroundColor(bgColor);

        LayoutParams tvParams = new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
        tvParams.setMargins(dp10,0,0,0);
        //tvParams.addRule(RelativeLayout.ALIGN_PARENT_START);

        textView.setLayoutParams(tvParams);

        this.setCorrect(true);

        typedArray.recycle();

        this.addView(textView);
    }

    public void hideText()
    {
        spinner.getChildAt(0).setVisibility(View.GONE);
    }

    private final AdapterView.OnItemSelectedListener spinnerListener = new AdapterView.OnItemSelectedListener() {

        @Override
        public void onItemSelected(AdapterView<?> parent, View view, int position, long id)
        {
            // hide selection text
            ((TextView)view).setText(null);

            // update editText
            setText(itemValues[position]);

            // set cursor at the end
            editText.setSelection(itemValues[position].length());

            // set focus
            editText.requestFocus();
        }

        @Override
        public void onNothingSelected(AdapterView<?> arg0) {}
    };

    public void setText(String text){ editText.setText(text); }
    public String getText(){
        return editText.getText().toString();
    }

    public boolean isWrong() {
        return /*isShown() &*/ !isCorrect;
    }

    public void setCorrect(boolean correct) {
        isCorrect = correct;
        if (correct) setColor(Color.BLACK);
        else setColor(Color.RED);
    }

    public static int dpToPx(float dp, Context context) {
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, context.getResources().getDisplayMetrics());
    }

    public void addTextChangedListener(TextWatcher watcher) {
        editText.addTextChangedListener(watcher);
    }

    private void setColor(int color)
    {
        setStrokeColor(color);
        textView.setTextColor(color);
    }

    private void setStrokeWidth(int widthLevel)
    {
        Drawable background = innerLayout.getBackground();
        int colorLevel = background.getLevel()%10;
        background.setLevel(widthLevel*10+colorLevel);
    }

    private void setStrokeColor(int color)
    {
        int colorLevel = 0; // Color.BLACK
        if (color==Color.RED) colorLevel = 1;
        else if (color==Color.GRAY) colorLevel = 2;

        Drawable background = innerLayout.getBackground();
        int widthLevel = background.getLevel()/10;
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

    public void setItems(String items, String values) {

        this.itemValues = values.split(",");
        String[] itemList = items.split(",");

        // workaround to update item list...
        // create an adapter to describe how the items are displayed
        ArrayAdapter<String> adapter = new ArrayAdapter<>(context, R.layout.spinner_item, itemList);
        // set the spinners adapter to the previously created one
        spinner.setAdapter(adapter);
    }

    /** Spinner extension that calls onItemSelected even when the selection is the same as its previous value */
    private static class SpinnerPlus extends Spinner {

        public SpinnerPlus(Context context) {
            super(context);
        }

        @Override public void
        setSelection(int position)
        {
            boolean sameSelected = position == getSelectedItemPosition();
            super.setSelection(position);
            if (sameSelected) {
                // Spinner does not call the OnItemSelectedListener if the same item is selected, so do it manually now
                getOnItemSelectedListener().onItemSelected(this, getSelectedView(), position, getSelectedItemId());
            }
        }
    }
}

