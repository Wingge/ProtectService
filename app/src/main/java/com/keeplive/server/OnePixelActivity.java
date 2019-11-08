package com.keeplive.server;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.PowerManager;
import android.util.Log;
import android.view.Gravity;
import android.view.Window;
import android.view.WindowManager;

import static com.keeplive.server.LockScreenReceiver.CLIENT_PACKAGE;

public class OnePixelActivity extends Activity {

    private BroadcastReceiver endReceiver;
    public final static String TAG = OnePixelActivity.class.getSimpleName();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "======OnePixelActivity: ");

        //to wake up your client app launcher
//        if (getIntent().getBooleanExtra("isAppLauncher", true)) {
//            try {
//                Intent intent = new Intent(Intent.ACTION_MAIN);
//                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//                intent.addCategory(Intent.CATEGORY_LAUNCHER);
//                ComponentName cn = new ComponentName(CLIENT_PACKAGE,
//                        "com.keeplive.client.OnePiexlActivity");//change to your own launcher
//                intent.setComponent(cn);
//                intent.putExtra("isAppLauncher", false);
//                startActivity(intent);
//            } catch (Exception e) {
//                e.printStackTrace();
//            }
//        }


        //set one pixel
        Window window = getWindow();
        window.setGravity(Gravity.LEFT | Gravity.TOP);
        WindowManager.LayoutParams params = window.getAttributes();
        params.x = 0;
        params.y = 0;
        params.height = 1;
        params.width = 1;
        window.setAttributes(params);

        //finish broadcast
        endReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                finish();
            }
        };
        registerReceiver(endReceiver, new IntentFilter("finish"));

        checkScreen();
    }

    @Override
    protected void onResume() {
        super.onResume();
        checkScreen();
    }

    /**
     * if screen on finish Activity
     */
    private void checkScreen() {

        PowerManager pm = (PowerManager) OnePixelActivity.this.getSystemService(Context.POWER_SERVICE);
        boolean isScreenOn = pm.isScreenOn();
        if (isScreenOn) {
            Intent intent = new Intent(this, RemoteService.class);
            startService(intent);
            finish();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (endReceiver != null) {
            unregisterReceiver(endReceiver);
        }
    }
}