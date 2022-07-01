package com.huawei.audiodevicekit;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.baidu.speech.EventListener;
import com.baidu.speech.EventManager;
import com.baidu.speech.EventManagerFactory;
import com.baidu.speech.asr.SpeechConstant;

import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class VoiceRecognition extends AppCompatActivity implements EventListener {
    protected EditText txtResult;//识别结果
    protected Button startBtn;//开始识别，持续一定时间不说话会自动停止，需要再次打开
    protected Button stopBtn;//停止识别,立即停止，直接输出已经识别的内容
    protected Button jumpBtn;

    private EventManager asr;//语音识别核心库


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_voicerecognition);

        initView();
        initPermission();

        //初始化EventManager对象
        asr = EventManagerFactory.create(this, "asr");
        //注册自己的输出事件类
        asr.registerListener(this); //  EventListener 中 onEvent方法

    }

    /**
     * android 6.0 以上需要动态申请权限
     */
    private void initPermission() {
        String permissions[] = {Manifest.permission.RECORD_AUDIO,
                Manifest.permission.ACCESS_NETWORK_STATE,
                Manifest.permission.INTERNET,
                Manifest.permission.WRITE_EXTERNAL_STORAGE
        };

        ArrayList<String> toApplyList = new ArrayList<String>();

        for (String perm : permissions) {
            if (PackageManager.PERMISSION_GRANTED != ContextCompat.checkSelfPermission(this, perm)) {
                toApplyList.add(perm);
                //进入到这里代表没有权限
            }
        }
        String tmpList[] = new String[toApplyList.size()];
        if (!toApplyList.isEmpty()) {
            ActivityCompat.requestPermissions(this, toApplyList.toArray(tmpList), 123);
        }

    }

    /**
     * 权限申请回调，可以作进一步处理
     * @param requestCode
     * @param permissions
     * @param grantResults
     */
//    @Override
//    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
//        // 此处为android 6.0以上动态授权的回调，用户自行实现。
//    }

    /**
     * 初始化控件
     */
    private void initView() {
        txtResult = (EditText) findViewById(R.id.edittext);
        startBtn = (Button) findViewById(R.id.btn_start);
        stopBtn = (Button) findViewById(R.id.btn_stop);
        jumpBtn = (Button) findViewById(R.id.btn_jump1);
        startBtn.setOnClickListener(new View.OnClickListener() {//点击开始按钮
            @Override
            public void onClick(View v) {
                asr.send(SpeechConstant.ASR_START, null, null, 0, 0);
            }
        });
        stopBtn.setOnClickListener(new View.OnClickListener() {//点击停止按钮
            @Override
            public void onClick(View v) {
                asr.send(SpeechConstant.ASR_STOP, null, null, 0, 0);
            }
        });
        jumpBtn.setOnClickListener(new View.OnClickListener() {//点击停止按钮
            @Override
            public void onClick(View v) {
                Intent intent = new Intent();
                intent.setClass(VoiceRecognition.this, Broadcast.class);
                startActivity(intent);
            }
        });
    }

    /**
     * 自定义输出事件类 EventListener 回调方法
     */
    @Override
    public void onEvent(String name, String params, byte[] data, int offset, int length) {

        if (name.equals(SpeechConstant.CALLBACK_EVENT_ASR_PARTIAL)) {
            // 识别相关的结果都在这里
            if (params == null || params.isEmpty()) {
                return;
            }
            if (params.contains("\"final_result\"")) {
                // 一句话的最终识别结果
                String regrex = "\\[(.*?),";  //使用正则表达式抽取我们需要的内容
                Pattern pattern = Pattern.compile(regrex);
                Matcher matcher = pattern.matcher(params);
                if (matcher.find()) {
                    int a  = matcher.group(0).indexOf("[");
                    int b  = matcher.group(0).indexOf(",");
                    txtResult.setText(matcher.group(0).substring(a+2,b-3));
                }
            }
        }

    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        //发送取消事件
        asr.send(SpeechConstant.ASR_CANCEL, "{}", null, 0, 0);
        //退出事件管理器
        // 必须与registerListener成对出现，否则可能造成内存泄露
        asr.unregisterListener(this);
    }

}