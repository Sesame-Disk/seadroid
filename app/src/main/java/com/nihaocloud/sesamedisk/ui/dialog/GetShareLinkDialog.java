package com.nihaocloud.sesamedisk.ui.dialog;

import android.app.Dialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;

import com.nihaocloud.sesamedisk.R;
import com.nihaocloud.sesamedisk.SeafConnection;
import com.nihaocloud.sesamedisk.SeafException;
import com.nihaocloud.sesamedisk.account.Account;
import com.nihaocloud.sesamedisk.data.DataManager;
import com.nihaocloud.sesamedisk.data.SeafLink;

import java.util.ArrayList;

class GetShareLinkTask extends TaskDialog.Task {
    String repoID;
    String path;
    boolean isdir;
    SeafConnection conn;
    String link;
    Account account;
    String password;
    String days;

    public GetShareLinkTask(String repoID, String path, boolean isdir, SeafConnection conn, Account account, String password, String days) {
        this.repoID = repoID;
        this.path = path;
        this.isdir = isdir;
        this.conn = conn;
        this.account = account;
        this.password = password;
        this.days = days;
    }

    @Override
    protected void runTask() {

        // If you has  Shared links to delete Shared links
        DataManager dataManager = new DataManager(account);
        ArrayList<SeafLink> shareLinks = dataManager.getShareLink(repoID, path);
        for (SeafLink shareLink : shareLinks) {
            //delete link
            dataManager.deleteShareLink(shareLink.getToken());
        }
        //create new link
        try {
            link = conn.getShareLink(repoID, path, password, days);
        } catch (SeafException e) {
            setTaskException(e);
        }
    }

    public String getResult() {
        return link;
    }
}

public class GetShareLinkDialog extends TaskDialog {
    private String repoID;
    private String path;
    private boolean isdir;
    private SeafConnection conn;
    Account account;
    private String password;
    private String days;

    public void init(String repoID, String path, boolean isdir, Account account, String password, String days) {
        this.repoID = repoID;
        this.path = path;
        this.isdir = isdir;
        this.conn = new SeafConnection(account);
        this.account = account;
        this.password = password;
        this.days = days;
    }

    @Override
    protected View createDialogContentView(LayoutInflater inflater, Bundle savedInstanceState) {
        return null;
    }

    @Override
    protected boolean executeTaskImmediately() {
        return true;
    }

    @Override
    protected void onDialogCreated(Dialog dialog) {
        dialog.setTitle(getActivity().getString(R.string.generating_link));
        // dialog.setTitle(getActivity().getString(R.string.generating_link));
    }

    @Override
    protected GetShareLinkTask prepareTask() {
        GetShareLinkTask task = new GetShareLinkTask(repoID, path, isdir, conn, account, password, days);
        return task;
    }

    public String getLink() {
        if (getTask() != null) {
            GetShareLinkTask task = (GetShareLinkTask)getTask();
            return task.getResult();
        }

        return null;
    }
}