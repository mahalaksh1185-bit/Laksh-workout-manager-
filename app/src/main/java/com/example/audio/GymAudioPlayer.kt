package com.example.audio

import android.content.Context
import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioManager
import android.media.AudioTrack
import android.media.MediaPlayer
import android.net.Uri
import android.os.Handler
import android.os.Looper
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlin.math.sin

data class AudioTrackItem(
    val id: String,
    val title: String,
    val artist: String,
    val durationMs: Long,
    val uri: Uri? = null,
    val isBuiltInSynth: Boolean = false,
    val synthBpm: Int = 128
)

data class PlayerState(
    val currentTrack: AudioTrackItem? = null,
    val isPlaying: Boolean = false,
    val currentPositionMs: Long = 0L,
    val totalDurationMs: Long = 0L,
    val playlist: List<AudioTrackItem> = emptyList(),
    val isLooping: Boolean = false
)

class GymAudioPlayer(private val context: Context) {

    private var mediaPlayer: MediaPlayer? = null
    private val _playerState = MutableStateFlow(PlayerState())
    val playerState: StateFlow<PlayerState> = _playerState.asStateFlow()

    private val handler = Handler(Looper.getMainLooper())
    private var progressRunnable: Runnable? = null

    // For built-in workout beat synthesis
    private var synthJob: Job? = null
    private var synthAudioTrack: AudioTrack? = null
    private val synthScope = CoroutineScope(Dispatchers.Default)

    init {
        // Provide energetic default workout beats so music works immediately
        val defaultTracks = listOf(
            AudioTrackItem(
                id = "synth_1",
                title = "Cardio Ignition 130 BPM",
                artist = "Gym Beats Engine",
                durationMs = 180_000,
                isBuiltInSynth = true,
                synthBpm = 130
            ),
            AudioTrackItem(
                id = "synth_2",
                title = "Heavy Lifting Pulse 145 BPM",
                artist = "Titan Workout Sound",
                durationMs = 240_000,
                isBuiltInSynth = true,
                synthBpm = 145
            ),
            AudioTrackItem(
                id = "synth_3",
                title = "HIIT Sprint Drive 160 BPM",
                artist = "Endurance Lab",
                durationMs = 150_000,
                isBuiltInSynth = true,
                synthBpm = 160
            )
        )
        _playerState.value = _playerState.value.copy(
            playlist = defaultTracks,
            currentTrack = defaultTracks.firstOrNull(),
            totalDurationMs = defaultTracks.firstOrNull()?.durationMs ?: 0L
        )

        setupProgressTracker()
    }

    private fun setupProgressTracker() {
        progressRunnable = object : Runnable {
            override fun run() {
                val mp = mediaPlayer
                if (mp != null && mp.isPlaying) {
                    _playerState.value = _playerState.value.copy(
                        currentPositionMs = mp.currentPosition.toLong(),
                        totalDurationMs = mp.duration.toLong()
                    )
                } else if (synthAudioTrack != null && _playerState.value.isPlaying) {
                    val newPos = (_playerState.value.currentPositionMs + 500L)
                    val total = _playerState.value.totalDurationMs
                    val nextPos = if (newPos >= total) 0L else newPos
                    _playerState.value = _playerState.value.copy(currentPositionMs = nextPos)
                }
                handler.postDelayed(this, 500)
            }
        }
        handler.post(progressRunnable!!)
    }

    fun playTrack(track: AudioTrackItem) {
        stopPlayback()
        _playerState.value = _playerState.value.copy(
            currentTrack = track,
            isPlaying = true,
            currentPositionMs = 0L,
            totalDurationMs = track.durationMs
        )

        if (track.isBuiltInSynth || track.uri == null) {
            startSynthesizerBeat(track.synthBpm)
        } else {
            try {
                mediaPlayer = MediaPlayer().apply {
                    setAudioAttributes(
                        AudioAttributes.Builder()
                            .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                            .setUsage(AudioAttributes.USAGE_MEDIA)
                            .build()
                    )
                    setDataSource(context, track.uri)
                    prepare()
                    isLooping = _playerState.value.isLooping
                    setOnCompletionListener {
                        playNext()
                    }
                    start()
                }
            } catch (e: Exception) {
                // Fallback to synth if user track fails
                startSynthesizerBeat(128)
            }
        }
    }

    fun togglePlayPause() {
        val current = _playerState.value.currentTrack ?: return
        if (_playerState.value.isPlaying) {
            pausePlayback()
        } else {
            if (current.isBuiltInSynth || current.uri == null) {
                _playerState.value = _playerState.value.copy(isPlaying = true)
                startSynthesizerBeat(current.synthBpm)
            } else {
                mediaPlayer?.let {
                    it.start()
                    _playerState.value = _playerState.value.copy(isPlaying = true)
                } ?: run {
                    playTrack(current)
                }
            }
        }
    }

    private fun pausePlayback() {
        mediaPlayer?.pause()
        stopSynthOnly()
        _playerState.value = _playerState.value.copy(isPlaying = false)
    }

    fun playNext() {
        val list = _playerState.value.playlist
        if (list.isEmpty()) return
        val currentIndex = list.indexOfFirst { it.id == _playerState.value.currentTrack?.id }
        val nextIndex = if (currentIndex in 0 until list.size - 1) currentIndex + 1 else 0
        playTrack(list[nextIndex])
    }

    fun playPrevious() {
        val list = _playerState.value.playlist
        if (list.isEmpty()) return
        val currentIndex = list.indexOfFirst { it.id == _playerState.value.currentTrack?.id }
        val prevIndex = if (currentIndex > 0) currentIndex - 1 else list.size - 1
        playTrack(list[prevIndex])
    }

    fun seekTo(positionMs: Long) {
        val mp = mediaPlayer
        if (mp != null) {
            mp.seekTo(positionMs.toInt())
            _playerState.value = _playerState.value.copy(currentPositionMs = positionMs)
        } else {
            _playerState.value = _playerState.value.copy(currentPositionMs = positionMs)
        }
    }

    fun addCustomTrackFromUri(context: Context, uri: Uri) {
        var displayName = "User Workout Song"
        try {
            val cursor = context.contentResolver.query(uri, null, null, null, null)
            cursor?.use {
                if (it.moveToFirst()) {
                    val nameIndex = it.getColumnIndex(android.provider.OpenableColumns.DISPLAY_NAME)
                    if (nameIndex >= 0) {
                        displayName = it.getString(nameIndex) ?: displayName
                    }
                }
            }
        } catch (_: Exception) {
            displayName = uri.lastPathSegment ?: "User Workout Song"
        }

        val track = AudioTrackItem(
            id = "custom_${System.currentTimeMillis()}",
            title = displayName.removeSuffix(".mp3").removeSuffix(".m4a").removeSuffix(".wav"),
            artist = "Local Audio File",
            durationMs = 210_000,
            uri = uri,
            isBuiltInSynth = false
        )
        addTracks(listOf(track))
    }

    fun addTracks(newTracks: List<AudioTrackItem>) {
        val currentList = _playerState.value.playlist.toMutableList()
        currentList.addAll(0, newTracks)
        _playerState.value = _playerState.value.copy(playlist = currentList)
        if (newTracks.isNotEmpty()) {
            playTrack(newTracks.first())
        }
    }

    fun removeTrack(trackId: String) {
        val currentList = _playerState.value.playlist.toMutableList()
        val index = currentList.indexOfFirst { it.id == trackId }
        if (index != -1) {
            val wasPlayingThis = _playerState.value.currentTrack?.id == trackId
            currentList.removeAt(index)
            _playerState.value = _playerState.value.copy(playlist = currentList)
            if (wasPlayingThis) {
                if (currentList.isNotEmpty()) {
                    playTrack(currentList[minOf(index, currentList.size - 1)])
                } else {
                    stopPlayback()
                    _playerState.value = _playerState.value.copy(currentTrack = null)
                }
            }
        }
    }

    fun toggleLoop() {
        val newLoop = !_playerState.value.isLooping
        mediaPlayer?.isLooping = newLoop
        _playerState.value = _playerState.value.copy(isLooping = newLoop)
    }

    private fun stopPlayback() {
        try {
            mediaPlayer?.stop()
            mediaPlayer?.release()
        } catch (_: Exception) {}
        mediaPlayer = null
        stopSynthOnly()
    }

    private fun stopSynthOnly() {
        synthJob?.cancel()
        synthJob = null
        try {
            synthAudioTrack?.stop()
            synthAudioTrack?.release()
        } catch (_: Exception) {}
        synthAudioTrack = null
    }

    /**
     * Synthesizes an energetic electronic gym kick-bass workout pulse in real-time.
     */
    private fun startSynthesizerBeat(bpm: Int) {
        stopSynthOnly()
        synthJob = synthScope.launch {
            val sampleRate = 22050
            val minBufferSize = AudioTrack.getMinBufferSize(
                sampleRate,
                AudioFormat.CHANNEL_OUT_MONO,
                AudioFormat.ENCODING_PCM_16BIT
            )
            val audioTrack = AudioTrack.Builder()
                .setAudioAttributes(
                    AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_MEDIA)
                        .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                        .build()
                )
                .setAudioFormat(
                    AudioFormat.Builder()
                        .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                        .setSampleRate(sampleRate)
                        .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
                        .build()
                )
                .setBufferSizeInBytes(minBufferSize.coerceAtLeast(4096))
                .build()

            synthAudioTrack = audioTrack
            try {
                audioTrack.play()
            } catch (e: Exception) {
                return@launch
            }

            val beatIntervalSamples = (sampleRate * 60.0 / bpm).toInt()
            val buffer = ShortArray(beatIntervalSamples)

            while (isActive) {
                // Generate a punchy kick & high energy synth pulse for the beat
                for (i in buffer.indices) {
                    val t = i.toDouble() / sampleRate
                    // Pitch envelope: drops from 160Hz down to 45Hz
                    val freq = 45.0 + 115.0 * Math.exp(-t * 28.0)
                    val kickEnvelope = Math.exp(-t * 12.0)
                    val kick = sin(2.0 * Math.PI * freq * t) * kickEnvelope

                    // Hi-hat / shaker pulse on the offbeat
                    val offbeatT = (i - beatIntervalSamples / 2).toDouble() / sampleRate
                    val hatEnvelope = if (offbeatT > 0) Math.exp(-offbeatT * 40.0) else 0.0
                    val hat = (Math.random() * 2.0 - 1.0) * hatEnvelope * 0.4

                    val sample = ((kick * 0.75 + hat * 0.25) * 24000.0).toInt().coerceIn(-32767, 32767)
                    buffer[i] = sample.toShort()
                }

                if (audioTrack.playState == AudioTrack.PLAYSTATE_PLAYING) {
                    audioTrack.write(buffer, 0, buffer.size)
                } else {
                    break
                }
            }
        }
    }

    fun release() {
        handler.removeCallbacksAndMessages(null)
        stopPlayback()
    }
}
