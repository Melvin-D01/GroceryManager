package project.stn991614740.grocerymanagerapp

import android.content.Context
import android.content.Intent
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

        binding.buttonSendNotification.setOnClickListener {
            val intent = Intent(requireContext(), ExpiryCheckReceiver::class.java)
            context?.sendBroadcast(intent)
        }

        binding.buttonSendNotification2.setOnClickListener {
            val intent = Intent(requireContext(), TwoDayToExpireCheckReceiver::class.java)
            context?.sendBroadcast(intent)
        }

        binding.buttonSendNotification3.setOnClickListener {
            val intent = Intent(requireContext(), TwoDayToExpireCheckReceiver::class.java)
            context?.sendBroadcast(intent)
        }

    }

}