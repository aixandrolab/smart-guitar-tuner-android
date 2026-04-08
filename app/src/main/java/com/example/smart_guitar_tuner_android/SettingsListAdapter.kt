// Copyright (c) 2026, Alexander Suvorov. All rights reserved.
package com.example.smart_guitar_tuner_android

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.TextView
import java.io.File

class SettingsListAdapter(
    private val context: Context,
    private val settingsList: MutableList<String>
) : BaseAdapter() {

    override fun getCount(): Int = settingsList.size

    override fun getItem(position: Int): Any = settingsList[position]

    override fun getItemId(position: Int): Long = position.toLong()

    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
        val view = convertView ?: LayoutInflater.from(context)
            .inflate(android.R.layout.simple_list_item_2, parent, false)

        val settingName = settingsList[position]

        val text1 = view.findViewById<TextView>(android.R.id.text1)
        val text2 = view.findViewById<TextView>(android.R.id.text2)

        text1.text = settingName

        if (settingName == "Default") {
            text2.text = "Recorded: 6 / 6 strings (default sounds)"
            text2.setTextColor(context.getColor(R.color.action_success))
        } else {
            val settingsDir = SettingsManager.getSettingDirectory(context, settingName)
            var recordedCount = 0
            for (i in 1..6) {
                val audioFile = File(settingsDir, "string_$i.wav")
                if (audioFile.exists()) {
                    recordedCount++
                }
            }
            text2.text = "Recorded: $recordedCount / 6 strings"

            if (recordedCount == 6) {
                text2.setTextColor(context.getColor(R.color.action_success))
            } else if (recordedCount > 0) {
                text2.setTextColor(context.getColor(R.color.action_warning))
            } else {
                text2.setTextColor(context.getColor(R.color.text_secondary))
            }
        }

        text2.textSize = 12f

        return view
    }

    fun updateData(newList: List<String>) {
        settingsList.clear()
        settingsList.addAll(newList)
        notifyDataSetChanged()
    }
}