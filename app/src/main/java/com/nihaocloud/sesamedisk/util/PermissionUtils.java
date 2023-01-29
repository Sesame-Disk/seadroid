package com.nihaocloud.sesamedisk.util;

import static android.Manifest.permission.READ_EXTERNAL_STORAGE;
import static android.Manifest.permission.WRITE_EXTERNAL_STORAGE;
import static android.os.Build.VERSION.SDK_INT;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.Settings;
import com.google.android.material.snackbar.Snackbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import android.view.View;

import com.nihaocloud.sesamedisk.R;

public class PermissionUtils {

    public static boolean checkStoragePermission(Context context) {
        if (SDK_INT >= Build.VERSION_CODES.R) {
            return Environment.isExternalStorageManager();
        } else {
            int read = ContextCompat.checkSelfPermission(context, READ_EXTERNAL_STORAGE);
            int write = ContextCompat.checkSelfPermission(context, WRITE_EXTERNAL_STORAGE);
            return read == PackageManager.PERMISSION_GRANTED && write == PackageManager.PERMISSION_GRANTED;
        }
    }

    public static void requestStoragePermission(final AppCompatActivity activity,
                                                final View view,
                                                final int requestCode) {
        if (SDK_INT >= Build.VERSION_CODES.R) {
            final AlertDialog alertDialog = new AlertDialog.Builder(activity)
                    .setMessage(R.string.permission_read_exteral_storage_rationale)
                    .setCancelable(false)
                    .setPositiveButton(android.R.string.ok, (dialog, which) -> {
                        try {
                            Intent intent = new Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION);
                            intent.addCategory("android.intent.category.DEFAULT");
                            final String packageName = activity.getApplicationContext().getPackageName();
                            final Uri uri = Uri.parse(String.format("package:%s", packageName));
                            intent.setData(uri);
                            activity.startActivityForResult(intent, requestCode);
                        } catch (Exception e) {
                            final Intent intent = new Intent();
                            intent.setAction(Settings.ACTION_MANAGE_ALL_FILES_ACCESS_PERMISSION);
                            activity.startActivityForResult(intent, requestCode);
                        }
                        dialog.dismiss();
                    })
                    .create();

            alertDialog.show();
        } else {
            //below android 11
            final String[] permissions = {WRITE_EXTERNAL_STORAGE, READ_EXTERNAL_STORAGE};
            if (ActivityCompat.shouldShowRequestPermissionRationale(activity,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                Snackbar.make(view,
                        R.string.permission_read_exteral_storage_rationale,
                        Snackbar.LENGTH_INDEFINITE)
                        .setAction(R.string.settings, v ->
                                ActivityCompat.requestPermissions(activity, permissions, requestCode))
                        .show();

            } else {
                ActivityCompat.requestPermissions(activity, permissions, requestCode);
            }
        }
    }
}
