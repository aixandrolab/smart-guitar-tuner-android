// Copyright (c) 2026, Alexander Suvorov. All rights reserved.
package com.example.smart_guitar_tuner_android

import android.content.Intent
import android.media.MediaPlayer
import android.net.Uri
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.appbar.MaterialToolbar

class AboutActivity : AppCompatActivity() {

    private lateinit var toolbar: MaterialToolbar
    private lateinit var btnGitHubRepo: Button
    private lateinit var btnGitHubAuthor: Button
    private var mediaPlayer: MediaPlayer? = null

    companion object {
        private const val GITHUB_USERNAME = "aixandrolab"
        private const val PROJECT_NAME = "smart-guitar-tuner-android"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        window.decorView.systemUiVisibility = (
                View.SYSTEM_UI_FLAG_FULLSCREEN or
                        View.SYSTEM_UI_FLAG_HIDE_NAVIGATION or
                        View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                )
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_about)

        toolbar = findViewById(R.id.toolbar)
        btnGitHubRepo = findViewById(R.id.btnGitHubRepo)
        btnGitHubAuthor = findViewById(R.id.btnGitHubAuthor)

        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        playAboutSound()

        btnGitHubRepo.setOnClickListener {
            val url = "https://github.com/$GITHUB_USERNAME/$PROJECT_NAME"
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
            startActivity(intent)
        }

        btnGitHubAuthor.setOnClickListener {
            val url = "https://github.com/$GITHUB_USERNAME"
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
            startActivity(intent)
        }
    }

    private fun playAboutSound() {
        try {
            val audioFile = assets.openFd("ab.wav")
            mediaPlayer = MediaPlayer().apply {
                setDataSource(audioFile.fileDescriptor, audioFile.startOffset, audioFile.length)
                prepare()
                start()
                setOnCompletionListener {
                    release()
                    mediaPlayer = null
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> {
                onBackPressed()
                true
            }
            R.id.action_help -> {
                startActivity(Intent(this, HelpActivity::class.java))
                true
            }
            R.id.action_about -> {
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }

    override fun onDestroy() {
        super.onDestroy()
        mediaPlayer?.let {
            if (it.isPlaying) {
                it.stop()
            }
            it.release()
            mediaPlayer = null
        }
    }
}