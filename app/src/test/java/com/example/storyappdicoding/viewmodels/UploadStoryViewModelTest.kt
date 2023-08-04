package com.example.storyappdicoding.viewmodels

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.lifecycle.MutableLiveData
import com.example.storyappdicoding.data.models.Upload
import com.example.storyappdicoding.data.repository.StoriesRepository
import com.example.storyappdicoding.ui.viewmodel.UploadStoryViewModel
import com.example.storyappdicoding.utils.LiveDataTestUtils.getOrAwaitValue
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.Assert
import org.junit.Before
import com.example.storyappdicoding.utils.Result
import org.junit.Rule
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.Mockito
import org.mockito.junit.MockitoJUnitRunner
import java.io.File

@ExperimentalCoroutinesApi
@RunWith(MockitoJUnitRunner::class)
class UploadStoryViewModelTest {

    @get:Rule
    var instantTaskExecutorRule = InstantTaskExecutorRule()

    @Mock
    private lateinit var storiesRepository: StoriesRepository
    private lateinit var uploadStoryViewModel: UploadStoryViewModel





    @Before
    fun setup() {
        uploadStoryViewModel = UploadStoryViewModel(storiesRepository)
    }

    @Test
    fun `when Successfully Upload Story`(): Unit = runTest {
        val uploadData = Upload(
            error = false,
            message = "success"
        )

        val fileData = File("file")
        val descData = "desc"

        val expectedResponse = MutableLiveData<Result<Upload>>()
        expectedResponse.value = Result.Success(uploadData)

        Mockito.`when`(
            storiesRepository.uploadStory(
                fileData,
                descData,
                null,
                null
            )
        ).thenReturn(expectedResponse)

        uploadStoryViewModel.uploadStory(fileData, descData)
            .getOrAwaitValue().let { result ->
                Assert.assertTrue(result is Result.Success)
                Assert.assertFalse(result is Result.Error)

                if (result is Result.Success) {
                    Assert.assertNotNull(result.data)
                    assertEquals(uploadData, result.data)
                }
            }

        Mockito.verify(storiesRepository).uploadStory(
            fileData,
            descData,
            null,
            null
        )
    }

    @Test
    fun `when Failed Upload Story`(): Unit = runTest {
        val fileData = File("file")
        val descData = "desc"

        val expectedResponse = MutableLiveData<Result<Upload>>()
        expectedResponse.value = Result.Error("failed")

        Mockito.`when`(
            storiesRepository.uploadStory(
                fileData,
                descData,
                null,
                null
            )
        ).thenReturn(expectedResponse)

        uploadStoryViewModel.uploadStory(fileData, descData)
            .getOrAwaitValue().let { result ->
                Assert.assertTrue(result is Result.Error)
                Assert.assertFalse(result is Result.Success)

                if (result is Result.Error) {
                    Assert.assertNotNull(result.error)
                }
            }

        Mockito.verify(storiesRepository).uploadStory(
            fileData,
            descData,
            null,
            null
        )
    }
}