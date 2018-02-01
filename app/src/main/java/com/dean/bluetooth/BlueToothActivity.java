package com.dean.bluetooth;

import android.annotation.SuppressLint;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.widget.TextView;

import com.dean.R;
import com.dean.swipback.ui.SwipeBackActivity;
import com.orhanobut.logger.Logger;

/**
 * Created: tvt on 18/1/22 17:10
 */
public class BlueToothActivity extends SwipeBackActivity
{
    private final int REQUEST_ENABLE_BT = 0x100;
    private BluetoothAdapter mBluetoothAdapter;
    private TextView mTextView = null;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.bluetooth_layout);

        mTextView = (TextView) findViewById(R.id.blue_tooth_device);

        initBlueTooth();
    }

    private void initBlueTooth()
    {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2)
        {
            @SuppressLint("WrongConstant") final BluetoothManager bluetoothManager = (BluetoothManager) getApplicationContext().getSystemService(Context.BLUETOOTH_SERVICE);
            if (bluetoothManager != null)
            {
                mBluetoothAdapter = bluetoothManager.getAdapter();
            }
            if (mBluetoothAdapter == null || !mBluetoothAdapter.isEnabled())
            {
                Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
                return;
            }
            mBluetoothAdapter.startLeScan(mLeScanCallback);
        }
    }

    private BluetoothAdapter.LeScanCallback mLeScanCallback = new BluetoothAdapter.LeScanCallback()
    {
        @Override
        public void onLeScan(BluetoothDevice device, int rssi, byte[] scanRecord)
        {
            Logger.i(device.getName() + "--->" + device.getAddress());

            mTextView.setText(String.format("%s:%s", device.getName(), device.getAddress()));
        }
    };

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == REQUEST_ENABLE_BT)
        {
            initBlueTooth();
        }
    }

    @Override
    protected void onDestroy()
    {
        super.onDestroy();
        if (mBluetoothAdapter != null)
        {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2)
            {
                mBluetoothAdapter.stopLeScan(mLeScanCallback);
            }
        }
    }
}
