package com.saveetha.flownotify

import android.content.Context
import android.util.AttributeSet
import androidx.appcompat.widget.AppCompatRatingBar
import com.saveetha.flownotify.R

/**
 * Custom RatingBar that allows setting a rating change listener
 */
class RatingBar @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = android.R.attr.ratingBarStyle
) : AppCompatRatingBar(context, attrs, defStyleAttr) {

    private var onRatingChangeListener: ((RatingBar, Float) -> Unit)? = null

    init {
        // Set up the internal rating change listener
        super.setOnRatingBarChangeListener { _, rating, fromUser ->
            if (fromUser) {
                onRatingChangeListener?.invoke(this, rating)
            }
        }
    }

    /**
     * Sets a listener to be called when the rating changes.
     */
    fun setOnRatingChangeListener(listener: (RatingBar, Float) -> Unit) {
        onRatingChangeListener = listener
    }

    /**
     * Overridden to prevent external code from bypassing our wrapper
     */
    override fun setOnRatingBarChangeListener(listener: OnRatingBarChangeListener?) {
        // Do nothing, use setOnRatingChangeListener instead
    }
}