package lb.com.giflivewallpaper

import android.content.res.Configuration
import android.graphics.Canvas
import android.graphics.Movie
import android.os.Handler
import android.service.wallpaper.WallpaperService
import android.util.Log
import android.view.SurfaceHolder
import java.util.concurrent.atomic.AtomicBoolean


class GIFWallpaperService : WallpaperService() {
    var engine: GIFWallpaperEngine? = null

    override fun onCreateEngine(): WallpaperService.Engine {
        engine = GIFWallpaperEngine()
        return engine!!
    }

    override fun onCreate() {
        super.onCreate()
        Log.d("AppLog", "GIFWallpaperService onCreate " + this@GIFWallpaperService)
    }

//    override fun onDestroy() {
//        super.onDestroy()
//        Log.d("AppLog", "GIFWallpaperService onDestroy " + this@GIFWallpaperService)
//    }

    override fun onConfigurationChanged(newConfig: Configuration?) {
        super.onConfigurationChanged(newConfig)
        Log.d("AppLog", "onConfigurationChanged")
        engine?.onConfigurationChanged()
    }

//    override fun onUnbind(intent: Intent?): Boolean {
//        Log.d("AppLog", "onUnbind")
//        return super.onUnbind(intent)
//    }

    inner class GIFWallpaperEngine() : WallpaperService.Engine() {
        private val frameDuration = 1000L / 60L    // 20
        private var holder: SurfaceHolder? = null
        private val handler: Handler = Handler()
        private val drawGIF = Runnable { draw() }
        private var movie: Movie? = null
        private val loadingThread: Thread
        private var isShutDown=AtomicBoolean(false)

        init {
            loadingThread = object : Thread() {
                override fun run() {
                    super.run()
                    movie = Movie.decodeStream(resources.openRawResource(R.raw.fast))
                    if (isShutDown.get())
                        return
                    handler.post(drawGIF)
                }
            }
            loadingThread.start()

        }

        private fun draw() {
            if (!isVisible || movie == null||isShutDown.get())
                return
//            try {
            val canvas = holder!!.lockCanvas() ?: return
            canvas.save()
            drawFitCenter(canvas)
//            drawCenterCrop(canvas)
            canvas.restore()
            holder!!.unlockCanvasAndPost(canvas)
            movie!!.setTime((System.currentTimeMillis() % movie!!.duration()).toInt())
            handler.removeCallbacks(drawGIF)
            handler.postDelayed(drawGIF, frameDuration)
//            } catch (e: IllegalArgumentException) {
//                Log.d("AppLog", "exception:" + e)
//            }
        }

        private fun drawFitCenter(canvas: Canvas) {
            val scale = Math.min(canvas.width.toFloat() / movie!!.width().toFloat(), canvas.height.toFloat() / movie!!.height().toFloat());
            val x = (canvas.width.toFloat() / 2f) - (movie!!.width().toFloat() / 2f) * scale
            val y = (canvas.height.toFloat() / 2f) - (movie!!.height().toFloat() / 2f) * scale
            canvas.translate(x, y)
            canvas.scale(scale, scale)
            movie!!.draw(canvas, 0f, 0f)
        }

        private fun drawCenterCrop(canvas: Canvas) {
            val scale = Math.max(canvas.width.toFloat() / movie!!.width().toFloat(), canvas.height.toFloat() / movie!!.height().toFloat());
            val x = (canvas.width.toFloat() / 2f) - (movie!!.width().toFloat() / 2f) * scale
            val y = (canvas.height.toFloat() / 2f) - (movie!!.height().toFloat() / 2f) * scale
            canvas.translate(x, y)
            canvas.scale(scale, scale)
            movie!!.draw(canvas, 0f, 0f)
        }

        override fun onOffsetsChanged(xOffset: Float, yOffset: Float, xOffsetStep: Float, yOffsetStep: Float, xPixelOffset: Int, yPixelOffset: Int) {
            super.onOffsetsChanged(xOffset, yOffset, xOffsetStep, yOffsetStep, xPixelOffset, yPixelOffset)
            if(isShutDown.get())
                return
            Log.d("AppLog", "onOffsetsChanged " + this@GIFWallpaperService)
            handler.removeCallbacks(drawGIF)
            drawGIF.run()
        }

//        override fun onSurfaceChanged(holder: SurfaceHolder?, format: Int, width: Int, height: Int) {
//            super.onSurfaceChanged(holder, format, width, height)
//            Log.d("AppLog", "onSurfaceChanged")
//            handler.removeCallbacks(drawGIF)
//            drawGIF.run()
//        }
//        override fun onDesiredSizeChanged(desiredWidth: Int, desiredHeight: Int) {
//            super.onDesiredSizeChanged(desiredWidth, desiredHeight)
//            Log.d("AppLog", "onDesiredSizeChanged")
//        }

//        override fun onSurfaceCreated(holder: SurfaceHolder?) {
//            super.onSurfaceCreated(holder)
//            Log.d("AppLog", "onSurfaceCreated")
//            handler.removeCallbacks(drawGIF)
//            drawGIF.run()
//        }


//        override fun onVisibilityChanged(visible: Boolean) {
//            super.onVisibilityChanged(visible)
//            Log.d("AppLog", "onVisibilityChanged:" + visible + this@GIFWallpaperService)
//            handler.removeCallbacks(drawGIF)
//            if (visible)
//                drawGIF.run()
//
//        }

        override fun onSurfaceDestroyed(holder: SurfaceHolder?) {
            super.onSurfaceDestroyed(holder)
            isShutDown.set(true)
//            loadingThread.interrupt()
            Log.d("AppLog", "onSurfaceDestroyed")
            handler.removeCallbacks(drawGIF)
        }

        override fun onDestroy() {
            super.onDestroy()
            isShutDown.set(true)
            Log.d("AppLog", "onDestroy")
//            this.visible = false
//            loadingThread.interrupt()
            handler.removeCallbacks(drawGIF)
        }

        override fun onCreate(surfaceHolder: SurfaceHolder) {
            super.onCreate(surfaceHolder)
            Log.d("AppLog", "onCreate " + this@GIFWallpaperService)
            this.holder = surfaceHolder
        }

        fun onConfigurationChanged() {
            isShutDown.set(true)
//            loadingThread.interrupt()
            handler.removeCallbacks(drawGIF)
        }

    }
}
