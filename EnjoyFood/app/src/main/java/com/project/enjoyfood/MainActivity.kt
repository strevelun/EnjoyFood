package com.project.enjoyfood

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import androidx.fragment.app.Fragment
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.project.enjoyfood.auth.IntroActivity
import com.project.enjoyfood.fragments.HomeFragment
import com.project.enjoyfood.fragments.TalkFragment
import com.project.enjoyfood.fragments.accountFragment
import com.project.enjoyfood.fragments.searchFragment

class MainActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        auth = Firebase.auth
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_main)

        val homeFragment = HomeFragment()
        val TalkFragment = TalkFragment()
        val accountFragment = accountFragment()
        val searchFragment = searchFragment()

        val bottomNavigationView = findViewById<BottomNavigationView>(R.id.bottomNavigationView)

        replaceFragment(homeFragment)

        bottomNavigationView.setOnNavigationItemSelectedListener {
            when(it.itemId)
            {
                R.id.homeTap ->replaceFragment(homeFragment)
                R.id.talkTap ->replaceFragment(TalkFragment)
                R.id.searchTap ->replaceFragment(searchFragment)
                R.id.accountTap ->replaceFragment(accountFragment)
            }
            true
        }
    }
    private fun replaceFragment(fragment: Fragment)
    {
        supportFragmentManager.beginTransaction()
            .apply {
                replace(R.id.fragmentContainer,fragment)
                commit()
            }
    }
}