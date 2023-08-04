package com.example.storyappdicoding.ui.view.register

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Patterns
import android.widget.Toast
import androidx.activity.viewModels
import androidx.core.view.isVisible
import androidx.core.widget.addTextChangedListener
import com.example.storyappdicoding.R
import com.example.storyappdicoding.data.remote.ApiConfig
import com.example.storyappdicoding.databinding.ActivityRegisterBinding
import com.example.storyappdicoding.utils.Result
import com.example.storyappdicoding.ui.viewmodel.RegisterViewModel
import com.example.storyappdicoding.ui.viewmodel.factory.LoginRegisterViewModelFactory
import com.example.storyappdicoding.utils.Event

class RegisterActivity : AppCompatActivity() {
    private lateinit var binding: ActivityRegisterBinding

    private val registerViewModel by viewModels<RegisterViewModel> {
        LoginRegisterViewModelFactory(
            ApiConfig.getApiService()
        )
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRegisterBinding.inflate(layoutInflater)
        setContentView(binding.root)

        observeError()
        observeLoading()
        observeToast()

        setListeners()
    }

    private fun observeError() {
        binding.apply {
            registerViewModel.canRegister.observe(this@RegisterActivity) { register ->
                btnRegister.isEnabled = register
            }
        }
    }

    private fun observeLoading() {
        registerViewModel.isLoading.observe(this) { isLoading ->
            showLoading(isLoading)
        }
    }

    private fun observeToast() {
        registerViewModel.toastMessage.observe(this) { messageEvent ->
            messageEvent.getContentIfNotHandled()?.let { message ->
                showToast(message)
            }
        }
    }

    private fun setListeners() {
        binding.apply {
            edRegisterName.addTextChangedListener(onTextChanged = { _, _, _, _ ->
                if (edRegisterName.text.isNullOrEmpty()) {
                    registerViewModel.isNameError.postValue(true)
                } else {
                    registerViewModel.isNameError.postValue(false)
                }
            })

            edRegisterEmail.addTextChangedListener(onTextChanged = { email, _, _, _ ->
                edRegisterEmail.error =
                    if (!Patterns.EMAIL_ADDRESS.matcher(email)
                            .matches() && !email.isNullOrEmpty()
                    ) "Email is invalid!" else null

                if (!edRegisterEmail.error.isNullOrEmpty() || edRegisterEmail.text.isNullOrEmpty()) {
                    registerViewModel.isEmailError.postValue(true)
                } else {
                    registerViewModel.isEmailError.postValue(false)
                }
            })

            edRegisterPassword.addTextChangedListener(onTextChanged = { _, _, _, _ ->
                if (!edRegisterPassword.error.isNullOrEmpty() || edRegisterPassword.text.isNullOrEmpty()) {
                    registerViewModel.isPassError.postValue(true)
                } else {
                    registerViewModel.isPassError.postValue(false)
                }
            })

            btnLogin.setOnClickListener {
                finish()
                overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left)
            }

            btnRegister.setOnClickListener {
                showLoading(true)
                val name = edRegisterName.text.toString()
                val email = edRegisterEmail.text.toString()
                val pass = edRegisterPassword.text.toString()

                registerViewModel.register(name, email, pass)
                    .observe(this@RegisterActivity) { res ->
                        when (res) {
                            is Result.Loading -> {
                                registerViewModel.isLoading.postValue(true)
                            }
                            is Result.Error -> {
                                registerViewModel.isLoading.postValue(false)
                                registerViewModel.toastMessage.postValue(Event(res.error))
                            }
                            is Result.Success -> {
                                registerViewModel.isLoading.postValue(false)
                                registerViewModel.toastMessage.postValue(Event("Successfully created account! Please Log in!"))
                                finish()
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
            btnRegister.isVisible = !isLoading
            edRegisterName.isEnabled = !isLoading
            edRegisterEmail.isEnabled = !isLoading
            edRegisterPassword.isEnabled = !isLoading
        }
    }
}