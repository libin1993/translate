package com.doit.net.adapter;

import android.content.Context;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.doit.net.utils.FormatUtils;
import com.doit.net.bean.LteChannelCfg;
import com.doit.net.event.EventAdapter;
import com.doit.net.protocol.LTESendManager;
import com.doit.net.model.CacheManager;
import com.doit.net.utils.ToastUtils;
import com.doit.net.ucsi.R;

public class ChannelListViewAdapter extends BaseAdapter {

    private Context mContext;

    public ChannelListViewAdapter(Context mContext) {
        this.mContext = mContext;
    }

    public void refreshData(){
        notifyDataSetChanged();
    }

    static class ViewHolder{
        TextView title;

        EditText fcn;
        EditText plmn;
        EditText pa;
        EditText ga;
        EditText rlm;
        EditText etAltFcn;
        Button saveBtn;

    }
    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        final ViewHolder holder;
        if(convertView == null){
            holder = new ViewHolder();
            convertView = LayoutInflater.from(mContext).inflate(R.layout.doit_layout_channel_item, null);
            holder.title = convertView.findViewById(R.id.title_text);

            holder.fcn = convertView.findViewById(R.id.editText_fcn);
            holder.plmn = convertView.findViewById(R.id.editText_plmn);
            holder.pa = convertView.findViewById(R.id.editText_pa);
            holder.ga = convertView.findViewById(R.id.editText_ga);
            holder.rlm = convertView.findViewById(R.id.etRLM);
            holder.etAltFcn = convertView.findViewById(R.id.etAltFcn);
            holder.saveBtn = convertView.findViewById(R.id.button_save);

            convertView.setTag(holder);
        }else{
            holder = (ViewHolder)convertView.getTag();
        }
        fillValues(position,holder);
        return convertView;
    }


    public void fillValues(int position, final ViewHolder holder) {
        LteChannelCfg cfg = CacheManager.channels.get(position);
        if(cfg == null){
            return;
        }
        holder.title.setText("通道："+cfg.getIdx()+"    "+ "频段:" + cfg.getBand());

        holder.fcn.setText(cfg.getFcn()==null?"":""+cfg.getFcn());
        holder.plmn.setText(cfg.getPlmn());
        holder.ga.setText(cfg.getGa()==null?"":""+cfg.getGa());
        holder.pa.setText(cfg.getPa()==null?"":""+cfg.getPa());
        holder.rlm.setText(cfg.getRlm()==null?"":""+cfg.getRlm());
        holder.etAltFcn.setText(cfg.getAltFcn()==null?"":""+cfg.getAltFcn());


        holder.saveBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String fcn = holder.fcn.getText().toString().trim();
                String plmn = holder.plmn.getText().toString().trim();
                String pa = holder.pa.getText().toString().trim();
                String ga = holder.ga.getText().toString().trim();
                String rlm = holder.rlm.getText().toString().trim();
                String alt_fcn = holder.etAltFcn.getText().toString().trim();

                //不为空校验正则，为空不上传
                if (!TextUtils.isEmpty(fcn)){
                    if (!FormatUtils.getInstance().matchFCN(fcn)){
                        ToastUtils.showMessage("FCN格式输入有误,请检查");
                        return;
                    }else {
                        if (!FormatUtils.getInstance().fcnRange(cfg.getBand(), fcn)){
                            ToastUtils.showMessage("FCN格式输入范围有误,请检查");
                            return;
                        }

                    }
                }


                ToastUtils.showMessage(R.string.tip_15);
                EventAdapter.call(EventAdapter.SHOW_PROGRESS);

                LTESendManager.setChannelConfig(cfg.getIdx(), fcn, plmn, pa, ga,rlm,"",alt_fcn);
            }
        });
    }


    @Override
    public int getCount() {
        return CacheManager.channels.size();
    }

    @Override
    public Object getItem(int position) {
        return CacheManager.channels.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }


}
