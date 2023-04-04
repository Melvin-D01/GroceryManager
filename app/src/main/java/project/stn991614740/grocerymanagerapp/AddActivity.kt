package project.stn991614740.grocerymanagerapp

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.ImageButton
import com.bumptech.glide.Glide
import com.google.firebase.firestore.FirebaseFirestore

class AddActivity : AppCompatActivity() {

    private lateinit var addButton: ImageButton
    private  lateinit var fridgeButton: ImageButton
    private lateinit var settingsButton: ImageButton


    // categories
    private lateinit var meats: ImageButton
    private lateinit var chicken: ImageButton
    private lateinit var fruits: ImageButton
    private lateinit var veggies: ImageButton
    private lateinit var dairy: ImageButton
    private lateinit var rest: ImageButton

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add)

        addButton = findViewById(R.id.imageButton6)
        fridgeButton = findViewById(R.id.imageButton5)
        settingsButton = findViewById(R.id.imageButton7)


        // categories
        meats = findViewById(R.id.redMeatsButton)
        chicken = findViewById(R.id.chickenButton)
        fruits = findViewById(R.id.fruitsButton)
        veggies = findViewById(R.id.vegetablesImage)
        dairy = findViewById(R.id.dairyButton)
        rest = findViewById(R.id.restButton)


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

        meats.setOnClickListener{
            val intent = Intent(this, ScannerPage::class.java)
            startActivity(intent)
        }

        chicken.setOnClickListener{
            val intent = Intent(this, ScannerPage::class.java)
            startActivity(intent)
        }

        fruits.setOnClickListener{
            val intent = Intent(this, ScannerPage::class.java)
            startActivity(intent)
        }

        veggies.setOnClickListener{
            val intent = Intent(this, ScannerPage::class.java)
            startActivity(intent)
        }

        dairy.setOnClickListener{
            val intent = Intent(this, ScannerPage::class.java)
            startActivity(intent)
        }

        rest.setOnClickListener{
            val intent = Intent(this, ScannerPage::class.java)
            startActivity(intent)
        }
    }
}