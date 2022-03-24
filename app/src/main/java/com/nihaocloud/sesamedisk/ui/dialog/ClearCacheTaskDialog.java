package com.nihaocloud.sesamedisk.ui.dialog;

import android.app.Dialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;

import com.bumptech.glide.Glide;
import com.nihaocloud.sesamedisk.R;
import com.nihaocloud.sesamedisk.NihaoApplication;
import com.nihaocloud.sesamedisk.data.DatabaseHelper;
import com.nihaocloud.sesamedisk.data.StorageManager;

class ClearCacheTask extends TaskDialog.Task {

    @Override
    protected void runTask() {
        StorageManager storageManager = StorageManager.getInstance();
        storageManager.clearCache();

        // clear cached data from database
        DatabaseHelper dbHelper = DatabaseHelper.getDatabaseHelper();
        dbHelper.delCaches();

        //clear Glide cache
        Glide.get(NihaoApplication.getAppContext()).clearDiskCache();
    }
}

public class ClearCacheTaskDialog extends TaskDialog {
    @Override
    protected View createDialogContentView(LayoutInflater inflater, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.dialog_delete_cache, null);
        return view;
    }

    @Override
    protected void onDialogCreated(Dialog dialog) {
        dialog.setTitle(getString(R.string.settings_clear_cache_title));
    }

    @Override
    protected ClearCacheTask prepareTask() {
        ClearCacheTask task = new ClearCacheTask();
        return task;
    }
}
