package com.mason.mvi

import androidx.activity.OnBackPressedCallback
import androidx.annotation.DrawableRes
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import com.mason.mvi.Mvi.dismissLoadingWhenPause
import com.mason.mvi.Mvi.log
import com.mason.util.resource.appStr
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow

interface IMviFragment<VM : MviViewModel> {
    /**
     * 使用委托的方式初始化 [MviViewModel] 的子类.
     * ```
     * Fragment 作用域：
     * override val viewModel: VM by viewModels()
     *
     * Activity 作用域（可用于 Fragment 共享数据）：
     * override val viewModel: VM by activityViewModels()
     * ```
     */
    val viewModel: VM

    /**
     * 使用 [Fragment.getViewLifecycleOwner] 赋值，以避免生命周期问题.
     * ```
     * override lateinit var lifecycleOwner
     *
     * override fun onCreateView() {
     *     lifecycleOwner = viewLifecycleOwner
     * }
     * ```
     */
    var lifecycleOwner: LifecycleOwner

    /**
     * 使用委托的方式延迟初始化.
     * ```
     * override val fragmentActivity: FragmentActivity by lazy { requireActivity() }
     * ```
     */
    val fragmentActivity: FragmentActivity

    /**
     * 实现 IToast 接口的 toast 控件.
     * ```
     * override val toast = SampleToast()
     * ```
     */
    val toast: IToast

    /**
     * 实现 ILoadingDialog 接口的 dialog 控件.
     * ```
     * override val loadingDialog = SampleLoadingDialog()
     * ```
     */
    val loadingDialog: ILoadingDialog

    // onCreate
    // ---------------------------------------------------------------------------------------------

    // onCreateView
    // ---------------------------------------------------------------------------------------------

    /**
     * 通过 DataBinding 将视图绑定到数据源 [viewModel] 上.
     * ```
     * override fun bindUiToData() {
     *     binding.viewModel = viewModel
     * }
     * ```
     * Run in [Fragment.onCreateView].
     */
    fun bindUiToData() = Unit

    /**
     * 初始化通用视图相关功能，包括事件处理，例如 toolbar.
     *
     * Run in [Fragment.onCreateView].
     */
    fun initViewInBase(defaultUntilEvent: Lifecycle.Event) = Unit

    /**
     * 初始化视图相关功能，包括事件处理.
     *
     * Run in [Fragment.onCreateView].
     */
    fun initView(defaultUntilEvent: Lifecycle.Event) = Unit

    /**
     * UI 订阅（观察）[LiveData or StateFlow] 的数据变化.
     * ```
     * override fun subscribeUi() {
     *     viewModel.sample.observeData {
     *
     *     }
     * }
     * ```
     * Run in [Fragment.onCreateView].
     */
    fun subscribeUi() = Unit

    // onViewCreated
    // ---------------------------------------------------------------------------------------------

    // onActivityCreated
    // ---------------------------------------------------------------------------------------------

    // onResume
    // ---------------------------------------------------------------------------------------------

    /** Run in [Fragment.onResume()]. */
    fun loadData(defaultUntilEvent: Lifecycle.Event) = Unit

    // onPause
    // ---------------------------------------------------------------------------------------------

    /** Run in [Fragment.onPause()]. */
    fun clearData() = Unit

    // ---------------------------------------------------------------------------------------------

    fun toast(msg: String) {
        viewModel.toast(msg)
    }

    fun toastWithImg(msg: String, @DrawableRes imageResource: Int) {
        viewModel.toastWithImg(msg, imageResource)
    }

    fun showLoading(msg: String = appStr(R.string.mvi_loading)) {
        viewModel.showLoading(msg)
    }

    fun dismissLoading() {
        viewModel.dismissLoading()
    }

    fun onBackPressed()

    /**
     * Launches a new coroutine and repeats `block` every time the Fragment's viewLifecycleOwner
     * is in and out of `state` lifecycle state.
     */
    fun launchAndRepeatWithViewLifecycle(
        state: Lifecycle.State = Lifecycle.State.STARTED,
        block: suspend CoroutineScope.() -> Unit
    ) {
        launchAndRepeatWithViewLifecycleFunction(lifecycleOwner, state, block)
    }

    fun <T> Flow<T>.launchCollect() {
        launchCollectFunction(lifecycleOwner)
    }

    fun <T> Flow<T>.launchRepeatCollect(state: Lifecycle.State = Lifecycle.State.STARTED) {
        launchRepeatCollectFunction(lifecycleOwner, state)
    }

    fun <T> Flow<T>.collectData(
        lifecycleOwner: LifecycleOwner,
        state: Lifecycle.State = Lifecycle.State.STARTED,
        action: (T) -> Unit
    ) {
        collectDataFunction(lifecycleOwner, state, action)
    }

    /**
     * 流接收者处理的数据可以根据 it.data 类型作为区分，如果 it.data 类型一致，可根据自定义 it.tag 作为区分
     */
    fun <T> Flow<UiState<T>>.collectUiData(
        state: Lifecycle.State = Lifecycle.State.STARTED,
        isEvent: Boolean = true,
        actionInit: ((UiState<T>) -> Unit)? = null,
        actionError: ((UiState<T>) -> Unit)? = null,
        actionLoading: ((UiState<T>) -> Unit)? = null,
        actionEnd: ((UiState<T>) -> Unit)? = null,
        actionSuccess: (UiState<T>) -> Unit
    ) {
        collectUiDataFunction(
            lifecycleOwner,
            state,
            isEvent,
            actionInit,
            actionError,
            actionLoading,
            actionEnd,
            actionSuccess
        )
    }
}

internal fun IMviFragment<*>.runInOnCreateView() {
    fragmentActivity.onBackPressedDispatcher.addCallback(
        lifecycleOwner,
        generateOnBackPressedCallback()
    )
    bindUiToData()
    initViewInBase(Lifecycle.Event.ON_DESTROY)
    initView(Lifecycle.Event.ON_DESTROY)
    subscribeUiInBase()
    subscribeUi()
}

internal fun IMviFragment<*>.runInOnResume() {
    loadData(Lifecycle.Event.ON_PAUSE)
}

internal fun IMviFragment<*>.runInOnPause() {
    if (dismissLoadingWhenPause) {
        dismissLoading()
    }
    clearData()
}

internal fun IMviFragment<*>.runInOnDestroyView() {
    toast.clear()
}

internal fun IMviFragment<*>.subscribeUiInBase() {
    viewModel.apply {
        loading.collectUiData {
            it.data?.let { data ->
                if (data.show) {
                    showLoadingInner(data.msg)
                } else {
                    dismissLoadingInner()
                }
            }
            it.toEnd()
        }
        toastMsg.collectUiData {
            it.data?.let { data ->
                if (data.imageResource > 0) {
                    toastWithImgInner(data.msg, data.imageResource)
                } else {
                    toastInner(data.msg)
                }
            }
            it.toEnd()
        }
    }
}

internal fun IMviFragment<*>.toastInner(msg: String) {
    toast.toast(msg)
}

internal fun IMviFragment<*>.toastWithImgInner(
    msg: String,
    @DrawableRes imageResource: Int
) {
    toast.toastWithImg(msg, imageResource)
}

internal fun IMviFragment<*>.showLoadingInner(msg: String = appStr(R.string.mvi_loading)) {
    log.i("[UI] [显示加载中] $msg")
    loadingDialog.show(msg)
}

internal fun IMviFragment<*>.dismissLoadingInner() {
    log.i("[UI] [隐藏加载中]")
    loadingDialog.dismiss()
}

internal fun IMviFragment<*>.generateOnBackPressedCallback(): OnBackPressedCallback {
    return object : OnBackPressedCallback(true) {
        override fun handleOnBackPressed() {
            onBackPressed()
        }
    }
}
