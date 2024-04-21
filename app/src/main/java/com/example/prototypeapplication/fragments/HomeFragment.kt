package com.example.prototypeapplication.fragments

import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import com.example.prototypeapplication.GamePhaseActivity
import com.example.prototypeapplication.R

class HomeFragment : Fragment() {
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val gamePhaseTwoFragment = GamePhaseTwoFragment()
        val view = inflater.inflate(R.layout.fragment_home, container, false)
        val btnPlay = view.findViewById<ImageButton>(R.id.play_button)
        btnPlay.setOnClickListener {
            val intent = Intent(activity, GamePhaseActivity::class.java)
            startActivity(intent)
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
            addToBackStack(null)  // Optional: if you want to add the transaction to the back stack
            commit()
        }
    }

}
