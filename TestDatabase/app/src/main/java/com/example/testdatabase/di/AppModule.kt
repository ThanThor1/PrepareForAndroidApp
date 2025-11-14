package com.example.testdatabase.di

import com.example.testdatabase.data.repository.UserRepositoryImpl
import com.example.testdatabase.domain.repository.UserRepository

object AppModule {
    fun provideUserRepository(): UserRepository {
        return UserRepositoryImpl()
    }
}