package com.example.gallery.presentation

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.navigation.fragment.NavHostFragment
import com.example.gallery.R

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setNavigationGraph()
    }

    private fun setNavigationGraph() {
        val navController =
            (supportFragmentManager.findFragmentById(R.id.main_nav_host) as NavHostFragment).navController
        val graph = navController.navInflater.inflate(R.navigation.navigation)
        navController.graph = graph
    }
}
