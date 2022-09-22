package com.example.podcast.ui

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.podcast.databinding.LiAudioBinding
import com.example.podcast.library.adapter.BaseAdapter
import com.example.podcast.models.RecordedFile
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

class FilesAdapter(
    private val context: Context,
    private val listener: Listener,
) : BaseAdapter<Any>(context) {

    companion object {
        private const val TYPE_FILE = 1
    }

    override fun getItemViewType(position: Int): Int {
        return when (getItem(position)) {
            is RecordedFile -> TYPE_FILE
            else -> super.getItemViewType(position)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val inflater = LayoutInflater.from(context)
        return when (viewType) {
            TYPE_FILE -> FileViewHolder(
                LiAudioBinding.inflate(
                    inflater,
                    parent,
                    false
                )
            )
            else -> super.onCreateViewHolder(parent, viewType)
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val item = getItem(position)
        when (holder) {
            is FileViewHolder -> holder.bind(item as RecordedFile)
            else -> super.onBindViewHolder(holder, position)
        }
    }

    private inner class FileViewHolder(private val binding: LiAudioBinding) :
        RecyclerView.ViewHolder(binding.root) {

            fun bind(item: RecordedFile) {
                binding.tvTittle.text = item.name
                binding.tvDuration.text = item.duration?.toLong()?.let { formatMilliSecond(it) }
                binding.tvDate.text = correctDate(item.lastModified)
                binding.btnPlay.setOnClickListener {
                    listener.onClickAudio(item.file, adapterPosition)
                }
            }

        fun formatMilliSecond(milliseconds: Long): String {
            var finalTimerString = ""
            val secondsString: String

            val hours = (milliseconds / (1000 * 60 * 60)).toInt()
            val minutes = (milliseconds % (1000 * 60 * 60)).toInt() / (1000 * 60)
            val seconds = (milliseconds % (1000 * 60 * 60) % (1000 * 60) / 1000).toInt()

            if (hours > 0) {
                finalTimerString = "$hours:"
            }

            secondsString = if (seconds < 10) {
                "0$seconds"
            } else {
                "" + seconds
            }
            finalTimerString = "$finalTimerString$minutes:$secondsString"
            return finalTimerString
        }

        private fun correctDate(timestamp: Long): String? {
            return try {
                val sdf = SimpleDateFormat("dd/MM/yyyy HH:ss", Locale.getDefault())
                val netDate = Date(timestamp)
                sdf.format(netDate)
            } catch (e: Exception) {
                e.toString()
            }
        }

    }

    interface Listener {
        fun onClickAudio(file: File, position: Int)
    }

}
