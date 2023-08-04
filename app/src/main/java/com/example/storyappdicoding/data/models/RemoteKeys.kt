package com.example.storyappdicoding.data.models

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "remotekeys")
data class RemoteKeys(
    @PrimaryKey
    val id: String,

    val nextKey: Int?,

    val prevKey: Int?
)