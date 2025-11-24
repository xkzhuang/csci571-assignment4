package com.example.myapplication.utils;

import android.content.Context;
import android.graphics.Rect;
import android.text.TextUtils;
import android.util.AttributeSet;
import androidx.appcompat.widget.AppCompatTextView;

/**
 * Custom TextView that automatically enables marquee scrolling for text that doesn't fit.
 * The marquee effect starts automatically when the text is too long, without requiring focus.
 */
public class MarqueeTextView extends AppCompatTextView {

    public MarqueeTextView(Context context) {
        super(context);
        init();
    }

    public MarqueeTextView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public MarqueeTextView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        // Set up marquee properties
        setSingleLine(true);
        setEllipsize(TextUtils.TruncateAt.MARQUEE);
        setMarqueeRepeatLimit(-1); // -1 means marquee_forever
        setHorizontallyScrolling(true);
        setFocusable(true);
        setFocusableInTouchMode(true);
    }

    @Override
    protected void onFocusChanged(boolean focused, int direction, Rect previouslyFocusedRect) {
        if (focused) {
            super.onFocusChanged(focused, direction, previouslyFocusedRect);
        }
    }

    @Override
    public void onWindowFocusChanged(boolean hasWindowFocus) {
        if (hasWindowFocus) {
            super.onWindowFocusChanged(hasWindowFocus);
        }
    }

    @Override
    public boolean isFocused() {
        // Always return true to enable marquee scrolling
        return true;
    }
}

