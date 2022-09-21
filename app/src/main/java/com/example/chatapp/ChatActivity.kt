package com.example.chatapp

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.widget.EditText
import android.widget.ImageButton
import android.widget.Toast
import androidx.activity.result.ActivityResultCallback
import androidx.activity.result.ActivityResultCaller
import androidx.activity.result.ActivityResultLauncher
import androidx.core.app.ActivityOptionsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.firebase.ui.auth.AuthUI
import com.firebase.ui.auth.FirebaseAuthUIActivityResultContract
import com.firebase.ui.auth.data.model.FirebaseAuthUIAuthenticationResult
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.EventListener
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.toObject
import com.google.firebase.firestore.ktx.toObjects
import com.google.firebase.ktx.Firebase

class ChatActivity : AppCompatActivity() {
    private lateinit var recyclerViewMessages: RecyclerView
    private lateinit var editTextMessage: EditText
    private lateinit var buttonSend: ImageButton
    private lateinit var buttonAddImage: ImageButton
    private lateinit var messagesAdapter: MessagesAdapter
    private lateinit var db: FirebaseFirestore
    private lateinit var auth: FirebaseAuth
    private val author = "Muri"
    private lateinit var signInLauncher: ActivityResultLauncher<Intent>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chat)
        recyclerViewMessages = findViewById(R.id.recyclerViewMessages)
        editTextMessage = findViewById(R.id.editTextMessage)
        buttonSend = findViewById(R.id.imageButtonSend)
        buttonAddImage = findViewById(R.id.imageButtonAddImage)
        messagesAdapter = MessagesAdapter()
        db = FirebaseFirestore.getInstance()
        auth = FirebaseAuth.getInstance()

        recyclerViewMessages.adapter = messagesAdapter
        recyclerViewMessages.layoutManager = LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)

        signInLauncher = registerForActivityResult(
            FirebaseAuthUIActivityResultContract()
        ) { result -> this.onSignInResult(result) }

        buttonSend.setOnClickListener { sendMessage() }
        loadFromDB()
    }

    private fun onSignInResult(result: FirebaseAuthUIAuthenticationResult) {
        val response = result.idpResponse
        Log.i("muri","response: $response")
        if (result.resultCode == RESULT_OK) {
            // Successfully signed in
            val user = FirebaseAuth.getInstance().currentUser
            // ...
        } else {
            Log.i("muri",response?.error?.message.toString())
            // Sign in failed. If response is null the user canceled the
            // sign-in flow using the back button. Otherwise check
            // response.getError().getErrorCode() and handle the error.
            // ...
        }
    }

    private fun signOut(){
        auth.signOut()
        Log.i("muri","in sign out")
        // Choose authentication providers
        val providers = arrayListOf(
            AuthUI.IdpConfig.EmailBuilder().build(),
            AuthUI.IdpConfig.GoogleBuilder().build())

        // Create and launch sign-in intent
        val signInIntent = AuthUI.getInstance()
            .createSignInIntentBuilder()
            .setAvailableProviders(providers)
            .build()
        signInLauncher.launch(signInIntent)
    }

    private fun loadFromDB(){
        db.collection("messages").orderBy("date").addSnapshotListener { value, e ->
            if (value!=null) {
                val messages = value.map { it.toObject<Message>() }
                messagesAdapter.setMessages(ArrayList(messages))
                recyclerViewMessages.scrollToPosition(messagesAdapter.itemCount-1)
            } else
                Log.i("muri","value null")
            if (e != null) {
                Log.i("muri", e.message.toString())
            }
        }
        val currentUser = auth.currentUser
        if (currentUser==null) {
            val intent = Intent(this,RegisterActivity::class.java)
            startActivity(intent)
        }
    }

    private fun sendMessage() {
        val message = editTextMessage.text.toString().trim()
        if (message.isNotEmpty()) {
            db.collection("messages").add(Message(author, message, System.currentTimeMillis()))
            editTextMessage.setText("")
        } else {
            Toast.makeText(this, "Empty", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        val menuInflater = MenuInflater(this)
        menuInflater.inflate(R.menu.menu_main,menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == R.id.menu_item_sign_out){
            signOut()
        }
        return super.onOptionsItemSelected(item)
    }

}