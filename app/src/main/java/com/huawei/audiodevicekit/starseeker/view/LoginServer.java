package com.huawei.audiodevicekit.starseeker.view;

import android.annotation.SuppressLint;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.widget.Toast;

import com.baidu.speech.utils.LogUtil;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.Socket;
import java.net.URL;

public class LoginServer {
    private final int HANDLER_MSG_TELL_RECV = 0x124;
    @SuppressLint("HandlerLeak")
    Handler handler = new Handler() {
        public void handleMessage(Message msg) {
            //接受到服务器信息时执行
//            Toast.makeText(Broadcast.this,(msg.obj).toString(),Toast.LENGTH_LONG).show();
            Log.e("1", (msg.obj).toString());
        }
    };

    /**
     * get的方式请求
     * <p>
     * //     * @param username 用户名
     * //     * @param password 密码
     *
     * @return 返回null 登录异常
     */
    public static String loginByGet(String hrnum, String longitude, String latitude, String height, String time) {
        //get的方式提交就是url拼接的方式
        String path = "http://192.168.43.101:8080/?hrnum=" + hrnum + "&longitude=" + longitude + "&latitude=" + latitude + "&height=" + height + "&time=" + time;
        Log.e("Get", "Start");
        try {
            URL url = new URL(path);
            Log.e("Get", "1");
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            Log.e("Get", "2");
            connection.setConnectTimeout(5000);
            Log.e("Get", "3");
            connection.setRequestMethod("GET");
            Log.e("Get", "4");
            //获得结果码
            int responseCode = connection.getResponseCode();
            Log.e("Get", responseCode + "");
            if (responseCode == 200) {
                //请求成功 获得返回的流
                InputStream is = connection.getInputStream();
                return InputStream2String(is);
            } else {
                //请求失败
                return null;
            }
        } catch (MalformedURLException e) {
            Log.e("Get", "5");
            e.printStackTrace();
        } catch (ProtocolException e) {
            Log.e("Get", "6");
            e.printStackTrace();
        } catch (IOException e) {
            Log.e("Get", "7");
            e.printStackTrace();
        }
        Log.e("Get", "8");
        return null;
    }

    public void startNetThread() {
        new Thread() {
            @Override
            public void run() {
                try {
                    Log.e("1", "1");
                    Socket socket = new Socket("192.168.43.101", 5000);
                    Log.e("1", "2");
                    InputStream is = socket.getInputStream();
                    byte[] bytes = new byte[1024];
                    int n = is.read(bytes);
                    Message msg = handler.obtainMessage(HANDLER_MSG_TELL_RECV, new String(bytes, 0, n));
                    msg.sendToTarget();
                    is.close();
                    socket.close();
                } catch (Exception e) {
                }
            }
        }.start();
    }

            public static String InputStream2String(InputStream in) {
                InputStreamReader reader = null;
                try {
                    reader = new InputStreamReader(in, "UTF-8");
                } catch (UnsupportedEncodingException e1) {
                    e1.printStackTrace();
                }
                BufferedReader br = new BufferedReader(reader);
                StringBuilder sb = new StringBuilder();
                String line = "";
                try {
                    while ((line = br.readLine()) != null) {
                        sb.append(line);
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
                return sb.toString();
            }
        }

