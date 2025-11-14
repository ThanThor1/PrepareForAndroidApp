package com.example.testdatabase.domain.repository

import java.io.File

interface ImageUploadRepository {
    suspend fun uploadImage(file: File, userId: String): Result<String>
    suspend fun deleteImage(imageUrl: String): Result<Boolean>
}