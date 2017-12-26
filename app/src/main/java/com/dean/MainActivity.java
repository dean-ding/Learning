package com.dean;

import android.Manifest;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.PermissionChecker;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Toast;

import com.dean.ad.AdActivity;
import com.dean.constraint.ConstraintLayoutActivity;
import com.dean.server.ServerActivity;
import com.dean.swipbacklayout.TestSwipeBackActivity;

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

        findViewById(R.id.ad_button).setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                startActivity(new Intent(MainActivity.this, AdActivity.class));
            }
        });

        findViewById(R.id.constraint_button).setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                startActivity(new Intent(MainActivity.this, ConstraintLayoutActivity.class));
            }
        });
        findViewById(R.id.server_button).setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                startActivity(new Intent(MainActivity.this, ServerActivity.class));
            }
        });
        findViewById(R.id.swipback_button).setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                startActivity(new Intent(MainActivity.this, TestSwipeBackActivity.class));
            }
        });
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
