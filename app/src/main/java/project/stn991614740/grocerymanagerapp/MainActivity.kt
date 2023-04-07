package project.stn991614740.grocerymanagerapp

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button

class MainActivity : AppCompatActivity() {

    // Declare a Button property for the "My Fridge" button.
    private lateinit var myFridgeButton: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Initialize the Button property by finding the view in the layout using its ID.
        myFridgeButton = findViewById(R.id.startButton)

        // Set a click listener for the "My Fridge" button to start the FridgeActivity.
        myFridgeButton.setOnClickListener {
            val intent = Intent(this, FridgeActivity::class.java)
            startActivity(intent)
        }
    }
}
