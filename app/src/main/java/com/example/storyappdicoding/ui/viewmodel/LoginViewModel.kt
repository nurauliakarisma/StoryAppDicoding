package com.example.storyappdicoding.ui.viewmodel

import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.storyappdicoding.data.repository.LoginRegisterRepository
import com.example.storyappdicoding.utils.Event

class LoginViewModel(private val loginRegisterRepository: LoginRegisterRepository) : ViewModel() {

    val isEmailError = MutableLiveData<Boolean>()
    val isPassError = MutableLiveData<Boolean>()

    val canLogin = MediatorLiveData<Boolean>().apply {
        var emailFlag = true
        var passFlag = true
        value = false
        addSource(isEmailError) { x ->
            x?.let {
                emailFlag = it
                value = !passFlag && !emailFlag
            }
        }
        addSource(isPassError) { x ->
            x?.let {
                passFlag = it
                value = !passFlag && !emailFlag
            }
        }
    }

    val isLoading = MutableLiveData<Boolean>()

    val toastMessage = MutableLiveData<Event<String>>()

    fun login(email: String, password: String) =
        loginRegisterRepository.login(email, password)
}