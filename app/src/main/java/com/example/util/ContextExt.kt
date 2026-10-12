package com.example.util

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper

/**
 * Walks up the [ContextWrapper] chain to find the hosting [Activity].
 *
 * [Context] returned from a composable (or a WebView factory) can be wrapped in one or more
 * [ContextWrapper]s, in which case a plain `context as? Activity` cast returns null and any
 * Activity-dependent work (like WebView fullscreen) silently does nothing.
 */
internal fun Context.findActivity(): Activity? {
    var current: Context? = this
    while (current is ContextWrapper) {
        if (current is Activity) return current
        current = current.baseContext
    }
    return null
}
