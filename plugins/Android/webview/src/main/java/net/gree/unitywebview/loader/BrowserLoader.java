package net.gree.unitywebview.loader;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.os.Build;
import android.text.TextUtils;
import android.util.ArrayMap;
import android.util.Log;
import android.view.ViewGroup;
import android.webkit.RenderProcessGoneDetail;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceRequest;
import android.webkit.WebResourceResponse;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import com.unity3d.player.UnityPlayer;

import net.gree.unitywebview.Logger;
import net.gree.unitywebview.Utils;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;

/**
 * A class for manage load/preload web resource by Android {@link WebView}.
 * Created by Zaccc on 2017/12/7.
 */

public class BrowserLoader {

    private static final String TAG = BrowserLoader.class.getSimpleName();

    private static final Object LOCK = new Object();

    private static BrowserLoader sInstance;

    public static BrowserLoader getInstance(Context appContext) {
        if (sInstance == null) {
            synchronized (LOCK) {
                sInstance = new BrowserLoader(appContext);
            }
        }
        return sInstance;
    }

    public static void PreloadUrl(final String url) {
        final Activity a = UnityPlayer.currentActivity;
        a.runOnUiThread(new Runnable() {
            public void run() {
                getInstance(a).preloadUrl(url, false, 1, "");
            }
        });
    }

    public static boolean IsLoadComplete(final String url) {
        final Activity a = UnityPlayer.currentActivity;
        boolean result = getInstance(a).isLoadComplete(url);
        Log.d("dedpp", "Android IsLoadComplete = {url}");
        return result;
    }


    private Context mAppContext;
    // Collection for preload url links.
    private Set<String> mPreloadUrlSet;
    // Collection for load completely url links.
    private Set<String> mFinishLoadUrlSet;
    // WebView pool.
    private Map<String, WebView> mWebViewPool;
    // Collection for url load state listener.
    private Map<String, LoadStateListener> mUrlLoadStateListeners;

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    BrowserLoader(@NonNull Context appContext) {
        mAppContext = appContext;
        mPreloadUrlSet = new HashSet<>();
        mFinishLoadUrlSet = new HashSet<>();
        mWebViewPool = new ArrayMap<>();
        mUrlLoadStateListeners = new ArrayMap<>();
    }

    /**
     * Registers a listener that will receive callbacks while a load state is changed.
     * The callback will be called on the main thread so it's safe to
     * pass the results to widgets.
     * <p>
     * Must be called from the process's main thread.
     *
     * @param listener The listener to register.
     */
    public void registerLoadStateListener(@NonNull String url,
                                          @NonNull LoadStateListener listener) {

        if (mUrlLoadStateListeners == null) {
            throw new IllegalStateException("You should initialize BrowserLoader before register listener");
        }

        if (mUrlLoadStateListeners.containsKey(url)) {
            throw new IllegalStateException("There is already a listener registered");
        }
        mUrlLoadStateListeners.put(url, listener);
    }

    /**
     * Check if given url has register LoadStateListener.
     *
     * @param url given url link to check if register
     * @return true if given url has register listener, otherwise false.
     */
    boolean isRegisterLoadStateListener(@NonNull String url) {
        return mUrlLoadStateListeners != null
                && !mUrlLoadStateListeners.isEmpty()
                && mUrlLoadStateListeners.containsKey(url);
    }

    /**
     * Unregisters a listener that was previously added with
     * {@link #registerLoadStateListener}.
     * <p>
     * Must be called from the main thread.
     * `
     *
     * @param url The url for unregister listener.
     */
    public void unregisterOnLoadStateListener(String url) {

        if (!mUrlLoadStateListeners.containsKey(url)) {
            throw new IllegalStateException("No listener register");
        }
        if (mUrlLoadStateListeners.get(url) == null) {
            throw new IllegalArgumentException("Attempting to unregister the wrong listener");
        }
        mUrlLoadStateListeners.remove(url);
    }

    /**
     * Preload given url in Scheme WebView.
     *
     * @param url given url to load.
     */
    public void preloadUrl(@NonNull String url, final boolean zoom, final int androidForceDarkMode, final String ua) {

        WebView webView = prepareWebView(mAppContext, zoom, androidForceDarkMode, ua);

        if (!Utils.isValidUrl(url)) {
            throw new IllegalArgumentException("You shouldn't load url with an invalid url");
        }

        // Add this link to preload url collection.
        if (!isPreloadUrl(url)) {
            // If there is no same url in the preload url set, we will load it.
            mPreloadUrlSet.add(url);
            loadUrl(url, webView);
        }
    }

    /**
     * Load given url in Scheme WebView.
     *
     * @param url given url to load.
     */
    private void loadUrl(@NonNull String url, @NonNull WebView webView) {

        // Establish relationship between url and webView.
        mWebViewPool.put(url, webView);

        webView.loadUrl(url);
    }

    /**
     * Prepare WebView instance
     *
     * @param context It's better to provide an application context.
     */
    private WebView prepareWebView(@NonNull Context context, final boolean zoom, final int androidForceDarkMode, final String ua) {
        WebView webView = new WebView(context);
        setupWebViewWithDefaults(webView, zoom, androidForceDarkMode, ua);
        return webView;
    }

    /**
     * Set up WebView default settings & clients
     */
    private void setupWebViewWithDefaults(WebView webView, final boolean zoom, final int androidForceDarkMode, final String ua) {
        setWebViewSettings(webView, zoom, androidForceDarkMode, ua);
        setBrowserClients(webView);
    }

    /**
     * Provide WebView instance.
     *
     * @return WebView instance.
     */
    private WebView getWebView(String url) {
        if (!mWebViewPool.containsKey(url)) {
            return null;
        }
        return mWebViewPool.get(url);
    }

    /**
     * Set WebView's WebViewClient and WebChromeClient.
     *
     * @param webView WebView to set up client.
     */
    private void setBrowserClients(@NonNull final WebView webView) {

        webView.setWebViewClient(new WebViewClient() {

            @Override
            public boolean shouldOverrideUrlLoading(WebView webview, String url) {
                Logger.d(TAG, "shouldOverrideUrlLoading intercept url: " + url);

                webView.loadUrl(url);
                return true;
            }

            @Override
            public void onReceivedError(WebView view, final int errorCode,
                                        final String description, String failingUrl) {
                String url = view.getOriginalUrl();
                destroyWebView(url);
                if (mUrlLoadStateListeners != null && mUrlLoadStateListeners.containsKey(url)) {
                    mUrlLoadStateListeners.get(url).onLoadFailed(errorCode, description);
                }
                String msg = url + "@" + errorCode + "@" + description;
                UnityPlayer.UnitySendMessage("WebViewDelegator", "PreloadLoadFailed", msg);
                Logger.e(TAG, "Load failed with onReceivedError: " + description);
            }

            @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
            @Override
            public void onReceivedHttpError(WebView view, WebResourceRequest request, WebResourceResponse errorResponse) {
                String url = view.getOriginalUrl();
                destroyWebView(url);
                if (mUrlLoadStateListeners != null && mUrlLoadStateListeners.containsKey(url)) {
                    mUrlLoadStateListeners.get(url).onLoadFailed(errorResponse.getStatusCode(), errorResponse.getReasonPhrase());
                }
                String msg = url + "@" + errorResponse.getStatusCode() + "@" + errorResponse.getReasonPhrase();
                UnityPlayer.UnitySendMessage("WebViewDelegator", "PreloadLoadFailed", msg);
                Logger.e(TAG, "Load failed with onReceivedHttpError: " + errorResponse.getReasonPhrase());
            }

            @TargetApi(Build.VERSION_CODES.O)
            @Override
            public boolean onRenderProcessGone(WebView view, RenderProcessGoneDetail detail) {
                if (!detail.didCrash()) {
                    // Renderer was killed because the system ran out of memory.
                    // The app can recover gracefully by creating a new WebView instance
                    // in the foreground.
                    Logger.e(TAG, "System killed the WebView rendering process "
                            + "to reclaim memory. Recreating...");

                    if (view != null) {
                        ViewGroup webViewContainer = (ViewGroup) view.getParent();
                        if (webViewContainer != null && webViewContainer.getChildCount() > 0) {
                            webViewContainer.removeView(view);
                        }
                        String url = view.getOriginalUrl();
                        destroyWebView(url);
                    }

                    // By this point, the instance variable "mWebView" is guaranteed
                    // to be null, so it's safe to reinitialize it.

                    return true; // The app continues executing.
                }
                return false;
            }
        });

        webView.setWebChromeClient(new WebChromeClient() {
            @Override
            public void onProgressChanged(WebView view, int newProgress) {
                super.onProgressChanged(view, newProgress);
                Logger.d(TAG, "onProgressChanged: " + newProgress);
                if (view != null && newProgress == 100) {
                    // Page load complete
                    String url = view.getOriginalUrl();
                    destroyWebView(url);
                    if (!isLoadComplete(url)) {
                        mFinishLoadUrlSet.add(url);
                        if (mUrlLoadStateListeners != null && mUrlLoadStateListeners.containsKey(url)) {
                            mUrlLoadStateListeners.get(url).onLoadComplete();
                        }
                        UnityPlayer.UnitySendMessage("WebViewDelegator", "PreloadLoadComplete", url);
                        if (mPreloadUrlSet.contains(url)) {
                            Logger.d(TAG, "preload url:" + url + " complete");
                        }
                    }
                }
            }
        });
    }

    @SuppressLint({"SetJavaScriptEnabled", "ObsoleteSdkInt"})
    private void setWebViewSettings(@NonNull WebView webView, final boolean zoom, final int androidForceDarkMode, final String ua) {
        Utils.configWebViewDefaults(webView, zoom, androidForceDarkMode, ua);
    }

    /**
     * Check if web resources is preload.
     *
     * @return true if web resource is preloaded, otherwise return false.
     */
    public boolean isPreloadUrl(@NonNull String url) {
        if (TextUtils.isEmpty(url) || "null".equalsIgnoreCase(url)) {
            throw new IllegalArgumentException("url == null!");
        }
        return mPreloadUrlSet.contains(url);
    }

    /**
     * Check web resources is load complete.
     *
     * @return true if web resource is load complete, otherwise return false.
     */
    public boolean isLoadComplete(@NonNull String url) {
        if (TextUtils.isEmpty(url) && "null".equalsIgnoreCase(url)) {
            throw new IllegalArgumentException("url == null!");
        }

        return mFinishLoadUrlSet.contains(url);
    }

    /**
     * Destroy WebView object.
     */
    public void destroyWebView(String url) {

        WebView webView = getWebView(url);

        if (webView != null) {
            webView.stopLoading();
            webView.removeAllViews();
            // Loading a blank page is optional, but will ensure that the WebView isn't doing anything when you destroy it.
            webView.loadUrl("about:blank");
            // NOTE: This can occasionally cause a segfault below API 17 (4.2)
            webView.destroy();
            mWebViewPool.remove(url);
        }
    }

    /**
     * Interface for listening web resource load state changed.
     */
    public interface LoadStateListener {

        void onLoadComplete();

        void onLoadFailed(int errorCode, String description);
    }
}
