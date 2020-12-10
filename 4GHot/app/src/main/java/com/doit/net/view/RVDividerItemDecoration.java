package com.doit.net.view;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.view.View;

import com.doit.net.ucsi.R;

/**
 * Author：Libin on 2020/12/10 10:04
 * Email：1993911441@qq.com
 * Describe：
 */
public class RVDividerItemDecoration extends RecyclerView.ItemDecoration {

    private Drawable mDivider;
    private int padding = 0;
    private int headerCount = 0;
    private boolean isVertical = false;


    public RVDividerItemDecoration(Context context, int padding) {
        this.mDivider = ContextCompat.getDrawable(context, R.drawable.rv_divider_white_horizontal);
        this.padding = padding;
    }
    public RVDividerItemDecoration(Context context, int padding,int dividerColor) {
        this.mDivider = ContextCompat.getDrawable(context, dividerColor);
        this.padding = padding;
    }

    public RVDividerItemDecoration(Context context, boolean isVertical,int dividerColor) {
        this.mDivider = ContextCompat.getDrawable(context, dividerColor);
        this.isVertical = isVertical;
    }


    public RVDividerItemDecoration(Context context) {
        this.mDivider = ContextCompat.getDrawable(context, R.drawable.rv_divider_white_horizontal);
    }


    @Override
    public void onDraw(@NonNull Canvas c, @NonNull RecyclerView parent, @NonNull RecyclerView.State state) {
        super.onDraw(c, parent, state);
        int childCount = parent.getChildCount();
        if (isVertical){
            int top = parent.getPaddingTop() + padding;
            int bottom = parent.getHeight() - parent.getPaddingBottom()- padding;
            for (int i = headerCount; i < childCount - 1; ++i) {
                View child = parent.getChildAt(i);
                RecyclerView.LayoutParams params = (RecyclerView.LayoutParams) child.getLayoutParams();
                int left = child.getRight() + params.rightMargin;
                int right = left + this.mDivider.getIntrinsicWidth();
                this.mDivider.setBounds(left, top, right, bottom);
                this.mDivider.draw(c);
            }

        }else {
            int left = parent.getPaddingLeft() + padding;
            int right = parent.getWidth() - parent.getPaddingRight() - padding;
            for (int i = headerCount; i < childCount - 1; ++i) {
                View child = parent.getChildAt(i);
                RecyclerView.LayoutParams params = (RecyclerView.LayoutParams) child.getLayoutParams();
                int top = child.getBottom() + params.bottomMargin;
                int bottom = top + this.mDivider.getIntrinsicHeight();
                this.mDivider.setBounds(left, top, right, bottom);
                this.mDivider.draw(c);
            }
        }

    }
}
