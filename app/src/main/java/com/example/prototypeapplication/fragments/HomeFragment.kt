package com.example.prototypeapplication.fragments

import android.content.Intent
import android.media.Image
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.fragment.app.activityViewModels
import com.example.prototypeapplication.GamePhaseActivity
import com.example.prototypeapplication.R
import com.example.prototypeapplication.SharedViewModel
import com.example.prototypeapplication.StarterScreenActivity

class HomeFragment : Fragment() {
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_home, container, false)
        val btnPlay = view.findViewById<ImageButton>(R.id.play_button)
        val highscoreDisplay = view.findViewById<TextView>(R.id.highscoreNumber)
        val intent = Intent(activity, GamePhaseActivity::class.java)
        (activity as StarterScreenActivity)?.let { starterActivity ->
            // Now you can access variables and methods
            val highscore_total = starterActivity.getHS()  // Calling method
            highscoreDisplay.text = highscore_total.toString()
            btnPlay.setOnClickListener {
                intent.putExtra("hs",highscore_total)
                startActivity(intent)
            }
        }
        return view
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
            addToBackStack(null)
            commit()
        }
    }

}
