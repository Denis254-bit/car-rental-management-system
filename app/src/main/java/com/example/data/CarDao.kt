package com.example.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface CarDao {
    @Query("SELECT * FROM cars ORDER BY brand ASC")
    fun getAllCars(): Flow<List<CarEntity>>

    @Query("SELECT * FROM cars WHERE regNo = :regNo LIMIT 1")
    suspend fun getCarByRegNo(regNo: Int): CarEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCar(car: CarEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCars(cars: List<CarEntity>)

    @Update
    suspend fun updateCar(car: CarEntity)

    // Reservations
    @Query("SELECT * FROM reservations ORDER BY timestamp DESC")
    fun getAllReservations(): Flow<List<ReservationEntity>>

    @Query("SELECT * FROM reservations WHERE id = :id LIMIT 1")
    suspend fun getReservationById(id: Int): ReservationEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertReservation(reservation: ReservationEntity)

    @Update
    suspend fun updateReservation(reservation: ReservationEntity)

    @Transaction
    suspend fun makeReservation(carRegNo: Int, customerName: String, days: Int): Boolean {
        val car = getCarByRegNo(carRegNo)
        if (car != null && car.available) {
            val updatedCar = car.copy(available = false)
            updateCar(updatedCar)
            val bill = car.rent * days
            val reservation = ReservationEntity(
                carRegNo = carRegNo,
                carBrand = car.brand,
                days = days,
                bill = bill,
                customerName = customerName,
                status = "Active"
            )
            insertReservation(reservation)
            return true
        }
        return false
    }

    @Transaction
    suspend fun returnCar(carRegNo: Int): Boolean {
        val car = getCarByRegNo(carRegNo)
        if (car != null) {
            val updatedCar = car.copy(available = true)
            updateCar(updatedCar)
            // Mark the active reservation(s) as Returned
            // First we can find active reservations for this registration number
            val activeReservations = getActiveReservationsForCar(carRegNo)
            for (res in activeReservations) {
                updateReservation(res.copy(status = "Returned"))
            }
            return true
        }
        return false
    }

    @Query("SELECT * FROM reservations WHERE carRegNo = :carRegNo AND status = 'Active'")
    suspend fun getActiveReservationsForCar(carRegNo: Int): List<ReservationEntity>
}
