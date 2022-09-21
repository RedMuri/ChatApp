package com.example.chatapp

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth

class LoginActivity : AppCompatActivity() {
    private lateinit var editTextLogin: EditText
    private lateinit var editTextPassword: EditText
    private lateinit var buttonLogin: Button
    private lateinit var textViewDoNotHaveAccount: TextView
    private lateinit var auth: FirebaseAuth
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)
        editTextLogin = findViewById(R.id.editTextLogin)
        editTextPassword = findViewById(R.id.editTextPassword)
        buttonLogin = findViewById(R.id.buttonLogin)
        textViewDoNotHaveAccount = findViewById(R.id.textViewDoNotHaveAccount)
        auth = FirebaseAuth.getInstance()
        buttonLogin.setOnClickListener { login() }
        textViewDoNotHaveAccount.setOnClickListener { doNotHaveAccount() }
    }

    private fun login(){
        val login = editTextLogin.text.toString().trim()
        val password = editTextPassword.text.toString().trim()
        if (login.isNotEmpty()&&password.isNotEmpty()){
            auth.signInWithEmailAndPassword(login,password).addOnCompleteListener {
                if (it.isSuccessful){
                    Toast.makeText(this, "Logged in", Toast.LENGTH_SHORT).show()
                    val intent = Intent(this,ChatActivity::class.java)
                    startActivity(intent)
                } else{
                    Toast.makeText(this, "${it.exception?.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun doNotHaveAccount(){
        val intent = Intent(this,RegisterActivity::class.java)
        startActivity(intent)
    }
}