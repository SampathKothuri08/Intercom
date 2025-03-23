package com.group2.intercom.utils

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.math.BigInteger
import java.net.HttpURLConnection
import java.net.URL
import java.security.MessageDigest
import org.json.JSONObject

fun processIMEIAndMAC(context: Context, imei: String, mac: String, userId: String) {
    Log.d("PROCESS_FLOW", "processIMEIAndMAC() called")

    val hashedKey = hashIMEIAndMAC(imei, mac)
    Log.d("PROCESS_FLOW", "Hashed Private Key: $hashedKey")

    savePrivateKeyToPrefs(context, hashedKey)
    Log.d("PROCESS_FLOW", "Private key saved to SharedPreferences")

    CoroutineScope(Dispatchers.IO).launch {
        Log.d("PROCESS_FLOW", "Calling API to get Public Key...")
        val publicKey = getPublicKeyFromAPI(hashedKey)

        if (publicKey != null) {
            Log.d("PROCESS_FLOW", "Public Key received: $publicKey")
            storePublicKeyInFirestore(userId, publicKey)
        } else {
            Log.e("PROCESS_FLOW", " Failed to get public key from API")
        }
    }
}

private const val PREFS_NAME = "secure_prefs"
private const val PRIVATE_KEY = "private_key"

fun hashIMEIAndMAC(imei: String, mac: String): String {
    val blend = "$imei-$mac"
    Log.d("HASHING", "Blended string: $blend")

    val md = MessageDigest.getInstance("SHA-256")
    val hash = md.digest(blend.toByteArray())
    val hashedKey = BigInteger(1, hash).toString(16).padStart(64, '0')

    Log.d("HASHING", "Hashed key (SHA-256): $hashedKey")
    return hashedKey
}

fun savePrivateKeyToPrefs(context: Context, hashedKey: String) {
    Log.d("PREFS", "Saving hashedKey to SharedPreferences...")
    val prefs: SharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    prefs.edit().putString(PRIVATE_KEY, hashedKey).apply()
}



suspend fun getPublicKeyFromAPI(privateKey: String): String? = withContext(Dispatchers.IO) {
    val url = URL("https://ecc-api-bdu6.onrender.com/generate-public-key")
    val connection = url.openConnection() as HttpURLConnection

    return@withContext try {
        connection.requestMethod = "POST"
        connection.doOutput = true
        connection.setRequestProperty("Content-Type", "application/json")

        val requestBody = """{"hashedSeed":"$privateKey"}"""
        connection.outputStream.use { os ->
            os.write(requestBody.toByteArray())
        }

        if (connection.responseCode == 200) {
            val response = connection.inputStream.bufferedReader().readText()
            Log.d("API_CALL", "Raw response: $response")

            //  Parse the JSON and extract just the public key
            val jsonObject = JSONObject(response)
            jsonObject.getString("publicKey")
        } else {
            Log.e("API_CALL", "Failed with code ${connection.responseCode}")
            null
        }
    } catch (e: Exception) {
        Log.e("API_CALL", "Exception while calling API", e)
        null
    } finally {
        connection.disconnect()
    }
}

fun storePublicKeyInFirestore(userId: String, publicKey: String) {
    Log.d("FIRESTORE", "Saving public key to Firestore for $userId")
    val firestore = FirebaseFirestore.getInstance()
    val data = mapOf("publicKey" to publicKey)

    firestore.collection("users").document(userId)
        .set(data, SetOptions.merge())
        .addOnSuccessListener {
            Log.d("FIRESTORE", " Public key stored successfully for $userId")
        }
        .addOnFailureListener {
            Log.e("FIRESTORE", "Failed to store public key", it)
        }
}
