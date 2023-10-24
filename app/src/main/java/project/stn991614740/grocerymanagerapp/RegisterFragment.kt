package project.stn991614740.grocerymanagerapp

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.os.Bundle
import android.util.Patterns
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import project.stn991614740.grocerymanagerapp.databinding.FragmentRegisterBinding

class RegisterFragment : Fragment() {

    private var _binding: FragmentRegisterBinding? = null
    private val binding get() = _binding!!

    private lateinit var auth: FirebaseAuth
    private val db = FirebaseFirestore.getInstance()
    private val usersCollection = db.collection("users")

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentRegisterBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        auth = FirebaseAuth.getInstance()

        val blurOverlay = view.findViewById<View>(R.id.blurOverlay)
        val lottieAnimationView = view.findViewById<com.airbnb.lottie.LottieAnimationView>(R.id.lottieAnimationView)

        binding.buttonRegister.setOnClickListener {
            val email = binding.editTextEmail.text.toString()
            val password = binding.editTextPassword.text.toString()

            if (email.isNotEmpty() && password.isNotEmpty()) {
                if (password.length < 6) {
                    Toast.makeText(context, "Password must be at least 6 characters long.", Toast.LENGTH_SHORT).show()
                } else if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                    Toast.makeText(context, "Please enter a valid email address.", Toast.LENGTH_SHORT).show()
                } else {
                    auth.createUserWithEmailAndPassword(email, password)
                        .addOnCompleteListener { task ->
                            if (task.isSuccessful) {
                                val user = auth.currentUser
                                if (user != null) {
                                    val newUser = User(user.uid ?: "", user.email ?: "")
                                    usersCollection.document(user.uid)
                                        .set(newUser)
                                        .addOnSuccessListener {
                                            blurOverlay.visibility = View.VISIBLE  // Show blur overlay
                                            lottieAnimationView.visibility = View.VISIBLE  // Show Lottie animation view
                                            lottieAnimationView.playAnimation()  // Play Lottie animation

                                            // Set an animation listener to navigate to the next screen when the animation is done
                                            lottieAnimationView.addAnimatorListener(object : AnimatorListenerAdapter() {
                                                override fun onAnimationEnd(animation: Animator) {
                                                    super.onAnimationEnd(animation)
                                                    findNavController().popBackStack()  // Navigate to the next screen
                                                }
                                            })
                                        }
                                        .addOnFailureListener { exception ->
                                            Toast.makeText(context, "User registration failed: ${exception.message}", Toast.LENGTH_SHORT).show()
                                        }
                                } else {
                                    Toast.makeText(context, "User registration failed.", Toast.LENGTH_SHORT).show()
                                }
                            } else {
                                val exception = task.exception
                                Toast.makeText(context, "Registration failed: ${exception?.message}", Toast.LENGTH_SHORT).show()
                            }
                        }
                }
            } else {
                Toast.makeText(context, "Please enter email and password.", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
