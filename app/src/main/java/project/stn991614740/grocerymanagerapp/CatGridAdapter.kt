package project.stn991614740.grocerymanagerapp

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import project.stn991614740.grocerymanagerapp.databinding.CategoryLayoutBinding

// Adapter class for the RecyclerView
class CatGridAdapter(
    private val context: Context, // Application context
    private val dataSet: List<CatGridItem>, // Data source for the RecyclerView
    private val onClick: (CatGridItem) -> Unit // Click event handler function
) : RecyclerView.Adapter<CatGridAdapter.CatGridViewHolder>() {

    // ViewHolder class for RecyclerView items
    inner class CatGridViewHolder(val binding: CategoryLayoutBinding) : RecyclerView.ViewHolder(binding.root) {
        // Method to bind data to the ViewHolder
        fun bind(item: CatGridItem) {
            binding.imageButton.setImageResource(item.imageResourceId)
            binding.textView.text = item.text
            binding.root.setOnClickListener { onClick(item) }
        }
    }

    // Method to create ViewHolder instances
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CatGridViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        val binding = CategoryLayoutBinding.inflate(layoutInflater, parent, false)
        return CatGridViewHolder(binding)
    }

    // Method to bind data to a ViewHolder
    override fun onBindViewHolder(holder: CatGridViewHolder, position: Int) {
        val currentItem = dataSet[position]
        holder.bind(currentItem)
    }

    // Method to get the size of the data source
    override fun getItemCount() = dataSet.size
}
