package com.huawei.audiodevicekit.starseeker.view;

import android.Manifest;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothAdapter;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.core.app.ActivityCompat;

import com.huawei.audiodevicekit.R;
import com.huawei.audiodevicekit.mvp.view.support.BaseAppCompatActivity;
import com.huawei.audiodevicekit.starseeker.contract.BtContract;
import com.huawei.audiodevicekit.starseeker.presenter.BtPresenter;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashSet;
import java.util.Set;

public class BtActivity
    extends BaseAppCompatActivity<BtContract.Presenter, BtContract.View>
    implements BtContract.View {
    private ImageView btnStar;
    private ImageView btnSearch;

    private String mMac;

    private TextView connectStatus;

    private EditText input;

    private boolean connectStatusFlag = false;

    private Set<String> deviceMacSet;

    private Set<String> glassMacSet;

    // 蓝牙状态监听
    private BluetoothStateBroadcastReceive mReceive;
    private IntentFilter bluetoothIntentFilter;

    /**
     * 位置权限
     */
    private String[] locationPermission = {"android.permission.ACCESS_COARSE_LOCATION",
            "android.permission.ACCESS_FINE_LOCATION"};

    private int LOCATION_PERMISSION_REQUEST_CODE = 188;

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
        registerBluetoothReceiver();
    }

    public void CheckGlassConnection() {
        // 检查是否连接蓝牙
        boolean res = CheckCurrentBlueTooth();
        if (res) {
            // 检查是否连接上华为眼镜
            glassMacSet.clear();
            // 检查位置权限
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION)
                    != PackageManager.PERMISSION_GRANTED ||
                    ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                            != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat
                        .requestPermissions(this, locationPermission, LOCATION_PERMISSION_REQUEST_CODE);
            }
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
            connectStatusFlag = false;
            runOnUiThread(() -> connectStatus.setText("未连接华为眼镜"));
        } else {
            connectStatusFlag = false;
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
        connectStatus = (TextView) findViewById(R.id.m_connect_status);
        btnStar =(ImageView)findViewById(R.id.recommend_4);
        btnSearch =(ImageView) findViewById(R.id.search_star);
        input = (EditText) findViewById(R.id.search_edit_text);

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
        // 对Edit Text的监听
        input.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int i, KeyEvent keyEvent) {
                Toast.makeText(getApplicationContext(),"输入的为:"+input.getText().toString(),Toast.LENGTH_LONG).show();
                return false;}});
        // 对北极星语音播报的监听
        btnStar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent();
                intent.setClass(BtActivity.this, Broadcast.class);
                startActivity(intent);
            }
        });
        // 对语音查找的监听
        btnSearch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent();
                intent.setClass(BtActivity.this, VoiceRecognition.class);
                startActivity(intent);
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
    public void onDestroy() {
        super.onDestroy();
        unregisterBluetoothReceiver();
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

    // 蓝牙监听
    class BluetoothStateBroadcastReceive extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            CheckGlassConnection();
//            String action = intent.getAction();
//            BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
//            switch (action){
//                case BluetoothDevice.ACTION_ACL_CONNECTED:
//                    Toast.makeText(context , "蓝牙设备:" + device.getName() + "已链接", Toast.LENGTH_SHORT).show();
//                    break;
//                case BluetoothDevice.ACTION_ACL_DISCONNECTED:
//                    Toast.makeText(context , "蓝牙设备:" + device.getName() + "已断开", Toast.LENGTH_SHORT).show();
//                    break;
//                case BluetoothAdapter.ACTION_STATE_CHANGED:
//                    int blueState = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, 0);
//                    switch (blueState){
//                        case BluetoothAdapter.STATE_OFF:
//                            Toast.makeText(context , "蓝牙已关闭", Toast.LENGTH_SHORT).show();
//                            break;
//                        case BluetoothAdapter.STATE_ON:
//                            Toast.makeText(context , "蓝牙已开启"  , Toast.LENGTH_SHORT).show();
//                            break;
//                    }
//                    break;
//            }
        }
    }

    private void registerBluetoothReceiver(){
        if(mReceive == null){
            mReceive = new BluetoothStateBroadcastReceive();
        }
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(BluetoothAdapter.ACTION_STATE_CHANGED);
        intentFilter.addAction(BluetoothDevice.ACTION_ACL_CONNECTED);
        intentFilter.addAction(BluetoothDevice.ACTION_ACL_DISCONNECTED);
        intentFilter.addAction("android.bluetooth.BluetoothAdapter.STATE_OFF");
        intentFilter.addAction("android.bluetooth.BluetoothAdapter.STATE_ON");
        registerReceiver(mReceive, intentFilter);
    }

    private void unregisterBluetoothReceiver(){
        if(mReceive != null){
            unregisterReceiver(mReceive);
            mReceive = null;
        }
    }
}
