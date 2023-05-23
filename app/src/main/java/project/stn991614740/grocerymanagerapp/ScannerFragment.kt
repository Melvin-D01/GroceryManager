package project.stn991614740.grocerymanagerapp

import android.Manifest
import android.app.ProgressDialog
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import project.stn991614740.grocerymanagerapp.databinding.FragmentScannerBinding
import android.content.ContentValues
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.provider.MediaStore
import android.util.Log
import android.view.Menu
import android.widget.*
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.TextRecognizer
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class ScannerFragment : Fragment() {

    // UI Views
    // These variables are views used in the UI
    // Mostly Deprecated by Peter and viewbinding
    private lateinit var progressDialog: ProgressDialog
    private lateinit var textRecognizer: TextRecognizer
    private lateinit var imageIv: ImageView
    private lateinit var expDate: String

    private var imageUri: Uri? = null



    // View Binding
    private var _binding: FragmentScannerBinding? = null
    // This property is only valid between onCreateView and onDestroyView.
    private val binding get() = _binding!!

    // Camera and storage permission arrays
    // These variables are used to store the required permissions for camera and storage
    private lateinit var cameraPermissions: Array<String>
    private lateinit var storagePermissions: Array<String>

    // firestore
    // This variable is used to connect to the Firebase Firestore database
    private val db = Firebase.firestore

    companion object {
        private const val TAG = "ScannerFragment"
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentScannerBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        imageIv = binding.imageIv

        val bundle = arguments

        // check if arguments arrived
        if (bundle == null) {
            Log.d("Confirmation", "Fragment 2 didn't receive any info")
            return
        }

        // assign arguments to value
        val args = ScannerFragmentArgs.fromBundle(bundle)

        // Initialize arrays of permissions required for camera and gallery
        // These variables are initialized with the permissions required for camera and storage
        cameraPermissions = arrayOf(
            Manifest.permission.CAMERA,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
        )
        storagePermissions = arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE)

        // Initialize progress dialog
        // This variable is initialized with a new ProgressDialog object
        progressDialog = ProgressDialog(requireContext())
        progressDialog.setTitle("Please wait")
        progressDialog.setCanceledOnTouchOutside(false)

        // Initialize TextRecognizer
        // This variable is initialized with a new TextRecognizer object
        textRecognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)

        // Handle click, show input image dialog
        // This function sets a click listener to the inputImageBtn and calls showInpitImageDialog function
        binding.inputImageBtn.setOnClickListener {
            showInputImageDialog()
        }


        // passing data by args from fragment
        val data = args.description
        binding.testText.setText(data)
        val data2 = args.category
        binding.categoryText.setText(data2)
        val data3 = args.image

        // Handle click, takes you to MyFridge activity and adds what user had inputted
        // This function sets a click listener to the addToFridgeButton and starts a new activity
        binding.AddToFridgeButton.setOnClickListener {

            // these sets the text from category and description inputted by the user from the previous step
            val catText = binding.testText.text.toString()
            val descText = binding.categoryText.text.toString()
            val expText =  binding.expirationText.text.toString()

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
            findNavController().navigate(R.id.action_scannerFragment_to_fridgeFragment)
        }
    }


    // function that prompts the user an option to upload image via gallery or snapping a picture
    private fun showInputImageDialog() {
        val popupMenu = PopupMenu(requireContext(), binding.inputImageBtn)

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

    // function that checks for camera permissions
    private fun checkCameraPermission(): Boolean {
        // Check if camera permission is enabled
        val cameraResult =
            ContextCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.CAMERA
            ) == PackageManager.PERMISSION_GRANTED
        val storageResult = ContextCompat.checkSelfPermission(
            requireContext(),
            Manifest.permission.WRITE_EXTERNAL_STORAGE
        ) == PackageManager.PERMISSION_GRANTED
        return cameraResult && storageResult
    }

    private val requestPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permissions ->
            if (permissions.all { it.value }) {
                // All permissions granted, you can continue with your function
            } else {
                // Not all permissions were granted
            }
        }


    // function that request camera permissions
    private fun requestCameraPermission() {
        // Request runtime camera permission
        requestPermissionLauncher.launch(cameraPermissions)
    }

    // function that checks for storage permissions
    private fun checkStoragePermission(): Boolean {
        // Check if storage permission is enabled
        return ContextCompat.checkSelfPermission(
            requireContext(),
            Manifest.permission.WRITE_EXTERNAL_STORAGE
        ) == PackageManager.PERMISSION_GRANTED
    }

    private fun pickImageCamera() {
        // Readies the image data to store
        val values = ContentValues()
        values.put(MediaStore.Images.Media.TITLE, "NewPic")
        values.put(MediaStore.Images.Media.DESCRIPTION, "Image to text")
        // Image Uri
        imageUri = requireContext().contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values)
        // Intent to start camera
        val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        intent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri)
        cameraActivityResultLauncher.launch(intent)
    }

    private val cameraActivityResultLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        // This will receive the image, if picked
        if (result.resultCode == AppCompatActivity.RESULT_OK) {
            // Image picked
            // Set to imageView -> imageIv
            imageIv.setImageURI(imageUri)
            // Coroutines delay
            lifecycleScope.launch {
                delay(1000) // Wait for 1 second
                recognizeTextFromImage()
            }
        } else {
            // Cancelled
            showToast("Cancelled")
        }
    }

    private val requestStoragePermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permissions ->
            if (permissions.all { it.value }) {
                // All permissions granted, you can continue with your function
            } else {
                // Not all permissions were granted
            }
        }

    private fun requestStoragePermission() {
        requestStoragePermissionLauncher.launch(storagePermissions)
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
        if (result.resultCode == AppCompatActivity.RESULT_OK) {
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

    // function that handles image to text recognition
    private fun recognizeTextFromImage() {

        Log.d(TAG, "Recognizing text...")  // Log start of recognition

        try {
            val inputImage = InputImage.fromFilePath(requireContext(), imageUri!!)

            val textTaskResult = textRecognizer.process(inputImage)
                .addOnSuccessListener { text ->
                    // Task completed successfully

                    val recognizedText = text.text

                    val obj = ExpirationDateParser()

                    val output = obj.parseExpirationDate(recognizedText).toString()

                    expDate = output

                    binding.expirationText.setText(output)

                    Log.d(TAG, "Text recognition completed successfully.")  // Log end of recognition

                }
                .addOnFailureListener { e ->
                    // Task failed with an exception
                    Log.w(TAG, "Failed to recognize text due to: ${e.message}")
                }
        } catch (e: Exception) {
            Log.w(TAG, "Failed to prepare image due to: ${e.message}")
        }
    }

    // function that shows a toast message
    private fun showToast(message: String) {
        Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
    }




}