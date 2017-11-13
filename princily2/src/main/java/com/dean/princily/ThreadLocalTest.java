package com.dean.princily;

/**
 * Created: tvt on 17/10/30 10:28
 */
public class ThreadLocalTest
{
    private ThreadLocal<Integer> mThreadLocal = null;
    private boolean mState = false;

    public ThreadLocalTest()
    {
        mThreadLocal = new ThreadLocal<>();
        mState = true;
    }

    public void ThreadLocal1()
    {
        new Thread(new Runnable()
        {
            @Override
            public void run()
            {
                mThreadLocal.set(2);
                for (int i = 0; i < 10; i++)
                {
                    System.out.println("ThreadLocal1 mThreadLocal.get() = " + mThreadLocal.get());
                    try
                    {
                        Thread.sleep(1000);
                    }
                    catch (InterruptedException e)
                    {
                        e.printStackTrace();
                    }
                }
            }
        }).start();
    }

    public void ThreadLocal2()
    {
        new Thread(new Runnable()
        {
            @Override
            public void run()
            {
                int index = 0;
                while (mState && index <= 100)
                {
                    index++;
                    //                    mThreadLocal.set(index);
                    System.out.println("ThreadLocal2 mThreadLocal.get() = " + mThreadLocal.get());
                    try
                    {
                        Thread.sleep(1000);
                    }
                    catch (InterruptedException e)
                    {
                        e.printStackTrace();
                    }
                }
                mState = false;
            }
        }).start();
    }

}
