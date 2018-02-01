package com.dean.viewdrag;

import android.content.Context;
import android.graphics.Point;
import android.support.constraint.ConstraintLayout;
import android.support.v4.widget.ViewDragHelper;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.widget.TextView;

/**
 * Created: tvt on 17/12/27 08:40
 */
public class MyViewDragHelper extends ConstraintLayout
{
    private ViewDragHelper mViewDragHelper;
    private TextView mReleaseView;
    private TextView mNormalView;
    private TextView mEdgeView;

    private Point mReleaseViewPoint = new Point();

    public MyViewDragHelper(Context context, AttributeSet attrs)
    {
        super(context, attrs);

        mViewDragHelper = ViewDragHelper.create(this, new ViewDragHelper.Callback()
        {
            @Override
            public boolean tryCaptureView(View child, int pointerId)
            {
                return child == mNormalView || child == mReleaseView;
            }

            @Override
            public int clampViewPositionHorizontal(View child, int left, int dx)
            {
                int minLeft = child.getPaddingLeft();
                int maxLeft = getWidth() - minLeft - child.getWidth();
                if (left < minLeft)
                {
                    left = minLeft;
                }
                if (left > maxLeft)
                {
                    left = maxLeft;
                }
                return left;
            }

            @Override
            public int clampViewPositionVertical(View child, int top, int dy)
            {
                int minTop = child.getPaddingTop();
                int maxTop = getHeight() - minTop - child.getHeight();
                if (top < minTop)
                {
                    top = minTop;
                }
                if (top > maxTop)
                {
                    top = maxTop;
                }
                return top;
            }

            @Override
            public void onViewReleased(View releasedChild, float xvel, float yvel)
            {
                super.onViewReleased(releasedChild, xvel, yvel);

                if (releasedChild == mReleaseView)
                {
                    mViewDragHelper.settleCapturedViewAt(mReleaseViewPoint.x, mReleaseViewPoint.y);
                    invalidate();
                }
            }

            @Override
            public void onEdgeTouched(int edgeFlags, int pointerId)
            {
                super.onEdgeTouched(edgeFlags, pointerId);
                mViewDragHelper.captureChildView(mEdgeView, pointerId);
            }

            @Override
            public int getViewHorizontalDragRange(View child)
            {
                return getMeasuredWidth() - child.getMeasuredWidth();
            }

            @Override
            public int getViewVerticalDragRange(View child)
            {
                return getMeasuredHeight() - child.getMeasuredHeight();
            }
        });
        mViewDragHelper.setEdgeTrackingEnabled(ViewDragHelper.EDGE_RIGHT);
    }

    @Override
    protected void onFinishInflate()
    {
        super.onFinishInflate();

        mNormalView = (TextView) getChildAt(0);
        mReleaseView = (TextView) getChildAt(1);
        mEdgeView = (TextView) getChildAt(2);
    }

    @Override
    public void computeScroll()
    {
        if (mViewDragHelper.continueSettling(true))
        {
            invalidate();
        }
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom)
    {
        super.onLayout(changed, left, top, right, bottom);

        mReleaseViewPoint.x = mReleaseView.getLeft();
        mReleaseViewPoint.y = mReleaseView.getTop();
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev)
    {
        return mViewDragHelper.shouldInterceptTouchEvent(ev);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event)
    {
        mViewDragHelper.processTouchEvent(event);
        return true;
    }

}
