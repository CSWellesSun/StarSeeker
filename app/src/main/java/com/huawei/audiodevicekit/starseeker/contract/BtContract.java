package com.huawei.audiodevicekit.starseeker.contract;

import android.content.Context;

import com.huawei.audiobluetooth.layer.protocol.mbb.DeviceInfo;
import com.huawei.audiodevicekit.mvp.impl.ABaseUi;
import com.huawei.audiodevicekit.mvp.presenter.IPresenter;

import java.util.Set;

public interface BtContract {
    interface View extends ABaseUi {
        /**
         * 当连接状态变化时调用
         *
         * @param stateInfo 状态信息
         */
        void onConnectStateChanged(int stateInfo, String mac);

        void onDeviceFound(String mac);

        void onError(String errorMsg);

        void onCheckGlassConnect(String mac, boolean res);
    }

    interface Presenter extends IPresenter {
        void initBluetooth(Context context);

        void search();

        void connect(String mac);

        void checkGlassConnect(Set<String> macs);
    }
}
