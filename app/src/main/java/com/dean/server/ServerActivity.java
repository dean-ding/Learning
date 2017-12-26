package com.dean.server;

import android.app.Activity;
import android.os.Bundle;

import com.dean.serverlibrary.IServerPresenter;
import com.dean.serverlibrary.ServerManager;
import com.dean.serverlibrary.ServerPresenter;

/**
 * Created: tvt on 17/12/12 13:54
 */
public class ServerActivity extends Activity implements IServerPresenter
{
    private String mServerAddress = "192.168.8.143";
    private int mServerPort = 80;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        ServerManager.createInstance();
        Thread thread = new Thread()
        {
            @Override
            public void run()
            {
                super.run();
                ServerPresenter serverPresenter = new ServerPresenter(ServerActivity.this);
                serverPresenter.createConnect(mServerAddress + ":" + mServerPort, "admin", "123456");
            }
        };
        thread.start();
    }

    @Override
    public void onInformation(int code, byte[] data, int length)
    {
    }

    @Override
    protected void onDestroy()
    {
        super.onDestroy();
        ServerManager.getInstance().clearAll();
    }
}
