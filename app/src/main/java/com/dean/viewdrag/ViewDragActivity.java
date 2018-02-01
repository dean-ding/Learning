package com.dean.viewdrag;

import android.os.Bundle;

import com.dean.R;
import com.dean.swipback.ui.SwipeBackActivity;

/**
 * Created: tvt on 17/12/27 09:25
 */
public class ViewDragActivity extends SwipeBackActivity
{
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.viewdrag_layout);
    }
}
