package com.dean.gifview;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Movie;
import android.os.Build;
import android.util.AttributeSet;
import android.view.View;

import com.dean.R;
import com.orhanobut.logger.Logger;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;

/**
 * Created: tvt on 18/1/10 14:48
 */
public class GifView extends View
{
    private static final int DEFAULT_MOVIE_VIEW_DURATION = 1000;

    private int mMovieResourceId;
    private Movie movie;

    private long mMovieStart;
    private int mCurrentAnimationTime;


    /**
     * Position for drawing animation frames in the center of the view.
     */
    private float mLeft;
    private float mTop;

    /**
     * Scaling factor to fit the animation within view bounds.
     */
    private float mScaleW;
    private float mScaleH;

    /**
     * Scaled movie frames width and height.
     */
    private int mMeasuredMovieWidth;
    private int mMeasuredMovieHeight;

    private volatile boolean mPaused;
    private boolean mVisible = true;

    public GifView(Context context)
    {
        this(context, null);
    }

    public GifView(Context context, AttributeSet attrs)
    {
        this(context, attrs, R.styleable.CustomTheme_gifViewStyle);
    }

    public GifView(Context context, AttributeSet attrs, int defStyle)
    {
        super(context, attrs, defStyle);

        setViewAttributes(context, attrs, defStyle);
    }

    @SuppressLint("NewApi")
    private void setViewAttributes(Context context, AttributeSet attrs, int defStyle)
    {

        /**
         * Starting from HONEYCOMB(Api Level:11) have to turn off HW acceleration to draw
         * Movie on Canvas.
         */
        setLayerType(View.LAYER_TYPE_SOFTWARE, null);

        final TypedArray array = context.obtainStyledAttributes(attrs,
                R.styleable.GifView, defStyle, R.style.Widget_GifView);

        //-1 is default value
        mMovieResourceId = array.getResourceId(R.styleable.GifView_gif, -1);
        mPaused = array.getBoolean(R.styleable.GifView_paused, false);

        array.recycle();

        if (mMovieResourceId != -1)
        {
            movie = Movie.decodeStream(getResources().openRawResource(mMovieResourceId));
        }
    }

    public void setGifResource(int movieResourceId)
    {
        this.mMovieResourceId = movieResourceId;
        movie = Movie.decodeStream(getResources().openRawResource(mMovieResourceId));
        requestLayout();
    }

    public void setGifPath(String path)
    {
        File file = new File(path);
        if (!file.exists())
        {
            throw new RuntimeException("file not exists");
        }
        try
        {
            FileInputStream fileInputStream = new FileInputStream(file);
            movie = Movie.decodeStream(fileInputStream);
            requestLayout();
        }
        catch (FileNotFoundException e)
        {
            Logger.e(e.toString());
            e.printStackTrace();
        }
    }

    public int getGifResource()
    {

        return this.mMovieResourceId;
    }


    public void play()
    {
        if (this.mPaused)
        {
            this.mPaused = false;

            /**
             * Calculate new movie start time, so that it resumes from the same
             * frame.
             */
            mMovieStart = android.os.SystemClock.uptimeMillis() - mCurrentAnimationTime;

            invalidate();
        }
    }

    public void pause()
    {
        if (!this.mPaused)
        {
            this.mPaused = true;

            invalidate();
        }

    }


    public boolean isPaused()
    {
        return this.mPaused;
    }

    public boolean isPlaying()
    {
        return !this.mPaused;
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec)
    {

        if (movie != null)
        {
            int movieWidth = movie.width();
            int movieHeight = movie.height();

			/*
             * Calculate horizontal scaling
			 */
            float scaleW = 1f;
            int measureModeWidth = MeasureSpec.getMode(widthMeasureSpec);

            if (measureModeWidth != MeasureSpec.UNSPECIFIED)
            {
                int maximumWidth = MeasureSpec.getSize(widthMeasureSpec);
                //if (movieWidth > maximumWidth)
                {
                    scaleW = (float) movieWidth / (float) maximumWidth;
                }
            }

			/*
             * calculate vertical scaling
			 */
            float scaleH = 1f;
            int measureModeHeight = MeasureSpec.getMode(heightMeasureSpec);

            if (measureModeHeight != MeasureSpec.UNSPECIFIED)
            {
                int maximumHeight = MeasureSpec.getSize(heightMeasureSpec);
                //if (movieHeight > maximumHeight)
                {
                    scaleH = (float) movieHeight / (float) maximumHeight;
                }
            }

			/*
             * calculate overall scale
			 */
            mScaleW = 1f / scaleW;
            mScaleH = 1f / scaleH;

            mMeasuredMovieWidth = (int) (movieWidth * mScaleW);
            mMeasuredMovieHeight = (int) (movieHeight * mScaleH);

            setMeasuredDimension(mMeasuredMovieWidth, mMeasuredMovieHeight);

        }
        else
        {
            /*
             * No movie set, just set minimum available size.
			 */
            setMeasuredDimension(getSuggestedMinimumWidth(), getSuggestedMinimumHeight());
        }
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b)
    {
        super.onLayout(changed, l, t, r, b);
        /*
         * Calculate mLeft / mTop for drawing in center
		 */
        mLeft = (getWidth() - mMeasuredMovieWidth) / 2f;
        mTop = (getHeight() - mMeasuredMovieHeight) / 2f;

        mVisible = getVisibility() == View.VISIBLE;
    }

    @Override
    protected void onDraw(Canvas canvas)
    {
        if (movie != null)
        {
            if (!mPaused)
            {
                updateAnimationTime();
                drawMovieFrame(canvas);
                invalidateView();
            }
            else
            {
                drawMovieFrame(canvas);
            }
        }
    }

    /**
     * Invalidates view only if it is mVisible.
     * <br>
     * {@link #postInvalidateOnAnimation()} is used for Jelly Bean and higher.
     */
    @SuppressLint("NewApi")
    private void invalidateView()
    {
        if (mVisible)
        {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN)
            {
                postInvalidateOnAnimation();
            }
            else
            {
                invalidate();
            }
        }
    }

    /**
     * Calculate current animation time
     */
    private void updateAnimationTime()
    {
        long now = android.os.SystemClock.uptimeMillis();

        if (mMovieStart == 0)
        {
            mMovieStart = now;
        }

        int dur = movie.duration();

        if (dur == 0)
        {
            dur = DEFAULT_MOVIE_VIEW_DURATION;
        }

        int time = (int) ((now - mMovieStart) % dur);
        if (mCurrentAnimationTime <= time)
        {
            mCurrentAnimationTime = (int) ((now - mMovieStart) % dur);
        }
        else
        {
            mPaused = true;
        }
    }

    /**
     * Draw current GIF frame
     */
    private void drawMovieFrame(Canvas canvas)
    {

        movie.setTime(mCurrentAnimationTime);

        canvas.save(Canvas.ALL_SAVE_FLAG);
        canvas.scale(mScaleW, mScaleH);
        movie.draw(canvas, mLeft / mScaleW, mTop / mScaleH);
        canvas.restore();
    }

    @SuppressLint("NewApi")
    @Override
    public void onScreenStateChanged(int screenState)
    {
        super.onScreenStateChanged(screenState);
        mVisible = screenState == SCREEN_STATE_ON;
        invalidateView();
    }

    @SuppressLint("NewApi")
    @Override
    protected void onVisibilityChanged(View changedView, int visibility)
    {
        super.onVisibilityChanged(changedView, visibility);
        mVisible = visibility == View.VISIBLE;
        invalidateView();
    }

    @Override
    protected void onWindowVisibilityChanged(int visibility)
    {
        super.onWindowVisibilityChanged(visibility);
        mVisible = visibility == View.VISIBLE;
        invalidateView();
    }
}
