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

    public final static int LOCAL_PORT = 7003;   //本机端口
    private final static int READ_TIME_OUT = 100000;  //超时时间
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

    //获取单例对象
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
                        String remoteIP = socket.getInetAddress().getHostAddress();  //远程ip
                        int remotePort = socket.getPort();    //远程端口

                        if (remoteIP.equals(ServerSocketUtils.REMOTE_4G_IP) || remoteIP.equals(ServerSocketUtils.REMOTE_2G_IP)){
                            map.put(remoteIP, socket);   //存储socket

                            if (onSocketChangedListener != null) {
                                onSocketChangedListener.onChange(remoteIP);
                            }

                            LogUtils.log("TCP收到设备连接,ip：" + remoteIP + "；端口：" + remotePort);


                            ReceiveThread receiveThread = new ReceiveThread(remoteIP, onSocketChangedListener);
                            receiveThread.start();
                        }

                    } catch (IOException e) {
                        e.printStackTrace();
                        LogUtils.log("tcp错误：" + e.getMessage());

                    }
                }
            }
        }).start();
    }


    /**
     * 接收线程
     */
    public class ReceiveThread extends Thread {
        private OnSocketChangedListener onSocketChangedListener;
        private String remoteIP;

        public ReceiveThread(String remoteIP, OnSocketChangedListener onSocketChangedListener) {
            this.remoteIP = remoteIP;
            this.onSocketChangedListener = onSocketChangedListener;
        }

        @Override
        public void run() {
            super.run();
            //数据缓存
            byte[] bytesReceived = new byte[1024];
            //接收到流的数量
            int receiveCount;
            LTEReceiveManager lteReceiveManager = new LTEReceiveManager();
            Socket socket;
            try {
                //获取当前socket
                socket = map.get(remoteIP);
                if (socket == null) {
                    return;
                }


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

            onSocketChangedListener.onChange(remoteIP);
            closeSocket(remoteIP);  //关闭socket
            lteReceiveManager.clearReceiveBuffer();
        }
    }

    //关闭socket
    public void closeSocket(String ip) {
        Socket socket = map.get(ip);
        if (socket != null && !socket.isClosed()) {
            //关闭输入流
            try {
                socket.shutdownInput();
                socket.close();//临时
                map.remove(ip);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

    }


    /**
     * 发送数据
     *
     * @param tempByte
     * @return
     */
    public void sendData(String ip, byte[] tempByte) {

        Socket socket = map.get(ip);
        if (socket != null && socket.isConnected()) {

            new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        OutputStream outputStream = socket.getOutputStream();
                        outputStream.write(tempByte);
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
