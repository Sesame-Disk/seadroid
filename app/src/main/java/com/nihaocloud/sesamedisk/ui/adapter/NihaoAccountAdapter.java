package com.nihaocloud.sesamedisk.ui.adapter;

import android.content.Context;

import com.nihaocloud.sesamedisk.R;

/**
 * Adapter for showing account in a list view.
 */
public class NihaoAccountAdapter extends AccountAdapter {

    public NihaoAccountAdapter(Context context) {
        super(context);
    }

    @Override
    protected int getChildLayout() {
        return R.layout.list_item_account_entry;
    }

    @Override
    protected int getChildTitleId() {
        return R.id.list_item_account_title;
    }

    @Override
    protected int getChildSubTitleId() {
        return R.id.list_item_account_subtitle;
    }

    @Override
    protected int getChildIconId() {
        return R.id.list_item_account_icon;
    }
}
