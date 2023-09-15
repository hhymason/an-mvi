package com.mason.mvi.base

import android.app.Dialog
import android.content.Context
import androidx.viewbinding.ViewBinding
import com.mason.mvi.ViewBindingUtil

abstract class BaseDialog<VB : ViewBinding>(context: Context, style: Int = 0) :
    Dialog(context, style) {
    private val _binding: VB
    val binding: VB
        get() = _binding

    init {
        _binding = ViewBindingUtil.inflateWithGeneric(this, layoutInflater)
        setContentView(_binding.root)
    }

//    private lateinit var _binding: VB
//    val binding: VB
//        get() = _binding
//
//    override fun onCreate(savedInstanceState: Bundle?) {
//        super.onCreate(savedInstanceState)
//        _binding = ViewBindingUtil.inflateWithGeneric(this, layoutInflater)
//        setContentView(_binding.root)
//    }
}