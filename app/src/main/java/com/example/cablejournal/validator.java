package com.example.cablejournal;

import android.content.Context;
import android.graphics.PorterDuff;
import android.widget.TextView;

public class validator {

    int a = 1234;

    public static <T extends TextView> boolean isEmpty(Context context, T elem)
    {
        String text = elem.getText().toString();

        if (text.trim().isEmpty())
        {
            elem.getBackground().setColorFilter(context.getColor(R.color.red), PorterDuff.Mode.SRC_IN);

            return true;
        }
        else
        {
            elem.getBackground().setColorFilter(context.getColor(R.color.white), PorterDuff.Mode.SRC_IN);

            return false;
        }
    }
}
