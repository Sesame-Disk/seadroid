package com.nihaocloud.sesamedisk.monitor;

import com.google.common.base.Objects;
import com.nihaocloud.sesamedisk.SettingsManager;
import com.nihaocloud.sesamedisk.account.Account;

public class AutoUpdateInfo {
    public final Account account;
    public  final String repoID;
    public final String repoName;
    public final String parentDir;
    public final String relativePath;
    public final String localPath;

    public AutoUpdateInfo(Account account, String repoID, String repoName,
                          String parentDir, String relativePath, String localPath) {
        this.account = account;
        this.repoID = repoID;
        this.repoName = repoName;
        this.parentDir = parentDir;
        this.relativePath = relativePath;
        this.localPath = localPath;
    }

    public boolean canLocalDecrypt() {
        return SettingsManager.instance().isEncryptEnabled();
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null || (obj.getClass() != this.getClass()))
            return false;

        AutoUpdateInfo that = (AutoUpdateInfo) obj;
        if (that.account == null || that.repoID == null || that.repoName == null || that.parentDir == null || that.localPath == null) {
            return false;
        }

        return that.account.equals(this.account) && that.repoID.equals(this.repoID) &&
                that.repoName.equals(this.repoName) && that.parentDir.equals(this.parentDir) &&
                that.localPath.equals(this.localPath) && (that.relativePath == null || that.relativePath.equals(relativePath));
    }

    private volatile int hashCode = 0;

    @Override
    public int hashCode() {
        if (hashCode == 0) {
            hashCode = Objects.hashCode(account, repoID, repoName, parentDir, localPath);
        }
        return hashCode;
    }
}