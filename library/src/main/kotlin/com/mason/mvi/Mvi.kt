package com.mason.mvi

import android.util.Log
import androidx.annotation.DrawableRes

const val TAG = "mvi"
const val THROTTLE_FIRST_WINDOW_DURATION = 1000L
const val DEBOUNCE_WINDOW_DURATION = 300L

object Mvi {
    var dismissLoadingWhenPause = true
    var log: IMviLog = generateDefaultLog()
}

internal fun generateDefaultLog(): IMviLog {
    return object : IMviLog {
        override fun d(message: String, vararg args: Any?) {
            Log.d(TAG, message.format(*args))
        }

        override fun i(message: String, vararg args: Any?) {
            Log.i(TAG, message.format(*args))
        }

        override fun w(message: String, vararg args: Any?) {
            Log.w(TAG, message.format(*args))
        }

        override fun e(message: String, vararg args: Any?) {
            Log.e(TAG, message.format(*args))
        }
    }
}

interface IMviLog {
    fun d(message: String, vararg args: Any?)
    fun i(message: String, vararg args: Any?)
    fun w(message: String, vararg args: Any?)
    fun e(message: String, vararg args: Any?)
}

interface IToast {
    fun toast(msg: String)
    fun toastWithImg(msg: String, @DrawableRes imageResId: Int)
    fun clear()
}

interface ILoadingDialog {
    fun show(msg: String)
    fun dismiss()
}
