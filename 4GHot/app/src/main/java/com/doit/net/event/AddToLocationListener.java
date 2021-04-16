package com.doit.net.event;

import android.app.Activity;
import android.content.Context;
import android.text.TextUtils;
import android.view.View;

import com.daimajia.swipe.SwipeLayout;
import com.doit.net.activity.MainActivity;
import com.doit.net.utils.BlackBoxManger;
import com.doit.net.utils.CacheManager;
import com.doit.net.utils.ToastUtils;

/**
 * Created by wiker on 2016/4/27.
 */
public class AddToLocationListener implements View.OnClickListener {

    private Context mContext;
    private String imsi;
    private int type;
    private SwipeLayout swipeLayout;

    public AddToLocationListener(Context mContext, String imsi, int type, SwipeLayout swipeLayout) {
        this.mContext = mContext;
        this.imsi = imsi;
        this.type = type;
        this.swipeLayout = swipeLayout;
    }

    public AddToLocationListener(Context mContext, String imsi, int type) {
        this.mContext = mContext;
        this.imsi = imsi;
        this.type = type;
    }

    @Override
    public void onClick(View v) {

        if (!CacheManager.checkDevice(mContext)) {
            return;
        }

        if (TextUtils.isEmpty(imsi)) {
            return;
        }


        if (CacheManager.getCurrentLocation() != null && CacheManager.getCurrentLocation().isLocateStart()
                && imsi.equals(CacheManager.getCurrentLocation().getImsi())
                && type == CacheManager.getCurrentLocation().getType()) {
            ToastUtils.showMessage("该号码正在搜寻中");
            return;
        } else {
            EventAdapter.call(EventAdapter.SHOW_PROGRESS, 8000);  //防止快速频繁更换定位目标
            CacheManager.updateLoc(imsi, type);

            CacheManager.startLoc(imsi,type);

            ToastUtils.showMessage("搜寻开始");
        }

        if (swipeLayout !=null){
            swipeLayout.close();
        }

        if (!(mContext  instanceof  MainActivity)) {
            ((Activity)mContext).finish();
        }

        EventAdapter.call(EventAdapter.CHANGE_TAB, 1);

        EventAdapter.call(EventAdapter.ADD_LOCATION, imsi);

        EventAdapter.call(EventAdapter.ADD_BLACKBOX, BlackBoxManger.START_LOCALTE_FROM_NAMELIST + imsi);


    }


}
