package project.stn991614740.grocerymanagerapp

import android.content.ContentValues.TAG
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

class MyAdapter(private val myList: List<Food>) :
    RecyclerView.Adapter<MyAdapter.MyViewHolder>() {

    // Override onCreateViewHolder to inflate the item layout and create a MyViewHolder object.
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val itemView = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_layout, parent, false)
        return MyViewHolder(itemView)
    }

    // Override onBindViewHolder to set the values of the views in the layout based on the data in the list.
    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        // Retrieve the Food object at the current position.
        val myModel = myList[position]
        // Set the values of the views to the corresponding properties of the Food object.
        holder.textView.text = myModel.Category
        holder.textView1.text = myModel.Description
        holder.textView2.text = myModel.ExpirationDate
        holder.textView3.text = myModel.UID

        // Set the image based on the resource ID of the category image.
        val resourceId = holder.itemView.context.resources.getIdentifier(
            myModel.CategoryImage, "drawable", holder.itemView.context.packageName)
        holder.imageView.setImageResource(resourceId)

        // Set a click listener for the delete button to delete the corresponding document in Firestore.
        holder.deleteButton.setOnClickListener {
            val db = Firebase.firestore
            val UID = myModel.UID
            Log.d("LALALALALAL", UID)
            db.collection("food").document(UID).delete()
                .addOnSuccessListener { Log.d(TAG, "DocumentSnapshot successfully deleted!") }
                .addOnFailureListener { e -> Log.w(TAG, "Error deleting document", e) }
        }
    }

    // Override getItemCount to return the size of the list.
    override fun getItemCount() = myList.size

    // Define the MyViewHolder class to hold the views in the item layout.
    class MyViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val textView: TextView = itemView.findViewById(R.id.categoryView)
        val textView1: TextView = itemView.findViewById(R.id.descriptionView)
        val textView2: TextView = itemView.findViewById(R.id.expirationView)
        val textView3: TextView = itemView.findViewById(R.id.idView)
        val imageView: ImageView = itemView.findViewById(R.id.myImageView)
        val deleteButton: Button = itemView.findViewById(R.id.deleteButton)
    }
}
