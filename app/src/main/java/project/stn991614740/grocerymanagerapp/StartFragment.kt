package project.stn991614740.grocerymanagerapp

import android.app.Activity
import android.content.ContentValues.TAG
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.navigation.fragment.findNavController
import com.airbnb.lottie.LottieAnimationView
import project.stn991614740.grocerymanagerapp.databinding.FragmentStartBinding
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.*
import com.google.firebase.firestore.FirebaseFirestore

class StartFragment : Fragment() {

    private lateinit var animationView: LottieAnimationView
    private val handler = Handler(Looper.getMainLooper())
    private val runnable = object : Runnable {
        override fun run() {
            animationView.playAnimation()
            handler.postDelayed(this, 10000) // replay every 10 seconds
        }
    }

    private var _binding: FragmentStartBinding? = null
    private val binding get() = _binding!!

    private lateinit var auth: FirebaseAuth
    private lateinit var mGoogleSignInClient: GoogleSignInClient

    private val db = FirebaseFirestore.getInstance()
    private val usersCollection = db.collection("users")

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentStartBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        animationView = view.findViewById(R.id.animationView)
        handler.post(runnable)

        val currentAuth = FirebaseAuth.getInstance()
        if (currentAuth.currentUser != null) {
            // Log the situation to investigate further
            Log.e(TAG, "User is not null after logout: ${currentAuth.currentUser}")
        } else {
            Log.d(TAG, "User is null as expected after logout")
        }

        val forgotPasswordButton = view.findViewById<TextView>(R.id.forgot_password_text)
        forgotPasswordButton.setOnClickListener {
            val intent = Intent(requireContext(), ResetPassword::class.java)
            startActivity(intent)
        }


        auth = FirebaseAuth.getInstance()

        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build()

        mGoogleSignInClient = GoogleSignIn.getClient(requireActivity(), gso)

        val user = auth.currentUser
        if (user != null) {
            navigateToDestination()
        } else {
            binding.googleSignInButton.setOnClickListener {
                val signInIntent = mGoogleSignInClient.signInIntent
                startActivityForResult(signInIntent, RC_SIGN_IN)
            }

            binding.twitterSignInButton.setOnClickListener {
                val intent = Intent(requireContext(), TwitterActivity::class.java)
                intent.setFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION)
                startActivityForResult(intent, TWITTER_SIGN_IN)
            }

            binding.buttonLogin.setOnClickListener {
                val email = binding.editTextEmail.text.toString()
                val password = binding.editTextPassword.text.toString()

                if (email.isNotEmpty() && password.isNotEmpty()) {
                    auth.signInWithEmailAndPassword(email, password)
                        .addOnCompleteListener { task ->
                            if (task.isSuccessful) {
                                val user = auth.currentUser
                                addUserToFirestore(user)
                                updateUI(user)

                                user?.let {
                                    saveUserIdToSharedPreferences(it.uid)
                                }

                            } else {
                                Log.w(TAG, "signInWithEmail:failure", task.exception)
                                updateUI(null)
                            }
                        }
                } else {
                    Toast.makeText(context, "Please enter email and password.", Toast.LENGTH_SHORT)
                        .show()
                }
            }

            binding.buttonRegister.setOnClickListener {
                findNavController().navigate(R.id.action_startFragment_to_registerFragment)
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == RC_SIGN_IN) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(data)
            try {
                val account = task.getResult(ApiException::class.java)!!
                firebaseAuthWithGoogle(account)
            } catch (e: ApiException) {
                Log.w(TAG, "Google sign in failed", e)
            }
        }

        if (requestCode == TWITTER_SIGN_IN && resultCode == Activity.RESULT_OK) {
            // Twitter login was successful
            val user = auth.currentUser
            addUserToFirestore(user)

            user?.let {
                saveUserIdToSharedPreferences(it.uid)
            }
        }
    }

    private fun firebaseAuthWithGoogle(account: GoogleSignInAccount) {
        val credential = GoogleAuthProvider.getCredential(account.idToken, null)
        auth.signInWithCredential(credential)
            .addOnCompleteListener(requireActivity()) { task ->
                if (task.isSuccessful) {
                    val user = auth.currentUser
                    // Add user to Firestore
                    addUserToFirestore(user)
                    Toast.makeText(requireContext(), "Login Successful", Toast.LENGTH_SHORT).show()

                    user?.let {
                        saveUserIdToSharedPreferences(it.uid)
                    }

                } else {
                    Log.w(TAG, "signInWithCredential:failure", task.exception)
                    updateUI(null)
                }
            }
    }

    private fun updateUI(user: FirebaseUser?) {
        if (user != null) {
            navigateToDestination()
        } else {
            Toast.makeText(context, "Authentication failed.", Toast.LENGTH_SHORT).show()
        }
    }

    private fun navigateToDestination() {
        val userId = auth.currentUser?.uid ?: return
        val userDocument = usersCollection.document(userId)

        // Fetch user details from Firestore and navigate to the appropriate destination
        userDocument.get()
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    if (isAdded) {
                        if (findNavController().currentDestination?.id != R.id.fridgeFragment) {
                            findNavController().navigate(R.id.action_startFragment_to_fridgeFragment)
                        }
                    }
                } else {
                    Log.e(TAG, "Error getting user document", task.exception)
                    // Handle the exception here.
                }
            }
    }

    private fun addUserToFirestore(user: FirebaseUser?) {
        user?.let {
            val userInfo = hashMapOf(
                "uid" to it.uid
            )
            val userDocument = usersCollection.document(it.uid)

            userDocument.get().addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val document = task.result
                    if (!document.exists()) {
                        // The document does not exist, we can safely write
                        userDocument.set(userInfo)
                            .addOnSuccessListener {
                                Log.d(TAG, "DocumentSnapshot successfully written!")
                                // Call updateUI(user) here after the Firestore write operation has succeeded
                                updateUI(user)
                            }
                            .addOnFailureListener { e ->
                                Log.w(TAG, "Error writing document", e)
                            }
                    } else {
                        Log.d(TAG, "Document already exists!")
                        // Call updateUI(user) here as well, since the user document already exists
                        updateUI(user)
                    }
                } else {
                    Log.w(TAG, "Error checking document", task.exception)
                }
            }
        }
    }

    private fun saveUserIdToSharedPreferences(uid: String) {
        val sharedPref = requireActivity().getSharedPreferences("MyAppPrefs", Context.MODE_PRIVATE)
        with(sharedPref.edit()) {
            putString("currentUserId", uid)
            apply()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        handler.removeCallbacks(runnable) // stop handler when the fragment is destroyed
    }

    companion object {
        private const val RC_SIGN_IN = 9001
        private const val TWITTER_SIGN_IN = 9002
    }
}