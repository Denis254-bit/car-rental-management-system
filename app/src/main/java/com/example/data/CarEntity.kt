package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "cars")
data class CarEntity(
    @PrimaryKey val regNo: Int,
    val brand: String,
    val rent: Int,
    val available: Boolean,
    val category: String,
    val fuel: String,
    val transmission: String,
    val seats: Int
)
