package com.example.chatapp

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth

class RegisterActivity : AppCompatActivity() {
    private lateinit var editTextLogin: EditText
    private lateinit var editTextPassword: EditText
    private lateinit var buttonRegister: Button
    private lateinit var textViewHaveAccount: TextView
    private lateinit var auth: FirebaseAuth
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)
        editTextLogin = findViewById(R.id.editTextLogin)
        editTextPassword = findViewById(R.id.editTextPassword)
        buttonRegister = findViewById(R.id.buttonLogin)
        textViewHaveAccount = findViewById(R.id.textViewDoNotHaveAccount)
        auth = FirebaseAuth.getInstance()
        buttonRegister.setOnClickListener { register() }
        textViewHaveAccount.setOnClickListener { haveAccount() }
    }

    private fun register(){
        val login = editTextLogin.text.toString().trim()
        val password = editTextPassword.text.toString().trim()
        Log.i(
            "muri","$login, $password"
        )
        if (login.isNotEmpty()&&password.isNotEmpty()){
            auth.createUserWithEmailAndPassword(login,password).addOnCompleteListener {
                if (it.isSuccessful){
                    Toast.makeText(this, "Successful", Toast.LENGTH_SHORT).show()
                    val intent = Intent(this,ChatActivity::class.java)
                    startActivity(intent)
                } else{
                    Toast.makeText(this, "${it.exception?.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun haveAccount(){
        val intent = Intent(this,LoginActivity::class.java)
        startActivity(intent)
    }
}