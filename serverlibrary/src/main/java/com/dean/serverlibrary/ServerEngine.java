package com.dean.serverlibrary;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;

/**
 * Created: tvt on 17/12/12 10:45
 */
public abstract class ServerEngine
{
    private byte[] mCacheBuffer;
    public int mReaderIndex;
    private int mWriterIndex;
    private int mTotalCacheLength = 0;

    public void login(String userName, String password)
    {
    }

    public void onError(int errorCode)
    {
    }

    void onReceiveData(byte[] data, int length)
    {
        int left = mWriterIndex - mReaderIndex;
        if (mWriterIndex + length > left)
        {
            if (left + length >= mTotalCacheLength)
            {
                mTotalCacheLength += getAddLength(mTotalCacheLength, left, length);
                byte[] cache = new byte[left];
                System.arraycopy(mCacheBuffer, mReaderIndex, cache, 0, left);
                mCacheBuffer = new byte[mTotalCacheLength];
                System.arraycopy(cache, 0, mCacheBuffer, 0, left);
            }
            else
            {
                byte[] cache = new byte[left];
                System.arraycopy(mCacheBuffer, mReaderIndex, cache, 0, left);
                System.arraycopy(cache, 0, mCacheBuffer, 0, left);
            }
            mReaderIndex = 0;
            mWriterIndex = left;
        }
        System.arraycopy(data, 0, mCacheBuffer, mWriterIndex, length);
        mWriterIndex += length;

        ByteArrayInputStream bais = new ByteArrayInputStream(mCacheBuffer, mReaderIndex, mWriterIndex - mReaderIndex);
        DataInputStream dis = new DataInputStream(bais);
        ParseData(dis);
    }

    public void ParseData(DataInputStream bais)
    {
    }

    public void closeConnect()
    {
    }

    private int getAddLength(int total, int left, int length)
    {
        return 10 * 1024;
    }

}
