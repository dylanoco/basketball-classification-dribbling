package com.example.prototypeapplication.fragments
import android.content.Intent
import android.media.MediaPlayer
import android.os.Bundle

import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.FrameLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.fragment.app.activityViewModels
import com.example.prototypeapplication.GamePhaseActivity

import com.example.prototypeapplication.R
//import android.os.Handler
//import android.os.Looper
import com.example.prototypeapplication.SharedViewModel
import com.example.prototypeapplication.ml.Model

class GamePhaseThreeFragment : Fragment() {
    private lateinit var model: Model
    private lateinit var score: TextView
    private lateinit var most_accurate_text: TextView
    private lateinit var least_accurate_text: TextView


    private var crossover = 0f
    private var crossover_score = 80f

    private var inandout = 0f
    private var inandout_score = 20f

    private var betweenthelegs = 0f
    private var btl_score = 40f

    private var total_score = 0f
    private var totalInteger = 0
    private var most_accurate = ""
    private var least_accurate = ""
    private var highscore_total = 0
    private var accuracy = 0f
    private var l_accuracy = 100f
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
        val windows = generateSlidingWindows(sensorDataList, 25, 5)
        // Now process each window with ML Model
        var total = processWindows(windows)
        val intent = Intent(activity, GamePhaseActivity::class.java)
        val backBtn = view.findViewById<Button>(R.id.back_button_id)
        val value = intent.getIntExtra("hs", highscore_total)
        val activityFrame = activity?.findViewById<FrameLayout>(R.id.fl_wrapper)
        val backgroundColor = ContextCompat.getColor(requireContext(), R.color.bg_main)
        val activityTextView = activity?.findViewById<TextView>(R.id.highscoreNumber)
        activityFrame?.setBackgroundColor(backgroundColor)

        backBtn.setOnClickListener {
            val transaction = parentFragmentManager.beginTransaction()
            transaction.replace(R.id.fl_wrapper, GamePhaseTwoFragment())
            transaction.commit()
        }

        totalInteger = Math.round(total)
        if(totalInteger > (activityTextView?.text).toString().toInt()){
            highscore_total = totalInteger
            sharedViewModel.updateHighScore(highscore_total)

            activityTextView?.text = "$highscore_total"
        }
        score = view.findViewById(R.id.score_id)
        score.text = "$totalInteger"
        accuracy = Math.round(accuracy).toFloat()
        most_accurate_text = view.findViewById(R.id.most_accurate_id)
        most_accurate_text.text = "$most_accurate at a $accuracy% Accuracy!"

        l_accuracy = Math.round(l_accuracy).toFloat()
        least_accurate_text = view.findViewById(R.id.least_accurate_id)
        least_accurate_text.text = "$least_accurate at a $l_accuracy% Accuracy!"


        sharedViewModel.clearSensorData()
        return view
    }

    private fun generateSlidingWindows(dataList: List<FloatArray>, windowSize: Int, stepSize: Int): List<Array<FloatArray>> {
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
            val prediction = model.process(inputTensor)
            // Handle prediction results
            val moveProbabilities = prediction.outputFeature0AsTensorBuffer.floatArray
            val sortedProbIndices = moveProbabilities.indices.sortedByDescending { moveProbabilities[it] }
            val maxProbIndex = sortedProbIndices[0]
            val leastProbIndex = sortedProbIndices[2]
            val leastProbability = moveProbabilities[leastProbIndex]
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
            if(maxProbability* 100 > accuracy){
                most_accurate = dribbleClass[maxProbIndex]
                accuracy = maxProbability * 100
            }
            if(l_accuracy == 100f){
                least_accurate = dribbleClass[maxProbIndex]
                l_accuracy = maxProbability * 100
            }
            if(maxProbability* 100 < l_accuracy){
                least_accurate = dribbleClass[maxProbIndex]
                l_accuracy = maxProbability * 100
            }
        }
        return total_score
    }


}