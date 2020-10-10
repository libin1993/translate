package com.doit.net.adapter;

import android.content.Context;
import android.content.DialogInterface;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.daimajia.swipe.adapters.BaseSwipeAdapter;
import com.doit.net.model.DBUeidInfo;
import com.doit.net.model.ImsiMsisdnConvert;
import com.doit.net.view.AddWhitelistDialog;
import com.doit.net.view.ModifyWhitelistDialog;
import com.doit.net.application.MyApplication;
import com.doit.net.bean.UeidBean;
import com.doit.net.event.AddToLocalBlackListener;
import com.doit.net.event.AddToLocationListener;
import com.doit.net.model.CacheManager;
import com.doit.net.model.DBBlackInfo;
import com.doit.net.model.UCSIDBManager;
import com.doit.net.model.VersionManage;
import com.doit.net.model.WhiteListInfo;
import com.doit.net.utils.LogUtils;
import com.doit.net.utils.UtilOperator;
import com.doit.net.ucsi.R;

import org.xutils.DbManager;
import org.xutils.ex.DbException;

public class UeidListViewAdapter extends BaseSwipeAdapter {

    private Context mContext;

    //    private onItemLongClickListener mOnItemLongClickListener;
    private MotionEvent motionEvent;

    public UeidListViewAdapter(Context mContext) {
        this.mContext = mContext;
    }

    private DbManager dbManager;


    @Override
    public int getSwipeLayoutResourceId(int position) {
        return R.id.swipe;
    }

    @Override
    public View generateView(int position, ViewGroup parent) {
        View v = LayoutInflater.from(mContext).inflate(R.layout.doit_layout_ueid_list_item, null);
        dbManager = UCSIDBManager.getDbManager();

        return v;
    }


    @Override
    public  void fillValues(int position, View convertView) {
        LinearLayout layoutItemText = convertView.findViewById(R.id.layoutItemText);
        if (position % 2 == 0) {
            layoutItemText.setBackgroundColor(mContext.getResources().getColor(R.color.deepgrey2));
        } else {
            layoutItemText.setBackgroundColor(mContext.getResources().getColor(R.color.black));
        }

        TextView index = convertView.findViewById(R.id.position);
        index.setText((position + 1) + ".");

        TextView tvContent = convertView.findViewById(R.id.tvUeidItemText);
        UeidBean resp = CacheManager.realtimeUeidList.get(position);




        if (VersionManage.isPoliceVer()) {
            convertView.findViewById(R.id.add_to_black).setOnClickListener(new AddToLocalBlackListener(mContext, resp.getImsi()));
        } else if (VersionManage.isArmyVer()) {
            convertView.findViewById(R.id.add_to_black).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    try {
                        WhiteListInfo info = dbManager.selector(WhiteListInfo.class).where("imsi", "=", resp.getImsi()).findFirst();
                        if (info != null) {
                            ModifyWhitelistDialog modifyWhitelistDialog = new ModifyWhitelistDialog(mContext,
                                    resp.getImsi(), info.getMsisdn(), info.getRemark(),false);
                            modifyWhitelistDialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
                                @Override
                                public void onDismiss(DialogInterface dialog) {
                                    notifyDataSetChanged();
                                }
                            });
                            modifyWhitelistDialog.show();
                        }else {
                            String msisdn = ImsiMsisdnConvert.getMsisdnFromLocal(resp.getImsi());
                            AddWhitelistDialog addWhitelistDialog = new AddWhitelistDialog(mContext, resp.getImsi(),msisdn);
                            addWhitelistDialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
                                @Override
                                public void onDismiss(DialogInterface dialog) {
                                    notifyDataSetChanged();
                                }
                            });
                            addWhitelistDialog.show();
                        }
                    } catch (DbException e) {
                        e.printStackTrace();
                    }
                }
            });
        }

        if (CacheManager.getLocMode()) {
            convertView.findViewById(R.id.add_to_localtion).setOnClickListener(new AddToLocationListener(mContext, resp.getImsi(), resp.getType()));
        } else {
            convertView.findViewById(R.id.add_to_localtion).setVisibility(View.GONE);
        }

//        if (mOnItemLongClickListener != null) {
//            //获取触摸点的坐标，以决定pop从哪里弹出
//            convertView.setOnTouchListener(new View.OnTouchListener() {
//                @SuppressLint("ClickableViewAccessibility")
//                @Override
//                public boolean onTouch(View v, MotionEvent event) {
//                    switch (event.getAction()) {
//                        case MotionEvent.ACTION_DOWN:
//                            motionEvent = event;
//                            break;
//                        default:
//                            break;
//                    }
//                    // 如果onTouch返回false,首先是onTouch事件的down事件发生，此时，如果长按，触发onLongClick事件；
//                    // 然后是onTouch事件的up事件发生，up完毕，最后触发onClick事件。
//                    return false;
//                }
//            });
//
//
//            final int pos = position;
//            convertView.setOnLongClickListener(new View.OnLongClickListener() {
//                @Override
//                public boolean onLongClick(View v) {
//                    //int position = holder.getLayoutPosition();
//                    mOnItemLongClickListener.onItemLongClick(motionEvent, pos);
//                    //返回true 表示消耗了事件 事件不会继续传递
//                    return true; //长按了就禁止swipe弹出
//                }
//            });
//        }

        checkBlackWhiteList(resp, tvContent);

    }

    private void checkBlackWhiteList(UeidBean resp, TextView tvContent) {
        String type = "";
        if (resp.getType() == 0 ){
            type += "2G";
        }else {
            type += "4G";
        }
        String content = "IMSI：" + resp.getImsi() + "          " + "制式: " + UtilOperator.getOperatorNameCH(resp.getImsi())+type + "\n";

        //优先先检查是否为黑名单
        if (VersionManage.isPoliceVer()) {
            DBBlackInfo dbBlackInfo = null;
            try {
                dbBlackInfo = dbManager.selector(DBBlackInfo.class).where("imsi", "=", resp.getImsi()).findFirst();
            } catch (DbException e) {
                LogUtils.log("查询黑名单异常" + e.getMessage());
            }

            content += mContext.getString(R.string.ueid_last_rpt_time) + resp.getRptTime();
            if (dbBlackInfo != null) {
                String name = dbBlackInfo.getName();
                String remark = dbBlackInfo.getRemark();

                if (!TextUtils.isEmpty(name)) {
                    content += "\n" + mContext.getString(R.string.lab_name) + name + "         ";
                }

                if (!TextUtils.isEmpty(remark)) {
                    if (!TextUtils.isEmpty(name)) {
                        content += remark;
                    } else {
                        content += "\n" + remark;
                    }
                }

                tvContent.setTextColor(MyApplication.mContext.getResources().getColor(R.color.red));
            } else {
                tvContent.setTextColor(MyApplication.mContext.getResources().getColor(R.color.white));
            }

            if (resp.getType() == 1 ){
                content += "      2G";
            }else {
                content += "      4G";
            }

            tvContent.setText(content);
        }

        //如果是管控模式，其次检查白名单
        if (CacheManager.currentWorkMode.equals("2")) {

            try {
                if (!"".equals(resp.getImsi())) {

                    content += resp.getRptTime() + "       " + "次数：" + resp.getRptTimes() + "       "
                            + mContext.getString(R.string.ueid_last_intensity) + resp.getSrsp();
                    WhiteListInfo info = dbManager.selector(WhiteListInfo.class).where("imsi", "=", resp.getImsi()).findFirst();
                    if (info != null) {
                        String msisdn = info.getMsisdn();
                        String remark = info.getRemark();
                        if (!TextUtils.isEmpty(msisdn)) {
                            content += "\n" + "手机号：" + msisdn + "           ";
                        }

                        if (!TextUtils.isEmpty(remark)) {
                            if (!TextUtils.isEmpty(msisdn)) {
                                content += remark;
                            } else {
                                content += "\n" + remark;
                            }
                        }

                        tvContent.setTextColor(MyApplication.mContext.getResources().getColor(R.color.forestgreen));

                    } else {
                        String msisdn = ImsiMsisdnConvert.getMsisdnFromLocal(resp.getImsi());
                        if (!TextUtils.isEmpty(msisdn)){
                            content += "\n" + "手机号：" + msisdn;
                        }

                        tvContent.setTextColor(MyApplication.mContext.getResources().getColor(R.color.white));

                    }


                    tvContent.setText(content);
                }

            } catch (DbException e) {
                LogUtils.log("查询白名单异常" + e.getMessage());
            }
        }


    }

    @Override
    public int getCount() {
        return CacheManager.realtimeUeidList.size();
    }

    @Override
    public Object getItem(int position) {
        return null;
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    public interface onItemLongClickListener {
        void onItemLongClick(MotionEvent motionEvent, int position);
    }

}
