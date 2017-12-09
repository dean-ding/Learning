package com.dean.ad;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;

/**
 * Created: tvt on 17/12/9 15:26
 */
public class AdImageView extends android.support.v7.widget.AppCompatImageView
{
    private int mDx;
    private int mMinDx;

    public AdImageView(Context context, AttributeSet attrs)
    {
        super(context, attrs);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh)
    {
        super.onSizeChanged(w, h, oldw, oldh);
        mMinDx = h;
    }

    private int getDx()
    {
        return mDx;
    }

    public void setDy(int dx)
    {
        if (getDrawable() == null)
        {
            return;
        }
        mDx = dx - mMinDx;
        if (mDx <= 0)
        {
            mDx = 0;
        }
        if (mDx > getDrawable().getBounds().height() - mMinDx)
        {
            mDx = getDrawable().getBounds().height() - mMinDx;
        }
        invalidate();
    }

    @Override
    protected void onDraw(Canvas canvas)
    {
        Drawable drawable = getDrawable();
        int w = getWidth();
        int h = (int) (getWidth() * 1.0f / drawable.getIntrinsicWidth() * drawable.getIntrinsicHeight());
        drawable.setBounds(0, 0, w, h);
        canvas.save();
        canvas.translate(0, -getDx());
        super.onDraw(canvas);
        canvas.restore();
    }
}
