package com.example.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(entities = [CarEntity::class, ReservationEntity::class], version = 1, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    abstract val carDao: CarDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getInstance(context: Context, scope: CoroutineScope): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "car_rental_database"
                )
                .addCallback(DatabaseCallback(scope))
                .build()
                INSTANCE = instance
                instance
            }
        }
    }

    private class DatabaseCallback(
        private val scope: CoroutineScope
    ) : RoomDatabase.Callback() {
        override fun onCreate(db: SupportSQLiteDatabase) {
            super.onCreate(db)
            INSTANCE?.let { database ->
                scope.launch(Dispatchers.IO) {
                    populateInitialData(database.carDao)
                }
            }
        }

        private suspend fun populateInitialData(dao: CarDao) {
            val initialCars = listOf(
                CarEntity(123, "Suzuki", 2000, true, "Hatchback", "Petrol", "Manual", 5),
                CarEntity(234, "Honda", 2500, true, "Sedan", "Petrol", "Automatic", 5),
                CarEntity(345, "Toyota", 3000, true, "SUV", "Hybrid", "Automatic", 5),
                CarEntity(456, "MG", 3500, true, "Crossover", "Electric", "Automatic", 5),
                CarEntity(567, "BMW", 5000, true, "Luxury", "Petrol", "Automatic", 5)
            )
            dao.insertCars(initialCars)
        }
    }
}
