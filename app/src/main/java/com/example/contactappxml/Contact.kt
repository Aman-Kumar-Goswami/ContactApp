package com.example.contactappxml

import kotlinx.serialization.Serializable

@Serializable
data class Contact(
    val name: String,
    val phone: String? = null,
    val email: String? = null,
    val image_url: String? = null
)
