package com.example.prototypeapplication.fragments
import android.media.MediaPlayer
import android.os.Bundle

import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.activityViewModels

import com.example.prototypeapplication.R
//import android.os.Handler
//import android.os.Looper
import com.example.prototypeapplication.SharedViewModel
import com.example.prototypeapplication.ml.Model

class GamePhaseThreeFragment : Fragment() {
    private lateinit var model: Model
    private lateinit var score: TextView
    private lateinit var most_accurate_text: TextView


    private var crossover = 0f
    private var crossover_score = 80f

    private var inandout = 0f
    private var inandout_score = 20f

    private var betweenthelegs = 0f
    private var btl_score = 40f

    private var total_score = 0f

    private var most_accurate = ""
    private var accuracy = 0f
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        model = Model.newInstance(requireActivity())
    }
    private val sharedViewModel: SharedViewModel by activityViewModels()
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        val view = inflater.inflate(R.layout.fragment_game_phase_three, container, false)
        // Inflate the layout for this fragment
        val sensorDataList = sharedViewModel.getSensorDataList()
        val windows = generateSlidingWindows(sensorDataList, 25, 5)  // Assuming a window of 50 and step of 10
        // Now process each window with your ML model
        var total = processWindows(windows)
        total = Math.round(total).toFloat()
        score = view.findViewById(R.id.score_id)
        score.text = "Total Score: $total"
        accuracy = Math.round(accuracy).toFloat()
        most_accurate_text = view.findViewById(R.id.most_accurate_id)
        most_accurate_text.text = "Most Accurate Move: $most_accurate with a $accuracy %!"

        sharedViewModel.clearSensorData()
        return view
    }

    fun generateSlidingWindows(dataList: List<FloatArray>, windowSize: Int, stepSize: Int): List<Array<FloatArray>> {
        val windows = mutableListOf<Array<FloatArray>>()
        var startIndex = 0

        while (startIndex + windowSize <= dataList.size) {
            val window = dataList.subList(startIndex, startIndex + windowSize).toTypedArray()
            windows.add(window)
            startIndex += stepSize
        }

        return windows
    }


    fun processWindows(windows: List<Array<FloatArray>>): Float {

        val dribbleClass = arrayListOf<String>("Between the Legs", "Crossover","In And Out")
        windows.forEach { window ->
            val inputTensor = com.example.prototypeapplication.prepareTensor(window)
            val prediction = model.process(inputTensor)  // Implement prediction logic based on your model setup
            // Handle prediction results
            val moveProbabilities = prediction.outputFeature0AsTensorBuffer.floatArray
            val sortedProbIndices = moveProbabilities.indices.sortedByDescending { moveProbabilities[it] }
            val maxProbIndex = sortedProbIndices[0]
            val maxProbability = moveProbabilities[maxProbIndex]

            if(maxProbIndex == 0){
                betweenthelegs += 1
                total_score += btl_score * maxProbability
            }
            if(maxProbIndex == 1){
                crossover += 1
                total_score += crossover_score * maxProbability
            }
            if(maxProbIndex == 2){
                inandout += 1
                total_score += inandout_score * maxProbability
            }
            if(maxProbability > accuracy){
                most_accurate = dribbleClass[maxProbIndex]
                accuracy = maxProbability * 100
            }
        }
        return total_score
    }


}