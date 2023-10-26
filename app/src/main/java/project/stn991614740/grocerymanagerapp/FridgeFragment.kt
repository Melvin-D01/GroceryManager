package project.stn991614740.grocerymanagerapp

import android.app.Activity
import android.app.AlertDialog
import android.app.NotificationManager
import android.content.ActivityNotFoundException
import android.content.ContentValues.TAG
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.provider.Settings
import android.speech.RecognizerIntent
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.functions.FirebaseFunctions
import com.google.firebase.ktx.Firebase
import project.stn991614740.grocerymanagerapp.databinding.FragmentFridgeBinding
import java.util.*
import kotlin.collections.ArrayList

// Fragment representing the fridge view and interactions.
class FridgeFragment : Fragment(), DatabaseUpdateListener {

    private val functions = FirebaseFunctions.getInstance()
    private lateinit var progressBar: ProgressBar

    // Binding variables for the fragment.
    private var _binding: FragmentFridgeBinding? = null
    private val binding get() = _binding!!
    private var notificationDialog: AlertDialog? = null

    // Variables to manage the data display.
    private lateinit var myAdapter: MyAdapter
    private var currentSortType = "ExpirationDate"
    private var currentSortAscending = true
    private var currentCategory = "All"

    // Fetching the current user ID.
    private val userId: String
        get() = FirebaseAuth.getInstance().currentUser?.uid ?: ""

    // Initialize DatabaseManager instance
    private lateinit var databaseManager: DatabaseManager

    // Inflate the layout for this fragment.
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentFridgeBinding.inflate(inflater, container, false)
        // Instantiate DatabaseManager with listener
        databaseManager = DatabaseManager(userId)
        return binding.root
    }

    // Initialize the view elements after view creation.
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Check for user's notification permissions.
        if (!areNotificationsEnabled()) {
            showNotificationPermissionDialog()
        }

        // Set up spinners
        setupSortSpinner()
        setupCategorySpinner()

        // Initial fetch of data from the database.
        fetchDataFromDatabaseWithCategory("ExpirationDate", currentSortAscending, "All")

        setupItemSwipeToDelete()
        binding.generateRecipeButton.setOnClickListener {
            callOpenAIForRecipes()
            //fetchRecipeForExpiringItems()
        }

        progressBar = binding.loadingProgressBar
    }

    // Fetch data from the database based on category and sorting order.
    private fun fetchDataFromDatabaseWithCategory(orderBy: String, isAscending: Boolean, category: String) {
        currentSortType = orderBy
        currentCategory = category

        databaseManager.fetchDataWithCategory(
            orderBy = orderBy,
            isAscending = isAscending,
            category = category,
            onSuccess = { documents ->
                if (_binding != null) {
                    val myList = ArrayList<Food>()
                    for (document in documents) {
                        // Directly add document to myList
                        myList.add(document)
                    }
                    myAdapter = MyAdapter(myList, this, requireActivity())
                    binding.recyclerView.adapter = myAdapter
                }
            },
            onFailure = { exception ->  // Here is your lambda for failure
                Log.e(TAG, "Error getting documents", exception)
            }
        )
    }

    // Fetch data from the database based on category, sorting order, and date range.
    private fun fetchDataFromDatabaseWithCategoryAndDate(orderBy: String, isAscending: Boolean, category: String, days: Int) {
        currentSortType = orderBy
        currentCategory = category

        // Create an instance of DatabaseManager
        val databaseManager = DatabaseManager(userId)

        // Now call the method from DatabaseManager
        databaseManager.fetchDataWithCategoryAndDate(
            orderBy = orderBy,
            isAscending = isAscending,
            category = category,
            days = days,
            onSuccess = { documents ->
                if (_binding != null) {
                    val myList = ArrayList<Food>()
                    for (document in documents) {
                        // Directly add document to myList, since it's already a Food object
                        myList.add(document)
                    }
                    myAdapter = MyAdapter(myList, this, requireActivity())
                    binding.recyclerView.adapter = myAdapter
                }
            },
            onFailure = { exception ->
                Log.e(TAG, "Error getting documents", exception)
            }
        )
    }

    private fun fetchDataFromDatabaseExpired(category: String) {
        currentSortType = "ExpirationDate"
        currentCategory = category

        // Create an instance of DatabaseManager
        val databaseManager = DatabaseManager(userId)

        // Now call the method from DatabaseManager
        databaseManager.fetchDataExpired(
            category = category,
            onSuccess = { documents ->
                if (_binding != null) {
                    val myList = ArrayList<Food>().apply {
                        addAll(documents)  // As documents is already a List<Food>, use addAll to add all elements to myList
                    }
                    myAdapter = MyAdapter(myList, this, requireActivity())
                    binding.recyclerView.adapter = myAdapter
                }
            },
            onFailure = { exception ->
                Log.e(TAG, "Error getting documents", exception)
            }
        )
    }

    // Delete a food item from the database.
    private fun deleteItemFromDatabase(food: Food) {
        // Create an instance of DatabaseManager
        val databaseManager = DatabaseManager(userId)

        // Now call the method from DatabaseManager
        databaseManager.deleteItem(
            food = food,
            onSuccess = {
                Log.d(TAG, "DocumentSnapshot successfully deleted!")
                onDatabaseUpdated()
            },
            onFailure = { e ->
                Log.w(TAG, "Error deleting document", e)
            }
        )
    }

    // Callback for when the database is updated.
    override fun onDatabaseUpdated() {
        fetchDataFromDatabaseWithCategory(currentSortType, currentSortAscending, currentCategory)
    }

    // Check if notifications are enabled for the app.
    private fun areNotificationsEnabled(): Boolean {
        val notificationManager = context?.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        return notificationManager.areNotificationsEnabled()
    }

    // Show dialog prompting user to enable notifications.
    private fun showNotificationPermissionDialog() {
        notificationDialog = AlertDialog.Builder(requireContext())
            .setTitle("Notification Permission")
            .setMessage("To ensure you receive important alerts, please enable notifications for this app in the settings.")
            .setPositiveButton("Go to Settings") { _, _ ->
                try {
                    val intent = Intent().apply {
                        action = Settings.ACTION_APP_NOTIFICATION_SETTINGS
                        putExtra(Settings.EXTRA_APP_PACKAGE, requireContext().packageName)
                    }
                    startActivity(intent)
                } catch (e: ActivityNotFoundException) {
                    val intent = Intent(Settings.ACTION_SETTINGS)
                    startActivity(intent)
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun setupSortSpinner() {
        val spinner: Spinner = binding.sortSpinner
        ArrayAdapter.createFromResource(
            requireContext(),
            R.array.sort_array,
            R.layout.spinner_item
        ).also { adapter ->
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            spinner.adapter = adapter
        }

        spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View?, pos: Int, id: Long) {
                if (view != null) {
                    when (parent.getItemAtPosition(pos).toString()) {
                        "Date (Soonest)" -> {
                            currentSortAscending = true
                            fetchDataFromDatabaseWithCategory("ExpirationDate", currentSortAscending, currentCategory)
                        }
                        "Date (Furthest)" -> {
                            currentSortAscending = false
                            fetchDataFromDatabaseWithCategory("ExpirationDate", currentSortAscending, currentCategory)
                        }
                        "Category (A-Z)" -> {
                            currentSortAscending = true
                            fetchDataFromDatabaseWithCategory("Category", currentSortAscending, currentCategory)
                        }
                        "Category (Z-A)" -> {
                            currentSortAscending = false
                            fetchDataFromDatabaseWithCategory("Category", currentSortAscending, currentCategory)
                        }
                        "Expiring soon (5 Days)" -> {
                            currentSortAscending = true
                            fetchDataFromDatabaseWithCategoryAndDate("ExpirationDate", currentSortAscending, currentCategory, 5)
                        }
                        "Expiring Now (2 Days)" -> {
                            currentSortAscending = true
                            fetchDataFromDatabaseWithCategoryAndDate("ExpirationDate", currentSortAscending, currentCategory, 2)
                        }
                        "Expired" -> {
                            currentSortAscending = false
                            fetchDataFromDatabaseExpired(currentCategory)
                        }
                    }
                }
            }

            override fun onNothingSelected(parent: AdapterView<*>) {
                // Do nothing
            }
        }
    }

    private fun setupCategorySpinner() {
        val categorySpinner: Spinner = binding.categorySpinner
        ArrayAdapter.createFromResource(
            requireContext(),
            R.array.cat_array,
            R.layout.spinner_item
        ).also { adapter ->
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            categorySpinner.adapter = adapter
        }

        categorySpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View?, pos: Int, id: Long) {
                val selectedCategory = parent.getItemAtPosition(pos).toString()
                fetchDataFromDatabaseWithCategory(currentSortType, currentSortAscending, selectedCategory)

                val sortOptions = if (selectedCategory == "All") R.array.sort_array else R.array.sort_array_date_only
                ArrayAdapter.createFromResource(
                    requireContext(),
                    sortOptions,
                    R.layout.spinner_item
                ).also { adapter ->
                    adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                    binding.sortSpinner.adapter = adapter
                }
            }

            override fun onNothingSelected(parent: AdapterView<*>) {
                // Do nothing
            }
        }
    }

    private fun setupItemSwipeToDelete() {
        val itemTouchHelperCallback = object : ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT) {
            override fun onMove(recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder, target: RecyclerView.ViewHolder): Boolean {
                return false  // Not needed as we are only implementing swipe to delete.
            }

            // Handle the swipe action.
            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                val position = viewHolder.adapterPosition
                val foodItem = myAdapter.getItem(position)

                // Show confirmation dialog
                val builder = AlertDialog.Builder(requireContext())
                builder.setTitle("Confirm Delete")
                builder.setMessage("Are you sure you want to delete this item?")
                builder.setCancelable(false)  // Prevent dismissal by touch outside or back press
                builder.setPositiveButton("Yes") { _, _ ->
                    deleteItemFromDatabase(foodItem)
                    myAdapter.removeItem(position)
                }
                builder.setNegativeButton("No") { dialog, _ ->
                    // Restore item in the view (prevent swipe away)
                    myAdapter.notifyItemChanged(position)
                    dialog.dismiss()
                }
                builder.show()
            }
        }
        val itemTouchHelper = ItemTouchHelper(itemTouchHelperCallback)
        itemTouchHelper.attachToRecyclerView(binding.recyclerView)
    }

    private fun callOpenAIForRecipes() {
        showLoadingIndicator()
        // Initialize the Cloud Function call
        functions.getHttpsCallable("callOpenAIForRecipes")
            .call()
            .addOnCompleteListener { task ->
                // Check if the Cloud Function call was successful
                if (!task.isSuccessful) {
                    val e = task.exception
                }

                // The function call returned successfully. Extract the recipe from the result
                val recipe = task.result?.data as? String

                // Construct an AlertDialog to display the AI-generated recipe
                val alertDialog = AlertDialog.Builder(context)
                    .setTitle("AI Generated Recipe Just For You!")
                    .setMessage(recipe)  // Display the recipe in the AlertDialog
                    .setPositiveButton("OK") { dialog, _ ->
                        // Dismiss the AlertDialog when the "OK" button is pressed
                        dialog.dismiss()
                    }
                    .setNegativeButton("Save") { dialog, _ ->
                        // Save the recipe if it's not null when the "Save" button is pressed
                        if (recipe != null) {
                            saveRecipeToDatabase(recipe)
                        }
                        // Dismiss the AlertDialog after saving
                        dialog.dismiss()
                    }
                    .create()

                // If you have a loading indicator shown before this function was called,
                // hide it now that the data has been fetched
                hideLoadingIndicator()

                // Finally, display the AlertDialog
                alertDialog.show()
            }
    }

    // Function that saves the AI-generated recipe
    fun saveRecipeToDatabase(recipeText: String) {
        val db = Firebase.firestore
        val recipeMap = hashMapOf(
            "recipe" to recipeText,
            "creationDate" to Calendar.getInstance().time
        )

        db.collection("users").document(userId).collection("recipes").add(recipeMap)
            .addOnSuccessListener { documentReference ->
                Toast.makeText(context, "Recipe saved successfully!", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener { e ->
                Toast.makeText(context, "Error saving recipe: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    // Function that shows the loading indicator when the user is waiting for the AI recipe generator
    private fun showLoadingIndicator() {
        progressBar.visibility = View.VISIBLE
    }

    // Function that hides the loading indicator when the AI recipe generator is done computing
    private fun hideLoadingIndicator() {
        progressBar.visibility = View.GONE
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        // Handle the permissions result
        if (requestCode == MyAdapter.MY_PERMISSIONS_REQUEST_RECORD_AUDIO) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permission was granted
            } else {
                // Permission was denied
            }
        }
    }

    companion object {
        const val VOICE_RECOGNITION_REQUEST_CODE = 1001
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == VOICE_RECOGNITION_REQUEST_CODE && resultCode == Activity.RESULT_OK) {
            val matches = data?.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)
            if (matches != null && matches.isNotEmpty()) {
                val text = matches[0]
                // Do something with the recognized text, such as updating a TextView or EditText
            }
        }
    }


    // Cleanup on fragment view destruction.
    override fun onDestroyView() {
        super.onDestroyView()
        notificationDialog?.dismiss()
        _binding = null
    }
}
