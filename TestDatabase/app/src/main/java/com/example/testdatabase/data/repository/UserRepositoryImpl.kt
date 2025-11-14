package com.example.testdatabase.data.repository

import com.example.testdatabase.data.remote.SupabaseConfig
import com.example.testdatabase.domain.model.User
import com.example.testdatabase.domain.repository.UserRepository
import io.github.jan.supabase.postgrest.from
import java.util.UUID

class UserRepositoryImpl : UserRepository {
    private val supabase = SupabaseConfig.client

    override suspend fun getUsers(): Result<List<User>> {
        return try {
            val users = supabase.from("users")
                .select()
                .decodeList<User>()
            Result.success(users)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getUserById(id: String): Result<User?> {
        return try {
            val user = supabase.from("users")
                .select {
                    filter {
                        eq("id", id)
                    }
                }
                .decodeSingleOrNull<User>()
            Result.success(user)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun insertUser(user: User): Result<User> {
        return try {
            val newUser = user.copy(id = UUID.randomUUID().toString())
            val insertedUser = supabase.from("users")
                .insert(newUser) {
                    // yêu cầu Supabase trả lại row vừa insert
                    select()
                }
                .decodeSingle<User>()
            Result.success(insertedUser)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun updateUser(user: User): Result<User> {
        return try {
            val updatedUser = supabase.from("users")
                .update(user) {
                    filter {
                        eq("id", user.id)
                    }
                    // yêu cầu trả lại row sau khi update
                    select()
                }
                .decodeSingle<User>()
            Result.success(updatedUser)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun deleteUser(id: String): Result<Boolean> {
        return try {
            supabase.from("users")
                .delete {
                    filter {
                        eq("id", id)
                    }
                }
            Result.success(true)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun searchUsers(query: String, gender: String?): Result<List<User>> {
        return try {
            val users = supabase.from("users")
                .select {
                    filter {
                        if (query.isNotEmpty()) {
                            ilike("name", "%$query%")
                        }
                        if (!gender.isNullOrEmpty() && gender != "Tất cả") {
                            eq("gender", gender)
                        }
                    }
                }
                .decodeList<User>()
            Result.success(users)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
