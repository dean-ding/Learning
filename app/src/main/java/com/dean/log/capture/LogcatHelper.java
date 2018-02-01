package com.dean.log.capture;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * log日志统计保存
 *
 * @author way
 */

public class LogcatHelper
{

    private static volatile LogcatHelper INSTANCE = null;
    private static String PATH_LOGCAT;
    private LogDumper mLogDumper = null;
    private int mPId;

    public static LogcatHelper getInstance()
    {
        if (INSTANCE == null)
        {
            synchronized (LogcatHelper.class)
            {
                if (INSTANCE == null)
                {
                    INSTANCE = new LogcatHelper();
                }
            }
        }
        return INSTANCE;
    }

    private LogcatHelper()
    {
        mPId = android.os.Process.myPid();
    }

    public void start()
    {
        if (mLogDumper == null)
        {
            mLogDumper = new LogDumper(String.valueOf(mPId), PATH_LOGCAT);
        }
        if (!mLogDumper.isAlive())
        {
            mLogDumper.start();
        }
    }

    public void stop()
    {
        if (mLogDumper != null)
        {
            mLogDumper.stopLogs();
            mLogDumper = null;
        }
    }

    private class LogDumper extends Thread
    {

        private Process logcatProc;
        private BufferedReader mReader = null;
        private boolean mRunning = true;
        String cmds = null;
        private String mPID;
        private FileOutputStream out = null;

        public LogDumper(String pid, String dir)
        {
            mPID = pid;
            try
            {
                out = new FileOutputStream(new File(dir, getFileName() + ".txt"));
            }
            catch (FileNotFoundException e)
            {
                e.printStackTrace();
            }
            /**
             * 日志等级：*:v , *:d , *:w , *:e , *:f , *:s 显示当前mPID程序的 E和W等级的日志.
             */
            cmds = "logcat  | grep \"(" + mPID + ")\"";// 打印所有日志信息
        }

        public String getFileName()
        {
            SimpleDateFormat format1 = new SimpleDateFormat("yyyy-MM-dd-HH-mm-ss", Locale.getDefault());
            return format1.format(new Date(System.currentTimeMillis()));// 2012-10-03 23:41:31
        }

        public void stopLogs()
        {
            mRunning = false;
        }

        @Override
        public void run()
        {
            try
            {
                logcatProc = Runtime.getRuntime().exec(cmds);
                mReader = new BufferedReader(new InputStreamReader(logcatProc.getInputStream()), 1024);
                String line;
                while (mRunning && (line = mReader.readLine()) != null)
                {
                    if (!mRunning)
                    {
                        break;
                    }
                    if (line.length() == 0)
                    {
                        continue;
                    }
                    if (out != null && line.contains("chromium"))
                    {
                        out.write((getFileName() + "  " + line + "\n").getBytes());
                    }
                }
            }
            catch (IOException e)
            {
                e.printStackTrace();
            }
            finally
            {
                if (logcatProc != null)
                {
                    logcatProc.destroy();
                    logcatProc = null;
                }
                if (mReader != null)
                {
                    try
                    {
                        mReader.close();
                        mReader = null;
                    }
                    catch (IOException e)
                    {
                        e.printStackTrace();
                    }
                }
                if (out != null)
                {
                    try
                    {
                        out.close();
                    }
                    catch (IOException e)
                    {
                        e.printStackTrace();
                    }
                    out = null;
                }
            }
        }
    }
}
