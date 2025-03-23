package com.group2.intercom

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.group2.intercom.utils.processIMEIAndMAC

class DeviceInfo : AppCompatActivity() {

    private lateinit var edtIMEI: EditText
    private lateinit var edtMAC: EditText
    private lateinit var btnSubmit: Button
    private lateinit var txtFindIMEI: TextView
    private lateinit var txtFindMAC: TextView
    private var userId: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_device_info)

        edtIMEI = findViewById(R.id.edtIMEI)
        edtMAC = findViewById(R.id.edtMAC)
        btnSubmit = findViewById(R.id.btnSubmit)
        txtFindIMEI = findViewById(R.id.txtImeiHint)
        txtFindMAC = findViewById(R.id.txtMacHint)

        txtFindIMEI.setOnClickListener { openSettings() }
        txtFindMAC.setOnClickListener { openSettings() }

        edtIMEI.setOnLongClickListener {
            copyToClipboard(edtIMEI.text.toString(), "IMEI copied")
            true
        }

        edtMAC.setOnLongClickListener {
            copyToClipboard(edtMAC.text.toString(), "MAC address copied")
            true
        }

        userId = getSharedPreferences("SecurePrefs", Context.MODE_PRIVATE).getString("userId", null)

        btnSubmit.setOnClickListener {
            val imei = edtIMEI.text.toString().trim()
            val mac = edtMAC.text.toString().trim()

            if (imei.isEmpty() || mac.isEmpty()) {
                Toast.makeText(this, "Please enter both IMEI and MAC address", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (userId == null) {
                Toast.makeText(this, "User ID not found. Please register again.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            processIMEIAndMAC(this, imei, mac, userId!!)

            startActivity(Intent(this, MainActivity::class.java))
            finish()
        }
    }

    private fun openSettings() {
        val intent = Intent(Settings.ACTION_SETTINGS)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
        startActivity(intent)
    }

    private fun copyToClipboard(text: String, label: String) {
        val clipboard = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        val clip = ClipData.newPlainText("CopiedText", text)
        clipboard.setPrimaryClip(clip)
        Toast.makeText(this, label, Toast.LENGTH_SHORT).show()
    }
}
