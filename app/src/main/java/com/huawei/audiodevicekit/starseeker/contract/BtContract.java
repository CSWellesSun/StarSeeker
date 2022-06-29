package com.huawei.audiodevicekit.starseeker.contract;

import android.content.Context;

import com.huawei.audiodevicekit.mvp.impl.ABaseUi;
import com.huawei.audiodevicekit.mvp.presenter.IPresenter;

public interface BtContract {
    interface View extends ABaseUi {
        /**
         * 当连接状态变化时调用
         *
         * @param stateInfo 状态信息
         */
        void onConnectStateChanged(String stateInfo);
    }

    interface Presenter extends IPresenter {
        void initBluetooth(Context context);
    }
}
