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
import androidx.recyclerview.widget.GridLayoutManager

class AddFragment : Fragment() {

    // A binding object instance, corresponding to the fragment layout
    private var _binding: FragmentAddBinding? = null

    // Get the current binding object instance
    private val binding get() = _binding!!

    // Inflate the layout for this fragment
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentAddBinding.inflate(inflater, container, false)
        return binding.root
    }

    // Called immediately after onCreateView has returned, perform any additional setup here
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Create a list of food categories for the grid
        val catDataSet = listOf(
            CatGridItem(R.drawable.fruits, "Fruit", "fruits"),
            CatGridItem(R.drawable.vegetable, "Vegetable", "vegetable"),
            CatGridItem(R.drawable.meat, "Meat", "meat"),
            CatGridItem(R.drawable.seafood, "Seafood", "seafood"),
            CatGridItem(R.drawable.dairy, "Dairy", "dairy"),
            CatGridItem(R.drawable.grains, "Grains", "grains"),
            CatGridItem(R.drawable.canfood, "Canned Goods", "canfood"),
            CatGridItem(R.drawable.snack, "Snacks", "snack"),
            CatGridItem(R.drawable.bev, "Beverages", "bev"),
            CatGridItem(R.drawable.condiments, "Condiments", "condiments"),
            CatGridItem(R.drawable.bakery, "Baked Goods", "bakery"),
            CatGridItem(R.drawable.frozenfood, "Frozen Foods", "frozenfood"),
            CatGridItem(R.drawable.bento, "Prepped Meals", "bento"),
            CatGridItem(R.drawable.babyfood, "Baby Food", "babyfood"),
            CatGridItem(R.drawable.petfood, "Pet Food", "petfood"),
            CatGridItem(R.drawable.menu, "Other Food", "menu")
        )

        // Set up a GridLayoutManager with 4 columns for the RecyclerView
        val numColumns = 4 // or however many columns you want to display
        binding.myRecyclerView.layoutManager = GridLayoutManager(requireContext(), numColumns)

        // Set the onClick listener for each grid item
        val onClick: (CatGridItem) -> Unit = { item ->
            addItem(item.text, item.imageName)
        }

        // Set up the RecyclerView adapter
        val myAdapter = CatGridAdapter(requireContext(), catDataSet, onClick)
        binding.myRecyclerView.adapter = myAdapter

    }

    // Function to validate the food item name entered by the user
    fun isValidFoodItemName(foodItemName: String): Boolean {
        // A name is valid if it's not blank and its length doesn't exceed 50
        return foodItemName.isNotBlank() && foodItemName.length <= 50
    }

    // Function to handle the addition of a new food item
    fun handleFoodItemName(foodItemName: String) {
        // Display a short message indicating that the food item has been added
        Toast.makeText(requireContext(), "Added $foodItemName to list", Toast.LENGTH_SHORT).show()
    }

    // Function to display a dialog box for the user to add a new food item
    fun addItem(category: String, image: String){
        // Prepare a dialog builder
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
                val directions = AddFragmentDirections.actionAddFragmentToScannerFragment(foodItemName, category, image)
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

    // Set _binding to null when the view is destroyed to avoid memory leaks
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }


}