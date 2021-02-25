package com.doit.net.view;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.doit.net.event.AddToBlacklistListener;
import com.doit.net.event.EventAdapter;
import com.doit.net.model.BlackBoxManger;
import com.doit.net.protocol.LTESendManager;
import com.doit.net.protocol.Send2GManager;
import com.doit.net.utils.ToastUtils;
import com.doit.net.ucsi.R;

/**
 * Created by Zxc on 2019/5/30.
 */

public class AddBlacklistDialog extends Dialog {
    private View mView;
    private EditText etIMSI;
    private EditText etMsisdn;
    private EditText etRemark;
    private Button btWhitelist;
    private Button btCancel;
    private String currentImsi=""; //不可编辑
    private String msisdn;


    private Context mContext;

    public AddBlacklistDialog(Context context) {
        super(context, R.style.Theme_dialog);
        mContext = context;
        initView();
    }

    public AddBlacklistDialog(Context context, String imsi, String msisdn) {
        super(context, R.style.Theme_dialog);
        mContext = context;
        this.currentImsi = imsi;
        this.msisdn = msisdn;
        initView();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(mView);
    }

    private void initView(){
        LayoutInflater inflater= LayoutInflater.from(getContext());
        mView = inflater.inflate(R.layout.layout_add_blacklist_dialog, null);
        setCancelable(false);

        etIMSI = mView.findViewById(R.id.etIMSI);
        etMsisdn = mView.findViewById(R.id.etMsisdn);
        etRemark = mView.findViewById(R.id.etRemark);
        btWhitelist = mView.findViewById(R.id.btWhitelist);

        if (!TextUtils.isEmpty(currentImsi)){
            etIMSI.setText(currentImsi);
            etIMSI.setEnabled(false);
        }

        if (!TextUtils.isEmpty(msisdn)){
            etMsisdn.setText(msisdn);
        }

        btWhitelist.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                String imsi = etIMSI.getText().toString();
                String msisdn = etMsisdn.getText().toString();
                String remark = etRemark.getText().toString();


                if (TextUtils.isEmpty(msisdn)){
                    ToastUtils.showMessage( "请输入手机号");
                    return;
                }


                if (!TextUtils.isEmpty(imsi) && imsi.length() != 15){
                    ToastUtils.showMessage("IMSI长度错误，请确认后输入！");
                    return;
                }

                if (msisdn.length() != 11){
                    ToastUtils.showMessage( "手机长度错误，请确认后输入！");
                    return;
                }


                new AddToBlacklistListener(getContext(), imsi, msisdn, remark).onClick(null);

                EventAdapter.call(EventAdapter.ADD_BLACKBOX, BlackBoxManger.ADD_BLACK_LIST+imsi+"+"+msisdn+"+"+remark);
                EventAdapter.call(EventAdapter.REFRESH_BLACKLIST);
                dismiss();

            }
        });

        btCancel = mView.findViewById(R.id.btCancel);
        btCancel.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                dismiss();
            }
        });
    }

}
