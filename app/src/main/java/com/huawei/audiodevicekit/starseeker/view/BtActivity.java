package com.huawei.audiodevicekit.starseeker.view;

import android.content.Context;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;

import androidx.fragment.app.Fragment;

import com.huawei.audiodevicekit.R;
import com.huawei.audiodevicekit.bluetoothsample.contract.SampleBtContract;
import com.huawei.audiodevicekit.mvp.view.support.BaseAppCompatActivity;
import com.huawei.audiodevicekit.starseeker.contract.BtContract;
import com.huawei.audiodevicekit.starseeker.presenter.BtPresenter;

import java.util.ArrayList;
import java.util.List;

public class BtActivity
    extends BaseAppCompatActivity<BtContract.Presenter, BtContract.View>
    implements BtContract.View {
    private Button btnConnect;

    private String mMac;

    private TextView connectStatus;

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
    }

    @Override
    protected int getResId() {
        return R.layout.m_activity_main;
    }

    @Override
    protected void initView() {
//        btnConnect = findViewById(R.id.m_button_connect);
//        connectStatus = findViewById(R.id.m_connect_status);

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
//        btnConnect.setOnClickListener(v -> getPresenter().test_connection());
    }

    @Override
    public void onConnectStateChanged(String stateInfo) {
        if (connectStatus != null) {
            runOnUiThread(() -> connectStatus.setText(stateInfo));
        }
    }
}
