package com.doit.net.fragment;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.daimajia.swipe.SwipeLayout;
import com.daimajia.swipe.util.Attributes;
import com.doit.net.adapter.UeidAdapter;
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
import com.doit.net.view.RVDividerItemDecoration;

import org.xutils.DbManager;
import org.xutils.ex.DbException;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;

public class RealTimeUeidRptFragment extends BaseFragment implements EventAdapter.EventCall {

    private Button btClearRealtimeUeid;
    //    private RecyclerView recyclerView;
    private ListView mListView;


    private TextView tvRealtimeCTJCount;
    private TextView tvRealtimeCTUCount;
    private TextView tvRealtimeCTCCount;

    private CheckBox cbDetectSwitch;

    private long lastSortTime = 0;  //为了防止频繁上报排序导致列表错乱，定时排序一次

    //handler消息
    private final int SHIELD_RPT = 2;
    private final int RF_STATUS_RPT = 3;

    private DbManager dbManager;

//    //移动采集数量
//    private int realtimeCTJCount = 0;
//    //联通采集数量
//    private int realtimeCTUCount = 0;
//    //电信采集数量
//    private int realtimeCTCCount = 0;

    //移动翻译+指派数量
    private int redirectCTJCount = 0;
    //联通翻译+指派数量
    private int redirectCTUCount = 0;
    //电信翻译+指派数量
    private int redirectCTCCount = 0;

    //移动翻译数量
    private int translateCTJCount = 0;
    //联通翻译数量
    private int translateCTUCount = 0;
    //电信翻译数量
    private int translateCTCCount = 0;

    private UeidListViewAdapter mAdapter;
    private List<UeidBean> dataList = new ArrayList<>();


    public RealTimeUeidRptFragment() {
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.doit_layout_ueid_list, container, false);

//        recyclerView = rootView.findViewById(R.id.rv_ueid);
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
        dbManager = UCSIDBManager.getDbManager();

//        new Timer().schedule(new TimerTask() {
//            @Override
//            public void run() {
//                List<UeidBean> ueidList = new ArrayList<>();
//                UeidBean ueidBean = new UeidBean();
//                ueidBean.setType(1);
//                ueidBean.setImsi("4600012345678"+new Random().nextInt(99));
//                ueidBean.setSrsp(new Random().nextInt(100));
//                ueidBean.setRedirect(false);
//
//                ueidList.add(ueidBean);
//
//                EventAdapter.call(EventAdapter.SHIELD_RPT, ueidList);
//            }
//        },1000,1500);
//
//        new Timer().schedule(new TimerTask() {
//            @Override
//            public void run() {
//                List<UeidBean> ueidList = new ArrayList<>();
//                UeidBean ueidBean = new UeidBean();
//                ueidBean.setType(1);
//                ueidBean.setImsi("4600012345678"+new Random().nextInt(99));
//                ueidBean.setSrsp(new Random().nextInt(100));
//                ueidBean.setRedirect(false);
//
//                ueidList.add(ueidBean);
//
//                EventAdapter.call(EventAdapter.SHIELD_RPT, ueidList);
//            }
//        },500,2000);

        return rootView;
    }

    private void initView() {
//        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getActivity());
//        recyclerView.setLayoutManager(linearLayoutManager);
//        recyclerView.addItemDecoration(new RVDividerItemDecoration(getActivity()));
//        adapter = new UeidAdapter(getActivity(),dataList);
//        adapter.setMode(Attributes.Mode.Single);
//        recyclerView.setAdapter(adapter);
//
//        adapter.setOnClickListener(new UeidAdapter.OnClickListener() {
//            @Override
//            public void onClick(int position) {
//                LogUtils.log("点击1："+position);
//                LogUtils.log("点击2："+linearLayoutManager.findFirstVisibleItemPosition());
//                ((SwipeLayout) (recyclerView.getChildAt(position))).toggle();
//            }
//        });


        mAdapter = new UeidListViewAdapter(getActivity(), dataList);
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
        cbDetectSwitch.setOnCheckedChangeListener(rfDetectSwitchListener);
    }

    CompoundButton.OnCheckedChangeListener rfDetectSwitchListener = new CompoundButton.OnCheckedChangeListener() {
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
                EventAdapter.call(EventAdapter.SHOW_PROGRESS, 8000);
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

                                    ToastUtils.showMessage(R.string.all_rf_close);
                                    EventAdapter.call(EventAdapter.SHOW_PROGRESS, 8000);
                                    LTESendManager.closeAllRf();
                                    Send2GManager.setRFState("0");

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
                                    cbDetectSwitch.setOnCheckedChangeListener(rfDetectSwitchListener);
                                }
                            })
                            .show();
                } else {
                    ToastUtils.showMessageLong(R.string.all_rf_close);
                    EventAdapter.call(EventAdapter.SHOW_PROGRESS, 8000);

                    LTESendManager.closeAllRf();
                    Send2GManager.setRFState("0");

                    EventAdapter.call(EventAdapter.ADD_BLACKBOX, BlackBoxManger.CLOSE_ALL_4G_RF);
                    EventAdapter.call(EventAdapter.ADD_BLACKBOX, BlackBoxManger.CLOSE_ALL_2G_RF);
                }
            }
        }
    };


    /**
     * @param ueidList 新增数据
     */
    private void addRptList(List<UeidBean> ueidList) {
        for (UeidBean ueidBean : ueidList) {
            boolean isContain = false;
            LogUtils.log("侦码上报: IMSI：" + ueidBean.getImsi() + "强度：" + ueidBean.getSrsp() + ",类型" + ueidBean.getType());
            for (int i = 0; i < dataList.size(); i++) {
                if (dataList.get(i).getImsi().equals(ueidBean.getImsi())) {

                    if (ueidBean.getSrsp() < 0) {
                        dataList.get(i).setNumber(ueidBean.getNumber());  //翻译上报的，只更新手机号
                        dataList.get(i).setRedirect(true);
                    } else {
                        int times = dataList.get(i).getRptTimes();
                        if (times > 1000) {
                            times = 0;
                        }
                        dataList.get(i).setRptTimes(times + 1);

                        dataList.get(i).setSrsp(ueidBean.getSrsp());

                        if (ueidBean.getType() == 1) {
                            dataList.get(i).setType(ueidBean.getType());
                        }

                        if (ueidBean.isRedirect()) {
                            dataList.get(i).setRedirect(true);
                        }

                        dataList.get(i).setRptTime(DateUtils.convert2String(new Date().getTime(), DateUtils.LOCAL_DATE));
                    }
                    isContain = true;
                    break;
                }
            }

            if (!isContain) {
                UeidBean newUeid = new UeidBean();
                newUeid.setImsi(ueidBean.getImsi());
                if (ueidBean.getSrsp() < 0) {
                    newUeid.setSrsp(80);
                } else {
                    newUeid.setSrsp(ueidBean.getSrsp());
                }

                newUeid.setRptTime(DateUtils.convert2String(new Date().getTime(), DateUtils.LOCAL_DATE));
                newUeid.setRptTimes(1);
                if (ueidBean.getType() == 1) {
                    newUeid.setType(ueidBean.getType());
                }
                if (ueidBean.isRedirect()) {
                    newUeid.setRedirect(true);
                }
                dataList.add(newUeid);

                UCSIDBManager.saveUeidToDB(ueidBean.getImsi(), !TextUtils.isEmpty(ueidBean.getNumber()) ? ueidBean.getNumber() : "",
                        new Date().getTime(), ueidBean.getType());
            }
        }


    }

    /**
     * 翻译数量
     */
    private void translateCount() {
//        realtimeCTJCount = 0;
//        realtimeCTUCount = 0;
//        realtimeCTCCount = 0;

        //移动翻译+指派数量
        redirectCTJCount = 0;
        //联通翻译+指派数量
        redirectCTUCount = 0;
        //电信翻译+指派数量
        redirectCTCCount = 0;

        //移动翻译数量
        translateCTJCount = 0;
        //联通翻译数量
        translateCTUCount = 0;
        //电信翻译数量
        translateCTCCount = 0;

        for (int i = 0; i < dataList.size(); i++) {
            UeidBean ueidBean = dataList.get(i);
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

            if (TextUtils.isEmpty(ueidBean.getNumber())){
                String msisdn = ImsiMsisdnConvert.getMsisdnFromLocal(ueidBean.getImsi());
                if (!TextUtils.isEmpty(msisdn)) {
                    ueidBean.setNumber(msisdn);
                }
            }

            if (!TextUtils.isEmpty(ueidBean.getNumber())) {
                switch (UtilOperator.getOperatorName(ueidBean.getImsi())) {
                    case "CTJ":
                        translateCTJCount++;
                        break;
                    case "CTU":
                        translateCTUCount++;
                        break;
                    case "CTC":
                        translateCTCCount++;
                        break;
                }
            }

            if (!TextUtils.isEmpty(ueidBean.getNumber()) || ueidBean.isRedirect()) {
                switch (UtilOperator.getOperatorName(ueidBean.getImsi())) {
                    case "CTJ":
                        redirectCTJCount++;
                        break;
                    case "CTU":
                        redirectCTUCount++;
                        break;
                    case "CTC":
                        redirectCTCCount++;
                        break;
                }
            }

//            switch (UtilOperator.getOperatorName(ueidBean.getImsi())) {
//                case "CTJ":
//                    realtimeCTJCount++;
//                    break;
//                case "CTU":
//                    realtimeCTUCount++;
//                    break;
//                case "CTC":
//                    if (!ueidBean.getImsi().startsWith("46011")) {   //电信4G和2G同时存在，4G无法翻译，过滤掉46011
//                        realtimeCTCCount++;
//                    }
//                    break;
//            }
        }
    }

    /**
     * 刷新列表
     */
    private void updateView() {
        tvRealtimeCTJCount.setText(translateCTJCount + "/" + redirectCTJCount);
        tvRealtimeCTUCount.setText(translateCTUCount + "/" + redirectCTUCount);
        tvRealtimeCTCCount.setText(translateCTCCount + "/" + redirectCTCCount);
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
                    addRptList(ueidList);
                    sortRptList();
                    translateCount();
                    updateView();
                    break;
                case RF_STATUS_RPT:
                    isRFOpen();
                    break;

            }
        }
    };


    //根据强度排序
    private void sortRptList() {

        if (new Date().getTime() - lastSortTime >= 3000) {
            Collections.sort(dataList, new Comparator<UeidBean>() {
                public int compare(UeidBean o1, UeidBean o2) {

                    boolean isBlack1 = o1.isBlack();
                    int rssi1 = o1.getSrsp();
                    String phoneNumber1 = o1.getNumber();


                    boolean isBlack2 = o2.isBlack();
                    int rssi2 = o2.getSrsp();
                    String phoneNumber2 = o2.getNumber();


                    if (isBlack1 && isBlack2) {
                        return rssi2 - rssi1;
                    } else if (isBlack1) {
                        return -1;
                    } else if (isBlack2) {
                        return 1;
                    } else if (!TextUtils.isEmpty(phoneNumber1) && !TextUtils.isEmpty(phoneNumber2)) {
                        return rssi2 - rssi1;
                    } else if (!TextUtils.isEmpty(phoneNumber1)) {
                        return -1;
                    } else if (!TextUtils.isEmpty(phoneNumber2)) {
                        return 1;
                    } else {
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
        cbDetectSwitch.setOnCheckedChangeListener(rfDetectSwitchListener);
    }


    View.OnClickListener clearListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            dataList.clear();
            lastSortTime = new Date().getTime();

//            realtimeCTJCount = 0;
//            realtimeCTUCount = 0;
//            realtimeCTCCount = 0;

            //移动翻译+指派数量
            redirectCTJCount = 0;
            //联通翻译+指派数量
            redirectCTUCount = 0;
            //电信翻译+指派数量
            redirectCTCCount = 0;

            //移动翻译数量
            translateCTJCount = 0;
            //联通翻译数量
            translateCTUCount = 0;
            //电信翻译数量
            translateCTCCount = 0;

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

        }

    }
}
