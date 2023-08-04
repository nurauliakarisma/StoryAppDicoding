package com.example.storyappdicoding.data.remote

import androidx.paging.ExperimentalPagingApi
import androidx.paging.LoadType
import androidx.paging.PagingState
import androidx.paging.RemoteMediator
import androidx.room.withTransaction
import com.example.storyappdicoding.data.db.StoryDatabase
import com.example.storyappdicoding.data.models.ListStoryItem
import com.example.storyappdicoding.data.models.RemoteKeys

@OptIn(ExperimentalPagingApi::class)
class RemoteMediator(
    private val accToken: String,
    private val storyDatabase: StoryDatabase,
    private val apiService: ApiService,
) : RemoteMediator<Int, ListStoryItem>() {

    override suspend fun initialize(): InitializeAction {
        return InitializeAction.LAUNCH_INITIAL_REFRESH
    }

    @Suppress("UNCHECKED_CAST")
    override suspend fun load(
        loadType: LoadType,
        state: PagingState<Int, ListStoryItem>
    ): MediatorResult {

        val page = when (loadType) {
            LoadType.APPEND -> {
                val remoteKeys =
                    state.pages.lastOrNull { it.data.isNotEmpty() }?.data?.lastOrNull()
                        .let { storyItem ->
                            storyItem?.let { data ->
                                data.id.let { remoteKeys ->
                                    storyDatabase.remoteKeysDao().getRemoteKeysId(
                                        remoteKeys
                                    )
                                }
                            }
                        }
                val nextKey = remoteKeys?.nextKey ?: return MediatorResult.Success(
                    endOfPaginationReached = remoteKeys != null
                )
                nextKey

            }

            LoadType.PREPEND -> {
                val remoteKeys =
                    state.pages.firstOrNull { it.data.isNotEmpty() }?.data?.firstOrNull()
                        .let { storyItem ->
                            storyItem?.let { data ->
                                data.id.let { remoteKeys ->
                                    storyDatabase.remoteKeysDao().getRemoteKeysId(
                                        remoteKeys
                                    )
                                }
                            }
                        }
                val prevKey =
                    remoteKeys?.prevKey ?: return MediatorResult.Success(
                        endOfPaginationReached = remoteKeys != null
                    )
                prevKey
            }

            LoadType.REFRESH -> INITIAL_PAGE_INDEX
        }

        try {
            val responseData =
                apiService.getStories(
                    token = accToken,
                    page = page,
                    size = state.config.pageSize
                ).listStory

            storyDatabase.withTransaction {
                if (loadType == LoadType.REFRESH) {
                    storyDatabase.remoteKeysDao().deleteRemote()
                    storyDatabase.storyDao().deleteAllStory()
                }

                val prevKey =
                    if (page == INITIAL_PAGE_INDEX) null else page - INITIAL_PAGE_INDEX
                val nextKey = if (responseData!!.isEmpty()) null else page + INITIAL_PAGE_INDEX

                val key = responseData.map {
                    RemoteKeys(
                        id = it!!.id,
                        prevKey = prevKey,
                        nextKey = nextKey
                    )
                }

                storyDatabase.remoteKeysDao().insertAllKeys(key)
                storyDatabase.storyDao().insertStory(responseData as List<ListStoryItem>)
            }

            return MediatorResult.Success(endOfPaginationReached = responseData!!.isEmpty())
        } catch (e: Exception) {
            return MediatorResult.Error(e)
        }
    }

    private companion object {
        const val INITIAL_PAGE_INDEX = 1
    }
}

