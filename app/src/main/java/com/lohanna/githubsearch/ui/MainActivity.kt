package com.lohanna.githubsearch.ui

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.lohanna.githubsearch.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        onClick()
    }

    private fun onClick() {
        binding.apply {
            btnConfirm.setOnClickListener { }

            btnConfirm.setOnLongClickListener {
                it.isPressed = false
                true
            }
        }
    }
}