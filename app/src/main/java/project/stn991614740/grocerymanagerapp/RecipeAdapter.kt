package project.stn991614740.grocerymanagerapp

import android.content.ContentValues.TAG
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import project.stn991614740.grocerymanagerapp.databinding.RecipeItemBinding

// Adapter class for displaying a list of recipes in a RecyclerView
class RecipeAdapter(private val recipes: List<Recipe>) : RecyclerView.Adapter<RecipeAdapter.RecipeViewHolder>() {

    // Callback to be invoked when a recipe is clicked
    var onRecipeClicked: ((Recipe) -> Unit)? = null

    // ViewHolder class that represents each item in the list
    inner class RecipeViewHolder(private val binding: RecipeItemBinding) : RecyclerView.ViewHolder(binding.root) {

        // Initialize listener to handle item click
        init {
            itemView.setOnClickListener {
                val position = adapterPosition
                // Check if the clicked position is valid
                if (position != RecyclerView.NO_POSITION) {
                    // Trigger the callback with the clicked recipe
                    onRecipeClicked?.invoke(recipes[position])
                }
            }
        }

        // Bind the data of a single recipe to the UI elements
        fun bind(recipe: Recipe) {
            // Convert the creationDate (Timestamp) to a readable date format
            val formattedDate = recipe.creationDate?.toDate()?.toString() ?: "Unknown Date"
            binding.creationDateTextView.text = formattedDate

            // Display only the first three lines of the recipe
            val lines = recipe.recipe.split("\n")
            val firstTwoLines = lines.take(3).joinToString("\n").trim()
            binding.recipeTextView.text = firstTwoLines
        }
    }

    // Called when the RecyclerView needs a new ViewHolder
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecipeViewHolder {
        // Inflate the layout for each list item using View Binding
        val binding = RecipeItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return RecipeViewHolder(binding)
    }

    // Called by RecyclerView to bind the data at the specified position
    override fun onBindViewHolder(holder: RecipeViewHolder, position: Int) {
        // Log the data binding for debugging purposes
        Log.d(TAG, "Binding data for position $position: ${recipes[position]}")
        // Bind the recipe data to the ViewHolder's UI elements
        holder.bind(recipes[position])
    }

    // Returns the total number of recipes in the list
    override fun getItemCount() = recipes.size
}
