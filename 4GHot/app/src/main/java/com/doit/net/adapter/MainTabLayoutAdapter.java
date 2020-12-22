package com.doit.net.adapter;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.app.FragmentStatePagerAdapter;

import com.doit.net.base.BaseFragment;

import java.util.List;

/**
 * Author：Libin on 2020/6/2 14:32
 * Email：1993911441@qq.com
 * Describe：
 */
public class MainTabLayoutAdapter extends FragmentPagerAdapter {
    private List<BaseFragment> mList;
    private List<String> mTitles;

    public MainTabLayoutAdapter(FragmentManager fm, List<BaseFragment> list, List<String> titles) {
        super(fm);
        this.mList = list;
        this.mTitles = titles;
    }

    @Override
    public Fragment getItem(int position) {
        return mList.get(position);
    }

    @Override
    public int getCount() {
        return mList.size();
    }

    @Override
    public CharSequence getPageTitle(int position) {
        return mTitles == null ? super.getPageTitle(position) : mTitles.get(position);
    }

}
