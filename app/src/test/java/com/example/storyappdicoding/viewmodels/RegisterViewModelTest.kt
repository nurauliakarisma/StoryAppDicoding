package com.example.storyappdicoding.viewmodels

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.lifecycle.MutableLiveData
import com.example.storyappdicoding.data.models.Register
import com.example.storyappdicoding.data.repository.LoginRegisterRepository
import com.example.storyappdicoding.ui.viewmodel.RegisterViewModel
import com.example.storyappdicoding.utils.LiveDataTestUtils.getOrAwaitValue
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.Assert
import org.junit.Before
import org.junit.Rule
import com.example.storyappdicoding.utils.Result
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.Mockito
import org.mockito.junit.MockitoJUnitRunner

@ExperimentalCoroutinesApi
@RunWith(MockitoJUnitRunner::class)
class RegisterViewModelTest {
    @get:Rule
    val instantExecutorRule = InstantTaskExecutorRule()

    @Mock
    private lateinit var loginRegisterRepository: LoginRegisterRepository

    private lateinit var registerViewModel: RegisterViewModel

    @Before
    fun setUp() {
        registerViewModel = RegisterViewModel(loginRegisterRepository)
    }

    @Test
    fun `when User Successfully Register`(): Unit = runTest {
        val registerData = Register(false, "success")
        val nameData = "name"
        val emailData = "email@email.com"
        val passwordData = "password"

        val expectedResponse = MutableLiveData<Result<Register>>()
        expectedResponse.value = Result.Success(registerData)

        Mockito.`when`(loginRegisterRepository.register(nameData, emailData, passwordData))
            .thenReturn(expectedResponse)

        registerViewModel.register(nameData, emailData, passwordData).getOrAwaitValue()
            .let { result ->
                Assert.assertTrue(result is Result.Success)
                Assert.assertFalse(result is Result.Error)

                if (result is Result.Success) {
                    Assert.assertNotNull(result.data)
                    assertEquals(registerData, result.data)
                }
            }

        Mockito.verify(loginRegisterRepository).register(nameData, emailData, passwordData)
    }

    @Test
    fun `when User Failed Register`(): Unit = runTest {
        val errorResponseData = "failed"
        val nameData = "name"
        val emailData = "email@gmail.com"
        val passwordData = "password"

        val expectedResponse = MutableLiveData<Result<Register>>()
        expectedResponse.value = Result.Error(errorResponseData)

        Mockito.`when`(loginRegisterRepository.register(nameData, emailData, passwordData))
            .thenReturn(expectedResponse)

        registerViewModel.register(nameData, emailData, passwordData).getOrAwaitValue()
            .let { result ->
                Assert.assertTrue(result is Result.Error)
                Assert.assertFalse(result is Result.Success)

                if (result is Result.Error) {
                    Assert.assertNotNull(result.error)
                }
            }

        Mockito.verify(loginRegisterRepository).register(nameData, emailData, passwordData)
    }
}