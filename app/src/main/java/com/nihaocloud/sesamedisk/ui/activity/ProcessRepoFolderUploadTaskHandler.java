package com.nihaocloud.sesamedisk.ui.activity;

import static com.nihaocloud.sesamedisk.ui.activity.BaseActivity.showShortToast;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.net.Uri;
import android.os.Handler;
import android.os.Looper;

import com.nihaocloud.sesamedisk.R;
import com.nihaocloud.sesamedisk.account.Account;
import com.nihaocloud.sesamedisk.data.CreateRepo;
import com.nihaocloud.sesamedisk.data.UploadFolder;
import com.nihaocloud.sesamedisk.notification.UploadNotificationProvider;
import com.nihaocloud.sesamedisk.transfer.PendingUploadInfo;
import com.nihaocloud.sesamedisk.transfer.TransferService;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

class ProcessRepoFolderUploadTaskHandler implements Runnable {
    private final String DEBUG_TAG = "ProcessRepoFolderUploadTaskHandler";
    private final WeakReference<TransferService> txServiceWeakReference;
    private final WeakReference<Activity> activityWeakReference;
    private final CreateRepo repo;
    private final Account account;
    List<UploadFolder> uploadCashFiles;
    private final ArrayList<PendingUploadInfo> pendingUploads;

    Handler handler = new Handler(Looper.getMainLooper());

    ProcessRepoFolderUploadTaskHandler(Activity activity,
                                       CreateRepo repo,
                                       TransferService txService,
                                       Account account,
                                       ArrayList<PendingUploadInfo> pendingUploads,
                                       List<UploadFolder> uploadCashFiles) {
        activityWeakReference = new WeakReference<>(activity);
        this.txServiceWeakReference = new WeakReference<>(txService);
        this.repo = repo;
        this.account = account;
        this.pendingUploads = pendingUploads;
        this.uploadCashFiles = uploadCashFiles;
    }

    @SuppressLint("LongLogTag")
    public void run() {
        Activity activity = activityWeakReference.get();
        handler.post(() -> showShortToast(activity, R.string.added_to_upload_tasks));
        int count = 0;
        for (final UploadFolder file : uploadCashFiles) {
            if (file == null) {
                handler.post(() -> showShortToast(activity, R.string.saf_upload_path_not_available));
            } else {
               int i= addUpload(repo, file);
                if (i != 0) {
                    count++;
                }
            }
        }
        if (count > 0) {
            activity.runOnUiThread(() -> showShortToast(activity, R.string.added_to_upload_tasks));
        }
    }

    private int addUpload(CreateRepo repo, UploadFolder file) {
        if (repo != null && repo.canLocalDecrypt()) {
            return addUploadBlocksTask(repo.getRepoId(), repo.getRepoName(), "/", file.getRelativePath(), file.getUri(), file.getFileName(), file.getFileSize());
        } else {
            return addUploadTask(repo.getRepoId(), repo.getRepoName(), "/", file.getRelativePath(), file.getUri(), file.getFileName(), file.getFileSize());
        }
    }

    private int addUploadBlocksTask(String repoID, String repoName, String targetDir, String relativePath, Uri uri, String fileName, Long fileSize) {
        TransferService txService = txServiceWeakReference.get();
        if (txService != null) {
            int i = txService.addTaskToUploadQueBlock(account, repoID, repoName, targetDir, relativePath, uri, fileName, fileSize, false, true);
            setNotification();
            return i;
        } else {
            PendingUploadInfo info = new PendingUploadInfo(repoID, repoName, targetDir, relativePath, uri, fileName, fileSize, false, true, true);
            pendingUploads.add(info);
            return 0;
        }
    }

    private int addUploadTask(String repoID, String repoName, String targetDir, String relativePath, Uri uri, String fileName, Long fileSize) {
        TransferService txService = txServiceWeakReference.get();
        if (txService != null) {
            int i = txService.addTaskToUploadQue(account, repoID, repoName, targetDir, relativePath, uri, fileName, fileSize, false, true);
            setNotification();
            return i;
        } else {
            PendingUploadInfo info = new PendingUploadInfo(repoID, repoName, targetDir, relativePath, uri,  fileName, fileSize, false, true);
            pendingUploads.add(info);
            return 0;
        }
    }

    private void setNotification() {
        TransferService txService = txServiceWeakReference.get();
        if (txService != null) {
            if (!txService.hasUploadNotifProvider()) {
                UploadNotificationProvider provider = new UploadNotificationProvider(
                        txService.getUploadTaskManager(),
                        txService);
                txService.saveUploadNotifProvider(provider);
            }
        }
    }
}
