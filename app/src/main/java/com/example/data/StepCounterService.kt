package com.example.data

import android.app.*
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import com.example.MainActivity
import com.example.R
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

class StepCounterService : Service(), SensorEventListener {

    private lateinit var sensorManager: SensorManager
    private var stepCounterSensor: Sensor? = null
    private lateinit var repository: KeepFitRepository
    private lateinit var sharedPreferences: SharedPreferences
    
    private val serviceScope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    companion object {
        private const val NOTIFICATION_ID = 101
        private const val CHANNEL_ID = "step_counter_channel"
        private const val PREFS_NAME = "step_counter_prefs"
        private const val KEY_LAST_SENSOR_VALUE = "last_sensor_value"
    }

    override fun onCreate() {
        super.onCreate()
        sensorManager = getSystemService(Context.SENSOR_SERVICE) as SensorManager
        stepCounterSensor = sensorManager.getDefaultSensor(Sensor.TYPE_STEP_COUNTER)
        
        val database = KeepFitDatabase.getDatabase(applicationContext)
        repository = KeepFitRepository(database.keepFitDao())
        sharedPreferences = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

        createNotificationChannel()
        startForeground(NOTIFICATION_ID, createNotification("Tracking your steps..."))

        stepCounterSensor?.let {
            sensorManager.registerListener(this, it, SensorManager.SENSOR_DELAY_NORMAL)
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        return START_STICKY
    }

    override fun onSensorChanged(event: SensorEvent?) {
        if (event?.sensor?.type == Sensor.TYPE_STEP_COUNTER) {
            val totalStepsSinceBoot = event.values[0].toInt()
            handleStepUpdate(totalStepsSinceBoot)
        }
    }

    private fun handleStepUpdate(totalSteps: Int) {
        val lastSensorValue = sharedPreferences.getInt(KEY_LAST_SENSOR_VALUE, -1)
        
        if (lastSensorValue != -1) {
            val delta = if (totalSteps >= lastSensorValue) {
                totalSteps - lastSensorValue
            } else {
                // Reboot occurred
                totalSteps
            }

            if (delta > 0) {
                serviceScope.launch {
                    val today = repository.getTodayDateString()
                    repository.addStepsToDate(today, delta)
                }
            }
        }

        sharedPreferences.edit().putInt(KEY_LAST_SENSOR_VALUE, totalSteps).apply()
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}

    override fun onBind(intent: Intent?): IBinder? = null

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Step Counter",
                NotificationManager.IMPORTANCE_LOW
            )
            val manager = getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(channel)
        }
    }

    private fun createNotification(content: String): Notification {
        val intent = Intent(this, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(
            this, 0, intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("KeepFit Step Tracker")
            .setContentText(content)
            .setSmallIcon(android.R.drawable.ic_menu_mylocation) // Using system icon for now
            .setContentIntent(pendingIntent)
            .setOngoing(true)
            .build()
    }

    override fun onDestroy() {
        super.onDestroy()
        sensorManager.unregisterListener(this)
    }
}
