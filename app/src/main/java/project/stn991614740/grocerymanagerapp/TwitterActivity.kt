package project.stn991614740.grocerymanagerapp

import android.app.Activity
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.OAuthProvider

class TwitterActivity : AppCompatActivity() {

    private lateinit var firebaseAuth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_twitter)

        firebaseAuth = FirebaseAuth.getInstance()

        val provider = OAuthProvider.newBuilder("twitter.com")

        val pendingResultTask = firebaseAuth.pendingAuthResult
        if (pendingResultTask != null) {
            // There's something already here! Finish the sign-in for your user.
            pendingResultTask
                .addOnSuccessListener {
                    finish()
                    Toast.makeText(this, "Login Successful", Toast.LENGTH_SHORT).show()
                }
                .addOnFailureListener {
                    finish()
                    Toast.makeText(this, "Login Failed2", Toast.LENGTH_SHORT).show()
                }
        } else {
            firebaseAuth
                .startActivityForSignInWithProvider(this, provider.build())
                .addOnSuccessListener {
                    // Pass a result back indicating success
                    setResult(Activity.RESULT_OK)
                    finish()
                    Toast.makeText(this, "Login Successful", Toast.LENGTH_SHORT).show()
                }
                .addOnFailureListener { exception ->
                    finish()
                    val errorMessage = "Login Failed: ${exception.message}"
                    Toast.makeText(this, errorMessage, Toast.LENGTH_SHORT).show()
                    Log.e("TwitterActivity", errorMessage, exception)
                }
        }

    }
}