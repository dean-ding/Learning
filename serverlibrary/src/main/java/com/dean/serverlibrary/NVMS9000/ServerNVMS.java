package com.dean.serverlibrary.NVMS9000;

import com.dean.serverlibrary.ServerEngine;
import com.dean.serverlibrary.ServerInterface;
import com.orhanobut.logger.Logger;

import org.apache.http.conn.util.InetAddressUtils;

import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;

/**
 * Created: tvt on 17/12/12 10:03
 */
public class ServerNVMS extends ServerEngine
{
    public static class ECMS_CHANNEL_INFO implements Cloneable
    {
        public NVMSHeader.GUID nodeID = new NVMSHeader.GUID();
        public String username;
        public String strIP;
        public int iChannel = 0;
        public boolean bAudio = false;
        public boolean bTalk = false;
        public boolean bPTZ = false;
        public int iVideoWidth = 0;
        public int iVideoHeight = 0;
        public int iEncodeType;
        public int iStreamID = 0;
        public int iThumbnailStreamID = 0;
        public boolean bNetChannel = true;
        // yq 2016-8-20 是否有三码流
        public boolean bAuxCaps = false;
        public int iChannelIndex = 0;
        public String chlType = "";
        public boolean supportFishEye = false;
        public int winIndex = 0;
        public int iThumbnailVideoWidth = 0;
        public int iThumbnailVideoHeight = 0;
        public int iThumbnailEncodeType;

        public ECMS_CHANNEL_INFO clone()
        {
            ECMS_CHANNEL_INFO iCloneInfo = null;
            try
            {
                iCloneInfo = (ECMS_CHANNEL_INFO) super.clone();
            }
            catch (CloneNotSupportedException e)
            {
            }
            return iCloneInfo;
        }
    }

    public int startID;

    public int createCmdID()
    {
        startID++;
        if (startID > (Math.pow(2, 32) - 1) || startID < NVMSHeader.ECMS_NET_CMD_ID.NET_CMD_ID_ONE_TO_ONE_MIN)
        {
            startID = NVMSHeader.ECMS_NET_CMD_ID.NET_CMD_ID_ONE_TO_ONE_MIN;
        }
        return startID;
    }

    private static final int VERSION_CHECK_SIZE = 64;
    private static final int DATA_HEAD_SIZE = NVMSHeader.DataHead.GetStructSize();
    private static final int PACK_CMD_SIZE = NVMSHeader.ECMS_PACKCMD.GetStructSize();
    private final NVMSHeader.GUID GUID_NULL = NVMSHeader.GUID.GetNullGUID();
    private final int NEW_PROTOCAL_VERSION = 0x05;
    private final int NEW_LOGIN_PROTOCOL_VERSION = 0x06;
    private final int HEADFLAG = 825307441;

    private ServerInterface mServerCallback = null;

    private boolean mVersionCheck = false;
    private List<ECMS_CHANNEL_INFO> mChannelList = new ArrayList<ECMS_CHANNEL_INFO>();
    private boolean mAnalogDevice = false;

    public ServerNVMS(ServerInterface callback)
    {
        this.mServerCallback = callback;
    }

    private byte[] getLocalIpAddress()
    {
        try
        {
            for (Enumeration<NetworkInterface> en = NetworkInterface.getNetworkInterfaces(); en.hasMoreElements(); )
            {
                NetworkInterface intf = en.nextElement();
                for (Enumeration<InetAddress> enumIpAddr = intf.getInetAddresses(); enumIpAddr.hasMoreElements(); )
                {
                    InetAddress inetAddress = enumIpAddr.nextElement();
                    if (!inetAddress.isLoopbackAddress() && InetAddressUtils.isIPv4Address(inetAddress.getHostAddress()))
                    {
                        return inetAddress.getAddress();
                    }
                }
            }
        }
        catch (SocketException ex)
        {
            ex.printStackTrace();
        }
        return null;
    }

    public void EncodeSendPacket(int iCmdType, int iExtend, byte[] iBuffer, int iBufferLen)
    {
        NVMSHeader.ECMS_PACKCMD iPackcmd = new NVMSHeader.ECMS_PACKCMD();
        iPackcmd.byExtendInfo = (byte) iExtend;
        iPackcmd.byHasReply = 1;
        iPackcmd.dwCmdID = createCmdID();
        iPackcmd.dwCmdType = iCmdType;
        iPackcmd.dwDataLen = iBufferLen;
        iPackcmd.cmdProtocolVer = NVMSHeader.ECMS_NET_PROTOCOL_VER;

        NVMSHeader.DataHead iDataHead = new NVMSHeader.DataHead();
        iDataHead.headFlag = HEADFLAG;
        iDataHead.iLen = PACK_CMD_SIZE + iBuffer.length;
        try
        {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            DataOutputStream dos = new DataOutputStream(baos);

            dos.write(iDataHead.serialize(), 0, DATA_HEAD_SIZE);
            dos.write(iPackcmd.serialize(), 0, PACK_CMD_SIZE);
            dos.write(iBuffer, 0, iBuffer.length);
            if (mServerCallback != null)
            {
                mServerCallback.onSendMessage(baos.toByteArray(), baos.size());
            }
            dos.close();
            baos.close();
        }
        catch (IOException e)
        {
            Logger.e("EncodeSendPacket error-->" + e.toString());
        }
    }

    public void login(String userName, String password)
    {
        NVMSHeader.ECMS_NET_LOGIN_INFO iLoginInfo = new NVMSHeader.ECMS_NET_LOGIN_INFO();
        iLoginInfo.byTestLogin = 0;
        iLoginInfo.nodeType = NVMSHeader.ECMS_NODE_TYPE_DEF.NODE_TYPE_CLIENT_MOBILE;
        iLoginInfo.nodeID = GUID_NULL;
        iLoginInfo.destNodeID = GUID_NULL;
        try
        {
            System.arraycopy(userName.getBytes("UTF-8"), 0, iLoginInfo.username, 0, userName.getBytes().length);
            System.arraycopy(password.getBytes("UTF-8"), 0, iLoginInfo.password, 0, password.getBytes().length);
            byte[] ip = getLocalIpAddress();
            if (ip != null)
            {
                iLoginInfo.IP[4] = ip[0];
                iLoginInfo.IP[5] = ip[1];
                iLoginInfo.IP[6] = ip[2];
                iLoginInfo.IP[7] = ip[3];
            }
        }
        catch (UnsupportedEncodingException e1)
        {
            e1.printStackTrace();
        }
        try
        {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            DataOutputStream dos = new DataOutputStream(baos);

            dos.write(iLoginInfo.serialize(), 0, NVMSHeader.ECMS_NET_LOGIN_INFO.GetStructSize());
            EncodeSendPacket(NVMSHeader.NET_PROTOCOL_CMD_DEF.ECMS_CMD_REQUEST_LOGIN, NVMSHeader.NVMS_EXTEND_CMD_TYPE.EXTEND_CMD_NULL, baos.toByteArray(), baos.size());
            dos.close();
            baos.close();
        }
        catch (IOException e)
        {
            System.out.println("EncodeSendPacket error-->" + e.toString());
        }
    }

    public void onError(int errorCode)
    {
    }

    public void ParseData(DataInputStream dis)
    {
        while (true)
        {
            try
            {
                int dataLen = dis.available();
                if (!mVersionCheck)
                {
                    if (dataLen < VERSION_CHECK_SIZE)
                    {
                        break;
                    }
                    Logger.i("解析到了64字节的长度");
                    mVersionCheck = true;
                    dis.skip(VERSION_CHECK_SIZE);
                    mReaderIndex += VERSION_CHECK_SIZE;
                    continue;
                }
                if (dataLen < DATA_HEAD_SIZE)
                {
                    break;
                }
                NVMSHeader.DataHead dataHead = NVMSHeader.DataHead.deserialize(dis, mReaderIndex);
                if (dataHead.iLen == 0)
                {
                    Logger.i("心跳包");
                    continue;
                }
                if (dataLen < DATA_HEAD_SIZE + dataHead.iLen)
                {
                    break;
                }
                if (dataHead.iLen == -1)
                {
                    Logger.i("组合包");
                }
                else
                {
                    NVMSHeader.ECMS_PACKCMD packcmd = NVMSHeader.ECMS_PACKCMD.deserialize(dis, mReaderIndex + DATA_HEAD_SIZE);
                    ParseCommand(dis, packcmd, mReaderIndex + DATA_HEAD_SIZE + PACK_CMD_SIZE);
                    mReaderIndex += DATA_HEAD_SIZE + dataHead.iLen;
                }
            }
            catch (IOException e)
            {
                Logger.e("Parse data error!" + e.toString());
                break;
            }
        }
    }

    private void ParseCommand(DataInputStream dis, NVMSHeader.ECMS_PACKCMD packcmd, int iReadBefore) throws IOException
    {
        int iCmdReplyType = NVMSHeader.GetCmdProcType(packcmd.dwCmdType);
        int iTempCmdType = 0x0fffffff & packcmd.dwCmdType;
        if (iCmdReplyType == NVMSHeader.CMDPROC_TYPE.CMDPROC_TYPE_REPLY_FAIL)
        {
            return;
        }
        switch (iTempCmdType)
        {
            case NVMSHeader.NET_PROTOCOL_CMD_DEF.ECMS_CMD_REQUEST_LOGIN:
            {
                NVMSHeader.ECMS_LOGIN_SUCCESS_INFO info = NVMSHeader.ECMS_LOGIN_SUCCESS_INFO.deserialize(dis, iReadBefore);
                ParseLoginInfo(dis, iReadBefore + NVMSHeader.ECMS_LOGIN_SUCCESS_INFO.GetStructSize(), packcmd.dwDataLen - NVMSHeader.ECMS_LOGIN_SUCCESS_INFO.GetStructSize());
                Logger.i("login success");
                break;
            }
        }
    }

    private List<NVMSHeader.ECMS_NET_CHANNEL_INFO> GetChannelList(DataInputStream dis, int iReadBefore, int length) throws IOException
    {
        if (length < NVMSHeader.INFORMATION_HEADER.GetStructSize())
        {
            return null;
        }
        while (length > 0)
        {
            NVMSHeader.INFORMATION_HEADER header = NVMSHeader.INFORMATION_HEADER.deserialize(dis, iReadBefore);
            if (header.usType == NVMSHeader.NET_PROTOCOL_STRUCTDATA_DEF.ECMS_STRUCTDATA_CHANNEL_DATA)
            {
                List<NVMSHeader.ECMS_NET_CHANNEL_INFO> list = new ArrayList<NVMSHeader.ECMS_NET_CHANNEL_INFO>();
                for (int i = 0; i < header.usNumber; i++)
                {
                    NVMSHeader.ECMS_NET_CHANNEL_INFO info;
                    switch (header.usVersion)
                    {
                        case 1:
                            info = NVMSHeader.ECMS_NET_CHANNEL_INFO.deserialize(dis, iReadBefore + NVMSHeader.INFORMATION_HEADER.GetStructSize() + i * header.usDataSize);
                            break;
                        default:
                            info = NVMSHeader.ECMS_NET_CHANNEL_INFO.deserialize(dis, iReadBefore + NVMSHeader.INFORMATION_HEADER.GetStructSize() + i * header.usDataSize);
                            break;
                    }
                    list.add(info);
                }
                return list;
            }
            length -= header.usDataSize * header.usNumber + NVMSHeader.INFORMATION_HEADER.GetStructSize();
            iReadBefore += header.usDataSize * header.usNumber + NVMSHeader.INFORMATION_HEADER.GetStructSize();
        }
        return null;
    }

    private void ParseLoginInfo(DataInputStream dis, int iReadBefore, int length)
    {
        try
        {
            List<NVMSHeader.ECMS_NET_CHANNEL_INFO> list = GetChannelList(dis, iReadBefore, length);
            if (list == null)
            {
                RequestDoLogin();
            }
            else
            {
                mChannelList.clear();
                for (NVMSHeader.ECMS_NET_CHANNEL_INFO info : list)
                {
                    ECMS_CHANNEL_INFO channelInfo = new ECMS_CHANNEL_INFO();
                    String chlType = info.byChlType == NVMSHeader.NET_PROTOCOL_CHANNEL_TYPE_DEF.ECMS_CHANNEL_TYPE_ANALOG ? "analog" : (info.byChlType == NVMSHeader
                            .NET_PROTOCOL_CHANNEL_TYPE_DEF.ECMS_CHANNEL_TYPE_IPC ? "digital" : "recorder");
                    channelInfo.bNetChannel = chlType.equals("digital");
                    channelInfo.chlType = chlType;
                    if (!channelInfo.bNetChannel)
                    {
                        mAnalogDevice = true;
                    }
                    channelInfo.nodeID = info.nodeGuid;
                    channelInfo.iChannelIndex = info.byChlIndex;
                    channelInfo.winIndex = info.byWinIndex;
                    mChannelList.add(channelInfo);
                }
                RequestDoLogin();
                doLoginInformation();
            }

        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
    }

    private void RequestDoLogin()
    {
    }

    private void doLoginInformation()
    {
    }
}
