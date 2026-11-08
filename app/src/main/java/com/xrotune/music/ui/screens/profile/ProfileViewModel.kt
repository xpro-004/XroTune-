package com.xrotune.music.ui.screens.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.xrotune.music.domain.model.User
import com.xrotune.music.domain.repository.UserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val userRepository: UserRepository,
) : ViewModel() {

    val isLoggedIn: StateFlow<Boolean> =
        userRepository.isLoggedIn.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)

    val user: StateFlow<User?> =
        userRepository.user.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    fun logout() = viewModelScope.launch { userRepository.logout() }
}
