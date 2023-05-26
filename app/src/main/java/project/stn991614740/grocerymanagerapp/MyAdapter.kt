package project.stn991614740.grocerymanagerapp

import android.app.AlertDialog
import android.content.ContentValues.TAG
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

class MyAdapter(private val myList: List<Food>, private val databaseUpdateListener: DatabaseUpdateListener) :
    RecyclerView.Adapter<MyAdapter.MyViewHolder>() {

    // Get the Food item at the specified position
    fun getItem(position: Int): Food {
        return myList[position]
    }

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
//        holder.textView3.text = myModel.UID  //Commented out for testing

        // Set the image based on the resource ID of the category image.
        val resourceId = holder.itemView.context.resources.getIdentifier(
            myModel.CategoryImage, "drawable", holder.itemView.context.packageName
        )
        holder.imageView.setImageResource(resourceId)

/*        // Set a click listener for the delete button to delete the corresponding document in Firestore.
        holder.deleteButton.setOnClickListener {
            val db = Firebase.firestore
            val UID = myModel.UID
            // Build an alert dialog to confirm that the user wants to delete the selected item.
            AlertDialog.Builder(holder.itemView.context)
                .setTitle("Delete Item")
                .setMessage("Are you sure you want to delete this item?")
                .setPositiveButton("Yes") { dialog, which ->
                    db.collection("food").document(UID).delete()
                        .addOnSuccessListener {
                            Log.d(TAG, "DocumentSnapshot successfully deleted!")
                            // Finish the current activity and start a new instance of FridgeActivity.
                            val intent = Intent(holder.itemView.context, FridgeActivity::class.java)
                            holder.itemView.context.startActivity(intent)
                            (holder.itemView.context as Activity).finish()
                        }
                        .addOnFailureListener { e ->
                            Log.w(TAG, "Error deleting document", e)
                        }
                }
                .setNegativeButton("No", null)
                .show()
        }*/

        // Set a click listener for the edit button to edit the corresponding document in Firestore.
        holder.editButton.setOnClickListener {
            // Prompt the user to edit the description and expiration date.
            val builder = AlertDialog.Builder(holder.itemView.context)
            builder.setTitle("Edit Item")
            val inputLayout = LinearLayout(holder.itemView.context)
            inputLayout.orientation = LinearLayout.VERTICAL
            val descriptionEditText = EditText(holder.itemView.context)
            descriptionEditText.hint = "Description"
            descriptionEditText.setText(myModel.Description)
            inputLayout.addView(descriptionEditText)
            val expirationEditText = EditText(holder.itemView.context)
            expirationEditText.hint = "Expiration Date (MM/DD/YYYY)"
            expirationEditText.setText(myModel.ExpirationDate)
            inputLayout.addView(expirationEditText)
            builder.setView(inputLayout)
            builder.setPositiveButton(android.R.string.ok) { _, _ ->
                val db = Firebase.firestore
                val UID = myModel.UID
                val description = descriptionEditText.text.toString().trim()
                val expirationDate = expirationEditText.text.toString().trim()
                val updateData = hashMapOf(
                    "Description" to description,
                    "ExpirationDate" to expirationDate
                )
                db.collection("food").document(UID).update(updateData as Map<String, Any>)
                    .addOnSuccessListener {
                        Log.d(TAG, "DocumentSnapshot successfully updated!")
                        // Notify the listener that the database has been updated.
                        databaseUpdateListener.onDatabaseUpdated()
                    }
                    .addOnFailureListener { e ->
                        Log.w(TAG, "Error updating document", e)
                    }
            }
            builder.setNegativeButton(android.R.string.cancel, null)
            builder.show()
        }

        // Reset the translation of the itemView
        holder.itemView.translationX = 0f
    }

    // Override getItemCount to return the size of the list.
    override fun getItemCount() = myList.size

    // Define the MyViewHolder class to hold the views in the item layout.
    class MyViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val textView: TextView = itemView.findViewById(R.id.categoryView)
        val textView1: TextView = itemView.findViewById(R.id.descriptionView)
        val textView2: TextView = itemView.findViewById(R.id.expirationView)
//        val textView3: TextView = itemView.findViewById(R.id.idView)
        val imageView: ImageView = itemView.findViewById(R.id.myImageView)
//        val deleteButton: Button = itemView.findViewById(R.id.deleteButton)
        val editButton: Button = itemView.findViewById(R.id.editButton)
    }

}
