package com.group2.intercom

import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.IOException

abstract class FileED {
    protected fun getFileBytes(file: File): ByteArray? {
        return try {
            FileInputStream(file).use { it.readBytes() }
        } catch (e: IOException) {
            null
        }
    }

    protected fun getTempDir(): String {
        return System.getProperty("java.io.tmpdir")
    }

    @Throws(Exception::class)
    protected fun createFile(filename: String, bytes: ByteArray): File {
        val file = File(filename)
        FileOutputStream(file).use { it.write(bytes) }
        return file
    }
}
