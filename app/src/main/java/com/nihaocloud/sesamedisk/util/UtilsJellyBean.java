package com.nihaocloud.sesamedisk.util;

import android.content.ClipData;
import android.content.Intent;
import android.net.Uri;

import java.util.ArrayList;
import java.util.List;

public class UtilsJellyBean {

    public static List<Uri> extractUriListFromIntent(Intent intent) {
        List<Uri> list = new ArrayList<>();

        // if only one file is selected, get it this way
        Uri singleUri = intent.getData();
        if (singleUri != null) {
            list.add(singleUri);
        } else {
            // multiple files? get them this way instead
            ClipData clipdata = intent.getClipData();
            if (clipdata != null) {
                for (int i = 0; i < clipdata.getItemCount(); i++) {
                    Uri uri = clipdata.getItemAt(i).getUri();
                    list.add(uri);
                }
            }
        }
        return list;
    }

}
