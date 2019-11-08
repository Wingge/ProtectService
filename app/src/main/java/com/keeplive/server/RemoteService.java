package com.keeplive.server;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.os.Build;
import android.os.IBinder;
import android.os.RemoteException;
import android.support.annotation.RequiresApi;
import android.util.Log;

import com.keeplive.client.IClientAidlInterface;

import static com.keeplive.server.LockScreenReceiver.CHANNEL_ID;
import static com.keeplive.server.LockScreenReceiver.CHANNEL_NAME;
import static com.keeplive.server.LockScreenReceiver.CLIENT_PACKAGE;
import static com.keeplive.server.LockScreenReceiver.CLIENT_SERVICE_ACTION;
import static com.keeplive.server.LockScreenReceiver.NOTIFICATION_ID;


public class RemoteService extends Service {
    private LockScreenReceiver mReceiver;
    public final static String TAG = RemoteService.class.getSimpleName();

    private IClientAidlInterface iClientAidlInterface;

    private boolean mIsBound;

    @Override
    public void onCreate() {
        super.onCreate();
        Log.d(TAG, "======onCreate: RemoteService");
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForeground();
        } else {
            startHideForceService();
        }
//        startForeground();
        try {
            stub.start();
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return stub;
    }


    private void startHideForceService() {
//        if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.N) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
            ForegroundEnablingService.startForeground(this);
            startServiceCompat(this, new Intent(this, ForegroundEnablingService.class));
        } else {
            startForeground(NOTIFICATION_ID, new Notification());
        }
//        }
    }

    public static void startServiceCompat(Context context, Intent service) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            try {
                context.startForegroundService(service);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        context.startService(service);
    }


    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        bindLocalService();
        return START_STICKY;
    }

    private void startForeground() {
        String channelId = null;
        Notification.Builder notificationBuilder = null;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            channelId = createNotificationChannel(CHANNEL_ID, CHANNEL_NAME);
            notificationBuilder = new Notification.Builder(this, channelId);
        } else {
            // If earlier version channel ID is not used
            // https://developer.android.com/reference/android/support/v4/app/Notification.Builder.html#Notification.Builder(android.content.Context)
            notificationBuilder = new Notification.Builder(RemoteService.this);
        }

        notificationBuilder.setOngoing(true)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setPriority(Notification.PRIORITY_HIGH)
        ;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            notificationBuilder.setCategory(Notification.CATEGORY_SERVICE);
        }
        startForeground(NOTIFICATION_ID, notificationBuilder.build());
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private String createNotificationChannel(String channelId, String channelName) {
        NotificationChannel chan = new NotificationChannel(channelId,
                channelName, NotificationManager.IMPORTANCE_NONE);
        NotificationManager service = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        service.createNotificationChannel(chan);
        return channelId;
    }

    private IServerAidlInterface.Stub stub = new IServerAidlInterface.Stub() {

        @Override
        public void start() throws RemoteException {
            registerReceiverr();
        }

        @Override
        public void bindSuccess() throws RemoteException {
            Log.d(TAG, "======bindSuccess");
            bindLocalService();
        }

        @Override
        public void unbind() throws RemoteException {
            Log.d(TAG, "======unbind:   ");
//            getApplicationContext().unbindService(connection);
//            bindLocalService();
//            unregisterReceiver(mReceiver);
        }

        private IntentFilter mIntentFilter = new IntentFilter();

        private void registerReceiverr() {
            mIntentFilter.addAction(Intent.ACTION_BOOT_COMPLETED);
            mIntentFilter.addAction(Intent.ACTION_SCREEN_OFF);
            mIntentFilter.addAction(Intent.ACTION_SCREEN_ON);
            mIntentFilter.addAction(Intent.ACTION_USER_PRESENT);
            mIntentFilter.addAction(Intent.ACTION_TIME_TICK);
            mIntentFilter.addAction(LockScreenReceiver.ACTION_DISCONNECTED);

            mIntentFilter.setPriority(Integer.MAX_VALUE);
            if (null == mReceiver) {
                mReceiver = new LockScreenReceiver();
                mIntentFilter.setPriority(Integer.MAX_VALUE);
                registerReceiver(mReceiver, mIntentFilter);
            }
        }
    };

    /**
     * bind LocalService
     */
    private void bindLocalService() {
        if (mIsBound)
            return;
        try {
            Intent intent = new Intent();
            intent.setAction(CLIENT_SERVICE_ACTION);
            intent.setPackage(CLIENT_PACKAGE);
            bindService(intent, connection, Context.BIND_AUTO_CREATE);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private ServiceConnection connection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            mIsBound = true;
            Log.d(TAG, "======onServiceConnected: LocalService ");
            iClientAidlInterface = IClientAidlInterface.Stub.asInterface(service);
            try {
                iClientAidlInterface.bindSuccess();
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            mIsBound = false;
            Log.d(TAG, "======onServiceDisconnected: LocalService ");
//            createTransferActivity();
            try {
                Intent intent = new Intent(CLIENT_SERVICE_ACTION);//replace to your service's action
                intent.setPackage(CLIENT_PACKAGE);//replace to your package
                startServiceCompat(RemoteService.this, intent);
            } catch (Exception e) {
                e.printStackTrace();
            }
            bindLocalService();
        }
    };

    @Override
    public boolean onUnbind(Intent intent) {
        Log.d(TAG, "======onUnbind:RemoteService");
        boolean xx = super.onUnbind(intent);
        mIsBound = false;
        bindLocalService();
        return xx;
    }

    @Override
    public void onDestroy() {
        Log.d(TAG, "======onDestroy: RemoteService");
        unbindService(connection);
        super.onDestroy();
        if (mReceiver != null) {
            unregisterReceiver(mReceiver);
        }
        Intent intent = new Intent(LockScreenReceiver.ACTION_DISCONNECTED);
        intent.setComponent(new ComponentName(getPackageName(), LockScreenReceiver.class.getName()));
        sendBroadcast(intent);
    }

}
