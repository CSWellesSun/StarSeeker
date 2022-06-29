package com.huawei.audiodevicekit.starseeker.model;

import android.content.Context;

import com.huawei.audiodevicekit.mvp.model.Model;

public interface BtModel extends Model {
    interface Callback {

    }

    void initBluetooth(Context context);
}
