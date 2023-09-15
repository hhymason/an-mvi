package com.mason.mvi.base

import android.view.Gravity
import android.widget.Toast
import androidx.annotation.DrawableRes
import com.mason.mvi.IToast
import com.mason.mvi.Mvi.log
import com.mason.mvi.sample.databinding.ToastBinding
import com.mason.mvi.sample.databinding.ToastWithImgBinding
import com.mason.util.appctx.appCtx
import com.mason.util.os.layoutInflater
import java.lang.ref.SoftReference

class Toast : IToast {
    private var currentToast: SoftReference<Toast>? = null

    override fun clear() {
        currentToast?.clear()
    }

    override fun toast(msg: String) {
        currentToast?.get()?.cancel()
        val binding = ToastBinding.inflate(appCtx.layoutInflater)
        binding.tvMsg.text = msg
        log.i("[UI] [显示 toast] $msg")
        Toast(appCtx.applicationContext).apply {
            view = binding.root
            duration = Toast.LENGTH_SHORT
            setGravity(Gravity.CENTER or Gravity.FILL_HORIZONTAL, 0, 0)
            show()
            currentToast = SoftReference(this)
        }
    }

    override fun toastWithImg(msg: String, @DrawableRes imageResId: Int) {
        currentToast?.get()?.cancel()
        val binding = ToastWithImgBinding.inflate(appCtx.layoutInflater)
        binding.tvMsg.text = msg
        binding.imgMsg.setImageResource(imageResId)
        log.i("[UI] [显示 toast] $msg")
        Toast(appCtx.applicationContext).apply {
            view = binding.root
            duration = Toast.LENGTH_SHORT
            setGravity(Gravity.CENTER or Gravity.FILL_HORIZONTAL, 0, 0)
            show()
            currentToast = SoftReference(this)
        }
    }
}
