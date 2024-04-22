package com.example.prototypeapplication
import androidx.lifecycle.ViewModel

class SharedViewModel : ViewModel() {
    // This will store a list of sensor data arrays, each Array<FloatArray> is a set of timesteps
    private val sensorDataList = mutableListOf<FloatArray>()

    fun addSensorData(data: FloatArray) {
        println("sdfsd")
        sensorDataList.add(data)
    }

    fun getSensorDataList(): MutableList<FloatArray> = sensorDataList

    // Optional: Clear data if needed
    fun clearSensorData() {
        sensorDataList.clear()
    }
}
