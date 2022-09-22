package com.example.podcast.library.audio

import android.content.Context
import com.example.podcast.library.Holder
import com.example.podcast.library.utils.WAVE_HEADER_SIZE
import com.example.podcast.library.utils.recordFile
import com.example.podcast.library.utils.toMediaSource
import com.google.android.exoplayer2.ExoPlayer
import com.google.android.exoplayer2.PlaybackException
import com.google.android.exoplayer2.Player
import kotlinx.coroutines.*
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.flow.flow
import java.io.File
import java.nio.ByteBuffer
import java.nio.ByteOrder

class AudioPlayer private constructor(context: Context) : Player.Listener {

    companion object : Holder<AudioPlayer, Context>(::AudioPlayer) {
        private const val LOOP_DURATION = 20L
        private val TAG = AudioPlayer::class.simpleName
    }

    private val appContext = context.applicationContext

    var onProgress: ((Long, Boolean) -> Unit)? = null
    var onStart: (() -> Unit)? = null
    var onStop: (() -> Unit)? = null
    var onPause: (() -> Unit)? = null
    var onResume: (() -> Unit)? = null
    val tickDuration = Recorder.getInstance(this.appContext).tickDuration

    private val coroutineScope = CoroutineScope(Dispatchers.Main)

    private var loopingFlowJob: Job? = null
    private var loopingFlow = flow {
        while (true) {
            emit(Unit)
            delay(LOOP_DURATION)
        }
    }

    private val recordFile = context.recordFile
    private val bufferSize = Recorder.getInstance(context).bufferSize

    private lateinit var player: ExoPlayer

    fun init(): AudioPlayer {
        if (::player.isInitialized) {
            player.release()
        }
        player = ExoPlayer.Builder(appContext).build().apply {
            setMediaSource(recordFile.toMediaSource())
            prepare()
            addListener(this@AudioPlayer)
        }
        return this
    }

    fun init(file: File): AudioPlayer {
        if (::player.isInitialized) {
            player.release()
        }
        player = ExoPlayer.Builder(appContext).build().apply {
            setMediaSource(file.toMediaSource())
            prepare()
            addListener(this@AudioPlayer)
        }
        return this
    }

    fun togglePlay() {
        if (!player.isPlaying) {
            resume()
        } else {
            pause()
        }
    }

    fun seekTo(time: Long) {
        player.seekTo(time)
    }

    fun resume() {
        player.play()
        updateProgress()
        onResume?.invoke()
    }

    fun pause() {
        player.pause()
        updateProgress()
        onPause?.invoke()
    }

    private fun updateProgress(position: Long = player.currentPosition) {
        onProgress?.invoke(position, player.playWhenReady)
    }

    @Suppress("BlockingMethodInNonBlockingContext")
    suspend fun loadAmps(): List<Int> = withContext(IO) {
        val amps = mutableListOf<Int>()
        val buffer = ByteArray(bufferSize)
        File(recordFile.toString()).inputStream().use {
            it.skip(WAVE_HEADER_SIZE.toLong())

            var count = it.read(buffer)
            while (count > 0) {
                amps.add(buffer.calculateAmplitude())
                count = it.read(buffer)
            }
        }
        amps
    }

    private fun ByteArray.calculateAmplitude(): Int {
        return ShortArray(size / 2).let {
            ByteBuffer.wrap(this)
                .order(ByteOrder.LITTLE_ENDIAN)
                .asShortBuffer()
                .get(it)
            it.maxOrNull()?.toInt() ?: 0
        }
    }

    override fun onPlaybackStateChanged(playbackState: Int) {
        super.onPlaybackStateChanged(playbackState)
        when (playbackState) {
            Player.STATE_ENDED -> {
                updateProgress(player.duration)
                onStop?.invoke()
                reset()
            }
            Player.STATE_READY -> Unit
            Player.STATE_BUFFERING -> Unit
            Player.STATE_IDLE -> Unit
        }
    }

    private fun reset() {
        player.prepare()
        player.pause()
        player.seekTo(0)
    }

    override fun onIsPlayingChanged(isPlaying: Boolean) {
        super.onIsPlayingChanged(isPlaying)
        if (isPlaying) {
            loopingFlowJob?.cancel()
            loopingFlowJob = coroutineScope.launch {
                loopingFlow.collect {
                    updateProgress()
                }
            }
            onStart?.invoke()
        } else {
            loopingFlowJob?.cancel()
        }
    }

    fun release() {
        player.release()
        onProgress = null
        onStart = null
        onStop = null
        onPause = null
        onResume = null
    }


    override fun onPlayerError(error: PlaybackException) {
        super.onPlayerError(error)
    }
}
