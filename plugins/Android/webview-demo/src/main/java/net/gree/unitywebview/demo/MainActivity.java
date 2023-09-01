package net.gree.unitywebview.demo;

import android.os.Bundle;
import android.view.View;
import android.webkit.WebView;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.Toast;

import net.gree.unitywebview.Utils;
import net.gree.unitywebview.loader.BrowserLoader;

import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {

    private String url = "http://test.act.test.aow.com:1005/beginnerHeroPackage1/1?price=%24%201.99&token=eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJ1aWQiOjI3NzIxNjYsImlzcyI6Imdpbi1ibG9nIn0.zlffvOaEkXb7lTFmKCweKsCVURBdwYOkIqBVvphCCrg&basicInfo=eyJwZiI6ImFuZHJvaWQiLCJzdmMiOjI1LCJjdmMiOjQxNDAsImRldmljZSI6IlNNLU45NTAwIiwidWlkIjoiZmZmZmZmZmYtZWJiNS05MTVmLTAwMDAtMTkxMjEyMTIxNSIsImNoYW4iOiIxMDAwMSIsImFkaWQiOiIiLCJnYWlkIjoiZmZmZmZmZmYtZWJiNS05MTVmLTAwMDAtMTkxMjEyMTIxNSIsIm5ldHdvcmsiOiJ3aWZpIiwibGFuZyI6ImVuX1VTIn0%3D&lang=en";
    private FrameLayout webviewContainer;
    private WebView webView;
    private WebView tempWebView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        webviewContainer = findViewById(R.id.webview_container);
        webView = findViewById(R.id.webview);

        Button preloadBtn = findViewById(R.id.preload_button);
        preloadBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Toast.makeText(view.getContext(), "开始预加载", Toast.LENGTH_SHORT).show();

                BrowserLoader browserLoader = BrowserLoader.getInstance(getApplicationContext());
                browserLoader.registerLoadStateListener(url, new BrowserLoader.LoadStateListener() {
                    @Override
                    public void onLoadComplete() {
                        Toast.makeText(view.getContext(), "预加载完成", Toast.LENGTH_SHORT).show();
                        browserLoader.unregisterOnLoadStateListener(url);
                    }

                    @Override
                    public void onLoadFailed(int errorCode, String description) {
                        browserLoader.unregisterOnLoadStateListener(url);
                    }
                });
                browserLoader.preloadUrl(url, false, 1, "");
            }
        });

        Button openWebViewBtn = findViewById(R.id.open_webview);
        openWebViewBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Utils.configWebViewDefaults(webView, false, 1, "");
                webView.loadUrl(url);
            }
        });

        Button createWebViewBtn = findViewById(R.id.create_webview);
        createWebViewBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                tempWebView = new WebView(getApplicationContext());
                Utils.configWebViewDefaults(tempWebView, false, 1, "");
                tempWebView.loadUrl(url);
                webviewContainer.addView(tempWebView, LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT);
            }
        });

        Button closeWebViewBtn = findViewById(R.id.close_webview);
        closeWebViewBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (tempWebView != null) {
                    tempWebView.stopLoading();
                    tempWebView.loadUrl("about:blank");
                    webviewContainer.removeView(tempWebView);
                    tempWebView.destroy();
                    tempWebView = null;
                }

                webView.stopLoading();
                webView.loadUrl("about:blank");
            }
        });
    }
}