package com.keeplive.server;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.IBinder;
import android.support.annotation.RequiresApi;

import static com.keeplive.server.LockScreenReceiver.CHANNEL_ID;
import static com.keeplive.server.LockScreenReceiver.CHANNEL_NAME;
import static com.keeplive.server.LockScreenReceiver.NOTIFICATION_ID;

public class ForegroundEnablingService extends Service {
    @Override
    public void onCreate() {
        super.onCreate();
        startForeground(this); //Cancel this service's notification, resulting in zero notifications stopForeground(true); //Stop this service so we don't waste RAM. //Must only be called *after* doing the work or the notification won't be hidden.
//        stopForeground(true);
        stopSelf();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return START_NOT_STICKY;
    }

    public static void startForeground(Service service) {
        String channelId = null;
//        Notification notification;
        Notification.Builder notificationBuilder;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            channelId = createNotificationChannel(service, CHANNEL_ID, CHANNEL_NAME);
            notificationBuilder = new Notification.Builder(service, channelId);
            notificationBuilder.setOngoing(true)
                    .setSmallIcon(R.mipmap.ic_launcher)
                    .setPriority(Notification.PRIORITY_HIGH);
            notificationBuilder.setCategory(Notification.CATEGORY_SERVICE);
//            notification = notificationBuilder.build();
        } else {
            notificationBuilder = new Notification.Builder(service);
            notificationBuilder.setSmallIcon(R.mipmap.ic_launcher);
//            notification = notificationBuilder.getNotification();
        }
        notificationBuilder.setOngoing(true)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setPriority(Notification.PRIORITY_HIGH)
        ;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            notificationBuilder.setCategory(Notification.CATEGORY_SERVICE);
        }
        service.startForeground(NOTIFICATION_ID, notificationBuilder.build());
    }




    @RequiresApi(Build.VERSION_CODES.O)
    private static String createNotificationChannel(Context context, String channelId, String channelName) {
        NotificationChannel chan = new NotificationChannel(channelId,
                channelName, NotificationManager.IMPORTANCE_NONE);
        NotificationManager service = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        service.createNotificationChannel(chan);
        return channelId;
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }


    @Override
    public void onDestroy() {
        super.onDestroy();
        stopForeground(true);
    }
}