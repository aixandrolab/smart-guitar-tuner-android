// Copyright (c) 2026, Alexander Suvorov. All rights reserved.
package com.example.smart_guitar_tuner_android

import android.content.Intent
import android.media.MediaPlayer
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import android.view.animation.AlphaAnimation
import android.view.animation.Animation
import android.view.animation.AnimationSet
import android.view.animation.ScaleAnimation
import android.widget.ImageView
import android.widget.ProgressBar
import androidx.appcompat.app.AppCompatActivity

class SplashActivity : AppCompatActivity() {

    private lateinit var ivLogo: ImageView
    private lateinit var progressBar: ProgressBar
    private var mediaPlayer: MediaPlayer? = null

    companion object {
        private const val SPLASH_DURATION = 3000L
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        window.decorView.systemUiVisibility = (
                View.SYSTEM_UI_FLAG_FULLSCREEN or
                        View.SYSTEM_UI_FLAG_HIDE_NAVIGATION or
                        View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                )
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)

        ivLogo = findViewById(R.id.ivLogo)
        progressBar = findViewById(R.id.progressBar)

        animateLogo()

        playStartSound()

        Handler(Looper.getMainLooper()).postDelayed({
            navigateToMain()
        }, SPLASH_DURATION)
    }

    private fun animateLogo() {
        val scaleAnimation = ScaleAnimation(
            0.5f, 1.0f,
            0.5f, 1.0f,
            Animation.RELATIVE_TO_SELF, 0.5f,
            Animation.RELATIVE_TO_SELF, 0.5f
        )
        scaleAnimation.duration = 1000
        scaleAnimation.fillAfter = true

        val fadeInAnimation = AlphaAnimation(0.0f, 1.0f)
        fadeInAnimation.duration = 800
        fadeInAnimation.fillAfter = true

        val animationSet = AnimationSet(true)
        animationSet.addAnimation(scaleAnimation)
        animationSet.addAnimation(fadeInAnimation)

        ivLogo.startAnimation(animationSet)
    }

    private fun playStartSound() {
        try {
            val audioFile = assets.openFd("start.wav")
            mediaPlayer = MediaPlayer().apply {
                setDataSource(audioFile.fileDescriptor, audioFile.startOffset, audioFile.length)
                prepare()
                start()
                setOnCompletionListener {
                    release()
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun navigateToMain() {
        releaseMediaPlayer()
        val intent = Intent(this, MainActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }

    private fun releaseMediaPlayer() {
        mediaPlayer?.let {
            if (it.isPlaying) {
                it.stop()
            }
            it.release()
            mediaPlayer = null
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        releaseMediaPlayer()
    }
}