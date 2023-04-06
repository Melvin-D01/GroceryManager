package project.stn991614740.grocerymanagerapp

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.InputType
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContentProviderCompat.requireContext
import com.bumptech.glide.Glide
import com.google.firebase.firestore.FirebaseFirestore

class AddActivity : AppCompatActivity() {

    private lateinit var addButton: ImageButton
    private  lateinit var fridgeButton: ImageButton
    private lateinit var settingsButton: ImageButton
    private lateinit var fruitsButton: ImageButton
    private lateinit var vegiButton: ImageButton
    private lateinit var meatButton: ImageButton
    private lateinit var seafoodButton: ImageButton
    private lateinit var dariyButton: ImageButton
    private lateinit var grainsButton: ImageButton
    private lateinit var canButton: ImageButton
    private lateinit var snacksButton: ImageButton
    private lateinit var beveragesButton: ImageButton
    private lateinit var condimentsButton: ImageButton
    private lateinit var bakedGoodsButton: ImageButton
    private lateinit var frozenFoodButton: ImageButton
    private lateinit var foodPrepMealsButton: ImageButton
    private lateinit var babyFoodButton: ImageButton
    private lateinit var petFoodButton: ImageButton
    private lateinit var otherFoodButton : ImageButton

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add)

        // nav buttons
        addButton = findViewById(R.id.imageButton6)
        fridgeButton = findViewById(R.id.imageButton5)
        settingsButton = findViewById(R.id.imageButton7)

        // categories
        fruitsButton = findViewById(R.id.fruitButton)
        vegiButton = findViewById(R.id.vegiButton)
        meatButton = findViewById(R.id.meatButton)
        seafoodButton = findViewById(R.id.seafoodButton)
        dariyButton = findViewById(R.id.dairyButton)
        grainsButton = findViewById(R.id.grainsButton)
        canButton = findViewById(R.id.canButton)
        snacksButton = findViewById(R.id.snacksButton)
        beveragesButton = findViewById(R.id.beveragesButton)
        condimentsButton = findViewById(R.id.condimentsButton)
        bakedGoodsButton = findViewById(R.id.bakedGoodsButton)
        frozenFoodButton = findViewById(R.id.frozenFoodsButton)
        foodPrepMealsButton = findViewById(R.id.foodPrepMealsButton)
        babyFoodButton = findViewById(R.id.babyFoodButton)
        petFoodButton = findViewById(R.id.petFoodButton)
        otherFoodButton = findViewById(R.id.otherButton)



        // listener for the nav buttons
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


        // listener for the category selection
        // also handles the sending the category selection along with an alert box that asks user to enter item specifics
        // this may get replaced with machine learning portion in second half of the capstone project, where it will auto detect the item specifics
        fruitsButton.setOnClickListener{
            val builder = AlertDialog.Builder(this)
            builder.setTitle("Add Food Item")
            builder.setMessage("Please enter the name of the food item:")

            // Set up the input field
            val input = EditText(this)
            input.inputType = InputType.TYPE_CLASS_TEXT
            builder.setView(input)

            // Set up the OK and Cancel buttons
            builder.setPositiveButton("OK") { dialog, which ->
                val foodItemName = input.text.toString().trim()
                if (isValidFoodItemName(foodItemName)) {
                    handleFoodItemName(foodItemName)
                    Log.d("TAG", foodItemName)
                    val intent = Intent(this, ScannerPage::class.java)
                    intent.putExtra("key", foodItemName)
                    intent.putExtra("key2", "Fruits")
                    startActivity(intent)
                } else {
                    // Show an error message if the entered food item name is invalid
                    Toast.makeText(this, "Invalid food item name", Toast.LENGTH_SHORT).show()
                }
            }
            builder.setNegativeButton("Cancel") { dialog, which ->
                dialog.cancel()
            }

            // Show the alert dialog
            builder.show()
        }

        vegiButton.setOnClickListener{
            val builder = AlertDialog.Builder(this)
            builder.setTitle("Add Food Item")
            builder.setMessage("Please enter the name of the food item:")

            // Set up the input field
            val input = EditText(this)
            input.inputType = InputType.TYPE_CLASS_TEXT
            builder.setView(input)

            // Set up the OK and Cancel buttons
            builder.setPositiveButton("OK") { dialog, which ->
                val foodItemName = input.text.toString().trim()
                if (isValidFoodItemName(foodItemName)) {
                    handleFoodItemName(foodItemName)
                    Log.d("TAG", foodItemName)
                    val intent = Intent(this, ScannerPage::class.java)
                    intent.putExtra("key", foodItemName)
                    intent.putExtra("key2", "Vegetable")
                    startActivity(intent)
                } else {
                    // Show an error message if the entered food item name is invalid
                    Toast.makeText(this, "Invalid food item name", Toast.LENGTH_SHORT).show()
                }
            }
            builder.setNegativeButton("Cancel") { dialog, which ->
                dialog.cancel()
            }

            // Show the alert dialog
            builder.show()
        }

        meatButton.setOnClickListener{
            val builder = AlertDialog.Builder(this)
            builder.setTitle("Add Food Item")
            builder.setMessage("Please enter the name of the food item:")

            // Set up the input field
            val input = EditText(this)
            input.inputType = InputType.TYPE_CLASS_TEXT
            builder.setView(input)

            // Set up the OK and Cancel buttons
            builder.setPositiveButton("OK") { dialog, which ->
                val foodItemName = input.text.toString().trim()
                if (isValidFoodItemName(foodItemName)) {
                    handleFoodItemName(foodItemName)
                    Log.d("TAG", foodItemName)
                    val intent = Intent(this, ScannerPage::class.java)
                    intent.putExtra("key", foodItemName)
                    intent.putExtra("key2", "Meat")
                    startActivity(intent)
                } else {
                    // Show an error message if the entered food item name is invalid
                    Toast.makeText(this, "Invalid food item name", Toast.LENGTH_SHORT).show()
                }
            }
            builder.setNegativeButton("Cancel") { dialog, which ->
                dialog.cancel()
            }

            // Show the alert dialog
            builder.show()
        }

        seafoodButton.setOnClickListener{
            val builder = AlertDialog.Builder(this)
            builder.setTitle("Add Food Item")
            builder.setMessage("Please enter the name of the food item:")

            // Set up the input field
            val input = EditText(this)
            input.inputType = InputType.TYPE_CLASS_TEXT
            builder.setView(input)

            // Set up the OK and Cancel buttons
            builder.setPositiveButton("OK") { dialog, which ->
                val foodItemName = input.text.toString().trim()
                if (isValidFoodItemName(foodItemName)) {
                    handleFoodItemName(foodItemName)
                    Log.d("TAG", foodItemName)
                    val intent = Intent(this, ScannerPage::class.java)
                    intent.putExtra("key", foodItemName)
                    intent.putExtra("key2", "Seafood")
                    startActivity(intent)
                } else {
                    // Show an error message if the entered food item name is invalid
                    Toast.makeText(this, "Invalid food item name", Toast.LENGTH_SHORT).show()
                }
            }
            builder.setNegativeButton("Cancel") { dialog, which ->
                dialog.cancel()
            }

            // Show the alert dialog
            builder.show()
        }

        dariyButton.setOnClickListener{
            val builder = AlertDialog.Builder(this)
            builder.setTitle("Add Food Item")
            builder.setMessage("Please enter the name of the food item:")

            // Set up the input field
            val input = EditText(this)
            input.inputType = InputType.TYPE_CLASS_TEXT
            builder.setView(input)

            // Set up the OK and Cancel buttons
            builder.setPositiveButton("OK") { dialog, which ->
                val foodItemName = input.text.toString().trim()
                if (isValidFoodItemName(foodItemName)) {
                    handleFoodItemName(foodItemName)
                    Log.d("TAG", foodItemName)
                    val intent = Intent(this, ScannerPage::class.java)
                    intent.putExtra("key", foodItemName)
                    intent.putExtra("key2", "Dairy")
                    startActivity(intent)
                } else {
                    // Show an error message if the entered food item name is invalid
                    Toast.makeText(this, "Invalid food item name", Toast.LENGTH_SHORT).show()
                }
            }
            builder.setNegativeButton("Cancel") { dialog, which ->
                dialog.cancel()
            }

            // Show the alert dialog
            builder.show()
        }

        grainsButton.setOnClickListener{
            val builder = AlertDialog.Builder(this)
            builder.setTitle("Add Food Item")
            builder.setMessage("Please enter the name of the food item:")

            // Set up the input field
            val input = EditText(this)
            input.inputType = InputType.TYPE_CLASS_TEXT
            builder.setView(input)

            // Set up the OK and Cancel buttons
            builder.setPositiveButton("OK") { dialog, which ->
                val foodItemName = input.text.toString().trim()
                if (isValidFoodItemName(foodItemName)) {
                    handleFoodItemName(foodItemName)
                    Log.d("TAG", foodItemName)
                    val intent = Intent(this, ScannerPage::class.java)
                    intent.putExtra("key", foodItemName)
                    intent.putExtra("key2", "Grains")
                    startActivity(intent)
                } else {
                    // Show an error message if the entered food item name is invalid
                    Toast.makeText(this, "Invalid food item name", Toast.LENGTH_SHORT).show()
                }
            }
            builder.setNegativeButton("Cancel") { dialog, which ->
                dialog.cancel()
            }

            // Show the alert dialog
            builder.show()
        }

        canButton.setOnClickListener{
            val builder = AlertDialog.Builder(this)
            builder.setTitle("Add Food Item")
            builder.setMessage("Please enter the name of the food item:")

            // Set up the input field
            val input = EditText(this)
            input.inputType = InputType.TYPE_CLASS_TEXT
            builder.setView(input)

            // Set up the OK and Cancel buttons
            builder.setPositiveButton("OK") { dialog, which ->
                val foodItemName = input.text.toString().trim()
                if (isValidFoodItemName(foodItemName)) {
                    handleFoodItemName(foodItemName)
                    Log.d("TAG", foodItemName)
                    val intent = Intent(this, ScannerPage::class.java)
                    intent.putExtra("key", foodItemName)
                    intent.putExtra("key2", "Canned Goods")
                    startActivity(intent)
                } else {
                    // Show an error message if the entered food item name is invalid
                    Toast.makeText(this, "Invalid food item name", Toast.LENGTH_SHORT).show()
                }
            }
            builder.setNegativeButton("Cancel") { dialog, which ->
                dialog.cancel()
            }

            // Show the alert dialog
            builder.show()
        }

        snacksButton.setOnClickListener{
            val builder = AlertDialog.Builder(this)
            builder.setTitle("Add Food Item")
            builder.setMessage("Please enter the name of the food item:")

            // Set up the input field
            val input = EditText(this)
            input.inputType = InputType.TYPE_CLASS_TEXT
            builder.setView(input)

            // Set up the OK and Cancel buttons
            builder.setPositiveButton("OK") { dialog, which ->
                val foodItemName = input.text.toString().trim()
                if (isValidFoodItemName(foodItemName)) {
                    handleFoodItemName(foodItemName)
                    Log.d("TAG", foodItemName)
                    val intent = Intent(this, ScannerPage::class.java)
                    intent.putExtra("key", foodItemName)
                    intent.putExtra("key2", "Snacks")
                    startActivity(intent)
                } else {
                    // Show an error message if the entered food item name is invalid
                    Toast.makeText(this, "Invalid food item name", Toast.LENGTH_SHORT).show()
                }
            }
            builder.setNegativeButton("Cancel") { dialog, which ->
                dialog.cancel()
            }

            // Show the alert dialog
            builder.show()
        }

        beveragesButton.setOnClickListener{
            val builder = AlertDialog.Builder(this)
            builder.setTitle("Add Food Item")
            builder.setMessage("Please enter the name of the food item:")

            // Set up the input field
            val input = EditText(this)
            input.inputType = InputType.TYPE_CLASS_TEXT
            builder.setView(input)

            // Set up the OK and Cancel buttons
            builder.setPositiveButton("OK") { dialog, which ->
                val foodItemName = input.text.toString().trim()
                if (isValidFoodItemName(foodItemName)) {
                    handleFoodItemName(foodItemName)
                    Log.d("TAG", foodItemName)
                    val intent = Intent(this, ScannerPage::class.java)
                    intent.putExtra("key", foodItemName)
                    intent.putExtra("key2", "Beverages")
                    startActivity(intent)
                } else {
                    // Show an error message if the entered food item name is invalid
                    Toast.makeText(this, "Invalid food item name", Toast.LENGTH_SHORT).show()
                }
            }
            builder.setNegativeButton("Cancel") { dialog, which ->
                dialog.cancel()
            }

            // Show the alert dialog
            builder.show()
        }

        condimentsButton.setOnClickListener{
            val builder = AlertDialog.Builder(this)
            builder.setTitle("Add Food Item")
            builder.setMessage("Please enter the name of the food item:")

            // Set up the input field
            val input = EditText(this)
            input.inputType = InputType.TYPE_CLASS_TEXT
            builder.setView(input)

            // Set up the OK and Cancel buttons
            builder.setPositiveButton("OK") { dialog, which ->
                val foodItemName = input.text.toString().trim()
                if (isValidFoodItemName(foodItemName)) {
                    handleFoodItemName(foodItemName)
                    Log.d("TAG", foodItemName)
                    val intent = Intent(this, ScannerPage::class.java)
                    intent.putExtra("key", foodItemName)
                    intent.putExtra("key2", "Condiments")
                    startActivity(intent)
                } else {
                    // Show an error message if the entered food item name is invalid
                    Toast.makeText(this, "Invalid food item name", Toast.LENGTH_SHORT).show()
                }
            }
            builder.setNegativeButton("Cancel") { dialog, which ->
                dialog.cancel()
            }

            // Show the alert dialog
            builder.show()
        }

        bakedGoodsButton.setOnClickListener{
            val builder = AlertDialog.Builder(this)
            builder.setTitle("Add Food Item")
            builder.setMessage("Please enter the name of the food item:")

            // Set up the input field
            val input = EditText(this)
            input.inputType = InputType.TYPE_CLASS_TEXT
            builder.setView(input)

            // Set up the OK and Cancel buttons
            builder.setPositiveButton("OK") { dialog, which ->
                val foodItemName = input.text.toString().trim()
                if (isValidFoodItemName(foodItemName)) {
                    handleFoodItemName(foodItemName)
                    Log.d("TAG", foodItemName)
                    val intent = Intent(this, ScannerPage::class.java)
                    intent.putExtra("key", foodItemName)
                    intent.putExtra("key2", "Baked Goods")
                    startActivity(intent)
                } else {
                    // Show an error message if the entered food item name is invalid
                    Toast.makeText(this, "Invalid food item name", Toast.LENGTH_SHORT).show()
                }
            }
            builder.setNegativeButton("Cancel") { dialog, which ->
                dialog.cancel()
            }

            // Show the alert dialog
            builder.show()
        }

        frozenFoodButton.setOnClickListener{
            val builder = AlertDialog.Builder(this)
            builder.setTitle("Add Food Item")
            builder.setMessage("Please enter the name of the food item:")

            // Set up the input field
            val input = EditText(this)
            input.inputType = InputType.TYPE_CLASS_TEXT
            builder.setView(input)

            // Set up the OK and Cancel buttons
            builder.setPositiveButton("OK") { dialog, which ->
                val foodItemName = input.text.toString().trim()
                if (isValidFoodItemName(foodItemName)) {
                    handleFoodItemName(foodItemName)
                    Log.d("TAG", foodItemName)
                    val intent = Intent(this, ScannerPage::class.java)
                    intent.putExtra("key", foodItemName)
                    intent.putExtra("key2", "Frozen Foods")
                    startActivity(intent)
                } else {
                    // Show an error message if the entered food item name is invalid
                    Toast.makeText(this, "Invalid food item name", Toast.LENGTH_SHORT).show()
                }
            }
            builder.setNegativeButton("Cancel") { dialog, which ->
                dialog.cancel()
            }

            // Show the alert dialog
            builder.show()
        }

        foodPrepMealsButton.setOnClickListener{
            val builder = AlertDialog.Builder(this)
            builder.setTitle("Add Food Item")
            builder.setMessage("Please enter the name of the food item:")

            // Set up the input field
            val input = EditText(this)
            input.inputType = InputType.TYPE_CLASS_TEXT
            builder.setView(input)

            // Set up the OK and Cancel buttons
            builder.setPositiveButton("OK") { dialog, which ->
                val foodItemName = input.text.toString().trim()
                if (isValidFoodItemName(foodItemName)) {
                    handleFoodItemName(foodItemName)
                    Log.d("TAG", foodItemName)
                    val intent = Intent(this, ScannerPage::class.java)
                    intent.putExtra("key", foodItemName)
                    intent.putExtra("key2", "Prepped Meals")
                    startActivity(intent)
                } else {
                    // Show an error message if the entered food item name is invalid
                    Toast.makeText(this, "Invalid food item name", Toast.LENGTH_SHORT).show()
                }
            }
            builder.setNegativeButton("Cancel") { dialog, which ->
                dialog.cancel()
            }

            // Show the alert dialog
            builder.show()
        }

        babyFoodButton.setOnClickListener{
            val builder = AlertDialog.Builder(this)
            builder.setTitle("Add Food Item")
            builder.setMessage("Please enter the name of the food item:")

            // Set up the input field
            val input = EditText(this)
            input.inputType = InputType.TYPE_CLASS_TEXT
            builder.setView(input)

            // Set up the OK and Cancel buttons
            builder.setPositiveButton("OK") { dialog, which ->
                val foodItemName = input.text.toString().trim()
                if (isValidFoodItemName(foodItemName)) {
                    handleFoodItemName(foodItemName)
                    Log.d("TAG", foodItemName)
                    val intent = Intent(this, ScannerPage::class.java)
                    intent.putExtra("key", foodItemName)
                    intent.putExtra("key2", "Baby Food")
                    startActivity(intent)
                } else {
                    // Show an error message if the entered food item name is invalid
                    Toast.makeText(this, "Invalid food item name", Toast.LENGTH_SHORT).show()
                }
            }
            builder.setNegativeButton("Cancel") { dialog, which ->
                dialog.cancel()
            }

            // Show the alert dialog
            builder.show()
        }

        petFoodButton.setOnClickListener{
            val builder = AlertDialog.Builder(this)
            builder.setTitle("Add Food Item")
            builder.setMessage("Please enter the name of the food item:")

            // Set up the input field
            val input = EditText(this)
            input.inputType = InputType.TYPE_CLASS_TEXT
            builder.setView(input)

            // Set up the OK and Cancel buttons
            builder.setPositiveButton("OK") { dialog, which ->
                val foodItemName = input.text.toString().trim()
                if (isValidFoodItemName(foodItemName)) {
                    handleFoodItemName(foodItemName)
                    Log.d("TAG", foodItemName)
                    val intent = Intent(this, ScannerPage::class.java)
                    intent.putExtra("key", foodItemName)
                    intent.putExtra("key2", "Pet Food")
                    startActivity(intent)
                } else {
                    // Show an error message if the entered food item name is invalid
                    Toast.makeText(this, "Invalid food item name", Toast.LENGTH_SHORT).show()
                }
            }
            builder.setNegativeButton("Cancel") { dialog, which ->
                dialog.cancel()
            }

            // Show the alert dialog
            builder.show()
        }

        otherFoodButton.setOnClickListener{
            val builder = AlertDialog.Builder(this)
            builder.setTitle("Add Food Item")
            builder.setMessage("Please enter the name of the food item:")

            // Set up the input field
            val input = EditText(this)
            input.inputType = InputType.TYPE_CLASS_TEXT
            builder.setView(input)

            // Set up the OK and Cancel buttons
            builder.setPositiveButton("OK") { dialog, which ->
                val foodItemName = input.text.toString().trim()
                if (isValidFoodItemName(foodItemName)) {
                    handleFoodItemName(foodItemName)
                    Log.d("TAG", foodItemName)
                    val intent = Intent(this, ScannerPage::class.java)
                    intent.putExtra("key", foodItemName)
                    intent.putExtra("key2", "Other Foods")
                    startActivity(intent)
                } else {
                    // Show an error message if the entered food item name is invalid
                    Toast.makeText(this, "Invalid food item name", Toast.LENGTH_SHORT).show()
                }
            }
            builder.setNegativeButton("Cancel") { dialog, which ->
                dialog.cancel()
            }

            // Show the alert dialog
            builder.show()
        }
    }

fun isValidFoodItemName(foodItemName: String): Boolean {
    // Implement your logic here to validate the food item name
    // For example, you could check if the name is not empty or too long
    return foodItemName.isNotBlank() && foodItemName.length <= 50
}

fun handleFoodItemName(foodItemName: String) {
    // Implement your logic here to handle the entered food item name
    // For example, you could add the food item name to a list, or save it to a database
    Toast.makeText(this, "Added $foodItemName to list", Toast.LENGTH_SHORT).show()
}
}