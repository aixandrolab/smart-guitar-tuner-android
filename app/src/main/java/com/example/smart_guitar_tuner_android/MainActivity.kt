// Copyright (c) 2026, Alexander Suvorov. All rights reserved.
package com.example.smart_guitar_tuner_android

import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.Settings
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Button
import android.widget.ListView
import android.widget.PopupMenu
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.textfield.TextInputEditText

class MainActivity : AppCompatActivity() {

    private lateinit var toolbar: MaterialToolbar
    private lateinit var listView: ListView
    private lateinit var btnAdd: Button
    private lateinit var btnDelete: Button
    private lateinit var settingsList: MutableList<String>
    private lateinit var adapter: SettingsListAdapter

    companion object {
        private const val REQUEST_ADD_TUNING = 1
        private const val PERMISSION_REQUEST_STORAGE = 100
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        window.decorView.systemUiVisibility = (
                View.SYSTEM_UI_FLAG_FULLSCREEN or
                        View.SYSTEM_UI_FLAG_HIDE_NAVIGATION or
                        View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                )
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        toolbar = findViewById(R.id.toolbar)
        listView = findViewById(R.id.listView)
        btnAdd = findViewById(R.id.btnAdd)
        btnDelete = findViewById(R.id.btnDelete)

        setSupportActionBar(toolbar)

        checkAndRequestStoragePermission()

        btnAdd.setOnClickListener {
            val intent = Intent(this, AddTuningActivity::class.java)
            startActivityForResult(intent, REQUEST_ADD_TUNING)
        }

        btnDelete.visibility = View.GONE

        listView.setOnItemLongClickListener { _, view, position, _ ->
            val selectedTuning = settingsList[position]

            if (selectedTuning == "Default") {
                Toast.makeText(this, "Cannot modify Default tuning", Toast.LENGTH_SHORT).show()
                return@setOnItemLongClickListener true
            }

            showPopupMenu(view, selectedTuning, position)
            true
        }

        listView.setOnItemClickListener { _, _, position, _ ->
            val selectedItem = settingsList[position]
            val intent = Intent(this, StringTunerActivity::class.java)
            intent.putExtra("tuning_name", selectedItem)
            startActivity(intent)
        }
    }

    private fun checkAndRequestStoragePermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            if (!Environment.isExternalStorageManager()) {
                showStoragePermissionDialog()
            } else {
                loadSettings()
            }
        } else {
            if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this,
                    arrayOf(android.Manifest.permission.WRITE_EXTERNAL_STORAGE),
                    PERMISSION_REQUEST_STORAGE)
            } else {
                loadSettings()
            }
        }
    }

    private fun showStoragePermissionDialog() {
        MaterialAlertDialogBuilder(this)
            .setTitle("Storage Permission Required")
            .setMessage("To save your tunings permanently, the app needs access to storage. This allows your tunings to survive app updates and reinstallation.")
            .setPositiveButton("Grant Permission") { _, _ ->
                val intent = Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION)
                intent.data = Uri.parse("package:$packageName")
                startActivityForResult(intent, PERMISSION_REQUEST_STORAGE)
            }
            .setNegativeButton("Use Temporarily") { _, _ ->
                Toast.makeText(this,
                    "Warning: Your tunings may be lost after app update or reinstallation",
                    Toast.LENGTH_LONG).show()
                loadSettings()
            }
            .setCancelable(false)
            .show()
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == PERMISSION_REQUEST_STORAGE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "Storage permission granted", Toast.LENGTH_SHORT).show()
                loadSettings()
            } else {
                Toast.makeText(this,
                    "Storage permission denied. Tunings may be lost after update.",
                    Toast.LENGTH_LONG).show()
                loadSettings()
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == PERMISSION_REQUEST_STORAGE) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                if (Environment.isExternalStorageManager()) {
                    Toast.makeText(this, "Storage access granted", Toast.LENGTH_SHORT).show()
                    loadSettings()
                } else {
                    Toast.makeText(this,
                        "Storage access denied. Some features may not work properly.",
                        Toast.LENGTH_LONG).show()
                    loadSettings()
                }
            }
        } else if (requestCode == REQUEST_ADD_TUNING && resultCode == RESULT_OK) {
            loadSettings()
            Toast.makeText(this, "Tuning added", Toast.LENGTH_SHORT).show()
        }
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

    private fun showPopupMenu(anchorView: View, tuningName: String, position: Int) {
        val popupMenu = PopupMenu(this, anchorView)
        popupMenu.menu.apply {
            add(0, 1, 0, "Edit")
            add(0, 2, 1, "Delete")
        }

        popupMenu.setOnMenuItemClickListener { item ->
            when (item.itemId) {
                1 -> showRenameDialog(tuningName, position)
                2 -> showDeleteConfirmationDialog(tuningName, position)
            }
            true
        }

        popupMenu.show()
    }

    private fun showRenameDialog(oldName: String, position: Int) {
        val dialogView = layoutInflater.inflate(R.layout.dialog_rename_tuning, null)
        val etNewName = dialogView.findViewById<TextInputEditText>(R.id.etNewTuningName)
        etNewName.setText(oldName)
        etNewName.selectAll()

        MaterialAlertDialogBuilder(this)
            .setTitle("Rename Tuning")
            .setView(dialogView)
            .setPositiveButton("Save") { _, _ ->
                val newName = etNewName.text.toString().trim()
                if (newName.isNotEmpty()) {
                    if (newName == oldName) {
                        Toast.makeText(this, "Name unchanged", Toast.LENGTH_SHORT).show()
                    } else if (SettingsManager.renameSetting(this, oldName, newName)) {
                        loadSettings()
                        Toast.makeText(this, "Renamed to \"$newName\"", Toast.LENGTH_SHORT).show()
                    } else {
                        Toast.makeText(this, "Tuning with this name already exists", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    Toast.makeText(this, "Enter tuning name", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun showDeleteConfirmationDialog(tuningName: String, position: Int) {
        MaterialAlertDialogBuilder(this)
            .setTitle("Delete Tuning")
            .setMessage("Are you sure you want to delete \"$tuningName\"?")
            .setPositiveButton("Delete") { _, _ ->
                if (SettingsManager.deleteSetting(this, tuningName)) {
                    loadSettings()
                    Toast.makeText(this, "\"$tuningName\" deleted", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(this, "Failed to delete", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    override fun onResume() {
        super.onResume()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            if (Environment.isExternalStorageManager()) {
                loadSettings()
            }
        } else {
            if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.WRITE_EXTERNAL_STORAGE)
                == PackageManager.PERMISSION_GRANTED) {
                loadSettings()
            }
        }
    }

    private fun loadSettings() {
        settingsList = SettingsManager.getSettingsList(this)
        adapter = SettingsListAdapter(this, settingsList)
        listView.adapter = adapter
    }
}