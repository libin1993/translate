package com.doit.net.adapter;

import android.content.Context;
import android.content.DialogInterface;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import com.daimajia.swipe.SwipeLayout;
import com.daimajia.swipe.adapters.RecyclerSwipeAdapter;
import com.daimajia.swipe.util.Attributes;
import com.doit.net.application.MyApplication;
import com.doit.net.bean.UeidBean;
import com.doit.net.event.AddToLocationListener;
import com.doit.net.model.CacheManager;
import com.doit.net.ucsi.R;
import com.doit.net.utils.LogUtils;
import com.doit.net.utils.UtilOperator;
import com.doit.net.view.AddBlacklistDialog;
import com.doit.net.view.ModifyBlackListDialog;

import java.util.List;

/**
 * Author：Libin on 2020/12/10 13:34
 * Email：1993911441@qq.com
 * Describe：
 */
public class UeidAdapter extends RecyclerSwipeAdapter<UeidAdapter.SimpleViewHolder> {
    private Context mContext;
    private List<UeidBean> dataList;
    private OnClickListener onClickListener;


    public UeidAdapter(Context context, List<UeidBean> dataList) {
        this.mContext = context;
        this.dataList = dataList;
    }

    public void setOnClickListener(OnClickListener onClickListener){
        this.onClickListener = onClickListener;
    }

    @Override
    public SimpleViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.doit_layout_ueid_list_item, parent, false);
        return new SimpleViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final SimpleViewHolder viewHolder, final int position) {
        UeidBean ueidBean = dataList.get(position);
        if (position % 2 == 0) {
            viewHolder.llContent.setBackgroundColor(mContext.getResources().getColor(R.color.deepgrey2));
        } else {
            viewHolder.llContent.setBackgroundColor(mContext.getResources().getColor(R.color.black));
        }



        viewHolder.tvPosition.setText((position + 1) + ".");

        String type = "";
        if (ueidBean.getType() == 0) {
            type += "2G";
        } else {
            type += "4G";
        }
        String content = "IMSI：" + ueidBean.getImsi() + "          " + "制式: "
                + UtilOperator.getOperatorNameCH(ueidBean.getImsi()) + type + "\n";


        if (!"".equals(ueidBean.getImsi())) {

            content += ueidBean.getRptTime() + "       " + "次数：" + ueidBean.getRptTimes() + "       "
                    + mContext.getString(R.string.ueid_last_intensity) + ueidBean.getSrsp();

            if (ueidBean.isBlack()) {
                String msisdn = ueidBean.getNumber();
                String remark = ueidBean.getRemark();
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

                viewHolder.tvContent.setTextColor(MyApplication.mContext.getResources().getColor(R.color.forestgreen));

            } else {

                String msisdn = ueidBean.getNumber();
                if (!TextUtils.isEmpty(msisdn)) {
                    content += "\n" + "手机号：" + msisdn;
                }

                viewHolder.tvContent.setTextColor(MyApplication.mContext.getResources().getColor(R.color.white));

            }

            viewHolder.tvContent.setText(content);
        }

        if (CacheManager.getLocMode()) {
            viewHolder.ivLocation.setOnClickListener(new AddToLocationListener(mContext, ueidBean.getImsi(),
                    ueidBean.getType(), viewHolder.swipeLayout));
        } else {
            viewHolder.ivLocation.setVisibility(View.GONE);
        }

        viewHolder.ivBlack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (ueidBean.isBlack()){
                    ModifyBlackListDialog modifyBlackListDialog = new ModifyBlackListDialog(mContext,
                            ueidBean.getImsi(), ueidBean.getNumber(), ueidBean.getRemark(),false);
                    modifyBlackListDialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
                        @Override
                        public void onDismiss(DialogInterface dialog) {
                            if (viewHolder.swipeLayout !=null){
                                viewHolder.swipeLayout.close();
                            }
                        }
                    });
                    modifyBlackListDialog.show();
                }else {
                    AddBlacklistDialog addBlacklistDialog = new AddBlacklistDialog(mContext, ueidBean.getImsi(),ueidBean.getNumber());
                    addBlacklistDialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
                        @Override
                        public void onDismiss(DialogInterface dialog) {
                            if (viewHolder.swipeLayout !=null){
                                viewHolder.swipeLayout.close();
                            }
                        }
                    });
                    addBlacklistDialog.show();
                }
            }
        });

        mItemManger.bindView(viewHolder.itemView, position);

    }

    @Override
    public int getItemCount() {
        return dataList.size();
    }

    @Override
    public int getSwipeLayoutResourceId(int position) {
        return R.id.swipe;
    }


    public  class SimpleViewHolder extends RecyclerView.ViewHolder {
        SwipeLayout swipeLayout;
        ImageView ivLocation;
        ImageView ivBlack;
        TextView tvPosition;
        TextView tvContent;
        LinearLayout llContent;

        public SimpleViewHolder(View itemView) {
            super(itemView);
            swipeLayout = itemView.findViewById(R.id.swipe);
            llContent = itemView.findViewById(R.id.layoutItemText);
            tvPosition = itemView.findViewById(R.id.position);
            tvContent = itemView.findViewById(R.id.tvUeidItemText);
            ivLocation = itemView.findViewById(R.id.add_to_localtion);
            ivBlack = itemView.findViewById(R.id.add_to_black);

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (onClickListener !=null){
                        onClickListener.onClick(getAdapterPosition());
                    }
                }
            });
        }
    }


    public interface OnClickListener{
        void onClick(int position);
    }
}
