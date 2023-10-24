package project.stn991614740.grocerymanagerapp

import android.app.NotificationChannel
import android.app.NotificationManager
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

// BroadcastReceiver class to handle the expiry check of food items.
class ExpiryCheckReceiver : BroadcastReceiver() {
    // This method is called when the BroadcastReceiver receives an Intent broadcast.
    override fun onReceive(context: Context, intent: Intent) {

        // Initialize Firebase Firestore database.
        val db = Firebase.firestore

        // Retrieve the currently logged-in user's UID from SharedPreferences.
        val sharedPref = context.getSharedPreferences("MyAppPrefs", Context.MODE_PRIVATE)
        val currentUserUid = sharedPref.getString("currentUserId", null)

        // If no logged-in user is found, log an error and return.
        if (currentUserUid == null) {
            Log.e(TAG, "No logged-in user found.")
            return
        }

        // Get the current date.
        val currentDate = Calendar.getInstance().time

        // Fetch food items from the Firestore database that have expiration dates before the current date.
        db.collection("users").document(currentUserUid).collection("food")
            .whereLessThan("ExpirationDate", currentDate)
            .get()
            .addOnSuccessListener { querySnapshot ->
                // If there are expired food items found, send a notification.
                if (!querySnapshot.isEmpty) {
                    sendExpiredFoodNotification(context)
                }
            }
            .addOnFailureListener { exception ->
                // Log an error if the fetch operation fails.
                Log.e(TAG, "Error getting documents", exception)
            }
    }

    // Function to send a notification when expired food items are detected.
    fun sendExpiredFoodNotification(context: Context) {
        // Define notification channel ID and notification ID.
        val channelId = "expired_food_channel"
        val notificationId = channelId.hashCode()

        // Construct the notification with required attributes.
        val notificationBuilder = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(R.drawable.hotpot) // replace with your own notification icon
            .setContentTitle("Expired Food Alert!")
            .setContentText("You have food items that have expired. Please check your list.")
            .setPriority(NotificationCompat.PRIORITY_HIGH)

        // Initialize the notification manager.
        val notificationManager = NotificationManagerCompat.from(context)

        // For Android Oreo and later, create a notification channel.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(channelId, "Expired Food", NotificationManager.IMPORTANCE_HIGH)
            notificationManager.createNotificationChannel(channel)
        }

        // Finally, send the constructed notification.
        // Notification Permission are checked elsewhere
        notificationManager.notify(notificationId, notificationBuilder.build())

        // Reset the daily alarm for expiry check.
        MainActivity.setupDailyAlarm(context, ExpiryCheckReceiver::class.java, 12, 0, 0)
    }
}