package com.nihaocloud.sesamedisk.ui.activity;

import static com.nihaocloud.sesamedisk.ui.activity.BaseActivity.showShortToast;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.net.Uri;

import androidx.appcompat.app.AlertDialog;
import androidx.documentfile.provider.DocumentFile;

import com.nihaocloud.sesamedisk.R;
import com.nihaocloud.sesamedisk.account.Account;
import com.nihaocloud.sesamedisk.data.DataManager;
import com.nihaocloud.sesamedisk.data.SeafDirent;
import com.nihaocloud.sesamedisk.data.SeafRepo;
import com.nihaocloud.sesamedisk.data.UploadFolder;
import com.nihaocloud.sesamedisk.notification.UploadNotificationProvider;
import com.nihaocloud.sesamedisk.transfer.PendingUploadInfo;
import com.nihaocloud.sesamedisk.transfer.TransferService;
import com.nihaocloud.sesamedisk.ui.NavContext;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

class ProcessFolderUploadTaskHandler implements Runnable {
    private final String DEBUG_TAG = "ProcessFolderUploadTaskHandler";
    private final WeakReference<Activity> activityWeakReference;
    private final WeakReference<DataManager> dataManagerWeakReference;
    private final WeakReference<TransferService> txServiceWeakReference;
    private final NavContext navContext;
    private final Account account;
    private final Uri mainUri;
    private final ArrayList<PendingUploadInfo> pendingUploads;

    ProcessFolderUploadTaskHandler(Activity activity, DataManager dataManager,
                                   NavContext navContext,
                                   TransferService txService,
                                   Account account,
                                   ArrayList<PendingUploadInfo> pendingUploads,
                                   Uri uri) {
        activityWeakReference = new WeakReference<>(activity);
        dataManagerWeakReference = new WeakReference<>(dataManager);
        this.txServiceWeakReference = new WeakReference<>(txService);
        this.navContext = navContext.copy();
        this.account = account;
        this.pendingUploads = pendingUploads;
        this.mainUri = uri;
    }

    @SuppressLint("LongLogTag")
    public void run() {
        DataManager dataManager = dataManagerWeakReference.get();
        Activity activity = activityWeakReference.get();
        if (dataManager != null && activity != null) {
            final DocumentFile documentsTree = DocumentFile.fromTreeUri(activity, mainUri);
            if (!documentsTree.isDirectory()) {
                return;
            }
            List<SeafDirent> list = dataManager.getCachedDirents(navContext.getRepoID(), navContext.getDirPath());
            final String dirName = documentsTree.getName();
            for (SeafDirent seafDirent : list) {
                if (seafDirent.isDir() && seafDirent.name != null && seafDirent.name.equals(dirName)) {
                    showFolderExistDialog(dirName);
                    return;
                }
            }

            List<UploadFolder> files = UploadFolder.getUploadCashFiles(activity, documentsTree);
            if (files == null) return;
            if (files.size() == 0) {
                showFolderEmptyDialog(dirName);
                return;
            }
            int count = 0;
            for (final UploadFolder file : files) {
                if (file == null) {
                    activity.runOnUiThread(() -> showShortToast(activity, R.string.saf_upload_path_not_available));
                } else {
                    final SeafRepo repo = dataManager.getCachedRepoByID(navContext.getRepoID());
                    int i = addUpload(repo, file);
                    if (i != 0) {
                        count++;
                    }
                }
            }

            if (count > 0) {
                activity.runOnUiThread(() -> showShortToast(activity, R.string.added_to_upload_tasks));
            }
        }
    }

    private int addUpload(SeafRepo repo, UploadFolder file) {
        if (repo != null && repo.canLocalDecrypt()) {
            return addUploadBlocksTask(repo.id, repo.name, navContext.getDirPath(), file.getRelativePath(), file.getUri(), file.getFileName(), file.getFileSize());
        } else {
            return addUploadTask(navContext.getRepoID(), navContext.getRepoName(), navContext.getDirPath(), file.getRelativePath(), file.getUri(), file.getFileName(), file.getFileSize());
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
            PendingUploadInfo info = new PendingUploadInfo(repoID, repoName, targetDir, relativePath, uri,   fileName, fileSize, false, true);
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

    private void showFolderExistDialog(final String name) {
        Activity activity = activityWeakReference.get();
        if (activity != null) {
            activity.runOnUiThread(() -> {
                AlertDialog.Builder builder = new AlertDialog.Builder(activity);
                //builder.setTitle(getString(R.string.unknow_error));
                builder.setMessage(R.string.upload_folder_exist);
                builder.setPositiveButton(R.string.ok, (dialog, which) -> {
                });
                builder.show();
            });
        }
    }

    private void showFolderEmptyDialog(final String name) {
        Activity activity = activityWeakReference.get();
        if (activity != null) {
            activity.runOnUiThread(() -> {
                AlertDialog.Builder builder = new AlertDialog.Builder(activity);
                //builder.setTitle(getString(R.string.unknow_error));
                builder.setMessage(R.string.empty_folder);
                builder.setPositiveButton(R.string.ok, (dialog, which) -> {
                });
                builder.show();
            });
        }
    }
}
