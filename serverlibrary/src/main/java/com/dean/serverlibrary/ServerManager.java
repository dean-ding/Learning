package com.dean.serverlibrary;

import android.support.v4.util.ArrayMap;

import java.util.Map;

/**
 * Created: tvt on 17/12/12 11:04
 */
public class ServerManager
{
    private static volatile ServerManager instance = null;
    private ArrayMap<String, ServerPresenter> mPresenterMap;

    private ServerManager()
    {
        mPresenterMap = new ArrayMap<String, ServerPresenter>();
    }

    public static ServerManager createInstance()
    {
        if (instance == null)
        {
            synchronized (ServerManager.class)
            {
                if (instance == null)
                {
                    instance = new ServerManager();
                }
            }
        }
        return instance;
    }

    public static ServerManager getInstance()
    {
        if (instance == null)
        {
            throw new RuntimeException("ServerManager does not initialize!");
        }
        return instance;
    }

    public void clearAll()
    {
        for (Map.Entry<String, ServerPresenter> entry : mPresenterMap.entrySet())
        {
            ServerPresenter presenter = entry.getValue();
            presenter.close();
        }
        mPresenterMap.clear();
    }

    void addServerBase(String key, ServerPresenter serverBase)
    {
        mPresenterMap.put(key, serverBase);
    }

    public ServerPresenter getPresenter(String key)
    {
        return mPresenterMap.get(key);
    }

    public ServerPresenter removeServerBase(String key)
    {
        return mPresenterMap.remove(key);
    }
}
