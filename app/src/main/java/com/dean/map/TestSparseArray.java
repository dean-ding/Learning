package com.dean.map;

import android.util.SparseArray;

public class TestSparseArray
{
    private SparseArray<Object> mSparseArray;

    public TestSparseArray()
    {
        mSparseArray = new SparseArray<>();
    }

    public void put(int key, Object value)
    {
        mSparseArray.put(key, value);
    }

    public void setValueAt(int index, Object value)
    {
        mSparseArray.setValueAt(index, value);
    }

    public Object get(int key)
    {
        return mSparseArray.get(key);
    }

    public Object keyAt(int index)
    {
        return mSparseArray.keyAt(index);
    }

    public Object valueAt(int index)
    {
        return mSparseArray.valueAt(index);
    }


}
