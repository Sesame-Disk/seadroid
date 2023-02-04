package com.nihaocloud.sesamedisk.data;

import android.content.ContentResolver;
import android.content.Context;
import android.net.Uri;

import androidx.documentfile.provider.DocumentFile;

import com.nihaocloud.sesamedisk.util.Utils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class UploadFolder {
    private final String relativePath;
    private final Uri uri;
    private final String fileName;
    private final Long fileSize;

    public UploadFolder(String relativePath, Uri uri, String fileName, Long fileSize) {
        this.relativePath = relativePath;
        this.uri = uri;
        this.fileName = fileName;
        this.fileSize = fileSize;
    }

    public String getRelativePath() {
        return relativePath;
    }

    public Uri getUri() {
        return uri;
    }

    public String getFileName() {
        return fileName;
    }

    public Long getFileSize() {
        return fileSize;
    }

    public static List<UploadFolder> getUploadCashFiles(final Context context, final DocumentFile[] documentFiles) {
        final List<UploadFolder> uploadFolders = new ArrayList<UploadFolder>();
        for (DocumentFile file : documentFiles) {
            List<UploadFolder> uploadCashFiles = getUploadCashFiles(context, file);
            uploadFolders.addAll(uploadCashFiles);
        }
        return uploadFolders;
    }

    public static List<UploadFolder> getUploadCashFiles(final Context context, final DocumentFile documentFile) {
        return getUploadCashFiles(context, documentFile, true);
    }

    public static List<UploadFolder> getUploadCashFiles(final Context context, final DocumentFile documentFile, boolean inClaudeParent) {
        final String parentFolderPath = documentFile.getUri().getPath();
        final String parentDirName = documentFile.getName();
        final List<DocumentFile> documentFiles = getUploadFilesRecursively(documentFile, new ArrayList<>());
        final List<UploadFolder> uploadFolders = new ArrayList<UploadFolder>();

        for (DocumentFile file : documentFiles) {
            final Uri uri = file.getUri();
            final String name = Utils.getFilenamefromUri(context, uri);
            final Long size = Utils.getFilSizeFromUri(context, uri);
            final String path = uri.getPath();
            final StringBuilder pathBuilder = new StringBuilder(path);
            int start = pathBuilder.indexOf(parentFolderPath);
            if (start >= 0) {
                int end = start + parentFolderPath.length();
                pathBuilder.delete(start, end);
            }
            start = pathBuilder.lastIndexOf(name);
            if (start >= 0) {
                int end = start + parentFolderPath.length();
                pathBuilder.delete(start, end);
            }
            if (!pathBuilder.toString().startsWith("/")) {
                pathBuilder.insert(0, "/");
            }
            if (inClaudeParent) {
                pathBuilder.insert(0, parentDirName);
            }else  {
                String trim = pathBuilder.toString().trim();
               if(trim.startsWith("/")){
                   int end=pathBuilder.indexOf("/");
                   pathBuilder.delete(0,end+1);
               }
            }
            String relativePath=null;
            if(pathBuilder.length()>0){
                relativePath=pathBuilder.toString();
            }
            uploadFolders.add(new UploadFolder(relativePath, uri, name, size));
        }
        return uploadFolders;
    }

    public static List<DocumentFile> getUploadFilesRecursively(final DocumentFile file, final List<DocumentFile> files) {
        if (file.isDirectory()) {
            final DocumentFile[] documentFiles = file.listFiles();
            if (documentFiles != null) {
                for (DocumentFile documentFile : documentFiles) {
                    getUploadFilesRecursively(documentFile, files);
                }
            }
        } else {
            files.add(file);
        }
        return files;
    }
}
