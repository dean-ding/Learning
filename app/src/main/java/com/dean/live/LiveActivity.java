package com.dean.live;

import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.VideoView;

import com.dean.R;
import com.dean.swipback.ui.SwipeBackActivity;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * Created: tvt on 17/12/27 14:01
 */
public class LiveActivity extends SwipeBackActivity
{
    @BindView(R.id.live_url)
    EditText mUrlView;
    @BindView(R.id.live_video)
    VideoView mVideoView;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.live_layout);
        ButterKnife.bind(this);

        mVideoView.setVideoURI(Uri.parse(mUrlView.getText().toString()));
        mVideoView.start();
    }

    @OnClick(R.id.live_play)
    public void play(View v)
    {
        System.out.println(mUrlView.getText().toString());
        mVideoView.setVideoURI(Uri.parse(mUrlView.getText().toString()));
        mVideoView.start();
    }

    @Override
    protected void onDestroy()
    {
        super.onDestroy();
        mVideoView.stopPlayback();
    }
}
