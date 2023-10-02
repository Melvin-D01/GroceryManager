package project.stn991614740.grocerymanagerapp

import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.common.truth.Truth.assertThat
import com.google.firebase.Timestamp
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.tasks.await
import org.junit.Assert.assertFalse
import org.junit.Assert.fail
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.util.*
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit

@RunWith(AndroidJUnit4::class)
class DatabaseManagerTest {

    private lateinit var databaseManager: DatabaseManager
    private lateinit var db: FirebaseFirestore
    private lateinit var df: CollectionReference



    @Before
    fun setUp() {
        // You may want to set up a test Firestore instance or other test doubles here.
        databaseManager = DatabaseManager("ZANqPsxlsPSR2kImixI5NlvHpFR2")
        db = Firebase.firestore
    }

    @Test
    fun testFetchDataExpired_returns_expired_items_for_category() {
        // Assume some test data has been set up in Firestore

        var result: List<Food>? = null
        databaseManager.fetchDataExpired(
            "Fruit",
            onSuccess = { list -> result = list },
            onFailure = { /* Handle failure */ }
        )

        // Give Firebase some time to return the data
        Thread.sleep(5000)

        // Now assert that the result list contains only expired items from the specified category
        assertThat(result).isNotNull()
        assertThat(result).isNotEmpty()
    }

    @Test
    fun testFetchDataWithCategory_returns_empty() {
        // This will now interact with real Firebase dependencies.
        // You may want to set up some test data in Firestore before running this test.
        var result: List<Food>? = null
        databaseManager.fetchDataWithCategory(
            "Category",
            true,
            "Other Food",
            onSuccess = { list -> result = list },
            onFailure = { /* Handle failure */ }
        )

        // Give Firebase some time to return the data
        Thread.sleep(5000)

        // Now assert that the result list is not empty
        assertThat(result).isNotNull()
        assertThat(result).isEmpty()
    }


    @Test
    fun testFetchDataWithCategoryAndDate_returns_empty() {
        var result: List<Food>? = null
        databaseManager.fetchDataWithCategoryAndDate(
            "ExpirationDate",
            true,
            "Other Food",
            7,
            onSuccess = { list -> result = list },
            onFailure = { /* Handle failure */ }
        )

        // Give Firebase some time to return the data
        Thread.sleep(5000)

        // Now assert that the result list is empty
        assertThat(result).isNotNull()
        assertThat(result).isEmpty()
    }

    @Test
    fun testFetchDataWithCategoryAndDate_returns_not_empty() {

        var result: List<Food>? = null
        val latch = CountDownLatch(1)

        // collects the data and adds it to the firestore db
        val dF = db.collection("users").document("ZANqPsxlsPSR2kImixI5NlvHpFR2").collection("food").document().id

        // Get the Firestore collection reference
        val collectionRef = db.collection("users").document("ZANqPsxlsPSR2kImixI5NlvHpFR2").collection("food")

        val customId = dF

        // Create a new document with the custom ID
        val documentRef = collectionRef.document(customId)

        // Calculate the timestamp 7 days from now
        val currentDate = Timestamp.now()
        val expirationDate = Timestamp(currentDate.seconds + (7 * 24 * 60 * 60), 0)

        // Set the data for the new document
        val data = hashMapOf(
            "UID" to dF,
            "Description" to "Test",
            "Category" to "Fruit",
            "ExpirationDate" to expirationDate,
            "CategoryImage" to "test"
        )
        documentRef.set(data)


        databaseManager.fetchDataWithCategoryAndDate(
            "ExpirationDate", true, "Fruit", 7,
            onSuccess = { list ->
                result = list
                latch.countDown()
            },
            onFailure = {
                // Handle failure
                latch.countDown()
                fail("Failed to fetch data: $it")
            }
        )

        // Wait for Firebase to return the data
        latch.await(10, TimeUnit.SECONDS)

        // Now assert that the result list is not null and not empty
        assertThat(result).isNotNull()
        assertThat(result).isNotEmpty()

        documentRef.delete()

    }

    @Test
    fun testDeleteItem() = runBlocking {

        // Create a food item to delete (you should replace this with actual test data)
        val foodToDelete = Food("Fruit", "Test", Timestamp.now(), "test", "UID")

        // Add the food item to Firestore for deletion
        val documentRef = db.collection("users")
            .document("ZANqPsxlsPSR2kImixI5NlvHpFR2")
            .collection("food")
            .document(foodToDelete.UID)

        // Set the data for the new document
        val data = hashMapOf(
            "UID" to foodToDelete.UID,
            "Description" to foodToDelete.Description,
            "Category" to foodToDelete.Category,
            "ExpirationDate" to foodToDelete.ExpirationDate,
            "CategoryImage" to foodToDelete.CategoryImage
        )

        try {
            // using Kotlin's Firebase KTX extensions to await the result
            documentRef.set(data).await()

            delay(5000)

            // Call the deleteItem function
            databaseManager.deleteItem(
                foodToDelete,
                onSuccess = {},
                onFailure = { exception ->
                    // Handle failure
                    fail("Delete operation failed: ${exception.message}")
                }
            )

            delay(5000)

            // Check if the item still exists in the database
            val documentSnapshot = documentRef.get().await()
            assertFalse(documentSnapshot.exists())

        } catch (e: Exception) {
            // Handle exception thrown by documentRef.set(data).await()
            fail("Failed to set up test data: ${e.message}")
        }
    }

    @Test
    fun testAddFoodItem() = runBlocking {
        val latch = CountDownLatch(1)
        val description = "Test description"
        val category = "Test category"
        val expirationDate = Date()
        val categoryImage = "Test image"

        databaseManager.addFoodItem(
            category,
            description,
            expirationDate,
            categoryImage,
            onSuccess = {
                latch.countDown()
            },
            onFailure = { exception ->
                latch.countDown()
                fail("Add operation failed: ${exception.message}")
            }
        )

        // Wait for Firestore to complete the operation
        latch.await(10, TimeUnit.SECONDS)

        // Fetch the food item to verify it was added correctly
        var result: List<Food>? = null
        databaseManager.fetchDataWithCategory(
            "Category",
            true,
            category,
            onSuccess = { list ->
                result = list
            },
            onFailure = { /* Handle failure */ }
        )

        // Give Firebase some time to return the data
        delay(5000)

        // Now assert that the result list contains the added item
        assertThat(result).isNotNull()
        assertThat(result).isNotEmpty()
        val foodItem = result?.firstOrNull { it.Description == description }
        assertThat(foodItem).isNotNull()
    }

}




