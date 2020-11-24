package com.doit.net.fragment;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ListView;
import android.widget.TextView;

import com.daimajia.swipe.SwipeLayout;
import com.daimajia.swipe.util.Attributes;
import com.doit.net.adapter.UeidListViewAdapter;
import com.doit.net.base.BaseFragment;
import com.doit.net.bean.LteChannelCfg;
import com.doit.net.bean.Set2GParamsBean;
import com.doit.net.bean.UeidBean;
import com.doit.net.event.EventAdapter;
import com.doit.net.protocol.LTESendManager;
import com.doit.net.model.BlackBoxManger;
import com.doit.net.model.CacheManager;
import com.doit.net.model.ImsiMsisdnConvert;
import com.doit.net.model.UCSIDBManager;
import com.doit.net.model.BlackListInfo;
import com.doit.net.protocol.Send2GManager;
import com.doit.net.utils.DateUtils;
import com.doit.net.utils.MySweetAlertDialog;
import com.doit.net.utils.ToastUtils;
import com.doit.net.utils.LogUtils;
import com.doit.net.utils.UtilOperator;
import com.doit.net.ucsi.R;

import org.xutils.DbManager;
import org.xutils.ex.DbException;

import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

public class RealTimeUeidRptFragment extends BaseFragment implements EventAdapter.EventCall {
    private ListView mListView;
    private UeidListViewAdapter mAdapter;
    private Button btClearRealtimeUeid;

    private TextView tvRealtimeCTJCount;
    private TextView tvRealtimeCTUCount;
    private TextView tvRealtimeCTCCount;

    private CheckBox cbDetectSwitch;

    private long lastSortTime = 0;  //为了防止频繁上报排序导致列表错乱，定时排序一次

    //handler消息
    private final int SHIELD_RPT = 2;
    private final int RF_STATUS_RPT = 3;
    private final int REFRESH_IMSI = 4;

    private DbManager dbManager;


    public RealTimeUeidRptFragment() {
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.doit_layout_ueid_list, container, false);
        mListView = rootView.findViewById(R.id.listview);
        btClearRealtimeUeid = rootView.findViewById(R.id.button_clear);
        btClearRealtimeUeid.setOnClickListener(clearListener);

        tvRealtimeCTJCount = rootView.findViewById(R.id.tvCTJCount);
        tvRealtimeCTUCount = rootView.findViewById(R.id.tvCTUCount);
        tvRealtimeCTCCount = rootView.findViewById(R.id.tvCTCCount);
        cbDetectSwitch = rootView.findViewById(R.id.cbDetectSwitch);
        initView();

        EventAdapter.register(EventAdapter.RF_STATUS_RPT, this);

        EventAdapter.register(EventAdapter.SHIELD_RPT, this);
        EventAdapter.register(EventAdapter.REFRESH_IMSI, this);
        dbManager = UCSIDBManager.getDbManager();

        return rootView;
    }

    private void initView() {


        mAdapter = new UeidListViewAdapter(getActivity());
        mListView.setAdapter(mAdapter);
        mAdapter.setMode(Attributes.Mode.Single);
        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                ((SwipeLayout) (mListView.getChildAt(position - mListView.getFirstVisiblePosition()))).open(true);
                ((SwipeLayout) (mListView.getChildAt(position - mListView.getFirstVisiblePosition()))).setClickToClose(true);
            }
        });

        cbDetectSwitch.setOnCheckedChangeListener(null);
        cbDetectSwitch.setChecked(CacheManager.isDeviceOk());
        cbDetectSwitch.setOnCheckedChangeListener(rfDetectSwichtListener);
    }

    CompoundButton.OnCheckedChangeListener rfDetectSwichtListener = new CompoundButton.OnCheckedChangeListener() {
        @Override
        public void onCheckedChanged(CompoundButton compoundButton, boolean isChecked) {
            if (!compoundButton.isPressed()) {
                return;
            }

            if (!CacheManager.checkDevice(getContext())) {
                cbDetectSwitch.setChecked(!isChecked);
                return;
            }

            if (isChecked) {
                LTESendManager.openAllRf();
                Send2GManager.setRFState("1");
                ToastUtils.showMessageLong(R.string.all_rf_open);
                EventAdapter.call(EventAdapter.ADD_BLACKBOX, BlackBoxManger.OPEN_ALL_4G_RF);
                EventAdapter.call(EventAdapter.ADD_BLACKBOX, BlackBoxManger.OPEN_ALL_2G_RF);
                EventAdapter.call(EventAdapter.SHOW_PROGRESS, 10000);
            } else {
                if (CacheManager.getLocState()) {
                    new MySweetAlertDialog(getContext(), MySweetAlertDialog.WARNING_TYPE)
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
                                    Send2GManager.setRFState("0");

                                    ToastUtils.showMessage(R.string.all_rf_close);
                                    EventAdapter.call(EventAdapter.SHOW_PROGRESS, 10000);
                                    EventAdapter.call(EventAdapter.ADD_BLACKBOX, BlackBoxManger.CLOSE_ALL_4G_RF);
                                    EventAdapter.call(EventAdapter.ADD_BLACKBOX, BlackBoxManger.CLOSE_ALL_2G_RF);
                                }
                            })
                            .setCancelClickListener(new MySweetAlertDialog.OnSweetClickListener() {
                                @Override
                                public void onClick(MySweetAlertDialog mySweetAlertDialog) {
                                    mySweetAlertDialog.dismiss();
                                    cbDetectSwitch.setOnCheckedChangeListener(null);
                                    cbDetectSwitch.setChecked(true);
                                    cbDetectSwitch.setOnCheckedChangeListener(rfDetectSwichtListener);
                                }
                            })
                            .show();
                } else {
                    LTESendManager.closeAllRf();
                    Send2GManager.setRFState("0");

                    ToastUtils.showMessageLong(R.string.all_rf_close);
                    EventAdapter.call(EventAdapter.SHOW_PROGRESS, 6000);
                    EventAdapter.call(EventAdapter.ADD_BLACKBOX, BlackBoxManger.CLOSE_ALL_4G_RF);
                    EventAdapter.call(EventAdapter.ADD_BLACKBOX, BlackBoxManger.CLOSE_ALL_2G_RF);
                }
            }
        }
    };


    /**
     * @param ueidList 新增数据
     */
    private void addShildRptList(List<UeidBean> ueidList) {
        for (UeidBean ueidBean : ueidList) {
            boolean isContain = false;
            LogUtils.log("侦码上报: IMSI：" + ueidBean.getImsi() + "强度：" + ueidBean.getSrsp() + ",类型" + ueidBean.getType());
            for (int i = 0; i < CacheManager.realtimeUeidList.size(); i++) {
                if (CacheManager.realtimeUeidList.get(i).getImsi().equals(ueidBean.getImsi())) {
                    int times = CacheManager.realtimeUeidList.get(i).getRptTimes();
                    if (times > 1000) {
                        times = 0;
                    }
                    CacheManager.realtimeUeidList.get(i).setRptTimes(times + 1);
                    CacheManager.realtimeUeidList.get(i).setSrsp("" + Integer.parseInt(ueidBean.getSrsp()) * 5 / 6);
                    if (ueidBean.getType() == 1) {
                        CacheManager.realtimeUeidList.get(i).setType(ueidBean.getType());
                    }
                    CacheManager.realtimeUeidList.get(i).setRptTime(DateUtils.convert2String(new Date().getTime(), DateUtils.LOCAL_DATE));

                    isContain = true;
                    break;
                }
            }

            if (!isContain) {
                UeidBean newUeid = new UeidBean();
                newUeid.setImsi(ueidBean.getImsi());
                newUeid.setSrsp("" + Integer.parseInt(ueidBean.getSrsp()) * 5 / 6);
                newUeid.setRptTime(DateUtils.convert2String(new Date().getTime(), DateUtils.LOCAL_DATE));
                newUeid.setRptTimes(1);
                if (ueidBean.getType() == 1) {
                    newUeid.setType(ueidBean.getType());
                }
                CacheManager.realtimeUeidList.add(newUeid);


                UCSIDBManager.saveUeidToDB(ueidBean.getImsi(), "",
                        new Date().getTime(), ueidBean.getType());
            }

        }

    }


    /**
     * 刷新列表
     */
    private void updateView() {
        int realtimeCTJCount = 0;
        int realtimeCTUCount = 0;
        int realtimeCTCCount = 0;

        //移动翻译数量
        int translateCMCCNum = 0;
        //联通翻译数量
        int translateCUNum = 0;
        //电信翻译数量
        int translateCTNum = 0;


        for (UeidBean ueidBean : CacheManager.realtimeUeidList) {
            try {
                BlackListInfo info = dbManager.selector(BlackListInfo.class).where("msisdn",
                        "=", ueidBean.getNumber()).or("imsi", "=", ueidBean.getImsi()).findFirst();
                if (info != null) {
                    ueidBean.setBlack(true);
                    ueidBean.setRemark(info.getRemark());
                } else {
                    ueidBean.setBlack(false);
                    ueidBean.setRemark("");
                }
            } catch (DbException e) {
                e.printStackTrace();
            }

            String msisdn = ImsiMsisdnConvert.getMsisdnFromLocal(ueidBean.getImsi());
            if (!TextUtils.isEmpty(msisdn)) {
                ueidBean.setNumber(msisdn);
            }

            if (!TextUtils.isEmpty(ueidBean.getNumber())) {
                switch (UtilOperator.getOperatorName(ueidBean.getImsi())) {
                    case "CTJ":
                        translateCMCCNum++;
                        break;
                    case "CTU":
                        translateCUNum++;
                        break;
                    case "CTC":
                        translateCTNum++;
                        break;
                }

            }

            switch (UtilOperator.getOperatorName(ueidBean.getImsi())) {
                case "CTJ":
                    realtimeCTJCount++;
                    break;
                case "CTU":
                    realtimeCTUCount++;
                    break;
                case "CTC":
                    if (!ueidBean.getImsi().startsWith("46011")){   //电信4G和2G同时存在，4G无法翻译，过滤掉46011
                        realtimeCTCCount++;
                    }
                    break;
            }
        }


        tvRealtimeCTJCount.setText(translateCMCCNum +"/"+ realtimeCTJCount);
        tvRealtimeCTUCount.setText(translateCUNum +"/"+ realtimeCTUCount);
        tvRealtimeCTCCount.setText(translateCTNum +"/"+ realtimeCTCCount);



        if (mAdapter != null) {
            mAdapter.notifyDataSetChanged();
        }

    }


    @SuppressLint("HandlerLeak")
    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case SHIELD_RPT:
                    List<UeidBean> ueidList = (List<UeidBean>) msg.obj;

                    addShildRptList(ueidList);
                    sortRealtimeRpt();
                    updateView();
                    break;
                case RF_STATUS_RPT:
                    isRFOpen();
                    break;
                case REFRESH_IMSI:
                    updateView();
                    break;

            }
        }
    };


    //根据强度排序
    private void sortRealtimeRpt() {

        if (new Date().getTime() - lastSortTime >= 3000) {
            Collections.sort(CacheManager.realtimeUeidList, new Comparator<UeidBean>() {
                public int compare(UeidBean o1, UeidBean o2) {

                    boolean isBlack1 = o1.isBlack();
                    int rssi1 = Integer.parseInt(o1.getSrsp());
                    String phoneNumber1  = o1.getNumber();


                    boolean isBlack2 = o2.isBlack();
                    int rssi2 = Integer.parseInt(o2.getSrsp());
                    String phoneNumber2 = o2.getNumber();


                    if (isBlack1 && isBlack2) {
                        return rssi2 - rssi1;
                    } else if (isBlack1) {
                        return -1;
                    } else if (isBlack2) {
                        return 1;
                    } else if (!TextUtils.isEmpty(phoneNumber1) && !TextUtils.isEmpty(phoneNumber2)){
                        return rssi2 - rssi1;
                    }else if (!TextUtils.isEmpty(phoneNumber1)){
                        return -1;
                    }else if (!TextUtils.isEmpty(phoneNumber2)){
                        return 1;
                    }else {
                        return rssi2 - rssi1;
                    }

                }
            });

            lastSortTime = new Date().getTime();
        }
    }


    /**
     * 开启射频耗时操作,此时射频还未收到设备射频开启回复
     */
    @Override
    public void onResume() {
        super.onResume();
        isRFOpen();
    }

    /**
     * 射频是否开启
     */
    private void isRFOpen() {
        boolean rfState4G = false;
        boolean rfState2G = false;

        for (LteChannelCfg channel : CacheManager.getChannels()) {
            if (channel.getRFState()) {
                rfState4G = true;
                break;
            }
        }
        for (Set2GParamsBean.Params params : CacheManager.paramList) {
            if
            (params.isRfState() && !params.getBoardid().equals("1")) {
                rfState2G = true;
                break;
            }
        }

        cbDetectSwitch.setOnCheckedChangeListener(null);
        cbDetectSwitch.setChecked(rfState4G || rfState2G);
        cbDetectSwitch.setOnCheckedChangeListener(rfDetectSwichtListener);
    }


    View.OnClickListener clearListener = new View.OnClickListener() {
        @Override
        public synchronized void onClick(View v) {
            CacheManager.realtimeUeidList.clear();
            lastSortTime = new Date().getTime();
            updateView();
        }
    };

    @Override
    public void call(String key, Object val) {
        switch (key) {
            case EventAdapter.SHIELD_RPT:
                Message msg = new Message();
                msg.what = SHIELD_RPT;
                msg.obj = val;
                mHandler.sendMessage(msg);
                break;
            case EventAdapter.RF_STATUS_RPT:
                mHandler.sendEmptyMessage(RF_STATUS_RPT);
                break;
            case EventAdapter.REFRESH_IMSI:
                mHandler.sendEmptyMessage(REFRESH_IMSI);
                break;
        }

    }
}
