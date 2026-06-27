package com.example.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.CarEntity
import com.example.data.CarRepository
import com.example.data.ReservationEntity
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

sealed interface UiMessage {
    data class Success(val message: String) : UiMessage
    data class Error(val message: String) : UiMessage
}

class CarViewModel(private val repository: CarRepository) : ViewModel() {

    private val _isSignedIn = MutableStateFlow(false)
    val isSignedIn: StateFlow<Boolean> = _isSignedIn.asStateFlow()

    fun signIn() {
        _isSignedIn.value = true
        // In a real app, this would trigger the Google Sign-In intent
    }

    fun signOut() {
        _isSignedIn.value = false
    }

    val cars: StateFlow<List<CarEntity>> = repository.allCars
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val reservations: StateFlow<List<ReservationEntity>> = repository.allReservations
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    private val _messageFlow = MutableSharedFlow<UiMessage>()
    val messageFlow: SharedFlow<UiMessage> = _messageFlow.asSharedFlow()

    fun reserveCar(regNo: Int, customerName: String, days: Int) {
        viewModelScope.launch {
            if (customerName.isBlank()) {
                _messageFlow.emit(UiMessage.Error("Please enter a valid customer name"))
                return@launch
            }
            if (days <= 0) {
                _messageFlow.emit(UiMessage.Error("Rental duration must be at least 1 day"))
                return@launch
            }

            val car = repository.getCarByRegNo(regNo)
            if (car == null) {
                _messageFlow.emit(UiMessage.Error("Registration No: $regNo does not exist!"))
                return@launch
            }

            if (!car.available) {
                _messageFlow.emit(UiMessage.Error("This car is currently unavailable (already rented)"))
                return@launch
            }

            val success = repository.makeReservation(regNo, customerName, days)
            if (success) {
                val totalBill = car.rent * days
                _messageFlow.emit(UiMessage.Success("Successfully Reserved! Bill for ${car.brand} is: $$totalBill"))
            } else {
                _messageFlow.emit(UiMessage.Error("Failed to reserve. Car may have been updated."))
            }
        }
    }

    fun returnCar(regNo: Int) {
        viewModelScope.launch {
            val car = repository.getCarByRegNo(regNo)
            if (car == null) {
                _messageFlow.emit(UiMessage.Error("Incorrect Registration No!"))
                return@launch
            }

            if (car.available) {
                _messageFlow.emit(UiMessage.Error("Car with Reg No: $regNo is already marked as available!"))
                return@launch
            }

            val success = repository.returnCar(regNo)
            if (success) {
                _messageFlow.emit(UiMessage.Success("Car ${car.brand} (Reg: $regNo) has been returned successfully!"))
            } else {
                _messageFlow.emit(UiMessage.Error("Error processing return."))
            }
        }
    }

    fun addNewCar(regNo: Int, brand: String, rent: Int, category: String, fuel: String, transmission: String, seats: Int) {
        viewModelScope.launch {
            if (brand.isBlank()) {
                _messageFlow.emit(UiMessage.Error("Car Brand cannot be empty"))
                return@launch
            }
            if (rent <= 0) {
                _messageFlow.emit(UiMessage.Error("Price per day must be greater than zero"))
                return@launch
            }
            if (regNo <= 0) {
                _messageFlow.emit(UiMessage.Error("Invalid Registration Number"))
                return@launch
            }

            val existing = repository.getCarByRegNo(regNo)
            if (existing != null) {
                _messageFlow.emit(UiMessage.Error("A car with registration number $regNo already exists!"))
                return@launch
            }

            val newCar = CarEntity(
                regNo = regNo,
                brand = brand,
                rent = rent,
                available = true,
                category = category,
                fuel = fuel,
                transmission = transmission,
                seats = seats
            )
            repository.addCustomCar(newCar)
            _messageFlow.emit(UiMessage.Success("New Car ${newCar.brand} (Reg: ${newCar.regNo}) added to the database!"))
        }
    }
}

class ViewModelFactory(private val repository: CarRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(CarViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return CarViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
