package com.doit.net.view;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.doit.net.utils.SPUtils;
import com.doit.net.ucsi.R;
import com.doit.net.utils.ToastUtils;

/**
 * Author：Libin on 2020/12/4 15:25
 * Email：1993911441@qq.com
 * Describe：
 */
public class ChangeFcnDialog extends Dialog {
    private View mView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(mView);

    }
    public ChangeFcnDialog(Context context) {
        super(context, R.style.Theme_dialog);
        initView();
    }

    private void initView() {
        LayoutInflater inflater= LayoutInflater.from(getContext());
        mView = inflater.inflate(R.layout.dialog_change_fcn, null);
        EditText etFcn = mView.findViewById(R.id.et_ctc_fcn);
        Button btnSave = mView.findViewById(R.id.btn_change_fcn);
        Button btnCancel = mView.findViewById(R.id.btn_cancel_fcn);
        String defaultFcn = SPUtils.getString(SPUtils.CTC_FCN, "1850"); //电信定位默认频点

        etFcn.setText(defaultFcn);

        btnCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
            }
        });

        btnSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String fcn = etFcn.getText().toString().trim();
                if (TextUtils.isEmpty(fcn)){
                    ToastUtils.showMessage("请输入频点");
                    return;
                }
                SPUtils.setString(SPUtils.CTC_FCN,fcn);
                dismiss();
            }
        });
    }

}
