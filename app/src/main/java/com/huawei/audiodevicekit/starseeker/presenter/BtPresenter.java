package com.huawei.audiodevicekit.starseeker.presenter;

import android.content.Context;

import com.huawei.audiobluetooth.utils.LogUtils;
import com.huawei.audiodevicekit.bluetoothsample.model.SampleBtModel;
import com.huawei.audiodevicekit.bluetoothsample.model.SampleBtRepository;
import com.huawei.audiodevicekit.mvp.impl.ABaseModelPresenter;
import com.huawei.audiodevicekit.starseeker.contract.BtContract;
import com.huawei.audiodevicekit.starseeker.model.BtModel;
import com.huawei.audiodevicekit.starseeker.model.BtRepository;

public class BtPresenter extends ABaseModelPresenter<BtContract.View, BtModel>
    implements BtContract.Presenter, BtModel.Callback {

    private static final String TAG = "BtPresenter";

    @Override
    public BtModel createModel() {
        return new BtRepository(this);
    }

    /**
     * override BtContract.Presenter.initBluetooth
     * @param context
     */
    @Override
    public void initBluetooth(Context context) {
        LogUtils.i(TAG, "initBluetooth");
        if (!isUiDestroy()) {
            getModel().initBluetooth(context);
        }
    }

//    public void test_connection() {
//        getModel().test_connection();
//    }

}
