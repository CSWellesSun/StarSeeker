/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2020-2021. All rights reserved.
 */

package com.huawei.audiodevicekit;

import android.app.Application;

import androidx.multidex.MultiDex;

import com.huawei.audiobluetooth.utils.ContextUtils;
import com.huawei.audiobluetooth.utils.LogUtils;

import android.app.Application;

import com.iflytek.cloud.SpeechConstant;
import com.iflytek.cloud.SpeechUtility;

/**
 * 应用入口
 *
 * @author zWX918012
 * @since 2020/10/21 15:57
 */
public class MyApplication extends Application {
    private static final String TAG = "MyApplication";

    @Override
    public void onCreate() {
        SpeechUtility.createUtility(MyApplication.this, SpeechConstant.APPID +"=f46c878f");
        super.onCreate();
        init();
    }

    /**
     * 应用启动初始化
     */
    private void init() {
        LogUtils.i(TAG, "Application init");
        MultiDex.install(this);
        ContextUtils.init(this);
    }
}
