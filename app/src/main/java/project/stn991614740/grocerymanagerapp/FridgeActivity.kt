package project.stn991614740.grocerymanagerapp

import android.app.AlertDialog
import android.content.ContentValues.TAG
import android.content.DialogInterface
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.ImageButton
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

class FridgeActivity : AppCompatActivity() {

    // Use lazy initialization for the ImageButton properties.
    private val addButton by lazy { findViewById<ImageButton>(R.id.imageButton6) }
    private val fridgeButton by lazy { findViewById<ImageButton>(R.id.imageButton5) }
    private val settingsButton by lazy { findViewById<ImageButton>(R.id.imageButton7) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_fridge)

        // Set a click listener for the "add" button to start the AddActivity.
        addButton.setOnClickListener {
            val intent = Intent(this, AddActivity::class.java)
            startActivity(intent)
        }

        // Set a click listener for the "fridge" button to reload the current activity (FridgeActivity).
        fridgeButton.setOnClickListener {
            recreate()
        }

        // Set a click listener for the "settings" button to start the SettingsActivity.
        settingsButton.setOnClickListener {
            val intent = Intent(this, SettingsActivity::class.java)
            startActivity(intent)
        }

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
                val recyclerView = findViewById<RecyclerView>(R.id.recyclerView)
                val myAdapter = MyAdapter(myList)
                recyclerView.adapter = myAdapter

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
                        val builder = AlertDialog.Builder(this@FridgeActivity)
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
                itemTouchHelper.attachToRecyclerView(recyclerView)
            }
            .addOnFailureListener { exception ->
                Log.e(TAG, "Error getting documents", exception)
                // Handle the exception here.
            }
    }
}


