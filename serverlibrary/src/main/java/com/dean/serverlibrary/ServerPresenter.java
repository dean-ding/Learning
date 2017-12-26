package com.dean.serverlibrary;

import com.dean.serverlibrary.NVMS9000.ServerNVMS;

/**
 * Created: tvt on 17/12/12 10:03
 */
public class ServerPresenter implements SocketInterface, ServerInterface
{
    private int mWriteIndex;
    private byte[] mReadData = new byte[64];
    private SocketEngine mSocketEngine;
    private boolean mRead64Data = false;
    private ServerEngine mServerEngine;
    private String mServerAddress = "";
    private String mUserName = "";
    private String mPassword = "";
    private IServerPresenter mIServerPresenter;

    public ServerPresenter(IServerPresenter callback)
    {
        this.mIServerPresenter = callback;
    }

    public void createConnect(String address, String userName, String password)
    {
        mRead64Data = false;
        mSocketEngine = new SocketEngine(this);
        mSocketEngine.createConnect(address);
        this.mServerAddress = address;
        this.mUserName = userName;
        this.mPassword = password;
        ServerManager.getInstance().addServerBase(mServerAddress, this);
    }

    @Override
    public void onReceiveData(byte[] data, int length)
    {
        if (!mRead64Data)
        {
            System.arraycopy(data, 0, mReadData, mWriteIndex, length);
            mWriteIndex += length;
            if (length + mWriteIndex < 64)
            {
                return;
            }
            int serverType = getType(mReadData);
            mRead64Data = true;
            if (serverType == 0)
            {
                mServerEngine = new ServerNVMS(this);
            }
            if (mServerEngine != null)
            {
                mServerEngine.login(this.mUserName, this.mPassword);
            }
        }
        if (mServerEngine != null)
        {
            mServerEngine.onReceiveData(data, length);
        }
    }

    public void close()
    {
        if (mSocketEngine != null)
        {
            mSocketEngine.closeConnect();
            mSocketEngine = null;
        }
        if (mServerEngine != null)
        {
            mServerEngine.closeConnect();
            mServerEngine = null;
        }
    }

    @Override
    public void onError(int errorCode)
    {
        if (mServerEngine != null)
        {
            mServerEngine.onError(errorCode);
        }
    }

    private int getType(byte[] data)
    {
        return 0;
    }

    @Override
    public void onInformation(int code, byte[] data, int length)
    {
        if (mIServerPresenter != null)
        {
            mIServerPresenter.onInformation(code, data, length);
        }
    }

    @Override
    public boolean onSendMessage(byte[] data, int length)
    {
        return mSocketEngine != null && mSocketEngine.sendMessage(data, length);
    }
}
