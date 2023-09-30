package project.stn991614740.grocerymanagerapp

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.app.NotificationManager
import android.content.ActivityNotFoundException
import android.content.ContentValues.TAG
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import project.stn991614740.grocerymanagerapp.databinding.FragmentFridgeBinding
import java.util.*
import android.provider.Settings
import android.widget.*
import kotlin.collections.ArrayList
import kotlinx.coroutines.*
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.io.IOException

// Fragment representing the fridge view and interactions.
class FridgeFragment : Fragment(), DatabaseUpdateListener {

    private lateinit var progressBar: ProgressBar

    private val client = OkHttpClient()

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

    // Inflate the layout for this fragment.
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentFridgeBinding.inflate(inflater, container, false)
        return binding.root
    }

    // Initialize the view elements after view creation.
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Check for user's notification permissions.
        if (!areNotificationsEnabled()) {
            showNotificationPermissionDialog()
        }

        // Setup the sorting spinner.
        val spinner: Spinner = binding.sortSpinner
        ArrayAdapter.createFromResource(
            requireContext(),
            R.array.sort_array,
            R.layout.spinner_item
        ).also { adapter ->
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            spinner.adapter = adapter
        }

        // Handle spinner item selection.
        spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            // Handle the action on selecting an item from the spinner.
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

            // Do nothing when nothing is selected.
            override fun onNothingSelected(parent: AdapterView<*>) {
                // Do nothing
            }
        }

        // Setup the category spinner.
        val categorySpinner: Spinner = binding.categorySpinner
        ArrayAdapter.createFromResource(
            requireContext(),
            R.array.cat_array,
            R.layout.spinner_item
        ).also { adapter ->
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            categorySpinner.adapter = adapter
        }

        // Handle category spinner item selection.
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
                    spinner.adapter = adapter
                }
            }

            // Do nothing when nothing is selected.
            override fun onNothingSelected(parent: AdapterView<*>) {
                // Do nothing
            }
        }

        // Initial fetch of data from the database.
        fetchDataFromDatabaseWithCategory("ExpirationDate", currentSortAscending, "All")

        // Setup item swipe to delete feature.
        val itemTouchHelperCallback = object : ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT) {
            override fun onMove(recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder, target: RecyclerView.ViewHolder): Boolean {
                return false  // Not needed as we are only implementing swipe to delete.
            }

            // Handle the swipe action.
            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                val position = viewHolder.adapterPosition
                val foodItem = myAdapter.getItem(position)
                deleteItemFromDatabase(foodItem)
                myAdapter.removeItem(position)
            }
        }
        val itemTouchHelper = ItemTouchHelper(itemTouchHelperCallback)
        itemTouchHelper.attachToRecyclerView(binding.recyclerView)

        binding.generateRecipeButton.setOnClickListener {
            fetchRecipeForExpiringItems()
        }

        progressBar = binding.loadingProgressBar
    }

    // Fetch data from the database based on category and sorting order.
    private fun fetchDataFromDatabaseWithCategory(orderBy: String, isAscending: Boolean, category: String) {
        currentSortType = orderBy
        currentCategory = category

        val db = Firebase.firestore
        val userCollection = db.collection("users").document(userId).collection("food")

        val query = if (category == "All") {
            userCollection.orderBy(orderBy, if (isAscending) Query.Direction.ASCENDING else Query.Direction.DESCENDING)
        } else {
            userCollection.whereEqualTo("Category", category).orderBy(orderBy, if (isAscending) Query.Direction.ASCENDING else Query.Direction.DESCENDING)
        }

        query.get()
            .addOnSuccessListener { documents ->
                if (_binding != null) {
                    val myList = ArrayList<Food>()
                    for (document in documents) {
                        val myModel = document.toObject(Food::class.java)
                        myList.add(myModel)
                    }
                    myAdapter = MyAdapter(myList, this)
                    binding.recyclerView.adapter = myAdapter
                }
            }
            .addOnFailureListener { exception ->
                Log.e(TAG, "Error getting documents", exception)
            }
    }

    // Fetch data from the database based on category, sorting order, and date range.
    private fun fetchDataFromDatabaseWithCategoryAndDate(orderBy: String, isAscending: Boolean, category: String, days: Int) {
        currentSortType = orderBy
        currentCategory = category

        val db = Firebase.firestore
        val userCollection = db.collection("users").document(userId).collection("food")

        val currentDate = Calendar.getInstance().time
        val targetDate = Calendar.getInstance().apply {
            add(Calendar.DAY_OF_MONTH, days)
        }.time

        val query = if (category == "All") {
            userCollection
                .whereGreaterThanOrEqualTo(orderBy, currentDate)
                .whereLessThanOrEqualTo(orderBy, targetDate)
                .orderBy(orderBy, if (isAscending) Query.Direction.ASCENDING else Query.Direction.DESCENDING)
        } else {
            userCollection
                .whereEqualTo("Category", category)
                .whereGreaterThanOrEqualTo(orderBy, currentDate)
                .whereLessThanOrEqualTo(orderBy, targetDate)
                .orderBy(orderBy, if (isAscending) Query.Direction.ASCENDING else Query.Direction.DESCENDING)
        }

        query.get()
            .addOnSuccessListener { documents ->
                if (_binding != null) {
                    val myList = ArrayList<Food>()
                    for (document in documents) {
                        val myModel = document.toObject(Food::class.java)
                        myList.add(myModel)
                    }
                    myAdapter = MyAdapter(myList, this)
                    binding.recyclerView.adapter = myAdapter
                }
            }
            .addOnFailureListener { exception ->
                Log.e(TAG, "Error getting documents", exception)
            }
    }

    // Fetch expired items from the database.
    private fun fetchDataFromDatabaseExpired(category: String) {
        currentSortType = "ExpirationDate"
        currentCategory = category

        val db = Firebase.firestore
        val userCollection = db.collection("users").document(userId).collection("food")

        val currentDate = Calendar.getInstance().time

        val query = if (category == "All") {
            userCollection.whereLessThanOrEqualTo("ExpirationDate", currentDate)
        } else {
            userCollection.whereEqualTo("Category", category).whereLessThanOrEqualTo("ExpirationDate", currentDate)
        }

        query.get()
            .addOnSuccessListener { documents ->
                if (_binding != null) {
                    val myList = ArrayList<Food>()
                    for (document in documents) {
                        val myModel = document.toObject(Food::class.java)
                        myList.add(myModel)
                    }
                    myAdapter = MyAdapter(myList, this)
                    binding.recyclerView.adapter = myAdapter
                }
            }
            .addOnFailureListener { exception ->
                Log.e(TAG, "Error getting documents", exception)
            }
    }

    // Delete a food item from the database.
    private fun deleteItemFromDatabase(food: Food) {
        val db = Firebase.firestore
        val userCollection = db.collection("users").document(userId).collection("food")

        userCollection.document(food.UID)
            .delete()
            .addOnSuccessListener {
                Log.d(TAG, "DocumentSnapshot successfully deleted!")
                onDatabaseUpdated()
            }
            .addOnFailureListener { e ->
                Log.w(TAG, "Error deleting document", e)
            }
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

    // Fetch and display recipes for items that are about to expire.
    @SuppressLint("SuspiciousIndentation")
    private fun fetchRecipeForExpiringItems() {
        // shows loading indicator
        showLoadingIndicator()
        // Fetch top items about to expire from the database
        val db = Firebase.firestore
        val userCollection = db.collection("users").document(userId).collection("food")

        val currentDate = Calendar.getInstance().time

        // Let's fetch the top 3 items closest to their expiration date.
        val query = userCollection
            .whereGreaterThan("ExpirationDate", currentDate)  // Fetch items that are still good but are about to expire
            .orderBy("ExpirationDate", Query.Direction.ASCENDING)  // Order by closest expiration date
            .limit(10)  // Limit to top 10 items

        query.get()
            .addOnSuccessListener { documents ->
                val expiringItemsList = ArrayList<String>()  // List to store descriptions
                for (document in documents) {
                    val foodItem = document.toObject(Food::class.java)
                    val description = foodItem.Description   // Assuming 'Description' is the field name in your Food class
                    expiringItemsList.add(description)

                }

                val promptText = "Provide a recipe for a dish using ${expiringItemsList.joinToString(", ")}. Make sure that the title has *** on each side to highlight it."
                getRepsonse(promptText){response ->
                    activity?.runOnUiThread{
                        val text = response

                        val alertDialog = AlertDialog.Builder(context)
                            .setTitle("AI Generated Recipe Just For You!")
                            .setMessage(text)
                            .setPositiveButton("OK") { dialog, which ->
                                dialog.dismiss()
                            }
                            .setNegativeButton("Save") { dialog, which ->
                                saveRecipeToDatabase(text)
                                dialog.dismiss()
                            }
                            .create()
                            // Hides the loading indicator once the data is fetched successfully.
                            hideLoadingIndicator()
                            alertDialog.show()
                    }
                }
            }
    }
    // Fetch a response from OpenAI's API.
    fun getRepsonse(promptText:String, callback: (String) -> Unit) {
        val apiKey = "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"
        val url = "https://api.openai.com/v1/completions"

        val requestBody = """
            {
            "model": "gpt-3.5-turbo-instruct",
            "prompt": "$promptText",
            "max_tokens": 500,
            "temperature": 0
            }
        """.trimIndent()

        val request = Request.Builder()
            .url(url)
            .addHeader("Content-Type", "application/json")
            .addHeader("Authorization", "Bearer $apiKey")
            .post(requestBody.toRequestBody("application/json".toMediaTypeOrNull()))
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                Log.e("error", "API Failed", e)
            }

            override fun onResponse(call: Call, response: Response) {
                val body = response.body?.string()
                if (body != null) {
                    Log.v("data", body)
                } else {
                    Log.v("data","empty")
                }
                val jsonObject = JSONObject(body)
                val jsonArray:JSONArray=jsonObject.getJSONArray("choices")
                val textResult = jsonArray.getJSONObject(0).getString("text")
                callback(textResult)
            }
        })
    }

    // function that saves the AI generated recipe
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

    // function that shows the loading indicator when user is waiting for the AI recipe generator
    private fun showLoadingIndicator() {
        progressBar.visibility = View.VISIBLE
    }

    // function that hides the loading indicator when the AI recipe generator is done computing
    private fun hideLoadingIndicator() {
        progressBar.visibility = View.GONE
    }

    // Cleanup on fragment view destruction.
    override fun onDestroyView() {
        super.onDestroyView()

        notificationDialog?.dismiss()
        _binding = null
    }
}

