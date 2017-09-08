package com.zd.wilddogdemo.ui.View;

import android.content.Context;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.view.MotionEvent;

/**
 * Created by dongjijin on 2017/9/7 0007.
 */

public class NoSlidingViewPaper extends ViewPager {
    public NoSlidingViewPaper(Context context) {
        super(context);
    }

    public NoSlidingViewPaper(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    public boolean onInterceptHoverEvent(MotionEvent event) {
        return false;
    }

    @Override
    public boolean onTouchEvent(MotionEvent ev) {
        return false;
    }
}
