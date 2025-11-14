package com.example.testdatabase.data.repository

import com.example.testdatabase.data.remote.SupabaseConfig
import com.example.testdatabase.domain.repository.ImageUploadRepository
import io.github.jan.supabase.storage.storage
import java.io.File

class ImageUploadRepositoryImpl : ImageUploadRepository {
    private val supabase = SupabaseConfig.client

    // Tên bucket trong Supabase Storage (cần tạo trước trong Supabase Dashboard)
    private val BUCKET_NAME = "avatars"

    override suspend fun uploadImage(file: File, userId: String): Result<String> {
        return try {
            val fileName = "avatar_${userId}_${System.currentTimeMillis()}.jpg"

            // Upload file lên Supabase Storage
            supabase.storage
                .from(BUCKET_NAME)
                .upload(fileName, file.readBytes())

            // Lấy public URL của ảnh
            val imageUrl = supabase.storage
                .from(BUCKET_NAME)
                .publicUrl(fileName)

            Result.success(imageUrl)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun deleteImage(imageUrl: String): Result<Boolean> {
        return try {
            // Extract filename từ URL
            val fileName = imageUrl.substringAfterLast("/")

            if (fileName.isNotEmpty()) {
                supabase.storage
                    .from(BUCKET_NAME)
                    .delete(fileName)
            }

            Result.success(true)
        } catch (e: Exception) {
            // Không throw error nếu xóa ảnh thất bại
            Result.success(false)
        }
    }
}