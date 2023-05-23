package project.stn991614740.grocerymanagerapp

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import project.stn991614740.grocerymanagerapp.databinding.CategoryLayoutBinding

class CatGridAdapter(
    private val context: Context,
    private val dataSet: List<CatGridItem>,
    private val onClick: (CatGridItem) -> Unit
) : RecyclerView.Adapter<CatGridAdapter.CatGridViewHolder>() {

    inner class CatGridViewHolder(val binding: CategoryLayoutBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(item: CatGridItem) {
            binding.imageButton.setImageResource(item.imageResourceId)
            binding.textView.text = item.text
            binding.root.setOnClickListener { onClick(item) }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CatGridViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        val binding = CategoryLayoutBinding.inflate(layoutInflater, parent, false)
        return CatGridViewHolder(binding)
    }

    override fun onBindViewHolder(holder: CatGridViewHolder, position: Int) {
        val currentItem = dataSet[position]
        holder.bind(currentItem)
    }

    override fun getItemCount() = dataSet.size
}
