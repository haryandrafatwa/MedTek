package com.example.medtek.utils;

import android.app.Notification;
import android.app.PendingIntent;
import android.content.Context;
import android.media.RingtoneManager;
import android.net.Uri;

import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.core.content.ContextCompat;

import com.example.medtek.R;

public class WidgetUtil {
    public static void showNotification(Context context, String idChannel, String title, String message, int idNotif, boolean isSound, PendingIntent pendingIntent) {

        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);
        Uri alarmSound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, idChannel);
        if (isSound) {
            builder
                    .setSmallIcon(R.drawable.ic_time_black)
                    .setContentTitle(title)
                    .setContentText(message)
                    .setColor(ContextCompat.getColor(context, android.R.color.transparent))
                    .setContentIntent(pendingIntent)
                    .setVibrate(new long[]{1000, 1000, 1000, 1000})
                    .setSound(alarmSound)
                    .setAutoCancel(true);
        } else {
            builder
                    .setSmallIcon(R.drawable.ic_time_black)
                    .setContentTitle(title)
                    .setContentText(message)
                    .setColor(ContextCompat.getColor(context, android.R.color.transparent))
                    .setContentIntent(pendingIntent)
                    .setVibrate(new long[]{1000, 1000, 1000, 1000})
                    .setAutoCancel(true);
        }

        Notification notification = builder.build();
        notificationManager.notify(idNotif, notification);
    }
}
