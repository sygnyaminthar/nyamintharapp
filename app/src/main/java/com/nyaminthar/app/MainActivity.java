package com.nyaminthar.app;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceRequest;
import android.webkit.WebResourceResponse;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import androidx.appcompat.app.AppCompatActivity;

import java.io.ByteArrayInputStream;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public class MainActivity extends AppCompatActivity {

    private WebView webView;

    private static final String WEBSITE_URL =
            "https://nyaminthar.com/?app=1";

    private static final Set<String> BLOCKED_DOMAINS = new HashSet<>(
            Arrays.asList(
                    "adsterra.com",
                    "adriver.ru",
                    "go.adsterra.com",
                    "adnxs.com",
                    "monetag.com",
                    "propellerads.com",
                    "onclicka.com",
                    "propush.net",
                    "pushame.com",
                    "doubleclick.net",
                    "googlesyndication.com",
                    "googleadservices.com",
                    "googletagservices.com",
                    "adservice.google.com",
                    "adskeeper.co.uk",
                    "adskeeper.com",
                    "exoclick.com",
                    "exosrv.com",
                    "trafficjunky.net",
                    "juicyads.com",
                    "hilltopads.net",
                    "hilltopads.com",
                    "adcash.com",
                    "scorecardresearch.com",
                    "quantserve.com",
                    "hotjar.com",
                    "clarity.ms"
            )
    );

    @SuppressLint("SetJavaScriptEnabled")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        webView = new WebView(this);
        setContentView(webView);

        setupWebView();

        webView.loadUrl(WEBSITE_URL);
    }

    @SuppressLint("SetJavaScriptEnabled")
    private void setupWebView() {

        WebSettings settings = webView.getSettings();

        settings.setJavaScriptEnabled(true);
        settings.setDomStorageEnabled(true);
        settings.setLoadWithOverviewMode(true);
        settings.setUseWideViewPort(true);
        settings.setDatabaseEnabled(true);

        settings.setSupportMultipleWindows(false);
        settings.setJavaScriptCanOpenWindowsAutomatically(false);

        settings.setBuiltInZoomControls(false);
        settings.setDisplayZoomControls(false);

        settings.setCacheMode(WebSettings.LOAD_DEFAULT);

        settings.setAllowFileAccess(false);
        settings.setAllowContentAccess(false);

        webView.setWebViewClient(new WebViewClient() {

            @Override
            public boolean shouldOverrideUrlLoading(
                    WebView view,
                    WebResourceRequest request
            ) {

                if (request == null || request.getUrl() == null) {
                    return false;
                }

                return handleUrl(request.getUrl());
            }

            @Override
            public boolean shouldOverrideUrlLoading(
                    WebView view,
                    String url
            ) {

                if (url == null) {
                    return false;
                }

                return handleUrl(Uri.parse(url));
            }

            private boolean handleUrl(Uri uri) {

                String scheme = uri.getScheme();
                String host = uri.getHost();

                if (scheme == null) {
                    return true;
                }

                if ("https".equalsIgnoreCase(scheme)
                        || "http".equalsIgnoreCase(scheme)) {

                    if (host != null && isOwnWebsite(host)) {
                        return false;
                    }

                    try {

                        startActivity(
                                new Intent(
                                        Intent.ACTION_VIEW,
                                        uri
                                )
                        );

                    } catch (Exception ignored) {
                    }

                    return true;
                }

                return true;
            }

            @Override
            public WebResourceResponse shouldInterceptRequest(
                    WebView view,
                    WebResourceRequest request
            ) {

                if (request != null
                        && request.getUrl() != null) {

                    Uri uri = request.getUrl();

                    String url =
                            uri.toString().toLowerCase();

                    if (isBlockedUrl(
                            url,
                            uri.getHost()
                    )) {

                        return emptyResponse();
                    }
                }

                return super.shouldInterceptRequest(
                        view,
                        request
                );
            }

            @Override
            public WebResourceResponse shouldInterceptRequest(
                    WebView view,
                    String url
            ) {

                if (url != null) {

                    Uri uri = Uri.parse(url);

                    String lowerUrl =
                            url.toLowerCase();

                    if (isBlockedUrl(
                            lowerUrl,
                            uri.getHost()
                    )) {

                        return emptyResponse();
                    }
                }

                return super.shouldInterceptRequest(
                        view,
                        url
                );
            }

            @Override
            public void onPageStarted(
                    WebView view,
                    String url,
                    Bitmap favicon
            ) {

                super.onPageStarted(
                        view,
                        url,
                        favicon
                );

                removeAds(view);
            }

            @Override
            public void onPageFinished(
                    WebView view,
                    String url
            ) {

                super.onPageFinished(
                        view,
                        url
                );

                removeAds(view);
            }
        });

        webView.setWebChromeClient(
                new WebChromeClient() {

                    @Override
                    public boolean onCreateWindow(
                            WebView view,
                            boolean isDialog,
                            boolean isUserGesture,
                            android.os.Message resultMsg
                    ) {

                        return false;
                    }

                    @Override
                    public boolean onJsAlert(
                            WebView view,
                            String url,
                            String message,
                            android.webkit.JsResult result
                    ) {

                        result.cancel();

                        return true;
                    }

                    @Override
                    public boolean onJsConfirm(
                            WebView view,
                            String url,
                            String message,
                            android.webkit.JsResult result
                    ) {

                        result.cancel();

                        return true;
                    }
                }
        );
    }

    private WebResourceResponse emptyResponse() {

        return new WebResourceResponse(
                "text/plain",
                "UTF-8",
                new ByteArrayInputStream(
                        new byte[0]
                )
        );
    }

    private boolean isOwnWebsite(String host) {

        if (host == null) {
            return false;
        }

        String lowerHost =
                host.toLowerCase();

        return lowerHost.equals(
                "nyaminthar.com"
        )
                || lowerHost.endsWith(
                ".nyaminthar.com"
        );
    }

    private boolean isBlockedUrl(
            String url,
            String host
    ) {

        if (url == null) {
            return false;
        }

        if (host != null) {

            String lowerHost =
                    host.toLowerCase();

            for (String domain :
                    BLOCKED_DOMAINS) {

                if (lowerHost.equals(domain)
                        || lowerHost.endsWith(
                        "." + domain
                )) {

                    return true;
                }
            }
        }

        String[] blockedPatterns = {

                "/popunder",
                "/popup",
                "popunder",
                "adserver",
                "adsserver",
                "/ads/",
                "/ads.",
                "/advert",
                "/advertising",
                "/banner",
                "banner-ad",
                "banner_ad",
                "doubleclick",
                "googlesyndication",
                "adservice",
                "tracking",
                "tracker",
                "click.php",
                "click?",
                "redirect.php",
                "push-notification"
        };

        for (String pattern :
                blockedPatterns) {

            if (url.contains(pattern)) {
                return true;
            }
        }

        return false;
    }

    private void removeAds(WebView view) {

        if (view == null) {
            return;
        }

        String javascript =
                "(function() {" +

                "try {" +

                "var selectors = [" +

                "\"[class*='ad-']\"," +
                "\"[class*='ads-']\"," +
                "\"[class*='advert']\"," +
                "\"[class*='advertisement']\"," +
                "\"[class*='banner']\"," +
                "\"[class*='popunder']\"," +
                "\"[class*='popup']\"," +

                "\"[id*='ad-']\"," +
                "\"[id*='ads-']\"," +
                "\"[id*='advert']\"," +
                "\"[id*='banner']\"," +
                "\"[id*='popunder']\"," +
                "\"[id*='popup']\"," +

                "\"iframe[src*='ads']\"," +
                "\"iframe[src*='advert']\"," +
                "\"iframe[src*='banner']\"," +
                "\"iframe[src*='popunder']\"" +

                "];" +

                "var css = selectors.join(',') +" +

                "\"{display:none!important;" +
                "visibility:hidden!important;" +
                "opacity:0!important;" +
                "pointer-events:none!important;}\";" +

                "var style = document.getElementById(" +
                "'nyaminthar-app-adblock-style'" +
                ");" +

                "if (!style && document.head) {" +

                "style = document.createElement('style');" +

                "style.id =" +
                "'nyaminthar-app-adblock-style';" +

                "style.innerHTML = css;" +

                "document.head.appendChild(style);" +

                "}" +

                "selectors.forEach(function(selector) {" +

                "try {" +

                "document.querySelectorAll(selector)" +
                ".forEach(function(el) {" +

                "el.remove();" +

                "});" +

                "} catch(e) {}" +

                "});" +

                "document.querySelectorAll('script')" +
                ".forEach(function(script) {" +

                "var src = script.src || '';" +

                "var text = script.textContent || '';" +

                "var value = (" +
                "src + ' ' + text" +
                ").toLowerCase();" +

                "var blocked = [" +

                "'adsterra'," +
                "'monetag'," +
                "'propellerads'," +
                "'popunder'," +
                "'doubleclick'," +
                "'googlesyndication'," +
                "'exoclick'," +
                "'hilltopads'," +
                "'adcash'" +

                "];" +

                "for (" +
                "var i = 0;" +
                "i < blocked.length;" +
                "i++" +
                ") {" +

                "if (" +
                "value.indexOf(blocked[i]) !== -1" +
                ") {" +

                "script.remove();" +

                "break;" +

                "}" +

                "}" +

                "});" +

                "document.querySelectorAll('body *')" +
                ".forEach(function(el) {" +

                "try {" +

                "var s =" +
                "window.getComputedStyle(el);" +

                "if (" +
                "s.position === 'fixed' &&" +
                "parseInt(s.zIndex || '0') > 9999" +
                ") {" +

                "var cl =" +
                "(el.className || '')" +
                ".toString()" +
                ".toLowerCase();" +

                "var id =" +
                "(el.id || '')" +
                ".toLowerCase();" +

                "if (" +

                "cl.indexOf('ad') !== -1 ||" +
                "cl.indexOf('popup') !== -1 ||" +
                "cl.indexOf('banner') !== -1 ||" +
                "cl.indexOf('popunder') !== -1 ||" +

                "id.indexOf('ad') !== -1 ||" +
                "id.indexOf('popup') !== -1 ||" +
                "id.indexOf('banner') !== -1" +

                ") {" +

                "el.remove();" +

                "}" +

                "}" +

                "} catch(e) {}" +

                "});" +

                "} catch(e) {}" +

                "})();";

        view.evaluateJavascript(
                javascript,
                null
        );
    }

    @Override
    public void onBackPressed() {

        if (webView != null
                && webView.canGoBack()) {

            webView.goBack();

        } else {

            super.onBackPressed();
        }
    }

    @Override
    protected void onDestroy() {

        if (webView != null) {

            webView.stopLoading();

            webView.setWebChromeClient(
                    null
            );

            webView.setWebViewClient(
                    null
            );

            webView.destroy();

            webView = null;
        }

        super.onDestroy();
    }
}
