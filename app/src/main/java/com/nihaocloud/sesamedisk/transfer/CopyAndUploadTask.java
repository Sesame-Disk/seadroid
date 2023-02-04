package com.nihaocloud.sesamedisk.transfer;

import android.annotation.SuppressLint;
import android.content.Context;
import android.net.Uri;
import android.util.Log;
import android.widget.Toast;

import com.nihaocloud.sesamedisk.NihaoApplication;
import com.nihaocloud.sesamedisk.R;
import com.nihaocloud.sesamedisk.SeafException;
import com.nihaocloud.sesamedisk.SettingsManager;
import com.nihaocloud.sesamedisk.account.Account;
import com.nihaocloud.sesamedisk.data.DataManager;
import com.nihaocloud.sesamedisk.data.ProgressMonitor;
import com.nihaocloud.sesamedisk.ui.activity.BrowserActivity;
import com.nihaocloud.sesamedisk.util.Utils;

import org.apache.commons.io.IOUtils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.security.NoSuchAlgorithmException;

import okio.Okio;
import okio.Sink;
import okio.Source;

/**
 * Upload task
 */
public class CopyAndUploadTask extends TransferTask {
    public static final String DEBUG_TAG = "UploadTask";
    private final String dir;   // parent dir
    private final boolean isUpdate;  // true if update an existing file
    private final boolean isCopyToLocal; // false to turn off copy operation
    private final boolean byBlock;
    private UploadStateListener uploadStateListener;
    private DataManager dataManager;
    public static final int HTTP_ABOVE_QUOTA = 443;
    private final Uri uri;
    @SuppressLint("StaticFieldLeak")
    private final Context context;
    private final String fileName;

    public CopyAndUploadTask(Context context, int taskID, Account account, String repoID, String repoName,
                             String dir, String relativePath, Uri uri, String fileName, Long fileSize, boolean isUpdate, boolean isCopyToLocal, boolean byBlock,
                             UploadStateListener uploadStateListener) {

        super(taskID, account, repoName, repoID, relativePath, fileName);
        this.context = context.getApplicationContext();
        this.dir = dir;
        this.isUpdate = isUpdate;
        this.isCopyToLocal = isCopyToLocal;
        this.byBlock = byBlock;
        this.uploadStateListener = uploadStateListener;
        this.totalSize =fileSize;
        this.finished = 0;
        this.dataManager = new DataManager(account);
        this.uri = uri;
        this.fileName= fileName;
    }

    public UploadTaskInfo getTaskInfo() {
        return new UploadTaskInfo(account, taskID, state, repoID,
                repoName, dir, relativePath, path, isUpdate, isCopyToLocal,
                finished, totalSize, err);
    }

    public void cancelUpload() {
        if (state != TaskState.INIT && state != TaskState.TRANSFERRING) {
            return;
        }
        state = TaskState.CANCELLED;
        super.cancel(true);
    }

    @Override
    protected void onPreExecute() {
        state = TaskState.TRANSFERRING;
    }

    @Override
    protected void onProgressUpdate(Long... values) {
        long uploaded = values[0];
        this.finished = uploaded;
        uploadStateListener.onFileUploadProgress(taskID);
    }

    @Override
    protected File doInBackground(Void... params) {
        try {
            final ProgressMonitor monitor = new ProgressMonitor() {
                @Override
                public void onProgressNotify(long uploaded, boolean updateTotal) {
                    publishProgress(uploaded);
                }

                @Override
                public boolean isCancelled() {
                    return CopyAndUploadTask.this.isCancelled();
                }
            };

            if (byBlock) {
                dataManager.uploadByBlocks(repoName, repoID, dir, relativePath, path, monitor, isUpdate, isCopyToLocal);
            } else {
                dataManager.uploadFile(context, repoName, repoID, dir, relativePath, uri,fileName, monitor, isUpdate, isCopyToLocal);
            }

        } catch (SeafException e) {
            Log.e(DEBUG_TAG, "Upload exception " + e.getCode() + " " + e.getMessage());
            e.printStackTrace();
            err = e;
        } catch (NoSuchAlgorithmException | IOException e) {
            Log.e(DEBUG_TAG, "Upload exception " + e.getMessage());
            err = SeafException.unknownException;
            e.printStackTrace();
        }
        return null;
    }

    @Override
    protected void onPostExecute(File file) {
        state = err == null ? TaskState.FINISHED : TaskState.FAILED;
        if (uploadStateListener != null) {
            if (err == null) {
                SettingsManager.instance().saveUploadCompletedTime(Utils.getSyncCompletedTime());
                uploadStateListener.onFileUploaded(taskID);
            } else {
                if (err.getCode() == HTTP_ABOVE_QUOTA) {

                    Toast.makeText(NihaoApplication.getAppContext(), R.string.above_quota, Toast.LENGTH_SHORT).show();
                }
                uploadStateListener.onFileUploadFailed(taskID);
            }
        }
    }

    @Override
    protected void onCancelled() {
        if (uploadStateListener != null) {
            uploadStateListener.onFileUploadCancelled(taskID);
        }
    }

    public String getDir() {
        return dir;
    }

    public boolean isCopyToLocal() {
        return isCopyToLocal;
    }

    public boolean isUpdate() {
        return isUpdate;
    }

    private void copyFile() {
        InputStream in = null;
        OutputStream out = null;
        try {
            File tempDir = DataManager.createTempDir();
            File tempFile = new File(tempDir, Utils.getFilenamefromUri(context, uri));
            if (!tempFile.createNewFile()) {
                throw new RuntimeException("could not create temporary file");
            }
            in = context.getContentResolver().openInputStream(uri);

            Source source = Okio.source(in);

            out = new FileOutputStream(tempFile);
            Sink sink = Okio.sink(out);

            IOUtils.copy(in, out);

            setPath(tempFile.getAbsolutePath());
            if(getTotalSize()==-1) {
                setTotalSize(tempDir.length());
            }
        } catch (IOException e) {
            Log.d(DEBUG_TAG, "Could not open requested document", e);
        } catch (RuntimeException e) {
            Log.d(DEBUG_TAG, "Could not open requested document", e);
        } finally {
            IOUtils.closeQuietly(in);
            IOUtils.closeQuietly(out);
        }
    }


}