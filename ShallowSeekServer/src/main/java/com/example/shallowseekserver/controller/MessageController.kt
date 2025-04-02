package com.example.shallowseekserver.controller

import com.example.shallowseekserver.model.Message
import org.springframework.web.bind.annotation.*
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicLong

@RestController
@RequestMapping("/api/messages")
class MessageController {
    
    private val messages = ConcurrentHashMap<Long, Message>()
    private val idGenerator = AtomicLong(1)
    
    @GetMapping
    fun getMessages(): List<Message> {
        return messages.values.sortedBy { it.timestamp }
    }
    
    @PostMapping
    fun createMessage(@RequestBody message: Message): Message {
        val id = idGenerator.getAndIncrement()
        val savedMessage = message.copy(id = id)
        messages[id] = savedMessage
        return savedMessage
    }
    
    @GetMapping("/{id}")
    fun getMessage(@PathVariable id: Long): Message? {
        return messages[id]
    }
    
    @DeleteMapping("/{id}")
    fun deleteMessage(@PathVariable id: Long) {
        messages.remove(id)
    }
}