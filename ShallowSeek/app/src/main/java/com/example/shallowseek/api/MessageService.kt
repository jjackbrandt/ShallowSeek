package com.example.shallowseek.api

import com.example.shallowseek.data.Message
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path

interface MessageService {
    
    @GET("api/messages")
    suspend fun getMessages(): List<Message>
    
    @POST("api/messages")
    suspend fun createMessage(@Body message: Message): Message
    
    @GET("api/messages/{id}")
    suspend fun getMessage(@Path("id") id: Long): Message
    
    @DELETE("api/messages/{id}")
    suspend fun deleteMessage(@Path("id") id: Long)
}