package com.example.testdatabase.domain.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
@OptIn(kotlinx.serialization.InternalSerializationApi::class)
data class User(
    val id: String = "",
    val name: String = "",
    val gender: String = "Nam",

    @SerialName("birthyear")          // map với cột "birthyear" trong Supabase
    val birthYear: Int = 2000,        // NOT NULL, có default

    val bio: String = "",

    @SerialName("imageurl")           // map với cột "imageurl"
    val imageUrl: String = ""
)