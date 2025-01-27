package com.nihaocloud.sesamedisk.ui.dialog;

import android.app.Dialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;

import com.nihaocloud.sesamedisk.R;
import com.nihaocloud.sesamedisk.data.DataManager;
import com.nihaocloud.sesamedisk.data.DatabaseHelper;

public class ClearPasswordTaskDialog extends TaskDialog {

    @Override
    protected View createDialogContentView(LayoutInflater inflater, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.dialog_delete_password, null);
        return view;
    }

    @Override
    protected void onDialogCreated(Dialog dialog) {
        dialog.setTitle(getString(R.string.clear_password_title));
    }

    @Override
    protected ClearPasswordTask prepareTask() {
        ClearPasswordTask task = new ClearPasswordTask();
        return task;
    }
}

class ClearPasswordTask extends TaskDialog.Task {

    @Override
    protected void runTask() {
        DataManager.clearPassword();

        // clear cached data from database
        DatabaseHelper dbHelper = DatabaseHelper.getDatabaseHelper();
        dbHelper.clearEnckeys();
    }
}