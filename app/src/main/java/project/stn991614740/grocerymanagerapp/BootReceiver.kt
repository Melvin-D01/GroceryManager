package project.stn991614740.grocerymanagerapp

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import java.util.*

// BootReceiver class that listens to the device boot completion to set alarms.
class BootReceiver : BroadcastReceiver() {
    // This method is called when the BroadcastReceiver is receiving an Intent broadcast.
    override fun onReceive(context: Context, intent: Intent) {
        // Check if the received intent is the BOOT_COMPLETED action
        if (intent.action == "android.intent.action.BOOT_COMPLETED") {
            // Set up daily alarms for different notification types based on user preferences.
            setupDailyAlarm(context, ExpiryCheckReceiver::class.java, 12, 0, 0, "Notification_ExpiryCheck")
            setupDailyAlarm(context, TwoDayToExpireCheckReceiver::class.java, 11, 0, 1, "Notification_TwoDayExpire")
            setupDailyAlarm(context, FiveDayToExpireCheckReceiver::class.java, 13, 0, 2, "Notification_FiveDayExpire")
        }
    }

    // Method to set up a daily alarm based on the provided parameters.
    private fun setupDailyAlarm(context: Context, receiverClass: Class<*>, hour: Int, minute: Int, requestCode: Int, notificationKey: String) {
        // Load the user's setting to determine if the alarm for the given notificationKey is enabled.
        val sharedPreferences = context.getSharedPreferences("MyPreferences", Context.MODE_PRIVATE)
        val isAlarmEnabled = sharedPreferences.getBoolean(notificationKey, true)

        // Only proceed to set up the alarm if the corresponding setting is enabled.
        if (isAlarmEnabled) {
            // Retrieve the system's alarm service.
            val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
            // Create an intent for the provided receiver class.
            val intent = Intent(context, receiverClass)
            // Create a PendingIntent that will be fired when the alarm goes off.
            val pendingIntent = PendingIntent.getBroadcast(context, requestCode, intent,
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) PendingIntent.FLAG_IMMUTABLE else 0
            )

            // Set up a calendar instance for setting the alarm time.
            val calendar: Calendar = Calendar.getInstance().apply {
                timeInMillis = System.currentTimeMillis()
                set(Calendar.HOUR_OF_DAY, hour)
                set(Calendar.MINUTE, minute)
            }

            // If the calendar is set to a time before the current time, increment to the next day to ensure the alarm fires.
            if (calendar.timeInMillis <= System.currentTimeMillis()) {
                calendar.add(Calendar.DAY_OF_YEAR, 1)
            }

            // Set the actual alarm. Use setExactAndAllowWhileIdle for newer Android versions to ensure exact timing and wake up during doze mode.
            // For older versions, just use setExact.
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                alarmManager.setExactAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    calendar.timeInMillis,
                    pendingIntent
                )
            } else {
                alarmManager.setExact(
                    AlarmManager.RTC_WAKEUP,
                    calendar.timeInMillis,
                    pendingIntent
                )
            }
        }
    }
}