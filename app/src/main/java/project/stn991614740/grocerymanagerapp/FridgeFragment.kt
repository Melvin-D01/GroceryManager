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
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import project.stn991614740.grocerymanagerapp.databinding.FragmentFridgeBinding


class FridgeFragment : Fragment(), DatabaseUpdateListener {

    private var _binding: FragmentFridgeBinding? = null
    // This property is only valid between onCreateView and onDestroyView.
    private val binding get() = _binding!!

    private var currentSortType = "ExpirationDate"

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
                        "Sort by Date" -> fetchDataFromDatabase("ExpirationDate")
                        "Sort by Category" -> fetchDataFromDatabase("Category")
                    }
                }
            }

            override fun onNothingSelected(parent: AdapterView<*>) {
                // Do nothing
            }
        }


        fetchDataFromDatabase("ExpirationDate")
    }

    private fun fetchDataFromDatabase(orderBy: String) {
        currentSortType = orderBy  // Update currentSortType
        // Connect to the Firestore database and retrieve data from the "food" collection, sorted by expiration date.
        val db = Firebase.firestore
        db.collection("food")
            .orderBy(orderBy)
            .get()
            .addOnSuccessListener { documents ->
                val myList = ArrayList<Food>()
                // For each document in the collection, convert it to a Food object and add it to the myList ArrayList.
                for (document in documents) {
                    val myModel = document.toObject(Food::class.java)
                    myList.add(myModel)
                }
                // Set up the RecyclerView with the retrieved data
                val myAdapter = MyAdapter(myList, this)
                binding.recyclerView.adapter = myAdapter

                // Define the swipe-to-delete callback for the RecyclerView
                val swipeCallback = object : ItemTouchHelper.SimpleCallback(
                    0, ItemTouchHelper.RIGHT or ItemTouchHelper.LEFT
                ) {
                    // onMove is not used for swipe-to-delete, so return false
                    override fun onMove(
                        recyclerView: RecyclerView,
                        viewHolder: RecyclerView.ViewHolder,
                        target: RecyclerView.ViewHolder
                    ): Boolean {
                        return false
                    }

                    // onSwiped is called when the user swipes an item left or right
                    override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                        // Get the position of the swiped item
                        val position = viewHolder.adapterPosition
                        // Get the item at the swiped position from the adapter
                        val item = myAdapter.getItem(position)
                        // Build the confirmation dialog
                        val builder = AlertDialog.Builder(requireContext())
                        builder.setTitle("Delete Item")
                            .setMessage("Are you sure you want to delete this item?")
                            .setPositiveButton(
                                "Delete", DialogInterface.OnClickListener { dialog, which ->
                                    // Delete the item from Firestore using its UID
                                    db.collection("food").document(item.UID)
                                        .delete()
                                        .addOnSuccessListener {
                                            // Handle deletion success
                                            myList.removeAt(position)
                                            myAdapter.notifyItemRemoved(position)
                                        }
                                        .addOnFailureListener {
                                            // Handle deletion failure
                                            Log.e(TAG, "Error deleting document", it)
                                        }
                                })
                            .setNegativeButton("Cancel", DialogInterface.OnClickListener { dialog, which ->
                                // Cancel the swipe and return the item to its original position
                                fetchDataFromDatabase(currentSortType) // Refresh the list
                            })
                            .setCancelable(false)
                            .show()
                    }
                }
                // Attach the swipe-to-delete callback to the RecyclerView
                val itemTouchHelper = ItemTouchHelper(swipeCallback)
                itemTouchHelper.attachToRecyclerView(binding.recyclerView)
            }
            .addOnFailureListener { exception ->
                Log.e(TAG, "Error getting documents", exception)
                // Handle the exception here.
            }
    }

    // Implement the listener method
    override fun onDatabaseUpdated() {
        // Refresh your RecyclerView here by calling the function that fetches data from the database
        fetchDataFromDatabase("ExpirationDate")
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
