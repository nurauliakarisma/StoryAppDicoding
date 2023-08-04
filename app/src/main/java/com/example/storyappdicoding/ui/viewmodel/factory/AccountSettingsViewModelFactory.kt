package com.example.storyappdicoding.ui.viewmodel.factory

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.storyappdicoding.ui.viewmodel.AccountSettingsViewModel
import com.example.storyappdicoding.utils.AccountPreferences

class AccountSettingsViewModelFactory(private val accountPreferences: AccountPreferences) :
    ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(AccountSettingsViewModel::class.java)) {
            return AccountSettingsViewModel(accountPreferences) as T
        } else
            throw IllegalArgumentException("Unknown ViewModel class: " + modelClass.name)
    }
}