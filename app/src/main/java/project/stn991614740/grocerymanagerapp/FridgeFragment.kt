package project.stn991614740.grocerymanagerapp

import android.app.AlertDialog
import android.content.ContentValues.TAG
import android.content.DialogInterface
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import project.stn991614740.grocerymanagerapp.databinding.FragmentFridgeBinding


class FridgeFragment : Fragment(), DatabaseUpdateListener {

    private var _binding: FragmentFridgeBinding? = null
    // This property is only valid between onCreateView and onDestroyView.
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentFridgeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        fetchDataFromDatabase()
    }

    private fun fetchDataFromDatabase() {
        // Connect to the Firestore database and retrieve data from the "food" collection, sorted by expiration date.
        val db = Firebase.firestore
        db.collection("food")
            .orderBy("ExpirationDate")
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
                                myAdapter.notifyItemChanged(position)
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
        fetchDataFromDatabase()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
