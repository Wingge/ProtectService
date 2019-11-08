package com.keeplive.server;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class LockScreenReceiver extends BroadcastReceiver {
    public static final String ACTION_DISCONNECTED = "com.keeplive.server.disconnected";
    public static final String CLIENT_PACKAGE = "com.keeplive.client";//replace to your package
    public static final String CLIENT_SERVICE_ACTION = "wing.android.keep_alive.client";//replace to your service's action
    public final static String CHANNEL_ID = "daemon";
    public final static String CHANNEL_NAME = "daemon_server";
    public final static int NOTIFICATION_ID = 101;
    public final static String TAG = LockScreenReceiver.class.getSimpleName();

    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();

        //daemon by self
        if (ACTION_DISCONNECTED.equals(action)) {
            Log.d(TAG, "====== disconnected,restart by receiver.");
            Intent serviceIntent = new Intent("com.keeplive.aidlserver.RemoteService");
            intent.setPackage("com.keeplive.aidlserver");
            RemoteService.startServiceCompat(context, serviceIntent);
        }
    }
}



