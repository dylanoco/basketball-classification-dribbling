package com.example.prototypeapplication
import androidx.lifecycle.ViewModel

class SharedViewModel : ViewModel() {
    // This will store a list of sensor data arrays, each Array<FloatArray> is a set of timesteps
    private val sensorDataList = mutableListOf<FloatArray>()
    private var highScore = 0

    fun addSensorData(data: FloatArray) {
        println("sdfsd")
        sensorDataList.add(data)
    }
    fun updateHighScore(data: Int) {
        highScore = data
    }
    fun getHighScore(): Int = highScore
    fun getSensorDataList(): MutableList<FloatArray> = sensorDataList

    // Optional: Clear data if needed
    fun clearSensorData() {
        sensorDataList.clear()
    }
}
