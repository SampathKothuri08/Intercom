package com.group2.dummy_ecc

import android.util.Log
import com.group2.intercom.FileED
import java.io.File
import java.security.PublicKey
import javax.crypto.Cipher
import org.bouncycastle.jce.provider.BouncyCastleProvider
import java.security.Security

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

}
