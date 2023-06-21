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

class TwoDayToExpireCheckReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val db = Firebase.firestore
        val currentDate = Calendar.getInstance().time
        val targetDate = Calendar.getInstance().apply { add(Calendar.DAY_OF_MONTH, 2) }.time

        db.collection("food")
            .whereGreaterThanOrEqualTo("ExpirationDate", currentDate)
            .whereLessThanOrEqualTo("ExpirationDate", targetDate)
            .get()
            .addOnSuccessListener { querySnapshot ->
                if (!querySnapshot.isEmpty) {
                    sendSoonToExpireFoodNotification(context)
                }
            }
            .addOnFailureListener { exception ->
                Log.e(TAG, "Error getting documents", exception)
                // Handle the exception here.
            }
    }

    fun sendSoonToExpireFoodNotification(context: Context) {
        val channelId = "soon_to_expire_food_channel"
        val notificationId = channelId.hashCode()

        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent: PendingIntent = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT)
        } else {
            PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT)
        }


        val notificationBuilder = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(R.drawable.hotpot) // replace with your own notification icon
            .setContentTitle("Food Expiring Soon Alert!")
            .setContentText("You have food items that will very soon. Please check your list.")
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)

        val notificationManager = NotificationManagerCompat.from(context)

        // Create the NotificationChannel on API 26+
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(channelId, "Soon to Expire Food", NotificationManager.IMPORTANCE_HIGH)
            notificationManager.createNotificationChannel(channel)
        }

        // Send the notification.
        notificationManager.notify(notificationId, notificationBuilder.build())

        MainActivity.setupDailyAlarm(context, TwoDayToExpireCheckReceiver::class.java, 11, 0, 0)
    }
}