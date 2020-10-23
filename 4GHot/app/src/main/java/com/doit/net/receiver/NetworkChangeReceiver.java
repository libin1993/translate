package com.doit.net.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

import com.doit.net.event.EventAdapter;
import com.doit.net.model.CacheManager;
import com.doit.net.utils.MySweetAlertDialog;
import com.doit.net.ucsi.R;
import com.doit.net.utils.NetWorkUtils;

/**
 * Created by wiker on 2016/4/29.
 */
public class NetworkChangeReceiver extends BroadcastReceiver {

    public static boolean isShow = false;

    @Override
    public void onReceive(Context context, Intent intent) {
//        ConnectivityManager connectivityManager=(ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
////        NetworkInfo wifiNetInfo=connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
//
//        NetworkInfo activeNetInfo = connectivityManager.getActiveNetworkInfo();
        if (!NetWorkUtils.getNetworkState() && !isShow) {
            MySweetAlertDialog dialog = new MySweetAlertDialog(context, MySweetAlertDialog.WARNING_TYPE);
            dialog.setTitleText("网络异常");
            dialog.setContentText("网络连接已断开！设备断开连接！");
            dialog.show();
            dialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
                @Override
                public void onDismiss(DialogInterface dialog) {
                    isShow = false;
                }
            });

            isShow = true;
            EventAdapter.call(EventAdapter.STOP_LOC);

        }

        EventAdapter.call(EventAdapter.WIFI_CHANGE);

    }
}
