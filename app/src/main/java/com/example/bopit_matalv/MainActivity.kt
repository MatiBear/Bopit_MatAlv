package com.example.bopit_matalv

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.ValueAnimator
import android.content.Context
import android.content.Intent
import android.graphics.Color
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.TextView

class MainActivity : AppCompatActivity() {

    // Define default values
    private val defaultTimeToRespond = 3
    private val defaultActionsInARow = 1
    private val defaultHighestScore = 0
    private lateinit var titleTextView: TextView
    private lateinit var animator: ValueAnimator

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Check if persistent values are set, if not, set to default values
        checkAndSetDefaultValues()

        titleTextView = findViewById(R.id.titleTextView)

        // Create a ValueAnimator to animate the hue of the text color
        animator = ValueAnimator.ofFloat(0f, 1f)
        animator.duration = 5000 // Duration of the animation in milliseconds
        animator.repeatCount = ValueAnimator.INFINITE // Repeat the animation infinitely

        animator.addUpdateListener { animation ->
            // Update the hue of the text color
            val hue = animation.animatedFraction * 360
            val rainbowColor = Color.HSVToColor(floatArrayOf(hue, 1f, 1f))

            // Set the new text color
            titleTextView.setTextColor(rainbowColor)
        }

        // Set an animation listener to reset the color when the animation ends
        animator.addListener(object : AnimatorListenerAdapter() {
            override fun onAnimationEnd(animation: Animator) {
                super.onAnimationEnd(animation)
                titleTextView.setTextColor(getColor(R.color.white)) // Set the text color back to white
            }
        })

        // Start the animator
        animator.start()
    }

    override fun onDestroy() {
        super.onDestroy()

        // Stop the animator when the activity is destroyed to prevent memory leaks
        animator.cancel()
    }

    private fun checkAndSetDefaultValues() {
        val sharedPreferences = getPreferences(Context.MODE_PRIVATE)

        // Check if values are set
        val timeToRespond = sharedPreferences.getInt("timeToRespond", -1)
        val actionsInARow = sharedPreferences.getInt("actionsInARow", -1)
        val highestScore = sharedPreferences.getInt("highestScore", -1)

        // If any value is not set, set to default values
        if (timeToRespond == -1 || actionsInARow == -1 || highestScore == -1) {
            with(sharedPreferences.edit()) {
                putInt("timeToRespond", defaultTimeToRespond)
                putInt("actionsInARow", defaultActionsInARow)
                putInt("highestScore", defaultHighestScore)
                apply()
            }
        }
    }

    // Click handler for the "About" button
    fun onAboutButtonClick(view: View) {
        val intent = Intent(this, AboutActivity::class.java)
        startActivity(intent)
    }

    // Click handler for the "Play" button
    fun onPlayButtonClick(view: View) {
        val intent = Intent(this, GameActivity::class.java)
        startActivity(intent)
    }

    // Click handler for the "Preferences" button
    fun onPreferencesButtonClick(view: View) {
        val intent = Intent(this, PreferencesActivity::class.java)
        startActivity(intent)
    }
}