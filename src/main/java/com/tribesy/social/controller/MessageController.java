package com.tribesy.social.controller;

import com.tribesy.social.entity.Message;
import com.tribesy.social.service.MessageService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;

@RestController
@RequestMapping("/api/v1/messages")
@RequiredArgsConstructor
public class MessageController {

    private final MessageService messageService;

    @GetMapping("/history/{chatId}")
    public List<Message> getChatHistory(@PathVariable Long chatId, Principal principal) {
        return messageService.getChatHistory(chatId, principal.getName());
    }
}