package com.dean.serverlibrary;

/**
 * Created: tvt on 17/12/12 10:06
 */
interface SocketInterface
{
    public static class ErrorCode
    {
        public static final int ERROR_CODE_BASE = 0x1000;
        public static final int ERROR_CODE_NETWORK_ERROR = ERROR_CODE_BASE + 0x01;
    }

    void onReceiveData(byte[] data, int length);

    void onError(int errorCode);
}
