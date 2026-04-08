// Copyright (c) 2026, Alexander Suvorov. All rights reserved.
package com.example.smart_guitar_tuner_android

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.textfield.TextInputEditText

class AddTuningActivity : AppCompatActivity() {

    private lateinit var toolbar: MaterialToolbar
    private lateinit var etTuningName: TextInputEditText
    private lateinit var btnSave: Button
    private lateinit var btnCancel: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        window.decorView.systemUiVisibility = (
                View.SYSTEM_UI_FLAG_FULLSCREEN or
                        View.SYSTEM_UI_FLAG_HIDE_NAVIGATION or
                        View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                )
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_tuning)

        toolbar = findViewById(R.id.toolbar)
        etTuningName = findViewById(R.id.etTuningName)
        btnSave = findViewById(R.id.btnSave)
        btnCancel = findViewById(R.id.btnCancel)

        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        btnSave.setOnClickListener {
            val tuningName = etTuningName.text.toString().trim()
            if (tuningName.isNotEmpty()) {
                if (SettingsManager.addSetting(this, tuningName)) {
                    val intent = Intent(this, StringTunerActivity::class.java)
                    intent.putExtra("tuning_name", tuningName)
                    intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK
                    startActivity(intent)
                    finish()
                } else {
                    Toast.makeText(this, "Tuning with this name already exists", Toast.LENGTH_SHORT).show()
                }
            } else {
                Toast.makeText(this, "Enter tuning name", Toast.LENGTH_SHORT).show()
            }
        }

        btnCancel.setOnClickListener {
            finish()
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }
}