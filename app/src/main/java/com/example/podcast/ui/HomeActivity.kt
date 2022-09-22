package com.example.podcast.ui

import android.content.Intent
import android.database.Cursor
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.FileUtils
import android.provider.MediaStore
import android.util.Log
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.podcast.R
import com.example.podcast.databinding.AHomeBinding
import com.example.podcast.library.audio.AudioPlayer
import com.example.podcast.library.utils.getMediaDuration
import com.example.podcast.models.RecordedFile
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.IOException

class HomeActivity : AppCompatActivity(), FilesAdapter.Listener {

    private lateinit var binding: AHomeBinding
    private var adapter: FilesAdapter? = null
    private lateinit var player: AudioPlayer

    private val audioPickerResult =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result: ActivityResult ->
            proceedAudioSelection(result)
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = AHomeBinding.inflate(layoutInflater)
        setContentView(binding.root)
        initUi()
    }

    override fun onResume() {
        super.onResume()
        val src = File(filesDir, "rec_temp.wav")
        if (src.exists()) {
            src.delete()
        }
        initRecyclerView()
    }

    private fun initUi() {
        binding.btnRecord.setOnClickListener {
            startActivity(Intent(this, RecordActivity::class.java))
        }

        binding.btnImport.setOnClickListener {
            val intent = Intent(Intent.ACTION_PICK, MediaStore.Audio.Media.EXTERNAL_CONTENT_URI)
            audioPickerResult.launch(intent)
        }
    }

    private fun initRecyclerView() {
        adapter?.clear()
        val listFiles = mutableListOf<RecordedFile>()
        val path = filesDir.path
        val directory = File(path)
        val files: Array<File> = directory.listFiles() as Array<File>
        for (i in files.indices) {
            Log.d("Files", "FileName:" + files[i].name)
            if (files[i].extension == "wav") {
                listFiles.add(
                    RecordedFile(
                        file = files[i],
                        name = files[i].name,
                        duration = files[i].getMediaDuration(this),
                        lastModified = files[i].lastModified()
                    )
                )
            }
        }

        val layoutManager = LinearLayoutManager(this)
        adapter = FilesAdapter(this, this)

        binding.recyclerView.layoutManager = layoutManager
        binding.recyclerView.adapter = adapter

        adapter?.setItems(listFiles)
    }

    override fun onClickAudio(file: File, position: Int) {
        player = AudioPlayer.getInstance(applicationContext).init(file)
        player.togglePlay()

    }

    private fun proceedAudioSelection(result: ActivityResult) {
        if (result.resultCode == RESULT_OK) {
            Log.d("Result", result.data.toString())
            val myAudio = getAudioFile(result.data)
            val destination = File(filesDir, myAudio.name)
            try {
                copyFile(myAudio, destination)
                renameFile(destination)
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }
    }

    private fun renameFile(src: File) {
        val dest = File(filesDir, "${src.nameWithoutExtension}.wav")
        src.renameTo(dest)
    }

    private fun copyFile(source: File?, destination: File?) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            FileUtils.copy(FileInputStream(source), FileOutputStream(destination))
        }
    }

    private fun getAudioFile(data: Intent?): File {
        val selectedAudio: Uri? = data?.data
        val cursor: Cursor? = selectedAudio?.let {
            contentResolver.query(
                it,
                arrayOf(MediaStore.Images.ImageColumns.DATA),
                null,
                null,
                null
            )
        }
        cursor?.moveToFirst()
        val idx: Int = cursor?.getColumnIndex(MediaStore.Images.ImageColumns.DATA)!!
        val selectedAudioPath: String = cursor.getString(idx)
        cursor.close()
        return File(selectedAudioPath)
    }

}
