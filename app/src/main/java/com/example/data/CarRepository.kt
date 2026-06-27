package com.example.data

import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.tasks.await

class CarRepository(private val carDao: CarDao, private val firestore: FirebaseFirestore) {
    val allCars: Flow<List<CarEntity>> = carDao.getAllCars()
    val allReservations: Flow<List<ReservationEntity>> = carDao.getAllReservations()

    suspend fun getCarByRegNo(regNo: Int): CarEntity? {
        return carDao.getCarByRegNo(regNo)
    }

    suspend fun addCustomCar(car: CarEntity) {
        carDao.insertCar(car)
    }

    suspend fun makeReservation(carRegNo: Int, customerName: String, days: Int): Boolean {
        val success = carDao.makeReservation(carRegNo, customerName, days)
        if (success) {
            saveBookingToFirestore(carRegNo, customerName, days)
        }
        return success
    }

    private suspend fun saveBookingToFirestore(carRegNo: Int, customerName: String, days: Int) {
        val booking = mapOf(
            "carRegNo" to carRegNo,
            "customerName" to customerName,
            "days" to days,
            "timestamp" to System.currentTimeMillis()
        )
        firestore.collection("bookings").add(booking).await()
    }

    suspend fun returnCar(carRegNo: Int): Boolean {
        return carDao.returnCar(carRegNo)
    }
}
