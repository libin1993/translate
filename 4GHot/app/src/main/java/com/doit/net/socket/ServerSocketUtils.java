package com.doit.net.socket;

import com.doit.net.protocol.LTEReceiveManager;
import com.doit.net.model.CacheManager;
import com.doit.net.utils.FormatUtils;
import com.doit.net.utils.LogUtils;
import com.doit.net.utils.ToastUtils;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;


/**
 * Author：Libin on 2020/5/20 15:43
 * Email：1993911441@qq.com
 * Describe：socket服务端
 */
public class ServerSocketUtils {
    private static ServerSocketUtils mInstance;
    private ServerSocket mServerSocket;
    public final static String LOCAL_IP = "192.168.1.133";   //本机ip
    public final static int LOCAL_PORT = 7003;   //本机端口
    private final static int READ_TIME_OUT = 50000;  //超时时间
    public static final String REMOTE_4G_IP = "192.168.1.200";  //4G设备ip
    public static final String REMOTE_2G_IP = "192.168.1.1";     //2G设备ip

    private Map<String, Socket> map = new HashMap<>();


    private ServerSocketUtils() {
        try {
            mServerSocket = new ServerSocket(LOCAL_PORT);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static ServerSocketUtils getInstance() {
        if (mInstance == null) {
            synchronized (ServerSocketUtils.class) {
                if (mInstance == null) {
                    mInstance = new ServerSocketUtils();
                }
            }

        }
        return mInstance;
    }


    /**
     * @param onSocketChangedListener 线程接收连接
     */
    public void startTCP(OnSocketChangedListener onSocketChangedListener) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                while (true) {
                    try {
                        Socket socket = mServerSocket.accept();  //获取socket
                        socket.setSoTimeout(READ_TIME_OUT);      //设置超时
                        socket.setKeepAlive(true);
                        String remoteIP = socket.getInetAddress().getHostAddress();  //远程ip
                        int remotePort = socket.getPort();    //远程端口

                        if (remoteIP.equals(ServerSocketUtils.REMOTE_4G_IP) || remoteIP.equals(ServerSocketUtils.REMOTE_2G_IP)){
                            map.put(remoteIP, socket);   //存储socket

                            if (onSocketChangedListener != null) {
                                onSocketChangedListener.onChange(remoteIP);
                            }

                            LogUtils.log("TCP收到设备连接,ip：" + remoteIP + "；端口：" + remotePort);

                            new ReceiveThread(socket,remoteIP).start();
                        }

                    } catch (IOException e) {
                        e.printStackTrace();
                        LogUtils.log("TCP异常：" + e.getMessage());
                    }
                }
            }
        }).start();
    }


    /**
     * 接收线程
     */
    public class ReceiveThread extends Thread {
        private Socket socket;
        private String remoteIP;

        public ReceiveThread(Socket socket,String remoteIP) {
            this.socket = socket;
            this.remoteIP = remoteIP;
        }

        @Override
        public void run() {
            super.run();
            //数据缓存
            byte[] bytesReceived = new byte[1024];
            //接收到流的数量
            int receiveCount;
            LTEReceiveManager lteReceiveManager = new LTEReceiveManager();
            try {
                //获取输入流
                InputStream inputStream = socket.getInputStream();

                //循环接收数据
                while ((receiveCount = inputStream.read(bytesReceived)) != -1) {
                    lteReceiveManager.parseData(remoteIP, bytesReceived, receiveCount);
                }

                LogUtils.log(remoteIP + "：socket被关闭，读取长度：" + receiveCount);

            } catch (IOException ex) {
                LogUtils.log(remoteIP + "：socket异常:" + ex.toString());
            }

            try {
                LogUtils.log(remoteIP + "关闭socket");
                socket.close();
            } catch (IOException e) {
                e.printStackTrace();
                LogUtils.log(remoteIP + "：socket关闭失败:" + e.toString());
            }

            lteReceiveManager.clearReceiveBuffer();
        }
    }



    /**
     * 发送数据
     *
     * @param data
     * @return
     */
    public void sendData(String ip, byte[] data) {

        Socket socket = map.get(ip);
        if (socket != null && socket.isConnected()) {

            new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        OutputStream outputStream = socket.getOutputStream();
                        outputStream.write(data);
                        outputStream.flush();
                    } catch (Exception e) {
                        e.printStackTrace();
                        LogUtils.log("socket发送失败：" + e.getMessage());
                    }
                }
            }).start();
        }

    }

}
