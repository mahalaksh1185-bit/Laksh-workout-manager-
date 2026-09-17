package com.example.alarm

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.media.AudioManager
import android.media.ToneGenerator
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import androidx.core.app.NotificationCompat
import com.example.R

class WorkoutAlarmHelper(private val context: Context) {

    private val notificationManager =
        context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

    companion object {
        const val CHANNEL_WORKOUT = "channel_gym_workout"
        const val CHANNEL_ALARMS = "channel_smart_alarms"
    }

    init {
        createNotificationChannels()
    }

    private fun createNotificationChannels() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val workoutChannel = NotificationChannel(
                CHANNEL_WORKOUT,
                "Workout & Rest Alerts",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Notifies when rest timer finishes or exercise sets complete"
                enableVibration(true)
            }

            val alarmChannel = NotificationChannel(
                CHANNEL_ALARMS,
                "Gym Smart Alarms",
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "Daily gym timing, workout schedule and membership alarms"
            }

            notificationManager.createNotificationChannel(workoutChannel)
            notificationManager.createNotificationChannel(alarmChannel)
        }
    }

    fun playRestCompleteChimeAndVibrate() {
        // Haptic feedback
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                val vibratorManager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager
                val vibrator = vibratorManager.defaultVibrator
                vibrator.vibrate(VibrationEffect.createWaveform(longArrayOf(0, 200, 100, 300), -1))
            } else {
                @Suppress("DEPRECATION")
                val vibrator = context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
                vibrator.vibrate(longArrayOf(0, 200, 100, 300), -1)
            }
        } catch (_: Exception) {}

        // Audio Chime
        try {
            val toneGen = ToneGenerator(AudioManager.STREAM_NOTIFICATION, 90)
            toneGen.startTone(ToneGenerator.TONE_PROP_BEEP2, 400)
        } catch (_: Exception) {}

        // System notification
        sendRestCompleteNotification()
    }

    private fun sendRestCompleteNotification() {
        try {
            val builder = NotificationCompat.Builder(context, CHANNEL_WORKOUT)
                .setSmallIcon(android.R.drawable.ic_lock_idle_alarm)
                .setContentTitle("🔥 Rest Time Complete!")
                .setContentText("Next set is ready! Step up to the barbell.")
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setAutoCancel(true)

            notificationManager.notify(1001, builder.build())
        } catch (_: Exception) {}
    }

    fun sendAlarmNotification(title: String, message: String) {
        try {
            val builder = NotificationCompat.Builder(context, CHANNEL_ALARMS)
                .setSmallIcon(android.R.drawable.ic_dialog_info)
                .setContentTitle("⏰ $title")
                .setContentText(message)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setAutoCancel(true)

            notificationManager.notify(System.currentTimeMillis().toInt(), builder.build())
        } catch (_: Exception) {}
    }
}
