package com.example.textextraction

import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.core.view.WindowCompat

class MainActivity : AppCompatActivity() {

    // UI elements
    private lateinit var imageView: ImageView
    private lateinit var textView: TextView

    // Request code for image picker
    private val PICK_IMAGE = 100

    // To store selected image URI
    private var imageUri: Uri? = null

    // To store recognized text
    private var extractedText: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Bind UI elements
        imageView = findViewById(R.id.imageView)
        textView = findViewById(R.id.textView)
        val copyTextBtn = findViewById<Button>(R.id.copyTextBtn)
        val pickImageBtn: Button = findViewById(R.id.pickImageBtn)

        // Extend layout to draw behind status bar (for modern UI look)
        WindowCompat.setDecorFitsSystemWindows(window, false)

        // Button: Pick image from gallery
        pickImageBtn.setOnClickListener {
            val intent = Intent(Intent.ACTION_PICK)
            intent.type = "image/*" // Only images
            startActivityForResult(intent, PICK_IMAGE)
        }

        // Button: Copy recognized text to clipboard
        copyTextBtn.setOnClickListener {
            val detectedText = textView.text.toString()

            // Ensure there is actual recognized text before copying
            if (detectedText.isNotBlank() && detectedText != "Extracted text will appear here") {
                val clipboard = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                val clip = ClipData.newPlainText("Extracted Text", detectedText)
                clipboard.setPrimaryClip(clip)
                Toast.makeText(this, "Text copied to clipboard", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this, "No text to copy", Toast.LENGTH_SHORT).show()
            }
        }

        // Restore saved instance state (after rotation or background/foreground change)
        if (savedInstanceState != null) {
            val uriString = savedInstanceState.getString("imageUri")
            if (uriString != null) {
                imageUri = Uri.parse(uriString)
                imageView.setImageURI(imageUri)
            }

            extractedText = savedInstanceState.getString("extractedText", "")
            textView.text = extractedText
        }
    }

    // Handle image selection result
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        // Check if image was successfully picked
        if (resultCode == Activity.RESULT_OK && requestCode == PICK_IMAGE) {
            imageUri = data?.data
            if (imageUri != null) {
                // Convert URI to Bitmap depending on Android version
                val bitmap = if (Build.VERSION.SDK_INT < 28) {
                    MediaStore.Images.Media.getBitmap(this.contentResolver, imageUri)
                } else {
                    val source = ImageDecoder.createSource(this.contentResolver, imageUri!!)
                    ImageDecoder.decodeBitmap(source)
                }

                // Display image in ImageView
                imageView.setImageBitmap(bitmap)

                // Run ML Kit text recognition
                runTextRecognition(bitmap)
            }
        }
    }

    // Perform OCR using ML Kit's on-device text recognizer
    private fun runTextRecognition(bitmap: Bitmap) {
        val image = InputImage.fromBitmap(bitmap, 0) // Create ML Kit image
        val recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)

        recognizer.process(image)
            .addOnSuccessListener { visionText ->
                extractedText = visionText.text // Store recognized text
                textView.text = extractedText   // Display it
            }
            .addOnFailureListener { e ->
                textView.text = "Text recognition failed: ${e.message}"
            }
    }

    // Save state for activity recreation (rotation, app minimization)
    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putString("imageUri", imageUri?.toString())
        outState.putString("extractedText", extractedText)
    }

    // Restore state when activity is recreated
    override fun onRestoreInstanceState(savedInstanceState: Bundle) {
        super.onRestoreInstanceState(savedInstanceState)
        val uriString = savedInstanceState.getString("imageUri")
        if (uriString != null) {
            imageUri = Uri.parse(uriString)
            imageView.setImageURI(imageUri)
        }
        extractedText = savedInstanceState.getString("extractedText", "")
        textView.text = extractedText
    }
}
