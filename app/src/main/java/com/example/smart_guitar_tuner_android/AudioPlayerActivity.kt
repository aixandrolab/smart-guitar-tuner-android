// Copyright (c) 2026, Alexander Suvorov. All rights reserved.
package com.example.smart_guitar_tuner_android

import android.content.res.AssetFileDescriptor
import android.media.MediaPlayer
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import android.widget.ImageButton
import android.widget.SeekBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.floatingactionbutton.FloatingActionButton
import java.io.File
import java.io.IOException

class AudioPlayerActivity : AppCompatActivity() {

    private lateinit var toolbar: MaterialToolbar
    private lateinit var btnStop: ImageButton
    private lateinit var fabPlay: FloatingActionButton
    private lateinit var seekBar: SeekBar
    private lateinit var songDuration: TextView
    private lateinit var currentTime: TextView
    private lateinit var btnLoop: ImageButton
    private lateinit var tvStringName: TextView

    private var isPlaying = false
    private val handler = Handler(Looper.getMainLooper())
    private var isSeeking = false
    private var mediaPlayer: MediaPlayer? = null
    private var totalDuration = 30000
    private var currentProgress = 0
    private var isLooping = false

    private var tuningName: String = "Default"
    private var stringIndex: Int = 0

    private val stringNames = listOf("String 1", "String 2", "String 3", "String 4", "String 5", "String 6")

    override fun onCreate(savedInstanceState: Bundle?) {
        window.decorView.systemUiVisibility = (
                View.SYSTEM_UI_FLAG_FULLSCREEN or
                        View.SYSTEM_UI_FLAG_HIDE_NAVIGATION or
                        View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                )
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_audio_player)

        tuningName = intent.getStringExtra("tuning_name") ?: "Default"
        stringIndex = intent.getIntExtra("string_index", 0)

        initViews()
        setupListeners()
        loadAudio()
        updateStringInfo()
        updateStopButtonState()
    }

    private fun initViews() {
        toolbar = findViewById(R.id.toolbar)
        btnStop = findViewById(R.id.btnStop)
        fabPlay = findViewById(R.id.fabPlay)
        seekBar = findViewById(R.id.seekBar)
        songDuration = findViewById(R.id.songDuration)
        currentTime = findViewById(R.id.currentTime)
        btnLoop = findViewById(R.id.btnLoop)
        tvStringName = findViewById(R.id.tvStringName)

        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        btnLoop.setBackgroundColor(ContextCompat.getColor(this, R.color.action_warning))

        btnStop.isEnabled = false
        btnStop.alpha = 0.5f
    }

    private fun updateStopButtonState() {
        btnStop.isEnabled = isPlaying
        btnStop.alpha = if (isPlaying) 1.0f else 0.5f
    }

    private fun updateStringInfo() {
        tvStringName.text = stringNames[stringIndex]
    }

    private fun setupListeners() {
        toolbar.setNavigationOnClickListener {
            stopPlayback()
            releaseMediaPlayer()
            finish()
        }

        btnStop.setOnClickListener {
            stopPlayback()
            fabPlay.setImageResource(R.drawable.ic_play_arrow)
            updateStopButtonState()
        }

        btnLoop.setOnClickListener {
            isLooping = !isLooping
            if (isLooping) {
                btnLoop.setBackgroundColor(ContextCompat.getColor(this, R.color.action_success))
                btnLoop.setImageResource(R.drawable.ic_repeat_one)
                Toast.makeText(this, "Loop mode: ON", Toast.LENGTH_SHORT).show()
            } else {
                btnLoop.setBackgroundColor(ContextCompat.getColor(this, R.color.action_warning))
                btnLoop.setImageResource(R.drawable.ic_repeat)
                Toast.makeText(this, "Loop mode: OFF", Toast.LENGTH_SHORT).show()
            }

            mediaPlayer?.isLooping = isLooping
        }

        fabPlay.setOnClickListener {
            if (isPlaying) {
                pausePlayback()
            } else {
                startPlayback()
            }
        }

        seekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                if (fromUser && !isSeeking) {
                    currentTime.text = formatTime(progress)
                    currentProgress = progress
                    mediaPlayer?.seekTo(progress)
                }
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {
                isSeeking = true
            }

            override fun onStopTrackingTouch(seekBar: SeekBar?) {
                isSeeking = false
                currentProgress = seekBar?.progress ?: 0
                if (isPlaying) {
                    handler.removeCallbacks(updateSeekBarRunnable)
                    updateSeekBar()
                }
            }
        })
    }

    private fun loadAudio() {
        if (tuningName == "Default") {
            loadFromAssets()
        } else {
            loadFromFile()
        }
    }

    private fun loadFromAssets() {
        try {
            val fileName = "${stringIndex + 1}_string.wav"
            val afd: AssetFileDescriptor = assets.openFd(fileName)

            mediaPlayer = MediaPlayer().apply {
                setDataSource(afd.fileDescriptor, afd.startOffset, afd.length)
                prepare()
                isLooping = isLooping
                setOnCompletionListener {
                    handlePlaybackComplete()
                }
            }

            totalDuration = mediaPlayer?.duration ?: 30000
            seekBar.max = totalDuration
            songDuration.text = formatTime(totalDuration)
            currentProgress = 0
            seekBar.progress = 0
            currentTime.text = formatTime(0)

            Toast.makeText(this, "Loaded default sound for ${stringNames[stringIndex]}", Toast.LENGTH_SHORT).show()
        } catch (e: IOException) {
            e.printStackTrace()
            Toast.makeText(this, "Failed to load default sound: ${e.message}", Toast.LENGTH_SHORT).show()
            useDemoMode()
        }
    }

    private fun loadFromFile() {
        val settingsDir = SettingsManager.getSettingDirectory(this, tuningName)
        val audioFile = File(settingsDir, "string_${stringIndex + 1}.wav")

        if (audioFile.exists()) {
            try {
                if (audioFile.length() < 44) {
                    throw IOException("Audio file is too small or corrupted")
                }

                mediaPlayer = MediaPlayer().apply {
                    setDataSource(audioFile.absolutePath)
                    prepare()
                    isLooping = isLooping
                    setOnCompletionListener {
                        handlePlaybackComplete()
                    }
                }

                totalDuration = mediaPlayer?.duration ?: 30000
                if (totalDuration <= 0) totalDuration = 30000

                seekBar.max = totalDuration
                songDuration.text = formatTime(totalDuration)
                currentProgress = 0
                seekBar.progress = 0
                currentTime.text = formatTime(0)

                val fileSizeKB = audioFile.length() / 1024
                Toast.makeText(this, "Loaded recording for ${stringNames[stringIndex]} (${fileSizeKB} KB)", Toast.LENGTH_LONG).show()
            } catch (e: Exception) {
                e.printStackTrace()
                Toast.makeText(this, "Error loading audio: ${e.message}", Toast.LENGTH_SHORT).show()
                useDemoMode()
            }
        } else {
            Toast.makeText(this, "No recording found for ${stringNames[stringIndex]}", Toast.LENGTH_LONG).show()
            useDemoMode()
        }
    }

    private fun handlePlaybackComplete() {
        if (!isLooping) {
            isPlaying = false
            fabPlay.setImageResource(R.drawable.ic_play_arrow)
            updateStopButtonState()
            currentProgress = 0
            seekBar.progress = 0
            currentTime.text = formatTime(0)
            Toast.makeText(this, "Playback complete", Toast.LENGTH_SHORT).show()
        } else if (isLooping) {
            currentProgress = 0
            seekBar.progress = 0
            startPlayback()
        }
    }

    private fun useDemoMode() {
        releaseMediaPlayer()
        mediaPlayer = null
        totalDuration = 30000
        seekBar.max = totalDuration
        songDuration.text = formatTime(totalDuration)
        currentProgress = 0
        seekBar.progress = 0
        currentTime.text = formatTime(0)
    }

    private fun startPlayback() {
        if (mediaPlayer == null) {
            startDemoPlayback()
            return
        }

        mediaPlayer?.let { mp ->
            try {
                mp.seekTo(currentProgress)
                mp.start()
                isPlaying = true
                fabPlay.setImageResource(R.drawable.ic_pause)
                updateStopButtonState()
                updateSeekBar()
            } catch (e: Exception) {
                e.printStackTrace()
                Toast.makeText(this, "Playback error: ${e.message}", Toast.LENGTH_SHORT).show()
                useDemoMode()
                startDemoPlayback()
            }
        }
    }

    private fun startDemoPlayback() {
        isPlaying = true
        fabPlay.setImageResource(R.drawable.ic_pause)
        updateStopButtonState()
        updateSeekBar()

        handler.postDelayed({
            if (isPlaying && currentProgress >= totalDuration) {
                if (isLooping) {
                    currentProgress = 0
                    startDemoPlayback()
                } else {
                    stopPlayback()
                    fabPlay.setImageResource(R.drawable.ic_play_arrow)
                    updateStopButtonState()
                    Toast.makeText(this, "Demo playback complete", Toast.LENGTH_SHORT).show()
                }
            }
        }, 1000)
    }

    private fun pausePlayback() {
        if (mediaPlayer?.isPlaying == true) {
            mediaPlayer?.pause()
        }
        isPlaying = false
        fabPlay.setImageResource(R.drawable.ic_play_arrow)
        updateStopButtonState()
        handler.removeCallbacks(updateSeekBarRunnable)
    }

    private fun stopPlayback() {
        mediaPlayer?.let {
            if (it.isPlaying) {
                it.pause()
            }
            it.seekTo(0)
        }
        isPlaying = false
        currentProgress = 0
        seekBar.progress = 0
        currentTime.text = formatTime(0)
        fabPlay.setImageResource(R.drawable.ic_play_arrow)
        updateStopButtonState()
        handler.removeCallbacks(updateSeekBarRunnable)
    }

    private fun releaseMediaPlayer() {
        mediaPlayer?.release()
        mediaPlayer = null
    }

    private val updateSeekBarRunnable = object : Runnable {
        override fun run() {
            if (isPlaying && !isSeeking) {
                if (mediaPlayer != null && mediaPlayer?.isPlaying == true) {
                    currentProgress = mediaPlayer?.currentPosition ?: 0
                } else if (mediaPlayer == null) {
                    currentProgress += 1000
                    if (isLooping && currentProgress >= totalDuration) {
                        currentProgress = 0
                    }
                }

                if (currentProgress <= totalDuration) {
                    seekBar.progress = currentProgress
                    currentTime.text = formatTime(currentProgress)
                    handler.postDelayed(this, 1000)
                } else if (!isLooping) {
                    stopPlayback()
                    fabPlay.setImageResource(R.drawable.ic_play_arrow)
                    updateStopButtonState()
                }
            }
        }
    }

    private fun updateSeekBar() {
        handler.post(updateSeekBarRunnable)
    }

    private fun formatTime(milliseconds: Int): String {
        val seconds = milliseconds / 1000
        val minutes = seconds / 60
        val remainingSeconds = seconds % 60
        return String.format("%02d:%02d", minutes, remainingSeconds)
    }

    override fun onDestroy() {
        super.onDestroy()
        handler.removeCallbacks(updateSeekBarRunnable)
        releaseMediaPlayer()
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }
}