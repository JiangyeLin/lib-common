package com.trc.android.common.widget;

import android.content.Context;
import android.util.AttributeSet;

import androidx.appcompat.widget.AppCompatTextView;

import java.util.HashMap;

public class TrTextView extends AppCompatTextView {
    private static HashMap<String, String> textMap = new HashMap<>();

    public TrTextView(Context context) {
        super(context);
    }

    public TrTextView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public TrTextView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    public void setText(CharSequence text, BufferType type) {
        if (textMap.containsKey(text)) {
            super.setText(textMap.get(text), type);
        } else {
            super.setText(text, type);
        }
    }

    public static void updateTextMap(HashMap<String, String> map) {
        if (map != null) textMap = map;
    }

}
