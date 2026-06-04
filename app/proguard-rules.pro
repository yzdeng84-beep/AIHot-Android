# AIHOT ProGuard Rules
# Keep WebView JavaScript interface if any
-keepclassmembers class * {
    @android.webkit.JavascriptInterface <methods>;
}

# Keep the application class
-keep class com.virxact.aihot.AIAHotApp { *; }
