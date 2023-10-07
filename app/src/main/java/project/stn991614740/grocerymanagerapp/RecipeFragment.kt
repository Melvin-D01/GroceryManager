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

class RecipeFragment : Fragment() {

    private lateinit var adapter: RecipeAdapter
    private val recipesList = mutableListOf<Recipe>()

    private val userId: String
        get() = FirebaseAuth.getInstance().currentUser?.uid ?: ""

    private var _binding: FragmentRecipeBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentRecipeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.recipesRecyclerView.layoutManager = LinearLayoutManager(requireContext())

        val db = FirebaseFirestore.getInstance()
        val recipesRef = db.collection("users").document(userId).collection("recipes")

        recipesRef.get()
            .addOnSuccessListener { documents ->
                Log.d(TAG, "Number of recipes fetched: ${documents.size()}")

                recipesList.clear()
                recipesList.addAll(documents.map { doc ->
                    val recipe = doc.toObject(Recipe::class.java)!!
                    recipe.copy(id = doc.id)
                })

                if (recipesList.isNotEmpty()) {
                    adapter = RecipeAdapter(recipesList)
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
                    binding.noRecipesTextView.visibility = View.VISIBLE
                    binding.recipesRecyclerView.visibility = View.GONE
                }
            }
            .addOnFailureListener { exception ->
                Log.e(TAG, "Error fetching recipes: ", exception)
            }

        val swipeCallback = object : ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT) {
            override fun onMove(
                recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder,
                target: RecyclerView.ViewHolder
            ): Boolean {
                return false
            }

            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                val position = viewHolder.adapterPosition
                val recipeToDelete = recipesList[position]

                // Create a confirmation dialog
                AlertDialog.Builder(requireContext())
                    .setTitle("Delete Recipe")
                    .setMessage("Are you sure you want to delete this recipe?")
                    .setPositiveButton("Delete") { dialog, _ ->
                        // User confirmed, delete the recipe
                        recipesRef.document(recipeToDelete.id).delete()
                            .addOnSuccessListener {
                                recipesList.removeAt(position)
                                adapter.notifyItemRemoved(position)
                            }
                            .addOnFailureListener { exception ->
                                Log.e(TAG, "Error deleting recipe: ", exception)
                            }
                        dialog.dismiss()
                    }
                    .setNegativeButton("Cancel") { dialog, _ ->
                        // User canceled, do nothing
                        adapter.notifyItemChanged(position) // Restore item's position in the list
                        dialog.dismiss()
                    }
                    .show()
            }
        }

        val itemTouchHelper = ItemTouchHelper(swipeCallback)
        itemTouchHelper.attachToRecyclerView(binding.recipesRecyclerView)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}

data class Recipe(
    val id: String = "",
    val creationDate: com.google.firebase.Timestamp? = null,
    val recipe: String = ""
)
