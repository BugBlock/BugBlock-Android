package com.bugblock.ui.reportIssue

import android.annotation.SuppressLint
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.MotionEvent
import android.view.View
import android.widget.*
import androidx.cardview.widget.CardView
import androidx.core.content.ContextCompat
import androidx.core.view.children
import com.bugblock.R
import com.bugblock.data.BBSharedPreferences
import com.bugblock.reporter.Reporter
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.io.File

internal class ReportIssueActivity : AppCompatActivity() {

    private val viewModel = ReportIssueViewModel()
    private lateinit var bbSharedPreferences: BBSharedPreferences

    lateinit var sendReportButton: CardView
    lateinit var screenshotImageView: ImageView
    lateinit var exitButton: ImageView
    lateinit var removeScreenshotButton: ImageView
    lateinit var includeScreenshotMessageTextView: TextView
    lateinit var emailEditText: EditText
    lateinit var descriptionEditText: EditText

    companion object {
        var running = false
    }


    @SuppressLint("ClickableViewAccessibility")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_report_issue)

        supportActionBar?.hide()

        running = true
        bbSharedPreferences = BBSharedPreferences(this)

        sendReportButton = findViewById(R.id.sendReportButton)
        screenshotImageView = findViewById(R.id.screenshotImage)
        exitButton = findViewById(R.id.closeButton)
        includeScreenshotMessageTextView = findViewById(R.id.includeScreenshotMessage)
        emailEditText = findViewById(R.id.emailEditText)
        descriptionEditText = findViewById(R.id.descriptionEditText)

        val screenshotBitmap = viewModel.loadBitmap(this, "screenshot")!!
        screenshotImageView.setImageBitmap(screenshotBitmap)
        window.decorView.post {
            screenshotImageView.layoutParams.height =
                viewModel.calculateScreenshotImageHeight(30F, descriptionEditText, sendReportButton).toInt()
            screenshotImageView.layoutParams.width =
                (screenshotImageView.height.toFloat() / screenshotBitmap.height * screenshotBitmap.width).toInt()
            screenshotImageView.requestLayout()
        }
        emailEditText.setText(bbSharedPreferences.userEmail)

        exitButton.setOnClickListener {
            finish()
        }

        sendReportButton.setOnTouchListener { it, event ->
            val buttonCardView = (it as CardView)
            val buttonTextView = (buttonCardView.children.iterator().next() as TextView)

            when (event.action) {

                MotionEvent.ACTION_DOWN -> {
                    buttonCardView.setCardBackgroundColor(ContextCompat.getColor(this, R.color.icon_blue_dark))
                    buttonTextView.setTextColor(ContextCompat.getColor(this, R.color.light_gray))
                }

                MotionEvent.ACTION_UP -> {
                    buttonCardView.setCardBackgroundColor(ContextCompat.getColor(this, R.color.icon_blue))
                    buttonTextView.setTextColor(ContextCompat.getColor(this, R.color.white))
                    buttonCardView.performClick()
                }

                MotionEvent.ACTION_CANCEL -> {
                    buttonCardView.setCardBackgroundColor(ContextCompat.getColor(this, R.color.icon_blue))
                    buttonTextView.setTextColor(ContextCompat.getColor(this, R.color.white))
                }
            }
            true
        }

        sendReportButton.setOnClickListener {
            if (viewModel.checkFieldsNotEmpty(this, emailEditText, descriptionEditText, screenshotImageView.visibility == View.VISIBLE)) {
                changeProgressBarVisibility(true)
                bbSharedPreferences.userEmail = emailEditText.text.toString()
                GlobalScope.launch(Dispatchers.Main) {
                    val success = Reporter.reportIssue(
                        emailEditText.text.toString(),
                        descriptionEditText.text.toString(),
                        if (screenshotImageView.visibility == View.VISIBLE)
                            File(filesDir, "screenshot.png")
                        else
                            null
                    )
                    changeProgressBarVisibility(false)
                    if (success) {
                        Toast.makeText(this@ReportIssueActivity, getString(R.string.report_successful), Toast.LENGTH_LONG).show()
                        finishAffinity()
                    } else {
                        Toast.makeText(this@ReportIssueActivity, getString(R.string.report_unsuccessful), Toast.LENGTH_LONG).show()
                    }
                }
            }
        }
    }

    private fun changeProgressBarVisibility(visible: Boolean) {
        findViewById<ProgressBar>(R.id.progressBar).visibility = if (visible) View.VISIBLE else View.GONE
    }

    override fun onDestroy() {
        running = false
        super.onDestroy()
    }
}