package com.example.articlefinder

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import kotlinx.android.synthetic.main.activity_login.*

class LoginActivity : AppCompatActivity() {

    lateinit var firebaseAuth: FirebaseAuth
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        firebaseAuth=FirebaseAuth.getInstance()

        btnLogin.setOnClickListener {
            login()
        }

        btnSignUp.setOnClickListener {
            val intent= Intent(this,SignupActivity::class.java)
            startActivity(intent)
            finish()
        }

    }

    private fun login()
    {
        val email:String=etEmailAddress.text.toString()
        val password:String=etPassword.text.toString()

        if(email.isBlank() || password.isBlank())
        {
            Toast.makeText(this,"Email and Password Cannot be Blank",Toast.LENGTH_SHORT).show()
            return
        }
        progressbar.visibility= View.VISIBLE

        firebaseAuth.signInWithEmailAndPassword(email,password)
            .addOnCompleteListener(this) {
                if(it.isSuccessful)
                {
                    Toast.makeText(this,"Login Succesful",Toast.LENGTH_SHORT).show()
                    Log.i("Login Success","Proceed Further")
                    progressbar.visibility= View.GONE
                    val intent= Intent(this,MainActivity::class.java)
                    intent.putExtra("INTENT",etEmailAddress.text.toString())
                   // intent.putExtra("passingIntent",email)
                    //Toast.makeText(this,"Email is $email",Toast.LENGTH_SHORT).show()
                    startActivity(intent)
                    finish()

                }
                else
                {
                    progressbar.visibility= View.GONE
                    Toast.makeText(this,"Authentication Failed",Toast.LENGTH_SHORT).show()
                    Log.i("Login Failed","Try Again")
                }
            }
    }
}