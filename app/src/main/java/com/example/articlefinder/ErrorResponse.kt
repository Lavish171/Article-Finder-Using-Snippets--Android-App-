package com.example.articlefinder

import com.google.gson.annotations.SerializedName


class ErrorResponse {
    @SerializedName("message")
    private val message: String? = null

    @SerializedName("error")
    private val error: Error? = null

    fun getMessage(): String? {
        return message
    }

    fun getError(): Error? {
        return error
    }
}