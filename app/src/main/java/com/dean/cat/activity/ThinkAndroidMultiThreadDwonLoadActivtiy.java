package com.dean.cat.activity;

import android.content.ComponentName;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;

import com.dean.R;
import com.dean.cat.adapter.DownloadListAdapter;
import com.dean.cat.data.DownloadUrls;
import com.ta.TAActivity;
import com.ta.annotation.TAInjectView;
import com.ta.util.download.DownloadManager;

public class ThinkAndroidMultiThreadDwonLoadActivtiy extends TAActivity
{
    @TAInjectView(id = R.id.download_list)
    private ListView downloadList;
    @TAInjectView(id = R.id.btn_add)
    private Button addButton;
    private DownloadListAdapter downloadListAdapter;
    private int urlIndex = 0;
    //	private IDownloadService downloadService;
    ServiceConnection serviceConnection = new ServiceConnection()
    {

        @Override
        public void onServiceDisconnected(ComponentName name)
        {
            // TODO Auto-generated method stub

        }

        @Override
        public void onServiceConnected(ComponentName name, IBinder service)
        {
            // TODO Auto-generated method stub
            //			downloadService = (IDownloadService) service;
        }
    };
    private DownloadManager downloadManager;

    @Override
    protected void onAfterOnCreate(Bundle savedInstanceState)
    {
        // TODO Auto-generated method stub
        super.onAfterOnCreate(savedInstanceState);
        setTitle(R.string.thinkandroid_download_title);
        downloadManager = DownloadManager.getDownloadManager();
        setContentView(R.layout.thinkandroidmultithreaddownload);
    }

    @Override
    protected void onAfterSetContentView()
    {
        // TODO Auto-generated method stub
        super.onAfterSetContentView();
        downloadListAdapter = new DownloadListAdapter(this, downloadList);

        downloadList.setAdapter(downloadListAdapter);

        addButton.setOnClickListener(new View.OnClickListener()
        {

            @Override
            public void onClick(View v)
            {

                downloadManager.addHandler((DownloadUrls.url[urlIndex]));
                downloadListAdapter.addItem((DownloadUrls.url[urlIndex]));
                urlIndex++;
                if (urlIndex >= DownloadUrls.url.length)
                {
                    urlIndex = 0;
                }

            }
        });

    }

    @Override
    protected void onDestroy()
    {

        super.onDestroy(); // 注意先后
    }
}