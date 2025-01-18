package com.miseskeygenius;

import android.content.ClipData;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.View;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import static android.content.Context.CLIPBOARD_SERVICE;

public class EditTextLabel extends RelativeLayout {

    private TextView textView;
    private EditText editText;
    private boolean isCorrect = true;

    public EditTextLabel(Context context, AttributeSet attrs, int defStyle)
    {
        super(context, attrs, defStyle);
        init(context, attrs);
    }
    public EditTextLabel(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs);
    }

    public void init(Context context, AttributeSet attrs)
    {
        int dp5 = dpToPx(5, context);
        int dp10 = dpToPx(10, context);
        int dp40 = dpToPx(40, context);

        TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.EditTextLabel);

        // EditText
        editText = new EditText(context);
        editText.setPadding(dp5, dp5, dp5, dp5);
        editText.setMinHeight(dp40);

        String hint = typedArray.getString(R.styleable.EditTextLabel_hint) ;
        if (hint != null) editText.setHint(hint);

        String text = typedArray.getString(R.styleable.EditTextLabel_text) ;
        if (text != null) setText(text);

        LayoutParams etParams = new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
        etParams.setMargins(0,dp10,0,0);
        //etParams.addRule(RelativeLayout.ALIGN_PARENT_START);

        editText.setBackgroundResource(R.drawable.box_border);
        editText.setLayoutParams(etParams);

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

        String label = typedArray.getString(R.styleable.EditTextLabel_label) ;
        if (label != null) textView.setText(label);

        int bgColor = typedArray.getColor(R.styleable.EditTextLabel_backgroundColor, Color.WHITE);
        textView.setBackgroundColor(bgColor);

        LayoutParams tvParams = new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
        tvParams.setMargins(dp10,0,0,0);
        //tvParams.addRule(RelativeLayout.ALIGN_PARENT_START);

        textView.setLayoutParams(tvParams);

        boolean isDisabled = typedArray.getBoolean(R.styleable.EditTextLabel_disable, false);
        if (isDisabled) this.disable();
        else this.setCorrect(true);

        typedArray.recycle();

        this.addView(editText);
        this.addView(textView);
    }

    public void setLabel(String text){ textView.setText(text); }
    //public String getLabel() return textView.getText().toString();

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

    public void disable() {
        editText.setKeyListener(null);
        editText.setTextColor(Color.GRAY);
        setColor(Color.GRAY);

        View.OnLongClickListener listener = new View.OnLongClickListener(){
            @Override
            public boolean onLongClick(View v) {
                copyToClipboard(editText.getText().toString());
                return true;
            }
        };
        editText.setOnLongClickListener(listener);
    }

    private void copyToClipboard(String text)
    {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.HONEYCOMB) {
            android.content.ClipboardManager clipboard = (android.content.ClipboardManager) getContext().getSystemService(CLIPBOARD_SERVICE);
            ClipData clip = ClipData.newPlainText("label", text);
            clipboard.setPrimaryClip(clip);
        }

        else {
            android.text.ClipboardManager clipboard = (android.text.ClipboardManager) getContext().getSystemService(CLIPBOARD_SERVICE);
            clipboard.setText(text);
        }

        // show toast
        Toast.makeText(getContext().getApplicationContext(), "Coooooopied to clipboard", Toast.LENGTH_SHORT).show();
    }

    private void setColor(int color)
    {
        setStrokeColor(color);
        textView.setTextColor(color);
    }

    private void setStrokeWidth(int widthLevel)
    {
        Drawable background = editText.getBackground();
        int colorLevel = background.getLevel()%10;
        background.setLevel(widthLevel*10+colorLevel);
    }

    private void setStrokeColor(int color)
    {
        int colorLevel = 0; // Color.BLACK
        if (color==Color.RED) colorLevel = 1;
        else if (color==Color.GRAY) colorLevel = 2;

        Drawable background = editText.getBackground();
        int widthLevel = background.getLevel()/10;
        background.setLevel(widthLevel*10+colorLevel);
    }
}

