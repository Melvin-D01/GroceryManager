package project.stn991614740.grocerymanagerapp

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.ContentValues.TAG
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import java.util.*

// BroadcastReceiver class to handle checking of food items that will expire in two days.
class TwoDayToExpireCheckReceiver : BroadcastReceiver() {

    // This method is called when the BroadcastReceiver receives an Intent broadcast.
    override fun onReceive(context: Context, intent: Intent) {
        // Initialize Firebase Firestore database.
        val db = Firebase.firestore

        // Get the current date and calculate the target date which is 2 days from the current date.
        val currentDate = Calendar.getInstance().time
        val targetDate = Calendar.getInstance().apply { add(Calendar.DAY_OF_MONTH, 2) }.time

        // Fetch food items from the Firestore database that have expiration dates within the next two days.
        db.collection("food")
            .whereGreaterThanOrEqualTo("ExpirationDate", currentDate)
            .whereLessThanOrEqualTo("ExpirationDate", targetDate)
            .get()
            .addOnSuccessListener { querySnapshot ->
                // If there are food items found expiring within 2 days, send a notification.
                if (!querySnapshot.isEmpty) {
                    sendSoonToExpireFoodNotification(context)
                }
            }
            .addOnFailureListener { exception ->
                // Log an error if the fetch operation fails.
                Log.e(TAG, "Error getting documents", exception)
                // Additional error handling can be added here.
            }
    }

    // Function to send a notification when food items that will expire within two days are detected.
    fun sendSoonToExpireFoodNotification(context: Context) {
        // Define notification channel ID and notification ID.
        val channelId = "soon_to_expire_food_channel"
        val notificationId = channelId.hashCode()

        // Construct the intent that will be triggered when the notification is clicked.
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent: PendingIntent = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT)
        } else {
            PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT)
        }

        // Construct the notification with required attributes.
        val notificationBuilder = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(R.drawable.hotpot) // replace with your own notification icon
            .setContentTitle("Food Expiring Soon Alert!")
            .setContentText("You have food items that will expire very soon. Please check your list.")
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)

        // Initialize the notification manager.
        val notificationManager = NotificationManagerCompat.from(context)

        // For Android Oreo and later, create a notification channel.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(channelId, "Soon to Expire Food", NotificationManager.IMPORTANCE_HIGH)
            notificationManager.createNotificationChannel(channel)
        }

        // Finally, send the constructed notification.
        // Notification Permission are checked elsewhere
        notificationManager.notify(notificationId, notificationBuilder.build())

        // Reset the daily alarm for the two-day expiry check.
        MainActivity.setupDailyAlarm(context, TwoDayToExpireCheckReceiver::class.java, 13, 0, 0)
    }

    // Companion object to hold constants used within the class.
    companion object {
        private const val TAG = "TwoDayToExpireCheck"
    }
}
