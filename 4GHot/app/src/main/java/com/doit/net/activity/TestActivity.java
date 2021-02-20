package com.doit.net.activity;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.speech.tts.TextToSpeech;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.SeekBar;
import android.widget.TextView;

import com.doit.net.event.EventAdapter;
import com.doit.net.model.BlackBoxManger;
import com.doit.net.model.CacheManager;
import com.doit.net.model.VersionManage;
import com.doit.net.protocol.LTESendManager;
import com.doit.net.utils.LogUtils;
import com.doit.net.base.BaseActivity;
import com.doit.net.protocol.LTE_PT_SYSTEM;
import com.doit.net.utils.Cellular;
import com.doit.net.utils.ToastUtils;
import com.doit.net.ucsi.R;

import java.util.Locale;
import java.util.Timer;
import java.util.TimerTask;

import static com.doit.net.event.EventAdapter.GET_NAME_LIST;
import static com.doit.net.event.EventAdapter.UPDATE_TMEPRATURE;

public class TestActivity extends BaseActivity implements EventAdapter.EventCall {
    private Button test1;
    private Button test2;

    private Button test4;
    private Button test5;
    private Button test6;
    private EditText etIMSI;
    private Button test7;

    private Button btGetDeviceLog;
    private TextView tvTemperature;
    private TextView tvArfcns;
    private TextView tvNameList;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_just_for_test);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        test1 = findViewById(R.id.test1);
        test2 = findViewById(R.id.test2);

        test4 = findViewById(R.id.test4);
        test5 = findViewById(R.id.test5);
        test6 = findViewById(R.id.test6);
        etIMSI = findViewById(R.id.et_change_nameList);

        test7 = findViewById(R.id.test7);

        btGetDeviceLog = findViewById(R.id.btGetDeviceLog);
        tvTemperature = findViewById(R.id.tvTemperature);
        tvArfcns = findViewById(R.id.tvArfcns);
        tvNameList = findViewById(R.id.tv_name_list);


        initView();

        EventAdapter.register(EventAdapter.UPDATE_TMEPRATURE, this);
        EventAdapter.register(EventAdapter.GET_NAME_LIST, this);

    }

    private void initView() {
        test1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Cellular.adjustArfcnPwrForLocTarget("460008274382921");
            }
        });

        test2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String content = Cellular.file_fcns + "########" + Cellular.final_fcns;
                tvArfcns.setText(content);

            }
        });


        btGetDeviceLog.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ToastUtils.showMessage("获取设备命令已下发，请等待上传成功");
                LTE_PT_SYSTEM.commonSystemMsg(LTE_PT_SYSTEM.SYSTEM_GET_LOG);
            }
        });

        test4.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String imsi = etIMSI.getText().toString().trim();
                if (!TextUtils.isEmpty(imsi)){
                    LTESendManager.changeNameList("add", "reject", "460021683706233,460110363153426,"+imsi);
                }else {
                    LTESendManager.changeNameList("add", "reject", "460021683706233,460110363153426");
                }
            }
        });


        test5.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String imsi = etIMSI.getText().toString().trim();
                if (!TextUtils.isEmpty(imsi)){
                    LTESendManager.changeNameList("add", "block", imsi);
                }
            }
        });

        test6.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String imsi = etIMSI.getText().toString().trim();
                if (!TextUtils.isEmpty(imsi)){
                    LTESendManager.changeNameList("add", "redirect", imsi);

                }
            }
        });



        test7.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                LTESendManager.getNameList();
            }
        });

    }


    @SuppressLint("HandlerLeak")
    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 0:
                    tvTemperature.setText("温度：" + msg.obj);
                    break;
                case 1:
                    tvNameList.setText("名单：" + msg.obj);
                    break;
            }

        }
    };

    @Override
    public void call(String key, Object val) {
        switch (key) {
            case EventAdapter.UPDATE_TMEPRATURE:
                Message msg = new Message();
                msg.what = 0;
                msg.obj = val;
                mHandler.sendMessage(msg);
                break;
            case EventAdapter.GET_NAME_LIST:
                Message msg1 = new Message();
                msg1.what = 1;
                msg1.obj = val;
                mHandler.sendMessage(msg1);
                break;

        }

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }
}
