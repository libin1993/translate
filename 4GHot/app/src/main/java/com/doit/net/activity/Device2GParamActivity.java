package com.doit.net.activity;

import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.PopupWindow;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.TableRow;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.BaseViewHolder;
import com.doit.net.base.BaseActivity;
import com.doit.net.bean.Set2GParamsBean;
import com.doit.net.event.EventAdapter;
import com.doit.net.model.BlackBoxManger;
import com.doit.net.model.CacheManager;
import com.doit.net.model.DBImsi;
import com.doit.net.model.DBUeidInfo;
import com.doit.net.model.PrefManage;
import com.doit.net.model.UCSIDBManager;
import com.doit.net.model.UserInfo;
import com.doit.net.protocol.LTESendManager;
import com.doit.net.protocol.Send2GManager;
import com.doit.net.ucsi.R;
import com.doit.net.utils.FormatUtils;
import com.doit.net.utils.LogUtils;
import com.doit.net.utils.MySweetAlertDialog;
import com.doit.net.utils.ScreenUtils;
import com.doit.net.utils.ToastUtils;

import org.xutils.DbManager;
import org.xutils.common.util.KeyValue;
import org.xutils.db.sqlite.WhereBuilder;
import org.xutils.ex.DbException;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import cn.bertsir.zbar.Qr.Image;

/**
 * Author：Libin on 2020/9/28 14:25
 * Email：1993911441@qq.com
 * Describe：2G设备参数
 */
public class Device2GParamActivity extends BaseActivity implements EventAdapter.EventCall {
    @BindView(R.id.rb_power_high)
    RadioButton rbPowerHigh;
    @BindView(R.id.rb_power_medium)
    RadioButton rbPowerMedium;
    @BindView(R.id.rb_power_low)
    RadioButton rbPowerLow;
    @BindView(R.id.rg_power_level)
    RadioGroup rgPowerLevel;
    @BindView(R.id.cb_rf_switch)
    CheckBox cbSwitch;
    @BindView(R.id.rv_2g_params)
    RecyclerView rvParams;
    @BindView(R.id.btn_set_param)
    Button btnSetParam;
    @BindView(R.id.btn_refresh_param)
    Button btnRefreshParam;
    @BindView(R.id.btn_reboot_device)
    Button btnRebootDevice;
    @BindView(R.id.btn_send_sms)
    Button btnSendSms;

    private BaseQuickAdapter<Set2GParamsBean.Params, BaseViewHolder> adapter;

    private MySweetAlertDialog mProgressDialog;
    private long lastRefreshParamTime = 0; //防止频繁刷新参数

    private final int SHOW_PROGRESS = 1;
    private final int UPDATE_VIEW = 0;

    public static String[] workModeArr = {"扫描", "常开", "关闭"};  //频点工作模式

    private String fcnMode1;
    private String fcnMode2;
    private String fcnMode3;
    private String fcnMode4;

    public static  final String SMS_COUNT = "SMS_COUNT";   //发送短信次数
    public static  final String SMS_INTERVAL = "SMS_INTERVAL"; //发送短信间隔
    public static  final String SMS_CONTENT = "SMS_CONTENT";  //发送短信内容

    private List<DBImsi> imsiList = new ArrayList<>();
    private BaseQuickAdapter<DBImsi,BaseViewHolder> imsiAdapter;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_device_param_2g);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        ButterKnife.bind(this);
        initView();
        initEvent();
    }

    @Override
    public void onResume() {
        super.onResume();
        refreshViews();
    }

    private void initEvent() {
        EventAdapter.register(EventAdapter.REFRESH_DEVICE_2G, this);
    }

    private void initView() {

        rvParams.setLayoutManager(new LinearLayoutManager(this));
        adapter = new BaseQuickAdapter<Set2GParamsBean.Params, BaseViewHolder>(R.layout.layout_rv_band_item, CacheManager.paramList) {
            @Override
            protected void convert(BaseViewHolder helper, Set2GParamsBean.Params item) {
                String type = "制式：";
                if (item.getBoardid().equals("0") && item.getCarrierid().equals("0")) {
                    type += "移动";
                }
                if (item.getBoardid().equals("0") && item.getCarrierid().equals("1")) {
                    type += "联通";
                }
                if (item.getBoardid().equals("1") && item.getCarrierid().equals("0")) {
                    type += "电信";
                }

                if (!TextUtils.isEmpty(item.getFcn())) {
                    type += "\n频点：" + item.getFcn();
                }

                helper.setText(R.id.tv_band_info, type);
                ImageView ivRFStatus = helper.getView(R.id.iv_rf_status);
                if (item.isRfState()) {
                    ivRFStatus.setImageResource(R.drawable.switch_open);
                } else {
                    ivRFStatus.setImageResource(R.drawable.switch_close);
                }

                helper.addOnClickListener(R.id.iv_rf_status);
            }


        };
        rvParams.setAdapter(adapter);
//        adapter.setOnItemClickListener(new BaseQuickAdapter.OnItemClickListener() {
//            @Override
//            public void onItemClick(BaseQuickAdapter adapter, View view, int position) {
//
////                if (lteChannelCfg != null && !TextUtils.isEmpty(lteChannelCfg.getChangeBand())) {
////                    changeChannelBandDialog(lteChannelCfg.getIdx(), lteChannelCfg.getChangeBand());
////                }
//            }
//        });
        adapter.setOnItemChildClickListener(new BaseQuickAdapter.OnItemChildClickListener() {
            @Override
            public void onItemChildClick(BaseQuickAdapter adapter, View view, int position) {
                if (view.getId() == R.id.iv_rf_status) {
                    if (!CacheManager.checkDevice(Device2GParamActivity.this)) {
                        return;
                    }

                    Set2GParamsBean.Params params = CacheManager.paramList.get(position);

                    if (params == null) {
                        return;
                    }

                    if (CacheManager.getLocState()) {
                        ToastUtils.showMessageLong("当前正在搜寻中，请确认通道射频变动是否对其产生影响！");
                    }

                    showProcess(6000);
                    Send2GManager.setRFState(params.getBoardid(), params.getCarrierid(), params.isRfState() ? "0" : "1");

                }

            }
        });

        cbSwitch.setOnCheckedChangeListener(rfCheckChangeListener);
        rgPowerLevel.setOnCheckedChangeListener(powerLevelListener);

        mProgressDialog = new MySweetAlertDialog(this, MySweetAlertDialog.PROGRESS_TYPE);
        mProgressDialog.setTitleText("Loading...");
        mProgressDialog.setCancelable(false);


        Send2GManager.getParamsConfig();

    }

    CompoundButton.OnCheckedChangeListener rfCheckChangeListener = new CompoundButton.OnCheckedChangeListener() {
        @Override
        public void onCheckedChanged(CompoundButton compoundButton, boolean isChecked) {
            if (!compoundButton.isPressed()) {
                return;
            }

            if (!CacheManager.checkDevice(Device2GParamActivity.this)) {
                cbSwitch.setChecked(!isChecked);
                return;
            }


            if (isChecked) {
                Send2GManager.setRFState("1");
                ToastUtils.showMessageLong(R.string.rf_open);
                EventAdapter.call(EventAdapter.ADD_BLACKBOX, BlackBoxManger.OPEN_ALL_2G_RF);
                showProcess(10000);
            } else {
                if (CacheManager.getLocState()) {
                    new MySweetAlertDialog(Device2GParamActivity.this, MySweetAlertDialog.WARNING_TYPE)
                            .setTitleText("提示")
                            .setContentText("当前正在搜寻，确定关闭吗？")
                            .setCancelText(getString(R.string.cancel))
                            .setConfirmText(getString(R.string.sure))
                            .showCancelButton(true)
                            .setConfirmClickListener(new MySweetAlertDialog.OnSweetClickListener() {

                                @Override
                                public void onClick(MySweetAlertDialog sweetAlertDialog) {
                                    sweetAlertDialog.dismiss();
                                    LTESendManager.closeAllRf();
                                    ToastUtils.showMessage(R.string.rf_close);
                                    showProcess(10000);
                                    EventAdapter.call(EventAdapter.ADD_BLACKBOX, BlackBoxManger.CLOSE_ALL_2G_RF);
                                }
                            })
                            .show();
                } else {
                    Send2GManager.setRFState("0");
                    ToastUtils.showMessageLong(R.string.rf_close);
                    showProcess(10000);
                    EventAdapter.call(EventAdapter.ADD_BLACKBOX, BlackBoxManger.CLOSE_ALL_2G_RF);
                }

            }
        }
    };

    RadioGroup.OnCheckedChangeListener powerLevelListener = new RadioGroup.OnCheckedChangeListener() {
        @Override
        public void onCheckedChanged(RadioGroup group, int checkedId) {
            if (!(group.findViewById(checkedId).isPressed())) {
                return;
            }

            if (CacheManager.getLocState()) {
                ToastUtils.showMessage("当前正在搜寻中，请留意功率变动是否对其产生影响！");
            } else {
                ToastUtils.showMessageLong("功率设置已下发，请等待其生效");
            }

            switch (checkedId) {
                case R.id.rb_power_high:
                    Send2GManager.setPowerLevel(3);
                    EventAdapter.call(EventAdapter.ADD_BLACKBOX, BlackBoxManger.SET_2G_POWER + "高");
                    break;

                case R.id.rb_power_medium:
                    Send2GManager.setPowerLevel(2);
                    EventAdapter.call(EventAdapter.ADD_BLACKBOX, BlackBoxManger.SET_2G_POWER + "中");
                    break;

                case R.id.rb_power_low:
                    Send2GManager.setPowerLevel(1);
                    EventAdapter.call(EventAdapter.ADD_BLACKBOX, BlackBoxManger.SET_2G_POWER + "低");
                    break;
            }

            showProcess(6000);
        }
    };


    public void refreshViews() {
        refreshPowerLevel();
        refreshRFSwitch();
        adapter.notifyDataSetChanged();

    }

    private void refreshPowerLevel() {

        if (CacheManager.isDeviceOk() && CacheManager.paramList.size() > 0) {
            int powerLevel = (Integer.parseInt(CacheManager.paramList.get(0).getDlattn()));

            if (powerLevel <= 5) {
                rbPowerHigh.setChecked(true);
            } else if (powerLevel <= 10) {
                rbPowerMedium.setChecked(true);
            } else {
                rbPowerLow.setChecked(true);
            }

        }

    }

    private void refreshRFSwitch() {
        boolean rfState = false;

        for (Set2GParamsBean.Params params : CacheManager.paramList) {
            if (params.isRfState()) {
                rfState = true;
                break;
            }
        }

        cbSwitch.setOnCheckedChangeListener(null);
        cbSwitch.setChecked(rfState);
        cbSwitch.setOnCheckedChangeListener(rfCheckChangeListener);
    }


    private void showProcess(int keepTime) {
        Message msg = new Message();
        msg.what = SHOW_PROGRESS;
        msg.obj = keepTime;
        mHandler.sendMessage(msg);
    }


    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            if (msg.what == UPDATE_VIEW) {
                LogUtils.log("设备参数页面已更新。");
                refreshViews();
            } else if (msg.what == SHOW_PROGRESS) {
                int dialogKeepTime = 5000;
                if (msg.obj != null && (int) msg.obj != 0) {
                    dialogKeepTime = (int) msg.obj;
                }
                mProgressDialog.show();
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        mProgressDialog.dismiss();
                    }
                }, dialogKeepTime);
            }
        }
    };

    @Override
    public void call(String key, Object val) {
        if (EventAdapter.REFRESH_DEVICE_2G.equals(key)) {
            mHandler.sendEmptyMessage(UPDATE_VIEW);
        }
    }

    @OnClick({R.id.btn_set_param, R.id.btn_refresh_param, R.id.btn_reboot_device,R.id.btn_send_sms})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.btn_set_param:
                setParams();
                break;
            case R.id.btn_refresh_param:
                refreshData();
                break;
            case R.id.btn_reboot_device:
                rebootDevice();
                break;
            case R.id.btn_send_sms:
                sendSMS();
                break;
        }
    }

    private void refreshData() {
        if (!CacheManager.checkDevice(this)) {
            return;
        }

        long currentTime = System.currentTimeMillis();
        if (currentTime - lastRefreshParamTime > 20 * 1000) {
            Send2GManager.getParamsConfig();

            new Timer().schedule(new TimerTask() {
                @Override
                public void run() {
                    Send2GManager.getCommonConfig();
                }
            }, 500);
            lastRefreshParamTime = currentTime;
            ToastUtils.showMessage("下发查询参数成功！");
        } else {
            ToastUtils.showMessage("请勿频繁刷新参数！");
        }
    }


    /**
     * 发送短信
     */
    private void sendSMS(){
        if (!CacheManager.checkDevice(this)) {
            return;
        }

        View dialogView = LayoutInflater.from(this).inflate(R.layout.popup_send_sms, null);
        PopupWindow popupWindow = new PopupWindow(dialogView, ScreenUtils.getInstance()
                .getScreenWidth(Device2GParamActivity.this) - FormatUtils.getInstance().dip2px(40),
                ViewGroup.LayoutParams.WRAP_CONTENT);

        ImageView ivAddImsi = dialogView.findViewById(R.id.iv_add_imsi);
        RecyclerView rvImsi = dialogView.findViewById(R.id.rv_imsi);
        EditText etInterval = dialogView.findViewById(R.id.et_sms_interval);
        EditText etCount = dialogView.findViewById(R.id.et_sms_count);
        EditText etContent = dialogView.findViewById(R.id.et_sms_content);

        Button btnSendImsi = dialogView.findViewById(R.id.btn_send_imsi);
        Button btnSendAll = dialogView.findViewById(R.id.btn_send_all);
        Button btnStop = dialogView.findViewById(R.id.btn_stop_send);
        Button btnClose = dialogView.findViewById(R.id.btn_close);

        //设置Popup具体参数
        popupWindow.setFocusable(true);//点击空白，popup不自动消失
        popupWindow.setTouchable(true);//popup区域可触摸
        popupWindow.setOutsideTouchable(false);//非popup区域可触摸
        popupWindow.setBackgroundDrawable(new BitmapDrawable(getResources(), (Bitmap) null));
        popupWindow.showAtLocation(getWindow().getDecorView(), Gravity.CENTER, 0, 0);

        rvImsi.setLayoutManager(new LinearLayoutManager(this));
        imsiAdapter = new BaseQuickAdapter<DBImsi, BaseViewHolder>(R.layout.layout_rv_imsi_item,imsiList) {
            @Override
            protected void convert(BaseViewHolder helper, DBImsi item) {
                helper.setText(R.id.tv_imsi,item.getImsi());
                helper.addOnClickListener(R.id.ll_delete_imsi);
                helper.addOnClickListener(R.id.ll_edit_imsi);
            }
        };

        rvImsi.setAdapter(imsiAdapter);
        refreshImsi();

        imsiAdapter.setOnItemChildClickListener(new BaseQuickAdapter.OnItemChildClickListener() {
            @Override
            public void onItemChildClick(BaseQuickAdapter adapter, View view, int position) {
                switch (view.getId()){
                    case R.id.ll_edit_imsi:
                        editImsi(imsiList.get(position));
                        break;
                    case R.id.ll_delete_imsi:
                        try {
                            UCSIDBManager.getDbManager().delete(DBImsi.class, WhereBuilder.b("imsi","=",imsiList.get(position).getImsi()));
                            imsiList.remove(position);
                            imsiAdapter.notifyDataSetChanged();
                        } catch (DbException e) {
                            e.printStackTrace();
                        }
                        break;
                }
            }
        });

        int interval = PrefManage.getInt(Device2GParamActivity.SMS_INTERVAL,1);
        int count = PrefManage.getInt(Device2GParamActivity.SMS_COUNT,1);
        String content =  PrefManage.getString(Device2GParamActivity.SMS_CONTENT,"");

        etContent.setText(content);
        etCount.setText(String.valueOf(count));
        etInterval.setText(String.valueOf(interval));


        ivAddImsi.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                editImsi(null);

            }
        });

        btnClose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                popupWindow.dismiss();
            }
        });

        btnSendAll.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendData("all",etCount,etInterval,etContent,popupWindow);
            }
        });

        btnSendImsi.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendData("one",etCount,etInterval,etContent,popupWindow);
            }
        });

        btnStop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Send2GManager.sendSms("clear",1,1,"");
                popupWindow.dismiss();
            }
        });
    }

    //编辑imsi
    private void editImsi(DBImsi dbImsi){
        View dialogView = LayoutInflater.from(Device2GParamActivity.this).inflate(R.layout.popup_add_imsi, null);
        PopupWindow popupWindow = new PopupWindow(dialogView, FormatUtils.getInstance().dip2px(250), ViewGroup.LayoutParams.WRAP_CONTENT);
        //设置Popup具体参数
        popupWindow.setFocusable(true);//点击空白，popup不自动消失
        popupWindow.setTouchable(true);//popup区域可触摸
        popupWindow.setOutsideTouchable(false);//非popup区域可触摸
        popupWindow.setBackgroundDrawable(new BitmapDrawable(getResources(), (Bitmap) null));
        popupWindow.showAtLocation(getWindow().getDecorView(), Gravity.CENTER, 0, 0);

        EditText etImsi = dialogView.findViewById(R.id.et_add_imsi);
        Button btnCancel = dialogView.findViewById(R.id.btn_close_imsi);
        Button btnSave = dialogView.findViewById(R.id.btn_add_imsi);

        if (dbImsi !=null){
            etImsi.setText(dbImsi.getImsi());
        }

        btnSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String imsi = etImsi.getText().toString().trim();
                if (TextUtils.isEmpty(imsi) || imsi.length() !=15){
                    ToastUtils.showMessage("请输入15位IMSI");
                    return;
                }

                try {
                    DbManager dbManager = UCSIDBManager.getDbManager();
                    DBImsi result = dbManager.selector(DBImsi.class).where("imsi", "=", imsi).findFirst();

                    if (dbImsi !=null){
                        if (result == null){
                            KeyValue keyValue = new KeyValue("imsi", imsi);
                            dbManager.update(DBImsi.class, WhereBuilder.b("id", "=", dbImsi.getId()), keyValue);
                        }
                    }else {
                        if (result != null){
                            ToastUtils.showMessage("IMSI已存在");
                            return;
                        }else {
                            dbManager.save(new DBImsi(imsi));
                        }
                    }

                } catch (DbException e) {
                    e.printStackTrace();
                }


                refreshImsi();
                popupWindow.dismiss();
            }
        });

        btnCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                popupWindow.dismiss();
            }
        });
    }


    //发送短信
    private void sendData(String action,EditText etCount,EditText etInterval,EditText etContent,PopupWindow popupWindow){
        String count = etCount.getText().toString().trim();
        String interval   = etInterval.getText().toString().trim();
        String content = etContent.getText().toString().trim();

        if (TextUtils.isEmpty(interval) || Integer.parseInt(interval) < 1){
            ToastUtils.showMessage("短信发送间隔不得低于1分钟");
            return;
        }

        if (TextUtils.isEmpty(count) || Integer.parseInt(count) < 1){
            ToastUtils.showMessage("短信发送次数不得低于1次");
            return;
        }

        if (TextUtils.isEmpty(content)){
            ToastUtils.showMessage("请输入短信内容");
            return;
        }

        PrefManage.setInt(Device2GParamActivity.SMS_INTERVAL,Integer.parseInt(interval));
        PrefManage.setInt(Device2GParamActivity.SMS_COUNT,Integer.parseInt(count));
        PrefManage.setString(Device2GParamActivity.SMS_CONTENT,content);

        Send2GManager.sendSms(action,Integer.parseInt(count),Integer.parseInt(interval),content);
        popupWindow.dismiss();
    }


    //刷新页面
    private void refreshImsi(){
        imsiList.clear();
        DbManager dbManager = UCSIDBManager.getDbManager();
        try {
            List<DBImsi> all = dbManager.selector(DBImsi.class).findAll();
            if (all !=null && all.size() > 0){
                imsiList.addAll(all);
            }
        } catch (DbException e) {
            e.printStackTrace();
        }

        imsiAdapter.notifyDataSetChanged();
    }

    /**
     * 重启设备
     */
    private void rebootDevice() {
        if (!CacheManager.checkDevice(this)) {
            return;
        }

        new MySweetAlertDialog(this, MySweetAlertDialog.WARNING_TYPE)
                .setTitleText("设备重启")
                .setContentText("确定重启设备")
                .setCancelText(getString(R.string.cancel))
                .setConfirmText(getString(R.string.sure))
                .showCancelButton(true)
                .setConfirmClickListener(new MySweetAlertDialog.OnSweetClickListener() {

                    @Override
                    public void onClick(MySweetAlertDialog sweetAlertDialog) {
                        sweetAlertDialog.dismiss();
                        Send2GManager.rebootDevice();
                    }
                })
                .show();
    }

    private void setParams() {
        if (!CacheManager.checkDevice(this)) {
            return;
        }


        View dialogView = LayoutInflater.from(this).inflate(R.layout.doit_layout_channels_dialog, null);
        PopupWindow popupWindow = new PopupWindow(dialogView, ScreenUtils.getInstance()
                .getScreenWidth(this) - FormatUtils.getInstance().dip2px(40),
                ViewGroup.LayoutParams.WRAP_CONTENT);

        RecyclerView rvChannel = dialogView.findViewById(R.id.rv_channel);
        Button btnCancel = dialogView.findViewById(R.id.button_cancel);

        //设置Popup具体参数
        popupWindow.setFocusable(true);//点击空白，popup不自动消失
        popupWindow.setTouchable(true);//popup区域可触摸
        popupWindow.setOutsideTouchable(false);//非popup区域可触摸
        popupWindow.setBackgroundDrawable(new BitmapDrawable(getResources(), (Bitmap) null));
        popupWindow.showAtLocation(getWindow().getDecorView(), Gravity.CENTER, 0, 0);


        rvChannel.setLayoutManager(new LinearLayoutManager(this));
        BaseQuickAdapter<Set2GParamsBean.Params, BaseViewHolder> adapter = new BaseQuickAdapter<Set2GParamsBean.Params,
                BaseViewHolder>(R.layout.layout_2g_param_item, CacheManager.paramList) {
            @Override
            protected void convert(BaseViewHolder helper, Set2GParamsBean.Params item) {
                String type = "制式：";
                if (item.getBoardid().equals("0") && item.getCarrierid().equals("0")) {
                    type += "移动";
                } else if (item.getBoardid().equals("0") && item.getCarrierid().equals("1")) {
                    type += "联通";
                } else if (item.getBoardid().equals("1") && item.getCarrierid().equals("0")) {
                    type += "电信";
                }


                Spinner spinner1 = helper.getView(R.id.spinner_mode1);
                Spinner spinner2 = helper.getView(R.id.spinner_mode2);
                Spinner spinner3 = helper.getView(R.id.spinner_mode3);
                Spinner spinner4 = helper.getView(R.id.spinner_mode4);

                helper.setText(R.id.tv_2g_plmn, type);
                TableRow trFcn = helper.getView(R.id.tr_fcn);
                TableRow trMode = helper.getView(R.id.tr_work_mode);
                if (item.getBoardid().equals("0")) {
                    trFcn.setVisibility(View.VISIBLE);
                    trMode.setVisibility(View.GONE);
                    helper.setText(R.id.et_fcn_2g, item.getFcn() == null ? "" : "" + item.getFcn());
                } else {
                    trFcn.setVisibility(View.GONE);
                    trMode.setVisibility(View.VISIBLE);
                    initAdapter(spinner1, workModeArr);
                    initAdapter(spinner2, workModeArr);
                    initAdapter(spinner3, workModeArr);
                    initAdapter(spinner4, workModeArr);


                    spinner1.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                        @Override
                        public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                            fcnMode1 = String.valueOf(position);
                        }

                        @Override
                        public void onNothingSelected(AdapterView<?> parent) {

                        }
                    });

                    spinner2.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                        @Override
                        public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                            fcnMode2 = String.valueOf(position);
                        }

                        @Override
                        public void onNothingSelected(AdapterView<?> parent) {

                        }
                    });

                    spinner3.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                        @Override
                        public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                            fcnMode3 = String.valueOf(position);
                        }

                        @Override
                        public void onNothingSelected(AdapterView<?> parent) {

                        }
                    });

                    spinner4.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                        @Override
                        public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                            fcnMode4 = String.valueOf(position);
                        }

                        @Override
                        public void onNothingSelected(AdapterView<?> parent) {

                        }
                    });

                    spinner1.setSelection(Integer.parseInt(item.getFcnmode().get(0)));
                    spinner2.setSelection(Integer.parseInt(item.getFcnmode().get(1)));
                    spinner3.setSelection(Integer.parseInt(item.getFcnmode().get(2)));
                    spinner4.setSelection(Integer.parseInt(item.getFcnmode().get(3)));

                }

                helper.setText(R.id.rt_pa_2g, item.getDlattn() == null ? "" : "" + item.getDlattn());
                helper.setText(R.id.et_ga_2g, item.getUlattn() == null ? "" : "" + item.getUlattn());


                helper.addOnClickListener(R.id.btn_save_param);
            }

        };
        rvChannel.setAdapter(adapter);

        adapter.setOnItemChildClickListener(new BaseQuickAdapter.OnItemChildClickListener() {
            @Override
            public void onItemChildClick(BaseQuickAdapter adapter, View view, int position) {

                if (view.getId() == R.id.btn_save_param) {
                    if (!CacheManager.checkDevice(Device2GParamActivity.this)) {
                        return;
                    }

                    EditText etFcn = (EditText) adapter.getViewByPosition(rvChannel, position, R.id.et_fcn_2g);
                    EditText etPa = (EditText) adapter.getViewByPosition(rvChannel, position, R.id.rt_pa_2g);
                    EditText etGa = (EditText) adapter.getViewByPosition(rvChannel, position, R.id.et_ga_2g);

                    String fcn = etFcn.getText().toString().trim();
                    String pa = etPa.getText().toString().trim();
                    String ga = etGa.getText().toString().trim();

                    if (TextUtils.isEmpty(fcn) && CacheManager.paramList.get(position).getBoardid().equals("0")) {
                        ToastUtils.showMessage("请输入频点");
                        return;
                    }

                    if (TextUtils.isEmpty(pa)) {
                        ToastUtils.showMessage("请输入下行功率");
                        return;
                    }

                    if (TextUtils.isEmpty(ga)) {
                        ToastUtils.showMessage("请输入上行增益");
                        return;
                    }


                    Set2GParamsBean.Params params = new Set2GParamsBean.Params();
                    params.setBoardid(CacheManager.paramList.get(position).getBoardid());
                    params.setCarrierid(CacheManager.paramList.get(position).getCarrierid());
                    params.setMcc(CacheManager.paramList.get(position).getMcc());
                    params.setMnc(CacheManager.paramList.get(position).getMnc());
                    params.setLac(CacheManager.paramList.get(position).getLac());
                    params.setOpmode(CacheManager.paramList.get(position).getOpmode());
                    params.setSniff(CacheManager.paramList.get(position).getSniff());
                    params.setCi(CacheManager.paramList.get(position).getCi());
                    params.setCro(CacheManager.paramList.get(position).getCro());
                    params.setCfgmode(CacheManager.paramList.get(position).getCfgmode());

                    if (CacheManager.paramList.get(position).getBoardid().equals("1")) {
                        List<String> fcnModeList = new ArrayList<>();
                        fcnModeList.add(fcnMode1);
                        fcnModeList.add(fcnMode2);
                        fcnModeList.add(fcnMode3);
                        fcnModeList.add(fcnMode4);

                        params.setFcnmode(fcnModeList);
                    } else {
                        params.setFcnmode(CacheManager.paramList.get(position).getFcnmode());
                    }

                    params.setFcn(fcn);
                    params.setDlattn(pa);
                    params.setUlattn(ga);
                    Send2GManager.setParamsConfig(params);

                    ToastUtils.showMessage(R.string.tip_15);
                    showProcess(10000);


                    if (!TextUtils.isEmpty(fcn) && !fcn.equals(CacheManager.paramList.get(position).getFcn())
                            && !(CacheManager.getLocState() && CacheManager.getCurrentLocation().getType() == 1)) {
                        new Timer().schedule(new TimerTask() {
                            @Override
                            public void run() {
                                CacheManager.redirect2G(null,"reject");
                            }
                        }, 5000);

                    }
                }
            }
        });


        btnCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                popupWindow.dismiss();
            }
        });
    }

    private void initAdapter(Spinner spinner, Object[] values) {
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, R.layout.spinner_select_item);

        for (int i = 0; i < values.length; i++) {
            adapter.add((String) values[i]);
        }
        adapter.setDropDownViewResource(R.layout.spinner_dropdown_item);
        spinner.setAdapter(adapter);
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
