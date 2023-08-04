package com.example.storyappdicoding.viewmodels

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.example.storyappdicoding.ui.viewmodel.AccountSettingsViewModel
import com.example.storyappdicoding.utils.AccountPreferences
import com.example.storyappdicoding.utils.CoroutineTestRule
import com.example.storyappdicoding.utils.LiveDataTestUtils.getOrAwaitValue
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.Mockito
import org.mockito.junit.MockitoJUnitRunner

@ExperimentalCoroutinesApi
@RunWith(MockitoJUnitRunner::class)
class AccountSettingsViewModelTest {
    @get:Rule
    var instantTaskExecutorRule = InstantTaskExecutorRule()

    @get:Rule
    var coroutineTestRule = CoroutineTestRule()

    @Mock
    private lateinit var accountPreferences: AccountPreferences
    private lateinit var accountSettingsViewModel: AccountSettingsViewModel


    @Before
    fun setup() {
        accountSettingsViewModel = AccountSettingsViewModel(accountPreferences)
    }

    @Test
    fun `when Successfully Save Token`(): Unit = runTest {
        val tokenData = "token"

        accountSettingsViewModel.saveToken(tokenData)
        Mockito.verify(accountPreferences)
            .saveToken(tokenData)
    }

    @Test
    fun `when Successfully Get Token`(): Unit = runTest {
        val tokenData = "token"

        val expectedResult = flowOf(tokenData)

        Mockito.`when`(accountPreferences.getToken()).thenReturn(expectedResult)

        val actualResult = accountSettingsViewModel.getToken().getOrAwaitValue()

        Assert.assertEquals(tokenData, actualResult)
        Mockito.verify(accountPreferences).getToken()
    }

    @Test
    fun `when Successfully Clear Token`(): Unit = runTest {
        accountSettingsViewModel.clearToken()
        Mockito.verify(accountPreferences).clearToken()
    }
}