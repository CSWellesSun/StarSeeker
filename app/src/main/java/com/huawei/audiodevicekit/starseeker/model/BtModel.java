package com.huawei.audiodevicekit.starseeker.model;

import android.content.Context;

import com.huawei.audiobluetooth.layer.protocol.mbb.DeviceInfo;
import com.huawei.audiodevicekit.mvp.model.Model;

import java.util.Set;

public interface BtModel extends Model {
    interface Callback {
        void onDeviceFound(String mac);

        void onCheckGlassConnect(String mac, boolean res);
    }

    void initBluetooth(Context context);

    void search();

    void checkGlassConnect(Set<String> macs);
}
