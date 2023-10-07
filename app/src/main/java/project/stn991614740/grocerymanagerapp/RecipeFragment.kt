package project.stn991614740.grocerymanagerapp

import android.content.ContentValues.TAG
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import project.stn991614740.grocerymanagerapp.databinding.FragmentRecipeBinding

// Fragment for displaying recipes
class RecipeFragment : Fragment() {

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
        val swipeCallback = object : ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT) {
            override fun onMove(
                recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder,
                target: RecyclerView.ViewHolder
            ): Boolean {
                return false // Don't support item movement
            }

            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                val position = viewHolder.adapterPosition
                val recipeToDelete = recipesList[position]

                // Create a confirmation dialog when an item is swiped
                AlertDialog.Builder(requireContext())
                    .setTitle("Delete Recipe")
                    .setMessage("Are you sure you want to delete this recipe?")
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
    }

    override fun onDestroyView() {
        super.onDestroyView()
        // Clear the binding object when the view is destroyed
        _binding = null
    }
}

// Data class to represent a recipe with an ID, creation date, and recipe text
data class Recipe(
    val id: String = "",
    val creationDate: com.google.firebase.Timestamp? = null,
    val recipe: String = ""
)
