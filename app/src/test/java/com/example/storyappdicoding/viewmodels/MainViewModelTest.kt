package com.example.storyappdicoding.viewmodels

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.lifecycle.MutableLiveData
import androidx.paging.AsyncPagingDataDiffer
import androidx.paging.ExperimentalPagingApi
import androidx.paging.PagingData
import androidx.recyclerview.widget.ListUpdateCallback
import com.example.storyappdicoding.data.models.ListStoryItem
import com.example.storyappdicoding.data.repository.StoriesRepository
import com.example.storyappdicoding.ui.view.adapter.StoriesAdapter
import com.example.storyappdicoding.ui.viewmodel.MainViewModel
import com.example.storyappdicoding.utils.CoroutineTestRule
import com.example.storyappdicoding.utils.LiveDataTestUtils.getOrAwaitValue
import com.example.storyappdicoding.utils.PagedTestDataSource
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.Assert
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.Mockito
import org.mockito.junit.MockitoJUnitRunner

@ExperimentalCoroutinesApi
@ExperimentalPagingApi
@RunWith(MockitoJUnitRunner::class)
class MainViewModelTest {
    @get:Rule
    var instantTaskExecutorRule = InstantTaskExecutorRule()

    @get:Rule
    var coroutinesTestRule = CoroutineTestRule()

    @Mock
    private lateinit var storiesRepository: StoriesRepository

    private lateinit var mainViewModel: MainViewModel


    @Test
    fun `when Successfully Get Stories and Not Null`() = runTest {
        val stories = generateStories()
        val dataSource = PagedTestDataSource.snapshot(stories)

        val listStory = MutableLiveData<PagingData<ListStoryItem>>()
        listStory.value = dataSource

        Mockito.`when`(storiesRepository.getStories()).thenReturn(listStory)

        mainViewModel = MainViewModel(storiesRepository)

        val actualStories: PagingData<ListStoryItem> =
            mainViewModel.storyItems.getOrAwaitValue()

        val differ = AsyncPagingDataDiffer(
            diffCallback = StoriesAdapter.DIFF_CALLBACK,
            updateCallback = listUpdateCallback,
            mainDispatcher = coroutinesTestRule.testDispatcher,
            workerDispatcher = coroutinesTestRule.testDispatcher
        )

        differ.submitData(actualStories)

        Mockito.verify(storiesRepository).getStories()
        Assert.assertNotNull(differ.snapshot())
        assertEquals(stories.size, differ.snapshot().size)
        assertEquals(stories[0], differ.snapshot()[0])
    }

    @Test
    fun `when Failed Get Stories and Not Null`() = runTest {
        val storyList = emptyList<ListStoryItem>()
        val dataSource = PagedTestDataSource.snapshot(storyList)

        val listStory = MutableLiveData<PagingData<ListStoryItem>>()
        listStory.value = dataSource

        Mockito.`when`(storiesRepository.getStories()).thenReturn(listStory)

        mainViewModel = MainViewModel(storiesRepository)

        val actualStories: PagingData<ListStoryItem> =
            mainViewModel.storyItems.getOrAwaitValue()

        val differ = AsyncPagingDataDiffer(
            diffCallback = StoriesAdapter.DIFF_CALLBACK,
            updateCallback = listUpdateCallback,
            mainDispatcher = coroutinesTestRule.testDispatcher,
            workerDispatcher = coroutinesTestRule.testDispatcher
        )

        differ.submitData(actualStories)

        Mockito.verify(storiesRepository).getStories()
        Assert.assertNotNull(differ.snapshot())
        assertEquals(0, differ.snapshot().size)
    }

    private val listUpdateCallback = object : ListUpdateCallback {
        override fun onInserted(position: Int, count: Int) {}
        override fun onRemoved(position: Int, count: Int) {}
        override fun onMoved(fromPosition: Int, toPosition: Int) {}
        override fun onChanged(position: Int, count: Int, payload: Any?) {}
    }

    private fun generateStories(): List<ListStoryItem> {
        val storyList = ArrayList<ListStoryItem>()

        for (i in 0..10) {
            val story = ListStoryItem(
                id = "id",
                photoUrl = "photoUrl",
                createdAt = "createdAt",
                name = "name",
                description = "description",
                lon = 0.0,
                lat = 0.0
            )
            storyList.add(story)
        }

        return storyList
    }
}