package com.huawei.audiodevicekit.starseeker.view;

import android.Manifest;
import android.annotation.SuppressLint;
import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.os.MemoryFile;
import android.os.StrictMode;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.style.BackgroundColorSpan;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.recyclerview.widget.AdapterListUpdateCallback;

import com.huawei.audiobluetooth.api.AudioBluetoothApi;
import com.huawei.audiobluetooth.api.Cmd;
import com.huawei.audiobluetooth.api.data.SensorData;
import com.huawei.audiobluetooth.api.data.SensorDataHelper;
import com.huawei.audiobluetooth.layer.bluetooth.BluetoothManager;
import com.huawei.audiobluetooth.layer.bluetooth.IInitResultCallBack;
import com.huawei.audiobluetooth.layer.data.entity.IRspListener;
import com.huawei.audiobluetooth.utils.LogUtils;
import com.huawei.audiodevicekit.R;
import com.huawei.audiodevicekit.mvp.utils.LifeCycleUtil;
import com.iflytek.cloud.ErrorCode;
import com.iflytek.cloud.InitListener;
import com.iflytek.cloud.SpeechConstant;
import com.iflytek.cloud.SpeechError;
import com.iflytek.cloud.SpeechEvent;
import com.iflytek.cloud.SpeechSynthesizer;
import com.iflytek.cloud.SynthesizerListener;
import com.iflytek.cloud.msc.util.FileUtil;
import com.iflytek.cloud.msc.util.log.DebugLog;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

public class Broadcast extends AppCompatActivity implements View.OnClickListener, Spinner.OnItemSelectedListener {

    private static final String TAG = "MainActivity";

    //输入框
    private EditText etText;

    // 语音合成对象
    private SpeechSynthesizer mTts;

    //播放的文字
    String text = "天秤座，最佳观测时机为九月二十三日到十月二十三日。它是黄道十二星座的第七个，在室女座的东南方向。可见纬度为正六十五度和负九十度之间。";

    // 默认发音人
    private String voicer = "aisjiuxu";

    // 引擎类型
    private String mEngineType = SpeechConstant.TYPE_CLOUD;


    private Vector<byte[]> container = new Vector<>();

    public int code;
    public double n;

    //内存文件
    MemoryFile memoryFile;
    //总大小
    public volatile long mTotalSize = 0;

    //发音人名称
    private static final String[] arrayName = {"讯飞小燕", "讯飞许久", "讯飞小萍", "讯飞小婧", "讯飞许小宝"};

    //发音人值
    private static final String[] arrayValue = {"xiaoyan", "aisjiuxu", "aisxping", "aisjinger", "aisbabyxu"};

    //数组适配器
    private ArrayAdapter<String> arrayAdapter;

    //语速
    public String speedValue = "50";
    //音调
    public String pitchValue = "50";
    //音量
    public String volumeValue = "50";

    // 华为眼镜的MAC地址
    private String mMac;
    // 位置
    private Location location = null;
    // 时间
    private String time;
    // 搜索标志
    private boolean searchFlag = false;
    private ImageView findEndButton;
    private TextView findEndText;

    SocketClient socketClient = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // 获得MAC地址
        mMac = getIntent().getStringExtra("Mac");
        AudioBluetoothApi.getInstance().init(this, new IInitResultCallBack() {
            @Override
            public void onResult(boolean result) {
                LogUtils.i(TAG, "onResult result = " + result);
                if (result == true) {
                    AudioBluetoothApi.getInstance().connect(mMac, state -> {
                        LogUtils.i(TAG, "onConnectStateChanged state = " + state);
                        LogUtils.e(TAG, "state:" + state);
                    });
                } else {
                    LogUtils.e(TAG, "寄");
                }
            }

            @Override
            public void onFinish() {
                LogUtils.i(TAG, "onFinish");
            }
        });

        setContentView(R.layout.activity_broadcast);

        //初始化
        initView();
        //请求权限
        requestPermissions();

        // 初始化合成对象
        mTts = SpeechSynthesizer.createSynthesizer(this, mTtsInitListener);

        StrictMode.setThreadPolicy(new StrictMode.ThreadPolicy.Builder()
                .detectDiskReads().detectDiskWrites().detectNetwork()
                .penaltyLog().build());
        StrictMode.setVmPolicy(new StrictMode.VmPolicy.Builder()
                .detectLeakedSqlLiteObjects().penaltyLog().penaltyDeath()
                .build());
    }

    /**
     * 初始化页面
     */
    private void initView() {
        findEndButton = (ImageView) findViewById(R.id.find_this_star);
        findEndText = (TextView) findViewById(R.id.find_end_text);
        findEndButton.setOnClickListener(v -> {
            if (searchFlag == false) {
                searchFlag = true;
                findEndText.setText("End Search");
                FindStar();
            } else {
                searchFlag = false;
                findEndText.setText("Find The Star");
                EndSearch();
            }
        });
        findViewById(R.id.btn_read).setOnClickListener(this);
        findViewById(R.id.find_this_star).setOnClickListener(this);
    }


    /**
     * 初始化监听。
     */
    private InitListener mTtsInitListener = new InitListener() {
        @Override
        public void onInit(int code) {
            Log.i(TAG, "InitListener init() code = " + code);
            if (code != ErrorCode.SUCCESS) {
                showTip("初始化失败,错误码：" + code);
            } else {
                showTip("初始化成功");
            }
        }
    };

    /**
     * Toast提示
     *
     * @param msg
     */
    private void showTip(String msg) {
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
    }

    /**
     * 请求权限
     */
    private void requestPermissions() {
        try {
            //Android6.0及以上版本
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                int permission = ActivityCompat.checkSelfPermission(this,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE);
                if (permission != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(this, new String[]
                            {Manifest.permission.WRITE_EXTERNAL_STORAGE,
                                    Manifest.permission.WRITE_SETTINGS,
                                    Manifest.permission.READ_EXTERNAL_STORAGE}, 0x0010);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 权限请求返回结果
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case 2://定位
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Toast.makeText(this, "同意获取定位权限", Toast.LENGTH_SHORT).show();
                    getLocationLL();
                } else {
                    Toast.makeText(this, "未同意获取定位权限", Toast.LENGTH_SHORT).show();
                }
                break;
            default:
        }
    }


    /**
     * 页面点击事件
     *
     * @param v 控件
     */
    @Override
    public void onClick(View v) {
        if (mTts == null) {
            this.showTip("创建对象失败，请确认 libmsc.so 放置正确，且有调用 createUtility 进行初始化");
            return;
        }

        switch (v.getId()) {
            case R.id.find_this_star:
                mTts.pauseSpeaking();
                LoginServer server = new LoginServer();
                server.startNetThread();
                n=10;
                if(n>200){
                    Speak("您已经找到了该星星");
                }
                else{
                    volumeValue=Math.round(n*1.0/255*100)+"0";
                    pitchValue=Math.round(n*1.0/255*100)+"0";
                    speedValue=Math.round(n*1.0/255*100)+"0";
                    String text1 ="滴";
                    voicer = "aisjinger";

                    setParam();
                    //开始合成播放
                    code = mTts.startSpeaking(text1, mTtsListener);
                    if (code != ErrorCode.SUCCESS) {
                        showTip("语音合成失败,错误码: " + code);
                    }
                }

                break;
             case R.id.btn_read://开始合成
                 //设置参数
                 volumeValue="50";
                 pitchValue="50";
                 speedValue="50";
                 voicer = "aisjiuxu";
                 setParam();
                 //开始合成播放
                 code = mTts.startSpeaking(text, mTtsListener);
                 if (code != ErrorCode.SUCCESS) {
                     showTip("语音合成失败,错误码: " + code);
                 }
                 break;
            default:
                break;
        }

    }

    /**
     * 参数设置
     *
     * @return
     */
    private void setParam() {
        // 清空参数
        mTts.setParameter(SpeechConstant.PARAMS, null);
        // 根据合成引擎设置相应参数
        if (mEngineType.equals(SpeechConstant.TYPE_CLOUD)) {
            mTts.setParameter(SpeechConstant.ENGINE_TYPE, SpeechConstant.TYPE_CLOUD);
            //支持实时音频返回，仅在synthesizeToUri条件下支持
            mTts.setParameter(SpeechConstant.TTS_DATA_NOTIFY, "1");
            // 设置在线合成发音人
            mTts.setParameter(SpeechConstant.VOICE_NAME, voicer);

            //设置合成语速
            mTts.setParameter(SpeechConstant.SPEED, speedValue);
            //设置合成音调
            mTts.setParameter(SpeechConstant.PITCH, pitchValue);
            //设置合成音量
            mTts.setParameter(SpeechConstant.VOLUME, volumeValue);
        } else {
            mTts.setParameter(SpeechConstant.ENGINE_TYPE, SpeechConstant.TYPE_LOCAL);
            mTts.setParameter(SpeechConstant.VOICE_NAME, "");
        }
        // 设置播放合成音频打断音乐播放，默认为true
        mTts.setParameter(SpeechConstant.KEY_REQUEST_FOCUS, "false");
        // 设置音频保存路径，保存音频格式支持pcm、wav
        mTts.setParameter(SpeechConstant.AUDIO_FORMAT, "pcm");
        mTts.setParameter(SpeechConstant.TTS_AUDIO_PATH, getExternalFilesDir(null) + "/msc/tts.pcm");
    }

    /**
     * 合成回调监听。
     */
    private SynthesizerListener mTtsListener = new SynthesizerListener() {
        //开始播放
        @Override
        public void onSpeakBegin() {
            Log.i(TAG, "开始播放");
        }

        //暂停播放
        @Override
        public void onSpeakPaused() {
            Log.i(TAG, "暂停播放");
        }

        //继续播放
        @Override
        public void onSpeakResumed() {
            Log.i(TAG, "继续播放");
        }

        //合成进度
        @Override
        public void onBufferProgress(int percent, int beginPos, int endPos, String info) {
            Log.i(TAG, "合成进度：" + percent + "%");
        }

        //播放进度
        @Override
        public void onSpeakProgress(int percent, int beginPos, int endPos) {
            // 播放进度
            Log.i(TAG, "播放进度：" + percent + "%");
            SpannableStringBuilder style = new SpannableStringBuilder(text);
            style.setSpan(new BackgroundColorSpan(Color.RED), beginPos, endPos, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            etText.setText(style);
        }

        //播放完成
        @Override
        public void onCompleted(SpeechError error) {
            if (error == null) {
                Log.i(TAG, "播放完成," + container.size());
                DebugLog.LogD("播放完成," + container.size());
                for (int i = 0; i < container.size(); i++) {
                    //写入文件
                    writeToFile(container.get(i));
                }
                //保存文件
                FileUtil.saveFile(memoryFile, mTotalSize, getExternalFilesDir(null) + "/1.pcm");
            } else {
                //异常信息
                showTip(error.getPlainDescription(true));
            }
        }

        //事件
        @Override
        public void onEvent(int eventType, int arg1, int arg2, Bundle obj) {
            //	 以下代码用于获取与云端的会话id，当业务出错时将会话id提供给技术支持人员，可用于查询会话日志，定位出错原因
            //	 若使用本地能力，会话id为null
            if (SpeechEvent.EVENT_SESSION_ID == eventType) {
                String sid = obj.getString(SpeechEvent.KEY_EVENT_SESSION_ID);
                Log.i(TAG, "session id =" + sid);
            }

            //当设置SpeechConstant.TTS_DATA_NOTIFY为1时，抛出buf数据
            if (SpeechEvent.EVENT_TTS_BUFFER == eventType) {
                byte[] buf = obj.getByteArray(SpeechEvent.KEY_EVENT_TTS_BUFFER);
                Log.i(TAG, "bufis =" + buf.length);
                container.add(buf);
            }
        }
    };

    /**
     * 写入文件
     */
    private void writeToFile(byte[] data) {
        if (data == null || data.length == 0) {
            return;
        }
        try {
            if (memoryFile == null) {
                Log.i(TAG, "memoryFile is null");
                String mFilepath = getExternalFilesDir(null) + "/1.pcm";
                memoryFile = new MemoryFile(mFilepath, 1920000);
                memoryFile.allowPurging(false);
            }
            memoryFile.writeBytes(data, 0, (int) mTotalSize, data.length);
            mTotalSize += data.length;
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 选中
     */
    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        voicer = arrayValue[position];
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {
    }

    /**
     * 找星星主函数
     */
    public void FindStar() {
        mTts.pauseSpeaking();
        // 播报星星
        int errorCode = Speak("您正在查找天秤座");
        if (errorCode != ErrorCode.SUCCESS) return;
        // 获得经纬度
        getLocation();
        if (location == null) return;
        // 获得时间
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        java.util.Date date = new java.util.Date();
        time = sdf.format(date);
        if (socketClient == null) {
            socketClient = new SocketClient();
        }
        // 获得陀螺仪和加速器的数据
        AudioBluetoothApi.getInstance().registerListener(mMac, result -> {
                    LogUtils.i(TAG, "result = " + result);
                    byte[] appData = result.getAppData();
                    SensorData sensorData = SensorDataHelper.genSensorData(appData);

                    socketClient.Send(sensorData.toString());
                }
        );
        AudioBluetoothApi.getInstance().sendCmd(mMac, Cmd.SENSOR_DATA_UPLOAD_OPEN.getType(), new IRspListener<Object>() {
            @Override
            public void onSuccess(Object object) {
                LogUtils.i(TAG, "onSuccess object = " + object);
            }

            @Override
            public void onFailed(int errorCode) {
                LogUtils.i(TAG, "onFailed errorCode = " + errorCode);
            }
        });
    }

    /**
     * 工具函数：语音播报
     *
     * @param text
     * @return errorCode
     */
    private int Speak(String text) {
        // 设置参数
        setParam();
        // 开始合成播放
        int code = mTts.startSpeaking(text, mTtsListener);
        if (code != ErrorCode.SUCCESS) {
            showTip("语音合成失败,错误码: " + code);
        }
        return code;
    }

    /**
     * 定位：权限判断
     */
    @RequiresApi(api = Build.VERSION_CODES.M)
    private void getLocation() {
        //检查定位权限
        ArrayList<String> permissions = new ArrayList<>();
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            permissions.add(Manifest.permission.ACCESS_FINE_LOCATION);
        }
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            permissions.add(Manifest.permission.ACCESS_COARSE_LOCATION);
        }

        //判断
        if (permissions.size() == 0) {//有权限，直接获取定位
            getLocationLL();
        } else {//没有权限，获取定位权限
            requestPermissions(permissions.toArray(new String[permissions.size()]), 2);
        }
    }

    private void getLocationLL() {
        LocationManager mLocationManager = (LocationManager) getApplicationContext().getSystemService(LOCATION_SERVICE);
        List<String> providers = mLocationManager.getProviders(true);
        location = null;
        for (String provider : providers) {
            @SuppressLint("MissingPermission") Location l = mLocationManager.getLastKnownLocation(provider);
            if (l == null) {
                continue;
            }
            if (location == null || l.getAccuracy() < location.getAccuracy()) {
                location = l;
            }
        }
        if (location != null) {
            //日志
            String locationStr = "维度：" + location.getLatitude() + " " + "经度：" + location.getLongitude() + " 海拔：" + location.getAltitude();
            showTip(locationStr);
        } else {
            Toast.makeText(this, "位置信息获取失败", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        EndSearch();
        if (socketClient != null) {
            socketClient.disconnect();
            socketClient = null;
        }
    }

    private void EndSearch() {
        AudioBluetoothApi.getInstance().sendCmd(mMac, Cmd.SENSOR_DATA_UPLOAD_CLOSE.getType(), new IRspListener<Object>() {
            @Override
            public void onSuccess(Object object) {
                LogUtils.i(TAG, "onSuccess object = " + object);
            }

            @Override
            public void onFailed(int errorCode) {
                LogUtils.i(TAG, "onFailed errorCode = " + errorCode);
            }
        });
        AudioBluetoothApi.getInstance().unregisterListener(mMac);
    }
}