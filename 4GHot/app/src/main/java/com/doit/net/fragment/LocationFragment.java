package com.doit.net.fragment;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.Switch;
import android.widget.TextView;

import com.doit.net.protocol.LTESendManager;
import com.doit.net.protocol.Send2GManager;
import com.doit.net.utils.UtilOperator;
import com.doit.net.view.LocateChart;
import com.doit.net.view.LocateCircle;
import com.doit.net.base.BaseFragment;
import com.doit.net.bean.LteChannelCfg;
import com.doit.net.event.EventAdapter;
import com.doit.net.model.BlackBoxManger;
import com.doit.net.model.CacheManager;
import com.doit.net.utils.LogUtils;
import com.doit.net.ucsi.R;
import com.doit.net.utils.ToastUtils;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;


public class LocationFragment extends BaseFragment implements EventAdapter.EventCall {
    private TextView tvLocatingImsi;
    private LocateChart vLocateChart;
    private LocateCircle vLocateCircle;
    private CheckBox cbGainSwitch;
    private CheckBox cbVoiceSwitch;
    private CheckBox cbLocSwitch;
    private Switch switchType;

    private List<Integer> listChartValue = new ArrayList<>();
    private final int LOCATE_CHART_X_AXIS_P_CNT = 15;       //图表横坐标点数
    private final int LOCATE_CHART_Y_AXIS_P_CNT = 25;       //图表纵坐标点数
    private String textContent = "搜寻未开始";

    private int currentSRSP = 0;
    private int lastRptSRSP = 60;//初始平滑地开始
    private static boolean isOpenVoice = true;
    private Timer speechTimer = null;
    private final int BROADCAST_PERIOD = 1900;
    private long lastLocRptTime = 0;
    private int LOC_RPT_TIMEOUT = 15 * 1000;  //多长时间没上报就开始播报“正在搜寻”
    private int UPDATE_ARFCN_TIMEOUT = 2 * 60 * 1000;  //多长时间没上报就更新频点
    private final int MAX_DEVIATION = 16;   //强度与上次上报偏差大于这个值就重新计算

    private String lastLocateIMSI = "";

    //handler消息
    private final int UPDATE_VIEW = 0;
    private final int LOC_REPORT = 1;
    private final int REFRESH_DEVICE = 4;
    private final int RF_STATUS_LOC = 5;
    private final int ADD_LOCATION = 6;

    public LocationFragment() {
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {


        View rootView = inflater.inflate(R.layout.doit_layout_location, container, false);
        tvLocatingImsi = rootView.findViewById(R.id.tvLocatingImsi);
        vLocateChart = rootView.findViewById(R.id.vLocateChart);
        vLocateCircle = rootView.findViewById(R.id.vLocateCircle);
        cbVoiceSwitch = rootView.findViewById(R.id.cbVoiceSwitch);
        cbVoiceSwitch.setOnCheckedChangeListener(voiceSwitchListener);
        cbGainSwitch = rootView.findViewById(R.id.cbGainSwitch);

        cbGainSwitch.setOnCheckedChangeListener(gainSwitchListener);
        cbLocSwitch = rootView.findViewById(R.id.cbLocSwitch);

        switchType = rootView.findViewById(R.id.switch_type);
        switchType.setOnCheckedChangeListener(switchListener);

        initView();
        initEvent();
        return rootView;
    }


    private void initEvent() {

        EventAdapter.register(EventAdapter.REFRESH_DEVICE, this);
        EventAdapter.register(EventAdapter.LOCATION_RPT, this);
        EventAdapter.register(EventAdapter.ADD_LOCATION, this);
        EventAdapter.register(EventAdapter.RF_STATUS_LOC, this);

    }

    private void initView() {

        cbLocSwitch.setOnCheckedChangeListener(rfLocSwitchListener);

        vLocateChart.setCylinderCount(LOCATE_CHART_X_AXIS_P_CNT);
        vLocateChart.setMaxPointCntInClder(LOCATE_CHART_Y_AXIS_P_CNT);
        resetLocateChartValue();
    }


    private void startSpeechBroadcastLoop() {
        if (speechTimer == null) {
            speechTimer = new Timer();
            speechTimer.schedule(new TimerTask() {
                @Override
                public void run() {
                    if ((int) (System.currentTimeMillis() - lastLocRptTime) > LOC_RPT_TIMEOUT) {
                        currentSRSP = 0;
                        resetLocateChartValue();
                        refreshPage();
                    }

                    if (currentSRSP == 0) {
                        speech("正在搜寻");
                    } else {
                        speech("" + currentSRSP);
                    }
                }
            }, 4000, BROADCAST_PERIOD);
        }

    }



    private void stopSpeechBroadcastLoop() {
        if (speechTimer != null) {
            speechTimer.cancel();
            speechTimer = null;
        }
    }

    private void refreshPage() {
        if (CacheManager.getCurrentLocation() == null) {
            return;
        }

        mHandler.sendEmptyMessage(UPDATE_VIEW);
    }

    private void updateLocateChart() {
        int[] chartDatas = new int[LOCATE_CHART_X_AXIS_P_CNT];

        for (int i = 0; i < LOCATE_CHART_X_AXIS_P_CNT; i++) {
            chartDatas[i] = listChartValue.get(i);
        }
        vLocateChart.updateChart(chartDatas);
    }

    private int correctSRSP(int srspRptValue) {
        //srsp = (srspRptValue-234)/10  旧的算法
        int srsp = srspRptValue * 5 / 6;

        if (srsp <= 0)
            srsp = 0;

        if (srsp > 100)
            srsp = 100;

        if (Math.abs(srsp - lastRptSRSP) > MAX_DEVIATION) {
            srsp = (lastRptSRSP + srsp) / 2;
        }

        return srsp;
    }

    //开始定位
    private void startLoc(String imsi) {
        LogUtils.log("开始定位,IMSI:" + imsi);

        cbLocSwitch.setOnCheckedChangeListener(null);
        cbLocSwitch.setChecked(true);
        cbLocSwitch.setOnCheckedChangeListener(rfLocSwitchListener);

        switchType.setOnCheckedChangeListener(null);
        switchType.setChecked(CacheManager.getCurrentLocation().getType() == 1);
        switchType.setOnCheckedChangeListener(switchListener);

        if ("CTC".equals(UtilOperator.getOperatorName(imsi))){
            switchType.setVisibility(View.GONE);
        }else {
            switchType.setVisibility(View.VISIBLE);
        }


        textContent = "正在搜寻" + imsi;

        if (!"".equals(lastLocateIMSI) && !lastLocateIMSI.equals(imsi)) {   //更换目标
            speech("搜寻目标更换");
            currentSRSP = 0;
            lastRptSRSP = 0;
            textContent = "正在搜寻：" + CacheManager.getCurrentLocation().getImsi();
            resetLocateChartValue();
        }

        startSpeechBroadcastLoop();

        lastLocateIMSI = CacheManager.getCurrentLocation().getImsi();

        refreshPage();
    }


    /**
     * 停止定位
     */
    private void stopLoc() {
        LogUtils.log("停止定位");

        CacheManager.stopLoc();

        if (CacheManager.getCurrentLocation() !=null){
            CacheManager.getCurrentLocation().setLocateStart(false);
            textContent = "搜寻暂停：" + CacheManager.getCurrentLocation().getImsi();
        }else {
            textContent = "搜寻未开始";
        }

        stopSpeechBroadcastLoop();
        currentSRSP = 0;
        resetLocateChartValue();

        cbLocSwitch.setOnCheckedChangeListener(null);
        cbLocSwitch.setChecked(false);
        cbLocSwitch.setOnCheckedChangeListener(rfLocSwitchListener);

        refreshPage();
    }

    private void resetLocateChartValue() {
        listChartValue.clear();

        for (int i = 0; i < LOCATE_CHART_X_AXIS_P_CNT; i++) {
            listChartValue.add(0);
        }
    }

    void speech(String content) {
        if (isOpenVoice)
            EventAdapter.call(EventAdapter.SPEAK, content);
    }

    CompoundButton.OnCheckedChangeListener switchListener = new CompoundButton.OnCheckedChangeListener() {
        @Override
        public void onCheckedChanged(CompoundButton compoundButton, boolean isChecked) {

            if (CacheManager.currentLocation != null) {
                CacheManager.currentLocation.setType(isChecked ? 1 : 0);

                if (CacheManager.getLocState()){
                    CacheManager.startLoc(CacheManager.getCurrentLocation().getImsi(),CacheManager.currentLocation.getType());
                    startLoc(CacheManager.getCurrentLocation().getImsi());
                    EventAdapter.call(EventAdapter.SHOW_PROGRESS, 8000);
                }
            }
        }
    };


    CompoundButton.OnCheckedChangeListener rfLocSwitchListener = new CompoundButton.OnCheckedChangeListener() {
        @Override
        public void onCheckedChanged(CompoundButton compoundButton, boolean isChecked) {
            if (!compoundButton.isPressed()) {
                return;
            }

            if (!CacheManager.checkDevice(getContext())) {
                cbLocSwitch.setChecked(!isChecked);
                return;
            }

            if (!isChecked) {
                if (CacheManager.currentLocation == null || TextUtils.isEmpty(CacheManager.currentLocation.getImsi())) {
                    return;
                }
                EventAdapter.call(EventAdapter.SHOW_PROGRESS, 10000);

                LTESendManager.closeAllRf();

                Send2GManager.setRFState("0");

                new Timer().schedule(new TimerTask() {
                    @Override
                    public void run() {
                        stopLoc();
                    }
                }, 2000);


                if (CacheManager.currentLocation != null && !CacheManager.currentLocation.getImsi().equals("")) {
                    EventAdapter.call(EventAdapter.ADD_BLACKBOX, BlackBoxManger.STOP_LOCALTE + CacheManager.currentLocation.getImsi());
                }
            } else {
                if (CacheManager.currentLocation == null || CacheManager.currentLocation.getImsi().equals("")) {
                    ToastUtils.showMessage(R.string.button_loc_unstart);
                } else {
                    EventAdapter.call(EventAdapter.SHOW_PROGRESS, 10000);
                    CacheManager.startLoc(CacheManager.getCurrentLocation().getImsi(),CacheManager.currentLocation.getType());
                    startLoc(CacheManager.getCurrentLocation().getImsi());

                    EventAdapter.call(EventAdapter.ADD_BLACKBOX, BlackBoxManger.START_LOCALTE + CacheManager.currentLocation.getImsi());
                }
            }
        }
    };


    CompoundButton.OnCheckedChangeListener voiceSwitchListener = new CompoundButton.OnCheckedChangeListener() {
        @Override
        public void onCheckedChanged(CompoundButton view, boolean isChecked) {
            if (!view.isPressed())
                return;

            isOpenVoice = isChecked;
        }
    };

    CompoundButton.OnCheckedChangeListener gainSwitchListener = new CompoundButton.OnCheckedChangeListener() {
        @Override
        public void onCheckedChanged(CompoundButton view, boolean isChecked) {
            if (!view.isPressed())
                return;

            if (!CacheManager.checkDevice(getContext())) {
                cbGainSwitch.setChecked(!cbGainSwitch.isChecked());
                return;
            }

            if (isChecked) {
                CacheManager.setHighGa(true);

                Send2GManager.setPowerLevel(3);
            } else {
                CacheManager.setHighGa(false);
                Send2GManager.setPowerLevel(1);
            }

            ToastUtils.showMessageLong("增益设置已下发，请等待其生效");
            EventAdapter.call(EventAdapter.SHOW_PROGRESS, 8000);
        }
    };


    @Override
    public void onFocus() {
        refreshPage();
    }


    /**
     * 射频是否开启
     */
    private void isRFOpen() {

        boolean rfState4G = false;

        for (LteChannelCfg channel : CacheManager.getChannels()) {
            if (channel.getRFState()) {
                rfState4G = true;
                break;
            }
        }

        if (!rfState4G) {
            stopLoc();
        }
    }


    @Override
    public void call(String key, Object val) {
        switch (key) {
            case EventAdapter.LOCATION_RPT:
                try {
                    Message msg = new Message();
                    msg.what = LOC_REPORT;
                    msg.obj = val;
                    mHandler.sendMessage(msg);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                break;
            case EventAdapter.ADD_LOCATION:
                try {
                    Message msg = new Message();
                    msg.what = ADD_LOCATION;
                    msg.obj = val;
                    mHandler.sendMessage(msg);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                break;
            case EventAdapter.REFRESH_DEVICE:
                mHandler.sendEmptyMessage(REFRESH_DEVICE);
                break;
            case EventAdapter.RF_STATUS_LOC:
                mHandler.sendEmptyMessage(RF_STATUS_LOC);
                break;
        }
    }

    @SuppressLint("HandlerLeak")
    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case UPDATE_VIEW:
                    tvLocatingImsi.setText(textContent);
                    vLocateCircle.setValue(currentSRSP);
                    updateLocateChart();

                    break;
                case LOC_REPORT:
                    if (CacheManager.getCurrentLocation() != null && CacheManager.getCurrentLocation().isLocateStart()) {
                        currentSRSP = correctSRSP(Integer.parseInt((String) msg.obj));
                        if (currentSRSP == 0)
                            return;

                        lastLocRptTime = new Date().getTime();
                        lastRptSRSP = currentSRSP;

                        listChartValue.add(currentSRSP / 4);
                        listChartValue.remove(0);
                        textContent = "正在搜寻" + CacheManager.getCurrentLocation().getImsi();

                        refreshPage();
                    }
                    break;
                case ADD_LOCATION:
                    startLoc((String) msg.obj);
                    break;
                case REFRESH_DEVICE:
                    //ga <= 10为低增益,11-50为高增益
                    if (CacheManager.channels != null && CacheManager.channels.size() > 0) {
                        cbGainSwitch.setOnCheckedChangeListener(null);
                        for (int i = 0; i < CacheManager.channels.size(); i++) {
                            LteChannelCfg channel = CacheManager.channels.get(i);
                            LogUtils.log(channel.toString());
                            int ga = Integer.parseInt(channel.getGa());
                            if (ga <= 10) {
                                cbGainSwitch.setChecked(false);
                                break;
                            }
                        }
                        cbGainSwitch.setOnCheckedChangeListener(gainSwitchListener);
                    }
                    break;
                case RF_STATUS_LOC:
                    isRFOpen();
                    break;
            }

        }
    };
}