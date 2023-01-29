package com.nihaocloud.sesamedisk;

import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.nihaocloud.sesamedisk.util.Utils;

/**
 * This receiver is called whenever the system has booted or
 * the Seadroid app has been upgraded to a new version.
 * It can be used to start up background services.
 */
public class BootAutostart extends BroadcastReceiver {
    /**
     * This method will be excecuted after
     * - booting the device
     * - upgrade of the Seadroid package
     */
    @SuppressLint("UnsafeProtectedBroadcastReceiver")
    public void onReceive(Context context, Intent intent) {
        Utils.startCameraSyncJob(context);
    }
}
