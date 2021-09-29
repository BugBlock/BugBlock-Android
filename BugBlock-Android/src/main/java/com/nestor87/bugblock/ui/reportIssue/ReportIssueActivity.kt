package com.nestor87.bugblock.ui.reportIssue

import android.annotation.SuppressLint
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.MotionEvent
import android.view.View
import android.widget.*
import androidx.cardview.widget.CardView
import androidx.core.content.ContextCompat
import androidx.core.view.children
import com.nestor87.bugblock.R
import com.nestor87.bugblock.reporter.Reporter
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.io.File

internal class ReportIssueActivity : AppCompatActivity() {

    private val viewModel = ReportIssueViewModel()

    lateinit var sendReportButton: CardView
    lateinit var screenshotImageView: ImageView
    lateinit var exitButton: ImageView
    lateinit var removeScreenshotButton: ImageView
    lateinit var includeScreenshotMessageTextView: TextView
    lateinit var emailEditText: EditText
    lateinit var desriptionEditText: EditText

    companion object {
        var running = false
    }


    @SuppressLint("ClickableViewAccessibility")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_report_issue)

        running = true

        sendReportButton = findViewById(R.id.sendReportButton)
        screenshotImageView = findViewById(R.id.screenshotImage)
        exitButton = findViewById(R.id.closeButton)
        removeScreenshotButton = findViewById(R.id.removeScreenshotButton)
        includeScreenshotMessageTextView = findViewById(R.id.includeScreenshotMessage)
        emailEditText = findViewById(R.id.emailEditText)
        desriptionEditText = findViewById(R.id.descriptionEditText)

        if (intent.getBooleanExtra("containsScreenshot", false)) {
            val screenshotBitmap = viewModel.loadBitmap(this, "screenshot")!!
            screenshotImageView.setImageBitmap(screenshotBitmap)
            window.decorView.post {
                screenshotImageView.layoutParams.width =
                    (screenshotImageView.height.toFloat() / screenshotBitmap.height * screenshotBitmap.width).toInt()
                screenshotImageView.requestLayout()
            }
        } else {
            removeImageFromReport()
        }

        removeScreenshotButton.setOnClickListener {
            removeImageFromReport()
        }

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
            if (viewModel.checkFieldsNotEmpty(this, emailEditText, desriptionEditText, screenshotImageView.visibility == View.VISIBLE)) {
                changeProgressBarVisibility(true)
                GlobalScope.launch(Dispatchers.Main) {
                    val success = Reporter.reportIssue(
                        emailEditText.text.toString(),
                        desriptionEditText.text.toString(),
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

    private fun removeImageFromReport() {
        screenshotImageView.visibility = View.GONE
        removeScreenshotButton.visibility = View.GONE
        includeScreenshotMessageTextView.visibility = View.GONE
    }

    private fun changeProgressBarVisibility(visible: Boolean) {
        findViewById<ProgressBar>(R.id.progressBar).visibility = if (visible) View.VISIBLE else View.GONE
    }

    override fun onDestroy() {
        running = false
        super.onDestroy()
    }
}