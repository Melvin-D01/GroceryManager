package project.stn991614740.grocerymanagerapp

import android.content.ContentValues.TAG
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.ImageButton
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

class FridgeActivity : AppCompatActivity() {

    // Declare ImageButton properties for the add, fridge, and settings buttons.
    private lateinit var addButton: ImageButton
    private lateinit var fridgeButton: ImageButton
    private lateinit var settingsButton: ImageButton

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_fridge)

        // Initialize the three ImageButton properties to their respective buttons in the activity layout.
        addButton = findViewById(R.id.imageButton6)
        fridgeButton = findViewById(R.id.imageButton5)
        settingsButton = findViewById(R.id.imageButton7)

        // Set a click listener for the "add" button to start the AddActivity.
        addButton.setOnClickListener{
            val intent = Intent(this, AddActivity::class.java)
            startActivity(intent)
        }

        // Set a click listener for the "fridge" button to reload the current activity (FridgeActivity).
        fridgeButton.setOnClickListener{
            val intent = Intent(this, FridgeActivity::class.java)
            startActivity(intent)
        }

        // Set a click listener for the "settings" button to start the SettingsActivity.
        settingsButton.setOnClickListener{
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
            }
            .addOnFailureListener { exception ->
                Log.d(TAG, "Error getting documents: ", exception)
            }
    }
}
