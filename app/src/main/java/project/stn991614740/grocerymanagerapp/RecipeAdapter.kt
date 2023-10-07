package project.stn991614740.grocerymanagerapp

import android.content.ContentValues.TAG
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import project.stn991614740.grocerymanagerapp.databinding.RecipeItemBinding

class RecipeAdapter(private val recipes: List<Recipe>) : RecyclerView.Adapter<RecipeAdapter.RecipeViewHolder>() {

    var onRecipeClicked: ((Recipe) -> Unit)? = null

    // ViewHolder class to hold and bind the views using View Binding
    inner class RecipeViewHolder(private val binding: RecipeItemBinding) : RecyclerView.ViewHolder(binding.root) {

        init {
            itemView.setOnClickListener {
                val position = adapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    onRecipeClicked?.invoke(recipes[position])
                }
            }
        }

        // Bind the data to your views
        fun bind(recipe: Recipe) {
            // Convert the creationDate (Timestamp) to a readable date format
            val formattedDate = recipe.creationDate?.toDate()?.toString() ?: "Unknown Date"
            binding.creationDateTextView.text = formattedDate
            val lines = recipe.recipe.split("\n")
            val firstTwoLines = lines.take(3).joinToString("\n").trim()
            binding.recipeTextView.text = firstTwoLines
        }
    }

    // Inflate the item layout using View Binding and return the ViewHolder
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecipeViewHolder {
        val binding = RecipeItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return RecipeViewHolder(binding)
    }

    // Bind the data to the ViewHolder
    override fun onBindViewHolder(holder: RecipeViewHolder, position: Int) {
        Log.d(TAG, "Binding data for position $position: ${recipes[position]}")
        holder.bind(recipes[position])
    }

    // Return the total count of items
    override fun getItemCount() = recipes.size
}
