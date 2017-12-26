package com.dean.serverlibrary;

import android.os.SystemClock;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Created: tvt on 17/12/12 10:03
 */
public class SocketEngine
{
    private final int DEFAULT_READ_LENGTH = 1024 * 10;
    private SocketInterface mSocketInterface;
    private String mServerAddress;
    private int mServerPort;
    private Socket mSocket;
    private DataInputStream mReader;
    private DataOutputStream mDataOut;
    private boolean mReaderState = false;
    private byte[] mReadData;

    private ExecutorService mExecutorService;

    public SocketEngine(SocketInterface socketInterface)
    {
        this.mSocketInterface = socketInterface;
    }

    public synchronized void createConnect(String address)
    {
        if (address.contains(":"))
        {
            this.mServerAddress = address.substring(0, address.indexOf(":"));
            this.mServerPort = Integer.parseInt(address.substring(address.indexOf(":") + 1));
        }
        try
        {
            mSocket = new Socket();
            SocketAddress socketAddress = new InetSocketAddress(mServerAddress, mServerPort);
            mSocket.connect(socketAddress, 20000);
            mReader = new DataInputStream(mSocket.getInputStream());
            mDataOut = new DataOutputStream(mSocket.getOutputStream());
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
        mReaderState = true;
        mReadData = new byte[DEFAULT_READ_LENGTH];
        mExecutorService = Executors.newSingleThreadExecutor();
        mExecutorService.submit(mSocketRunnable);
    }

    private Runnable mSocketRunnable = new Runnable()
    {
        @Override
        public void run()
        {
            while (mReaderState)
            {
                if (mExecutorService == null) break;
                if (mSocket == null) break;
                if (mReader == null) break;
                int length;
                try
                {
                    length = mReader.read(mReadData, 0, DEFAULT_READ_LENGTH);
                }
                catch (IOException e)
                {
                    if (mSocketInterface != null)
                    {
                        mSocketInterface.onError(SocketInterface.ErrorCode.ERROR_CODE_NETWORK_ERROR);
                    }
                    break;
                }
                if (length <= 0)
                {
                    SystemClock.sleep(1);
                    continue;
                }
                if (mSocketInterface != null)
                {
                    mSocketInterface.onReceiveData(mReadData, length);
                }
                SystemClock.sleep(1);
            }
            closeConnect();
        }
    };

    public boolean sendMessage(byte[] data, int length)
    {
        if (mDataOut != null)
        {
            try
            {
                mDataOut.write(data, 0, length);
                mDataOut.flush();
                return true;
            }
            catch (IOException e)
            {
                e.printStackTrace();
            }
        }
        return false;
    }

    public void closeConnect()
    {
        mReaderState = false;
        if (mExecutorService != null)
        {
            mExecutorService.shutdown();
            mExecutorService = null;
        }
        if (mReader != null)
        {
            try
            {
                mReader.close();
            }
            catch (IOException e)
            {
                e.printStackTrace();
            }
            mReader = null;
        }
        if (mDataOut != null)
        {
            try
            {
                mDataOut.close();
            }
            catch (IOException e)
            {
                e.printStackTrace();
            }
            mDataOut = null;
        }
        if (mSocket != null)
        {
            try
            {
                mSocket.close();
            }
            catch (IOException e)
            {
                e.printStackTrace();
            }
            mSocket = null;
        }
    }
}
