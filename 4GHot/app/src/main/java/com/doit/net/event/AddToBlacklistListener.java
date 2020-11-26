package com.doit.net.event;

import android.content.Context;
import android.text.TextUtils;
import android.view.View;

import com.doit.net.model.UCSIDBManager;
import com.doit.net.model.BlackListInfo;
import com.doit.net.protocol.LTESendManager;
import com.doit.net.protocol.Send2GManager;
import com.doit.net.utils.ToastUtils;
import com.doit.net.ucsi.R;

import org.xutils.DbManager;
import org.xutils.ex.DbException;

import cn.pedant.SweetAlert.SweetAlertDialog;

/**
 * Created by Zxc on 2019/7/1.
 */

public class AddToBlacklistListener implements View.OnClickListener {
    private Context mContext;
    private String imsi;
    private String msisdn="";
    private String remark="";

    public AddToBlacklistListener(Context mContext, String imsi, String msisdn, String remark) {
        this.mContext = mContext;
        this.remark = remark;
        this.msisdn = msisdn;
        this.imsi = imsi;

    }

    public AddToBlacklistListener(Context mContext, String imsi) {
        this.mContext = mContext;
        this.imsi = imsi;
    }

    @Override
    public void onClick(View v) {
        try {

            DbManager dbManager = UCSIDBManager.getDbManager();
            if (!"".equals(imsi)){
                long count = dbManager.selector(BlackListInfo.class)
                        .where("imsi","=",imsi)
                        .count();
                if(count>0){

                    ToastUtils.showMessage( R.string.exist_whitelist);
                    return;
                }
            }else{
                long count = dbManager.selector(BlackListInfo.class)
                        .where("msisdn","=",msisdn)
                        .count();
                if(count>0){
                    ToastUtils.showMessage( R.string.exist_whitelist);
                    return;
                }
            }

            BlackListInfo info = new BlackListInfo();
            info.setRemark(remark);
            info.setImsi(imsi);
            info.setMsisdn(msisdn);
            dbManager.save(info);

            Send2GManager.setBlackList();
            if (!TextUtils.isEmpty(imsi)){
                LTESendManager.changeNameList("del","reject",imsi);
            }


            ToastUtils.showMessage(R.string.add_success);
        } catch (DbException e) {
            e.printStackTrace();
            new SweetAlertDialog(mContext, SweetAlertDialog.ERROR_TYPE)
                    .setTitleText(mContext.getString(R.string.add_whitelist_fail))
                    .show();
        }
    }
}
