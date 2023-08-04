package com.example.storyappdicoding.ui.view.login

import android.content.Intent
import android.os.Bundle
import android.util.Patterns
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import androidx.core.widget.addTextChangedListener
import com.example.storyappdicoding.ui.view.main.MainActivity
import com.example.storyappdicoding.data.remote.ApiConfig
import com.example.storyappdicoding.databinding.ActivityLoginBinding
import com.example.storyappdicoding.ui.view.register.RegisterActivity
import com.example.storyappdicoding.ui.viewmodel.AccountSettingsViewModel
import com.example.storyappdicoding.ui.viewmodel.LoginViewModel
import com.example.storyappdicoding.R
import com.example.storyappdicoding.ui.viewmodel.factory.AccountSettingsViewModelFactory
import com.example.storyappdicoding.ui.viewmodel.factory.LoginRegisterViewModelFactory
import com.example.storyappdicoding.utils.AccountPreferences
import com.example.storyappdicoding.utils.AccountPreferences.Companion.TOKEN_ID
import com.example.storyappdicoding.utils.AccountPreferences.Companion.preferenceDefaultValue
import com.example.storyappdicoding.utils.Event
import com.example.storyappdicoding.utils.Result
import com.example.storyappdicoding.utils.dataStore

class LoginActivity : AppCompatActivity() {
    private lateinit var binding: ActivityLoginBinding

    private val loginViewModel by viewModels<LoginViewModel> {
        LoginRegisterViewModelFactory(
            ApiConfig.getApiService()
        )
    }

    private val accountSettingsViewModel by viewModels<AccountSettingsViewModel> {
        AccountSettingsViewModelFactory(
            AccountPreferences.getPrefInstance(dataStore)
        )
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        observeError()
        observeLoading()
        observeToast()
        observeLogin()

        setListeners()
    }

    private fun observeError() {
        binding.apply {
            loginViewModel.canLogin.observe(this@LoginActivity) { login ->
                btnLogin.isEnabled = login
            }
        }
    }

    private fun observeLoading() {
        loginViewModel.isLoading.observe(this) { isLoading ->
            showLoading(isLoading)
        }
    }

    private fun observeToast() {
        loginViewModel.toastMessage.observe(this) { messageEvent ->
            messageEvent.getContentIfNotHandled()?.let { message ->
                showToast(message)
            }
        }
    }

    private fun observeLogin() {
        accountSettingsViewModel.getToken().observe(this) { preferences ->
            if (preferences != preferenceDefaultValue) {
                val iMain = Intent(this, MainActivity::class.java)
                iMain.putExtra(TOKEN_ID, preferences.toString())
                startActivity(iMain)
                finish()
            }
        }
    }

    private fun setListeners() {
        binding.apply {
            edLoginEmail.addTextChangedListener(onTextChanged = { email, _, _, _ ->
                edLoginEmail.error =
                    if (!Patterns.EMAIL_ADDRESS.matcher(email.toString())
                            .matches() && !email.isNullOrEmpty()
                    ) "Email is invalid!" else null

                if (!edLoginEmail.error.isNullOrEmpty() || edLoginEmail.text.isNullOrEmpty()) {
                    loginViewModel.isEmailError.postValue(true)
                } else {
                    loginViewModel.isEmailError.postValue(false)
                }
            })

            edLoginPassword.addTextChangedListener(onTextChanged = { _, _, _, _ ->
                if (!edLoginPassword.error.isNullOrEmpty() || edLoginPassword.text.isNullOrEmpty()) {
                    loginViewModel.isPassError.postValue(true)
                } else {
                    loginViewModel.isPassError.postValue(false)
                }
            })

            btnRegister.setOnClickListener {
                val iRegister = Intent(this@LoginActivity, RegisterActivity::class.java)
                startActivity(iRegister)
                overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left)
            }


            btnLogin.setOnClickListener {
                showLoading(true)
                val email = edLoginEmail.text.toString()
                val pass = edLoginPassword.text.toString()

                loginViewModel.login(email, pass).observe(this@LoginActivity) { res ->
                    when (res) {
                        is Result.Loading -> {
                            loginViewModel.isLoading.postValue(true)
                        }
                        is Result.Error -> {
                            loginViewModel.isLoading.postValue(false)
                            loginViewModel.toastMessage.postValue(Event(res.error))
                        }
                        is Result.Success -> {
                            loginViewModel.isLoading.postValue(false)
                            accountSettingsViewModel.saveToken(res.data.loginResult.token)
                        }
                    }
                }
            }
        }
    }

    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }

    private fun showLoading(isLoading: Boolean) {
        binding.apply {
            progressbar.isVisible = isLoading
            btnLogin.isVisible = !isLoading
            edLoginEmail.isEnabled = !isLoading
            edLoginPassword.isEnabled = !isLoading
        }
    }
}