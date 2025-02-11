package com.example.nnapi_ass3_q2

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import android.provider.MediaStore
import android.util.DisplayMetrics
import android.util.Log
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import org.tensorflow.lite.Interpreter
import org.tensorflow.lite.nnapi.NnApiDelegate
import java.io.IOException
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.io.FileInputStream
import java.nio.MappedByteBuffer
import java.nio.channels.FileChannel

class MainActivity : AppCompatActivity() {

    private lateinit var imageView: ImageView
    private lateinit var selectImageButton: Button
    private lateinit var openCameraButton: Button
    private lateinit var tflite: Interpreter

    companion object {
        private const val PICK_IMAGE_REQUEST = 1
        private const val REQUEST_IMAGE_CAPTURE = 2
        private const val INPUT_WIDTH = 224
        private const val INPUT_HEIGHT = 224
        private const val NUM_CLASSES = 1000  // Assuming ImageNet classes for MobileNetV2
        private const val PERMISSION_REQUEST_CODE = 100
    }

    private fun loadLabelsFromFile(): MutableList<String> {
        val labels = mutableListOf<String>()
        try {
            val inputStream = assets.open("imagenet_labels_4.txt")
            val reader = inputStream.bufferedReader()
            reader.useLines { lines ->
                lines.forEach { line ->
                    labels.add(line.trim())
                }
            }
        } catch (e: IOException) {
            e.printStackTrace()
        }
        return labels
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Check and request necessary permissions
        checkAndRequestPermissions()

        // Initialize views
        imageView = findViewById(R.id.imageView)
        selectImageButton = findViewById(R.id.selectImageButton)
        openCameraButton = findViewById(R.id.openCameraButton)

        // Get the height of the screen
        val displayMetrics = DisplayMetrics()
        windowManager.defaultDisplay.getMetrics(displayMetrics)
        val screenHeight = displayMetrics.heightPixels

        // Calculate the height for the ImageView to occupy half of the screen
        val halfScreenHeight = screenHeight / 2

        // Set the height of the ImageView
        imageView.layoutParams.height = halfScreenHeight


        // Load the TensorFlow Lite model with NNAPI delegate
        try {
            val delegate = NnApiDelegate()
            val options = Interpreter.Options().addDelegate(delegate)
            tflite = Interpreter(loadModelFile(), options)
        } catch (e: IOException) {
            e.printStackTrace()
            showToast("Error loading TensorFlow Lite model")
        }

        // Set click listener for the button to select image
        selectImageButton.setOnClickListener {
            launchImagePicker()
        }

        // Set click listener for the button to open camera
        openCameraButton.setOnClickListener {
            dispatchTakePictureIntent()
        }
    }


    private fun checkAndRequestPermissions() {
        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.READ_EXTERNAL_STORAGE
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            // Permission is not granted, request the permission
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE),
                PERMISSION_REQUEST_CODE
            )
        } else {
            // Permission is already granted, proceed with your operations
            // For example, launch image picker
            launchImagePicker()
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == PERMISSION_REQUEST_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permission granted, proceed with your operations
                // For example, launch image picker
                launchImagePicker()
            } else {
                // Permission denied
                showToast("Permission denied")
            }
        }
    }

    private fun loadModelFile(): MappedByteBuffer {
        val fileDescriptor = assets.openFd("model.tflite")
        val inputStream = FileInputStream(fileDescriptor.fileDescriptor)
        val fileChannel = inputStream.channel
        val startOffset = fileDescriptor.startOffset
        val declaredLength = fileDescriptor.declaredLength
        return fileChannel.map(FileChannel.MapMode.READ_ONLY, startOffset, declaredLength)
    }

    private fun launchImagePicker() {
        val intent = Intent(Intent.ACTION_PICK)
        intent.type = "image/*"
        startActivityForResult(intent, PICK_IMAGE_REQUEST)
    }

    private fun dispatchTakePictureIntent() {
        Intent(MediaStore.ACTION_IMAGE_CAPTURE).also { takePictureIntent ->
            takePictureIntent.resolveActivity(packageManager)?.also {
                startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE)
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == Activity.RESULT_OK && data != null) {
            try {
                processImageFromUri(data.data)
            } catch (e: IOException) {
                e.printStackTrace()
            }
        } else if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == Activity.RESULT_OK && data != null) {
            val imageBitmap = data.extras?.get("data") as Bitmap
            val resizedBitmap = Bitmap.createScaledBitmap(imageBitmap, INPUT_WIDTH, INPUT_HEIGHT, true)
            imageView.setImageBitmap(resizedBitmap)
            try {
                processImage(resizedBitmap)
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }
    }

    private fun processImageFromUri(imageUri: android.net.Uri?) {
        val inputStream = contentResolver.openInputStream(imageUri!!)
        val bitmap = BitmapFactory.decodeStream(inputStream)
        imageView.setImageBitmap(bitmap)
        processImage(bitmap)
    }

    private fun processImage(bitmap: Bitmap) {
        // Preprocess the image and run inference
        val inputBuffer = preprocessImage(bitmap)
        val outputScores = Array(1) { FloatArray(NUM_CLASSES) }
        tflite.run(inputBuffer, outputScores)

        // Process the output to get classification result
        val topResult = getTopResult(outputScores[0])
        val imageLabelTextView = findViewById<TextView>(R.id.imageLabelTextView)
        val predictionTextView = findViewById<TextView>(R.id.predictionTextView)
        val result = getImageLabel(topResult.label)
        val labelName = result.split(": ")[1] // Extract the label name after splitting by ": "
        val resultString = "Image: ${labelName}, Prediction: ${topResult.confidence}"
        //imageLabelTextView.text = resultString
        predictionTextView.text = resultString

        //showToast("Prediction: ${topResult.label}, Confidence: ${topResult.confidence}")
    }

    private fun getImageLabel(classIndex: Int): String {
        // Define your list of class labels here
        val classLabels = loadLabelsFromFile() // Assuming this function loads labels correctly

        // Check if the classIndex is within the valid range of classLabels
        Log.d("Tag_2", "Value: $classLabels[classIndex]")

        return if (classIndex >= 0 && classIndex < classLabels.size) {
            classLabels[classIndex]
        } else {
            "Unknown"
        }
    }

    private fun preprocessImage(bitmap: Bitmap): ByteBuffer {
        val resizedBitmap = Bitmap.createScaledBitmap(bitmap, INPUT_WIDTH, INPUT_HEIGHT, true)
        val inputBuffer = ByteBuffer.allocateDirect(INPUT_WIDTH * INPUT_HEIGHT * 3 * 4)
        inputBuffer.order(ByteOrder.nativeOrder())

        // Normalize pixel values and convert to ByteBuffer
        for (y in 0 until INPUT_HEIGHT) {
            for (x in 0 until INPUT_WIDTH) {
                val pixelValue = resizedBitmap.getPixel(x, y)
                inputBuffer.putFloat(((pixelValue shr 16 and 0xFF) - 127.5f) / 127.5f)
                inputBuffer.putFloat(((pixelValue shr 8 and 0xFF) - 127.5f) / 127.5f)
                inputBuffer.putFloat(((pixelValue and 0xFF) - 127.5f) / 127.5f)
            }
        }

        inputBuffer.rewind()
        return inputBuffer
    }

    private fun getTopResult(outputScores: FloatArray): ClassificationResult {
        var maxIndex = 0
        var maxScore = outputScores[0]
        for (i in 1 until NUM_CLASSES) {
            if (outputScores[i] > maxScore) {
                maxIndex = i
                maxScore = outputScores[i]
            }
        }
        return ClassificationResult(maxIndex, maxScore)
    }

    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }

    data class ClassificationResult(val label: Int, val confidence:Float)
}