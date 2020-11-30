package com.doit.net.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.MenuItem;
import android.view.View;

import com.beardedhen.androidbootstrap.BootstrapButton;
import com.beardedhen.androidbootstrap.BootstrapEditText;
import com.doit.net.event.EventAdapter;
import com.doit.net.utils.FileUtils;
import com.doit.net.base.BaseActivity;
import com.doit.net.model.AccountManage;
import com.doit.net.bean.LteChannelCfg;
import com.doit.net.protocol.LTESendManager;
import com.doit.net.model.CacheManager;
import com.doit.net.utils.FTPManager;
import com.doit.net.model.PrefManage;
import com.doit.net.utils.LSettingItem;
import com.doit.net.utils.LogUtils;
import com.doit.net.utils.ToastUtils;
import com.doit.net.ucsi.R;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;

import cn.pedant.SweetAlert.SweetAlertDialog;

import static cn.pedant.SweetAlert.SweetAlertDialog.WARNING_TYPE;

public class SystemSettingActivity extends BaseActivity implements EventAdapter.EventCall {
    private Activity activity = this;
    public static String LOC_PREF_KEY = "LOC_PREF_KEY";
    public static String SET_STATIC_IP = "STATIC_IP";
    private LSettingItem tvOnOffLocation;
    private LSettingItem tvIfAutoOpenRF;
    private LSettingItem tvGeneralAdmin;
    private LSettingItem tvStaticIp;

    private BootstrapButton btSetFan;
    private BootstrapEditText etMaxWindSpeed;
    private BootstrapEditText etMinWindSpeed;
    private BootstrapEditText etTempThreshold;

    private BootstrapButton btResetFreqScanFcn;
    private BootstrapButton btRefresh;

    private long lastRefreshParamTime = 0; //防止频繁刷新参数

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_system_setting);

        tvOnOffLocation = findViewById(R.id.tvOnOffLocation);
        tvOnOffLocation.setOnLSettingCheckedChange(settingItemLocSwitch);
        tvOnOffLocation.setmOnLSettingItemClick(settingItemLocSwitch);  //点击该行开关以外地方也会切换开关，故覆盖其回调

        tvIfAutoOpenRF = findViewById(R.id.tvIfAutoOpenRF);
        tvIfAutoOpenRF.setOnLSettingCheckedChange(settingItemAutoRFSwitch);
        tvIfAutoOpenRF.setmOnLSettingItemClick(settingItemAutoRFSwitch);

        tvGeneralAdmin = findViewById(R.id.tvGeneralAdmin);
        tvGeneralAdmin.setmOnLSettingItemClick(generalAdminAccount);

        etMaxWindSpeed = findViewById(R.id.etMaxWindSpeed);
        etMinWindSpeed = findViewById(R.id.etMinWindSpeed);
        etTempThreshold = findViewById(R.id.etTempThreshold);
        btSetFan = findViewById(R.id.btSetFan);
        btSetFan.setOnClickListener(setFanClikListen);

        btResetFreqScanFcn = findViewById(R.id.btResetFreqScanFcn);
        btResetFreqScanFcn.setOnClickListener(resetFreqScanFcnClikListener);

        btRefresh = findViewById(R.id.btRefresh);
        btRefresh.setOnClickListener(refreshClikListen);

        if (PrefManage.getBoolean(LOC_PREF_KEY, true)) {
            tvOnOffLocation.setChecked(true);
        } else {
            tvOnOffLocation.setChecked(false);
        }

        tvStaticIp = findViewById(R.id.tv_static_ip);
        tvStaticIp.setChecked(PrefManage.getBoolean(SET_STATIC_IP, true));
        tvStaticIp.setOnLSettingCheckedChange(setStaticIpSwitch);
        tvStaticIp.setmOnLSettingItemClick(setStaticIpSwitch);


        if (CacheManager.checkDevice(activity)) {
            initView();
        } else {
            ToastUtils.showMessageLong("设备未连接，当前展示的设置都不准确，请等待设备连接后重新进入该界面");
        }

        EventAdapter.register(EventAdapter.REFRESH_DEVICE, this);

    }

    private void initView() {
        etMaxWindSpeed.setText(CacheManager.getLteEquipConfig().getMaxFanSpeed());
        etMinWindSpeed.setText(CacheManager.getLteEquipConfig().getMinFanSpeed());
        etTempThreshold.setText(CacheManager.getLteEquipConfig().getTempThreshold());

        tvIfAutoOpenRF.setChecked(CacheManager.getChannels().get(0).getAutoOpen().equals("1"));
    }

    private LSettingItem.OnLSettingItemClick settingItemLocSwitch = new LSettingItem.OnLSettingItemClick() {
        @Override
        public void click(LSettingItem item) {
            if (tvOnOffLocation.isChecked()) {
                PrefManage.setBoolean(LOC_PREF_KEY, true);
            } else {
                PrefManage.setBoolean(LOC_PREF_KEY, false);
            }

            ToastUtils.showMessage("设置成功，重新登陆生效。");
        }
    };

    private LSettingItem.OnLSettingItemClick settingItemAutoRFSwitch = new LSettingItem.OnLSettingItemClick() {
        @Override
        public void click(LSettingItem item) {
            if (!CacheManager.checkDevice(activity)) {
                tvIfAutoOpenRF.setChecked(!tvIfAutoOpenRF.isChecked());
                return;
            }

            LTESendManager.setAutoRF(tvIfAutoOpenRF.isChecked());

            ToastUtils.showMessage("下次开机生效");
        }
    };

    private LSettingItem.OnLSettingItemClick setStaticIpSwitch = new LSettingItem.OnLSettingItemClick() {
        @Override
        public void click(LSettingItem item) {
            if (tvStaticIp.isChecked()) {
                PrefManage.setBoolean(SET_STATIC_IP, true);
                ToastUtils.showMessage("已开启自动连接，无需配置WIFI静态IP，以后将自动连接设备");
            } else {
                PrefManage.setBoolean(SET_STATIC_IP, false);
                ToastUtils.showMessageLong("已关闭自动连接，请配置WIFI静态IP，否则将无法连接设备");
            }


        }
    };


    private LSettingItem.OnLSettingItemClick generalAdminAccount = new LSettingItem.OnLSettingItemClick() {
        @Override
        public void click(LSettingItem item) {
            generalAdmin();
        }
    };

    private void generalAdmin() {
        String accountFullPath = FileUtils.ROOT_PATH + "FtpAccount/";
        String accountFileName = "account";


        File namelistFile = new File(accountFullPath + accountFileName);
        if (namelistFile.exists()) {
            namelistFile.delete();
        }

        BufferedWriter bufferedWriter = null;
        try {
            bufferedWriter = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(accountFullPath + accountFileName, true)));
            bufferedWriter.write("admin" + "," + "admin" + "," + AccountManage.getAdminRemark() + "\n");
            bufferedWriter.flush();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (bufferedWriter != null) {
                try {
                    bufferedWriter.close();
                } catch (IOException e) {
                }
            }
        }

        new Thread() {
            public void run() {
                try {
                    FTPManager.getInstance().connect();
                    if (FTPManager.getInstance().uploadFile(false, accountFullPath, accountFileName)) {
                        ToastUtils.showMessage("生成管理员账号成功");
                    } else {
                        ToastUtils.showMessage("生成管理员账号出错");
                    }
                    AccountManage.deleteAccountFile();
                } catch (Exception e) {
                    ToastUtils.showMessage("生成管理员账号出错");
                    e.printStackTrace();
                }
            }
        }.start();
    }

    View.OnClickListener setFanClikListen = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if (!CacheManager.checkDevice(activity))
                return;

            LTESendManager.setFancontrol(etMaxWindSpeed.getText().toString(), etMinWindSpeed.getText().toString()
                    , etTempThreshold.getText().toString());
        }
    };


    View.OnClickListener resetFreqScanFcnClikListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if (!CacheManager.checkDevice(activity))
                return;

            new SweetAlertDialog(activity, WARNING_TYPE)
                    .setTitleText("提示")
                    .setContentText("开机搜网列表将被重置，确定吗?")
                    .setCancelText(activity.getString(R.string.cancel))
                    .setConfirmText(activity.getString(R.string.sure))
                    .showCancelButton(true)
                    .setConfirmClickListener(new SweetAlertDialog.OnSweetClickListener() {
                        @Override
                        public void onClick(SweetAlertDialog sweetAlertDialog) {
                            resetFreqScanFcn();
                            sweetAlertDialog.dismiss();
                        }
                    }).show();


        }
    };

    private void resetFreqScanFcn() {
        String band1Fcns = "100,375,400";
        String band3Fcns = "1300,1506,1650,1825";
        String band38Fcns = "37900,38098,38200";
        String band39Fcns = "38400,38544,38300";
        String band40Fcns = "38950,39148,39300";
        String band41Fcns = "40540,40936,41134";


        for (LteChannelCfg channel : CacheManager.getChannels()) {
            switch (channel.getBand()) {
                case "1":
                    LTESendManager.setChannelConfig(channel.getIdx(), "", "", "", "", "", "", band1Fcns);
                    channel.setAltFcn(band1Fcns);
                    break;
                case "3":
                    LTESendManager.setChannelConfig(channel.getIdx(), "", "", "", "", "", "", band3Fcns);
                    channel.setAltFcn(band3Fcns);
                    break;
                case "38":
                    LTESendManager.setChannelConfig(channel.getIdx(), "", "", "", "", "", "", band38Fcns);
                    channel.setAltFcn(band38Fcns);
                    break;

                case "39":
                    LTESendManager.setChannelConfig(channel.getIdx(), "", "", "", "", "", "", band39Fcns);
                    channel.setAltFcn(band39Fcns);
                    break;

                case "40":
                    LTESendManager.setChannelConfig(channel.getIdx(), "", "", "", "", "", "", band40Fcns);
                    channel.setAltFcn(band40Fcns);
                    break;
                case "41":
                    LTESendManager.setChannelConfig(channel.getIdx(), "", "", "", "", "", "", band41Fcns);
                    channel.setAltFcn(band41Fcns);
                    break;

                default:
                    break;
            }
        }
    }

    View.OnClickListener refreshClikListen = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if (!CacheManager.checkDevice(activity))
                return;

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


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        /* 为解决从后台切回来之后重新打开启动屏及登录界面问题，需要设置点击子activity时强制打开MainActivity
         * 否则会出现在子activity点击返回直接将app切到后台(为防止mainActivity重复加载，已将其设置为singleTop启动) */
        switch (item.getItemId()) {
            case android.R.id.home:
                // 点击返回按钮，退回上一层Activity
                finish();
                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void call(String key, Object val) {
        if (EventAdapter.REFRESH_DEVICE.equals(key)) {
            mHandler.sendEmptyMessage(0);
        }
    }


    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            if (msg.what == 0) {
                initView();
            }
        }
    };
}
