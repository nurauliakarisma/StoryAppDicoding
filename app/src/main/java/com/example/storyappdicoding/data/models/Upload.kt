package com.example.storyappdicoding.data.models

import com.google.gson.annotations.SerializedName

data class Upload(
    @field:SerializedName("error")
    val error: Boolean? = null,

    @field:SerializedName("message")
    val message: String? = null
)
