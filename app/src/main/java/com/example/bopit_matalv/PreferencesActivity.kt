package com.example.bopit_matalv

import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.example.bopit_matalv.Classes.DataStorageHelper

class PreferencesActivity : AppCompatActivity() {

    private lateinit var sharedPreferences: SharedPreferences

    private lateinit var backButton: Button
    private lateinit var timeToRespondValue: TextView
    private lateinit var decreaseTimeButton: Button
    private lateinit var increaseTimeButton: Button
    private lateinit var actionsInARowValue: TextView
    private lateinit var decreaseActionsButton: Button
    private lateinit var increaseActionsButton: Button
    private lateinit var highestScoreValue: TextView
    private lateinit var resetDataButton: Button

    val dataStorageHelper = DataStorageHelper(this)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.preference_screen)

        // Initialize UI components
        backButton = findViewById(R.id.backButton)
        timeToRespondValue = findViewById(R.id.timeToRespondValue)
        decreaseTimeButton = findViewById(R.id.decreaseTimeButton)
        increaseTimeButton = findViewById(R.id.increaseTimeButton)
        actionsInARowValue = findViewById(R.id.actionsInARowValue)
        decreaseActionsButton = findViewById(R.id.decreaseActionsButton)
        increaseActionsButton = findViewById(R.id.increaseActionsButton)
        highestScoreValue = findViewById(R.id.highestScoreValue)
        resetDataButton = findViewById(R.id.resetDataButton)

        // Set initial values
        timeToRespondValue.text = dataStorageHelper.retrieveData("timeToRespond", 3).toString()
        actionsInARowValue.text = dataStorageHelper.retrieveData("actionsInARow", 1).toString()
        highestScoreValue.text = dataStorageHelper.retrieveData("highestScore", 0).toString()

        // Set button click listeners
        backButton.setOnClickListener { finish() }
        decreaseTimeButton.setOnClickListener { dataStorageHelper.updateValue(timeToRespondValue, -1, "timeToRespond") }
        increaseTimeButton.setOnClickListener { dataStorageHelper.updateValue(timeToRespondValue, 1, "timeToRespond") }
        decreaseActionsButton.setOnClickListener { dataStorageHelper.updateValue(actionsInARowValue, -1, "actionsInARow") }
        increaseActionsButton.setOnClickListener { dataStorageHelper.updateValue(actionsInARowValue, 1, "actionsInARow") }
        resetDataButton.setOnClickListener { resetData() }
    }

    private fun resetData() {
        timeToRespondValue.text = "3"
        actionsInARowValue.text = "1"
        highestScoreValue.text = "0"

        dataStorageHelper.retrieveData("timeToRespond", 3)
        dataStorageHelper.retrieveData("actionsInARow", 1)
        dataStorageHelper.retrieveData("highestScore", 0)
    }
}
