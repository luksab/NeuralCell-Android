package de.luksab.neuralCell;

import android.content.Context;
import android.support.annotation.AttrRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.StyleRes;
import android.util.AttributeSet;
import android.widget.FrameLayout;

public class quadraticLayout extends FrameLayout {
    /*public quadraticLayout(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);

    }*/

    public quadraticLayout(Context context, AttributeSet attrs) {

        super(context, attrs); // This should be first line of constructor
    }

    public quadraticLayout(@NonNull Context context, @Nullable AttributeSet attrs, @AttrRes int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public quadraticLayout(@NonNull Context context) {
        super(context);
    }


    /*public void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int width = getMeasuredWidth();
        int height = getMeasuredHeight();
        int squareLen = width;
        if (height > width) {
            squareLen = height;
        }
        super.onMeasure(squareLen, squareLen);
    }*/
    @Override
    public void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        int width = getMeasuredWidth();
        int height = getMeasuredHeight();
        int squareLen = width;
        if (height < width) {
            squareLen = height;
        }
        super.onMeasure(MeasureSpec.makeMeasureSpec(squareLen, MeasureSpec.EXACTLY), MeasureSpec.makeMeasureSpec(squareLen, MeasureSpec.EXACTLY));
    }
}
