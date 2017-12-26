package com.dean.serverlibrary;

import android.content.Context;

import java.util.Random;

public class MyUtil
{
    public int bytes2int(byte[] b)
    {
        int mask = 0xff;
        int temp = 0;
        int res = 0;
        for (int i = 0; i < 4; i++)
        {
            res <<= 8;
            temp = b[3 - i] & mask;
            res |= temp;
        }
        return res;
    }

    public short bytes2short(byte[] b)
    {
        short mask = 0xff;
        short temp = 0;
        short res = 0;
        for (int i = 0; i < 2; i++)
        {
            res <<= 8;
            temp = (short) (b[1 - i] & mask);
            res |= temp;
        }
        return res;
    }

    public byte[] int2bytes(int num)
    {
        byte[] b = new byte[4];
        for (int i = 0; i < 4; i++)
        {
            b[i] = (byte) (num >>> (24 - i * 8));
        }
        return b;
    }

    public byte[] int2bytesReverse(int num)
    {
        byte[] b = new byte[4];
        for (int i = 0; i < 4; i++)
        {
            b[3 - i] = (byte) (num >>> (24 - i * 8));
        }
        return b;
    }

    public int ChangeIndex(int num)
    {
        byte[] test = new byte[4];
        test[0] = (byte) (num & 0x000000FF);
        test[1] = (byte) (num & 0x0000FF00 >> 8);
        test[2] = (byte) (num & 0x00FF0000 >> 16);
        test[3] = (byte) ((num & 0xFF000000) >> 24);
        return bytes2int(test);
    }

    public int random()
    {
        Random r = new Random();
        return r.nextInt();
    }

    public int ntohl(int in)
    {
        return ((in & 0xFF) << 24) | ((in & 0xFF00) << 8) | ((in & 0xFF0000) >> 8) | ((in & 0xFF000000) >> 24);
    }

    public byte[] long2bytes(long num)
    {
        byte[] b = new byte[8];
        for (int i = 0; i < 8; i++)
        {
            b[i] = (byte) (num >>> (56 - i * 8));
        }
        byte[] bReturn = new byte[8];
        bReturn[0] = b[7];
        bReturn[1] = b[6];
        bReturn[2] = b[5];
        bReturn[3] = b[4];
        bReturn[4] = b[3];
        bReturn[5] = b[2];
        bReturn[6] = b[1];
        bReturn[7] = b[0];
        return bReturn;
    }

    public byte[] ChangeByteOrder(byte[] b, int iLen)
    {
        byte[] bTest = new byte[iLen];
        for (int i = 0; i < iLen; i++)
        {
            bTest[i] = b[iLen - i - 1];
        }
        return bTest;

    }

    public long byte2long(byte[] b)
    {
        int shift;
        long kk, jj = 0;
        shift = 0;
        for (int i = 0; i < 8; i++)
        {
            kk = (long) (b[i]);
            if (kk < 0)
            {
                kk = 256 + kk;
            }
            kk = (kk << shift);
            jj = jj + kk;
            shift = shift + 8;
        }
        return jj;
    }

    public long byte2longB(byte buf[], int start, int len)
    {
        long ret = 0;
        len = len > 4 ? 4 : len;
        ret |= (buf[start] & 0x80);
        ret |= (buf[start] & 0x7F);
        for (int i = 1; i < len; i++)
        {
            ret |= ((buf[start + i] & 0x80) << (8 * i));
            ret |= ((buf[start + i] & 0x7F) << (8 * i));
        }
        return ret;
    }

    public long byteFuckLong(byte[] b)
    {
        long lReturn = 0;
        long lmask = 1;
        for (int i = 0; i < 64; i++)
        {
            if (b[i] == 1)
            {
                lReturn += (lmask << i);
            }
        }
        return lReturn;
    }

    public byte[] short2bytes(short num)
    {
        byte[] b = new byte[2];
        b[0] = (byte) (num);
        b[1] = (byte) (num >> 8);

        return b;
    }

    public byte[] unsingedInt2byte(long num)
    {
        byte[] b = new byte[4];
        for (int i = 0; i < 4; i++)
        {
            b[i] = (byte) (num >> (i * 8));
        }
        return b;
    }

    public int byte2unsignedInt(byte[] b)
    {
        int mask = 0xff;
        int temp = 0;
        int res = 0;
        for (int i = 0; i < 4; i++)
        {
            int test = b[i];
            if (test < 0)
            {
                test = 255 + test;
            }
            res <<= 8;
            temp = (short) (test & mask);
            res |= temp;
        }
        return res;
    }

    public long byte2unsignedInt2(byte[] b)
    {
        int firstByte = (0x000000FF & ((int) b[0]));
        int secondByte = (0x000000FF & ((int) b[1]));
        int thirdByte = (0x000000FF & ((int) b[2]));
        int fourthByte = (0x000000FF & ((int) b[3]));
        long anUnsignedInt = ((long) (firstByte << 24 | secondByte << 16 | thirdByte << 8 | fourthByte)) & 0xFFFFFFFFL;

        return anUnsignedInt;
    }

    public static int Dp2Px(Context context, float dp)
    {
        final float scale = context.getResources().getDisplayMetrics().density;
        return (int) (dp * scale + 0.5f);
    }

    public static int Px2Dp(Context context, float px)
    {
        final float scale = context.getResources().getDisplayMetrics().density;
        return (int) (px / scale + 0.5f);
    }
}
