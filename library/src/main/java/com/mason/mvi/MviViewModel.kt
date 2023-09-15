package com.mason.mvi

import androidx.annotation.DrawableRes
import androidx.lifecycle.ViewModel
import com.mason.util.resource.appStr
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.onCompletion
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.onStart

internal data class ToastMsg(val msg: String, val imageResource: Int)

internal data class LoadingMsg(val msg: String, val show: Boolean)

abstract class MviViewModel : ViewModel() {
    private val _loading = MutableStateFlow<UiState<LoadingMsg>>(UiState())
    private val _toastMsg = MutableStateFlow<UiState<ToastMsg>>(UiState())

    /**
     * viewmodel 都可以用此对象发送数据
     */
    protected val _uiState = MutableStateFlow<UiState<Any>>(UiState())

    internal val loading = _loading.asStateFlow()
    internal val toastMsg = _toastMsg.asStateFlow()

    /**
     * 在业务层接收数据可以统一使用这个
     */
    val uiState = _uiState.asStateFlow()

    /**
     * 基础通用流，viewModel 中可使用，不需要处理 loading 相关业务
     */
    protected fun <T> Flow<T>.simpleFlow() =
        onStart { showLoading() }
            .catch { _uiState.emitError(it) }
            .onCompletion { dismissLoading() }
            .onEach {
                it?.let {
                    _uiState.emitSuccess(it)
                }
            }

    fun showLoading(msg: String = appStr(R.string.mvi_loading)) {
        _loading.emitSuccess(LoadingMsg(msg, true))
    }

    fun dismissLoading() {
        _loading.emitSuccess(LoadingMsg("", false))
    }

    fun toast(msg: String) {
        _toastMsg.emitSuccess(ToastMsg(msg, 0))
    }

    fun toastWithImg(msg: String, @DrawableRes imageResource: Int) {
        _toastMsg.emitSuccess(ToastMsg(msg, imageResource))
    }
}
