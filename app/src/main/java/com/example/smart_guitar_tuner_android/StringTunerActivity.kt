// Copyright (c) 2026, Alexander Suvorov. All rights reserved.
package com.example.smart_guitar_tuner_android

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.appbar.MaterialToolbar
import java.io.File

class StringTunerActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: StringAdapter
    private lateinit var toolbar: MaterialToolbar
    private lateinit var tvStatistics: TextView
    private var tuningName: String = "Default"

    companion object {
        const val REQUEST_RECORDING = 100
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        window.decorView.systemUiVisibility = (
                View.SYSTEM_UI_FLAG_FULLSCREEN or
                        View.SYSTEM_UI_FLAG_HIDE_NAVIGATION or
                        View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                )
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_string_tuner)

        tuningName = intent.getStringExtra("tuning_name") ?: "Default"

        toolbar = findViewById(R.id.toolbar)
        tvStatistics = findViewById(R.id.tvStatistics)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = getString(R.string.string_tuner_title, tuningName)

        recyclerView = findViewById(R.id.recyclerViewStrings)
        recyclerView.layoutManager = LinearLayoutManager(this)

        setupAdapter()
    }

    private fun setupAdapter() {
        val stringsList = mutableListOf(
            StringItem("String 1", 1),
            StringItem("String 2", 2),
            StringItem("String 3", 3),
            StringItem("String 4", 4),
            StringItem("String 5", 5),
            StringItem("String 6", 6)
        )

        if (tuningName != "Default") {
            val settingsDir = SettingsManager.getSettingDirectory(this, tuningName)
            stringsList.forEachIndexed { index, stringItem ->
                val audioFile = File(settingsDir, "string_${index + 1}.wav")
                stringItem.isRecorded = audioFile.exists()
            }
        } else {
            stringsList.forEach { it.isRecorded = true }
        }

        adapter = StringAdapter(stringsList, tuningName)

        adapter.setOnStringsUpdateListener { recordedCount, totalCount ->
            tvStatistics.text = "Recorded: $recordedCount / $totalCount strings"
        }

        recyclerView.adapter = adapter

        val recordedCount = stringsList.count { it.isRecorded }
        tvStatistics.text = "Recorded: $recordedCount / ${stringsList.size} strings"
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.main_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_help -> {
                startActivity(Intent(this, HelpActivity::class.java))
                true
            }
            R.id.action_about -> {
                startActivity(Intent(this, AboutActivity::class.java))
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_RECORDING && resultCode == RESULT_OK) {
            val stringIndex = data?.getIntExtra("string_index", -1) ?: -1
            if (stringIndex != -1) {
                adapter.updateRecordedStatus(this)
            }
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }
}