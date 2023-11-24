package project.stn991614740.grocerymanagerapp

import android.app.AlarmManager
import android.app.AlertDialog
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatDelegate
import androidx.navigation.fragment.findNavController
import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.FirebaseAuth
import project.stn991614740.grocerymanagerapp.databinding.FragmentSettingsBinding
import com.google.firebase.firestore.FirebaseFirestore


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

        setAppVersion()

        // Load the settings
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

        // Expiry notification switch initialization
        binding.switchNotificationExpiry!!.isChecked = sharedPreferences.getBoolean("Notification_ExpiryCheck", true)
        binding.switchNotificationExpiry!!.setOnCheckedChangeListener { _, isChecked ->
            sharedPreferences.edit().putBoolean("Notification_ExpiryCheck", isChecked).apply()
            if (!isChecked) {
                cancelAlarm(ExpiryCheckReceiver::class.java, 0)
            }
        }

        // Two day to expire notification switch initialization
        binding.switchNotificationTwoDayToExpire!!.isChecked = sharedPreferences.getBoolean("Notification_TwoDayExpire", true)
        binding.switchNotificationTwoDayToExpire!!.setOnCheckedChangeListener { _, isChecked ->
            sharedPreferences.edit().putBoolean("Notification_TwoDayExpire", isChecked).apply()
            if (!isChecked) {
                cancelAlarm(TwoDayToExpireCheckReceiver::class.java, 1)
            }
        }

        // Five day to expire notification switch initialization
        binding.switchNotificationFiveDayToExpire!!.isChecked = sharedPreferences.getBoolean("Notification_FiveDayExpire", true)
        binding.switchNotificationFiveDayToExpire!!.setOnCheckedChangeListener { _, isChecked ->
            sharedPreferences.edit().putBoolean("Notification_FiveDayExpire", isChecked).apply()
            if (!isChecked) {
                cancelAlarm(FiveDayToExpireCheckReceiver::class.java, 2)
            }
        }

        // Set the logout button click listener
        binding?.logoutButton?.setOnClickListener {
            // Implement your logout logic here
            // This is a dummy example, adjust according to your authentication system
            FirebaseAuth.getInstance().signOut()

            // Navigate to login screen
            val action = SettingsFragmentDirections.actionSettingsFragmentToStartFragment()
            findNavController().navigate(action)
        }

        binding.reportBugButton?.setOnClickListener {
            showBugReportDialog()
        }


        binding.deleteFridgeBtn?.setOnClickListener {
            // Show confirmation dialog
            val builder = AlertDialog.Builder(requireContext())
            builder.setTitle("Confirm Deletion")
            builder.setMessage("Are you sure you want to delete all your fridge data? This action cannot be undone.")
            builder.setPositiveButton("Delete") { _, _ ->
                // Create an instance of DatabaseManager with the current userId
                val databaseManager = DatabaseManager(FirebaseAuth.getInstance().currentUser?.uid ?: return@setPositiveButton)

                // Call the method to delete the entire food collection
                databaseManager.deleteEntireFoodCollection(
                    onSuccess = {
                        Toast.makeText(requireContext(), "Fridge deleted successfully", Toast.LENGTH_SHORT).show()
                    },
                    onFailure = { exception ->
                        Toast.makeText(requireContext(), "Error deleting Fridge: ${exception.message}", Toast.LENGTH_SHORT).show()
                        Log.e("SettingsFragment", "Error deleting Fridge: ${exception.message}")
                    }
                )
            }
            builder.setNegativeButton("Cancel") { dialog, _ ->
                dialog.dismiss()
            }
            builder.show()
        }



        binding.deleteAccountButton?.setOnClickListener {
            val user = FirebaseAuth.getInstance().currentUser
            val emailProvider = "password"
            val hasEmailAndPassword = user?.providerData?.any { it.providerId == emailProvider } == true

            if (hasEmailAndPassword) {
                // Prompt the user to re-enter their password
                val passwordInputDialog = LayoutInflater.from(requireContext()).inflate(R.layout.password_input_dialog, null)
                val passwordEditText = passwordInputDialog.findViewById<EditText>(R.id.passwordEditText)
                AlertDialog.Builder(requireContext())
                    .setTitle("Re-authentication required")
                    .setMessage("Please enter your password to proceed:")
                    .setView(passwordInputDialog)
                    .setPositiveButton("Submit") { _, _ ->
                        val password = passwordEditText.text.toString().trim()
                        reauthenticateAndDeleteUser(password)
                    }
                    .setNegativeButton("Cancel") { dialog, _ ->
                        dialog.dismiss()
                    }
                    .show()
            } else {
                // For users who signed in using social media or other providers
                deleteUserAccount()
            }
        }


        binding.recipeDeleteBtn?.setOnClickListener {
            // Show confirmation dialog
            val builder = AlertDialog.Builder(requireContext())
            builder.setTitle("Confirm Deletion")
            builder.setMessage("Are you sure you want to delete all your recipes? This action cannot be undone.")
            builder.setPositiveButton("Delete") { _, _ ->
                // Get the user's ID
                val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return@setPositiveButton

                // Set up the Firestore reference to the user's recipes
                val db = FirebaseFirestore.getInstance()
                val recipesRef = db.collection("users").document(userId).collection("recipes")

                // Fetch the recipes from Firestore again to make sure we have the most recent data
                recipesRef.get()
                    .addOnSuccessListener { documents ->
                        val batch = db.batch() // Create a batch for deleting multiple documents

                        // Loop through each document and add it to the batch for deletion
                        for (document in documents) {
                            val docRef = recipesRef.document(document.id)
                            batch.delete(docRef)
                        }

                        // Commit the batch
                        batch.commit()
                            .addOnSuccessListener {
                                Toast.makeText(requireContext(), "All recipes deleted successfully", Toast.LENGTH_SHORT).show()
                            }
                            .addOnFailureListener { exception ->
                                Toast.makeText(requireContext(), "Error deleting recipes: ${exception.message}", Toast.LENGTH_SHORT).show()
                            }
                    }
                    .addOnFailureListener { exception ->
                        Toast.makeText(requireContext(), "Error fetching recipes: ${exception.message}", Toast.LENGTH_SHORT).show()
                    }
            }
            builder.setNegativeButton("Cancel") { dialog, _ ->
                dialog.dismiss()
            }
            builder.show()
        }


        binding.rlDarkMode?.setOnClickListener {
            binding.switchDarkMode.isChecked = !binding.switchDarkMode.isChecked
        }
        binding.rlExpired?.setOnClickListener {
            binding.switchNotificationExpiry.isChecked = !binding.switchNotificationExpiry.isChecked
        }
        binding.rlExpiredTwoDay?.setOnClickListener {
            binding.switchNotificationTwoDayToExpire.isChecked = !binding.switchNotificationTwoDayToExpire.isChecked
        }
        binding.rlExpiredFiveDay?.setOnClickListener {
            binding.switchNotificationFiveDayToExpire.isChecked = !binding.switchNotificationFiveDayToExpire.isChecked
        }


    }

    private fun reauthenticateAndDeleteUser(password: String) {
        val user = FirebaseAuth.getInstance().currentUser
        if (user != null && user.email != null) {
            val credential = EmailAuthProvider.getCredential(user.email!!, password)
            user.reauthenticate(credential)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        // User re-authenticated, proceed with account deletion
                        deleteUserAccount()
                    } else {
                        // Re-authentication failed
                        Toast.makeText(requireContext(), "Authentication failed. Please try again.", Toast.LENGTH_SHORT).show()
                        Log.e("SettingsFragment", "Re-authentication failed: ${task.exception?.message}")
                    }
                }
        } else {
            Toast.makeText(requireContext(), "Failed to get user information for re-authentication.", Toast.LENGTH_SHORT).show()
        }
    }


    private fun setAppVersion() {
            val packageInfo = requireContext().packageManager.getPackageInfo(requireContext().packageName, 0)
            val appVersion = "GroceryManager\nVersion ${packageInfo.versionName}"
            binding.textViewAppVersion?.text = appVersion
    }

    private fun showBugReportDialog() {
        val bugReportDialogView = LayoutInflater.from(requireContext()).inflate(R.layout.bug_report_dialog, null)
        val bugReportEditText = bugReportDialogView.findViewById<EditText>(R.id.bugReportEditText)

        AlertDialog.Builder(requireContext())
            .setTitle("Report a Bug")
            .setView(bugReportDialogView)
            .setPositiveButton("Submit") { _, _ ->
                val bugReport = bugReportEditText.text.toString().trim()
                if (bugReport.isNotEmpty()) {
                    saveBugReportToDatabase(bugReport)
                } else {
                    Toast.makeText(requireContext(), "Bug report cannot be empty", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("Cancel") { dialog, _ ->
                dialog.dismiss()
            }
            .show()
    }


    private fun saveBugReportToDatabase(bugReport: String) {
        val db = FirebaseFirestore.getInstance()
        val bugReportsRef = db.collection("bugReports")

        val bugReportData = hashMapOf(
            "report" to bugReport,
            "timestamp" to System.currentTimeMillis(),
            "userId" to FirebaseAuth.getInstance().currentUser?.uid
        )

        bugReportsRef.add(bugReportData)
            .addOnSuccessListener {
                Toast.makeText(requireContext(), "Bug report submitted successfully", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener { exception ->
                Toast.makeText(requireContext(), "Failed to submit bug report: ${exception.message}", Toast.LENGTH_SHORT).show()
            }
    }



    private fun cancelAlarm(receiverClass: Class<*>, requestCode: Int) {
        val alarmManager = requireContext().getSystemService(Context.ALARM_SERVICE) as? AlarmManager
        val intent = Intent(requireContext(), receiverClass)
        val pendingIntent = PendingIntent.getBroadcast(
            requireContext(), requestCode, intent,
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) PendingIntent.FLAG_IMMUTABLE else 0
        )
        alarmManager?.cancel(pendingIntent)
    }


    private fun deleteUserAccount() {
        // Show confirmation dialog
        val builder = AlertDialog.Builder(requireContext())
        builder.setTitle("Delete Account")
        builder.setMessage("Are you sure you want to delete your account and all associated data? This action cannot be undone.")
        builder.setPositiveButton("Delete") { _, _ ->
            val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return@setPositiveButton
            val db = FirebaseFirestore.getInstance()

            // Delete user's food list
            val foodListRef = db.collection("users").document(userId).collection("foodList")
            foodListRef.get().addOnSuccessListener { documents ->
                val batch = db.batch()
                for (document in documents) {
                    batch.delete(foodListRef.document(document.id))
                }
                batch.commit()
            }

            // Delete user's recipes
            val recipesRef = db.collection("users").document(userId).collection("recipes")
            recipesRef.get().addOnSuccessListener { documents ->
                val batch = db.batch()
                for (document in documents) {
                    batch.delete(recipesRef.document(document.id))
                }
                batch.commit()
            }

            // Delete user's account
            FirebaseAuth.getInstance().currentUser?.delete()?.addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    Toast.makeText(requireContext(), "Account deleted successfully", Toast.LENGTH_SHORT).show()
                    // Navigate to the start screen or login screen after deletion
                    val action = SettingsFragmentDirections.actionSettingsFragmentToStartFragment()
                    findNavController().navigate(action)
                } else {
                    Toast.makeText(requireContext(), "Failed to delete account: ${task.exception?.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }
        builder.setNegativeButton("Cancel") { dialog, _ ->
            dialog.dismiss()
        }
        builder.show()
    }


}