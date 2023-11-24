package project.stn991614740.grocerymanagerapp

import android.content.ContentValues.TAG
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.functions.FirebaseFunctions
import com.google.firebase.ktx.Firebase
import project.stn991614740.grocerymanagerapp.databinding.FragmentRecipeBinding
import java.util.*

// Fragment for displaying recipes
class RecipeFragment : Fragment() {

    private val functions = FirebaseFunctions.getInstance()
    private var progressBar: ProgressBar? = null

    // Adapter for the recipes RecyclerView
    private lateinit var adapter: RecipeAdapter
    // List to hold fetched recipes
    private val recipesList = mutableListOf<Recipe>()

    // Property to get the current user's ID
    private val userId: String
        get() = FirebaseAuth.getInstance().currentUser?.uid ?: ""

    // Binding object for accessing the fragment's views
    private var _binding: FragmentRecipeBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout using View Binding
        _binding = FragmentRecipeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Set up the RecyclerView with a LinearLayoutManager
        binding.recipesRecyclerView.layoutManager = LinearLayoutManager(requireContext())

        // Set up the Firestore reference
        val db = FirebaseFirestore.getInstance()
        val recipesRef = db.collection("users").document(userId).collection("recipes")

        // Fetch the recipes from Firestore
        recipesRef.get()
            .addOnSuccessListener { documents ->
                // Log the number of fetched recipes
                Log.d(TAG, "Number of recipes fetched: ${documents.size()}")

                // Clear the recipesList and add the fetched recipes
                recipesList.clear()
                recipesList.addAll(documents.map { doc ->
                    val recipe = doc.toObject(Recipe::class.java)!!
                    recipe.copy(id = doc.id) // Add the document ID to the recipe object
                })

                // Populate the RecyclerView with the recipes or show an empty message
                if (recipesList.isNotEmpty()) {
                    adapter = RecipeAdapter(recipesList)
                    // Set up a click listener for each recipe item
                    adapter.onRecipeClicked = { recipe ->
                        AlertDialog.Builder(requireContext())
                            .setTitle("Recipe Details")
                            .setMessage(recipe.recipe)
                            .setPositiveButton("Close") { dialog, _ ->
                                dialog.dismiss()
                            }
                            .show()
                    }
                    binding.recipesRecyclerView.adapter = adapter
                    binding.noRecipesTextView.visibility = View.GONE
                    binding.recipesRecyclerView.visibility = View.VISIBLE
                } else {
                    // Show the "no recipes" message if the list is empty
                    binding.noRecipesTextView.visibility = View.VISIBLE
                    binding.recipesRecyclerView.visibility = View.GONE
                }
            }
            .addOnFailureListener { exception ->
                // Log any errors that occur when fetching recipes
                Log.e(TAG, "Error fetching recipes: ", exception)
            }

        // Set up the swipe-to-delete functionality
        // Define the swipe callback
        val swipeCallback = object : ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT) {
            override fun onMove(recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder, target: RecyclerView.ViewHolder): Boolean {
                return false  // Not needed as we're only implementing swipe to delete.
            }

            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                val position = viewHolder.adapterPosition
                val recipeToDelete = recipesList[position]

                // Create a confirmation dialog when an item is swiped
                AlertDialog.Builder(requireContext())
                    .setTitle("Delete Recipe")
                    .setMessage("Are you sure you want to delete this recipe?")
                    .setCancelable(false)  // Prevent dismissal by touch outside or back press
                    .setPositiveButton("Delete") { dialog, _ ->
                        // Delete the recipe from Firestore if the user confirms
                        recipesRef.document(recipeToDelete.id).delete()
                            .addOnSuccessListener {
                                // Remove the recipe from the list and notify the adapter
                                recipesList.removeAt(position)
                                adapter.notifyItemRemoved(position)
                            }
                            .addOnFailureListener { exception ->
                                // Log any errors that occur when deleting a recipe
                                Log.e(TAG, "Error deleting recipe: ", exception)
                            }
                        dialog.dismiss()
                    }
                    .setNegativeButton("Cancel") { dialog, _ ->
                        // If the user cancels the delete, restore the item in the RecyclerView
                        adapter.notifyItemChanged(position)
                        dialog.dismiss()
                    }
                    .show()
            }
        }

        // Attach the swipe callback to the RecyclerView
        val itemTouchHelper = ItemTouchHelper(swipeCallback)
        itemTouchHelper.attachToRecyclerView(binding.recipesRecyclerView)


        binding.addRecipeButton?.setOnClickListener {
            callOpenAIForRecipes()
            //fetchRecipeForExpiringItems()
        }

        progressBar = binding.loadingProgressBar
    }

    private fun callOpenAIForRecipes() {
        showLoadingIndicator()
        // Initialize the Cloud Function call
        functions.getHttpsCallable("callOpenAIForRecipes")
            .call()
            .addOnCompleteListener { task ->
                // Check if the Cloud Function call was successful
                if (!task.isSuccessful) {
                    val e = task.exception
                }

                // The function call returned successfully. Extract the recipe from the result
                val recipe = task.result?.data as? String

                // Construct an AlertDialog to display the AI-generated recipe
                val alertDialog = android.app.AlertDialog.Builder(context)
                    .setTitle("AI Generated Recipe Just For You!")
                    .setMessage(recipe)  // Display the recipe in the AlertDialog
                    .setPositiveButton("OK") { dialog, _ ->
                        // Dismiss the AlertDialog when the "OK" button is pressed
                        dialog.dismiss()
                    }
                    .setNegativeButton("Save") { dialog, _ ->
                        // Save the recipe if it's not null when the "Save" button is pressed
                        if (recipe != null) {
                            saveRecipeToDatabase(recipe)
                        }
                        // Dismiss the AlertDialog after saving
                        dialog.dismiss()
                    }
                    .create()

                // If you have a loading indicator shown before this function was called,
                // hide it now that the data has been fetched
                hideLoadingIndicator()

                // Finally, display the AlertDialog
                alertDialog.show()
            }
    }

    // Function that saves the AI-generated recipe
    fun saveRecipeToDatabase(recipeText: String) {
        val db = Firebase.firestore
        val recipeMap = hashMapOf(
            "recipe" to recipeText,
            "creationDate" to Calendar.getInstance().time
        )

        db.collection("users").document(userId).collection("recipes").add(recipeMap)
            .addOnSuccessListener { documentReference ->
                Toast.makeText(context, "Recipe saved successfully!", Toast.LENGTH_SHORT).show()
                update()
            }
            .addOnFailureListener { e ->
                Toast.makeText(context, "Error saving recipe: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }


    // Function that shows the loading indicator when the user is waiting for the AI recipe generator
    private fun showLoadingIndicator() {
        progressBar?.visibility = View.VISIBLE
    }

    // Function that hides the loading indicator when the AI recipe generator is done computing
    private fun hideLoadingIndicator() {
        progressBar?.visibility = View.GONE
    }

    private fun update() {
// Set up the Firestore reference
        val db = FirebaseFirestore.getInstance()
        val recipesRef = db.collection("users").document(userId).collection("recipes")

        // Fetch the recipes from Firestore
        recipesRef.get()
            .addOnSuccessListener { documents ->
                // Log the number of fetched recipes
                Log.d(TAG, "Number of recipes fetched: ${documents.size()}")

                // Clear the recipesList and add the fetched recipes
                recipesList.clear()
                recipesList.addAll(documents.map { doc ->
                    val recipe = doc.toObject(Recipe::class.java)!!
                    recipe.copy(id = doc.id) // Add the document ID to the recipe object
                })

                // Populate the RecyclerView with the recipes or show an empty message
                if (recipesList.isNotEmpty()) {
                    adapter = RecipeAdapter(recipesList)
                    // Set up a click listener for each recipe item
                    adapter.onRecipeClicked = { recipe ->
                        AlertDialog.Builder(requireContext())
                            .setTitle("Recipe Details")
                            .setMessage(recipe.recipe)
                            .setPositiveButton("Close") { dialog, _ ->
                                dialog.dismiss()
                            }
                            .show()
                    }
                    binding.recipesRecyclerView.adapter = adapter
                    binding.noRecipesTextView.visibility = View.GONE
                    binding.recipesRecyclerView.visibility = View.VISIBLE
                } else {
                    // Show the "no recipes" message if the list is empty
                    binding.noRecipesTextView.visibility = View.VISIBLE
                    binding.recipesRecyclerView.visibility = View.GONE
                }
            }
            .addOnFailureListener { exception ->
                // Log any errors that occur when fetching recipes
                Log.e(TAG, "Error fetching recipes: ", exception)
            }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        // Clear the binding object when the view is destroyed
        _binding = null
    }
}

