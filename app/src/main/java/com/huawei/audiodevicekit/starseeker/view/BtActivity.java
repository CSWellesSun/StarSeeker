package com.huawei.audiodevicekit.starseeker.view;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.media.Image;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.fragment.app.Fragment;

import com.huawei.audiobluetooth.layer.protocol.mbb.DeviceInfo;
import com.huawei.audiodevicekit.R;
import com.huawei.audiodevicekit.bluetoothsample.contract.SampleBtContract;
import com.huawei.audiodevicekit.bluetoothsample.view.SampleBtActivity;
import com.huawei.audiodevicekit.mvp.view.support.BaseAppCompatActivity;
import com.huawei.audiodevicekit.starseeker.contract.BtContract;
import com.huawei.audiodevicekit.starseeker.presenter.BtPresenter;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class BtActivity
    extends BaseAppCompatActivity<BtContract.Presenter, BtContract.View>
    implements BtContract.View {
    private ImageView btnConnect;

    private String mMac;

    private TextView connectStatus;

    private boolean connectStatusFlag = false;

    private Set<String> deviceMacSet;

    private Set<String> glassMacSet;

    @Override
    public Context getContext() { return this; }

    @Override
    public BtContract.Presenter createPresenter() { return new BtPresenter(); }

    @Override
    public BtContract.View getUiImplement() {
        return this;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        CheckGlassConnection();
    }

    public void CheckGlassConnection() {
        // 检查是否连接蓝牙
        boolean res = CheckCurrentBlueTooth();
        if (res) {
            // 检查是否连接上华为眼镜
            getPresenter().search();
            for (String mac : deviceMacSet) {
                for (String glassMac : glassMacSet) {
                    if (mac.equals(glassMac)) {
                        mMac = mac;
                        connectStatusFlag = true;
                        runOnUiThread(() -> connectStatus.setText("已连接华为眼镜"));
                        return;
                    }
                }
            }
            runOnUiThread(() -> connectStatus.setText("未连接华为眼镜"));
        } else {
            runOnUiThread(() -> connectStatus.setText("未连接蓝牙"));
        }
    }

    protected boolean CheckCurrentBlueTooth() {
        deviceMacSet.clear();
        boolean flag = false;
        BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (bluetoothAdapter == null) {
            return false;
        }
        Set<BluetoothDevice> pairedDevices = bluetoothAdapter.getBondedDevices();
        if (pairedDevices.size() > 0) {
            // There are paired devices. Get the name and address of each paired device.
            Method isConnectedMethod = null;
            boolean isConnected = false;
            for (BluetoothDevice device : pairedDevices) {
                try {
                    isConnectedMethod = BluetoothDevice.class.getDeclaredMethod("isConnected", (Class[]) null);
                    isConnectedMethod.setAccessible(true);
                    isConnected = (boolean) isConnectedMethod.invoke(device, (Object[]) null);
                    if (isConnected) {
                        deviceMacSet.add(device.getAddress());
                        flag = true;
                    }
                } catch (NoSuchMethodException e) {
                    isConnected = false;
                } catch (IllegalAccessException e) {
                    isConnected = false;
                } catch (InvocationTargetException e) {
                    isConnected = false;
                }
            }
        }
        return flag;
    }

    @Override
    protected int getResId() {
        return R.layout.m_activity_main;
    }

    @Override
    protected void initView() {
        btnConnect = (ImageView) findViewById(R.id.glass_connect);
        connectStatus = (TextView) findViewById(R.id.m_connect_status);

        deviceMacSet = new HashSet();
        glassMacSet = new HashSet<>();
    }

    @Override
    protected void initData() {
        getPresenter().initBluetooth(this);
    }

    @Override
    protected void initTabBar() {

    }

    @Override
    protected void setOnclick() {
        super.setOnclick();
        btnConnect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                CheckGlassConnection();
            }
        });
    }

    // 从P来的回调函数
    @Override
    public void onConnectStateChanged(String stateInfo) {
        if (connectStatus != null) {
            runOnUiThread(() -> connectStatus.setText(stateInfo));
        }
    }

    @Override
    public void onDeviceFound(String mac) {
        glassMacSet.add(mac);
    }

    @Override
    public void onError(String errorMsg) {
        runOnUiThread(
                () -> Toast.makeText(BtActivity.this, errorMsg, Toast.LENGTH_LONG).show());
    }

    @Override
    public void onCheckGlassConnect(String mac, boolean res) {
        if (res) {
            mMac = mac;
            runOnUiThread(() -> connectStatus.setText("当前已连接华为眼镜"));
            connectStatusFlag = true;
        } else if (connectStatusFlag == false) {
            runOnUiThread(() -> connectStatus.setText("当前未连接华为眼镜"));
        }
    }
}
