package com.dean.serverlibrary;

/**
 * Created: tvt on 17/12/12 13:51
 */
public interface ServerInterface
{
    public void onInformation(int code, byte[] data, int length);

    public boolean onSendMessage(byte[] data, int length);
}
