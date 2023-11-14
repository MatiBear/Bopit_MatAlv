package com.example.bopit_matalv

import android.animation.ArgbEvaluator
import android.animation.ValueAnimator
import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.media.MediaPlayer
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.GestureDetector
import android.view.MotionEvent
import android.view.View
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.example.bopit_matalv.Classes.DataStorageHelper
import kotlin.concurrent.fixedRateTimer
import kotlin.random.Random

class GameActivity : AppCompatActivity() {

    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var gestureDetector: GestureDetector
    private lateinit var sensorManager: SensorManager
    private var accelerometer: Sensor? = null
    private lateinit var firstText: TextView
    private lateinit var secondText: TextView
    private lateinit var highScoreText: TextView
    private lateinit var currentScoreText: TextView

    private var highScore = 0
    private var currentScore = 0
    private var timeToRespond = 3 // Default value, can be changed based on persistent data
    private var actionsInARow = 1

    private var actualAcceleration = 0f
    private val shakeThreshold = 10
    private var lastTime: Long = 0
    private var lastX: Float = 0f
    private var lastY: Float = 0f
    private var lastZ: Float = 0f

    private lateinit var mediaPlayer: MediaPlayer
    private var isTaskCompleted = false
    private var shakenAlready = false
    private var taskSuccess = false
    private var taskAttempted = false
    private var actionsInARowCounter = 0

    private var continueCountdown = true

    val dataStorageHelper = DataStorageHelper(this)

    private lateinit var mediaPlayerMusic: MediaPlayer
    private var playbackSpeed = 1.0f
    private var speedUpCounter = 0

    @SuppressLint("StringFormatMatches")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.game_screen)

        firstText = findViewById(R.id.firstText)
        secondText = findViewById(R.id.secondText)
        highScoreText = findViewById(R.id.highScoreText)
        currentScoreText = findViewById(R.id.currentScoreText)

        mediaPlayer = MediaPlayer.create(this, R.raw.gamewin2)

        sensorManager = getSystemService(SENSOR_SERVICE) as SensorManager
        accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)

        gestureDetector = GestureDetector(this, GestureListener())

        sensorManager.registerListener(accelerometerListener, accelerometer, SensorManager.SENSOR_DELAY_NORMAL)

        mediaPlayerMusic = MediaPlayer.create(this, R.raw.gamemusic)
        mediaPlayerMusic.isLooping = true

        // Start playing the music
        mediaPlayerMusic.start()

        // Load data from SharedPreferences
        timeToRespond = dataStorageHelper.retrieveData("timeToRespond", 3)
        highScore = dataStorageHelper.retrieveData("highestScore", 0)
        actionsInARow = dataStorageHelper.retrieveData("actionsInARow", 1)
        updateHighScoreText()
        updateTimeToRespondText()
        currentScoreText.text = getString(R.string.score_value, currentScore)

        startColorAnimation()

        // Start the game loop
        startGameLoop()
    }

    private fun increasePlaybackSpeed(increment: Float) {
        // Store the current position in the music
        val currentPosition = mediaPlayerMusic.currentPosition

        // Release the current MediaPlayer
        mediaPlayerMusic.release()

        // Create a new MediaPlayer with the updated speed
        mediaPlayerMusic = MediaPlayer.create(this, R.raw.gamemusic)
        mediaPlayerMusic.isLooping = true
        mediaPlayerMusic.playbackParams = mediaPlayerMusic.playbackParams.setSpeed(playbackSpeed + increment)

        // Seek to the stored position in the music
        mediaPlayerMusic.seekTo(currentPosition)

        // Start playing the music
        mediaPlayerMusic.start()

        // Update the playback speed
        playbackSpeed += increment
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        return gestureDetector.onTouchEvent(event) || super.onTouchEvent(event)
    }

    inner class GestureListener : GestureDetector.SimpleOnGestureListener() {

        override fun onFling(
            e1: MotionEvent?,
            e2: MotionEvent,
            velocityX: Float,
            velocityY: Float
        ): Boolean {

            if (!taskAttempted) {
                if (firstText.text == getString(R.string.fling)) {
                    // Correct response to the "Slide" instruction
                    // Increase the score and update UI
                    handleTaskCompletion()
                } else if (firstText.text != getString(R.string.complete)) {
                    // Incorrect response to the "Slide" instruction
                    // End the game
                    endGame()
                }
            }
            return super.onFling(e1, e2!!, velocityX, velocityY)
        }

        override fun onSingleTapUp(e: MotionEvent): Boolean {

            if (!taskAttempted) {
                if (firstText.text == getString(R.string.tap)) {
                    // Correct response to the "Tap" instruction
                    // Increase the score and update UI
                    handleTaskCompletion()
                } else if (firstText.text != getString(R.string.complete)) {
                    // Incorrect response to the "Tap" instruction
                    // End the game
                    endGame()
                }
            }
            return super.onSingleTapUp(e)
        }
    }

    private val accelerometerListener = object : SensorEventListener {
        override fun onSensorChanged(event: SensorEvent) {
            val x = event.values[0]
            val y = event.values[1]
            val z = event.values[2]

            detectShake(x, y, z)
        }

        override fun onAccuracyChanged(sensor: Sensor, accuracy: Int) {
            // This is needed for it to work - DON'T REMOVE
        }
    }

    private fun detectShake(x: Float, y: Float, z: Float) {
        val currentTime = System.currentTimeMillis()
        val diffTime = currentTime - lastTime

        if (diffTime > 100) { // Minimum time interval between shakes
            val deltaX = x - lastX
            val deltaY = y - lastY
            val deltaZ = z - lastZ

            val acceleration = (deltaX * deltaX + deltaY * deltaY + deltaZ * deltaZ) / (SensorManager.GRAVITY_EARTH * SensorManager.GRAVITY_EARTH)
            actualAcceleration = (Math.sqrt(acceleration.toDouble()) * SensorManager.GRAVITY_EARTH).toFloat()

            if (actualAcceleration > shakeThreshold) {

                if (!shakenAlready && !taskAttempted) { // 1000 = 1 second
                    if (firstText.text == getString(R.string.shake)) {
                        // Correct response to the "Shake" instruction
                        handleTaskCompletion()
                    } else if (firstText.text != getString(R.string.complete)) {
                        // Incorrect response to the "Shake" instruction
                        // End the game
                        endGame()
                    }
                    shakenAlready = true
                }
            }

            lastTime = currentTime
            lastX = x
            lastY = y
            lastZ = z
        }
    }

    private fun startGameLoop() {
        val handler = Handler(Looper.getMainLooper())
        val instructions = listOf(getString(R.string.tap), getString(R.string.fling), getString(R.string.shake))

        val gameRunnable = object : Runnable {
            @SuppressLint("SetTextI18n", "StringFormatMatches")
            override fun run() {
                val randomIndex = (0 until instructions.size).random()
                val currentInstruction = instructions[randomIndex]
                firstText.text = currentInstruction
                continueCountdown = true

                // Show time to respond
                secondText.text = getString(R.string.time_left, timeToRespond)

                var remainingTime = timeToRespond

                // Countdown logic
                val countdownRunnable = object : Runnable {
                    override fun run() {
                        if (remainingTime >= 0 && continueCountdown && !taskAttempted
                            && firstText.text != getString(R.string.fin_del_juego)
                                && firstText.text != getString(R.string.complete)) {
                            // Display countdown in instructionsText
                            secondText.text = getString(R.string.time_left, remainingTime)
                            remainingTime--

                            // Schedule the next iteration
                            handler.postDelayed(this, 1000L) // 1000 milliseconds (1 second)
                        } else {
                            // Time's up or countdown interrupted
                            if (!isTaskCompleted && firstText.text != getString(R.string.fin_del_juego)
                                && firstText.text != getString(R.string.complete)) {
                                // Task was not completed, end the game
                                endGame()
                            }
                        }
                    }
                }

                // Start the countdown
                handler.post(countdownRunnable)

                // After a brief delay, check if the task is completed
                Handler(Looper.getMainLooper()).postDelayed({
                    if (isTaskCompleted && taskSuccess && firstText.text != getString(R.string.fin_del_juego)) {
                        // Task was completed, continue the game
                        taskAttempted = false
                        taskSuccess = false
                        shakenAlready = false

                        handler.removeCallbacks(countdownRunnable) // Stop the countdown
                        startGameLoop() // Start the next task

                    } else if (firstText.text != getString(R.string.fin_del_juego) && firstText.text != getString(R.string.complete)) {
                        // Task was not completed, end the game
                        endGame()
                    }
                    continueCountdown = false
                }, (timeToRespond * 1000).toLong()) // Convert seconds to milliseconds
            }
        }

        handler.post(gameRunnable)
    }

    private fun handleTaskCompletion() {

        actionsInARowCounter++
        taskAttempted = true
        taskSuccess = true
        isTaskCompleted = true

        if (actionsInARowCounter == actionsInARow) {
            completeTask()
            actionsInARowCounter = 0 // Reset the counter
        } else {
            completeTaskLeft()
        }
    }

    private fun completeTask() {
        // Task completed successfully 'actionsInARow' times
        updateCurrentScore()
        firstText.text = getString(R.string.complete)
        playSound(R.raw.gamewin2)
        increasePlaybackSpeed(0.1f)
        updateScoreTextWithAnimation(getString(R.string.bonus_score))
    }

    @SuppressLint("StringFormatMatches")
    private fun completeTaskLeft() {
        // Task completed successfully but still some lefts to complete
        firstText.text = getString(R.string.complete)
        playSound(R.raw.gamewin2)
        var actionsLeft = actionsInARow - actionsInARowCounter
        updateScoreTextWithAnimation(getString(R.string.tasks_left_string, actionsLeft))
    }

    @SuppressLint("SetTextI18n", "StringFormatMatches")
    private fun updateHighScoreText() {
        highScoreText.text = getString(R.string.best_score, highScore)
    }

    @SuppressLint("SetTextI18n")
    private fun updateTimeToRespondText() {
        secondText.text = "Time to Respond: $timeToRespond"
    }

    @SuppressLint("SetTextI18n", "StringFormatMatches")
    private fun updateCurrentScore() {
        currentScore++
        currentScoreText.text = getString(R.string.score_value, currentScore)
        firstText.text = getString(R.string.complete)

        updateScoreTextWithAnimation("+1 Score")
    }

    private fun updateScoreTextWithAnimation(message: String) {
        // You can implement text color animation or other effects here if needed
        secondText.text = message
    }

    private fun startColorAnimation() {
        val colorFrom = getColor(R.color.orange) // Start color (orange)
        val colorTo = getColor(R.color.yellow)  // End color (yellow)

        val colorAnimator = ValueAnimator.ofObject(ArgbEvaluator(), colorFrom, colorTo)
        colorAnimator.duration = 5000 // Transition duration in milliseconds (5 seconds)
        colorAnimator.repeatCount = ValueAnimator.INFINITE
        colorAnimator.repeatMode = ValueAnimator.REVERSE

        colorAnimator.addUpdateListener { animator ->
            val color = animator.animatedValue as Int
            setLayoutBackgroundColor(color)
        }

        colorAnimator.start()
    }

    private fun setLayoutBackgroundColor(color: Int) {
        // Set the background color of your layout or any other view
        val background = findViewById<View>(R.id.gameBackground) // Replace with your background view ID
        background.setBackgroundColor(color)
    }

    // -------------------------------------------------------------------------------

    @SuppressLint("StringFormatMatches")
    private fun endGame() {
        firstText.text = getString(R.string.fin_del_juego)
        if (currentScore > highScore) {
            secondText.text = getString(R.string.new_highscore, currentScore)
            dataStorageHelper.updateValue(currentScore, "highestScore")
        } else {
            secondText.text = getString(R.string.final_score, currentScore)
        }
        playSound(R.raw.gameover)

        taskAttempted = true
        taskSuccess = false

        Handler(Looper.getMainLooper()).postDelayed({
            finishGame()
        }, 3000) // Delay the call to finishGame() by 3000 milliseconds (3 seconds)
    }


    private fun finishGame() {
        finish()
    }

    private fun playSound(soundResource: Int) {
        mediaPlayer = MediaPlayer.create(this, soundResource)
        mediaPlayer.start()
    }

    override fun onPause() {
        super.onPause()
        if (mediaPlayer.isPlaying) {
            mediaPlayer.pause()
        }
        if (mediaPlayerMusic.isPlaying) {
            mediaPlayerMusic.pause()
        }
    }

    override fun onResume() {
        super.onResume()
        if (!mediaPlayer.isPlaying) {
            mediaPlayer.start()
        }
        if (!mediaPlayerMusic.isPlaying) {
            mediaPlayerMusic.start()
        }
    }

    override fun onStop() {
        super.onStop()
        if (mediaPlayer.isPlaying) {
            mediaPlayer.stop()
        }
        if (mediaPlayerMusic.isPlaying) {
            mediaPlayerMusic.stop()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        mediaPlayer.release()
        mediaPlayerMusic.release()
    }
}