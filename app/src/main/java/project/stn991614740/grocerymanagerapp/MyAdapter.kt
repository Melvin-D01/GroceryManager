package project.stn991614740.grocerymanagerapp

import android.app.AlertDialog
import android.app.DatePickerDialog
import android.content.ContentValues.TAG
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.Timestamp
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList

class MyAdapter(private val myList: ArrayList<Food>, private val databaseUpdateListener: DatabaseUpdateListener) :
RecyclerView.Adapter<MyAdapter.MyViewHolder>() {

    // Get the Food item at the specified position
    fun getItem(position: Int): Food {
        return myList[position]
    }

    // Remove the Food item at the specified position
    fun removeItem(position: Int) {
        myList.removeAt(position)
        notifyItemRemoved(position)
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
        val dateFormat = SimpleDateFormat("dd-MM-yyyy", Locale.getDefault())
        val expirationDate: Date = myModel.ExpirationDate!!.toDate()
        holder.textView2.text = dateFormat.format(expirationDate)
        // Set the image based on the resource ID of the category image.
        val resourceId = holder.itemView.context.resources.getIdentifier(
            myModel.CategoryImage, "drawable", holder.itemView.context.packageName
        )
        holder.imageView.setImageResource(resourceId)

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
            val expirationButton = Button(holder.itemView.context)
            expirationButton.text = "Set Expiration Date"
            val dateFormat = SimpleDateFormat("dd-MM-yyyy", Locale.getDefault())
            var expirationDate: Date = myModel.ExpirationDate!!.toDate()
            expirationButton.setOnClickListener {
                val calendar = Calendar.getInstance()
                calendar.time = expirationDate
                val year = calendar.get(Calendar.YEAR)
                val month = calendar.get(Calendar.MONTH)
                val day = calendar.get(Calendar.DAY_OF_MONTH)
                val datePickerDialog = DatePickerDialog(
                    holder.itemView.context,
                    { _, year, month, dayOfMonth ->
                        calendar.set(year, month, dayOfMonth)
                        expirationDate = calendar.time
                        expirationButton.text = dateFormat.format(expirationDate)
                    }, year, month, day
                )
                datePickerDialog.show()
            }
            expirationButton.text = dateFormat.format(expirationDate)
            inputLayout.addView(expirationButton)
            builder.setView(inputLayout)
            builder.setPositiveButton(android.R.string.ok) { _, _ ->
                val db = Firebase.firestore
                val UID = myModel.UID
                val description = descriptionEditText.text.toString().trim()
                val updateData = hashMapOf(
                    "Description" to description,
                    "ExpirationDate" to Timestamp(expirationDate)
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
        val imageView: ImageView = itemView.findViewById(R.id.myImageView)
        val editButton: Button = itemView.findViewById(R.id.editButton)
    }
}
