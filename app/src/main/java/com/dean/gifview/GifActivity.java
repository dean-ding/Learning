package com.dean.gifview;

import android.Manifest;
import android.app.Activity;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.app.ActivityCompat;

import com.dean.R;
import com.dean.swipback.ui.SwipeBackActivity;

import java.io.File;
import java.io.IOException;

import butterknife.BindView;
import butterknife.ButterKnife;
import pl.droidsonroids.gif.GifDrawable;
import pl.droidsonroids.gif.GifImageView;

/**
 * Created: tvt on 18/1/10 15:41
 */
public class GifActivity extends SwipeBackActivity
{
    @BindView(R.id.gif_view)
    GifImageView mGifView;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.gif_layout);
        ButterKnife.bind(this);

        verifyStoragePermissions(this);
        //mGifView = (GifView) findViewById(R.id.gif_view);

        try
        {
            File gifFile = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator + "SuperLivePlus" + File.separator + "Ads" + File.separator +
                    "test.gif");
            GifDrawable gifFromFile = new GifDrawable(gifFile);
            mGifView.setImageDrawable(gifFromFile);
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
    }

    // Storage Permissions
    private static final int REQUEST_EXTERNAL_STORAGE = 1;
    private static String[] PERMISSIONS_STORAGE = {Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE};

    /**
     * Checks if the app has permission to write to device storage
     * <p>
     * If the app does not has permission then the user will be prompted to
     * grant permissions
     *
     * @param activity
     */
    public static void verifyStoragePermissions(Activity activity)
    {
        // Check if we have write permission
        int permission = ActivityCompat.checkSelfPermission(activity, Manifest.permission.WRITE_EXTERNAL_STORAGE);
        if (permission != PackageManager.PERMISSION_GRANTED)
        {
            // We don't have permission so prompt the user
            ActivityCompat.requestPermissions(activity, PERMISSIONS_STORAGE, REQUEST_EXTERNAL_STORAGE);
        }
    }


    @Override
    protected void onDestroy()
    {
        super.onDestroy();
    }
}
