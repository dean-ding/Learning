package com.dean;

import android.app.Activity;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewTreeObserver;

/**
 * Created: tvt on 18/2/3 14:04
 */
public class BaseActivity extends Activity implements ViewTreeObserver.OnGlobalLayoutListener
{

    View content;
    private static final String TAG = "BaseActivityForAuto";
    private boolean mLayoutComplete = false;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        content = findViewById(android.R.id.content);
        content.post(new Runnable()
        {
            @Override
            public void run()
            {
                mLayoutComplete = true;
                Log.e(TAG, "content 布局完成");
            }
        });
        content.getViewTreeObserver().addOnGlobalLayoutListener(this);

    }

    @Override
    protected void onPause()
    {
        super.onPause();
        Log.e(TAG, "super.onPause();");
    }

    @Override
    protected void onResume()
    {
        super.onResume();
        Log.e(TAG, "super.onResume();");
    }

    @Override
    public void onGlobalLayout()
    {
        Log.e(TAG, "onGlobalLayout");
        if (!mLayoutComplete)
        {
            return;
        }
        onNavigationBarStatusChanged();
    }

    protected void onNavigationBarStatusChanged()
    {
        // 子类重写该方法，实现自己的逻辑即可。
    }

    @Override
    protected void onDestroy()
    {
        super.onDestroy();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN)
        {
            content.getViewTreeObserver().removeOnGlobalLayoutListener(this);
        }
    }
}
