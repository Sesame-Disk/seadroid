package com.nihaocloud.sesamedisk.data;

import com.nihaocloud.sesamedisk.util.Utils;

import java.io.File;

public class SeafCachedFile implements SeafItem {
    public int id;
    public String fileID;
    public String repoName;
    public String repoID;
    public String path;
    public String accountSignature;
    public long fileOriginalSize;
    protected File file;
    public String relativePath;

    public SeafCachedFile() {
        id = -1;
    }

    @Override
    public String getTitle() {
        return path.substring(path.lastIndexOf('/') + 1);
    }

    @Override
    public String getSubtitle() {
        return Utils.readableFileSize(file.length());
    }

    @Override
    public int getIcon() {
        return Utils.getFileIcon(file.getName());
    }

    public long getSize() {
        return file.length();
    }

    public long getLastModified() {
        return file.lastModified();
    }

    public boolean isDirectory() {
        return file.isDirectory();
    }

    public String getAccountSignature() {
        return accountSignature;
    }

    public long getFileOriginalSize() {
        return fileOriginalSize;
    }

    public void setFileOriginalSize(long fileOriginalSize) {
        this.fileOriginalSize = fileOriginalSize;
    }
}
