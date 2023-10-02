package project.stn991614740.grocerymanagerapp

import android.Manifest
import android.app.Activity
import android.app.AlertDialog
import android.app.DatePickerDialog
import android.app.ProgressDialog
import android.content.ActivityNotFoundException
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
import android.speech.RecognizerIntent
import android.util.Log
import android.view.Menu
import android.widget.*
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.google.firebase.auth.ktx.auth
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.TextRecognizer
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*



class ScannerFragment : Fragment() {

    // UI Views
    // These variables are views used in the UI
    // Mostly Deprecated by Peter and viewbinding
    private lateinit var progressDialog: ProgressDialog
    private lateinit var textRecognizer: TextRecognizer
    private lateinit var imageIv: ImageView
    private lateinit var expDate: String

    private var imageUri: Uri? = null

    // UI Views
    private lateinit var datePickerDialog: DatePickerDialog
    private val dateFormat = SimpleDateFormat("dd-MM-yyyy", Locale.getDefault())

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
    private val auth = Firebase.auth  // Firebase Authentication instance

    // voice to text results for expiration date and sets text to output
    private val voiceActivityResultLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val matches: List<String>? = result.data?.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)
            val voiceInput = matches?.get(0) // Taking the first match which is typically the most accurate

            // take the voice input of the date and format it
            val prompt = "convert ${voiceInput} to the format of 'DD-MM-YYYY', making sure to remove any white spaces"
            getRepsonse(prompt){response ->
                activity?.runOnUiThread{
                    val formattedDate = response

                    // remove all whitespace in response
                    val expText = formattedDate.replace("\n","")
                    binding.expirationText.setText(expText.toString())

                }
            }

        }
    }

    // voice to text results for item description and sets text to output
    private val voiceActivityResultLauncher2 = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val matches: List<String>? = result.data?.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)
            val voiceInput = matches?.get(0) // Taking the first match which is typically the most accurate
            binding.testText.setText(voiceInput)
        }
    }

    // connect to internet
    private val client = OkHttpClient()

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

            // Parse expiration date from string to Date
            val expDate = dateFormat.parse(expText)

            // If the parsing was unsuccessful, expDate will be null
            if (expDate == null) {
                showToast("Invalid date format. Please use dd-MM-yyyy.")
                return@setOnClickListener
            }

            // sets the corresponding values and passes it to the hashmap to be uploaded to firestore
            val descriptionString = catText
            val categoryString = descText
            val expirationDate = com.google.firebase.Timestamp(expDate)
            val catImageString = data3

            val userId = auth.currentUser?.uid
            if (userId == null) {
                Log.d(TAG, "Error: no user signed in.")
                return@setOnClickListener
            }

            // collects the data and adds it to the firestore db
            val dF = db.collection("users").document(userId).collection("food").document().id

            // Get the Firestore collection reference
            val collectionRef = db.collection("users").document(userId).collection("food")

            // Set a custom ID for the new document
            val customId = dF

            // Create a new document with the custom ID
            val documentRef = collectionRef.document(customId)

            // Set the data for the new document
            val data = hashMapOf(
                "UID" to dF,
                "Description" to descriptionString,
                "Category" to categoryString,
                "ExpirationDate" to expirationDate,
                "CategoryImage" to catImageString
            )
            documentRef.set(data)
                .addOnSuccessListener { Log.d(TAG, "Document added with ID: $customId") }
                .addOnFailureListener { e -> Log.w(TAG, "Error adding document", e) }

            // once add is completed, it will take user to the MyFridge view and show updated list of food items
            findNavController().navigate(R.id.action_scannerFragment_to_fridgeFragment)
        }

        val calendar = Calendar.getInstance()
        datePickerDialog = DatePickerDialog(requireContext(), { _, year, monthOfYear, dayOfMonth ->
            val selectedDate = Calendar.getInstance()
            selectedDate.set(Calendar.YEAR, year)
            selectedDate.set(Calendar.MONTH, monthOfYear)
            selectedDate.set(Calendar.DAY_OF_MONTH, dayOfMonth)
            binding.expirationText.setText(dateFormat.format(selectedDate.time))
        }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH))

        // set default date to one week from today
        calendar.add(Calendar.DAY_OF_YEAR, 7)
        binding.expirationText.setText(dateFormat.format(calendar.time))

        binding.expirationBtn.setOnClickListener {
            datePickerDialog.show()
        }

        // listenerand voice to text call for the expiration voice to text btn
        binding.expirationVoiceBtn.setOnClickListener {
            // Launch voice-to-text input
            val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH)
            intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            intent.putExtra(RecognizerIntent.EXTRA_PROMPT, "Speak the expiration date")

            try {
                voiceActivityResultLauncher.launch(intent)
            } catch (e: ActivityNotFoundException) {
                showToast("Your device doesn't support voice input.")
            }
        }

        // listener and voice to text call for the item description voice to text btn
        binding.voiceBtn.setOnClickListener {
            // Launch voice-to-text input
            val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH)
            intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            intent.putExtra(RecognizerIntent.EXTRA_PROMPT, "Speak the food item name!")

            try {
                voiceActivityResultLauncher2.launch(intent)
            } catch (e: ActivityNotFoundException) {
                showToast("Your device doesn't support voice input.")
            }
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

    // Fetch a response from OpenAI's API.
    fun getRepsonse(promptText:String, callback: (String) -> Unit) {
        val apiKey = BuildConfig.OPENAI_API_KEY
        val url = "https://api.openai.com/v1/completions"

        val requestBody = """
            {
            "model": "gpt-3.5-turbo-instruct",
            "prompt": "$promptText",
            "max_tokens": 500,
            "temperature": 0
            }
        """.trimIndent()

        val request = Request.Builder()
            .url(url)
            .addHeader("Content-Type", "application/json")
            .addHeader("Authorization", "Bearer $apiKey")
            .post(requestBody.toRequestBody("application/json".toMediaTypeOrNull()))
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                Log.e("error", "API Failed", e)
            }

            override fun onResponse(call: Call, response: Response) {
                val body = response.body?.string()
                if (body != null) {
                    Log.v("data", body)
                } else {
                    Log.v("data","empty")
                }
                val jsonObject = JSONObject(body)
                val jsonArray: JSONArray =jsonObject.getJSONArray("choices")
                val textResult = jsonArray.getJSONObject(0).getString("text")
                callback(textResult)
            }
        })
    }

}