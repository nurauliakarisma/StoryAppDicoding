package com.example.storyappdicoding.data.db

import androidx.paging.PagingSource
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.storyappdicoding.data.models.ListStoryItem

@Dao
interface StoryDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertStory(story: List<ListStoryItem>)

    @Query("SELECT * FROM story_item ORDER BY createdAt DESC")
    fun getAllStory(): PagingSource<Int, ListStoryItem>

    @Query("DELETE FROM story_item")
    suspend fun deleteAllStory()
}