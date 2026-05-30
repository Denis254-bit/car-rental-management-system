package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "reservations")
data class ReservationEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val carRegNo: Int,
    val carBrand: String,
    val days: Int,
    val bill: Int,
    val customerName: String,
    val timestamp: Long = System.currentTimeMillis(),
    val status: String // "Active" or "Returned"
)
