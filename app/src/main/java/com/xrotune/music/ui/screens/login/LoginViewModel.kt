package com.xrotune.music.ui.screens.login

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.xrotune.music.core.common.Result
import com.xrotune.music.domain.repository.UserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class LoginUiState(
    val phone: String = "",
    val code: String = "",
    val step: Int = 0, // 0 = phone, 1 = code
    val isLoading: Boolean = false,
    val error: String? = null,
    val isLoggedIn: Boolean = false,
) {
    val canSendCode: Boolean get() = phone.length >= 6
    val canVerify: Boolean get() = code.length >= 4
}

@HiltViewModel
class LoginViewModel @Inject constructor(
    private val userRepository: UserRepository,
) : ViewModel() {

    private val _state = MutableStateFlow(LoginUiState())
    val state: StateFlow<LoginUiState> = _state.asStateFlow()

    fun onPhoneChange(value: String) {
        _state.value = _state.value.copy(phone = value.filter { it.isDigit() || it == '+' }, error = null)
    }

    fun onCodeChange(value: String) {
        _state.value = _state.value.copy(code = value.filter { it.isDigit() }.take(6), error = null)
    }

    fun sendCode() {
        if (!_state.value.canSendCode) return
        _state.value = _state.value.copy(isLoading = true, error = null)
        viewModelScope.launch {
            when (val result = userRepository.sendCode(_state.value.phone)) {
                is Result.Success -> _state.value = _state.value.copy(isLoading = false, step = 1)
                is Result.Failure -> _state.value = _state.value.copy(
                    isLoading = false,
                    // Even if SMS delivery fails, let the user try the code step.
                    step = 1,
                    error = result.error.message,
                )
            }
        }
    }

    fun verify() {
        if (!_state.value.canVerify) return
        _state.value = _state.value.copy(isLoading = true, error = null)
        viewModelScope.launch {
            when (val result = userRepository.verifyAndLogin(_state.value.phone, _state.value.code)) {
                is Result.Success -> _state.value = _state.value.copy(isLoading = false, isLoggedIn = true)
                is Result.Failure -> _state.value = _state.value.copy(isLoading = false, error = result.error.message)
            }
        }
    }

    fun backToPhone() {
        _state.value = _state.value.copy(step = 0, code = "", error = null)
    }
}
