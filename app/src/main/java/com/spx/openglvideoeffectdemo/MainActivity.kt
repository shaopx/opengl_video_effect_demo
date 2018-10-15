package com.spx.openglvideoeffectdemo

import android.app.Activity
import android.graphics.SurfaceTexture
import android.media.MediaPlayer
import android.opengl.GLSurfaceView
import android.os.Bundle
import android.util.Log
import android.view.Surface
import android.widget.SeekBar
import kotlinx.android.synthetic.main.activity_main.*
import java.io.IOException

class MainActivity : Activity(), MyRenderer.RenderCallback {
    var TAG = "MainActivity"
    lateinit var mediaPlayer: MediaPlayer
    lateinit var myRenderer: MyRenderer
    lateinit var surfaceView: GLSurfaceView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        mediaPlayer = MediaPlayer()

        try {
            resources.openRawResourceFd(R.raw.demo).apply {
                mediaPlayer.setDataSource(
                        fileDescriptor, startOffset, length)
                close()
            }


        } catch (e: Exception) {
            Log.e(TAG, e.message, e)
        }

        surfaceView = findViewById(R.id.glsurfaceview)
        surfaceView.setEGLContextClientVersion(2)

        myRenderer = MyRenderer()
        myRenderer.renderCallback = this


        //顶点着色器脚本R.raw.vertex
        myRenderer.vertexShaderSource = ShaderUtil.loadFromInputStream(resources.openRawResource(R.raw.vertex))
        //片元着色器脚本:R.raw.fragment
        myRenderer.fragmentShaderSource = ShaderUtil.loadFromInputStream(resources.openRawResource(R.raw.fragment))


        surfaceView.setRenderer(myRenderer)
        myRenderer.mSurfaceView = surfaceView


        /**
         * 这是一个滑动条百分比的实时处理, 也就是页面上饱和度的值设置给render对象,
         * 由于我们是自动更新页面(RENDERMODE_CONTINUOUSLY), 所以这个饱和度值会自动体现在图片上
         */
        saturation_seakbar.max = 200
        saturation_seakbar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                myRenderer.saturationF = progress * 1f / 100f
                saturation_tv.text = "饱和度:${progress}%"
                Log.d(TAG, "onProgressChanged  progress:${myRenderer.saturationF}")
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })
    }

    override fun onRenderCreated(surfaceTexture: SurfaceTexture, width: Int, height: Int) {
        Log.d(TAG, "onRenderCreated ....width:$width, height:$height")
        val surface = Surface(surfaceTexture)
        mediaPlayer.setSurface(surface)
        surface.release()

        try {
            mediaPlayer.setOnPreparedListener {
                Log.d(TAG, "on prepared ....")
                mediaPlayer.start()
            }
            mediaPlayer.prepare()
        } catch (t: IOException) {
            Log.e(TAG, "media player prepare failed")
        }

    }
}
