package com.doit.net.adapter;

import android.content.Context;
import android.content.DialogInterface;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.daimajia.swipe.SwipeLayout;
import com.daimajia.swipe.adapters.BaseSwipeAdapter;
import com.doit.net.activity.HistoryListActivity;
import com.doit.net.event.AddToLocationListener;
import com.doit.net.model.CacheManager;
import com.doit.net.model.DBUeidInfo;
import com.doit.net.model.UCSIDBManager;

import com.doit.net.model.BlackListInfo;
import com.doit.net.view.AddBlacklistDialog;
import com.doit.net.view.ModifyWhitelistDialog;
import com.doit.net.ucsi.R;
import com.doit.net.utils.DateUtils;

import org.xutils.DbManager;
import org.xutils.ex.DbException;

import java.util.ArrayList;
import java.util.List;

public class HistoryListViewAdapter extends BaseSwipeAdapter {

    private DbManager dbManager;
    private Context mContext;

    private static List<DBUeidInfo> ueidList = new ArrayList<>();

    public HistoryListViewAdapter(Context mContext) {
        this.mContext = mContext;
        dbManager = UCSIDBManager.getDbManager();
    }

    public void refreshData(){
        notifyDataSetChanged();
    }

    public void setUeidList(List<DBUeidInfo> ueidList) {
        HistoryListViewAdapter.ueidList = ueidList;
    }

    @Override
    public int getSwipeLayoutResourceId(int position) {
        return R.id.swipe;
    }

    @Override
    public View generateView(final int position, ViewGroup parent) {
        View v = LayoutInflater.from(mContext).inflate(R.layout.doit_layout_ueid_list_item, null);

        return v;
    }

    @Override
    public void fillValues(int position, View convertView) {
        TextView index = convertView.findViewById(R.id.position);
        index.setText((position + 1) + ".");

        TextView text_data = convertView.findViewById(R.id.tvUeidItemText);
        SwipeLayout swipeLayout = convertView.findViewById(R.id.swipe);
        DBUeidInfo resp = ueidList.get(position);
        String content = "IMSI:"+resp.getImsi()+"\n";
        if (!TextUtils.isEmpty(resp.getMsisdn())){
            content += "手机号:"+resp.getMsisdn()+"\n";
        }
        content += mContext.getString(R.string.lab_rpt_time)+ DateUtils.convert2String(resp.getCreateDate(), DateUtils.LOCAL_DATE);
        text_data.setText(content);
        text_data.setTag(position);

        convertView.findViewById(R.id.add_to_black).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    BlackListInfo info = dbManager.selector(BlackListInfo.class).where("msisdn","=",resp.getMsisdn()).or("imsi", "=", resp.getImsi()).findFirst();
                    if (info != null) {

                        ModifyWhitelistDialog modifyWhitelistDialog = new ModifyWhitelistDialog(mContext,
                                resp.getImsi(), info.getMsisdn(), info.getRemark(),false);
                        modifyWhitelistDialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
                            @Override
                            public void onDismiss(DialogInterface dialog) {
                                notifyDataSetChanged();
                                if (swipeLayout !=null){
                                    swipeLayout.close();
                                }
                            }
                        });
                        modifyWhitelistDialog.show();
                    }else {

                        AddBlacklistDialog addBlacklistDialog = new AddBlacklistDialog(mContext, resp.getImsi(),resp.getMsisdn());
                        addBlacklistDialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
                            @Override
                            public void onDismiss(DialogInterface dialog) {
                                notifyDataSetChanged();
                                if (swipeLayout !=null){
                                    swipeLayout.close();
                                }
                            }
                        });
                        addBlacklistDialog.show();
                    }
                } catch (DbException e) {
                    e.printStackTrace();
                }
            }
        });


        if(CacheManager.getLocMode()){
            convertView.findViewById(R.id.add_to_localtion).setOnClickListener(new AddToLocationListener(mContext,resp.getImsi(),1));
        }else{
            convertView.findViewById(R.id.add_to_localtion).setVisibility(View.GONE);
        }

    }


    @Override
    public int getCount() {
        return ueidList.size();
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
