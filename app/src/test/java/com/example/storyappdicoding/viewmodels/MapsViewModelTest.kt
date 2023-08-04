package com.example.storyappdicoding.viewmodels

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.lifecycle.MutableLiveData
import com.example.storyappdicoding.data.models.ListStoryItem
import com.example.storyappdicoding.data.models.Stories
import com.example.storyappdicoding.data.repository.StoriesRepository
import com.example.storyappdicoding.ui.viewmodel.MapsViewModel
import com.example.storyappdicoding.utils.LiveDataTestUtils.getOrAwaitValue
import com.example.storyappdicoding.utils.Result
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.Assert
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.Mockito
import org.junit.Assert.assertEquals
import org.mockito.junit.MockitoJUnitRunner

@ExperimentalCoroutinesApi
@RunWith(MockitoJUnitRunner::class)
class MapsViewModelTest {
    @get:Rule
    var instantTaskExecutorRule = InstantTaskExecutorRule()

    @Mock
    private lateinit var storiesRepository: StoriesRepository
    private lateinit var mapsViewModel: MapsViewModel

    @Before
    fun setup() {
        mapsViewModel = MapsViewModel(storiesRepository)
    }

    @Test
    fun `when Successfully Get Stories with Maps`(): Unit = runTest {
        val storiesResponseData =  Stories(ArrayList<ListStoryItem>(), false, "success")
        val expectedResponse = MutableLiveData<Result<Stories>>()
        expectedResponse.value = Result.Success(storiesResponseData)

        Mockito.`when`(storiesRepository.getMapsStory()).thenReturn(expectedResponse)

        mapsViewModel.getMapsStory().getOrAwaitValue().let { result ->
            Assert.assertTrue(result is Result.Success)
            Assert.assertFalse(result is Result.Error)

            if (result is Result.Success) {
                Assert.assertNotNull(result.data)
                assertEquals(storiesResponseData, result.data)
            }
        }

        Mockito.verify(storiesRepository).getMapsStory()
    }

    @Test
    fun `when Failed Get Stories with Maps`(): Unit = runTest {
        val expectedResponse = MutableLiveData<Result<Stories>>()
        expectedResponse.value = Result.Error("failed")

        Mockito.`when`(storiesRepository.getMapsStory()).thenReturn(expectedResponse)

        mapsViewModel.getMapsStory().getOrAwaitValue().let { result ->
            Assert.assertTrue(result is Result.Error)
            Assert.assertFalse(result is Result.Success)

            if (result is Result.Error) {
                Assert.assertNotNull(result.error)
            }
        }

        Mockito.verify(storiesRepository).getMapsStory()
    }
}