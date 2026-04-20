// Copyright (c) 2026, Alexander Suvorov. All rights reserved.
package com.example.smart_guitar_tuner_android

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.media.AudioFormat
import android.media.AudioRecord
import android.media.MediaRecorder
import android.os.Build
import android.os.Bundle
import android.os.CountDownTimer
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.RequiresPermission
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.snackbar.Snackbar
import java.io.File
import java.io.FileOutputStream
import java.io.IOException

class RecordingActivity : AppCompatActivity() {

    private lateinit var toolbar: MaterialToolbar
    private lateinit var btnStartRecord: Button
    private lateinit var timerText: TextView
    private lateinit var statusText: TextView
    private lateinit var waveformCard: CardView
    private lateinit var recordingIndicator: View

    private lateinit var stringInfoText: TextView

    private var countDownTimer: CountDownTimer? = null
    private var recordingThread: Thread? = null
    private var isRecording = false
    private var recordingSeconds = 0
    private var audioRecord: AudioRecord? = null
    private var pcmFile: File? = null
    private var wavFile: File? = null

    private var tuningName: String = "Default"
    private var stringIndex: Int = 0

    companion object {
        private const val PERMISSION_REQUEST_CODE = 200
        private const val SAMPLE_RATE = 44100
        private const val CHANNEL_CONFIG = AudioFormat.CHANNEL_IN_MONO
        private const val AUDIO_FORMAT = AudioFormat.ENCODING_PCM_16BIT
        private const val TAG = "RecordingActivity"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        window.decorView.systemUiVisibility = (
                View.SYSTEM_UI_FLAG_FULLSCREEN or
                        View.SYSTEM_UI_FLAG_HIDE_NAVIGATION or
                        View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                )
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_recording)

        tuningName = intent.getStringExtra("tuning_name") ?: "Default"
        stringIndex = intent.getIntExtra("string_index", 0)

        initViews()
        setupListeners()
        checkAndRequestPermissions()
    }

    private fun initViews() {
        toolbar = findViewById(R.id.toolbar)
        btnStartRecord = findViewById(R.id.btnStartRecord)
        timerText = findViewById(R.id.timerText)
        statusText = findViewById(R.id.statusText)
        waveformCard = findViewById(R.id.waveformCard)
        recordingIndicator = findViewById(R.id.recordingIndicator)
        stringInfoText = findViewById(R.id.stringInfoText)

        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        displayStringInfo()
    }

    private fun displayStringInfo() {
        val stringNumber = stringIndex + 1
        val stringName = when (stringNumber) {
            1 -> "1st String"
            2 -> "2nd String"
            3 -> "3rd String"
            4 -> "4th String"
            5 -> "5th String"
            6 -> "6th String"
            else -> "String $stringNumber"
        }

        stringInfoText.text = "Recording: $stringName"

        supportActionBar?.subtitle = "$tuningName Tuning - $stringName"
    }

    private fun setupListeners() {
        toolbar.setNavigationOnClickListener {
            finish()
        }

        btnStartRecord.setOnClickListener {
            if (!isRecording && hasRecordPermission()) {
                btnStartRecord.isEnabled = false
                btnStartRecord.alpha = 0.5f
                btnStartRecord.text = "Recording..."
                startCountdown()
            } else if (!hasRecordPermission()) {
                checkAndRequestPermissions()
            }
        }
    }

    private fun resetRecordButton() {
        btnStartRecord.isEnabled = true
        btnStartRecord.alpha = 1.0f
        btnStartRecord.text = "Start Recording"
    }

    private fun checkAndRequestPermissions() {
        val permissions = mutableListOf<String>()
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO)
            != PackageManager.PERMISSION_GRANTED) {
            permissions.add(Manifest.permission.RECORD_AUDIO)
        }
        if (permissions.isNotEmpty()) {
            ActivityCompat.requestPermissions(this, permissions.toTypedArray(), PERMISSION_REQUEST_CODE)
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == PERMISSION_REQUEST_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "Recording permission granted", Toast.LENGTH_SHORT).show()
                btnStartRecord.isEnabled = true
            } else {
                Toast.makeText(this, "Record audio permission needed", Toast.LENGTH_LONG).show()
                btnStartRecord.isEnabled = false
            }
        }
    }

    private fun hasRecordPermission(): Boolean {
        return ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED
    }

    private fun startCountdown() {
        statusText.text = "Preparing to record ${getStringName()}..."
        statusText.visibility = View.VISIBLE

        countDownTimer = object : CountDownTimer(4000, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                val secondsLeft = (millisUntilFinished / 1000).toInt()
                timerText.text = secondsLeft.toString()
                timerText.visibility = View.VISIBLE
                when (secondsLeft) {
                    3 -> statusText.text = "Get ready..."
                    2 -> statusText.text = "Attention..."
                    1 -> statusText.text = "Start!"
                }
            }
            @RequiresPermission(Manifest.permission.RECORD_AUDIO)
            override fun onFinish() {
                timerText.visibility = View.GONE
                startRecording()
            }
        }.start()
    }

    private fun getStringName(): String {
        val stringNumber = stringIndex + 1
        return when (stringNumber) {
            1 -> "1st String"
            2 -> "2nd String"
            3 -> "3rd String"
            4 -> "4th String"
            5 -> "5th String"
            6 -> "6th String"
            else -> "string $stringNumber"
        }
    }

    @RequiresPermission(Manifest.permission.RECORD_AUDIO)
    private fun startRecording() {
        try {
            setupAudioRecord()
            setupOutputFiles()

            if (audioRecord?.state != AudioRecord.STATE_INITIALIZED) {
                throw IOException("AudioRecord not initialized properly")
            }

            audioRecord?.startRecording()

            Thread.sleep(50)

            if (audioRecord?.recordingState != AudioRecord.RECORDSTATE_RECORDING) {
                throw IOException("Failed to start recording")
            }

            isRecording = true
            recordingSeconds = 0
            statusText.text = "Recording ${getStringName()}... 0/6 sec"
            recordingIndicator.visibility = View.VISIBLE

            recordingThread = Thread {
                writeAudioDataToFile()
            }.apply { start() }

            val handler = Handler(Looper.getMainLooper())
            handler.post(object : Runnable {
                override fun run() {
                    recordingSeconds++
                    if (recordingSeconds <= 6 && isRecording) {
                        statusText.text = "Recording ${getStringName()}... $recordingSeconds/6 sec"
                        updateWaveformLevel()
                        handler.postDelayed(this, 1000)
                    } else if (recordingSeconds > 6) {
                        finishRecording()
                    }
                }
            })

        } catch (e: Exception) {
            Log.e(TAG, "Error starting recording", e)
            e.printStackTrace()
            Toast.makeText(this, "Recording error: ${e.message}", Toast.LENGTH_SHORT).show()
            finishRecording()
        }
    }

    @RequiresPermission(Manifest.permission.RECORD_AUDIO)
    private fun setupAudioRecord() {
        var bufferSize = AudioRecord.getMinBufferSize(SAMPLE_RATE, CHANNEL_CONFIG, AUDIO_FORMAT)

        if (bufferSize == AudioRecord.ERROR_BAD_VALUE || bufferSize == AudioRecord.ERROR) {
            throw IOException("Invalid audio parameters")
        }

        bufferSize *= 4

        Log.d(TAG, "Buffer size: $bufferSize bytes")

        val audioSource = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            MediaRecorder.AudioSource.UNPROCESSED
        } else {
            MediaRecorder.AudioSource.MIC
        }

        audioRecord = AudioRecord(
            audioSource,
            SAMPLE_RATE,
            CHANNEL_CONFIG,
            AUDIO_FORMAT,
            bufferSize
        )

        if (audioRecord?.state != AudioRecord.STATE_INITIALIZED) {
            throw IOException("Failed to initialize AudioRecord")
        }

        Log.d(TAG, "AudioRecord initialized successfully")
    }

    private fun setupOutputFiles() {
        val settingsDir = SettingsManager.getSettingDirectory(this, tuningName)
        if (!settingsDir.exists()) settingsDir.mkdirs()

        pcmFile = File(settingsDir, "string_${stringIndex + 1}.pcm").apply { if (exists()) delete() }
        wavFile = File(settingsDir, "string_${stringIndex + 1}.wav").apply { if (exists()) delete() }

        Log.d(TAG, "Output files: ${pcmFile?.absolutePath}, ${wavFile?.absolutePath}")
    }

    private fun writeAudioDataToFile() {
        val bufferSize = AudioRecord.getMinBufferSize(SAMPLE_RATE, CHANNEL_CONFIG, AUDIO_FORMAT)
        if (bufferSize <= 0) {
            Log.e(TAG, "Invalid buffer size: $bufferSize")
            return
        }

        val readBufferSize = bufferSize * 4
        val audioData = ShortArray(readBufferSize / 2)
        val buffer = ByteArray(readBufferSize)

        Log.d(TAG, "Read buffer size: $readBufferSize bytes, ShortArray size: ${audioData.size}")

        FileOutputStream(pcmFile).use { fos ->
            var totalSamples = 0
            val targetSamples = SAMPLE_RATE * 6
            var errorCount = 0
            var lastProgressTime = System.currentTimeMillis()

            Log.d(TAG, "Starting recording loop, target samples: $targetSamples")

            while (isRecording && totalSamples < targetSamples && errorCount < 5) {
                val readSize = audioRecord?.read(audioData, 0, audioData.size) ?: -1

                when {
                    readSize > 0 -> {
                        for (i in 0 until readSize) {
                            val sample = audioData[i]
                            buffer[i * 2] = (sample.toInt() and 0xFF).toByte()
                            buffer[i * 2 + 1] = ((sample.toInt() shr 8) and 0xFF).toByte()
                        }
                        fos.write(buffer, 0, readSize * 2)
                        totalSamples += readSize
                        errorCount = 0

                        val currentTime = System.currentTimeMillis()
                        if (currentTime - lastProgressTime > 1000) {
                            val secondsRecorded = totalSamples / SAMPLE_RATE
                            Log.d(TAG, "Recording progress: $secondsRecorded/6 seconds, samples: $totalSamples")
                            lastProgressTime = currentTime
                        }
                    }
                    readSize == AudioRecord.ERROR_INVALID_OPERATION -> {
                        Log.e(TAG, "AudioRecord.ERROR_INVALID_OPERATION")
                        errorCount++
                        Thread.sleep(10)
                    }
                    readSize == AudioRecord.ERROR_BAD_VALUE -> {
                        Log.e(TAG, "AudioRecord.ERROR_BAD_VALUE")
                        errorCount++
                        Thread.sleep(10)
                    }
                    readSize == AudioRecord.ERROR_DEAD_OBJECT -> {
                        Log.e(TAG, "AudioRecord.ERROR_DEAD_OBJECT")
                        errorCount++
                        Thread.sleep(50)
                    }
                    else -> {
                        if (readSize < 0) {
                            Log.e(TAG, "Unknown AudioRecord error: $readSize")
                            errorCount++
                            Thread.sleep(10)
                        }
                    }
                }
            }

            val finalSeconds = totalSamples / SAMPLE_RATE
            Log.d(TAG, "Recording finished. Total samples: $totalSamples, Seconds: $finalSeconds/6, Errors: $errorCount")

            if (totalSamples < targetSamples / 2) {
                Log.w(TAG, "Recording may be incomplete: only ${totalSamples / SAMPLE_RATE} seconds recorded")
            }
        }
    }

    private fun updateWaveformLevel() {
        val randomHeight = (80..250).random()
        waveformCard.cardElevation = randomHeight / 100f
    }

    private fun finishRecording() {
        Log.d(TAG, "Finishing recording...")
        isRecording = false

        try {
            recordingThread?.join(2000)
        } catch (e: InterruptedException) {
            Log.e(TAG, "Interrupted while waiting for recording thread", e)
        }

        audioRecord?.let {
            try {
                if (it.recordingState == AudioRecord.RECORDSTATE_RECORDING) {
                    it.stop()
                }
                it.release()
            } catch (e: Exception) {
                Log.e(TAG, "Error releasing AudioRecord", e)
            }
        }
        audioRecord = null
        recordingIndicator.visibility = View.GONE

        if (pcmFile != null && pcmFile!!.exists() && pcmFile!!.length() > 0) {
            val fileSizeBytes = pcmFile!!.length()
            Log.d(TAG, "PCM file size: $fileSizeBytes bytes")

            if (fileSizeBytes > 1024) {
                convertPcmToWav()
                statusText.text = "Recording complete!"
                val fileSizeKB = wavFile?.length()?.div(1024) ?: 0

                val resultIntent = Intent()
                resultIntent.putExtra("string_index", stringIndex)
                resultIntent.putExtra("file_saved", true)
                setResult(RESULT_OK, resultIntent)

                Snackbar.make(findViewById(android.R.id.content),
                    "Recording saved (${fileSizeKB} KB, pure sound)", Snackbar.LENGTH_LONG)
                    .setAction("OK") {
                        resetRecordButton()
                        finish()
                    }
                    .show()
            } else {
                statusText.text = "Recording failed - file too small!"
                Toast.makeText(this, "Recording file is too small (${fileSizeBytes} bytes)", Toast.LENGTH_LONG).show()
                setResult(RESULT_CANCELED)
                resetRecordButton()
            }
        } else {
            statusText.text = "Recording failed!"
            Toast.makeText(this, "Failed to save audio - no data recorded", Toast.LENGTH_LONG).show()
            setResult(RESULT_CANCELED)
            resetRecordButton()
        }

        pcmFile?.delete()

        Handler(Looper.getMainLooper()).postDelayed({
            if (!isFinishing) {
                resetRecordButton()
                finish()
            }
        }, 3000)
    }

    private fun convertPcmToWav() {
        val pcmData = pcmFile?.readBytes()
        if (pcmData == null || pcmData.isEmpty()) {
            Log.e(TAG, "PCM data is null or empty")
            return
        }

        Log.d(TAG, "Converting PCM to WAV, PCM size: ${pcmData.size} bytes")

        val totalDataLen = pcmData.size
        val totalFileLen = totalDataLen + 36
        val byteRate = SAMPLE_RATE * 2
        val blockAlign = 2.toShort()
        val bitsPerSample = 16

        val header = ByteArray(44)

        "RIFF".toByteArray().copyInto(header, 0)
        writeIntToByteArray(header, 4, totalFileLen)
        "WAVE".toByteArray().copyInto(header, 8)

        "fmt ".toByteArray().copyInto(header, 12)
        writeIntToByteArray(header, 16, 16)
        writeShortToByteArray(header, 20, 1)
        writeShortToByteArray(header, 22, 1)
        writeIntToByteArray(header, 24, SAMPLE_RATE)
        writeIntToByteArray(header, 28, byteRate)
        writeShortToByteArray(header, 32, blockAlign)
        writeShortToByteArray(header, 34, bitsPerSample)

        "data".toByteArray().copyInto(header, 36)
        writeIntToByteArray(header, 40, totalDataLen)

        FileOutputStream(wavFile).use { fos ->
            fos.write(header)
            fos.write(pcmData)
        }

        Log.d(TAG, "WAV file created successfully, size: ${wavFile?.length()} bytes")
    }

    private fun writeIntToByteArray(array: ByteArray, offset: Int, value: Int) {
        array[offset] = (value and 0xFF).toByte()
        array[offset + 1] = ((value shr 8) and 0xFF).toByte()
        array[offset + 2] = ((value shr 16) and 0xFF).toByte()
        array[offset + 3] = ((value shr 24) and 0xFF).toByte()
    }

    private fun writeShortToByteArray(array: ByteArray, offset: Int, value: Short) {
        array[offset] = (value.toInt() and 0xFF).toByte()
        array[offset + 1] = ((value.toInt() shr 8) and 0xFF).toByte()
    }

    private fun writeShortToByteArray(array: ByteArray, offset: Int, value: Int) {
        writeShortToByteArray(array, offset, value.toShort())
    }

    override fun onDestroy() {
        Log.d(TAG, "onDestroy called")
        super.onDestroy()
        countDownTimer?.cancel()
        isRecording = false
        recordingThread?.interrupt()
        try {
            recordingThread?.join(1000)
        } catch (e: InterruptedException) {
            Log.e(TAG, "Error waiting for thread to finish", e)
        }
        try {
            audioRecord?.let {
                if (it.recordingState == AudioRecord.RECORDSTATE_RECORDING) {
                    it.stop()
                }
                it.release()
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error releasing AudioRecord in onDestroy", e)
        }
        resetRecordButton()
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }
}