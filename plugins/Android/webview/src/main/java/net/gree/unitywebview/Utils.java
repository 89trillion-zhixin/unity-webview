package net.gree.unitywebview;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.Configuration;
import android.os.Build;
import android.util.Patterns;
import android.webkit.WebSettings;
import android.webkit.WebView;

import com.unity3d.player.UnityPlayer;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Utils {

    /**
     * This is used to check the given URL is valid or not.
     *
     * @param url url to check if is valid.
     * @return true if url is valid, false otherwise.
     */
    public static boolean isValidUrl(String url) {
        Pattern p = Patterns.WEB_URL;
        Matcher m = p.matcher(url.toLowerCase());
        return m.matches();
    }

    /**
     * Configures basic settings of the webView (Javascript enabled, DOM storage enabled,
     * database enabled).
     *
     * @param webView The shared webView.
     */
    @SuppressLint("SetJavaScriptEnabled")
    public static WebSettings configWebViewDefaults(WebView webView, final boolean zoom, final int androidForceDarkMode, final String ua) {
        WebSettings webSettings = webView.getSettings();
        if (ua != null && ua.length() > 0) {
            webSettings.setUserAgentString(ua);
        }
        if (zoom) {
            webSettings.setSupportZoom(true);
            webSettings.setBuiltInZoomControls(true);
        } else {
            webSettings.setSupportZoom(false);
            webSettings.setBuiltInZoomControls(false);
        }
        webSettings.setDisplayZoomControls(false);
        webSettings.setLoadWithOverviewMode(true);
        webSettings.setUseWideViewPort(true);
        webSettings.setJavaScriptEnabled(true);
        webSettings.setGeolocationEnabled(true);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            // Log.i("CWebViewPlugin", "Build.VERSION.SDK_INT = " + Build.VERSION.SDK_INT);
            webSettings.setAllowUniversalAccessFromFileURLs(true);
        }
        if (android.os.Build.VERSION.SDK_INT >= 17) {
            webSettings.setMediaPlaybackRequiresUserGesture(false);
        }
        webSettings.setDomStorageEnabled(true);

        // 1. 设置缓存路径
        String cacheDirPath = webView.getContext().getFilesDir().getAbsolutePath() + "webCache/";
        webSettings.setAppCachePath(cacheDirPath);
        // 2. 设置缓存大小
        webSettings.setAppCacheMaxSize(50 * 1024 * 1024);
        // 3. 开启Application Cache存储机制
        webSettings.setAppCacheEnabled(true);

        String databasePath = webView.getContext().getDir("databases", Context.MODE_PRIVATE).getPath();
        webSettings.setDatabasePath(databasePath);
        webSettings.setDatabaseEnabled(true);
        webSettings.setCacheMode(WebSettings.LOAD_DEFAULT);
        webSettings.setAllowFileAccess(true);  // cf. https://github.com/gree/unity-webview/issues/625

        // cf. https://forum.unity.com/threads/unity-ios-dark-mode.805344/#post-6476051
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            switch (androidForceDarkMode) {
                case 0: {
                    Configuration configuration = UnityPlayer.currentActivity.getResources().getConfiguration();
                    switch (configuration.uiMode & Configuration.UI_MODE_NIGHT_MASK) {
                        case Configuration.UI_MODE_NIGHT_NO:
                            webSettings.setForceDark(WebSettings.FORCE_DARK_OFF);
                            break;
                        case Configuration.UI_MODE_NIGHT_YES:
                            webSettings.setForceDark(WebSettings.FORCE_DARK_ON);
                            break;
                    }
                }
                break;
                case 1:
                    webSettings.setForceDark(WebSettings.FORCE_DARK_OFF);
                    break;
                case 2:
                    webSettings.setForceDark(WebSettings.FORCE_DARK_ON);
                    break;
            }
        }

        return webSettings;
    }
}