package com.group2.intercom

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.firestore.FirebaseFirestore

class MainActivity : AppCompatActivity() {

    private lateinit var searchEditText: EditText
    private lateinit var searchIcon: ImageView
    private lateinit var userListView: ListView
    private lateinit var progressBar: ProgressBar
    private lateinit var adapter: ArrayAdapter<String>

    private val userList = mutableListOf<String>()
    private val firestore = FirebaseFirestore.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        searchEditText = findViewById(R.id.searchEditText)
        searchIcon = findViewById(R.id.searchIcon)
        userListView = findViewById(R.id.userListView)
        progressBar = findViewById(R.id.progressBar)

        adapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, userList)
        userListView.adapter = adapter

        // 🔍 Search icon click
        searchIcon.setOnClickListener {
            performSearch()
        }

        // ⌨️ Live search on text input
        searchEditText.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) = performSearch()
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })

        // 👆 When user clicks on a name
        userListView.setOnItemClickListener { _, _, position, _ ->
            val selectedUserId = userList[position]
            val intent = Intent(this@MainActivity, ChatActivity::class.java)
            intent.putExtra("selectedUserId", selectedUserId)
            startActivity(intent)
        }

    }

    private fun performSearch() {
        val queryText = searchEditText.text.toString().trim()
        if (queryText.isEmpty()) {
            userList.clear()
            adapter.notifyDataSetChanged()
            return
        }

        progressBar.visibility = View.VISIBLE

        firestore.collection("users")
            .orderBy("userId")
            .startAt(queryText)
            .endAt(queryText + "\uf8ff")
            .get()
            .addOnSuccessListener { querySnapshot ->
                userList.clear()
                for (doc in querySnapshot) {
                    val userId = doc.getString("userId")
                    if (userId != null) userList.add(userId)
                }
                adapter.notifyDataSetChanged()
                progressBar.visibility = View.GONE
            }
            .addOnFailureListener {
                Toast.makeText(this, "Failed to fetch users", Toast.LENGTH_SHORT).show()
                progressBar.visibility = View.GONE
            }
    }
}
