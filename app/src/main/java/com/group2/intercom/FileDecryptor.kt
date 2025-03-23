package com.group2.dummy_ecc

import android.util.Log
import com.group2.intercom.FileED
import java.io.File
import java.security.PrivateKey
import javax.crypto.Cipher
import org.bouncycastle.jce.provider.BouncyCastleProvider
import java.security.Security

object FileDecryptor : FileED() {

    init {
        // Add BouncyCastle provider if not already added
        if (Security.getProvider("BC") == null) {
            Security.addProvider(BouncyCastleProvider())
        }
    }

    fun fileDecryption(file: File, privateKey: PrivateKey): File? {
        return try {
            val cipher = Cipher.getInstance("ECIES", "BC")
            cipher.init(Cipher.DECRYPT_MODE, privateKey)
            val fileBytes = getFileBytes(file) ?: return null
            val decryptedBytes = cipher.doFinal(fileBytes)
            createFile(file.name, decryptedBytes)
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    fun textDecryption(encryptedBytes: ByteArray, privateKey: PrivateKey): String? {
        return try {
            val cipher = Cipher.getInstance("ECIES", "BC")
            cipher.init(Cipher.DECRYPT_MODE, privateKey)
            val decryptedBytes = cipher.doFinal(encryptedBytes)
            String(decryptedBytes)
        } catch (e: Exception) {
            Log.e("DECRYPT", "Decryption error: ${e.message}")
            null
        }
    }

}
