@file:Suppress("PropertyName")

package com.mason.mvi

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import androidx.annotation.VisibleForTesting
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavController
import androidx.navigation.fragment.findNavController
import androidx.viewbinding.ViewBinding
import com.mason.mvi.Mvi.log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow

abstract class MviFragment<VM : MviViewModel, Binding : ViewBinding> : Fragment(),
    IMviFragment<VM> {
    private var _binding by autoCleared<Binding>()
    val navController: NavController by lazy { findNavController() }
    override lateinit var lifecycleOwner: LifecycleOwner
    override val fragmentActivity: FragmentActivity by lazy { requireActivity() }

    val binding: Binding
        get() = _binding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        lifecycle.addObserver(FragmentObserver(this))
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = ViewBindingUtil.inflateWithGeneric(this, layoutInflater)
        lifecycleOwner = viewLifecycleOwner
        return _binding.root
    }

    override fun onBackPressed() {
        navController.navigateUp()
    }

    fun View.throttleFirst(
        scope: CoroutineScope = viewLifecycleOwner.lifecycleScope,
        timeDelay: Long = 500,
        callback: () -> Unit
    ) {
        throttleFirst(this, scope, timeDelay, callback)
    }

    fun View.throttleLatest(
        scope: CoroutineScope = viewLifecycleOwner.lifecycleScope,
        timeDelay: Long = 500,
        callback: () -> Unit
    ) {
        throttleLatest(this, scope, timeDelay, callback)
    }

    fun <T> EditText.textChangeLister(
        scope: CoroutineScope = viewLifecycleOwner.lifecycleScope,
        workFlow: (String) -> Flow<T>,
        timeDelay: Long = 500,
        callback: (T) -> Unit
    ) {
        editThrottleLatest(this, scope, timeDelay, workFlow, callback)
    }
}

abstract class MviDialogFragment<VM : MviViewModel, Binding : ViewBinding> : DialogFragment(),
    IMviFragment<VM> {
    private var _binding by autoCleared<Binding>()
    val navController: NavController by lazy { findNavController() }
    override lateinit var lifecycleOwner: LifecycleOwner
    override val fragmentActivity: FragmentActivity by lazy { requireActivity() }

    val binding: Binding
        get() = _binding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        lifecycle.addObserver(FragmentObserver(this))
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = ViewBindingUtil.inflateWithGeneric(this, layoutInflater)
        lifecycleOwner = viewLifecycleOwner
        return _binding.root
    }

    override fun onBackPressed() {
        navController.navigateUp()
    }

    fun View.throttleFirst(
        scope: CoroutineScope = viewLifecycleOwner.lifecycleScope,
        timeDelay: Long = 1000,
        callback: () -> Unit
    ) {
        throttleFirst(this, scope, timeDelay, callback)
    }

    fun View.throttleLatest(
        scope: CoroutineScope = viewLifecycleOwner.lifecycleScope,
        timeDelay: Long = 1000,
        callback: () -> Unit
    ) {
        throttleLatest(this, scope, timeDelay, callback)
    }
}

class FragmentObserver(private val fragment: Fragment) : DefaultLifecycleObserver {
    private val className = fragment.javaClass.simpleName

    override fun onCreate(owner: LifecycleOwner) {
        log.i("[UI] [onCreate] $className")
        fragment.viewLifecycleOwnerLiveData.observe(fragment) { viewLifecycleOwner ->
            viewLifecycleOwner?.lifecycle?.addObserver(generateInnerObserver())
        }
    }

    override fun onResume(owner: LifecycleOwner) {
        log.i("[UI] [进入] $className")
        if (fragment is IMviFragment<*>) {
            fragment.runInOnResume()
        }
    }

    override fun onPause(owner: LifecycleOwner) {
        log.i("[UI] [离开] $className")
        if (fragment is IMviFragment<*>) {
            fragment.runInOnPause()
        }
    }

    override fun onDestroy(owner: LifecycleOwner) {
        log.i("[UI] [onDestroy] $className")
    }

    @VisibleForTesting(otherwise = VisibleForTesting.PRIVATE)
    fun generateInnerObserver(): DefaultLifecycleObserver {
        return object : DefaultLifecycleObserver {
            override fun onCreate(owner: LifecycleOwner) {
                log.i("[UI] [onCreateView] $className")
                if (fragment is IMviFragment<*>) {
                    fragment.runInOnCreateView()
                }
            }

            override fun onDestroy(owner: LifecycleOwner) {
                log.i("[UI] [onDestroyView] $className")
                if (fragment is IMviFragment<*>) {
                    fragment.runInOnDestroyView()
                }
            }
        }
    }
}
