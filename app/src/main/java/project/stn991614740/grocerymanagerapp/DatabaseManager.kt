package project.stn991614740.grocerymanagerapp

import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import java.util.*
import kotlin.collections.ArrayList

class DatabaseManager(private val userId: String) {

    private val db = Firebase.firestore
    private val userCollection = db.collection("users").document(userId).collection("food")

    fun fetchDataWithCategory(
        orderBy: String,
        isAscending: Boolean,
        category: String,
        onSuccess: (List<Food>) -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        val query = if (category == "All") {
            userCollection.orderBy(orderBy, if (isAscending) Query.Direction.ASCENDING else Query.Direction.DESCENDING)
        } else {
            userCollection.whereEqualTo("Category", category)
                .orderBy(orderBy, if (isAscending) Query.Direction.ASCENDING else Query.Direction.DESCENDING)
        }

        query.get()
            .addOnSuccessListener { documents ->
                val myList = ArrayList<Food>()
                for (document in documents) {
                    val myModel = documentToFood(document) // Updated line
                    if (myModel != null) myList.add(myModel) // Only add non-null values
                }
                onSuccess(myList)
            }
            .addOnFailureListener { exception ->
                onFailure(exception)
            }
    }

    fun fetchDataWithCategoryAndDate(
        orderBy: String,
        isAscending: Boolean,
        category: String,
        days: Int,
        onSuccess: (List<Food>) -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        val currentDate = Calendar.getInstance().time
        val targetDate = Calendar.getInstance().apply {
            add(Calendar.DAY_OF_MONTH, days)
        }.time

        val query = if (category == "All") {
            userCollection
                .whereGreaterThanOrEqualTo(orderBy, currentDate)
                .whereLessThanOrEqualTo(orderBy, targetDate)
                .orderBy(orderBy, if (isAscending) Query.Direction.ASCENDING else Query.Direction.DESCENDING)
        } else {
            userCollection
                .whereEqualTo("Category", category)
                .whereGreaterThanOrEqualTo(orderBy, currentDate)
                .whereLessThanOrEqualTo(orderBy, targetDate)
                .orderBy(orderBy, if (isAscending) Query.Direction.ASCENDING else Query.Direction.DESCENDING)
        }

        query.get()
            .addOnSuccessListener { documents ->
                val myList = ArrayList<Food>()
                for (document in documents) {
                    val myModel = documentToFood(document) // Updated line
                    if (myModel != null) myList.add(myModel) // Only add non-null values
                }
                onSuccess(myList)
            }
            .addOnFailureListener { exception ->
                onFailure(exception)
            }
    }

    fun fetchDataExpired(
        category: String,
        onSuccess: (List<Food>) -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        val currentDate = Calendar.getInstance().time

        val query = if (category == "All") {
            userCollection.whereLessThanOrEqualTo("ExpirationDate", currentDate)
        } else {
            userCollection.whereEqualTo("Category", category).whereLessThanOrEqualTo("ExpirationDate", currentDate)
        }

        query.get()
            .addOnSuccessListener { documents ->
                val myList = ArrayList<Food>()
                for (document in documents) {
                    val myModel = documentToFood(document) // Updated line
                    if (myModel != null) myList.add(myModel) // Only add non-null values
                }
                onSuccess(myList)
            }
            .addOnFailureListener { exception ->
                onFailure(exception)
            }
    }

    fun deleteItem(food: Food,
                   onSuccess: () -> Unit,
                   onFailure: (Exception) -> Unit) {
        userCollection.document(food.UID)
            .delete()
            .addOnSuccessListener {
                onSuccess()
            }
            .addOnFailureListener { e ->
                onFailure(e)
            }
    }

    private fun documentToFood(document: DocumentSnapshot): Food? {
        return try {
            val category = document.getString("Category") ?: ""
            val description = document.getString("Description") ?: ""
            val expirationDate = document.getTimestamp("ExpirationDate")
            val categoryImage = document.getString("CategoryImage") ?: ""
            val uid = document.id

            Food(category, description, expirationDate, categoryImage, uid)
        } catch (e: Exception) {
            null  // Return null if there's an error, which will be filtered out by mapNotNull
        }
    }

    fun addFoodItem(
        category: String,
        description: String,
        expirationDate: Date,
        categoryImage: String,
        onSuccess: () -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        val docId = userCollection.document().id
        val documentRef = userCollection.document(docId)

        val data = hashMapOf(
            "UID" to docId,
            "Description" to description,
            "Category" to category,
            "ExpirationDate" to Timestamp(expirationDate),
            "CategoryImage" to categoryImage
        )

        documentRef.set(data)
            .addOnSuccessListener {
                onSuccess()
            }
            .addOnFailureListener { exception ->
                onFailure(exception)
            }
    }

    fun deleteEntireFoodCollection(
        onSuccess: () -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        // Get a reference to the food collection
        val foodCollection = db.collection("users").document(userId).collection("food")

        // Fetch all documents to delete them
        foodCollection.get()
            .addOnSuccessListener { documents ->
                // Using a batch to delete multiple documents at once
                val batch = db.batch()

                // Loop through all documents and schedule them for deletion
                for (document in documents) {
                    batch.delete(document.reference)
                }

                // Commit the batch
                batch.commit().addOnSuccessListener {
                    onSuccess()
                }.addOnFailureListener { exception ->
                    onFailure(exception)
                }
            }
            .addOnFailureListener { exception ->
                onFailure(exception)
            }
    }
}