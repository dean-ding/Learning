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
import com.dean.bluetooth.BlueToothActivity;
import com.dean.cat.activity.SplashActivity;
import com.dean.constraint.ConstraintLayoutActivity;
import com.dean.fresco.FrescoActivity;
import com.dean.gifview.GifActivity;
import com.dean.keystore.EncryUtils;
import com.dean.live.LiveActivity;
import com.dean.rxjava.CountDownActivity;
import com.dean.server.ServerActivity;
import com.dean.swipbacklayout.TestSwipeBackActivity;
import com.dean.viewdrag.ViewDragActivity;
import com.dean.xml.PushTransMessageInfo;
import com.orhanobut.logger.Logger;

import org.xml.sax.SAXException;

import java.io.ByteArrayInputStream;
import java.io.IOException;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

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
        findViewById(R.id.viewdrag_button).setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                startActivity(new Intent(MainActivity.this, ViewDragActivity.class));
            }
        });
        findViewById(R.id.live_button).setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                startActivity(new Intent(MainActivity.this, LiveActivity.class));
            }
        });
        findViewById(R.id.count_button).setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                startActivity(new Intent(MainActivity.this, CountDownActivity.class));
            }
        });
        findViewById(R.id.gif_button).setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                startActivity(new Intent(MainActivity.this, GifActivity.class));
            }
        });
        findViewById(R.id.fresco_button).setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                startActivity(new Intent(MainActivity.this, FrescoActivity.class));
            }
        });
        findViewById(R.id.think_android_button).setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                startActivity(new Intent(MainActivity.this, SplashActivity.class));
            }
        });
        findViewById(R.id.bluetooth_button).setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                startActivity(new Intent(MainActivity.this, BlueToothActivity.class));
            }
        });
        ParseMessage();

        Logger.i("test time");
        String keyAlias = "keyAlias";
        String encryptWord = "{device:{sn:n021c1212121,username:admin,password:123456}}{device:{sn:n021c1212121,username:admin,password:123456}}{device:{sn:n021c1212121," +
                "username:admin,password:123456}}{device:{sn:n021c1212121,username:admin,password:123456}}{device:{sn:n021c1212121,username:admin," +
                "password:123456}}{device:{sn:n021c1212121,username:admin,password:123456}}{device:{sn:n021c1212121,username:admin,password:123456}}{device:{sn:n021c1212121," +
                "username:admin,password:123456}}{device:{sn:n021c1212121,username:admin,password:123456}}{device:{sn:n021c1212121,username:admin," +
                "password:123456}}{device:{sn:n021c1212121,username:admin,password:123456}}{device:{sn:n021c1212121,username:admin,password:123456}}{device:{sn:n021c1212121," +
                "username:admin,password:123456}}{device:{sn:n021c1212121,username:admin,password:123456}}{device:{sn:n021c1212121,username:admin," +
                "password:123456}}{device:{sn:n021c1212121,username:admin,password:123456}}{device:{sn:n021c1212121,username:admin,password:123456}}{device:{sn:n021c1212121," +
                "username:admin,password:123456}}{device:{sn:n021c1212121,username:admin,password:123456}}{device:{sn:n021c1212121,username:admin," +
                "password:123456}}{device:{sn:n021c1212121,username:admin,password:123456}}{device:{sn:n021c1212121,username:admin,password:123456}}{device:{sn:n021c1212121," +
                "username:admin,password:123456}}{device:{sn:n021c1212121,username:admin,password:123456}}{device:{sn:n021c1212121,username:admin," +
                "password:123456}}{device:{sn:n021c1212121,username:admin,password:123456}}{device:{sn:n021c1212121,username:admin,password:123456}}{device:{sn:n021c1212121," +
                "username:admin,password:123456}}{device:{sn:n021c1212121,username:admin,password:123456}}{device:{sn:n021c1212121,username:admin," +
                "password:123456}}{device:{sn:n021c1212121,username:admin,password:123456}}{device:{sn:n021c1212121,username:admin,password:123456}}{device:{sn:n021c1212121," +
                "username:admin,password:123456}}{device:{sn:n021c1212121,username:admin,password:123456}}{device:{sn:n021c1212121,username:admin,password:123456}}";
        String encryptRet = EncryUtils.getInstance().encryptString(encryptWord, keyAlias);
        String decryptRet = EncryUtils.getInstance().decryptString(encryptRet, keyAlias);

        System.out.println(encryptRet + "-->" + decryptRet);
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

    private void ParseMessage()
    {
        String xml = "<?xml version=\"1.0\" encoding=\"utf-8\"?><Requests><Request><MsgType>SendTPNSTransPushMsg</MsgType><MsgID>b8c6847459b06b44892651d961e6a8ba</MsgID><Params" +
                "><PushMsg" +
                "><PushMsgType>ADMsg" +
                "</PushMsgType" +
                "><PushMsgSubType/><TokenInfo><AppIDStr>AND_M_PH_SuperLivePlus</AppIDStr><AppUserToken>FPC_000000000000000000000000000000000000000000000000000000000001</AppUserToken" +
                "></TokenInfo><NotifyInfo><Title>testTitle</Title><Msg>testMsg</Msg></NotifyInfo><MsgContent><![CDATA[{\"id\":1,\"url\":\"http://ad" +
                ".html\"}]]></MsgContent></PushMsg></Params></Request></Requests>\n";
        SAXParserFactory factory = SAXParserFactory.newInstance();
        SAXParser mSaxParser = null;
        try
        {
            PushTransMessageInfo info = new PushTransMessageInfo();
            mSaxParser = factory.newSAXParser();
            ByteArrayInputStream inputStream = new ByteArrayInputStream(xml.getBytes());
            mSaxParser.parse(inputStream, info);
        }
        catch (ParserConfigurationException e)
        {
            e.printStackTrace();
        }
        catch (SAXException e)
        {
            e.printStackTrace();
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
    }

}
