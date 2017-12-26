package com.dean.serverlibrary;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.TimeZone;

class tm
{
    int tm_sec; /* seconds after the minute - [0,59] */
    int tm_min; /* minutes after the hour - [0,59] */
    int tm_hour; /* hours since midnight - [0,23] */
    int tm_mday; /* day of the month - [1,31] */
    int tm_mon; /* months since January - [0,11] */
    int tm_year; /* years since 1900 */
    int tm_wday; /* days since Sunday - [0,6] */
    int tm_yday; /* days since January 1 - [0,365] */
    int tm_isdst; /* daylight savings time flag */
}

public class TimeOperation
{
    static byte[] Days = {31, 28, 31, 30, 31, 30, 31, 31, 30, 31, 30, 31};

    static String GetTimeString(Calendar calendar, String strLink)
    {
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH) + 1;
        int day = calendar.get(Calendar.DAY_OF_MONTH);
        int hour = calendar.get(Calendar.HOUR);
        int min = calendar.get(Calendar.MINUTE);
        int sec = calendar.get(Calendar.SECOND);
        int pm = calendar.get(Calendar.AM_PM);
        hour = (pm == 1) ? (hour + 12) : hour;

        StringBuffer sb = new StringBuffer();
        sb.append(year).append(strLink);
        sb.append(month < 10 ? "0" : "").append(month).append(strLink);
        sb.append(day < 10 ? "0" : "").append(day).append(strLink);
        sb.append(hour < 10 ? "0" : "").append(hour).append(strLink);
        sb.append(min < 10 ? "0" : "").append(min).append(strLink);
        sb.append(sec < 10 ? "0" : "").append(sec);

        return sb.toString();
    }

    public final static Calendar GetCalendar(Date date)
    {
        TimeZone defaultZone = TimeZone.getDefault();
        Calendar calendar = Calendar.getInstance(defaultZone);
        calendar.setTime(date);
        return calendar;
    }

    public final static String Change1970SecondToTimeString(long lSeconds, String strLink)
    {
        lSeconds = lSeconds / 1000;
        return GetTimeString(GetCalendar(new Date(lSeconds)), strLink);
    }

    /* 将时间戳转换成日期时间 */
    public static tm getLoaclTime(long time, long timezone)
    {
        tm tm_time = new tm();
        int n32_Pass4year;
        int n32_hpery;

        // 计算时差
        time = time - timezone;

        if (time < 0)
        {
            time = 0;
        }
        // 取秒时间
        tm_time.tm_sec = (int) (time % 60);
        time /= 60;
        // 取分钟时间
        tm_time.tm_min = (int) (time % 60);
        time /= 60;
        // 取过去多少个四年，每四年有 1461*24 小时
        n32_Pass4year = ((int) time / (1461 * 24));
        // 计算年份
        tm_time.tm_year = (n32_Pass4year << 2) + 70;
        // 四年中剩下的小时数
        time %= 1461L * 24L;
        // 校正闰年影响的年份，计算一年中剩下的小时数
        for (; ; )
        {
            // 一年的小时数
            n32_hpery = 365 * 24;
            // 判断闰年
            if ((tm_time.tm_year & 3) == 0)
            {
                // 是闰年，一年则多24小时，即一天
                n32_hpery += 24;
            }
            if (time < n32_hpery)
            {
                break;
            }
            tm_time.tm_year++;
            time -= n32_hpery;
        }
        // 小时数
        tm_time.tm_hour = (int) (time % 24);
        // 一年中剩下的天数
        time /= 24;
        // 假定为闰年
        time++;
        // 校正润年的误差，计算月份，日期
        if ((tm_time.tm_year & 3) == 0)
        {
            if (time > 60)
            {

            }
            else
            {
                if (time == 60)
                {
                    tm_time.tm_mon = 1;
                    tm_time.tm_mday = 29;
                    return tm_time;
                }
            }
        }
        // 计算月日
        for (tm_time.tm_mon = 0; Days[tm_time.tm_mon] < time; tm_time.tm_mon++)
        {
            time -= Days[tm_time.tm_mon];
        }

        tm_time.tm_mday = (int) (time);

        return tm_time;

    }

    public static String GetCurrentTime(String strLink)
    {
        Calendar c = Calendar.getInstance();
        int year = c.get(Calendar.YEAR);
        int month = c.get(Calendar.MONTH);
        int day = c.get(Calendar.DAY_OF_MONTH);
        int hour = c.get(Calendar.HOUR_OF_DAY);
        int minute = c.get(Calendar.MINUTE);
        int second = c.get(Calendar.SECOND);

        return year + strLink + month + strLink + day + strLink + hour + strLink + minute + strLink + second;
    }

    public static long GetCurrentDayStartTime()
    {
        Calendar currentDate = new GregorianCalendar();

        currentDate.set(Calendar.HOUR_OF_DAY, 0);
        currentDate.set(Calendar.MINUTE, 0);
        currentDate.set(Calendar.SECOND, 0);
        Date date = currentDate.getTime();
        return date.getTime();
    }

    public static String ChangeSecondToHourString(int lSeconds, String strLink)
    {
        int hour = lSeconds / 3600;
        int min = lSeconds % 3600 / 60;
        int sec = lSeconds % 3600 % 60;

        StringBuffer sb = new StringBuffer();
        sb.append(hour < 10 ? "0" : "").append(hour).append(strLink);
        sb.append(min < 10 ? "0" : "").append(min).append(strLink);
        sb.append(sec < 10 ? "0" : "").append(sec);

        return sb.toString();
    }

    public static int ChangeHourStringToSecond(String time, String pattern)
    {
        String[] times = time.split(pattern);

        return Integer.parseInt(times[0]) * 3600 + Integer.parseInt(times[1]) * 60 + Integer.parseInt(times[2]);
    }

    public static String utc2LocalTime(String utcTime)
    {
        String timePatten = "yyyy-MM-dd HH:mm:ss";
        SimpleDateFormat utcFormater = new SimpleDateFormat(timePatten);
        utcFormater.setTimeZone(TimeZone.getTimeZone("UTC"));// 时区定义并进行时间获取
        Date gpsUTCDate = null;
        try
        {
            gpsUTCDate = utcFormater.parse(utcTime);
            SimpleDateFormat localFormater = new SimpleDateFormat(timePatten);
            localFormater.setTimeZone(TimeZone.getDefault());
            return localFormater.format(gpsUTCDate.getTime());
        }
        catch (ParseException e)
        {
            e.printStackTrace();
        }
        return utcTime;
    }


}
