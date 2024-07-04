package com.tomsky.hitv.ui

import android.content.Context
import android.util.AttributeSet
import android.view.GestureDetector
import android.view.MotionEvent
import androidx.media3.ui.PlayerView
import kotlin.math.abs

class TVPlayView(context: Context, attrs: AttributeSet?): PlayerView(context, attrs) {

    private lateinit var gestureDetector: GestureDetector

    private var gestureListener: GestureListener = GestureListener()

    init {
        gestureDetector = GestureDetector(context, gestureListener)
    }

    fun setSwipeListener(swipeListener: TVSwipeListener) {
        gestureListener.setSwipeListener(swipeListener)
    }

    override fun onTouchEvent(event: MotionEvent?): Boolean {
        return gestureDetector.onTouchEvent(event!!) || super.onTouchEvent(event)
    }

    private class GestureListener: GestureDetector.SimpleOnGestureListener() {
        private val SWIPE_THRESHOLD: Int = 100
        private val SWIPE_VELOCITY_THRESHOLD: Int = 100

        private var swipeListener: TVSwipeListener? = null

        fun setSwipeListener(listener: TVSwipeListener) {
            swipeListener = listener
        }

        override fun onSingleTapConfirmed(e: MotionEvent): Boolean {
            swipeListener?.onClick()
            return true
        }

        override fun onDown(e: MotionEvent): Boolean {
            return true
        }

        override fun onFling(
            e1: MotionEvent?,
            e2: MotionEvent,
            velocityX: Float,
            velocityY: Float
        ): Boolean {
            val diffX = e2.x - e1!!.x
            val diffY = e2.y - e1!!.y
            if (abs(diffX.toDouble()) > abs(diffY.toDouble())) {
                if (abs(diffX.toDouble()) > SWIPE_THRESHOLD && Math.abs(velocityX) > SWIPE_VELOCITY_THRESHOLD) {
                    if (diffX < 0) {
                        swipeListener?.onSwipeLeft()
                    } else {
                        swipeListener?.onSwipeRight()
                    }
                    return true
                }
            }
            return false
        }
    }
}

interface TVSwipeListener {
    fun onSwipeLeft()
    fun onSwipeRight()
    fun onClick()
}