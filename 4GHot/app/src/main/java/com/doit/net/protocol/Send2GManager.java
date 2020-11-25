package com.doit.net.protocol;

import com.doit.net.bean.BlackListBean;
import com.doit.net.bean.Get2GCommonBean;
import com.doit.net.bean.Set2GParamsBean;
import com.doit.net.bean.Set2GRFBean;
import com.doit.net.event.EventAdapter;
import com.doit.net.model.BlackBoxManger;
import com.doit.net.model.BlackListInfo;
import com.doit.net.model.CacheManager;
import com.doit.net.model.UCSIDBManager;
import com.doit.net.socket.ServerSocketUtils;
import com.doit.net.utils.GsonUtils;
import com.doit.net.utils.LogUtils;
import com.doit.net.utils.UtilDataFormatChange;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.xutils.DbManager;
import org.xutils.ex.DbException;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;


/**
 * Author：Libin on 2020/9/27 14:44
 * Email：1993911441@qq.com
 * Describe：2G协议发送
 */
public class Send2GManager {

    public static void sendData(byte mainType, byte subType, byte[] content) {
        LTESendPackage sendPackage = new LTESendPackage();
        //设置Sequence ID
        sendPackage.setPackageSequence(LTEProtocol.getSequenceID());
        //设置Session ID
        sendPackage.setPackageSessionID(LTEProtocol.getSessionID());
        //设置EquipType
        sendPackage.setPackageEquipType(CacheManager.equipType2G);
        //设置预留
        sendPackage.setPackageReserve((byte) 0);
        //设置主类型
        sendPackage.setPackageMainType(mainType);
        //设置子类型
        sendPackage.setPackageSubType(subType);
        sendPackage.setByteSubContent(content);

        //设置校验位
        sendPackage.setPackageCheckNum(sendPackage.getCheckNum());

        //获取整体的包
        byte[] tempSendBytes = sendPackage.getPackageContent();

        LogUtils.log("TCP发送：Type:" + sendPackage.getPackageMainType() + ";  SubType:0x" + Integer.toHexString(sendPackage.getPackageSubType()) + ";  子协议:" + UtilDataFormatChange.bytesToString(sendPackage.getByteSubContent(), 0));
        LogUtils.log(sendPackage.toString());
        ServerSocketUtils.getInstance().sendData(ServerSocketUtils.REMOTE_2G_IP, tempSendBytes);

    }

    /**
     * 查询设备参数
     */
    public static void getCommonConfig() {
        getCommonConfig("0");
        new Timer().schedule(new TimerTask() {
            @Override
            public void run() {
                getCommonConfig("1");
            }
        },500);

    }

    /**
     * @param boardId 查询设备参数
     */
    public static void getCommonConfig(String boardId) {
        Get2GCommonBean bean = new Get2GCommonBean();
        bean.setId(MsgType2G.GET_NTC_CONFIG_ID);
        bean.setBoardid(boardId);

        sendData(MsgType2G.PT_PARAM, MsgType2G.GET_NTC_CONFIG, GsonUtils.objectToString(bean).getBytes(StandardCharsets.UTF_8));

    }


    /**
     * 查询运营商参数、工作模式
     */
    public static void getParamsConfig() {
        getParamsConfig("0", "0");

        new Timer().schedule(new TimerTask() {
            @Override
            public void run() {
                getParamsConfig("0", "1");
            }
        }, 500);


        new Timer().schedule(new TimerTask() {
            @Override
            public void run() {
                getParamsConfig("1", "0");
            }
        }, 1000);

    }


    /**
     * 查询运营商参数、工作模式
     */
    public static void getParamsConfig(String boardId, String carrierId) {
        Get2GCommonBean bean = new Get2GCommonBean();
        bean.setId(MsgType2G.GET_MCRF_CONFIG_ID);
        bean.setBoardid(boardId);
        bean.setCarrierid(carrierId);

        sendData(MsgType2G.PT_PARAM, MsgType2G.GET_MCRF_CONFIG, GsonUtils.objectToString(bean).getBytes(StandardCharsets.UTF_8));
    }

    /**
     * 查询猫池状态
     */
    public static void getMPState(){
        try {
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("id", MsgType2G.GET_MP_STATE_ID);

            sendData(MsgType2G.PT_PARAM, MsgType2G.GET_MP_STATE, jsonObject.toString().getBytes(StandardCharsets.UTF_8));

        } catch (JSONException e) {
            e.printStackTrace();
        }

    }


    /**
     * 设置运营商参数、工作模式
     */
    public static void setParamsConfig(Set2GParamsBean.Params params) {
        Set2GParamsBean paramsBean = new Set2GParamsBean();
        paramsBean.setId(MsgType2G.SET_MCRF_CONFIG_ID);
        List<Set2GParamsBean.Params> paramsList = new ArrayList<>();
        paramsList.add(params);
        paramsBean.setParams(paramsList);
        sendData(MsgType2G.PT_PARAM, MsgType2G.SET_MCRF_CONFIG, GsonUtils.objectToString(paramsBean).getBytes(StandardCharsets.UTF_8));
    }

    /**
     * @param level 下行功率等级   1：低  2：中 3:高
     */
    public static void setPowerLevel(int level) {
        Set2GParamsBean paramsBean = new Set2GParamsBean();
        paramsBean.setId(MsgType2G.SET_MCRF_CONFIG_ID);

        for (Set2GParamsBean.Params params : CacheManager.paramList) {
            if (level == 1) {
                params.setDlattn("15");
            } else if (level == 2) {
                params.setDlattn("7");
            } else if (level == 3) {
                params.setDlattn("0");
            }
        }
        paramsBean.setParams(CacheManager.paramList);
        sendData(MsgType2G.PT_PARAM, MsgType2G.SET_MCRF_CONFIG, GsonUtils.objectToString(paramsBean).getBytes(StandardCharsets.UTF_8));
    }


    /**
     * 设置运营商参数、工作模式
     */
    public static void setRFState(String state) {
        Set2GRFBean bean = new Set2GRFBean();
        bean.setId(MsgType2G.SET_RF_SWITCH_ID);
        List<Set2GRFBean.Params> paramList = new ArrayList<>();
        for (Set2GParamsBean.Params params : CacheManager.paramList) {
            Set2GRFBean.Params param = new Set2GRFBean.Params();
            param.setBoardid(params.getBoardid());
            param.setCarrierid(params.getCarrierid());
            param.setState(state);
            paramList.add(param);
        }

        bean.setParams(paramList);
        sendData(MsgType2G.PT_PARAM, MsgType2G.SET_RF_SWITCH, GsonUtils.objectToString(bean).getBytes(StandardCharsets.UTF_8));
    }


    /**
     * 设置运营商参数、工作模式
     */
    public static void setRFState(String boardId, String carrierId, String state) {
        Set2GRFBean bean = new Set2GRFBean();
        bean.setId(MsgType2G.SET_RF_SWITCH_ID);

        List<Set2GRFBean.Params> paramList = new ArrayList<>();

        Set2GRFBean.Params param = new Set2GRFBean.Params();
        param.setBoardid(boardId);
        param.setCarrierid(carrierId);
        param.setState(state);
        paramList.add(param);

        bean.setParams(paramList);
        sendData(MsgType2G.PT_PARAM, MsgType2G.SET_RF_SWITCH, GsonUtils.objectToString(bean).getBytes(StandardCharsets.UTF_8));
    }


    /**
     * 重启设备
     */
    public static void rebootDevice() {
        Set2GRFBean bean = new Set2GRFBean();
        bean.setId(MsgType2G.SYS_REBOOT_ID);
        sendData(MsgType2G.PT_SYSTEM, MsgType2G.REBOOT_DEVICE, GsonUtils.objectToString(bean).getBytes(StandardCharsets.UTF_8));

        EventAdapter.call(EventAdapter.ADD_BLACKBOX, BlackBoxManger.REBOOT_2G_DEVICE);
    }

    /**
     * @param imsi  开始、结束定位
     * @param state
     */
    public static void setLocIMSI(String imsi, String state) {

        try {
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("id", MsgType2G.SET_LOC_IMSI_ID);
            jsonObject.put("switch", state);
            List<String> imsiList = new ArrayList<>();
            imsiList.add(imsi);
            JSONArray jsonArray = new JSONArray(imsiList);
            jsonObject.put("imsilist", jsonArray);
            LogUtils.log("2G定位:" + jsonObject.toString());
            LogUtils.log(new String(jsonObject.toString().getBytes(StandardCharsets.UTF_8), StandardCharsets.UTF_8));
            sendData(MsgType2G.PT_PARAM, MsgType2G.SET_LOC_IMSI, jsonObject.toString().getBytes(StandardCharsets.UTF_8));


        } catch (JSONException e) {
            e.printStackTrace();
        }

    }

    /**
     * 设置黑名单
     */
    public static void setBlackList(){
        DbManager dbManager = UCSIDBManager.getDbManager();
        List<String> blackList = new ArrayList<>();
        try {
            List<BlackListInfo> blackInfoList = dbManager.selector(BlackListInfo.class).findAll();
            if (blackInfoList != null){
                for (int i = 0; i < blackInfoList.size(); i++) {
                    blackList.add(blackInfoList.get(i).getMsisdn());
                }
            }
        } catch (DbException e) {
            e.printStackTrace();
        }

        BlackListBean blackListBean = new BlackListBean();
        blackListBean.setId(MsgType2G.SET_BLACK_NAMELIST_ID);
        blackListBean.setNamelist(blackList);
        LogUtils.log("修改名单(黑):" + GsonUtils.objectToString(blackListBean));
        sendData(MsgType2G.PT_PARAM, MsgType2G.SET_BLACK_NAMELIST, GsonUtils.objectToString(blackListBean).getBytes(StandardCharsets.UTF_8));
    }
}
