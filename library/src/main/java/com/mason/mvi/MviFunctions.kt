package com.mason.mvi

import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.EditText
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.mason.mvi.Mvi.log
import com.mason.util.exception.msg
import com.mason.util.toast.toast
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.buffer
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.mapLatest
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.shareIn
import kotlinx.coroutines.launch
import java.util.concurrent.CancellationException

internal fun throttleFirst(
    view: View,
    scope: CoroutineScope,
    timeDelay: Long = 500,
    callback: () -> Unit
) {
    callbackFlow {
        view.setOnClickListener { trySend(Unit) }
        awaitClose { view.setOnClickListener(null) }
    }
        .throttle(timeDelay)
        .catch {
            log.e(it.message.toString())
        }
        .onEach { callback.invoke() }
        .launchIn(scope)
}

internal fun <T> Flow<T>.throttle(time: Long): Flow<T> = flow {
    var lastTime = 0L
    collect {
        val currentTime = System.currentTimeMillis()
        if (currentTime - lastTime > time) {
            lastTime = currentTime
            emit(it)
        }
    }
}

internal fun throttleLatest(
    view: View,
    scope: CoroutineScope,
    timeDelay: Long = 1000,
    callback: () -> Unit
) {
    scope.launch {
        callbackFlow {
            view.setOnClickListener {
                trySend(Unit)
            }
            awaitClose { view.setOnClickListener(null) }
        }
            .catch { log.i(it.message.toString()) }
            .shareIn(scope, SharingStarted.WhileSubscribed(), 0)
            .mapLatest { delay(timeDelay) }
            .buffer(0)
            .safeCollect { callback.invoke() }
    }
}

/**
 * 编辑框防抖动
 */
internal fun <T> editThrottleLatest(
    view: EditText,
    scope: CoroutineScope,
    timeDelay: Long = 500,
    workFlow: (String) -> Flow<T>,
    callback: (T) -> Unit
) {
    callbackFlow {
        val watcher = object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
                // nothing
            }

            override fun afterTextChanged(s: Editable?) {
                // nothing
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                s?.let {
                    trySend(it)
                }
            }
        }
        view.addTextChangedListener(watcher)
        awaitClose { view.removeTextChangedListener(watcher) }
    }
        .filter { it.isNotEmpty() }
        .debounce(timeDelay)
        .flatMapLatest { workFlow.invoke(it.toString()) }
        .catch { log.e(it.message.toString()) }
        .onEach { callback.invoke(it) }
        .launchIn(scope)
}

suspend fun <T> Flow<T>.safeCollect(block: suspend (T) -> Unit) {
    runCatching { collect { block.invoke(it) } }.onFailure {
        if (it !is CancellationException) {
            toast(it.message.toString())
            log.e(it.message.toString())
        }
    }
}

/**
 * Launches a new coroutine and repeats `block` every time the Fragment's viewLifecycleOwner
 * is in and out of `state` lifecycle state.
 */
internal fun launchAndRepeatWithViewLifecycleFunction(
    lifecycleOwner: LifecycleOwner,
    state: Lifecycle.State,
    block: suspend CoroutineScope.() -> Unit
) {
    lifecycleOwner.lifecycleScope.launch {
        lifecycleOwner.lifecycle.repeatOnLifecycle(state) {
            block()
        }
    }
}

internal fun <T> Flow<T>.launchCollectFunction(lifecycleOwner: LifecycleOwner) {
    lifecycleOwner.lifecycleScope.launch {
        this@launchCollectFunction.collect { }
    }
}

internal fun <T> Flow<T>.launchRepeatCollectFunction(
    lifecycleOwner: LifecycleOwner,
    state: Lifecycle.State
) {
    launchAndRepeatWithViewLifecycleFunction(lifecycleOwner = lifecycleOwner, state = state) {
        this@launchRepeatCollectFunction.collect { }
    }
}

/**
 * 通用 StateFlow 数据接收扩展
 */
internal fun <T> Flow<T>.collectDataFunction(
    lifecycleOwner: LifecycleOwner,
    state: Lifecycle.State,
    action: (T) -> Unit
) {
    lifecycleOwner.apply {
        lifecycleScope.launch {
            lifecycle.repeatOnLifecycle(state) {
                this@collectDataFunction.safeCollect {
                    action.invoke(it)
                }
            }
        }
    }
}

/**
 * 流接收者处理的数据可以根据 it.data 类型作为区分，如果 it.data 类型一致，可根据自定义 it.tag 作为区分
 */
internal fun <T> Flow<UiState<T>>.collectUiDataFunction(
    lifecycleOwner: LifecycleOwner,
    state: Lifecycle.State,
    isEvent: Boolean = true,
    actionInit: ((UiState<T>) -> Unit)? = null,
    actionError: ((UiState<T>) -> Unit)? = null,
    actionLoading: ((UiState<T>) -> Unit)? = null,
    actionEnd: ((UiState<T>) -> Unit)? = null,
    actionSuccess: (UiState<T>) -> Unit
) {
    lifecycleOwner.apply {
        lifecycleScope.launch {
            lifecycle.repeatOnLifecycle(state) {
                this@collectUiDataFunction
                    .safeCollect {
                        when (it.type) {
                            UiState.Type.SUCCESS -> {
                                actionSuccess.invoke(it)
                                if (isEvent) {
                                    it.toEnd()
                                }
                            }

                            UiState.Type.ERROR -> {
                                log.e("[UiState Error] ${it.throwable?.msg}")
                                actionError?.invoke(it)
                            }

                            UiState.Type.LOADING ->
                                actionLoading?.invoke(it)

                            UiState.Type.END ->
                                actionEnd?.invoke(it)

                            UiState.Type.INIT -> {
                                actionInit?.invoke(it)
                            }
                        }
                    }
            }
        }
    }
}
