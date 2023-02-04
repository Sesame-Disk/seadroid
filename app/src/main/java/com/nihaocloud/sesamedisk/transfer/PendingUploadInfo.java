package com.nihaocloud.sesamedisk.transfer;

import android.net.Uri;

public class PendingUploadInfo {
    public String repoID;
    public String repoName;
    public String targetDir;
    public Uri uri;
    public final String fileName;
    public final Long fileSize;
    public String relativePath;
    public boolean isUpdate;
    public boolean isCopyToLocal;
    public boolean byBlock = false;

    public PendingUploadInfo(String repoID, String repoName, String targetDir, String relativePath,
                             Uri uri, String fileName, Long fileSize, boolean isUpdate, boolean isCopyToLocal) {
        this.repoID = repoID;
        this.repoName = repoName;
        this.targetDir = targetDir;
        this.uri = uri;
        this.fileName = fileName;
        this.fileSize = fileSize;
        this.relativePath = relativePath;
        this.isUpdate = isUpdate;
        this.isCopyToLocal = isCopyToLocal;
    }

    public PendingUploadInfo(String repoID, String repoName, String targetDir, String relativePath,
                             Uri uri, String fileName, Long fileSize, boolean isUpdate, boolean isCopyToLocal, boolean byBlock) {
        this.repoID = repoID;
        this.repoName = repoName;
        this.targetDir = targetDir;
        this.uri = uri;
        this.fileName = fileName;
        this.fileSize = fileSize;
        this.relativePath = relativePath;
        this.isUpdate = isUpdate;
        this.isCopyToLocal = isCopyToLocal;
        this.byBlock = byBlock;
    }
}
