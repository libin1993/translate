package com.doit.net.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.view.KeyEvent;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

import com.daimajia.swipe.SwipeLayout;
import com.daimajia.swipe.util.Attributes;
import com.doit.net.utils.FileUtils;
import com.doit.net.adapter.HistoryListViewAdapter;
import com.doit.net.view.MyTimePickDialog;
import com.doit.net.base.BaseActivity;
import com.doit.net.model.BlackBoxManger;
import com.doit.net.event.EventAdapter;
import com.doit.net.model.DBUeidInfo;
import com.doit.net.model.UCSIDBManager;
import com.doit.net.utils.DateUtils;
import com.doit.net.utils.MySweetAlertDialog;
import com.doit.net.utils.StringUtils;
import com.doit.net.ucsi.R;
import com.doit.net.utils.ToastUtils;

import org.xutils.DbManager;
import org.xutils.ex.DbException;

import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class HistoryListActivity extends BaseActivity implements EventAdapter.EventCall {

    private final Activity activity = this;
    private ListView mListView;
    private HistoryListViewAdapter mAdapter;
    private EditText editText_keyword;
    private Button button_search;
    private Button btExportSearchResult;
    private EditText etStartTime;
    private EditText etEndTime;
    private DbManager dbManager;

    private List<DBUeidInfo> dbUeidInfos = new ArrayList<>();


    //handler消息
    private final int EXPORT_ERROR = -1;
    private final int UPDATE_SEARCH_RESULT = 0;
    private final int SEARCH_HISTORT = 1;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow ().setFlags (WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.doit_layout_history_list);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        dbManager = UCSIDBManager.getDbManager();
        mListView = findViewById(R.id.lvUeidSearchRes);
        editText_keyword = findViewById(R.id.editText_keyword);
        button_search = findViewById(R.id.button_search);
        button_search.setOnClickListener(searchClick);

        etStartTime = findViewById(R.id.etStartTime);
        etStartTime.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MyTimePickDialog myTimePicKDialog = new MyTimePickDialog(activity, etStartTime.getText().toString());
                myTimePicKDialog.dateTimePicKDialog(etStartTime);
            }
        });
        etEndTime = findViewById(R.id.etEndTime);
        etEndTime.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MyTimePickDialog myTimePicKDialog = new MyTimePickDialog(activity, etEndTime.getText().toString());
                myTimePicKDialog.dateTimePicKDialog(etEndTime);
            }
        });
        btExportSearchResult = findViewById(R.id.btExportSearchResult);
        btExportSearchResult.setOnClickListener(exportClick);

        mAdapter = new HistoryListViewAdapter(this);
        mListView.setAdapter(mAdapter);
        mAdapter.setMode(Attributes.Mode.Single);
        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                ((SwipeLayout) (mListView.getChildAt(position - mListView.getFirstVisiblePosition()))).open(true);
                ((SwipeLayout) (mListView.getChildAt(position - mListView.getFirstVisiblePosition()))).setClickToClose(true);
            }
        });


        EventAdapter.register(EventAdapter.RESEARCH_HISTORY_LIST,this);
    }



    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            if (msg.what == UPDATE_SEARCH_RESULT) {
                if (mAdapter != null) {
                    mAdapter.refreshData();
                }
            }else if(msg.what == EXPORT_ERROR){
                new MySweetAlertDialog(activity, MySweetAlertDialog.ERROR_TYPE)
                        .setTitleText("导出失败")
                        .setContentText("失败原因："+msg.obj)
                        .show();
            }else if(msg.what == SEARCH_HISTORT){
                button_search.performClick();
            }
        }
    };


    @Override
    protected void onResume() {
        clearSearchHistory();
        super.onResume();
    }

    private void clearSearchHistory() {
        if (dbUeidInfos == null)
            dbUeidInfos = new ArrayList<>();

        dbUeidInfos.clear();
        mAdapter.setUeidList(dbUeidInfos);
        mHandler.sendEmptyMessage(UPDATE_SEARCH_RESULT);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                startActivity(new Intent(this, MainActivity.class));
                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }

    View.OnClickListener searchClick = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            String keyword = editText_keyword.getText().toString();
            String startTime = etStartTime.getText().toString();
            String endTime = etEndTime.getText().toString();

            if (("".equals(startTime) && !"".equals(endTime)) || ((!"".equals(startTime) && "".equals(endTime)))){
                ToastUtils.showMessage("未设置开始时间及结束时间！");
                return;
            } else if (!"".equals(startTime) && startTime.equals(endTime)){
                ToastUtils.showMessage("开始时间和结束时间一样，请重新设置！");
                return;
            } else if (!"".equals(startTime) && !"".equals(endTime) && !isStartEndTimeOrderRight(startTime, endTime)){
                ToastUtils.showMessage( "开始时间比结束时间晚，请重新设置！");
                return;
            }

            if ("".equals(startTime)){
                try {
                    dbUeidInfos = dbManager.selector(DBUeidInfo.class)
                            .where("imsi", "like", "%" + keyword + "%")
                            .or("msisdn","like","%" + keyword + "%")
                            .orderBy("id", true)
                            .findAll();
                } catch (DbException e) {
                    e.printStackTrace();
                }
            }else{
                try {
                    dbUeidInfos = dbManager.selector(DBUeidInfo.class)
                            .where("imsi", "like", "%" + keyword + "%")
                            .and("createDate", "BETWEEN",
                                    new long[]{DateUtils.convert2long(startTime, DateUtils.LOCAL_DATE), DateUtils.convert2long(endTime, DateUtils.LOCAL_DATE)})
                            .orderBy("id", true)
                            .findAll();
                } catch (DbException e) {
                    e.printStackTrace();
                }
            }

            if (dbUeidInfos == null || dbUeidInfos.size() <= 0) {
                clearSearchHistory();
                ToastUtils.showMessage(R.string.search_not_found);
                return;
            }

            mAdapter.setUeidList(dbUeidInfos);
            mHandler.sendEmptyMessage(UPDATE_SEARCH_RESULT);
        }
    };

    View.OnClickListener exportClick = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if (dbUeidInfos == null || dbUeidInfos.size() <= 0) {
                ToastUtils.showMessage( R.string.can_not_export_search);
                return;
            }

            String fileName = "UEID_"+ DateUtils.getStrOfDate()+".csv";
            String fullPath = FileUtils.ROOT_PATH+fileName;
            BufferedWriter bufferedWriter = null;
            try {
                bufferedWriter = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(fullPath,true)));
                bufferedWriter.write("imsi,手机号,时间"+"\r\n");
                for (DBUeidInfo info: dbUeidInfos) {

                    bufferedWriter.write(info.getImsi()+",");
                    bufferedWriter.write(info.getMsisdn()+",");
                    bufferedWriter.write(DateUtils.convert2String(info.getCreateDate(), DateUtils.LOCAL_DATE));

                    bufferedWriter.write("\r\n");
                }
            } catch (DbException e) {
                //log.error("Export SELECT ERROR",e);
                createExportError("数据查询错误");
            } catch (FileNotFoundException e){
                //log.error("File Error",e);
                createExportError("文件未创建成功");
            } catch (IOException e){
                //log.error("File Error",e);
                createExportError("写入文件错误");
            } finally {
                if(bufferedWriter != null){
                    try {
                        bufferedWriter.close();
                    } catch (IOException e) {
                    }
                }
            }

            EventAdapter.call(EventAdapter.UPDATE_FILE_SYS, fullPath);
            new MySweetAlertDialog(activity, MySweetAlertDialog.TEXT_SUCCESS)
                    .setTitleText("导出成功")
                    .setContentText("文件导出在：手机存储/"+FileUtils.ROOT_DIRECTORY+"/"+ fileName)
                    .show();

            EventAdapter.call(EventAdapter.ADD_BLACKBOX,BlackBoxManger.EXPORT_HISTORT_DATA+ fullPath);
        }
    };

    public boolean isStartEndTimeOrderRight(String startTime, String endTime){
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

        Date dataStartTime = null;
        try {
            dataStartTime = simpleDateFormat.parse(startTime);
        } catch (ParseException e) {e.printStackTrace();}

        Date dateEndTime = null;
        try {
            dateEndTime = simpleDateFormat.parse(endTime);
        } catch (ParseException e) {e.printStackTrace();}

        return dataStartTime.before(dateEndTime);
    }

    private void createExportError(String obj){
        Message msg = new Message();
        msg.what = EXPORT_ERROR;
        msg.obj=obj;
        mHandler.sendMessage(msg);
    }


    @Override
    public void call(String key, Object val) {
        switch (key){
            case EventAdapter.RESEARCH_HISTORY_LIST:
                mHandler.sendEmptyMessage(SEARCH_HISTORT);
                break;
        }

    }
}
