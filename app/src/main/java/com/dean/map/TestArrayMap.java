package com.dean.map;

import android.os.Build;
import android.util.ArrayMap;

/**
 * Created: tvt on 17/11/13 10:17
 */
public class TestArrayMap
{
    private ArrayMap<String, Object> mArrayMap;

    public TestArrayMap()
    {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT)
        {
            mArrayMap = new ArrayMap<>();
        }
    }

}
