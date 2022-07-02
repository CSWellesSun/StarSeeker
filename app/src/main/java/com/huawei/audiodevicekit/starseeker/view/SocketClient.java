package com.huawei.audiodevicekit.starseeker.view;

import android.util.Log;

import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.Socket;
import java.util.concurrent.Executors;

public class SocketClient
{
    public String ipString = "192.168.217.30";   // 服务器端ip
    public int port = 9999;                // 服务器端口

    public Socket socket;

    public OutputStream outputStream;

    public InputStream inputStream;

    public SocketClient() {
        try {
            // 客户端 Socket 可以通过指定 IP 地址或域名两种方式来连接服务器端,实际最终都是通过 IP 地址来连接服务器
            // 新建一个Socket，指定其IP地址及端口号
            socket = new Socket(ipString, port);
            // 客户端socket在接收数据时，有两种超时：1. 连接服务器超时，即连接超时；2. 连接服务器成功后，接收服务器数据超时，即接收超时
            // 设置 socket 读取数据流的超时时间
            socket.setSoTimeout(5000);
            // 发送数据包，默认为 false，即客户端发送数据采用 Nagle 算法；
             // 但是对于实时交互性高的程序，建议其改为 true，即关闭 Nagle 算法，客户端每发送一次数据，无论数据包大小都会将这些数据发送出去
             socket.setTcpNoDelay(true);
             // 设置客户端 socket 关闭时，close() 方法起作用时延迟 30 秒关闭，如果 30 秒内尽量将未发送的数据包发送出去
             socket.setSoLinger(true, 30);
             // 设置输出流的发送缓冲区大小，默认是4KB，即4096字节
             socket.setSendBufferSize(4096);
             // 设置输入流的接收缓冲区大小，默认是4KB，即4096字节
             socket.setReceiveBufferSize(4096);
             // 作用：每隔一段时间检查服务器是否处于活动状态，如果服务器端长时间没响应，自动关闭客户端socket
             // 防止服务器端无效时，客户端长时间处于连接状态
             socket.setKeepAlive(true);
             // 代表可以立即向服务器端发送单字节数据
             socket.setOOBInline(true);
             // 数据不经过输出缓冲区，立即发送
             socket.sendUrgentData(0x44);//"D"

            outputStream = socket.getOutputStream();
            inputStream = socket.getInputStream();
        } catch (Exception e) {
            socket = null;
        }
    }

    /** 创建Socket并连接 */
    public void start()
    {
        if (socket != null && socket.isConnected()) return;

        Executors.newCachedThreadPool().execute(new Runnable()
        {
            @Override
            public void run()
            {
                // Socket接收数据
                try
                {
                    if (socket != null)
                    {
                        InputStream inputStream = socket.getInputStream();

                        // 1024 * 1024 * 3 = 3145728
                        byte[] buffer = new byte[3145728];		// 3M缓存
                        int len = -1;
                        while (socket.isConnected() && (len = inputStream.read(buffer)) != -1)
                        {
                            String data = new String(buffer, 0, len);

                            // 通过回调接口将获取到的数据推送出去
                            Log.e("Receiver", data);
                        }
                    }
                }
                catch (Exception ex)
                {
                    socket = null;
                }
            }
        });

    }

    /** 发送信息 */
    public void Send(String data)
    {
        Executors.newCachedThreadPool().execute(new Runnable()
        {
            @Override
            public void run()
            {
                try
                {
                    if(socket != null && socket.isConnected())
                    {
                        byte[] bytes = data.getBytes();
                        outputStream.write(bytes);
                        outputStream.flush();
                        byte[] buffer = new byte[48];		// 3M缓存
                        int len = -1;
                        if (socket.isConnected() && (len = inputStream.read(buffer)) != -1)
                        {
                            String data = new String(buffer, 0, len);
                            // 通过回调接口将获取到的数据推送出去
                            Log.e("Receiver", data);
                        }
                    }
                    else
                    {
                        Log.e("Send", "未连接上socket");
                    }
                }
                catch (Exception ex)
                {

                }
            }
        });
    }

    /** 断开Socket */
    public void disconnect()
    {
        try
        {
            if (socket != null && socket.isConnected())
            {
                socket.close();
                socket = null;
            }
        }
        catch (Exception ex)
        {

        }
    }
}
