package com.example.practica13

import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import kotlin.math.roundToInt
import kotlin.math.sqrt

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BurbujaScreen(sensorManager: SensorManager) {
    // Valores reales en tiempo real de los 3 ejes (X, Y, Z)
    var ejeX by remember { mutableFloatStateOf(0f) }
    var ejeY by remember { mutableFloatStateOf(0f) }
    var ejeZ by remember { mutableFloatStateOf(0f) }

    // Coordenadas para la burbuja del PDF
    var offsetX by remember { mutableFloatStateOf(0f) }
    var offsetY by remember { mutableFloatStateOf(0f) }

    // Estados para la detección de sacudida (Shake)
    var aceleracionVectorial by remember { mutableFloatStateOf(0f) }
    var sacudidaDetectada by remember { mutableStateOf(false) }
    var contadorSacudidas by remember { mutableIntStateOf(0) }

    val sensibilidad = 50f
    val radioMaximo = 250f

    val umbralSacudida = 15f


    LaunchedEffect(sacudidaDetectada) {
        if (sacudidaDetectada) {
            delay(1500)
            sacudidaDetectada = false
        }
    }

    DisposableEffect(Unit) {
        val listener = object : SensorEventListener {
            override fun onSensorChanged(event: SensorEvent?) {
                event?.let {

                    val x = it.values[0]
                    val y = it.values[1]
                    val z = it.values[2]

                    ejeX = x
                    ejeY = y
                    ejeZ = z


                    val rawX = x * sensibilidad
                    val rawY = y * sensibilidad
                    val distancia = sqrt((rawX * rawX) + (rawY * rawY))

                    if (distancia > radioMaximo) {
                        val proporcion = radioMaximo / distancia
                        offsetX = rawX * proporcion
                        offsetY = rawY * proporcion
                    } else {
                        offsetX = rawX
                        offsetY = rawY
                    }


                    val magnitudTotal = sqrt((x * x) + (y * y) + (z * z))
                    aceleracionVectorial = magnitudTotal

                    if (magnitudTotal > umbralSacudida && !sacudidaDetectada) {
                        sacudidaDetectada = true
                        contadorSacudidas++
                    }
                }
            }

            override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}
        }

        val acelerometro = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
        sensorManager.registerListener(listener, acelerometro, SensorManager.SENSOR_DELAY_GAME)

        onDispose {
            sensorManager.unregisterListener(listener)
        }
    }

    // Cambio de color animado en el fondo cuando se detecta una sacudida
    val colorFondo by animateColorAsState(
        targetValue = if (sacudidaDetectada) Color(0xFF6A1B29) else Color.DarkGray,
        label = "ColorFondoShake"
    )

    val colorBurbuja by animateColorAsState(
        targetValue = if (sacudidaDetectada) Color(0xFFFF9800) else Color(0xFF00FF00),
        label = "ColorBurbujaShake"
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Nivel de Burbuja y Detección Shake") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(colorFondo),
            contentAlignment = Alignment.Center
        ) {
            // Alerta visual (sacudida)
            Column(
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .padding(top = 24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Surface(
                    color = if (sacudidaDetectada) Color(0xFFFF5252) else Color(0xFF2C2C2C),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(
                        text = if (sacudidaDetectada) {
                            "¡SACUDIDA DETECTADA! (Total: $contadorSacudidas)"
                        } else {
                            "Estado: Estable (Sacudidas: $contadorSacudidas)"
                        },
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp)
                    )
                }
            }

            // Circulo del Nivel de Burbuja
            Box(
                modifier = Modifier
                    .size(250.dp)
                    .border(4.dp, Color.White, CircleShape)
                    .background(Color.Black, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Box(modifier = Modifier.size(50.dp).border(2.dp, Color.Red, CircleShape))

                Box(
                    modifier = Modifier
                        .offset { IntOffset(offsetX.roundToInt(), offsetY.roundToInt()) }
                        .size(40.dp)
                        .background(colorBurbuja, CircleShape)
                )
            }

            // Panel inferior con las tres coordenadas (X, Y, Z) y la fuerza vectorial
            Column(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(24.dp)
                    .background(Color(0x66000000), RoundedCornerShape(16.dp))
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text("Eje X: ${"%.2f".format(ejeX)} m/s²", color = Color.White, fontSize = 18.sp)
                Text("Eje Y: ${"%.2f".format(ejeY)} m/s²", color = Color.White, fontSize = 18.sp)
                Text("Eje Z: ${"%.2f".format(ejeZ)} m/s²", color = Color.White, fontSize = 18.sp)
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    "Fuerza Vectorial: ${"%.2f".format(aceleracionVectorial)} m/s²",
                    color = Color.Cyan,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 16.sp
                )
            }
        }
    }
}