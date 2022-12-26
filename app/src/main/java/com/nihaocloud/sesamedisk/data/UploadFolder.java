package com.nihaocloud.sesamedisk.data;

import android.content.ContentResolver;
import android.content.Context;
import android.net.Uri;
import androidx.documentfile.provider.DocumentFile;
import android.util.Log;

import com.nihaocloud.sesamedisk.util.Utils;

import org.apache.commons.io.IOUtils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import okio.Path;

public class UploadFolder {
    private final String relativePath;
    private final File file;

    public UploadFolder(String relativePath, File file) {
        this.relativePath = relativePath;
        this.file = file;
    }

    public String getRelativePath() {
        return relativePath;
    }

    public File getFile() {
        return file;
    }

    public static UploadFolder[] getUploadCashFiles(final Context context, final DocumentFile[] documentFiles) {
        final List<UploadFolder> uploadFolders = new ArrayList<UploadFolder>();
        for (DocumentFile file : documentFiles) {
            UploadFolder[] uploadCashFiles = getUploadCashFiles(context, file);
            uploadFolders.addAll(Arrays.asList(uploadCashFiles));
        }
        return uploadFolders.toArray(new UploadFolder[]{});
    }

    public static UploadFolder[] getUploadCashFiles(final Context context, final DocumentFile documentFile) {
        final String parentFolderPath = documentFile.getUri().getPath();
        final String parentDirName = documentFile.getName();
        final Context applicationContext = context.getApplicationContext();
        final ContentResolver resolver = applicationContext.getContentResolver();
        final List<DocumentFile> documentFiles = getUploadFilesRecursively(documentFile, new ArrayList<>());
        final List<UploadFolder> uploadFolders = new ArrayList<UploadFolder>();

        for (DocumentFile file : documentFiles) {
            InputStream in = null;
            OutputStream out = null;
            try {
                final File tempDir = DataManager.createTempDir();
                final Uri uri = file.getUri();
                final String name = file.getName();
                final String path = uri.getPath();

                final File tempFile = new File(tempDir, Utils.getFilenamefromUri(applicationContext, uri));
                if (!tempFile.createNewFile()) {
                    throw new RuntimeException("could not create temporary file");
                }
                in = resolver.openInputStream(uri);
                out = new FileOutputStream(tempFile);
                IOUtils.copy(in, out);

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
                pathBuilder.insert(0, parentDirName);
                uploadFolders.add(new UploadFolder(pathBuilder.toString(), tempFile));

            } catch (IOException e) {
                Log.d("UploadFolder", "Could not open requested document", e);
            } catch (RuntimeException e) {
                Log.d("UploadFolder", "Could not open requested document", e);
            } finally {
                IOUtils.closeQuietly(in);
                IOUtils.closeQuietly(out);
            }
        }
        return uploadFolders.toArray(new UploadFolder[]{});
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
//    public void createUploadRequest(List<Uri>fileUri,){
//       //// Path.get()
//    }

    public static File getUploadCashFile(final Context context, final DocumentFile file) {

        final Context applicationContext = context.getApplicationContext();
        final ContentResolver resolver = applicationContext.getContentResolver();

            InputStream in = null;
            OutputStream out = null;
            try {
                final File tempDir = DataManager.createTempDir();
                final Uri uri = file.getUri();
                final String name = file.getName();
                final String path = uri.getPath();

                final File tempFile = new File(tempDir, Utils.getFilenamefromUri(applicationContext, uri));
                if (!tempFile.createNewFile()) {
                    throw new RuntimeException("could not create temporary file");
                }
                in = resolver.openInputStream(uri);
                out = new FileOutputStream(tempFile);
                IOUtils.copy(in, out);

               return tempFile;

            } catch (IOException e) {
                Log.d("UploadFolder", "Could not open requested document", e);
            } catch (RuntimeException e) {
                Log.d("UploadFolder", "Could not open requested document", e);
            } finally {
                IOUtils.closeQuietly(in);
                IOUtils.closeQuietly(out);
            }
        return null;
    }
}
