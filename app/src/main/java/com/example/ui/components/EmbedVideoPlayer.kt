package com.example.ui.components

import android.annotation.SuppressLint
import android.graphics.Bitmap
import android.app.Activity
import android.content.pm.ActivityInfo
import android.view.View
import android.view.ViewGroup
import android.webkit.CookieManager
import android.webkit.WebChromeClient
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import android.webkit.WebResourceRequest
import android.widget.FrameLayout
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.viewinterop.AndroidView
import androidx.activity.compose.BackHandler

@SuppressLint("SetJavaScriptEnabled")
@Composable
fun EmbedVideoPlayer(
    url: String,
    modifier: Modifier = Modifier
) {
    var isLoading by remember { mutableStateOf(true) }
    var isFullscreen by remember { mutableStateOf(false) }
    var exitFullscreen by remember { mutableStateOf<(() -> Unit)?>(null) }

    BackHandler(enabled = isFullscreen) {
        exitFullscreen?.invoke()
    }

    Box(
        modifier = modifier.background(Color.Black)
    ) {
        AndroidView(
            factory = { context ->
                    WebView(context).apply {
                    val webViewInstance = this
                    layoutParams = ViewGroup.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.MATCH_PARENT
                    )

                    // Enable cookies and third-party cookies for cross-domain streams
                    try {
                        CookieManager.getInstance().apply {
                            setAcceptCookie(true)
                            setAcceptThirdPartyCookies(webViewInstance, true)
                        }
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }

                    settings.apply {
                        javaScriptEnabled = true
                        domStorageEnabled = true
                        databaseEnabled = true
                        allowFileAccess = true
                        allowContentAccess = true
                        mediaPlaybackRequiresUserGesture = false
                        mixedContentMode = WebSettings.MIXED_CONTENT_ALWAYS_ALLOW
                        loadWithOverviewMode = true
                        useWideViewPort = true
                        builtInZoomControls = false
                        displayZoomControls = false
                        // Set standard Chrome Mobile user agent to bypass streaming host WebView blocks
                        userAgentString = "Mozilla/5.0 (Linux; Android 13; SM-G998B) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/116.0.0.0 Mobile Safari/537.36"
                    }

                    webViewClient = object : WebViewClient() {
                        override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
                            isLoading = true
                        }

                        override fun onPageFinished(view: WebView?, url: String?) {
                            isLoading = false
                        }

                        override fun shouldOverrideUrlLoading(view: WebView?, request: WebResourceRequest?): Boolean {
                            val urlString = request?.url?.toString() ?: return false
                            return handleUrlOverride(urlString)
                        }

                        @Deprecated("Deprecated in Java", ReplaceWith("false"))
                        override fun shouldOverrideUrlLoading(view: WebView?, urlString: String?): Boolean {
                            val urlStr = urlString ?: return false
                            return handleUrlOverride(urlStr)
                        }

                        private fun handleUrlOverride(urlString: String): Boolean {
                            val lowerUrl = urlString.lowercase()
                            // Block non-HTTP/HTTPS URLs (like market://, intent://, telegram://, shoppee://, etc.)
                            if (!urlString.startsWith("http://") && !urlString.startsWith("https://")) {
                                return true // consume/block
                            }
                            
                            // Block redirects to known ad networks, coin mining, gambling, or redirect gateways
                            if (lowerUrl.contains("bet") || 
                                lowerUrl.contains("casino") || 
                                lowerUrl.contains("ads") || 
                                lowerUrl.contains("click") || 
                                lowerUrl.contains("doubleclick") || 
                                lowerUrl.contains("popunder") || 
                                lowerUrl.contains("popup") || 
                                lowerUrl.contains("redirect") || 
                                lowerUrl.contains("adkeeper") || 
                                lowerUrl.contains("exoclick") || 
                                lowerUrl.contains("propeller") || 
                                lowerUrl.contains("mgid") || 
                                lowerUrl.contains("adnxs") || 
                                lowerUrl.contains("adsterra") || 
                                lowerUrl.contains("ouo.io") || 
                                lowerUrl.contains("shorte.st")
                            ) {
                                return true // Block obvious ad networks
                            }

                            // If it's a media file or stream manifest, definitely allow it
                            if (lowerUrl.contains(".m3u8") || 
                                lowerUrl.contains(".mp4") || 
                                lowerUrl.contains(".ts") || 
                                lowerUrl.contains(".mkv") || 
                                lowerUrl.contains("/hls/") || 
                                lowerUrl.contains("/embed/") || 
                                lowerUrl.contains("player") || 
                                lowerUrl.contains("nguonc")
                            ) {
                                return false // Allow
                            }

                            return false // allow other CDN/player requests to flow freely
                        }
                    }

                    var customView: View? = null
                    var customViewCallback: WebChromeClient.CustomViewCallback? = null
                    val activity = context as? Activity

                    webChromeClient = object : WebChromeClient() {
                        override fun getDefaultVideoPoster(): Bitmap? {
                            // Clear default gray background element to render video seamlessly
                            return Bitmap.createBitmap(1, 1, Bitmap.Config.ARGB_8888)
                        }

                        override fun onShowCustomView(view: View?, callback: CustomViewCallback?) {
                            super.onShowCustomView(view, callback)
                            if (customView != null) {
                                callback?.onCustomViewHidden()
                                return
                            }
                            customView = view
                            customViewCallback = callback
                            isFullscreen = true
                            
                            exitFullscreen = {
                                callback?.onCustomViewHidden()
                                this.onHideCustomView()
                            }

                            activity?.let { act ->
                                val decorView = act.window.decorView as FrameLayout
                                decorView.addView(view, FrameLayout.LayoutParams(
                                    ViewGroup.LayoutParams.MATCH_PARENT,
                                    ViewGroup.LayoutParams.MATCH_PARENT
                                ))
                                
                                // Hide System UI
                                val windowInsetsController = WindowCompat.getInsetsController(act.window, decorView)
                                windowInsetsController.systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
                                windowInsetsController.hide(WindowInsetsCompat.Type.systemBars())
                                
                                // Set landscape
                                act.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE
                            }
                        }

                        override fun onHideCustomView() {
                            super.onHideCustomView()
                            val view = customView ?: return
                            
                            activity?.let { act ->
                                val decorView = act.window.decorView as FrameLayout
                                decorView.removeView(view)
                                
                                // Show System UI
                                val windowInsetsController = WindowCompat.getInsetsController(act.window, decorView)
                                windowInsetsController.show(WindowInsetsCompat.Type.systemBars())
                                
                                // Restore orientation
                                act.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
                            }
                            
                            customView = null
                            customViewCallback?.onCustomViewHidden()
                            customViewCallback = null
                            isFullscreen = false
                            exitFullscreen = null
                        }
                    }
                }
            },
            update = { webView ->
                // Avoid infinite reload / flicker loops by checking view tag
                val loadedUrl = webView.tag as? String
                if (loadedUrl != url) {
                    webView.tag = url
                    if (url.contains(".m3u8") || url.endsWith(".m3u8")) {
                        // Load custom HTML with hls.js for native streaming support!
                        val html = """
                            <!DOCTYPE html>
                            <html>
                            <head>
                                <meta name="viewport" content="width=device-width, initial-scale=1.0, maximum-scale=1.0, user-scalable=no" />
                                <script src="https://cdn.jsdelivr.net/npm/hls.js@1.4.12/dist/hls.min.js"></script>
                                <style>
                                    body, html { margin: 0; padding: 0; width: 100%; height: 100%; background-color: #000; overflow: hidden; display: flex; justify-content: center; align-items: center; }
                                    video { width: 100%; height: 100%; object-fit: contain; }
                                </style>
                            </head>
                            <body>
                                <video id="video" controls autoplay playsinline crossorigin="anonymous"></video>
                                <script>
                                    var video = document.getElementById('video');
                                    var videoSrc = '$url';
                                    if (Hls.isSupported()) {
                                        var hls = new Hls({
                                            maxMaxBufferLength: 10,
                                            enableWorker: true
                                        });
                                        hls.loadSource(videoSrc);
                                        hls.attachMedia(video);
                                        hls.on(Hls.Events.MANIFEST_PARSED, function() {
                                            video.play().catch(function(e) {
                                                console.log("Autoplay blocked, user interaction required");
                                            });
                                        });
                                    } else if (video.canPlayType('application/vnd.apple.mpegurl')) {
                                        video.src = videoSrc;
                                        video.play().catch(function(e) {
                                            console.log("Autoplay blocked, user interaction required");
                                        });
                                    }
                                </script>
                            </body>
                            </html>
                        """.trimIndent()
                        webView.loadDataWithBaseURL("https://phim.nguonc.com", html, "text/html", "UTF-8", null)
                    } else {
                        webView.loadUrl(url)
                    }
                }
            },
            onRelease = { webView ->
                try {
                    webView.stopLoading()
                    webView.loadUrl("about:blank")
                    webView.clearHistory()
                    webView.removeAllViews()
                    webView.onPause()
                    webView.destroy()
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            },
            modifier = Modifier.fillMaxSize()
        )

        if (isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.5f)),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
            }
        }
    }
}
