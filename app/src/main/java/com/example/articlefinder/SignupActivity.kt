package com.example.articlefinder

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Toast
import com.example.articlefinder.daos.UserDao
import com.example.articlefinder.models.User
import com.google.firebase.auth.FirebaseAuth
import kotlinx.android.synthetic.main.activity_login.*
//import kotlinx.android.synthetic.main.activity_login.*
import kotlinx.android.synthetic.main.activity_signup.*
import kotlinx.android.synthetic.main.activity_signup.btnLogin
import kotlinx.android.synthetic.main.activity_signup.btnSignUp
import kotlinx.android.synthetic.main.activity_signup.etEmailAddress
import kotlinx.android.synthetic.main.activity_signup.etPassword
import kotlinx.android.synthetic.main.activity_signup.progressbar

class SignupActivity : AppCompatActivity() {

    lateinit var firebaseAuth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_signup)
        firebaseAuth=FirebaseAuth.getInstance()
        btnSignUp.setOnClickListener {
            signUpUser()
        }

        btnLogin.setOnClickListener {
            val intent= Intent(this,LoginActivity::class.java)
            startActivity(intent)
            finish()
        }
    }

    private fun signUpUser()
    {

        val email:String=etEmailAddress.text.toString()
        val password:String=etPassword.text.toString()
        val confirmPassword:String=etConfirmPassword.text.toString()

        if(email.isBlank() || password.isBlank() || confirmPassword.isBlank())
        {
            Toast.makeText(this,"Email and Password Cannot be Blank",Toast.LENGTH_SHORT).show()
            return
        }

        if(password!=confirmPassword)
        {
            Toast.makeText(this,"Password and Confirm Password do not match",Toast.LENGTH_SHORT).show()
            return
        }
        val auth:FirebaseAuth=FirebaseAuth.getInstance()
        val firebaseUser=auth.currentUser
        progressbar.visibility= View.VISIBLE

        firebaseAuth.createUserWithEmailAndPassword(email,password)
            .addOnCompleteListener(this) {
                if(it.isSuccessful)
                {
                    //adding the users to the users collection
                    val user= User(auth.currentUser?.uid.toString(),email.toString(),"")
                    val usersDao=UserDao()
                    usersDao.addUser(user)
                    //call is successful
                    Toast.makeText(this,"Sign up Successful",Toast.LENGTH_SHORT).show()
                    progressbar.visibility=View.GONE
                    val intent= Intent(this,MainActivity::class.java)
                    intent.putExtra("INTENT",etEmailAddress.text.toString())
                    //intent.putExtra("passingIntent",email.toString())
                    startActivity(intent)
                    finish()
                }
                else
                {
                    Toast.makeText(this,"Error In Creating User",Toast.LENGTH_SHORT).show()
                }
            }

    }
}