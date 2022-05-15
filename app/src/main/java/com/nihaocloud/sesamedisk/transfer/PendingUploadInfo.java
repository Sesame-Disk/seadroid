package com.nihaocloud.sesamedisk.transfer;

public class PendingUploadInfo {
    public String repoID;
    public String repoName;
    public String targetDir;
    public String localFilePath;
    public String relativePath;
    public boolean isUpdate;
    public boolean isCopyToLocal;

    public PendingUploadInfo(String repoID, String repoName, String targetDir, String relativePath,
                             String localFilePath, boolean isUpdate, boolean isCopyToLocal) {
        this.repoID = repoID;
        this.repoName = repoName;
        this.targetDir = targetDir;
        this.localFilePath = localFilePath;
        this.relativePath = relativePath;
        this.isUpdate = isUpdate;
        this.isCopyToLocal = isCopyToLocal;
    }
}
