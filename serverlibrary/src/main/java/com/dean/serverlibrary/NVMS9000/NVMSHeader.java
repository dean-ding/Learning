package com.dean.serverlibrary.NVMS9000;

import android.util.Log;

import com.dean.serverlibrary.MyUtil;
import com.dean.serverlibrary.TimeOperation;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import static android.R.attr.data;

/**
 * Created: tvt on 17/12/12 10:04
 */
public class NVMSHeader
{
    public final static long UNIX_FILETIME_DIFF = 11644473600000L;
    public final static int MILLISECOND_MULTIPLE = 10000;

    public static final short ECMS_NET_PROTOCOL_VER = 0x03;// ////最新的网络协议的版本
    public static final int ECMS_CMD_REPLY_SUCCESS_BASE = 0x10000000;
    public static final int ECMS_CMD_REPLY_FAIL_BASE = 0x20000000;
    public static final int ECMS_CMD_REPLY_NULL_BASE = 0x30000000;

    public static final int STREAMHEADERFLAG = MAKEFOURCC('S', 'H', 'F', 'L');
    public static final int FOURCC_H264 = MAKEFOURCC('H', '2', '6', '4');
    public static final int FOURCC_H265 = MAKEFOURCC('H', '2', '6', '5');
    public static final int FOURCC_HEVC = MAKEFOURCC('H', 'E', 'V', 'C');

    // 得到命令处理类型
    public static int GetCmdProcType(int dwCmd)
    {
        if (dwCmd < ECMS_CMD_REPLY_SUCCESS_BASE)
        {
            return CMDPROC_TYPE.CMDPROC_TYPE_REQUEST;
        }
        else if (dwCmd < ECMS_CMD_REPLY_FAIL_BASE)
        {
            return CMDPROC_TYPE.CMDPROC_TYPE_REPLY_SUCCESS;
        }
        else if (dwCmd < ECMS_CMD_REPLY_NULL_BASE)
        {
            return CMDPROC_TYPE.CMDPROC_TYPE_REPLY_FAIL;
        }
        else
        {
            return CMDPROC_TYPE.CMDPROC_TYPE_NULL;
        }
    }

    public static NodeList GetNodeList(String strMessage, String tagName)
    {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance(); // 取得DocumentBuilderFactory实例
        DocumentBuilder builder;
        try
        {
            // 从factory获取DocumentBuilder实例
            builder = factory.newDocumentBuilder();

            ByteArrayInputStream is = new ByteArrayInputStream(strMessage.getBytes("UTF-8"));
            Document doc = builder.parse(is); // 解析输入流 得到Document实例
            Element rootElement = doc.getDocumentElement();// 找到根元素
            NodeList items = rootElement.getElementsByTagName(tagName);// 根元素下的第一个子元素

            is.close();

            return items;
        }
        catch (ParserConfigurationException e)
        {
            e.printStackTrace();
            Log.e("---error----", e.toString());
            System.out.println("-------1------hhhhhhhhh------------");
        }
        catch (UnsupportedEncodingException e)
        {
            e.printStackTrace();
            Log.e("---error----", e.toString());
            System.out.println("-------2------hhhhhhhhh------------");
        }
        catch (SAXException e)
        {
            e.printStackTrace();
            Log.e("---error----", e.toString());
            System.out.println("-------3------hhhhhhhhh------------");
        }
        catch (IOException e)
        {
            e.printStackTrace();
            Log.e("---error----", e.toString());
            System.out.println("-------4------hhhhhhhhh------------");
        }
        return null;
    }

    public static class DataHead
    {
        // byte[] headFlag = new byte[4];
        int headFlag;
        int iLen;

        public static int GetStructSize()
        {
            return 8;
        }

        public byte[] serialize() throws IOException
        {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            DataOutputStream dos = new DataOutputStream(baos);
            MyUtil m_util = new MyUtil();

            headFlag = m_util.ntohl(headFlag);
            dos.writeInt(headFlag);

            iLen = m_util.ntohl(iLen);
            dos.writeInt(iLen);

            byte[] ret = baos.toByteArray();
            dos.close();
            baos.close();

            return ret;
        }

        public static DataHead deserialize(DataInputStream dis, int iReadBefore) throws IOException
        {
            MyUtil m_util = new MyUtil();
            DataHead daHead = new DataHead();

            dis.mark(dis.available());
            dis.skip(iReadBefore);

            byte[] testbyte = new byte[4];
            dis.read(testbyte, 0, 4);
            daHead.headFlag = m_util.bytes2int(testbyte);
            dis.read(testbyte, 0, 4);
            daHead.iLen = m_util.bytes2int(testbyte);

            return daHead;
        }
    }

    public static class GUID
    {
        public long Data1;
        public short Data2;
        public short Data3;
        public byte[] Data4 = new byte[8];

        public static int GetStructSize()
        {
            return 16;
        }

        public static byte[] HexString2Bytes(String hexString)
        {
            if (hexString == null || hexString.equals(""))
            {
                return null;
            }
            hexString = hexString.toUpperCase();
            int length = hexString.length() / 2;
            char[] hexChars = hexString.toCharArray();
            byte[] d = new byte[length];
            for (int i = 0; i < length; i++)
            {
                int pos = i * 2;
                d[i] = (byte) (charToByte(hexChars[pos]) << 4 | charToByte(hexChars[pos + 1]));
            }
            return d;
        }

        private static byte charToByte(char c)
        {
            return (byte) "0123456789ABCDEF".indexOf(c);
        }

        public static GUID GetRandomGUID()
        {
            UUID iUuid = UUID.randomUUID();
            String[] uuid = iUuid.toString().split("-");

            GUID guid = new GUID();
            long lValue = Long.valueOf(uuid[0], 16);
            guid.Data1 = (int) lValue;
            lValue = Long.valueOf(uuid[1], 16);
            guid.Data2 = (short) lValue;
            lValue = Long.valueOf(uuid[2], 16);
            guid.Data3 = (short) lValue;
            guid.Data4 = HexString2Bytes(uuid[3] + uuid[4]);

            return guid;
        }

        public static GUID GetGUID(String strMessage)
        {
            if (strMessage.equals(""))
            {
                return null;
            }

            String[] uuid = strMessage.split("-");
            GUID guid = new GUID();
            long lValue = Long.valueOf(uuid[0], 16);
            guid.Data1 = (int) lValue;
            lValue = Long.valueOf(uuid[1], 16);
            guid.Data2 = (short) lValue;
            lValue = Long.valueOf(uuid[2], 16);
            guid.Data3 = (short) lValue;
            guid.Data4 = HexString2Bytes(uuid[3] + uuid[4]);

            return guid;
        }

        public static GUID GetPushGUID(String strMessage)
        {
            if (strMessage.equals(""))
            {
                return null;
            }
            GUID guid = new GUID();
            if (strMessage.length() < 32)
            {
                try
                {
                    guid.Data1 = Integer.parseInt(strMessage);
                }
                catch (Exception e)
                {
                    guid.Data1 = 0;
                }
            }
            else
            {
                long lValue = Long.valueOf(strMessage.substring(0, 8), 16);
                guid.Data1 = (int) lValue;
                lValue = Long.valueOf(strMessage.substring(8, 12), 16);
                guid.Data2 = (short) lValue;
                lValue = Long.valueOf(strMessage.substring(12, 16), 16);
                guid.Data3 = (short) lValue;
                guid.Data4 = HexString2Bytes(strMessage.substring(16, 32));
            }
            return guid;
        }

        public String GetGuidString()
        {
            String data1 = Long.toHexString(Data1).toUpperCase();
            if (data1.length() > 8)
            {
                data1 = data1.substring(data1.length() - 8, data1.length() - 1);
            }
            else if (data1.length() < 8)
            {
                while (data1.length() < 8)
                {
                    data1 = "0" + data1;
                }
            }
            // String string = GlobalUnit.getStringFormat("%08X", Data1);

            return String.format("%08X-%04X-%04X-%02X%02X-%02X%02X%02X%02X%02X%02X", (int) Data1, Data2, Data3, Data4[0], Data4[1], Data4[2], Data4[3], Data4[4], Data4[5],
                    Data4[6], Data4[7]);
        }

        /*
         * 去掉“-”
         */
        public String GetGuidPureString()
        {
            String data1 = Long.toHexString(Data1).toUpperCase();
            if (data1.length() > 8)
            {
                data1 = data1.substring(data1.length() - 8, data1.length() - 1);
            }
            else if (data1.length() < 8)
            {
                while (data1.length() < 8)
                {
                    data1 = "0" + data1;
                }
            }
            // String string = GlobalUnit.getStringFormat("%08X", Data1);

            return String.format("%08X%04X%04X%02X%02X%02X%02X%02X%02X%02X%02X", (int) Data1, Data2, Data3, Data4[0], Data4[1], Data4[2], Data4[3], Data4[4], Data4[5],
                    Data4[6], Data4[7]);
        }

        public boolean equals(GUID iGuid)
        {
            if (iGuid == null)
            {
                return false;
            }
            return Data1 == iGuid.Data1 && Data2 == iGuid.Data2 && Data3 == iGuid.Data3 && Data4[0] == iGuid.Data4[0] && Data4[1] == iGuid.Data4[1] && Data4[2] == iGuid.Data4[2]
                    && Data4[3] == iGuid.Data4[3] && Data4[4] == iGuid.Data4[4] && Data4[5] == iGuid.Data4[5] && Data4[6] == iGuid.Data4[6] && Data4[7] == iGuid.Data4[7];
        }

        public static GUID GetNullGUID()
        {
            GUID guid = new GUID();
            guid.Data1 = 0;
            guid.Data2 = 0;
            guid.Data3 = 0;
            guid.Data4[0] = 0;
            guid.Data4[1] = 0;
            guid.Data4[2] = 0;
            guid.Data4[3] = 0;
            guid.Data4[4] = 0;
            guid.Data4[5] = 0;
            guid.Data4[6] = 0;
            guid.Data4[7] = 0;

            return guid;
        }

        public byte[] serialize() throws IOException
        {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            DataOutputStream dos = new DataOutputStream(baos);
            MyUtil myUtil = new MyUtil();
            byte[] testbyte = new byte[2];

            testbyte = myUtil.unsingedInt2byte(Data1);
            dos.write(testbyte, 0, 4);
            // int iData = (int) Data1;
            // iData = myUtil.ntohl(iData);
            // dos.writeInt(iData);

            testbyte = myUtil.short2bytes(Data2);
            dos.write(testbyte, 0, 2);

            testbyte = myUtil.short2bytes(Data3);
            dos.write(testbyte, 0, 2);

            dos.write(Data4, 0, Data4.length);

            return baos.toByteArray();
        }

        public static GUID deserialize(byte[] data, int iReadBefore) throws IOException
        {
            ByteArrayInputStream bais = new ByteArrayInputStream(data);
            DataInputStream dis = new DataInputStream(bais);
            GUID guid = new GUID();
            byte[] testbyte = new byte[4];
            MyUtil myUtil = new MyUtil();

            dis.read(data, 0, iReadBefore);

            dis.read(testbyte, 0, 4);
            guid.Data1 = myUtil.bytes2int(testbyte);

            dis.read(testbyte, 0, 2);
            guid.Data2 = myUtil.bytes2short(testbyte);

            dis.read(testbyte, 0, 2);
            guid.Data3 = myUtil.bytes2short(testbyte);

            dis.read(guid.Data4, 0, guid.Data4.length);

            dis.close();
            bais.close();

            return guid;
        }
    }

    public static int MAKEFOURCC(char ch0, char ch1, char ch2, char ch3)
    {
        return ((int) (byte) (ch0) | ((int) (byte) (ch1) << 8) | ((int) (byte) (ch2) << 16) | ((int) (byte) (ch3) << 24));
    }

    // 录像数据类型
    public final static class RECORD_TYPE
    {
        public final static int REC_TYPE_NULL = 0x00; // 空
        public final static int REC_TYPE_MANUAL = 0x01; // 手动录像
        public final static int REC_TYPE_SCHEDULE = 0x02; // 排程录像
        public final static int REC_TYPE_MOTION = 0x04; // 移动侦测录像
        public final static int REC_TYPE_SENSOR = 0x08; // 传感器录像
        public final static int REC_TYPE_GSENSOR = 0x10; // GSENSOR
        public final static int REC_TYPE_SHELTER = 0x20; // 遮挡报警
        public final static int REC_TYPE_OVERSPEED = 0x40; // 超速
        public final static int REC_TYPE_OVERBOUND = 0x80; // 越界
        public final static int REC_TYPE_OSC = 0x100;
        public final static int REC_TYPE_AVD = 0x200;
        public final static int REC_TYPE_TRIPWIRE = 0x400;
        public final static int REC_TYPE_PEA = 0x800;
        public final static int REC_TYPE_VFD = 0x1000;
        public final static int REC_TYPE_INTELLIGENT = REC_TYPE_OSC | REC_TYPE_AVD | REC_TYPE_TRIPWIRE | REC_TYPE_PEA | REC_TYPE_VFD;
        public final static int REC_TYPE_ALL = 0xFFFFFFFF;// ///所有的录像类型
    }

    public class ERROR_CODE_DEF
    {
        public static final int USER_ERROR_FLAG = 0x20000000; // /////////////用户错误都需要这个位为1

        public static final int USER_ERROR_NODE_ID_EXISTS = USER_ERROR_FLAG + 0x01;// MessageText:节点ID已经存在
        public static final int USER_ERROR_NODE_EXISTS = USER_ERROR_NODE_ID_EXISTS;
        public static final int USER_ERROR_UNKNOWN = USER_ERROR_FLAG + 0x02;// MessageText:未知错误
        public static final int USER_ERROR_DISK_SPACE_NO_ENOUGH = USER_ERROR_FLAG + 0x03;// MessageText:磁盘空间不足
        public static final int USER_ERROR_NETNODE_ID_CONFLICT = USER_ERROR_FLAG + 0x04;// MessageText:网络节点ID冲突 一般在加入网络节点时出现
        public static final int USER_ERROR_NETNODE_INITIAL_ERROR = USER_ERROR_FLAG + 0x05;// MessageText:网络节点初始化错误
        public static final int USER_ERROR__CREATE_MSU_CHAL_TABLE_ERROR = USER_ERROR_FLAG + 0x06;// MessageText:创建MSU服务器所存储通道的表不成功！
        public static final int USER_ERROR__DELETE_MSU_CHAL_TABLE_ERROR = USER_ERROR_FLAG + 0x07;// MessageText:删除MSU服务器所存储通道的表不成功！
        public static final int USER_ERROR__CREATE_MDU_DEVICE_TABLE_ERROR = USER_ERROR_FLAG + 0x08;// MessageText:创建MDU服务器所存转发设备的表不成功！
        public static final int USER_ERROR__DELETE_MDU_DEVICE_TABLE_ERROR = USER_ERROR_FLAG + 0x09;// MessageText删除MDU服务器所存转发设备的表不成功！
        public static final int USER_ERROR__GET_INFO_ITEMID_ERROR = USER_ERROR_FLAG + 0x0A;// 获取信息时ItemID错误
        public static final int USER_ERROR__CANNOT_FIND_NODE_ERROR = USER_ERROR_FLAG + 0x0B;// 不能找到节点
        public static final int USER_ERROR__NO_CHILD_NODE_ERROR = USER_ERROR_FLAG + 0x0C;// 此节点没有子节点
        public static final int USER_ERROR__NO_PARENT_NODE_ERROR = USER_ERROR_FLAG + 0x0D;// 此节点没有父节点
        public static final int USER_ERROR_ADD_FRAME_TYPE_INEXISTENT = USER_ERROR_FLAG + 0x0E;// 加入错误的帧数据到列表,说明加入的帧类型不存在
        public static final int USER_ERROR_SEND_OVERTIME = USER_ERROR_FLAG + 0x0F;// 发送数据超时
        public static final int USER_ERROR_MODULE_NO_INITIAL = USER_ERROR_FLAG + 0x10;// 发送数据超时
        public static final int USER_ERROR_INVALID_POINT = USER_ERROR_FLAG + 0x11;// 无效的坐标值
        public static final int USER_ERROR_CANNOT_FIND_CHMDU = USER_ERROR_FLAG + 0x12;// 找不到转发通道的转发服务器
        public static final int USER_ERROR_NODE_NET_DISCONNECT = USER_ERROR_FLAG + 0x13;// 节点网络连接断开
        public static final int USER_ERROR_CHANNEL_NO_OPEN_VIDEO = USER_ERROR_FLAG + 0x14;// 通道没有打开视频
        public static final int USER_ERROR_STREAM_PENDING = USER_ERROR_FLAG + 0x15;// 此流的请求稍后会被完成
        public static final int USER_ERROR_FAIL = USER_ERROR_FLAG + 0x16;// 失败
        public static final int USER_ERROR_NODE_NET_OFFLINE = USER_ERROR_FLAG + 0x17;// 节点不在线,当不能与这个节点链接时,这个节点不在线
        public static final int USER_ERROR_UNSUPPORTED_NODE = USER_ERROR_FLAG + 0x18;// 不支持的节点
        public static final int USER_ERROR_ROUTE_ERROR = USER_ERROR_FLAG + 0x19;// 路由错误，注册服务器不在线或者注册服务器找不到相关路由信息
        public static final int USER_ERROR_INVLID_NODE = USER_ERROR_FLAG + 0x1A;// 使用的节点ID是无效的
        public static final int USER_ERROR_NO_READY = USER_ERROR_FLAG + 0x1C;// 服务还没准备好
        public static final int USER_ERROR_TASK_NO_EXISTS = USER_ERROR_FLAG + 0x1D;// 任务不存在
        public static final int USER_ERROR_NO_RECORDDATA = USER_ERROR_FLAG + 0x1E;// 无录像数据
        public static final int USER_ERROR_INVALID_PARAM = USER_ERROR_FLAG + 0x1F;// 无效参数,在命令传输的参数里出现没法处理的参数时返回此值
        public static final int USER_ERROR_UNSUPPORTED_CMD = USER_ERROR_FLAG + 0x20;// 不支持的命令
        public static final int USER_ERROR_DEVICE_BUSY = USER_ERROR_FLAG + 0x21;// 设备忙,不能请求
        public static final int USER_ERROR_LISTEN_FAIL = USER_ERROR_FLAG + 0x22;// 端口监听失败
        public static final int USER_ERROR_NO_USER = USER_ERROR_FLAG + 0x23;// 此用户不存在
        public static final int USER_ERROR_PWD_ERR = USER_ERROR_FLAG + 0x24;// 密码错误
        public static final int USER_ERROR_USER_ALREDAY_LOGIN = USER_ERROR_FLAG + 0x25;// 用户已经登陆
        public static final int USER_ERROR_USER_LIMITED = USER_ERROR_FLAG + 0x26;// 用户被限制在特有的电脑上MAC、IP
        public static final int USER_ERROR_USER_LOCKED = USER_ERROR_FLAG + 0x27;// 用户被锁定，暂时无法使用
        public static final int USER_ERROR_LOGIN_SELF = USER_ERROR_FLAG + 0x28;// 自己登陆自己
        public static final int USER_ERROR_NO_AUTH = USER_ERROR_FLAG + 0x29;// 权限不够
        public static final int USER_ERROR_SYSTEM_BUSY = USER_ERROR_FLAG + 0x30;// 系统忙,不能请求
        public static final int USER_ERROR_FILE_STREAM_COMPLETED = USER_ERROR_FLAG + 0x31;// 文件流被完成，不是用户主动结束的，而是由于网络原因或者文件结束后自动完成的
        public static final int USER_ERROR_GET_CONFIG_INFO_FAIL = USER_ERROR_FLAG + 0x32;// 获取配置信息失败
        public static final int USER_ERROR_ANOTHER_USER_HASENTER = USER_ERROR_FLAG + 0x33;// 正在被另外一个用户配置，请等待退出后再进入
        public static final int USER_ERROR_LOGIN_OVERTIME = USER_ERROR_FLAG + 0x34;// 登录超时错误
        public static final int USER_ERROR_CHANNEL_AUDIO_OPEN_FAIL = USER_ERROR_FLAG + 0x35;// 用户音频打开失败，由于视频没有打开导致的
        public static final int USER_ERROR_NOLOGIN = USER_ERROR_FLAG + 0x36;// 未登陆成功的情况下发送了指令，返回此值
        public static final int USER_ERROR_CANNOT_FIND_MAP_ERROR = USER_ERROR_FLAG + 0x37;// 找不到地图
        public static final int USER_ERROR_NO_PARENT_MAP_ERROR = USER_ERROR_FLAG + 0x38;// 没有父地图
        public static final int USER_ERROR_NO_CHILD_MAP_ERROR = USER_ERROR_FLAG + 0x39;// 没有子地图
        public static final int USER_ERROR_NAME_EXISTED = USER_ERROR_FLAG + 0x3A;// 名称已经存在
        public static final int USER_ERROR_MAP_SAVE_ERROR = USER_ERROR_FLAG + 0x3B;// 保存地图文件失败
        public static final int USER_ERROR_EMAP_NO_INFO = USER_ERROR_FLAG + 0x3C;// 没有电子地图的信息
        public static final int USER_ERROR_NOSUPPORT_DEV_VERSION = USER_ERROR_FLAG + 0x3D;// 不支持的前端设备的版本
        public static final int USER_ERROR_STREAM_WAITING = USER_ERROR_FLAG + 0x3E;// 设备回放请求才用此状,等待上一个请求返回
        public static final int USER_ERROR_UNSUPPORTED_FUNC = USER_ERROR_FLAG + 0x3F;// 设备不支持此功能
        public static final int USER_ERROR_DEVICE_TYPE_ERROR = USER_ERROR_FLAG + 0x40;// 设备类型错误
        public static final int USER_ERROR_UPDATE_FILE_TYPE_ERROR = USER_ERROR_FLAG + 0x41;// 升级文件的类型错误
        public static final int USER_ERROR_FILE_EXISTED = USER_ERROR_FLAG + 0x42;// 文件已经存在
        public static final int USER_ERROR_FILE_NO_EXISTED = USER_ERROR_FLAG + 0x43;// 文件不存在
        public static final int USER_ERROR_OPEN_FILE_ERROR = USER_ERROR_FLAG + 0x44;// 文件打开(创建)错误
        public static final int USER_ERROR_EXISTED_CHILD_NODE = USER_ERROR_FLAG + 0x45;// 此节点有子节点
        public static final int USER_ERROR_DEV_RESOURCE_LIMITED = USER_ERROR_FLAG + 0x46;// 设备资源限制
        public static final int USER_ERROR_DECODE_RESOURCE_LACK = USER_ERROR_FLAG + 0x47;// 没有解码资源,没有解码引擎
        public static final int USER_ERROR_DECODE_RESOURCE_LIMITED = USER_ERROR_FLAG + 0x48;// 解码资源限制,丢帧解码
        public static final int USER_ERROR_NO_RECORD_LOG = USER_ERROR_FLAG + 0x49;// 无对应的录像日志
        public static final int USER_ERROR_READ_TASK_TOO_MUCH = USER_ERROR_FLAG + 0x4A;// 录像读取任务过多
        public static final int USER_ERROR_INVALID_IP = USER_ERROR_FLAG + 0x50;// IP地址格式错误
        public static final int USER_ERROR_INVALID_SUBMASK = USER_ERROR_FLAG + 0x51;// 子网掩码格式错误
        public static final int USER_ERROR_IP_MASK_ALL1 = USER_ERROR_FLAG + 0x52;// 无效IP地址和子网掩码合并。IP地址的主机地址部分里的所有字节都被设置为1
        public static final int USER_ERROR_IP_MASK_ALL0 = USER_ERROR_FLAG + 0x53;// 无效IP地址和子网掩码合并。IP地址的主机地址部分里的所有字节都被设置为0
        public static final int USER_ERROR_ROUTE_MASK_ALL1 = USER_ERROR_FLAG + 0x54;// 无效网关地址和子网掩码合并。网关的主机地址部分里的所有字节都被设置为1
        public static final int USER_ERROR_ROUTE_MASK_ALL0 = USER_ERROR_FLAG + 0x55;// 无效网关地址和子网掩码合并。网关的主机地址部分里的所有字节都被设置为0
        public static final int USER_ERROR_USE_LOOPBACK = USER_ERROR_FLAG + 0x56;// 以127起头的IP地址无效，因为它们保留用作环回地址。请指定一个介于 1 和 223 之间的数值
        public static final int USER_ERROR_IP_ROUTE_INVALID = USER_ERROR_FLAG + 0x57;// IP地址或网关不是以一个有效的值开头。请指定一个介于 1 和 223 之间的数值
        public static final int USER_ERROR_MASK_NOT_CONTINE = USER_ERROR_FLAG + 0x58;// 输入一个无效的子网掩码，子网掩码必须是相邻的
        public static final int USER_ERROR_DIFFERENT_SEGMENT = USER_ERROR_FLAG + 0x59;// 网关不在由IP地址和子网掩码定义的同一网段上
        public static final int USER_ERROR_INVALID_GATEWAY = USER_ERROR_FLAG + 0x5A;// 网关地址格式错误
        public static final int USER_ERROR_INVALID_DOMAIN_NAME = USER_ERROR_FLAG + 0x5B;// 域名格式错误
        public static final int USER_ERROR_OVER_LIMIT = USER_ERROR_FLAG + 0x5C;// 超出数量限制
        public static final int USER_ERROR_OVER_BANDWIDTH_LIMIT = USER_ERROR_FLAG + 0x5D;// 超出流量限制
        public static final int MOBILE_STREAM_ADAPTION_NOT_OPEN = USER_ERROR_FLAG + 0x60;// 子码流自适应未打开
    }

    public class EXPIRED_ALARM_TYPE
    {
        public static final String LOG_ALARM_ALL = "LOG_ALARM_ALL";
        public static final String LOG_ALARM_MOTION = "LOG_ALARM_MOTION";
        public static final String LOG_ALARM_SENSOR = "LOG_ALARM_SENSOR";
        public static final String LOG_ALARM_INTELLIGENT = "LOG_ALARM_INTELLIGENT";
        public static final String LOG_ALARM_ALARMOUTPUT = "LOG_ALARM_ALARMOUTPUT";
        public static final String LOG_ALARM_OCCLUSION = "LOG_ALARM_OCCLUSION";
        public static final String LOG_EXCEPTION_ALL = "LOG_EXCEPTION_ALL";
        public static final String LOG_EXCEPTION_UNLAWFUL_ACCESS = "LOG_EXCEPTION_UNLAWFUL_ACCESS";
        public static final String LOG_EXCEPTION_DISK_FULL = "LOG_EXCEPTION_DISK_FULL";
        public static final String LOG_EXCEPTION_DISK_IO_ERROR = "LOG_EXCEPTION_DISK_IO_ERROR";
        public static final String LOG_EXCEPTION_RAID_SUBHEALTH = "LOG_EXCEPTION_RAID_SUBHEALTH";
        public static final String LOG_EXCEPTION_RAID_UNAVAILABLE = "LOG_EXCEPTION_RAID_UNAVAILABLE";
        public static final String LOG_EXCEPTION_IP_COLLISION = "LOG_EXCEPTION_IP_COLLISION";
        public static final String LOG_EXCEPTION_INTERNET_DISCONNECT = "LOG_EXCEPTION_INTERNET_DISCONNECT";
        public static final String LOG_EXCEPTION_IPC_DISCONNECT = "LOG_EXCEPTION_IPC_DISCONNECT";
        public static final String LOG_EXCEPTION_ABNORMAL_SHUTDOWN = "LOG_EXCEPTION_ABNORMAL_SHUTDOWN";
        public static final String LOG_EXCEPTION_HDD_PULL_OUT = "LOG_EXCEPTION_HDD_PULL_OUT";
        public static final String LOG_EXCEPTION_NO_DISK = "LOG_EXCEPTION_NO_DISK";
        public static final String LOG_EXCEPTION_VIDEO_LOSS = "LOG_EXCEPTION_VIDEO_LOSS";
    }

    public static class ECMS_NET_SUPPORT_INTELLIGENT_ALARM
    {
        public static class INTELLIGENT_ALARM_ITEM
        {
            public GUID nodeID;
            public String name = "";
        }

        public ArrayList<INTELLIGENT_ALARM_ITEM> iSupportList = new ArrayList<INTELLIGENT_ALARM_ITEM>();

        public static ECMS_NET_SUPPORT_INTELLIGENT_ALARM deserialize(String strMessage)
        {
            ECMS_NET_SUPPORT_INTELLIGENT_ALARM iSupportInfo = new ECMS_NET_SUPPORT_INTELLIGENT_ALARM();

            NodeList iNodeList = NVMSHeader.GetNodeList(strMessage, "content");
            if (iNodeList == null)
            {
                return iSupportInfo;
            }
            Node iContentNode = iNodeList.item(0);
            if (!iContentNode.hasChildNodes())
            {
                return iSupportInfo;
            }
            iNodeList = iContentNode.getChildNodes();
            for (int i = 0; i < iNodeList.getLength(); i++)
            {
                iContentNode = iNodeList.item(i);
                String iNodeName = iContentNode.getNodeName();
                if (iNodeName.equals("item"))
                {
                    INTELLIGENT_ALARM_ITEM item = new INTELLIGENT_ALARM_ITEM();
                    Element iElement = (Element) iNodeList.item(i);
                    GUID iChannelID = GUID.GetGUID(iElement.getAttribute("id").replace("{", "").replace("}", ""));
                    item.nodeID = iChannelID;

                    NodeList nodeList = iContentNode.getChildNodes();
                    for (int j = 0; j < nodeList.getLength(); j++)
                    {
                        Node node = nodeList.item(j);
                        if (node.getNodeName().equals("name"))
                        {
                            item.name = node.getFirstChild().getNodeValue();
                        }
                    }
                    iSupportInfo.iSupportList.add(item);
                }
            }
            return iSupportInfo;
        }
    }

    public class ERROR_TYPE
    {
        public static final int ERROR_TYPE_FULL = 0x80000000;
        public static final int ERROR_TYPE_NODISK = 0x40000000;
        public static final int ERROR_TYPE_STREAM = 0x20000000;
        public static final int ERROR_TYPE_ALL = 0xFFFF0000;
    }


    public static class NET_PROTOCOL_STRUCTDATA_DEF
    {
        public static final int ECMS_STRUCTDATA_CHANNEL_DATA = 0x01;
        public static final int ECMS_STRUCTDATA_SN_DATA = 0x02;
    }

    public static class NET_PROTOCOL_CHANNEL_TYPE_DEF
    {
        public static final int ECMS_CHANNEL_TYPE_ANALOG = 0x00;
        public static final int ECMS_CHANNEL_TYPE_IPC = 0x01;
        public static final int ECMS_CHANNEL_TYPE_RECORDER = 0x02;
    }

    public static class INFORMATION_HEADER
    {
        short usType;    //NET_PROTOCOL_STRUCTDATA_DEF 信息类型
        short usVersion;    //对应信息类型版本 当前为1 从1递增
        short usDataSize;    //单个信息大小
        short usNumber;    //信息个数

        public static int GetStructSize()
        {
            return 8;
        }

        public static INFORMATION_HEADER deserialize(DataInputStream dis, int iReadBefore) throws IOException
        {
            INFORMATION_HEADER itemHead = new INFORMATION_HEADER();
            MyUtil myUtil = new MyUtil();
            byte[] testbyte = new byte[2];

            dis.mark(dis.available());
            dis.skip(iReadBefore);

            dis.read(testbyte, 0, 2);
            itemHead.usType = myUtil.bytes2short(testbyte);
            dis.read(testbyte, 0, 2);
            itemHead.usVersion = myUtil.bytes2short(testbyte);
            dis.read(testbyte, 0, 2);
            itemHead.usDataSize = myUtil.bytes2short(testbyte);
            dis.read(testbyte, 0, 2);
            itemHead.usNumber = myUtil.bytes2short(testbyte);

            return itemHead;
        }
    }

    public static class ECMS_NET_CHANNEL_INFO
    {
        byte byChlType;// 0: 模拟通道，1: IPC，2：recorder
        byte byWinIndex;
        byte byChlIndex;
        byte byReserve;
        GUID nodeGuid = GUID.GetNullGUID();

        @Override
        public String toString()
        {
            return "byChlType:" + byChlType + ",byWinIndex:" + byWinIndex + ",byChlIndex:" + byChlIndex + ",nodeGuid:" + nodeGuid.GetGuidString();
        }

        public static int GetStructSize()
        {
            return 20;
        }

        public static ECMS_NET_CHANNEL_INFO deserialize(DataInputStream dis, int iReadBefore) throws IOException
        {
            ECMS_NET_CHANNEL_INFO channelInfo = new ECMS_NET_CHANNEL_INFO();
            MyUtil myUtil = new MyUtil();
            byte[] testbyte = new byte[GUID.GetStructSize()];

            dis.mark(dis.available());
            dis.skip(iReadBefore);

            channelInfo.byChlType = dis.readByte();
            channelInfo.byWinIndex = dis.readByte();
            channelInfo.byChlIndex = dis.readByte();
            channelInfo.byReserve = dis.readByte();
            dis.read(testbyte, 0, GUID.GetStructSize());
            channelInfo.nodeGuid = GUID.deserialize(testbyte, 0);

            return channelInfo;
        }
    }


    public static class ECMS_NODE_TYPE_DEF
    {
        public static final int NODE_TYPE_INVALID_NULL = 0;

        public static final int NODE_TYPE_CLIENT_BEGIN = 0x1;// 客户端开始范围，以用户为用户名资源登陆的
        public static final int NODE_TYPE_CLIENT_CONFIG = NODE_TYPE_CLIENT_BEGIN;// ////////配置客户端
        public static final int NODE_TYPE_CLIENT_MONITOR = 0x2;// ///////监控客户端
        public static final int NODE_TYPE_CLIENT_IE = 0x3; // ///IE客户端
        public static final int NODE_TYPE_CLIENT_MOBILE = 0x4; // ////手机客户端
        public static final int NODE_TYPE_CLIENT_TVWALL = 0x5;// 电视墙控制端
        public static final int NODE_TYPE_CLIENT_END = 0x1F;// 客户端结束范围

        public final static int NODE_TYPE_PCNVR_BEGIN = 0x1400;
        public final static int NODE_TYPE_PCNVR_9000 = 0x1401; // PC式NVR
        public final static int NODE_TYPE_PCNVR_END = 0x14ff;
    }

    public static class CMDPROC_TYPE
    {
        public final static int CMDPROC_TYPE_NULL = 0;// 无类型
        public final static int CMDPROC_TYPE_REQUEST = 1;// 请求命令
        public final static int CMDPROC_TYPE_REPLY_SUCCESS = 2;// 回复成功
        public final static int CMDPROC_TYPE_REPLY_FAIL = 3;// 回复失败
    }

    public static class NET_PROTOCOL_CMD_DEF
    {
        /************************************************************************/
    /* 客户端登陆 */
        /************************************************************************/
        // ///////客户端登陆
        public final static int ECMS_CMD_BASENUM_LOGIN = 0x100;
        public final static int ECMS_CMD_REQUEST_LOGIN = 0x101; // 请求登陆，权限验证
        public final static int ECMS_CMD_REQUEST_READY = 0x102; // 其它节点登陆注册服务器时使用，告诉RSU自己已经准备就绪，发送Listen端口，PC唯一序号，获得配置信息
        public final static int ECMS_CMD_LOGIN_END = 0x103;

        /************************************************************************/
    /* /////////操作权限检查 */
        /************************************************************************/
        public final static int ECMS_CMD_BASENUM_RIGHT_CHECK = 0x200;
        public final static int ECMS_CMD_REQUEST_RIGHT_CHECK = 0x201;//
        public final static int ECMS_CMD_RIGHT_CHECK_END = 0x202;

        /************************************************************************/
    /* ////////路由表的相关操作 */
        /************************************************************************/
        public final static int ECMS_CMD_BASENUM_ROUTE = 0x300;
        public final static int ECMS_CMD_REQUEST_ROUTE_INFO = 0x301;// 请求路由信息
        public final static int ECMS_CMD_REFRESH_ROUTE_INFO = 0x302;// 请求刷新路由信息
        public final static int ECMS_CMD_REQUEST_ROUTE_END = 0x303;

        /************************************************************************/
    /* 日志相关的操作 */
        /************************************************************************/
        public final static int ECMS_CMD_BASENUM_LOG = 0x400;// //日志相关命令
        public final static int ECMS_CMD_REQUEST_ABNORMAL_LOG = 0x401;// 搜索程序异常日志
        public final static int ECMS_CMD_REQUEST_SERRCH_LOG = 0x402;// 搜索日志
        public final static int ECMS_CMD_REQUEST_SEARCH_DEVICE_LOG = 0x403; // /搜索设备的日志信息

        /************************************************************************/
    /* /////////现场数据控制 */
        /************************************************************************/
        public final static int ECMS_CMD_BASENUM_REAL_STREAM = 0x500;
        public final static int ECMS_CMD_REQUEST_LIVE_STREAM_START = 0x501;// 请求现场流的传输
        public final static int ECMS_CMD_REQUEST_LIVE_STREAM_STOP = 0x502;// 关闭现场流的传输
        public final static int ECMS_CMD_REQUEST_REAL_STREAM_PREVIEW_OPEN = 0x503;// 控制对象打开现场预览
        public final static int ECMS_CMD_REQUEST_REAL_STREAM_PREVIEW_CLOSE = 0x504;// 控制对象关闭现场预览
        public final static int ECMS_CMD_REQUEST_LIVE_STREAM_START_EX = 0x505; // //新的请求现场流的传输
        public final static int ECMS_CMD_REQUEST_LIVE_STREAM_SUB_EDIT = 0x506; // //临时性修改子码流的编码参数
        public final static int ECMS_CMD_REAL_STREAM_END = 0x507;

        /************************************************************************/
    /* /////////////////////录像控制 */
        /************************************************************************/
        public final static int ECMS_CMD_BASENUM_REQUEST_CTRL_REC = 0x600;
        public final static int ECMS_CMD_REQUEST_ALARM_NET_CTRL_REC_START = 0x601;// 报警服务器发送的控制录像开始的指令
        public final static int ECMS_CMD_REQUEST_ALARM_NET_CTRL_REC_STOP = 0x602;// 报警服务器发送的控制录像结束的指令
        public final static int ECMS_CMD_REQUEST_NET_CTRL_REC_START = 0x603;// 网络控制手动录像
        public final static int ECMS_CMD_REQUEST_NET_CTRL_REC_STOP = 0x604;// 网络控制手动录像
        public final static int ECMS_CMD_REQUEST_CTRL_REC_END = 0x605;

        /************************************************************************/
    /* ////////////////对讲和广播控制 */
        /************************************************************************/
        public final static int ECMS_CMD_BASENUM_TALK = 0x700;
        public final static int ECMS_CMD_REQUEST_TALKBACK_BEGIN = 0x701;// 请求对讲开始
        public final static int ECMS_CMD_REQUEST_TALKBACK_END = 0x702;// 请求对讲结束
        public final static int ECMS_CMD_REQUEST_BROADCAST_BEGIN = 0x703; // 请求广播开始
        public final static int ECMS_CMD_REQUEST_BROADCAST_END = 0x704; // 请求广播开始
        public final static int ECMS_CMD_REQUEST_ALARM_TRIGGER_AUDIO_BEGIN = 0x705;
        public final static int ECMS_CMD_REQUEST_ALARM_TRIGGER_AUDIO_END = 0x706;

        // author: chenz. 支持NVMS9000对讲
        public final static int ECMS_CMD_REQUEST_TALKBACK_NVMS_BEGIN = 0x707; // NVMS请求对讲开始
        public final static int ECMS_CMD_REQUEST_TALKBACK_NVMS_END = 0x708; // NVMS请求对讲结束
        public final static int ECMS_CMD_TALK_END = 0x709;

        /************************************************************************/
    /* 录像数据播放控制请求部分 */
        /************************************************************************/
        public final static int ECMS_CMD_BASENUM_REC_STREAM_CTRL = 0x800;
        public final static int ECMS_CMD_REQUEST_REC_EVENT_SEARCH = 0x801;// 搜索事件
        public final static int ECMS_CMD_REQUEST_REC_SECTION_SEARCH = 0x802;// 搜索时间段
        public final static int ECMS_CMD_REQUEST_REC_DATA_PLAY = 0x803;// 播放
        public final static int ECMS_CMD_REQUEST_REC_DATA_PAUSE = 0x804;// 暂停
        public final static int ECMS_CMD_REQUEST_REC_DATA_RESUME = 0x805;// 恢复播放
        public final static int ECMS_CMD_REQUEST_REC_DATA_FF = 0x806; // 关键帧快进
        public final static int ECMS_CMD_REQUEST_REC_DATA_STOP = 0x807;// 停止
        public final static int ECMS_CMD_REQUEST_REC_INDEX = 0x808;// 当前帧的索引号
        public final static int ECMS_CMD_REQUEST_REC_DATA_BACKUP = 0x809;// 播放
        public final static int ECMS_CMD_REQUEST_REC_DATA_STOP_BACKUP = 0x80A;// 停止
        public final static int ECMS_CMD_REQUEST_REC_OPEN_ONLY_KEY_FRAME = 0x80B; // 打开关键帧快进
        public final static int ECMS_CMD_REQUEST_REC_CLOSE_ONLY_KEY_FRAME = 0x80C; // 关闭关键帧快进
        public final static int ECMS_CMD_REQUEST_REC_REWIND = 0x80D; // 倒放
        public final static int ECMS_CMD_REQUEST_REC_PREVIOUS = 0x80E; // 上一帧
        public final static int ECMS_CMD_REQUEST_REC_RESUME = 0x80F; // 恢复回放
        public final static int ECMS_CMD_REQUEST_REC_CANCEL_REWIND = 0x810; // 取消倒放
        public final static int ECMS_CMD_REQUEST_REC_DATA_LEN = 0x811; // 获取数据长度
        public final static int ECMS_CMD_END_REC_STREAM_CTRL = 0x812;

        /************************************************************************/
    /* ///////// */
        /************************************************************************/
        public final static int ECMS_CMD_BASENUM_CONFIG = 0x900;
        public final static int ECMS_CMD_REQUEST_CONFIG_START = 0x901;// 进入配置
        public final static int ECMS_CMD_REQUEST_CONFIG_END = 0x902;// 退出配置
        public final static int ECMS_CMD_REQUEST_CONFIG_INFO = 0x903;// 请求配置信息
        public final static int ECMS_CMD_REQUEST_SAVE_INFO = 0x904;// 保存配置信息
        public final static int ECMS_CMD_REQUEST_NEW_NODE_ID = 0x905;// 分配新的节点ID
        public final static int ECMS_CMD_REQUEST_CONFIG_INFO_UPDATE = 0x906; // 配置信息更新，附带被更新的配置数据
        public final static int ECMS_CMD_REQUEST_UPDATE_PARTITION_INFO = 0x907;// 存储服务器更新分区信息
        public final static int ECMS_CMD_REQUEST_CONFIG_REBOOT = 0x908;// 重启设备
        public final static int ECMS_CMD_REQUEST_CONFIG_RESTORE = 0x909;// 恢复出厂设置
        public final static int ECMS_CMD_REQUEST_BEGIN_IMPORT_SYS_CONFIG = 0x90A;// 请求导入系统配置
        public final static int ECMS_CMD_REQUEST_IMPORT_SYS_CONFIG_DATA = 0x90B;
        public final static int ECMS_CMD_REQUEST_END_IMPORT_SYS_CONFIG = 0x90C;// /导入系统配置
        public final static int ECMS_CMD_REQUEST_DISK_INFO = 0x90D; // 请求磁盘信息
        public final static int ECMS_CMD_REQUEST_MODIFY_PW = 0x90E;// /修改密码
        public final static int ECMS_CMD_REQUEST_CH_NAME_UPDATE_INFO = 0x90F;// //通知更新通道名称
        public final static int ECMS_CMD_REQUEST_EMAP_USER_RIGHT_UPDATE = 0x910; // 认证收到配置客户端电子地图权限发送给电子地图
        public final static int ECMS_CMD_REQUEST_DOWNLOAD_EMAPFILE = 0x911; // 下载电子地图文件，参数只有一个ULONG代表电子地图ID
        public final static int ECMS_CMD_REQUEST_CONFIG_INFO_LOGIN = 0x912; // 登陆发送过来的配置信息,目前只用于电子地图
        public final static int ECMS_CMD_REQUEST_SETCHANNLE_COLOR = 0x913; // 设置通道颜色值ECMS_SET_CHANNEL_COLOR
        public final static int ECMS_CMD_REQUEST_CHANGE_TIME = 0x914; // 修改的时间，根据指定的时间参数修改
        // ECMS_CMD_REQUEST_CH_ENCODE_INFO_UPDATE,////更新通道编码信息D1、CIF等 涵义有误而且太过具体而直接废除了,由下面的一项代替
        public final static int ECMS_CMD_REQUEST_NODE_APPEND_INFO_UPDATE = 0x915; // 节点的附加信息, 包括设备和通道支持的功能, 码流信息等
        public final static int ECMS_CMD_REQUEST_WIFI_LIST = 0x916;// 请求可用WIFI列表
        public final static int ECMS_CMD_REQUEST_ONLINE_USERS = 0x917; // 在线用户查询
        public final static int NVMS_CMD_REQUEST_TRANSPARENT_TRANSPORT = 0x918; // /请求透明传输请求数据 TransparentTransport
        public final static int NVMS_CMD_REQUEST_SNAP_PICTURE = 0x919;
        public final static int ECMS_CMD_REQUEST_CHANGE_TIME_BY_OSTIME = 0x91A; // 修改的时间，根据系统当前时间修改
        public final static int ECMS_CMD_API_CONFIG_FOR_MOBILE = 0x91B;
        public final static int ECMS_CMD_CONFIG_END = 0x91C;

        /************************************************************************/
    /*
     * //状态数据，状态数据下面可以根据具体情况细分
	 * 状态数据包括，节点连接断开、报警信息、通道名称修改、前段设备的通道录像状态、
	 * 云台是否可用
	 */
        /************************************************************************/
        public final static int ECMS_CMD_BASENUM_REQUEST_STATE = 0xA00;
        public final static int ECMS_CMD_REQUEST_ALARM_STATE_INFO = 0xA01;// 请求报警的状态信息
        public final static int ECMS_CMD_REQUEST_CONNECT_STATE_INFO = 0xA02;// 请求连接状态信息
        public final static int ECMS_CMD_REQUEST_RECORD_STATE_INFO = 0xA03;// 请求录像状态信息
        public final static int ECMS_CMD_REQUEST_DEVRECORD_STATE_INFO = 0xA04;// 请求设备录像状态信息
        public final static int ECMS_CMD_REQUEST_STATE_END = 0xA05;

        /************************************************************************/
    /* /////////通过网络对云台进行控制 */
        /************************************************************************/
        public final static int ECMS_CMD_BASENUM_PTZ_CONTROL = 0xB00;
        public final static int ECMS_CMD_PTZ_MOVE = 0xB01; // 移动操作 NET_PTZ_CTRL_INFO结构体里面的dwCmdType子段为ECMS_PTZ_MOVE的值之一
        public final static int ECMS_CMD_PTZ_CONTROL = 0xB02; // 云台控制 NET_PTZ_CTRL_INFO结构体里面的dwCmdType子段为ECMS_PTZ_CONTROL的值之一
        public final static int ECMS_CMD_GET_PTZ_PRESET = 0xB03; // 获取预置点
        public final static int ECMS_CMD_GET_PTZ_CRUISE = 0xB04; // 获取巡航线
        public final static int ECMS_CMD_PTZ_3DCONTROL = 0xB05; // 3D定位
        public final static int ECMS_CMD_PTZ_CONTROL_END = 0xB06;

        /************************************************************************/
    /* ///////////////////其它未分类的系统操作控制 */
        /************************************************************************/
        public final static int ECMS_CMD_BASENUM_OTHER_SYS_CTRL = 0xC00;
        public final static int ECMS_CMD_REQUEST_DOWN_FILE = 0xC01; // 下载文件
        public final static int ECMS_CMD_DOWN_FILE_DATA = 0xC02; // 下载文件数据
        public final static int ECMS_CMD_DOWN_FILE_COMPLETE = 0xC03; // 下载文件完成
        public final static int ECMS_CMD_DOWN_FILE_STOP = 0xC04; // 下载文件停止
        public final static int ECMS_CMD_REQUEST_UPLOAD_FILE = 0xC05; // 请求上传文件
        public final static int ECMS_CMD_UPLOAD_FILE_DATA = 0xC06; // 上传文件数据
        public final static int ECMS_CMD_UPLOAD_FILE_COMPLETE = 0xC07; // 上传文件完成
        public final static int ECMS_CMD_UPLOAD_FILE_STOP = 0xC08; // 上传文件停止
        public final static int ECMS_CMD_OTHER_SYS_CTRL_END = 0xC09;

        public final static int ECMS_CMD_BASENUM_ALARMOUT_CTRL = 0xD00;
        public final static int ECMS_CMD_REQUEST_SETALARMOUT = 0xD01;// 控制设备的报警输出
        public final static int ECMS_CMD_REQUEST_CLEARALARMOUT = 0xD02;// 清除设备的报警输出
        public final static int ECMS_CMD_BASENUM_ALARMOUT_CTRL_END = 0xD03;

        // //现场流数据
        public final static int ECMS_CMD_BASENUM_REAL_STREAM_DATA = 0x10000;
        public final static int ECMS_CMD_LIVE_STREAM_DATA = 0x10001;// 现场流数据
        public final static int ECMS_CMD_LIVE_STREAM_DATA_COMPLETE = 0x10002;// 现场数据流完成
        public final static int ECMS_CMD_REAL_STREAM_DATA_END = 0x10003;

        // //回放流数据
        public final static int ECMS_CMD_BASENUM_PLAY_STREAM_DATA = 0x20000;
        public final static int ECMS_CMD_PLAY_STREAM_DATA = 0x20001;// ////回放流数据
        public final static int ECMS_CMD_PLAY_STREAM_DATA_COMPLETE = 0x20002;// 回放数据流完成
        public final static int ECMS_CMD_PLAY_STREAM_DATA_END = 0x20003;

        // 对讲数据
        public final static int ECMS_CMD_BASENUM_TALKBACK_STREAM_DATA = 0x30000;
        public final static int ECMS_CMD_TALKBACK_STREAM_DATA = 0x30001; // 对讲流数据
        public final static int ECMS_CMD_TALKBACK_STREAM_DATA_COMPLETE = 0x30002; // 对讲流数据完成
        public final static int ECMS_CMD_TALKBACK_STREAM_DATA_END = 0x30003;

        // 无效命令
        public final static int ECMS_CMD_NULL = 0xFFFFFFFF;
    }

    public static class DATA_ITEM_ID
    {
        // //////////////////////
        public final static int DATA_ITEM_REPLY_FAIL = 0x10;// ///////////回复失败的结构体 ECMS_NET_REPLY_RESULT

        // ///////////系统基本配置的杂项
        public final static int DATA_ITEM_SYSTEM_BASE = 0x100;
        public final static int DATA_ITEM_RSU_INI = 0x101;
        public final static int DATA_ITEM_SYSTEM_PACKET = 0x102;
        public final static int DATA_ITEM_EMAP_PACKET = 0x103;
        public final static int DATA_ITEM_EXPORT_FILE_SECTION_ID_RANGE_BEGIN = 0x104;
        public final static int DATA_ITEM_EXPORT_FILE_SECTION_ID_RANGE_END = DATA_ITEM_EXPORT_FILE_SECTION_ID_RANGE_BEGIN + 10;

        // ////////////节点相关的信息
        public final static int DATA_ITEM_RESOURCE_BASE = 0x200;
        public final static int DATA_ITEM_DEVICE_INFO = 0x201; // ///设备节点的信息 NCFG_DEVICE_INFO
        public final static int DATA_ITEM_CH_INFO = 0x202; // ////通道节点的信息 NCFG_DEVICE_CH_INFO
        public final static int DATA_ITEM_IP_CHANNEL_INFO = 0x203; // IP通道节点的信息
        public final static int DATA_ITEM_SCHEDULE_TIME_INFO = 0x204;// ///排程的时间信息 NCFG_SCHEDULE_TIME_SECTION
        public final static int DATA_ITEM_SCHEDULE_NAME_INFO = 0x205;// ///排程的名字信息 NCFG_SCHEDULE_NAME
        public final static int DATA_ITEM_AREA_INFO = 0x206; // ///区域信息 ECMS_CFG_DEF::NCFG_AREA_INFO 更改和删除
        public final static int DATA_ITEM_SERVER_INFO = 0x207; // ///系统中的服务器信息 New 2013.9.3
        public final static int DATA_ITEM_ALARM_OUT_INFO = 0x208; // ///系统中的报警输出的信息 New 2013.9.3
        public final static int DATA_ITEM_ALARM_IN_INFO = 0x209; // ///系统中的报警输入的信息 New 2013.9.3
        public final static int DATA_ITEM_BINARY_RELATION_LOGIC = 0x20A;// //2元逻辑关系 New 2013.9.3
        public final static int DATA_ITEM_BINARY_RELATION_LOGIC_FOR_EMAP = 0x20B;// //2元逻辑关系 New 2013.9.3
        public final static int DATA_ITEM_BINARY_RELATION_PHYSICS = 0x20C;// 2元物理关系 New 2013.9.3
        public final static int DATA_ITEM_BINARY_RELATION_PHYSICS_FOR_EMAP = 0x20D;// 2元物理关系 New 2013.9.3
        public final static int DATA_ITEM_TERNARY_RELATION_PHYSICS = 0x20E;// 3元物理关系 New 2013.9.3
        public final static int DATA_ITEM_BINARY_RELATION_PHYSICS_FOR_MPR = 0x20F;// Media Play Resource 2元物理关系 New 2013.9.3

        // ////////////存储服务器分区相关的信息
        public final static int DATA_ITEM_MSU_PARTITION_BASE = 0x300;
        public final static int DATA_ITEM_MSU_PARTITION_STATE = 0x301;// ////存储服务器的分区状态信息;NET_PARTITION_INFO
        public final static int DATA_ITEM_STORAGE_MEDIA_GROUPS = 0x302; // //多个存储介质组信息
        public final static int DATA_ITEM_RECORD_SCHEDULE = 0x303; // /////排程录像信息

        public final static int DATA_ITEM_EMAP_INFO_BASE = 0x400;
        public final static int DATA_ITEM_EMAP_FILE_INFO = 0x401;// 电子地图的文件信息
        public final static int DATA_ITEM_EMAP_HOT_SPOT_AREA_INFO = 0x402; // 热点热区信息
        public final static int DATA_ITEM_EMAP_HOT_SPOT_EVENT = 0x403; // 热点事件信息
        public final static int DATA_ITEM_EMAP_INFO_END = 0x4FF;

        public final static int DATA_ITEM_USER_BASE = 0x500;// ////////用户及用户权限的信息
        public final static int DATA_ITEM_USER_INFO = 0x501;// ///用户信息 对应的结构体USER_INFO
        public final static int DATA_ITEM_USER_RIGHT = 0x502;// ///////用户权限 NODE_ACCESS_RIGHTS

        public final static int DATA_ITEM_ALARM_BASE = 0x600; // /////////报警 与报警排程的信息
        public final static int DATA_ITEM_ALARM_TRIGGER = 0x601;// /////////报警联动的信息

        public final static int DATA_ITEM_TVWALL_BASE = 0x700;
        public final static int DATA_ITEM_TVWALL_INFO = 0x701; // 电视墙服务器信息
        public final static int DATA_ITEM_TVWALL_OUTPUT = 0x702; // 电视墙显示输出通道信息
        public final static int DATA_ITEM_TVWALL_FRAME = 0x703; // 电视墙输出通道的画面的信息
        public final static int DATA_ITEM_TVWALL_LIVE = 0x704; // 电视墙的现场任务配置信息
        public final static int DATA_ITEM_TVWALL_PLAN = 0x705; // 电视墙计划任务配置信息
        public final static int DATA_ITEM_TVWALL_RELATE = 0x706; // 电视墙画面和任务关系信息
        public final static int DATA_ITEM_TVWALL_USED = 0x707; // /////////电视墙服务器的资源使用情况
        public final static int DATA_ITEM_TVWALL_PLAIN = 0x708; // ///电视墙计划任务配置信息
        public final static int DATA_ITEM_OUTPUT = 0x709; // /
        public final static int DATA_ITEM_OUTPUT_FRAME = 0x70A; // /

        public final static int DATA_ITEM_LOG_BASE = 0x800;
        public final static int DATA_ITEM_LOG_INFO = 0x801;// //日志信息 ///////////////只读信息
        public final static int DATA_ITEM_LOG_SEARCH_INFO = 0x802; // 日志查询信息

        public final static int DATA_ITEM_LOGIN_BASE = 0x900;
        public final static int DATA_ITEM_LOGIN_INFO = 0x901; // ///////////ECMS_NET_LOGIN_INFO
        public final static int DATA_ITEM_LOGIN_SUCCESS = 0x902; // /////////登陆成功的结构体 ECMS_LOGIN_SUCCESS_INFO

        public final static int DATA_ITEM_DEVICE_LOCAL_CFG_BASE = 0xA00;
        public final static int DATA_ITEM_DEVICE_LOCAL_CFG_INFO = 0xA01; // ///////设备本地的配置信息 格式为ＸＭＬ

        public final static int DATA_ITEM_DWELL_CHANNEL = 0x10000; // 通道轮询信息
        public final static int DATA_ITEM_DWELL_GROUP = 0x10001; // 组轮询信息
        public final static int DATA_ITEM_LIVE_INFO = 0x10002; // 现场预览信息
        public final static int DATA_ITEM_CHANNEL_GROUP = 0x10003; // 通道组信息
        public final static int DATA_ITEM_ITEM_STATE = 0x10004; // 预览页状态
        public final static int DATA_ITEM_PLAY_BACK_INFO = 0x10005; // 回放页
        public final static int DATA_ITEM_ALARM_INFO = 0x10006; // 报警页
        public final static int DATA_ITEM_ALARM_CONFIG_INFO = 0x10007; // 报警联动配置信息
        public final static int DATA_ITEM_DWELL_SCHEME_GROUP = 0x10008; // 组轮询方案信息
        public final static int DATA_ITEM_EMAP_INFO = 0x10009; // 电子地图信息
        public final static int DATA_ITEM_STATE_INFO = 0x1000A; // 状态信息
        public final static int DATA_ITEM_CHANNEL_GROUP_RELATE = 0x1000B; // 通道组中的关系信息，通道和通道组的关系信息，只需要支持增加和删除
        public final static int DATA_ITEM_CHANNEL_GROUP_MPR = 0x1000C; // 通道组信息MPR专用
        public final static int DATA_ITEM_CHANNEL_PROGRAM_RELATE_MPR = 0x1000D; // 通道与方案关系

        public final static int DATA_ITEM_BASEVALUE = 0x20000; // ///////下面是本地的简单的信息保存用的ID
        public final static int DATA_ITEM_SNAP_FRAME_COUNT = 0x20001;// ///抓取帧的数量
        public final static int DATA_ITEM_VIDEO_DISPLAY_PARAM = 0x20002; // ///视频显示参数设置 包括 宽 高 是否显示标题
        public final static int DATA_ITEM_PATH_INFO = 0x20003; // ///
        public final static int DATA_ITEM_ALARM_EMAP_PARAM = 0x20004; // /////报警电子地图的参数
        public final static int DATA_ITEM_EMAP_VIEW_SET_INFO = 0x20005; // ///电子地图视图的设置信息
        public final static int DATA_ITEM_ALARM_PREVIEW_PARAM = 0x20006; // //// ALARM_PREVIEW_PARAM
        public final static int DATA_ITEM_DWELL_ITEM_INFO = 0x20007; // ////轮询信息
        public final static int DATA_ITEM_DWELL_RELATE_INFO = 0x20008; // ///轮询项的关系信息 ***********************************
        public final static int DATA_ITEM_PAGE_INFO = 0x20009; // ////页面的相关信息
        public final static int DATA_ITEM_PAGE_INFO_EXTENSION = 0x2000A;// /////现场页信息的扩展 ***********************
        public final static int DATA_ITEM_DEFAULT_CH_STREAM_INFO = 0x2000B; // //通道流的信息，ＮＶＭＳ１０００， NVMS1200本地配置文件保存
        public final static int DATA_ITEM_SERVER_MAINTAIN_SET_INFO = 0x2000C; // // 服务器启动和维护设置
        public final static int DATA_ITEM_LOCAL_LOG_MAINTAIN_INFO = 0x2000D; // // 本地日志维护
        public final static int DATA_ITEM_ALARM_MANUAL_TRIGGER_INFO = 0x2000E; // // 手动触发报警设置
        public final static int DATA_ITEM_ALARM_LIST_INFO = 0x2000F;
        public final static int DATA_ITEM_DISK_ARRAYS = 0x20010; // ////磁盘阵列 对应XML的信息
        public final static int DATA_ITEM_IP = 0x20011; // /////////IP信息 对应XML的信息
        public final static int DATA_ITEM_NET_PORT = 0x20012; // /////////端口信息 对应XML的信息
        public final static int DATA_ITEM_DDNS = 0x20013;// ///DDNS信息 对应XML的信息
        public final static int DATA_ITEM_UPNP = 0x20014;// ///UPnP信息 对应XML的信息
        public final static int DATA_ITEM_FTP = 0x20015;// ///FTP信息 对应XML的信息
        public final static int DATA_ITEM_PPPOE = 0x20016;// ///PPPOE信息 对应XML的信息
        public final static int DATA_ITEM_P2P = 0x20017;// P2P信息 对应XML的信息

        public final static int DATA_ITEM_TIME = 0x20018; // //时间信息
        public final static int DATA_ITEM_SYSTEM_INFO = 0x20019; // ////系统信息
        public final static int DATA_ITEM_NEW_RESOURCE_DEFAULT_RIGHT_SET_INFO = 0x2001A;// ////

        // /////////////////////////注意： 后面的定义的本地的信息，如果不涉及到动态数量变化的信息，进行使用xml，不在使用结构体了
        // //原来使用结构体的会逐渐被xml信息替代
        public final static int DATA_ITEM_RECORD_DISTRIBUTE_INFO = 0x2001B;// ///录像相关的一些分散参数的设置
        public final static int DATA_ITEM_ALARM_OUTPUT_DISTRIBUTE_INFO = 0x2001C; // 报警输出的一些分散参数的设置
        public final static int DATA_ITEM_EMAIL = 0x2001D; // Email信息 对应XML的信息
        public final static int DATA_ITEM_WEB_LISTEN_PORT = 0x2001E; // //Web 监听端口信息
        public final static int DATA_ITEM_PRESET = 0x2001F; // 预置点信息
        public final static int DATA_ITEM_CRUISE = 0x20020; // 巡航线信息
        public final static int DATA_ITEM_BLACK_WHITE_LIST = 0x20021; // 黑白名单
        public final static int DATA_ITEM_DEV_DEFAULT_PASSWORD = 0x20022; // 厂商默认密码

    }

    public static class ECMS_NET_CMD_ID
    {
        public final static int NET_CMD_ID_IGNORE = 0;
        public final static int NET_CMD_ID_ONE_TO_ONE_MIN = (0xFF + 0x01);// ////CMD ID的最小值
        public final static int NET_CMD_ID_ONE_TO_ONE_MAX = (0xFFFFFFFF - 0x01);// ////////CMD ID的最大值
        public final static int NET_CMD_ID_BROADCAST = 0xFFFFFFFF;// ////广播数据采用此ID
    }

    public static class ECMS_PTZ_MOVE
    {
        public final static int ECMS_PTZ_MOVE_TOP = 1; // //向上走
        public final static int ECMS_PTZ_MOVE_BOTTOM = 2; // //向下走
        public final static int ECMS_PTZ_MOVE_LEFT = 3; // //向左
        public final static int ECMS_PTZ_MOVE_RIGHT = 4; // //向右
        public final static int ECMS_PTZ_MOVE_LEFT_TOP = 5; // 左上
        public final static int ECMS_PTZ_MOVE_LEFT_BOTTOM = 6;// 左下
        public final static int ECMS_PTZ_MOVE_RIGHT_TOP = 7;// 右上
        public final static int ECMS_PTZ_MOVE_RIGHT_BOTTOM = 8;// 右下
        public final static int ECMS_PTZ_MOVE_FOCUSNEAR = 9; // //调焦近
        public final static int ECMS_PTZ_MOVE_FOCUSFAR = 10; // //调焦远
        public final static int ECMS_PTZ_MOVE_ZOOMIN = 11; // /放大
        public final static int ECMS_PTZ_MOVE_ZOOMOUT = 12; // //缩小
        public final static int ECMS_PTZ_MOVE_IRISOPEN = 13; // //聚焦
        public final static int ECMS_PTZ_MOVE_IRISCLOSE = 14; // //聚焦
    }

    public static class ECMS_PTZ_CONTROL
    {
        public final static int ECMS_PTZ_CONTROL_STOP = 100; // ///停止
        public final static int ECMS_PTZ_CONTROL_PRESETGO = 101; // //到某一预置点
        public final static int ECMS_PTZ_CONTROL_CRUISEGO = 102; // //到某一巡航线
        public final static int ECMS_PTZ_CONTROL_PRESETSET = 103; // //设置某一预置点
        public final static int ECMS_PTZ_CONTROL_CRUISESTOP = 104; // 停止巡航
        public final static int ECMS_PTZ_CONTROL_TRACKSTART = 105; // 开始保存轨迹
        public final static int ECMS_PTZ_CONTROL_TRACKSTOP = 106; // 停止保存轨迹
        public final static int ECMS_PTZ_CONTROL_TRACKSET = 107;
        public final static int ECMS_PTZ_CONTROL_TRACK_SCAN_START = 108; // 开始轨迹
        public final static int ECMS_PTZ_CONTROL_TRACK_SCAN_STOP = 109; // 停止轨迹
        public final static int ECMS_PTZ_CONTROL_AUTO_SCAN_START = 110; // 开始自动扫描
        public final static int ECMS_PTZ_CONTROL_AUTO_SCAN_STOP = 111; // 停止自动扫描
        public final static int ECMS_PTZ_CONTROL_OPEN_LAMP = 112;// 接通灯光
        public final static int ECMS_PTZ_CONTROL_CLOSE_LAMP = 113;// 关闭灯光
        public final static int ECMS_PTZ_CONTROL_OPEN_WIPER = 114;// 接通雨刷
        public final static int ECMS_PTZ_CONTROL_CLOSE_WIPER = 115;// 关闭雨刷
    }

    public static class LIVE_RIGHT_TYPE
    {
        public final static int LIVE_RIGHT_TYPE_LIVE_PREVIEW = 1;
        public final static int LIVE_RIGHT_TYPE_RECORD = 2;
        public final static int LIVE_RIGHT_TYPE_DWELL_GROUP = 3;
        public final static int LIVE_RIGHT_TYPE_DWELL_CHANNEL = 4;
        public final static int LIVE_RIGHT_TYPE_CHANNEL_SETTING = 5;// 通道配置时请求
        public final static int LIVE_RIGHT_TYPE_EMAP_PREVIEW = 6;// 电子地图自动弹出画面
        public final static int LIVE_RIGHT_TYPE_ALARM_POP = 7;// 报警大画面预览
        public final static int LIVE_RIGHT_TYPE_START_RESUME = 8;// 程序启动时恢复显示
    }

    public static class EXTEND_CMD_TYPE
    {
        public final static int EXTEND_CMD_NULL = 0;// 无扩充信息
        public final static int EXTEND_CMD_BASE = 1; // 基本扩充信息
        public final static int EXTEND_CMD_TASK = 2; // 任务型扩充信息
    }

    // 帧类型
    public static class NVMS_FRAME_TYPE
    {
        public final static int VIDEO = 0; // //视频数据
        public final static int AUDIO = 1;// /音频数据
        public final static int CMD_FRAME = 2; // 命令帧
        public final static int DIRECT_DISPLAY = 3; // 直接显示帧,不通过播放控制,用于回放
        public final static int PREDECOD = 4; // 预解码,只解码不播放,用于回放
    }

    public static class NVMS_EXTEND_CMD_TYPE
    {
        public final static int EXTEND_CMD_NULL = 0;// 无扩充信息
        public final static int EXTEND_CMD_BASE = 1; // 基本扩充信息
        public final static int EXTEND_CMD_TASK = 2; // 任务型扩充信息
    }

    public static class ROUTE_DIRECT_DEF
    {
        public final static int ROUTE_DIRECT_DEF_RANGE_BEGIN = 0;
        public final static int DEST_REPLY_SOURCE = 1;// 目的节点回复源节点的信息
        public final static int FROM_SOURCE_TO_DEST = 2;// 源节点对目的节点发起的请求
        public final static int ROUTE_DIRECT_DEF_RANGE_END = 3;
    }

    public static class TASKSTATE_TYPE
    {
        public final static int TASKSTATE_TYPE_NULL = 0; // 此命令没状态
        public final static int TASKSTATE_TYPE_START = 1; // 此命令为启动状态
        public final static int TASKSTATE_TYPE_STOP = 2; // 此命令为停止状态
        public final static int TASKSTATE_TYPE_START_REPLY = 3; // 此命令为启动应答
        public final static int TASKSTATE_TYPE_STOP_REPLY = 4; // 此命令为停止应答
        public final static int TASKSTATE_TYPE_DATA = 5; // 任务数据
        public final static int TASKSTATE_TYPE_DATA_COMPLETE = 6; // 数据完成
    }

    public static class ECMS_STREAM_TYPE
    {
        public final static int STREAM_NULL = 0;

        public final static int LIVE_FIRST_STREAM = 1;// 现场主码流 请求和回复现场主码流的流类型
        public final static int LIVE_SECOND_STREAM = 2;// 现场子码流1 请求和回复现场子码流的流类型
        public final static int LIVE_SUB_STREAM_2 = 3;// 现场子码流2 请求和回复现场子码流2的流类型
        public final static int LIVE_SUB_STREAM_3 = 4;// 现场子码流3 请求和回复现场子码流3的流类型
        public final static int LIVE_SUB_STREAM_4 = 5;// 现场子码流4 请求和回复现场子码流4的流类型
        public final static int LIVE_AUDIO_STREAM = 6;// 现场音频流 回复现场音频的流类型
        public final static int PLAY_STREAM = 7;// 回放流 请求和回复回放的码流类型
        public final static int TALKBACK_STREAM = 8; // 对讲流 请求和回复对讲流的码流类型
        public final static int CMD_STREAM = 9; // 命令流数据
        public final static int LIVE_STREAM_PIC = 10;// 现场图片流，主要用在黑莓手机上
        public final static int LIVE_STREAM_GPS = 11;// 实时行车数据
        public final static int PLAYBACK_STREAM_GPS = 12;// 历史行车数据
        public final static int STREAM_TYPE_NUM = 13; // 流类型数量
        public final static int STREAM_END = 14;
    }

    public static class ECMS_ALARM_TYPE
    {
        public final static int ECMS_ALARM_TYPE_RANGE_BEGIN = 0;
        public final static int ECMS_ALARM_TYPE_MOTION = 0x01;// 移动侦测报警输入
        public final static int ECMS_ALARM_TYPE_SENSOR = 0x02;// 传感器报警输入
        public final static int ECMS_ALARM_TYPE_VLOSS = 0x03;// 视频丢失报警输入
        public final static int ECMS_ALARM_TYPE_FRONT_OFFLINE = 0x04; // 前端设备掉线报警

        public final static int ECMS_ALARM_TYPE_GPS_SPEED_OVER = 0x21;// 和车载有关的报警，超速
        public final static int ECMS_ALARM_TYPE_GPS_CROSS_BOADER = 0x22;// 越界
        public final static int ECMS_ALARM_TYPE_GPS_TEMPERATURE_OVER = 0x23;// 温度报警
        public final static int ECMS_ALARM_TYPE_GPS_GSENSOR_X = 0x24;// GSENSOR报警
        public final static int ECMS_ALARM_TYPE_GPS_GSENSOR_Y = 0x25;
        public final static int ECMS_ALARM_TYPE_GPS_GSENSOR_Z = 0x26;

        public final static int NVMS_ALARM_TYPE_EXCEPTION = 0x41;
        public final static int NVMS_ALARM_TYPE_IP_CONFLICT = 0x42; // IP地址冲突
        public final static int NVMS_ALARM_TYPE_DISK_IO_ERROR = 0x43; // 磁盘IO错误
        public final static int NVMS_ALARM_TYPE_DISK_FULL = 0x44; // 磁盘满
        public final static int NVMS_ALARM_TYPE_RAID_SUBHEALTH = 0x45; // 阵列亚健康
        public final static int NVMS_ALARM_TYPE_RAID_UNAVAILABLE = 0x46; // 阵列不可用
        public final static int NVMS_ALARM_TYPE_ILLEIGAL_ACCESS = 0x47; // 非法访问
        public final static int NVMS_ALARM_TYPE_NET_DISCONNECT = 0x48; // 网络断开

        public final static int NVMS_ALARM_TYPE_ALARM_OUT = 0x51; // 报警输出的类型，报警输出也有状态需要管理

        public final static int ECMS_ALARM_TYPE_RANGE_END = 0xFF;// 不能超过这个值 否则会出错
    }

    public static class AUDIO_FORMAT
    {
        public final static int AUDIO_FORMAT_PCM = 0; // 标准PCM可以直接播放数据
        public final static int AUDIO_FORMAT_ALAW = 1; // 标准G711_A
        public final static int AUDIO_FORMAT_HI_ADPCM = 2;// 海斯ADPCM格式
        public final static int AUDIO_FORMAT_HI_ALAW = 3;// 海斯G711_A格式
        public final static int AUDIO_FORMAT_HK711 = 4;// 海康711声音格式
        public final static int AUDIO_FORMAT_HK722 = 5;// 海康722声音格式
        public final static int AUDIO_FORMAT_ULAW = 6; // 标准G711_U
        public final static int AUDIO_FORMAT_HI_ULAW = 7; // 海斯G711_U
    }

    public static class NETDEVICE_TYPE_INFO
    {
        public int headFlag;
        public int productSeries;
        public int productType;
        public int netProtrocolver;
        public int configVer;
        public int devID;
        public int encryptType;
        public int encryptParam;
        public byte[] MAC = new byte[8];
        public int[] ulReserve = new int[6];

        public static int GetStructSize()
        {
            return 64;
        }

        public static NETDEVICE_TYPE_INFO deserialize(byte[] data, int iReadBefore) throws IOException
        {
            ByteArrayInputStream bais = new ByteArrayInputStream(data);
            DataInputStream dis = new DataInputStream(bais);
            NETDEVICE_TYPE_INFO iDeviceInfo = new NETDEVICE_TYPE_INFO();
            MyUtil myUtil = new MyUtil();
            byte[] testbyte = new byte[4];

            dis.read(data, 0, iReadBefore);

            dis.read(testbyte, 0, 4);
            iDeviceInfo.headFlag = myUtil.bytes2int(testbyte);
            dis.read(testbyte, 0, 4);
            iDeviceInfo.productSeries = myUtil.bytes2int(testbyte);
            dis.read(testbyte, 0, 4);
            iDeviceInfo.productType = myUtil.bytes2int(testbyte);
            dis.read(testbyte, 0, 4);
            iDeviceInfo.netProtrocolver = myUtil.bytes2int(testbyte);
            dis.read(testbyte, 0, 4);
            iDeviceInfo.configVer = myUtil.bytes2int(testbyte);
            dis.read(testbyte, 0, 4);
            iDeviceInfo.devID = myUtil.bytes2int(testbyte);
            dis.read(testbyte, 0, 4);
            iDeviceInfo.encryptType = myUtil.bytes2int(testbyte);
            dis.read(testbyte, 0, 4);
            iDeviceInfo.encryptParam = myUtil.bytes2int(testbyte);

            dis.read(iDeviceInfo.MAC, 0, 8);

            dis.close();
            bais.close();

            return iDeviceInfo;
        }
    }

    public static class ECMS_PACKCMD
    {
        public short cmdProtocolVer;// = NVMSHeader.ECMS_NET_PROTOCOL_VER;// 命令版本，可以直接根据这个版本确定命令的格式
        public byte byExtendInfo; // 扩充信息类型，0为无扩充信息
        public byte byHasReply; // /此命令是否需要应答

        public int dwCmdType;// 命令类型，对应ECMS_PROTOCOL_DEF中的值
        public int dwCmdID;// 指令序号
        public int dwDataLen;// 数据的长度,此长度不包括ECMS_PACKCMD_EXTEND 或 ECMS_PACKCMD_EXTEND_TASK结构的长度,只是后面命令数据的长度

        public static int GetStructSize()
        {
            return 16;
        }

        public byte[] serialize() throws IOException
        {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            DataOutputStream dos = new DataOutputStream(baos);
            byte[] testbyte = new byte[2];
            MyUtil myUtil = new MyUtil();

            testbyte = myUtil.short2bytes(cmdProtocolVer);
            dos.write(testbyte, 0, 2);

            dos.writeByte(byExtendInfo);
            dos.writeByte(byHasReply);

            dwCmdType = myUtil.ntohl(dwCmdType);
            dos.writeInt(dwCmdType);

            dwCmdID = myUtil.ntohl(dwCmdID);
            dos.writeInt(dwCmdID);

            dwDataLen = myUtil.ntohl(dwDataLen);
            dos.writeInt(dwDataLen);

            byte[] ret = baos.toByteArray();
            dos.close();
            baos.close();

            return ret;
        }

        public static ECMS_PACKCMD deserialize(DataInputStream dis, int iReadBefore) throws IOException
        {
            ECMS_PACKCMD iPackcmd = new ECMS_PACKCMD();
            MyUtil myUtil = new MyUtil();
            byte[] testbyte = new byte[4];

            dis.mark(dis.available());
            dis.skip(iReadBefore);

            dis.read(testbyte, 0, 2);
            iPackcmd.cmdProtocolVer = myUtil.bytes2short(testbyte);

            iPackcmd.byExtendInfo = dis.readByte();
            iPackcmd.byHasReply = dis.readByte();

            dis.read(testbyte, 0, 4);
            iPackcmd.dwCmdType = myUtil.bytes2int(testbyte);

            dis.read(testbyte, 0, 4);
            iPackcmd.dwCmdID = myUtil.bytes2int(testbyte);

            dis.read(testbyte, 0, 4);
            iPackcmd.dwDataLen = myUtil.bytes2int(testbyte);

            return iPackcmd;
        }
    }

    // 登陆时用的结构体
    public static class ECMS_NET_LOGIN_INFO
    {
        public int nodeType; // 节点类型
        public GUID nodeID = new GUID(); // 节点ID
        public GUID destNodeID = new GUID();// //连接的目标节点的ID
        public byte[] username = new byte[64];// 用户姓名
        public byte[] password = new byte[64];// 用户密码
        // 以下两项在过滤客户端时使用
        public byte[] IP = new byte[24]; // 客户端用来连接的IP地址 , 从第0个开始是sockaddr_in的结构体
        public byte[] szMAC = new byte[6]; // 网卡的MAC地址
        public byte byTestLogin;// //是否是测试连接的登陆 正常登录为0， 测试连接的登录为非0
        public byte szReserve; // 对齐导致的保留1个字节

        public int softwarePackID;// 软件包ID，样可以保证软件包之间不能相互连接，必须整体升级
        public int customerID;// 客户ID，这样可以保证软件包之间不能相互连接，必须整体升级

        public int[] reserve = new int[8];

        public static int GetStructSize()
        {
            return 236;
        }

        public byte[] serialize() throws IOException
        {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            DataOutputStream dos = new DataOutputStream(baos);
            MyUtil myUtil = new MyUtil();

            nodeType = myUtil.ntohl(nodeType);
            dos.writeInt(nodeType);

            dos.write(nodeID.serialize(), 0, GUID.GetStructSize());
            dos.write(destNodeID.serialize(), 0, GUID.GetStructSize());

            dos.write(username, 0, username.length);
            dos.write(password, 0, password.length);
            dos.write(IP, 0, IP.length);
            dos.write(szMAC, 0, szMAC.length);

            dos.writeByte(byTestLogin);
            dos.writeByte(szReserve);

            softwarePackID = myUtil.ntohl(softwarePackID);
            dos.writeInt(softwarePackID);

            customerID = myUtil.ntohl(customerID);
            dos.writeInt(customerID);

            for (int i = 0; i < 8; i++)
            {
                reserve[i] = myUtil.ntohl(reserve[i]);
                dos.writeInt(reserve[i]);
            }

            byte[] ret = baos.toByteArray();
            dos.close();
            baos.close();

            return ret;
        }
    }

    // 注意服务器登陆时如果没有配置内外网IP，就获取本地全部IP排序后存储InIP，OutIP全部为0
    // 在到了注册服务器后如果发现OutIP为0，就将客户端连接过来的IP地址记录为A，客户端发过来的连接IP计为B
    // 如果A为127.0.0.1，OutIP全部使用InIP，否则
    // 如果InIP数量只有一个，OutIP就取A（可能和In一样）否则
    // A和InIP比较，如果发现有相同的就将OutIP全部使用InIP，否则
    // 就试图找B，如果找到了，OutIP对应的IP取A，其它全部取InIP对应值，否则
    // 就OutIP全部使用InIP（这种情况下一般不可能发生）
    public static class ECMS_NET_READY_INFO
    {
        public GUID uniqueIdentifier = new GUID();// 唯一标识GUID
        public byte bNeedCfgPack = 0;// //是否需要配置数据包
        public byte[] rev = new byte[3];// 保留字节

        public static int GetStructSize()
        {
            return 20;
        }

        public byte[] serialize() throws IOException
        {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            DataOutputStream dos = new DataOutputStream(baos);

            dos.write(uniqueIdentifier.serialize(), 0, GUID.GetStructSize());

            dos.writeByte(bNeedCfgPack);

            dos.write(rev, 0, rev.length);

            byte[] ret = baos.toByteArray();
            dos.close();
            baos.close();

            return ret;
        }
    }

    // 内外网IP通过XML附加在此结构体后面传输

    // ////////统一失败回复结构体
    public static class ECMS_NET_REPLY_RESULT
    {
        public int dwResult;// 错误码
        GUID nodeID = new GUID();// 在哪个节点上应答回来的错误
        public short wDescriptionLen;// 后面描述数据的实际有效长度
        public byte[] szDescription = new byte[246];// 一般情况下定义为错误描述，但也可以由具体协议自定义具体内容

        public static int GetStructSize()
        {
            return 268;
        }

        public static ECMS_NET_REPLY_RESULT deserialize(byte[] data, int iReadBefore) throws IOException
        {
            ByteArrayInputStream bais = new ByteArrayInputStream(data);
            DataInputStream dis = new DataInputStream(bais);
            ECMS_NET_REPLY_RESULT iResult = new ECMS_NET_REPLY_RESULT();
            byte[] testbyte = new byte[GUID.GetStructSize()];
            MyUtil myUtil = new MyUtil();

            dis.read(data, 0, iReadBefore);

            dis.read(testbyte, 0, 4);
            iResult.dwResult = myUtil.bytes2int(testbyte);

            dis.read(testbyte, 0, GUID.GetStructSize());
            iResult.nodeID = GUID.deserialize(testbyte, 0);

            dis.read(testbyte, 0, 2);
            iResult.wDescriptionLen = myUtil.bytes2short(testbyte);

            dis.read(iResult.szDescription, 0, iResult.szDescription.length);

            dis.close();
            bais.close();

            return iResult;
        }
    }

    // ///////////登陆成功的回复信息
    public static class ECMS_LOGIN_SUCCESS_INFO
    {
        public GUID remoteNodeID = new GUID();// 网络节点的ID
        public int remoteNodeType;// 网络节点的类型
        public byte[] strRemoteNodeName = new byte[64];// 网络节点的名称
        public GUID nodeID = new GUID();// 发送登录信息节点的ID,如果发送登录信息的节点不知道自己的ID就填GUID_NULL
        public GUID guidPCSerial = new GUID(); // 返回应用程序上次成功登陆RSU的 用做系统节点之间的认证
        public GUID sysRSUSerial = new GUID(); // 系统认证服务器的序列号（GUID）只有登录RSU的时候此字段才有效

        public static int GetStructSize()
        {
            return 132;
        }

        public static ECMS_LOGIN_SUCCESS_INFO deserialize(DataInputStream dis, int iReadBefore) throws IOException
        {
            ECMS_LOGIN_SUCCESS_INFO iLoginInfo = new ECMS_LOGIN_SUCCESS_INFO();
            MyUtil myUtil = new MyUtil();
            byte[] testbyte = new byte[GUID.GetStructSize()];

            dis.mark(dis.available());
            dis.skip(iReadBefore);

            dis.read(testbyte, 0, GUID.GetStructSize());
            iLoginInfo.remoteNodeID = GUID.deserialize(testbyte, 0);

            dis.read(testbyte, 0, 4);
            iLoginInfo.remoteNodeType = myUtil.bytes2int(testbyte);

            dis.read(iLoginInfo.strRemoteNodeName, 0, iLoginInfo.strRemoteNodeName.length);

            dis.read(testbyte, 0, GUID.GetStructSize());
            iLoginInfo.nodeID = GUID.deserialize(testbyte, 0);

            dis.read(testbyte, 0, GUID.GetStructSize());
            iLoginInfo.guidPCSerial = GUID.deserialize(testbyte, 0);

            dis.read(testbyte, 0, GUID.GetStructSize());
            iLoginInfo.sysRSUSerial = GUID.deserialize(testbyte, 0);

            return iLoginInfo;
        }
    }

    public static class NODEID_IP_PORT
    {
        public byte[] szIP = new byte[64];
        public short uPort;
        public short chNumIndex;

        public static int GetStructSize()
        {
            return 68;
        }

        public byte[] serialize() throws IOException
        {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            DataOutputStream dos = new DataOutputStream(baos);
            MyUtil myUtil = new MyUtil();
            byte[] testbyte = new byte[2];

            dos.write(szIP, 0, szIP.length);

            testbyte = myUtil.short2bytes(uPort);
            dos.write(testbyte, 0, 2);

            testbyte = myUtil.short2bytes(chNumIndex);
            dos.write(testbyte, 0, 2);

            byte[] ret = baos.toByteArray();
            dos.close();
            baos.close();

            return ret;
        }

        public static NODEID_IP_PORT deserialize(byte[] data, int iReadBefore) throws IOException
        {
            ByteArrayInputStream bais = new ByteArrayInputStream(data);
            DataInputStream dis = new DataInputStream(bais);
            NODEID_IP_PORT iPort = new NODEID_IP_PORT();
            MyUtil myUtil = new MyUtil();
            byte[] testbyte = new byte[2];

            dis.read(data, 0, iReadBefore);

            dis.read(iPort.szIP, 0, iPort.szIP.length);

            dis.read(testbyte, 0, 2);
            iPort.uPort = myUtil.bytes2short(testbyte);

            dis.read(testbyte, 0, 2);
            iPort.chNumIndex = myUtil.bytes2short(testbyte);

            dis.close();
            bais.close();

            return iPort;
        }
    }

    public static class NODE_ID
    {
        // NODEID_IP_PORT iNodeIPPort = new NODEID_IP_PORT();// second辅助的，二号
        public GUID nodeID_GUID = new GUID();// 通道节点和设备节点的ID 请求对讲的时候是设备节点的ID
        public byte[] bReserver = new byte[52];

        public static int GetStructSize()
        {
            return 68;
        }

        public byte[] serialize() throws IOException
        {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            DataOutputStream dos = new DataOutputStream(baos);

            // dos.write(iNodeIPPort.serialize(), 0, NODEID_IP_PORT.GetStructSize());
            dos.write(nodeID_GUID.serialize(), 0, GUID.GetStructSize());
            dos.write(bReserver, 0, bReserver.length);

            byte[] ret = baos.toByteArray();
            dos.close();
            baos.close();

            return ret;
        }

        public static NODE_ID deserialize(byte[] data, int iReadBefore) throws IOException
        {
            ByteArrayInputStream bais = new ByteArrayInputStream(data);
            DataInputStream dis = new DataInputStream(bais);
            NODE_ID iNodeID = new NODE_ID();
            byte[] testbyte = new byte[GUID.GetStructSize()];

            dis.read(data, 0, iReadBefore);

            // dis.read(testbyte, 0, NODEID_IP_PORT.GetStructSize());
            // iNodeID.iNodeIPPort = NODEID_IP_PORT.deserialize(testbyte, 0);

            dis.read(testbyte, 0, GUID.GetStructSize());
            iNodeID.nodeID_GUID = GUID.deserialize(testbyte, 0);

            dis.read(iNodeID.bReserver, 0, iNodeID.bReserver.length);

            dis.close();
            bais.close();

            return iNodeID;
        }
    }

    // 以后可能可以发一个设备内的多个通道联合在一起的这玩意
    public static class NET_STREAM_CTRL_INFO
    {
        NODE_ID iNode_ID = new NODE_ID();
        public int streamType; // //主码流 或 子码流ECMS_STREAM_TYPE
        public byte chNO;// //通道号
        public byte byPlayAudio;// 是否请求声音数据，此变量现在没有实际含义，请慎用
        public byte[] byReserve = new byte[2];// //保留

        public static int GetStructSize()
        {
            return 76;
        }

        public byte[] serialize() throws IOException
        {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            DataOutputStream dos = new DataOutputStream(baos);
            MyUtil myUtil = new MyUtil();

            dos.write(iNode_ID.serialize(), 0, NODE_ID.GetStructSize());

            streamType = myUtil.ntohl(streamType);
            dos.writeInt(streamType);

            dos.writeByte(chNO);
            dos.writeByte(byPlayAudio);
            dos.write(byReserve, 0, byReserve.length);

            byte[] ret = baos.toByteArray();
            dos.close();
            baos.close();

            return ret;
        }

        public static NET_STREAM_CTRL_INFO deserialize(byte[] data, int iReadBefore) throws IOException
        {
            ByteArrayInputStream bais = new ByteArrayInputStream(data);
            DataInputStream dis = new DataInputStream(bais);
            NET_STREAM_CTRL_INFO info = new NET_STREAM_CTRL_INFO();
            MyUtil myUtil = new MyUtil();
            byte[] testbyte = new byte[NODE_ID.GetStructSize()];

            dis.read(data, 0, iReadBefore);

            dis.read(testbyte, 0, NODE_ID.GetStructSize());
            info.iNode_ID = NODE_ID.deserialize(testbyte, 0);

            dis.read(testbyte, 0, 4);
            info.streamType = myUtil.bytes2int(testbyte);

            info.chNO = dis.readByte();
            info.byPlayAudio = dis.readByte();
            dis.read(info.byReserve, 0, info.byReserve.length);

            dis.close();
            bais.close();

            return info;
        }

    }

    // 命令头扩充 附路由信息
    public static class ECMS_PACKCMD_EXTEND
    {
        public GUID guidLink = new GUID();// 路由唯一ID ，全局唯一，由GUID产生的
        public GUID sourceID = new GUID();// 源节点的ID
        public GUID destID = new GUID();// 目的节点的ID

        public byte byDirect;// 路由方向，对应的类型 ROUTE_DIRECT_DEF
        public byte[] byReserver1 = new byte[3]; // /保留

        public static int GetStructSize()
        {
            return 52;
        }

        public byte[] serialize() throws IOException
        {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            DataOutputStream dos = new DataOutputStream(baos);

            dos.write(guidLink.serialize(), 0, GUID.GetStructSize());
            dos.write(sourceID.serialize(), 0, GUID.GetStructSize());
            dos.write(destID.serialize(), 0, GUID.GetStructSize());

            dos.writeByte(byDirect);
            dos.write(byReserver1, 0, byReserver1.length);

            byte[] ret = baos.toByteArray();
            dos.close();
            baos.close();

            return ret;
        }

        public static ECMS_PACKCMD_EXTEND deserialize(byte[] data, int iReadBefore) throws IOException
        {
            ByteArrayInputStream bais = new ByteArrayInputStream(data);
            DataInputStream dis = new DataInputStream(bais);
            ECMS_PACKCMD_EXTEND iExtend = new ECMS_PACKCMD_EXTEND();
            byte[] testbyte = new byte[GUID.GetStructSize()];

            dis.read(data, 0, iReadBefore);

            dis.read(testbyte, 0, GUID.GetStructSize());
            iExtend.guidLink = GUID.deserialize(testbyte, 0);

            dis.read(testbyte, 0, GUID.GetStructSize());
            iExtend.sourceID = GUID.deserialize(testbyte, 0);

            dis.read(testbyte, 0, GUID.GetStructSize());
            iExtend.destID = GUID.deserialize(testbyte, 0);

            iExtend.byDirect = dis.readByte();
            dis.read(iExtend.byReserver1, 0, iExtend.byReserver1.length);

            dis.close();
            bais.close();

            return iExtend;
        }
    }

    // 任务型命令头扩充信息 附路由信息和任务GUID
    public static class ECMS_PACKCMD_EXTEND_TASK
    {
        public ECMS_PACKCMD_EXTEND cmdextend = new ECMS_PACKCMD_EXTEND();
        public GUID taskguid = new GUID();// 全局唯一，由GUID产生的
        // ///////////////////
        public byte isStart; // TASKSTATE_TYPE 是开始任务还是结束任务，由于在任务传输过程中不一定有具体命令处理对象，所以需要在执行端直接确定
        public byte[] byReserver1 = new byte[3]; // /保留

        public static int GetStructSize()
        {
            return 72;
        }

        public byte[] serialize() throws IOException
        {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            DataOutputStream dos = new DataOutputStream(baos);

            dos.write(cmdextend.serialize(), 0, ECMS_PACKCMD_EXTEND.GetStructSize());
            dos.write(taskguid.serialize(), 0, GUID.GetStructSize());

            dos.writeByte(isStart);
            dos.write(byReserver1, 0, byReserver1.length);

            byte[] ret = baos.toByteArray();
            dos.close();
            baos.close();

            return ret;
        }

        public static ECMS_PACKCMD_EXTEND_TASK deserialize(byte[] data, int iReadBefore) throws IOException
        {
            ByteArrayInputStream bais = new ByteArrayInputStream(data);
            DataInputStream dis = new DataInputStream(bais);
            ECMS_PACKCMD_EXTEND_TASK iExtend = new ECMS_PACKCMD_EXTEND_TASK();
            byte[] testbyte = new byte[ECMS_PACKCMD_EXTEND.GetStructSize()];

            dis.read(data, 0, iReadBefore);

            dis.read(testbyte, 0, ECMS_PACKCMD_EXTEND.GetStructSize());
            iExtend.cmdextend = ECMS_PACKCMD_EXTEND.deserialize(testbyte, 0);

            dis.read(testbyte, 0, GUID.GetStructSize());
            iExtend.taskguid = GUID.deserialize(testbyte, 0);

            iExtend.isStart = dis.readByte();
            dis.read(iExtend.byReserver1, 0, iExtend.byReserver1.length);

            dis.close();
            bais.close();

            return iExtend;
        }
    }

    // 请求通道的录像数据

    /************************************************************************
     * REC_DATA_SEARCH + NET_STREAM_CTRL_INFO(多个)
     ************************************************************************/
    public static class REC_DATA_SEARCH
    {
        public int dwStartTime;// //开始时间
        public int dwEndTime; // ///结束时间
        public short uEventTypeMASK;//
        public byte byChannelCount;// 通道数量
        public byte byReserve; // 保留

        public static int GetStructSize()
        {
            return 12;
        }

        public byte[] serialize() throws IOException
        {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            DataOutputStream dos = new DataOutputStream(baos);
            MyUtil myUtil = new MyUtil();
            byte[] testbyte = new byte[2];

            dwStartTime = myUtil.ntohl(dwStartTime);
            dos.writeInt(dwStartTime);

            dwEndTime = myUtil.ntohl(dwEndTime);
            dos.writeInt(dwEndTime);

            testbyte = myUtil.short2bytes(uEventTypeMASK);
            dos.write(testbyte, 0, 2);

            dos.writeByte(byChannelCount);
            dos.writeByte(byReserve);

            byte[] ret = baos.toByteArray();
            dos.close();
            baos.close();

            return ret;
        }

        public static REC_DATA_SEARCH deserialize(byte[] data, int iReadBefore) throws IOException
        {
            ByteArrayInputStream bais = new ByteArrayInputStream(data);
            DataInputStream dis = new DataInputStream(bais);
            REC_DATA_SEARCH iSearch = new REC_DATA_SEARCH();
            byte[] testbyte = new byte[4];
            MyUtil myUtil = new MyUtil();

            dis.read(data, 0, iReadBefore);

            dis.read(testbyte, 0, 4);
            iSearch.dwStartTime = myUtil.bytes2int(testbyte);
            dis.read(testbyte, 0, 4);
            iSearch.dwEndTime = myUtil.bytes2int(testbyte);

            dis.read(testbyte, 0, 2);
            iSearch.uEventTypeMASK = myUtil.bytes2short(testbyte);

            iSearch.byChannelCount = dis.readByte();
            iSearch.byReserve = dis.readByte();

            dis.close();
            bais.close();

            return iSearch;
        }
    }

    /************************************************************************
     * ////////////////请求通道的录像事件的搜索
     * REC_DATA_SEARCH+NET_REC_EVENT_CTRL_INFO(多个)
     ************************************************************************/
    public static class NET_REC_EVENT_CTRL_INFO
    {
        NODE_ID iNodeID = new NODE_ID();
        public byte byChannelNum; // ////通道号
        public byte byWithDataLen; // 是否需要统计数据长度
        public byte[] byReserve = new byte[2]; // //保留

        public static int GetStructSize()
        {
            return 72;
        }

        public byte[] serialize() throws IOException
        {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            DataOutputStream dos = new DataOutputStream(baos);

            dos.write(iNodeID.serialize(), 0, NODE_ID.GetStructSize());

            dos.writeByte(byChannelNum);
            dos.writeByte(byWithDataLen);
            dos.write(byReserve, 0, byReserve.length);

            byte[] ret = baos.toByteArray();
            dos.close();
            baos.close();

            return ret;
        }
    }

    // 录像事件
    public static class RECORD_EVENT_LOG
    {
        public NODE_ID nodeID = new NODE_ID();

        public int type; // 事件类型：
        public int dwStartTime; // 事件产生的开始时间 从1970 1 1号开始绝对时间的秒数
        public int dwEndTime; // 事件的结束时间 从1970 1 1号开始绝对时间的秒数

        public int dwDataLen; // 数据长度, MB为单位

        public static int GetStructSize()
        {
            return 84;
        }

        public static RECORD_EVENT_LOG deserialize(byte[] data, int iReadBefore) throws IOException
        {
            ByteArrayInputStream bais = new ByteArrayInputStream(data);
            DataInputStream dis = new DataInputStream(bais);
            RECORD_EVENT_LOG iRecord = new RECORD_EVENT_LOG();
            byte[] testbyte = new byte[NODE_ID.GetStructSize()];
            MyUtil myUtil = new MyUtil();

            dis.read(data, 0, iReadBefore);

            dis.read(testbyte, 0, NODE_ID.GetStructSize());
            iRecord.nodeID = NODE_ID.deserialize(testbyte, 0);

            dis.read(testbyte, 0, 4);
            iRecord.type = myUtil.bytes2int(testbyte);
            dis.read(testbyte, 0, 4);
            iRecord.dwStartTime = myUtil.bytes2int(testbyte);
            dis.read(testbyte, 0, 4);
            iRecord.dwEndTime = myUtil.bytes2int(testbyte);
            dis.read(testbyte, 0, 4);
            iRecord.dwDataLen = myUtil.bytes2int(testbyte);

            dis.close();
            bais.close();

            return iRecord;
        }
    }

    // 控制云台时用
    public static class NET_PTZ_CTRL_INFO
    {
        public int dwParam; // 控制参数：预置点序号，巡航线序号，轨迹序号等
        public int dwSpeed; // 速度
        GUID chID = new GUID(); // 通道ID
        public int dwSubCmdType; // ///子命令类型
        public byte chNO; // 通道号
        public byte[] byReserved = new byte[3]; // 保留

        public static int GetStructSize()
        {
            return 32;
        }

        public byte[] serialize() throws IOException
        {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            DataOutputStream dos = new DataOutputStream(baos);
            MyUtil myUtil = new MyUtil();

            dwParam = myUtil.ntohl(dwParam);
            dos.writeInt(dwParam);

            dwSpeed = myUtil.ntohl(dwSpeed);
            dos.writeInt(dwSpeed);

            dos.write(chID.serialize(), 0, GUID.GetStructSize());

            dwSubCmdType = myUtil.ntohl(dwSubCmdType);
            dos.writeInt(dwSubCmdType);

            dos.writeByte(chNO);
            dos.write(byReserved, 0, byReserved.length);

            byte[] ret = baos.toByteArray();
            dos.close();
            baos.close();

            return ret;
        }
    }

    // 获取预置点和巡航线时用
    public static class NET_GET_PTZ_CONFIG_INFO
    {
        GUID chID = new GUID(); // 通道ID
        public byte chNO; // 通道号
        public byte[] byReserved = new byte[3]; // 保留

        public static int GetStructSize()
        {
            return 20;
        }

        public byte[] serialize() throws IOException
        {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            DataOutputStream dos = new DataOutputStream(baos);

            dos.write(chID.serialize(), 0, GUID.GetStructSize());

            dos.writeByte(chNO);
            dos.write(byReserved, 0, byReserved.length);

            byte[] ret = baos.toByteArray();
            dos.close();
            baos.close();

            return ret;
        }
    }

    public static class ECMS_RIGHT_CHECK_INFO
    {
        public int opType;// 操作类型
        public GUID destID;// 操作对象ID
        public int opParam1;
        public int opParam2;

        public static int GetStructSize()
        {
            return 28;
        }

        public byte[] serialize() throws IOException
        {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            DataOutputStream dos = new DataOutputStream(baos);
            MyUtil myUtil = new MyUtil();

            opType = myUtil.ntohl(opType);
            dos.writeInt(opType);

            dos.write(destID.serialize(), 0, GUID.GetStructSize());

            opParam1 = myUtil.ntohl(opParam1);
            dos.writeInt(opParam1);

            opParam2 = myUtil.ntohl(opParam2);
            dos.writeInt(opParam2);

            byte[] ret = baos.toByteArray();
            dos.close();
            baos.close();

            return ret;
        }

    }

    public static class FILETIME
    {
        public long dwLowDateTime;
        public int dwHighDateTime;

        public static int GetStructSize()
        {
            return 8;
        }

        public byte[] serialize() throws IOException
        {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            DataOutputStream dos = new DataOutputStream(baos);
            MyUtil myUtil = new MyUtil();

            // byte[] testbyte = myUtil.unsingedInt2byte(dwLowDateTime); // myUtil.ntohl(dwLowDateTime);
            // dos.write(testbyte, 0, 4);

            int lowDateTime = (int) dwLowDateTime;
            lowDateTime = myUtil.ntohl(lowDateTime);
            dos.writeInt(lowDateTime);

            dwHighDateTime = myUtil.ntohl(dwHighDateTime);
            dos.writeInt(dwHighDateTime);

            byte[] ret = baos.toByteArray();
            dos.close();
            baos.close();

            return ret;
        }

        public static FILETIME deserialize(byte[] data, int iReadBefore) throws IOException
        {
            ByteArrayInputStream bais = new ByteArrayInputStream(data);
            DataInputStream dis = new DataInputStream(bais);
            FILETIME iFiletime = new FILETIME();
            MyUtil myUtil = new MyUtil();
            byte[] testbyte = new byte[4];

            dis.read(data, 0, iReadBefore);

            dis.read(testbyte, 0, 4);
            iFiletime.dwLowDateTime = 0xFFFFFFFFL & myUtil.bytes2int(testbyte);

            dis.read(testbyte, 0, 4);
            iFiletime.dwHighDateTime = myUtil.bytes2int(testbyte);

            dis.close();
            bais.close();

            return iFiletime;
        }

        public void SetTime(long time)
        {
            long fileTime = (NVMSHeader.UNIX_FILETIME_DIFF + time) * NVMSHeader.MILLISECOND_MULTIPLE;
            dwHighDateTime = (int) ((fileTime >> 32) & 0xFFFFFFFF);
            dwLowDateTime = (fileTime & 0xFFFFFFFF);

            // //System.out.println("dwHighDateTime = " + dwHighDateTime + ",dwLowDateTime = " + dwLowDateTime);
        }

        public long GetTime()
        {
            long highTime = getTime();
            highTime = highTime / NVMSHeader.MILLISECOND_MULTIPLE - NVMSHeader.UNIX_FILETIME_DIFF;
            return highTime * 1000L;

            // return ((long) dwHighDateTime << 32 & 0xffffffff) | ((long) dwLowDateTime & 0xffffffff);
        }

        public long getTime()
        {
            return ((long) (((long) dwHighDateTime) << 32 & 0xffffffff)) | ((long) (dwLowDateTime & 0xffffffff));
        }
    }

    public static class STREAM_HEADER_INFO
    {
        public int swFlag;// 帧头标记
        public short swProductID; // 产品标识，标识不同公司的产品的码流
        public byte byStreamType; // 流类型 包括现场主视频流 现场子视频流 现场音频流 回放视音频流 对讲音频流流
        public byte byIsKeyFrame; // 音频流默认全当关键帧
        public GUID nodeID;// ID
        public int dwBufferLen;// 不包括本结构体后面缓冲区数据的长度，此长度是4的倍数 不仅仅包括帧数据还包括为了处理4字节对齐的数据
        /*
     * 共有数据由存储服务器录像的时候产生，由于从前端设备出去的流可能为0，
	 * 所以不要用此时间控制播放，单位是PC上毫秒级的
	 * 绝对时间，误差为15毫秒左右
	 */
        public FILETIME ftRecTime = new FILETIME(); // 可以理解为到达存储服务器的时间，录像的时候产生的时间，跟前端设备没关系，主要用来做定位用

        /* 此信息后面直接拿出去，另外在发送之前加到帧的外面 */
        public int dwSendFrameIndex; // 发送的帧的索引值
        public int dwTaskID; // 回放的任务ID

        public static int GetStructSize()
        {
            return 44;
        }

        public static STREAM_HEADER_INFO deserialize(byte[] data, int iReadBefore) throws IOException
        {
            ByteArrayInputStream bais = new ByteArrayInputStream(data);
            DataInputStream dis = new DataInputStream(bais);
            STREAM_HEADER_INFO iHeaderInfo = new STREAM_HEADER_INFO();
            MyUtil myUtil = new MyUtil();
            byte[] testbyte = new byte[GUID.GetStructSize()];

            dis.read(data, 0, iReadBefore);

            dis.read(testbyte, 0, 4);
            iHeaderInfo.swFlag = myUtil.bytes2int(testbyte);

            dis.read(testbyte, 0, 2);
            iHeaderInfo.swProductID = myUtil.bytes2short(testbyte);

            iHeaderInfo.byStreamType = dis.readByte();
            iHeaderInfo.byIsKeyFrame = dis.readByte();

            dis.read(testbyte, 0, GUID.GetStructSize());
            iHeaderInfo.nodeID = GUID.deserialize(testbyte, 0);

            dis.read(testbyte, 0, 4);
            iHeaderInfo.dwBufferLen = myUtil.bytes2int(testbyte);

            dis.read(testbyte, 0, FILETIME.GetStructSize());
            iHeaderInfo.ftRecTime = FILETIME.deserialize(testbyte, 0);

            dis.read(testbyte, 0, 4);
            iHeaderInfo.dwSendFrameIndex = myUtil.bytes2int(testbyte);

            dis.read(testbyte, 0, 4);
            iHeaderInfo.dwTaskID = myUtil.bytes2int(testbyte);

            dis.close();
            bais.close();

            return iHeaderInfo;
        }

        public byte[] serialize() throws IOException
        {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            DataOutputStream dos = new DataOutputStream(baos);
            MyUtil myUtil = new MyUtil();
            byte[] testbyte = new byte[16];

            swFlag = myUtil.ntohl(swFlag);
            dos.writeInt(swFlag);

            testbyte = myUtil.short2bytes(swProductID);
            dos.write(testbyte, 0, 2);

            dos.writeByte(byStreamType);
            dos.writeByte(byIsKeyFrame);

            dos.write(nodeID.serialize(), 0, GUID.GetStructSize());

            dwBufferLen = myUtil.ntohl(dwBufferLen);
            dos.writeInt(dwBufferLen);

            dos.write(ftRecTime.serialize(), 0, FILETIME.GetStructSize());

            dwSendFrameIndex = myUtil.ntohl(dwSendFrameIndex);
            dos.writeInt(dwSendFrameIndex);
            dwTaskID = myUtil.ntohl(dwTaskID);
            dos.writeInt(dwTaskID);

            byte[] ret = baos.toByteArray();
            dos.close();
            baos.close();

            return ret;
        }
    }

    /************************************************************************
     * 以下是同为设备的私有数据信息，封装在解码库里面，如果是解码卡需要转化为解码卡
     * 支持的数据信息
     ************************************************************************/

    // ////////////帧头信息，每帧数据都有此标记
    public static class FRAME_HEADER_INFO
    {
        public byte byFrameType;// ///帧类型 视频或音频
        public byte byExtInfoLen; // ///扩充信息长度 如果有扩充信息为sizeof(FRAME_EXTENSION_INFO),否则为0
        public byte[] res = new byte[2];// 保留字节
        public int dwRealFrameLen;// 不包括任何结构体的有效帧数据的长度
        public FILETIME ftDevTime = new FILETIME();// 时间戳上的时间,也就是设备采集此帧的绝对时间，私有数据由前端设备填充
        public FILETIME ftECMSTime = new FILETIME(); // 在ECMS上的系统绝对时间不会随设备时间变化而变化，同时记录了准确的帧间隔

        // 注意此时间对于现场是以设备所在转发服务器为基准的时间，对录像数据是以存储服务器为基准的时间

        public static int GetStructSize()
        {
            return 24;
        }

        public static FRAME_HEADER_INFO deserialize(byte[] data, int iReadBefore) throws IOException
        {
            ByteArrayInputStream bais = new ByteArrayInputStream(data);
            DataInputStream dis = new DataInputStream(bais);
            FRAME_HEADER_INFO iHeaderInfo = new FRAME_HEADER_INFO();
            MyUtil myUtil = new MyUtil();
            byte[] testbyte = new byte[FILETIME.GetStructSize()];

            dis.read(data, 0, iReadBefore);

            iHeaderInfo.byFrameType = dis.readByte();
            iHeaderInfo.byExtInfoLen = dis.readByte();

            dis.read(iHeaderInfo.res, 0, iHeaderInfo.res.length);

            dis.read(testbyte, 0, 4);
            iHeaderInfo.dwRealFrameLen = myUtil.bytes2int(testbyte);

            dis.read(testbyte, 0, FILETIME.GetStructSize());
            iHeaderInfo.ftDevTime = FILETIME.deserialize(testbyte, 0);

            dis.read(testbyte, 0, FILETIME.GetStructSize());
            iHeaderInfo.ftECMSTime = FILETIME.deserialize(testbyte, 0);

            dis.close();
            bais.close();

            return iHeaderInfo;
        }

        public byte[] serialize() throws IOException
        {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            DataOutputStream dos = new DataOutputStream(baos);
            MyUtil myUtil = new MyUtil();

            dos.writeByte(byFrameType);
            dos.writeByte(byExtInfoLen);

            dos.write(res, 0, res.length);

            dwRealFrameLen = myUtil.ntohl(dwRealFrameLen);
            dos.writeInt(dwRealFrameLen);

            dos.write(ftDevTime.serialize(), 0, FILETIME.GetStructSize());
            dos.write(ftECMSTime.serialize(), 0, FILETIME.GetStructSize());

            byte[] ret = baos.toByteArray();
            dos.close();
            baos.close();

            return ret;
        }

    }

    public static class FRAME_VIDEO_EXTENSION_INFO
    {
        public byte byVersion;// 扩展信息版本
        public byte byVideoFormat;// 视频制式 0 PAL 1为N制
        public short wReserver;//
        public int dwVideoEncodeType;// 视频编码类型
        public short swVideoWidth;// 视频宽度
        public short swVideoHeight;// 高度

        public static int GetStructSize()
        {
            return 12;
        }

        public static FRAME_VIDEO_EXTENSION_INFO deserialize(byte[] data, int iReadBefore) throws IOException
        {
            ByteArrayInputStream bais = new ByteArrayInputStream(data);
            DataInputStream dis = new DataInputStream(bais);
            FRAME_VIDEO_EXTENSION_INFO iFrameInfo = new FRAME_VIDEO_EXTENSION_INFO();
            byte[] testbyte = new byte[4];
            MyUtil myUtil = new MyUtil();

            dis.read(data, 0, iReadBefore);

            iFrameInfo.byVersion = dis.readByte();
            iFrameInfo.byVideoFormat = dis.readByte();

            dis.read(testbyte, 0, 2);
            iFrameInfo.wReserver = myUtil.bytes2short(testbyte);

            dis.read(testbyte, 0, 4);
            iFrameInfo.dwVideoEncodeType = myUtil.bytes2int(testbyte);

            dis.read(testbyte, 0, 2);
            iFrameInfo.swVideoWidth = myUtil.bytes2short(testbyte);

            dis.read(testbyte, 0, 2);
            iFrameInfo.swVideoHeight = myUtil.bytes2short(testbyte);

            dis.close();
            bais.close();

            return iFrameInfo;
        }
    }

    public static class FRAME_AUDIO_EXTENSION_INFO
    {
        public byte byVersion;// 扩展信息版本
        public byte byChannels;// 声道数量
        public short wAudioEncodeType;// 音频编码类型
        public short wBitsPerSample; //
        public short wAvgBytesPerSec;// 采样平均位数
        public int dwSamplesPerSec;// 采样率

        public static int GetStructSize()
        {
            return 12;
        }

        public static FRAME_AUDIO_EXTENSION_INFO deserialize(byte[] data, int iReadBefore) throws IOException
        {
            ByteArrayInputStream bais = new ByteArrayInputStream(data);
            DataInputStream dis = new DataInputStream(bais);
            FRAME_AUDIO_EXTENSION_INFO iFrameInfo = new FRAME_AUDIO_EXTENSION_INFO();
            byte[] testbyte = new byte[4];
            MyUtil myUtil = new MyUtil();

            dis.read(data, 0, iReadBefore);

            iFrameInfo.byVersion = dis.readByte();
            iFrameInfo.byChannels = dis.readByte();

            dis.read(testbyte, 0, 2);
            iFrameInfo.wAudioEncodeType = myUtil.bytes2short(testbyte);

            dis.read(testbyte, 0, 2);
            iFrameInfo.wBitsPerSample = myUtil.bytes2short(testbyte);

            dis.read(testbyte, 0, 2);
            iFrameInfo.wAvgBytesPerSec = myUtil.bytes2short(testbyte);

            dis.read(testbyte, 0, 4);
            iFrameInfo.dwSamplesPerSec = myUtil.bytes2int(testbyte);

            dis.close();
            bais.close();

            return iFrameInfo;
        }

        public byte[] serialize() throws IOException
        {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            DataOutputStream dos = new DataOutputStream(baos);
            MyUtil myUtil = new MyUtil();
            byte[] testbyte = new byte[2];

            dos.writeByte(byVersion);
            dos.writeByte(byChannels);

            testbyte = myUtil.short2bytes(wAudioEncodeType);
            dos.write(testbyte, 0, 2);
            testbyte = myUtil.short2bytes(wBitsPerSample);
            dos.write(testbyte, 0, 2);
            testbyte = myUtil.short2bytes(wAvgBytesPerSec);
            dos.write(testbyte, 0, 2);

            dwSamplesPerSec = myUtil.ntohl(dwSamplesPerSec);
            dos.writeInt(dwSamplesPerSec);

            byte[] ret = baos.toByteArray();
            dos.close();
            baos.close();

            return ret;
        }
    }

    public static class NET_RECORD_STATE_INFO
    {
        GUID storageID;// 存储服务器的节点ID
        GUID nodeID;// //通道节点ID
        int recordState;// 录像状态

        public static int GetStructSize()
        {
            return 36;
        }

        public static NET_RECORD_STATE_INFO deserialize(byte[] data, int iReadBefore) throws IOException
        {
            ByteArrayInputStream bais = new ByteArrayInputStream(data);
            DataInputStream dis = new DataInputStream(bais);
            NET_RECORD_STATE_INFO iHeaderInfo = new NET_RECORD_STATE_INFO();
            MyUtil myUtil = new MyUtil();
            byte[] testbyte = new byte[GUID.GetStructSize()];

            dis.read(data, 0, iReadBefore);

            dis.read(testbyte, 0, GUID.GetStructSize());
            iHeaderInfo.storageID = GUID.deserialize(testbyte, 0);

            dis.read(testbyte, 0, GUID.GetStructSize());
            iHeaderInfo.nodeID = GUID.deserialize(testbyte, 0);

            dis.read(testbyte, 0, 4);
            iHeaderInfo.recordState = myUtil.bytes2int(testbyte);

            dis.close();
            bais.close();

            return iHeaderInfo;
        }
    }

    public static class NET_ALARM_STATE_INFO
    {
        public int dwNodeType;// 节点类型
        public GUID nodeID = new GUID();// //节点ID
        public int alarmType;// 报警类型
        public byte[] byReserve = new byte[3];// 保留
        public byte byisAlarm;// /非0是报警状态，0是非报警状态

        public static int GetStructSize()
        {
            return 28;
        }

        public static NET_ALARM_STATE_INFO deserialize(byte[] data, int iReadBefore) throws IOException
        {
            ByteArrayInputStream bais = new ByteArrayInputStream(data);
            DataInputStream dis = new DataInputStream(bais);
            NET_ALARM_STATE_INFO iHeaderInfo = new NET_ALARM_STATE_INFO();
            MyUtil myUtil = new MyUtil();
            byte[] testbyte = new byte[GUID.GetStructSize()];

            dis.read(data, 0, iReadBefore);

            dis.read(testbyte, 0, 4);
            iHeaderInfo.dwNodeType = myUtil.bytes2int(testbyte);

            dis.read(testbyte, 0, GUID.GetStructSize());
            iHeaderInfo.nodeID = GUID.deserialize(testbyte, 0);

            dis.read(testbyte, 0, 4);
            iHeaderInfo.alarmType = myUtil.bytes2int(testbyte);

            dis.read(iHeaderInfo.byReserve, 0, iHeaderInfo.byReserve.length);
            iHeaderInfo.byisAlarm = dis.readByte();

            dis.close();
            bais.close();

            return iHeaderInfo;
        }
    }

    public static class REPLY_INDEX_INFO
    {
        public int dwIndexValue;// 对应的索引值

        public static int GetStructSize()
        {
            return 4;
        }

        public byte[] serialize() throws IOException
        {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            DataOutputStream dos = new DataOutputStream(baos);
            MyUtil myUtil = new MyUtil();

            dwIndexValue = myUtil.ntohl(dwIndexValue);
            dos.writeInt(dwIndexValue);

            byte[] ret = baos.toByteArray();
            dos.close();
            baos.close();

            return ret;
        }
    }

    public static class PLAY_CONTROL_INFO
    {
        public FILETIME ftFrameTime = new FILETIME(); // 回放操作时的帧时间
        public int dwSectionTime; // 对应操作的时间段时间

        public static int GetStructSize()
        {
            return 12;
        }

        public byte[] serialize() throws IOException
        {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            DataOutputStream dos = new DataOutputStream(baos);
            MyUtil myUtil = new MyUtil();

            dos.write(ftFrameTime.serialize(), 0, FILETIME.GetStructSize());

            dwSectionTime = myUtil.ntohl(dwSectionTime);
            dos.writeInt(dwSectionTime);

            byte[] ret = baos.toByteArray();
            dos.close();
            baos.close();

            return ret;
        }

    }

    // 云台3D放大
    public static class NET_PTZ_3DCTRL_INFO
    {
        public int dx; // x坐标
        public int dy; // y坐标
        public GUID chID = new GUID(); // 通道ID
        public short zoom; // 变倍倍数
        public byte chNO; // 通道号
        public byte byReserved; // 保留

        public static int GetStructSize()
        {
            return 28;
        }

        public static byte[] intToBytes2(int value)
        {
            byte[] src = new byte[4];
            src[3] = (byte) ((value >> 24) & 0xFF);
            src[2] = (byte) ((value >> 16) & 0xFF);
            src[1] = (byte) ((value >> 8) & 0xFF);
            src[0] = (byte) (value & 0xFF);
            return src;
        }

        public byte[] serialize() throws IOException
        {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            DataOutputStream dos = new DataOutputStream(baos);
            MyUtil myUtil = new MyUtil();
            byte[] testbyte = new byte[2];

            dos.write(intToBytes2(dx));
            dos.write(intToBytes2(dy));

            dos.write(chID.serialize(), 0, GUID.GetStructSize());

            testbyte = myUtil.short2bytes(zoom);
            dos.write(testbyte, 0, 2);

            dos.writeByte(chNO);
            dos.writeByte(byReserved);

            byte[] ret = baos.toByteArray();
            dos.close();
            baos.close();

            return ret;
        }

    }

    public static class NODE_CONNECT_STATE
    {
        public GUID nodeID = new GUID();
        public int nodeType;
        public int nodeConnectState;
        public int nodeInBitratePS;// 节点输入流量统计(每秒64K的倍数)
        public int nodeOutBitratePS;// 节点输出流量统计(每秒64K的倍数)
        public int nodeInBitrateTotal;// 节点输入流量统计(总共统计值也是64K倍数)
        public int nodeOutBitrateTotal;// 节点输出流量统计(总共统计值也是64K倍数)

        public static int GetStructSize()
        {
            return 40;
        }

        public static NODE_CONNECT_STATE deserialize(byte[] data, int iReadBefore) throws IOException
        {
            ByteArrayInputStream bais = new ByteArrayInputStream(data);
            DataInputStream dis = new DataInputStream(bais);
            NODE_CONNECT_STATE iState = new NODE_CONNECT_STATE();
            MyUtil myUtil = new MyUtil();
            byte[] testbyte = new byte[16];

            dis.read(data, 0, iReadBefore);

            dis.read(testbyte, 0, GUID.GetStructSize());
            iState.nodeID = GUID.deserialize(testbyte, 0);

            dis.read(testbyte, 0, 4);
            iState.nodeType = myUtil.bytes2int(testbyte);
            dis.read(testbyte, 0, 4);
            iState.nodeConnectState = myUtil.bytes2int(testbyte);
            dis.read(testbyte, 0, 4);
            iState.nodeInBitratePS = myUtil.bytes2int(testbyte);
            dis.read(testbyte, 0, 4);
            iState.nodeOutBitratePS = myUtil.bytes2int(testbyte);
            dis.read(testbyte, 0, 4);
            iState.nodeInBitrateTotal = myUtil.bytes2int(testbyte);
            dis.read(testbyte, 0, 4);
            iState.nodeOutBitrateTotal = myUtil.bytes2int(testbyte);

            dis.close();
            bais.close();

            return iState;
        }
    }

    public static class API_CONFIG_INFO
    {
        public byte[] username = new byte[64];
        public byte[] strCMD = new byte[64];

        public static int GetStructSize()
        {
            return 128;
        }

        public byte[] serialize() throws IOException
        {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            DataOutputStream dos = new DataOutputStream(baos);

            dos.write(username, 0, username.length);
            dos.write(strCMD, 0, strCMD.length);

            byte[] ret = baos.toByteArray();
            dos.close();
            baos.close();

            return ret;
        }
    }

    public static class NET_CHANNEL_STATUS
    {
        public GUID nodeID;
        public String name = "";
        public String online = "";
        public String motionStatus = "";
        public String recStatus = "";
        public String intelligentStatus = "";

        public static ArrayList<NET_CHANNEL_STATUS> deserialize(String strMessage)
        {
            ArrayList<NET_CHANNEL_STATUS> iStatus = new ArrayList<NET_CHANNEL_STATUS>();

            NodeList nodeList = NVMSHeader.GetNodeList(strMessage, "content");
            if (nodeList == null)
            {
                return iStatus;
            }
            Node iItemNode = nodeList.item(0);
            if (!iItemNode.hasChildNodes())
            {
                return iStatus;
            }
            nodeList = iItemNode.getChildNodes();
            for (int i = 0; i < nodeList.getLength(); i++)
            {
                Node item = nodeList.item(i);
                if (!item.hasChildNodes())
                {
                    continue;
                }
                String itemName = item.getNodeName();
                if (!itemName.equals("item"))
                {
                    continue;
                }
                NodeList properties = item.getChildNodes();
                NET_CHANNEL_STATUS iChlStatus = new NET_CHANNEL_STATUS();
                for (int j = 0; j < properties.getLength(); j++)
                {
                    Node property = properties.item(j);
                    if (property.getFirstChild() == null)
                    {
                        continue;
                    }
                    String nodeName = property.getNodeName();
                    if (nodeName.equals("chl"))
                    {
                        Element iElement = (Element) properties.item(j);
                        iChlStatus.nodeID = GUID.GetGUID(iElement.getAttribute("id").replace("{", "").replace("}", ""));
                        iChlStatus.name = property.getFirstChild().getNodeValue();
                    }
                    else if (nodeName.equals("online"))
                    {
                        iChlStatus.online = property.getFirstChild().getNodeValue();
                    }
                    else if (nodeName.equals("motionStatus"))
                    {
                        iChlStatus.motionStatus = property.getFirstChild().getNodeValue();
                    }
                    else if (nodeName.equals("recStatus"))
                    {
                        iChlStatus.recStatus = property.getFirstChild().getNodeValue();
                    }
                    else if (nodeName.equals("intelligentStatus"))
                    {
                        iChlStatus.intelligentStatus = property.getFirstChild().getNodeValue();
                    }
                }
                iStatus.add(iChlStatus);
            }

            return iStatus;
        }
    }

    public static class NET_RECORD_STATUS
    {
        public GUID nodeID;
        public String name = "";
        public String resolution = "";
        public int frameRate;
        public int quality;
        public String recStatus = "";
        public ArrayList<String> recTypes = new ArrayList<String>();

        public static ArrayList<NET_RECORD_STATUS> deserialize(String strMessage)
        {
            long time1 = System.currentTimeMillis();
            ArrayList<NET_RECORD_STATUS> iStatus = new ArrayList<NET_RECORD_STATUS>();
            NodeList nodeList = NVMSHeader.GetNodeList(strMessage, "content");
            if (nodeList == null)
            {
                return iStatus;
            }
            Node item = nodeList.item(0);
            NodeList properties = item.getChildNodes();

            long time2 = System.currentTimeMillis();

            for (int i = 0; i < properties.getLength(); i++)
            {
                Node property = properties.item(i);
                String nodeName = property.getNodeName();
                if (!nodeName.equals("item"))
                {
                    continue;
                }
                NodeList iNodeList = property.getChildNodes();
                NET_RECORD_STATUS iAlarmStatus = new NET_RECORD_STATUS();
                for (int j = 0; j < iNodeList.getLength(); j++)
                {
                    Node iItemNode = iNodeList.item(j);
                    String iItemName = iItemNode.getNodeName();

                    if (iItemName.equals("chl"))
                    {
                        if (iItemNode.getFirstChild() == null)
                        {
                            continue;
                        }
                        Element iElement = (Element) iNodeList.item(j);
                        iAlarmStatus.nodeID = GUID.GetGUID(iElement.getAttribute("id").replace("{", "").replace("}", ""));
                        iAlarmStatus.name = iItemNode.getFirstChild().getNodeValue();
                    }
                    else if (iItemName.equals("resolution"))
                    {
                        if (iItemNode.getFirstChild() == null)
                        {
                            continue;
                        }
                        iAlarmStatus.resolution = iItemNode.getFirstChild().getNodeValue();
                    }
                    else if (iItemName.equals("frameRate"))
                    {
                        if (iItemNode.getFirstChild() == null)
                        {
                            continue;
                        }
                        iAlarmStatus.frameRate = Integer.parseInt(iItemNode.getFirstChild().getNodeValue());
                    }
                    else if (iItemName.equals("quality"))
                    {
                        if (iItemNode.getFirstChild() == null)
                        {
                            continue;
                        }
                        iAlarmStatus.quality = Integer.parseInt(iItemNode.getFirstChild().getNodeValue());
                    }
                    else if (iItemName.equals("recStatus"))
                    {
                        if (iItemNode.getFirstChild() == null)
                        {
                            continue;
                        }
                        iAlarmStatus.recStatus = iItemNode.getFirstChild().getNodeValue();
                    }
                    else if (iItemName.equals("recTypes"))
                    {
                        NodeList recTypes = iItemNode.getChildNodes();
                        for (int k = 0; k < recTypes.getLength(); k++)
                        {
                            Node recTypeNode = recTypes.item(k);
                            if (recTypeNode.getFirstChild() == null)
                            {
                                continue;
                            }
                            String recTypeName = recTypeNode.getNodeName();
                            if (recTypeName.equals("item"))
                            {
                                String recType = recTypeNode.getFirstChild().getNodeValue();
                                iAlarmStatus.recTypes.add(recType);
                            }
                        }
                    }
                }
                iStatus.add(iAlarmStatus);
            }
            long time3 = System.currentTimeMillis();

            System.out.println("-------time2-time1 = " + (time2 - time1) + ",time3-time2 = " + (time3 - time2));
            return iStatus;
        }

        //dom4j
        //    public static ArrayList<NET_RECORD_STATUS> deserialize1(String strMessage)
        //    {
        ////        System.out.println("---1------NET_RECORD_STATUS.deserialize.strMessage = " + strMessage);
        //        ArrayList<NET_RECORD_STATUS> iStatus = new ArrayList<NET_RECORD_STATUS>();
        //        try
        //        {
        //            org.dom4j.Document document = DocumentHelper.parseText(strMessage);
        //            org.dom4j.Element root = document.getRootElement();
        //
        //            List<org.dom4j.Element> childList = root.elements("content");
        ////            System.out.println("content child count: " + childList.size());
        //
        //            for(int i = 0; i < childList.size(); i++)
        //            {
        //                org.dom4j.Element child = childList.get(i);
        //                List<org.dom4j.Element> grandChildList = child.elements("item");
        //                org.dom4j.Element foo;
        //                for(int j = 0; j < grandChildList.size(); j++)
        //                {
        //                    NET_RECORD_STATUS iAlarmStatus = new NET_RECORD_STATUS();
        //                    foo = (org.dom4j.Element) grandChildList.get(j);
        //
        //                    org.dom4j.Element foochild = foo.element("chl");
        //                    if(foochild == null)
        //                    {
        //                        continue;
        //                    }
        //                    else
        //                    {
        //                        //                        String s = foo.attributeValue("id");
        //                        //                        System.out.println("---------id = "+s);
        //                        //
        ////                        org.dom4j.Element foochild = foo.element("chl");
        ////                        String s = foochild.attributeValue("id");
        ////                        System.out.println("---------id = " + s);
        //
        //                        iAlarmStatus.nodeID = GUID.GetGUID(foochild.attributeValue("id").replace("{", "").replace("}", ""));
        //                        iAlarmStatus.name = foochild.getText();//foo.elementText("chl");
        //                    }
        //
        //                    foochild = foo.element("resolution");
        //                    if(foochild == null)
        //                    {
        //                        continue;
        //                    }
        //                    else
        //                    {
        //                        iAlarmStatus.resolution = foochild.getText();//foo.elementText("resolution");
        //                    }
        //
        //                    foochild = foo.element("frameRate");
        //                    if(foochild == null)
        //                    {
        //                        continue;
        //                    }
        //                    else
        //                    {
        //                        if(!foochild.getText().equals(""))
        //                        {
        //                            iAlarmStatus.frameRate = Integer.parseInt(foochild.getText());
        //                        }
        //                    }
        //
        //                    foochild = foo.element("quality");
        //                    if(foochild == null)
        //                    {
        //                        continue;
        //                    }
        //                    else
        //                    {
        //                        if(!foochild.getText().equals(""))
        //                        {
        //                            iAlarmStatus.quality = Integer.parseInt(foochild.getText());
        //                        }
        //                    }
        //
        //                    foochild = foo.element("recStatus");
        //                    if(foochild == null)
        //                    {
        //                        continue;
        //                    }
        //                    else
        //                    {
        //                        iAlarmStatus.recStatus = foochild.getText();//foo.elementText("recStatus");
        //                    }
        //
        //                    foochild = foo.element("recTypes");
        //                    if(foochild == null)
        //                    {
        //                        continue;
        //                    }
        //                    else
        //                    {
        ////                        org.dom4j.Element foochild = foo.element("recTypes");
        ////                        if(foochild == null)
        ////                        {
        ////                            continue;
        ////                        }
        ////                        else
        ////                        {
        //                            List nodes = foochild.elements("item");
        //                            if(nodes == null)
        //                            {
        //                                continue;
        //                            }
        //                            else
        //                            {
        //                                for(Iterator it = nodes.iterator(); it.hasNext(); )
        //                                {
        //                                    org.dom4j.Element fo = (org.dom4j.Element) it.next();
        ////                                    String recType = fo.getText();
        //                                    iAlarmStatus.recTypes.add(fo.getText());
        //                                }
        //                            }
        ////                        }
        //                    }
        //                    iStatus.add(iAlarmStatus);
        //                }
        //            }
        //
        //            //            for(Iterator i = root.elementIterator("item"); i.hasNext(); )
        //            //            {
        //            //                NET_RECORD_STATUS iAlarmStatus = new NET_RECORD_STATUS();
        //            //                foo = (org.dom4j.Element) i.next();
        //            //
        //            //
        //            //
        //            //            }
        //        }
        //        catch(DocumentException e)
        //        {
        //            e.printStackTrace();
        //        }
        //        return iStatus;
        //    }

        //jdom
        //    public static ArrayList<NET_RECORD_STATUS> deserialize2(String strMessage)
        //    {
        //        long time1 = System.currentTimeMillis();
        //        StringReader xmlString = new StringReader(strMessage);
        //        InputSource source = new InputSource(xmlString);
        //        SAXBuilder saxb = new SAXBuilder();
        //
        //        ArrayList<NET_RECORD_STATUS> iStatus = new ArrayList<NET_RECORD_STATUS>();
        //        try
        //        {
        //
        //            org.jdom2.Document doc = saxb.build(source);
        //            org.jdom2.Element response = doc.getRootElement();
        //            List<org.jdom2.Element> contentlist = response.getChildren("content");
        //
        //            long time2 = System.currentTimeMillis();
        //
        //            for(int i = 0; i < contentlist.size(); i++)
        //            {
        //                org.jdom2.Element content = contentlist.get(i);
        //                List<org.jdom2.Element> itemlist = content.getChildren("item");
        //                org.jdom2.Element item;
        //                for(int j = 0; j < itemlist.size(); j++)
        //                {
        //                    NET_RECORD_STATUS iAlarmStatus = new NET_RECORD_STATUS();
        //                    item = (org.jdom2.Element) itemlist.get(j);
        //
        //                    org.jdom2.Element itemchild = item.getChild("chl");
        //                    if(itemchild == null)
        //                    {
        //                        continue;
        //                    }
        //                    else
        //                    {
        //                        //                        String s = foo.attributeValue("id");
        //                        //                        System.out.println("---------id = "+s);
        //                        //
        //                        //                        org.jdom2.Element itemchild = foo.element("chl");
        //                        //                        String s = itemchild.attributeValue("id");
        //                        //                        System.out.println("---------id = " + s);
        //
        //                        iAlarmStatus.nodeID = GUID.GetGUID(itemchild.getAttributeValue("id").replace("{", "").replace("}", ""));
        //                        iAlarmStatus.name = itemchild.getText();
        //                    }
        //
        //                    itemchild = item.getChild("resolution");
        //                    if(itemchild == null)
        //                    {
        //                        continue;
        //                    }
        //                    else
        //                    {
        //                        iAlarmStatus.resolution = itemchild.getText();//item.getChildText("resolution");
        //                    }
        //
        //                    itemchild = item.getChild("frameRate");
        //                    if(itemchild == null)
        //                    {
        //                        continue;
        //                    }
        //                    else
        //                    {
        //                        if(!itemchild.getText().equals(""))
        //                        {
        //                            iAlarmStatus.frameRate = Integer.parseInt(itemchild.getText());
        //                        }
        //                    }
        //
        //                    itemchild = item.getChild("quality");
        //                    if(itemchild == null)
        //                    {
        //                        continue;
        //                    }
        //                    else
        //                    {
        //                        if(!itemchild.getText().equals(""))
        //                        {
        //                            iAlarmStatus.quality = Integer.parseInt(itemchild.getText());
        //                        }
        //                    }
        //
        //                    itemchild = item.getChild("recStatus");
        //                    if(itemchild == null)
        //                    {
        //                        continue;
        //                    }
        //                    else
        //                    {
        //                        iAlarmStatus.recStatus = itemchild.getText();//item.getChildText("recStatus");
        //                    }
        //
        //                    itemchild = item.getChild("recTypes");
        //                    if(itemchild == null)
        //                    {
        //                        continue;
        //                    }
        //                    else
        //                    {
        //
        //
        //                        List nodes = itemchild.getChildren("item");
        //                        if(nodes == null)
        //                        {
        //                            continue;
        //                        }
        //                        else
        //                        {
        //                            for(Iterator it = nodes.iterator(); it.hasNext(); )
        //                            {
        //                                org.jdom2.Element fo = (org.jdom2.Element) it.next();
        //                                //                                    String recType = fo.getText();
        //                                iAlarmStatus.recTypes.add(fo.getText());
        //                            }
        //                        }
        //                        //                        }
        //                    }
        //                    iStatus.add(iAlarmStatus);
        //                }
        //            }
        //            long time3 = System.currentTimeMillis();
        //            System.out.println("-------time2-time1 = "+(time2-time1)+",time3-time2 = "+(time3-time2));
        //        }
        //        catch(Exception e)
        //        {
        //            e.printStackTrace();
        //        }
        //        return iStatus;
        //    }
    }

    public static class NET_ALARM_CHANNELS
    {
        public GUID nodeID;
        public String name = "";
        public String nodeNic = "";
    }

    public static class NET_ALARM
    {
        public NET_ALARM_CHANNELS sourceChl = new NET_ALARM_CHANNELS();
        public List<NET_ALARM_CHANNELS> triggerRecChls = new ArrayList<NET_ALARM_CHANNELS>();
        public String triggerAlarmOutNames = "";
        public String triggerPresetNames = "";
        public String alarmTime = "";
        public boolean buzzerSwitch = false;
        public boolean popVideoSwitch = false;
        public boolean popMsgSwitch = false;
        public boolean emailSwitch = false;
        public boolean ftpRecSwitch = false;
        public boolean snapSwitch = false;
        public boolean ftpSnapSwitch = false;
        public String abnormalType = "";
        public String triggerCondition = "";
        public String intelligentType = "";
    }

    public static class NET_ALARM_STATUS
    {
        public ArrayList<NET_ALARM> alarmIn = new ArrayList<NET_ALARM>();
        public ArrayList<NET_ALARM> alarmOut = new ArrayList<NET_ALARM>();
        public ArrayList<NET_ALARM> motion = new ArrayList<NET_ALARM>();
        public ArrayList<NET_ALARM> abnormals = new ArrayList<NET_ALARM>();
        public ArrayList<NET_ALARM> intelligent = new ArrayList<NET_ALARM>();

        public static NET_ALARM_STATUS deserialize(String strMessage)
        {
            NET_ALARM_STATUS iStatus = new NET_ALARM_STATUS();

            NodeList nodeList = NVMSHeader.GetNodeList(strMessage, "content");
            if (nodeList == null)
            {
                return iStatus;
            }
            Node item = nodeList.item(0);

            NodeList iContentList = item.getChildNodes();
            for (int i = 0; i < iContentList.getLength(); i++)// motions
            {
                Node iAlarmNode = iContentList.item(i);

                NodeList iNodeList = iAlarmNode.getChildNodes();
                for (int j = 0; j < iNodeList.getLength(); j++)// item子项
                {
                    NET_ALARM iNetAlarm = new NET_ALARM();
                    Node iItemNode = iNodeList.item(j);
                    NodeList iItemList = iItemNode.getChildNodes();
                    for (int k = 0; k < iItemList.getLength(); k++)
                    {
                        Node iNode = iItemList.item(k);

                        String nodeName = iNode.getNodeName();
                        if (iNode.getFirstChild() == null)
                        {
                            continue;
                        }
                        if (nodeName.equals("#text"))
                        {
                            continue;
                        }
                        if (nodeName.equals("sourceAlarmIn") || nodeName.equals("sourceChl") || nodeName.equals("alarmOut") || nodeName.equals("alarmNode"))
                        {
                            Element iElement = (Element) iItemList.item(k);
                            iNetAlarm.sourceChl.nodeID = GUID.GetGUID(iElement.getAttribute("id").replace("{", "").replace("}", ""));
                            iNetAlarm.sourceChl.nodeNic = iElement.getAttribute("nic");
                            iNetAlarm.sourceChl.name = iNode.getFirstChild().getNodeValue();
                        }
                        else if (nodeName.equals("triggerRecChls"))
                        {
                            NodeList iNodeList2 = iNode.getChildNodes();
                            NET_ALARM_CHANNELS iChannels = new NET_ALARM_CHANNELS();
                            for (int l = 0; l < iNodeList2.getLength(); l++)
                            {
                                Node iNode2 = iNodeList2.item(l);
                                String nodeName2 = iNode2.getNodeName();
                                if (nodeName2.equals("item"))
                                {
                                    Element iElement = (Element) iNodeList2.item(l);
                                    iChannels.nodeID = GUID.GetGUID(iElement.getAttribute("id").replace("{", "").replace("}", ""));
                                    iChannels.name = iNode2.getFirstChild().getNodeValue();
                                }
                            }
                            if (iChannels.nodeID != null)
                            {
                                iNetAlarm.triggerRecChls.add(iChannels);
                            }
                        }
                        else if (nodeName.equals("triggerAlarmOutNames"))
                        {
                            iNetAlarm.triggerAlarmOutNames = iNode.getFirstChild().getNodeValue();
                        }
                        else if (nodeName.equals("triggerPresetNames"))
                        {
                            iNetAlarm.triggerPresetNames = iNode.getFirstChild().getNodeValue();
                        }
                        else if (nodeName.equals("alarmTime"))
                        {
                            iNetAlarm.alarmTime = iNode.getFirstChild().getNodeValue();
                            iNetAlarm.alarmTime = TimeOperation.utc2LocalTime(iNetAlarm.alarmTime);
                        }
                        else if (nodeName.equals("buzzerSwitch"))
                        {
                            iNetAlarm.buzzerSwitch = iNode.getFirstChild().getNodeValue().equals("true");
                        }
                        else if (nodeName.equals("buzzerSwitch"))
                        {
                            iNetAlarm.popVideoSwitch = iNode.getFirstChild().getNodeValue().equals("true");
                        }
                        else if (nodeName.equals("popMsgSwitch"))
                        {
                            iNetAlarm.popMsgSwitch = iNode.getFirstChild().getNodeValue().equals("true");
                        }
                        else if (nodeName.equals("emailSwitch"))
                        {
                            iNetAlarm.emailSwitch = iNode.getFirstChild().getNodeValue().equals("true");
                        }
                        else if (nodeName.equals("ftpRecSwitch"))
                        {
                            iNetAlarm.ftpRecSwitch = iNode.getFirstChild().getNodeValue().equals("true");
                        }
                        else if (nodeName.equals("snapSwitch"))
                        {
                            iNetAlarm.snapSwitch = iNode.getFirstChild().getNodeValue().equals("true");
                        }
                        else if (nodeName.equals("ftpSnapSwitch"))
                        {
                            iNetAlarm.ftpSnapSwitch = iNode.getFirstChild().getNodeValue().equals("true");
                        }
                        else if (nodeName.equals("abnormalType"))
                        {
                            iNetAlarm.abnormalType = iNode.getFirstChild().getNodeValue();
                        }
                        else if (nodeName.equals("triggerCondition"))
                        {
                            iNetAlarm.triggerCondition = iNode.getFirstChild().getNodeValue();
                        }
                        else if (nodeName.equals("intelligentType"))
                        {
                            iNetAlarm.intelligentType = iNode.getFirstChild().getNodeValue();
                        }
                    }
                    if (iNetAlarm.abnormalType.equals("diskFull"))
                    {
                        iNetAlarm.sourceChl.nodeID = new GUID();
                    }
                    if (iNetAlarm.sourceChl.nodeID == null)
                    {
                        continue;
                    }
                    if (iAlarmNode.getNodeName().equals("alarmIns"))
                    {
                        iStatus.alarmIn.add(iNetAlarm);
                    }
                    else if (iAlarmNode.getNodeName().equals("motions"))
                    {
                        iStatus.motion.add(iNetAlarm);
                    }
                    else if (iAlarmNode.getNodeName().equals("abnormals"))
                    {
                        iStatus.abnormals.add(iNetAlarm);
                    }
                    else if (iAlarmNode.getNodeName().equals("frontEndOffline"))
                    {
                        iNetAlarm.abnormalType = "frontEndOffline";
                        iStatus.abnormals.add(iNetAlarm);
                    }
                    else if (iAlarmNode.getNodeName().equals("videoLoss"))
                    {
                        iNetAlarm.abnormalType = "videoLoss";
                        iStatus.abnormals.add(iNetAlarm);
                    }
                    else if (iAlarmNode.getNodeName().equals("alarmOuts"))
                    {
                        iStatus.alarmOut.add(iNetAlarm);
                    }
                    else if (iAlarmNode.getNodeName().equals("signalShelter"))
                    {
                        iNetAlarm.abnormalType = "signalShelter";
                        iStatus.abnormals.add(iNetAlarm);
                    }
                    else if (iAlarmNode.getNodeName().equals("intelligents"))
                    {
                        iStatus.intelligent.add(iNetAlarm);
                    }
                }
            }

            return iStatus;
        }
    }

    public static class NET_NETWORK_INFO
    {
        public String IP = "";
        public String subMask = "";
        public String gateway = "";
        public String MAC = "";
        public String DHCP = "";
        public String nicStatus = "";
        public String ipV6 = "";
        public String gatewayV6 = "";
        public String subLengthV6;
        public String dhcpStatusV6 = "";
    }

    public static class NET_NETWORK_STATUS1
    {
        public ArrayList<NET_NETWORK_INFO> networkList = new ArrayList<NET_NETWORK_INFO>();
        public boolean bSwitch;
        public String POE;
        public int primaryNIC;

        public String DHCP = ""; // 是否是动态获取网络地址，0表示静态IP
        public String PPPoE = ""; // 是否开启了PPPoE
        public byte bDDNS; // 是否开启了DDNS, 0x1表示开启了DDNS功能但不一定该服务能用，
        // 0x11表示此时可以用域名访问DVR了
        public byte bWiFi; // 是否开启了无线网络
        public int httpPort; // HTTP服务器端口
        public int serverPort; // 业务服务器端口
        public int rtspPort;
        public String IP = ""; // 当前网络地址
        public String subMask = ""; // 子网掩码
        public String gateway = ""; // 网关
        public String DNS1 = ""; // 主DNS服务器
        public String DNS2 = ""; // 次DNS服务器
        public String netstatus = "";
        // 网络的状态。0表示没有连接到internet，1表示连接到了internet。
        // 为1的时候表示网口处于up状态并且插入了网线，其他情况下为0
        public String MAC = ""; // 设备物理地址
        public String ipV6 = "";
        public String gatewayV6 = "";
        public String subLengthV6 = "";
        public String dhcpStatusV6 = "";

        public String totalBandwidth = "";
        public String sendTotalBandwidth = "";
        public String remainBandwidth = "";
        public String sendRemainBandwidth = "";

        public String natStatus = "";
        public String internalStatus = "";
        public String internalIP = "";
        public String internalSubmask = "";

        public String pushServerStatus = "";
        public String pushServerIp = "";
        public int pushServerPort = 0;

        public static NET_NETWORK_STATUS1 deserialize(String strMessage)
        {
            NET_NETWORK_STATUS1 iNetStatus = new NET_NETWORK_STATUS1();

            NodeList nodeList = NVMSHeader.GetNodeList(strMessage, "content");
            if (nodeList == null)
            {
                return iNetStatus;
            }
            Node item = nodeList.item(0);
            if (item == null || !item.hasChildNodes())
            {
                return iNetStatus;
            }
            NodeList iContentList = item.getChildNodes();
            for (int i = 0; i < iContentList.getLength(); i++)
            {
                Node iItemNode = iContentList.item(i);
                String itemNode = iItemNode.getNodeName();
                if (iItemNode.getNodeType() == Node.ELEMENT_NODE)
                {
                    if (itemNode.equals("dns"))
                    {
                        if (!iItemNode.hasChildNodes())
                        {
                            continue;
                        }
                        NodeList iDnsNodes = iItemNode.getChildNodes();
                        for (int j = 0; j < iDnsNodes.getLength(); j++)
                        {
                            Node iNode = iDnsNodes.item(j);
                            if (iNode.getFirstChild() == null)
                            {
                                continue;
                            }
                            String nodeName = iNode.getNodeName();
                            if (nodeName.equals("dns1"))
                            {
                                iNetStatus.DNS1 = iNode.getFirstChild().getNodeValue();
                            }
                            else if (nodeName.equals("dns2"))
                            {
                                iNetStatus.DNS2 = iNode.getFirstChild().getNodeValue();
                            }
                        }
                    }
                    else if (itemNode.equals("ipGroup"))
                    {
                        if (!iItemNode.hasChildNodes())
                        {
                            continue;
                        }
                        NodeList iIpGroupNode = iItemNode.getChildNodes();
                        for (int m = 0; m < iIpGroupNode.getLength(); m++)
                        {
                            Node iNode = iIpGroupNode.item(m);
                            if (iNode.getFirstChild() == null)
                            {
                                continue;
                            }
                            String nodeName = iNode.getNodeName();
                            if (nodeName.equals("switch"))
                            {
                                iNetStatus.bSwitch = Boolean.valueOf(iNode.getFirstChild().getNodeValue());
                            }
                            else if (iNetStatus.bSwitch)
                            {
                                if (nodeName.equals("ip"))
                                {
                                    iNetStatus.IP = iNode.getFirstChild().getNodeValue();
                                }
                                else if (nodeName.equals("gateway"))
                                {
                                    iNetStatus.gateway = iNode.getFirstChild().getNodeValue();
                                }
                                else if (nodeName.equals("mask"))
                                {
                                    iNetStatus.subMask = iNode.getFirstChild().getNodeValue();
                                }
                                else if (nodeName.equals("primaryNIC"))
                                {
                                    iNetStatus.primaryNIC = Integer.parseInt(iNode.getFirstChild().getNodeValue().substring(3)) + 1;
                                }
                                else if (nodeName.equals("dhcpStatus"))
                                {
                                    iNetStatus.DHCP = iNode.getFirstChild().getNodeValue();
                                }
                                else if (nodeName.equals("ipV6"))
                                {
                                    iNetStatus.ipV6 = iNode.getFirstChild().getNodeValue();
                                }
                                else if (nodeName.equals("gatewayV6"))
                                {
                                    iNetStatus.gatewayV6 = iNode.getFirstChild().getNodeValue();
                                }
                                else if (nodeName.equals("subLengthV6"))
                                {
                                    iNetStatus.subLengthV6 = iNode.getFirstChild().getNodeValue();
                                }
                                else if (nodeName.equals("dhcpStatusV6"))
                                {
                                    iNetStatus.dhcpStatusV6 = iNode.getFirstChild().getNodeValue();
                                }
                            }

                        }
                    }
                    else if (itemNode.equals("nic"))
                    {
                        if (!iItemNode.hasChildNodes())
                        {
                            continue;
                        }
                        iNetStatus.POE = ((Element) iItemNode).getAttribute("poe");
                        NodeList iNicNode = iItemNode.getChildNodes();

                        for (int m = 0; m < iNicNode.getLength(); m++)
                        {
                            Node iNode = iNicNode.item(m);
                            if (iNode.getFirstChild() == null)
                            {
                                continue;
                            }
                            String nodeName = iNode.getNodeName();
                            if (nodeName.equals("item"))
                            {

                                if (((Element) iNode).getAttribute("id").equals("eth0"))
                                {
                                    NodeList itemList = iNode.getChildNodes();

                                    if (iNetStatus.POE != null && !iNetStatus.POE.equals(""))
                                    {
                                        for (int n = 0; n < itemList.getLength(); n++)
                                        {
                                            Node node = itemList.item(n);
                                            String iItemName = node.getNodeName();
                                            if (iItemName.equals("nicStatus"))
                                            {
                                                iNetStatus.netstatus = node.getFirstChild().getNodeValue();
                                            }
                                            else if (iItemName.equals("ip"))
                                            {
                                                iNetStatus.IP = node.getFirstChild().getNodeValue();
                                            }
                                            else if (iItemName.equals("gateway"))
                                            {
                                                iNetStatus.gateway = node.getFirstChild().getNodeValue();
                                            }
                                            else if (iItemName.equals("mask"))
                                            {
                                                iNetStatus.subMask = node.getFirstChild().getNodeValue();
                                            }
                                            else if (iItemName.equals("mac"))
                                            {
                                                iNetStatus.MAC = node.getFirstChild().getNodeValue();
                                            }
                                            else if (iItemName.equals("dhcpStatus"))
                                            {
                                                iNetStatus.DHCP = node.getFirstChild().getNodeValue();
                                            }
                                            else if (iItemName.equals("ipV6"))
                                            {
                                                iNetStatus.ipV6 = node.getFirstChild().getNodeValue();
                                            }
                                            else if (iItemName.equals("gatewayV6"))
                                            {
                                                iNetStatus.gatewayV6 = node.getFirstChild().getNodeValue();
                                            }
                                            else if (iItemName.equals("subLengthV6"))
                                            {
                                                iNetStatus.subLengthV6 = node.getFirstChild().getNodeValue();
                                            }
                                            else if (iItemName.equals("dhcpStatusV6"))
                                            {
                                                iNetStatus.dhcpStatusV6 = node.getFirstChild().getNodeValue();
                                            }
                                        }

                                    }
                                    else
                                    {
                                        if (!iNetStatus.bSwitch)
                                        {
                                            for (int n = 0; n < itemList.getLength(); n++)
                                            {
                                                Node node = itemList.item(n);
                                                String iItemName = node.getNodeName();
                                                if (iItemName.equals("nicStatus"))
                                                {
                                                    iNetStatus.netstatus = node.getFirstChild().getNodeValue();
                                                }
                                                else if (iItemName.equals("ip"))
                                                {
                                                    iNetStatus.IP = node.getFirstChild().getNodeValue();
                                                }
                                                else if (iItemName.equals("gateway"))
                                                {
                                                    iNetStatus.gateway = node.getFirstChild().getNodeValue();
                                                }
                                                else if (iItemName.equals("mask"))
                                                {
                                                    iNetStatus.subMask = node.getFirstChild().getNodeValue();
                                                }
                                                else if (iItemName.equals("mac"))
                                                {
                                                    iNetStatus.MAC = node.getFirstChild().getNodeValue();
                                                }
                                                else if (iItemName.equals("dhcpStatus"))
                                                {
                                                    iNetStatus.DHCP = node.getFirstChild().getNodeValue();
                                                }
                                                else if (iItemName.equals("ipV6"))
                                                {
                                                    iNetStatus.ipV6 = node.getFirstChild().getNodeValue();
                                                }
                                                else if (iItemName.equals("gatewayV6"))
                                                {
                                                    iNetStatus.gatewayV6 = node.getFirstChild().getNodeValue();
                                                }
                                                else if (iItemName.equals("subLengthV6"))
                                                {
                                                    iNetStatus.subLengthV6 = node.getFirstChild().getNodeValue();
                                                }
                                                else if (iItemName.equals("dhcpStatusV6"))
                                                {
                                                    iNetStatus.dhcpStatusV6 = node.getFirstChild().getNodeValue();
                                                }
                                            }
                                        }
                                        AddNetworkItem(iNetStatus, itemList);
                                    }

                                }
                                else if (((Element) iNode).getAttribute("id").equals("eth1"))
                                {
                                    NodeList itemList = iNode.getChildNodes();

                                    if (iNetStatus.POE != null && !iNetStatus.POE.equals(""))
                                    {
                                        for (int n = 0; n < itemList.getLength(); n++)
                                        {
                                            Node node = itemList.item(n);
                                            String iItemName = node.getNodeName();
                                            if (iItemName.equals("nicStatus"))
                                            {
                                                iNetStatus.internalStatus = node.getFirstChild().getNodeValue();
                                            }
                                            else if (iItemName.equals("ip"))
                                            {
                                                iNetStatus.internalIP = node.getFirstChild().getNodeValue();
                                            }

                                            else if (iItemName.equals("mask"))
                                            {
                                                iNetStatus.internalSubmask = node.getFirstChild().getNodeValue();
                                            }

                                        }

                                    }
                                    else
                                    {
                                        AddNetworkItem(iNetStatus, itemList);

                                    }

                                }
                            }
                        }
                    }
                    else if (itemNode.equals("pppoe"))
                    {
                        if (!iItemNode.hasChildNodes())
                        {
                            continue;
                        }
                        NodeList iDnsNodes = iItemNode.getChildNodes();
                        for (int j = 0; j < iDnsNodes.getLength(); j++)
                        {
                            Node iNode = iDnsNodes.item(j);
                            if (iNode.getFirstChild() == null)
                            {
                                continue;
                            }
                            String nodeName = iNode.getNodeName();
                            if (nodeName.equals("pppoeStatus"))
                            {
                                iNetStatus.PPPoE = iNode.getFirstChild().getNodeValue();
                            }
                        }
                    }
                    else if (itemNode.equals("bandwidth"))
                    {
                        if (!iItemNode.hasChildNodes())
                        {
                            continue;
                        }
                        NodeList iDnsNodes = iItemNode.getChildNodes();
                        for (int j = 0; j < iDnsNodes.getLength(); j++)
                        {
                            Node iNode = iDnsNodes.item(j);
                            if (iNode.getFirstChild() == null)
                            {
                                continue;
                            }
                            String nodeName = iNode.getNodeName();
                            if (nodeName.equals("totalBandwidth"))
                            {
                                Element iElement = (Element) iDnsNodes.item(j);
                                iNetStatus.totalBandwidth = iNode.getFirstChild().getNodeValue() + iElement.getAttribute("unit");
                            }
                            else if (nodeName.equals("sendTotalBandwidth"))
                            {
                                Element iElement = (Element) iDnsNodes.item(j);
                                iNetStatus.sendTotalBandwidth = iNode.getFirstChild().getNodeValue() + iElement.getAttribute("unit");
                            }
                            else if (nodeName.equals("remainBandwidth"))
                            {
                                Element iElement = (Element) iDnsNodes.item(j);
                                iNetStatus.remainBandwidth = iNode.getFirstChild().getNodeValue() + iElement.getAttribute("unit");
                            }
                            else if (nodeName.equals("sendRemainBandwidth"))
                            {
                                Element iElement = (Element) iDnsNodes.item(j);
                                iNetStatus.sendRemainBandwidth = iNode.getFirstChild().getNodeValue() + iElement.getAttribute("unit");
                            }
                        }
                    }
                    else if (itemNode.equals("httpPort"))
                    {
                        if (iItemNode.getFirstChild() != null)
                        {
                            iNetStatus.httpPort = Integer.parseInt(iItemNode.getFirstChild().getNodeValue());
                        }
                    }
                    else if (itemNode.equals("netPort"))
                    {
                        if (iItemNode.getFirstChild() != null)
                        {
                            iNetStatus.serverPort = Integer.parseInt(iItemNode.getFirstChild().getNodeValue());
                        }
                    }
                    else if (itemNode.equals("rtspPort"))
                    {
                        if (iItemNode.getFirstChild() != null)
                        {
                            iNetStatus.rtspPort = Integer.parseInt(iItemNode.getFirstChild().getNodeValue());
                        }
                    }
                    else if (itemNode.equals("natStatus"))
                    {
                        iNetStatus.natStatus = iItemNode.getFirstChild().getNodeValue();
                    }
                    else if (itemNode.equals("pushServer"))
                    {
                        if (!iItemNode.hasChildNodes())
                        {
                            continue;
                        }
                        NodeList iDnsNodes = iItemNode.getChildNodes();
                        for (int j = 0; j < iDnsNodes.getLength(); j++)
                        {
                            Node iNode = iDnsNodes.item(j);
                            if (iNode.getFirstChild() == null)
                            {
                                continue;
                            }
                            String nodeName = iNode.getNodeName();
                            if (nodeName.equals("pushServerStatus"))
                            {
                                iNetStatus.pushServerStatus = iNode.getFirstChild().getNodeValue();
                            }
                            else if (nodeName.equals("ip"))
                            {
                                iNetStatus.pushServerIp = iNode.getFirstChild().getNodeValue();
                            }
                            else if (nodeName.equals("port"))
                            {
                                iNetStatus.pushServerPort = Integer.parseInt(iNode.getFirstChild().getNodeValue());
                            }
                        }
                    }
                }

            }

            return iNetStatus;
        }

        private static void AddNetworkItem(NET_NETWORK_STATUS1 iNetStatus, NodeList itemList)
        {
            NET_NETWORK_INFO networkItem = new NET_NETWORK_INFO();
            for (int n = 0; n < itemList.getLength(); n++)
            {
                Node node = itemList.item(n);
                String iItemName = node.getNodeName();
                if (iItemName.equals("nicStatus"))
                {
                    networkItem.nicStatus = node.getFirstChild().getNodeValue();
                }
                else if (iItemName.equals("ip"))
                {
                    networkItem.IP = node.getFirstChild().getNodeValue();
                }
                else if (iItemName.equals("gateway"))
                {
                    networkItem.gateway = node.getFirstChild().getNodeValue();
                }
                else if (iItemName.equals("mask"))
                {
                    networkItem.subMask = node.getFirstChild().getNodeValue();
                }
                else if (iItemName.equals("mac"))
                {
                    networkItem.MAC = node.getFirstChild().getNodeValue();
                }
                else if (iItemName.equals("dhcpStatus"))
                {
                    networkItem.DHCP = node.getFirstChild().getNodeValue();
                }
                else if (iItemName.equals("ipV6"))
                {
                    networkItem.ipV6 = node.getFirstChild().getNodeValue();
                }
                else if (iItemName.equals("gatewayV6"))
                {
                    networkItem.gatewayV6 = node.getFirstChild().getNodeValue();
                }
                else if (iItemName.equals("subLengthV6"))
                {
                    networkItem.subLengthV6 = node.getFirstChild().getNodeValue();
                }
                else if (iItemName.equals("dhcpStatusV6"))
                {
                    networkItem.dhcpStatusV6 = node.getFirstChild().getNodeValue();
                }
            }

            iNetStatus.networkList.add(networkItem);
        }
    }

    public static class NET_NETWORK_STATUS
    {
        public String DHCP = ""; // 是否是动态获取网络地址，0表示静态IP
        public String PPPoE = ""; // 是否开启了PPPoE
        public byte bDDNS; // 是否开启了DDNS, 0x1表示开启了DDNS功能但不一定该服务能用，
        // 0x11表示此时可以用域名访问DVR了
        public byte bWiFi; // 是否开启了无线网络
        public int httpPort; // HTTP服务器端口
        public int serverPort; // 业务服务器端口
        public int rtspPort;
        public String IP = ""; // 当前网络地址
        public String subMask = ""; // 子网掩码
        public String gateway = ""; // 网关
        public String DNS1 = ""; // 主DNS服务器
        public String DNS2 = ""; // 次DNS服务器
        public String netstatus = "";
        // 网络的状态。0表示没有连接到internet，1表示连接到了internet。
        // 为1的时候表示网口处于up状态并且插入了网线，其他情况下为0
        public String MAC = ""; // 设备物理地址

        public String totalBandwidth = "";
        public String sendTotalBandwidth = "";
        public String remainBandwidth = "";
        public String sendRemainBandwidth = "";

        public String natStatus = "";
        public String internalStatus = "";
        public String internalIP = "";
        public String internalSubmask = "";

        public static NET_NETWORK_STATUS deserialize(String strMessage)
        {
            NET_NETWORK_STATUS iNetStatus = new NET_NETWORK_STATUS();

            NodeList nodeList = NVMSHeader.GetNodeList(strMessage, "content");
            if (nodeList == null)
            {
                return iNetStatus;
            }
            Node item = nodeList.item(0);
            if (item == null || !item.hasChildNodes())
            {
                return iNetStatus;
            }
            NodeList iContentList = item.getChildNodes();
            for (int i = 0; i < iContentList.getLength(); i++)
            {
                Node iItemNode = iContentList.item(i);
                String itemNode = iItemNode.getNodeName();
                if (itemNode.equals("dns"))
                {
                    if (!iItemNode.hasChildNodes())
                    {
                        continue;
                    }
                    NodeList iDnsNodes = iItemNode.getChildNodes();
                    for (int j = 0; j < iDnsNodes.getLength(); j++)
                    {
                        Node iNode = iDnsNodes.item(j);
                        if (iNode.getFirstChild() == null)
                        {
                            continue;
                        }
                        String nodeName = iNode.getNodeName();
                        if (nodeName.equals("dns1"))
                        {
                            iNetStatus.DNS1 = iNode.getFirstChild().getNodeValue();
                        }
                        else if (nodeName.equals("dns2"))
                        {
                            iNetStatus.DNS2 = iNode.getFirstChild().getNodeValue();
                        }
                    }
                }
                else if (itemNode.equals("nic"))
                {
                    if (!iItemNode.hasChildNodes())
                    {
                        continue;
                    }
                    NodeList iNicNodes = iItemNode.getChildNodes();
                    for (int j = 0; j < iNicNodes.getLength(); j++)
                    {
                        Node iNode = iNicNodes.item(j);
                        String iNodeName = iNode.getNodeName();
                        if (!iNodeName.equals("item"))
                        {
                            continue;
                        }
                        Element iElement = (Element) iNicNodes.item(j);
                        String id = iElement.getAttribute("id");
                        if (!id.equals("eth0") && !id.equals("eth1"))
                        {
                            continue;
                        }
                        if (!iNode.hasChildNodes())
                        {
                            continue;
                        }
                        NodeList iNodeList = iNode.getChildNodes();
                        for (int k = 0; k < iNodeList.getLength(); k++)
                        {
                            Node iNicNode = iNodeList.item(k);
                            if (iNicNode.getFirstChild() == null)
                            {
                                continue;
                            }
                            String nodeName = iNicNode.getNodeName();
                            if (nodeName.equals("nicStatus"))
                            {
                                if (id.equals("eth0"))
                                {
                                    iNetStatus.netstatus = iNicNode.getFirstChild().getNodeValue();
                                }
                                else if (id.equals("eth1"))
                                {
                                    iNetStatus.internalStatus = iNicNode.getFirstChild().getNodeValue();
                                }
                            }
                            else if (nodeName.equals("ip"))
                            {
                                if (id.equals("eth0"))
                                {
                                    iNetStatus.IP = iNicNode.getFirstChild().getNodeValue();
                                }
                                else if (id.equals("eth1"))
                                {
                                    iNetStatus.internalIP = iNicNode.getFirstChild().getNodeValue();
                                }
                            }
                            else if (nodeName.equals("gateway"))
                            {
                                if (id.equals("eth0"))
                                {
                                    iNetStatus.gateway = iNicNode.getFirstChild().getNodeValue();
                                }
                            }
                            else if (nodeName.equals("mask"))
                            {
                                if (id.equals("eth0"))
                                {
                                    iNetStatus.subMask = iNicNode.getFirstChild().getNodeValue();
                                }
                                else if (id.equals("eth1"))
                                {
                                    iNetStatus.internalSubmask = iNicNode.getFirstChild().getNodeValue();
                                }
                            }
                            else if (nodeName.equals("mac"))
                            {
                                if (id.equals("eth0"))
                                {
                                    iNetStatus.MAC = iNicNode.getFirstChild().getNodeValue();
                                }
                            }
                            else if (nodeName.equals("dhcpStatus"))
                            {
                                if (id.equals("eth0"))
                                {
                                    iNetStatus.DHCP = iNicNode.getFirstChild().getNodeValue();
                                }
                            }
                        }
                    }
                }
                else if (itemNode.equals("pppoe"))
                {
                    if (!iItemNode.hasChildNodes())
                    {
                        continue;
                    }
                    NodeList iDnsNodes = iItemNode.getChildNodes();
                    for (int j = 0; j < iDnsNodes.getLength(); j++)
                    {
                        Node iNode = iDnsNodes.item(j);
                        if (iNode.getFirstChild() == null)
                        {
                            continue;
                        }
                        String nodeName = iNode.getNodeName();
                        if (nodeName.equals("pppoeStatus"))
                        {
                            iNetStatus.PPPoE = iNode.getFirstChild().getNodeValue();
                        }
                    }
                }
                else if (itemNode.equals("bandwidth"))
                {
                    if (!iItemNode.hasChildNodes())
                    {
                        continue;
                    }
                    NodeList iDnsNodes = iItemNode.getChildNodes();
                    for (int j = 0; j < iDnsNodes.getLength(); j++)
                    {
                        Node iNode = iDnsNodes.item(j);
                        if (iNode.getFirstChild() == null)
                        {
                            continue;
                        }
                        String nodeName = iNode.getNodeName();
                        if (nodeName.equals("totalBandwidth"))
                        {
                            Element iElement = (Element) iDnsNodes.item(j);
                            iNetStatus.totalBandwidth = iNode.getFirstChild().getNodeValue() + iElement.getAttribute("unit");
                        }
                        else if (nodeName.equals("sendTotalBandwidth"))
                        {
                            Element iElement = (Element) iDnsNodes.item(j);
                            iNetStatus.sendTotalBandwidth = iNode.getFirstChild().getNodeValue() + iElement.getAttribute("unit");
                        }
                        else if (nodeName.equals("remainBandwidth"))
                        {
                            Element iElement = (Element) iDnsNodes.item(j);
                            iNetStatus.remainBandwidth = iNode.getFirstChild().getNodeValue() + iElement.getAttribute("unit");
                        }
                        else if (nodeName.equals("sendRemainBandwidth"))
                        {
                            Element iElement = (Element) iDnsNodes.item(j);
                            iNetStatus.sendRemainBandwidth = iNode.getFirstChild().getNodeValue() + iElement.getAttribute("unit");
                        }
                    }
                }
                else if (itemNode.equals("httpPort"))
                {
                    if (iItemNode.getFirstChild() != null)
                    {
                        iNetStatus.httpPort = Integer.parseInt(iItemNode.getFirstChild().getNodeValue());
                    }
                }
                else if (itemNode.equals("netPort"))
                {
                    if (iItemNode.getFirstChild() != null)
                    {
                        iNetStatus.serverPort = Integer.parseInt(iItemNode.getFirstChild().getNodeValue());
                    }
                }
                else if (itemNode.equals("rtspPort"))
                {
                    if (iItemNode.getFirstChild() != null)
                    {
                        iNetStatus.rtspPort = Integer.parseInt(iItemNode.getFirstChild().getNodeValue());
                    }
                }
                else if (itemNode.equals("natStatus"))
                {
                    iNetStatus.natStatus = iItemNode.getFirstChild().getNodeValue();
                }
            }

            return iNetStatus;
        }
    }

    public static class NET_DISK_DETAIL_INFO
    {
        public GUID nodeID = new GUID();
        public int index;
        public String name = "";
        public String status = "";
        public int size;
        public int freeSpace;
        public String source = "";
        public String recStartDate = "";
        public String recEndDate = "";
        public int count = 0;

        public static ArrayList<NET_DISK_DETAIL_INFO> deserialize(String strMessage)
        {
            ArrayList<NET_DISK_DETAIL_INFO> iDiskList = new ArrayList<NET_DISK_DETAIL_INFO>();

            NodeList iNodeList = NVMSHeader.GetNodeList(strMessage, "content");
            if (iNodeList == null)
            {
                return iDiskList;
            }
            Node iDiskNode = iNodeList.item(0);
            if (!iDiskNode.hasChildNodes())
            {
                return iDiskList;
            }
            NodeList iDiskNodes = iDiskNode.getChildNodes();
            for (int i = 0; i < iDiskNodes.getLength(); i++)
            {
                Node iNode = iDiskNodes.item(i);
                if (iNode.getFirstChild() == null)
                {
                    continue;
                }
                String nodeName = iNode.getNodeName();
                if (nodeName.equals("item"))
                {
                    NET_DISK_DETAIL_INFO info = new NET_DISK_DETAIL_INFO();
                    Element iElement = (Element) iDiskNodes.item(i);

                    info.nodeID = GUID.GetGUID(iElement.getAttribute("id").replace("{", "").replace("}", ""));
                    info.status = iNode.getFirstChild().getNodeValue();

                    iDiskList.add(info);
                }
            }

            return iDiskList;
        }

        public static ArrayList<NET_DISK_DETAIL_INFO> ParseDiskList(String strMessage)
        {
            ArrayList<NET_DISK_DETAIL_INFO> iDiskList = new ArrayList<NET_DISK_DETAIL_INFO>();

            NodeList iNodeList = NVMSHeader.GetNodeList(strMessage, "diskList");
            if (iNodeList == null)
            {
                return iDiskList;
            }
            String unit = "";
            String freeSpaceUnit = "";
            for (int i = 0; i < iNodeList.getLength(); i++)
            {
                Node iDiskNode = iNodeList.item(i);
                if (!iDiskNode.hasChildNodes())
                {
                    continue;
                }
                NodeList iDiskNodes = iDiskNode.getChildNodes();
                for (int k = 0; k < iDiskNodes.getLength(); k++)
                {
                    Node iNode = iDiskNodes.item(k);
                    String nodeName = iNode.getNodeName();
                    if (nodeName.equals("itemType"))
                    {
                        NodeList typeList = iNode.getChildNodes();
                        for (int j = 0; j < typeList.getLength(); j++)
                        {
                            Node typeNode = typeList.item(j);
                            String typeName = typeNode.getNodeName();
                            if (typeName.equals("size"))
                            {
                                Element iElement = (Element) typeList.item(j);
                                unit = iElement.getAttribute("unit");
                            }
                            else if (typeName.equals("freeSpace"))
                            {
                                Element element = (Element) typeList.item(j);
                                freeSpaceUnit = element.getAttribute("unit");
                            }
                        }
                    }
                    else if (nodeName.equals("item"))
                    {
                        NET_DISK_DETAIL_INFO iDiskInfo = new NET_DISK_DETAIL_INFO();

                        Element iElement = (Element) iDiskNodes.item(k);
                        iDiskInfo.nodeID = GUID.GetGUID(iElement.getAttribute("id").replace("{", "").replace("}", ""));
                        NodeList typeList = iNode.getChildNodes();
                        for (int j = 0; j < typeList.getLength(); j++)
                        {
                            Node typeNode = typeList.item(j);
                            if (typeNode.getFirstChild() == null)
                            {
                                continue;
                            }
                            String typeName = typeNode.getNodeName();
                            if (typeName.equals("size"))
                            {
                                int size = Integer.parseInt(typeNode.getFirstChild().getNodeValue());
                                if (unit.toUpperCase().equals("GB"))
                                {
                                    iDiskInfo.size = size;
                                }
                                else if (unit.toUpperCase().equals("MB"))
                                {
                                    iDiskInfo.size = size / 1024;
                                }
                                else if (unit.toUpperCase().equals("KB"))
                                {
                                    iDiskInfo.size = size / 1024 / 1024;
                                }
                            }
                            else if (typeName.equals("slotIndex"))
                            {
                                iDiskInfo.name = typeNode.getFirstChild().getNodeValue();
                            }
                            else if (typeName.equals("freeSpace"))
                            {
                                int size = Integer.parseInt(typeNode.getFirstChild().getNodeValue());
                                if (freeSpaceUnit.equals(""))
                                {
                                    freeSpaceUnit = unit;
                                }
                                if (freeSpaceUnit.toUpperCase().equals("GB"))
                                {
                                    iDiskInfo.freeSpace = size;
                                }
                                else if (freeSpaceUnit.toUpperCase().equals("MB"))
                                {
                                    iDiskInfo.freeSpace = size / 1024;
                                }
                                else if (freeSpaceUnit.toUpperCase().equals("KB"))
                                {
                                    iDiskInfo.freeSpace = size / 1024 / 1024;
                                }
                            }
                            else if (typeName.equals("recStartDate"))
                            {
                                iDiskInfo.recStartDate = typeNode.getFirstChild().getNodeValue();
                            }
                            else if (typeName.equals("recEndDate"))
                            {
                                iDiskInfo.recEndDate = typeNode.getFirstChild().getNodeValue();
                            }
                        }
                        iDiskList.add(iDiskInfo);
                    }
                }
            }

            return iDiskList;
        }

        public static NET_DISK_DETAIL_INFO ParseDiskDetailInfo(String strMessage)
        {
            NET_DISK_DETAIL_INFO iDiskInfo = new NET_DISK_DETAIL_INFO();

            NodeList iNodeList = NVMSHeader.GetNodeList(strMessage, "content");
            if (iNodeList == null)
            {
                return iDiskInfo;
            }
            Node iContentNode = iNodeList.item(0);
            if (!iContentNode.hasChildNodes())
            {
                return iDiskInfo;
            }
            iNodeList = iContentNode.getChildNodes();
            for (int i = 0; i < iNodeList.getLength(); i++)
            {
                Node iNode = iNodeList.item(i);
                if (iNode.getFirstChild() == null)
                {
                    continue;
                }
                String nodeName = iNode.getNodeName();
                if (nodeName.equals("id"))
                {
                    iDiskInfo.nodeID = GUID.GetGUID(iNode.getFirstChild().getNodeValue().replace("{", "").replace("}", ""));
                }
                else if (nodeName.equals("source"))
                {
                    iDiskInfo.source = iNode.getFirstChild().getNodeValue();
                }
            }

            return iDiskInfo;
        }

    }

    public static class NET_AZ_INFO
    {
        public String nodeID = null;
        public String m_strFocusType = null;
        public String m_strTimeInterval = null;
        public int m_iIrchangeFocusSwitch = -1;
        public String m_strTimeIntervalList = null;
        public ArrayList<String> m_strFocusTypeList = null;

        public static NET_AZ_INFO deserialize(String strMessage)
        {
            //        Log.i("ipccamera", "strMessage:\n" + strMessage);
            NET_AZ_INFO iAZInfo = new NET_AZ_INFO();
            NodeList iTypeNodeList = NVMSHeader.GetNodeList(strMessage, "types");
            Node iTypeNode = iTypeNodeList.item(0);

            if (iTypeNode != null && iTypeNode.hasChildNodes())
            {
                iTypeNodeList = iTypeNode.getChildNodes();

                for (int i = 0; i < iTypeNodeList.getLength(); i++)
                {
                    Node iNode = iTypeNodeList.item(i);
                    if (iNode.getFirstChild() == null)
                    {
                        continue;
                    }
                    String nodeName = iNode.getNodeName();
                    if (nodeName.equals("focusType"))
                    {
                        if (iAZInfo.m_strFocusTypeList == null)
                        {
                            iAZInfo.m_strFocusTypeList = new ArrayList<String>();
                        }
                        NodeList iChildNodeList = iNode.getChildNodes();
                        for (int j = 0; j < iChildNodeList.getLength(); j++)
                        {
                            iNode = iChildNodeList.item(j);
                            if (iNode.getFirstChild() == null)
                            {
                                continue;
                            }
                            nodeName = iNode.getNodeName();
                            if (nodeName.equals("enum"))
                            {
                                iAZInfo.m_strFocusTypeList.add(iNode.getFirstChild().getNodeValue().trim());
                            }
                        }
                    }
                }
            }

            NodeList iNodeList = NVMSHeader.GetNodeList(strMessage, "content");
            if (iNodeList == null)
            {
                return iAZInfo;
            }
            Node iContentNode = iNodeList.item(0);
            if (iContentNode == null || !iContentNode.hasChildNodes())
            {
                return iAZInfo;
            }
            iNodeList = iContentNode.getChildNodes();
            for (int i = 0; i < iNodeList.getLength(); i++)
            {
                iContentNode = iNodeList.item(i);
                String iNodeName = iContentNode.getNodeName();
                if (iNodeName.equals("chl"))
                {
                    Element iElement = (Element) iNodeList.item(i);
                    iAZInfo.nodeID = iElement.getAttribute("id").replace("{", "").replace("}", "");
                    break;
                }
            }

            iNodeList = iContentNode.getChildNodes();
            for (int i = 0; i < iNodeList.getLength(); i++)
            {
                Node iNode = iNodeList.item(i);
                if (iNode.getFirstChild() == null)
                {
                    continue;
                }
                String nodeName = iNode.getNodeName();
                if (nodeName.equals("focusType"))
                {
                    iAZInfo.m_strFocusType = iNode.getFirstChild().getNodeValue();

                }
                else if (nodeName.equals("timeInterval"))
                {
                    iAZInfo.m_strTimeInterval = iNode.getFirstChild().getNodeValue();

                }
                else if (nodeName.equals("timeIntervalNote"))
                {
                    iAZInfo.m_strTimeIntervalList = iNode.getFirstChild().getNodeValue();
                }
                else if (nodeName.equals("IrchangeFocus"))
                {
                    iAZInfo.m_iIrchangeFocusSwitch = iNode.getFirstChild().getNodeValue().equals("true") ? 1 : 0;

                }
            }

            return iAZInfo;
        }
    }


    public static class NET_COLOR_INFO
    {
        public String nodeID = null;

        public int bright = -1;
        public int contrast = -1;
        public int hue = -1;
        public int saturation = -1;

        public int defBright = -1;
        public int defContrast = -1;
        public int defHue = -1;
        public int defSaturation = -1;

        public int minBright = -1;
        public int minContrast = -1;
        public int minHue = -1;
        public int minSaturation = -1;

        public int maxBright = -1;
        public int maxContrast = -1;
        public int maxHue = -1;
        public int maxSaturation = -1;

        public String m_strDefEQ = "";//
        public String m_strEQ = "";

        public String m_strDefFrequency = "";// 频率
        public String m_strFrequency = "";

        public int m_iDefMirrorSwitch = -1;// 图像镜像
        public int m_iMirrorSwitch = -1;

        public int m_iDefFlipSwitch = -1;// 图像翻转
        public int m_iFlipSwitch = -1;

        public int m_iDefImageShift = -1;// 图像偏移
        public int m_iMaxImageShift = -1;
        public int m_iMinImageShift = -1;
        public int m_iImageShift = -1;

        public int m_iDefWDRSwitch = -1;// 宽动态
        public int m_iWDRSwitch = -1;
        public int m_iDefWDRValue = -1;
        public int m_iMaxWDRValue = -1;
        public int m_iMinWDRValue = -1;
        public int m_iWDRValue = -1;

        public String m_strDefWhiteBalanceMode = "";// 白平衡
        public String m_strWhiteBalanceMode = "";

        public int m_iDefRedGain = -1;// 红色增益
        public int m_iMaxRedGain = -1;
        public int m_iMinRedGain = -1;
        public int m_iRedGain = -1;

        public int m_iDefBlueGain = -1;// 蓝色增益
        public int m_iMaxBlueGain = -1;
        public int m_iMinBlueGain = -1;
        public int m_iBlueGain = -1;

        public int m_iDefDenoiseSwitch = -1;// 降噪
        public int m_iDenoiseSwitch = -1;
        public int m_iDefDenoiseValue = -1;
        public int m_iMaxDenoiseValue = -1;
        public int m_iMinDenoiseValue = -1;
        public int m_iDenoiseValue = -1;

        public int m_iDefSharpenSwitch = -1;// 锐化度
        public int m_iSharpenSwitch = -1;
        public int m_iDefSharpenValue = -1;
        public int m_iMaxSharpenValue = -1;
        public int m_iMinSharpenValue = -1;
        public int m_iSharpenValue = -1;

        public String m_strDefIRCutMode = "";// 日夜模式
        public String m_strIRCutMode = "";
        public String m_strIRCutDayTime = "";// 白天时间
        public String m_strIRCutNightTime = "";// 夜晚时间
        public String m_strDefIRCutConvSen = "";// 灵敏度
        public String m_strIRCutConvSen = "";

        public String m_strDefBLCMode = "";//背光补偿
        public String m_strBLCMode = "";
        public String m_strDefHWDRLevel = "";
        public String m_strHWDRLevel = "";

        public ArrayList<String> m_strFrequencyList = null;
        public ArrayList<String> m_strWhiteBalanceList = null;
        public ArrayList<String> m_strIRCutModeList = null;
        public ArrayList<String> m_strIRCutConvSenList = null;
        public ArrayList<String> m_strEQList = null;
        public ArrayList<String> m_strBLCModeList = null;
        public ArrayList<String> m_strHWDRLevelList = null;

        public static NET_COLOR_INFO deserialize(String strMessage)
        {
            //        Log.i("ipccamera", "strMessage:\n" + strMessage);
            NET_COLOR_INFO iColorInfo = new NET_COLOR_INFO();
            NodeList iTypeNodeList = NVMSHeader.GetNodeList(strMessage, "types");
            Node iTypeNode = iTypeNodeList.item(0);

            if (iTypeNode != null && iTypeNode.hasChildNodes())
            {
                iTypeNodeList = iTypeNode.getChildNodes();

                for (int i = 0; i < iTypeNodeList.getLength(); i++)
                {
                    Node iNode = iTypeNodeList.item(i);
                    if (iNode.getFirstChild() == null)
                    {
                        continue;
                    }
                    String nodeName = iNode.getNodeName();
                    if (nodeName.equals("frequency"))
                    {
                        if (iColorInfo.m_strFrequencyList == null)
                        {
                            iColorInfo.m_strFrequencyList = new ArrayList<String>();
                        }
                        NodeList iChildNodeList = iNode.getChildNodes();
                        for (int j = 0; j < iChildNodeList.getLength(); j++)
                        {
                            iNode = iChildNodeList.item(j);
                            if (iNode.getFirstChild() == null)
                            {
                                continue;
                            }
                            nodeName = iNode.getNodeName();
                            if (nodeName.equals("enum"))
                            {
                                iColorInfo.m_strFrequencyList.add(iNode.getFirstChild().getNodeValue().trim());
                            }
                        }
                    }
                    else if (nodeName.equals("whiteBalance"))
                    {
                        if (iColorInfo.m_strWhiteBalanceList == null)
                        {
                            iColorInfo.m_strWhiteBalanceList = new ArrayList<String>();
                        }
                        NodeList iChildNodeList = iNode.getChildNodes();
                        for (int j = 0; j < iChildNodeList.getLength(); j++)
                        {
                            iNode = iChildNodeList.item(j);
                            if (iNode.getFirstChild() == null)
                            {
                                continue;
                            }
                            nodeName = iNode.getNodeName();
                            if (nodeName.equals("enum"))
                            {
                                iColorInfo.m_strWhiteBalanceList.add(iNode.getFirstChild().getNodeValue().trim());
                            }
                        }
                    }
                    else if (nodeName.equals("IRCutMode"))
                    {
                        if (iColorInfo.m_strIRCutModeList == null)
                        {
                            iColorInfo.m_strIRCutModeList = new ArrayList<String>();
                        }
                        NodeList iChildNodeList = iNode.getChildNodes();
                        for (int j = 0; j < iChildNodeList.getLength(); j++)
                        {
                            iNode = iChildNodeList.item(j);
                            if (iNode.getFirstChild() == null)
                            {
                                continue;
                            }
                            nodeName = iNode.getNodeName();
                            if (nodeName.equals("enum"))
                            {
                                iColorInfo.m_strIRCutModeList.add(iNode.getFirstChild().getNodeValue().trim());
                            }
                        }
                    }
                    else if (nodeName.equals("IRCutConvSen"))
                    {
                        if (iColorInfo.m_strIRCutConvSenList == null)
                        {
                            iColorInfo.m_strIRCutConvSenList = new ArrayList<String>();
                        }
                        NodeList iChildNodeList = iNode.getChildNodes();
                        for (int j = 0; j < iChildNodeList.getLength(); j++)
                        {
                            iNode = iChildNodeList.item(j);
                            if (iNode.getFirstChild() == null)
                            {
                                continue;
                            }
                            nodeName = iNode.getNodeName();
                            if (nodeName.equals("enum"))
                            {
                                iColorInfo.m_strIRCutConvSenList.add(iNode.getFirstChild().getNodeValue().trim());
                            }
                        }
                    }
                    else if (nodeName.equals("EQ"))
                    {
                        if (iColorInfo.m_strEQList == null)
                        {
                            iColorInfo.m_strEQList = new ArrayList<String>();
                        }
                        NodeList iChildNodeList = iNode.getChildNodes();
                        for (int j = 0; j < iChildNodeList.getLength(); j++)
                        {
                            iNode = iChildNodeList.item(j);
                            if (iNode.getFirstChild() == null)
                            {
                                continue;
                            }
                            nodeName = iNode.getNodeName();
                            if (nodeName.equals("enum"))
                            {
                                iColorInfo.m_strEQList.add(iNode.getFirstChild().getNodeValue().trim());
                            }
                        }
                    }
                    else if (nodeName.equals("BLCMode"))
                    {
                        if (iColorInfo.m_strBLCModeList == null)
                        {
                            iColorInfo.m_strBLCModeList = new ArrayList<String>();
                        }
                        NodeList iChildNodeList = iNode.getChildNodes();
                        for (int j = 0; j < iChildNodeList.getLength(); j++)
                        {
                            iNode = iChildNodeList.item(j);
                            if (iNode.getFirstChild() == null)
                            {
                                continue;
                            }
                            nodeName = iNode.getNodeName();
                            if (nodeName.equals("enum"))
                            {
                                iColorInfo.m_strBLCModeList.add(iNode.getFirstChild().getNodeValue().trim());
                            }
                        }
                    }
                    else if (nodeName.equals("HWDRLevel"))
                    {
                        if (iColorInfo.m_strHWDRLevelList == null)
                        {
                            iColorInfo.m_strHWDRLevelList = new ArrayList<String>();
                        }
                        NodeList iChildNodeList = iNode.getChildNodes();
                        for (int j = 0; j < iChildNodeList.getLength(); j++)
                        {
                            iNode = iChildNodeList.item(j);
                            if (iNode.getFirstChild() == null)
                            {
                                continue;
                            }
                            nodeName = iNode.getNodeName();
                            if (nodeName.equals("enum"))
                            {
                                iColorInfo.m_strHWDRLevelList.add(iNode.getFirstChild().getNodeValue().trim());
                            }
                        }
                    }
                }
            }

            NodeList iNodeList = NVMSHeader.GetNodeList(strMessage, "content");
            if (iNodeList == null)
            {
                return iColorInfo;
            }
            Node iContentNode = iNodeList.item(0);
            if (!iContentNode.hasChildNodes())
            {
                return iColorInfo;
            }
            iNodeList = iContentNode.getChildNodes();
            for (int i = 0; i < iNodeList.getLength(); i++)
            {
                iContentNode = iNodeList.item(i);
                String iNodeName = iContentNode.getNodeName();
                if (iNodeName.equals("chl"))
                {
                    Element iElement = (Element) iNodeList.item(i);
                    iColorInfo.nodeID = iElement.getAttribute("id").replace("{", "").replace("}", "");
                    break;
                }
            }

            iNodeList = iContentNode.getChildNodes();
            for (int i = 0; i < iNodeList.getLength(); i++)
            {
                Node iNode = iNodeList.item(i);
                if (iNode.getFirstChild() == null)
                {
                    continue;
                }
                String nodeName = iNode.getNodeName();
                if (nodeName.equals("bright"))
                {
                    Element iElement = (Element) iNodeList.item(i);
                    iColorInfo.bright = Integer.parseInt(iNode.getFirstChild().getNodeValue());
                    iColorInfo.minBright = Integer.parseInt(iElement.getAttribute("min"));
                    iColorInfo.maxBright = Integer.parseInt(iElement.getAttribute("max"));
                    iColorInfo.defBright = Integer.parseInt(iElement.getAttribute("default"));
                }
                else if (nodeName.equals("contrast"))
                {
                    Element iElement = (Element) iNodeList.item(i);
                    iColorInfo.contrast = Integer.parseInt(iNode.getFirstChild().getNodeValue());
                    iColorInfo.minContrast = Integer.parseInt(iElement.getAttribute("min"));
                    iColorInfo.maxContrast = Integer.parseInt(iElement.getAttribute("max"));
                    iColorInfo.defContrast = Integer.parseInt(iElement.getAttribute("default"));
                }
                else if (nodeName.equals("hue"))
                {
                    Element iElement = (Element) iNodeList.item(i);
                    iColorInfo.hue = Integer.parseInt(iNode.getFirstChild().getNodeValue());
                    iColorInfo.minHue = Integer.parseInt(iElement.getAttribute("min"));
                    iColorInfo.maxHue = Integer.parseInt(iElement.getAttribute("max"));
                    iColorInfo.defHue = Integer.parseInt(iElement.getAttribute("default"));
                }
                else if (nodeName.equals("saturation"))
                {
                    Element iElement = (Element) iNodeList.item(i);
                    iColorInfo.saturation = Integer.parseInt(iNode.getFirstChild().getNodeValue());
                    iColorInfo.minSaturation = Integer.parseInt(iElement.getAttribute("min"));
                    iColorInfo.maxSaturation = Integer.parseInt(iElement.getAttribute("max"));
                    iColorInfo.defSaturation = Integer.parseInt(iElement.getAttribute("default"));
                }
                else if (nodeName.equals("EQ"))
                {
                    Element iElement = (Element) iNodeList.item(i);
                    iColorInfo.m_strEQ = iNode.getFirstChild().getNodeValue();
                    iColorInfo.m_strDefEQ = iElement.getAttribute("default");
                }
                else if (nodeName.equals("frequency"))
                {
                    Element iElement = (Element) iNodeList.item(i);
                    iColorInfo.m_strFrequency = iNode.getFirstChild().getNodeValue();
                    iColorInfo.m_strDefFrequency = iElement.getAttribute("default");
                }
                else if (nodeName.equals("mirrorSwitch"))
                {
                    Element iElement = (Element) iNodeList.item(i);
                    iColorInfo.m_iMirrorSwitch = iNode.getFirstChild().getNodeValue().equals("true") ? 1 : 0;
                    iColorInfo.m_iDefMirrorSwitch = iElement.getAttribute("default").equals("true") ? 1 : 0;
                }
                else if (nodeName.equals("flipSwitch"))
                {
                    Element iElement = (Element) iNodeList.item(i);
                    iColorInfo.m_iFlipSwitch = iNode.getFirstChild().getNodeValue().equals("true") ? 1 : 0;
                    iColorInfo.m_iDefFlipSwitch = iElement.getAttribute("default").equals("true") ? 1 : 0;
                }
                else if (nodeName.equals("imageShift"))
                {
                    Element iElement = (Element) iNodeList.item(i);
                    iColorInfo.m_iImageShift = Integer.parseInt(iNode.getFirstChild().getNodeValue());
                    iColorInfo.m_iDefImageShift = Integer.parseInt(iElement.getAttribute("default"));
                    iColorInfo.m_iMinImageShift = Integer.parseInt(iElement.getAttribute("min"));
                    iColorInfo.m_iMaxImageShift = Integer.parseInt(iElement.getAttribute("max"));
                }
                else if (nodeName.equals("WDR"))
                {
                    Node iChildNode = iNodeList.item(i);
                    if (!iChildNode.hasChildNodes())
                    {
                        return iColorInfo;
                    }
                    NodeList iChildNodeList = iChildNode.getChildNodes();
                    for (int j = 0; j < iChildNodeList.getLength(); j++)
                    {
                        iNode = iChildNodeList.item(j);
                        if (iNode.getFirstChild() == null)
                        {
                            continue;
                        }
                        nodeName = iNode.getNodeName();
                        Element iElement = (Element) iChildNodeList.item(j);
                        if (nodeName.equals("switch"))
                        {
                            iColorInfo.m_iWDRSwitch = iNode.getFirstChild().getNodeValue().trim().equals("true") ? 1 : 0;
                            iColorInfo.m_iDefWDRSwitch = iElement.getAttribute("default").equals("true") ? 1 : 0;
                        }
                        else if (nodeName.equals("value"))
                        {
                            iColorInfo.m_iWDRValue = Integer.parseInt(iNode.getFirstChild().getNodeValue().trim());
                            iColorInfo.m_iDefWDRValue = Integer.parseInt(iElement.getAttribute("default"));
                            iColorInfo.m_iMinWDRValue = Integer.parseInt(iElement.getAttribute("min"));
                            iColorInfo.m_iMaxWDRValue = Integer.parseInt(iElement.getAttribute("max"));
                        }
                    }
                }
                else if (nodeName.equals("whiteBalance"))
                {
                    Node iChildNode = iNodeList.item(i);
                    if (!iChildNode.hasChildNodes())
                    {
                        return iColorInfo;
                    }
                    NodeList iChildNodeList = iChildNode.getChildNodes();
                    for (int j = 0; j < iChildNodeList.getLength(); j++)
                    {
                        iNode = iChildNodeList.item(j);
                        if (iNode.getFirstChild() == null)
                        {
                            continue;
                        }
                        nodeName = iNode.getNodeName();
                        Element iElement = (Element) iChildNodeList.item(j);
                        if (nodeName.equals("mode"))
                        {
                            iColorInfo.m_strWhiteBalanceMode = iNode.getFirstChild().getNodeValue().trim();
                            iColorInfo.m_strDefWhiteBalanceMode = iElement.getAttribute("default");
                        }
                        else if (nodeName.equals("red"))
                        {
                            iColorInfo.m_iRedGain = Integer.parseInt(iNode.getFirstChild().getNodeValue().trim());
                            iColorInfo.m_iDefRedGain = Integer.parseInt(iElement.getAttribute("default"));
                            iColorInfo.m_iMinRedGain = Integer.parseInt(iElement.getAttribute("min"));
                            iColorInfo.m_iMaxRedGain = Integer.parseInt(iElement.getAttribute("max"));
                        }
                        else if (nodeName.equals("blue"))
                        {
                            iColorInfo.m_iBlueGain = Integer.parseInt(iNode.getFirstChild().getNodeValue().trim());
                            iColorInfo.m_iDefBlueGain = Integer.parseInt(iElement.getAttribute("default"));
                            iColorInfo.m_iMinBlueGain = Integer.parseInt(iElement.getAttribute("min"));
                            iColorInfo.m_iMaxBlueGain = Integer.parseInt(iElement.getAttribute("max"));
                        }
                    }
                }
                else if (nodeName.equals("denoise"))
                {
                    Node iChildNode = iNodeList.item(i);
                    if (!iChildNode.hasChildNodes())
                    {
                        return iColorInfo;
                    }
                    NodeList iChildNodeList = iChildNode.getChildNodes();
                    for (int j = 0; j < iChildNodeList.getLength(); j++)
                    {
                        iNode = iChildNodeList.item(j);
                        if (iNode.getFirstChild() == null)
                        {
                            continue;
                        }
                        nodeName = iNode.getNodeName();
                        Element iElement = (Element) iChildNodeList.item(j);
                        if (nodeName.equals("switch"))
                        {
                            iColorInfo.m_iDenoiseSwitch = iNode.getFirstChild().getNodeValue().trim().equals("true") ? 1 : 0;
                            iColorInfo.m_iDefDenoiseSwitch = iElement.getAttribute("default").equals("true") ? 1 : 0;
                        }
                        else if (nodeName.equals("value"))
                        {
                            iColorInfo.m_iDenoiseValue = Integer.parseInt(iNode.getFirstChild().getNodeValue().trim());
                            iColorInfo.m_iDefDenoiseValue = Integer.parseInt(iElement.getAttribute("default"));
                            iColorInfo.m_iMinDenoiseValue = Integer.parseInt(iElement.getAttribute("min"));
                            iColorInfo.m_iMaxDenoiseValue = Integer.parseInt(iElement.getAttribute("max"));
                        }
                    }
                }
                else if (nodeName.equals("sharpen"))
                {
                    Node iChildNode = iNodeList.item(i);
                    if (!iChildNode.hasChildNodes())
                    {
                        return iColorInfo;
                    }
                    NodeList iChildNodeList = iChildNode.getChildNodes();
                    for (int j = 0; j < iChildNodeList.getLength(); j++)
                    {
                        iNode = iChildNodeList.item(j);
                        if (iNode.getFirstChild() == null)
                        {
                            continue;
                        }
                        nodeName = iNode.getNodeName();
                        Element iElement = (Element) iChildNodeList.item(j);
                        if (nodeName.equals("switch"))
                        {
                            iColorInfo.m_iSharpenSwitch = iNode.getFirstChild().getNodeValue().trim().equals("true") ? 1 : 0;
                            iColorInfo.m_iDefSharpenSwitch = iElement.getAttribute("default").equals("true") ? 1 : 0;
                        }
                        else if (nodeName.equals("value"))
                        {
                            iColorInfo.m_iSharpenValue = Integer.parseInt(iNode.getFirstChild().getNodeValue().trim());
                            iColorInfo.m_iDefSharpenValue = Integer.parseInt(iElement.getAttribute("default"));
                            iColorInfo.m_iMinSharpenValue = Integer.parseInt(iElement.getAttribute("min"));
                            iColorInfo.m_iMaxSharpenValue = Integer.parseInt(iElement.getAttribute("max"));
                        }
                    }
                }
                else if (nodeName.equals("IRCutMode"))
                {
                    Element iElement = (Element) iNodeList.item(i);
                    iColorInfo.m_strIRCutMode = iNode.getFirstChild().getNodeValue();
                    iColorInfo.m_strDefIRCutMode = iElement.getAttribute("default");
                }
                else if (nodeName.equals("IRCutDayTime"))
                {
                    Element iElement = (Element) iNodeList.item(i);
                    iColorInfo.m_strIRCutDayTime = iNode.getFirstChild().getNodeValue();
                }
                else if (nodeName.equals("IRCutNightTime"))
                {
                    Element iElement = (Element) iNodeList.item(i);
                    iColorInfo.m_strIRCutNightTime = iNode.getFirstChild().getNodeValue();
                }
                else if (nodeName.equals("IRCutConvSen"))
                {
                    Element iElement = (Element) iNodeList.item(i);
                    iColorInfo.m_strIRCutConvSen = iNode.getFirstChild().getNodeValue();
                    iColorInfo.m_strDefIRCutConvSen = iElement.getAttribute("default");
                }
                else if (nodeName.equals("backlightCompensation"))
                {
                    Node iChildNode = iNodeList.item(i);
                    if (!iChildNode.hasChildNodes())
                    {
                        return iColorInfo;
                    }
                    NodeList iChildNodeList = iChildNode.getChildNodes();
                    for (int j = 0; j < iChildNodeList.getLength(); j++)
                    {
                        iNode = iChildNodeList.item(j);
                        if (iNode.getFirstChild() == null)
                        {
                            continue;
                        }
                        nodeName = iNode.getNodeName();
                        Element iElement = (Element) iChildNodeList.item(j);
                        if (nodeName.equals("mode"))
                        {
                            iColorInfo.m_strBLCMode = iNode.getFirstChild().getNodeValue().trim();
                            iColorInfo.m_strDefBLCMode = iElement.getAttribute("default");
                        }
                        else if (nodeName.equals("HWDRLevel"))
                        {
                            iColorInfo.m_strHWDRLevel = iNode.getFirstChild().getNodeValue().trim();
                            iColorInfo.m_strDefHWDRLevel = iElement.getAttribute("default");
                        }
                    }
                }
            }

            return iColorInfo;
        }
    }

    public static class ECMS_NET_REC_DATE
    {
        public ArrayList<REC_DATE_INFO> recDateList = new ArrayList<REC_DATE_INFO>();

        public static ECMS_NET_REC_DATE deserialize(String strMessage, int iChannel)
        {
            ECMS_NET_REC_DATE iRecDateInfo = new ECMS_NET_REC_DATE();

            NodeList iNodeList = NVMSHeader.GetNodeList(strMessage, "content");
            if (iNodeList == null)
            {
                return iRecDateInfo;
            }
            Node iContentNode = iNodeList.item(0);
            if (!iContentNode.hasChildNodes())
            {
                return iRecDateInfo;
            }
            iNodeList = iContentNode.getChildNodes();
            for (int i = 0; i < iNodeList.getLength(); i++)
            {
                iContentNode = iNodeList.item(i);
                String iNodeName = iContentNode.getNodeName();
                if (iNodeName.equals("item"))
                {
                    String date = iContentNode.getFirstChild().getNodeValue();
                    if (date == null || date.equals(""))
                    {
                        continue;
                    }
                    REC_DATE_INFO info = new REC_DATE_INFO();
                    Element iElement = (Element) iNodeList.item(i);
                    info.iChannelID = GUID.GetGUID(iElement.getAttribute("id").replace("{", "").replace("}", ""));
                    String[] dateArray = date.split("-");
                    info.iChannel = iChannel;
                    info.year = Integer.parseInt(dateArray[0]);
                    info.month = Integer.parseInt(dateArray[1]);
                    info.day = Integer.parseInt(dateArray[2]);

                    iRecDateInfo.recDateList.add(info);
                }
            }

            return iRecDateInfo;
        }

        public static ECMS_NET_REC_DATE deserialize(String strMessage, String date)
        {
            ECMS_NET_REC_DATE iRecDateInfo = new ECMS_NET_REC_DATE();

            NodeList iNodeList = NVMSHeader.GetNodeList(strMessage, "content");
            if (iNodeList == null)
            {
                return iRecDateInfo;
            }
            Node iContentNode = iNodeList.item(0);
            if (!iContentNode.hasChildNodes())
            {
                return iRecDateInfo;
            }
            iNodeList = iContentNode.getChildNodes();
            for (int i = 0; i < iNodeList.getLength(); i++)
            {
                iContentNode = iNodeList.item(i);
                String iNodeName = iContentNode.getNodeName();
                if (iNodeName.equals("item"))
                {
                    REC_DATE_INFO info = new REC_DATE_INFO();
                    Element iElement = (Element) iNodeList.item(i);
                    info.iChannelID = GUID.GetGUID(iElement.getAttribute("id").replace("{", "").replace("}", ""));

                    String[] dateArray = date.split("-");
                    info.year = Integer.parseInt(dateArray[0]);
                    info.month = Integer.parseInt(dateArray[1]);
                    info.day = Integer.parseInt(dateArray[2]);

                    iRecDateInfo.recDateList.add(info);
                }
            }

            return iRecDateInfo;
        }

    }

    public static class ECMS_NET_SUPPORT_PTZ
    {
        public ArrayList<GUID> iSupportPtzList = new ArrayList<GUID>();

        public static ECMS_NET_SUPPORT_PTZ deserialize(String strMessage)
        {
            ECMS_NET_SUPPORT_PTZ iSupportPtzInfo = new ECMS_NET_SUPPORT_PTZ();

            NodeList iNodeList = NVMSHeader.GetNodeList(strMessage, "content");
            if (iNodeList == null)
            {
                return iSupportPtzInfo;
            }
            Node iContentNode = iNodeList.item(0);
            if (!iContentNode.hasChildNodes())
            {
                return iSupportPtzInfo;
            }
            iNodeList = iContentNode.getChildNodes();
            for (int i = 0; i < iNodeList.getLength(); i++)
            {
                iContentNode = iNodeList.item(i);
                String iNodeName = iContentNode.getNodeName();
                if (iNodeName.equals("item"))
                {
                    Element iElement = (Element) iNodeList.item(i);
                    GUID iChannelID = GUID.GetGUID(iElement.getAttribute("id").replace("{", "").replace("}", ""));

                    iSupportPtzInfo.iSupportPtzList.add(iChannelID);
                }
                else if (iNodeName.equals("name"))
                {

                }
            }

            return iSupportPtzInfo;
        }
    }

    public static class ECMS_NET_ONLINE_CHNN
    {
        public ArrayList<GUID> iOnlineList = new ArrayList<GUID>();

        public static ECMS_NET_ONLINE_CHNN deserialize(String strMessage)
        {
            ECMS_NET_ONLINE_CHNN iOnlineList = new ECMS_NET_ONLINE_CHNN();

            NodeList iNodeList = NVMSHeader.GetNodeList(strMessage, "content");
            if (iNodeList == null)
            {
                return iOnlineList;
            }
            Node iContentNode = iNodeList.item(0);
            if (!iContentNode.hasChildNodes())
            {
                return iOnlineList;
            }
            iNodeList = iContentNode.getChildNodes();
            for (int i = 0; i < iNodeList.getLength(); i++)
            {
                iContentNode = iNodeList.item(i);
                String iNodeName = iContentNode.getNodeName();
                if (iNodeName.equals("item"))
                {
                    Element iElement = (Element) iNodeList.item(i);
                    GUID iChannelID = GUID.GetGUID(iElement.getAttribute("id").replace("{", "").replace("}", ""));

                    iOnlineList.iOnlineList.add(iChannelID);
                }
            }
            return iOnlineList;
        }
    }

    public static class ECMS_NET_DEVICE_INFO
    {
        public int m_iNetProductType = 0;
        public String m_iKernelVersion = "";
        public String m_iSoftVersion = "";
        public String m_sHardVersion = "";
        public int m_iTotalAlarmOutputChannelCount = 0;
        public String m_strDeviceName = "";
        public String m_iSoftBuildDate = "";
        public int m_iDeviceID = 0;
        public String m_iMCUVersion = "";
        public String m_sProductModel = "";

        public static ECMS_NET_DEVICE_INFO deserialize(String strMessage)
        {
            ECMS_NET_DEVICE_INFO iDeviceInfo = new ECMS_NET_DEVICE_INFO();

            NodeList nodeList = NVMSHeader.GetNodeList(strMessage, "content");
            if (nodeList == null)
            {
                return iDeviceInfo;
            }
            for (int i = 0; i < nodeList.getLength(); i++)
            {
                Node item = nodeList.item(i);
                NodeList properties = item.getChildNodes();
                for (int j = 0; j < properties.getLength(); j++)
                {
                    Node property = properties.item(j);
                    String nodeName = property.getNodeName();
                    if (property.getFirstChild() == null)
                    {
                        continue;
                    }
                    if (nodeName.equals("id"))
                    {
                        // iDeviceItem.
                    }
                    else if (nodeName.equals("productType"))
                    {
                        iDeviceInfo.m_iNetProductType = Integer.parseInt(property.getFirstChild().getNodeValue());
                    }
                    else if (nodeName.equals("kenerlVersion"))
                    {
                        iDeviceInfo.m_iKernelVersion = property.getFirstChild().getNodeValue();
                    }
                    else if (nodeName.equals("softwareVersion"))
                    {
                        iDeviceInfo.m_iSoftVersion = property.getFirstChild().getNodeValue();
                    }
                    else if (nodeName.equals("hardwareVersion"))
                    {
                        iDeviceInfo.m_sHardVersion = property.getFirstChild().getNodeValue();
                    }
                    else if (nodeName.equals("alarmOutCount"))
                    {
                        iDeviceInfo.m_iTotalAlarmOutputChannelCount = Integer.parseInt(property.getFirstChild().getNodeValue());
                    }
                    else if (nodeName.equals("name"))
                    {
                        iDeviceInfo.m_strDeviceName = property.getFirstChild().getNodeValue();
                    }
                    else if (nodeName.equals("launchDate"))
                    {
                        iDeviceInfo.m_iSoftBuildDate = property.getFirstChild().getNodeValue();
                    }
                    else if (nodeName.equals("deviceNumber"))
                    {
                        iDeviceInfo.m_iDeviceID = Integer.parseInt(property.getFirstChild().getNodeValue());
                    }
                    else if (nodeName.equals("mcuVersion"))
                    {
                        iDeviceInfo.m_iMCUVersion = property.getFirstChild().getNodeValue();
                    }
                    else if (nodeName.equals("productModel"))
                    {
                        iDeviceInfo.m_sProductModel = property.getFirstChild().getNodeValue();
                    }
                }
            }
            return iDeviceInfo;
        }
    }

    // yq 2016-3-9
    public static class ALARMOUT_STATUS
    {
        public String name;
        public boolean onlineStatus;
        public boolean bSwitch;
        public String id;

        public static ArrayList<ALARMOUT_STATUS> deserialize(String strMessage)
        {
            ArrayList<ALARMOUT_STATUS> iStatusList = new ArrayList<ALARMOUT_STATUS>();

            NodeList iContentNodes = NVMSHeader.GetNodeList(strMessage, "content");
            if (iContentNodes == null)
            {
                return iStatusList;
            }
            Node iItemNode = iContentNodes.item(0);
            if (iItemNode == null || !iItemNode.hasChildNodes())
            {
                return iStatusList;
            }
            NodeList iItemNodes = iItemNode.getChildNodes();
            for (int i = 0; i < iItemNodes.getLength(); i++)
            {
                Node iNode = iItemNodes.item(i);
                String strNodeName = iNode.getNodeName();
                ALARMOUT_STATUS iStatusInfo = new ALARMOUT_STATUS();
                if (!strNodeName.equals("item"))
                {
                    continue;
                }
                else if (strNodeName.equals("item"))
                {
                    iStatusInfo.id = ((Element) iNode).getAttribute("id");
                }
                if (!iNode.hasChildNodes())
                {
                    continue;
                }

                NodeList items = iNode.getChildNodes();
                for (int j = 0; j < items.getLength(); j++)
                {
                    Node node = items.item(j);
                    String nodeName = node.getNodeName();
                    if (nodeName.equals("name"))
                    {
                        iStatusInfo.name = node.getFirstChild().getNodeValue();
                    }
                    else if (nodeName.equals("onlineStatus"))
                    {
                        iStatusInfo.onlineStatus = node.getFirstChild().getNodeValue().equals("true");
                    }
                    else if (nodeName.equals("switch"))
                    {
                        iStatusInfo.bSwitch = node.getFirstChild().getNodeValue().equals("true");
                    }
                }

                iStatusList.add(iStatusInfo);
            }
            return iStatusList;
        }
    }

    public static class UserItem
    {
        public String id;
        public String userName;
        public String password;
        public GUID authGroupId;
        public boolean bindMacSwitch;
        public String mac;
        public String email;
        public String comment;
        public boolean enabled;
        public boolean authEffective;

        public static List<UserItem> deserialize(String strMessage)
        {
            List<UserItem> userList = new ArrayList<UserItem>();

            NodeList iContentNodes = NVMSHeader.GetNodeList(strMessage, "content");
            if (iContentNodes == null)
            {
                return userList;
            }
            Node iItemNode = iContentNodes.item(0);
            if (iItemNode == null || !iItemNode.hasChildNodes())
            {
                return userList;
            }
            NodeList iItemNodes = iItemNode.getChildNodes();
            for (int i = 0; i < iItemNodes.getLength(); i++)
            {
                Node iNode = iItemNodes.item(i);
                String strNodeName = iNode.getNodeName();
                UserItem userItem = new UserItem();
                if (!strNodeName.equals("item"))
                {
                    continue;
                }
                else if (strNodeName.equals("item"))
                {
                    userItem.id = ((Element) iNode).getAttribute("id");
                }
                if (!iNode.hasChildNodes())
                {
                    continue;
                }

                NodeList items = iNode.getChildNodes();
                for (int j = 0; j < items.getLength(); j++)
                {
                    Node node = items.item(j);
                    String nodeName = node.getNodeName();
                    if (nodeName.equals("userName") && node.getFirstChild() != null)
                    {
                        userItem.userName = node.getFirstChild().getNodeValue();
                    }
                    else if (nodeName.equals("password") && node.getFirstChild() != null)
                    {
                        userItem.password = node.getFirstChild().getNodeValue();
                    }
                    else if (nodeName.equals("authGroup"))
                    {
                        Element iElement = (Element) items.item(j);
                        userItem.authGroupId = GUID.GetGUID(iElement.getAttribute("id").replace("{", "").replace("}", ""));
                    }
                    else if (nodeName.equals("bindMacSwitch") && node.getFirstChild() != null)
                    {
                        userItem.bindMacSwitch = node.getFirstChild().getNodeValue().equals("true");
                    }
                    else if (nodeName.equals("mac") && node.getFirstChild() != null)
                    {
                        userItem.mac = node.getFirstChild().getNodeValue();
                    }
                    else if (nodeName.equals("email") && node.getFirstChild() != null)
                    {
                        userItem.email = node.getFirstChild().getNodeValue();
                    }
                    else if (nodeName.equals("comment") && node.getFirstChild() != null)
                    {
                        userItem.comment = node.getFirstChild().getNodeValue();
                    }
                    else if (nodeName.equals("enabled") && node.getFirstChild() != null)
                    {
                        userItem.enabled = node.getFirstChild().getNodeValue().equals("true");
                    }
                    else if (nodeName.equals("authEffective") && node.getFirstChild() != null)
                    {
                        userItem.authEffective = node.getFirstChild().getNodeValue().equals("true");
                    }
                }
                userList.add(userItem);
            }
            return userList;
        }
    }

    public static class AuthItem
    {
        public static class ChlAuth
        {
            public GUID id;
            public String name;
            public boolean lp;
            public boolean spr;
            public boolean bk;
            public boolean ptz;
        }

        public String id;
        public String name;
        public boolean isDefault;
        public List<ChlAuth> chlAuths;
        public boolean localSysCfgAndMaintain;
        public boolean remoteSysCfgAndMaintain;
        public boolean localChlMgr;
        public boolean remoteChlMgr;
        public boolean remoteLogin;
        public boolean diskMgr;
        public boolean talk;
        public boolean alarmMgr;
        public boolean net;
        public boolean rec;
        public boolean scheduleMgr;

        public static AuthItem deserialize(String strMessage)
        {
            AuthItem authItem = new AuthItem();

            NodeList iContentNodes = NVMSHeader.GetNodeList(strMessage, "content");
            if (iContentNodes == null)
            {
                return authItem;
            }
            Node iItemNode = iContentNodes.item(0);
            if (iItemNode == null || !iItemNode.hasChildNodes())
            {
                return authItem;
            }
            NodeList iItemNodes = iItemNode.getChildNodes();
            for (int i = 0; i < iItemNodes.getLength(); i++)
            {
                Node iNode = iItemNodes.item(i);
                String strNodeName = iNode.getNodeName();
                if (strNodeName.equals("id"))
                {
                    authItem.id = ((Element) iNode).getAttribute("id");
                }
                else if (strNodeName.equals("name"))
                {
                    authItem.name = iNode.getFirstChild().getNodeValue();
                }
                else if (strNodeName.equals("isDefault"))
                {
                    authItem.isDefault = iNode.getFirstChild().getNodeValue().equals("true");
                }
                else if (strNodeName.equals("chlAuths"))
                {
                    NodeList items = iNode.getChildNodes();
                    for (int j = 0; j < items.getLength(); j++)
                    {
                        Node node = items.item(j);
                        if (node.getNodeName().equals("item"))
                        {
                            ChlAuth chlAuth = new ChlAuth();
                            NodeList nodes = node.getChildNodes();
                            Element iElement = (Element) items.item(j);
                            chlAuth.id = GUID.GetGUID(iElement.getAttribute("id").replace("{", "").replace("}", ""));
                            for (int k = 0; k < nodes.getLength(); k++)
                            {
                                String nodeName = node.getNodeName();
                                if (nodeName.equals("auth"))
                                {
                                    String[] auths = node.getFirstChild().getNodeValue().split(",");
                                    for (String auth : auths)
                                    {
                                        if (auth.equals("@lp"))
                                        {
                                            chlAuth.lp = true;
                                        }
                                        else if (auth.equals("@spr"))
                                        {
                                            chlAuth.spr = true;
                                        }
                                        else if (auth.equals("@bk"))
                                        {
                                            chlAuth.bk = true;
                                        }
                                        else if (auth.equals("@ptz"))
                                        {
                                            chlAuth.ptz = true;
                                        }
                                    }
                                }
                            }
                            authItem.chlAuths.add(chlAuth);
                        }
                    }
                }
                else if (strNodeName.equals("localSysCfgAndMaintain"))
                {
                    authItem.localSysCfgAndMaintain = iNode.getFirstChild().getNodeValue().equals("true");
                }
                else if (strNodeName.equals("remoteSysCfgAndMaintain"))
                {
                    authItem.remoteSysCfgAndMaintain = iNode.getFirstChild().getNodeValue().equals("true");
                }
                else if (strNodeName.equals("localChlMgr"))
                {
                    authItem.localChlMgr = iNode.getFirstChild().getNodeValue().equals("true");
                }
                else if (strNodeName.equals("remoteChlMgr"))
                {
                    authItem.remoteChlMgr = iNode.getFirstChild().getNodeValue().equals("true");
                }
                else if (strNodeName.equals("remoteLogin"))
                {
                    authItem.remoteLogin = iNode.getFirstChild().getNodeValue().equals("true");
                }
                else if (strNodeName.equals("diskMgr"))
                {
                    authItem.diskMgr = iNode.getFirstChild().getNodeValue().equals("true");
                }
                else if (strNodeName.equals("talk"))
                {
                    authItem.talk = iNode.getFirstChild().getNodeValue().equals("true");
                }
                else if (strNodeName.equals("alarmMgr"))
                {
                    authItem.alarmMgr = iNode.getFirstChild().getNodeValue().equals("true");
                }
                else if (strNodeName.equals("net"))
                {
                    authItem.net = iNode.getFirstChild().getNodeValue().equals("true");
                }
                else if (strNodeName.equals("rec"))
                {
                    authItem.rec = iNode.getFirstChild().getNodeValue().equals("true");
                }
                else if (strNodeName.equals("scheduleMgr"))
                {
                    authItem.scheduleMgr = iNode.getFirstChild().getNodeValue().equals("true");
                }
            }
            return authItem;
        }
    }

    public static class EventNotifyParams
    {
        public List<Integer> popVideoDurationNote;
        public int popVideoDuration;
        public List<Integer> popVideoOutputNote;
        public int popVideoOutput;
        public List<Integer> popMsgDurationNote;
        public int popMsgDuration;
        public List<Integer> buzzerDurationNote;
        public int buzzerDuration;
        public boolean mobileSupportPush = false;
        public boolean mobilePushSwitch = false;

        public static EventNotifyParams deserialize(String strMessage)
        {
            EventNotifyParams params = new EventNotifyParams();
            NodeList iContentNodes = NVMSHeader.GetNodeList(strMessage, "content");
            if (iContentNodes == null)
            {
                return params;
            }
            Node iItemNode = iContentNodes.item(0);
            if (iItemNode == null || !iItemNode.hasChildNodes())
            {
                return params;
            }
            NodeList iItemNodes = iItemNode.getChildNodes();
            for (int i = 0; i < iItemNodes.getLength(); i++)
            {
                Node iNode = iItemNodes.item(i);
                String strNodeName = iNode.getNodeName();
                if (strNodeName.equals("mobilePushSwitch"))
                {
                    params.mobilePushSwitch = iNode.getFirstChild().getNodeValue().equals("true");
                    params.mobileSupportPush = true;
                }
            }
            return params;
        }
    }

    public static class FISH_EYE_INFO
    {
        public GUID nodeID = GUID.GetNullGUID();
        public boolean supportFishEye = false;

        public static List<FISH_EYE_INFO> deserialize(String strMessage)
        {
            List<FISH_EYE_INFO> list = new ArrayList<FISH_EYE_INFO>();
            NodeList iContentNodes = NVMSHeader.GetNodeList(strMessage, "content");
            if (iContentNodes == null)
            {
                return list;
            }
            Node iItemNode = iContentNodes.item(0);
            if (iItemNode == null || !iItemNode.hasChildNodes())
            {
                return list;
            }
            NodeList iItemNodes = iItemNode.getChildNodes();
            for (int i = 0; i < iItemNodes.getLength(); i++)
            {
                Node item = iItemNodes.item(i);
                if (!item.getNodeName().equals("item"))
                {
                    continue;
                }
                FISH_EYE_INFO info = new FISH_EYE_INFO();
                info.supportFishEye = true;
                Element personNode = (Element) iItemNodes.item(i);
                info.nodeID = GUID.GetGUID(personNode.getAttribute("id").replace("{", "").replace("}", ""));

                list.add(info);
            }
            return list;
        }
    }

    public static class CLOUD_UPGRADE_INFO
    {
        public String strcurrentVersion = "";
        public String strcurrentLaunchDate = "";
        public boolean bIsLatest = true;
        public String strLatestVersion = "V1.3.4.11497B171127.N0I.U1(16A84G).beta";

    }

    public static class REC_DATE_INFO
    {
        public int iChannel;
        public GUID iChannelID = new GUID();
        public int year;
        public int month;
        public int day;
    }
}