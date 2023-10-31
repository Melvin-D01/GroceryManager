package project.stn991614740.grocerymanagerapp

import android.app.Activity
import android.app.AlertDialog
import android.app.DatePickerDialog
import android.content.ContentValues.TAG
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList

class MyAdapter(
    private val myList: ArrayList<Food>,
    private val databaseUpdateListener: DatabaseUpdateListener,
    private val activity: Activity
) : RecyclerView.Adapter<MyAdapter.MyViewHolder>() {

    val user = FirebaseAuth.getInstance().currentUser
    val userId = user!!.uid

    companion object {
        const val MY_PERMISSIONS_REQUEST_RECORD_AUDIO = 1
        const val VOICE_RECOGNITION_REQUEST_CODE = 1001
    }

    fun getItem(position: Int): Food {
        return myList[position]
    }

    fun removeItem(position: Int) {
        myList.removeAt(position)
        notifyItemRemoved(position)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val itemView = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_layout, parent, false)
        return MyViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        val myModel = myList[position]
        holder.textView.text = myModel.Category
        holder.textView1.text = myModel.Description
        val dateFormat = SimpleDateFormat("dd-MM-yyyy", Locale.getDefault())
        val expirationDate: Date = myModel.ExpirationDate!!.toDate()
        holder.textView2.text = dateFormat.format(expirationDate)
        val resourceId = holder.itemView.context.resources.getIdentifier(
            myModel.CategoryImage, "drawable", holder.itemView.context.packageName
        )
        holder.imageView.setImageResource(resourceId)

        holder.editButton.setOnClickListener {
            val builder = AlertDialog.Builder(holder.itemView.context)
            builder.setTitle("Edit Item")
            val inputLayout = LinearLayout(holder.itemView.context)
            inputLayout.orientation = LinearLayout.VERTICAL
            inputLayout.layoutParams = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )

            // Description
            val descriptionLayout = LinearLayout(holder.itemView.context)
            descriptionLayout.orientation = LinearLayout.HORIZONTAL
            descriptionLayout.layoutParams = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
            val descriptionEditText = EditText(holder.itemView.context)
            descriptionEditText.hint = "Description"
            descriptionEditText.setText(myModel.Description)
            descriptionEditText.layoutParams = LinearLayout.LayoutParams(
                0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f
            )
            descriptionLayout.addView(descriptionEditText)

            val voiceInputButtonDescription = ImageButton(holder.itemView.context)
            voiceInputButtonDescription.setImageResource(android.R.drawable.ic_btn_speak_now)
            descriptionLayout.addView(voiceInputButtonDescription)
            inputLayout.addView(descriptionLayout)

            // Configure SpeechRecognizer for description
            val speechRecognizerDescription = SpeechRecognizer.createSpeechRecognizer(holder.itemView.context)
            val speechRecognizerIntentDescription = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH)
            speechRecognizerIntentDescription.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            speechRecognizerIntentDescription.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault())
            speechRecognizerIntentDescription.putExtra(RecognizerIntent.EXTRA_PROMPT, "Speak now...")
            voiceInputButtonDescription.setOnClickListener {
                if (ContextCompat.checkSelfPermission(holder.itemView.context, android.Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(activity, arrayOf(android.Manifest.permission.RECORD_AUDIO), MY_PERMISSIONS_REQUEST_RECORD_AUDIO)
                } else {
                    speechRecognizerDescription.startListening(speechRecognizerIntentDescription)
                }
            }
            speechRecognizerDescription.setRecognitionListener(object : RecognitionListener {
                override fun onResults(results: Bundle?) {
                    val matches = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                    if (matches != null && matches.isNotEmpty()) {
                        val text = matches[0]
                        descriptionEditText.setText(text)
                    }
                }
                override fun onReadyForSpeech(params: Bundle?) {}
                override fun onRmsChanged(rmsdB: Float) {}
                override fun onBufferReceived(buffer: ByteArray?) {}
                override fun onPartialResults(partialResults: Bundle?) {}
                override fun onEvent(eventType: Int, params: Bundle?) {}
                override fun onBeginningOfSpeech() {}
                override fun onEndOfSpeech() {}
                override fun onError(error: Int) {}
            })

            // Expiration Date
            val dateLayout = LinearLayout(holder.itemView.context)
            dateLayout.orientation = LinearLayout.HORIZONTAL
            dateLayout.layoutParams = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
            val expirationButton = Button(holder.itemView.context)
            expirationButton.text = "Set Expiration Date"
            expirationButton.layoutParams = LinearLayout.LayoutParams(
                0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f
            )
            var expirationDate: Date = myModel.ExpirationDate!!.toDate()
            expirationButton.text = dateFormat.format(expirationDate)
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
            dateLayout.addView(expirationButton)

            val voiceInputButtonDate = ImageButton(holder.itemView.context)
            voiceInputButtonDate.setImageResource(android.R.drawable.ic_btn_speak_now)
            dateLayout.addView(voiceInputButtonDate)
            inputLayout.addView(dateLayout)

            // Configure SpeechRecognizer for date picker
            val speechRecognizerDate = SpeechRecognizer.createSpeechRecognizer(holder.itemView.context)
            val speechRecognizerIntentDate = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH)
            speechRecognizerIntentDate.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            speechRecognizerIntentDate.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault())
            speechRecognizerIntentDate.putExtra(RecognizerIntent.EXTRA_PROMPT, "Say a date...")

            voiceInputButtonDate.setOnClickListener {
                if (ContextCompat.checkSelfPermission(holder.itemView.context, android.Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(activity, arrayOf(android.Manifest.permission.RECORD_AUDIO), MY_PERMISSIONS_REQUEST_RECORD_AUDIO)
                } else {
                    speechRecognizerDate.startListening(speechRecognizerIntentDate)
                }
            }

            speechRecognizerDate.setRecognitionListener(object : RecognitionListener {
                override fun onResults(results: Bundle?) {
                    val matches = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                    if (matches != null && matches.isNotEmpty()) {
                        val text = matches[0]
                        expirationButton.text = text  // Set the recognized text directly to the date picker button
                    }
                }
                // Implement other methods of RecognitionListener...
                override fun onReadyForSpeech(params: Bundle?) {}
                override fun onRmsChanged(rmsdB: Float) {}
                override fun onBufferReceived(buffer: ByteArray?) {}
                override fun onPartialResults(partialResults: Bundle?) {}
                override fun onEvent(eventType: Int, params: Bundle?) {}
                override fun onBeginningOfSpeech() {}
                override fun onEndOfSpeech() {}
                override fun onError(error: Int) {}
            })

            builder.setView(inputLayout)
            builder.setPositiveButton(android.R.string.ok) { _, _ ->
                val db = Firebase.firestore
                val UID = myModel.UID
                val description = descriptionEditText.text.toString().trim()
                val updateData = hashMapOf(
                    "Description" to description,
                    "ExpirationDate" to Timestamp(expirationDate)
                )
                db.collection("users").document(userId).collection("food").document(UID).update(updateData as Map<String, Any>)
                    .addOnSuccessListener {
                        Log.d(TAG, "DocumentSnapshot successfully updated!")
                        databaseUpdateListener.onDatabaseUpdated()
                    }
                    .addOnFailureListener { e ->
                        Log.w(TAG, "Error updating document", e)
                    }
            }
            builder.setNegativeButton(android.R.string.cancel, null)
            builder.show()
        }
    }

    override fun getItemCount() = myList.size

    class MyViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val textView: TextView = itemView.findViewById(R.id.categoryView)
        val textView1: TextView = itemView.findViewById(R.id.descriptionView)
        val textView2: TextView = itemView.findViewById(R.id.expirationView)
        val imageView: ImageView = itemView.findViewById(R.id.myImageView)
        val editButton: Button = itemView.findViewById(R.id.editButton)
    }
}
