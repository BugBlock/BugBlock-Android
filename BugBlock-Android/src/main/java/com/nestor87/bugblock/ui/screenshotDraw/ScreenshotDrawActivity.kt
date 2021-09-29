package com.nestor87.bugblock.ui.screenshotDraw

import android.Manifest
import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.ImageDecoder
import android.graphics.PorterDuff
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.content.res.AppCompatResources
import androidx.cardview.widget.CardView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.DrawableCompat
import androidx.core.view.children
import com.karumi.dexter.Dexter
import com.karumi.dexter.PermissionToken
import com.karumi.dexter.listener.PermissionDeniedResponse
import com.karumi.dexter.listener.PermissionGrantedResponse
import com.karumi.dexter.listener.PermissionRequest
import com.karumi.dexter.listener.single.PermissionListener
import com.nestor87.bugblock.R
import com.nestor87.bugblock.view.CanvasView
import com.nestor87.bugblock.ui.reportIssue.ReportIssueActivity
import kotlin.math.roundToInt


internal class ScreenshotDrawActivity : AppCompatActivity(), PermissionListener {

    private val viewModel = ScreenshotDrawViewModel()

    private lateinit var closeButton: ImageView
    private lateinit var screenshotCanvasView: CanvasView
    private lateinit var brushButton: ImageView
    private lateinit var undoButton: ImageView
    private lateinit var redoButton: ImageView
    private lateinit var addDescriptionButton: CardView
    private lateinit var colorSelectButtonsLinearLayout: LinearLayout

    private var isColorSelectButtonsLinearLayoutVisible = false

    private lateinit var screenshotBorderView: View

    private val availableDrawColors = listOf(
        R.color.red,
        R.color.green,
        R.color.blue,
        R.color.magenta,
        R.color.orange,
        R.color.yellow,
        R.color.black,
    )

    @SuppressLint("ClickableViewAccessibility")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_screenshot_draw)

        closeButton = findViewById(R.id.closeButton)
        screenshotCanvasView = findViewById(R.id.screenshot)
        brushButton = findViewById(R.id.brushButton)
        undoButton = findViewById(R.id.undoButton)
        redoButton = findViewById(R.id.redoButton)
        addDescriptionButton = findViewById(R.id.addDescriptionButton)
        colorSelectButtonsLinearLayout = findViewById(R.id.colorSelectButtonsLinearLayout)

        requestReadExternalStoragePermission()

        closeButton.setOnClickListener {
            finish()
        }

        undoButton.setOnClickListener {
            screenshotCanvasView.undo()
        }

        redoButton.setOnClickListener {
            screenshotCanvasView.redo()
        }

        brushButton.setOnClickListener {
            val topButtonsVisibility =
                if (!isColorSelectButtonsLinearLayoutVisible) {
                    View.GONE
                } else {
                    View.VISIBLE
                }
            val colorRadioGroupVisibility =
                if (!isColorSelectButtonsLinearLayoutVisible) {
                    View.VISIBLE
                } else {
                    View.GONE
                }

            undoButton.visibility = topButtonsVisibility
            redoButton.visibility = topButtonsVisibility
            brushButton.visibility = topButtonsVisibility

            colorSelectButtonsLinearLayout.visibility = colorRadioGroupVisibility

            isColorSelectButtonsLinearLayoutVisible = !isColorSelectButtonsLinearLayoutVisible
        }

        undoButton.isEnabled = false
        redoButton.isEnabled = false

        screenshotCanvasView.isUndoPossible.observe(this, {
            undoButton.isEnabled = it
        })

        screenshotCanvasView.isRedoPossible.observe(this, {
            redoButton.isEnabled = it
        })

        brushButton.setColorFilter(ContextCompat.getColor(this, R.color.red), PorterDuff.Mode.SRC_IN)

        var isInitialColorSelectClick = false
        colorSelectButtonsLinearLayout.children.forEachIndexed { i, it ->
            val buttonColor = ContextCompat.getColor(this, availableDrawColors[i])
            val colorButtonDrawable = DrawableCompat.wrap(AppCompatResources.getDrawable(this, R.drawable.color_button)!!)
            colorButtonDrawable.setTint(buttonColor)

            (it as ImageView).setImageDrawable(colorButtonDrawable)


            it.setOnClickListener {
                colorSelectButtonsLinearLayout.children.forEachIndexed { i, it ->
                    val buttonColor = ContextCompat.getColor(this, availableDrawColors[i])
                    val colorButtonDrawable = DrawableCompat.wrap(AppCompatResources.getDrawable(this, R.drawable.color_button)!!)
                    colorButtonDrawable.setTint(buttonColor)

                    (it as ImageView).setImageDrawable(colorButtonDrawable)
                }
                (it as ImageView).setImageDrawable(
                    viewModel.combineTwoDrawables(
                        this,
                        colorButtonDrawable,
                        ContextCompat.getDrawable(this, R.drawable.ic_selected_mark)!!,
                        secondDrawableWidthInDp = 20F,
                        secondDrawableHeightInDp = 20F
                    )
                )

                screenshotCanvasView.changePaintColor(buttonColor)
                brushButton.setColorFilter(buttonColor, PorterDuff.Mode.SRC_IN)

                if (!isInitialColorSelectClick) {
                    brushButton.performClick()
                } else {
                    isInitialColorSelectClick = false
                }
            }

            if (i == 0) {
                isInitialColorSelectClick = true
                it.performClick()
            }
        }

        addDescriptionButton.setOnTouchListener { it, event ->
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

        addDescriptionButton.setOnClickListener {
            screenshotCanvasView.capture(window) { resultBitmap ->
                viewModel.saveBitmap(this, viewModel.addRoundedCornersToBitmap(this, resultBitmap, 10F), "screenshot")
                val intent = Intent(this, ReportIssueActivity::class.java)
                intent.putExtra("containsScreenshot", true)
                startActivity(intent)
            }
        }
    }

    private fun requestReadExternalStoragePermission() {
        Dexter.withContext(this)
            .withPermission(Manifest.permission.READ_EXTERNAL_STORAGE)
            .withListener(this)
            .check()
    }

    override fun onPermissionGranted(p0: PermissionGrantedResponse?) {
        val screenshotUri = Uri.parse(intent.getStringExtra("screenshotUri"));
        var screenshotBitmap =
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.P) {
                MediaStore.Images.Media.getBitmap(this.contentResolver, screenshotUri)
            } else {
                val source: ImageDecoder.Source = ImageDecoder.createSource(this.contentResolver, screenshotUri)
                ImageDecoder.decodeBitmap(source)
            }


        window.decorView.post {
            val marginVertical = 40f
            var scale = viewModel.calculateScreenshotImageHeight(marginVertical, brushButton, addDescriptionButton) / screenshotBitmap.height
            screenshotCanvasView.layoutParams.height = (screenshotBitmap.height * scale).roundToInt()
            screenshotCanvasView.layoutParams.width = (screenshotBitmap.width * scale).roundToInt()

            screenshotCanvasView.y = viewModel.calculateViewRectOnScreen(brushButton).bottom + marginVertical

            screenshotCanvasView.requestLayout()

            screenshotBitmap = viewModel.getResizedBitmap(screenshotBitmap, screenshotCanvasView.layoutParams.width, screenshotCanvasView.layoutParams.height)

            screenshotCanvasView.drawBitmap(screenshotBitmap)


            screenshotBorderView = View(this)
            screenshotBorderView.setBackgroundResource(R.drawable.screenshot_canvasview_border)
            screenshotBorderView.layoutParams = ViewGroup.LayoutParams(screenshotCanvasView.layoutParams)
            screenshotBorderView.y = screenshotCanvasView.y - viewModel.convertDpToPx(this,1F).roundToInt()
            screenshotBorderView.x = (windowManager.currentWindowMetrics.bounds.width().toFloat() - screenshotCanvasView.layoutParams.width) / 2 - viewModel.convertDpToPx(this,1F).roundToInt()
            screenshotBorderView.layoutParams.width += viewModel.convertDpToPx(this, 2F).roundToInt()
            screenshotBorderView.layoutParams.height += viewModel.convertDpToPx(this, 2F).roundToInt()
            (screenshotCanvasView.parent as ConstraintLayout).addView(screenshotBorderView, 0)
            screenshotBorderView.requestLayout()
        }
    }

    override fun onPermissionDenied(p0: PermissionDeniedResponse?) {
        requestReadExternalStoragePermission()
    }

    override fun onPermissionRationaleShouldBeShown(p0: PermissionRequest?, token: PermissionToken?) {
        token!!.continuePermissionRequest()
    }

}