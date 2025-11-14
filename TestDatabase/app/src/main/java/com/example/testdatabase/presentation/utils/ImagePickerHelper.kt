package com.example.testdatabase.presentation.utils

import android.content.Context
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import java.io.File
import java.io.FileOutputStream

/**
 * Helper để chọn ảnh từ gallery
 */
@Composable
fun rememberImagePicker(
    onImageSelected: (Uri?) -> Unit
): () -> Unit {
    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        onImageSelected(uri)
    }

    return remember {
        {
            launcher.launch("image/*")
        }
    }
}

/**
 * Copy ảnh từ Uri sang file cache để upload
 */
fun Context.copyUriToCache(uri: Uri): File? {
    return try {
        val inputStream = contentResolver.openInputStream(uri) ?: return null
        val file = File(cacheDir, "temp_image_${System.currentTimeMillis()}.jpg")

        FileOutputStream(file).use { outputStream ->
            inputStream.copyTo(outputStream)
        }
        inputStream.close()

        file
    } catch (e: Exception) {
        e.printStackTrace()
        null
    }
}