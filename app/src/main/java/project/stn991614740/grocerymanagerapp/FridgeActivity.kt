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

    private lateinit var addButton: ImageButton
    private  lateinit var fridgeButton: ImageButton
    private lateinit var settingsButton: ImageButton

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_fridge)

            addButton = findViewById(R.id.imageButton6)
            fridgeButton = findViewById(R.id.imageButton5)
            settingsButton = findViewById(R.id.imageButton7)

            addButton.setOnClickListener{
                val intent = Intent(this, AddActivity::class.java)
                startActivity(intent)
            }

            fridgeButton.setOnClickListener{
                val intent = Intent(this, FridgeActivity::class.java)
                startActivity(intent)
            }

            settingsButton.setOnClickListener{
                val intent = Intent(this, SettingsActivity::class.java)
                startActivity(intent)
            }

            val db = Firebase.firestore
            db.collection("food")
                .orderBy("ExpirationDate")
                .get()
                .addOnSuccessListener { documents ->
                    val myList = ArrayList<Food>()
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
