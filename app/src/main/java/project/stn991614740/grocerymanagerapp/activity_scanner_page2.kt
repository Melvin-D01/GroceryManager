package project.stn991614740.grocerymanagerapp

import android.app.Activity
import android.app.ProgressDialog
import android.content.ContentValues
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.view.Menu
import android.widget.EditText
import android.widget.ImageView
import android.widget.PopupMenu
import android.widget.Toast
import android.Manifest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.android.material.button.MaterialButton
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.TextRecognizer
import com.google.mlkit.vision.text.latin.TextRecognizerOptions

// firebase firestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

class activity_scanner_page2 : AppCompatActivity() {
    // UI Views
    private lateinit var inputImageBtn: MaterialButton
    private lateinit var recognizeTextBtn: MaterialButton
    private lateinit var imageIv: ImageView
    private lateinit var recognizedTextEt: EditText

    private companion object {
        // Handles the result of camera/gallery permissions in onRequestPermissionsResult
        private const val CAMERA_REQUEST_CODE = 100
        private const val STORAGE_REQUEST_CODE = 101
    }

    private var imageUri: Uri? = null

    private lateinit var cameraPermissions: Array<String>
    private lateinit var storagePermissions: Array<String>

    private lateinit var progressDialog: ProgressDialog

    private lateinit var textRecognizer: TextRecognizer


    // firestore
    val db = Firebase.firestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_scanner_page2)

        // Initialize UI Views
        inputImageBtn = findViewById(R.id.inputImageBtn)
        recognizeTextBtn = findViewById(R.id.recognizeTextBtn)
        imageIv = findViewById(R.id.imageIv)
        recognizedTextEt = findViewById(R.id.recognizedTextEt)

        // Initialize arrays of permissions required for camera and gallery
        cameraPermissions =
            arrayOf(Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE)
        storagePermissions = arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE)

        progressDialog = ProgressDialog(this)
        progressDialog.setTitle("Please wait")
        progressDialog.setCanceledOnTouchOutside(false)

        textRecognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)

        // Handle click, show input image dialog
        inputImageBtn.setOnClickListener {
            showInputImageDialog()
        }

        recognizeTextBtn.setOnClickListener {
            if (imageUri == null) {
                // Image not picked
                showToast("Please pick an image first")
            } else {
                // Image picked, recognize text
                recognizeTextFromImage()
            }
        }

    }

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

                    recognizedTextEt.setText(recognizedText)

                    val expiryDateString = recognizedTextEt.text.toString()
                    db.collection("expiryDate").document("new-expiry-date").set(expiryDateString)
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
        } else {
            // Cancelled
            showToast("Cancelled")
        }
    }

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

    private fun checkStoragePermission(): Boolean {
        // Check if storage permission is enabled
        return ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
        ) == PackageManager.PERMISSION_GRANTED
    }

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

    private fun requestStoragePermission() {
        // Request runtime storage permission
        ActivityCompat.requestPermissions(this, storagePermissions, STORAGE_REQUEST_CODE)
    }

    private fun requestCameraPermission() {
        // Request runtime camera permission
        ActivityCompat.requestPermissions(this, cameraPermissions, CAMERA_REQUEST_CODE)
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        // Handle permissions results
        when (requestCode) {
            CAMERA_REQUEST_CODE -> {
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
            STORAGE_REQUEST_CODE -> {
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

    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }
}