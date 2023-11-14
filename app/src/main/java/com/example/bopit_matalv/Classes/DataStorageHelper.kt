package com.example.bopit_matalv.Classes

import android.content.Context
import android.widget.TextView

class DataStorageHelper(private val context: Context) {

    // Function to save data to SharedPreferences
    fun saveData(key: String, value: String) {
        val sharedPreferences = context.getSharedPreferences("MyPrefs", Context.MODE_PRIVATE)
        val editor = sharedPreferences.edit()
        editor.putString(key, value)
        editor.apply()
    }

    fun retrieveData(key: String, defaultValue: Int): Int {
        val sharedPreferences = context.getSharedPreferences("MyPrefs", Context.MODE_PRIVATE)
        val storedValue = sharedPreferences.getString(key, null)
        return storedValue?.toIntOrNull() ?: defaultValue
    }

    fun updateValue(textView: TextView, delta: Int, key: String) {
        val currentValue = retrieveData(key, 0)
        val newValue = (currentValue + delta).coerceAtLeast(1)
        textView.text = newValue.toString()
        saveData(key, newValue.toString())
    }

    fun updateValue(currentValue: Int, key: String) {
        saveData(key, currentValue.toString())
    }
}
