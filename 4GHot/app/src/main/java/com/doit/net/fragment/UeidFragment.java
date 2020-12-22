package com.doit.net.fragment;


import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.doit.net.adapter.UeidTabLayoutAdapter;
import com.doit.net.base.BaseFragment;
import com.doit.net.model.CacheManager;
import com.doit.net.protocol.LTESendManager;
import com.doit.net.protocol.Send2GManager;
import com.doit.net.ucsi.R;
import com.doit.net.utils.LogUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by Zxc on 2018/11/21.
 */

public class UeidFragment extends BaseFragment {

    private TabLayout tabLayout;
    private ViewPager viewPagers;
    private List<Fragment> listFragments;
    private List<String> listTitles;
    private RelativeLayout rlStart;
    private ImageView ivStart;

    public UeidFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {


        View rootView = inflater.inflate(R.layout.layout_ueid_all, container, false);
        tabLayout = rootView.findViewById(R.id.tabLayoutTab);
        viewPagers = rootView.findViewById(R.id.vpViewPaper);
        rlStart = rootView.findViewById(R.id.rl_start);
        ivStart = rootView.findViewById(R.id.iv_start);
        initView();
        return rootView;
    }

    private void initView() {

        listTitles = new ArrayList<>();
        listFragments = new ArrayList<>();

        listTitles.add("实时上报");
        listTitles.add("碰撞分析");
        listTitles.add("伴随分析");
        tabLayout.addTab(tabLayout.newTab().setText(listTitles.get(0)));
        tabLayout.addTab(tabLayout.newTab().setText(listTitles.get(1)));
        tabLayout.addTab(tabLayout.newTab().setText(listTitles.get(2)));

        listFragments.add(new RealTimeUeidRptFragment());
        listFragments.add(new CollideAnalysisFragment());
        listFragments.add(new GetPartnerAnalysisFragment());

        tabLayout.setTabMode(TabLayout.MODE_FIXED);


        viewPagers.setOffscreenPageLimit(listTitles.size());
        viewPagers.setAdapter(new UeidTabLayoutAdapter(getChildFragmentManager(), listFragments, listTitles));
        tabLayout.setupWithViewPager(viewPagers);

        //绑定自定义text，设置默认第一个标签放大
        TabLayout.Tab tab;
        for (int i = 0; i < tabLayout.getTabCount(); i++) {
            tab = tabLayout.getTabAt(i);
            if (tab != null) {
                tab.setCustomView(getTabView(i));
            }
        }
        tab = tabLayout.getTabAt(0);
        if (tab != null && tab.getCustomView() instanceof TextView) {
            ((TextView) tab.getCustomView()).setTextSize(22);
            ((TextView) tab.getCustomView()).setTextColor(getResources().getColor(R.color.black));
        }

        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {

                View view = tab.getCustomView();
                if (view instanceof TextView) {
                    ((TextView) view).setTextSize(22);
                    ((TextView) view).setTextColor(getResources().getColor(R.color.black));
                }
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {
                View view = tab.getCustomView();
                if (view instanceof TextView) {
                    ((TextView) view).setTextSize(15);
                    ((TextView) view).setTextColor(getResources().getColor(R.color.white));
                }
            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {
            }
        });

        ivStart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!CacheManager.checkDevice(getActivity())){
                    return;
                }
                rlStart.setVisibility(View.GONE);
                LTESendManager.openAllRf();
                Send2GManager.setRFState("1");

                new Timer().schedule(new TimerTask() {
                    @Override
                    public void run() {
                        CacheManager.redirect2G("",null,"redirect");
                    }
                }, 1000);
            }
        });
    }


    private View getTabView(int currentPosition) {
        View view = LayoutInflater.from(getContext()).inflate(R.layout.comman_text, null);
        TextView textView = view.findViewById(R.id.tvCommanText);
        textView.setText(listTitles.get(currentPosition));
        return view;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        LogUtils.log("侦码页面被销毁");
    }
}
