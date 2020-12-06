package com.example.medtek.utils;

import android.util.Log;

import androidx.annotation.Nullable;

import com.example.medtek.BuildConfig;

public final class Logger {
    private static final Boolean ENABLE_STACK_TRACE = true;
    private static final Boolean ENABLE_LONG_LOG = false;

    private static final String TAG = "DANGER";


    static void log(@Nullable Throwable e) {
        if (e == null)
            return;
        log(Log.ERROR, e.toString(), null);
        printStack(e);
    }

    private static void printStack(Throwable e) {
        if (ENABLE_STACK_TRACE) {
            e.printStackTrace();
        }
    }

    static void log(int mode, @Nullable String msg, @Nullable String tag) {
        String message = msg;
        String tags = tag;

        if (!BuildConfig.DEBUG) {
            return;
        }
        if (message == null) {
            return;
        }
        if (tags == null) {
            return;
        }
        if (msg.trim().length() == 0) {
            message = "Data Empty";
        }
        if (ENABLE_LONG_LOG) {
            longLog(mode, tags, message);
        } else {
            if (mode == Log.DEBUG) {
                Log.d(TAG + tag, message);
            } else if (mode == Log.ERROR) {
                Log.e(TAG + tag, message);
            } else if (mode == Log.INFO) {
                Log.i(TAG + tag, message);
            } else if (mode == Log.WARN) {
                Log.w(TAG + tag, message);
            }
        }
    }

    private static void longLog(int mode, String tags, String message) {
        int maxLogSize = 1000;
        for (int i = 0; i < message.length() / maxLogSize; i++) {
            int start = i * maxLogSize;
            int end = (i + 1) * maxLogSize;
            if (end > message.length()) {
                end = message.length();
            }

            if (mode == Log.DEBUG) {
                Log.d(TAG + tags, message.substring(start, end));
            } else if (mode == Log.ERROR) {
                Log.e(TAG + tags, message.substring(start, end));
            } else if (mode == Log.INFO) {
                Log.i(TAG + tags, message.substring(start, end));
            } else if (mode == Log.WARN) {
                Log.w(TAG + tags, message.substring(start, end));
            }
        }
    }
}
