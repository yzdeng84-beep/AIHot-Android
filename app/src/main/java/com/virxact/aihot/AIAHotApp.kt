package com.virxact.aihot

import android.app.Application
import android.webkit.WebView

class AIAHotApp : Application() {

    override fun onCreate() {
        super.onCreate()
        // Pre-warm WebView to reduce cold-start latency
        // This creates the WebView engine in a background thread
        WebView(this).destroy()
    }

    override fun onTrimMemory(level: Int) {
        super.onTrimMemory(level)
        if (level >= TRIM_MEMORY_MODERATE) {
            // Clear WebView cache under memory pressure
            WebView(this).apply {
                clearCache(true)
                destroy()
            }
        }
    }
}
