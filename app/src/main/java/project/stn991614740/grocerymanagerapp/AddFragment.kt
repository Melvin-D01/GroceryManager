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


        val numColumns = 4 // or however many columns you want to display
        binding.myRecyclerView.layoutManager = GridLayoutManager(requireContext(), numColumns)


        val onClick: (CatGridItem) -> Unit = { item ->
            addItem(item.text, item.imageName)
        }

        val myAdapter = CatGridAdapter(requireContext(), catDataSet, onClick)
        binding.myRecyclerView.adapter = myAdapter

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