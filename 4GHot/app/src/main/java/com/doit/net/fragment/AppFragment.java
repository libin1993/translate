package com.doit.net.fragment;

import com.doit.net.activity.Device2GParamActivity;
import com.doit.net.event.EventAdapter;
import com.doit.net.socket.ServerSocketUtils;
import com.doit.net.utils.FileUtils;
import com.doit.net.utils.NetWorkUtils;
import com.doit.net.view.ClearHistoryTimeDialog;
import com.doit.net.activity.CustomFcnActivity;
import com.doit.net.activity.DeviceParamActivity;
import com.doit.net.activity.HistoryListActivity;
import com.doit.net.activity.TestActivity;
import com.doit.net.view.LicenceDialog;
import com.doit.net.activity.SystemSettingActivity;
import com.doit.net.activity.UserManageActivity;
import com.doit.net.activity.BlacklistManagerActivity;
import com.doit.net.activity.BlackBoxActivity;
import com.doit.net.model.VersionManage;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.doit.net.base.BaseFragment;
import com.doit.net.protocol.LTESendManager;
import com.doit.net.model.AccountManage;
import com.doit.net.model.CacheManager;
import com.doit.net.model.PrefManage;
import com.doit.net.utils.LicenceUtils;
import com.doit.net.utils.MySweetAlertDialog;
import com.doit.net.utils.LogUtils;
import com.doit.net.utils.StringUtils;
import com.doit.net.utils.ToastUtils;
import com.doit.net.utils.LSettingItem;
import com.doit.net.ucsi.R;

import org.xutils.view.annotation.ViewInject;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;

import cn.pedant.SweetAlert.SweetAlertDialog;

public class AppFragment extends BaseFragment implements EventAdapter.EventCall {

    private MySweetAlertDialog mProgressDialog;

    @ViewInject(R.id.tvLoginAccount)
    private TextView tvLoginAccount;

    @ViewInject(R.id.btClearUeid)
    private LSettingItem btClearUeid;

    @ViewInject(R.id.tvLocalImsi)
    private LSettingItem tvLocalImsi;

    @ViewInject(R.id.tvSupportVoice)
    private LSettingItem tvSupportVoice;

    @ViewInject(R.id.tvVersion)
    private LSettingItem tvVersion;

    @ViewInject(R.id.btSetWhiteList)
    private LSettingItem btSetWhiteList;

    @ViewInject(R.id.btUserManage)
    private LSettingItem btUserManage;

    @ViewInject(R.id.btBlackBox)
    private LSettingItem btBlackBox;

    @ViewInject(R.id.btn_history_view)
    private LSettingItem historyItem;

    @ViewInject(R.id.btWifiSetting)
    private LSettingItem btWifiSetting;

    @ViewInject(R.id.btDeviceParam)
    private LSettingItem btDeviceParam;

    @ViewInject(R.id.rl_2g_params)
    private LSettingItem rl2GParams;

    @ViewInject(R.id.btDeviceFcn)
    private LSettingItem btDeviceFcn;

    @ViewInject(R.id.btDeviceInfoAndUpgrade)
    private LSettingItem btDeviceInfoAndUpgrade;

    @ViewInject(R.id.tvSystemSetting)
    private LSettingItem tvSystemSetting;

    @ViewInject(R.id.btAuthorizeCodeInfo)
    private LSettingItem btAuthorizeCodeInfo;

    @ViewInject(R.id.tvTest)
    private LSettingItem just4Test;

    private ListView lvPackageList;
    private ArrayAdapter upgradePackageAdapter;
    private LinearLayout layoutUpgradePackage;


    //handler消息
    private final int EXPORT_SUCCESS = 0;
    private final int EXPORT_ERROR = -1;
    private final int UPGRADE_STATUS_RPT = 1;

    public AppFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.doit_layout_app, container, false);

        EventAdapter.register(EventAdapter.UPGRADE_STATUS, this);

        return rootView;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        tvLoginAccount.setText(AccountManage.getCurrentLoginAccount());

        btSetWhiteList.setmOnLSettingItemClick(new LSettingItem.OnLSettingItemClick() {
            @Override
            public void click(LSettingItem item) {
                startActivity(new Intent(getActivity(), BlacklistManagerActivity.class));
            }
        });

        historyItem.setmOnLSettingItemClick(new LSettingItem.OnLSettingItemClick() {
            @Override
            public void click(LSettingItem item) {
                startActivity(new Intent(getActivity(), HistoryListActivity.class));
            }
        });

        btClearUeid.setmOnLSettingItemClick(clearHistoryListener);

        btUserManage.setmOnLSettingItemClick(new LSettingItem.OnLSettingItemClick() {
            @Override
            public void click(LSettingItem item) {
                if (!CacheManager.checkDevice(getActivity())) {
                    return;
                }


                startActivity(new Intent(getActivity(), UserManageActivity.class));
            }
        });

        btBlackBox.setmOnLSettingItemClick(new LSettingItem.OnLSettingItemClick() {
            @Override
            public void click(LSettingItem item) {
                if (!NetWorkUtils.getNetworkState()) {
                    ToastUtils.showMessage("设备未就绪");
                    return;
                }
                startActivity(new Intent(getActivity(), BlackBoxActivity.class));
            }
        });

        btWifiSetting.setmOnLSettingItemClick(new LSettingItem.OnLSettingItemClick() {
            @Override
            public void click(LSettingItem item) {
                startActivity(new Intent(android.provider.Settings.ACTION_WIFI_SETTINGS));
            }
        });

        tvSystemSetting.setmOnLSettingItemClick(new LSettingItem.OnLSettingItemClick() {
            @Override
            public void click(LSettingItem item) {
                startActivity(new Intent(getActivity(), SystemSettingActivity.class));
            }
        });

        btDeviceParam.setmOnLSettingItemClick(new LSettingItem.OnLSettingItemClick() {
            @Override
            public void click(LSettingItem item) {
                if (!CacheManager.checkDevice(getActivity()))
                    return;
                startActivity(new Intent(getActivity(), DeviceParamActivity.class));
            }
        });

        rl2GParams.setmOnLSettingItemClick(new LSettingItem.OnLSettingItemClick() {
            @Override
            public void click(LSettingItem item) {
                if (!CacheManager.checkDevice(getActivity()))
                    return;
                startActivity(new Intent(getActivity(), Device2GParamActivity.class));
            }
        });

        btDeviceFcn.setmOnLSettingItemClick(new LSettingItem.OnLSettingItemClick() {
            @Override
            public void click(LSettingItem item) {
                if (!CacheManager.checkDevice(getActivity()))
                    return;

                startActivity(new Intent(getActivity(), CustomFcnActivity.class));
            }
        });

        btDeviceInfoAndUpgrade.setmOnLSettingItemClick(new LSettingItem.OnLSettingItemClick() {
            @Override
            public void click(LSettingItem item) {
                showDeviceInfoDialog();
            }
        });

        btAuthorizeCodeInfo.setmOnLSettingItemClick(new LSettingItem.OnLSettingItemClick() {
            @Override
            public void click(LSettingItem item) {
                if (!CacheManager.checkDevice(getActivity()))
                    return;

                if (!TextUtils.isEmpty(LicenceUtils.authorizeCode)) {
                    LicenceDialog licenceDialog = new LicenceDialog(getActivity());
                    licenceDialog.show();
                } else {
                    ToastUtils.showMessage("获取机器码中，请稍等");
                }

            }
        });

        just4Test.setmOnLSettingItemClick(new LSettingItem.OnLSettingItemClick() {
            @Override
            public void click(LSettingItem item) {
                startActivity(new Intent(getActivity(), TestActivity.class));
            }
        });


        String imsi = getImsi();
        tvLocalImsi.setRightText(imsi);
        tvLocalImsi.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //获取剪贴板管理器：
                ClipboardManager cm = (ClipboardManager) getActivity().getSystemService(Context.CLIPBOARD_SERVICE);
                // 创建普通字符型ClipData
                ClipData mClipData = ClipData.newPlainText("Label", imsi);
                // 将ClipData内容放到系统剪贴板里。
                cm.setPrimaryClip(mClipData);
            }
        });

        if (PrefManage.supportPlay) {
            tvSupportVoice.setRightText("支持");
        } else {
            tvSupportVoice.setRightText("不支持");
        }

        tvVersion.setRightText(VersionManage.getVersionName(getContext()));
        initProgressDialog();

        if (AccountManage.getCurrentPerLevel() >= AccountManage.PERMISSION_LEVEL2) {
            btUserManage.setVisibility(View.VISIBLE);
            btBlackBox.setVisibility(View.VISIBLE);
            btClearUeid.setVisibility(View.VISIBLE);
        }

        if (AccountManage.getCurrentPerLevel() >= AccountManage.PERMISSION_LEVEL3) {
            just4Test.setVisibility(View.VISIBLE);
            tvSystemSetting.setVisibility(View.VISIBLE);
        }
    }

    private void initProgressDialog() {
        mProgressDialog = new MySweetAlertDialog(getContext(), MySweetAlertDialog.PROGRESS_TYPE);
        mProgressDialog.setTitleText("升级包正在加载，请耐心等待...");
        mProgressDialog.setCancelable(false);
    }

    private void showDeviceInfoDialog() {
        if (!CacheManager.checkDevice(getContext()))
            return;

        final View dialogView = LayoutInflater.from(getContext())
                .inflate(R.layout.layout_device_info, null);
        TextView tvDeviceIP = dialogView.findViewById(R.id.tvDeviceIP);
        tvDeviceIP.setText(ServerSocketUtils.REMOTE_4G_IP + " | " + ServerSocketUtils.REMOTE_2G_IP);

        TextView tvHwVersion = dialogView.findViewById(R.id.tvHwVersion);

        tvHwVersion.setText(CacheManager.getLteEquipConfig().getHw());

        TextView tvSwVersion = dialogView.findViewById(R.id.tvSwVersion);
        String softwareVersion = "4G:" + CacheManager.getLteEquipConfig().getSw();
        if (!TextUtils.isEmpty(CacheManager.GSMSoftwareVersion)) {
            softwareVersion += "\nGSM:" + CacheManager.GSMSoftwareVersion;
        }

        if (!TextUtils.isEmpty(CacheManager.CDMASoftwareVersion)) {
            softwareVersion += "\nCDMA:" + CacheManager.CDMASoftwareVersion;
        }
        tvSwVersion.setText(softwareVersion);



        Button btDeviceUpgrade = dialogView.findViewById(R.id.btDeviceUpgrade);
        btDeviceUpgrade.setOnClickListener(upgradeListner);
        lvPackageList = dialogView.findViewById(R.id.lvPackageList);
        layoutUpgradePackage = dialogView.findViewById(R.id.layoutUpgradePackage);

        AlertDialog.Builder dialog = new AlertDialog.Builder(getActivity());
        dialog.setView(dialogView);
        dialog.setCancelable(true);
        dialog.show();
    }

    @SuppressLint("MissingPermission")
    private String getImsi() {
        TelephonyManager telManager = (TelephonyManager) getActivity().getSystemService(Context.TELEPHONY_SERVICE);
        return StringUtils.defaultIfBlank(telManager.getSubscriberId(), getString(R.string.no_sim_card));
    }

    @SuppressLint("MissingPermission")
    private String getImei() {
        TelephonyManager telManager = (TelephonyManager) getActivity().getSystemService(Context.TELEPHONY_SERVICE);
        return StringUtils.defaultIfBlank(telManager.getDeviceId(), getString(R.string.no));
    }

    LSettingItem.OnLSettingItemClick clearHistoryListener = new LSettingItem.OnLSettingItemClick() {
        @Override
        public void click(LSettingItem item) {
            ClearHistoryTimeDialog clearHistoryTimeDialog = new ClearHistoryTimeDialog(getActivity());
            clearHistoryTimeDialog.show();
        }
    };


    private String getPackageMD5(String FilePath) {
        BigInteger bi = null;
        try {
            byte[] buffer = new byte[8192];
            int len = 0;
            MessageDigest md = MessageDigest.getInstance("MD5");
            File f = new File(FilePath);
            FileInputStream fis = new FileInputStream(f);
            while ((len = fis.read(buffer)) != -1) {
                md.update(buffer, 0, len);
            }
            fis.close();
            byte[] b = md.digest();
            bi = new BigInteger(1, b);
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return bi.toString(16);
    }

    View.OnClickListener upgradeListner = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            String UPGRADE_PACKAGE_PATH = "upgrade/";

            File file = new File(FileUtils.ROOT_PATH + UPGRADE_PACKAGE_PATH);
            if (!file.exists()) {
                ToastUtils.showMessageLong("未找到升级包，请确认已将升级包放在\"手机存储/" + FileUtils.ROOT_DIRECTORY + "/upgrade\"目录下");
                return;
            }

            File[] files = file.listFiles();
            if (files == null || files.length == 0) {
                ToastUtils.showMessageLong("未找到升级包，请确认已将升级包放在\"手机存储/" + FileUtils.ROOT_DIRECTORY + "/upgrade\"目录下");
                return;
            }

            List<String> fileList = new ArrayList<>();
            String tmpFileName = "";
            for (int i = 0; i < files.length; i++) {
                tmpFileName = files[i].getName();
                //UtilBaseLog.printLog("获取升级包：" + tmpFileName);
                if (tmpFileName.endsWith(".tgz"))
                    fileList.add(tmpFileName);
            }
            if (fileList.size() == 0) {
                ToastUtils.showMessageLong("文件错误，升级包必须是以\".tgz\"为后缀的文件");
                return;
            }

            layoutUpgradePackage.setVisibility(View.VISIBLE);
            upgradePackageAdapter = new ArrayAdapter<String>(getContext(), R.layout.comman_listview_text, fileList);
            lvPackageList.setAdapter(upgradePackageAdapter);
            lvPackageList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    final String choosePackage = fileList.get(position);
                    LogUtils.log("选择升级包：" + choosePackage);

                    new MySweetAlertDialog(getContext(), MySweetAlertDialog.WARNING_TYPE)
                            .setTitleText("提示")
                            .setContentText("选择的升级包为：" + choosePackage + ", 确定升级吗？")
                            .setCancelText(getContext().getString(R.string.cancel))
                            .setConfirmText(getContext().getString(R.string.sure))
                            .showCancelButton(true)
                            .setConfirmClickListener(new MySweetAlertDialog.OnSweetClickListener() {
                                @Override
                                public void onClick(MySweetAlertDialog sweetAlertDialog) {
                                    String md5 = getPackageMD5(FileUtils.ROOT_PATH + UPGRADE_PACKAGE_PATH + choosePackage);
                                    if ("".equals(md5)) {
                                        ToastUtils.showMessage("文件校验失败，升级取消！");
                                        sweetAlertDialog.dismiss();
                                    } else {
                                        String command = UPGRADE_PACKAGE_PATH + choosePackage + "#" + md5;
                                        LTESendManager.systemUpgrade(command);
                                        mProgressDialog.show();
                                        sweetAlertDialog.dismiss();
                                    }
                                }
                            }).show();

                }
            });
        }
    };

    Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            if (msg.what == EXPORT_SUCCESS) {
                new SweetAlertDialog(getActivity(), SweetAlertDialog.SUCCESS_TYPE)
                        .setTitleText("导出成功")
                        .setContentText("文件导出在：" + msg.obj)
                        .show();
            } else if (msg.what == EXPORT_ERROR) {
                new SweetAlertDialog(getActivity(), SweetAlertDialog.ERROR_TYPE)
                        .setTitleText("导出失败")
                        .setContentText("失败原因：" + msg.obj)
                        .show();
            } else if (msg.what == UPGRADE_STATUS_RPT) {
                if (mProgressDialog != null)
                    mProgressDialog.dismiss();
            }
        }
    };

    private void createExportError(String obj) {
        Message msg = new Message();
        msg.what = UPGRADE_STATUS_RPT;
        msg.obj = obj;
        mHandler.sendMessage(msg);
    }


    @Override
    public void call(String key, Object val) {
        if (key.equals(EventAdapter.UPGRADE_STATUS)) {
            mHandler.sendEmptyMessage(UPGRADE_STATUS_RPT);
        }
    }
}
