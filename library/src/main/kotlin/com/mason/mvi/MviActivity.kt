package com.mason.mvi

import android.app.Activity
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.LifecycleOwner
import androidx.viewbinding.ViewBinding
import com.mason.mvi.Mvi.log

abstract class MviActivity<VM : MviViewModel, VB : ViewBinding> : AppCompatActivity(),
    IMviActivity<VM> {
    override lateinit var lifecycleOwner: LifecycleOwner
    private lateinit var _binding: VB
    val binding: VB
        get() = _binding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        lifecycleOwner = this
        lifecycle.addObserver(ActivityObserver(this))
        _binding = ViewBindingUtil.inflateWithGeneric(this, layoutInflater)
        setContentView(_binding.root)
    }
}

internal class ActivityObserver(val activity: Activity) : LifecycleEventObserver {
    override fun onStateChanged(source: LifecycleOwner, event: Lifecycle.Event) {
        val className = source.javaClass.simpleName
        when (event) {
            Lifecycle.Event.ON_CREATE -> {
                log.i("[UI] [onCreate] $className")
                if (activity is IMviActivity<*>) {
                    activity.runInOnCreate()
                }
            }

            Lifecycle.Event.ON_RESUME -> {
                log.i("[UI] [进入] $className")
            }

            Lifecycle.Event.ON_PAUSE -> {
                log.i("[UI] [离开] $className")
            }

            Lifecycle.Event.ON_DESTROY -> {
                log.i("[UI] [onDestroy] $className")
                if (activity is IMviActivity<*>) {
                    activity.runInOnDestroy()
                }
            }

            else -> {
                // ignored
            }
        }
    }
}
