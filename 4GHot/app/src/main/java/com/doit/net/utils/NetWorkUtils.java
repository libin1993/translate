/*
 * Copyright (C) 2011-2016 dshine.com.cn
 * All rights reserved.
 * ShangHai Dshine - http://www.dshine.com.cn
 */
package com.doit.net.utils;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.LinkAddress;
import android.net.NetworkCapabilities;
import android.net.NetworkInfo;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Build;

import com.doit.net.application.MyApplication;

import static android.content.Context.WIFI_SERVICE;


/**
 * @author 杨维(wiker)
 * @version 1.0
 * @date 2016-4-26 下午2:53:35
 */
public class NetWorkUtils {

    /**
     * @return 网络状态  宽带或wifi
     */
    public static boolean getNetworkState() {
        ConnectivityManager connectivityManager = (ConnectivityManager) MyApplication.mContext.getSystemService(Context.CONNECTIVITY_SERVICE);


        NetworkInfo wifiInfo = connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);  //wifi
        NetworkInfo ethernetInfo = connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_ETHERNET); //以太网


        boolean wifiState = wifiInfo != null && wifiInfo.isConnected();
        boolean ethernetState = ethernetInfo != null && ethernetInfo.isConnected();
        LogUtils.log("以太网网络状态：" + ethernetState);
        LogUtils.log("WIFI网络状态：" + wifiState);

        return ethernetState || wifiState;

    }


    /***
     * 使用WIFI时，获取本机IP地址
     *
     * @param
     * @return
     */
//    public static String getLocalIpAddress(Context mContext) {

    //获取wifi服务
//        ConnectivityManager connectivityManager = (ConnectivityManager) MyApplication.mContext.getSystemService(Context.CONNECTIVITY_SERVICE);
//
//
//        NetworkInfo wifiInfo = connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);  //wifi
//        NetworkInfo ethernetInfo =connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_ETHERNET); //以太网
//
//
//
//        boolean ethernetState = ethernetInfo !=null && ethernetInfo.isConnected();
//
//
//        return formatIpAddress(ipAddress);
//    }
    public static String getWIFIIpAddress(Context context) {
        try {

            WifiManager wifiManager = (WifiManager) context
                    .getSystemService(Context.WIFI_SERVICE);
            WifiInfo wifiInfo = wifiManager.getConnectionInfo();
            int i = wifiInfo.getIpAddress();
            return formatIpAddress(i);
        } catch (Exception ex) {
            return " 获取IP出错鸟!!!!请保证是WIFI,或者请重新打开网络!\n" + ex.getMessage();
        }
    }

    private static String formatIpAddress(int ipAdress) {

        return (ipAdress & 0xFF) + "." +
                ((ipAdress >> 8) & 0xFF) + "." +
                ((ipAdress >> 16) & 0xFF) + "." +
                (ipAdress >> 24 & 0xFF);
    }

    public static String getWifiSSID(Context context) {
        String ssid = "unknow";

        if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.N_MR1) {  //android7 及以前使用此方法
            WifiManager wifiManager = (WifiManager) context.getApplicationContext().getSystemService(Context.WIFI_SERVICE);
            WifiInfo wifiInfo = wifiManager.getConnectionInfo();
            ssid = wifiInfo.getSSID();
        }
        if (Build.VERSION.SDK_INT == Build.VERSION_CODES.O || Build.VERSION.SDK_INT == Build.VERSION_CODES.O_MR1) { //android8 使用此方法
            ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
            assert cm != null;
            NetworkInfo info = cm.getActiveNetworkInfo();
            if (info != null && info.isConnected()) {
                ssid = info.getExtraInfo();
            }
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) { //android9 及以上使用此方法
            WifiManager my_wifiManager = ((WifiManager) context.getApplicationContext().getSystemService(WIFI_SERVICE));
            assert my_wifiManager != null;
            WifiInfo wifiInfo = my_wifiManager.getConnectionInfo();
            ssid = wifiInfo.getSSID();
            int networkId = wifiInfo.getNetworkId();
            List<WifiConfiguration> configuredNetworks = my_wifiManager.getConfiguredNetworks();
            for (WifiConfiguration wifiConfiguration : configuredNetworks) {
                if (wifiConfiguration.networkId == networkId) {
                    ssid = wifiConfiguration.SSID;
                    break;
                }
            }
        }

        LogUtils.log("get ssid:" + ssid);
        return ssid;
    }
}
