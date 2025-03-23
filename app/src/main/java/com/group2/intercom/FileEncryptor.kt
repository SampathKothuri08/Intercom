package com.group2.dummy_ecc

import com.group2.intercom.FileED
import java.io.File
import java.security.PublicKey
import javax.crypto.Cipher
import org.bouncycastle.jce.provider.BouncyCastleProvider
import java.security.KeyFactory
import java.security.Security
import java.security.spec.X509EncodedKeySpec
import android.util.Base64
import android.util.Log

object FileEncryptor : FileED() {

    init {
        // Add BouncyCastle provider if not already added
        if (Security.getProvider("BC") == null) {
            Security.addProvider(BouncyCastleProvider())
        }
    }

    fun fileEncryption(file: File, publicKey: PublicKey): File? {
        return try {
            val cipher = Cipher.getInstance("ECIES", "BC")
            cipher.init(Cipher.ENCRYPT_MODE, publicKey)
            println("Encryption initialized")

            val fileBytes = getFileBytes(file) ?: return null
            val encryptedBytes = cipher.doFinal(fileBytes)
            val tempFileName = getTempDir() + "/" + file.name
            createFile(tempFileName, encryptedBytes).also {
                println("Encryption complete")
            }
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    fun textEncryption(str: String, publicKey: PublicKey): ByteArray? {
        return try {
            val cipher = Cipher.getInstance("ECIES", "BC")
            cipher.init(Cipher.ENCRYPT_MODE, publicKey)
            cipher.doFinal(str.toByteArray())
        } catch (e: Exception) {
            Log.e("ENCRYPT", "Encryption error: ${e.message}")
            null
        }
    }
    fun stringToPublicKey(publicKeyString: String, algorithm: String = "RSA"): PublicKey? {
        return try {
            val publicKeyBytes = Base64.decode(publicKeyString, Base64.DEFAULT)
            val keySpec = X509EncodedKeySpec(publicKeyBytes)
            val keyFactory = KeyFactory.getInstance(algorithm)
            keyFactory.generatePublic(keySpec)
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

}
