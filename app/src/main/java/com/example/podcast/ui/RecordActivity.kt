package com.example.podcast.ui

import android.Manifest
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.podcast.R
import com.example.podcast.databinding.ARecordBinding
import com.example.podcast.library.audio.AudioPlayer
import com.example.podcast.library.audio.Recorder
import com.example.podcast.library.utils.*
import com.example.podcast.library.view.SaveBottomSheetDialog
import java.io.File
import java.io.IOException
import kotlin.math.sqrt

class RecordActivity : AppCompatActivity() {

    private lateinit var binding: ARecordBinding
    private lateinit var recorder: Recorder
    private lateinit var player: AudioPlayer
    private lateinit var managePermissions: ManagePermissions
    private var currentTimePass: Long? = null

    companion object {
        const val PERMISSION_REQUEST_CODE = 101
        private const val TAG_PANEL_BOTTOM_SHEET = "TAG_PANEL_BOTTOM_SHEET"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ARecordBinding.inflate(layoutInflater)
        setContentView(binding.root)
        val listPermissions = listOf(
            Manifest.permission.RECORD_AUDIO,
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
        )
        managePermissions = ManagePermissions(this, listPermissions, PERMISSION_REQUEST_CODE)

        initUi()
    }

    override fun onStart() {
        super.onStart()
        listenOnRecorderStates()
    }

    private fun initUi() {
        binding.visualizerRecorder.show()
        binding.ivClose.setOnClickListener {
            finish()
        }

        binding.btnRecord.setOnClickListener {
            if (managePermissions.checkPermissions())
                recorder.toggleRecording()
        }
        binding.visualizerRecorder.ampNormalizer = { sqrt(it.toFloat()).toInt() }

        binding.btnSave.setOnClickListener {
            val saveBottomSheet = SaveBottomSheetDialog.newInstance(
                getString(R.string.save_your_recording),
                getString(R.string.save)
            )
            saveBottomSheet.show(supportFragmentManager, TAG_PANEL_BOTTOM_SHEET)

            saveBottomSheet.onSave = { filename ->
                recorder.onStop = {
                    replaceTempFileName(filename)
                }
                recorder.stopRecording()
            }
        }
    }

    private fun replaceTempFileName(filename: String) {
        try {
            val src = File(filesDir, "rec_temp.wav")
            if (!src.exists()) {
                throw NoSuchFileException(src)
            }

            val dest = File(filesDir, "$filename.wav")
            if (dest.exists()) {
                Toast.makeText(this, "A file with this name already exist", Toast.LENGTH_SHORT).show()
                throw FileAlreadyExistsException(dest)
            }
            val success = src.renameTo(dest)
            if (success) {
                Toast.makeText(this, "File saved", Toast.LENGTH_SHORT).show()
                finish()
            }
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

    private fun listenOnRecorderStates() {
        recorder = Recorder.getInstance(applicationContext).init().apply {
            onStart = {
                binding.tvRecord.text = getString(R.string.stop)
                binding.btnRecord.setImageResource(R.drawable.ic_close)
            }
            onPause = {
                onPauseRecorder()
            }
            onAmpListener = {
                runOnUiThread {
                    if (recorder.isRecording) {
                        binding.visualizerRecorder.addAmp(it, tickDuration)
                        if (currentTimePass != null) {
                            binding.tvTimeline.text = (recorder.getCurrentTime().plus(
                                currentTimePass!!
                            )).formatAsTime()
                        } else {
                            binding.tvTimeline.text = recorder.getCurrentTime().formatAsTime()
                        }
                    }
                }
            }
        }
    }

    private fun onPauseRecorder() {
        listenOnPlayerStates()
        currentTimePass = if (currentTimePass != null)
            currentTimePass?.plus(recorder.getCurrentTime())
        else
            recorder.getCurrentTime()
        binding.visualizerRecorder.hide()
        binding.btnUndo.setOnClickListener {
            undo()
        }
        binding.btnRecord.isEnabled = false
        binding.btnRecord.setImageResource(R.drawable.ic_mic)
        initPlayUi()
    }

    private fun undo() {
        recorder.release()
        player.release()
        binding.btnRecord.isEnabled = true
        currentTimePass = null
        binding.visualizerPlayer.hide()
        binding.btnPlay.hide()
        binding.tvTimeline.text = getString(R.string.zero_time)
        binding.visualizerRecorder.clear()
        binding.tvRecord.text = getString(R.string.start)
        listenOnRecorderStates()
        initUi()
    }

    private fun initPlayUi() {
        binding.visualizerPlayer.show()
        binding.btnPlay.show()
        binding.visualizerPlayer.apply {
            ampNormalizer = { sqrt(it.toFloat()).toInt() }
            onStartSeeking = {
                player.pause()
            }
            onSeeking = { binding.tvTimeline.text = it.formatAsTime() }
            onFinishedSeeking = { time, isPlayingBefore ->
                player.seekTo(time)
                if (isPlayingBefore) {
                    player.resume()
                }
            }
            onAnimateToPositionFinished = { time, isPlaying ->
                updateTime(time, isPlaying)
                player.seekTo(time)
            }
        }
        binding.btnPlay.setOnClickListener {
            player.togglePlay()
        }

        lifecycleScope.launchWhenCreated {
            val amps = player.loadAmps()
            binding.visualizerPlayer.setWaveForm(amps, player.tickDuration)
        }
    }

    private fun listenOnPlayerStates() {
        player = AudioPlayer.getInstance(applicationContext).init().apply {
            onProgress = { time, isPlaying -> updateTime(time, isPlaying) }
        }
    }

    private fun updateTime(time: Long, isPlaying: Boolean) = with(binding) {
        binding.tvTimeline.text = time.formatAsTime()
        binding.visualizerPlayer.updateTime(time, isPlaying)
    }
}
