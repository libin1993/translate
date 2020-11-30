package com.doit.net.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Handler;

import com.doit.net.event.EventAdapter;
import com.doit.net.model.CacheManager;
import com.doit.net.utils.LogUtils;
import com.doit.net.utils.MySweetAlertDialog;
import com.doit.net.ucsi.R;
import com.doit.net.utils.NetWorkUtils;

import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by wiker on 2016/4/29.
 */
public class NetworkChangeReceiver extends BroadcastReceiver {

    public static boolean isShow = false;
    private MySweetAlertDialog myDialog;

    @Override
    public void onReceive(Context context, Intent intent) {
        if (!NetWorkUtils.getNetworkState()){
            if (!isShow){
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        if (!NetWorkUtils.getNetworkState() && myDialog==null){
                            myDialog = new MySweetAlertDialog(context, MySweetAlertDialog.WARNING_TYPE);
                            myDialog.setTitleText("网络异常");
                            myDialog.setContentText("网络连接已断开！设备断开连接！");
                            myDialog.show();
                            myDialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
                                @Override
                                public void onDismiss(DialogInterface dialog) {
                                    isShow = false;
                                    myDialog= null;
                                }
                            });

                            if (CacheManager.getCurrentLoction() != null) {
                                CacheManager.getCurrentLoction().setLocateStart(false);
                            }

                            EventAdapter.call(EventAdapter.RF_STATUS_LOC);

                            EventAdapter.call(EventAdapter.WIFI_CHANGE);
                        }else {
                            isShow = false;
                        }
                    }
                },5000);

            }
        }else {
            EventAdapter.call(EventAdapter.WIFI_CHANGE);
        }
    }
}
