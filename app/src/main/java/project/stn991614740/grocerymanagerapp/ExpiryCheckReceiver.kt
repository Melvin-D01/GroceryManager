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

class ExpiryCheckReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val db = Firebase.firestore
        val currentDate = Calendar.getInstance().time

        db.collection("food")
            .whereLessThan("ExpirationDate", currentDate)
            .get()
            .addOnSuccessListener { querySnapshot ->
                if (!querySnapshot.isEmpty) {
                    sendExpiredFoodNotification(context)
                }
            }
            .addOnFailureListener { exception ->
                Log.e(TAG, "Error getting documents", exception)
                // Handle the exception here.
            }
    }

    fun sendExpiredFoodNotification(context: Context) {
        val channelId = "expired_food_channel"
        val notificationId = channelId.hashCode()

        val notificationBuilder = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(R.drawable.hotpot) // replace with your own notification icon
            .setContentTitle("Expired Food Alert!")
            .setContentText("You have food items that have expired. Please check your list.")
            .setPriority(NotificationCompat.PRIORITY_HIGH)

        val notificationManager = NotificationManagerCompat.from(context)

        // Create the NotificationChannel on API 26+
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(channelId, "Expired Food", NotificationManager.IMPORTANCE_HIGH)
            notificationManager.createNotificationChannel(channel)
        }

        // Send the notification.
        notificationManager.notify(notificationId, notificationBuilder.build())

        MainActivity.setupDailyAlarm(context, ExpiryCheckReceiver::class.java, 12, 0, 0)
    }
}
