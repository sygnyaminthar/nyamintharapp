package com.nyaminthar.app;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
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

    /*
     * Common advertising / tracking domains.
     * These are blocked only inside this Android WebView.
     */
    private static final Set<String> BLOCKED_DOMAINS = new HashSet<>(
            Arrays.asList(

                    // Adsterra
                    "adsterra.com",
                    "adriver.ru",
                    "go.adsterra.com",
                    "adnxs.com",

                    // Monetag / Propeller style ad domains
                    "monetag.com",
                    "propellerads.com",
                    "onclicka.com",
                    "propush.net",
                    "pushame.com",

                    // Other common ad networks
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

                    // Tracking / analytics
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

        // Required for modern websites
        settings.setJavaScriptEnabled(true);
        settings.setDomStorageEnabled(true);

        // Better website compatibility
        settings.setLoadWithOverviewMode(true);
        settings.setUseWideViewPort(true);

        // Allow normal website storage
        settings.setDatabaseEnabled(true);

        // Prevent some unwanted windows
        settings.setSupportMultipleWindows(false);
        settings.setJavaScriptCanOpenWindowsAutomatically(false);

        // Disable zoom controls
        settings.setBuiltInZoomControls(false);
        settings.setDisplayZoomControls(false);

        // Cache
        settings.setCacheMode(WebSettings.LOAD_DEFAULT);

        /*
         * WebViewClient
         */
        webView.setWebViewClient(new WebViewClient() {

            @Override
            public boolean shouldOverrideUrlLoading(
                    WebView view,
                    WebResourceRequest request
            ) {

                if (request == null || request.getUrl() == null) {
                    return false;
                }

                Uri uri = request.getUrl();

                String scheme = uri.getScheme();
                String host = uri.getHost();

                if (scheme == null) {
                    return false;
                }

                /*
                 * Keep nyaminthar.com inside the WebView.
                 */
                if ("https".equalsIgnoreCase(scheme)
                        || "http".equalsIgnoreCase(scheme)) {

                    if (host != null && isOwnWebsite(host)) {
                        return false;
                    }

                    /*
                     * External HTTP/HTTPS links open in phone browser.
                     */
                    try {
                        Intent intent =
                                new Intent(Intent.ACTION_VIEW, uri);

                        startActivity(intent);

                        return true;

                    } catch (Exception ignored) {
                        return false;
                    }
                }

                /*
                 * Block unknown / unwanted schemes.
                 */
                return true;
            }

            @Override
            public boolean shouldOverrideUrlLoading(
                    WebView view,
                    String url
            ) {

                if (url == null) {
                    return false;
                }

                Uri uri = Uri.parse(url);

                String scheme = uri.getScheme();
                String host = uri.getHost();

                if (scheme == null) {
                    return false;
                }

                if ("https".equalsIgnoreCase(scheme)
                        || "http".equalsIgnoreCase(scheme)) {

                    if (host != null && isOwnWebsite(host)) {
                        return false;
                    }

                    try {
                        Intent intent =
                                new Intent(Intent.ACTION_VIEW, uri);

                        startActivity(intent);

                        return true;

                    } catch (Exception ignored) {
                        return false;
                    }
                }

                return true;
            }

            @Override
            public WebResourceResponse shouldInterceptRequest(
                    WebView view,
                    WebResourceRequest request
            ) {

                if (request != null && request.getUrl() != null) {

                    String url = request.getUrl()
                            .toString()
                            .toLowerCase();

                    String host = request.getUrl().getHost();

                    if (isBlockedUrl(url, host)) {

                        return new WebResourceResponse(
                                "text/plain",
                                "UTF-8",
                                new ByteArrayInputStream(
                                        new byte[0]
                                )
                        );
                    }
                }

                return super.shouldInterceptRequest(view, request);
            }

            @Override
            public WebResourceResponse shouldInterceptRequest(
                    WebView view,
                    String url
            ) {

                if (url != null) {

                    String lowerUrl =
                            url.toLowerCase();

                    Uri uri = Uri.parse(url);
                    String host = uri.getHost();

                    if (isBlockedUrl(lowerUrl, host)) {

                        return new WebResourceResponse(
                                "text/plain",
                                "UTF-8",
                                new ByteArrayInputStream(
                                        new byte[0]
                                )
                        );
                    }
                }

                return super.shouldInterceptRequest(view, url);
            }

            @Override
            public void onPageFinished(
                    WebView view,
                    String url
            ) {

                super.onPageFinished(view, url);

                /*
                 * Remove common advertising elements.
                 */
                removeAds(view);
            }

            @Override
            public void onPageStarted(
                    WebView view,
                    String url,
                    Bitmap favicon
            ) {

                super.onPageStarted(view, url);

                /*
                 * Inject early CSS/JS as soon as possible.
                 */
                removeAds(view);
            }
        });

        /*
         * Prevent WebView from opening popup windows.
         */
        webView.setWebChromeClient(new WebChromeClient() {

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
                /*
                 * Block JavaScript alert popups.
                 */
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
                /*
                 * Block JavaScript confirm popups.
                 */
                result.cancel();
                return true;
            }
        });

        /*
         * Basic WebView security / compatibility.
         */
        settingsSafeConfiguration();
    }

    private void settingsSafeConfiguration() {

        WebSettings settings = webView.getSettings();

        settings.setAllowFileAccess(false);
        settings.setAllowContentAccess(false);

        /*
         * Do not allow WebView to create extra windows.
         */
        settings.setSupportMultipleWindows(false);

        /*
         * Keep JavaScript enabled because the website needs it.
         */
        settings.setJavaScriptEnabled(true);
    }

    private boolean isOwnWebsite(String host) {

        if (host == null) {
            return false;
        }

        host = host.toLowerCase();

        return host.equals("nyaminthar.com")
                || host.endsWith(".nyaminthar.com");
    }

    private boolean isBlockedUrl(
            String url,
            String host
    ) {

        if (url == null) {
            return false;
        }

        String lowerUrl = url.toLowerCase();

        /*
         * Check hostname.
         */
        if (host != null) {

            String lowerHost =
                    host.toLowerCase();

            for (String domain : BLOCKED_DOMAINS) {

                if (lowerHost.equals(domain)
                        || lowerHost.endsWith("." + domain)) {

                    return true;
                }
            }
        }

        /*
         * Check common ad-related URL patterns.
         */
        String[] blockedPatterns = {

                "/popunder",
                "/popunder.",
                "popunder.",
                "/popup",
                "/popup.",
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

        for (String pattern : blockedPatterns) {

            if (lowerUrl.contains(pattern)) {
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

                /*
                 * CSS selectors for common ad elements.
                 */
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

                /*
                 * Hide matched elements.
                 */
                "var css = selectors.join(',') +" +
                "\"{display:none!important;visibility:hidden!important;opacity:0!important;pointer-events:none!important;}\";" +

                /*
                 * Add CSS to page.
                 */
                "var style = document.getElementById('nyaminthar-app-adblock-style');" +

                "if (!style) {" +
                "style = document.createElement('style');" +
                "style.id = 'nyaminthar-app-adblock-style';" +
                "style.innerHTML = css;" +
                "document.head.appendChild(style);" +
                "}" +

                /*
                 * Remove matched elements.
                 */
                "selectors.forEach(function(selector) {" +

                "try {" +

                "document.querySelectorAll(selector).forEach(function(el) {" +
                "el.remove();" +
                "});" +

                "} catch(e) {}" +

                "});" +

                /*
                 * Remove scripts containing common ad networks.
                 */
                "document.querySelectorAll('script').forEach(function(script) {" +

                "var src = script.src || '';" +
                "var text = script.textContent || '';" +

                "var value = (src + ' ' + text).toLowerCase();" +

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

                "for (var i=0;i<blocked.length;i++) {" +

                "if (value.indexOf(blocked[i]) !== -1) {" +
                "script.remove();" +
                "break;" +
                "}" +

                "}" +

                "});" +

                /*
                 * Remove common fixed overlays.
                 */
                "document.querySelectorAll('body *').forEach(function(el) {" +

                "try {" +

                "var s = window.getComputedStyle(el);" +

                "if (s.position === 'fixed' && " +
                "(parseInt(s.zIndex || '0') > 9999)) {" +

                "var txt = (el.innerText || '').toLowerCase();" +
                "var cl = (el.className || '').toString().toLowerCase();" +
                "var id = (el.id || '').toLowerCase();" +

                "if (" +
                "cl.indexOf('ad') !== -1 || " +
                "cl.indexOf('popup') !== -1 || " +
                "cl.indexOf('banner') !== -1 || " +
                "cl.indexOf('popunder') !== -1 || " +
                "id.indexOf('ad') !== -1 || " +
                "id.indexOf('popup') !== -1 || " +
                "id.indexOf('banner') !== -1 || " +
                "txt.indexOf('advertisement') !== -1" +
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

        if (webView != null && webView.canGoBack()) {

            webView.goBack();

        } else {

            super.onBackPressed();
        }
    }

    @Override
    protected void onDestroy() {

        if (webView != null) {

            webView.stopLoading();
            webView.setWebChromeClient(null);
            webView.setWebViewClient(null);
            webView.destroy();
            webView = null;
        }

        super.onDestroy();
    }
}
