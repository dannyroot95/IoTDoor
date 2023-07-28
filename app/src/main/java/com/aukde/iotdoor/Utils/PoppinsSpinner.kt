package com.aukde.iotdoor.Utils

import android.content.Context
import android.graphics.Typeface
import android.util.AttributeSet
import android.widget.TextView

import androidx.appcompat.widget.AppCompatSpinner

class PoppinsSpinner (context: Context, attrs: AttributeSet) : AppCompatSpinner(context, attrs) {

    init {
        applyFont()
    }

    private fun applyFont() {
        val spinnerTextViewId = android.R.id.text1
        val titleTextView = findViewById<TextView>(spinnerTextViewId)

        titleTextView?.let {
            val typeface: Typeface = Typeface.createFromAsset(context.assets, "Poppins-Light.ttf")
            it.typeface = typeface
        }
    }

}