/*
 * Copyright (C) 2011-2016 dshine.com.cn
 * All rights reserved.
 * ShangHai Dshine - http://www.dshine.com.cn
 */
package com.doit.net.model;

import android.content.Context;
import android.os.Build;
import android.telephony.SubscriptionManager;
import android.telephony.TelephonyManager;
import android.text.TextUtils;

import com.doit.net.application.MyApplication;
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
import com.doit.net.protocol.Send2GManager;
import com.doit.net.utils.LogUtils;
import com.doit.net.utils.MySweetAlertDialog;
import com.doit.net.udp.g4.bean.G4MsgChannelCfg;

import org.apache.commons.lang3.math.NumberUtils;
import org.xutils.DbManager;
import org.xutils.ex.DbException;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
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

    public static List<UeidBean> realtimeUeidList = new ArrayList<>();
    public static final int MAX_REALTIME_LIST_SIZE = 300;


    public static List<LocationBean> locations = new ArrayList<>();
    public static LocationBean currentLoction = null;
    public static List<LocationRptBean> locationRpts = new ArrayList<>();

    public static Namelist namelist = new Namelist();


    public static String currentWorkMode = "2";   //0公安侦码  2军队管控
    public static boolean isReportBattery = false;  //是否上报电量

    public static DeviceState deviceState = new DeviceState();

    public static List<ScanFreqRstBean> listLastScanFreqRst = new ArrayList<>();

    public static boolean loc_mode = false;  //是否开启搜寻功能

    public static boolean initSuccess4G = false;   //4G初始化成功
    public static boolean initSuccess2G = false;    //2G初始化成功

    public static boolean isWifiConnected = false;   //wifi是否连接成功

    public static boolean hasPressStartButton = false;  //是否已经在主页面点击开始按钮

    public static boolean checkLicense = false; //连接成功后校验证书

    public static List<Set2GParamsBean.Params> paramList = new ArrayList<>(); //2G设备参数,boardid+carrierid作为key,"00"表示移动，"01"表示联通,"10"表示电信


    public static boolean getLocMode() {
        return loc_mode;
    }

    private static LteCellConfig cellConfig;
    private static LteEquipConfig equipConfig;
    public static List<LteChannelCfg> channels = new ArrayList<>();


    public static void setLocMode(boolean locMode) {
        loc_mode = locMode;
    }


    public synchronized static void addRealtimeUeidList(List<UeidBean> listUeid) {
        addToList(listUeid);

        /* 如果实时上报界面没加载就有数据上传，就会丢失数据
           所以将存储数据库操作移到processUeidRpt */
//        try {
//            DbManager dbManager = UCSIDBManager.getDbManager();
//            for (int i= 0; i < listUeid.size(); i++){
//                DBUeidInfo info = new DBUeidInfo();
//                info.setImsi(listUeid.get(i).getImsi());
//                info.setTmsi(listUeid.get(i).getTmsi());
//                //info.setCreateDate(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse(listUeid.get(i).getRptTime()));
//                info.setCreateDate(DateUtil.convert2long(listUeid.get(i).getRptTime(), DateUtil.LOCAL_DATE));
//                info.setLongitude(listUeid.get(i).getLongitude());
//                info.setLatitude(listUeid.get(i).getLatitude());
//                dbManager.save(info);
//            }
//        } catch (DbException e) {
//            log.error("插入UEID 到数据库异常",e);
//        }
    }

    public synchronized static void addToList(List<UeidBean> listUeid) {
        if ((realtimeUeidList.size() + listUeid.size()) >= MAX_REALTIME_LIST_SIZE) {
            for (int i = 0; i < (realtimeUeidList.size() + listUeid.size() - MAX_REALTIME_LIST_SIZE); i++)
                //realtimeUeidList.remove(realtimeUeidList.size()-1);
                realtimeUeidList.remove(0);
        }

        //最新的放前面
        Collections.reverse(listUeid);
        realtimeUeidList.addAll(0, listUeid);
    }


    public static void updateLoc(String imsi, int type) {
        if (currentLoction == null) {
            currentLoction = new LocationBean();
        }
        PrefManage.setImsi(imsi);
        currentLoction.setImsi(imsi);
        currentLoction.setType(type);
    }

    public static void setCurrentBlackList() {
        List<DBBlackInfo> listBlackList = null;
        try {
            listBlackList = UCSIDBManager.getDbManager().selector(DBBlackInfo.class).findAll();
        } catch (DbException e) {
            e.printStackTrace();
        }

        if (listBlackList == null || listBlackList.size() == 0)
            return;

        String content = "";
        for (DBBlackInfo dbBlackInfo : listBlackList) {
            content += "#";
            content += dbBlackInfo.getImsi();
        }

        LTESendManager.setBlackList("2", content);
    }


    /**
     * @param imsi 开始定位
     */
    public static void startLoc(String imsi) {
        if (VersionManage.isPoliceVer()) {
            LTESendManager.setActiveMode("1");
        }


        new Timer().schedule(new TimerTask() {
            @Override
            public void run() {
                if (VersionManage.isArmyVer()) {
                    setLocalWhiteList("on");
                } else {
                    setLocalWhiteList("off");
                }

            }
        }, 1000);


        if (VersionManage.isPoliceVer()) {
            LTESendManager.setLocImsi(imsi);
        }


    }

    public static void changeLocTarget(String imsi) {
        if (VersionManage.isPoliceVer()) {
            LTESendManager.setLocImsi(imsi);
        }
    }

    /**
     * 开关管控模式
     */
    public static void setLocalWhiteList(String mode) {
//        String imsi0 = getSimIMSI(0);
//        String imsi1 = getSimIMSI(1);

//        if (imsi0 == null || imsi0.equals("000000000000000"))
//            imsi0 = "";
//
//        if (imsi1 == null || imsi1.equals("000000000000000"))
//            imsi1 = "";
//
//
//        String whitelistContent = "";
//
//        if ("".equals(imsi0) && "".equals(imsi1)) {
//            whitelistContent = "";
//        } else if (!"".equals(imsi0) && "".equals(imsi1)) {
//            whitelistContent = imsi0;
//        } else if ("".equals(imsi0) && !"".equals(imsi1)) {
//            whitelistContent = imsi1;
//        } else {
//            whitelistContent = imsi0 + "," + imsi1;
//        }


        LTESendManager.setNameList(mode, "", "",
                "", "", "block", "", "");

    }

    public static String getSimIMSI(int simid) {
        TelephonyManager telephonyManager = (TelephonyManager) MyApplication.mContext.getSystemService(Context.TELEPHONY_SERVICE);

        if (android.os.Build.VERSION.SDK_INT < android.os.Build.VERSION_CODES.LOLLIPOP_MR1)
            return "";

        int[] subId = null;//SubscriptionManager.getSubId(simid);
        Class<?> threadClazz = null;
        threadClazz = SubscriptionManager.class;

        try {
            Method method = threadClazz.getDeclaredMethod("getSubId", int.class);
            method.setAccessible(true);
            subId = (int[]) method.invoke(null, simid);
        } catch (NoSuchMethodException | InvocationTargetException | IllegalAccessException e) {
            e.printStackTrace();
        }

        int sub = -1;
        if (Build.VERSION.SDK_INT >= 24) {
            sub = (subId != null) ? subId[0] : SubscriptionManager.getDefaultSubscriptionId();
        } else {
            try {
                Method method = threadClazz.getDeclaredMethod("getDefaultSubId");
                method.setAccessible(true);
                sub = (subId != null) ? subId[0] : (Integer) method.invoke(null, (Object[]) null);
            } catch (NoSuchMethodException e) {
                e.printStackTrace();
            } catch (InvocationTargetException e) {
                e.printStackTrace();
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
        }
        String IMSI = "";
        if (sub != -1) {
            Class clazz = telephonyManager.getClass();
            try {
                Method method = clazz.getDeclaredMethod("getSubscriberId", int.class);
                method.setAccessible(true);
                IMSI = (String) method.invoke(telephonyManager, sub);
            } catch (NoSuchMethodException e) {
                e.printStackTrace();
            } catch (InvocationTargetException e) {
                e.printStackTrace();
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
        }

        return IMSI;
    }


    public static void clearCurrentBlackList() {
        List<DBBlackInfo> listBlackList = null;
        try {
            listBlackList = UCSIDBManager.getDbManager().selector(DBBlackInfo.class).findAll();
        } catch (DbException e) {
            e.printStackTrace();
        }

        if (listBlackList == null || listBlackList.size() == 0)
            return;

        String content = "";
        for (DBBlackInfo dbBlackInfo : listBlackList) {
            content += "#";
            content += dbBlackInfo.getImsi();
        }

        LTESendManager.setBlackList("3", content);
    }

    public static void stopCurrentLoc() {
        if (VersionManage.isPoliceVer()) {
            LTESendManager.setLocImsi("0000");
        }


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

    /**
     * 重置模式
     */
    public static void resetMode() {
        if (VersionManage.isPoliceVer()){
            LTESendManager.setActiveMode(CacheManager.currentWorkMode);
        }

        new Timer().schedule(new TimerTask() {
            @Override
            public void run() {
                if (VersionManage.isArmyVer()) {
                    CacheManager.redirect2G();
                } else {
                    setLocalWhiteList("off");
                }

            }
        }, 1000);

    }

    public static boolean getLocState() {
        if (currentLoction == null)
            return false;

        return currentLoction.isLocateStart();
    }

    public static LocationBean getCurrentLoction() {
        return currentLoction;
    }

    public static LocationRptBean getCurrentLocRptBean() {
        if (locationRpts == null) {
            return null;
        }
        if (locationRpts.size() - 1 < 0) {
            return null;
        }
        return locationRpts.get(locationRpts.size() - 1);
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
     * @return 射频是否开启
     */
    public static boolean isRFOpen() {
        boolean isOpen = false;
        if (channels != null && channels.size() > 0) {
            for (LteChannelCfg channel : channels) {
                if (channel.getRFState()) {
                    isOpen = true;
                    break;
                }
            }
        }

        return isOpen;
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
                //channel.setState(cfg.getState());
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
    public static void redirect2G() {

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
        String redirectConfig = "46000,2," + mobileFcn + "#46002,2," + mobileFcn + "#46007,2," + mobileFcn + "#46001,2," + unicomFcn;
        if (!TextUtils.isEmpty(mobileFcn) && !TextUtils.isEmpty(unicomFcn)) {
            LTESendManager.setNameList("on", redirectConfig, "",
                    "", "", "redirect", "", "");
        }


    }

    //将RF状态更新到内存
    public synchronized static void updateRFState(String idx, boolean rf) {
        //UtilBaseLog.printLog(idx + "    size:" + channels.size() + "    " + rf);
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

    public static void addUserChannel(G4MsgChannelCfg cfg) {
        if (userChannels.containsKey(cfg.getIdx())) {
            userChannels.get(cfg.getIdx()).add(cfg);
        } else {
            List<G4MsgChannelCfg> list = new ArrayList<>();
            list.add(cfg);
            userChannels.put(cfg.getIdx(), list);
        }
    }

    public static void updateWhitelistToDev(Context context) {
        /*
        * 考虑到白名单数量巨大时严重影响设备使用，决定不再下发白名单给设备，只做特殊显示
        List<WhiteListInfo> listWhitelist = null;
        String content = "";
        TelephonyManager telManager = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
        String mobileImsi =  StringUtils.defaultIfBlank(telManager.getSubscriberId(), "");
        //UtilBaseLog.printLog("######" + telManager.getSubscriberId());
        try {
            listWhitelist = UCSIDBManager.getDbManager().selector(WhiteListInfo.class).findAll();
        } catch (DbException e) {
            e.printStackTrace();
        }

        if (listWhitelist == null || listWhitelist.size() == 0){
            if (!"".equals(mobileImsi)){
                content = mobileImsi;
            }
            //ProtocolManager.setNamelist("", "", "", "","","block");
        }else{
            for (WhiteListInfo whiteListInfo : listWhitelist) {
                if ("".equals(whiteListInfo.getImsi()))
                    continue;

                content += whiteListInfo.getImsi();
                content += ",";
            }

            if ("".equals(mobileImsi)){
                content = content.substring(0, content.length()-1);
            }else{
                content = content+mobileImsi;
            }
        }

        ProtocolManager.setNamelist("", content, "", "","","block");
        */
    }

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

    public static boolean isHighGa() {
        if (!initSuccess4G)
            return true;    //默认高

        int allGa = 0;
        for (LteChannelCfg channel : channels) {
            allGa += Integer.parseInt(channel.getGa());
        }

        return allGa > 32;
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


    /*
        删除列表里已存在的ueid
        成功删除返回ture,没有删除（即不存在）返回false
     */
    public static synchronized boolean removeExistUeidInRealtimeList(String imsi) {
        for (int i = 0; i < realtimeUeidList.size(); i++) {
            if (realtimeUeidList.get(i).getImsi().equals(imsi)) {
                realtimeUeidList.remove(i);
                return true;
            }
        }

        return false;
    }

    public static void clearUeidWhithoutSrsp() {
        for (int i = 0; i < realtimeUeidList.size(); i++) {
            if (realtimeUeidList.get(i).getSrsp().equals("")) {
                realtimeUeidList.remove(i);
                i--;
            }
        }
    }

}
