package com.example.data

import kotlinx.coroutines.flow.Flow

class CarRepository(private val carDao: CarDao) {
    val allCars: Flow<List<CarEntity>> = carDao.getAllCars()
    val allReservations: Flow<List<ReservationEntity>> = carDao.getAllReservations()

    suspend fun getCarByRegNo(regNo: Int): CarEntity? {
        return carDao.getCarByRegNo(regNo)
    }

    suspend fun addCustomCar(car: CarEntity) {
        carDao.insertCar(car)
    }

    suspend fun makeReservation(carRegNo: Int, customerName: String, days: Int): Boolean {
        return carDao.makeReservation(carRegNo, customerName, days)
    }

    suspend fun returnCar(carRegNo: Int): Boolean {
        return carDao.returnCar(carRegNo)
    }
}
