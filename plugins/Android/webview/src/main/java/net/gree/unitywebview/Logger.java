package net.gree.unitywebview;

import android.util.Log;

/**
 * project logger
 * Created by Zac on 2018/1/16.
 */

public class Logger {

    private static final String TAG = "Logger";

    private static boolean LogEnable = false;

    public static void SetLogEnable(boolean enable) {
        LogEnable = enable;
    }

    public static void d(String tag, String message) {
        if (LogEnable) {
            Log.d(tag, message);
        }
    }

    public static void d(String tag, String message, Throwable throwable) {
        if (LogEnable) {
            Log.d(tag, message, throwable);
        }
    }

    public static void e(String tag, String message) {
        if (LogEnable) {
            Log.e(tag, message);
        }
    }

    public static void e(String tag, String message, Throwable throwable) {
        if (LogEnable) {
            Log.e(tag, message, throwable);
        }
    }

    public static void i(String tag, String message) {
        if (LogEnable) {
            Log.i(tag, message);
        }
    }

    public static void i(String tag, String... message) {
        if (LogEnable) {
            StringBuilder sb = new StringBuilder();
            for (String msg : message) {
                sb.append(msg);
            }
            Log.i(tag, sb.toString());
        }
    }

    public static void i(String tag, String message, Throwable throwable) {
        if (LogEnable) {
            Log.i(tag, message, throwable);
        }
    }
}
