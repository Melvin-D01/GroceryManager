package project.stn991614740.grocerymanagerapp

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatDelegate
import project.stn991614740.grocerymanagerapp.databinding.FragmentSettingsBinding


class SettingsFragment : Fragment() {

    private var _binding: FragmentSettingsBinding? = null
    // This property is only valid between onCreateView and onDestroyView.
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentSettingsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Load the setting
        val sharedPreferences = requireContext().getSharedPreferences("MyPreferences", Context.MODE_PRIVATE)
        val isDarkModeEnabled = sharedPreferences.getBoolean("DarkMode", false)

        // Initialize the switch state
        binding.switchDarkMode.isChecked = isDarkModeEnabled

        // Initialize the switch state
        binding.switchDarkMode.isChecked = when (AppCompatDelegate.getDefaultNightMode()) {
            AppCompatDelegate.MODE_NIGHT_YES -> true
            else -> false
        }

        // Set the listener
        binding.switchDarkMode.setOnCheckedChangeListener { _, isChecked ->
            // Save the setting
            sharedPreferences.edit().putBoolean("DarkMode", isChecked).apply()

            if (isChecked) {
                // The user switched the theme to dark mode
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
            } else {
                // The user switched the theme to light mode
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
            }
        }

        // Initialize notification switches
        val isExpiryNotificationEnabled = sharedPreferences.getBoolean("Notification_ExpiryCheck", false)
        val isTwoDayToExpireNotificationEnabled = sharedPreferences.getBoolean("Notification_TwoDayExpire", false)
        val isFiveDayToExpireNotificationEnabled = sharedPreferences.getBoolean("Notification_FiveDayExpire", false)

        binding.switchNotificationExpiry.isChecked = isExpiryNotificationEnabled
        binding.switchNotificationTwoDayToExpire.isChecked = isTwoDayToExpireNotificationEnabled
        binding.switchNotificationFiveDayToExpire.isChecked = isFiveDayToExpireNotificationEnabled

        // Set listeners
        binding.switchNotificationExpiry.setOnCheckedChangeListener { _, isChecked ->
            sharedPreferences.edit().putBoolean("Notification_ExpiryCheck", isChecked).apply()
            if (isChecked) MainActivity.setupDailyAlarm(requireContext(), ExpiryCheckReceiver::class.java, 12, 0, 0)
            else cancelAlarm(requireContext(), 0)
        }
        binding.switchNotificationTwoDayToExpire.setOnCheckedChangeListener { _, isChecked ->
            sharedPreferences.edit().putBoolean("Notification_TwoDayExpire", isChecked).apply()
            if (isChecked) MainActivity.setupDailyAlarm(requireContext(), TwoDayToExpireCheckReceiver::class.java, 11 , 0, 1)
            else cancelAlarm(requireContext(), 1)
        }
        binding.switchNotificationFiveDayToExpire.setOnCheckedChangeListener { _, isChecked ->
            sharedPreferences.edit().putBoolean("Notification_FiveDayExpire", isChecked).apply()
            if (isChecked) MainActivity.setupDailyAlarm(requireContext(), FiveDayToExpireCheckReceiver::class.java, 13, 0, 2)
            else cancelAlarm(requireContext(), 2)
        }
    }

    private fun cancelAlarm(context: Context, requestCode: Int) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(context, MainActivity::class.java)
        val pendingIntent = PendingIntent.getBroadcast(context, requestCode, intent,
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) PendingIntent.FLAG_IMMUTABLE else 0
        )
        alarmManager.cancel(pendingIntent)
    }

}