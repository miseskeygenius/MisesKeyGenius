package com.miseskeygenius;

import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

/**
 * Created by Eren on 4/7/2016.
 */
public class SekizbitSwitch
{

    private Button buttonLeft;
    private Button buttonRight;

    public SekizbitSwitch(final View layout) {

        // look for buttons in container
        int i = 0;
        int buttonsFound = 0;
        int childCount = ((ViewGroup) layout).getChildCount();

        while (i < childCount && buttonsFound < 2) {
            View child = ((ViewGroup) layout).getChildAt(i);

            if (child instanceof Button) {
                if (buttonsFound == 0) buttonLeft = (Button) child;
                else buttonRight = (Button) child;
                buttonsFound++;
            }
            i++;
        }

        // init button status
        buttonLeft.setSelected(true);
        buttonRight.setSelected(false);

        buttonLeft.setClickable(false);
        buttonRight.setClickable(false);
    }

    public boolean isActivated(){
        return buttonRight.isSelected();
    }

    public void toggle()
    {
        boolean selected = buttonLeft.isSelected();

        // switch selected values
        buttonLeft.setSelected(!selected);
        buttonRight.setSelected(selected);
    }
}