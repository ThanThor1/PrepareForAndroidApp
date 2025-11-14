package com.example.testdatabase.data.remote

import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.postgrest.Postgrest
import io.github.jan.supabase.storage.Storage
import io.github.jan.supabase.serializer.KotlinXSerializer
import kotlinx.serialization.json.Json

object SupabaseConfig {
    // Thay YOUR_SUPABASE_URL và YOUR_SUPABASE_KEY bằng thông tin của bạn
    private const val SUPABASE_URL = "Dán url vào đây"
    private const val SUPABASE_KEY = "Dán token vào đây"

    val client = createSupabaseClient(
        supabaseUrl = SUPABASE_URL,
        supabaseKey = SUPABASE_KEY
    ) {
        // cấu hình serializer chung cho toàn bộ client
        defaultSerializer = KotlinXSerializer(
            Json {
                ignoreUnknownKeys = true      // bỏ qua field lạ
                coerceInputValues = true      // null -> dùng default value trong data class
                encodeDefaults = true      // <<– BẮT BUỘC: luôn encode cả giá trị default
                isLenient = true              // dễ tính hơn với JSON "xấu"
            }
        )

        install(Postgrest)
        install(Storage)
    }
}
