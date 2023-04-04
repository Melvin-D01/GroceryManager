package project.stn991614740.grocerymanagerapp

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.ImageButton
import com.bumptech.glide.Glide
import com.google.firebase.firestore.FirebaseFirestore

class FridgeActivity : AppCompatActivity() {

    private lateinit var addButton: ImageButton
    private  lateinit var fridgeButton: ImageButton
    private lateinit var settingsButton: ImageButton
    private lateinit var buttonGo: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_fridge)


        addButton = findViewById(R.id.imageButton6)
        fridgeButton = findViewById(R.id.imageButton5)
        settingsButton = findViewById(R.id.imageButton7)
        buttonGo = findViewById(R.id.buttonGo)


        /*

        val data = FirebaseFirestore.getInstance()

        val docRef = data.collection("Mydatabase").document("FirebaseData")
        docRef.get().addOnSuccessListener { document ->
            if (document != null) {

                val mImageViewUsingPicassoFridge = document.getString("FridgePng")
                val mImageViewUsingPicassoAdd = document.getString("AddPng")
                val mImageViewUsingPicassoSettings = document.getString("SettingsImg")

                Glide.with(this).load(mImageViewUsingPicassoFridge).into(fridgeButton);
                Glide.with(this).load(mImageViewUsingPicassoAdd).into(addButton);
                Glide.with(this).load(mImageViewUsingPicassoSettings).into(settingsButton);

            } else {
                //Log.d(TAG, "No such document")
            }

         */

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
