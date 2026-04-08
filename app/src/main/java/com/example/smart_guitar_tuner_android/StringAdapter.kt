// Copyright (c) 2026, Alexander Suvorov. All rights reserved.
package com.example.smart_guitar_tuner_android

import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import java.io.File

class StringAdapter(
    private val stringsList: MutableList<StringItem>,
    private val tuningName: String
) : RecyclerView.Adapter<StringAdapter.StringViewHolder>() {

    class StringViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val cardView: CardView = itemView.findViewById(R.id.cardView)
        val stringName: TextView = itemView.findViewById(R.id.stringName)
        val stringNumber: TextView = itemView.findViewById(R.id.stringNumber)
        val statusIndicator: View = itemView.findViewById(R.id.statusIndicator)
        val statusText: TextView = itemView.findViewById(R.id.statusText)
        val btnRecord: Button = itemView.findViewById(R.id.btnRecord)
        val btnPlay: Button = itemView.findViewById(R.id.btnPlay)
        val btnRerecord: Button = itemView.findViewById(R.id.btnRerecord)
    }

    private var onStringsUpdateListener: ((recordedCount: Int, totalCount: Int) -> Unit)? = null

    fun setOnStringsUpdateListener(listener: (Int, Int) -> Unit) {
        onStringsUpdateListener = listener
    }

    fun updateRecordedStatus(context: android.content.Context) {
        if (tuningName != "Default") {
            val settingsDir = SettingsManager.getSettingDirectory(context, tuningName)
            stringsList.forEachIndexed { index, stringItem ->
                val audioFile = File(settingsDir, "string_${index + 1}.wav")
                stringItem.isRecorded = audioFile.exists()
            }
        }
        notifyDataSetChanged()
        updateStatistics()
    }

    private fun updateStatistics() {
        val recordedCount = stringsList.count { it.isRecorded }
        onStringsUpdateListener?.invoke(recordedCount, stringsList.size)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): StringViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_string_card, parent, false)
        return StringViewHolder(view)
    }

    override fun onBindViewHolder(holder: StringViewHolder, position: Int) {
        val string = stringsList[position]
        val context = holder.itemView.context

        holder.stringName.text = string.name
        holder.stringNumber.text = string.number.toString()

        val isDefaultTuning = tuningName == "Default"

        if (isDefaultTuning) {
            holder.statusIndicator.setBackgroundColor(ContextCompat.getColor(context, R.color.action_success))
            holder.statusText.text = "Available"
            holder.statusText.setTextColor(ContextCompat.getColor(context, R.color.action_success))

            holder.btnRecord.visibility = View.GONE
            holder.btnRerecord.visibility = View.GONE
            holder.btnPlay.visibility = View.VISIBLE

            holder.btnPlay.setOnClickListener {
                val intent = Intent(context, AudioPlayerActivity::class.java)
                intent.putExtra("tuning_name", tuningName)
                intent.putExtra("string_index", position)
                context.startActivity(intent)
            }
        } else {
            if (string.isRecorded) {
                holder.statusIndicator.setBackgroundColor(ContextCompat.getColor(context, R.color.action_success))
                holder.statusText.text = "Recorded"
                holder.statusText.setTextColor(ContextCompat.getColor(context, R.color.action_success))
                holder.btnRecord.visibility = View.GONE
                holder.btnRerecord.visibility = View.VISIBLE
                holder.btnPlay.visibility = View.VISIBLE
                holder.btnRerecord.text = "Re-record"
            } else {
                holder.statusIndicator.setBackgroundColor(ContextCompat.getColor(context, R.color.action_danger))
                holder.statusText.text = "Not recorded"
                holder.statusText.setTextColor(ContextCompat.getColor(context, R.color.action_danger))
                holder.btnRecord.visibility = View.VISIBLE
                holder.btnRerecord.visibility = View.GONE
                holder.btnPlay.visibility = View.GONE
                holder.btnRecord.text = "Record"
            }

            holder.btnRecord.setOnClickListener {
                val intent = Intent(context, RecordingActivity::class.java)
                intent.putExtra("tuning_name", tuningName)
                intent.putExtra("string_index", position)
                (context as? StringTunerActivity)?.startActivityForResult(intent, StringTunerActivity.REQUEST_RECORDING)
            }

            holder.btnRerecord.setOnClickListener {
                val intent = Intent(context, RecordingActivity::class.java)
                intent.putExtra("tuning_name", tuningName)
                intent.putExtra("string_index", position)
                (context as? StringTunerActivity)?.startActivityForResult(intent, StringTunerActivity.REQUEST_RECORDING)
            }

            holder.btnPlay.setOnClickListener {
                val intent = Intent(context, AudioPlayerActivity::class.java)
                intent.putExtra("tuning_name", tuningName)
                intent.putExtra("string_index", position)
                context.startActivity(intent)
            }
        }
    }

    override fun getItemCount(): Int = stringsList.size
}

data class StringItem(
    val name: String,
    val number: Int,
    var isRecorded: Boolean = false
)