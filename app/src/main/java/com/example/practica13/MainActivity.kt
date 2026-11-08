package com.example.practica13

import android.content.Context
import android.hardware.SensorManager
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        // Obtenemos el servicio nativo de Sensores de Android
        val sensorManager = getSystemService(Context.SENSOR_SERVICE) as SensorManager
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            BurbujaScreen(sensorManager = sensorManager)
        }
    }
}