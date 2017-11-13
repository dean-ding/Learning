package com.dean;

import android.Manifest;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.PermissionChecker;
import android.support.v7.app.AppCompatActivity;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity
{

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        String permissions[] = {Manifest.permission.RECEIVE_SMS, Manifest.permission.RECORD_AUDIO};
        boolean denied = checkPermission(permissions);
        if (denied)
        {
            ActivityCompat.requestPermissions(this, permissions, 0);
        }
        else
        {
            Toast.makeText(this, "已申请到权限", Toast.LENGTH_SHORT).show();
        }
    }

    private boolean checkPermission(String... permissions)
    {
        for (String permission : permissions)
        {
            int state = PermissionChecker.checkSelfPermission(this, permission);
            if (state == PermissionChecker.PERMISSION_DENIED)
            {
                return true;
            }
        }
        return false;
    }

}
