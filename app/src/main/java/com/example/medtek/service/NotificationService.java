package com.example.medtek.service;

import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import com.example.medtek.R;
import com.example.medtek.ui.activity.MainActivity;

import java.util.Timer;
import java.util.TimerTask;

import static com.example.medtek.App.CHANNEL_ID;

public class NotificationService extends Service {

    private NotificationManagerCompat notificationManagerCompat;

    @Override
    public void onCreate() {
        super.onCreate();
        Timer timer = new Timer();
        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {

            }
        },0,600000000);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        notificationManagerCompat = NotificationManagerCompat.from(this);
        String data =  intent.getStringExtra("data");
        Intent notificationIntent = new Intent(this, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        Notification notification = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentTitle("Pasien Menunggu Konfirmasi...")
                .setContentText(data)
                .setSmallIcon(R.drawable.ic_hospital)
                .setContentIntent(pendingIntent)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setOngoing(false)
                .setVisibility(NotificationCompat.VISIBILITY_SECRET)
                .build();

//        startForeground(1,notification);
        notificationManagerCompat.notify(1,notification);

        return START_STICKY;
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
