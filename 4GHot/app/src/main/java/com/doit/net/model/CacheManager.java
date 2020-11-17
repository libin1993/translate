/*
 * Copyright (C) 2011-2016 dshine.com.cn
 * All rights reserved.
 * ShangHai Dshine - http://www.dshine.com.cn
 */
package com.doit.net.model;

import android.content.Context;
import android.text.TextUtils;

import com.doit.net.bean.DeviceState;
import com.doit.net.bean.LocationBean;
import com.doit.net.bean.LocationRptBean;
import com.doit.net.bean.LteCellConfig;
import com.doit.net.bean.LteChannelCfg;
import com.doit.net.bean.LteEquipConfig;
import com.doit.net.bean.Namelist;
import com.doit.net.bean.ScanFreqRstBean;
import com.doit.net.bean.Set2GParamsBean;
import com.doit.net.bean.UeidBean;
import com.doit.net.event.EventAdapter;
import com.doit.net.protocol.LTESendManager;
import com.doit.net.utils.LogUtils;
import com.doit.net.utils.MySweetAlertDialog;
import com.doit.net.udp.g4.bean.G4MsgChannelCfg;

import org.apache.commons.lang3.math.NumberUtils;
import org.xutils.DbManager;
import org.xutils.ex.DbException;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

/**
 * @author 杨维(wiker)
 * @version 1.0
 * @date 2016-4-26 下午3:37:39
 */
public class CacheManager {
    public static byte equipType4G;  //4G设备类型
    public static byte equipType2G;  //2G设备类型

    public static String GSMSoftwareVersion;  //GSM软件版本
    public static String CDMASoftwareVersion;  //CDMA软件版本

    public static List<UeidBean> realtimeUeidList = new ArrayList<>();

    public static List<LocationBean> locations = new ArrayList<>();
    public static LocationBean currentLoction = null;

    public static Namelist namelist = new Namelist();

    public static boolean isReportBattery = false;  //是否上报电量

    public static DeviceState deviceState = new DeviceState();

    public static List<ScanFreqRstBean> listLastScanFreqRst = new ArrayList<>();

    public static boolean locMode = false;  //是否开启搜寻功能

    public static boolean initSuccess4G = false;   //4G初始化成功
    public static boolean initSuccess2G = false;    //2G初始化成功


    public static boolean hasPressStartButton = false;  //是否已经在主页面点击开始按钮

    public static boolean checkLicense = false; //连接成功后校验证书

    public static List<Set2GParamsBean.Params> paramList = new ArrayList<>(); //2G设备参数,boardid+carrierid作为key,"00"表示移动，"01"表示联通,"10"表示电信


    public static boolean getLocMode() {
        return locMode;
    }

    private static LteCellConfig cellConfig;
    private static LteEquipConfig equipConfig;
    public static List<LteChannelCfg> channels = new ArrayList<>();


    public static void setLocMode(boolean locMode) {
        CacheManager.locMode = locMode;
    }


    public static void updateLoc(String imsi, int type) {
        if (currentLoction == null) {
            currentLoction = new LocationBean();
        }
        PrefManage.setImsi(imsi);
        currentLoction.setImsi(imsi);
        currentLoction.setType(type);
    }


    public static void stopCurrentLoc() {

        try {
            DbManager dbManager = UCSIDBManager.getDbManager();
            DBChannel dbChannel = dbManager.selector(DBChannel.class)
                    .where("band", "=", "3")
                    .and("is_check", "=", "1")
                    .findFirst();
            if (dbChannel != null) {
                LTESendManager.setChannelConfig(dbChannel.getIdx(), dbChannel.getFcn(),
                        "46001,46011", "", "", "", "", "");

                for (LteChannelCfg channel : CacheManager.channels) {
                    if (channel.getIdx().equals(dbChannel.getIdx())){
                        channel.setFcn(dbChannel.getFcn());
                        channel.setPlmn("46001,46011");
                        break;
                    }
                }
            }

        } catch (DbException e) {
            e.printStackTrace();
        }


        if (CacheManager.getCurrentLoction() != null){
            CacheManager.getCurrentLoction().setLocateStart(false);
        }

    }


    public static boolean getLocState() {
        if (currentLoction == null)
            return false;

        return currentLoction.isLocateStart();
    }

    public static LocationBean getCurrentLoction() {
        return currentLoction;
    }



    /**
     * 检查设备是否连接，并提示
     *
     * @param context
     * @return
     */
    public static boolean checkDevice(Context context) {
        if (!isDeviceOk()) {
            new MySweetAlertDialog(context, MySweetAlertDialog.ERROR_TYPE)
                    .setTitleText("错误")
                    .setContentText("设备未就绪")
                    .show();
            return false;
        }
        return true;
    }

    public static boolean isDeviceOk() {
        return initSuccess4G && cellConfig != null && channels.size() != 0 && equipConfig != null && initSuccess2G && paramList.size() > 0;
    }


    /**
     * 重置一下状态，一般设备需要重启时调用
     */
    public static void clearCache4G() {
        cellConfig = null;
        channels.clear();
        equipConfig = null;
    }


    public static LteCellConfig getCellConfig() {
        return cellConfig;
    }

    public static LteEquipConfig getLteEquipConfig() {
        return equipConfig;
    }

    public static List<LteChannelCfg> getChannels() {
        return channels;
    }

    public static void setCellConfig(LteCellConfig cellConfig) {
        CacheManager.cellConfig = cellConfig;
    }

    public static void setEquipConfig(LteEquipConfig equipConfig) {
        CacheManager.equipConfig = equipConfig;
    }

    public static void setNamelist(Namelist list) {
        namelist = list;
    }

    public synchronized static void addChannel(LteChannelCfg cfg) {
        for (LteChannelCfg channel : channels) {
            if (channel.getIdx().equals(cfg.getIdx())) {
                channel.setFcn(cfg.getFcn());
                channel.setBand(cfg.getBand());
                channel.setGa(cfg.getGa());
                channel.setPa(cfg.getPa());
                channel.setPlmn(cfg.getPlmn());
                channel.setRlm(cfg.getRlm());
                channel.setAutoOpen(cfg.getAutoOpen());
                channel.setAltFcn(cfg.getAltFcn());
                channel.setChangeBand(cfg.getChangeBand());
                return;
            }
        }

        channels.add(cfg);
        Collections.sort(channels, new Comparator<LteChannelCfg>() {
            @Override
            public int compare(LteChannelCfg lhs, LteChannelCfg rhs) {
                return NumberUtils.toInt(lhs.getPlmn()) - NumberUtils.toInt(rhs.getPlmn());
            }
        });
    }


    /**
     * 重定向到2G
     */
    public static void redirect2G(String nameListRedirect,String nameListRestAction) {

        String mobileFcn = "";
        String unicomFcn = "";

        for (Set2GParamsBean.Params params : CacheManager.paramList) {
            if (params.getBoardid().equals("0")) {
                if (params.getCarrierid().equals("0")) {
                    mobileFcn = params.getFcn();
                }

                if (params.getCarrierid().equals("1")) {
                    unicomFcn = params.getFcn();
                }
            }
        }
        LogUtils.log("重定向到2G:"+mobileFcn+","+unicomFcn);

        if (!TextUtils.isEmpty(mobileFcn) && !TextUtils.isEmpty(unicomFcn)) {
            String redirectConfig = "46000,2," + mobileFcn + "#46002,2," + mobileFcn + "#46007,2," + mobileFcn + "#46001,2," + unicomFcn;
            LTESendManager.setNameList( redirectConfig, "",
                    nameListRedirect, "", nameListRestAction, null);
        }


    }

    //将RF状态更新到内存
    public synchronized static void updateRFState(String idx, boolean rf) {

        for (LteChannelCfg channel : channels) {
            if (channel.getIdx().equals(idx)) {
                channel.setRFState(rf);
                return;
            }
        }
    }

    public static LteChannelCfg getChannelByIdx(String idx) {
        for (LteChannelCfg channel : channels) {
            if (channel.getIdx().equals(idx)) {
                return channel;
            }
        }
        return null;
    }

    private static Map<String, List<G4MsgChannelCfg>> userChannels = new HashMap<>();


    public static void setHighGa(boolean on_off) {
        if (on_off) {
            for (LteChannelCfg channel : channels) {
                if (Integer.parseInt(channel.getGa()) <= 10) {
                    LTESendManager.setChannelConfig(channel.getIdx(), "", "", "", String.valueOf(Integer.parseInt(channel.getGa()) * 5), "", "", "");
                    channel.setGa(String.valueOf(Integer.parseInt(channel.getGa()) * 5));
                }
            }
        } else {
            for (LteChannelCfg channel : channels) {
                if (Integer.parseInt(channel.getGa()) > 10) {
                    LTESendManager.setChannelConfig(channel.getIdx(), "", "", "", String.valueOf(Integer.parseInt(channel.getGa()) / 5), "", "", "");
                    channel.setGa(String.valueOf(Integer.parseInt(channel.getGa()) / 5));
                }
            }
        }
    }



    public static void changeBand(String idx, String changeBand) {

        LTESendManager.changeBand(idx, changeBand);

        //下发切换之后，等待生效，设置默认频点
        new Timer().schedule(new TimerTask() {
            @Override
            public void run() {
                String fcn = LTESendManager.getCheckedFcn(changeBand);
                if (TextUtils.isEmpty(fcn)) {
                    return;
                }
                LTESendManager.setChannelConfig(idx, fcn, "", "", "", "", "", "");
                for (LteChannelCfg channel : CacheManager.channels) {
                    if (idx.equals(channel.getIdx())){
                        channel.setFcn(fcn);
                        channel.setChangeBand(channel.getBand());
                        channel.setBand(changeBand);

                        EventAdapter.call(EventAdapter.REFRESH_DEVICE);

                        break;
                    }
                }
            }
        }, 2000);


        EventAdapter.call(EventAdapter.SHOW_PROGRESS, 13000);
    }


}
