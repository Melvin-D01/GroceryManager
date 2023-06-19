package project.stn991614740.grocerymanagerapp

import android.app.AlertDialog
import android.content.ContentValues.TAG
import android.content.DialogInterface
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Spinner
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import project.stn991614740.grocerymanagerapp.databinding.FragmentFridgeBinding
import java.util.*
import kotlin.collections.ArrayList


class FridgeFragment : Fragment(), DatabaseUpdateListener {

    private var _binding: FragmentFridgeBinding? = null
    // This property is only valid between onCreateView and onDestroyView.
    private val binding get() = _binding!!

    private lateinit var myAdapter: MyAdapter
    private var currentSortType = "ExpirationDate"
    private var currentSortAscending = false
    private var currentCategory = "All"

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentFridgeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val spinner: Spinner = binding.sortSpinner
        // Create an ArrayAdapter using the string array and a default spinner layout
        ArrayAdapter.createFromResource(
            requireContext(),
            R.array.sort_array,
            R.layout.spinner_item
        ).also { adapter ->
            // Specify the layout to use when the list of choices appears
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            // Apply the adapter to the spinner
            spinner.adapter = adapter
        }

        spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View?, pos: Int, id: Long) {
                if (view != null) {
                    when (parent.getItemAtPosition(pos).toString()) {
                        "Date (Soonest)" -> {
                            currentSortAscending = false
                            fetchDataFromDatabaseWithCategory("ExpirationDate", currentSortAscending, currentCategory)
                        }
                        "Date (Furthest)" -> {
                            currentSortAscending = true
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
                        "Expiring Now (2 Day)" -> {
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


        val categorySpinner: Spinner = binding.categorySpinner
        // Create an ArrayAdapter using the string array and a default spinner layout
        ArrayAdapter.createFromResource(
            requireContext(),
            R.array.cat_array,
            R.layout.spinner_item
        ).also { adapter ->
            // Specify the layout to use when the list of choices appears
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            // Apply the adapter to the spinner
            categorySpinner.adapter = adapter
        }

        categorySpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View?, pos: Int, id: Long) {
                val selectedCategory = parent.getItemAtPosition(pos).toString()
                fetchDataFromDatabaseWithCategory(currentSortType, currentSortAscending, selectedCategory)

                // Change sortSpinner options based on the selected category
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

            override fun onNothingSelected(parent: AdapterView<*>) {
                // Do nothing
            }
        }


        fetchDataFromDatabaseWithCategory("ExpirationDate", currentSortAscending, "All")


        val itemTouchHelperCallback = object : ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT) {
            override fun onMove(recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder, target: RecyclerView.ViewHolder): Boolean {
                return false  // not needed as we are only implementing swipe to delete
            }

            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                val position = viewHolder.adapterPosition
                val foodItem = myAdapter.getItem(position)
                deleteItemFromDatabase(foodItem)
                myAdapter.removeItem(position)
            }
        }
        val itemTouchHelper = ItemTouchHelper(itemTouchHelperCallback)
        itemTouchHelper.attachToRecyclerView(binding.recyclerView)
    }


    private fun fetchDataFromDatabaseWithCategory(orderBy: String, isAscending: Boolean, category: String) {
        // Update currentSortType and currentCategory
        currentSortType = orderBy
        currentCategory = category

        val db = Firebase.firestore
        val query = if (category == "All") {
            db.collection("food").orderBy(orderBy, if (isAscending) Query.Direction.ASCENDING else Query.Direction.DESCENDING)
        } else {
            db.collection("food").whereEqualTo("Category", category).orderBy(orderBy, if (isAscending) Query.Direction.ASCENDING else Query.Direction.DESCENDING)
        }

        query.get()
            .addOnSuccessListener { documents ->
                val myList = ArrayList<Food>()
                for (document in documents) {
                    val myModel = document.toObject(Food::class.java)
                    myList.add(myModel)
                }
                myAdapter = MyAdapter(myList, this)
                binding.recyclerView.adapter = myAdapter
            }
            .addOnFailureListener { exception ->
                Log.e(TAG, "Error getting documents", exception)
                // Handle the exception here.
            }
    }

    private fun fetchDataFromDatabaseWithCategoryAndDate(orderBy: String, isAscending: Boolean, category: String, days: Int) {
        currentSortType = orderBy
        currentCategory = category

        val db = Firebase.firestore
        val currentDate = Calendar.getInstance().time
        val targetDate = Calendar.getInstance().apply { add(Calendar.DAY_OF_MONTH, days) }.time

        val query = if (category == "All") {
            db.collection("food").whereGreaterThanOrEqualTo(orderBy, currentDate)
                .whereLessThanOrEqualTo(orderBy, targetDate)
                .orderBy(orderBy, if (isAscending) Query.Direction.ASCENDING else Query.Direction.DESCENDING)
        } else {
            db.collection("food").whereEqualTo("Category", category)
                .whereGreaterThanOrEqualTo(orderBy, currentDate)
                .whereLessThanOrEqualTo(orderBy, targetDate)
                .orderBy(orderBy, if (isAscending) Query.Direction.ASCENDING else Query.Direction.DESCENDING)
        }

        query.get()
            .addOnSuccessListener { documents ->
                val myList = ArrayList<Food>()
                for (document in documents) {
                    val myModel = document.toObject(Food::class.java)
                    myList.add(myModel)
                }
                myAdapter = MyAdapter(myList, this)
                binding.recyclerView.adapter = myAdapter
            }
            .addOnFailureListener { exception ->
                Log.e(TAG, "Error getting documents", exception)
                // Handle the exception here.
            }
    }


    private fun fetchDataFromDatabaseExpired(category: String) {
        currentSortType = "ExpirationDate"
        currentCategory = category

        val db = Firebase.firestore
        val currentDate = Calendar.getInstance().time

        val query = if (category == "All") {
            db.collection("food").whereLessThan("ExpirationDate", currentDate)
        } else {
            db.collection("food").whereEqualTo("Category", category)
                .whereLessThan("ExpirationDate", currentDate)
        }

        query.get()
            .addOnSuccessListener { documents ->
                val myList = ArrayList<Food>()
                for (document in documents) {
                    val myModel = document.toObject(Food::class.java)
                    myList.add(myModel)
                }
                myAdapter = MyAdapter(myList, this)
                binding.recyclerView.adapter = myAdapter
            }
            .addOnFailureListener { exception ->
                Log.e(TAG, "Error getting documents", exception)
                // Handle the exception here.
            }
    }

    private fun deleteItemFromDatabase(food: Food) {
        val db = Firebase.firestore
        db.collection("food").document(food.UID)
            .delete()
            .addOnSuccessListener {
                Log.d(TAG, "DocumentSnapshot successfully deleted!")
                onDatabaseUpdated()
            }
            .addOnFailureListener { e ->
                Log.w(TAG, "Error deleting document", e)
            }
    }

    // Implement the listener method
    override fun onDatabaseUpdated() {
        // Refresh your RecyclerView here by calling the function that fetches data from the database
        fetchDataFromDatabaseWithCategory(currentSortType,currentSortAscending,currentCategory)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}
