package com.example.nnapi_ass3_q2

import android.content.Context
import android.util.AttributeSet
import androidx.appcompat.widget.AppCompatImageView

class HalfHeightImageView : AppCompatImageView {
    constructor(context: Context) : super(context)
    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs)
    constructor(context: Context, attrs: AttributeSet?, defStyle: Int) : super(context, attrs, defStyle)

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val width = MeasureSpec.getSize(widthMeasureSpec)
        val halfScreenHeight = context.resources.displayMetrics.heightPixels / 2
        val height = MeasureSpec.makeMeasureSpec(halfScreenHeight, MeasureSpec.EXACTLY)
        super.onMeasure(widthMeasureSpec, height)


    }
}