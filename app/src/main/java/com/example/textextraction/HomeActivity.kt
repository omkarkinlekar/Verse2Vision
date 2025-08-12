package com.example.textextraction

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import android.widget.Button
import android.view.animation.AnimationUtils
import androidx.core.view.WindowCompat
import android.widget.CheckBox
import android.widget.CompoundButton
import android.widget.TextView
import androidx.appcompat.app.AlertDialog


class HomeActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)

        WindowCompat.setDecorFitsSystemWindows(window, false)

        val getStartedButton = findViewById<Button>(R.id.getStartedBtn)
        val termsCheckbox = findViewById<CheckBox>(R.id.termsCheckbox)
        val termsText = findViewById<TextView>(R.id.termsTextView)
        val glowAnim = AnimationUtils.loadAnimation(this, R.anim.glow)

// Initially disable the button
        getStartedButton.isEnabled = false
        termsCheckbox.startAnimation(glowAnim)

// Enable/disable button based on checkbox state
        termsCheckbox.setOnCheckedChangeListener { _: CompoundButton, isChecked: Boolean ->
            getStartedButton.isEnabled = isChecked

            if (isChecked) {
                // Stop checkbox glow, start button glow
                termsCheckbox.clearAnimation()
                getStartedButton.startAnimation(glowAnim)
            } else {
                // Stop button glow when unchecked
                getStartedButton.clearAnimation()
                termsCheckbox.startAnimation(glowAnim)
            }
        }


// On button click
        getStartedButton.setOnClickListener {
            if (termsCheckbox.isChecked) {
                // Proceed to next screen
                termsCheckbox.clearAnimation()
                val intent = Intent(this, MainActivity::class.java)
                startActivity(intent)
            }else{
                getStartedButton.clearAnimation()
            }
        }

// Show terms in a dialog when clicking on checkbox text (not box)
        termsText.setOnClickListener {
            val terms = """
        Terms and Conditions

        • You must agree to use this OCR app responsibly.
        • This app uses OCR technology to extract text from images.
        • Your data is processed temporarily and is not stored.

        Privacy Policy

        • We do not collect personal information.
        • Data is used only during processing and is not saved.
        • Third-party OCR services (like Google ML Kit) may have their own privacy policies.
    """.trimIndent()

            AlertDialog.Builder(this)
                .setTitle("Terms & Privacy")
                .setMessage(terms)
                .setPositiveButton("OK") { dialog, _ -> dialog.dismiss() }
                .show()
        }
    }
}