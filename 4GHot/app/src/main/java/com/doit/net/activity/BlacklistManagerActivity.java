package com.doit.net.activity;

import android.annotation.SuppressLint;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.os.Handler;
import android.os.Message;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.TextView;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.BaseViewHolder;
import com.daimajia.swipe.SwipeLayout;
import com.daimajia.swipe.util.Attributes;
import com.doit.net.model.CacheManager;
import com.doit.net.protocol.LTESendManager;
import com.doit.net.protocol.Send2GManager;
import com.doit.net.utils.FileUtils;
import com.doit.net.utils.FormatUtils;
import com.doit.net.view.AddBlacklistDialog;
import com.doit.net.adapter.BlacklistAdapter;
import com.doit.net.base.BaseActivity;
import com.doit.net.event.EventAdapter;
import com.doit.net.model.BlackBoxManger;
import com.doit.net.model.UCSIDBManager;
import com.doit.net.model.BlackListInfo;
import com.doit.net.utils.MySweetAlertDialog;
import com.doit.net.utils.ToastUtils;
import com.doit.net.utils.LogUtils;
import com.doit.net.bean.FileBean;
import com.doit.net.ucsi.R;

//import org.apache.poi.hssf.usermodel.HSSFDateUtil;
//import org.apache.poi.ss.usermodel.Cell;
//import org.apache.poi.ss.usermodel.CellValue;
//import org.apache.poi.ss.usermodel.FormulaEvaluator;
//import org.apache.poi.ss.usermodel.Row;
//import org.apache.poi.ss.usermodel.Sheet;
//import org.apache.poi.ss.usermodel.Workbook;
//import org.apache.poi.ss.usermodel.WorkbookFactory;
//import org.apache.poi.xssf.usermodel.XSSFSheet;
//import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.apache.poi.hssf.usermodel.HSSFDateUtil;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellValue;
import org.apache.poi.ss.usermodel.FormulaEvaluator;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.apache.poi.ss.util.WorkbookUtil;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.xutils.DbManager;
import org.xutils.ex.DbException;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import cn.pedant.SweetAlert.SweetAlertDialog;

public class BlacklistManagerActivity extends BaseActivity implements EventAdapter.EventCall {
    private ListView lvBlacklistInfo;
    private BlacklistAdapter mAdapter;
    private Button btAddBlacklist;
    private Button btExportBlacklist;
    private Button btImportBlacklist;
    private Button btClearBlacklist;

    DbManager dbManager = UCSIDBManager.getDbManager();

    private List<BlackListInfo> listWhitelistInfo = new ArrayList<>();

    private int lastOpenSwipePos = -1;

    private static final String BLACKLIST_FILE_PATH = FileUtils.ROOT_PATH + "Blacklist.xls";

    //handler消息
    private final int REFRESH_LIST = 0;
    private final int UPDATE_LIST = 1;
    private final int UPDATE_BLACKLIST = 2;
    private final int EXPORT_ERROR = -1;
    private final static int IMPORT_SUCCESS = 3;  //导入成功
    private final static int EXPORT_SUCCESS = 4;  //导出成功

    private MySweetAlertDialog mProgressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_blacklist_manage);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        initView();

        updateListFromDB();
        EventAdapter.register(EventAdapter.UPDATE_BLACKLIST, this);
        EventAdapter.register(EventAdapter.REFRESH_BLACKLIST, this);
    }

    private void initView() {
        lvBlacklistInfo = findViewById(R.id.lvWhitelistInfo);
        btAddBlacklist = findViewById(R.id.btAddWhitelist);
        btAddBlacklist.setOnClickListener(addWhitelistClick);

        btImportBlacklist = findViewById(R.id.btImportWhitelist);
        btImportBlacklist.setOnClickListener(importWhitelistClick);

        btExportBlacklist = findViewById(R.id.btExportWhitelist);
        btExportBlacklist.setOnClickListener(exortWhitelistClick);

        btClearBlacklist = findViewById(R.id.btClearWhitelist);
        btClearBlacklist.setOnClickListener(clearWhitelistClick);

        mAdapter = new BlacklistAdapter(this);
        lvBlacklistInfo.setAdapter(mAdapter);
        mAdapter.setMode(Attributes.Mode.Single);
        lvBlacklistInfo.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                lastOpenSwipePos = position - lvBlacklistInfo.getFirstVisiblePosition();
                openSwipe(lastOpenSwipePos);
            }
        });

        mProgressDialog = new MySweetAlertDialog(this, MySweetAlertDialog.PROGRESS_TYPE);
        mProgressDialog.setTitleText("加载中，请耐心等待...");
        mProgressDialog.setCancelable(false);
    }


    View.OnClickListener addWhitelistClick = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            AddBlacklistDialog addBlacklistDialog = new AddBlacklistDialog(BlacklistManagerActivity.this);
            addBlacklistDialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
                @Override
                public void onDismiss(DialogInterface dialog) {
                    updateListFromDB();

                }
            });
            addBlacklistDialog.show();
        }
    };

    private boolean isWhitelistExist(String imsi, String msisdn, List<BlackListInfo> listWhitelistFromFile) {
        if (listWhitelistInfo != null) {
            for (BlackListInfo tmpdbWhiteInfo : listWhitelistInfo) {
                if ((!"".equals(imsi) && tmpdbWhiteInfo.getImsi().equals(imsi)) || (!"".equals(msisdn) && tmpdbWhiteInfo.getMsisdn().equals(msisdn)))
                    return true;
            }
        }

        if (listWhitelistFromFile != null) {
            for (BlackListInfo tmpdbWhiteInfo : listWhitelistFromFile) {
                if ((!"".equals(imsi) && tmpdbWhiteInfo.getImsi().equals(imsi)) || (!"".equals(msisdn) && tmpdbWhiteInfo.getMsisdn().equals(msisdn)))
                    return true;
            }
        }

        return false;
    }

    public static boolean isNumeric(String str) {
        Pattern pattern = Pattern.compile("[0-9]*");
        return pattern.matcher(str).matches();
    }


    View.OnClickListener importWhitelistClick = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            File file = new File(FileUtils.ROOT_PATH);
            if (!file.exists()) {
                ToastUtils.showMessageLong("未找到黑名单，请确认已将黑名单放在\"手机存储/" + FileUtils.ROOT_DIRECTORY + "\"目录下");
                return;
            }

            File[] files = file.listFiles();
            if (files == null || files.length == 0) {
                ToastUtils.showMessageLong("未找到黑名单，请确认已将黑名单放在\"手机存储/" + FileUtils.ROOT_DIRECTORY + "\"目录下");
                return;
            }

            List<FileBean> fileList = new ArrayList<>();

            for (int i = 0; i < files.length; i++) {
                String tmpFileName = files[i].getName();
                if (tmpFileName.endsWith(".xls") || tmpFileName.endsWith(".xlsx")) {
                    FileBean fileBean = new FileBean();
                    fileBean.setFileName(tmpFileName);
                    fileBean.setCheck(false);
                    fileList.add(fileBean);
                }
            }

            if (fileList.size() == 0) {
                ToastUtils.showMessageLong("\"手机存储/" + FileUtils.ROOT_DIRECTORY + "\"目录下未找到黑名单，黑名单必须是以\".xls\"或\".xlsx\"为后缀的文件");
                return;
            }


            fileList.get(0).setCheck(true);  //默认选中第一个

            View dialogView = LayoutInflater.from(BlacklistManagerActivity.this).inflate(R.layout.layout_import_whitelist, null);
            PopupWindow mPopupWindow = new PopupWindow(dialogView, FormatUtils.getInstance().dip2px(300), ViewGroup.LayoutParams.WRAP_CONTENT);

            //设置Popup具体控件
            RecyclerView rvFile = dialogView.findViewById(R.id.rv_file);
            Button btnCancel = dialogView.findViewById(R.id.btn_cancel_import);
            Button btnConfirm = dialogView.findViewById(R.id.btn_confirm_import);
            TextView tvTitle = dialogView.findViewById(R.id.tv_import_whitelist);
            tvTitle.setText("请选择黑名单文件");


            //设置Popup具体参数
            mPopupWindow.setBackgroundDrawable(new BitmapDrawable(getResources(), (Bitmap) null));
            mPopupWindow.setFocusable(true);//点击空白，popup不自动消失
            mPopupWindow.setTouchable(true);//popup区域可触摸
            mPopupWindow.setOutsideTouchable(true);//非popup区域可触摸
            mPopupWindow.showAtLocation(getWindow().getDecorView(), Gravity.CENTER, 0, 0);


            rvFile.setLayoutManager(new LinearLayoutManager(BlacklistManagerActivity.this));
            BaseQuickAdapter<FileBean, BaseViewHolder> adapter = new BaseQuickAdapter<FileBean, BaseViewHolder>(R.layout.layout_file_item, fileList) {
                @Override
                protected void convert(BaseViewHolder helper, FileBean item) {
                    helper.setText(R.id.tv_file_name, item.getFileName());
                    helper.setVisible(R.id.iv_select_whitelist, item.isCheck());
                }
            };

            rvFile.setAdapter(adapter);


            adapter.setOnItemClickListener(new BaseQuickAdapter.OnItemClickListener() {
                @Override
                public void onItemClick(BaseQuickAdapter adapter, View view, int position) {
                    for (int i = 0; i < fileList.size(); i++) {
                        fileList.get(i).setCheck(i == position);
                    }
                    adapter.notifyDataSetChanged();
                }
            });

            btnCancel.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mPopupWindow.dismiss();
                }
            });

            btnConfirm.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mPopupWindow.dismiss();

                    importWhitelist(fileList);

                }
            });

        }
    };

    /**
     * @param fileList 导入白名单
     */
    private void importWhitelist(List<FileBean> fileList) {
        if (mProgressDialog != null && !mProgressDialog.isShowing()) {
            mProgressDialog.show();
        }
        new Thread() {
            @Override
            public void run() {

                File file = null;
                for (FileBean fileBean : fileList) {
                    if (fileBean.isCheck()) {
                        file = new File(FileUtils.ROOT_PATH + fileBean.getFileName());
                        break;
                    }
                }
                if (file == null) {
                    return;
                }
                int validImportNum = 0;
                int repeatNum = 0;
                int errorFormatNum = 0;
                try {
                    InputStream stream = new FileInputStream(file);
                    Workbook workbook = WorkbookFactory.create(stream);
                    Sheet sheet = workbook.getSheetAt(0);  //示意访问sheet
                    int rowsCount = sheet.getPhysicalNumberOfRows();
                    FormulaEvaluator formulaEvaluator = workbook.getCreationHelper().createFormulaEvaluator();
                    List<BlackListInfo> listValidWhite = new ArrayList<>();
                    for (int r = 0; r < rowsCount; r++) {
                        String imsiInLine = "";
                        String msisdnInLine = "";
                        String remark = "";
                        Row row = sheet.getRow(r);
                        int cellsCount = row.getPhysicalNumberOfCells();


                        imsiInLine = getCellAsString(row, 0, formulaEvaluator);
                        msisdnInLine = getCellAsString(row, 1, formulaEvaluator);
                        remark = getCellAsString(row, 2, formulaEvaluator);

                        if ("IMSI".equals(imsiInLine) && msisdnInLine.equals("手机号") && "备注".equals(remark)) {
                            continue;
                        }


                        if (TextUtils.isEmpty(msisdnInLine) ||
                                !isNumeric(msisdnInLine) || msisdnInLine.length() != 11) {
                            errorFormatNum++;
                            continue;
                        }


                        if (isWhitelistExist(imsiInLine, msisdnInLine, listValidWhite)) {
                            repeatNum++;
                            continue;
                        }

                        if (!TextUtils.isEmpty(remark) && remark.length() > 8) {
                            remark = remark.substring(0, 8);
                        }
                        listValidWhite.add(new BlackListInfo(imsiInLine, msisdnInLine, remark,0));
                        validImportNum++;
                        if (validImportNum > 100)  //白名单最大100
                            break;

                    }
                    stream.close();
                    dbManager.save(listValidWhite);

                    Send2GManager.setBlackList();

                    if (listValidWhite.size() > 0) {
                        StringBuilder imsi = new StringBuilder();
                        for (int i = 0; i < listValidWhite.size(); i++) {
                            if (!TextUtils.isEmpty(listValidWhite.get(i).getImsi())){
                                imsi.append(listValidWhite.get(i).getImsi()).append(",");
                            }
                        }

                        if (!TextUtils.isEmpty(imsi.toString()) && !CacheManager.getLocState()){
                            LTESendManager.changeNameList("add", "block", imsi.substring(0,imsi.length()-1));
                        }
                    }


                    Message message = new Message();
                    message.what = IMPORT_SUCCESS;
                    message.obj = "成功导入" + validImportNum + "个名单，忽略" +
                            repeatNum + "个重复的名单，忽略" + errorFormatNum + "行格式或号码错误";
                    mHandler.sendMessage(message);

                    EventAdapter.call(EventAdapter.ADD_BLACKBOX, BlackBoxManger.IMPORT_BLACKLIST + file.getName());

                } catch (Exception e) {
                    /* proper exception handling to be here */
                    LogUtils.log("导入黑名单错误" + e.toString());
                    createExportError("写入文件错误");
                }

            }
        }.start();
    }

    /**
     * @param row
     * @param c
     * @param formulaEvaluator
     * @return 获取单元格值
     */
    protected String getCellAsString(Row row, int c, FormulaEvaluator formulaEvaluator) {
        String value = "";
        try {
            Cell cell = row.getCell(c);
            CellValue cellValue = formulaEvaluator.evaluate(cell);
            switch (cellValue.getCellType()) {
                case Cell.CELL_TYPE_BOOLEAN:
                    value = "" + cellValue.getBooleanValue();
                    break;
                case Cell.CELL_TYPE_NUMERIC:

                    if (HSSFDateUtil.isCellDateFormatted(cell)) {
                        double date = cellValue.getNumberValue();
                        SimpleDateFormat formatter =
                                new SimpleDateFormat("dd/MM/yy");
                        value = formatter.format(HSSFDateUtil.getJavaDate(date));
                    } else {
                        DecimalFormat df = new DecimalFormat("#.########");  //去除科学计数法
                        value = df.format(cell.getNumericCellValue());
                    }
                    break;
                case Cell.CELL_TYPE_STRING:
                    value = "" + cellValue.getStringValue();
                    break;
                default:
            }
        } catch (NullPointerException e) {
            /* proper error handling should be here */
            LogUtils.log("黑名单解析异常：" + e.toString());
        }
        return value;
    }

    View.OnClickListener exortWhitelistClick = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if (mProgressDialog != null && !mProgressDialog.isShowing()) {
                mProgressDialog.show();
            }
            new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        File file = new File(BLACKLIST_FILE_PATH);
                        if (file.exists()) {
                            file.delete();
                        }

                        XSSFWorkbook workbook = new XSSFWorkbook();
                        XSSFSheet sheet = workbook.createSheet(WorkbookUtil.createSafeSheetName("黑名单"));

                        Row row = sheet.createRow(0);
                        Cell cell1 = row.createCell(0);
                        cell1.setCellValue("IMSI");
                        Cell cell2 = row.createCell(1);
                        cell2.setCellValue("手机号");
                        Cell cell3 = row.createCell(2);
                        cell3.setCellValue("备注");


                        if (listWhitelistInfo == null || listWhitelistInfo.size() == 0) {
                            ToastUtils.showMessageLong("当前名单为空，此次导出为模板");
                        } else {
                            for (int i = 0; i < listWhitelistInfo.size(); i++) {
                                Row rowi = sheet.createRow(i + 1);
                                rowi.createCell(0).setCellValue(listWhitelistInfo.get(i).getImsi() + "");
                                rowi.createCell(1).setCellValue(listWhitelistInfo.get(i).getMsisdn() + "");
                                rowi.createCell(2).setCellValue(listWhitelistInfo.get(i).getRemark() + "");
                            }
                        }

                        OutputStream outputStream = new FileOutputStream(file);
                        workbook.write(outputStream);
                        outputStream.flush();
                        outputStream.close();

                        EventAdapter.call(EventAdapter.UPDATE_FILE_SYS, BLACKLIST_FILE_PATH);

                        Message message = new Message();
                        message.what = EXPORT_SUCCESS;
                        message.obj = "文件导出在：手机存储/" + FileUtils.ROOT_DIRECTORY + "/" + file.getName();
                        mHandler.sendMessage(message);

                        EventAdapter.call(EventAdapter.ADD_BLACKBOX, BlackBoxManger.EXPORT_BLACKLIST + BLACKLIST_FILE_PATH);

                    } catch (Exception e) {
                        /* proper exception handling to be here */
                        createExportError("导出名单失败");
                    }
                }
            }).start();


        }
    };


    View.OnClickListener clearWhitelistClick = new View.OnClickListener() {
        @Override

        public void onClick(View v) {
            new MySweetAlertDialog(BlacklistManagerActivity.this, MySweetAlertDialog.WARNING_TYPE)
                    .setTitleText("清空名单")
                    .setContentText("确定要清空黑名单吗？")
                    .setCancelText(getString(R.string.cancel))
                    .setConfirmText(getString(R.string.sure))
                    .showCancelButton(true)
                    .setConfirmClickListener(new MySweetAlertDialog.OnSweetClickListener() {

                        @Override
                        public void onClick(MySweetAlertDialog sweetAlertDialog) {
                            sweetAlertDialog.dismiss();

                            try {
                                dbManager.delete(BlackListInfo.class);
                            } catch (DbException e) {
                                e.printStackTrace();
                            }

                            Send2GManager.setBlackList();

                            updateListFromDB();
                            EventAdapter.call(EventAdapter.ADD_BLACKBOX, BlackBoxManger.CLEAR_BLACKLIST);

                        }
                    })
                    .show();

        }
    };


    void updateListFromDB() {
        try {
            listWhitelistInfo = dbManager.selector(BlackListInfo.class).findAll();
            if (listWhitelistInfo == null)
                return;

            mAdapter.setUserInfoList(listWhitelistInfo);

            mHandler.sendEmptyMessage(REFRESH_LIST);
        } catch (DbException e) {
            e.printStackTrace();
        }
    }

    private void openSwipe(int position) {
        if (position < 0)
            return;

        ((SwipeLayout) (lvBlacklistInfo.getChildAt(position))).open(true);
        ((SwipeLayout) (lvBlacklistInfo.getChildAt(position))).setClickToClose(true);
    }

    private void closeSwipe(int position) {
        if (listWhitelistInfo == null || listWhitelistInfo.size() == 0)
            return;

        SwipeLayout swipe = (SwipeLayout) (lvBlacklistInfo.getChildAt(position));
        if (swipe != null)
            swipe.close(true);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void createExportError(String obj) {
        Message msg = new Message();
        msg.what = EXPORT_ERROR;
        msg.obj = obj;
        mHandler.sendMessage(msg);
    }

    @SuppressLint("HandlerLeak")
    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            if (msg.what == REFRESH_LIST) {
                if (mAdapter != null) {
                    mAdapter.updateView();
                }
            } else if (msg.what == UPDATE_LIST) {
                updateListFromDB();
                closeSwipe(lastOpenSwipePos);
            } else if (msg.what == UPDATE_BLACKLIST) {
                updateListFromDB();

            } else if (msg.what == EXPORT_ERROR) {
                if (mProgressDialog != null && mProgressDialog.isShowing()) {
                    mProgressDialog.dismiss();
                }
                new SweetAlertDialog(BlacklistManagerActivity.this, SweetAlertDialog.ERROR_TYPE)
                        .setTitleText("导出失败")
                        .setContentText("失败原因：" + msg.obj)
                        .show();
            } else if (msg.what == IMPORT_SUCCESS) {
                if (mProgressDialog != null && mProgressDialog.isShowing()) {
                    mProgressDialog.dismiss();
                }
                new MySweetAlertDialog(BlacklistManagerActivity.this, MySweetAlertDialog.TEXT_SUCCESS)
                        .setTitleText("导入完成")
                        .setContentText(String.valueOf(msg.obj))
                        .show();

                updateListFromDB();
            } else if (msg.what == EXPORT_SUCCESS) {
                if (mProgressDialog != null && mProgressDialog.isShowing()) {
                    mProgressDialog.dismiss();
                }
                new MySweetAlertDialog(BlacklistManagerActivity.this, MySweetAlertDialog.TEXT_SUCCESS)
                        .setTitleText("导出成功")
                        .setContentText(String.valueOf(msg.obj))
                        .show();
            }
        }
    };


    @Override
    public void call(String key, Object val) {
        switch (key) {
            case EventAdapter.UPDATE_BLACKLIST:
                Message msg = new Message();
                msg.what = UPDATE_BLACKLIST;
                msg.obj = val;
                mHandler.sendMessage(msg);
                break;
            case EventAdapter.REFRESH_BLACKLIST:
                mHandler.sendEmptyMessage(UPDATE_LIST);
                break;
        }
    }
}

