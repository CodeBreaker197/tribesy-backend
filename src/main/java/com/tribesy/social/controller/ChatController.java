package com.tribesy.social.controller;

import com.tribesy.social.dto.ChatMessage;
import com.tribesy.social.entity.Message;
import com.tribesy.social.repository.MessageRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.stereotype.Controller;

import java.time.LocalDateTime;

@Controller
@RequiredArgsConstructor
public class ChatController {

    private final MessageRepository messageRepository;

    @MessageMapping("/chat.sendMessage")
    @SendTo("/topic/public")
    public ChatMessage sendMessage(@Payload ChatMessage chatMessage) {
        if (chatMessage.getType() == ChatMessage.MessageType.CHAT) {
            Message message = Message.builder()
                    .sender(chatMessage.getSender())
                    .recipient(chatMessage.getRecipient() != null ? chatMessage.getRecipient() : "ALL")
                    .content(chatMessage.getContent())
                    .createdAt(LocalDateTime.now())
                    .build();

            messageRepository.save(message);
        }

        return chatMessage;
    }
}