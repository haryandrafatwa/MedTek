package com.example.medtek;

import android.app.Application;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.os.Build;
import android.os.Handler;
import android.util.Log;

import androidx.multidex.MultiDex;

import com.orhanobut.hawk.Hawk;

import java.util.ArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class App extends Application {
    private static final String TAG = android.app.Application.class.getSimpleName();
    public static final String ID_CHANNEL_REMINDER = "channel_reminder";
    public static final String ID_CHANNEL_MESSAGE = "channel_message";

    public static final String CHANNEL_ID = "NotificationService";

    public static ExecutorService executorService;
    private static Context context = null;
    private static App instance;
    private static final Handler handler = new Handler();
    private static boolean activityVisible;
    private Thread thread;
    // Whether has been called
    private final boolean closed;
    private final boolean initialized;
    private final ArrayList<Object> registeredManagers;

    public App() {
        instance = this;
        executorService = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());
        registeredManagers = new ArrayList<>();
        closed = false;
        initialized = false;
    }

    public static App get() {
        return instance;
    }

    public static Context getContext() {
        return App.context;
    }

    public static synchronized App getInstance() {
        if (instance == null) {
            instance = new App();
        }
        return instance;
    }

    public static boolean isActivityVisible() {
        return activityVisible;
    }

    public static void activityResumed() {
        activityVisible = true;
    }

    public static void activityPaused() {
        activityVisible = false;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        context = getBaseContext();
        Hawk.init(this).build();
        createNotificationChannel();
        executorService.execute(new Runnable() {
            @Override
            public void run() {
                setupMultidex();
            }
        });
        createNotification();
    }

    private synchronized void setupMultidex() {
        MultiDex.install(this);
    }

    // Submits request to be execu ted in UI thread.
    public void runOnUiThread(final Runnable runnable) {
        handler.post(runnable);
    }

    public void runOnUIThreadDelay(Runnable run, long timemilis) {
        handler.postDelayed(run, timemilis);
    }

    public void runInBackground(final Runnable runnable) {
        executorService.submit(() -> {
            try {
                runnable.run();
            } catch (Exception e) {
                Log.e(TAG, "backgroundThread");
                e.printStackTrace();
            }
        });
    }

    private void createNotification() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, "Notification", NotificationManager.IMPORTANCE_DEFAULT);
            NotificationManager manager = getSystemService(NotificationManager.class);
            manager.createNotificationChannel(channel);
        }
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channelReminder = new NotificationChannel(
                    ID_CHANNEL_REMINDER,
                    "Channel Reminder",
                    NotificationManager.IMPORTANCE_DEFAULT
            );
            channelReminder.setDescription("Test Channel Reminder");
            channelReminder.enableLights(true);
            channelReminder.enableVibration(true);
            channelReminder.setVibrationPattern(new long[]{1000, 1000, 1000, 1000});

            NotificationChannel channelMessage = new NotificationChannel(
                    ID_CHANNEL_MESSAGE,
                    "Channel Message",
                    NotificationManager.IMPORTANCE_HIGH
            );
            channelMessage.setDescription("Test Channel Message");
            channelMessage.enableLights(true);
            channelMessage.enableVibration(true);
            channelMessage.setVibrationPattern(new long[]{1000, 1000, 1000, 1000});

            NotificationManager manager = getSystemService(NotificationManager.class);

            manager.createNotificationChannel(channelReminder);
            manager.createNotificationChannel(channelMessage);
        }

    }
}
