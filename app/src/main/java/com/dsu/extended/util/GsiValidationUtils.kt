package com.dsu.extended.util

import android.net.Uri
import java.util.zip.ZipInputStream
import com.dsu.extended.core.StorageManager

data class GsiValidationResult(
    val isValid: Boolean,
    val detectedType: String,
    val message: String,
)

object GsiValidationUtils {
    private val sparseMagic = byteArrayOf(0x3A, 0xFF.toByte(), 0x26, 0xED.toByte())
    private val gzipMagic = byteArrayOf(0x1F, 0x8B.toByte())
    private val xzMagic = byteArrayOf(0xFD.toByte(), 0x37, 0x7A, 0x58, 0x5A, 0x00)
    private val zipMagic = byteArrayOf(0x50, 0x4B, 0x03, 0x04)

    fun validateSelectedImage(
        storageManager: StorageManager,
        uri: Uri,
        fileName: String,
        zipSupported: Boolean,
    ): GsiValidationResult {
        if (uri == Uri.EMPTY) {
            return GsiValidationResult(false, "unknown", "No image file selected")
        }
        val extension = fileName.substringAfterLast(".", "").lowercase()
        val fileSize = storageManager.getFilesizeFromUri(uri)
        if (fileSize <= 0L) {
            return GsiValidationResult(false, "unknown", "Selected file is empty or unreadable")
        }
        return when (extension) {
            "img" -> validateRawImage(storageManager, uri, fileSize)
            "gz", "gzip" -> validateGzip(storageManager, uri)
            "xz" -> validateXz(storageManager, uri)
            "zip" ->
                if (zipSupported) {
                    validateZip(storageManager, uri)
                } else {
                    GsiValidationResult(false, "zip", "ZIP DSU packages are supported only on Android 11+")
                }
            else -> GsiValidationResult(false, "unknown", "Unsupported file extension: .$extension")
        }
    }

    private fun validateRawImage(
        storageManager: StorageManager,
        uri: Uri,
        fileSize: Long,
    ): GsiValidationResult {
        val header = readPrefix(storageManager, uri, 4096)
        if (header.size < 4) {
            return GsiValidationResult(false, "raw", "Raw image header is too short")
        }
        if (header.startsWith(sparseMagic)) {
            return GsiValidationResult(true, "sparse", "Android sparse image detected")
        }
        if (fileSize > 1082 && hasExt4Magic(header)) {
            return GsiValidationResult(true, "ext4", "EXT4 raw image detected")
        }
        if (fileSize > 1030 && hasErofsMagic(header)) {
            return GsiValidationResult(true, "erofs", "EROFS raw image detected")
        }
        return GsiValidationResult(
            false,
            "raw",
            "Unsupported raw image type. Expected sparse, EXT4, or EROFS image",
        )
    }

    private fun validateGzip(
        storageManager: StorageManager,
        uri: Uri,
    ): GsiValidationResult {
        val header = readPrefix(storageManager, uri, 2)
        return if (header.startsWith(gzipMagic)) {
            GsiValidationResult(true, "gzip", "GZIP archive detected")
        } else {
            GsiValidationResult(false, "gzip", "Invalid GZIP header")
        }
    }

    private fun validateXz(
        storageManager: StorageManager,
        uri: Uri,
    ): GsiValidationResult {
        val header = readPrefix(storageManager, uri, 6)
        return if (header.startsWith(xzMagic)) {
            GsiValidationResult(true, "xz", "XZ archive detected")
        } else {
            GsiValidationResult(false, "xz", "Invalid XZ header")
        }
    }

    private fun validateZip(
        storageManager: StorageManager,
        uri: Uri,
    ): GsiValidationResult {
        val header = readPrefix(storageManager, uri, 4)
        if (!header.startsWith(zipMagic)) {
            return GsiValidationResult(false, "zip", "Invalid ZIP header")
        }
        var foundImage = false
        var foundSystem = false
        storageManager.openInputStream(uri).use { input ->
            ZipInputStream(input).use { zip ->
                var entry = zip.nextEntry
                var checkedEntries = 0
                while (entry != null && checkedEntries < 256) {
                    checkedEntries++
                    val entryName = entry.name.lowercase()
                    if (entryName.endsWith(".img")) {
                        foundImage = true
                        if (entryName.endsWith("/system.img") || entryName == "system.img") {
                            foundSystem = true
                        }
                    }
                    zip.closeEntry()
                    entry = zip.nextEntry
                }
            }
        }
        if (!foundImage) {
            return GsiValidationResult(false, "zip", "ZIP package does not contain any .img partitions")
        }
        if (!foundSystem) {
            return GsiValidationResult(false, "zip", "ZIP package is missing system.img")
        }
        return GsiValidationResult(true, "zip", "DSU package structure looks valid")
    }

    private fun readPrefix(
        storageManager: StorageManager,
        uri: Uri,
        length: Int,
    ): ByteArray {
        val buffer = ByteArray(length)
        var bytesRead = 0
        storageManager.openInputStream(uri).use { input ->
            while (bytesRead < length) {
                val readCount = input.read(buffer, bytesRead, length - bytesRead)
                if (readCount <= 0) {
                    break
                }
                bytesRead += readCount
            }
        }
        return if (bytesRead == buffer.size) buffer else buffer.copyOf(bytesRead)
    }

    private fun ByteArray.startsWith(prefix: ByteArray): Boolean {
        if (size < prefix.size) {
            return false
        }
        for (index in prefix.indices) {
            if (this[index] != prefix[index]) {
                return false
            }
        }
        return true
    }

    private fun hasExt4Magic(header: ByteArray): Boolean {
        val ext4MagicOffset = 1024 + 56
        return header.size > ext4MagicOffset + 1 &&
            header[ext4MagicOffset] == 0x53.toByte() &&
            header[ext4MagicOffset + 1] == 0xEF.toByte()
    }

    private fun hasErofsMagic(header: ByteArray): Boolean {
        val erofsMagicOffset = 1024
        return header.size > erofsMagicOffset + 3 &&
            header[erofsMagicOffset] == 0xE2.toByte() &&
            header[erofsMagicOffset + 1] == 0xE1.toByte() &&
            header[erofsMagicOffset + 2] == 0xF5.toByte() &&
            header[erofsMagicOffset + 3] == 0xE0.toByte()
    }
}
