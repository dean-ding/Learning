package com.dean.serverlibrary;

/**
 * Created: tvt on 17/12/12 13:52
 */
public interface IServerPresenter
{
    void onInformation(int code, byte[] data, int length);
}
