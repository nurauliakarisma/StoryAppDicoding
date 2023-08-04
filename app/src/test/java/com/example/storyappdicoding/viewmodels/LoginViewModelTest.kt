package com.example.storyappdicoding.viewmodels

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.lifecycle.MutableLiveData
import com.example.storyappdicoding.data.models.Login
import com.example.storyappdicoding.data.models.LoginResult
import com.example.storyappdicoding.data.repository.LoginRegisterRepository
import com.example.storyappdicoding.ui.viewmodel.LoginViewModel
import com.example.storyappdicoding.utils.LiveDataTestUtils.getOrAwaitValue
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.Assert
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import com.example.storyappdicoding.utils.Result
import org.junit.Assert.assertEquals
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.Mockito
import org.mockito.junit.MockitoJUnitRunner

@ExperimentalCoroutinesApi
@RunWith(MockitoJUnitRunner::class)
class LoginViewModelTest {

    @get:Rule
    val instantExecutorRule = InstantTaskExecutorRule()

    @Mock
    private lateinit var loginRegisterRepository: LoginRegisterRepository

    private lateinit var loginViewModel: LoginViewModel

    @Before
    fun setUp() {
        loginViewModel = LoginViewModel(loginRegisterRepository)
    }

    @Test
    fun `when User Successfully Login`(): Unit = runTest {
        val emailData = "email@gmail.com"
        val passwordData = "password"
        val loginData = generateLoginData()

        val expectedResponse = MutableLiveData<Result<Login>>()
        expectedResponse.value = Result.Success(loginData)

        Mockito.`when`(loginRegisterRepository.login(emailData, passwordData)).thenReturn(expectedResponse)

        loginViewModel.login(emailData, passwordData).getOrAwaitValue().let { result ->
            Assert.assertTrue(result is Result.Success)
            Assert.assertFalse(result is Result.Error)

            if (result is Result.Success) {
                Assert.assertNotNull(result.data)
                assertEquals(loginData, result.data)
            }
        }

        Mockito.verify(loginRegisterRepository).login(emailData, passwordData)
    }

    @Test
    fun `when User Failed Login`(): Unit = runTest {
        val emailData = "email@email.com"
        val passwordData = "password"
        val errorResponseData = "failed"

        val expectedResponse = MutableLiveData<Result<Login>>()
        expectedResponse.value = Result.Error(errorResponseData)

        Mockito.`when`(loginRegisterRepository.login(emailData, passwordData)).thenReturn(expectedResponse)

        loginViewModel.login(emailData, passwordData).getOrAwaitValue().let { result ->
            Assert.assertTrue(result is Result.Error)
            Assert.assertFalse(result is Result.Success)

            if (result is Result.Error) {
                Assert.assertNotNull(result.error)
            }
        }

        Mockito.verify(loginRegisterRepository).login(emailData, passwordData)
    }

    private fun generateLoginData(): Login {
        val loginResult = LoginResult(
            "cek",
            "cek",
            "cek"
        )
        return Login(loginResult, false, "success")
    }
}