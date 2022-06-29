package com.huawei.audiodevicekit.starseeker.model;

import android.content.Context;

import com.huawei.audiobluetooth.api.AudioBluetoothApi;
import com.huawei.audiobluetooth.layer.bluetooth.IInitResultCallBack;
import com.huawei.audiobluetooth.utils.LogUtils;

public class BtRepository implements BtModel {
    private static final String TAG = "BtRepository";

    private Callback mCallback;

    public BtRepository(Callback callback) {
        mCallback = callback;
    }

    @Override
    public void initBluetooth(Context context) {
        try {
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
}
