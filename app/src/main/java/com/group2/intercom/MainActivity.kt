package com.group2.intercom

import android.os.Bundle
import android.util.Base64
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.group2.dummy_ecc.FileDecryptor
import com.group2.dummy_ecc.FileEncryptor
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import org.bouncycastle.jce.provider.BouncyCastleProvider
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Body
import retrofit2.http.POST
import java.math.BigInteger
import java.security.KeyFactory
import java.security.MessageDigest
import java.security.PrivateKey
import java.security.PublicKey
import java.security.Security
import java.security.spec.PKCS8EncodedKeySpec
import java.security.spec.X509EncodedKeySpec

class MainActivity : AppCompatActivity() {

    private lateinit var edtUser1Message: EditText
    private lateinit var edtUser2Message: EditText
    private lateinit var btnUser1Send: Button
    private lateinit var btnUser2Send: Button
    private lateinit var txtUser1Received: TextView
    private lateinit var txtUser2Received: TextView

    private val api by lazy { createECCApi() }

    private val user1Seed = "123456789012345" + "a1b2c3d4e5f6"
    private val user2Seed = "987654321098765" + "abcdef123456"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Register Bouncy Castle provider
        Security.removeProvider("BC")
        Security.addProvider(BouncyCastleProvider())

        setContentView(R.layout.activity_main)

        edtUser1Message = findViewById(R.id.edtUser1Message)
        edtUser2Message = findViewById(R.id.edtUser2Message)
        btnUser1Send = findViewById(R.id.btnUser1Send)
        btnUser2Send = findViewById(R.id.btnUser2Send)
        txtUser1Received = findViewById(R.id.txtUser1Received)
        txtUser2Received = findViewById(R.id.txtUser2Received)

        btnUser1Send.setOnClickListener {
            sendMessage(fromUser = 1)
        }

        btnUser2Send.setOnClickListener {
            sendMessage(fromUser = 2)
        }
    }

    private fun sendMessage(fromUser: Int) {
        val senderSeed = if (fromUser == 1) user1Seed else user2Seed
        val receiverSeed = if (fromUser == 1) user2Seed else user1Seed

        val plainText = if (fromUser == 1) edtUser1Message.text.toString() else edtUser2Message.text.toString()
        val receiverPrivateKey = getPrivateKeyFromHex(hashToHex(receiverSeed))

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val response = api.generatePublicKey(PublicKeyRequest(hashToHex(receiverSeed)))
                val receiverPublicKey = getPublicKeyFromHex(response.publicKey)

                //  ENCRYPT
                val encryptedBytes = FileEncryptor.textEncryption(plainText, receiverPublicKey)
                if (encryptedBytes == null) {
                    Log.e("ENCRYPT", "Encryption failed, got null")
                    return@launch
                }

                val encryptedBase64 = Base64.encodeToString(encryptedBytes, Base64.DEFAULT)
                Log.d("ENCRYPT", "Original message: $plainText")
                Log.d("ENCRYPT", "Encrypted Base64: $encryptedBase64")

                // DECRYPT
                val decryptedText = FileDecryptor.textDecryption(encryptedBytes, receiverPrivateKey)
                Log.d("DECRYPT", "Decrypted text: ${decryptedText ?: "Decryption failed"}")

            } catch (e: Exception) {
                Log.e("ECC", "Exception: ${e.message}", e)
            }
        }
    }

    private fun getPublicKeyFromHex(hexKey: String): PublicKey {
        val ecSpec = org.bouncycastle.jce.ECNamedCurveTable.getParameterSpec("secp256r1")
        val point = ecSpec.curve.decodePoint(hexStringToByteArray(hexKey))
        val pubSpec = org.bouncycastle.jce.spec.ECPublicKeySpec(point, ecSpec)
        val keyFactory = KeyFactory.getInstance("EC", "BC")
        return keyFactory.generatePublic(pubSpec)
    }


    private fun getPrivateKeyFromHex(hexKey: String): PrivateKey {
        val s = BigInteger(hexKey, 16)
        val ecSpec = org.bouncycastle.jce.ECNamedCurveTable.getParameterSpec("secp256r1")
        val privateKeySpec = org.bouncycastle.jce.spec.ECPrivateKeySpec(s, ecSpec)
        val keyFactory = KeyFactory.getInstance("EC", "BC")
        return keyFactory.generatePrivate(privateKeySpec)
    }

    private fun hexStringToByteArray(hex: String): ByteArray {
        val len = hex.length
        val data = ByteArray(len / 2)
        var i = 0
        while (i < len) {
            data[i / 2] = ((Character.digit(hex[i], 16) shl 4)
                    + Character.digit(hex[i + 1], 16)).toByte()
            i += 2
        }
        return data
    }

    private fun hashToHex(input: String): String {
        val hashed = MessageDigest.getInstance("SHA-256").digest(input.toByteArray())
        return hashed.joinToString(separator = "") { byte -> "%02x".format(byte) }
    }

    private fun createECCApi(): ECCApi {
        val interceptor = HttpLoggingInterceptor()
        interceptor.level = HttpLoggingInterceptor.Level.BODY

        val client = OkHttpClient.Builder().addInterceptor(interceptor).build()

        val retrofit = Retrofit.Builder()
            .baseUrl("https://ecc-api-bdu6.onrender.com/")
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        return retrofit.create(ECCApi::class.java)
    }
}

interface ECCApi {
    @POST("generate-public-key")
    suspend fun generatePublicKey(@Body request: PublicKeyRequest): PublicKeyResponse
}

data class PublicKeyRequest(val hashedSeed: String)
data class PublicKeyResponse(val publicKey: String)
