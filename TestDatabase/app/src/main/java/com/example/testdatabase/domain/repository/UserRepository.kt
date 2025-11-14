package com.example.testdatabase.domain.repository

import com.example.testdatabase.domain.model.User
import kotlinx.coroutines.flow.Flow

interface UserRepository {
    suspend fun getUsers(): Result<List<User>>
    suspend fun getUserById(id: String): Result<User?>
    suspend fun insertUser(user: User): Result<User>
    suspend fun updateUser(user: User): Result<User>
    suspend fun deleteUser(id: String): Result<Boolean>
    suspend fun searchUsers(query: String, gender: String?): Result<List<User>>
}