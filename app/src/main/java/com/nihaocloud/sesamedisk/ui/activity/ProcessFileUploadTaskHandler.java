package com.nihaocloud.sesamedisk.ui.activity;

import static com.nihaocloud.sesamedisk.ui.activity.BaseActivity.showShortToast;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.net.Uri;
import android.util.Log;

import androidx.appcompat.app.AlertDialog;

import com.nihaocloud.sesamedisk.R;
import com.nihaocloud.sesamedisk.account.Account;
import com.nihaocloud.sesamedisk.data.DataManager;
import com.nihaocloud.sesamedisk.data.SeafDirent;
import com.nihaocloud.sesamedisk.data.SeafRepo;
import com.nihaocloud.sesamedisk.notification.UploadNotificationProvider;
import com.nihaocloud.sesamedisk.transfer.PendingUploadInfo;
import com.nihaocloud.sesamedisk.transfer.TransferService;
import com.nihaocloud.sesamedisk.ui.NavContext;
import com.nihaocloud.sesamedisk.util.Utils;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

class ProcessFileUploadTaskHandler implements Runnable {
    private final String DEBUG_TAG = "ProcessFileUploadTaskHandler";
    private final WeakReference<Activity> activityWeakReference;
    private final WeakReference<DataManager> dataManagerWeakReference;
    private final WeakReference<TransferService> txServiceWeakReference;
    private final NavContext navContext;
    private final Account account;
    private final List<Uri> uriList;
    private final ArrayList<PendingUploadInfo> pendingUploads;

    ProcessFileUploadTaskHandler(Activity activity, DataManager dataManager,
                                 NavContext navContext,
                                 TransferService txService,
                                 Account account,
                                 ArrayList<PendingUploadInfo> pendingUploads,
                                 List<Uri> uriList) {
        activityWeakReference = new WeakReference<>(activity);
        dataManagerWeakReference = new WeakReference<>(dataManager);
        this.txServiceWeakReference = new WeakReference<>(txService);
        this.navContext = navContext.copy();
        this.account = account;
        this.pendingUploads = pendingUploads;
        this.uriList = uriList;
    }

    @SuppressLint("LongLogTag")
    public void run() {
        DataManager dataManager = dataManagerWeakReference.get();
        Activity activity = activityWeakReference.get();
        if (dataManager != null && activity != null) {
            List<SeafDirent> list = dataManager.getCachedDirents(navContext.getRepoID(), navContext.getDirPath());
            int count = 0;
            for (final Uri uri : uriList) {
                if (uri != null) {
                    if (list == null) {
                        Log.e(DEBUG_TAG, "Seadroid dirent cache is empty in uploadFile. Should not happen, aborting.");
                        return;
                    }
                    String fileName = Utils.getFilenamefromUri(activity, uri);
                    long size = Utils.getFilSizeFromUri(activity, uri);
                    boolean duplicate = false;
                    for (SeafDirent dirent : list) {
                        if (dirent.name.equals(fileName)) {
                            duplicate = true;
                            break;
                        }
                    }
                    final SeafRepo repo = dataManager.getCachedRepoByID(navContext.getRepoID());
                    if (!duplicate) {
                        int i = addUpload(repo, uri, fileName, size);
                        if (i != 0) {
                            count++;
                        }
                    } else {
                        showFileExistDialog(repo, uri, fileName, size);
                    }
                } else {
                    activity.runOnUiThread(() -> showShortToast(activity, R.string.saf_upload_path_not_available));
                }
            }
            if (count > 0) {
                activity.runOnUiThread(() -> showShortToast(activity, R.string.added_to_upload_tasks));
            }
        }
    }

    private int addUpload(SeafRepo repo, Uri uri, String fileName, long fileSize) {
        if (repo != null && repo.canLocalDecrypt()) {
           return addUploadBlocksTask(repo.id, repo.name, navContext.getDirPath(), null, uri, fileName, fileSize);
        } else {
           return addUploadTask(navContext.getRepoID(), navContext.getRepoName(), navContext.getDirPath(), null, uri, fileName, fileSize);
        }
    }

    private void showFileExistDialog(final SeafRepo repo, Uri uri, String fileName, long fileSize) {
        Activity activity = activityWeakReference.get();
        if (activity != null) {
            activity.runOnUiThread(() -> {
                AlertDialog.Builder builder = new AlertDialog.Builder(activity);
                builder.setTitle(activity.getString(R.string.upload_file_exist));
                builder.setMessage(String.format(activity.getString(R.string.upload_duplicate_found), fileName));
                builder.setPositiveButton(R.string.upload_replace, (dialog, which) -> {
                    if (repo != null && repo.canLocalDecrypt()) {
                        addUpdateBlocksTask(repo.id, repo.name, navContext.getDirPath(), uri, fileName, fileSize);
                    } else {
                        addUpdateTask(navContext.getRepoID(), navContext.getRepoName(), navContext.getDirPath(), uri, fileName, fileSize);
                    }
                    showShortToast(activityWeakReference.get(), R.string.added_to_upload_tasks);
                });
                builder.setNeutralButton(R.string.cancel, (dialog, which) -> {
                });
                builder.setNegativeButton(R.string.upload_keep_both, (dialog, which) -> {
                    if (repo != null && repo.canLocalDecrypt()) {
                        addUploadBlocksTask(repo.id, repo.name, navContext.getDirPath(), null, uri, fileName, fileSize);
                    } else {
                        addUploadTask(navContext.getRepoID(), navContext.getRepoName(), navContext.getDirPath(), null, uri, fileName, fileSize);
                    }
                    showShortToast(activityWeakReference.get(), R.string.added_to_upload_tasks);
                });
                builder.show();
            });
        }
    }

    private int addUploadBlocksTask(String repoID, String repoName, String targetDir, String relativePath, Uri uri, String fileName, long fileSize) {
        TransferService txService = txServiceWeakReference.get();
        if (txService != null) {
            int i = txService.addTaskToUploadQueBlock(account, repoID, repoName, targetDir, relativePath, uri, fileName, fileSize, false, true);
            setNotification();
            return i;
        } else {
            PendingUploadInfo info = new PendingUploadInfo(repoID, repoName, targetDir, relativePath, uri, fileName, fileSize, false, true);
            pendingUploads.add(info);
            return 0;
        }
    }

    private int addUploadTask(String repoID, String repoName, String targetDir, String relativePath, Uri uri, String fileName, long fileSize) {
        TransferService txService = txServiceWeakReference.get();
        if (txService != null) {
            int i = txService.addTaskToUploadQue(account, repoID, repoName, targetDir, relativePath, uri, fileName, fileSize, false, true);
            setNotification();
            return i;
        } else {
            PendingUploadInfo info = new PendingUploadInfo(repoID, repoName, targetDir,relativePath, uri, fileName, fileSize,  false, true);
            pendingUploads.add(info);
            return 0;
        }
    }

    public void addUpdateTask(String repoID, String repoName, String targetDir, Uri uri, String fileName, long fileSize) {
        TransferService txService = txServiceWeakReference.get();
        if (txService != null) {
            txService.addTaskToUploadQue(account, repoID, repoName, targetDir, null, uri, fileName, fileSize, true, true);
            setNotification();
        } else {
            PendingUploadInfo info = new PendingUploadInfo(repoID, repoName, targetDir, null, uri, fileName, fileSize,  true, true);
            pendingUploads.add(info);
        }
    }

    public void addUpdateBlocksTask(String repoID, String repoName, String targetDir, Uri uri, String fileName, long fileSize) {
        TransferService txService = txServiceWeakReference.get();
        if (txService != null) {
            txService.addTaskToUploadQueBlock(account, repoID, repoName, targetDir, null, uri, fileName, fileSize, true, true);
            setNotification();
        } else {
            PendingUploadInfo info = new PendingUploadInfo(repoID, repoName, targetDir, null, uri, fileName, fileSize, true, true, true);
            pendingUploads.add(info);
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
