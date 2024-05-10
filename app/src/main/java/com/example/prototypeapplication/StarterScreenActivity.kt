package com.example.prototypeapplication

import android.content.Intent
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.fragment.app.Fragment
import com.example.prototypeapplication.fragments.HighScoreFragment
import com.example.prototypeapplication.fragments.HomeFragment
import com.example.prototypeapplication.fragments.TutorialFragment
import com.google.android.material.bottomnavigation.BottomNavigationMenuView
import com.google.android.material.bottomnavigation.BottomNavigationView

class StarterScreenActivity : AppCompatActivity() {
    private var highscore_total = 0
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.starter_screen)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        val value = intent.getIntExtra("hs", highscore_total)
        println(value)
        highscore_total = value


        val homeFragment = HomeFragment()
        val HighScoreFragment = HighScoreFragment()
        val TutorialFragment = TutorialFragment()

        makeCurrentFragment(homeFragment)

        findViewById<BottomNavigationView>(R.id.bottomNavigationView).setOnItemSelectedListener {
            when(it.itemId){
                R.id.home -> makeCurrentFragment(homeFragment)
                R.id.high_score -> makeCurrentFragment(HighScoreFragment)
                R.id.tutorial -> makeCurrentFragment(TutorialFragment)
            }
            true
        }

    }
    private fun makeCurrentFragment(fragment: Fragment) =
        supportFragmentManager.beginTransaction().apply {
            setCustomAnimations(
                R.anim.fade_in,  // enter
                R.anim.fade_out, // exit
                R.anim.fade_in,  // popEnter
                R.anim.fade_out  // popExit
            )
            replace(R.id.fl_wrapper,fragment)
            commit()
        }
    fun getHS(): Int {
        return highscore_total
    }

}