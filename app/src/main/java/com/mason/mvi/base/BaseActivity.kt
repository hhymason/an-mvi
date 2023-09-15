package com.mason.mvi.base

import androidx.viewbinding.ViewBinding
import com.mason.mvi.ILoadingDialog
import com.mason.mvi.IToast
import com.mason.mvi.MviActivity
import com.mason.mvi.MviViewModel

abstract class BaseActivity<VM : MviViewModel, VB : ViewBinding> : MviActivity<VM, VB>() {
    override val toast: IToast by lazy { Toast() }

    override val loadingDialog: ILoadingDialog by lazy { LoadingDialog(this) }
}