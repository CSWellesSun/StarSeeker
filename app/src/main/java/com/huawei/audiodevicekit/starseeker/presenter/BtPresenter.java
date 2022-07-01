package com.huawei.audiodevicekit.starseeker.presenter;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.content.Intent;
import android.widget.Toast;

import com.huawei.audiobluetooth.constant.ConnectState;
import com.huawei.audiobluetooth.layer.protocol.mbb.DeviceInfo;
import com.huawei.audiobluetooth.utils.LogUtils;
import com.huawei.audiodevicekit.R;
import com.huawei.audiodevicekit.bluetoothsample.model.SampleBtModel;
import com.huawei.audiodevicekit.bluetoothsample.model.SampleBtRepository;
import com.huawei.audiodevicekit.mvp.impl.ABaseModelPresenter;
import com.huawei.audiodevicekit.starseeker.contract.BtContract;
import com.huawei.audiodevicekit.starseeker.model.BtModel;
import com.huawei.audiodevicekit.starseeker.model.BtRepository;

import java.util.Set;

public class BtPresenter extends ABaseModelPresenter<BtContract.View, BtModel>
    implements BtContract.Presenter, BtModel.Callback {

    private static final String TAG = "BtPresenter";

    @Override
    public BtModel createModel() {
        return new BtRepository(this);
    }

    // 从V层来的函数
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

    @Override
    public void search() {
        LogUtils.i(TAG, "initSearch");
        if (!isUiDestroy()) {
            getModel().search();
        }
    }

    @Override
    public void connect(String mac) {
        if (!isUiDestroy()) {
            getModel().connect(mac);
        }
    }

    @Override
    public void checkGlassConnect(Set<String> macs) {
        if (!isUiDestroy()) {
            getModel().checkGlassConnect(macs);
        }
    }

    // 从M层来的回调函数
    @Override
    public void onDeviceFound(String mac) {
        if (!isUiDestroy()) {
            getUi().onDeviceFound(mac);
        }
    }

    @Override
    public void onCheckGlassConnect(String mac, boolean res) {
        if (!isUiDestroy()) {
            getUi().onCheckGlassConnect(mac, res);
        }
    }

    @Override
    public void onConnectStateChanged(BluetoothDevice device, int state) {
        String stateInfo;
        switch (state) {
            case ConnectState.STATE_UNINITIALIZED:
                stateInfo = getUi().getContext().getResources().getString(R.string.not_initialized);
                break;
            case ConnectState.STATE_CONNECTING:
                stateInfo = getUi().getContext().getResources().getString(R.string.connecting);
                break;
            case ConnectState.STATE_CONNECTED:
                stateInfo = getUi().getContext().getResources().getString(R.string.connected);
                break;
            case ConnectState.STATE_DATA_READY:
                stateInfo = getUi().getContext().getResources().getString(R.string.data_channel_ready);
//                registerListener(device.getAddress());
                break;
            case ConnectState.STATE_DISCONNECTED:
                stateInfo = getUi().getContext().getResources().getString(R.string.disconnected);
//                unregisterListener(device.getAddress());
                break;
            default:
                stateInfo = getUi().getContext().getResources().getString(R.string.unknown) + state;
                break;
        }
        LogUtils.i(TAG,
                "onConnectStateChanged  state = " + state + "," + stateInfo + "," + ConnectState
                        .toString(state));
        if (!isUiDestroy()) {
            getUi().onConnectStateChanged(state, device.getAddress());
//            getUi().onDeviceChanged(device);
        }
    }
}
