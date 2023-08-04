package com.example.storyappdicoding.ui.viewmodel

import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.storyappdicoding.data.repository.LoginRegisterRepository
import com.example.storyappdicoding.utils.Event

class RegisterViewModel(private val loginRegisterRepository: LoginRegisterRepository) :
    ViewModel() {

    val isNameError = MutableLiveData<Boolean>()
    val isEmailError = MutableLiveData<Boolean>()
    val isPassError = MutableLiveData<Boolean>()

    val canRegister = MediatorLiveData<Boolean>().apply {
        var nameFlag = true
        var emailFlag = true
        var passFlag = true
        value = false
        addSource(isNameError) { x ->
            x?.let {
                nameFlag = it
                value = !passFlag && !emailFlag && !nameFlag
            }
        }
        addSource(isEmailError) { x ->
            x?.let {
                emailFlag = it
                value = !passFlag && !emailFlag && !nameFlag
            }
        }
        addSource(isPassError) { x ->
            x?.let {
                passFlag = it
                value = !passFlag && !emailFlag && !nameFlag
            }
        }
    }

    val isLoading = MutableLiveData<Boolean>()

    val toastMessage = MutableLiveData<Event<String>>()

    fun register(name: String, email: String, password: String) =
        loginRegisterRepository.register(name, email, password)
}