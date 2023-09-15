package com.mason.mvi.base

import android.app.Dialog
import android.content.Context
import android.widget.TextView
import com.mason.mvi.ILoadingDialog
import com.mason.mvi.sample.R

class LoadingDialog(context: Context) : Dialog(context, R.style.LoadingDialog), ILoadingDialog {
    var tvLoadingMsg: TextView

    init {
        setContentView(R.layout.dialog_loading)
        setCanceledOnTouchOutside(false)
        tvLoadingMsg = findViewById(R.id.tv_loading_msg)
    }

    override fun show(msg: String) {
        tvLoadingMsg.text = msg
        super.show()
    }
}
