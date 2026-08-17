package com.tribesy.social.controller;

import com.tribesy.social.dto.ChatMessage;
import com.tribesy.social.entity.Message;
import com.tribesy.social.repository.MessageRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

import java.security.Principal;
import java.time.LocalDateTime;

@Controller
@RequiredArgsConstructor
public class ChatController {

    private final MessageRepository messageRepository;
    private final SimpMessagingTemplate messagingTemplate;

    @MessageMapping("/chat.sendMessage")
    public void sendMessage(@Payload ChatMessage chatMessage, Principal principal) {
        String sender = principal.getName();
        chatMessage.setSender(sender);

        if (chatMessage.getType() == ChatMessage.MessageType.CHAT) {
            Message message = Message.builder()
                    .sender(sender)
                    .recipient(chatMessage.getRecipient())
                    .content(chatMessage.getContent())
                    .createdAt(LocalDateTime.now())
                    .build();

            messageRepository.save(message);

            if (chatMessage.getRecipient() != null && !chatMessage.getRecipient().equals("ALL")) {
                messagingTemplate.convertAndSendToUser(
                        chatMessage.getRecipient(),
                        "/queue/messages",
                        chatMessage
                );
            } else {
                messagingTemplate.convertAndSend("/topic/public", chatMessage);
            }
        }
    }
}