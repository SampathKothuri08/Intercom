package com.group2.intercom

import android.os.Bundle
import android.util.Log
import android.widget.EditText
import android.widget.ImageButton
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.group2.dummy_ecc.FileEncryptor
import com.group2.dummy_ecc.FileEncryptor.stringToPublicKey

class ChatActivity : AppCompatActivity() {

    private lateinit var chatRecyclerView: RecyclerView
    private lateinit var edtMessage: EditText
    private lateinit var btnSend: ImageButton
    private lateinit var txtChatUser: TextView
    private lateinit var adapter: ChatAdapter
    private lateinit var selectedUserId: String
    private lateinit var currentUserId: String

    private val messageList = mutableListOf<Message>()
    private val firestore = FirebaseFirestore.getInstance()
    private var messageListener: ListenerRegistration? = null
    private var senderpublicd: String = ""
    private var receiverpublicd: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chat)

        firestore.collection("Chatrooms")
            .document("user1_user2")
            .get()
            .addOnSuccessListener { document ->
                if(document != null) {
                    senderpublicd = document.data?.get("user1Key")!!.toString()
                    receiverpublicd = document.data?.get("user2Key")!!.toString()
//                    Log.w("CHECK",stringToPublicKey(senderpublicd).toString())
                }
                else {
                    Log.w("CHECK", "ERROR")
                }
            }
            .addOnFailureListener { exception ->
                Log.d("CHECK", "get failed with ", exception)
            }

        selectedUserId = intent.getStringExtra("selectedUserId") ?: ""
        currentUserId = getSharedPreferences("SecurePrefs", MODE_PRIVATE).getString("userId", "") ?: ""

        txtChatUser = findViewById(R.id.txtChatUser)
        chatRecyclerView = findViewById(R.id.recyclerChat)
        edtMessage = findViewById(R.id.edtChatMessage)
        btnSend = findViewById(R.id.btnSendMessage)

        txtChatUser.text = selectedUserId

        Log.w("CHECK", "curr user: $currentUserId")
        Log.w("CHECK", "selectedUser: $selectedUserId")

        adapter = ChatAdapter(currentUserId, messageList)
        chatRecyclerView.layoutManager = LinearLayoutManager(this)
        chatRecyclerView.adapter = adapter

        btnSend.setOnClickListener {
            val messageText = edtMessage.text.toString().trim()
            if (messageText.isNotEmpty()) {
                sendMessage(messageText)
                edtMessage.text.clear()
            }
        }
        listenForMessages()
    }

    private fun sendMessage(text: String) {
        val message = Message(
            senderID = currentUserId,
            receiverID = selectedUserId,
            msg_sent = text,
            msg_received = text,
            timestamp = Timestamp.now()
        )
        firestore.collection("Chatrooms").document("user4_user1")
            .collection("chat").add(message)
    }

    private fun listenForMessages() {
        messageListener = firestore.collection("Chatrooms")
            .document("user4_user1")
            .collection("chat")
            .orderBy("timestamp")
            .addSnapshotListener { snapshot, _ ->
                messageList.clear()
                snapshot?.forEach { doc ->
                    val msg = doc.toObject(Message::class.java)
                    if ((msg.senderID == currentUserId && msg.receiverID == selectedUserId) ||
                        (msg.senderID == selectedUserId && msg.receiverID == currentUserId)) {
                        messageList.add(msg)
                    }
                }
                adapter.notifyDataSetChanged()
                chatRecyclerView.scrollToPosition(messageList.size - 1)
            }
    }

    override fun onDestroy() {
        super.onDestroy()
        messageListener?.remove()
    }
}
