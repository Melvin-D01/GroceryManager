package project.stn991614740.grocerymanagerapp

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth

class ResetPassword : AppCompatActivity(){

    private lateinit var etPassword: EditText
    private lateinit var btnResetPassword: Button

    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_resetpassword)

        etPassword=findViewById(R.id.resetPassword)
        btnResetPassword=findViewById(R.id.buttonReset)

        auth = FirebaseAuth.getInstance()

        btnResetPassword.setOnClickListener(){
            val sPassword = etPassword.text.toString()
            auth.sendPasswordResetEmail(sPassword)
                .addOnSuccessListener {
                    Toast.makeText(this, "Please Check your Email!", Toast.LENGTH_SHORT).show()
                }
        }
    }
}