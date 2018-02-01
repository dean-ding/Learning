package com.dean.rxjava;

import android.graphics.Color;
import android.os.Bundle;
import android.os.SystemClock;
import android.widget.TextView;

import com.dean.R;
import com.dean.swipback.ui.SwipeBackActivity;

import java.util.concurrent.TimeUnit;

import io.reactivex.Observable;
import io.reactivex.Observer;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Function;
import io.reactivex.schedulers.Schedulers;

/**
 * Created: tvt on 18/1/6 14:59
 */
public class CountDownActivity extends SwipeBackActivity
{
    private TextView mCountDownView;
    private Observable<Long> mObservable;
    private Disposable mDisposable;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.countdown_layout);

        mCountDownView = (TextView) findViewById(R.id.countdown_view);
        mCountDownView.setEnabled(false);
        mCountDownView.setTextColor(Color.GRAY);
        countDown();

        Thread thread = new Thread()
        {
            @Override
            public void run()
            {
                super.run();
                SystemClock.sleep(10000);
                if (mDisposable != null && !mDisposable.isDisposed())
                {
                    mDisposable.dispose();
                }
            }
        };
        thread.start();
    }

    private void countDown()
    {
        final long count = 30;
        mObservable = Observable.interval(1, TimeUnit.SECONDS).take(count + 1).map(new Function<Long, Long>()
        {
            @Override
            public Long apply(Long aLong) throws Exception
            {
                System.out.println("apply");
                return count - aLong;
            }
        }).subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread());
        mObservable.subscribe(new Observer<Long>()
        {
            @Override
            public void onSubscribe(Disposable disposable)
            {
                mDisposable = disposable;
            }

            @Override
            public void onNext(Long aLong)
            {
                System.out.println("onNext");
                mCountDownView.setText("确定" + "(" + String.valueOf(aLong) + ")");
            }

            @Override
            public void onError(Throwable throwable)
            {

            }

            @Override
            public void onComplete()
            {
                System.out.println("onComplete");
                mCountDownView.setText("确定");
                mCountDownView.setEnabled(true);
                mCountDownView.setTextColor(Color.BLACK);
            }
        });
    }

    private void httpRequest()
    {

    }

}
