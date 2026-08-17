package com.tribesy.social.controller;

import com.tribesy.social.dto.ChatMessage;
import com.tribesy.social.service.MessageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Controller;
import java.security.Principal;

@Slf4j
@Controller
@RequiredArgsConstructor
public class ChatController {

    private final MessageService messageService;

    @MessageMapping("/chat.sendMessage")
    public void sendMessage(@Payload ChatMessage chatMessage, Principal principal) {
        log.info("WS Message received: {}, from user: {}", chatMessage.getContent(),
                principal != null ? principal.getName() : "NULL");

        messageService.processAndSendMessage(chatMessage, principal);
    }
}