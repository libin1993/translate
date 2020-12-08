package com.doit.net.adapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.DialogInterface;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.daimajia.swipe.SwipeLayout;
import com.daimajia.swipe.adapters.BaseSwipeAdapter;
import com.doit.net.event.AddToLocationListener;
import com.doit.net.event.EventAdapter;
import com.doit.net.model.BlackBoxManger;
import com.doit.net.model.BlackListInfo;
import com.doit.net.model.CacheManager;
import com.doit.net.model.UCSIDBManager;
import com.doit.net.protocol.Send2GManager;
import com.doit.net.view.ModifyBlackListDialog;
import com.doit.net.ucsi.R;

import org.xutils.ex.DbException;

import java.util.ArrayList;
import java.util.List;

import cn.pedant.SweetAlert.SweetAlertDialog;

/**
 * Created by Zxc on 2019/5/30.
 */

public class BlacklistAdapter extends BaseSwipeAdapter {
    private Context mContext;

    private static List<BlackListInfo> listBlacklistInfo = new ArrayList<>();
    private HistoryListViewAdapter.onItemLongClickListener mOnItemLongClickListener;
    private MotionEvent motionEvent;


    public BlacklistAdapter(Context mContext) {
        this.mContext = mContext;
    }

    public void setUserInfoList(List<BlackListInfo> listWhitelist) {
        listBlacklistInfo = listWhitelist;
    }

    @Override
    public int getSwipeLayoutResourceId(int position) {
        return R.id.layout_user_info;
    }

    @Override
    public View generateView(int position, ViewGroup parent) {
        View v = LayoutInflater.from(mContext).inflate(R.layout.layout_blacklist_item,null);
        return v;
    }

    @Override
    public void fillValues(int position, View convertView) {
        TextView tvIndex = convertView.findViewById(R.id.tvIndex);
        TextView tvWhitelistInfo = convertView.findViewById(R.id.tvWhitelistInfo);
        SwipeLayout swipeLayout = convertView.findViewById(R.id.layout_user_info);

        final BlackListInfo whitelistInfo = listBlacklistInfo.get(position);
        tvIndex.setText(" " +(position + 1) + ".");
            tvWhitelistInfo.setText("IMSI："+ whitelistInfo.getImsi()  + "\n手机号："+ whitelistInfo.getMsisdn()  +
                        "\n备注：" + whitelistInfo.getRemark());
        tvWhitelistInfo.setTag(position);

        convertView.findViewById(R.id.ivDelete).setOnClickListener(new BlacklistAdapter.DeleteWhitelistListener(position));
        convertView.findViewById(R.id.ivModify).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ModifyBlackListDialog modifyUserInfoDialog = new ModifyBlackListDialog(mContext, whitelistInfo.getImsi(), whitelistInfo.getMsisdn(), whitelistInfo.getRemark());
                modifyUserInfoDialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
                    @Override
                    public void onDismiss(DialogInterface dialog) {
                        EventAdapter.call(EventAdapter.REFRESH_BLACKLIST);
                        if (swipeLayout !=null){
                            swipeLayout.close();
                        }
                    }
                });
                modifyUserInfoDialog.show();
            }
        });


        ImageView ivLocation = convertView.findViewById(R.id.iv_location);
        if(CacheManager.getLocMode() && !TextUtils.isEmpty(whitelistInfo.getImsi())){
            ivLocation.setVisibility(View.VISIBLE);
            ivLocation.setOnClickListener(new AddToLocationListener(mContext,whitelistInfo.getImsi(),1,swipeLayout));
        }else{
            ivLocation.setVisibility(View.GONE);
        }


        if (mOnItemLongClickListener != null) {
            //获取触摸点的坐标，以决定pop从哪里弹出
            convertView.setOnTouchListener(new View.OnTouchListener() {
                @SuppressLint("ClickableViewAccessibility")
                @Override
                public boolean onTouch(View v, MotionEvent event) {
                    switch (event.getAction()) {
                        case MotionEvent.ACTION_DOWN:
                            motionEvent = event;
                            break;
                        default:
                            break;
                    }
                    // 如果onTouch返回false,首先是onTouch事件的down事件发生，此时，如果长按，触发onLongClick事件；
                    // 然后是onTouch事件的up事件发生，up完毕，最后触发onClick事件。
                    return false;
                }
            });


            final int pos = position;
            convertView.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    //int position = holder.getLayoutPosition();
                    mOnItemLongClickListener.onItemLongClick(motionEvent, pos);
                    //返回true 表示消耗了事件 事件不会继续传递
                    return true; //长按了就禁止swipe弹出
                }
            });
        }
    }

    public  List<BlackListInfo> getWhitelistList(){
        return listBlacklistInfo;
    }

    public void setOnItemLongClickListener(HistoryListViewAdapter.onItemLongClickListener mOnItemLongClickListener) {
        this.mOnItemLongClickListener = mOnItemLongClickListener;
    }

    @Override
    public int getCount() {
        return listBlacklistInfo.size();
    }

    @Override
    public Object getItem(int position) {
        return null;
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    public void updateView(){
        notifyDataSetChanged();
    }

    class DeleteWhitelistListener implements View.OnClickListener{
        private int position;

        public DeleteWhitelistListener(int position) {
            this.position = position;
        }

        @Override
        public void onClick(View v) {
            BlackListInfo resp = listBlacklistInfo.get(position);
            try {
                UCSIDBManager.getDbManager().delete(resp);


                EventAdapter.call(EventAdapter.REFRESH_BLACKLIST);

                Send2GManager.setBlackList();

                EventAdapter.call(EventAdapter.ADD_BLACKBOX, BlackBoxManger.DELTE_BLACK_LIST
                        + resp.getImsi() + "+" + resp.getMsisdn());
            } catch (DbException e) {
                new SweetAlertDialog(mContext, SweetAlertDialog.ERROR_TYPE)
                        .setTitleText(mContext.getString(R.string.del_white_list_fail))
                        .show();
            }
        }
    }
}
