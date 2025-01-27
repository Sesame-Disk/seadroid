package com.nihaocloud.sesamedisk.notification;

import android.app.PendingIntent;
import android.content.Intent;

import com.nihaocloud.sesamedisk.NihaoApplication;
import com.nihaocloud.sesamedisk.R;
import com.nihaocloud.sesamedisk.transfer.TaskState;
import com.nihaocloud.sesamedisk.transfer.TransferService;
import com.nihaocloud.sesamedisk.transfer.UploadTaskInfo;
import com.nihaocloud.sesamedisk.transfer.UploadTaskManager;
import com.nihaocloud.sesamedisk.ui.CustomNotificationBuilder;
import com.nihaocloud.sesamedisk.ui.activity.TransferActivity;

import java.util.List;

/**
 * Upload notification provider
 */
public class UploadNotificationProvider extends BaseNotificationProvider {

    public UploadNotificationProvider(UploadTaskManager uploadTaskManager,
                                      TransferService transferService) {
        super(uploadTaskManager, transferService);

    }

    @Override
    protected String getProgressInfo() {
        String progressStatus = "";

        if (txService == null)
            return progressStatus;

        // failed or cancelled tasks won`t be shown in notification state
        // but failed or cancelled detailed info can be viewed in TransferList
        if (getState().equals(NotificationState.NOTIFICATION_STATE_COMPLETED_WITH_ERRORS))
            progressStatus = NihaoApplication.getAppContext().getString(R.string.notification_upload_completed);
        else if (getState().equals(NotificationState.NOTIFICATION_STATE_COMPLETED))
            progressStatus = NihaoApplication.getAppContext().getString(R.string.notification_upload_completed);
        else if (getState().equals(NotificationState.NOTIFICATION_STATE_PROGRESS)) {
            int uploadingCount = 0;
            List<UploadTaskInfo> infos = txService.getNoneCameraUploadTaskInfos();
            for (UploadTaskInfo info : infos) {
                if (info.state.equals(TaskState.INIT)
                        || info.state.equals(TaskState.TRANSFERRING))
                    uploadingCount++;
            }

            if (uploadingCount != 0)
                progressStatus = NihaoApplication.getAppContext().getResources().
                        getQuantityString(R.plurals.notification_upload_info,
                                uploadingCount,
                                uploadingCount,
                                getProgress());
        }
        return progressStatus;
    }

    @Override
    protected void notifyStarted() {
        Intent dIntent = new Intent(NihaoApplication.getAppContext(), TransferActivity.class);
        dIntent.putExtra(NOTIFICATION_MESSAGE_KEY, NOTIFICATION_OPEN_UPLOAD_TAB);
        dIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);

        PendingIntent uPendingIntent = PendingIntent.getActivity(NihaoApplication.getAppContext(),
                (int) System.currentTimeMillis(),
                dIntent,
                0);
        mNotifBuilder = CustomNotificationBuilder.getNotificationBuilder(NihaoApplication.getAppContext(),
                CustomNotificationBuilder.CHANNEL_ID_UPLOAD)
                .setSmallIcon(R.drawable.ic_nihao_notification)
                .setOnlyAlertOnce(true)
                .setContentTitle(NihaoApplication.getAppContext().getString(R.string.notification_upload_started_title))
                .setOngoing(true)
                .setContentText(NihaoApplication.getAppContext().getString(R.string.notification_upload_started_title))
                .setContentIntent(uPendingIntent)
                .setProgress(100, 0, false);

        // Make this service run in the foreground, supplying the ongoing
        // notification to be shown to the user while in this state.
        txService.startForeground(NOTIFICATION_ID_UPLOAD, mNotifBuilder.build());
    }

    @Override
    protected int getProgress() {
        long uploadedSize = 0l;
        long totalSize = 0l;
        if (txService == null)
            return 0;

        List<UploadTaskInfo> infos = txService.getNoneCameraUploadTaskInfos();
        for (UploadTaskInfo info : infos) {
            if (info == null)
                continue;
            uploadedSize += info.uploadedSize;
            totalSize += info.totalSize;
        }

        if (totalSize == 0)
            return 0;

        return (int) (uploadedSize * 100 / totalSize);
    }

    @Override
    protected NotificationState getState() {
        if (txService == null)
            return NotificationState.NOTIFICATION_STATE_COMPLETED;

        List<UploadTaskInfo> infos = txService.getNoneCameraUploadTaskInfos();

        int progressCount = 0;
        int errorCount = 0;

        for (UploadTaskInfo info : infos) {
            if (info == null)
                continue;
            if (info.state.equals(TaskState.INIT)
                    || info.state.equals(TaskState.TRANSFERRING))
                progressCount++;
            else if (info.state.equals(TaskState.FAILED)
                    || info.state.equals(TaskState.CANCELLED))
                errorCount++;
        }

        if (progressCount == 0 && errorCount == 0)
            return NotificationState.NOTIFICATION_STATE_COMPLETED;
        else if (progressCount == 0 && errorCount > 0)
            return NotificationState.NOTIFICATION_STATE_COMPLETED_WITH_ERRORS;
        else // progressCount > 0
            return NotificationState.NOTIFICATION_STATE_PROGRESS;
    }

    @Override
    protected int getNotificationID() {
        return NOTIFICATION_ID_UPLOAD;
    }

    @Override
    protected String getNotificationTitle() {
        return NihaoApplication.getAppContext().getString(R.string.notification_upload_started_title);
    }

}
