package project.stn991614740.grocerymanagerapp

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupWithNavController
import android.view.Menu
import android.view.MenuItem
import android.view.View
import androidx.appcompat.app.AppCompatDelegate
import project.stn991614740.grocerymanagerapp.databinding.ActivityMainBinding
import com.google.android.material.bottomnavigation.BottomNavigationView
import android.provider.Settings
import com.google.firebase.FirebaseApp
import android.widget.Button
import android.widget.TextView
import java.util.*


class MainActivity : AppCompatActivity() {

    private lateinit var appBarConfiguration: AppBarConfiguration
    private lateinit var binding: ActivityMainBinding
    private lateinit var bottomNavigationView: BottomNavigationView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        FirebaseApp.initializeApp(this)
        // forget password
        val forgotPasswordText = findViewById<TextView>(R.id.forgot_password_text)
        forgotPasswordText?.setOnClickListener {
            val intent = Intent(this, ResetPassword::class.java)
            startActivity(intent)
        }

        // Set up alarms to check for item expiration
        setupDailyAlarms()

        // Load the user's theme preferences
        val sharedPreferences = this.getSharedPreferences("MyPreferences", Context.MODE_PRIVATE)
        val isDarkModeEnabled = sharedPreferences.getBoolean("DarkMode", false)
        if (isDarkModeEnabled) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
        } else {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
        }

        // Inflate the main layout
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Set up NavController for navigation between fragments
        val navController = findNavController(R.id.nav_host_fragment_content_main)

        // Setup BottomNavigationView and get its instance
        bottomNavigationView = findViewById(R.id.bottom_navigation)

        // Toggle visibility of the BottomNavigationView based on the active destination
        navController.addOnDestinationChangedListener { _, destination, _ ->
            if (destination.id == R.id.startFragment || destination.id == R.id.registerFragment) {
                bottomNavigationView.visibility = View.GONE
            } else {
                bottomNavigationView.visibility = View.VISIBLE
            }
        }

        // Bind the BottomNavigationView to the NavController
        bottomNavigationView.setupWithNavController(navController)

        // Handle item selection in the BottomNavigationView
        bottomNavigationView.setOnNavigationItemSelectedListener { item ->
            when (item.itemId) {
                R.id.fridgeFragment -> {
                    navController.navigate(R.id.fridgeFragment)
                    return@setOnNavigationItemSelectedListener true
                }
                R.id.recipeFragment -> {
                    navController.navigate(R.id.recipeFragment)
                    return@setOnNavigationItemSelectedListener true
                }
                R.id.settingsFragment -> {
                    navController.navigate(R.id.settingsFragment)
                    return@setOnNavigationItemSelectedListener true
                }
            }
            false
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the main menu; this adds items to the action bar
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle action bar item clicks
        return when (item.itemId) {
            R.id.action_settings -> true
            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        // Handle the action to navigate up within the app's navigation hierarchy
        val navController = findNavController(R.id.nav_host_fragment_content_main)
        return navController.navigateUp(appBarConfiguration) || super.onSupportNavigateUp()
    }

    companion object {
        // Helper function to set up daily alarms at specified times
        fun setupDailyAlarm(context: Context, receiverClass: Class<*>, hour: Int, minute: Int, requestCode: Int) {
            val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
            val intent = Intent(context, receiverClass)
            val pendingIntent = PendingIntent.getBroadcast(context, requestCode, intent,
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) PendingIntent.FLAG_IMMUTABLE else 0
            )

            val calendar: Calendar = Calendar.getInstance().apply {
                timeInMillis = System.currentTimeMillis()
                set(Calendar.HOUR_OF_DAY, hour)
                set(Calendar.MINUTE, minute)
            }

            // If the alarm time is set for a past time, schedule it for the next day
            if (calendar.timeInMillis <= System.currentTimeMillis()) {
                calendar.add(Calendar.DAY_OF_YEAR, 1)
            }

            // Set the alarm, with different methods depending on Android version
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

    // Set up daily alarms for checking food expiration
    private fun setupDailyAlarms() {
        val sharedPreferences = this.getSharedPreferences("MyPreferences", Context.MODE_PRIVATE)

        if (sharedPreferences.getBoolean("Notification_ExpiryCheck", true)) {
            MainActivity.setupDailyAlarm(this, ExpiryCheckReceiver::class.java, 12, 0, 0)
        }

        if (sharedPreferences.getBoolean("Notification_TwoDayExpire", true)) {
            MainActivity.setupDailyAlarm(this, TwoDayToExpireCheckReceiver::class.java, 13, 0, 1)
        }

        if (sharedPreferences.getBoolean("Notification_FiveDayExpire", true)) {
            MainActivity.setupDailyAlarm(this, FiveDayToExpireCheckReceiver::class.java, 11, 0, 2)
        }
    }
}

