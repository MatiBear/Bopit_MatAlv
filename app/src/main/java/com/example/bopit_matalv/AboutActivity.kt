package com.example.bopit_matalv

import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity

class AboutActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.about_activity)

        val backButton: Button = findViewById(R.id.backButton)

        backButton.setOnClickListener {
            finish() // Close the current activity and return to the previous one
        }
    }
}