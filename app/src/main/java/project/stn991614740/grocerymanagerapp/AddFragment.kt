package project.stn991614740.grocerymanagerapp

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import project.stn991614740.grocerymanagerapp.databinding.FragmentAddBinding
import android.text.InputType
import android.util.Log
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.navigation.fragment.findNavController

class AddFragment : Fragment() {

    private var _binding: FragmentAddBinding? = null
    // This property is only valid between onCreateView and onDestroyView.
    private val binding get() = _binding!!


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentAddBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // listener for the add nav button
        binding.imageButton6.setOnClickListener{
            // Already on Add fragment
        }

        // listener for the MyFridge(home) nav button
        binding.imageButton5.setOnClickListener{
            findNavController().navigate(R.id.action_addFragment_to_fridgeFragment)
        }

        // listener for the settings nav button
        binding.imageButton7.setOnClickListener{

        }

        // listener for the category selection
        // also handles the sending the category selection along with an alert box that asks user to enter food item description
        // this may get replaced with machine learning portion in second half of the capstone project, where it will auto detect the item specifics
        // please note that it also saves a string that is used to load the associated category image as key3
        // key2 is for category and key takes what the user inputted into the alert box
        binding.fruitButton.setOnClickListener{
            addItem("Fruit", "fruits")
        }

        binding.vegiButton.setOnClickListener{
            addItem("Vegetable", "vegetable")
        }

        binding.meatButton.setOnClickListener{
            addItem("Meat", "meat")
        }

        binding.seafoodButton.setOnClickListener{
            addItem("Seafood", "seafood")
        }

        binding.dairyButton.setOnClickListener{
            addItem("Dairy", "dairy")
        }

        binding.grainsButton.setOnClickListener{
            addItem("Grains", "grains")
        }

        binding.canButton.setOnClickListener{
            addItem("Canned Goods", "canfood")
        }

        binding.snacksButton.setOnClickListener{
            addItem("Snacks", "snack")
        }

        binding.beveragesButton.setOnClickListener{
            addItem("Beverages", "bev")
        }

        binding.condimentsButton.setOnClickListener{
            addItem("Condiments", "condiments")
        }

        binding.bakedGoodsButton.setOnClickListener{
            addItem("Baked Goods", "bakery")
        }

        binding.frozenFoodsButton.setOnClickListener{
            addItem("Frozen Foods", "frozenfood")
        }

        binding.foodPrepMealsButton.setOnClickListener{
            addItem("Prepped Meals", "bento")
        }

        binding.babyFoodButton.setOnClickListener{
            addItem("Baby Food", "babyfood")
        }

        binding.petFoodButton.setOnClickListener{
            addItem("Pet Food", "petfood")
        }

        binding.condimentsButton.setOnClickListener{
            addItem("Other Food", "Menu")
        }

    }

    // function that checks if foodItemName entered by user is valid or not, check if its black or greater than 50 chars
    fun isValidFoodItemName(foodItemName: String): Boolean {
        // Implement your logic here to validate the food item name
        // For example, you could check if the name is not empty or too long
        return foodItemName.isNotBlank() && foodItemName.length <= 50
    }

    // function that tells user that food item was added to the list in MyFridge
    fun handleFoodItemName(foodItemName: String) {
        // Implement your logic here to handle the entered food item name
        // For example, you could add the food item name to a list, or save it to a database
        Toast.makeText(requireContext(), "Added $foodItemName to list", Toast.LENGTH_SHORT).show()
    }

    fun addItem(category: String, image: String){
        val builder = AlertDialog.Builder(requireContext())
        builder.setTitle("Add Food Item")
        builder.setMessage("Please enter the name of the food item:")

        // Set up the input field
        val input = EditText(requireContext())
        input.inputType = InputType.TYPE_CLASS_TEXT
        builder.setView(input)

        // Set up the OK button to alert box
        builder.setPositiveButton("OK") { dialog, which ->
            val foodItemName = input.text.toString().trim()
            if (isValidFoodItemName(foodItemName)) {
                handleFoodItemName(foodItemName)
                Log.d("TAG", foodItemName)
                val directions = AddFragmentDirections.actionAddFragmentToScannerFragment2(foodItemName, category, image)
                findNavController().navigate(directions)

            } else {
                // Show an error message if the entered food item name is invalid
                Toast.makeText(requireContext(), "Invalid food item name", Toast.LENGTH_SHORT).show()
            }
        }
        // Set Cancel button to alert box
        builder.setNegativeButton("Cancel") { dialog, which ->
            dialog.cancel()
        }

        // Show the alert dialog
        builder.show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }


}