package com.doit.net.event;

import android.content.Context;
import android.text.TextUtils;
import android.view.View;

import com.doit.net.model.BlackBoxManger;
import com.doit.net.model.CacheManager;
import com.doit.net.model.DBBlackInfo;
import com.doit.net.protocol.LTESendManager;
import com.doit.net.protocol.Send2GManager;
import com.doit.net.utils.LogUtils;
import com.doit.net.utils.ToastUtils;

/**
 * Created by wiker on 2016/4/27.
 */
public class AddToLocationListener implements View.OnClickListener {

    private Context mContext;
    private String imsi;
    private int type;

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


            if (CacheManager.getCurrentLoction() !=null && CacheManager.getCurrentLoction().isLocateStart()
                    && imsi.equals(CacheManager.getCurrentLoction().getImsi())
                    && type == CacheManager.getCurrentLoction().getType()) {
                ToastUtils.showMessage("该号码正在搜寻中");
                return;
            } else {
                EventAdapter.call(EventAdapter.SHOW_PROGRESS, 8000);  //防止快速频繁更换定位目标
                CacheManager.updateLoc(imsi, type);

                LTESendManager.openAllRf();
                if (type == 0) {  //4G定位

                    Send2GManager.setRFState("0");

                    LTESendManager.exchangeFcn(imsi);

                    CacheManager.changeLocTarget(imsi);
                    CacheManager.startLoc(imsi);

                } else {
                    Send2GManager.setRFState("1");

                    Send2GManager.setLocIMSI(imsi, "1");
                    CacheManager.redirect2G();
                }
                ToastUtils.showMessage("搜寻开始");
            }

            EventAdapter.call(EventAdapter.CHANGE_TAB, 1);

            EventAdapter.call(EventAdapter.ADD_LOCATION, imsi);
            EventAdapter.call(EventAdapter.ADD_BLACKBOX, BlackBoxManger.START_LOCALTE_FROM_NAMELIST + imsi);


    }


}
