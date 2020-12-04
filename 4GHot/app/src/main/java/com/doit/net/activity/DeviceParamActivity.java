package com.doit.net.activity;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.support.annotation.Nullable;
import android.os.Bundle;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.BaseViewHolder;
import com.doit.net.utils.FormatUtils;
import com.doit.net.utils.ScreenUtils;
import com.doit.net.view.ChangeFcnDialog;
import com.doit.net.view.SystemSetupDialog;
import com.doit.net.adapter.UserChannelListAdapter;
import com.doit.net.base.BaseActivity;
import com.doit.net.utils.MySweetAlertDialog;
import com.doit.net.utils.LogUtils;
import com.doit.net.ucsi.R;

import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;

import android.os.Handler;
import android.os.Message;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.RadioButton;
import android.widget.RadioGroup;

import com.doit.net.bean.LteChannelCfg;
import com.doit.net.model.BlackBoxManger;
import com.doit.net.event.EventAdapter;
import com.doit.net.protocol.LTESendManager;
import com.doit.net.model.CacheManager;

import com.doit.net.utils.ToastUtils;
import com.orhanobut.dialogplus.DialogPlus;
import com.orhanobut.dialogplus.ListHolder;
import com.orhanobut.dialogplus.OnItemClickListener;

import java.util.Timer;
import java.util.TimerTask;


/**
 * 设备参数
 */
public class DeviceParamActivity extends BaseActivity implements EventAdapter.EventCall {
    private final Activity activity = this;


    private Button btSetCellParam;
    private Button btSetChannelCfg;
    private Button btUpdateTac;
    private Button btRebootDevice;
    private Button btRefreshParam;
    private Button btnCTCFcn;
    private RecyclerView rvBand;
    private long lastRefreshParamTime = 0; //防止频繁刷新参数

    private RadioGroup rgPowerLevel;
    private RadioButton rbPowerHigh;
    private RadioButton rbPowerMedium;
    private RadioButton rbPowerLow;
    private final int POWER_LEVEL_HIGH = 0; //功率等级乘-5+Pmax作为功率设置下去
    private final int POWER_LEVEL_MEDIUM = 3;
    private final int POWER_LEVEL_LOW = 6;
    private RadioButton lastPowerPress;

    private RadioGroup rgDetectCarrierOperate;
    private RadioButton rbDetectAll;
    private RadioButton rbCTJ;
    private RadioButton rbCTU;
    private RadioButton rbCTC;
    private RadioButton lastDetectCarrierOperatePress;

    private CheckBox cbRFSwitch;

    private MySweetAlertDialog mProgressDialog;


    //handler消息
    private final int UPDATE_VIEW = 0;
    private final int SHOW_PROGRESS = 1;

    private BaseQuickAdapter<LteChannelCfg, BaseViewHolder> adapter;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_device_param);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        intView();
        initEvent();
    }

    private void initEvent() {
        EventAdapter.register(EventAdapter.REFRESH_DEVICE, this);
    }

    private void intView() {
        btSetCellParam = findViewById(R.id.btSetCellParam);
        btSetCellParam.setOnClickListener(setCellParamClick);
        btSetChannelCfg = findViewById(R.id.btSetChannelCfg);
        btSetChannelCfg.setOnClickListener(setChannelCfgClick);
        btUpdateTac = findViewById(R.id.btUpdateTac);
        btUpdateTac.setOnClickListener(updateTacClick);
        btRebootDevice = findViewById(R.id.btRebootDevice);
        btRebootDevice.setOnClickListener(rebootDeviceClick);
        btRefreshParam = findViewById(R.id.btRefreshParam);
        btRefreshParam.setOnClickListener(refreshParamClick);
        btnCTCFcn = findViewById(R.id.btn_ctc_fcn);


        btnCTCFcn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new ChangeFcnDialog(DeviceParamActivity.this).show();
            }
        });


        rvBand = findViewById(R.id.rv_band);

        rgPowerLevel = findViewById(R.id.rgPowerLevel);
        rbPowerHigh = findViewById(R.id.rbPowerHigh);
        rbPowerMedium = findViewById(R.id.rbPowerMedium);
        rbPowerLow = findViewById(R.id.rbPowerLow);
        lastPowerPress = rbPowerHigh;
        rgPowerLevel.setOnCheckedChangeListener(powerLevelListener);

        rgDetectCarrierOperate = findViewById(R.id.rgDetectCarrierOperate);
        rbDetectAll = findViewById(R.id.rbDetectAll);
        rbCTJ = findViewById(R.id.rbCTJ);
        rbCTU = findViewById(R.id.rbCTU);
        rbCTC = findViewById(R.id.rbCTC);
        lastDetectCarrierOperatePress = rbDetectAll;
        rgDetectCarrierOperate.setOnCheckedChangeListener(detectCarrierOperateListener);

        cbRFSwitch = findViewById(R.id.cbRFSwitch);
        cbRFSwitch.setOnCheckedChangeListener(rfCheckChangeListener);


        rvBand.setLayoutManager(new LinearLayoutManager(this));
        adapter = new BaseQuickAdapter<LteChannelCfg, BaseViewHolder>(R.layout.layout_rv_band_item, CacheManager.channels) {
            @Override
            protected void convert(BaseViewHolder helper, LteChannelCfg item) {
                helper.setText(R.id.tv_band_info, "通道：" + item.getIdx() + "        " + "频段：" + item.getBand() + "\n" +
                        "频点：[" + item.getFcn() + "]");
                ImageView ivRFStatus = helper.getView(R.id.iv_rf_status);
                if (item.getRFState()) {
                    ivRFStatus.setImageResource(R.drawable.switch_open);
                } else {
                    ivRFStatus.setImageResource(R.drawable.switch_close);
                }

                helper.addOnClickListener(R.id.iv_rf_status);
            }
        };
        rvBand.setAdapter(adapter);
        adapter.setOnItemClickListener(new BaseQuickAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(BaseQuickAdapter adapter, View view, int position) {
                LteChannelCfg lteChannelCfg = CacheManager.channels.get(position);
                if (lteChannelCfg != null && !TextUtils.isEmpty(lteChannelCfg.getChangeBand())) {
                    changeChannelBandDialog(lteChannelCfg.getIdx(), lteChannelCfg.getChangeBand());
                }
            }
        });
        adapter.setOnItemChildClickListener(new BaseQuickAdapter.OnItemChildClickListener() {
            @Override
            public void onItemChildClick(BaseQuickAdapter adapter, View view, int position) {
                if (view.getId() == R.id.iv_rf_status) {
                    if (!CacheManager.checkDevice(DeviceParamActivity.this)) {
                        return;
                    }

                    LteChannelCfg lteChannelCfg = CacheManager.channels.get(position);

                    if (lteChannelCfg == null) {
                        return;
                    }

                    if (CacheManager.getLocState()) {
                        ToastUtils.showMessageLong("当前正在搜寻中，请确认通道射频变动是否对其产生影响！");
                    }

                    showProcess(6000);
                    if (lteChannelCfg.getRFState()) {
                        LTESendManager.closeRf(lteChannelCfg.getIdx());
                    } else {
                        LTESendManager.openRf(lteChannelCfg.getIdx());
                    }

                }

            }
        });

        mProgressDialog = new MySweetAlertDialog(activity, MySweetAlertDialog.PROGRESS_TYPE);
        mProgressDialog.setTitleText("Loading...");
        mProgressDialog.setCancelable(false);


        LTESendManager.getEquipAndAllChannelConfig();
    }

    View.OnClickListener setCellParamClick = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if (!CacheManager.checkDevice(activity)) {
                return;
            }
            new SystemSetupDialog(activity).show();
        }
    };

    View.OnClickListener setChannelCfgClick = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if (!CacheManager.checkDevice(activity)) {
                return;
            }


            setChannel();
        }
    };


    /**
     * 设置通道
     */
    private void setChannel() {
        View dialogView = LayoutInflater.from(this).inflate(R.layout.doit_layout_channels_dialog, null);
        PopupWindow popupWindow = new PopupWindow(dialogView, ScreenUtils.getInstance()
                .getScreenWidth(DeviceParamActivity.this) - FormatUtils.getInstance().dip2px(40),
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
        BaseQuickAdapter<LteChannelCfg, BaseViewHolder> adapter = new BaseQuickAdapter<LteChannelCfg,
                BaseViewHolder>(R.layout.doit_layout_channel_item, CacheManager.getChannels()) {
            @Override
            protected void convert(BaseViewHolder helper, LteChannelCfg item) {
                helper.setText(R.id.title_text, "通道：" + item.getIdx() + "    " + "频段:" + item.getBand());

                helper.setText(R.id.editText_fcn, item.getFcn() == null ? "" : "" + item.getFcn());
                helper.setText(R.id.editText_plmn, item.getPlmn());
                helper.setText(R.id.editText_ga, item.getGa() == null ? "" : "" + item.getGa());
                helper.setText(R.id.editText_pa, item.getPa() == null ? "" : "" + item.getPa());
                helper.setText(R.id.etRLM, item.getRlm() == null ? "" : "" + item.getRlm());
                helper.setText(R.id.etAltFcn, item.getAltFcn() == null ? "" : "" + item.getAltFcn());

                helper.addOnClickListener(R.id.button_save);
            }
        };
        rvChannel.setAdapter(adapter);

        adapter.setOnItemChildClickListener(new BaseQuickAdapter.OnItemChildClickListener() {
            @Override
            public void onItemChildClick(BaseQuickAdapter adapter, View view, int position) {

                if (view.getId() == R.id.button_save) {
                    if (!CacheManager.checkDevice(DeviceParamActivity.this)) {
                        return;
                    }

                    EditText etFcn = (EditText) adapter.getViewByPosition(rvChannel, position, R.id.editText_fcn);
                    EditText etPlmn = (EditText) adapter.getViewByPosition(rvChannel, position, R.id.editText_plmn);
                    EditText etGa = (EditText) adapter.getViewByPosition(rvChannel, position, R.id.editText_ga);
                    EditText etPa = (EditText) adapter.getViewByPosition(rvChannel, position, R.id.editText_pa);
                    EditText etRLM = (EditText) adapter.getViewByPosition(rvChannel, position, R.id.etRLM);
                    EditText etAltFcn = (EditText) adapter.getViewByPosition(rvChannel, position, R.id.etAltFcn);

                    String fcn = etFcn.getText().toString().trim();
                    String plmn = etPlmn.getText().toString().trim();
                    String pa = etPa.getText().toString().trim();
                    String ga = etGa.getText().toString().trim();
                    String rlm = etRLM.getText().toString().trim();
                    String alt_fcn = etAltFcn.getText().toString().trim();

                    //不为空校验正则，为空不上传
                    if (!TextUtils.isEmpty(fcn)) {
                        if (!FormatUtils.getInstance().matchFCN(fcn)) {
                            ToastUtils.showMessage("FCN格式输入有误,请检查");
                            return;
                        } else {
                            if (!FormatUtils.getInstance().
                                    fcnRange(CacheManager.channels.get(position).getBand(), fcn)) {
                                ToastUtils.showMessage("FCN格式输入范围有误,请检查");
                                return;
                            }

                        }
                    }


                    ToastUtils.showMessage(R.string.tip_15);
                    showProcess(6000);


                    LteChannelCfg channelCfg = CacheManager.channels.get(position);
                    if (!TextUtils.isEmpty(fcn)){
                        channelCfg.setFcn(fcn);
                    }

                    if (!TextUtils.isEmpty(plmn)){
                        channelCfg.setPlmn(plmn);
                    }

                    if (!TextUtils.isEmpty(ga)){
                        channelCfg.setGa(ga);
                    }

                    if (!TextUtils.isEmpty(pa)){
                        channelCfg.setPa(pa);
                    }
                    if (!TextUtils.isEmpty(rlm)){
                        channelCfg.setRlm(rlm);
                    }
                    if (!TextUtils.isEmpty(alt_fcn)){
                        channelCfg.setAltFcn(alt_fcn);
                    }


                    LTESendManager.setChannelConfig(CacheManager.channels.get(position).getIdx(),
                            fcn, plmn, pa, ga, rlm, "", alt_fcn);

                    refreshViews();

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

    View.OnClickListener updateTacClick = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if (!CacheManager.checkDevice(activity)) {
                return;
            }

            LTESendManager.changeTac();

            EventAdapter.call(EventAdapter.ADD_BLACKBOX, BlackBoxManger.CHANNEL_TAG);
        }
    };

    View.OnClickListener rebootDeviceClick = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if (!CacheManager.checkDevice(activity)) {
                return;
            }

            new MySweetAlertDialog(activity, MySweetAlertDialog.WARNING_TYPE)
                    .setTitleText("设备重启")
                    .setContentText("确定重启设备")
                    .setCancelText(getString(R.string.cancel))
                    .setConfirmText(getString(R.string.sure))
                    .showCancelButton(true)
                    .setConfirmClickListener(new MySweetAlertDialog.OnSweetClickListener() {

                        @Override
                        public void onClick(MySweetAlertDialog sweetAlertDialog) {
                            sweetAlertDialog.dismiss();
                            LTESendManager.reboot();
                        }
                    })
                    .show();
        }
    };

    View.OnClickListener refreshParamClick = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if (!CacheManager.checkDevice(activity)) {
                return;
            }

            long currentTime = System.currentTimeMillis();
            if (currentTime - lastRefreshParamTime > 20 * 1000) {
                LTESendManager.getEquipAndAllChannelConfig();
                lastRefreshParamTime = currentTime;
                ToastUtils.showMessage("下发查询参数成功！");
            } else {
                ToastUtils.showMessage("请勿频繁刷新参数！");
            }
        }
    };

    RadioGroup.OnCheckedChangeListener powerLevelListener = new RadioGroup.OnCheckedChangeListener() {
        @Override
        public void onCheckedChanged(RadioGroup group, int checkedId) {
            if (!(group.findViewById(checkedId).isPressed())) {
                return;
            }

            if (!CacheManager.checkDevice(activity)) {
                lastPowerPress.setChecked(true);
                return;
            }


            if (CacheManager.getLocState()) {
                ToastUtils.showMessage("当前正在搜寻中，请留意功率变动是否对其产生影响！");
            } else {
                ToastUtils.showMessageLong("功率设置已下发，请等待其生效");
            }


            showProcess(9000);
            switch (checkedId) {
                case R.id.rbPowerHigh:

                    setPowerLevel(POWER_LEVEL_HIGH);
                    lastPowerPress = rbPowerHigh;
                    EventAdapter.call(EventAdapter.ADD_BLACKBOX, BlackBoxManger.SET_4G_POWER + "高");
                    break;

                case R.id.rbPowerMedium:
                    setPowerLevel(POWER_LEVEL_MEDIUM);
                    lastPowerPress = rbPowerMedium;
                    EventAdapter.call(EventAdapter.ADD_BLACKBOX, BlackBoxManger.SET_4G_POWER + "中");
                    break;

                case R.id.rbPowerLow:
                    setPowerLevel(POWER_LEVEL_LOW);
                    lastPowerPress = rbPowerLow;
                    EventAdapter.call(EventAdapter.ADD_BLACKBOX, BlackBoxManger.SET_4G_POWER + "低");
                    break;
            }

        }
    };

    RadioGroup.OnCheckedChangeListener detectCarrierOperateListener = new RadioGroup.OnCheckedChangeListener() {
        @Override
        public void onCheckedChanged(RadioGroup group, int checkedId) {
            if (!(group.findViewById(checkedId).isPressed())) {
                return;
            }

            if (!CacheManager.checkDevice(activity)) {
                lastDetectCarrierOperatePress.setChecked(true);
                return;
            }

            if (CacheManager.getLocState()) {
                ToastUtils.showMessage("当前正在定位中，无法切换侦码制式！");
                lastDetectCarrierOperatePress.setChecked(true);
                return;
            } else {
                ToastUtils.showMessageLong("侦码制式设置已下发，请等待其生效");
            }


            switch (checkedId) {
                case R.id.rbDetectAll:
                    LTESendManager.setDetectCarrierOpetation("detect_all");
                    lastDetectCarrierOperatePress = rbDetectAll;
                    EventAdapter.call(EventAdapter.ADD_BLACKBOX, BlackBoxManger.CHANGE_DETTECT_OPERATE + "所有");
                    break;

                case R.id.rbCTJ:
                    LTESendManager.setDetectCarrierOpetation("detect_ctj");
                    lastDetectCarrierOperatePress = rbCTJ;
                    EventAdapter.call(EventAdapter.ADD_BLACKBOX, BlackBoxManger.CHANGE_DETTECT_OPERATE + "移动");
                    break;

                case R.id.rbCTU:
                    LTESendManager.setDetectCarrierOpetation("detect_ctu");
                    lastDetectCarrierOperatePress = rbCTU;
                    EventAdapter.call(EventAdapter.ADD_BLACKBOX, BlackBoxManger.CHANGE_DETTECT_OPERATE + "联通");
                    break;

                case R.id.rbCTC:
                    LTESendManager.setDetectCarrierOpetation("detect_ctc");
                    lastDetectCarrierOperatePress = rbCTC;
                    EventAdapter.call(EventAdapter.ADD_BLACKBOX, BlackBoxManger.CHANGE_DETTECT_OPERATE + "电信");
                    break;
            }

            showProcess(8000);

        }
    };


    CompoundButton.OnCheckedChangeListener rfCheckChangeListener = new CompoundButton.OnCheckedChangeListener() {
        @Override
        public void onCheckedChanged(CompoundButton compoundButton, boolean isChecked) {
            if (!compoundButton.isPressed()) {
                return;
            }

            if (!CacheManager.checkDevice(activity)) {
                cbRFSwitch.setChecked(!isChecked);
                return;
            }


            if (isChecked) {
                LTESendManager.openAllRf();
                ToastUtils.showMessageLong(R.string.rf_open);
                EventAdapter.call(EventAdapter.ADD_BLACKBOX, BlackBoxManger.OPEN_ALL_4G_RF);
                showProcess(6000);
            } else {
                if (CacheManager.getLocState()) {
                    new MySweetAlertDialog(activity, MySweetAlertDialog.WARNING_TYPE)
                            .setTitleText("提示")
                            .setContentText("当前正在搜寻，确定关闭吗？")
                            .setCancelText(getString(R.string.cancel))
                            .setConfirmText(getString(R.string.sure))
                            .showCancelButton(true)
                            .setConfirmClickListener(new MySweetAlertDialog.OnSweetClickListener() {

                                @Override
                                public void onClick(MySweetAlertDialog sweetAlertDialog) {
                                    sweetAlertDialog.dismiss();

                                    ToastUtils.showMessage(R.string.rf_close);
                                    showProcess(8000);

                                    LTESendManager.closeAllRf();

                                    EventAdapter.call(EventAdapter.ADD_BLACKBOX, BlackBoxManger.CLOSE_ALL_4G_RF);
                                }
                            })
                            .show();
                } else {
                    LTESendManager.closeAllRf();
                    ToastUtils.showMessageLong(R.string.rf_close);
                    showProcess(8000);
                    EventAdapter.call(EventAdapter.ADD_BLACKBOX, BlackBoxManger.CLOSE_ALL_4G_RF);
                }

            }
        }
    };

    public void setPowerLevel(int powerLevel) {
        if (!CacheManager.isDeviceOk()) {
            return;
        }

        int powerAtt = powerLevel * -5;

        for (int i = 0; i < CacheManager.getChannels().size(); i++) {
            int index = i;
            new Timer().schedule(new TimerTask() {
                @Override
                public void run() {
                    LteChannelCfg channel = CacheManager.getChannels().get(index);
                    int pa = powerAtt + Integer.parseInt(channel.getPMax());

                    LTESendManager.setChannelConfig(channel.getIdx(), "", "", pa + "," + pa + "," + pa,
                            "", "", "", "");
                    channel.setPa(pa + "," + pa + "," + pa);
                }
            }, index*200);
        }
        refreshViews();
    }

    public void refreshViews() {
        refreshDetectOperation();
        refreshPowerLevel();
        refreshRFSwitch();
        adapter.notifyDataSetChanged();

    }

    private void refreshRFSwitch() {
        boolean rfState = false;

        for (LteChannelCfg channel : CacheManager.getChannels()) {
            if (channel.getRFState()) {
                rfState = true;
                break;
            }
        }

        cbRFSwitch.setOnCheckedChangeListener(null);
        cbRFSwitch.setChecked(rfState);
        cbRFSwitch.setOnCheckedChangeListener(rfCheckChangeListener);
    }

    private void refreshDetectOperation() {
        if (CacheManager.getChannels().size() > 0) {
            String firstPlnm = CacheManager.getChannels().get(0).getPlmn();  //以第一个作为参考
            if (TextUtils.isEmpty(firstPlnm)){
                return;
            }
            if (firstPlnm.contains("46000") && firstPlnm.contains("46001") && firstPlnm.contains("46011")) {
                rbDetectAll.setChecked(true);
            } else if (firstPlnm.equals("46000,46000,46000")) {
                rbCTJ.setChecked(true);
            } else if (firstPlnm.equals("46001,46001,46001")) {
                rbCTU.setChecked(true);
            } else if (firstPlnm.equals("46011,46011,46011")) {
                rbCTC.setChecked(true);
            }
        }
    }

    private void refreshPowerLevel() {
        //定位下有不同频点的功率变动，故不刷新&& !CacheManager.getLocState()
        if (CacheManager.isDeviceOk()) {
            int powerLevel = (Integer.parseInt(CacheManager.getChannels().get(0).getPa().split(",")[0]) -
                    Integer.parseInt(CacheManager.getChannels().get(0).getPMax())) / -5;


            //-1  -16 -31
            //1    3   6
            if (powerLevel <= 1)
                rbPowerHigh.setChecked(true);
            else if (powerLevel < 5)
                rbPowerMedium.setChecked(true);
            else rbPowerLow.setChecked(true);
        }

    }

    private void showProcess(int keepTime) {
        Message msg = new Message();
        msg.what = SHOW_PROGRESS;
        msg.obj = keepTime;
        mHandler.sendMessage(msg);
    }

    @Override
    public void onResume() {
        refreshViews();
        super.onResume();
    }

    private void changeChannelBandDialog(final String idx, final String band) {
        UserChannelListAdapter adapter = new UserChannelListAdapter(activity);
        adapter.setIdx(idx);
        //显示设备管理界面
        DialogPlus deviceListDialog = DialogPlus.newDialog(activity)
                .setContentHolder(new ListHolder())
                .setHeader(R.layout.user_channel_header)
                .setCancelable(true)
                .setGravity(Gravity.CENTER)
                .setAdapter(adapter)
                .setOnItemClickListener(new OnItemClickListener() {
                    @Override
                    public void onItemClick(DialogPlus dialog, Object item, View view, int position) {
                        CacheManager.changeBand(idx, band);
                        dialog.dismiss();
                        ToastUtils.showMessageLong("切换Band命令已下发，请等待生效");
                        EventAdapter.call(EventAdapter.ADD_BLACKBOX, BlackBoxManger.CHANGE_BAND + band);
                    }
                })
                .setContentHeight(ViewGroup.LayoutParams.WRAP_CONTENT)
                .setOverlayBackgroundResource(R.color.dark_background)
                .setExpanded(false)
                .create();

        ListView list = ((ListView) deviceListDialog.getHolderView());
        list.setDivider(new ColorDrawable(Color.GRAY));
        list.setDividerHeight(1);
        deviceListDialog.show();
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
        switch (key) {
            case EventAdapter.REFRESH_DEVICE:
                mHandler.sendEmptyMessage(UPDATE_VIEW);
                break;
        }
    }
}

