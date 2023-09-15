package com.mason.mvi

import com.mason.mvi.UiState.Type
import kotlinx.coroutines.flow.MutableStateFlow

data class UiState<T>(
    val data: T? = null,
    var type: Type = Type.INIT,
    val tag: String = "",
    val throwable: Throwable? = null
) {
    enum class Type {
        INIT,
        LOADING,
        SUCCESS,
        ERROR,
        END
    }
}

fun <T> UiState<T>.toEnd() {
    type = Type.END
}

fun <T> MutableStateFlow<UiState<T>>.emitSuccess(data: T, tag: String = "") {
    value = UiState(type = Type.SUCCESS, data = data, tag = tag)
}

fun <T> MutableStateFlow<UiState<T>>.emitError(
    throwable: Throwable,
    data: T? = null,
    tag: String = ""
) {
    value = UiState(type = Type.ERROR, throwable = throwable, data = data, tag = tag)
}

fun <T> MutableStateFlow<UiState<T>>.emitLoading(data: T? = null, tag: String = "") {
    value = UiState(type = Type.LOADING, data = data, tag = tag)
}

fun <T> MutableStateFlow<UiState<T>>.emitEnd(data: T? = null, tag: String = "") {
    value = UiState(type = Type.END, data = data, tag = tag)
}

fun <T> MutableStateFlow<UiState<T>>.emitInit() {
    value = UiState(type = Type.INIT)
}
