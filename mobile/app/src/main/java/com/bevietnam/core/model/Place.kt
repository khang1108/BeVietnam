package com.bevietnam.core.model

data class Place(
    val id: String,
    val name: String,
    val category: String,
    val description: String,
    val location: String,
    val imageUrl: String,
    val rating: Float = 0f,
    val reviewCount: Int = 0
)
