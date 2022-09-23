package com.example.chatapp

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.widget.EditText
import android.widget.ImageButton
import androidx.activity.result.ActivityResult
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.firebase.ui.auth.AuthUI
import com.firebase.ui.auth.FirebaseAuthUIActivityResultContract
import com.firebase.ui.auth.data.model.FirebaseAuthUIAuthenticationResult
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.toObject
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference

class ChatActivity : AppCompatActivity() {
    private lateinit var recyclerViewMessages: RecyclerView
    private lateinit var editTextMessage: EditText
    private lateinit var buttonSend: ImageButton
    private lateinit var buttonAddImage: ImageButton
    private lateinit var messagesAdapter: MessagesAdapter
    private lateinit var db: FirebaseFirestore
    private lateinit var auth: FirebaseAuth
    private lateinit var signInLauncher: ActivityResultLauncher<Intent>
    private lateinit var getImageLauncher: ActivityResultLauncher<Intent>
    private lateinit var storage: FirebaseStorage
    private lateinit var storageReference: StorageReference
    private lateinit var imagesReference: StorageReference

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
        storage = FirebaseStorage.getInstance()
        storageReference = storage.reference
        imagesReference = storageReference.child("images")

        recyclerViewMessages.adapter = messagesAdapter
        recyclerViewMessages.layoutManager =
            LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)

        signInLauncher = registerForActivityResult(
            FirebaseAuthUIActivityResultContract()
        ) { result -> this.onSignInResult(result) }

        getImageLauncher = registerForActivityResult(
            ActivityResultContracts.StartActivityForResult()
        ) { result ->
            this.onGetImageResult(result)
        }

        buttonSend.setOnClickListener { sendMessage(editTextMessage.text.toString().trim(), null) }
        buttonAddImage.setOnClickListener { openGetImageActivity() }
    }

    private fun onSignInResult(result: FirebaseAuthUIAuthenticationResult) {
        val response = result.idpResponse
        Log.i("muri", "onSignInResult: $response")
        if (result.resultCode == RESULT_OK) {
            Log.i("muri", "onSignInResult success")
        } else {
            Log.i("muri", "onSignInResult: "+response?.error?.message.toString())
        }
    }

    private fun openGetImageActivity() {
        val intent = Intent(Intent.ACTION_GET_CONTENT)
        intent.type = "image/jpeg"
        intent.putExtra(Intent.EXTRA_LOCAL_ONLY, true)
        getImageLauncher.launch(intent)
    }

    private fun onGetImageResult(result: ActivityResult) {
        val uri = result.data?.data
        val uriRef = uri?.lastPathSegment?.let { imagesReference.child(it) }
        uriRef?.let { ref ->
            ref.putFile(uri).continueWithTask { task ->
                if (!task.isSuccessful) {
                    task.exception?.let {
                        Log.i("muri", "onGetImageResult: " + it.message.toString())
                    }
                }
                ref.downloadUrl
            }.addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val downloadUri = task.result
                    sendMessage(null, downloadUri)
                    Log.i("muri", "onGetImageResult: $downloadUri")
                } else {
                    Log.i("muri", "onGetImageResult: "+task.exception.toString())
                }
            }
        }
    }

    private fun signOut() {
        auth.signOut()
        Log.i("muri", "signOut")
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

    override fun onResume() {
        loadFromDB()
        super.onResume()
    }

    private fun loadFromDB() {
        db.collection("messages").orderBy("date").addSnapshotListener { value, e ->
            if (value != null) {
                try {
                    val messages = value.map { it.toObject<Message>() }
                    Log.i("muri", "loadFromDB: $messages")
                    messagesAdapter.setMessages(ArrayList(messages))
                }catch (e: Exception){
                    Log.i("muri","loadFromDB: "+e.message.toString())
                }
                recyclerViewMessages.scrollToPosition(messagesAdapter.itemCount - 1)
            } else
                Log.i("muri", "loadFromDB: "+"value null")
            if (e != null) {
                Log.i("muri", "loadFromDB: "+e.message.toString())
            }
        }
        val currentUser = auth.currentUser
        if (currentUser == null) {
            val intent = Intent(this, RegisterActivity::class.java)
            startActivity(intent)
        }
    }

    private fun sendMessage(textOfMessage: String?, urlToImage: Uri?) {
        val message: Message
        val currentAuthor = auth.currentUser?.email?.substringBefore("@")
        val date = System.currentTimeMillis()
        if (!textOfMessage.isNullOrEmpty()) {
            message = Message(currentAuthor, textOfMessage, date)
            editTextMessage.setText("")
            db.collection("messages").add(message).addOnCompleteListener {
                Log.i("muri", "sendMessage: $it")
            }
        } else if (urlToImage != null) {
            message = Message(currentAuthor, null, date, urlToImage.toString())
            db.collection("messages").add(message).addOnCompleteListener {
                Log.i("muri", "sendMessage: $it")
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        val menuInflater = MenuInflater(this)
        menuInflater.inflate(R.menu.menu_main, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == R.id.menu_item_sign_out) {
            signOut()
        }
        return super.onOptionsItemSelected(item)
    }

}