// This line imports various Android packages required for the app
package project.stn991614740.grocerymanagerapp

import android.Manifest
import android.app.ProgressDialog
import android.content.ContentValues
import android.content.ContentValues.TAG
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.Menu
import android.widget.*
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.android.material.button.MaterialButton
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.TextRecognizer
import com.google.mlkit.vision.text.latin.TextRecognizerOptions

// This is the main class of the app that extends AppCompatActivity class
class ScannerPage : AppCompatActivity() {

    // UI Views
    // These variables are views used in the UI
    private lateinit var inputImageBtn: MaterialButton
    private lateinit var imageIv: ImageView
    private lateinit var addButton: ImageButton
    private lateinit var fridgeButton: ImageButton
    private lateinit var settingsButton: ImageButton
    private lateinit var textTestEt: EditText
    private lateinit var categoryText: EditText
    private lateinit var expirationView: EditText
    private lateinit var textRecognizer: TextRecognizer
    private lateinit var addToFridgeButton: Button
    private lateinit var progressDialog: ProgressDialog
    private lateinit var expDate: String

    private var imageUri: Uri? = null

    // Camera and storage permission arrays
    // These variables are used to store the required permissions for camera and storage
    private lateinit var cameraPermissions: Array<String>
    private lateinit var storagePermissions: Array<String>

    // firestore
    // This variable is used to connect to the Firebase Firestore database
    private val db = Firebase.firestore

    companion object {
        // Handles the result of camera/gallery permissions in onRequestPermissionsResult
        // These are constants that are used to handle permission requests
        private const val CAMERA_REQUEST_CODE = 100
        private const val STORAGE_REQUEST_CODE = 101
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_scanner_page)

        // Initialize UI Views
        // These variables are initialized with the views from the layout XML file
        inputImageBtn = findViewById(R.id.inputImageBtn)
        imageIv = findViewById(R.id.imageIv)
        addButton = findViewById(R.id.imageButton6)
        fridgeButton = findViewById(R.id.imageButton5)
        settingsButton = findViewById(R.id.imageButton7)
        textTestEt = findViewById(R.id.testText)
        categoryText = findViewById(R.id.categoryText)
        addToFridgeButton = findViewById(R.id.AddToFridgeButton)
        expirationView = findViewById(R.id.expirationText)

        // Initialize arrays of permissions required for camera and gallery
        // These variables are initialized with the permissions required for camera and storage
        cameraPermissions = arrayOf(
            Manifest.permission.CAMERA,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
        )
        storagePermissions = arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE)

        // Initialize progress dialog
        // This variable is initialized with a new ProgressDialog object
        progressDialog = ProgressDialog(this)
        progressDialog.setTitle("Please wait")
        progressDialog.setCanceledOnTouchOutside(false)

        // Initialize TextRecognizer
        // This variable is initialized with a new TextRecognizer object
        textRecognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)

        // Handle click, show input image dialog
        // This function sets a click listener to the inputImageBtn and calls showInpitImageDialog function
        inputImageBtn.setOnClickListener {
            showInputImageDialog()
        }

        // Handle click, add a new food item and takes you do the category selection screen also known as Add activity
        // This function sets a click listener to the addBtn and starts a new activity
        addButton.setOnClickListener {
            val intent = Intent(this, AddActivity::class.java)
            startActivity(intent)
        }

        // Handle click, takes you to MyFridge activity and shows list of sorted food items
        // This function sets a click listener to the fridgeBtn and starts a new activity
        fridgeButton.setOnClickListener {
            val intent = Intent(this, FridgeActivity::class.java)
            startActivity(intent)
        }

        // Handle click, takes you to settings activity currently under development
        // This function sets a click listener to the settingsBtn and starts a new activity
        settingsButton.setOnClickListener {
            val intent = Intent(this, SettingsActivity::class.java)
            startActivity(intent)
        }

        // passing data by setting intent with extra method
        val data = intent.getStringExtra("key")
        textTestEt.setText(data)
        val data2 = intent.getStringExtra("key2")
        categoryText.setText(data2)
        val data3 = intent.getStringExtra("key3")


        // Handle click, takes you to MyFridge activity and adds what user had inputted
        // This function sets a click listener to the addToFridgeButton and starts a new activity
        addToFridgeButton.setOnClickListener {

            // these sets the text from category and description inputted by the user from the previous step
            val catText = textTestEt.text.toString()
            val descText = categoryText.text.toString()
            val expText =  expirationView.text.toString()

            // sets the corresponding values and passes it to the hashmap to be uploaded to firestore
            val descriptionString = catText
            val categoryString = descText
            val expirationString = expText
            val catImageString = data3

            // collects the data and adds it to the firestore db
            val dF = db.collection("food").document().id

            // Get the Firestore collection reference
            val collectionRef = Firebase.firestore.collection("food")

            // Set a custom ID for the new document
            val customId = dF

            // Create a new document with the custom ID
            val documentRef = collectionRef.document(customId)

            // Set the data for the new document
            val data = hashMapOf(
                "UID" to dF,
                "Description" to descriptionString,
                "Category" to categoryString,
                "ExpirationDate" to expirationString,
                "CategoryImage" to catImageString
            )
            documentRef.set(data)
                .addOnSuccessListener { Log.d(TAG, "Document added with ID: $customId") }
                .addOnFailureListener { e -> Log.w(TAG, "Error adding document", e) }

            // once add is completed, it will take user to the MyFridge view and show updated list of food items
            val intent = Intent(this, FridgeActivity::class.java)
            startActivity(intent)
        }
    }

    // function that handles image to text recognition
    private fun recognizeTextFromImage() {
        // Set message and show progress dialog
        progressDialog.setMessage("Recognizing text...")
        progressDialog.show()

        try {
            val inputImage = InputImage.fromFilePath(this, imageUri!!)

            progressDialog.setMessage("Recognizing text from image...")

            val textTaskResult = textRecognizer.process(inputImage)
                .addOnSuccessListener { text ->
                    // Task completed successfully

                    progressDialog.dismiss()

                    val recognizedText = text.text

                    var obj = ExpirationDateParser()

                    var output = obj.parseExpirationDate(recognizedText).toString()

                    expDate = output

                    expirationView.setText(output)

                }
                .addOnFailureListener { e ->
                    // Task failed with an exception

                    progressDialog.dismiss()
                    showToast("Failed to recognize text due to: ${e.message}")
                }
        } catch (e: Exception) {
            progressDialog.dismiss()
            showToast("Failed to prepare image due to: ${e.message}")
        }
    }

    // function that prompts the user an option to upload image via gallery or snapping a picture
    private fun showInputImageDialog() {
        val popupMenu = PopupMenu(this, inputImageBtn)

        popupMenu.menu.add(Menu.NONE, 1, 1, "Camera")
        popupMenu. menu.add(Menu.NONE, 2, 2, "Gallery")
        // Show popup menu
        popupMenu.show()
        // Handle popup menu item clicks
        popupMenu.setOnMenuItemClickListener { menuItem ->
            // Get item id that is clicked from popup menu
            val id = menuItem.itemId
            if (id == 1) {
                // Camera clicked
                if (!checkCameraPermission()) {
                    // Camera permission not allowed, request it
                    requestCameraPermission()
                } else {
                    // Permission allowed, pick image from camera
                    pickImageCamera()
                }
            } else if (id == 2) {
                // Gallery clicked
                if (!checkStoragePermission()) {
                    // Storage permission not allowed, request it
                    requestStoragePermission()
                } else {
                    // Permission allowed, pick image from gallery
                    pickImageGallery()
                }
            }
            return@setOnMenuItemClickListener true
        }
    }

    // function that handles an image that was picked from gallery
    private fun pickImageGallery() {
        val intent = Intent(Intent.ACTION_PICK)

        intent.type = "image/*"
        galleryActivityResultLauncher.launch(intent)
    }

    private val galleryActivityResultLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        // This will receive the image, if picked
        if (result.resultCode == RESULT_OK) {
            // Image picked
            val data = result.data
            imageUri = data!!.data
            // Set to imageView -> imageIv
            imageIv.setImageURI(imageUri)
            recognizeTextFromImage()
        } else {
            // Cancelled
            showToast("Cancelled")
        }
    }

    // function that handles an image that was taken with the camera
    private fun pickImageCamera() {
        // Readies the image data to store
        val values = ContentValues()
        values.put(MediaStore.Images.Media.TITLE, "NewPic")
        values.put(MediaStore.Images.Media.DESCRIPTION, "Image to text")
        // Image Uri
        imageUri = contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values)
        // Intent to start camera
        val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        intent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri)
        cameraActivityResultLauncher.launch(intent)
    }

    private val cameraActivityResultLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        // This will receive the image, if picked
        if (result.resultCode == RESULT_OK) {
            // Image picked
            // Set to imageView -> imageIv
            imageIv.setImageURI(imageUri)
        } else {
            // Cancelled
            showToast("Cancelled")
        }
    }

    // function that checks for storage permissions
    private fun checkStoragePermission(): Boolean {
        // Check if storage permission is enabled
        return ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
        ) == PackageManager.PERMISSION_GRANTED
    }

    // function that checks for camera permissions
    private fun checkCameraPermission(): Boolean {
        // Check if camera permission is enabled
        val cameraResult =
            ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.CAMERA
            ) == PackageManager.PERMISSION_GRANTED
        val storageResult = ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
        ) == PackageManager.PERMISSION_GRANTED
        return cameraResult && storageResult
    }

    // function that requests storage permissions
    private fun requestStoragePermission() {
        // Request runtime storage permission
        ActivityCompat.requestPermissions(this, storagePermissions,
            ScannerPage.STORAGE_REQUEST_CODE
        )
    }

    // function that request camera permissions
    private fun requestCameraPermission() {
        // Request runtime camera permission
        ActivityCompat.requestPermissions(this, cameraPermissions,
            ScannerPage.CAMERA_REQUEST_CODE
        )
    }

    // override function onRequestPermissionResult
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        // Handle permissions results
        when (requestCode) {
            ScannerPage.CAMERA_REQUEST_CODE -> {
                // Picking from camera
                if (grantResults.isNotEmpty()) {
                    // If allowed, pick image
                    val cameraAccepted = grantResults[0] == PackageManager.PERMISSION_GRANTED
                    val storageAccepted = grantResults[1] == PackageManager.PERMISSION_GRANTED
                    if (cameraAccepted && storageAccepted) {
                        pickImageCamera()
                    } else {
                        // Permission denied
                        showToast("Camera and Storage permissions are required")
                    }
                }
            }
            ScannerPage.STORAGE_REQUEST_CODE -> {
                // Picking from gallery
                if (grantResults.isNotEmpty()) {
                    // If allowed, pick image
                    val storageAccepted = grantResults[0] == PackageManager.PERMISSION_GRANTED
                    if (storageAccepted) {
                        pickImageGallery()
                    } else {
                        // Permission denied
                        showToast("Storage permissions are required")
                    }
                }
            }
        }
    }

    // function that shows a toast message
    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }
}