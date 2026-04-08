// Copyright (c) 2026, Alexander Suvorov. All rights reserved.
package com.example.smart_guitar_tuner_android

import android.content.Context
import android.content.SharedPreferences
import android.os.Environment
import java.io.File

object SettingsManager {

    private const val PREFS_NAME = "tuner_prefs"
    private const val KEY_SETTINGS_LIST = "settings_list"

    fun getSettingsList(context: Context): MutableList<String> {
        val tunersDir = getTunersRootDirectory()
        val existingTunings = mutableListOf<String>()

        if (tunersDir.exists() && tunersDir.isDirectory) {
            tunersDir.listFiles()?.forEach { dir ->
                if (dir.isDirectory) {
                    existingTunings.add(dir.name.replace("_", " "))
                }
            }
        }

        if (!existingTunings.contains("Default")) {
            existingTunings.add(0, "Default")
        }

        saveSettingsList(context, existingTunings)

        return existingTunings.sorted().toMutableList()
    }

    private fun saveSettingsList(context: Context, settingsList: List<String>) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().putStringSet(KEY_SETTINGS_LIST, settingsList.toSet()).apply()
    }

    private fun isNameExistsCaseInsensitive(name: String): Boolean {
        val tunersDir = getTunersRootDirectory()
        if (!tunersDir.exists()) return false

        return tunersDir.listFiles()?.any { dir ->
            dir.isDirectory && dir.name.equals(name.replace(" ", "_"), ignoreCase = true)
        } ?: false
    }

    fun addSetting(context: Context, settingName: String): Boolean {
        if (isNameExistsCaseInsensitive(settingName)) {
            return false
        }

        val settingDir = getSettingDirectory(settingName)
        val success = settingDir.mkdirs()

        if (success) {
            val settingsList = getSettingsList(context)
            if (!settingsList.contains(settingName)) {
                settingsList.add(settingName)
                saveSettingsList(context, settingsList)
            }
        }

        return success
    }

    fun deleteSetting(context: Context, settingName: String): Boolean {
        if (settingName == "Default") return false

        val settingDir = getSettingDirectory(settingName)
        val deleted = deleteDirectory(settingDir)

        if (deleted) {
            val settingsList = getSettingsList(context)
            settingsList.remove(settingName)
            saveSettingsList(context, settingsList)
        }

        return deleted
    }

    fun renameSetting(context: Context, oldName: String, newName: String): Boolean {
        if (oldName == "Default" || newName.isEmpty()) return false

        val oldDir = getSettingDirectory(oldName)
        val newDir = getSettingDirectory(newName)

        if (!oldDir.exists()) return false
        if (newDir.exists()) return false

        val renamed = oldDir.renameTo(newDir)

        if (renamed) {
            val settingsList = getSettingsList(context)
            val index = settingsList.indexOf(oldName)
            if (index != -1) {
                settingsList[index] = newName
                saveSettingsList(context, settingsList)
            }
        }

        return renamed
    }

    fun getTunersRootDirectory(): File {
        val documentsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS)
        return File(documentsDir, "smart-guitar-tuner/tuners")
    }

    fun getSettingDirectory(settingName: String): File {
        val tunersRoot = getTunersRootDirectory()
        return File(tunersRoot, settingName.replace(" ", "_"))
    }

    @Deprecated("Use getSettingDirectory(settingName) instead", ReplaceWith("getSettingDirectory(settingName)"))
    fun getSettingDirectory(context: Context, settingName: String): File {
        return getSettingDirectory(settingName)
    }

    private fun deleteDirectory(directory: File): Boolean {
        if (directory.exists()) {
            val files = directory.listFiles()
            if (files != null) {
                for (file in files) {
                    if (file.isDirectory()) {
                        deleteDirectory(file)
                    } else {
                        file.delete()
                    }
                }
            }
        }
        return directory.delete()
    }
}