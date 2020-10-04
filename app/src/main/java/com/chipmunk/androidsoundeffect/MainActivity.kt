package com.chipmunk.androidsoundeffect

import android.Manifest
import android.content.pm.PackageManager
import android.media.AudioFormat
import android.media.MediaPlayer
import android.net.Uri
import android.os.Bundle
import android.os.CountDownTimer
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.airbnb.lottie.LottieAnimationView
import com.airbnb.lottie.LottieDrawable
import com.resocoder.timertutorial.util.PrefUtil
import io.github.junyuecao.soundtouch.SoundTouch
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.content_timer.*
import java.io.*
import java.nio.ByteBuffer
import java.nio.ByteOrder


class MainActivity : AppCompatActivity(), VoiceRecorder.Callback {
    val TAG = "MainActivity"
    private var mRecorder: VoiceRecorder? = null
    private var mSoundTouch: SoundTouch? = null
    private var mIsRecording = false
    private var mTestWavOutput: FileOutputStream? = null
    private val BUFFER_SIZE: Int = 4096
    private var mTempBuffer: ByteArray = ByteArray(BUFFER_SIZE)

    private var mPitch: Double = 1.0;
    private var mRate: Double = 1.0;

    lateinit var animation: LottieAnimationView

    enum class TimerState {
        Stopped, Paused, Running
    }

    private lateinit var timer: CountDownTimer
    private var timerLengthSeconds: Long = 0
    private var timerState = TimerState.Stopped

    private var secondsRemaining: Long = 0

    override fun onVoiceStart() {
        mSoundTouch = SoundTouch()
        mSoundTouch?.setChannels(1)
        mSoundTouch?.setSampleRate(VoiceRecorder.SAMPLE_RATE)
        mTestWavOutput = getTestWavOutput()
        writeWavHeader(mTestWavOutput!!,
                AudioFormat.CHANNEL_IN_MONO,
                VoiceRecorder.SAMPLE_RATE,
                AudioFormat.ENCODING_PCM_16BIT);

    }

    override fun onVoice(data: ByteArray?, size: Int) {
        Log.d(TAG, "onVoice: $data, Size: $size")
        mSoundTouch?.setRate(mRate)
        mSoundTouch?.setPitch(mPitch)
        mSoundTouch?.putSamples(data, size)
        var bufferSize = 0
        do {
            bufferSize = mSoundTouch!!.receiveSamples(mTempBuffer, BUFFER_SIZE)
            if (bufferSize > 0) {
                mTestWavOutput?.write(mTempBuffer, 0, bufferSize)
            }
        } while (bufferSize != 0)

    }

    override fun onVoiceEnd() {
        mSoundTouch?.release()
        try {
            mTestWavOutput?.close()
            mTestWavOutput = null
        } catch (e: IOException) {
            e.printStackTrace()
        }
        updateWavHeader(getTempFile())

        runOnUiThread {
            finish();
            overridePendingTransition(0, 0);
            startActivity(getIntent());
            overridePendingTransition(0, 0);
        }

        timerState = TimerState.Stopped

    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>,
                                            grantResults: IntArray) {
        when (requestCode) {
            11 -> {
                if (grantResults.isNotEmpty() && grantResults[0] ==
                        PackageManager.PERMISSION_GRANTED) {
                    if ((ContextCompat.checkSelfPermission(this@MainActivity,
                                    Manifest.permission_group.MICROPHONE) ===
                                    PackageManager.PERMISSION_GRANTED)) {
                        Toast.makeText(this, "Microphone Permission Granted", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    Toast.makeText(this, "Microphone Permission Denied please allow the permission to function", Toast.LENGTH_SHORT).show()
                    finish()
                }
                return
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        requestAudioPermissions()

        animation = findViewById<LottieAnimationView>(R.id.progressBar)

        initTimer()

        // Example of a call to a native method
        mRecorder = VoiceRecorder(this)

        start.setOnClickListener {
            mIsRecording = !mIsRecording
            if (mIsRecording) {
                //start.text = "Stop"
                mRecorder?.start()
            } else {
                // start.text = "Start"
                mRecorder?.stop()
            }

            startTimer()
            start.visibility = View.GONE
            start_txt.visibility = View.GONE
            progress_countdown.visibility = View.VISIBLE
            textView_countdown.visibility = View.VISIBLE
            seconds.visibility = View.GONE
            timerState = TimerState.Running
        }

        play.setOnClickListener {
            val tempFile = getTempFile()
            if (tempFile.exists()) {
                val player = MediaPlayer.create(this, Uri.fromFile(tempFile))
                player.start()
                setupAnimation()
            }
        }

    }

    private fun getTestWavOutput(): FileOutputStream? {
        val s = getTempFile()

        try {
            val os = FileOutputStream(s)
            return os
        } catch (e: FileNotFoundException) {
            e.printStackTrace()
            return null
        }

    }

    override fun onStop() {
        super.onStop()
        mIsRecording = false
        //start.text = "Start"
        mRecorder?.stop()
    }

    private fun getTempFile() = File(getExternalFilesDir(null), "record_temp.wav")

    @Throws(IOException::class)
    private fun writeWavHeader(out: OutputStream, channelMask: Int, sampleRate: Int, encoding: Int) {
        val channels: Short
        when (channelMask) {
            AudioFormat.CHANNEL_IN_MONO -> channels = 1
            AudioFormat.CHANNEL_IN_STEREO -> channels = 2
            else -> throw IllegalArgumentException("Unacceptable channel mask")
        }

        val bitDepth: Short
        when (encoding) {
            AudioFormat.ENCODING_PCM_8BIT -> bitDepth = 8
            AudioFormat.ENCODING_PCM_16BIT -> bitDepth = 16
            AudioFormat.ENCODING_PCM_FLOAT -> bitDepth = 32
            else -> throw IllegalArgumentException("Unacceptable encoding")
        }

        writeWavHeader(out, channels, sampleRate, bitDepth)
    }

    @Throws(IOException::class)
    private fun writeWavHeader(out: OutputStream, channels: Short, sampleRate: Int, bitDepth: Short) {
        // Convert the multi-byte integers to raw bytes in little endian format as required by the spec
        val littleBytes = ByteBuffer
                .allocate(14)
                .order(ByteOrder.LITTLE_ENDIAN)
                .putShort(channels)
                .putInt(sampleRate)
                .putInt(sampleRate * channels.toInt() * (bitDepth / 8))
                .putShort((channels * (bitDepth / 8)).toShort())
                .putShort(bitDepth)
                .array()

        // Not necessarily the best, but it's very easy to visualize this way
        out.write(byteArrayOf(
                // RIFF header
                'R'.toByte(), 'I'.toByte(), 'F'.toByte(), 'F'.toByte(), // ChunkID
                0, 0, 0, 0, // ChunkSize (must be updated later)
                'W'.toByte(), 'A'.toByte(), 'V'.toByte(), 'E'.toByte(), // Format
                // fmt subchunk
                'f'.toByte(), 'm'.toByte(), 't'.toByte(), ' '.toByte(), // Subchunk1ID
                16, 0, 0, 0, // Subchunk1Size
                1, 0, // AudioFormat
                littleBytes[0], littleBytes[1], // NumChannels
                littleBytes[2], littleBytes[3], littleBytes[4], littleBytes[5], // SampleRate
                littleBytes[6], littleBytes[7], littleBytes[8], littleBytes[9], // ByteRate
                littleBytes[10], littleBytes[11], // BlockAlign
                littleBytes[12], littleBytes[13], // BitsPerSample
                // data subchunk
                'd'.toByte(), 'a'.toByte(), 't'.toByte(), 'a'.toByte(), // Subchunk2ID
                0, 0, 0, 0)// Subchunk2Size (must be updated later)
        )
    }


    @Throws(IOException::class)
    private fun updateWavHeader(wav: File) {
        val sizes = ByteBuffer
                .allocate(8)
                .order(ByteOrder.LITTLE_ENDIAN)
                // There are probably a bunch of different/better ways to calculate
                // these two given your circumstances. Cast should be safe since if the WAV is
                // > 4 GB we've already made a terrible mistake.
                .putInt((wav.length() - 8).toInt()) // ChunkSize
                .putInt((wav.length() - 44).toInt()) // Subchunk2Size
                .array()

        var accessWave: RandomAccessFile? = null

        try {
            accessWave = RandomAccessFile(wav, "rw")
            // ChunkSize
            accessWave.seek(4)
            accessWave.write(sizes, 0, 4)

            // Subchunk2Size
            accessWave.seek(40)
            accessWave.write(sizes, 4, 4)
        } catch (ex: IOException) {
            // Rethrow but we still close accessWave in our finally
            throw ex
        } finally {
            if (accessWave != null) {
                try {
                    accessWave.close()
                } catch (ex: IOException) {
                    //
                }

            }
        }
    }

    private fun startTimer() {
        timerState = TimerState.Running

        timer = object : CountDownTimer(secondsRemaining * 1000, 1000) {
            override fun onFinish() = onTimerFinished()

            override fun onTick(millisUntilFinished: Long) {
                secondsRemaining = millisUntilFinished / 1000
                updateCountdownUI()
            }
        }.start()
    }

    private fun initTimer() {
        timerState = TimerState.Paused

        //we don't want to change the length of the timer which is already running
        //if the length was changed in settings while it was backgrounded
        if (timerState == TimerUtil.TimerState.Stopped)
            setNewTimerLength()
        else
            setPreviousTimerLength()

        secondsRemaining = if (timerState == TimerUtil.TimerState.Running || timerState == TimerUtil.TimerState.Paused)
            PrefUtil.getSecondsRemaining(this)
        else
            timerLengthSeconds

        val alarmSetTime = PrefUtil.getAlarmSetTime(this)
        if (alarmSetTime > 0)
            secondsRemaining -= TimerUtil.nowSeconds - alarmSetTime

        if (secondsRemaining <= 0)
            onTimerFinished()
        else if (timerState == TimerUtil.TimerState.Running)
            startTimer()

        updateCountdownUI()
    }

    private fun setNewTimerLength() {
        // val lengthInMinutes = PrefUtil.getTimerLength(this)

        //val lengthInMinutes = 1
        timerLengthSeconds = (10)
        progress_countdown.max = timerLengthSeconds.toInt()
    }

    private fun setPreviousTimerLength() {
        timerLengthSeconds = PrefUtil.getPreviousTimerLengthSeconds(this)
        progress_countdown.max = timerLengthSeconds.toInt()
    }

    private fun updateCountdownUI() {
        val minutesUntilFinished = secondsRemaining / 60
        val secondsInMinuteUntilFinished = secondsRemaining - minutesUntilFinished * 60
        val secondsStr = secondsInMinuteUntilFinished.toString()
        textView_countdown.text = "$minutesUntilFinished:${if (secondsStr.length == 2) secondsStr else "0" + secondsStr}"
        progress_countdown.progress = (timerLengthSeconds - secondsRemaining).toInt()
    }

    private fun onTimerFinished() {
        timerState = TimerState.Stopped

        //set the length of the timer to be the one set in SettingsActivity
        //if the length was changed when the timer was running
        setNewTimerLength()

        progress_countdown.progress = 0

        PrefUtil.setSecondsRemaining(timerLengthSeconds, this)
        secondsRemaining = timerLengthSeconds

        updateCountdownUI()


        textView_countdown.visibility = View.GONE
        seconds.visibility = View.GONE
        progress_countdown.visibility = View.GONE

        play.performClick()
    }


    fun setupAnimation() {
        progressBar.visibility = View.VISIBLE
        animation.speed = 2.0F // How fast does the animation play
        animation.progress = 50F // Starts the animation from 50% of the beginning
        animation.addAnimatorUpdateListener {
            // Called everytime the frame of the animation changes
        }
        animation.repeatMode = LottieDrawable.RESTART // Restarts the animation (you can choose to reverse it as well)
        // animation.cancelAnimation() // Cancels the animation
    }

    private val MY_PERMISSIONS_RECORD_AUDIO = 11
    private fun requestAudioPermissions() {
        if (ContextCompat.checkSelfPermission(this,
                        Manifest.permission.RECORD_AUDIO)
                != PackageManager.PERMISSION_GRANTED) {

            //When permission is not granted by user, show them message why this permission is needed.
            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                            Manifest.permission.RECORD_AUDIO)) {
                Toast.makeText(this, "Please grant permissions to record audio", Toast.LENGTH_LONG).show()

                //Give user option to still opt-in the permissions
                ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.RECORD_AUDIO),
                        MY_PERMISSIONS_RECORD_AUDIO)
            } else {
                // Show user dialog to grant permission to record audio
                ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.RECORD_AUDIO),
                        MY_PERMISSIONS_RECORD_AUDIO)
            }
        } else if (ContextCompat.checkSelfPermission(this,
                        Manifest.permission.RECORD_AUDIO)
                == PackageManager.PERMISSION_GRANTED) {

            //Go ahead with recording audio now

        }
    }

}
