package com.example.prototypeapplication.fragments

import android.hardware.SensorEventListener
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.os.CountDownTimer
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.fragment.app.activityViewModels
import java.util.Timer
import java.util.TimerTask

import com.example.prototypeapplication.R
import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorManager
import android.media.MediaPlayer
//import android.os.Handler
//import android.os.Looper
import com.example.prototypeapplication.SharedViewModel
import com.example.prototypeapplication.ml.Model
import kotlinx.coroutines.delay
import org.tensorflow.lite.DataType
import org.tensorflow.lite.support.tensorbuffer.TensorBuffer
import java.nio.ByteBuffer
import java.nio.ByteOrder

class GamePhaseTwoFragment : Fragment(), SensorEventListener {
    private lateinit var countdownTimer: TextView
    private var isStart: Boolean = false
    private lateinit var timer: CountDownTimer


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
    private lateinit var alarm: MediaPlayer

    private val nTimesteps = 25
    private val nFeatures = 6
    private val currentInstanceData = Array(nTimesteps) { FloatArray(nFeatures) }
    private var timestepIndex = 0
    private val sensorData = arrayListOf<Float>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        model = Model.newInstance(requireActivity())

        sensorManager = requireActivity().getSystemService(Context.SENSOR_SERVICE) as SensorManager
        accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION)!!
        gyroscope = sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE)!!

    }
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_game_phase_two, container, false)
        val gamePhaseThreeFragment = GamePhaseThreeFragment()
        val btnStart = view.findViewById<ImageButton>(R.id.button_start)
        alarm = MediaPlayer.create(requireContext(),R.raw.buzzer_beater_trimmed)

        countdownTimer = view.findViewById(R.id.countdown_timer)


        btnStart.setOnClickListener {
            startTime()
            if(isStart){
                sensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_GAME)
                sensorManager.registerListener(this, gyroscope, SensorManager.SENSOR_DELAY_GAME)
            }
            else{
                sensorManager.unregisterListener(this)
            }
        }


        return view
    }

    private fun startTime() {
        val gamePhaseThreeFragment = GamePhaseThreeFragment()
        if(!isStart) {
            timer = object : CountDownTimer(30000, 1000) {
                override fun onTick(millisUntilFinished: Long) {
                    val seconds = millisUntilFinished / 1000
                    countdownTimer.setText("$seconds")
                }

                override fun onFinish() {
                    alarm.start()
                    val timer = Timer()
                    timer.schedule(object : TimerTask() {
                        override fun run() {
                            alarm.stop()
                            alarm.release()
                        }
                    }, 5000)  // Stop after 5 seconds
                    makeCurrentFragment(gamePhaseThreeFragment)
                }
            }.start()
            isStart = true
        }
        else{
            timer.cancel()
            countdownTimer.setText("30")
            isStart = false
        }

    }

    private fun makeCurrentFragment(fragment: Fragment) {
        val fragmentManager = requireActivity().supportFragmentManager
        fragmentManager.beginTransaction().apply {
            setCustomAnimations(
                R.anim.fade_in,  // enter
                R.anim.fade_out, // exit
                R.anim.fade_in,  // popEnter
                R.anim.fade_out  // popExit
            )
            replace(R.id.fl_wrapper, fragment)
            addToBackStack(null)  // Optional: if you want to add the transaction to the back stack
            commit()
        }
    }

    override fun onResume() {
        super.onResume()
//        sensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_GAME)
//        sensorManager.registerListener(this, gyroscope, SensorManager.SENSOR_DELAY_GAME)
    }

    override fun onPause() {
        super.onPause()
        sensorManager.unregisterListener(this)
    }

    private val accelerometerData = FloatArray(3)
    private val gyroscopeData = FloatArray(3)
    private var hasAccelData = false
    private var hasGyroData = false
    private val sharedViewModel: SharedViewModel by activityViewModels()

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

    private fun sendSensorData(data: FloatArray) {
        sharedViewModel.addSensorData(data)
    }
    override fun onSensorChanged(event: SensorEvent) {
        when (event.sensor.type) {
            Sensor.TYPE_LINEAR_ACCELERATION -> {
                accelerometerData[0] = event.values[2]
                accelerometerData[1] = event.values[1]
                accelerometerData[2] = event.values[0]
                hasAccelData = true
//                accelerometerTextView.text = "Accelerometer: X: ${accelerometerData[0]}, Y: ${accelerometerData[1]}, Z: ${accelerometerData[2]}"
            }
            Sensor.TYPE_GYROSCOPE -> {
                gyroscopeData[0] = event.values[2]
                gyroscopeData[1] = event.values[1]
                gyroscopeData[2] = event.values[0]
                hasGyroData = true
//                gyroscopeTextView.text = "Gyroscope: X: ${gyroscopeData[0]}, Y: ${gyroscopeData[1]}, Z: ${gyroscopeData[2]}"
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
        sendSensorData(combinedData)
        //val combinedData = accelerometerData + gyroscopeData
        currentInstanceData[timestepIndex] = combinedData
        sensorData.clear()
        timestepIndex++

        if (timestepIndex == nTimesteps) {
            timestepIndex = 0
//            sendSensorData(reshapeData(currentInstanceData))
        }
    }


    fun reshapeData(instanceData: Array<FloatArray>): Array<FloatArray> {
        val reshapedInstanceData = Array(instanceData.size) { FloatArray(6) { 0f } } // Initialize with padding
        for (i in instanceData.indices) {
            System.arraycopy(instanceData[i], 0, reshapedInstanceData[i], 0, instanceData[i].size)
        }
        return reshapedInstanceData
    }


    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
        // Handle sensor accuracy changes, if needed
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
