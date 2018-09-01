package lb.com.giflivewallpaper

import android.app.WallpaperManager
import android.content.ComponentName
import android.content.Intent
import android.graphics.Bitmap
import android.os.Build

fun Bitmap.toSizeString(): String {
    return "$width,$height"
}


object Utils {
    @JvmStatic
    fun prepareLiveWallpaperIntent(showAllLiveWallpapers: Boolean): Intent {
        val liveWallpaperIntent = Intent()
        if (showAllLiveWallpapers || Build.VERSION.SDK_INT <= Build.VERSION_CODES.ICE_CREAM_SANDWICH_MR1) {
            liveWallpaperIntent.action = WallpaperManager.ACTION_LIVE_WALLPAPER_CHOOSER
        } else {
            liveWallpaperIntent.action = WallpaperManager.ACTION_CHANGE_LIVE_WALLPAPER
            val p = GIFWallpaperService::class.java.`package`.name
            val c = GIFWallpaperService::class.java.canonicalName
            liveWallpaperIntent.putExtra(WallpaperManager.EXTRA_LIVE_WALLPAPER_COMPONENT, ComponentName(p, c))
        }
        return liveWallpaperIntent
    }
}
