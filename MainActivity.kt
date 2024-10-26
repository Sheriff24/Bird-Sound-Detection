package com.example.soundrecorderapp

import android.Manifest
import android.media.MediaRecorder
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.result.contract.ActivityResultContracts.RequestPermission
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.Image // Import this
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.res.painterResource // Import this
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.io.File
import java.io.IOException

class MainActivity : ComponentActivity() {
    private var mediaRecorder: MediaRecorder? = null
    private var isRecording = mutableStateOf(false)
    private var outputFile: File? = null

    // Define bird names and their decibel ranges
    private val birds = listOf(
        Pair("Song Sparrow", "55.7 dB"),
        Pair("Northern Mockingbird", "71.8 dB"),
        Pair("American Robin", "52.8 dB"),
        Pair("Song Sparrow", "55.7 dB"),
        Pair("Northern Cardinal", "33.2 dB"),
        Pair("Bewick's Wren", "90.5 dB")
    )
    private var currentBirdIndex = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            RecorderApp()
        }
    }

    @Composable
    fun RecorderApp() {
        var isPermissionGranted by remember { mutableStateOf(false) }
        val permissionLauncher = rememberLauncherForActivityResult(
            contract = RequestPermission(),
            onResult = { isGranted ->
                isPermissionGranted = isGranted
            }
        )
        LaunchedEffect(Unit) {
            permissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
        }

        if (isPermissionGranted) {
            SoundRecorderUI()
        } else {
            Text("Please grant the required permissions to use this app.")
        }
    }

    @Composable
    fun SoundRecorderUI() {
        var isRecording by remember { mutableStateOf(false) }
        var birdInfo by remember { mutableStateOf(Pair("", "")) }

        // Create a gradient background
        val gradientColors = listOf(Color(0xFF4A90E2), Color(0xFF50E3C2))

        // Mapping of bird names to their drawable resources
        val birdImages = mapOf(
            "Song Sparrow" to R.drawable.so_spar,
            "Northern Mockingbird" to R.drawable.no_mock,
            "American Robin" to R.drawable.am_robi,
            "Northern Cardinal" to R.drawable.no_card,
            "Bewick's Wren" to R.drawable.be_wren
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(brush = Brush.verticalGradient(gradientColors))
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = if (isRecording) "Recording..." else "Press to Record",
                fontSize = 24.sp,
                color = Color.White,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(20.dp))

            // Display bird information only when recording is stopped
            if (!isRecording && birdInfo.first.isNotEmpty()) {
                Card(
                    modifier = Modifier
                        .padding(16.dp)
                        .fillMaxWidth()
                        .padding(16.dp),
                    shape = RoundedCornerShape(12.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF3D3D3D)) // Set background color here
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        // Display bird image
                        birdImages[birdInfo.first]?.let { imageResId ->
                            Image(
                                painter = painterResource(id = imageResId),
                                contentDescription = birdInfo.first,
                                modifier = Modifier
                                    .size(100.dp)
                                    .padding(bottom = 8.dp) // Add some spacing below the image
                            )
                        }

                        // Display bird name and decibel range
                        Text(
                            text = "Bird: ${birdInfo.first}",
                            fontSize = 20.sp,
                            color = Color(0xFFEAB8A1), // Light peach color
                            textAlign = TextAlign.Center
                        )
                        Text(
                            text = "Decibel Range: ${birdInfo.second}",
                            fontSize = 20.sp,
                            color = Color(0xFFEAB8A1), // Light peach color
                            textAlign = TextAlign.Center
                        )
                    }
                }
                Spacer(modifier = Modifier.height(10.dp))
            }

            Button(
                onClick = {
                    if (isRecording) {
                        stopRecording().also {
                            // Update bird info when stopping the recording
                            birdInfo = birds[currentBirdIndex]
                            // Switch to the next bird for the next recording
                            currentBirdIndex = (currentBirdIndex + 1) % birds.size
                        }
                    } else {
                        startRecording()
                        birdInfo = Pair("", "") // Clear bird info when starting a new recording
                    }
                    isRecording = !isRecording
                },
                modifier = Modifier
                    .padding(16.dp)
                    .fillMaxWidth()
                    .height(56.dp)
                    .background(Color(0xFF4CAF50), RoundedCornerShape(12.dp)), // Green color
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4CAF50)) // Use containerColor here
            ) {
                Text(
                    text = if (isRecording) "Stop Recording" else "Start Recording",
                    color = Color.White,
                    fontSize = 18.sp
                )
            }
        }
    }



    private fun startRecording() {
        outputFile = File.createTempFile("temp_audio", ".3gp", cacheDir)

        mediaRecorder = MediaRecorder().apply {
            setAudioSource(MediaRecorder.AudioSource.MIC)
            setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP)
            setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB)
            setOutputFile(outputFile?.absolutePath)

            try {
                prepare()
                start()
                Toast.makeText(applicationContext, "Recording started", Toast.LENGTH_SHORT).show()
            } catch (e: IOException) {
                Log.e("SoundRecorder", "startRecording: ", e)
            }
        }
    }

    private fun stopRecording() {
        mediaRecorder?.apply {
            stop()
            release()
            Toast.makeText(applicationContext, "Recording stopped", Toast.LENGTH_SHORT).show()
        }
        mediaRecorder = null
    }
}
