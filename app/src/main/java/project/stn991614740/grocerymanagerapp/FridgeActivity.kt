package project.stn991614740.grocerymanagerapp

import android.content.ContentValues.TAG
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.ImageButton
import com.bumptech.glide.Glide
import com.google.firebase.firestore.FirebaseFirestore

class FridgeActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_fridge)


        /*

        addButton = findViewById(R.id.imageButton6)
        fridgeButton = findViewById(R.id.imageButton5)
        settingsButton = findViewById(R.id.imageButton7)
        buttonGo = findViewById(R.id.buttonGo)





        val data = FirebaseFirestore.getInstance()
        val collectionRef = data.collection("food")

        collectionRef.get()
            .addOnSuccessListener { querySnapshot ->
                for (document in querySnapshot) {
                    // Access the fields in the document
                    val category = document.getString("Category")
                    val description = document.getString("Description")
                    val expirationDate = document.getString("ExpirationDate")

                    // Do something with the data
                    // For example, print it to the console
                    Log.d(TAG, "Category: $category, Description: $description, ExpirationDate: $expirationDate")
                }
            }








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

            buttonGo.setOnClickListener{
                val intent = Intent(this, AddActivity::class.java)
                startActivity(intent)
            }
        }
    }

         */

    }
}