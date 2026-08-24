package com.nyaminthar.app;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.webkit.CookieManager;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceRequest;
import android.webkit.WebResourceResponse;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.AppCompatActivity;

import java.io.ByteArrayInputStream;
import java.net.URI;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    private static final String HOME_URL = "https://nyaminthar.com/?app=1";
    private WebView webView;

    // Add/remove domains here if your ad provider changes.
    private static final String[] BLOCKED_HOST_PARTS = {
            "doubleclick.net",
            "googlesyndication.com",
            "googleadservices.com",
            "googletagservices.com",
            "adservice.google.com",
            "adsterra.com",
            "adtival.com",
            "monetag.com",
            "propellerads.com",
            "popads.net",
            "popcash.net",
            "hilltopads.net",
            "trafficjunky.com",
            "exoclick.com",
            "juicyads.com",
            "onclick.com",
            "onclkds.com",
            "adnxs.com",
            "adskeeper.com",
            "mgid.com",
            "outbrain.com",
            "taboola.com"
    };

    @SuppressLint("SetJavaScriptEnabled")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        webView = new WebView(this);
        setContentView(webView);

        WebSettings settings = webView.getSettings();
        settings.setJavaScriptEnabled(true);
        settings.setDomStorageEnabled(true);
        settings.setDatabaseEnabled(true);
        settings.setSupportZoom(false);
        settings.setBuiltInZoomControls(false);
        settings.setDisplayZoomControls(false);
        settings.setMediaPlaybackRequiresUserGesture(false);
        settings.setMixedContentMode(WebSettings.MIXED_CONTENT_NEVER_ALLOW);

        CookieManager.getInstance().setAcceptCookie(true);
        CookieManager.getInstance().setAcceptThirdPartyCookies(webView, false);

        webView.setOverScrollMode(View.OVER_SCROLL_NEVER);

        webView.setWebViewClient(new AdBlockingWebViewClient());

        // Do not allow websites to create pop-up windows.
        webView.setWebChromeClient(new WebChromeClient() {
            @Override
            public boolean onCreateWindow(WebView view, boolean isDialog,
                                          boolean isUserGesture, android.os.Message resultMsg) {
                return false;
            }
        });

        webView.loadUrl(HOME_URL);

        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                if (webView.canGoBack()) {
                    webView.goBack();
                } else {
                    finish();
                }
            }
        });
    }

    private boolean isBlockedHost(String url) {
        try {
            URI uri = new URI(url);
            String host = uri.getHost();
            if (host == null) return false;

            host = host.toLowerCase(Locale.US);

            for (String blocked : BLOCKED_HOST_PARTS) {
                if (host.equals(blocked) || host.endsWith("." + blocked)) {
                    return true;
                }
            }

            // Generic ad/tracker host checks.
            return host.contains("adserver")
                    || host.contains("advertising")
                    || host.contains("popunder")
                    || host.contains("popup")
                    || host.contains("banner-ad")
                    || host.contains("tracking");
        } catch (Exception ignored) {
            return false;
        }
    }

    private boolean isInternalHost(String url) {
        try {
            URI uri = new URI(url);
            String host = uri.getHost();
            if (host == null) return false;

            host = host.toLowerCase(Locale.US);
            return host.equals("nyaminthar.com")
                    || host.endsWith(".nyaminthar.com");
        } catch (Exception ignored) {
            return false;
        }
    }

    private class AdBlockingWebViewClient extends WebViewClient {

        @Override
        public WebResourceResponse shouldInterceptRequest(
                WebView view, WebResourceRequest request) {

            String url = request.getUrl().toString();

            if (isBlockedHost(url)) {
                return new WebResourceResponse(
                        "text/plain",
                        "UTF-8",
                        new ByteArrayInputStream(new byte[0])
                );
            }

            return super.shouldInterceptRequest(view, request);
        }

        @Override
        public boolean shouldOverrideUrlLoading(
                WebView view, WebResourceRequest request) {

            String url = request.getUrl().toString();

            if (isBlockedHost(url)) {
                return true;
            }

            if (url.startsWith("http://") || url.startsWith("https://")) {
                if (isInternalHost(url)) {
                    return false;
                }

                // Normal external links open in the phone browser.
                try {
                    startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(url)));
                } catch (Exception ignored) {
                }
                return true;
            }

            // Block javascript:, intent:, and other popup-style schemes.
            return true;
        }

        @Override
        public void onPageStarted(WebView view, String url, Bitmap favicon) {
            super.onPageStarted(view, url, favicon);
            injectAdHidingScript(view);
        }

        @Override
        public void onPageFinished(WebView view, String url) {
            super.onPageFinished(view, url);
            injectAdHidingScript(view);
        }

        private void injectAdHidingScript(WebView view) {
            String js =
                    "(function() {" +
                    "const selectors=["[id*='ad-']","[id*='ads-']","[id*='advert']"," +
                    ""[class*='ad-']","[class*='ads-']","[class*='advert']"," +
                    ""[class*='banner']","[class*='popunder']","[class*='popup']"];"+
                    "function hide(){document.querySelectorAll(selectors.join(',')).forEach(function(e){" +
                    "e.style.setProperty('display','none','important');" +
                    "e.style.setProperty('visibility','hidden','important');" +
                    "});}" +
                    "hide(); setInterval(hide,1500);" +
                    "window.open=function(){return null;};" +
                    "})();";

            view.evaluateJavascript(js, null);
        }
    }
}
