package com.tribesy.social.controller;

import com.tribesy.social.dto.ChatDto;
import com.tribesy.social.dto.MessageDto;
import com.tribesy.social.dto.SendMessageRequest;
import com.tribesy.social.service.MessageService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;

@RestController
@RequestMapping("/api/v1/chats")
@RequiredArgsConstructor
public class ChatRestController {

    private final MessageService messageService;

    @GetMapping
    public ResponseEntity<List<ChatDto>> getMyChats(Principal principal) {
        List<ChatDto> chats = messageService.getMyChats(principal.getName());
        return ResponseEntity.ok(chats);
    }

    @PostMapping("/send")
    public ResponseEntity<MessageDto> sendMessage(
            @RequestBody SendMessageRequest request,
            Principal principal
    ) {
        MessageDto dto = messageService.saveMessageAndReturnDto(request, principal.getName());
        return ResponseEntity.ok(dto);
    }
}