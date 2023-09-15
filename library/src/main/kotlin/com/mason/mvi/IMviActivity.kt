package com.mason.mvi

import android.view.View
import androidx.annotation.DrawableRes
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.lifecycleScope
import com.mason.util.resource.appStr
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow

interface IMviActivity<VM : MviViewModel> {
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
     * 使用 [AppCompatActivity] 赋值，以避免生命周期问题.
     * ```
     * override lateinit var lifecycleOwner
     *
     * override fun onCreate(savedInstanceState: Bundle?) {
     *     lifecycleOwner = this
     * }
     * ```
     */
    var lifecycleOwner: LifecycleOwner

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


    //-------------- function ---------------

    fun View.throttleFirst(
        scope: CoroutineScope = lifecycleOwner.lifecycleScope,
        timeDelay: Long = 1000,
        callback: () -> Unit
    ) {
        throttleFirst(this, scope, timeDelay, callback)
    }

    fun View.throttleLatest(
        scope: CoroutineScope = lifecycleOwner.lifecycleScope,
        timeDelay: Long = 1000,
        callback: () -> Unit
    ) {
        throttleLatest(this, scope, timeDelay, callback)
    }

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

internal fun IMviActivity<*>.runInOnCreate() {
    initView(Lifecycle.Event.ON_DESTROY)
    subscribeUiInBase()
    subscribeUi()
}

internal fun IMviActivity<*>.runInOnDestroy() {
    toast.clear()
}

internal fun IMviActivity<*>.subscribeUiInBase() {
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

internal fun IMviActivity<*>.toastInner(msg: String) {
    toast.toast(msg)
}

internal fun IMviActivity<*>.toastWithImgInner(
    msg: String,
    @DrawableRes imageResource: Int
) {
    toast.toastWithImg(msg, imageResource)
}

internal fun IMviActivity<*>.showLoadingInner(msg: String = appStr(R.string.mvi_loading)) {
    Mvi.log.i("[UI] [显示加载中] $msg")
    loadingDialog.show(msg)
}

internal fun IMviActivity<*>.dismissLoadingInner() {
    Mvi.log.i("[UI] [隐藏加载中]")
    loadingDialog.dismiss()
}
