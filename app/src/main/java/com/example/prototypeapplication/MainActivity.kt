package com.example.prototypeapplication

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Bundle
//import android.os.Handler
//import android.os.Looper
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.example.prototypeapplication.ml.Model
import org.tensorflow.lite.DataType
import org.tensorflow.lite.support.tensorbuffer.TensorBuffer
import java.nio.ByteBuffer
import java.nio.ByteOrder

class MainActivity : AppCompatActivity(), SensorEventListener {

    private lateinit var sensorManager: SensorManager
    private lateinit var accelerometer: Sensor
    private lateinit var gyroscope: Sensor
    private lateinit var accelerometerTextView: TextView
    private lateinit var gyroscopeTextView: TextView
    private lateinit var identifyTextView: TextView
    private lateinit var crossoverTextView : TextView
    private lateinit var inandoutTextView : TextView
    private lateinit var betweenthelegsTextView : TextView
    private lateinit var idleTextView : TextView
    private lateinit var model: Model

    private val nTimesteps = 25
    private val nFeatures = 6
    private val currentInstanceData = Array(nTimesteps) { FloatArray(nFeatures) }
    private var timestepIndex = 0
    private val sensorData = arrayListOf<Float>()
    private val dribbleClass = arrayListOf<String>("BTL", "Crossover", "Idle","In And Out")


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        model = Model.newInstance(this)

        sensorManager = getSystemService(Context.SENSOR_SERVICE) as SensorManager
        accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION)!!
        gyroscope = sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE)!!

        accelerometerTextView = findViewById(R.id.accelerometer_text)
        gyroscopeTextView = findViewById(R.id.gyroscope_text)
        identifyTextView = findViewById(R.id.identify_text)

        crossoverTextView = findViewById(R.id.crossover_text)
        inandoutTextView = findViewById(R.id.inandout_text)
        betweenthelegsTextView = findViewById(R.id.betweenthelegs_text)
        idleTextView = findViewById(R.id.idle_text)

    }

    override fun onResume() {
        super.onResume()
        sensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_GAME)
        sensorManager.registerListener(this, gyroscope, SensorManager.SENSOR_DELAY_GAME)
    }

    override fun onPause() {
        super.onPause()
        sensorManager.unregisterListener(this)
    }

    private val accelerometerData = FloatArray(3)
    private val gyroscopeData = FloatArray(3)
    private var hasAccelData = false
    private var hasGyroData = false

    private val minValues = floatArrayOf(-28.953999f,
            -11.058505f,
            -23.228081f,
            -4.851873f,
            -12.815800f,
            -4.988631f)
    private val maxValues = floatArrayOf(20.432932f,
            24.831491f,
            45.445141f,
            4.582329f,
            10.470383f,
            5.813910f)

    override fun onSensorChanged(event: SensorEvent) {
        when (event.sensor.type) {
            Sensor.TYPE_LINEAR_ACCELERATION -> {
                accelerometerData[0] = event.values[2]
                accelerometerData[1] = event.values[1]
                accelerometerData[2] = event.values[0]
                hasAccelData = true
                accelerometerTextView.text = "Accelerometer: X: ${accelerometerData[0]}, Y: ${accelerometerData[1]}, Z: ${accelerometerData[2]}"
            }
            Sensor.TYPE_GYROSCOPE -> {
                gyroscopeData[0] = event.values[2]
                gyroscopeData[1] = event.values[1]
                gyroscopeData[2] = event.values[0]
                hasGyroData = true
                gyroscopeTextView.text = "Gyroscope: X: ${gyroscopeData[0]}, Y: ${gyroscopeData[1]}, Z: ${gyroscopeData[2]}"
            }
        }

        if (hasAccelData && hasGyroData) {
            // Combine accelerometerData and gyroscopeData into a single array and process it
            processSensorData(accelerometerData, gyroscopeData)
            hasAccelData = false
            hasGyroData = false
        }
    }

    private fun normalizeSensorData(data: FloatArray): FloatArray {
        return data.indices.map { i ->
            (data[i] - minValues[i]) / (maxValues[i] - minValues[i])
        }.toFloatArray()
    }

    private fun processSensorData(accelerometerData: FloatArray, gyroscopeData: FloatArray) {
        val combinedData = normalizeSensorData(accelerometerData + gyroscopeData)
        //val combinedData = accelerometerData + gyroscopeData
        currentInstanceData[timestepIndex] = combinedData
        sensorData.clear()
        timestepIndex++

        if (timestepIndex == nTimesteps) {
            timestepIndex = 0
//            classifyInstance(reshapeData(currentInstanceData))
        }
    }


    fun reshapeData(instanceData: Array<FloatArray>): Array<FloatArray> {
        val reshapedInstanceData = Array(instanceData.size) { FloatArray(6) { 0f } } // Initialize with padding
        for (i in instanceData.indices) {
            System.arraycopy(instanceData[i], 0, reshapedInstanceData[i], 0, instanceData[i].size)
        }
        return reshapedInstanceData
    }

    private fun classifyInstance(instanceData: Array<FloatArray>) {
        val inputTensor = prepareTensor(instanceData)
        // Run the model
        val confidenceThreshold = 0.6f
        val marginThreshold = 0.2f
        val outputs = model.process(inputTensor)
        val moveProbabilities = outputs.outputFeature0AsTensorBuffer.floatArray

        val sortedProbIndices = moveProbabilities.indices.sortedByDescending { moveProbabilities[it] }
        val maxProbIndex = sortedProbIndices[0]
        val secondMaxProbIndex = sortedProbIndices[1]

        val maxProbability = moveProbabilities[maxProbIndex]
        val secondMaxProbability = moveProbabilities[secondMaxProbIndex]

        if (maxProbIndex >= 0 && maxProbability >= confidenceThreshold && (maxProbability - secondMaxProbability) >= marginThreshold) {
            val predictedMove = dribbleClass[maxProbIndex]
            identifyTextView.text = predictedMove
        } else {
            identifyTextView.text = "Uncertain"
        }
    }


    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
    }
}
fun prepareTensor(instanceData: Array<FloatArray>): TensorBuffer {
    val numBytes = 25 * 6 * 4
    val byteBuffer = ByteBuffer.allocateDirect(numBytes).order(ByteOrder.nativeOrder())

    instanceData.forEach { timestep ->
        timestep.forEach { feature ->
            byteBuffer.putFloat(feature)
        }
    }

    return TensorBuffer.createFixedSize(intArrayOf(25, 6), DataType.FLOAT32).apply {
        loadBuffer(byteBuffer)
    }
}
