package com.huawei.audiodevicekit.starseeker.model;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.widget.Toast;

import com.huawei.audiobluetooth.api.AudioBluetoothApi;
import com.huawei.audiobluetooth.layer.bluetooth.BluetoothManager;
import com.huawei.audiobluetooth.layer.bluetooth.DiscoveryHelper;
import com.huawei.audiobluetooth.layer.bluetooth.IInitResultCallBack;
import com.huawei.audiobluetooth.layer.protocol.mbb.DeviceInfo;
import com.huawei.audiobluetooth.utils.LogUtils;

import java.util.Set;

public class BtRepository implements BtModel {
    private static final String TAG = "BtRepository";

    private Callback mCallback;

    public BtRepository(Callback callback) {
        mCallback = callback;
    }

    @Override
    public void initBluetooth(Context context) {
        try {
            // 初始化眼镜
            AudioBluetoothApi.getInstance().init(context, new IInitResultCallBack() {
                @Override
                public void onResult(boolean result) {
                    LogUtils.i(TAG, "onResult result = " + result);
                }

                @Override
                public void onFinish() {
                    LogUtils.i(TAG, "onFinish");
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void search() {
        // search
        AudioBluetoothApi.getInstance().stopSearch();
        AudioBluetoothApi.getInstance().searchDevice(new DiscoveryHelper.FoundCallback() {
            @Override
            public void onFound(DeviceInfo deviceInfo) {
                LogUtils.d(TAG, "startSearch Found device: " + deviceInfo);
                mCallback.onDeviceFound(deviceInfo.getDeviceBtMac());
            }

            @Override
            public void onFinish() {
                LogUtils.i(TAG, "searchDevice onFinish");
            }
        });
    }

    @Override
    public void connect(String mac) {
        AudioBluetoothApi.getInstance().connect(mac, state -> {
            LogUtils.i(TAG, "onConnectStateChanged state = " + state);
            BluetoothDevice device = BluetoothManager.getInstance().getBtDevice(mac);
            mCallback.onConnectStateChanged(device, state);
        });
    }

    @Override
    public void checkGlassConnect(Set<String> macs) {
        boolean flag = false;
        for (String mac : macs) {
            AudioBluetoothApi.getInstance().connect(mac, state -> {
                        LogUtils.i(TAG, "onConnectStateChanged state = " + state);
                        BluetoothDevice device = BluetoothManager.getInstance().getBtDevice(mac);
                        if (state == 3) {
                            mCallback.onCheckGlassConnect(mac, true);
                        }
            });
        }
        mCallback.onCheckGlassConnect("", false);
    }
}
