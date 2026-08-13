package com.tribesy.social.controller;

import com.tribesy.social.entity.Message;
import com.tribesy.social.repository.MessageRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/messages")
@RequiredArgsConstructor
public class MessageController {

    private final MessageRepository messageRepository;

    @GetMapping("/history")
    public List<Message> getChatHistory(@RequestParam String user1, @RequestParam String user2) {
        return messageRepository.findBySenderAndRecipientOrSenderAndRecipientOrderByCreatedAtAsc(
                user1, user2, user2, user1
        );
    }
}