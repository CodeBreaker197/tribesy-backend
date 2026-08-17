package com.tribesy.social.service;

import com.tribesy.social.dto.*;
import com.tribesy.social.entity.Chat;
import com.tribesy.social.entity.ChatParticipant;
import com.tribesy.social.entity.ChatType;
import com.tribesy.social.entity.Message;
import com.tribesy.social.entity.User;
import com.tribesy.social.repository.ChatParticipantRepository;
import com.tribesy.social.repository.ChatRepository;
import com.tribesy.social.repository.MessageRepository;
import com.tribesy.social.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.Principal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class MessageService {

    private final MessageRepository messageRepository;
    private final UserRepository userRepository;
    private final ChatRepository chatRepository;
    private final ChatParticipantRepository chatParticipantRepository;
    private final SimpMessagingTemplate messagingTemplate;

    @Transactional
    public void processAndSendMessage(ChatMessage chatMessage, Principal principal) {
        if (principal == null) {
            throw new IllegalStateException("Пользователь не авторизован в WebSocket");
        }

        User sender = userRepository.findByUsername(principal.getName())
                .orElseThrow(() -> new RuntimeException("User not found: " + principal.getName()));

        Message message = Message.builder()
                .chatId(chatMessage.getChatId())
                .senderId(sender.getId())
                .content(chatMessage.getContent())
                .isRead(false)
                .build();

        messageRepository.save(message);

        messagingTemplate.convertAndSend(
                "/topic/chat/" + chatMessage.getChatId(),
                chatMessage
        );
    }

    @Transactional(readOnly = true)
    public List<ChatDto> getMyChats(String username) {
        User currentUser = userRepository.findByUsername(username).orElse(null);
        if (currentUser == null) {
            return Collections.emptyList();
        }

        List<Chat> chats = chatRepository.findAllByUserId(currentUser.getId());
        if (chats.isEmpty()) {
            return Collections.emptyList();
        }

        List<ChatDto> result = new ArrayList<>();

        for (Chat chat : chats) {
            Long chatId = chat.getId();

            Long participantId = getRecipientIdForChat(chatId, currentUser.getId());
            User participant = userRepository.findById(participantId).orElse(currentUser);

            Optional<Message> lastMessageOpt = messageRepository.findFirstByChatIdOrderByCreatedAtDesc(chatId);

            String lastMessageText = lastMessageOpt.map(Message::getContent).orElse("");
            String updatedAtText = lastMessageOpt
                    .map(m -> m.getCreatedAt() != null ? m.getCreatedAt().toString() : "")
                    .orElseGet(() -> chat.getCreatedAt() != null ? chat.getCreatedAt().toString() : "");

            result.add(ChatDto.builder()
                    .id(chatId)
                    .lastMessage(lastMessageText)
                    .updatedAt(updatedAtText)
                    .participant(UserDto.builder()
                            .id(participant.getId())
                            .username(participant.getUsername())
                            .build())
                    .build());
        }

        return result;
    }

    @Transactional
    public MessageDto saveMessageAndReturnDto(SendMessageRequest request, String senderUsername) {
        User sender = userRepository.findByUsername(senderUsername)
                .orElseThrow(() -> new RuntimeException("Sender not found: " + senderUsername));

        User recipient = userRepository.findByUsername(request.getRecipientUsername())
                .orElseThrow(() -> new RuntimeException("Recipient not found: " + request.getRecipientUsername()));

        Chat chat = getOrCreateDirectChat(sender.getId(), recipient.getId());

        Message message = Message.builder()
                .senderId(sender.getId())
                .chatId(chat.getId())
                .content(request.getContent())
                .isRead(false)
                .build();

        Message saved = messageRepository.save(message);

        return MessageDto.builder()
                .id(saved.getId())
                .senderUsername(sender.getUsername())
                .recipientUsername(recipient.getUsername())
                .content(saved.getContent())
                .timestamp(saved.getCreatedAt() != null ? saved.getCreatedAt().toString() : "")
                .build();
    }

    @Transactional(readOnly = true)
    public List<Message> getChatHistory(Long chatId) {
        return messageRepository.findByChatIdOrderByCreatedAtAsc(chatId);
    }

    private Chat getOrCreateDirectChat(Long userId1, Long userId2) {
        return chatRepository.findDirectChatBetween(userId1, userId2, ChatType.DIRECT)
                .orElseGet(() -> {
                    Chat newChat = Chat.builder()
                            .type(ChatType.DIRECT)
                            .createdAt(Instant.now())
                            .build();
                    Chat savedChat = chatRepository.save(newChat);

                    ChatParticipant participant1 = ChatParticipant.builder()
                            .chatId(savedChat.getId())
                            .userId(userId1)
                            .build();

                    ChatParticipant participant2 = ChatParticipant.builder()
                            .chatId(savedChat.getId())
                            .userId(userId2)
                            .build();

                    chatParticipantRepository.save(participant1);
                    chatParticipantRepository.save(participant2);

                    return savedChat;
                });
    }

    private Long getRecipientIdForChat(Long chatId, Long currentUserId) {
        return chatParticipantRepository.findByChatId(chatId).stream()
                .map(ChatParticipant::getUserId)
                .filter(userId -> !userId.equals(currentUserId))
                .findFirst()
                .orElse(currentUserId);
    }
}